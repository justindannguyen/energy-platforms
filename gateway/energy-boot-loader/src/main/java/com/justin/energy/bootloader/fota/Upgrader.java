/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.bootloader.fota;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class Upgrader extends Thread {
  private final String newBinaryFile;

  public Upgrader(final String newFile) {
    this.newBinaryFile = newFile;
    setName("FOTA Upgrader");
  }

  @Override
  public void run() {
    final String java = System.getProperty("java.home") + "/bin/java";
    runCmd(java, "-jar", newBinaryFile);
  }

  public boolean runCmd(final String... params) {
    if (params == null || params.length == 0) {
      throw new IllegalArgumentException("command is null or empty");
    }
    final ProcessBuilder pb = new ProcessBuilder(Arrays.asList(params));
    try {
      final Process process = pb.redirectErrorStream(true).start();
      final BufferedReader reader =
          new BufferedReader(new InputStreamReader(process.getInputStream()));
      String consoleLine;
      while ((consoleLine = reader.readLine()) != null) {
        System.out.println(consoleLine);
        // Just make buffer empty to prevent process from endless execution, especially on platform
        // that limited buffer size for standard input and output streams.
      }
      return process.waitFor() == 0;
    } catch (IOException | InterruptedException ex) {
      return false;
    }
  }
}
