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

import org.gbif.api.model.common.search.Facet;
import org.gbif.api.model.common.search.FacetedSearchRequest;
import org.gbif.api.model.common.search.SearchParameter;
import org.gbif.api.model.common.search.SearchResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.FilterAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;

import static org.gbif.literature.util.EsQueryUtils.extractFacetOffset;

/**
 * Converts ES search responses into SearchResponse objects.
 */
public class EsResponseParser<T, P extends SearchParameter> {

  private final SearchResultConverter<T> searchResultConverter;
  private final EsFieldMapper<P> esFieldMapper;

  public EsResponseParser(SearchResultConverter<T> searchResultConverter, EsFieldMapper<P> esFieldMapper) {
    this.searchResultConverter = searchResultConverter;
    this.esFieldMapper = esFieldMapper;
  }

  /**
   * Translates the ES response into the common search response format.
   */
  public SearchResponse<T, P> buildSearchResponse(
      co.elastic.clients.elasticsearch.core.SearchResponse<Object> esResponse,
      FacetedSearchRequest<P> searchRequest) {

    SearchResponse<T, P> response = new SearchResponse<>(searchRequest);
    response.setResults(extractResults(esResponse));
    response.setCount(esResponse.hits().total().value());

    if (searchRequest.getFacets() != null && !searchRequest.getFacets().isEmpty() &&
        esResponse.aggregations() != null && !esResponse.aggregations().isEmpty()) {
      List<Facet<P>> facets = extractFacets(esResponse.aggregations(), searchRequest);
      response.setFacets(facets);
    }

    return response;
  }

  /**
   * Builds a response for get-by-id requests.
   */
  public Optional<T> buildGetResponse(co.elastic.clients.elasticsearch.core.SearchResponse<Object> esResponse) {
    List<T> results = extractResults(esResponse);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }

  /**
   * Extracts the results from search hits.
   */
  private List<T> extractResults(co.elastic.clients.elasticsearch.core.SearchResponse<Object> esResponse) {
    return esResponse.hits().hits().stream()
        .map(searchResultConverter::toResult)
        .toList();
  }

  /**
   * Extracts facets from ES aggregations.
   */
  private List<Facet<P>> extractFacets(Map<String, Aggregate> aggregations, FacetedSearchRequest<P> searchRequest) {
    return searchRequest.getFacets().stream()
        .map(facetParam -> {
          String esField = esFieldMapper.get(facetParam);
          if (esField == null) {
            return new Facet<>(facetParam);
          }

          Aggregate agg = aggregations.get(esField);

          if (agg != null) {
            return extractFacet(agg, facetParam, searchRequest);
          }
          return new Facet<>(facetParam);
        })
        .toList();
  }

  /**
   * Extracts a single facet from an ES aggregate.
   */
  private Facet<P> extractFacet(Aggregate aggregate, P facetParam, FacetedSearchRequest<P> searchRequest) {
    List<Facet.Count> counts = null;

    // Handle filter aggregations, which contain a nested "inner" terms aggregation
    // for multi-select facet logic.
    if (aggregate.isFilter()) {
      FilterAggregate filterAgg = aggregate.filter();
      if (filterAgg.aggregations() != null && filterAgg.aggregations().containsKey("inner")) {
        Aggregate innerAgg = filterAgg.aggregations().get("inner");
        if (innerAgg.isSterms()) {
          counts = extractStringTermsIntoCounts(innerAgg.sterms(), facetParam, searchRequest);
        } else if (innerAgg.isLterms()) {
          counts = extractLongTermsIntoCounts(innerAgg.lterms(), facetParam, searchRequest);
        }
      }
    }
    // Handle direct terms aggregations
    else if (aggregate.isSterms()) {
      counts = extractStringTermsIntoCounts(aggregate.sterms(), facetParam, searchRequest);
    }
    // Handle long terms aggregations (for numeric fields like 'year')
    else if (aggregate.isLterms()) {
      counts = extractLongTermsIntoCounts(aggregate.lterms(), facetParam, searchRequest);
    }

    return counts != null ? new Facet<>(facetParam, counts) : new Facet<>(facetParam);
  }

  /**
   * Extracts terms from a StringTermsAggregate into a list of Facet.Count objects.
   */
  private List<Facet.Count> extractStringTermsIntoCounts(StringTermsAggregate termsAgg, P facetParam, FacetedSearchRequest<P> searchRequest) {
    long offset = extractFacetOffset(searchRequest, facetParam);
    return termsAgg.buckets().array().stream()
        .skip(offset)
        .map(bucket -> {
          String key = bucket.key().stringValue();
          return new Facet.Count(key, bucket.docCount());
        })
        .toList();
  }

  /**
   * Extracts terms from a LongTermsAggregate (for numeric fields) into a list of Facet.Count objects.
   */
  private List<Facet.Count> extractLongTermsIntoCounts(LongTermsAggregate termsAgg, P facetParam, FacetedSearchRequest<P> searchRequest) {
    long offset = extractFacetOffset(searchRequest, facetParam);
    return termsAgg.buckets().array().stream()
        .skip(offset)
        .map(bucket -> {
          String key = String.valueOf(bucket.key());
          return new Facet.Count(key, bucket.docCount());
        })
        .toList();
  }
}
