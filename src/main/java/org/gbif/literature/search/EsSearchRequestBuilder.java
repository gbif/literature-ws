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

import co.elastic.clients.elasticsearch._types.SortOptions;

import org.gbif.api.model.common.search.FacetedSearchRequest;
import org.gbif.api.model.common.search.SearchConstants;
import org.gbif.api.model.common.search.SearchParameter;
import org.gbif.api.model.literature.LiteratureType;
import org.gbif.api.util.VocabularyUtils;
import org.gbif.api.vocabulary.Country;
import org.gbif.api.vocabulary.Language;
import static org.gbif.api.util.SearchTypeValidator.isNumericRange;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;

import static org.gbif.api.util.SearchTypeValidator.isDateRange;
import static org.gbif.literature.util.EsQueryUtils.LOWER_BOUND_RANGE_PARSER;
import static org.gbif.literature.util.EsQueryUtils.RANGE_SEPARATOR;
import static org.gbif.literature.util.EsQueryUtils.RANGE_WILDCARD;
import static org.gbif.literature.util.EsQueryUtils.UPPER_BOUND_RANGE_PARSER;
import static org.gbif.literature.util.EsQueryUtils.extractFacetLimit;
import static org.gbif.literature.util.EsQueryUtils.extractFacetOffset;

/**
 * Enhanced Elasticsearch search request builder for Elasticsearch 9.
 * Supports multi-select facets, nested field queries, and proper date/range handling.
 */
public class EsSearchRequestBuilder<P extends SearchParameter> {

  private static final int MAX_SIZE_TERMS_AGGS = 1200000;
  private static final String PRE_HL_TAG = "<em class=\"gbifHl\">";
  private static final String POST_HL_TAG = "</em>";

  // Nested fields that require special query handling
  private static final Set<String> NESTED_FIELDS = Set.of("authors", "editors", "translators", "identifiers");

  private final EsFieldMapper<P> esFieldMapper;

  public EsSearchRequestBuilder(EsFieldMapper<P> esFieldMapper) {
    this.esFieldMapper = esFieldMapper;
  }

  /**
   * Builds the main search request.
   */
  public SearchRequest buildSearchRequest(FacetedSearchRequest<P> searchRequest, String index) {
    return buildRequest(searchRequest, index);
  }

  /**
   * Main search request builder with comprehensive filtering and aggregation support.
   */
  private SearchRequest buildRequest(FacetedSearchRequest<P> searchRequest, String index) {
    SearchRequest.Builder builder = new SearchRequest.Builder();

    // Basic request setup
    configureBasicRequest(builder, searchRequest, index);

    // Setup filtering strategy for multi-select facets
    GroupedParams<P> groupedParams = groupParameters(searchRequest);

    // Build the main query
    BoolQuery mainQuery = buildMainQuery(searchRequest, groupedParams);
    builder.query(Query.of(q -> q.bool(mainQuery)));

    // Add post-filter for facet isolation
    addPostFilter(builder, groupedParams);

    // Configure sorting
    configureSorting(builder, searchRequest);

    // Add highlighting if requested
    configureHighlighting(builder, searchRequest);

    // Add aggregations with multi-select support
    addAggregations(builder, searchRequest, groupedParams);

    return builder.build();
  }

  /**
   * Configures basic request parameters.
   */
  private void configureBasicRequest(SearchRequest.Builder builder, FacetedSearchRequest<P> searchRequest, String index) {
    builder.index(index);
    builder.size(searchRequest.getLimit());
    builder.from((int) searchRequest.getOffset());
    builder.trackTotalHits(t -> t.enabled(true));

    // Source filtering
    builder.source(s -> s.filter(f -> f
        .includes(List.of(esFieldMapper.getMappedFields()))
        .excludes(List.of(esFieldMapper.excludeFields()))
    ));
  }

  /**
   * Builds the main bool query combining text search and filters.
   */
  private BoolQuery buildMainQuery(FacetedSearchRequest<P> searchRequest, GroupedParams<P> groupedParams) {
    BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

    // Main text query (must clause)
    addTextQuery(boolQueryBuilder, searchRequest);

    // Add non-facet parameter filters
    addQueryFilters(boolQueryBuilder, groupedParams);

    return boolQueryBuilder.build();
  }

  /**
   * Adds the main text query to the bool query.
   */
  private void addTextQuery(BoolQuery.Builder boolQueryBuilder, FacetedSearchRequest<P> searchRequest) {
    if (searchRequest.getQ() == null ||
        searchRequest.getQ().trim().isEmpty() ||
        searchRequest.getQ().equals(SearchConstants.QUERY_WILDCARD)) {
      boolQueryBuilder.must(q -> q.matchAll(ma -> ma));
    } else {
      boolQueryBuilder.must(esFieldMapper.fullTextQuery(searchRequest.getQ()));
    }
  }

  /**
   * Adds query filters for non-facet parameters.
   */
  private void addQueryFilters(BoolQuery.Builder boolQueryBuilder, GroupedParams<P> groupedParams) {
    if (groupedParams.queryParams != null && !groupedParams.queryParams.isEmpty()) {
      List<Query> queryFilters = groupedParams.queryParams.entrySet().stream()
          .filter(e -> esFieldMapper.get(e.getKey()) != null)
          .flatMap(e -> buildTermQuery(e.getValue(), e.getKey(), esFieldMapper.get(e.getKey())).stream())
          .toList();

      if (!queryFilters.isEmpty()) {
        boolQueryBuilder.filter(queryFilters);
      }
    }
  }

  /**
   * Adds post-filter for facet parameter isolation.
   */
  private void addPostFilter(SearchRequest.Builder builder, GroupedParams<P> groupedParams) {
    if (groupedParams.postFilterParams != null && !groupedParams.postFilterParams.isEmpty()) {
      List<Query> postFilterQueries = groupedParams.postFilterParams.entrySet().stream()
          .flatMap(e -> buildTermQuery(e.getValue(), e.getKey(), esFieldMapper.get(e.getKey())).stream())
          .toList();

      if (!postFilterQueries.isEmpty()) {
        builder.postFilter(q -> q.bool(b -> b.filter(postFilterQueries)));
      }
    }
  }

  /**
   * Configures sorting based on query type.
   */
  private void configureSorting(SearchRequest.Builder builder, FacetedSearchRequest<P> searchRequest) {
    if (searchRequest.getQ() == null ||
        searchRequest.getQ().trim().isEmpty() ||
        searchRequest.getQ().equals(SearchConstants.QUERY_WILDCARD)) {
      // Default sorting for non-text queries
      for (SortOptions sort : esFieldMapper.sorts()) {
        builder.sort(sort);
      }
    } else {
      // Relevance sorting for text queries
      builder.sort(s -> s.score(sc -> sc.order(SortOrder.Desc)));
    }
  }

  /**
   * Configures highlighting if requested.
   */
  private void configureHighlighting(SearchRequest.Builder builder, FacetedSearchRequest<P> searchRequest) {
    if (searchRequest.isHighlight()) {
      builder.highlight(h -> h
          .numberOfFragments(0)
          .preTags(PRE_HL_TAG)
          .postTags(POST_HL_TAG)
          .fields("title", f -> f)
          .fields("abstract", f -> f)
      );
    }
  }

  /**
   * Adds aggregations with multi-select facet support.
   */
  private void addAggregations(SearchRequest.Builder builder, FacetedSearchRequest<P> searchRequest, GroupedParams<P> groupedParams) {
    if (searchRequest.getFacets() == null || searchRequest.getFacets().isEmpty()) {
      return;
    }

    // Create aggregations for each requested facet
    for (P facetParam : searchRequest.getFacets()) {
      String esField = esFieldMapper.get(facetParam);
      if (esField == null) continue;

      addSingleFacetAggregation(builder, searchRequest, groupedParams, facetParam, esField);
    }
  }

  /**
   * adds a single facet aggregation with multi-select logic.
   */
  private void addSingleFacetAggregation(SearchRequest.Builder builder, FacetedSearchRequest<P> searchRequest,
                                        GroupedParams<P> groupedParams, P facetParam, String esField) {

    // Base terms aggregation
    Aggregation termsAgg = createTermsAggregation(searchRequest, facetParam, esField);

    // Handle multi-select facet logic
    if (searchRequest.isFacetMultiSelect() &&
        groupedParams.postFilterParams != null &&
        !groupedParams.postFilterParams.isEmpty()) {

      addMultiSelectFacetAggregation(builder, groupedParams, facetParam, esField, termsAgg);
    } else {
      builder.aggregations(esField, termsAgg);
    }
  }

  /**
   * Creates a terms aggregation for a facet.
   */
  private Aggregation createTermsAggregation(FacetedSearchRequest<P> searchRequest, P facetParam, String esField) {
    int facetOffset = extractFacetOffset(searchRequest, facetParam);
    int facetLimit = extractFacetLimit(searchRequest, facetParam);
    int aggsSize = calculateAggsSize(esField, facetOffset, facetLimit);

    return Aggregation.of(a -> a
        .terms(t -> t
            .field(esField)
            .size(aggsSize)
            .minDocCount(searchRequest.getFacetMinCount() != null ? searchRequest.getFacetMinCount() : 1)
        )
    );
  }

  /**
   * Adds a multi-select facet aggregation with proper filter isolation.
   */
  private void addMultiSelectFacetAggregation(SearchRequest.Builder builder, GroupedParams<P> groupedParams,
                                             P facetParam, String esField, Aggregation termsAgg) {

    // Create filter of all other active facet filters (excluding current facet)
    Map<P, Set<String>> otherFacetFilters = new HashMap<>(groupedParams.postFilterParams);
    otherFacetFilters.remove(facetParam);

    if (!otherFacetFilters.isEmpty()) {
      // Build filter query for other facets
      List<Query> filterQueries = otherFacetFilters.entrySet().stream()
          .flatMap(e -> buildTermQuery(e.getValue(), e.getKey(), esFieldMapper.get(e.getKey())).stream())
          .toList();

      Query filterQuery = Query.of(q -> q.bool(b -> b.filter(filterQueries)));

      // Wrap terms aggregation in filter aggregation
      builder.aggregations(esField, agg -> agg
          .filter(filterQuery)
          .aggregations("inner", termsAgg)
      );
    } else {
      builder.aggregations(esField, termsAgg);
    }
  }

  /**
   * Calculates appropriate aggregation size with limits.
   */
  private int calculateAggsSize(String esField, int facetOffset, int facetLimit) {
    int maxCardinality = esFieldMapper.getCardinality(esField) != null ?
        esFieldMapper.getCardinality(esField) : Integer.MAX_VALUE;

    int limit = Math.min(facetOffset + facetLimit, maxCardinality);

    if (limit > MAX_SIZE_TERMS_AGGS) {
      throw new IllegalArgumentException(
          "Facets paging is only supported up to " + MAX_SIZE_TERMS_AGGS + " elements");
    }
    return limit;
  }

  /**
   * Builds a get-by-id request.
   */
  public SearchRequest buildGetRequest(Object identifier, String index) {
    return new SearchRequest.Builder()
        .index(index)
        .source(s -> s
            .filter(f -> f
                .includes(List.of(esFieldMapper.getMappedFields()))
                .excludes(List.of(esFieldMapper.excludeFields()))
            )
        )
        .query(Query.of(q -> q.match(MatchQuery.of(m -> m
            .field("id")
            .query(FieldValue.of(identifier.toString()))
        ))))
        .build();
  }

  /**
   * Builds queries for a parameter's values, handling ranges and nested fields.
   */
  protected List<Query> buildTermQuery(Collection<String> values, P param, String esField) {
    List<Query> queries = new ArrayList<>();
    List<String> parsedValues = new ArrayList<>();

    for (String value : values) {
      if (isNumericRange(value) || (esFieldMapper.isDateField(esField) && isDateRange(value))) {
        queries.add(buildRangeQuery(esField, value));
        continue;
      }

      String parsedValue = parseParamValue(value, param);
      if (parsedValue != null) {
        parsedValues.add(parsedValue);
      }
    }

    if (!parsedValues.isEmpty()) {
      if (isNestedField(esField)) {
        queries.add(buildNestedQuery(esField, parsedValues));
      } else {
        queries.add(buildTermOrTermsQuery(esField, parsedValues));
      }
    }

    return queries;
  }

  /**
   * Checks if a field is nested and requires special query handling.
   */
  private boolean isNestedField(String esField) {
    return NESTED_FIELDS.stream().anyMatch(esField::startsWith);
  }

  /**
   * Builds a nested query for nested field types.
   */
  private Query buildNestedQuery(String esField, List<String> values) {
    String[] fieldParts = esField.split("\\.", 2);
    if (fieldParts.length < 2) {
      return buildTermOrTermsQuery(esField, values);
    }

    String nestedPath = fieldParts[0];

    List<FieldValue> fieldValues = values.stream()
        .map(FieldValue::of)
        .toList();

    return Query.of(q -> q.nested(n -> n
        .path(nestedPath)
        .query(Query.of(nq -> nq.terms(t -> t
            .field(esField)
            .terms(ts -> ts.value(fieldValues))
        )))
    ));
  }

  /**
   * Builds a term or terms query for regular fields.
   */
  private Query buildTermOrTermsQuery(String esField, List<String> values) {
    if (values.size() == 1) {
      return Query.of(q -> q.term(t -> t.field(esField).value(FieldValue.of(values.get(0)))));
    } else {
      List<FieldValue> fieldValues = values.stream()
          .map(FieldValue::of)
          .toList();
      return Query.of(q -> q.terms(t -> t.field(esField).terms(ts -> ts.value(fieldValues))));
    }
  }

  /**
   * Builds range queries for numeric and date fields.
   */
  private Query buildRangeQuery(String esField, String value) {
    String[] values = value.split(RANGE_SEPARATOR);

    if (values.length != 2) {
      throw new IllegalArgumentException("Invalid range format: " + value);
    }

    if (esFieldMapper.isDateField(esField)) {
      return buildDateRangeQuery(esField, values);
    } else {
      return buildNumericRangeQuery(esField, values);
    }
  }

  /**
   * Builds a date range query.
   */
  private Query buildDateRangeQuery(String esField, String[] values) {
    LocalDateTime lower = LOWER_BOUND_RANGE_PARSER.apply(values[0]);
    LocalDateTime upper = UPPER_BOUND_RANGE_PARSER.apply(values[1]);

    return Query.of(q -> q.range(r -> r.date(d -> {
      d.field(esField);
      if (lower != null) d.gte(lower.toString());
      if (upper != null) d.lte(upper.toString());
      return d;
    })));
  }

  /**
   * Builds a numeric range query.
   */
  private Query buildNumericRangeQuery(String esField, String[] values) {
    // Try to parse as numbers first
    try {
      Double lowerBound = RANGE_WILDCARD.equals(values[0]) ? null : Double.parseDouble(values[0]);
      Double upperBound = RANGE_WILDCARD.equals(values[1]) ? null : Double.parseDouble(values[1]);

      return Query.of(q -> q.range(r -> r.number(n -> {
        n.field(esField);
        if (lowerBound != null) n.gte(lowerBound);
        if (upperBound != null) n.lte(upperBound);
        return n;
      })));
    } catch (NumberFormatException e) {
      // Fall back to term range for non-numeric values
      return Query.of(q -> q.range(r -> r.term(t -> {
        t.field(esField);
        if (!RANGE_WILDCARD.equals(values[0])) t.gte(values[0]);
        if (!RANGE_WILDCARD.equals(values[1])) t.lte(values[1]);
        return t;
      })));
    }
  }

  /**
   * Parses parameter values based on their type.
   */
  private String parseParamValue(String value, P parameter) {
    if (Enum.class.isAssignableFrom(parameter.type())) {
      return parseEnumValue(value, parameter);
    }

    if (Boolean.class.isAssignableFrom(parameter.type())) {
      return value.toLowerCase();
    }

    return value;
  }

  /**
   * Parses enum parameter values with special handling for different enum types.
   */
  private String parseEnumValue(String value, P parameter) {
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

  /**
   * Groups parameters into query filters and post filters for multi-select facet support.
   */
  private GroupedParams<P> groupParameters(FacetedSearchRequest<P> searchRequest) {
    GroupedParams<P> groupedParams = new GroupedParams<>();

    if (!searchRequest.isFacetMultiSelect() ||
        searchRequest.getFacets() == null ||
        searchRequest.getFacets().isEmpty()) {
      // No multi-select, all parameters go to main query
      groupedParams.queryParams = searchRequest.getParameters();
      return groupedParams;
    }

    // Split parameters between query and post-filter
    groupedParams.queryParams = new HashMap<>();
    groupedParams.postFilterParams = new HashMap<>();

    searchRequest.getParameters().forEach((k, v) -> {
      if (searchRequest.getFacets().contains(k)) {
        groupedParams.postFilterParams.put(k, v);
      } else {
        groupedParams.queryParams.put(k, v);
      }
    });

    return groupedParams;
  }

  /**
   * Helper class to hold grouped parameters for multi-select facet logic.
   */
  static class GroupedParams<P extends SearchParameter> {
    Map<P, Set<String>> postFilterParams = new HashMap<>();
    Map<P, Set<String>> queryParams = new HashMap<>();
  }
}
