/*
 * Copyright 2021 Global Biodiversity Information Facility (GBIF)
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

import org.gbif.api.model.literature.LiteratureRelevance;
import org.gbif.api.model.literature.LiteratureTopic;
import org.gbif.api.model.literature.LiteratureType;
import org.gbif.api.model.literature.search.LiteratureSearchResult;
import org.gbif.api.util.VocabularyUtils;
import org.gbif.api.vocabulary.Country;
import org.gbif.api.vocabulary.GbifRegion;
import org.gbif.api.vocabulary.Language;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static org.gbif.literature.util.EsQueryUtils.STRING_TO_DATE;

@SuppressWarnings("unchecked")
@Component
public class LiteratureSearchResultConverter
    implements SearchResultConverter<LiteratureSearchResult> {

  private static final Logger LOG = LoggerFactory.getLogger(LiteratureSearchResultConverter.class);

  private static final Pattern NESTED_PATTERN = Pattern.compile("^\\w+(\\.\\w+)+$");
  private static final Predicate<String> IS_NESTED = s -> NESTED_PATTERN.matcher(s).find();

  @Override
  public LiteratureSearchResult toSearchResult(SearchHit searchHit) {
    LiteratureSearchResult result = new LiteratureSearchResult();
    Map<String, Object> fields = searchHit.getSourceAsMap();

    getStringValue(fields, "abstract").ifPresent(result::setAbstr);
    getStringValue(fields, "accessed").ifPresent(result::setDiscovered);
    getObjectsListValue(fields, "authors").ifPresent(result::setAuthors);
    getCountrySetValue(fields, "countriesOfCoverage").ifPresent(result::setCountriesOfCoverage);
    getCountrySetValue(fields, "countriesOfResearcher").ifPresent(result::setCountriesOfResearcher);
    getDateValue(fields, "created").ifPresent(result::setAdded);
    getDateValue(fields, "createdAt").ifPresent(result::setPublished);
    getIntegerValue(fields, "day").ifPresent(result::setDay);
    getListValue(fields, "gbifDownloadKey").ifPresent(result::setGbifDownloadKey);
    getRegionSetValue(fields, "gbifRegion").ifPresent(result::setGbifRegion);
    getUuidValue(fields, "id").ifPresent(result::setId);
    getMapValue(fields, "identifiers").ifPresent(result::setIdentifiers);
    getListValue(fields, "keywords").ifPresent(result::setKeywords);
    getLanguageValue(fields, "language").ifPresent(result::setLanguage);
    getLiteratureTypeValue(fields, "literatureType").ifPresent(result::setLiteratureType);
    getIntegerValue(fields, "month").ifPresent(result::setMonth);
    getStringValue(fields, "notes").ifPresent(result::setNotes);
    getBooleanValue(fields, "openAccess").ifPresent(result::setOpenAccess);
    getBooleanValue(fields, "peerReview").ifPresent(result::setPeerReview);
    getStringValue(fields, "publisher").ifPresent(result::setPublisher);
    getRelevanceSetValue(fields, "relevance").ifPresent(result::setRelevance);
    getStringValue(fields, "source").ifPresent(result::setSource);
    getListValue(fields, "tags").ifPresent(result::setTags);
    getStringValue(fields, "title").ifPresent(result::setTitle);
    getTopicSetValue(fields, "topics").ifPresent(result::setTopics);
    getDateValue(fields, "updatedAt").ifPresent(result::setModified);
    getListValue(fields, "websites").ifPresent(result::setWebsites);
    getIntegerValue(fields, "year").ifPresent(result::setYear);

    return result;
  }

  private static Optional<String> getStringValue(Map<String, Object> fields, String esField) {
    return getValue(fields, esField, Function.identity());
  }

  private static Optional<Integer> getIntegerValue(Map<String, Object> fields, String esField) {
    return getValue(fields, esField, Integer::valueOf);
  }

  private static Optional<Boolean> getBooleanValue(Map<String, Object> fields, String esField) {
    return getValue(fields, esField, Boolean::valueOf);
  }

  private static Optional<Date> getDateValue(Map<String, Object> fields, String esField) {
    return getValue(fields, esField, STRING_TO_DATE);
  }

  private static Optional<UUID> getUuidValue(Map<String, Object> fields, String esField) {
    return getValue(fields, esField, UUID::fromString);
  }

  private static Optional<Country> getCountryValue(Map<String, Object> fields, String esField) {
    return getValue(fields, esField, Country::fromIsoCode);
  }

  private static Optional<Language> getLanguageValue(Map<String, Object> fields, String esField) {
    return getValue(fields, esField, Language::fromIsoCode);
  }

  private static Optional<List<String>> getListValue(Map<String, Object> fields, String esField) {
    return Optional.ofNullable(fields.get(esField))
        .map(v -> (List<String>) v)
        .filter(v -> !v.isEmpty());
  }

  private static Optional<Map<String, Object>> getMapValue(
      Map<String, Object> fields, String esField) {
    return Optional.ofNullable(fields.get(esField))
        .map(v -> (Map<String, Object>) v)
        .filter(v -> !v.isEmpty());
  }

  private static Optional<Set<Country>> getCountrySetValue(
      Map<String, Object> fields, String esField) {
    return Optional.ofNullable(fields.get(esField))
        .map(v -> (List<String>) v)
        .filter(v -> !v.isEmpty())
        .map(v -> v.stream().map(Country::fromIsoCode).collect(Collectors.toSet()));
  }

  private static Optional<Set<GbifRegion>> getRegionSetValue(
      Map<String, Object> fields, String esField) {
    return Optional.ofNullable(fields.get(esField))
        .map(v -> (List<String>) v)
        .filter(v -> !v.isEmpty())
        .map(v -> v.stream().map(GbifRegion::fromString).collect(Collectors.toSet()));
  }

  private static Optional<LiteratureType> getLiteratureTypeValue(
      Map<String, Object> fields, String esField) {
    return getValue(
        fields, esField, value -> VocabularyUtils.lookupEnum(value, LiteratureType.class));
  }

  private static Optional<Set<LiteratureRelevance>> getRelevanceSetValue(
      Map<String, Object> fields, String esField) {
    return Optional.ofNullable(fields.get(esField))
        .map(v -> (List<String>) v)
        .filter(v -> !v.isEmpty())
        .map(
            v ->
                v.stream()
                    .map(value -> VocabularyUtils.lookupEnum(value, LiteratureRelevance.class))
                    .collect(Collectors.toSet()));
  }

  private static Optional<Set<LiteratureTopic>> getTopicSetValue(
      Map<String, Object> fields, String esField) {
    return Optional.ofNullable(fields.get(esField))
        .map(v -> (List<String>) v)
        .filter(v -> !v.isEmpty())
        .map(
            v ->
                v.stream()
                    .map(value -> VocabularyUtils.lookupEnum(value, LiteratureTopic.class))
                    .collect(Collectors.toSet()));
  }

  private static Optional<List<Map<String, Object>>> getObjectsListValue(
      Map<String, Object> fields, String esField) {
    return Optional.ofNullable(fields.get(esField))
        .map(v -> (List<Map<String, Object>>) v)
        .filter(v -> !v.isEmpty());
  }

  private static <T> Optional<T> getValue(
      Map<String, Object> fields, String esField, Function<String, T> mapper) {
    String fieldName = esField;
    if (IS_NESTED.test(esField)) {
      // take all paths till the field name
      String[] paths = esField.split("\\.");
      for (int i = 0; i < paths.length - 1 && fields.containsKey(paths[i]); i++) {
        // update the fields with the current path
        fields = (Map<String, Object>) fields.get(paths[i]);
      }
      // the last path is the field name
      fieldName = paths[paths.length - 1];
    }

    return extractValue(fields, fieldName, mapper);
  }

  private static <T> Optional<T> extractValue(
      Map<String, Object> fields, String fieldName, Function<String, T> mapper) {
    return Optional.ofNullable(fields.get(fieldName))
        .map(String::valueOf)
        .filter(v -> !v.isEmpty())
        .map(
            v -> {
              try {
                return mapper.apply(v);
              } catch (Exception ex) {
                LOG.error("Error extracting field {} with value {}", fieldName, v);
                return null;
              }
            });
  }
}
