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

import org.gbif.api.model.common.search.SearchResponse;
import org.gbif.api.model.literature.search.LiteratureSearchParameter;
import org.gbif.api.model.literature.search.LiteratureSearchRequest;
import org.gbif.api.model.literature.search.LiteratureSearchResult;
import org.gbif.literature.config.EsClientConfigProperties;
import org.gbif.literature.config.EsConfig;

import java.io.IOException;
import java.util.Optional;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LiteratureSearchServiceEs implements LiteratureSearchService {

  @Value("${literature.bufferLimitBytesExport}")
  private int bufferLimitBytesExport;

  private RestHighLevelClient restHighLevelClient;
  private final LiteratureEsResponseParser esResponseParser;
  private final EsSearchRequestBuilder<LiteratureSearchParameter> esSearchRequestBuilder;
  private final String index;
  private final int maxResultWindow;

  private final EsConfig esConfig;

  public LiteratureSearchServiceEs(
      EsClientConfigProperties esClientConfigProperties,
      RestHighLevelClient restHighLevelClient,
      LiteratureEsResponseParser esResponseParser,
      EsSearchRequestBuilder<LiteratureSearchParameter> esSearchRequestBuilder,
      EsConfig esConfig) {
    this.index = esClientConfigProperties.getIndex();
    this.maxResultWindow = esClientConfigProperties.getMaxResultWindow();
    this.restHighLevelClient = restHighLevelClient;
    this.esResponseParser = esResponseParser;
    this.esSearchRequestBuilder = esSearchRequestBuilder;
    this.esConfig = esConfig;
  }

  public RestHighLevelClient restHighLevelClient() {
    if (!restHighLevelClient.getLowLevelClient().isRunning()) {
      restHighLevelClient = esConfig.reCreateRestHighLevelClient();
    }
    return restHighLevelClient;
  }

  @Override
  public SearchResponse<LiteratureSearchResult, LiteratureSearchParameter> search(
      LiteratureSearchRequest literatureSearchRequest) {
    return searchInternal(literatureSearchRequest, RequestOptions.DEFAULT);
  }

  private SearchResponse<LiteratureSearchResult, LiteratureSearchParameter> searchInternal(
      LiteratureSearchRequest literatureSearchRequest, RequestOptions requestOptions) {
    int limit = literatureSearchRequest.getLimit();
    long offset = literatureSearchRequest.getOffset();
    boolean offsetExceeded = false;

    if (limit + offset >= maxResultWindow) {
      literatureSearchRequest.setOffset(maxResultWindow - limit);
      offsetExceeded = true;
    }

    try {
      SearchRequest searchRequest =
          esSearchRequestBuilder.buildSearchRequest(literatureSearchRequest, true, index);
      SearchResponse<LiteratureSearchResult, LiteratureSearchParameter> response =
          esResponseParser.buildSearchResponse(
              restHighLevelClient().search(searchRequest, requestOptions), literatureSearchRequest);

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
      return esResponseParser.buildGetResponse(
          restHighLevelClient().search(getByIdRequest, RequestOptions.DEFAULT));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public SearchResponse<LiteratureSearchResult, LiteratureSearchParameter> exportSearch(
      LiteratureSearchRequest literatureSearchRequest) {
    RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
    builder.setHttpAsyncResponseConsumerFactory(
        new HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory(
            bufferLimitBytesExport));
    return searchInternal(literatureSearchRequest, builder.build());
  }
}
