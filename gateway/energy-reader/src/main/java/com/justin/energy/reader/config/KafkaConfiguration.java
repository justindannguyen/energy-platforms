/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.reader.config;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class KafkaConfiguration {
  private String bootstrapServers;

  public String getBootstrapServers() {
    return bootstrapServers;
  }

  public void setBootstrapServers(final String bootstrapServers) {
    this.bootstrapServers = bootstrapServers;
  }
}
