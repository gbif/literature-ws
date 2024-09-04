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
package org.gbif.literature.export;

import org.gbif.api.model.common.export.ExportFormat;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.model.literature.LiteratureTopic;
import org.gbif.api.model.literature.LiteratureType;
import org.gbif.api.model.literature.search.LiteratureSearchResult;
import org.gbif.api.vocabulary.Country;

import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.supercsv.cellprocessor.FmtBool;
import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.io.dozer.CsvDozerBeanWriter;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.quote.AlwaysQuoteMode;
import org.supercsv.util.CsvContext;

import com.fasterxml.jackson.databind.util.StdDateFormat;

import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;

@Data
@Builder
public class CsvWriter<T> {

  // Delimiter used for list/array of elements
  public static final String ARRAY_DELIMITER = "|";

  private final String[] header;

  private final String[] fields;

  private final CellProcessor[] processors;

  private final LiteraturePager pager;

  private final ExportFormat preference;

  private final int exportPageLimit;

  // Use dozer if set to true.
  private Class<?> forClass;

  private CsvPreference csvPreference() {
    if (ExportFormat.CSV == preference) {
      return new CsvPreference.Builder(CsvPreference.STANDARD_PREFERENCE)
          .useQuoteMode(new AlwaysQuoteMode())
          .build();
    } else if (ExportFormat.TSV == preference) {
      return new CsvPreference.Builder(CsvPreference.TAB_PREFERENCE)
          .useQuoteMode(new AlwaysQuoteMode())
          .build();
    }
    throw new IllegalArgumentException("Export format not supported " + preference);
  }

  @SneakyThrows
  public void export(Writer writer) {
    if (forClass != null) {
      exportUsingDozerBeanWriter(writer);
    } else {
      exportUsingBeanWriter(writer);
    }
  }

  @SneakyThrows
  private void exportUsingBeanWriter(Writer writer) {
    try (ICsvBeanWriter beanWriter = new CsvBeanWriter(writer, csvPreference())) {
      beanWriter.writeHeader(header);
      while (true) {
        PagingResponse<LiteratureSearchResult> response = pager.nextPage(exportPageLimit);
        for (LiteratureSearchResult result : response.getResults()) {
          beanWriter.write(result, fields, processors);
        }
        if (response.isEndOfRecords() || response.getResults().isEmpty()) {
          break;
        }
      }
    }
  }

  @SneakyThrows
  private void exportUsingDozerBeanWriter(Writer writer) {
    try (CsvDozerBeanWriter beanWriter = new CsvDozerBeanWriter(writer, csvPreference())) {
      beanWriter.writeHeader(header);
      beanWriter.configureBeanMapping(forClass, fields);
      while (true) {
        PagingResponse<LiteratureSearchResult> response = pager.nextPage(exportPageLimit);
        for (LiteratureSearchResult result : response.getResults()) {
          beanWriter.write(result, processors);
        }
        if (response.isEndOfRecords() || response.getResults().isEmpty()) {
          break;
        }
      }
    }
  }

  /** Creates an CsvWriter/exporter of DatasetSearchResult. */
  public static CsvWriter<LiteratureSearchResult> literatureSearchResultCsvWriter(
      LiteraturePager pager, ExportFormat preference, int exportPageLimit) {
    return CsvWriter.<LiteratureSearchResult>builder()
        .fields(
            new String[] {
              "title",
              "authors",
              "source",
              "discovered",
              "published",
              "openAccess",
              "peerReview",
              "citationType",
              "countriesOfCoverage",
              "countriesOfResearcher",
              "keywords",
              "literatureType",
              "websites",
              "identifiers",
              "id",
              "abstract",
              "topics",
              "added",
              "gbifDownloadKey"
            })
        .header(
            new String[] {
              "title",
              "authors",
              "source",
              "discovered",
              "published",
              "open_access",
              "peer_review",
              "citation_type",
              "countries_of_coverage",
              "countries_of_researcher",
              "keywords",
              "literature_type",
              "websites",
              "identifiers",
              "id",
              "abstract",
              "topics",
              "added",
              "gbif_download_key"
            })
        .processors(
            new CellProcessor[] {
              new Optional(new CleanStringProcessor()), // title
              new Optional(new AuthorProcessor()), //  authors
              new Optional(new CleanStringProcessor()), // source,
              new Optional(new CleanStringProcessor()), // discovered,
              new Optional(new FmtDate(StdDateFormat.DATE_FORMAT_STR_ISO8601)), // published,
              new Optional(new FmtBool("true", "false")), // openAccess,
              new Optional(new FmtBool("true", "false")), // peerReview,
              new Optional(new CleanStringProcessor()), // citationType,
              new Optional(new CountrySetProcessor()), // countriesOfCoverage,
              new Optional(new CountrySetProcessor()), // countriesOfResearcher,
              new Optional(new ListStringProcessor()), // keywords,
              new Optional(new LiteratureTypeProcessor()), // literatureType,
              new Optional(new ListStringProcessor()), // websites,
              new Optional(new IdentifiersProcessor()), // identifiers,
              new Optional(new UUIDProcessor()), // id,
              new Optional(new CleanStringProcessor()), // abstract,
              new Optional(new SetLiteratureTopicProcessor()), // topics,
              new Optional(new FmtDate(StdDateFormat.DATE_FORMAT_STR_ISO8601)), // added,
              new Optional(new ListStringProcessor()) // gbifDownloadKey
            })
        .preference(preference)
      .pager(pager)
      .exportPageLimit(exportPageLimit)
        .build();
  }

  /** Null aware UUID processor. */
  public static class UUIDProcessor implements CellProcessor {
    @Override
    public String execute(Object value, CsvContext csvContext) {
      return value != null ? ((UUID) value).toString() : "";
    }
  }

  public static class LiteratureTypeProcessor implements CellProcessor {
    @Override
    public String execute(Object value, CsvContext csvContext) {
      return value != null ? ((LiteratureType) value).name() : "";
    }
  }

  /** Null aware List of UUIDs processor. */
  public static class SetLiteratureTopicProcessor implements CellProcessor {
    @Override
    public String execute(Object value, CsvContext csvContext) {
      return value != null
          ? ((Set<LiteratureTopic>) value)
              .stream().map(LiteratureTopic::name).collect(Collectors.joining(ARRAY_DELIMITER))
          : "";
    }
  }

  /** Null aware UUID processor. */
  public static class DOIProcessor implements CellProcessor {
    @Override
    public String execute(Object value, CsvContext csvContext) {
      return value != null ? value.toString() : "";
    }
  }

  /**
   * Produces a String instance clean of delimiter. If the value is null an empty string is
   * returned. Borrowed from Occurrence Downloads!!.
   */
  public static class CleanStringProcessor implements CellProcessor {

    private static final String DELIMETERS_MATCH =
        "\\t|\\n|\\r|(?:(?>\\u000D\\u000A)|[\\u000A\\u000B\\u000C\\u000D\\u0085\\u2028\\u2029\\u0000])";

    private static final Pattern DELIMETERS_MATCH_PATTERN = Pattern.compile(DELIMETERS_MATCH);

    public static String cleanString(String value) {
      return DELIMETERS_MATCH_PATTERN.matcher(value).replaceAll(" ");
    }

    @Override
    public String execute(Object value, CsvContext context) {
      return value != null ? CleanStringProcessor.cleanString((String) value) : "";
    }
  }

  /** Null aware List<String> processor. */
  public static class ListStringProcessor implements CellProcessor {

    public static String toString(List<String> value) {
      return value.stream()
          .map(CleanStringProcessor::cleanString)
          .collect(Collectors.joining(ARRAY_DELIMITER));
    }

    @Override
    public String execute(Object value, CsvContext csvContext) {
      return value != null ? toString((List<String>) value) : "";
    }
  }

  /** Joins elements using as a delimiter. */
  public static String notNullJoiner(String delimiter, String... elements) {
    return Arrays.stream(elements)
        .filter(s -> s != null && !s.isEmpty())
        .collect(Collectors.joining(delimiter));
  }

  public static class AuthorProcessor implements CellProcessor {

    public static String toString(List<Map<String, Object>> authors) {
      return authors.stream()
          .map(t -> CleanStringProcessor.cleanString(t.get("firstName") + " " + t.get("lastName")))
          .collect(Collectors.joining(ARRAY_DELIMITER));
    }

    @Override
    public String execute(Object value, CsvContext csvContext) {
      return value != null ? toString(((List<Map<String, Object>>) value)) : "";
    }
  }

  public static class IdentifiersProcessor implements CellProcessor {

    public static String toString(Map<String, Object> identifiers) {
      return identifiers.values().stream()
          .map(o -> CleanStringProcessor.cleanString(o.toString()))
          .collect(Collectors.joining(ARRAY_DELIMITER));
    }

    @Override
    public String execute(Object value, CsvContext csvContext) {
      return value != null ? toString(((Map<String, Object>) value)) : "";
    }
  }

  public static class CountrySetProcessor implements CellProcessor {

    @Override
    public String execute(Object value, CsvContext csvContext) {
      return value != null
          ? ((Set<Country>) value)
              .stream().map(Country::name).collect(Collectors.joining(ARRAY_DELIMITER))
          : "";
    }
  }
}
