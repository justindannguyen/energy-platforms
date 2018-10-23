/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.api.controller;

import java.util.Date;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.justin.energy.server.api.entity.DayAggregationEntity;
import com.justin.energy.server.api.repository.DayAggregationRepository;

/**
 * @author tuan3.nguyen@gmail.com
 */
@RestController()
@RequestMapping("/api/v1/daggregated-energy")
public class DayAggregationController {
  @Autowired
  private DayAggregationRepository dayAggregationRepository;

  @GetMapping("byDay/{gatewayId}/{meterId}")
  @ResponseBody
  public DayAggregationEntity getEnergyByDay(@PathVariable final String gatewayId,
      @PathVariable final int meterId, @SpanTag(key = "date") @RequestParam(
          name = "date") @DateTimeFormat(iso = ISO.DATE) final Date date) {
    final DayAggregationEntity energy =
        dayAggregationRepository.findByGatewayIdAndMeterIdAndDate(gatewayId, meterId, date);
    if (energy == null) {
      throw new NoSuchElementException(String.format(
          "No energy data on day of %s for (gateway %s, meter %s)", date, gatewayId, meterId));
    }
    return energy;
  }

  @GetMapping("{gatewayId}/{meterId}")
  @ResponseBody
  public Page<DayAggregationEntity> getEnergyByDayBetween(@PathVariable final String gatewayId,
      @PathVariable final int meterId,
      @SpanTag(key = "from") @RequestParam(name = "from") @DateTimeFormat(
          iso = ISO.DATE) final Date from,
      @SpanTag(key = "to") @RequestParam(name = "to") @DateTimeFormat(iso = ISO.DATE) final Date to,
      @SpanTag(key = "pageNumber") @RequestParam(name = "pageNumber", required = false,
          defaultValue = "1") final Integer pageNumber,
      @SpanTag(key = "pageSize") @RequestParam(name = "pageSize", required = false,
          defaultValue = "30") final Integer pageSize) {
    final Pageable pageable = PageRequest.of(pageNumber, pageSize);
    return dayAggregationRepository.findByGatewayIdAndMeterIdAndDateBetween(pageable, gatewayId,
        meterId, from, to);
  }
}
