package org.gbif.literature.search;

import org.gbif.literature.api.LiteratureSearchParameter;
import org.gbif.literature.api.LiteratureSearchResult;
import org.gbif.literature.api.LiteratureSuggestResult;
import org.springframework.stereotype.Component;

@Component
public class LiteratureEsResponseParser
    extends EsResponseParser<LiteratureSearchResult, LiteratureSuggestResult, LiteratureSearchParameter> {

  private LiteratureEsResponseParser(
      SearchResultConverter<LiteratureSearchResult, LiteratureSuggestResult> searchResultConverter,
      EsFieldMapper<LiteratureSearchParameter> esFieldMapper) {
    super(searchResultConverter, esFieldMapper);
  }
}
