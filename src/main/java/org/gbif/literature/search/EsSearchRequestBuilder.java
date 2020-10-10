package org.gbif.literature.search;

import org.elasticsearch.action.search.SearchRequest;
import org.gbif.api.model.common.search.FacetedSearchRequest;
import org.gbif.api.model.common.search.SearchParameter;

public class EsSearchRequestBuilder<P extends SearchParameter> {

  private final EsFieldMapper<P> esFieldMapper;

  public EsSearchRequestBuilder(EsFieldMapper<P> esFieldMapper) {
    this.esFieldMapper = esFieldMapper;
  }

  public SearchRequest buildSearchRequest(
      FacetedSearchRequest<P> searchRequest, boolean facetsEnabled, String index) {
    return null;
  }
}
