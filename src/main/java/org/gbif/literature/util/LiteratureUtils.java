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
package org.gbif.literature.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class LiteratureUtils {

  private LiteratureUtils() {}

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
