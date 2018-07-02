/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.reader.transmission;

import java.util.concurrent.TimeUnit;

import org.pmw.tinylog.Logger;

import com.intelligt.modbus.jlibmodbus.Modbus;
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.serial.SerialParameters;
import com.intelligt.modbus.jlibmodbus.serial.SerialPort.BaudRate;
import com.intelligt.modbus.jlibmodbus.serial.SerialPort.Parity;
import com.justin.energy.reader.config.RunConfiguration;
import com.justin.energy.reader.config.MeterConfiguration.HoldingRegister;
import com.intelligt.modbus.jlibmodbus.serial.SerialPortException;
import com.intelligt.modbus.jlibmodbus.serial.SerialUtils;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class MeterModbusMaster implements Runnable {
  private final ModbusMaster modbusMaster;
  private transient boolean running = true;
  private Exception lastError;
  private final Thread watchDog;

  public MeterModbusMaster(final RunConfiguration configuration) throws SerialPortException {
    final SerialParameters sp = new SerialParameters();
    Modbus.setLogLevel(Modbus.LogLevel.LEVEL_DEBUG);

    sp.setDevice(configuration.getSerialPort());
    sp.setBaudRate(BaudRate.getBaudRate(configuration.getBaudRate()));
    sp.setDataBits(configuration.getDataBits());
    sp.setParity(Parity.valueOf(configuration.getParity().toUpperCase()));
    sp.setStopBits(configuration.getStopBits());
    SerialUtils.setSerialPortFactoryJSSC();
    modbusMaster = ModbusMasterFactory.createModbusMasterRTU(sp);
    watchDog = new Thread(this);
    watchDog.setDaemon(true);
    watchDog.setName("Modbus Watch Dog");
  }

  public void connect() throws ModbusIOException {
    modbusMaster.connect();
    watchDog.start();
    Logger.info("Modbus connected");
  }

  public void disconnect() throws ModbusIOException, InterruptedException {
    running = false;
    modbusMaster.disconnect();
    watchDog.interrupt();
    watchDog.join();
    Logger.info("Modbus terminated!!!");
  }

  public Exception getLastError() {
    return lastError;
  }

  public int[] readHoldingRegister(final int meterId, final HoldingRegister register) {
    try {
      if (modbusMaster.isConnected()) {
        return modbusMaster.readHoldingRegisters(meterId, register.getOffset(),
            register.getQuantity());
      }
    } catch (final Exception ex) {
      lastError = ex;
      Logger.error(ex);
    }
    return null;
  }

  @Override
  public void run() {
    int retryTime = 0;

    while (running) {
      // Watch dog to check modbus connection status, every 1 minute
      if (modbusMaster.isConnected()) {
        try {
          TimeUnit.MINUTES.sleep(1);
        } catch (final InterruptedException ex) {
          running = false;
        }
        continue;
      }

      // Try to connect to modbus connection.
      try {
        modbusMaster.connect();
        Logger.info("Connected to modbus...");
        retryTime = 0;
        lastError = null;
      } catch (final ModbusIOException ex) {
        lastError = ex;
        retryTime++;
        try {
          Logger.error("Fail when connect to modbus, retry after {} seconds. Detail error: {}",
              retryTime, ex.getMessage());
          TimeUnit.SECONDS.sleep(retryTime);
        } catch (final InterruptedException ex1) {
          running = false;
        }
      }
    }
    Logger.info("Watch dog terminated!!!");
  }
}
