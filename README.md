# Literature service

RESTful literature search service.


## GBIF portal

https://www.gbif.org/resource/search?contentType=literature

## Search and parameters

http://api.gbif.org/v1/literature/search


| Parameter                 | Value                                                                                                      | Description                                                                             |
|---------------------------|------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| q                         | String                                                                                                     | Simple search parameter. The value for this parameter can be a simple word or a phrase. |
| countriesOfResearcher     | [Country](https://github.com/gbif/gbif-api/blob/master/src/main/java/org/gbif/api/vocabulary/Country.java) | Country of institution with which author is affiliated, e.g. DK (for Denmark)           |
| countriesOfCoverage       | [Country](https://github.com/gbif/gbif-api/blob/master/src/main/java/org/gbif/api/vocabulary/Country.java) | Country of focus of study, e.g. BR (for Brazil)                                         |
| literatureType            | [Enum](src/main/java/org/gbif/literature/api/LiteratureType.java)                                          | Type of literature, e.g. journal article                                                |
| relevance                 | [Enum](src/main/java/org/gbif/literature/api/Relevance.java)                                               | Relevance to GBIF community, see https://www.gbif.org/faq?question=literature-relevance |
| year                      | Integer                                                                                                    | Year of publication                                                                     |
| topics                    | [Enum](src/main/java/org/gbif/literature/api/Topic.java)                                                   | Topic of publication                                                                    |
| gbifDatasetKey            | UUID                                                                                                       | GBIF dataset referenced in publication                                                  |
| publishingOrganizationKey | UUID                                                                                                       | Publisher whose dataset is referenced in publication                                    |
| peerReview                | Boolean                                                                                                    | Has publication undergone peer-review?                                                  |
| openAccess                | Boolean                                                                                                    | Is publication Open Access?                                                             |
| gbifDownloadKey           | String                                                                                                     | Download referenced in publication                                                      |
| doi                       | String                                                                                                     | Digital Object Identifier (DOI)                                                         |
| source                    | String                                                                                                     | Journal of publication                                                                  |
| publisher                 | String                                                                                                     | Publisher of journal                                                                    |


## Find by id

http://api.gbif.org/v1/literature/{id}
