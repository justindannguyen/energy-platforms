/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.simulator;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class TestConsumer {
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
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.1.11:9092");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaExampleConsumer");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

    // Create the consumer using props.
    final Consumer<Long, String> consumer = new KafkaConsumer<>(props);

    // Subscribe to the topic.
    consumer.subscribe(Collections.singletonList("energysolution_parsedreading"));
    return consumer;
  }

  private static void printKafka(final String value, final long offset) {
    System.out.println(String.format("offset %s %s", offset, value));
  }
}
