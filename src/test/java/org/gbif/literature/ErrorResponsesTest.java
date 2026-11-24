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
package org.gbif.literature;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Invalid input is rejected before reaching Elasticsearch, through the app's error mappers. */
@SpringBootTest
@AutoConfigureMockMvc
class ErrorResponsesTest {

  @MockBean UserDetailsService userDetailsService;
  @MockBean PasswordEncoder passwordEncoder;

  @Autowired MockMvc mvc;

  @Test
  void invalidDateReturns400() throws Exception {
    mvc.perform(get("/literature/search").param("added", "2024-13-01"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void facetOffsetPlusLimitOverCapReturns400() throws Exception {
    mvc.perform(
            get("/literature/search")
                .param("facet", "gbifOccurrenceKey")
                .param("facetOffset", "500")
                .param("facetLimit", "1000"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void facetOffsetOverIntReturns400() throws Exception {
    mvc.perform(
            get("/literature/search")
                .param("facet", "year")
                .param("year.facetOffset", "4294967301"))
        .andExpect(status().isBadRequest());
  }
}
