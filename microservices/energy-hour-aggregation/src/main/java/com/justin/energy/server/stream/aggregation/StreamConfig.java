/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.stream.aggregation;

import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.SendTo;

import com.justin.energy.server.stream.aggregation.dto.AggregatedHourDto;
import com.justin.energy.server.stream.sink.SinkService;

/**
 * @author tuan3.nguyen@gmail.com
 */
@EnableBinding(HourAggregationProcessor.class)
public class StreamConfig {
  @Autowired
  private SinkService sinkService;

  @StreamListener(HourAggregationProcessor.INPUT)
  @SendTo(HourAggregationProcessor.OUTPUT)
  public KStream<String, AggregatedHourDto> process(final KStream<String, String> energyStream) {
    return energyStream.map(new HourKeyValueMapper())
                       .groupByKey()
                       .aggregate(new HourAggregationInitializer(), new HourAggregator(),
                           Materialized.with(Serdes.keySerde(), Serdes.aggregatedSerde()))
                       .toStream();
  }

  @StreamListener(HourAggregationProcessor.HOUR_AGGREGATION_INPUT)
  public void sink(final String aggregation) {
    sinkService.sink(aggregation);
  }
}
