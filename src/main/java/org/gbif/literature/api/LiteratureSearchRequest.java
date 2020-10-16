package org.gbif.literature.api;

import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.search.FacetedSearchRequest;

public class LiteratureSearchRequest extends FacetedSearchRequest<LiteratureSearchParameter> {

  public LiteratureSearchRequest() {
  }

  public LiteratureSearchRequest(Pageable page) {
    super(page);
  }

  public LiteratureSearchRequest(long offset, int limit) {
    super(offset, limit);
  }
}
