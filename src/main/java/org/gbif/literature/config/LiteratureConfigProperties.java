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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "literature")
@Data
public class LiteratureConfigProperties {

  /** Maximum bytes written to a single export response before aborting. */
  private long bufferLimitBytesExport = 200_000_000L;

  /** Maximum number of literature records exported in one request. */
  private int maxExportRecords = 100_000;

  /** Page size for each Elasticsearch export request (PIT + search_after). */
  private int exportPageSize = 500;
}
