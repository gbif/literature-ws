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

import org.gbif.api.model.common.search.SearchResponse;
import org.gbif.api.model.literature.LiteratureRelevance;
import org.gbif.api.model.literature.LiteratureTopic;
import org.gbif.api.model.literature.LiteratureType;
import org.gbif.api.model.literature.search.LiteratureSearchParameter;
import org.gbif.api.model.literature.search.LiteratureSearchRequest;
import org.gbif.api.model.literature.search.LiteratureSearchResult;
import org.gbif.api.vocabulary.Country;
import org.gbif.literature.search.LiteratureSearchService;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
  info = @Info(
    title = "Literature API",
    version = "v1",
    description = "This API enables you to search for literature indexed by GBIF, including peer-reviewed papers citing GBIF datasets and downloads. It powers the Literature search on GBIF.org.",
    termsOfService = "https://www.gbif.org/terms"),
  servers = {@Server(url = "https://api.gbif.org/v1/", description = "Production"),
    @Server(url = "https://api.gbif-uat.org/v1/", description = "User testing")}
)
@RequestMapping(value = "literature", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class LiteratureResource {

  private final LiteratureSearchService searchService;

  public LiteratureResource(LiteratureSearchService searchService) {
    this.searchService = searchService;
  }

  @Operation(summary = "Search literature", description = "Full-text and parameterized search across all literature")
  @Parameters(value = {
    @Parameter(name = "citationType", description = "", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY, explode = Explode.TRUE),
    @Parameter(name = "countriesOfCoverage", description = "Country or area of institution with which author is affiliated, e.g. DK (for Denmark). Country codes are listed in our Country enum.", schema = @Schema(implementation = Country.class), in = ParameterIn.QUERY, explode = Explode.TRUE),
    @Parameter(name = "countriesOfResearcher", description = "Country or area of focus of study, e.g. BR (for Brazil). Country codes are listed in our Country enum.", schema = @Schema(implementation = Country.class), in = ParameterIn.QUERY, explode = Explode.TRUE),
    @Parameter(name = "doi", description = "Digital Object Identifier (DOI)", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY, explode = Explode.TRUE),
    @Parameter(name = "gbifDatasetKey", description = "GBIF dataset referenced in publication", schema = @Schema(implementation = UUID.class), in = ParameterIn.QUERY, explode = Explode.TRUE),
    @Parameter(name = "gbifDownloadKey", description = "Download referenced in publication", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY, explode = Explode.TRUE),
    @Parameter(name = "gbifHigherTaxonKey", description = "", schema = @Schema(implementation = Integer.class), in = ParameterIn.QUERY, explode = Explode.TRUE),
    @Parameter(name = "gbifOccurrenceKey", description = "", schema = @Schema(implementation = Long.class), in = ParameterIn.QUERY, explode = Explode.TRUE),
    @Parameter(name = "gbifTaxonKey", description = "", schema = @Schema(implementation = Integer.class), in = ParameterIn.QUERY, explode = Explode.TRUE),
    @Parameter(name = "literatureType", description = "Type of literature, e.g. journal article.", schema = @Schema(implementation = LiteratureType.class), in = ParameterIn.QUERY, explode = Explode.TRUE),
    @Parameter(name = "openAccess", description = "Is publication Open Access?", schema = @Schema(implementation = Boolean.class), in = ParameterIn.QUERY),
    @Parameter(name = "peerReview", description = "Has publication undergone peer-review?", schema = @Schema(implementation = Boolean.class), in = ParameterIn.QUERY),
    @Parameter(name = "publisher", description = "Publisher of journal", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY, explode = Explode.TRUE),
    @Parameter(name = "publishingOrganizationKey", description = "Publisher whose dataset is referenced in publication", schema = @Schema(implementation = UUID.class), in = ParameterIn.QUERY, explode = Explode.TRUE),
    @Parameter(name = "relevance", description = "Relevance to GBIF community, see #literature relevance#.", schema = @Schema(implementation = LiteratureRelevance.class), in = ParameterIn.QUERY, explode = Explode.TRUE),
    @Parameter(name = "source", description = "Journal of publication", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY, explode = Explode.TRUE),
    @Parameter(name = "topics", description = "Topic of publication", schema = @Schema(implementation = LiteratureTopic.class), in = ParameterIn.QUERY, explode = Explode.TRUE),
    @Parameter(name = "year", description = "Year of publication", schema = @Schema(implementation = Integer.class), in = ParameterIn.QUERY, explode = Explode.TRUE),

    @Parameter(name = "facet", description = "A facet name used to retrieve the most frequent values for a field. This parameter may by repeated to request multiple facets", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),
    @Parameter(name = "facetLimit", description = "Facet parameters allow paging requests using the parameters facetOffset and facetLimit", schema = @Schema(implementation = Integer.class), in = ParameterIn.QUERY),
    @Parameter(name = "facetOffset", description = "Facet parameters allow paging requests using the parameters facetOffset and facetLimit", schema = @Schema(implementation = Integer.class, minimum = "0"), in = ParameterIn.QUERY),
    @Parameter(name = "facetMincount", description = "Used in combination with the facet parameter. Set facetMincount={#} to exclude facets with a count less than {#}, e.g. /search?facet=type&limit=0&facetMincount=10000 only shows the type value 'OCCURRENCE' because 'CHECKLIST' and 'METADATA' have counts less than 10000.", schema = @Schema(implementation = Integer.class), in = ParameterIn.QUERY),
    @Parameter(name = "facetMultiselect", description = "Used in combination with the facet parameter. Set facetMultiselect=true to still return counts for values that are not currently filtered, e.g. /search?facet=type&limit=0&type=CHECKLIST&facetMultiselect=true still shows type values 'OCCURRENCE' and 'METADATA' even though type is being filtered by type=CHECKLIST", schema = @Schema(implementation = Boolean.class), in = ParameterIn.QUERY),

    @Parameter(name = "hl", description = "Set hl=true to highlight terms matching the query when in fulltext search fields. The highlight will be an emphasis tag of class 'gbifH1' e.g. /search?q=plant&hl=true. Fulltext search fields include: title, keyword, country, publishing country, publishing organization title, hosting organization title, and description. One additional full text field is searched which includes information from metadata documents, but the text of this field is not returned in the response.", schema = @Schema(implementation = Boolean.class), in = ParameterIn.QUERY),
    @Parameter(name = "q", description = "Simple full text search parameter. The value for this parameter can be a simple word or a phrase. Wildcards are not supported", schema = @Schema(implementation = String.class), in = ParameterIn.QUERY),

    @Parameter(name = "limit", description = "Controls the number of results in the page. Using too high a value will be overwritten with the default maximum threshold, depending on the service. Sensible defaults are used so this may be omitted.", schema = @Schema(implementation = Integer.class, minimum = "0"), in = ParameterIn.QUERY),
    @Parameter(name = "offset", description = "Determines the offset for the search results. A limit of 20 and offset of 20, will get the second page of 20 results. ", schema = @Schema(implementation = Integer.class, minimum = "0"), in = ParameterIn.QUERY)
  })
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Literature items found",
      content = {@Content(mediaType = "application/json",
        schema = @Schema(implementation = LiteratureSearchResult.class))}),
    @ApiResponse(responseCode = "400", description = "Invalid search query",
      content = @Content)})
  @GetMapping("search")
  public SearchResponse<LiteratureSearchResult, LiteratureSearchParameter> search(
    @Parameter(hidden = true) LiteratureSearchRequest searchRequest) {
    return searchService.search(searchRequest);
  }

  @Operation(summary = "Literature item by id", description = "Retrieve details for a single literature item")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Literature item found",
      content = {@Content(mediaType = "application/json",
        schema = @Schema(implementation = LiteratureSearchResult.class))}),
    @ApiResponse(responseCode = "400", description = "Invalid identifier supplied",
      content = @Content),
    @ApiResponse(responseCode = "404", description = "Literature item not found",
      content = @Content)})
  @GetMapping("{uuid}")
  public ResponseEntity<LiteratureSearchResult> get(@PathVariable("uuid") @Parameter(description = "UUID for the literature item", example = "83a00190-7038-3970-a7e8-5e5563c40e37") UUID uuid) {
    return searchService.get(uuid).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }
}
