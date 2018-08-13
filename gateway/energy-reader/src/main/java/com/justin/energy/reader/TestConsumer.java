/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.reader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.justin.energy.reader.transmission.dto.EnergyUsageDto;
import com.justin.energy.reader.transmission.dto.GatewayUsageDto;
import com.justin.energy.reader.transmission.dto.MeterUsageDto;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class TestConsumer {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static void main(final String[] args) {
    final Consumer<Long, String> consumer = createConsumer();

    while (true) {
      final ConsumerRecords<Long, String> consumerRecords = consumer.poll(Duration.ofMillis(1000));

      if (consumerRecords.count() == 0) {
        continue;
      }

      consumerRecords.forEach(record -> printKafka(record.value(), record.offset()));

      consumer.commitAsync();
    }
  }

  private static Consumer<Long, String> createConsumer() {
    final Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.31:9092");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaExampleConsumer");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

    // Create the consumer using props.
    final Consumer<Long, String> consumer = new KafkaConsumer<>(props);

    // Subscribe to the topic.
    consumer.subscribe(Collections.singletonList("energysolution_rawreading"));
    return consumer;
  }

  private static void printKafka(final String value, final long offset) {
    try {
      final GatewayUsageDto readValue = objectMapper.readValue(value, GatewayUsageDto.class);
      final MeterUsageDto meterUsageDto =
          readValue.getMeterUsages().get(readValue.getMeterUsages().size() - 1);
      final List<EnergyUsageDto> energyUsages = meterUsageDto.getEnergyUsages();
      if (energyUsages.isEmpty()) {
        return;
      }
      final EnergyUsageDto energyUsageDto = energyUsages.get(0);
      final int[] byteArray = energyUsageDto.getEnergyUsageResponse();
      final ByteBuffer allocate = ByteBuffer.allocate(4);
      allocate.putShort((short) byteArray[0]);
      allocate.putShort((short) byteArray[1]);
      System.out.println(offset + " Voltage " + allocate.order(ByteOrder.BIG_ENDIAN).getFloat(0));
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
  }
}
