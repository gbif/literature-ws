package org.gbif.literature.search;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.gbif.api.model.common.search.SearchParameter;

public interface EsFieldMapper<P extends SearchParameter> {

  String get(P searchParameter);

  P get(String esFieldName);

  String[] excludeFields();

  default QueryBuilder fullTextQuery(String q) {
    return QueryBuilders.matchQuery("all", q);
  }

  default String[] getMappedFields() {
    return new String[0];
  }
}
