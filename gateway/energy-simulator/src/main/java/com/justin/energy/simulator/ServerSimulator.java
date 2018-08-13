/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.simulator;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.justin.energy.common.WebsocketOverMqttClient;
import com.justin.energy.common.WebsocketOverMqttClient.QOS;
import com.justin.energy.common.config.MqttConfiguration;
import com.justin.energy.common.dto.FotaDto;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class ServerSimulator {
  public static void main(final String[] args) throws MqttException, InterruptedException {
    final MqttConfiguration config = new MqttConfiguration();
    config.setBrokerUrl("tcp://14.161.34.137:1883");
    final WebsocketOverMqttClient mqttClient =
        new WebsocketOverMqttClient(ServerSimulator.class.getSimpleName(), config);
    mqttClient.connect();

    Thread.sleep(2000);

    final FotaDto fot = new FotaDto();
    fot.setGatewayId(com.justin.energy.common.config.ApplicationProperties.getConfiguredDeviceId());
    mqttClient.publish("energysolution/fota/parameter/" + fot.getGatewayId(), QOS.AT_LEAST_ONE,
        fot);
    mqttClient.disconnect();

    System.exit(0);
  }
}
