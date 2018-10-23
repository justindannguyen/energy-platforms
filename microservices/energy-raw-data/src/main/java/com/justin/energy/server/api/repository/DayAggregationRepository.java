/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.api.repository;

import org.springframework.stereotype.Repository;

import com.justin.energy.server.api.entity.DayAggregationEntity;

/**
 * @author tuan3.nguyen@gmail.com
 */
@Repository
public interface DayAggregationRepository extends EnergyDataRepository<DayAggregationEntity> {
}
