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

import org.gbif.api.model.literature.LiteratureRelevance;
import org.gbif.api.model.literature.LiteratureTopic;
import org.gbif.api.model.literature.LiteratureType;
import org.gbif.api.model.literature.search.LiteratureSearchParameter;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

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
          .put(LiteratureSearchParameter.WEBSITES, "websites")
          .put(LiteratureSearchParameter.TOPICS, "topics")
          .put(LiteratureSearchParameter.GBIF_DATASET_KEY, "gbifDatasetKey")
          .put(LiteratureSearchParameter.PUBLISHING_ORGANIZATION_KEY, "publishingOrganizationKey")
          .put(LiteratureSearchParameter.PUBLISHING_COUNTRY, "publishingCountry")
          .put(LiteratureSearchParameter.PUBLISHER, "publisher")
          .put(LiteratureSearchParameter.SOURCE, "source")
          .put(LiteratureSearchParameter.PEER_REVIEW, "peerReview")
          .put(LiteratureSearchParameter.OPEN_ACCESS, "openAccess")
          .put(LiteratureSearchParameter.GBIF_DOWNLOAD_KEY, "gbifDownloadKey")
          .put(LiteratureSearchParameter.GBIF_TAXON_KEY, "gbifTaxonKey")
          .put(LiteratureSearchParameter.GBIF_OCCURRENCE_KEY, "gbifOccurrenceKey")
          .put(LiteratureSearchParameter.GBIF_HIGHERTAXON_KEY, "gbifHigherTaxonKey")
          .put(LiteratureSearchParameter.CITATION_TYPE, "citationType")
          .put(LiteratureSearchParameter.GBIF_NETWORK_KEY, "gbifNetworkKey")
          .put(LiteratureSearchParameter.GBIF_PROJECT_IDENTIFIER, "gbifProjectIdentifier")
          .put(LiteratureSearchParameter.GBIF_PROGRAMME, "gbifProgrammeAcronym")
          .put(LiteratureSearchParameter.LANGUAGE, "language")
          .put(LiteratureSearchParameter.DOI, "identifiers.doi") // nested field path
          .put(LiteratureSearchParameter.ADDED, "created")
          .put(LiteratureSearchParameter.PUBLISHED, "createdAt")
          .put(LiteratureSearchParameter.DISCOVERED, "accessed")
          .put(LiteratureSearchParameter.MODIFIED, "updatedAt")
          .build();

  public static final Map<String, Integer> CARDINALITIES =
      ImmutableMap.<String, Integer>builder()
          .put("literatureType", LiteratureType.values().length)
          .put("relevance", LiteratureRelevance.values().length)
          .put("topics", LiteratureTopic.values().length)
          .build();

  private static final SortOptions[] SORT =
      new SortOptions[] {
        SortOptions.of(s -> s.field(f -> f.field("created").order(SortOrder.Desc)))
      };

  private static final String[] EXCLUDE_FIELDS =
      new String[] {
        "_all",
        "authored",
        "chapter",
        "city",
        "code",
        "confirmed",
        "contentType",
        "department",
        "edition",
        "editors",
        "fileAttached",
        "genre",
        "groupId",
        "gbifFeatureId",
        "hidden",
        "institution",
        "issue",
        "medium",
        "pages",
        "patentApplicationNumber",
        "patentLegalStatus",
        "patentOwner",
        "privatePublication",
        "profileId",
        "read",
        "reprintEdition",
        "revision",
        "searchable",
        "series",
        "seriesEditor",
        "shortTitle",
        "sourceType",
        "starred",
        "translators",
        "userContext",
        "volume"
      };

  public static final List<String> DATE_FIELDS =
      ImmutableList.of("created", "createdAt", "updatedAt", "accessed");

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
  public SortOptions[] sorts() {
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
      "language",
      "notes",
      "abstract",
      "updatedAt",
      "citationKey",
      "literatureType",
      "createdAt",
      "countriesOfResearcher",
      "countriesOfCoverage",
      "gbifRegion",
      "gbifDatasetKey",
      "publishingOrganizationKey",
      "publishingCountry",
      "gbifTaxonKey",
      "gbifOccurrenceKey",
      "gbifHigherTaxonKey",
      "citationType",
      "relevance",
      "topics",
      "gbifDownloadKey",
      "gbifNetworkKey",
      "peerReview",
      "gbifProjectIdentifier",
      "gbifProgrammeAcronym",
      "openAccess"
    };
  }

  @Override
  public Query fullTextQuery(String q) {
    String escapedQuery = escapeQuery(q);
    return Query.of(query -> query.multiMatch(mm -> mm
        .query(escapedQuery)
        .fields("title^10", "abstract^2", "_all^1", "source", "publisher", "keywords^5")
        .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.And)
        .fuzziness("AUTO")
        .prefixLength(3)
    ));
  }
}
