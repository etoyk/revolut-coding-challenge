package com.revolut.repo.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.revolut.db.InMemoryDB;
import java.util.ArrayList;
import java.util.List;
import org.jooq.TransactionContext;
import org.jooq.TransactionListener;
import org.junit.jupiter.api.Test;

public class InMemoryTransactionalHelperTest {

  @Test
  public void testTransactional() {
    var db = new InMemoryDB();
    List<String> list = new ArrayList<>();
    final String data = "1";

    db.ctx().configuration().set(() -> new TransactionListener() {
      @Override
      public void beginStart(TransactionContext ctx) {

      }

      @Override
      public void beginEnd(TransactionContext ctx) {

      }

      @Override
      public void commitStart(TransactionContext ctx) {

      }

      @Override
      public void commitEnd(TransactionContext ctx) {

      }

      @Override
      public void rollbackStart(TransactionContext ctx) {
        list.remove(data);
      }

      public void rollbackEnd(TransactionContext ctx) {

      }
    });
    var helper = new InMemoryTransactionHelper(db);

    try {
      helper.runTransactional(cf -> {
        list.add(data);
        throw new RuntimeException();
      });
    } catch (Exception e) {

    }
    assertThat(list).isEmpty();
  }
}
