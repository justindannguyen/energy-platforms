/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.reader;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.pmw.tinylog.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.justin.energy.common.dto.FotaDto;
import com.justin.energy.common.exception.ShutdownException;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class FotaHandler implements IMqttMessageListener {
  protected final EnergyReader gateway;
  protected boolean upgradeInProgress;

  public FotaHandler(final EnergyReader gateway) {
    this.gateway = gateway;
  }

  @Override
  public void messageArrived(final String topic, final MqttMessage message) throws Exception {
    final ObjectMapper jsonMapper = new ObjectMapper();
    final FotaDto fotaInfo = jsonMapper.readValue(message.getPayload(), FotaDto.class);
    // Upgrade package is not for this gateway.
    if (!gateway.getGatewayId().equals(fotaInfo.getGatewayId())) {
      return;
    }

    synchronized (this) {
      // Upgrade already in progress
      if (upgradeInProgress) {
        return;
      }
      upgradeInProgress = true;

      final Thread shutdownThread = new Thread(() -> {
        try {
          gateway.stop();
          Logger.info("System is now shutting down for new software upgrade");
        } catch (final ShutdownException ex) {
          Logger.error(ex, "Shutting down for software upgrade encounters problem. Force shutdown");
        } finally {
          System.exit(0);
        }
      });
      shutdownThread.setName("Shutdown Thread");
      shutdownThread.setDaemon(true);
      shutdownThread.start();
    }
  }
}
