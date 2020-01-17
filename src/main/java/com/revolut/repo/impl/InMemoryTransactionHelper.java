package com.revolut.repo.impl;

import com.revolut.db.InMemoryDB;
import com.revolut.repo.TransactionHelper;
import org.jooq.Configuration;
import org.jooq.impl.DSL;

public class InMemoryTransactionHelper implements TransactionHelper {

  private final InMemoryDB db;

  public InMemoryTransactionHelper(InMemoryDB db) {
    this.db = db;
  }

  public void runTransactional(Tx runnable) {
    DSL.using(db.ctx().configuration())
        .transaction(runnable::run);
  }

  public interface Tx {

    void run(Configuration cf);
  }
}
