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

import org.gbif.api.model.literature.search.LiteratureSearchParameter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

/**
 * Literature-specific search request builder with special handling for nested fields like DOI.
 */
@Component
public class LiteratureEsSearchRequestBuilder
    extends EsSearchRequestBuilder<LiteratureSearchParameter> {

  public LiteratureEsSearchRequestBuilder(EsFieldMapper<LiteratureSearchParameter> esFieldMapper) {
    super(esFieldMapper);
  }

  /**
   * Override to provide special handling for DOI nested queries.
   */
  @Override
  protected List<Query> buildTermQuery(Collection<String> values, LiteratureSearchParameter param, String esField) {
    // Special handling for DOI - nested query required
    if (param == LiteratureSearchParameter.DOI && esField.equals("identifiers.doi")) {
      return buildDoiNestedQuery(values);
    }

    // Use parent implementation for all other fields
    return super.buildTermQuery(values, param, esField);
  }

  /**
   * Builds a nested query specifically for DOI searches.
   * DOI values are converted to lowercase for case-insensitive matching.
   */
  private List<Query> buildDoiNestedQuery(Collection<String> values) {
    List<Query> queries = new ArrayList<>();

    if (values != null && !values.isEmpty()) {
      // Convert DOI values to lowercase for case-insensitive search
      List<FieldValue> doiValues = values.stream()
          .map(String::toLowerCase)
          .map(FieldValue::of)
          .toList();

      // Build nested query for identifiers.doi
      Query nestedQuery = Query.of(q -> q.nested(n -> n
          .path("identifiers")
          .query(Query.of(nq -> nq.terms(t -> t
              .field("identifiers.doi")
              .terms(ts -> ts.value(doiValues))
          )))
      ));

      queries.add(nestedQuery);
    }

    return queries;
  }
}
