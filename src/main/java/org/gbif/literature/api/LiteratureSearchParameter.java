package org.gbif.literature.api;

import org.gbif.api.model.common.search.SearchParameter;
import org.gbif.api.vocabulary.Country;

public enum LiteratureSearchParameter implements SearchParameter {

  COUNTRY_OR_AREA_OF_RESEARCHER(Country.class),
  COUNTRY_OR_AREA_OF_COVERAGE(Country.class),
  LITERATURE_TYPE(LiteratureType.class),
  RELEVANCE(Relevance.class),
  YEAR(Integer.class),
  TOPIC(Topic.class),
  DATASET(String.class),
  PUBLISHER(String.class),
  PEER_REVIEWED(Boolean.class),
  OPEN_ACCESS(Boolean.class),
  DOWNLOAD_KEY(String.class),
  JOURNAL(String.class),
  JOURNAL_PUBLISHER(String.class);

  private final Class<?> type;

  LiteratureSearchParameter(Class<?> type) {
    this.type = type;
  }

  @Override
  public Class<?> type() {
    return type;
  }
}
