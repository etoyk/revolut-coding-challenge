package com.revolut.repo.impl;

import com.revolut.repo.TransactionHelper;
import com.revolut.repo.impl.InMemoryTransactionHelper.Tx;

public class TestTransactionHelper implements TransactionHelper {

  @Override
  public void runTransactional(Tx runnable) {
    runnable.run(null);
  }
}
