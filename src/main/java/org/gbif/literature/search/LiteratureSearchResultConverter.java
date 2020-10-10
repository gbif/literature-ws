package org.gbif.literature.search;

import org.elasticsearch.search.SearchHit;
import org.gbif.literature.api.LiteratureSearchResult;
import org.gbif.literature.api.LiteratureSuggestResult;
import org.springframework.stereotype.Component;

@Component
public class LiteratureSearchResultConverter
    implements SearchResultConverter<LiteratureSearchResult, LiteratureSuggestResult> {

  @Override
  public LiteratureSearchResult toSearchResult(SearchHit searchHit) {
    return null;
  }

  @Override
  public LiteratureSuggestResult toSearchSuggestResult(SearchHit searchHit) {
    return null;
  }
}
