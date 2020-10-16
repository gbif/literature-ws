package org.gbif.literature.util;

import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.search.FacetedSearchRequest;
import org.gbif.api.model.common.search.SearchParameter;

import java.util.Optional;

public final class EsQueryUtils {

  // defaults
  private static final int DEFAULT_FACET_OFFSET = 0;
  private static final int DEFAULT_FACET_LIMIT = 10;

  private EsQueryUtils() {
  }

  public static <P extends SearchParameter> int extractFacetLimit(
      FacetedSearchRequest<P> request, P facet) {
    return Optional.ofNullable(request.getFacetPage(facet))
        .map(Pageable::getLimit)
        .orElse(request.getFacetLimit() != null ? request.getFacetLimit() : DEFAULT_FACET_LIMIT);
  }

  public static <P extends SearchParameter> int extractFacetOffset(
      FacetedSearchRequest<P> request, P facet) {
    return Optional.ofNullable(request.getFacetPage(facet))
        .map(v -> (int) v.getOffset())
        .orElse(request.getFacetOffset() != null ? request.getFacetOffset() : DEFAULT_FACET_OFFSET);
  }
}
