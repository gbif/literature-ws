/*
 * Copyright 2020 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
