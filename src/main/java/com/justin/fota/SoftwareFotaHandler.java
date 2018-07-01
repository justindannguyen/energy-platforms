/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.fota;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.pmw.tinylog.Logger;

import com.justin.EnergyGateway;
import com.justin.exception.ShutdownException;
import com.justin.transmission.dto.FotaDto;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class SoftwareFotaHandler extends FotaHandler {

  public SoftwareFotaHandler(final EnergyGateway gateway) {
    super(gateway);
  }

  @Override
  protected void startUpgrade(final FotaDto fotaInfo) throws MalformedURLException, IOException {
    final String newFile = downloadNewBinary(fotaInfo.getRemoteUrl());
    final Thread shutdownThread = new Thread(() -> {
      try {
        gateway.stop();
      } catch (final ShutdownException ex) {
        Logger.error(ex, "Can't stop the current energy software");
      }
      System.exit(0);
    });
    shutdownThread.setName("Software FOTA");
    shutdownThread.setDaemon(true);

    Runtime.getRuntime().addShutdownHook(new Upgrader(newFile));
    shutdownThread.start();
  }

  private String downloadNewBinary(final String remoteUrl)
      throws MalformedURLException, IOException {
    final HttpURLConnection connection = (HttpURLConnection) new URL(remoteUrl).openConnection();
    connection.setRequestMethod("GET");

    final File upgradeFolder = new File(System.getProperty("user.home"), "energy-reader-fota");
    final File upgradeFile =
        new File(upgradeFolder, String.format("%s.jar", System.currentTimeMillis()));
    Logger.info("Download new firmware {}", remoteUrl);
    try (InputStream inputStream = connection.getInputStream()) {
      if (!upgradeFolder.exists()) {
        upgradeFolder.mkdirs();
      }
      try (FileOutputStream outputStream = new FileOutputStream(upgradeFile)) {
        int bytesRead = -1;
        final byte[] buffer = new byte[4069];
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }
      }
    }
    connection.disconnect();
    Logger.info("New firmware is downloaded successful");
    return upgradeFile.getAbsolutePath();
  }
}
