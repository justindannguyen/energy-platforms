/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.bootloader;

import java.io.File;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;

import org.pmw.tinylog.Logger;

import com.justin.energy.common.ProcessUtils;
import com.justin.energy.common.config.LocalStorage;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class EnergyReaderWatchDog implements Runnable {
  private final int energyReaderApplicationPort;
  private final Thread thread;
  private transient boolean running = false;
  private transient boolean paused = false;

  public EnergyReaderWatchDog(final int energyReaderApplicationPort) {
    this.energyReaderApplicationPort = energyReaderApplicationPort;
    thread = new Thread(this);
    thread.setName("Energy Reader Watch Dog");
    thread.setDaemon(true);
  }

  @Override
  public void run() {
    while (running) {
      if (!paused) {
        try (ServerSocket checkIfReaderIsRunning = new ServerSocket(energyReaderApplicationPort)) {
          Logger.info("Reader appliation is not running, awake it up!!!");
          awakeUpReaderApplication();
        } catch (final Exception ex) {
          // Reader application is running as usual, going to sleep and check later
        }
      }
      try {
        TimeUnit.MINUTES.sleep(2);
      } catch (final InterruptedException ex1) {
        running = false;
        break;
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
    final String java = System.getenv("JAVA_HOME") + "/bin/java";
    final String energyReaderJar =
        new File(System.getProperty("user.home"), LocalStorage.applicationName + ".jar")
            .getAbsolutePath();
    if (!ProcessUtils.runCmd(java, "-jar", energyReaderJar)) {
      Logger.error("Could not awake up the energy reader application ({})", energyReaderJar);
    }
  }
}
