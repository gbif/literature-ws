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

import java.time.Instant;

import java.time.LocalDate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import java.time.format.DateTimeFormatter;

import java.time.format.DateTimeParseException;

import org.gbif.api.model.literature.LiteratureRelevance;
import org.gbif.api.model.literature.LiteratureTopic;
import org.gbif.api.model.literature.LiteratureType;
import org.gbif.api.model.literature.search.LiteratureSearchResult;
import org.gbif.api.vocabulary.Country;
import org.gbif.api.vocabulary.GbifRegion;
import org.gbif.api.vocabulary.Language;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LiteratureSearchResultConverter
    implements SearchResultConverter<LiteratureSearchResult> {

  private static final ObjectMapper MAPPER =
      new ObjectMapper().registerModule(new JavaTimeModule());
  Logger logger = LoggerFactory.getLogger(LiteratureSearchResultConverter.class);

  @Override
  public LiteratureSearchResult toResult(Hit<Object> hit) {
    return toSearchResult(hit);
  }

  private LiteratureSearchResult toSearchResult(Hit<Object> hit) {
    LiteratureSearchResult result = new LiteratureSearchResult();

    try {
      // Convert hit source to JsonNode for easy navigation
      JsonNode source = MAPPER.valueToTree(hit.source());

      // Set ID from hit if available
      Optional.ofNullable(hit.id()).map(UUID::fromString).ifPresent(result::setId);

      // Populate fields from source
      populateFields(result, source);

      // Handle highlighting
      handleHighlighting(result, hit, source);

    } catch (Exception e) {
      // Log error but continue with partial result
      logger.error(
          "Error parsing literature search result for hit {}: {}", hit.id(), e.getMessage(), e);
    }

    return result;
  }

  private void populateFields(LiteratureSearchResult result, JsonNode source) {
    extractStringField(source, "abstract").ifPresent(result::setAbstract);
    extractStringField(source, "discovered").ifPresent(result::setDiscovered);
    extractValue(
            source,
            "authors",
            node -> MAPPER.convertValue(node, new TypeReference<List<Map<String, Object>>>() {}))
        .ifPresent(result::setAuthors);
    extractSet(source, "countriesOfCoverage", Country.class, Country::fromIsoCode)
        .ifPresent(result::setCountriesOfCoverage);
    extractSet(source, "countriesOfResearcher", Country.class, Country::fromIsoCode)
        .ifPresent(result::setCountriesOfResearcher);
    extractIsoDateField(source, "created").ifPresent(result::setAdded);
    extractIntegerField(source, "day").ifPresent(result::setDay);
    extractStringList(source, "gbifDownloadKey").ifPresent(result::setGbifDownloadKey);
    extractLongList(source, "gbifOccurrenceKey").ifPresent(result::setGbifOccurrenceKey);
    extractIntegerList(source, "gbifTaxonKey").ifPresent(result::setGbifTaxonKey);
    extractIntegerList(source, "gbifHigherTaxonKey").ifPresent(result::setGbifHigherTaxonKey);
    extractStringField(source, "citationType").ifPresent(result::setCitationType);
    extractSet(source, "gbifRegion", GbifRegion.class, GbifRegion::fromString)
        .ifPresent(result::setGbifRegion);
    extractList(source, "gbifNetworkKey", UUID.class, (node) -> UUID.fromString(node.asText()))
        .ifPresent(result::setGbifNetworkKey);
    extractStringList(source, "gbifProjectIdentifier").ifPresent(result::setGbifProjectIdentifier);
    extractStringList(source, "gbifProgrammeAcronym").ifPresent(result::setGbifProgramme);
    extractValue(source, "id", node -> UUID.fromString(node.asText())).ifPresent(result::setId);
    extractValue(
            source,
            "identifiers",
            node -> MAPPER.convertValue(node, new TypeReference<Map<String, Object>>() {}))
        .ifPresent(result::setIdentifiers);
    extractStringList(source, "keywords").ifPresent(result::setKeywords);
    extractValue(source, "language", node -> Language.fromIsoCode(node.asText()))
        .ifPresent(result::setLanguage);
    extractValue(source, "literatureType", node -> LiteratureType.valueOf(node.asText().toUpperCase()))
        .ifPresent(result::setLiteratureType);
    extractIntegerField(source, "month").ifPresent(result::setMonth);
    extractStringField(source, "notes").ifPresent(result::setNotes);
    extractBooleanField(source, "openAccess").ifPresent(result::setOpenAccess);
    extractBooleanField(source, "peerReview").ifPresent(result::setPeerReview);
    extractStringField(source, "publisher").ifPresent(result::setPublisher);
    extractSet(source, "relevance", LiteratureRelevance.class, s -> LiteratureRelevance.valueOf(s.toUpperCase()))
        .ifPresent(result::setRelevance);
    extractStringField(source, "source").ifPresent(result::setSource);
    extractStringList(source, "tags").ifPresent(result::setTags);
    extractStringField(source, "title").ifPresent(result::setTitle);
    extractSet(source, "topics", LiteratureTopic.class, s -> LiteratureTopic.valueOf(s.toUpperCase()))
        .ifPresent(result::setTopics);
    extractDateField(source, "modified").ifPresent(result::setModified);
    extractStringList(source, "websites").ifPresent(result::setWebsites);
    extractIntegerField(source, "year").ifPresent(result::setYear);
    extractSet(source, "publishingCountry", Country.class, Country::fromIsoCode)
        .ifPresent(result::setPublishingCountry);
    extractDateField(source, "createdAt").ifPresent(result::setPublished);
  }

  private void handleHighlighting(
      LiteratureSearchResult result, Hit<Object> hit, JsonNode source) {
    if (hit.highlight() != null) {
      if (hit.highlight().containsKey("title") && !hit.highlight().get("title").isEmpty()) {
        result.setTitle(hit.highlight().get("title").get(0));
      } else {
        extractStringField(source, "title").ifPresent(result::setTitle);
      }
      if (hit.highlight().containsKey("abstract") && !hit.highlight().get("abstract").isEmpty()) {
        result.setAbstract(hit.highlight().get("abstract").get(0));
      } else {
        extractStringField(source, "abstract").ifPresent(result::setAbstract);
      }
    } else {
      extractStringField(source, "title").ifPresent(result::setTitle);
      extractStringField(source, "abstract").ifPresent(result::setAbstract);
    }
  }

  private <T> Optional<T> extractValue(
      JsonNode source, String fieldName, java.util.function.Function<JsonNode, T> mapper) {
    JsonNode field = source.get(fieldName);
    if (field != null && !field.isNull()) {
      try {
        return Optional.of(mapper.apply(field));
      } catch (Exception e) {
        log.error("Error extracting/mapping field '{}' from source: {}", fieldName, source, e);
        return Optional.empty();
      }
    }
    return Optional.empty();
  }

  private Optional<String> extractStringField(JsonNode source, String fieldName) {
    return extractValue(source, fieldName, JsonNode::asText);
  }

  private Optional<Integer> extractIntegerField(JsonNode source, String fieldName) {
    return extractValue(source, fieldName, JsonNode::asInt);
  }

  private Optional<Boolean> extractBooleanField(JsonNode source, String fieldName) {
    return extractValue(source, fieldName, JsonNode::asBoolean);
  }

  private Optional<Date> extractDateField(JsonNode source, String fieldName) {
    return extractValue(
      source,
      fieldName,
      node -> {
        String s = node.asText();

        // 1) Date-only: yyyy-MM-dd
        try {
          LocalDate d = LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
          return Date.from(d.atStartOfDay(ZoneOffset.UTC).toInstant());
        } catch (DateTimeParseException ignored) {
          // not a plain date
        }

        // 2) Timestamp with offset or zone (e.g. 2025-11-21T00:00:00.000+00:00)
        try {
          LocalDate d = OffsetDateTime.parse(s).toLocalDate();
          return Date.from(d.atStartOfDay(ZoneOffset.UTC).toInstant());
        } catch (DateTimeParseException ignored) {
          // not an offset datetime
        }

        // 3) ISO instant with 'Z' (e.g. 2025-11-21T00:00:00.000Z)
        // (This is mostly redundant if #2 works, but harmless and explicit.)
        try {
          LocalDate d = Instant.parse(s).atZone(ZoneOffset.UTC).toLocalDate();
          return Date.from(d.atStartOfDay(ZoneOffset.UTC).toInstant());
        } catch (DateTimeParseException e) {
          throw new IllegalArgumentException("Unsupported date format for field '" + fieldName + "': " + s, e);
        }
      });
  }

  private Optional<Date> extractIsoDateField(JsonNode source, String fieldName) {
    return extractValue(
      source,
      fieldName,
      node -> Date.from(java.time.Instant.parse(node.asText()))
    );
  }

  private <T> Optional<List<T>> extractList(
      JsonNode source,
      String fieldName,
      Class<T> clazz,
      java.util.function.Function<JsonNode, T> mapper) {
    JsonNode field = source.get(fieldName);
    if (field != null && field.isArray()) {
      List<T> values = new java.util.ArrayList<>();
      field.forEach(
          item -> {
            try {
              values.add(mapper.apply(item));
            } catch (Exception e) {
              log.error("Error parsing item in list '{}': {}", fieldName, item, e);
            }
          });
      return values.isEmpty() ? Optional.empty() : Optional.of(values);
    }
    return Optional.empty();
  }

  private Optional<List<String>> extractStringList(JsonNode source, String fieldName) {
    return extractList(source, fieldName, String.class, JsonNode::asText);
  }

  private Optional<List<Integer>> extractIntegerList(JsonNode source, String fieldName) {
    return extractList(source, fieldName, Integer.class, JsonNode::asInt);
  }

  private Optional<List<Long>> extractLongList(JsonNode source, String fieldName) {
    return extractList(source, fieldName, Long.class, JsonNode::asLong);
  }

  private <T extends Enum<T>> Optional<Set<T>> extractSet(
      JsonNode source,
      String fieldName,
      Class<T> enumClass,
      java.util.function.Function<String, T> fromString) {
    JsonNode field = source.get(fieldName);
    if (field != null && field.isArray()) {
      Set<T> values = new java.util.HashSet<>();
      field.forEach(
          item -> {
            try {
              values.add(fromString.apply(item.asText()));
            } catch (Exception e) {
              log.error("Error parsing enum for item in list '{}': {}", fieldName, item, e);
            }
          });
      return values.isEmpty() ? Optional.empty() : Optional.of(values);
    }
    return Optional.empty();
  }
}
