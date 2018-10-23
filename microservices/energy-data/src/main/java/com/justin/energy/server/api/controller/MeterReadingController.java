/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.api.controller;

import java.util.Date;

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

import com.justin.energy.server.api.entity.MeterReadingEntity;
import com.justin.energy.server.api.repository.MeterReadingRepository;

/**
 * @author tuan3.nguyen@gmail.com
 */
@RestController()
@RequestMapping("/api/v1/raw-energy")
public class MeterReadingController {
  @Autowired
  private MeterReadingRepository meterReadingRepository;

  @GetMapping("{gatewayId}/{meterId}")
  @ResponseBody
  public Page<MeterReadingEntity> getEnergy(@PathVariable final String gatewayId,
      @PathVariable final int meterId,
      @SpanTag(key = "from") @RequestParam(name = "from") @DateTimeFormat(
          iso = ISO.DATE_TIME) final Date from,
      @SpanTag(key = "to") @RequestParam(name = "to") @DateTimeFormat(
          iso = ISO.DATE_TIME) final Date to,
      @SpanTag(key = "pageNumber") @RequestParam(name = "pageNumber", required = false,
          defaultValue = "1") final Integer pageNumber,
      @SpanTag(key = "pageSize") @RequestParam(name = "pageSize", required = false,
          defaultValue = "100") final Integer pageSize) {
    final Pageable pageable = PageRequest.of(pageNumber, pageSize);
    return meterReadingRepository.findByGatewayIdAndMeterIdAndDateBetween(pageable, gatewayId,
        meterId, from, to);
  }
}
