/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin;

import org.pmw.tinylog.Logger;

import com.intelligt.modbus.jlibmodbus.utils.CRC16;

import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class MeterSimulator {
  public static void main(final String[] args) {
    // In the constructor pass the name of the port with which we work
    final SerialPort serialPort = new SerialPort("COM4");
    try {
      // Open port
      serialPort.openPort();
      // We expose the settings. You can also use this line - serialPort.setParams(9600, 8, 1, 0);
      serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
          SerialPort.PARITY_EVEN);
      while (true) {
        final byte[] data = serialPort.readBytes(8);
        final int registerLength = (data[4] << 8) + data[5];
        System.out.print("INFO: Frame received - ");
        printBytes(data);

        final byte[] response = new byte[registerLength * 2 + 5];
        int index = 0;
        response[index++] = data[0];
        response[index++] = data[1];
        response[index++] = (byte) (registerLength * 2);
        for (int i = 0; i < response[2]; i++) {
          response[index++] = 0x00;
        }
        final int crc = CRC16.calc(CRC16.INITIAL_VALUE, response, index);
        response[index++] = (byte) crc;
        response[index++] = (byte) (crc >> 8);
        System.out.print("INFO: Frame sent - ");
        printBytes(response);
        serialPort.writeBytes(response);
      }
      // Closing the port
    } catch (final SerialPortException ex) {
      Logger.error(ex);
    }
  }

  private static void printBytes(final byte[] data) {
    for (final byte b : data) {
      final String hexString = String.format("%02X", b);
      if (hexString.length() == 1) {
        System.out.print("0");
      }
      System.out.print(hexString);
    }
    System.out.println();
  }
}
