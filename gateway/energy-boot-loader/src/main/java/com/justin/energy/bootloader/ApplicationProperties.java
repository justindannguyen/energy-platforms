/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.bootloader;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class ApplicationProperties extends com.justin.energy.common.config.ApplicationProperties {
  public String getBrokerPassword() {
    return props.getProperty("config.mqtt.brokerPassword");
  }

  public String getBrokerUrl() {
    return props.get("config.mqtt.brokerUrl").toString();
  }

  public String getBrokerUsername() {
    return props.getProperty("config.mqtt.brokerUsername");
  }

  public String getFotaParameterTopic() {
    return props.getProperty("fota.mqtt.parameterTopic").toString();
  }

  public String getFotaSoftwareTopic() {
    return props.getProperty("fota.mqtt.softwareTopic").toString();
  }

  public int getReaderApplicationLockPort() {
    return Integer.parseInt(props.getProperty("reader.lock.port"));
  }
}
