/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.stream.aggregation;

import java.util.Date;

import org.apache.commons.lang.SerializationException;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.KeyValueMapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.justin.energy.server.stream.aggregation.dto.InputDto;

/**
 * Re-map the records to add hour information.
 *
 * @author tuan3.nguyen@gmail.com
 */
public class HourKeyValueMapper
    implements KeyValueMapper<Object, String, KeyValue<String, String>> {
  private final ObjectMapper jsonObjectMapper = new ObjectMapper();

  public HourKeyValueMapper() {
    jsonObjectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
    jsonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Override
  public KeyValue<String, String> apply(final Object key, final String value) {
    try {
      final InputDto inputDto = jsonObjectMapper.readValue(value, InputDto.class);
      final String gatewayId = inputDto.getGatewayId();
      final Integer meterId = inputDto.getMeterId();
      final Date dateWithoutMinutes = Dates.withoutMinute(inputDto.getDate());
      final String newKey =
          String.format("%s-%s-%s", gatewayId, meterId, dateWithoutMinutes.getTime());
      return KeyValue.<String, String>pair(newKey, value);
    } catch (final Exception ex) {
      throw new SerializationException("Can't serialize data for key" + key, ex);
    }
  }
}
