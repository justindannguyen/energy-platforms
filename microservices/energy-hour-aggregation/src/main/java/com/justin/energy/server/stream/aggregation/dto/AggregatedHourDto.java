/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.energy.server.stream.aggregation.dto;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class AggregatedHourDto {
  private String gatewayId;
  private Integer meterId;
  private Long date;
  private Float averageVa;
  private Float minVa;
  private Float maxVa;
  private Float averageCa;
  private Float minCa;
  private Float maxCa;

  public Float getAverageCa() {
    return averageCa;
  }

  public Float getAverageVa() {
    return averageVa;
  }

  public Long getDate() {
    return date;
  }

  public String getGatewayId() {
    return gatewayId;
  }

  public Float getMaxCa() {
    return maxCa;
  }

  public Float getMaxVa() {
    return maxVa;
  }

  public Integer getMeterId() {
    return meterId;
  }

  public Float getMinCa() {
    return minCa;
  }

  public Float getMinVa() {
    return minVa;
  }

  public void setAverageCa(final Float averageCa) {
    this.averageCa = averageCa;
  }

  public void setAverageVa(final Float averageVa) {
    this.averageVa = averageVa;
  }

  public void setDate(final Long date) {
    this.date = date;
  }

  public void setGatewayId(final String gatewayId) {
    this.gatewayId = gatewayId;
  }

  public void setMaxCa(final Float maxCa) {
    this.maxCa = maxCa;
  }

  public void setMaxVa(final Float maxVa) {
    this.maxVa = maxVa;
  }

  public void setMeterId(final Integer meterId) {
    this.meterId = meterId;
  }

  public void setMinCa(final Float minCa) {
    this.minCa = minCa;
  }

  public void setMinVa(final Float minVa) {
    this.minVa = minVa;
  }
}
