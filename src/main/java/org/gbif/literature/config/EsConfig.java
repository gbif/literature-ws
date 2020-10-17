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
package org.gbif.literature.config;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class EsConfig {

  @Bean
  @Primary
  public RestHighLevelClient restHighLevelClient(RestClient restClient) {
    return new RestHighLevelClient(restClient);
  }

  @Bean
  @Primary
  public RestClient elasticsearchRestClient(EsClientConfigProperties properties) {
    String[] hostsUrl = properties.getHosts().toArray(new String[0]);
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

    return RestClient.builder(hosts)
        .setRequestConfigCallback(
            requestConfigBuilder ->
                requestConfigBuilder
                    .setConnectTimeout(properties.getConnectionTimeOut())
                    .setSocketTimeout(properties.getSocketTimeOut())
                    .setConnectionRequestTimeout(
                        properties.getConnectionRequestTimeOut()))
        .build();
  }

  public static RestHighLevelClient provideEsClient(EsClientConfigProperties esClientConfigProperties) {
    String[] hostsUrl = esClientConfigProperties.getHosts().toArray(new String[0]);
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
                        .setConnectTimeout(esClientConfigProperties.getConnectionTimeOut())
                        .setSocketTimeout(esClientConfigProperties.getSocketTimeOut())
                        .setConnectionRequestTimeout(
                            esClientConfigProperties.getConnectionRequestTimeOut()))
            .build());
  }
}
