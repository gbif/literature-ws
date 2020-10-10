package org.gbif.literature.search;

import org.elasticsearch.search.SearchHit;

public interface SearchResultConverter<T, S> {

  T toSearchResult(SearchHit searchHit);

  S toSearchSuggestResult(SearchHit searchHit);
}
