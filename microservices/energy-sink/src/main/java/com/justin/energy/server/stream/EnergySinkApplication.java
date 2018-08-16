package com.justin.energy.server.stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EnergySinkApplication {
  public static void main(final String[] args) {
    SpringApplication.run(EnergySinkApplication.class, args);
  }
}
