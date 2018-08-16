package com.justin.energy.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import zipkin.server.internal.EnableZipkinServer;

@SpringBootApplication
@EnableDiscoveryClient
@EnableZipkinServer
public class EnergyZipkinServerApplication {
  public static void main(final String[] args) {
    SpringApplication.run(EnergyZipkinServerApplication.class, args);
  }
}
