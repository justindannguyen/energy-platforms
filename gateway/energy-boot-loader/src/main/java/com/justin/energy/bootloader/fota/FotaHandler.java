/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.bootloader.fota;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.justin.energy.common.dto.FotaDto;

/**
 * @author tuan3.nguyen@gmail.com
 */
public abstract class FotaHandler implements IMqttMessageListener {
  protected final String gatewayId;
  protected boolean upgradeInProgress;

  protected FotaHandler(final String gatewayId) {
    this.gatewayId = gatewayId;
  }

  @Override
  public void messageArrived(final String topic, final MqttMessage message) throws Exception {
    // Upgrade package is not for this gateway.
    final ObjectMapper jsonMapper = new ObjectMapper();
    final FotaDto fotaInfo = jsonMapper.readValue(message.getPayload(), FotaDto.class);
    if (!gatewayId.equals(fotaInfo.getGatewayId())) {
      return;
    }

    synchronized (this) {
      // Upgrade already in progress
      if (upgradeInProgress) {
        return;
      }

      upgradeInProgress = true;
      startUpgrade(fotaInfo);
    }
  }

  protected abstract void startUpgrade(FotaDto fotaInfo) throws Exception;
}
