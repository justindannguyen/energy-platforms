/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.stream.aggregation;

import org.apache.kafka.streams.kstream.Initializer;

import com.justin.energy.server.stream.aggregation.dto.AggregatedHourDto;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class HourAggregationInitializer implements Initializer<AggregatedHourDto> {
  @Override
  public AggregatedHourDto apply() {
    return new AggregatedHourDto();
  }
}
