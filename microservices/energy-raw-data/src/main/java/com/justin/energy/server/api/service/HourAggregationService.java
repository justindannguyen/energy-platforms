/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.api.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.stereotype.Service;

import com.justin.energy.server.api.Dates;
import com.justin.energy.server.api.entity.HourAggregationEntity;
import com.justin.energy.server.api.repository.HourAggregationRepository;

/**
 * @author tuan3.nguyen@gmail.com
 */
@Service
public class HourAggregationService {
  @Autowired
  private HourAggregationRepository hourAggregationRepository;

  @NewSpan("energyByHour")
  public HourAggregationEntity findByGatewayIdAndMeterIdAndDate(final String gatewayId,
      final int meterId, @SpanTag(key = "date") final Date date) {
    final Date dateWithoutMinute = Dates.withoutMinute(date);
    return hourAggregationRepository.findByGatewayIdAndMeterIdAndDate(gatewayId, meterId,
        dateWithoutMinute);
  }
}
