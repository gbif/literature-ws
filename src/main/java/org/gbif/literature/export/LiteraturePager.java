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

import java.util.function.Consumer;

/** Iterates over results of {@link LiteratureSearchService#search(SearchRequest)}. */
public class LiteraturePager extends BasePager<LiteratureSearchResult> {

  private final LiteratureSearchService literatureSearchService;
  private final LiteratureSearchRequest literatureSearchRequest;
  private final Consumer<PagingResponse<LiteratureSearchResult>> onPageResponse;

  public LiteraturePager(
      LiteratureSearchService literatureSearchService,
      LiteratureSearchRequest literatureSearchRequest,
      int pageSize,
      Consumer<PagingResponse<LiteratureSearchResult>> onPageResponse) {
    super(pageSize);
    this.literatureSearchService = literatureSearchService;
    this.literatureSearchRequest = literatureSearchRequest;
    this.onPageResponse = onPageResponse;
  }

  @Override
  public PagingResponse<LiteratureSearchResult> nextPage(PagingRequest pagingRequest) {
    literatureSearchRequest.setOffset(pagingRequest.getOffset());
    literatureSearchRequest.setLimit(pagingRequest.getLimit());
    PagingResponse<LiteratureSearchResult> response = literatureSearchService.exportSearch(literatureSearchRequest);
    onPageResponse.accept(response);
    return response;
  }
}
