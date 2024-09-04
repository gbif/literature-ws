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
package org.gbif.literature.export;

import java.io.IOException;

import java.util.Arrays;
import java.util.List;

import org.elasticsearch.action.search.ClosePointInTimeRequest;
import org.elasticsearch.action.search.OpenPointInTimeRequest;
import org.elasticsearch.action.search.OpenPointInTimeResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.core.TimeValue;

import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.model.common.search.SearchRequest;
import org.gbif.api.model.literature.search.LiteratureSearchRequest;
import org.gbif.api.model.literature.search.LiteratureSearchResult;
import org.gbif.literature.config.EsClientConfigProperties;
import org.gbif.literature.search.LiteratureEsResponseParser;
import org.gbif.literature.search.LiteratureSearchService;

/** Iterates over results of {@link LiteratureSearchService#search(SearchRequest)}. */
public class LiteraturePager {

  private final LiteratureSearchService literatureSearchService;
  private final RestHighLevelClient client;
  private final LiteratureSearchRequest literatureSearchRequest;
  private List<Object> searchAfterValues;
  private String pitId;
  private String index;
  private final LiteratureEsResponseParser esResponseParser;

  public LiteraturePager(
    EsClientConfigProperties esClientConfigProperties,
    LiteratureSearchService literatureSearchService,
    LiteratureSearchRequest literatureSearchRequest,
    RestHighLevelClient client, LiteratureEsResponseParser esResponseParser) {
    this.index = esClientConfigProperties.getIndex();
    this.literatureSearchService = literatureSearchService;
    this.literatureSearchRequest = literatureSearchRequest;
    this.client = client;
    this.esResponseParser = esResponseParser;
    this.pitId = null;
    this.searchAfterValues = null;
  }

  public PagingResponse<LiteratureSearchResult> nextPage(int pageSize) {
    literatureSearchRequest.setLimit(pageSize);
    try {
      if (pitId == null) {
        pitId = openPIT();
      }

      SearchResponse searchResponse =
          literatureSearchService.exportSearch(literatureSearchRequest, searchAfterValues, pitId);

      org.gbif.api.model.common.search.SearchResponse results = parseSearchResults(searchResponse);

      pitId = searchResponse.pointInTimeId();

      if (results.isEndOfRecords() || searchResponse.getHits().getHits().length == 0) {
        closePIT(pitId);
        pitId = null;
        return results;
      }

      searchAfterValues = getSearchAfterValues(searchResponse);
      return results;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private org.gbif.api.model.common.search.SearchResponse parseSearchResults(
      org.elasticsearch.action.search.SearchResponse esSearchResponse) {
    return esResponseParser.buildSearchResponse(esSearchResponse, literatureSearchRequest);
  }

  private List<Object> getSearchAfterValues(SearchResponse searchResponse) {
    if (searchResponse.getHits().getHits().length == 0) {
      return null;
    }
    return Arrays.asList(searchResponse.getHits().getHits()[searchResponse.getHits().getHits().length - 1].getSortValues());
  }

  private String openPIT() throws IOException {
    OpenPointInTimeRequest openRequest = new OpenPointInTimeRequest(index);
    openRequest.keepAlive(TimeValue.timeValueMinutes(1));
    OpenPointInTimeResponse openResponse = client.openPointInTime(openRequest, RequestOptions.DEFAULT);
    return openResponse.getPointInTimeId();
  }

  private void closePIT(String pitId) throws IOException {
    ClosePointInTimeRequest closeRequest = new ClosePointInTimeRequest(pitId);
    client.closePointInTime(closeRequest, RequestOptions.DEFAULT);
  }
}
