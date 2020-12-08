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
package org.gbif.literature.search;

import org.gbif.api.service.common.SearchService;
import org.gbif.literature.api.LiteratureSearchParameter;
import org.gbif.literature.api.LiteratureSearchRequest;
import org.gbif.literature.api.LiteratureSearchResult;

import java.util.Optional;
import java.util.UUID;

public interface LiteratureSearchService
    extends SearchService<
        LiteratureSearchResult, LiteratureSearchParameter, LiteratureSearchRequest> {

  Optional<LiteratureSearchResult> get(UUID id);
}
