package com.revolut.web.handler;

import static com.revolut.web.Validators.validateDepositBalanceRequest;
import static com.revolut.web.Validators.validateNewAccountRequest;
import static com.revolut.web.Validators.validatePathParamUserId;
import static com.revolut.web.Validators.validateTransferBalanceRequest;
import static com.revolut.web.Validators.validateUpdateAccountRequest;
import static com.revolut.web.Validators.validateWithdrawBalanceRequest;

import com.revolut.domain.Account;
import com.revolut.service.AccountService;
import com.revolut.service.TransferService;
import com.revolut.web.reqresp.req.DepositBalanceRequest;
import com.revolut.web.reqresp.resp.ErrorResponse;
import com.revolut.web.reqresp.req.NewAccountRequest;
import com.revolut.web.reqresp.req.TransferBalanceRequest;
import com.revolut.web.reqresp.req.UpdateAccountRequest;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.HttpMethod;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiContent;
import io.javalin.plugin.openapi.annotations.OpenApiParam;
import io.javalin.plugin.openapi.annotations.OpenApiRequestBody;
import io.javalin.plugin.openapi.annotations.OpenApiResponse;

public class AccountHandler {

  public static final String PARAM_USER_ID = "id";
  public static final String PARAM_USER_FROM_ID = "fromId";
  public static final String PARAM_USER_TO_ID = "toId";

  private final AccountService accountService;
  private final TransferService transferService;

  public AccountHandler(
      AccountService accountService,
      TransferService transferService
  ) {
    this.accountService = accountService;
    this.transferService = transferService;
  }

  @OpenApi(
      summary = "Gets account by id",
      operationId = "getAccountById",
      path = "/accounts/:id",
      method = HttpMethod.GET,
      pathParams = {
          @OpenApiParam(name = "id", type = Integer.class, description = "The account Id")},
      tags = {"Account"},
      responses = {
          @OpenApiResponse(status = "200", content = {@OpenApiContent(from = Account.class)}),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
          @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
      }
  )
  public void getOne(Context ctx) {
    ctx.json(accountService.get(validatePathParamUserId(ctx, PARAM_USER_ID)));
  }

  @OpenApi(
      summary = "Gets all accounts",
      operationId = "getAllAccounts",
      path = "/accounts",
      method = HttpMethod.GET,
      tags = {"Account"},
      responses = {
          @OpenApiResponse(status = "200", content = {@OpenApiContent(from = Account[].class)}),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
          @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
      }
  )
  public void getAll(Context ctx) {
    ctx.json(accountService.getAll());
  }

  @OpenApi(
      summary = "Create account",
      operationId = "createAccount",
      path = "/accounts",
      method = HttpMethod.POST,
      tags = {"Account"},
      requestBody = @OpenApiRequestBody(content = {
          @OpenApiContent(from = NewAccountRequest.class)}),
      responses = {
          @OpenApiResponse(status = "201"),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)})
      }
  )
  public void create(Context ctx) {
    var account = validateNewAccountRequest(ctx);
    ctx.json(accountService.insert(account.getName(), account.getBalance(), account.getCurrency()));
    ctx.status(201);
  }

  @OpenApi(
      summary = "Update account by Id",
      operationId = "updateAccountById",
      path = "/accounts/:id",
      method = HttpMethod.PUT,
      pathParams = {
          @OpenApiParam(name = "id", type = Integer.class, description = "The account Id")},
      tags = {"Account"},
      requestBody = @OpenApiRequestBody(content = {
          @OpenApiContent(from = UpdateAccountRequest.class)}),
      responses = {
          @OpenApiResponse(status = "204"),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
          @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
      }
  )
  public void update(Context ctx) {
    var account = validateUpdateAccountRequest(ctx);
    var id = validatePathParamUserId(ctx, PARAM_USER_ID);
    accountService.update(id, account.getName());
    ctx.status(204);
  }

  @OpenApi(
      summary = "Transfer balance between accounts",
      operationId = "transfer",
      path = "/accounts/transfer/from/:fromId/to/:toId",
      method = HttpMethod.PATCH,
      pathParams = {
          @OpenApiParam(name = "fromId", type = Integer.class, description = "The account Id that balance will be removed from"),
          @OpenApiParam(name = "toId", type = Integer.class, description = "The account Id that balance will be added to"),
      },
      requestBody = @OpenApiRequestBody(content = {
          @OpenApiContent(from = TransferBalanceRequest.class)}),
      tags = {"Account"},
      responses = {
          @OpenApiResponse(status = "204"),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
          @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
      }
  )
  public void transfer(Context ctx) {
    var request = validateTransferBalanceRequest(ctx);
    var fromId = validatePathParamUserId(ctx, PARAM_USER_FROM_ID);
    var toId = validatePathParamUserId(ctx, PARAM_USER_TO_ID);
    transferService.transfer(fromId, toId, request.getAmount(), request.getTxId());
  }

  @OpenApi(
      summary = "Delete account by Id",
      operationId = "deleteAccountById",
      path = "/accounts/:id",
      method = HttpMethod.DELETE,
      pathParams = {
          @OpenApiParam(name = "id", type = Integer.class, description = "The account Id")},
      tags = {"Account"},
      responses = {
          @OpenApiResponse(status = "204"),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
          @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
      }
  )
  public void delete(Context ctx) {
    var id = validatePathParamUserId(ctx, PARAM_USER_ID);
    accountService.delete(id);
  }

  @OpenApi(
      summary = "Deposit balance to an account",
      operationId = "deposit",
      path = "/accounts/deposit/:id",
      method = HttpMethod.PATCH,
      pathParams = {
          @OpenApiParam(name = "id", type = Integer.class, description = "The account Id that balance will be added to"),
      },
      requestBody = @OpenApiRequestBody(content = {
          @OpenApiContent(from = DepositBalanceRequest.class)}),
      tags = {"Account"},
      responses = {
          @OpenApiResponse(status = "204"),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
          @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
      }
  )
  public void deposit(Context ctx) {
    var req = validateDepositBalanceRequest(ctx);
    var id = validatePathParamUserId(ctx, "id");
    accountService.deposit(id, req.getAmount(), req.getTxId());
    ctx.status(204);
  }

  @OpenApi(
      summary = "Withdraw balance from an account",
      operationId = "withdraw",
      path = "/accounts/withdraw/:id",
      method = HttpMethod.PATCH,
      pathParams = {
          @OpenApiParam(name = "id", type = Integer.class, description = "The account Id that balance will be removed from"),
      },
      requestBody = @OpenApiRequestBody(content = {
          @OpenApiContent(from = DepositBalanceRequest.class)}),
      tags = {"Account"},
      responses = {
          @OpenApiResponse(status = "204"),
          @OpenApiResponse(status = "400", content = {@OpenApiContent(from = ErrorResponse.class)}),
          @OpenApiResponse(status = "404", content = {@OpenApiContent(from = ErrorResponse.class)})
      }
  )
  public void withdraw(Context ctx) {
    var req = validateWithdrawBalanceRequest(ctx);
    var id = validatePathParamUserId(ctx, "id");
    accountService.withdraw(id, req.getAmount(), req.getTxId());
    ctx.status(204);
  }
}
