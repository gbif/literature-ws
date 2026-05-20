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

import org.gbif.api.model.literature.search.LiteratureSearchParameter;
import org.gbif.api.model.literature.search.LiteratureSearchRequest;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class ExportRequestSupportTest {

  @Test
  void prepareForExport_stripsFacetsAndPagingState() {
    LiteratureSearchRequest request = new LiteratureSearchRequest();
    request.setQ("fish");
    request.setLimit(300);
    request.setOffset(9000);
    request.setHighlight(true);
    request.setFacetMultiSelect(true);
    request.setFacets(EnumSet.of(LiteratureSearchParameter.LITERATURE_TYPE));

    LiteratureSearchRequest export = ExportRequestSupport.prepareForExport(request);

    assertEquals("fish", export.getQ());
    assertEquals(300, export.getLimit());
    assertEquals(0, export.getOffset());
    assertNull(export.getFacets());
    assertFalse(export.isHighlight());
    assertFalse(export.isFacetMultiSelect());
  }
}
