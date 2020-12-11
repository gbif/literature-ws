package org.gbif.literature.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class LiteratureUtils {

  private LiteratureUtils() {
  }

  public static String decodeUrl(String value) {
    return decodeUrl(value, StandardCharsets.UTF_8);
  }

  public static String decodeUrl(String value, Charset charset) {
    try {
      return URLDecoder.decode(value, charset.name());
    } catch (UnsupportedEncodingException uee) {
      return value;
    }
  }
}
