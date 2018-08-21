/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.stream.aggregation;

import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.binder.kafka.streams.annotations.KafkaStreamsProcessor;
import org.springframework.messaging.handler.annotation.SendTo;

import com.justin.energy.server.stream.aggregation.dto.AggregatedHourDto;

/**
 * @author tuan3.nguyen@gmail.com
 */
@EnableBinding(KafkaStreamsProcessor.class)
public class StreamConfig {
  @StreamListener("input")
  @SendTo("output")
  public KStream<String, AggregatedHourDto> process(final KStream<String, String> energyStream) {
    return energyStream.map(new HourKeyValueMapper())
                       .groupByKey()
                       .aggregate(new HouAggregationInitializer(), new HourAggregator(),
                           Materialized.with(Serdes.keySerde(), Serdes.aggregatedSerde()))
                       .toStream();
  }
}
