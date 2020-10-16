package org.gbif.literature.search;

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.gbif.api.model.common.search.Facet;
import org.gbif.api.model.common.search.FacetedSearchRequest;
import org.gbif.api.model.common.search.SearchParameter;
import org.gbif.api.model.common.search.SearchRequest;
import org.gbif.api.model.common.search.SearchResponse;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.gbif.literature.util.EsQueryUtils.extractFacetLimit;
import static org.gbif.literature.util.EsQueryUtils.extractFacetOffset;

public class EsResponseParser<T, S, P extends SearchParameter> {

  private final EsFieldMapper<P> fieldParameterMapper;
  private final SearchResultConverter<T, S> searchResultConverter;

  public EsResponseParser(
      SearchResultConverter<T, S> searchResultConverter, EsFieldMapper<P> fieldParameterMapper) {
    this.searchResultConverter = searchResultConverter;
    this.fieldParameterMapper = fieldParameterMapper;
  }

  public SearchResponse<T, P> buildSearchResponse(
      org.elasticsearch.action.search.SearchResponse esResponse, SearchRequest<P> request) {
    return buildSearchResponse(esResponse, request, searchResultConverter::toSearchResult);
  }

  public <R> SearchResponse<R, P> buildSearchResponse(
      org.elasticsearch.action.search.SearchResponse esResponse,
      SearchRequest<P> request,
      Function<SearchHit, R> mapper) {
    SearchResponse<R, P> response = new SearchResponse<>(request);
    response.setCount(esResponse.getHits().getTotalHits());
    parseHits(esResponse, mapper).ifPresent(response::setResults);
    if (request instanceof FacetedSearchRequest) {
      parseFacets(esResponse, (FacetedSearchRequest<P>) request).ifPresent(response::setFacets);
    }

    return response;
  }

  private <R> Optional<List<R>> parseHits(
      org.elasticsearch.action.search.SearchResponse esResponse, Function<SearchHit, R> mapper) {
    if (esResponse.getHits() == null
        || esResponse.getHits().getHits() == null
        || esResponse.getHits().getHits().length == 0) {
      return Optional.empty();
    }

    return Optional.of(
        Stream.of(esResponse.getHits().getHits()).map(mapper).collect(Collectors.toList()));
  }

  private Optional<List<Facet<P>>> parseFacets(
      org.elasticsearch.action.search.SearchResponse esResponse, FacetedSearchRequest<P> request) {
    return Optional.ofNullable(esResponse.getAggregations())
        .map(
            aggregations ->
                aggregations.asList().stream()
                    .map(
                        aggs -> {
                          // get buckets
                          List<? extends Terms.Bucket> buckets = getBuckets(aggs);

                          // get facet of the agg
                          P facet = fieldParameterMapper.get(aggs.getName());

                          // check for paging in facets
                          long facetOffset = extractFacetOffset(request, facet);
                          long facetLimit = extractFacetLimit(request, facet);

                          List<Facet.Count> counts =
                              buckets.stream()
                                  .skip(facetOffset)
                                  .limit(facetOffset + facetLimit)
                                  .map(b -> new Facet.Count(b.getKeyAsString(), b.getDocCount()))
                                  .collect(Collectors.toList());

                          return new Facet<>(facet, counts);
                        })
                    .collect(Collectors.toList()));
  }

  private List<? extends Terms.Bucket> getBuckets(Aggregation aggregation) {
    if (aggregation instanceof Terms) {
      return ((Terms) aggregation).getBuckets();
    } else if (aggregation instanceof Filter) {
      return ((Filter) aggregation)
          .getAggregations().asList().stream()
          .flatMap(agg -> ((Terms) agg).getBuckets().stream())
          .collect(Collectors.toList());
    } else {
      throw new IllegalArgumentException(aggregation.getClass() + " aggregation not supported");
    }
  }
}

