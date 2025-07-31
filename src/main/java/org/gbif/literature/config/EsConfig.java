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
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class EsConfig {

  private static final Logger log = LoggerFactory.getLogger(EsConfig.class);

  @Autowired private ApplicationContext applicationContext;

  /**
   * Re-creates the instance of the ElasticsearchClient.
   */
  public ElasticsearchClient reCreateElasticsearchClient() {
    ElasticsearchClient elasticsearchClient =
        applicationContext.getBean("elasticsearchClient", ElasticsearchClient.class);

    // Check if the underlying transport is still healthy
    try {
      elasticsearchClient.ping();
      return elasticsearchClient;
    } catch (Exception e) {
      log.warn("Recreating Elasticsearch client due to unhealthy connection", e);
      DefaultSingletonBeanRegistry registry =
          (DefaultSingletonBeanRegistry) applicationContext.getAutowireCapableBeanFactory();
      registry.destroySingleton("elasticsearchClient");
      registry.registerSingleton(
          "elasticsearchClient",
          elasticsearchClient(applicationContext.getBean(EsClientConfigProperties.class)));
      return applicationContext.getBean("elasticsearchClient", ElasticsearchClient.class);
    }
  }

  @Bean("elasticsearchClient")
  @Primary
  public ElasticsearchClient elasticsearchClient(EsClientConfigProperties esProperties) {
    return provideEsClient(esProperties);
  }

  public static ElasticsearchClient provideEsClient(EsClientConfigProperties esProperties) {
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

    // Create the low-level REST client
    RestClient restClient = RestClient.builder(hosts)
        .setRequestConfigCallback(
            requestConfigBuilder ->
                requestConfigBuilder
                    .setConnectTimeout(esProperties.getConnectionTimeOut())
                    .setSocketTimeout(esProperties.getSocketTimeOut())
                    .setConnectionRequestTimeout(esProperties.getConnectionRequestTimeOut()))
        .build();

    // Create the transport with a Jackson mapper
    ElasticsearchTransport transport = new RestClientTransport(
        restClient, new JacksonJsonpMapper());

    // Create and return the API client
    return new ElasticsearchClient(transport);
  }
}
