package org.gbif.literature.search;

import org.elasticsearch.search.SearchHit;
import org.gbif.literature.api.LiteratureSearchResult;
import org.gbif.literature.api.LiteratureSuggestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Component
public class LiteratureSearchResultConverter
    implements SearchResultConverter<LiteratureSearchResult, LiteratureSuggestResult> {

  private static final Logger LOG = LoggerFactory.getLogger(LiteratureSearchResultConverter.class);

  private static final Pattern NESTED_PATTERN = Pattern.compile("^\\w+(\\.\\w+)+$");
  private static final Predicate<String> IS_NESTED = s -> NESTED_PATTERN.matcher(s).find();

  @Override
  public LiteratureSearchResult toSearchResult(SearchHit searchHit) {
    LiteratureSearchResult result = new LiteratureSearchResult();
    Map<String, Object> fields = searchHit.getSourceAsMap();

    // TODO: 16/10/2020 key?
    // TODO: 16/10/2020 highlight?
    getStringValue(fields, "abstract").ifPresent(result::setAbstr);
    getStringValue(fields, "accessed").ifPresent(result::setAccessed);
    getBooleanValue(fields, "authored").ifPresent(result::setAuthored);
    getListValue(fields, "authors").ifPresent(result::setAuthors);
    getBooleanValue(fields, "confirmed").ifPresent(result::setConfirmed);
    // TODO: 16/10/2020 map rest of the fields

    return result;
  }

  @Override
  public LiteratureSuggestResult toSearchSuggestResult(SearchHit searchHit) {
    return null;
  }

  private static Optional<String> getStringValue(Map<String, Object> fields, String esField) {
    return getValue(fields, esField, Function.identity());
  }

  private static Optional<Boolean> getBooleanValue(Map<String, Object> fields, String esField) {
    return getValue(fields, esField, Boolean::valueOf);
  }

  private static Optional<List<String>> getListValue(Map<String, Object> fields, String esField) {
    return Optional.ofNullable(fields.get(esField))
        .map(v -> (List<String>) v)
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
