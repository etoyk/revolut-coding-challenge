package com.revolut.repo;

import com.revolut.domain.Transaction;
import java.util.List;
import org.jooq.Configuration;

public interface TransactionRepo {

  void saveTransactional(Transaction tx, Configuration configuration);

  List<Transaction> getAll();

  boolean exists(String txId);
}
