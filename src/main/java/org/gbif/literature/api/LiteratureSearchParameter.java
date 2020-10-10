package org.gbif.literature.api;

import org.gbif.api.model.common.search.SearchParameter;

public enum  LiteratureSearchParameter implements SearchParameter {
  ;

  private final Class<?> type;

  LiteratureSearchParameter(Class<?> type) {
    this.type = type;
  }

  @Override
  public Class<?> type() {
    return type;
  }
}
