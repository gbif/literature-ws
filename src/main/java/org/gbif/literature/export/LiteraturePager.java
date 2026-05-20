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

import org.gbif.api.model.common.search.SearchResponse;
import org.gbif.api.model.literature.search.LiteratureSearchParameter;
import org.gbif.api.model.literature.search.LiteratureSearchRequest;
import org.gbif.api.model.literature.search.LiteratureSearchResult;
import org.gbif.literature.config.EsClientConfigProperties;
import org.gbif.literature.config.LiteratureConfigProperties;
import org.gbif.literature.search.LiteratureSearchService;

import java.io.IOException;
import java.util.List;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.ClosePointInTimeRequest;
import co.elastic.clients.elasticsearch.core.OpenPointInTimeRequest;
import co.elastic.clients.elasticsearch.core.OpenPointInTimeResponse;

import static org.gbif.literature.search.EsSearchRequestBuilder.EXPORT_PIT_KEEP_ALIVE;

/** Cursor-paginated export over literature search results using PIT and search_after. */
public class LiteraturePager implements AutoCloseable {

  private final LiteratureSearchService literatureSearchService;
  private final ElasticsearchClient elasticsearchClient;
  private final LiteratureSearchRequest literatureSearchRequest;
  private final String index;
  private final int maxExportRecords;

  private List<FieldValue> searchAfterValues;
  private String pitId;
  private int exportedRecords;

  public LiteraturePager(
      LiteratureSearchService literatureSearchService,
      ElasticsearchClient elasticsearchClient,
      EsClientConfigProperties esClientConfigProperties,
      LiteratureConfigProperties literatureConfigProperties,
      LiteratureSearchRequest literatureSearchRequest) {
    this.literatureSearchService = literatureSearchService;
    this.elasticsearchClient = elasticsearchClient;
    this.index = esClientConfigProperties.getIndex();
    this.maxExportRecords = literatureConfigProperties.getMaxExportRecords();
    this.literatureSearchRequest = literatureSearchRequest;
  }

  public SearchResponse<LiteratureSearchResult, LiteratureSearchParameter> nextPage()
      throws IOException {
    if (exportedRecords >= maxExportRecords) {
      throw new ExportLimitExceededException(
          "Export exceeds maximum of " + maxExportRecords + " records");
    }

    if (pitId == null) {
      pitId = openPit();
    }

    var exportPage =
        literatureSearchService.exportSearch(literatureSearchRequest, searchAfterValues, pitId);

    pitId = exportPage.getPitId();
    searchAfterValues = exportPage.getNextSearchAfter();

    SearchResponse<LiteratureSearchResult, LiteratureSearchParameter> response = exportPage.getPage();
    int pageCount = response.getResults().size();
    exportedRecords += pageCount;

    if (response.isEndOfRecords() || response.getResults().isEmpty()) {
      closePit();
    } else if (exportedRecords >= maxExportRecords) {
      response.setEndOfRecords(true);
      closePit();
    }

    return response;
  }

  @Override
  public void close() {
    if (pitId != null) {
      try {
        closePit();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private String openPit() throws IOException {
    OpenPointInTimeRequest openRequest =
        OpenPointInTimeRequest.of(o -> o.index(index).keepAlive(EXPORT_PIT_KEEP_ALIVE));
    OpenPointInTimeResponse openResponse = elasticsearchClient.openPointInTime(openRequest);
    return openResponse.id();
  }

  private void closePit() throws IOException {
    if (pitId == null) {
      return;
    }
    ClosePointInTimeRequest closeRequest = ClosePointInTimeRequest.of(c -> c.id(pitId));
    elasticsearchClient.closePointInTime(closeRequest);
    pitId = null;
    searchAfterValues = null;
  }
}
