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

import org.gbif.api.model.literature.search.LiteratureSearchRequest;

/** Prepares search requests for export (lean ES queries, no facets). */
public final class ExportRequestSupport {

  private ExportRequestSupport() {}

  /**
   * Returns a copy of the request suitable for export: no facets, highlighting, or paging offset.
   */
  public static LiteratureSearchRequest prepareForExport(LiteratureSearchRequest request) {
    LiteratureSearchRequest exportRequest = new LiteratureSearchRequest();
    exportRequest.setQ(request.getQ());
    exportRequest.setParameters(request.getParameters());
    exportRequest.setLimit(request.getLimit());
    exportRequest.setOffset(0);
    exportRequest.setFacets(null);
    exportRequest.setHighlight(false);
    exportRequest.setFacetMultiSelect(false);
    return exportRequest;
  }
}
