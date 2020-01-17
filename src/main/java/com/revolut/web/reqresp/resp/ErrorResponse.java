package com.revolut.web.reqresp.resp;

import java.util.Map;
import lombok.Builder;

@Builder
public class ErrorResponse {

  public String title;
  public int status;
  public String type;
  public Map<String, String> details;
}