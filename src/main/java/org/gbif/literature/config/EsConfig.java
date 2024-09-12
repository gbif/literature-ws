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

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpHost;
import org.elasticsearch.client.NodeSelector;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class EsConfig {

  @Autowired
  private ApplicationContext applicationContext;

  /**
   * Re-creates the instance of the RestHighLevelClient.
   */
  public RestHighLevelClient reCreateRestHighLevelClient() {
    RestHighLevelClient restHighLevelClient = applicationContext.getBean("restHighLevelClient", RestHighLevelClient.class);
    if (!restHighLevelClient.getLowLevelClient().isRunning()) {
      log.warn("Recreating Elasticsearch RestHighLevelClient");
      DefaultSingletonBeanRegistry registry = (DefaultSingletonBeanRegistry) applicationContext.getAutowireCapableBeanFactory();
      registry.destroySingleton("restHighLevelClient");
      registry.registerSingleton("restHighLevelClient", reCreateRestHighLevelClient(applicationContext.getBean(EsClientConfigProperties.class)));
    }
    return restHighLevelClient;
  }

  @Bean("restHighLevelClient")
  @Primary
  public RestHighLevelClient reCreateRestHighLevelClient(EsClientConfigProperties esProperties) {
    return provideEsClient(esProperties);
  }

  public static RestHighLevelClient provideEsClient(EsClientConfigProperties esProperties) {
    String[] hostsUrl = esProperties.getHosts().toArray(new String[0]);
    HttpHost[] hosts = new HttpHost[hostsUrl.length];
    int i = 0;
    for (String host : hostsUrl) {
      try {
        URL url = new URL(host);
        hosts[i] = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
        i++;
      } catch (MalformedURLException e) {
        throw new IllegalArgumentException(e.getMessage(), e);
      }
    }

    return new RestHighLevelClient(
        RestClient.builder(hosts)
            .setRequestConfigCallback(
                requestConfigBuilder ->
                    requestConfigBuilder
                        .setConnectTimeout(esProperties.getConnectionTimeOut())
                        .setSocketTimeout(esProperties.getSocketTimeOut())
                        .setConnectionRequestTimeout(esProperties.getConnectionRequestTimeOut()))
            .setNodeSelector(NodeSelector.SKIP_DEDICATED_MASTERS));
  }
}
