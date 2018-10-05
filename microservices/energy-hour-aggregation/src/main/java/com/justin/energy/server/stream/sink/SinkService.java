/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.stream.sink;

import java.sql.Date;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

/**
 * @author tuan3.nguyen@gmail.com
 */
@Service
public class SinkService {
  @Autowired
  private MongoTemplate mongoTemplate;

  @NewSpan("sinkAggregationDataToDb")
  public void sink(final String aggregation) {
    final Document doc = Document.parse(aggregation);
    // Device sent as long in ms, now convert it to date object
    doc.replace("date", new Date(doc.getLong("date")));
    mongoTemplate.insert(doc, "energy_by_hour");
  }
}
