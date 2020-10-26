# Literature service

RESTful literature search service.

http://api.gbif-dev.org/v1/literature/search

## GBIF portal

https://www.gbif.org/resource/search?contentType=literature

## Parameters

| Parameter                  | Value                                                                                                       | Description                                                                             |
| -------------------------- | ----------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------- |
| q                          | String                                                                                                      | Simple search parameter. The value for this parameter can be a simple word or a phrase. |
| countriesOfResearcher      | [Country](https://github.com/gbif/gbif-api/blob/master/src/main/java/org/gbif/api/vocabulary/Country.java)  | |
| countriesOfCoverage        | [Country](https://github.com/gbif/gbif-api/blob/master/src/main/java/org/gbif/api/vocabulary/Country.java)  | |
| literatureType             | [Enum](src/main/java/org/gbif/literature/api/LiteratureType.java)                                           | |
| relevance                  | [Enum](src/main/java/org/gbif/literature/api/Relevance.java)                                                | |
| year                       | Integer                                                                                                     | |
| topics                     | [Enum](src/main/java/org/gbif/literature/api/Topic.java)                                                    | |
| gbifDatasetKey             | UUID                                                                                                        | |
| publishingOrganizationKey  | UUID                                                                                                        | |
| peerReview                 | Boolean                                                                                                     | |
| openAccess                 | Boolean                                                                                                     | |
| gbifDownloadKey            | String                                                                                                      | |
| source                     | String                                                                                                      | |
| publisher                  | String                                                                                                      | |
