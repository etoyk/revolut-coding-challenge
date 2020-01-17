package com.revolut.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.revolut.domain.Account;
import com.revolut.domain.Currency;
import com.revolut.domain.Exceptions.CurrencyMismatchException;
import com.revolut.domain.Exceptions.InvalidTransactionId;
import com.revolut.domain.Exceptions.NotEnoughBalanceException;
import com.revolut.repo.AccountRepo;
import com.revolut.repo.impl.TestTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class TransferServiceTest {


  public final AccountRepo accountRepo = Mockito.mock(AccountRepo.class);
  public final TransactionService txService = Mockito
      .mock(TransactionService.class);
  public final TestTransactionHelper txHelper = new TestTransactionHelper();
  public final TransferService service = new TransferService(accountRepo, new Lock(), txHelper,
      txService);

  @Test
  public void successfulTransferShouldLog2Transactions() {
    withValidTxId();
    when(accountRepo.get(any())).thenReturn(anAccount(100L));
    service.transfer(0, 1, 10L, "tx-id");
    verify(txService, times(2)).saveTransactional(any(), any());
  }

  @Test
  public void shouldValidateBalance() {
    withValidTxId();
    when(accountRepo.get(any())).thenReturn(anAccount(10L));
    Assertions.assertThrows(NotEnoughBalanceException.class, () -> {
      service.transfer(0, 1, 100L, "tx-id");
    });
    verify(accountRepo, never()).updateTransactional(any(), any());
  }

  @Test
  public void shouldValidateCurrency() {
    withValidTxId();
    when(accountRepo.get(eq(0))).thenReturn(anAccount(100L, Currency.EUR));
    when(accountRepo.get(eq(1))).thenReturn(anAccount(100L, Currency.USD));
    Assertions.assertThrows(CurrencyMismatchException.class, () -> {
      service.transfer(0, 1, 10L, "tx-id");
    });
    verify(accountRepo, never()).updateTransactional(any(), any());
  }

  @Test
  public void shouldValidateTransactionId() {
    withInvalidTxId();
    Assertions.assertThrows(InvalidTransactionId.class, () -> {
      service.transfer(0, 1, 100L, "tx-id");
    });
    verify(accountRepo, never()).updateTransactional(any(), any());
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

  private void withInvalidTxId() {
    when(txService.isUsable(any())).thenReturn(false);
  }

}
