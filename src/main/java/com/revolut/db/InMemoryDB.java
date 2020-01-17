package com.revolut.db;

import org.hsqldb.jdbc.JDBCPool;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class InMemoryDB {

  private static final String ACCOUNTS_DB_URL = "jdbc:hsqldb:res:/hsqldb/bank";

  private final DSLContext dslContext;

  public InMemoryDB() {
    JDBCPool pool = new JDBCPool();
    pool.setUrl(ACCOUNTS_DB_URL);
    dslContext = DSL.using(pool, SQLDialect.HSQLDB);
  }

  public DSLContext ctx() {
    return dslContext;
  }
}
