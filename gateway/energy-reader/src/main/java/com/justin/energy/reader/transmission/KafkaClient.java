/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.reader.transmission;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.pmw.tinylog.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.justin.energy.reader.config.KafkaConfiguration;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class KafkaClient {
  private final KafkaProducer<Long, String> kafkaProducer;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private Exception currentError;

  public KafkaClient(final KafkaConfiguration kafkaConfiguration, final String clientId) {
    final Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfiguration.getBootstrapServers());
    props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    kafkaProducer = new KafkaProducer<>(props);
  }

  public void disconnect() throws InterruptedException {
    if (kafkaProducer != null) {
      kafkaProducer.close();
    }
  }

  public Exception getCurrentException() {
    return currentError;
  }

  public boolean publish(final String topic, final Object message) {
    try {
      final ProducerRecord<Long, String> record =
          new ProducerRecord<>(topic, objectMapper.writeValueAsString(message));
      kafkaProducer.send(record).get();
      kafkaProducer.flush();
      currentError = null;
      return true;
    } catch (final Exception ex) {
      currentError = ex;
      Logger.error(ex);
      return false;
    }
  }
}
