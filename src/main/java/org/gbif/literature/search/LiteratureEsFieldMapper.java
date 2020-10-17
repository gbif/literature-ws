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

  private static final String[] EXCLUDE_FIELDS = new String[] {"all"};

  @Override
  public String get(LiteratureSearchParameter searchParameter) {
    return SEARCH_TO_ES_MAPPING.get(searchParameter);
  }

  @Override
  public LiteratureSearchParameter get(String esField) {
    return SEARCH_TO_ES_MAPPING.inverse().get(esField);
  }

  @Override
  public String[] excludeFields() {
    return EXCLUDE_FIELDS;
  }

  @Override
  public String[] getMappedFields() {
    return new String[] {"literatureType"};
  }
}
