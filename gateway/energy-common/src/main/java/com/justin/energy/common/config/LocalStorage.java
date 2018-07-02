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
  public static final String applicationName = "energy-reader";
  private static final String runConfigurationFolderName = "energy-profile";
  private static final String runConfigurationFileName = "runConfiguration.json";

  public static File getRoot() {
    return new File(System.getProperty("user.home"), runConfigurationFolderName);
  }

  public static File getRunConfigurationFile() {
    return new File(getRoot(), runConfigurationFileName);
  }

  public boolean delete() {
    if (doesConfigurationExist()) {
      return delete(getRoot());
    }
    return true;
  }

  public boolean doesConfigurationExist() {
    return getRunConfigurationFile().exists();
  }

  public T load(final Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
    final ObjectMapper jsonMapper = new ObjectMapper();
    if (!doesConfigurationExist()) {
      throw new FileNotFoundException("Configuration file not exist on local drive");
    }
    return jsonMapper.readValue(getRunConfigurationFile(), clazz);
  }

  public void store(final T configuration)
      throws JsonGenerationException, JsonMappingException, IOException {
    final ObjectMapper jsonMapper = new ObjectMapper();
    final File root = getRoot();
    if (!root.exists()) {
      root.mkdirs();
    }
    jsonMapper.writeValue(getRunConfigurationFile(), configuration);
  }

  private boolean delete(final File file) {
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
