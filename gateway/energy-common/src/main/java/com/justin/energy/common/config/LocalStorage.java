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
public class LocalStorage {
  public static final String readerApplicationName = "energy-reader";
  public static final String applicationFotaFolderName = "fota";
  private static final String applocationDataFolderName = "data";
  private static final String runConfigurationFolderName = "configurations";
  private static final String runReaderConfigurationFileName = "runReaderConfiguration.json";
  private static final ObjectMapper jsonMapper = new ObjectMapper();

  public static boolean deleteConfigs() {
    return delete(getConfigurationRoot());
  }

  public static boolean doesRunReaderConfigurationExist() {
    return getRunReaderConfigurationFile().exists();
  }

  public static File getApplicationExecutable() {
    return new File(getApplicationRoot(), readerApplicationName + ".jar");
  }

  public static File getApplicationRoot() {
    final String energyHome = System.getenv("ENERGY_HOME");
    return new File(energyHome != null ? energyHome : System.getProperty("user.home"));
  }

  public static File getConfigurationRoot() {
    return new File(getStorageRoot(), runConfigurationFolderName);
  }

  public static File getEnergyDataRoot() {
    return new File(getStorageRoot(), applocationDataFolderName);
  }

  public static File getFotaRoot() {
    return new File(getStorageRoot(), applicationFotaFolderName);
  }

  public static File getRunReaderConfigurationFile() {
    return new File(getConfigurationRoot(), runReaderConfigurationFileName);
  }

  public static File getStorageRoot() {
    return new File(getApplicationRoot(), ".energy");
  }

  public static <T> T loadConfigs(final Class<T> clazz)
      throws JsonParseException, JsonMappingException, IOException {
    if (!doesRunReaderConfigurationExist()) {
      throw new FileNotFoundException("Configuration file not exist on local drive");
    }
    return jsonMapper.readValue(getRunReaderConfigurationFile(), clazz);
  }

  public static <T> T loadEnergyData(final File energyFile, final Class<T> clazz)
      throws JsonParseException, JsonMappingException, IOException {
    if (!energyFile.exists()) {
      throw new FileNotFoundException("Energy file not exist on local drive " + energyFile);
    }
    return jsonMapper.readValue(energyFile, clazz);
  }

  public static File storeConfigs(final Object configuration)
      throws JsonGenerationException, JsonMappingException, IOException {
    final File root = getConfigurationRoot();
    if (!root.exists()) {
      root.mkdirs();
    }
    final File file = getRunReaderConfigurationFile();
    jsonMapper.writeValue(file, configuration);
    return file;
  }

  public static <T> void storeEnergyData(final T energies)
      throws JsonGenerationException, JsonMappingException, IOException {
    final File root = getEnergyDataRoot();
    if (!root.exists()) {
      root.mkdirs();
    }
    final File energyFile = new File(root, String.format("%s.json", System.currentTimeMillis()));
    jsonMapper.writeValue(energyFile, energies);
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
}
