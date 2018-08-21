/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.stream.aggregation;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes.StringSerde;
import org.springframework.kafka.support.serializer.JsonSerde;

import com.justin.energy.server.stream.aggregation.dto.AggregatedHourDto;
import com.justin.energy.server.stream.aggregation.dto.InputDto;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class Serdes {
  private static final Serde<InputDto> inputSerde = new JsonSerde<>(InputDto.class);
  private static final Serde<AggregatedHourDto> aggregatedSerde =
      new JsonSerde<>(AggregatedHourDto.class);
  private static final Serde<String> keySerde = new StringSerde();

  public static Serde<AggregatedHourDto> aggregatedSerde() {
    return aggregatedSerde;
  }

  public static Serde<InputDto> inputSerde() {
    return inputSerde;
  }

  public static Serde<String> keySerde() {
    return keySerde;
  }
}
