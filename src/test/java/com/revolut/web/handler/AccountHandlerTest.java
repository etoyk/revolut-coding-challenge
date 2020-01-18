package com.revolut.web.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.domain.Account;
import com.revolut.domain.Currency;
import com.revolut.service.AccountService;
import com.revolut.service.TransferService;
import io.javalin.http.util.ContextUtil;
import java.util.Collections;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

public class AccountHandlerTest {

  private final AccountService accountService = mock(AccountService.class);
  private final TransferService transferService = mock(TransferService.class);
  private final HttpServletRequest request = mock(HttpServletRequest.class);
  private final HttpServletResponse response = mock(HttpServletResponse.class);
  private final AccountHandler handler = new AccountHandler(accountService, transferService);
  private static final ObjectMapper mapper = new ObjectMapper();

  @Test
  public void getAll() {
    var acc = anAccount();
    withAccount(acc);
    var ctx = ContextUtil.init(request, response);

    handler.getAll(ctx);

    var res = ctx.resultString();
    assertThat(deserialize(res, Account[].class)).containsExactly(acc);
  }

  @Test
  public void getOne() {
    var acc = anAccount();
    withAccount(acc);
    var pathRequestParamMap = Map.of("id", "1");
    var ctx = ContextUtil.init(request, response, "/", pathRequestParamMap);

    handler.getOne(ctx);

    var res = ctx.resultString();
    assertThat(deserialize(res, Account.class)).isEqualTo(acc);
  }

  private <T> T deserialize(String json, Class<T> type) {
    try {
      return mapper.readValue(json, type);
    } catch (Exception e) {
      return null;
    }
  }

  private void withAccount(Account account) {
    when(accountService.getAll())
        .thenReturn(Collections.singletonList(account));

    when(accountService.get(anyInt()))
        .thenReturn((account));
  }

  private Account anAccount() {
    return new Account(1, "egemen", Currency.EUR, 10L);
  }

}
