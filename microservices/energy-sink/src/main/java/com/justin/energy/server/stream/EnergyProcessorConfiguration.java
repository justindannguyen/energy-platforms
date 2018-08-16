/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.stream;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * @author tuan3.nguyen@gmail.com
 */
@EnableBinding(Sink.class)
public class EnergyProcessorConfiguration {
  @Autowired
  private MongoTemplate mongoTemplate;

  @StreamListener(Sink.INPUT)
  @NewSpan("sinkToDB")
  public void sink(final String gatewayUsages) {
    final Document doc = Document.parse(gatewayUsages);
    mongoTemplate.insert(doc, "energy");
  }
}
