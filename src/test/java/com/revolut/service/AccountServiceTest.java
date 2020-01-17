package com.revolut.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.revolut.domain.Account;
import com.revolut.domain.Currency;
import com.revolut.domain.Exceptions.EntityNotFoundException;
import com.revolut.domain.Exceptions.NotEnoughBalanceException;
import com.revolut.repo.AccountRepo;
import com.revolut.repo.impl.TestTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AccountServiceTest {

  public final AccountRepo accountRepo = Mockito.mock(AccountRepo.class);
  public final TransactionService txService = Mockito
      .mock(TransactionService.class);
  public final TestTransactionHelper txHelper = new TestTransactionHelper();
  public final AccountService service = new AccountService(accountRepo, new Lock(), txHelper,
      txService);

  @Test
  public void successfulDepositShouldLogOneTransaction() {
    withValidTxId();
    when(accountRepo.get(any())).thenReturn(anAccount(100L));
    service.deposit(0, 10L, "tx-id");
    verify(txService, times(1)).saveTransactional(any(), any());
  }

  @Test
  public void successfulWithdrawShouldLogOneTransaction() {
    withValidTxId();
    when(accountRepo.get(any())).thenReturn(anAccount(100L));
    service.withdraw(0, 10L, "tx-id");
    verify(txService, times(1)).saveTransactional(any(), any());
  }

  @Test
  public void shouldValidateBalanceForWithdraw() {
    withValidTxId();
    when(accountRepo.get(any())).thenReturn(anAccount(10L));
    Assertions.assertThrows(NotEnoughBalanceException.class, () -> {
      service.withdraw(0, 100L, "tx-id");
    });
    verify(accountRepo, never()).updateTransactional(any(), any());
  }

  @Test
  public void shouldThrowExceptionIfNotFound() {
    when(accountRepo.get(anyInt())).thenReturn(null);
    Assertions.assertThrows(EntityNotFoundException.class, () -> {
      service.get(0);
    });
  }

  private Account anAccount(Long balance) {
    return anAccount(balance, Currency.EUR);
  }

  private Account anAccount(Long balance, Currency currency) {
    return new Account(1, "acc", currency, balance);
  }

  private void withValidTxId() {
    when(txService.isUsable(any())).thenReturn(true);
  }

}
