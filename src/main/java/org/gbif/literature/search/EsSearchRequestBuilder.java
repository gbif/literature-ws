/*
 * Copyright 2020 Global Biodiversity Information Facility (GBIF)
 *
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
import org.gbif.api.util.VocabularyUtils;
import org.gbif.api.vocabulary.Country;
import org.gbif.common.shaded.com.google.common.annotations.VisibleForTesting;

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
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import static org.gbif.api.util.SearchTypeValidator.isRange;
import static org.gbif.literature.util.EsQueryUtils.LOWER_BOUND_RANGE_PARSER;
import static org.gbif.literature.util.EsQueryUtils.RANGE_SEPARATOR;
import static org.gbif.literature.util.EsQueryUtils.RANGE_WILDCARD;
import static org.gbif.literature.util.EsQueryUtils.UPPER_BOUND_RANGE_PARSER;

public class EsSearchRequestBuilder<P extends SearchParameter> {

  private final EsFieldMapper<P> esFieldMapper;

  public EsSearchRequestBuilder(EsFieldMapper<P> esFieldMapper) {
    this.esFieldMapper = esFieldMapper;
  }

  public SearchRequest buildSearchRequest(
      FacetedSearchRequest<P> searchRequest, boolean facetsEnabled, String index) {

    SearchRequest esSearchRequest = new SearchRequest();
    esSearchRequest.indices(index);

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    esSearchRequest.source(searchSourceBuilder);
    searchSourceBuilder.fetchSource(esFieldMapper.getMappedFields(), esFieldMapper.excludeFields());

    // size and offset
    searchSourceBuilder.size(searchRequest.getLimit());
    searchSourceBuilder.from((int) searchRequest.getOffset());

    // sort
    // TODO: 16/10/2020

    // group params
    GroupedParams<P> groupedParams = groupParameters(searchRequest);

    // add query
    if (SearchConstants.QUERY_WILDCARD.equals(searchRequest.getQ())) {
      // search all
      searchSourceBuilder.query(QueryBuilders.matchAllQuery());
    } else {
      buildQuery(groupedParams.queryParams, searchRequest.getQ())
          .ifPresent(searchSourceBuilder::query);
    }

    return esSearchRequest;
  }

  private Optional<QueryBuilder> buildQuery(Map<P, Set<String>> params, String qParam) {
    // create bool node
    BoolQueryBuilder bool = QueryBuilders.boolQuery();

    // adding full text search parameter
    if (StringUtils.isNotBlank(qParam)) {
      bool.must(esFieldMapper.fullTextQuery(qParam));
    }

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

  private List<QueryBuilder> buildTermQuery(Collection<String> values, P param, String esField) {
    List<QueryBuilder> queries = new ArrayList<>();

    // collect queries for each value
    List<String> parsedValues = new ArrayList<>();
    for (String value : values) {
      if (isRange(value)) {
        queries.add(buildRangeQuery(esField, value));
        continue;
      }

      parsedValues.add(parseParamValue(value, param));
    }

    if (parsedValues.size() == 1) {
      // single term
      queries.add(QueryBuilders.termQuery(esField, parsedValues.get(0)));
    } else if (parsedValues.size() > 1) {
      // multi term query
      queries.add(QueryBuilders.termsQuery(esField, parsedValues));
    }

    return queries;
  }

  private RangeQueryBuilder buildRangeQuery(String esField, String value) {
    RangeQueryBuilder builder = QueryBuilders.rangeQuery(esField);

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
      if (!Country.class.isAssignableFrom(parameter.type())) {
        return VocabularyUtils.lookup(value, (Class<Enum<?>>) parameter.type())
            .map(Enum::name)
            .orElse(null);
      } else {
        return VocabularyUtils.lookup(value, Country.class)
            .map(Country::getIso2LetterCode)
            .orElse(value);
      }
    }

    if (Boolean.class.isAssignableFrom(parameter.type())) {
      return value.toLowerCase();
    }
    return value;
  }

  @VisibleForTesting
  GroupedParams<P> groupParameters(FacetedSearchRequest<P> searchRequest) {
    GroupedParams<P> groupedParams = new GroupedParams<P>();

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

  @VisibleForTesting
  static class GroupedParams<P extends SearchParameter> {
    Map<P, Set<String>> postFilterParams;
    Map<P, Set<String>> queryParams;
  }
}
