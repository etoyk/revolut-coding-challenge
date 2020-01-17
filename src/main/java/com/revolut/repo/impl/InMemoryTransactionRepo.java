package com.revolut.repo.impl;

import static com.revolut.db.tables.Transactions.TRANSACTIONS;
import static org.jooq.impl.DSL.count;

import com.revolut.db.InMemoryDB;
import com.revolut.domain.Transaction;
import com.revolut.repo.TransactionRepo;
import java.util.List;
import org.jooq.Configuration;
import org.jooq.impl.DSL;

public class InMemoryTransactionRepo implements TransactionRepo {

  private final InMemoryDB db;

  public InMemoryTransactionRepo(InMemoryDB db) {
    this.db = db;
  }

  @Override
  public void saveTransactional(Transaction tx, Configuration configuration) {
    DSL.using(configuration)
        .insertInto(TRANSACTIONS, TRANSACTIONS.AMOUNT, TRANSACTIONS.CURRENCY,
            TRANSACTIONS.OPERATION_NAME, TRANSACTIONS.RELATED_ENTITY_ID, TRANSACTIONS.TIMESTAMP_MS,
            TRANSACTIONS.TRANSACTION_ID)
        .values(tx.getAmount(), tx.getCurrency().toString(), tx.getOperationName().toString(),
            tx.getRelatedEntityId(), tx.getTimestampMs(), tx.getTransactionId())
        .execute();
  }

  @Override
  public List<Transaction> getAll() {
    return db.ctx()
        .selectFrom(TRANSACTIONS)
        .fetchInto(Transaction.class);
  }

  @Override
  public boolean exists(String txId) {
    var res =  db.ctx()
        .select(count())
        .from(TRANSACTIONS)
        .where(TRANSACTIONS.TRANSACTION_ID.eq(txId))
        .fetchInto(Integer.class).get(0) > 0;
    return res;
  }
}
