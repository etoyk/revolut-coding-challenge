package com.revolut.web.reqresp.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(name = "DepositBalanceRequest")
public class DepositBalanceRequest {

  @Schema(required = true, description = "Amount that will be deposited,"
      + " with 2 decimal places at the end (10000 means 100.00)")
  private Long amount;

  @Schema(required = true, description = "Transaction id that can be obtained by calling /transactions endpoint."
      + " Necessary for preventing double spending problem")
  private String txId;

}
