/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.pmw.tinylog.Logger;

/**
 * @author tuan3.nguyen@gmail.com
 */
public interface ProcessUtils {
  public static boolean runCmd(final String... params) {
    if (params == null || params.length == 0) {
      throw new IllegalArgumentException("command is null or empty");
    }
    final ProcessBuilder pb = new ProcessBuilder(Arrays.asList(params));
    try {
      final Process process = pb.redirectErrorStream(true).start();
      final BufferedReader reader =
          new BufferedReader(new InputStreamReader(process.getInputStream()));
      String consoleLine;
      final StringBuilder response = new StringBuilder();
      while ((consoleLine = reader.readLine()) != null) {
        response.append(consoleLine);
        // Just make buffer empty to prevent process from endless execution, especially on platform
        // that limited buffer size for standard input and output streams.
      }
      Logger.info(response.toString());
      return process.waitFor() == 0;
    } catch (IOException | InterruptedException ex) {
      return false;
    }
  }
}
