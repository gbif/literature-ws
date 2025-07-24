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

import co.elastic.clients.elasticsearch.core.search.Hit;

/**
 * Converts search hits into concrete result objects.
 */
public interface SearchResultConverter<T> {

  /**
   * Converts a search hit into a concrete result object.
   * 
   * @param hit the search hit
   * @return converted result object
   */
  T toResult(Hit<Object> hit);
}
