package org.gbif.literature;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class LiteratureApplication {

  public static void main(String[] args) {
    SpringApplication.run(LiteratureApplication.class, args);
  }
}
