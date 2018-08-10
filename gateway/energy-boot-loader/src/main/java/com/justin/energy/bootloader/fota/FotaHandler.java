/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.bootloader.fota;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.pmw.tinylog.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.justin.energy.common.dto.FotaDto;

/**
 * @author tuan3.nguyen@gmail.com
 */
public abstract class FotaHandler implements IMqttMessageListener {
  protected final String gatewayId;
  protected final Object softwareUpgradeLock;
  private final int readerApplicationPort;

  protected FotaHandler(final String gatewayId, final Object softwareUpgradeLock,
      final int readerApplicationPort) {
    this.gatewayId = gatewayId;
    this.softwareUpgradeLock = softwareUpgradeLock;
    this.readerApplicationPort = readerApplicationPort;
  }

  @Override
  public void messageArrived(final String topic, final MqttMessage message) throws Exception {
    // Upgrade package is not for this gateway.
    final ObjectMapper jsonMapper = new ObjectMapper();
    final FotaDto fotaInfo = jsonMapper.readValue(message.getPayload(), FotaDto.class);
    if (!gatewayId.equals(fotaInfo.getGatewayId())) {
      return;
    }

    synchronized (softwareUpgradeLock) {
      shutdownReaderApplication();
      startUpgrade(fotaInfo);
    }
  }

  protected abstract void startUpgrade(FotaDto fotaInfo) throws Exception;

  private void shutdownReaderApplication() {
    Logger.info("Shutting down reader application for firmware upgrade");
    try (Socket readerApplicationShutdown = new Socket("127.0.0.1", readerApplicationPort)) {
      try (OutputStream os = readerApplicationShutdown.getOutputStream()) {
        os.write(gatewayId.getBytes());
        os.flush();
        Logger.info("Reader application is shutdown");
      }
    } catch (final IOException ex) {
      // Reader application is not running, fine.
      Logger.info(ex, "Reader application is not running, ok fine");
    }
  }
}
