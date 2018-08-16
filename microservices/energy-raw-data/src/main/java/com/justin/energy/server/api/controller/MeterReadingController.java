/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
