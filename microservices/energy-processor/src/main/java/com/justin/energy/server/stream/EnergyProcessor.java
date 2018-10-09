/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author tuan3.nguyen@gmail.com
 */
public interface EnergyProcessor extends Processor {
  String SINK_INPUT = "sinkInput";

  @Input(SINK_INPUT)
  SubscribableChannel sinkInput();
}
