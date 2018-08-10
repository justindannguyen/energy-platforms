/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.bootloader;

import static com.justin.energy.common.config.ApplicationProperties.GATEWAY_ID_ENV_KEY;
import static com.justin.energy.common.config.ApplicationProperties.getConfiguredDeviceId;

import java.io.IOException;
import java.net.ServerSocket;

import org.pmw.tinylog.Logger;

import com.justin.energy.common.ProcessUtils;
import com.justin.energy.common.config.LocalStorage;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class EnergyReaderWatchDog implements Runnable {
  private final int energyReaderApplicationPort;
  private final Thread thread;
  private final Object softwareUpgradeLock;
  private transient boolean running = false;

  public EnergyReaderWatchDog(final int energyReaderApplicationPort,
      final Object softwareUpgradeLock) {
    this.energyReaderApplicationPort = energyReaderApplicationPort;
    this.softwareUpgradeLock = softwareUpgradeLock;
    thread = new Thread(this);
    thread.setName("Energy Reader Watch Dog");
    thread.setDaemon(true);
  }

  @Override
  public void run() {
    while (running) {
      boolean readerIsRunning = true;
      try (ServerSocket checkIfReaderIsRunning = new ServerSocket(energyReaderApplicationPort)) {
        readerIsRunning = false;
      } catch (final Exception ex) {
        // Reader application is running as usual, going to sleep and check later
      }
      if (readerIsRunning == false) {
        synchronized (softwareUpgradeLock) {
          Logger.info("Reader appliation is not running, awake it up!!!");
          // Execute and return only if reader is exit.
          awakeUpReaderApplication();
        }
      }
    }
  }

  public void start() {
    running = true;
    thread.start();
  }

  public void waitAndStop() throws InterruptedException {
    running = false;
    thread.interrupt();
    thread.join();
  }

  private void awakeUpReaderApplication() {
    final String energyReaderJar = LocalStorage.getApplicationExecutable().getAbsolutePath();
    final String java = System.getenv("JAVA_HOME") + "/bin/java";
    try {
      ProcessUtils.runCmd(true, java, "-jar",
          String.format("-D%s=%s", GATEWAY_ID_ENV_KEY, getConfiguredDeviceId()), energyReaderJar);
    } catch (final IOException ex) {
      Logger.error("Could not awake up the energy reader application ({})", energyReaderJar);
    }
  }
}
