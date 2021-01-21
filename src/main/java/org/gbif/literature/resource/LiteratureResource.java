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
package org.gbif.literature.resource;

import org.gbif.api.model.common.search.SearchResponse;
import org.gbif.api.model.literature.search.LiteratureSearchParameter;
import org.gbif.api.model.literature.search.LiteratureSearchRequest;
import org.gbif.api.model.literature.search.LiteratureSearchResult;
import org.gbif.literature.search.LiteratureSearchService;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "literature", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class LiteratureResource {

  private final LiteratureSearchService searchService;

  public LiteratureResource(LiteratureSearchService searchService) {
    this.searchService = searchService;
  }

  @GetMapping("search")
  public SearchResponse<LiteratureSearchResult, LiteratureSearchParameter> search(
      LiteratureSearchRequest searchRequest) {
    return searchService.search(searchRequest);
  }

  @GetMapping("{id}")
  public ResponseEntity<LiteratureSearchResult> get(@PathVariable("id") UUID id) {
    return searchService.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }
}
