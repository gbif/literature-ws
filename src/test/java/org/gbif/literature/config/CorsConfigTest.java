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

import org.gbif.api.model.common.search.SearchResponse;
import org.gbif.literature.search.LiteratureSearchService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CorsConfigTest {

  private static final String ORIGIN = "https://example.org";

  @MockBean UserDetailsService userDetailsService;
  @MockBean PasswordEncoder passwordEncoder;
  @MockBean LiteratureSearchService searchService;

  @Autowired MockMvc mvc;

  @Test
  void preflightAllowsReadMethods() throws Exception {
    for (String method : new String[] {"GET", "HEAD", "OPTIONS"}) {
      mvc.perform(preflight(method))
          .andExpect(status().isOk())
          .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"));
    }
  }

  @Test
  void preflightRejectsWriteMethods() throws Exception {
    for (String method : new String[] {"POST", "PUT", "DELETE", "PATCH"}) {
      mvc.perform(preflight(method))
          .andExpect(status().isForbidden())
          .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
  }

  @Test
  void crossOriginGetAllowed() throws Exception {
    when(searchService.search(any())).thenReturn(new SearchResponse<>(0, 20));

    mvc.perform(get("/literature/search").header(HttpHeaders.ORIGIN, ORIGIN))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*"));
  }

  private static org.springframework.test.web.servlet.RequestBuilder preflight(String method) {
    return options("/literature/search")
        .header(HttpHeaders.ORIGIN, ORIGIN)
        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, method);
  }
}
