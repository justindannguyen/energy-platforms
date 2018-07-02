/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.reader.config;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class ApplicationProperties extends com.justin.energy.common.config.ApplicationProperties {
  public int getApplicationLockPort() {
    return Integer.parseInt(props.getProperty("lock.port"));
  }

  public String getConfigApiPath() {
    return props.get("config.api.path").toString();
  }

  public String getDeviceStatusTopic() {
    return props.getProperty("config.mqtt.deviceStatusTopic");
  }

  public String getEnergyReportTopic() {
    return props.getProperty("config.mqtt.energyReportTopic");
  }

  public String getFotaParameterTopic() {
    return props.getProperty("fota.mqtt.parameterTopic").toString();
  }

  public String getFotaSoftwareTopic() {
    return props.getProperty("fota.mqtt.softwareTopic").toString();
  }
}
