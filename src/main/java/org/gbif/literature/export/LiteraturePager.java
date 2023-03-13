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
