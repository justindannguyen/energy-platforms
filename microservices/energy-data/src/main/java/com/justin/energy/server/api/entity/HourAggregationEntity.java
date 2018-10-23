/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.api.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.justin.energy.server.api.Constants;

/**
 * @author tuan3.nguyen@gmail.com
 */
@Document(collection = Constants.ENERGY_BY_HOUR_COLLECTION)
public class HourAggregationEntity {
  @Id
  private String id;

  private String gatewayId;

  private int meterId;

  private Date date;

  private Float minVa;

  private Float maxVa;

  public Date getDate() {
    return date;
  }

  public String getGatewayId() {
    return gatewayId;
  }

  public String getId() {
    return id;
  }

  public Float getMaxVa() {
    return maxVa;
  }

  public int getMeterId() {
    return meterId;
  }

  public Float getMinVa() {
    return minVa;
  }

  public void setDate(final Date date) {
    this.date = date;
  }

  public void setGatewayId(final String gatewayId) {
    this.gatewayId = gatewayId;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setMaxVa(final Float maxVa) {
    this.maxVa = maxVa;
  }

  public void setMeterId(final int meterId) {
    this.meterId = meterId;
  }

  public void setMinVa(final Float minVa) {
    this.minVa = minVa;
  }
}
