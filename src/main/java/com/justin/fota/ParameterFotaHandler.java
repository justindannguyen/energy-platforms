/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.fota;

import org.pmw.tinylog.Logger;

import com.justin.EnergyGateway;
import com.justin.config.LocalStorage;
import com.justin.exception.ShutdownException;
import com.justin.exception.StartupException;
import com.justin.transmission.dto.FotaDto;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class ParameterFotaHandler extends FotaHandler {

  public ParameterFotaHandler(final EnergyGateway gateway) {
    super(gateway);
  }

  @Override
  protected void startUpgrade(final FotaDto fotaInfo) {
    final Thread shutdownThread = new Thread(() -> {
      try {
        if (!LocalStorage.delete()) {
          Logger.info("FOTA fail!!!");
          synchronized (ParameterFotaHandler.this) {
            upgradeInProgress = false;
          }
          return;
        }
        Logger.info("FOTA successful, applying new configuration");
        gateway.stop();
        new EnergyGateway().start();
        Logger.info("System is now running with new configuration");
      } catch (final ShutdownException | StartupException ex) {
        Logger.error(ex, "FOTA successful but physical restart is required");
      }
    });
    shutdownThread.setName("Parameter FOTA");
    shutdownThread.setDaemon(true);
    shutdownThread.start();
  }
}
