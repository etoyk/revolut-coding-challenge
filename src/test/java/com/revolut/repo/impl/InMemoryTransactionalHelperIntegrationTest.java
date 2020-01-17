package com.revolut.repo.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.revolut.db.InMemoryDB;
import com.revolut.domain.Currency;
import com.revolut.domain.Transaction;
import com.revolut.domain.Transaction.TransactionType;
import org.junit.jupiter.api.Test;

public class InMemoryTransactionalHelperIntegrationTest {

  @Test
  public void testTransactional() {
    var db = new InMemoryDB();
    var helper = new InMemoryTransactionHelper(db);
    var transactionRepo = new InMemoryTransactionRepo(db);
    String transactionId = "tx_1";

    try {
      helper.runTransactional(tx -> {
        transactionRepo.saveTransactional(Transaction
            .builder()
            .transactionId(transactionId)
            .relatedEntityId(0)
            .amount(10L)
            .currency(Currency.EUR)
            .operationName(TransactionType.WITHDRAW)
            .timestampMs(1L)
            .build(), tx);
        throw new RuntimeException();
      });
    } catch (Exception e) {

    }
    assertThat(transactionRepo.exists(transactionId)).isFalse();

  }
}
