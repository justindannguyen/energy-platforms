/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.stream.aggregation;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author tuan3.nguyen@gmail.com
 */
public interface HourAggregationProcessor {
  String INPUT = "input";
  String OUTPUT = "output";
  String HOUR_AGGREGATION_INPUT = "hourAggregationInput";

  @Input(HOUR_AGGREGATION_INPUT)
  SubscribableChannel hourAggregationInput();

  /**
   * @return {@link Input} binding for {@link KStream} type.
   */
  @Input(INPUT)
  KStream<?, ?> input();

  /**
   * @return {@link Output} binding for {@link KStream} type.
   */
  @Output(OUTPUT)
  KStream<?, ?> output();
}
