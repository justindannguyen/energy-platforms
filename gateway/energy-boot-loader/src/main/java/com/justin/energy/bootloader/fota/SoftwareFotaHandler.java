/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.bootloader.fota;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.pmw.tinylog.Logger;

import com.justin.energy.common.config.LocalStorage;
import com.justin.energy.common.dto.FotaDto;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class SoftwareFotaHandler extends FotaHandler {

  public SoftwareFotaHandler(final String gatewayId, final Object softwareUpgradeLock,
      final int readerApplicationPort) {
    super(gatewayId, softwareUpgradeLock, readerApplicationPort);
  }

  public void extract(final String newFirmware) throws IOException {
    final StringBuilder extractLog = new StringBuilder();
    final byte[] buffer = new byte[1024];
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(newFirmware))) {
      ZipEntry zipEntry = zis.getNextEntry();
      while (zipEntry != null) {
        final String fileName = zipEntry.getName();
        final File newFile = new File(LocalStorage.getApplicationRoot(), fileName);
        try (FileOutputStream fos = new FileOutputStream(newFile)) {
          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }
        }
        extractLog.append(String.format("Extracted %s\r\n", newFile));
        zipEntry = zis.getNextEntry();
      }
      zis.closeEntry();
      Logger.info(extractLog);
    }
  }

  @Override
  protected void startUpgrade(final FotaDto fotaInfo) {
    String newFile = null;
    try {
      newFile = downloadNewBinary(fotaInfo.getRemoteUrl());
    } catch (final IOException ex) {
      Logger.error(ex, "Can't download new firmware");
    }
    if (newFile != null) {
      try {
        extract(newFile);
      } catch (final IOException ex) {
        Logger.error(ex, "Can't extract new firmware");
      }
    }
  }

  private String downloadNewBinary(final String remoteUrl)
      throws MalformedURLException, IOException {
    final HttpURLConnection connection = (HttpURLConnection) new URL(remoteUrl).openConnection();
    connection.setRequestMethod("GET");

    final File upgradeFolder = LocalStorage.getFotaRoot();
    final File upgradeFile =
        new File(upgradeFolder, String.format("%s.zip", System.currentTimeMillis()));
    Logger.info("Downloading new firmware from {}", remoteUrl);
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
    Logger.info("New firmware is downloaded successful to {}", upgradeFile);
    return upgradeFile.getAbsolutePath();
  }
}
