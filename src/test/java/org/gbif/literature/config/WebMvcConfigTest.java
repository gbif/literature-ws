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
package org.gbif.literature.config;

import org.gbif.api.model.literature.search.LiteratureSearchResult;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebMvcConfigTest {

  @Test
  void localDatesSerializedAsIsoDates() throws Exception {
    ObjectMapper mapper = new WebMvcConfig().registryObjectMapper();
    LiteratureSearchResult result = new LiteratureSearchResult();
    result.setAdded(LocalDate.of(2024, 7, 14));
    result.setPublished(LocalDate.of(2024, 2, 22));
    result.setModified(LocalDate.of(2024, 7, 26));

    JsonNode json = mapper.readTree(mapper.writeValueAsString(result));

    assertEquals("2024-07-14", json.get("added").asText());
    assertEquals("2024-02-22", json.get("published").asText());
    assertEquals("2024-07-26", json.get("modified").asText());
  }
}
