/**
 * Copyright (C) 2018, Justin Nguyen
 */
package com.justin.transmission.dto;

/**
 * @author tuan3.nguyen@gmail.com
 */
public class FotaDto {
  private String gatewayId;
  private String remoteUrl;

  public String getGatewayId() {
    return gatewayId;
  }

  public String getRemoteUrl() {
    return remoteUrl;
  }

  public void setGatewayId(final String gatewayId) {
    this.gatewayId = gatewayId;
  }

  public void setRemoteUrl(final String remoteUrl) {
    this.remoteUrl = remoteUrl;
  }
}
