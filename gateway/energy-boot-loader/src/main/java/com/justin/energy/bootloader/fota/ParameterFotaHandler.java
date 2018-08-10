/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.bootloader.fota;

import java.io.File;

import org.pmw.tinylog.Logger;

import com.justin.energy.common.config.LocalStorage;
import com.justin.energy.common.dto.FotaDto;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class ParameterFotaHandler extends FotaHandler {

  public ParameterFotaHandler(final String gatewayId, final Object softwareUpgradeLock,
      final int readerApplicationPort) {
    super(gatewayId, softwareUpgradeLock, readerApplicationPort);
  }

  @Override
  protected void startUpgrade(final FotaDto fotaInfo) {
    final File file = LocalStorage.getRunReaderConfigurationFile();
    if (file.exists() && !file.delete()) {
      Logger.info("Parameter FOTA fail!!!");
    } else {
      Logger.info("Parameter FOTA successful");
    }
  }
}
