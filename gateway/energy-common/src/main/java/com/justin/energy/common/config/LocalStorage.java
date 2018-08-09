/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.common.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JavaType;
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
    return new File(getReaderApplicationRoot(), readerApplicationName + ".jar");
  }

  public static File getApplicationRoot() {
    return new File(System.getProperty("user.home"), ".energy");
  }

  public static File getConfigurationRoot() {
    return new File(getApplicationRoot(), runConfigurationFolderName);
  }

  public static File getEnergyDataRoot() {
    return new File(getApplicationRoot(), applocationDataFolderName);
  }

  public static File getFotaRoot() {
    return new File(getApplicationRoot(), applicationFotaFolderName);
  }

  public static File getReaderApplicationRoot() {
    return new File(getApplicationRoot(), readerApplicationName);
  }

  public static File getRunReaderConfigurationFile() {
    return new File(getConfigurationRoot(), runReaderConfigurationFileName);
  }

  public static <T> T loadConfigs(final Class<T> clazz)
      throws JsonParseException, JsonMappingException, IOException {
    if (!doesRunReaderConfigurationExist()) {
      throw new FileNotFoundException("Configuration file not exist on local drive");
    }
    return jsonMapper.readValue(getRunReaderConfigurationFile(), clazz);
  }

  public static <T> List<T> loadEnergyData(final File energyFile, final Class<T> clazz)
      throws JsonParseException, JsonMappingException, IOException {
    if (!energyFile.exists()) {
      throw new FileNotFoundException("Energy file not exist on local drive " + energyFile);
    }
    final JavaType type = jsonMapper.getTypeFactory().constructCollectionType(List.class, clazz);
    return jsonMapper.readValue(energyFile, type);
  }

  public static void storeConfigs(final Object configuration)
      throws JsonGenerationException, JsonMappingException, IOException {
    final File root = getConfigurationRoot();
    if (!root.exists()) {
      root.mkdirs();
    }
    jsonMapper.writeValue(getRunReaderConfigurationFile(), configuration);
  }

  public static void storeEnergyData(final List<?> energies)
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
