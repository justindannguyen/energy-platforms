/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.common.config;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class MqttConfiguration {
  private String brokerUrl;
  private String brokerUsername;
  private String brokerPassword;

  public String getBrokerPassword() {
    return brokerPassword;
  }

  public String getBrokerUrl() {
    return brokerUrl;
  }

  public String getBrokerUsername() {
    return brokerUsername;
  }

  public void setBrokerPassword(final String brokerPassword) {
    this.brokerPassword = brokerPassword;
  }

  public void setBrokerUrl(final String brokerUrl) {
    this.brokerUrl = brokerUrl;
  }

  public void setBrokerUsername(final String brokerUsername) {
    this.brokerUsername = brokerUsername;
  }
}
