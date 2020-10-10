package org.gbif.literature.search;

import org.gbif.literature.api.LiteratureSearchParameter;
import org.springframework.stereotype.Component;

@Component
public class LiteratureEsSearchRequestBuilder extends EsSearchRequestBuilder<LiteratureSearchParameter> {

  public LiteratureEsSearchRequestBuilder(EsFieldMapper<LiteratureSearchParameter> esFieldMapper) {
    super(esFieldMapper);
  }
}
