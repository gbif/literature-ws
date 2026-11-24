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
package org.gbif.literature.resource;

import org.gbif.api.model.common.export.ExportFormat;
import org.gbif.api.model.common.search.SearchResponse;
import org.gbif.api.model.literature.search.LiteratureSearchParameter;
import org.gbif.api.model.literature.search.LiteratureSearchRequest;
import org.gbif.api.model.literature.search.LiteratureSearchResult;
import org.gbif.literature.config.EsClientConfigProperties;
import org.gbif.literature.config.LiteratureConfigProperties;
import org.gbif.literature.export.ExportLimitExceededException;
import org.gbif.literature.search.LiteratureSearchService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LiteratureResourceTest {

  private LiteratureSearchService searchService;
  private LiteratureResource resource;

  @BeforeEach
  void setUp() {
    searchService = mock(LiteratureSearchService.class);
    LiteratureConfigProperties config = new LiteratureConfigProperties();
    config.setMaxExportRecords(100);
    resource =
        new LiteratureResource(
            searchService, mock(ElasticsearchClient.class), new EsClientConfigProperties(), config);
  }

  @Test
  void export_rejectsTooManyRecordsBeforeStreaming() {
    mockCount(101L);

    assertThrows(
        ExportLimitExceededException.class,
        () -> resource.export(new LiteratureSearchRequest(), ExportFormat.TSV));
  }

  @Test
  void export_allowsRecordsWithinLimit() {
    mockCount(100L);

    assertEquals(
        200,
        resource.export(new LiteratureSearchRequest(), ExportFormat.TSV).getStatusCode().value());
  }

  private void mockCount(long count) {
    SearchResponse<LiteratureSearchResult, LiteratureSearchParameter> response =
        new SearchResponse<>(0, 0);
    response.setCount(count);
    when(searchService.search(any())).thenReturn(response);
  }
}
