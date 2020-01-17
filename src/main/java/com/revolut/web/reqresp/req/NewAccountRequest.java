package com.revolut.web.reqresp.req;

import com.revolut.domain.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(name = "NewAccountRequest", description = "Request object to create a new account.")
public class NewAccountRequest {

  @Schema(required = true, description = "name of the account holder")
  private String name;

  @Schema(required = true, description = "currency of the account")
  private Currency currency;

  @Schema(required = true, description = "balance of the account with 2 decimal places at the end (10000 means 100.00)")
  private Long balance;

}
