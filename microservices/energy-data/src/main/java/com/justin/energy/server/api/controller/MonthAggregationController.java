/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.api.controller;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.justin.energy.server.api.entity.MonthAggregationEntity;
import com.justin.energy.server.api.service.MonthAggregationService;

/**
 * @author tuan3.nguyen@gmail.com
 */
@RestController()
@RequestMapping("/api/v1/maggregated-energy")
public class MonthAggregationController {
  @Autowired
  private MonthAggregationService monthAggregationService;

  @GetMapping("{gatewayId}/{meterId}")
  @ResponseBody
  public List<MonthAggregationEntity> getEnergyByDayBetween(@PathVariable final String gatewayId,
      @PathVariable final int meterId,
      @SpanTag(key = "from") @RequestParam(name = "from") @DateTimeFormat(
          iso = ISO.DATE) final Date from,
      @SpanTag(key = "to") @RequestParam(name = "to") @DateTimeFormat(
          iso = ISO.DATE) final Date to) {
    return monthAggregationService.findByGatewayIdAndMeterIdAndMonthBetween(gatewayId, meterId,
        from, to);
  }

  @GetMapping("byMonth/{gatewayId}/{meterId}")
  @ResponseBody
  public MonthAggregationEntity getEnergyByMonth(@PathVariable final String gatewayId,
      @PathVariable final int meterId, @SpanTag(key = "date") @RequestParam(
          name = "date") @DateTimeFormat(iso = ISO.DATE) final Date date) {
    final MonthAggregationEntity energy =
        monthAggregationService.findByGatewayIdAndMeterIdAndMonth(gatewayId, meterId, date);
    if (energy == null) {
      throw new NoSuchElementException(String.format(
          "No energy data on month of %s for (gateway %s, meter %s)", date, gatewayId, meterId));
    }
    return energy;
  }
}
