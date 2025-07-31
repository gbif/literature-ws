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

import org.gbif.api.model.literature.search.LiteratureSearchParameter;
import org.gbif.api.model.literature.search.LiteratureSearchRequest;
import org.gbif.api.model.literature.search.LiteratureSearchResult;
import org.gbif.literature.config.EsClientConfigProperties;
import org.gbif.literature.config.EsConfig;

import java.io.IOException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;

@Service
public class LiteratureSearchServiceEs implements LiteratureSearchService {

  private ElasticsearchClient elasticsearchClient;
  private final EsResponseParser<LiteratureSearchResult, LiteratureSearchParameter>
      esResponseParser;
  private final EsSearchRequestBuilder<LiteratureSearchParameter> esSearchRequestBuilder;
  private final String index;
  private final int maxResultWindow;

  private final EsConfig esConfig;

  public LiteratureSearchServiceEs(
      EsClientConfigProperties esClientConfigProperties,
      ElasticsearchClient elasticsearchClient,
      SearchResultConverter<LiteratureSearchResult> searchResultConverter,
      EsSearchRequestBuilder<LiteratureSearchParameter> esSearchRequestBuilder,
      EsFieldMapper<LiteratureSearchParameter> esFieldMapper,
      EsConfig esConfig) {
    this.index = esClientConfigProperties.getIndex();
    this.maxResultWindow = esClientConfigProperties.getMaxResultWindow();
    this.elasticsearchClient = elasticsearchClient;
    this.esResponseParser = new LiteratureEsResponseParser(searchResultConverter, esFieldMapper);
    this.esSearchRequestBuilder = esSearchRequestBuilder;
    this.esConfig = esConfig;
  }

  public ElasticsearchClient elasticsearchClient() {
    try {
      // Try to ping to check if client is healthy
      elasticsearchClient.ping();
      return elasticsearchClient;
    } catch (Exception e) {
      // Recreate client if unhealthy
      elasticsearchClient = esConfig.reCreateElasticsearchClient();
      return elasticsearchClient;
    }
  }

  @Override
  public org.gbif.api.model.common.search.SearchResponse<
          LiteratureSearchResult, LiteratureSearchParameter>
      search(LiteratureSearchRequest literatureSearchRequest) {
    return searchInternal(literatureSearchRequest);
  }

  private org.gbif.api.model.common.search.SearchResponse<
          LiteratureSearchResult, LiteratureSearchParameter>
      searchInternal(LiteratureSearchRequest literatureSearchRequest) {
    int limit = literatureSearchRequest.getLimit();
    long offset = literatureSearchRequest.getOffset();
    boolean offsetExceeded = false;

    if (limit + offset >= maxResultWindow) {
      literatureSearchRequest.setOffset(maxResultWindow - limit);
      offsetExceeded = true;
    }

    try {
      SearchRequest searchRequest =
          esSearchRequestBuilder.buildSearchRequest(literatureSearchRequest, index);
      co.elastic.clients.elasticsearch.core.SearchResponse<Object> esResponse =
          elasticsearchClient().search(searchRequest, Object.class);

      org.gbif.api.model.common.search.SearchResponse<
              LiteratureSearchResult, LiteratureSearchParameter>
          response = esResponseParser.buildSearchResponse(esResponse, literatureSearchRequest);

      if (offsetExceeded) {
        response.setOffset(offset);
      }

      return response;
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public Optional<LiteratureSearchResult> get(Object identifier) {
    SearchRequest getByIdRequest = esSearchRequestBuilder.buildGetRequest(identifier, index);
    try {
      co.elastic.clients.elasticsearch.core.SearchResponse<Object> esResponse =
          elasticsearchClient().search(getByIdRequest, Object.class);
      return esResponseParser.buildGetResponse(esResponse);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public org.gbif.api.model.common.search.SearchResponse<
          LiteratureSearchResult, LiteratureSearchParameter>
      exportSearch(LiteratureSearchRequest literatureSearchRequest) {
    // For export searches, we use the same internal search method
    // The new client handles buffering internally
    return searchInternal(literatureSearchRequest);
  }
}
