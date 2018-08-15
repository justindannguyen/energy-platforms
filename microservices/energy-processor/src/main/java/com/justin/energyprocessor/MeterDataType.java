/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energyprocessor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * @author tuan3.nguyen@gmail.com
 */
public enum MeterDataType {
  FLOAT32(2) {
    @Override
    public Float getValue(final int[] words, final int index) {
      return getValueBuffer(words, index).getFloat(0);
    }
  };

  public static MeterDataType fromString(final String name) {
    return Arrays.stream(MeterDataType.values()).filter(type -> type.name().equalsIgnoreCase(name))
        .findAny().orElse(null);
  }

  private int wordCount;

  private MeterDataType(final int wordCount) {
    this.wordCount = wordCount;
  }

  public int getByteCount() {
    return getWordCount() * 2;
  }

  public abstract Object getValue(int[] words, int index);

  public int getWordCount() {
    return wordCount;
  }

  protected ByteBuffer getValueBuffer(final int[] words, final int index) {
    final ByteBuffer allocate = ByteBuffer.allocate(getByteCount());
    for (int word = 0; word < getWordCount(); word++) {
      allocate.putShort((short) words[index + word]);
    }
    return allocate.order(ByteOrder.BIG_ENDIAN);
  }
}
