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
import org.gbif.literature.api.LiteratureType;
import org.gbif.literature.api.Relevance;
import org.gbif.literature.api.Topic;

import java.util.List;
import java.util.Map;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.search.MatchQuery;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import static org.gbif.literature.util.EsQueryUtils.escapeQuery;

@Component
public class LiteratureEsFieldMapper implements EsFieldMapper<LiteratureSearchParameter> {

  private static final ImmutableBiMap<LiteratureSearchParameter, String> SEARCH_TO_ES_MAPPING =
      ImmutableBiMap.<LiteratureSearchParameter, String>builder()
          .put(LiteratureSearchParameter.COUNTRIES_OF_RESEARCHER, "countriesOfResearcher")
          .put(LiteratureSearchParameter.COUNTRIES_OF_COVERAGE, "countriesOfCoverage")
          .put(LiteratureSearchParameter.LITERATURE_TYPE, "literatureType")
          .put(LiteratureSearchParameter.RELEVANCE, "relevance")
          .put(LiteratureSearchParameter.YEAR, "year")
          .put(LiteratureSearchParameter.TOPICS, "topics")
          .put(LiteratureSearchParameter.GBIF_DATASET_KEY, "gbifDatasetKey")
          .put(LiteratureSearchParameter.PUBLISHING_ORGANIZATION_KEY, "publishingOrganizationKey")
          .put(LiteratureSearchParameter.PUBLISHER, "publisher")
          .put(LiteratureSearchParameter.SOURCE, "source")
          .put(LiteratureSearchParameter.PEER_REVIEW, "peerReview")
          .put(LiteratureSearchParameter.OPEN_ACCESS, "openAccess")
          .put(LiteratureSearchParameter.GBIF_DOWNLOAD_KEY, "gbifDownloadKey")
          .build();

  public static final Map<String, Integer> CARDINALITIES =
      ImmutableMap.<String, Integer>builder()
          .put("literatureType", LiteratureType.values().length)
          .put("relevance", Relevance.values().length)
          .put("topics", Topic.values().length)
          .build();

  private static final FieldSortBuilder[] SORT =
      new FieldSortBuilder[] {SortBuilders.fieldSort("created").order(SortOrder.DESC)};

  private static final String[] EXCLUDE_FIELDS = new String[] {"all"};

  public static final List<String> DATE_FIELDS =
      ImmutableList.of("created", "createdAt", "updatedAt");

  @Override
  public String get(LiteratureSearchParameter searchParameter) {
    return SEARCH_TO_ES_MAPPING.get(searchParameter);
  }

  @Override
  public LiteratureSearchParameter get(String esField) {
    return SEARCH_TO_ES_MAPPING.inverse().get(esField);
  }

  @Override
  public Integer getCardinality(String esFieldName) {
    return CARDINALITIES.get(esFieldName);
  }

  @Override
  public String[] excludeFields() {
    return EXCLUDE_FIELDS;
  }

  @Override
  public SortBuilder<? extends SortBuilder>[] sorts() {
    return SORT;
  }

  @Override
  public boolean isDateField(String esFieldName) {
    return DATE_FIELDS.contains(esFieldName);
  }

  @Override
  public String[] getMappedFields() {
    return new String[] {
      "title",
      "authors",
      "year",
      "source",
      "identifiers",
      "keywords",
      "websites",
      "month",
      "publisher",
      "day",
      "id",
      "created",
      "accessed",
      "tags",
      "read",
      "starred",
      "authored",
      "confirmed",
      "hidden",
      "language",
      "country",
      "notes",
      "abstract",
      "fileAttached",
      "profileId",
      "groupId",
      "updatedAt",
      "citationKey",
      "userContext",
      "privatePublication",
      "literatureType",
      "searchable",
      "createdAt",
      "countriesOfResearcher",
      "countriesOfCoverage",
      "gbifRegion",
      "gbifDatasetKey",
      "publishingOrganizationKey",
      "relevance",
      "topics",
      "gbifDownloadKey",
      "peerReview",
      "openAccess",
      "contentType"
    };
  }

  @Override
  public QueryBuilder fullTextQuery(String q) {
    BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

    boolQueryBuilder
        .should()
        .add(
            QueryBuilders.matchQuery("_all", escapeQuery(q))
                .operator(Operator.AND)
                .boost(10.0F)
                .fuzziness("AUTO")
                .prefixLength(3)
                .lenient(true)
                .zeroTermsQuery(MatchQuery.ZeroTermsQuery.ALL));

    return boolQueryBuilder;
  }
}
