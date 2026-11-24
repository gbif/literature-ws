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

import org.gbif.api.model.common.search.SearchResponse;
import org.gbif.api.model.literature.search.LiteratureSearchParameter;
import org.gbif.api.model.literature.search.LiteratureSearchResult;
import org.gbif.literature.config.EsClientConfigProperties;
import org.gbif.literature.config.LiteratureConfigProperties;
import org.gbif.literature.config.LiteratureSearchRequestHandlerMethodArgumentResolver;
import org.gbif.literature.search.LiteratureEsFieldMapper;
import org.gbif.literature.search.LiteratureEsSearchRequestBuilder;
import org.gbif.literature.search.LiteratureSearchService;
import org.gbif.literature.search.LiteratureSearchServiceEs;
import org.gbif.literature.search.SearchResultConverter;
import org.gbif.ws.server.mapper.IllegalArgumentExceptionMapper;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LiteratureResourceTest {

  private final LiteratureSearchService searchService = mock(LiteratureSearchService.class);
  private final ElasticsearchClient esClient = mock(ElasticsearchClient.class);

  @Test
  void export_overRecordLimitReturns413BeforeStreaming() throws Exception {
    MockMvc mvc = mockMvc(searchService, 1);
    mockCount(101L);

    mvc.perform(get("/literature/export")).andExpect(status().isPayloadTooLarge());
    verifyNoInteractions(esClient);
  }

  @Test
  void export_withinRecordLimitStartsStreaming() throws Exception {
    MockMvc mvc = mockMvc(searchService, 1);
    mockCount(100L);

    mvc.perform(get("/literature/export")).andExpect(request().asyncStarted());
  }

  @Test
  void export_whenAllPermitsTakenReturns429WithoutCounting() throws Exception {
    MockMvc mvc = mockMvc(searchService, 1);
    AtomicInteger counts = new AtomicInteger();
    AtomicInteger nestedStatus = new AtomicInteger();
    SearchResponse<LiteratureSearchResult, LiteratureSearchParameter> response =
        new SearchResponse<>(0, 0);
    response.setCount(1L);
    // A second export while the first holds the only permit
    when(searchService.search(any()))
        .thenAnswer(
            inv -> {
              if (counts.incrementAndGet() == 1) {
                nestedStatus.set(
                    mvc.perform(get("/literature/export")).andReturn().getResponse().getStatus());
              }
              return response;
            });

    mvc.perform(get("/literature/export")).andExpect(request().asyncStarted());

    assertEquals(429, nestedStatus.get());
    assertEquals(1, counts.get());
  }

  @Test
  void search_invalidDateReturns400() throws Exception {
    LiteratureEsFieldMapper fieldMapper = new LiteratureEsFieldMapper();
    EsClientConfigProperties esProps = new EsClientConfigProperties();
    esProps.setIndex("literature");
    esProps.setMaxResultWindow(10_000);
    @SuppressWarnings("unchecked")
    SearchResultConverter<LiteratureSearchResult> converter = mock(SearchResultConverter.class);
    LiteratureSearchService realService =
        new LiteratureSearchServiceEs(
            esProps,
            esClient,
            converter,
            new LiteratureEsSearchRequestBuilder(fieldMapper),
            fieldMapper);

    mockMvc(realService, 1)
        .perform(get("/literature/search").param("added", "2024-13-01"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(esClient);
  }

  @Test
  void maxConcurrentExportsMustBePositive() {
    LiteratureConfigProperties config = new LiteratureConfigProperties();
    config.setMaxConcurrentExports(0);

    assertThrows(
        IllegalArgumentException.class,
        () -> new LiteratureResource(searchService, esClient, new EsClientConfigProperties(), config));
  }

  private MockMvc mockMvc(LiteratureSearchService service, int maxConcurrentExports) {
    LiteratureConfigProperties config = new LiteratureConfigProperties();
    config.setMaxExportRecords(100);
    config.setMaxConcurrentExports(maxConcurrentExports);
    LiteratureResource resource =
        new LiteratureResource(service, esClient, new EsClientConfigProperties(), config);
    return MockMvcBuilders.standaloneSetup(resource)
        .setCustomArgumentResolvers(new LiteratureSearchRequestHandlerMethodArgumentResolver())
        .setControllerAdvice(new IllegalArgumentExceptionMapper())
        .build();
  }

  private void mockCount(long count) {
    SearchResponse<LiteratureSearchResult, LiteratureSearchParameter> response =
        new SearchResponse<>(0, 0);
    response.setCount(count);
    when(searchService.search(any())).thenReturn(response);
  }
}
