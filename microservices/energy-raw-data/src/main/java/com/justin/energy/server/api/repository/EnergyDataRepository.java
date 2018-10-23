/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.api.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author tuan3.nguyen@gmail.com
 */
@NoRepositoryBean
public interface EnergyDataRepository<E> extends MongoRepository<E, String> {
  Page<E> findByGatewayIdAndDateBetween(Pageable pageable, String gatewayId, Date from, Date to);

  E findByGatewayIdAndMeterIdAndDate(String gatewayId, int meterId, Date date);

  Page<E> findByGatewayIdAndMeterIdAndDateBetween(Pageable pageable, String gatewayId, int meterId,
      Date from, Date to);
}
