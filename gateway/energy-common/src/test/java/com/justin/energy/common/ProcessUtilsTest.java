/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.common;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class ProcessUtilsTest {

  @Test
  public void testRunCmd_withInvalidCommand() {
    Assert.assertFalse(ProcessUtils.runCmd("this-is-an-invalid-command"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRunCmd_withInvalidParams() {
    ProcessUtils.runCmd();
  }

  @Test
  public void testRunCmd_withLinuxPingCommand() {
    // This test only run on Linux
    Assume.assumeTrue(System.getProperty("os.name").toLowerCase().startsWith("linux"));
    Assert.assertTrue(ProcessUtils.runCmd("ping", "8.8.8.8", "-c", "1"));
  }

  @Test
  public void testRunCmd_withWindowsPingCommand() {
    // This test only run on Windows
    Assume.assumeTrue(System.getProperty("os.name").toLowerCase().startsWith("win"));
    Assert.assertTrue(ProcessUtils.runCmd("ping", "8.8.8.8", "-n", "1"));
  }
}

