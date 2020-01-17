package com.revolut.service;

import static com.revolut.service.TransactionService.buildTransaction;

import com.revolut.domain.Account;
import com.revolut.domain.Exceptions.CouldNotAcquiredLockException;
import com.revolut.domain.Exceptions.CurrencyMismatchException;
import com.revolut.domain.Exceptions.EntityNotFoundException;
import com.revolut.domain.Exceptions.InvalidTransactionId;
import com.revolut.domain.Exceptions.NotEnoughBalanceException;
import com.revolut.domain.Exceptions.SomethingWentWrongException;
import com.revolut.domain.Transaction.TransactionType;
import com.revolut.repo.AccountRepo;
import com.revolut.repo.TransactionHelper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransferService {

  private final AccountRepo accounts;
  private final Lock lock;
  private final TransactionHelper transactionHelper;
  private final TransactionService transactionService;

  public TransferService(
      AccountRepo accounts,
      Lock lock,
      TransactionHelper transactionHelper,
      TransactionService transactionService
  ) {
    this.accounts = accounts;
    this.lock = lock;
    this.transactionHelper = transactionHelper;
    this.transactionService = transactionService;
  }

  public boolean transfer(Integer from, Integer to, Long amount, String txId) {
    try {
      lock.runWithLock(from, to, txId, () -> {
        var fromAcc = accounts.get(from);
        var toAcc = accounts.get(to);
        validateTransfer(fromAcc, toAcc, amount, txId);
        transactionHelper.runTransactional(tx -> {
          fromAcc.removeBalance(amount);
          accounts.updateTransactional(fromAcc, tx);
          toAcc.addBalance(amount);
          accounts.updateTransactional(toAcc, tx);
          transactionService
              .saveTransactional(
                  buildTransaction(fromAcc, amount, TransactionType.TRANSFER_SOURCE, txId), tx);
          transactionService
              .saveTransactional(
                  buildTransaction(toAcc, amount, TransactionType.TRANSFER_DESTINATION, txId), tx);
        });
      });
    } catch (CouldNotAcquiredLockException e) {
      log.error("Couldn't acquire lock for {} and {}, ", from, to, e);
      throw new SomethingWentWrongException(e);
    }
    return true;
  }

  private void validateTransfer(Account from, Account to, Long amount, String txId) {
    if (!transactionService.isUsable(txId)) {
      throw new InvalidTransactionId(String.format("%s is not a valid txId", txId));
    }

    if (from == null || to == null) {
      throw new EntityNotFoundException("Account cannot be found");
    }
    if (from.getCurrency() != to.getCurrency()) {
      throw new CurrencyMismatchException(
          "Source and destination account currencies are different");
    }
    if (from.getBalance().compareTo(amount) < 0) {
      throw new NotEnoughBalanceException("Source account doesn't have enough balance");
    }
  }
}
