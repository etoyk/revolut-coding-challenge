package com.revolut.service;

import com.revolut.domain.Account;
import com.revolut.domain.Transaction;
import com.revolut.domain.Transaction.TransactionType;
import com.revolut.repo.TransactionRepo;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Configuration;

@Slf4j
public class TransactionService {

  private static final Long TRANSACTION_ID_VALIDITY_IN_MS = 1_000L * 60L * 5L; //5 min

  private static AtomicInteger txCounter = new AtomicInteger();
  private final TransactionRepo transactions;

  public TransactionService(TransactionRepo transactions) {
    this.transactions = transactions;
  }

  public List<Transaction> getAll() {
    return transactions.getAll();
  }

  public void saveTransactional(Transaction tx, Configuration config) {
    transactions.saveTransactional(tx, config);
  }

  public boolean isUsable(String txId) {
    if (!validateTxId(txId)) {
      return false;
    }
    if (transactions.exists(txId)) {
      return false;
    }
    return true;
  }

  //should be implemented differently in a distributed system
  public String generateTxId() {
    return String.format("tx-%d-%d", Instant.now().toEpochMilli(), txCounter.incrementAndGet());
  }

  public static boolean validateTxId(String txId) {
    if (txId == null) {
      return false;
    }
    String[] parts = txId.split("-");
    if (parts.length != 3) {
      return false;
    }
    if (!parts[0].equals("tx")) {
      return false;
    }
    if (parts[1].length() != 13) {
      return false;
    }
    try {
      Long timestamp = Long.parseLong(parts[1]);
      if (Instant.now().toEpochMilli() - timestamp > TRANSACTION_ID_VALIDITY_IN_MS) { //expired
        return false;
      }
      if (timestamp > Instant.now().toEpochMilli()) { //txId from future?
        return false;
      }
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  public static Transaction buildTransaction(
      Account account,
      Long amount,
      TransactionType type,
      String txId
  ) {
    return Transaction
        .builder()
        .amount(amount)
        .currency(account.getCurrency())
        .operationName(type)
        .relatedEntityId(account.getId())
        .timestampMs(Instant.now().toEpochMilli())
        .transactionId(txId)
        .build();
  }
}
