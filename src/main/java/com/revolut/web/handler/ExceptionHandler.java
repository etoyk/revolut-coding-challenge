package com.revolut.web.handler;

import com.revolut.domain.Exceptions.CurrencyMismatchException;
import com.revolut.domain.Exceptions.EntityNotFoundException;
import com.revolut.domain.Exceptions.InvalidTransactionId;
import com.revolut.domain.Exceptions.NotEnoughBalanceException;
import com.revolut.web.reqresp.resp.ErrorResponse;
import io.javalin.http.Context;

public class ExceptionHandler {

  public void handle(RuntimeException e, Context ctx) {
    Integer statusCode;
    if (e instanceof EntityNotFoundException) {
      statusCode = 404;
    } else if (e instanceof InvalidTransactionId) {
      statusCode = 400;
    } else if (e instanceof NotEnoughBalanceException || e instanceof CurrencyMismatchException) {
      statusCode = 500;
    } else {
      statusCode = 500;
    }
    ctx.status(statusCode);
    ctx.json(ErrorResponse
        .builder()
        .status(statusCode)
        .title(e.getMessage())
        .build());
  }
}
