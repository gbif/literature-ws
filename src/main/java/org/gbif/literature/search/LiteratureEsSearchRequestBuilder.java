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

import org.gbif.literature.api.LiteratureSearchParameter;

import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.stereotype.Component;

import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;

@Component
public class LiteratureEsSearchRequestBuilder
    extends EsSearchRequestBuilder<LiteratureSearchParameter> {

  public LiteratureEsSearchRequestBuilder(EsFieldMapper<LiteratureSearchParameter> esFieldMapper) {
    super(esFieldMapper);
  }

  @Override
  protected void buildSpecificQuery(
      BoolQueryBuilder queryBuilder, Map<LiteratureSearchParameter, Set<String>> params) {
    if (params != null && !params.isEmpty()) {
      queryBuilder.must(
          nestedQuery(
              "identifiers",
              termsQuery("identifiers.doi", params.get(LiteratureSearchParameter.DOI)),
              ScoreMode.None));
    }
  }
}
