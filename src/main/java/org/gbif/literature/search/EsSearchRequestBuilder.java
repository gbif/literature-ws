/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.literature.search;

import org.gbif.api.model.common.search.FacetedSearchRequest;
import org.gbif.api.model.common.search.SearchConstants;
import org.gbif.api.model.common.search.SearchParameter;
import org.gbif.api.model.literature.LiteratureType;
import org.gbif.api.util.VocabularyUtils;
import org.gbif.api.vocabulary.Country;
import org.gbif.api.vocabulary.Language;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.gbif.api.util.SearchTypeValidator.isDateRange;
import static org.gbif.api.util.SearchTypeValidator.isNumericRange;
import static org.gbif.literature.util.EsQueryUtils.LOWER_BOUND_RANGE_PARSER;
import static org.gbif.literature.util.EsQueryUtils.RANGE_SEPARATOR;
import static org.gbif.literature.util.EsQueryUtils.RANGE_WILDCARD;
import static org.gbif.literature.util.EsQueryUtils.UPPER_BOUND_RANGE_PARSER;
import static org.gbif.literature.util.EsQueryUtils.extractFacetLimit;
import static org.gbif.literature.util.EsQueryUtils.extractFacetOffset;

public abstract class EsSearchRequestBuilder<P extends SearchParameter> {

  private static final int MAX_SIZE_TERMS_AGGS = 1200000;
  private static final String PRE_HL_TAG = "<em class=\"gbifHl\">";
  private static final String POST_HL_TAG = "</em>";

  private final EsFieldMapper<P> esFieldMapper;

  private final HighlightBuilder highlightBuilder =
      new HighlightBuilder()
          .forceSource(true)
          .preTags(PRE_HL_TAG)
          .postTags(POST_HL_TAG)
          .encoder("html")
          .highlighterType("unified")
          .requireFieldMatch(false)
          .numOfFragments(0);

  public EsSearchRequestBuilder(EsFieldMapper<P> esFieldMapper) {
    this.esFieldMapper = esFieldMapper;
  }

  public SearchRequest buildSearchRequest(
      FacetedSearchRequest<P> searchRequest, boolean facetsEnabled, String index) {

    SearchRequest esSearchRequest = new SearchRequest();
    esSearchRequest.indices(index);

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.trackTotalHits(true);
    esSearchRequest.source(searchSourceBuilder);
    searchSourceBuilder.fetchSource(esFieldMapper.getMappedFields(), esFieldMapper.excludeFields());

    // size and offset
    searchSourceBuilder.size(searchRequest.getLimit());
    searchSourceBuilder.from((int) searchRequest.getOffset());

    // sort
    if (Strings.isNullOrEmpty(searchRequest.getQ())) {
      for (SortBuilder<?> sb : esFieldMapper.sorts()) {
        searchSourceBuilder.sort(sb);
      }
    } else {
      searchSourceBuilder.sort(SortBuilders.scoreSort());
      if (searchRequest.isHighlight()) {
        searchSourceBuilder.highlighter(highlightBuilder);
      }
    }

    // group params
    GroupedParams<P> groupedParams = groupParameters(searchRequest);

    // add query
    if (SearchConstants.QUERY_WILDCARD.equals(searchRequest.getQ())) {
      // search all
      searchSourceBuilder.query(matchAllQuery());
    } else {
      buildQuery(groupedParams.queryParams, searchRequest.getQ())
          .ifPresent(searchSourceBuilder::query);
    }

    // add aggs
    buildAggs(searchRequest, groupedParams.postFilterParams, facetsEnabled)
        .ifPresent(aggsList -> aggsList.forEach(searchSourceBuilder::aggregation));

    // post-filter
    buildPostFilter(groupedParams.postFilterParams).ifPresent(searchSourceBuilder::postFilter);

    return esSearchRequest;
  }

  public SearchRequest buildGetRequest(Object identifier, String index) {
    SearchRequest esSearchRequest = new SearchRequest();
    esSearchRequest.indices(index);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    esSearchRequest.source(searchSourceBuilder);
    searchSourceBuilder.fetchSource(esFieldMapper.getMappedFields(), esFieldMapper.excludeFields());
    searchSourceBuilder.query(matchQuery("id", identifier.toString()));

    return esSearchRequest;
  }

  private Optional<QueryBuilder> buildPostFilter(Map<P, Set<String>> postFilterParams) {
    if (postFilterParams == null || postFilterParams.isEmpty()) {
      return Optional.empty();
    }

    BoolQueryBuilder bool = boolQuery();
    bool.filter()
        .addAll(
            postFilterParams.entrySet().stream()
                .flatMap(
                    e ->
                        buildTermQuery(e.getValue(), e.getKey(), esFieldMapper.get(e.getKey()))
                            .stream())
                .collect(Collectors.toList()));

    return Optional.of(bool);
  }

  private Optional<List<AggregationBuilder>> buildAggs(
      FacetedSearchRequest<P> searchRequest,
      Map<P, Set<String>> postFilterParams,
      boolean facetsEnabled) {
    if (!facetsEnabled
        || searchRequest.getFacets() == null
        || searchRequest.getFacets().isEmpty()) {
      return Optional.empty();
    }

    if (searchRequest.isMultiSelectFacets()
        && postFilterParams != null
        && !postFilterParams.isEmpty()) {
      return Optional.of(buildFacetsMultiselect(searchRequest, postFilterParams));
    }

    return Optional.of(buildFacets(searchRequest));
  }

  private List<AggregationBuilder> buildFacetsMultiselect(
      FacetedSearchRequest<P> searchRequest, Map<P, Set<String>> postFilterParams) {

    if (searchRequest.getFacets().size() == 1) {
      // same case as normal facets
      return buildFacets(searchRequest);
    }

    return searchRequest.getFacets().stream()
        .filter(p -> esFieldMapper.get(p) != null)
        .map(
            facetParam -> {
              // build filter aggs
              BoolQueryBuilder bool = boolQuery();
              bool.filter()
                  .addAll(
                      postFilterParams.entrySet().stream()
                          .filter(entry -> entry.getKey() != facetParam)
                          .flatMap(
                              e ->
                                  buildTermQuery(
                                      e.getValue(), e.getKey(), esFieldMapper.get(e.getKey()))
                                      .stream())
                          .collect(Collectors.toList()));

              // add filter to the aggs
              String esField = esFieldMapper.get(facetParam);
              FilterAggregationBuilder filterAggs = AggregationBuilders.filter(esField, bool);

              // build terms aggs and add it to the filter aggs
              TermsAggregationBuilder termsAggs =
                  buildTermsAggs(
                      "filtered_" + esField,
                      esField,
                      extractFacetOffset(searchRequest, facetParam),
                      extractFacetLimit(searchRequest, facetParam),
                      searchRequest.getFacetMinCount());
              filterAggs.subAggregation(termsAggs);

              return filterAggs;
            })
        .collect(Collectors.toList());
  }

  private List<AggregationBuilder> buildFacets(FacetedSearchRequest<P> searchRequest) {
    return searchRequest.getFacets().stream()
        .filter(p -> esFieldMapper.get(p) != null)
        .map(
            facetParam -> {
              String esField = esFieldMapper.get(facetParam);
              return buildTermsAggs(
                  esField,
                  esField,
                  extractFacetOffset(searchRequest, facetParam),
                  extractFacetLimit(searchRequest, facetParam),
                  searchRequest.getFacetMinCount());
            })
        .collect(Collectors.toList());
  }

  private TermsAggregationBuilder buildTermsAggs(
      String aggsName, String esField, int facetOffset, int facetLimit, Integer minCount) {
    // build aggs for the field
    TermsAggregationBuilder termsAggsBuilder = AggregationBuilders.terms(aggsName).field(esField);

    // min count
    Optional.ofNullable(minCount).ifPresent(termsAggsBuilder::minDocCount);

    // aggs size
    int size = calculateAggsSize(esField, facetOffset, facetLimit);
    termsAggsBuilder.size(size);

    // aggs shard size
    /*
    termsAggsBuilder.shardSize(
        Optional.ofNullable(esFieldMapper.getCardinality(esField))
            .orElse(DEFAULT_SHARD_SIZE.applyAsInt(size)));
    */
    return termsAggsBuilder;
  }

  private int calculateAggsSize(String esField, int facetOffset, int facetLimit) {
    int maxCardinality =
        Optional.ofNullable(esFieldMapper.getCardinality(esField)).orElse(Integer.MAX_VALUE);

    // the limit is bounded by the max cardinality of the field
    int limit = Math.min(facetOffset + facetLimit, maxCardinality);

    // we set a maximum limit for performance reasons
    if (limit > MAX_SIZE_TERMS_AGGS) {
      throw new IllegalArgumentException(
          "Facets paging is only supported up to " + MAX_SIZE_TERMS_AGGS + " elements");
    }
    return limit;
  }

  private Optional<QueryBuilder> buildQuery(Map<P, Set<String>> params, String qParam) {
    // create bool node
    BoolQueryBuilder bool = boolQuery();

    // adding full text search parameter
    if (StringUtils.isNotBlank(qParam)) {
      bool.must(esFieldMapper.fullTextQuery(qParam));
    }

    // adding specific stuff (e.g. DOI search)
    buildSpecificQuery(bool, params);

    if (params != null && !params.isEmpty()) {
      // adding term queries to bool
      bool.filter()
          .addAll(
              params.entrySet().stream()
                  .filter(e -> Objects.nonNull(esFieldMapper.get(e.getKey())))
                  .flatMap(
                      e ->
                          buildTermQuery(e.getValue(), e.getKey(), esFieldMapper.get(e.getKey()))
                              .stream())
                  .collect(Collectors.toList()));
    }

    return bool.must().isEmpty() && bool.filter().isEmpty() ? Optional.empty() : Optional.of(bool);
  }

  protected abstract void buildSpecificQuery(
      BoolQueryBuilder queryBuilder, Map<P, Set<String>> params);

  private List<QueryBuilder> buildTermQuery(Collection<String> values, P param, String esField) {
    List<QueryBuilder> queries = new ArrayList<>();

    // collect queries for each value
    List<String> parsedValues = new ArrayList<>();
    for (String value : values) {
      if (isNumericRange(value) || (esFieldMapper.isDateField(esField) && isDateRange(value))) {
        queries.add(buildRangeQuery(esField, value));
        continue;
      }

      parsedValues.add(parseParamValue(value, param));
    }

    if (parsedValues.size() == 1) {
      // single term
      queries.add(termQuery(esField, parsedValues.get(0)));
    } else if (parsedValues.size() > 1) {
      // multi term query
      queries.add(termsQuery(esField, parsedValues));
    }

    return queries;
  }

  private RangeQueryBuilder buildRangeQuery(String esField, String value) {
    RangeQueryBuilder builder = rangeQuery(esField);

    if (esFieldMapper.isDateField(esField)) {
      String[] values = value.split(RANGE_SEPARATOR);

      LocalDateTime lowerBound = LOWER_BOUND_RANGE_PARSER.apply(values[0]);
      if (lowerBound != null) {
        builder.gte(lowerBound);
      }

      LocalDateTime upperBound = UPPER_BOUND_RANGE_PARSER.apply(values[1]);
      if (upperBound != null) {
        builder.lte(upperBound);
      }
    } else {
      String[] values = value.split(RANGE_SEPARATOR);
      if (!RANGE_WILDCARD.equals(values[0])) {
        builder.gte(values[0]);
      }
      if (!RANGE_WILDCARD.equals(values[1])) {
        builder.lte(values[1]);
      }
    }

    return builder;
  }

  private String parseParamValue(String value, P parameter) {
    if (Enum.class.isAssignableFrom(parameter.type())) {
      if (Country.class.isAssignableFrom(parameter.type())) {
        return VocabularyUtils.lookup(value, Country.class)
            .map(Country::getIso2LetterCode)
            .orElse(value);
      } else if (LiteratureType.class.isAssignableFrom(parameter.type())) {
        return VocabularyUtils.lookup(value, LiteratureType.class)
            .map(Enum::name)
            .map(String::toLowerCase)
            .orElse(value);
      } else if (Language.class.isAssignableFrom(parameter.type())) {
        return VocabularyUtils.lookup(value, Language.class)
            .map(Language::getIso3LetterCode)
            .orElse(value);
      } else {
        return VocabularyUtils.lookup(value, (Class<Enum<?>>) parameter.type())
            .map(Enum::name)
            .orElse(null);
      }
    }

    if (Boolean.class.isAssignableFrom(parameter.type())) {
      return value.toLowerCase();
    }

    return value;
  }

  GroupedParams<P> groupParameters(FacetedSearchRequest<P> searchRequest) {
    GroupedParams<P> groupedParams = new GroupedParams<>();

    if (!searchRequest.isMultiSelectFacets()
        || searchRequest.getFacets() == null
        || searchRequest.getFacets().isEmpty()) {
      groupedParams.queryParams = searchRequest.getParameters();
      return groupedParams;
    }

    groupedParams.queryParams = new HashMap<>();
    groupedParams.postFilterParams = new HashMap<>();

    searchRequest
        .getParameters()
        .forEach(
            (k, v) -> {
              if (searchRequest.getFacets().contains(k)) {
                groupedParams.postFilterParams.put(k, v);
              } else {
                groupedParams.queryParams.put(k, v);
              }
            });

    return groupedParams;
  }

  static class GroupedParams<P extends SearchParameter> {
    Map<P, Set<String>> postFilterParams;
    Map<P, Set<String>> queryParams;
  }
}
