/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.common.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class LocalStorage<T> {
  public static final String readerApplicationName = "energy-reader";
  public static final String applicationFotaFolderName = "energy-fota";
  private static final String runConfigurationFolderName = "energy-configurations";
  private static final String runReaderConfigurationFileName = "runReaderConfiguration.json";

  public static boolean delete() {
    return delete(getStorageRoot());
  }

  public static boolean doesRunReaderConfigurationExist() {
    return getRunReaderConfigurationFile().exists();
  }

  public static File getApplicationExecutable() {
    return new File(getReaderApplicationRoot(), readerApplicationName + ".jar");
  }

  public static File getApplicationRoot() {
    return new File(System.getProperty("user.home"));
  }

  public static File getFotaRoot() {
    return new File(getApplicationRoot(), applicationFotaFolderName);
  }

  public static File getReaderApplicationRoot() {
    return new File(getApplicationRoot(), readerApplicationName);
  }

  public static File getRunReaderConfigurationFile() {
    return new File(getStorageRoot(), runReaderConfigurationFileName);
  }

  public static File getStorageRoot() {
    return new File(getApplicationRoot(), runConfigurationFolderName);
  }

  private static boolean delete(final File file) {
    if (!file.exists()) {
      return true;
    }

    boolean result = true;
    if (file.isDirectory()) {
      final File[] listFiles = file.listFiles();
      for (final File f : listFiles) {
        result = delete(f) && result;
      }
    }
    return file.delete() & result;
  }

  public T load(final Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
    final ObjectMapper jsonMapper = new ObjectMapper();
    if (!doesRunReaderConfigurationExist()) {
      throw new FileNotFoundException("Configuration file not exist on local drive");
    }
    return jsonMapper.readValue(getRunReaderConfigurationFile(), clazz);
  }

  public void store(final T configuration)
      throws JsonGenerationException, JsonMappingException, IOException {
    final ObjectMapper jsonMapper = new ObjectMapper();
    final File root = getStorageRoot();
    if (!root.exists()) {
      root.mkdirs();
    }
    jsonMapper.writeValue(getRunReaderConfigurationFile(), configuration);
  }
}
