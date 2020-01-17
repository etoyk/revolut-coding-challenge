package com.revolut.repo;


import com.revolut.repo.impl.InMemoryTransactionHelper.Tx;

public interface TransactionHelper {

  void runTransactional(Tx runnable);

}
