package org.gbif.literature.config;

import org.gbif.literature.api.LiteratureSearchParameter;
import org.gbif.literature.api.LiteratureSearchRequest;
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
    return getValue(webRequest);
  }
}
