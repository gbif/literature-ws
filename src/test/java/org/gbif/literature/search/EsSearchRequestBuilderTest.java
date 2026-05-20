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

import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.SearchRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EsSearchRequestBuilderTest {

  private static final String INDEX = "literature";

  private EsSearchRequestBuilder<LiteratureSearchParameter> builder;
  private LiteratureEsFieldMapper fieldMapper;

  @BeforeEach
  void setUp() {
    fieldMapper = new LiteratureEsFieldMapper();
    builder = new EsSearchRequestBuilder<>(fieldMapper);
  }

  @Test
  void buildExportSearchRequest_isLeanComparedToFacetedSearch() {
    LiteratureSearchRequest request = facetedSearchRequest();

    SearchRequest exportRequest =
        builder.buildExportSearchRequest(request, INDEX, "pit-abc", null);
    SearchRequest uiRequest = builder.buildSearchRequest(request, INDEX);

    assertTrue(exportRequest.aggregations() == null || exportRequest.aggregations().isEmpty());
    assertNotNull(uiRequest.aggregations());
    assertFalse(uiRequest.aggregations().isEmpty());

    assertFalse(exportRequest.trackTotalHits().enabled());
    assertTrue(uiRequest.trackTotalHits().enabled());

    assertNull(exportRequest.from());
    assertEquals(10, uiRequest.from());

    assertNull(exportRequest.highlight());

    assertEquals(("pit-abc"), exportRequest.pit().id());
    assertEquals(List.of(INDEX), uiRequest.index());

    assertEquals(2, exportRequest.sort().size());
    assertEquals("created", exportRequest.sort().get(0).field().field());
    assertEquals("id", exportRequest.sort().get(1).field().field());

    assertEquals(
        List.of(fieldMapper.getExportMappedFields()),
        exportRequest.source().filter().includes());
  }

  @Test
  void buildExportSearchRequest_withSearchAfterCursor() {
    LiteratureSearchRequest request = facetedSearchRequest();
    List<FieldValue> cursor =
        List.of(FieldValue.of("2024-01-01T00:00:00"), FieldValue.of("doc-id-1"));

    SearchRequest exportRequest =
        builder.buildExportSearchRequest(request, INDEX, "pit-xyz", cursor);

    assertEquals(cursor, exportRequest.searchAfter());
  }

  private static LiteratureSearchRequest facetedSearchRequest() {
    LiteratureSearchRequest request = new LiteratureSearchRequest();
    request.setQ("biodiversity");
    request.setLimit(500);
    request.setOffset(10);
    request.setHighlight(true);
    request.setFacets(EnumSet.of(LiteratureSearchParameter.COUNTRIES_OF_COVERAGE));
    return request;
  }
}
