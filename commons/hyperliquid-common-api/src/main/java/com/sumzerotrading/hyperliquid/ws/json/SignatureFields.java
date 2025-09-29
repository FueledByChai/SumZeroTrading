package com.sumzerotrading.hyperliquid.ws.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "r", "s", "v" })
public class SignatureFields {
  @JsonProperty("r")
  public String r;

  @JsonProperty("s")
  public String s;

  @JsonProperty("v")
  public int v;

}