package com.revolut.web;

import com.revolut.service.TransactionService;
import com.revolut.web.reqresp.req.DepositBalanceRequest;
import com.revolut.web.reqresp.req.NewAccountRequest;
import com.revolut.web.reqresp.req.TransferBalanceRequest;
import com.revolut.web.reqresp.req.UpdateAccountRequest;
import com.revolut.web.reqresp.req.WithdrawBalanceRequest;
import io.javalin.http.Context;

public class Validators {

  public static final Long MIN_ACC_CREATION_BALANCE_INCLUDING = 0L;
  public static final Long MIN_TRANSFER_AMOUNT_EXCLUDING = 0L;
  public static final int MAX_NAME_LENGTH = 25;

  public static int validatePathParamUserId(Context ctx, String param) {
    return ctx.pathParam(param, Integer.class).check(id -> id >= 0).get();
  }

  public static NewAccountRequest validateNewAccountRequest(Context ctx) {
    return ctx
        .bodyValidator(NewAccountRequest.class)
        .check(req -> req.getBalance().compareTo(MIN_ACC_CREATION_BALANCE_INCLUDING) >= 0,
            String.format("Balance cannot be smaller than %d",
                MIN_ACC_CREATION_BALANCE_INCLUDING.intValue()))
        .check(acc -> acc.getName().length() <= MAX_NAME_LENGTH,
            String.format("Name cannot be longer than %d", MAX_NAME_LENGTH))
        .getOrNull();
  }

  public static UpdateAccountRequest validateUpdateAccountRequest(Context ctx) {
    return ctx
        .bodyValidator(UpdateAccountRequest.class)
        .check(req -> req.getName().length() <= MAX_NAME_LENGTH,
            String.format("Name cannot be longer than %d", MAX_NAME_LENGTH))
        .getOrNull();
  }

  public static TransferBalanceRequest validateTransferBalanceRequest(Context ctx) {
    return ctx
        .bodyValidator(TransferBalanceRequest.class)
        .check(req -> req.getAmount().compareTo(MIN_TRANSFER_AMOUNT_EXCLUDING) > 0,
            String.format("Amount cannot be equal or smaller than %d",
                MIN_TRANSFER_AMOUNT_EXCLUDING.intValue()))
        .check(req -> TransactionService.validateTxId(req.getTxId()), "txId is not valid")
        .getOrNull();
  }

  public static DepositBalanceRequest validateDepositBalanceRequest(Context ctx) {
    return ctx
        .bodyValidator(DepositBalanceRequest.class)
        .check(req -> req.getAmount().compareTo(MIN_TRANSFER_AMOUNT_EXCLUDING) > 0,
            String.format("Amount cannot be equal or smaller than %d",
                MIN_TRANSFER_AMOUNT_EXCLUDING.intValue()))
        .check(req -> TransactionService.validateTxId(req.getTxId()), "txId is not valid")
        .getOrNull();
  }

  public static WithdrawBalanceRequest validateWithdrawBalanceRequest(Context ctx) {
    return ctx
        .bodyValidator(WithdrawBalanceRequest.class)
        .check(req -> req.getAmount().compareTo(MIN_TRANSFER_AMOUNT_EXCLUDING) > 0,
            String.format("Amount cannot be equal or smaller than %d",
                MIN_TRANSFER_AMOUNT_EXCLUDING.intValue()))
        .check(req -> TransactionService.validateTxId(req.getTxId()), "txId is not valid")
        .getOrNull();
  }
}
