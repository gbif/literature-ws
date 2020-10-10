package org.gbif.literature.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@ConfigurationProperties(prefix = "elasticsearch")
public class EsClientConfigProperties {

  private Set<String> hosts;
  private int connectionTimeOut;
  private int socketTimeOut;
  private int connectionRequestTimeOut;

  public Set<String> getHosts() {
    return hosts;
  }

  public void setHosts(Set<String> hosts) {
    this.hosts = hosts;
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
}
