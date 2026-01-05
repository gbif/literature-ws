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
package org.gbif.literature.config;

import org.gbif.api.model.literature.search.LiteratureSearchParameter;
import org.gbif.api.model.literature.search.LiteratureSearchRequest;
import org.gbif.ws.server.provider.FacetedSearchRequestProvider;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class LiteratureSearchRequestHandlerMethodArgumentResolver
    extends FacetedSearchRequestProvider<LiteratureSearchRequest, LiteratureSearchParameter>
    implements HandlerMethodArgumentResolver {

  public LiteratureSearchRequestHandlerMethodArgumentResolver() {
    super(LiteratureSearchRequest.class, LiteratureSearchParameter.class);
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return LiteratureSearchRequest.class.equals(parameter.getParameterType());
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    
    // Get the base search request from the parent provider
    // Standard facet parameters (multiSelectFacets, facetLimit, facetOffset) are automatically handled
    LiteratureSearchRequest searchRequest = super.getValue(webRequest);
    
    // Manually handle facetMinCount - this ES-specific parameter is not handled by parent provider
    String facetMinCountStr = webRequest.getParameter("facetMinCount");
    if (facetMinCountStr != null) {
      try {
        searchRequest.setFacetMinCount(Integer.parseInt(facetMinCountStr));
      } catch (NumberFormatException e) {
        // Ignore invalid values
      }
    }
    
    return searchRequest;
  }
}
