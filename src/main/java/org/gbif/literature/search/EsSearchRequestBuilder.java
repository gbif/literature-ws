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

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.gbif.api.model.common.search.FacetedSearchRequest;
import org.gbif.api.model.common.search.SearchConstants;
import org.gbif.api.model.common.search.SearchParameter;
import org.gbif.common.shaded.com.google.common.annotations.VisibleForTesting;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class EsSearchRequestBuilder<P extends SearchParameter> {

  private final EsFieldMapper<P> esFieldMapper;

  public EsSearchRequestBuilder(EsFieldMapper<P> esFieldMapper) {
    this.esFieldMapper = esFieldMapper;
  }

  public SearchRequest buildSearchRequest(
      FacetedSearchRequest<P> searchRequest, boolean facetsEnabled, String index) {

    SearchRequest esSearchRequest = new SearchRequest();
    esSearchRequest.indices(index);

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    esSearchRequest.source(searchSourceBuilder);
    searchSourceBuilder.fetchSource(esFieldMapper.getMappedFields(), esFieldMapper.excludeFields());

    // size and offset
    searchSourceBuilder.size(searchRequest.getLimit());
    searchSourceBuilder.from((int) searchRequest.getOffset());

    // sort
    // TODO: 16/10/2020

    // group params
    GroupedParams<P> groupedParams = groupParameters(searchRequest);

    // add query
    if (SearchConstants.QUERY_WILDCARD.equals(searchRequest.getQ())) {
      // search all
      searchSourceBuilder.query(QueryBuilders.matchAllQuery());
    } else {
      buildQuery(groupedParams.queryParams, searchRequest.getQ())
          .ifPresent(searchSourceBuilder::query);
    }


    return esSearchRequest;
  }

  private Optional<QueryBuilder> buildQuery(Map<P, Set<String>> params, String qParam) {
    // create bool node
    BoolQueryBuilder bool = QueryBuilders.boolQuery();

    // adding full text search parameter
    if (StringUtils.isNotBlank(qParam)) {
      bool.must(esFieldMapper.fullTextQuery(qParam));
    }

    if (params != null && !params.isEmpty()) {
      // adding term queries to bool
      // TODO: 16/10/2020
    }

    return bool.must().isEmpty() && bool.filter().isEmpty() ? Optional.empty() : Optional.of(bool);
  }

  @VisibleForTesting
  GroupedParams<P> groupParameters(FacetedSearchRequest<P> searchRequest) {
    GroupedParams<P> groupedParams = new GroupedParams<P>();

    if (!searchRequest.isMultiSelectFacets()
        || searchRequest.getFacets() == null
        || searchRequest.getFacets().isEmpty()) {
      groupedParams.queryParams = searchRequest.getParameters();
      return groupedParams;
    }

    groupedParams.queryParams = new HashMap<>();
    groupedParams.postFilterParams = new HashMap<>();

    searchRequest
        .getParameters()
        .forEach(
            (k, v) -> {
              if (searchRequest.getFacets().contains(k)) {
                groupedParams.postFilterParams.put(k, v);
              } else {
                groupedParams.queryParams.put(k, v);
              }
            });

    return groupedParams;
  }

  @VisibleForTesting
  static class GroupedParams<P extends SearchParameter> {
    Map<P, Set<String>> postFilterParams;
    Map<P, Set<String>> queryParams;
  }
}
