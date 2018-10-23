/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.api.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.stereotype.Service;

import com.justin.energy.server.api.Dates;
import com.justin.energy.server.api.entity.MonthAggregationEntity;
import com.justin.energy.server.api.repository.MonthAggregationRepository;

/**
 * @author tuan3.nguyen@gmail.com
 */
@Service
public class MonthAggregationService {
  @Autowired
  private MonthAggregationRepository monthAggregationRepository;

  private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM");

  @NewSpan("energyByMonth")
  public MonthAggregationEntity findByGatewayIdAndMeterIdAndMonth(final String gatewayId,
      final int meterId, @SpanTag(key = "date") final Date date) {
    final String monthString = dateFormat.format(date);
    return monthAggregationRepository.findByGatewayIdAndMeterIdAndMonth(gatewayId, meterId,
        monthString);
  }

  public List<MonthAggregationEntity> findByGatewayIdAndMeterIdAndMonthBetween(
      final String gatewayId, final int meterId, final Date from, final Date to) {
    if (from.after(to)) {
      throw new IllegalArgumentException("FromDate is not before ToDate");
    }
    final Date fromMonth = Dates.startOfMonth(from);
    final Date toMonth = Dates.startOfMonth(to);
    final List<MonthAggregationEntity> energyByMonth = new ArrayList<>();
    Date month = fromMonth;
    while (month.before(toMonth) || month.equals(toMonth)) {
      final MonthAggregationEntity data =
          findByGatewayIdAndMeterIdAndMonth(gatewayId, meterId, month);
      if (data != null) {
        energyByMonth.add(data);
      }
      month = Dates.nextMonth(month);
    }
    return energyByMonth;
  }
}
