/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.api.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author tuan3.nguyen@gmail.com
 */
@Document(collection = "energy")
public class MeterReadingEntity {
  @Id
  private String id;

  private String gatewayId;

  private int meterId;

  private Date date;

  private Float va;

  public Date getDate() {
    return date;
  }

  public String getGatewayId() {
    return gatewayId;
  }

  public String getId() {
    return id;
  }

  public int getMeterId() {
    return meterId;
  }

  public Float getVa() {
    return va;
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

  public void setMeterId(final int meterId) {
    this.meterId = meterId;
  }

  public void setVa(final Float va) {
    this.va = va;
  }
}
