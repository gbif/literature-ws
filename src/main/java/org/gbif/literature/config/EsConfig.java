package org.gbif.literature.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.elasticsearch.client.RestClient;

import java.net.MalformedURLException;
import java.net.URL;

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
