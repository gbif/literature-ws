package org.gbif.literature.search;

import org.elasticsearch.search.SearchHit;
import org.gbif.api.model.common.search.SearchParameter;
import org.gbif.api.model.common.search.SearchRequest;
import org.gbif.api.model.common.search.SearchResponse;

import java.util.function.Function;

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
    return null;
  }
}

