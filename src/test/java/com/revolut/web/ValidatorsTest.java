package com.revolut.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.domain.Currency;
import com.revolut.repo.TransactionRepo;
import com.revolut.service.TransactionService;
import com.revolut.web.reqresp.req.DepositBalanceRequest;
import com.revolut.web.reqresp.req.NewAccountRequest;
import com.revolut.web.reqresp.req.UpdateAccountRequest;
import com.revolut.web.reqresp.req.WithdrawBalanceRequest;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.util.ContextUtil;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ValidatorsTest {


  private static final String USER_ID_PARAM = "id";
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final String NAME = "egemen";
  private static final long POSITIVE_BALANCE = 1L;
  private static final Currency EUR = Currency.EUR;
  private static final long NEGATIVE_BALANCE = -1L;
  private static final String LONG_NAME = "12345678901234567890123456";
  private static final long NEGATIVE_AMOUNT = -1L;
  private static final long POSITIVE_AMOUNT = 1L;
  private static final String INVALID_TX_ID = "tx";
  private static final TransactionService transactionService = new TransactionService(
      Mockito.mock(TransactionRepo.class));

  @Test
  public void pathParameterUserIdNegativeThrowsException() {
    var req = withRequest();
    var map = Map.of(USER_ID_PARAM, "-1");
    var ctx = ContextUtil.init(req, withResponse(), "/", map);
    Assertions.assertThrows(BadRequestResponse.class,
        () -> Validators.validatePathParamUserId(ctx, USER_ID_PARAM));
  }

  @Test
  public void pathParameterUserIdValid() {
    var req = withRequest();
    var map = Map.of(USER_ID_PARAM, "1");
    var ctx = ContextUtil.init(req, withResponse(), "/", map);
    var userId = Validators.validatePathParamUserId(ctx, USER_ID_PARAM);
    assertThat(userId).isEqualTo(1);
  }

  @Test
  public void newAccountRequestNegativeBalance() throws IOException {
    var newAccRequest = new NewAccountRequest(NAME, EUR, NEGATIVE_BALANCE);
    var req = withBodyOf(newAccRequest);
    var ctx = ContextUtil.init(req, withResponse());
    Assertions
        .assertThrows(BadRequestResponse.class, () -> Validators.validateNewAccountRequest(ctx));
  }

  @Test
  public void newAccountRequestMaxNameLength() throws IOException {
    var newAccRequest = new NewAccountRequest("12345678901234567890123456", EUR, POSITIVE_BALANCE);
    var req = withBodyOf(newAccRequest);
    var ctx = ContextUtil.init(req, withResponse());
    Assertions
        .assertThrows(BadRequestResponse.class, () -> Validators.validateNewAccountRequest(ctx));
  }

  @Test
  public void newAccountRequestValid() throws IOException {
    var newAccRequest = new NewAccountRequest(NAME, EUR, POSITIVE_BALANCE);
    var req = withBodyOf(newAccRequest);
    var ctx = ContextUtil.init(req, withResponse());

    var validatedReq = Validators.validateNewAccountRequest(ctx);

    assertThat(validatedReq.getBalance()).isEqualTo(POSITIVE_BALANCE);
    assertThat(validatedReq.getName()).isEqualTo(NAME);
    assertThat(validatedReq.getCurrency()).isEqualTo(EUR);
  }

  @Test
  public void updateAccountRequestMaxLength() throws IOException {
    var updateAccRequest = new UpdateAccountRequest(LONG_NAME);
    var req = withBodyOf(updateAccRequest);
    var ctx = ContextUtil.init(req, withResponse());

    Assertions
        .assertThrows(BadRequestResponse.class,
            () -> Validators.validateUpdateAccountRequest(ctx));
  }

  @Test
  public void updateAccountRequestValid() throws IOException {
    var updateAccRequest = new UpdateAccountRequest(NAME);
    var req = withBodyOf(updateAccRequest);
    var ctx = ContextUtil.init(req, withResponse());

    var validatedReq = Validators.validateUpdateAccountRequest(ctx);

    assertThat(validatedReq.getName()).isEqualTo(NAME);
  }

  @Test
  public void withdrawRequestMinBalance() throws IOException {
    var withdrawBalanceReq = new WithdrawBalanceRequest(NEGATIVE_AMOUNT,
        transactionService.generateTxId());
    var req = withBodyOf(withdrawBalanceReq);
    var ctx = ContextUtil.init(req, withResponse());

    Assertions
        .assertThrows(BadRequestResponse.class,
            () -> Validators.validateWithdrawBalanceRequest(ctx));
  }

  @Test
  public void withdrawRequestInvalidTxId() throws IOException {
    var withdrawBalanceReq = new WithdrawBalanceRequest(POSITIVE_AMOUNT, INVALID_TX_ID);
    var req = withBodyOf(withdrawBalanceReq);
    var ctx = ContextUtil.init(req, withResponse());

    Assertions
        .assertThrows(BadRequestResponse.class,
            () -> Validators.validateWithdrawBalanceRequest(ctx));
  }

  @Test
  public void withdrawRequestValid() throws IOException {
    String txId = transactionService.generateTxId();
    var withdrawBalanceReq = new WithdrawBalanceRequest(POSITIVE_AMOUNT,
        txId);
    var req = withBodyOf(withdrawBalanceReq);
    var ctx = ContextUtil.init(req, withResponse());

    var validatedReq = Validators.validateWithdrawBalanceRequest(ctx);

    assertThat(validatedReq.getAmount()).isEqualTo(POSITIVE_AMOUNT);
    assertThat(validatedReq.getTxId()).isEqualTo(txId);
  }

  @Test
  public void depositRequestMinBalance() throws IOException {
    var depositBalanceRequest = new DepositBalanceRequest(NEGATIVE_AMOUNT,
        transactionService.generateTxId());
    var req = withBodyOf(depositBalanceRequest);
    var ctx = ContextUtil.init(req, withResponse());

    Assertions
        .assertThrows(BadRequestResponse.class,
            () -> Validators.validateDepositBalanceRequest(ctx));

  }

  @Test
  public void depositRequestInvalidTxId() throws IOException {
    var depositBalanceRequest = new DepositBalanceRequest(POSITIVE_AMOUNT, INVALID_TX_ID);
    var req = withBodyOf(depositBalanceRequest);
    var ctx = ContextUtil.init(req, withResponse());

    Assertions
        .assertThrows(BadRequestResponse.class,
            () -> Validators.validateDepositBalanceRequest(ctx));
  }

  @Test
  public void depositRequestValid() throws IOException {
    String txId = transactionService.generateTxId();
    var depositBalanceRequest = new DepositBalanceRequest(POSITIVE_AMOUNT,
        txId);
    var req = withBodyOf(depositBalanceRequest);
    var ctx = ContextUtil.init(req, withResponse());
    var validatedReq = Validators.validateDepositBalanceRequest(ctx);
    assertThat(validatedReq.getAmount()).isEqualTo(POSITIVE_AMOUNT);
    assertThat(validatedReq.getTxId()).isEqualTo(txId);
  }

  //mocks all possible ways of getting the body from HttpServletRequest
  private <T> HttpServletRequest withBodyOf(T obj) throws IOException {
    var json = mapper.writeValueAsString(obj);
    var request = withRequest();
    when(request.getInputStream()).thenReturn(
        new DelegatingServletInputStream(
            new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))));
    when(request.getReader()).thenReturn(
        new BufferedReader(new StringReader(json)));
    when(request.getContentType()).thenReturn("application/json");
    when(request.getCharacterEncoding()).thenReturn("UTF-8");
    return request;
  }

  private HttpServletRequest withRequest() {
    return Mockito.mock(HttpServletRequest.class);
  }

  private HttpServletResponse withResponse() {
    return Mockito.mock(HttpServletResponse.class);
  }

}
