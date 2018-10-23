/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.justin.energy.server.api.entity.MonthAggregationEntity;

/**
 * @author tuan3.nguyen@gmail.com
 */
@Repository
public interface MonthAggregationRepository
    extends MongoRepository<MonthAggregationEntity, String> {
  MonthAggregationEntity findByGatewayIdAndMeterIdAndMonth(String gatewayId, int meterId,
      String monthString);
}
