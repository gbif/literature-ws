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

import org.gbif.api.model.common.search.SearchParameter;
import org.gbif.api.model.common.search.SearchRequest;
import org.gbif.api.model.common.search.SearchResponse;

import java.util.function.Function;

import org.elasticsearch.search.SearchHit;

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
