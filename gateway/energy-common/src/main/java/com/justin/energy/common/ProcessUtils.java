/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.pmw.tinylog.Logger;

import com.justin.energy.common.config.LocalStorage;

/**
 * @author tuan3.nguyen@gmail.com
 */
public interface ProcessUtils {
  public static void runCmd(final boolean printError, final List<String> params)
      throws IOException {
    if (params == null || params.size() == 0) {
      throw new IllegalArgumentException("command is null or empty");
    }
    final Process process =
        new ProcessBuilder(params).directory(LocalStorage.getApplicationRoot()).start();
    if (printError) {
      final BufferedReader reader =
          new BufferedReader(new InputStreamReader(process.getErrorStream()));
      String errorLine;
      final StringBuilder response = new StringBuilder();
      while ((errorLine = reader.readLine()) != null) {
        response.append(errorLine + "\n");
      }
      Logger.error(response.toString());
    }
  }

  public static boolean runCmdAndWait(final String... params) {
    if (params == null || params.length == 0) {
      throw new IllegalArgumentException("command is null or empty");
    }
    final ProcessBuilder pb =
        new ProcessBuilder(Arrays.asList(params)).directory(LocalStorage.getApplicationRoot());
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
