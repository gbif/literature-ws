package org.gbif.literature.search;

import org.gbif.common.shaded.com.google.common.collect.ImmutableBiMap;
import org.gbif.literature.api.LiteratureSearchParameter;
import org.springframework.stereotype.Component;

@Component
public class LiteratureEsFieldMapper implements EsFieldMapper<LiteratureSearchParameter> {

  private static final ImmutableBiMap<LiteratureSearchParameter, String> SEARCH_TO_ES_MAPPING =
      ImmutableBiMap.<LiteratureSearchParameter, String>builder()
          .put(LiteratureSearchParameter.LITERATURE_TYPE, "literatureType")
          .build();

  private static final String[] EXCLUDE_FIELDS = new String[]{"all"};

  @Override
  public String get(LiteratureSearchParameter searchParameter) {
    return SEARCH_TO_ES_MAPPING.get(searchParameter);
  }

  @Override
  public String[] excludeFields() {
    return EXCLUDE_FIELDS;
  }

  @Override
  public String[] getMappedFields() {
    return new String[] {
        "literatureType"
    };
  }
}
