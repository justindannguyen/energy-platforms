/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.stream.aggregation;

import org.apache.commons.lang.SerializationException;
import org.apache.kafka.streams.kstream.Aggregator;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.justin.energy.server.stream.aggregation.dto.AggregatedHourDto;
import com.justin.energy.server.stream.aggregation.dto.InputDto;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class HourAggregator implements Aggregator<String, String, AggregatedHourDto> {
  private final ObjectMapper jsonObjectMapper = new ObjectMapper();

  public HourAggregator() {
    jsonObjectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
    jsonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  @Override
  public AggregatedHourDto apply(final String key, final String input,
      final AggregatedHourDto aggregation) {
    try {
      final InputDto inputDto = jsonObjectMapper.readValue(input, InputDto.class);
      aggregateCommonProperties(aggregation, inputDto);

      aggregateVoltageProperties(aggregation, inputDto);

      aggregateCurrentProperties(aggregation, inputDto);
      return aggregation;
    } catch (final Exception ex) {
      throw new SerializationException("Can't serialize data for key" + key, ex);
    }
  }

  private void aggregateCommonProperties(final AggregatedHourDto aggregation,
      final InputDto inputDto) {
    if (aggregation.getDate() == null) {
      aggregation.setDate(Dates.withoutMinute(inputDto.getDate()).getTime());
    }
    if (aggregation.getGatewayId() == null) {
      aggregation.setGatewayId(inputDto.getGatewayId());
    }
    if (aggregation.getMeterId() == null) {
      aggregation.setMeterId(inputDto.getMeterId());
    }
  }

  private void aggregateCurrentProperties(final AggregatedHourDto aggregation,
      final InputDto inputDto) {
    final Float ca = inputDto.getCa();
    if (ca != null) {
      if (aggregation.getMaxCa() == null || ca > aggregation.getMaxCa()) {
        aggregation.setMaxCa(ca);
      }
      if (aggregation.getMinCa() == null || ca < aggregation.getMinCa()) {
        aggregation.setMinCa(ca);
      }
      if (aggregation.getAverageCa() == null) {
        aggregation.setAverageCa(ca);
      } else {
        aggregation.setAverageCa((aggregation.getAverageCa() + ca) / 2);
      }
    }
  }

  private void aggregateVoltageProperties(final AggregatedHourDto aggregation,
      final InputDto inputDto) {
    final Float va = inputDto.getVa();
    if (va != null) {
      if (aggregation.getMaxVa() == null || va > aggregation.getMaxVa()) {
        aggregation.setMaxVa(va);
      }
      if (aggregation.getMinVa() == null || va < aggregation.getMinVa()) {
        aggregation.setMinVa(va);
      }
      if (aggregation.getAverageVa() == null) {
        aggregation.setAverageVa(va);
      } else {
        aggregation.setAverageVa((aggregation.getAverageVa() + va) / 2);
      }
    }
  }
}
