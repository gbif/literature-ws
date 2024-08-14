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

import org.gbif.api.documentation.CommonParameters;
import org.gbif.api.model.common.export.ExportFormat;
import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.search.FacetedSearchRequest;
import org.gbif.api.model.common.search.SearchResponse;
import org.gbif.api.model.literature.LiteratureRelevance;
import org.gbif.api.model.literature.LiteratureTopic;
import org.gbif.api.model.literature.LiteratureType;
import org.gbif.api.model.literature.search.LiteratureSearchParameter;
import org.gbif.api.model.literature.search.LiteratureSearchRequest;
import org.gbif.api.model.literature.search.LiteratureSearchResult;
import org.gbif.api.vocabulary.Country;
import org.gbif.api.vocabulary.Language;
import org.gbif.literature.export.CsvWriter;
import org.gbif.literature.export.LiteraturePager;
import org.gbif.literature.search.LiteratureSearchService;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.links.LinkParameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;

@OpenAPIDefinition(
    info =
        @Info(
            title = "Literature API",
            version = "v1",
            description =
                "This API enables you to search for literature indexed by GBIF, including peer-reviewed "
                    + "papers citing GBIF datasets and downloads. It powers the "
                    + "[Literature search](https://www.gbif.org/resource/search?contentType=literature) on GBIF.org.",
            termsOfService = "https://www.gbif.org/terms"),
    servers = {
      @Server(url = "https://api.gbif.org/v1/", description = "Production"),
      @Server(url = "https://api.gbif-uat.org/v1/", description = "User testing")
    })
@Tag(name = "Literature", description = "Literature indexed by GBIF")
@RequestMapping(value = "literature", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class LiteratureResource {

  // Export header prefix
  private static final String FILE_HEADER_PRE = "attachment; filename=literature_";

  // Page size to iterate over literature search export service
  private static final int EXPORT_PAGE_LIMIT = 5_000;

  private final LiteratureSearchService searchService;

  public LiteratureResource(LiteratureSearchService searchService) {
    this.searchService = searchService;
  }

  private static final String REPEATED =
      "\n\n*This parameter may be repeated to search for multiple values.*";

  // Search parameters shared between the standard search and the export.
  @Target({METHOD, ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  @Parameters(
      value = {
        @Parameter(
            name = "citationType",
            description =
                "The manner in which GBIF is cited in a paper.\n\n"
                    + "Make a [facet query](https://api.gbif.org/v1/literature/search?limit=0&facet=citationType) for available values."
                    + REPEATED,
            array = @ArraySchema(schema = @Schema(implementation = String.class)),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "countriesOfCoverage",
            description =
                "Country or area of focus of study. Country codes are listed in our "
                    + "[Country enum](https://api.gbif.org/v1/enumeration/country)."
                    + REPEATED,
            array = @ArraySchema(schema = @Schema(implementation = Country.class)),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "countriesOfResearcher",
            description =
                "Country or area of institution with which author is affiliated. Country codes are listed in our "
                    + "[Country enum](https://api.gbif.org/v1/enumeration/country)."
                    + REPEATED,
            array = @ArraySchema(schema = @Schema(implementation = Country.class)),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "doi",
            description = "Digital Object Identifier (DOI) of the literature item." + REPEATED,
            array = @ArraySchema(schema = @Schema(implementation = String.class)),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "gbifDatasetKey",
            description = "GBIF dataset referenced in publication." + REPEATED,
            schema = @Schema(implementation = UUID.class),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "gbifDownloadKey",
            description = "GBIF download referenced in publication." + REPEATED,
            array = @ArraySchema(schema = @Schema(implementation = String.class)),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "gbifHigherTaxonKey",
            description =
                "All parent keys of any taxon that is the focus of the paper (see `gbifTaxonKey`)"
                    + REPEATED,
            array = @ArraySchema(schema = @Schema(implementation = Integer.class)),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "gbifNetworkKey",
            description = "GBIF network referenced in publication." + REPEATED,
            schema = @Schema(implementation = UUID.class),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "gbifOccurrenceKey",
            description = "Any GBIF occurrence keys directly mentioned in a paper." + REPEATED,
            array = @ArraySchema(schema = @Schema(implementation = Long.class)),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "gbifProjectIdentifier", // TODO
            description = "GBIF dataset referenced in publication." + REPEATED,
            schema = @Schema(implementation = UUID.class),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "gbifProgrammeAcronym", // TODO
            description = "GBIF dataset referenced in publication." + REPEATED,
            schema = @Schema(implementation = UUID.class),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "gbifTaxonKey",
            description =
                "Key(s) from the GBIF backbone of taxa that are the focus of a paper." + REPEATED,
            array = @ArraySchema(schema = @Schema(implementation = Integer.class)),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "literatureType",
            description = "Type of literature, e.g. journal article." + REPEATED,
            array = @ArraySchema(schema = @Schema(implementation = LiteratureType.class)),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "openAccess",
            description = "Is the publication Open Access?",
            schema = @Schema(implementation = Boolean.class),
            in = ParameterIn.QUERY),
        @Parameter(
            name = "peerReview",
            description = "Has the publication undergone peer review?",
            schema = @Schema(implementation = Boolean.class),
            in = ParameterIn.QUERY),
        @Parameter(
            name = "publisher",
            description = "Publisher of journal." + REPEATED,
            array = @ArraySchema(schema = @Schema(implementation = String.class)),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "publishingOrganizationKey",
            description = "Publisher whose dataset is referenced in publication." + REPEATED,
            array = @ArraySchema(schema = @Schema(implementation = UUID.class)),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "relevance",
            description =
                "Relevance to GBIF community, see [literature relevance](https://www.gbif.org/faq?question=literature-relevance)."
                    + REPEATED,
            array = @ArraySchema(schema = @Schema(implementation = LiteratureRelevance.class)),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "source",
            description = "Journal of publication." + REPEATED,
            array = @ArraySchema(schema = @Schema(implementation = String.class)),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "topics",
            description = "Topic of publication." + REPEATED,
            array = @ArraySchema(schema = @Schema(implementation = LiteratureTopic.class)),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "year",
            description =
                "Year of publication.  This can be a single range such as "
                    + "`2019,2021`, or can be repeated to search multiple years.",
            schema = @Schema(implementation = Integer.class),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "language",
            description =
                "Language of publication. Language codes are listed in our "
                    + "[Language enum](https://api.gbif.org/v1/enumeration/language)."
                    + REPEATED,
            schema = @Schema(implementation = Language.class),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "added",
            description =
                "Date or date range when the publication was added. Format is ISO 8601, e.g., '2024-07-14' or '2024-07-14,2024-08-14'.",
            schema = @Schema(implementation = Date.class),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "published",
            description =
                "Date or date range when the publication was published. Format is ISO 8601, e.g., '2024-02-22' or '2024-02-22,2024-03-22'.",
            schema = @Schema(implementation = Date.class),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "discovered",
            description =
                "Date or date range when the publication was discovered. Format is ISO 8601, e.g., '2024-02-26' or '2024-02-26,2024-03-26'.",
            schema = @Schema(implementation = Date.class),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE),
        @Parameter(
            name = "modified",
            description =
                "Date or date range when the publication was discovered. Format is ISO 8601, e.g., '2024-07-26' or '2024-07-26,2024-10-26'.",
            schema = @Schema(implementation = Date.class),
            in = ParameterIn.QUERY,
            explode = Explode.TRUE)
      })
  @CommonParameters.QParameter
  @interface CommonSearchParameters {}

  @Operation(
      summary = "Search literature",
      description = "Full-text and parameterized search across all literature")
  @CommonSearchParameters
  @CommonParameters.HighlightParameter
  @Pageable.OffsetLimitParameters
  @FacetedSearchRequest.FacetParameters
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Literature items found",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = LiteratureSearchResult.class))
            },
            links = {
              @Link(
                  name = "GetLiteratureById",
                  operationId = "getLiteratureById",
                  parameters = {@LinkParameter(name = "uuid", expression = "$response.body#/id")})
            }),
        @ApiResponse(responseCode = "400", description = "Invalid search query", content = @Content)
      })
  @GetMapping("search")
  public SearchResponse<LiteratureSearchResult, LiteratureSearchParameter> search(
      @Parameter(hidden = true) LiteratureSearchRequest searchRequest) {
    return searchService.search(searchRequest);
  }

  @Operation(
      operationId = "getLiteratureById",
      summary = "Literature item by id",
      description = "Retrieve details for a single literature item")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Literature item found",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = LiteratureSearchResult.class))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid identifier supplied",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "Literature item not found",
            content = @Content)
      })
  @GetMapping("{uuid}")
  public ResponseEntity<LiteratureSearchResult> get(
      @PathVariable("uuid")
          @Parameter(
              description = "UUID for the literature item",
              example = "83a00190-7038-3970-a7e8-5e5563c40e37")
          UUID uuid) {
    return searchService
        .get(uuid)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(
      summary = "Export literature search results",
      description = "Exports the result of a literature search.")
  @Parameters(
      value = {
        @Parameter(
            name = "format",
            description = "The format for the search results export. Defaults to `TSV`.",
            in = ParameterIn.QUERY)
      })
  @CommonSearchParameters
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Literature search results export."),
        @ApiResponse(responseCode = "400", description = "Invalid search query", content = @Content)
      })
  @GetMapping(value = "export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public void export(
      HttpServletResponse response,
      @Parameter(hidden = true) LiteratureSearchRequest searchRequest,
      @RequestParam(value = "format", defaultValue = "TSV") ExportFormat format)
      throws IOException {

    response.setHeader(
        HttpHeaders.CONTENT_DISPOSITION,
        FILE_HEADER_PRE + System.currentTimeMillis() + '.' + format.name().toLowerCase());

    try (Writer writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()))) {
      CsvWriter.literatureSearchResultCsvWriter(
              new LiteraturePager(searchService, searchRequest, EXPORT_PAGE_LIMIT), format)
          .export(writer);
    }
  }
}
