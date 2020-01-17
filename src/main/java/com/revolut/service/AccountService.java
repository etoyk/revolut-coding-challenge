package com.revolut.service;

import static com.revolut.service.TransactionService.buildTransaction;

import com.revolut.domain.Account;
import com.revolut.domain.Currency;
import com.revolut.domain.Exceptions.CouldNotAcquiredLockException;
import com.revolut.domain.Exceptions.EntityNotFoundException;
import com.revolut.domain.Exceptions.InvalidTransactionId;
import com.revolut.domain.Exceptions.NotEnoughBalanceException;
import com.revolut.domain.Exceptions.SomethingWentWrongException;
import com.revolut.domain.Transaction.TransactionType;
import com.revolut.repo.AccountRepo;
import com.revolut.repo.TransactionHelper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccountService {

  private final AccountRepo accounts;
  private final Lock lock;
  private final TransactionHelper transactionHelper;
  private final TransactionService transactionService;

  public AccountService(
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

  public Account get(Integer id) {
    var acc = accounts.get(id);
    if (acc == null) {
      throw new EntityNotFoundException(String.format("Account cannot be found: %d", id));
    }
    return acc;
  }

  public List<Account> getAll() {
    return accounts.getAll();
  }

  public Account insert(String name, Long balance, Currency currency) {
    Account acc = new Account(name, currency, balance);
    return accounts.insert(acc);
  }

  public void update(int id, String name) {
    var acc = get(id);
    acc.setName(name);
    accounts.update(acc);
  }

  public void delete(Integer id) {
    try {
      lock.runWithLock(id, () -> {
        validateAccountExists(id);
        accounts.delete(id);
      });
    } catch (CouldNotAcquiredLockException e) {
      log.error("Couldn't acquire lock for {}, ", id, e);
      throw new SomethingWentWrongException(e);
    }
  }

  public void withdraw(Integer id, Long amount, String txId) {
    try {
      lock.runWithLock(id, txId, () -> {
        validateTxId(txId);
        var account = get(id);
        validateWithdraw(account, amount);
        transactionHelper.runTransactional(tx -> {
          account.removeBalance(amount);
          accounts.updateTransactional(account, tx);
          transactionService
              .saveTransactional(buildTransaction(account, amount, TransactionType.WITHDRAW, txId),
                  tx);
        });
      });
    } catch (CouldNotAcquiredLockException e) {
      log.error("Couldn't acquire lock for {}, ", id, e);
      throw new SomethingWentWrongException(e);
    }
  }

  public void deposit(Integer id, Long amount, String txId) {
    try {
      lock.runWithLock(id, txId, () -> {
        var account = get(id);
        validateTxId(txId);
        transactionHelper.runTransactional(tx -> {
          account.addBalance(amount);
          accounts.updateTransactional(account, tx);
          transactionService
              .saveTransactional(buildTransaction(account, amount, TransactionType.DEPOSIT, txId),
                  tx);
        });
      });
    } catch (CouldNotAcquiredLockException e) {
      log.error("Couldn't acquire lock for {}, ", id, e);
      throw new SomethingWentWrongException(e);
    }
  }

  private void validateAccountExists(Integer id) {
    get(id);
  }

  private void validateTxId(String txId) {
    if (!transactionService.isUsable(txId)) {
      throw new InvalidTransactionId(String.format("%s is not a valid txId", txId));
    }
  }

  private void validateWithdraw(Account account, Long amount) {
    if (account.getBalance().compareTo(amount) < 0) {
      throw new NotEnoughBalanceException("Source account doesn't have enough balance");
    }
  }
}
