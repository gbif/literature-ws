package org.gbif.literature.search;

import org.gbif.api.service.common.SearchService;
import org.gbif.literature.api.LiteratureSearchParameter;
import org.gbif.literature.api.LiteratureSearchRequest;
import org.gbif.literature.api.LiteratureSearchResult;

public interface LiteratureSearchService
    extends SearchService<LiteratureSearchResult, LiteratureSearchParameter, LiteratureSearchRequest> {
}
