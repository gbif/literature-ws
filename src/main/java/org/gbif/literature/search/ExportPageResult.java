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

import java.util.List;

import org.gbif.api.model.common.search.SearchResponse;
import org.gbif.api.model.literature.search.LiteratureSearchParameter;
import org.gbif.api.model.literature.search.LiteratureSearchResult;

import co.elastic.clients.elasticsearch._types.FieldValue;

import lombok.Value;

/** One page of export results plus cursor state for the next request. */
@Value
public class ExportPageResult {

  SearchResponse<LiteratureSearchResult, LiteratureSearchParameter> page;
  String pitId;
  List<FieldValue> nextSearchAfter;
}
