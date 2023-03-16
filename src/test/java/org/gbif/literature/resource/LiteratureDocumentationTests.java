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
package org.gbif.literature.resource;

import org.gbif.api.model.literature.search.LiteratureSearchParameter;
import org.gbif.literature.search.LiteratureEsFieldMapper;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.annotations.Parameters;

import static org.springframework.test.util.AssertionErrors.assertTrue;

public class LiteratureDocumentationTests {

  @Test
  public void searchParametersDocumented() {
    Set documentedParameters =
        Arrays.stream(
                LiteratureResource.CommonSearchParameters.class
                    .getAnnotation(Parameters.class)
                    .value())
            .map(p -> p.name())
            .collect(Collectors.toSet());

    LiteratureEsFieldMapper fieldMapper = new LiteratureEsFieldMapper();

    for (LiteratureSearchParameter param : LiteratureSearchParameter.values()) {
      if (param == LiteratureSearchParameter.DOI) {
        continue; // Handled specially.
      }
      String name = fieldMapper.get(param);
      assertTrue(param + "/" + name + " is not documented", documentedParameters.contains(name));
    }
  }
}
