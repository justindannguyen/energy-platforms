/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.annotation.SpanTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
@RequestMapping("api/v1/raws")
public class MeterReadingController {
  @Autowired
  private MeterReadingRepository meterReadingRepository;

  @GetMapping
  @ResponseBody
  public List<MeterReadingEntity> getAll() {
    return meterReadingRepository.findAll();
  }

  @GetMapping("/{gatewayId}/{meterId}")
  @ResponseBody
  public Page<MeterReadingEntity> getPage(@PathVariable final String gatewayId,
      @PathVariable final int meterId,
      @SpanTag(key = "pageNumber") @RequestParam(name = "pageNumber", required = false,
          defaultValue = "1") final Integer pageNumber,
      @SpanTag(key = "pageSize") @RequestParam(name = "pageSize", required = false,
          defaultValue = "100") final Integer pageSize) {
    final Pageable pageable = PageRequest.of(pageNumber, pageSize);
    return meterReadingRepository.findByGatewayIdAndMeterId(pageable, gatewayId, meterId);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleException(final Exception ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
