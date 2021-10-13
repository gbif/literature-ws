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

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "elasticsearch")
public class EsClientConfigProperties {

  private Set<String> hosts;
  private String index;
  private int connectionTimeOut;
  private int socketTimeOut;
  private int connectionRequestTimeOut;
  private int maxResultWindow;

  public Set<String> getHosts() {
    return hosts;
  }

  public void setHosts(Set<String> hosts) {
    this.hosts = hosts;
  }

  public String getIndex() {
    return index;
  }

  public void setIndex(String index) {
    this.index = index;
  }

  public int getConnectionTimeOut() {
    return connectionTimeOut;
  }

  public void setConnectionTimeOut(int connectionTimeOut) {
    this.connectionTimeOut = connectionTimeOut;
  }

  public int getSocketTimeOut() {
    return socketTimeOut;
  }

  public void setSocketTimeOut(int socketTimeOut) {
    this.socketTimeOut = socketTimeOut;
  }

  public int getConnectionRequestTimeOut() {
    return connectionRequestTimeOut;
  }

  public void setConnectionRequestTimeOut(int connectionRequestTimeOut) {
    this.connectionRequestTimeOut = connectionRequestTimeOut;
  }

  public int getMaxResultWindow() {
    return maxResultWindow;
  }

  public void setMaxResultWindow(int maxResultWindow) {
    this.maxResultWindow = maxResultWindow;
  }
}
