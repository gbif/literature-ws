/*
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

import org.gbif.api.model.common.search.SearchParameter;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

public interface EsFieldMapper<P extends SearchParameter> {

  String get(P searchParameter);

  P get(String esFieldName);

  Integer getCardinality(String esFieldName);

  String[] excludeFields();

  SortOptions[] sorts();

  boolean isDateField(String esFieldName);

  Query fullTextQuery(String q);

  default String[] getMappedFields() {
    return new String[0];
  }
}
