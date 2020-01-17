package com.revolut.web.handler;

import com.revolut.domain.Transaction;
import com.revolut.service.TransactionService;
import com.revolut.web.reqresp.resp.ErrorResponse;
import com.revolut.web.reqresp.resp.TransactionIdResponse;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.HttpMethod;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiContent;
import io.javalin.plugin.openapi.annotations.OpenApiResponse;

public class TransactionHandler {


  private final TransactionService transactionService;

  public TransactionHandler(
      TransactionService transactionService
  ) {
    this.transactionService = transactionService;
  }


  @OpenApi(
      summary = "Generates a unique transactionId which will be valid for 5 minutes",
      operationId = "generateTxId",
      path = "/transactions",
      method = HttpMethod.POST,
      tags = {"Transaction"},
      responses = {
          @OpenApiResponse(status = "200", content = {@OpenApiContent(from = TransactionIdResponse.class)}),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
          @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
      }
  )
  public void generateTxId(Context ctx) {
    ctx.json(new TransactionIdResponse(transactionService.generateTxId()));
  }

  @OpenApi(
      summary = "Lists all transactions",
      operationId = "getAllTransactions",
      path = "/transactions",
      method = HttpMethod.GET,
      tags = {"Transaction"},
      responses = {
          @OpenApiResponse(status = "200", content = {@OpenApiContent(from = Transaction[].class)}),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
          @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
      }
  )
  public void getAll(Context ctx) {
    ctx.json(transactionService.getAll());
  }
}
