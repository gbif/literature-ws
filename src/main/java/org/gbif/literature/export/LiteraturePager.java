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

import org.gbif.api.model.common.paging.PagingRequest;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.model.common.search.SearchRequest;
import org.gbif.api.model.literature.search.LiteratureSearchRequest;
import org.gbif.api.model.literature.search.LiteratureSearchResult;
import org.gbif.api.util.iterables.BasePager;
import org.gbif.literature.search.LiteratureSearchService;

/** Iterates over results of {@link LiteratureSearchService#search(SearchRequest)}. */
public class LiteraturePager extends BasePager<LiteratureSearchResult> {

  private final LiteratureSearchService literatureSearchService;
  private final LiteratureSearchRequest literatureSearchRequest;

  public LiteraturePager(
      LiteratureSearchService literatureSearchService,
      LiteratureSearchRequest literatureSearchRequest,
      int pageSize) {
    super(pageSize);
    this.literatureSearchService = literatureSearchService;
    this.literatureSearchRequest = literatureSearchRequest;
  }

  @Override
  public PagingResponse<LiteratureSearchResult> nextPage(PagingRequest pagingRequest) {
    literatureSearchRequest.setOffset(pagingRequest.getOffset());
    literatureSearchRequest.setLimit(pagingRequest.getLimit());
    return literatureSearchService.search(literatureSearchRequest);
  }
}
