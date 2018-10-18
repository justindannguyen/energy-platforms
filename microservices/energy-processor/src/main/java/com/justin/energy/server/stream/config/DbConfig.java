/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.stream.config;

import javax.annotation.PostConstruct;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.stereotype.Component;

/**
 * @author tuan3.nguyen@gmail.com
 */
@Component
public class DbConfig {

  public static final String ENERGY_COLLECTION = "energy";

  @Autowired
  private MongoTemplate mongoTemplate;

  @PostConstruct
  public void configDb() {
    final Document indexOptions = new Document();
    indexOptions.put("gatewayId", 1);
    indexOptions.put("meterId", 1);
    indexOptions.put("date", 1);
    final CompoundIndexDefinition indexDefinition = new CompoundIndexDefinition(indexOptions);
    mongoTemplate.indexOps(ENERGY_COLLECTION).ensureIndex(indexDefinition);
  }
}
