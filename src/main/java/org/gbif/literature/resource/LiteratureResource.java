package org.gbif.literature.resource;

import org.gbif.api.model.common.search.SearchResponse;
import org.gbif.literature.api.LiteratureSearchParameter;
import org.gbif.literature.api.LiteratureSearchRequest;
import org.gbif.literature.api.LiteratureSearchResult;
import org.gbif.literature.search.LiteratureSearchService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "literature", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class LiteratureResource {

  private final LiteratureSearchService searchService;

  public LiteratureResource(LiteratureSearchService searchService) {
    this.searchService = searchService;
  }

  @GetMapping("search")
  public SearchResponse<LiteratureSearchResult, LiteratureSearchParameter> search(
      LiteratureSearchRequest searchRequest) {
    return searchService.search(searchRequest);
  }
}
