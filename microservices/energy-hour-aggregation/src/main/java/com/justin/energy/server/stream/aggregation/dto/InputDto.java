/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.stream.aggregation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author tuan3.nguyen@gmail.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InputDto {
  private String gatewayId;
  private Integer meterId;
  private Float va;
  private Float ca;
  private Long date;

  public Float getCa() {
    return ca;
  }

  public Long getDate() {
    return date;
  }

  public String getGatewayId() {
    return gatewayId;
  }

  public Integer getMeterId() {
    return meterId;
  }

  public Float getVa() {
    return va;
  }

  public void setCa(final Float ca) {
    this.ca = ca;
  }

  public void setDate(final Long date) {
    this.date = date;
  }

  public void setGatewayId(final String gatewayId) {
    this.gatewayId = gatewayId;
  }

  public void setMeterId(final Integer meterId) {
    this.meterId = meterId;
  }

  public void setVa(final Float va) {
    this.va = va;
  }
}
