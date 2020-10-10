package org.gbif.literature.search;

import org.elasticsearch.action.search.SearchRequest;
import org.gbif.api.model.common.search.SearchResponse;
import org.gbif.literature.api.LiteratureSearchRequest;
import org.gbif.literature.api.LiteratureSearchResult;
import org.elasticsearch.client.RestHighLevelClient;
import org.gbif.literature.api.LiteratureSearchParameter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class LiteratureSearchServiceEs implements LiteratureSearchService {

  private final RestHighLevelClient restHighLevelClient;
  private final LiteratureEsResponseParser esResponseParser;
  private final EsSearchRequestBuilder<LiteratureSearchParameter> esSearchRequestBuilder;
  private final String index;

  public LiteratureSearchServiceEs(
      @Value("${elasticsearch.index}") String index,
      RestHighLevelClient restHighLevelClient,
      LiteratureEsResponseParser esResponseParser,
      EsSearchRequestBuilder<LiteratureSearchParameter> esSearchRequestBuilder) {
    this.index = index;
    this.restHighLevelClient = restHighLevelClient;
    this.esResponseParser = esResponseParser;
    this.esSearchRequestBuilder = esSearchRequestBuilder;
  }

  @Override
  public SearchResponse<LiteratureSearchResult, LiteratureSearchParameter> search(LiteratureSearchRequest literatureSearchRequest) {
    try {
      SearchRequest searchRequest =
          esSearchRequestBuilder.buildSearchRequest(literatureSearchRequest, true, index);
      return esResponseParser.buildSearchResponse(
          restHighLevelClient.search(searchRequest), literatureSearchRequest);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }
}
