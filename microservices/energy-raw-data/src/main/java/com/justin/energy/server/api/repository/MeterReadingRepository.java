/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.api.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.justin.energy.server.api.entity.MeterReadingEntity;

/**
 * @author tuan3.nguyen@gmail.com
 */
public interface MeterReadingRepository extends MongoRepository<MeterReadingEntity, String> {
  Page<MeterReadingEntity> findByGatewayId(Pageable pageable, String gatewayId);

  Page<MeterReadingEntity> findByGatewayIdAndDateBetween(Pageable pageable, String gatewayId,
      Date from, Date to);

  Page<MeterReadingEntity> findByGatewayIdAndMeterIdAndDateBetween(Pageable pageable,
      String gatewayId, int meterId, Date from, Date to);
}
