package com.revolut.repo.impl;


import static com.revolut.db.tables.Accounts.ACCOUNTS;

import com.revolut.db.InMemoryDB;
import com.revolut.db.tables.records.AccountsRecord;
import com.revolut.domain.Account;
import com.revolut.repo.AccountRepo;
import java.util.List;
import org.jooq.Configuration;
import org.jooq.impl.DSL;


public class InMemoryAccountRepo implements AccountRepo {

  private final InMemoryDB db;

  public InMemoryAccountRepo(InMemoryDB db) {
    this.db = db;
  }

  @Override
  public Account get(Integer id) {
    return db.ctx()
        .selectFrom(ACCOUNTS)
        .where(ACCOUNTS.ID.eq(id))
        .fetchOneInto(Account.class);
  }

  @Override
  public List<Account> getAll() {
    return db.ctx()
        .selectFrom(ACCOUNTS)
        .fetchInto(Account.class);
  }

  @Override
  public Account insert(Account acc) {
    AccountsRecord record = db.ctx()
        .insertInto(ACCOUNTS, ACCOUNTS.BALANCE, ACCOUNTS.CURRENCY, ACCOUNTS.NAME)
        .values(acc.getBalance(), acc.getCurrency().toString(), acc.getName())
        .returning(ACCOUNTS.asterisk())
        .fetchOne();

    return Account.fromAccountRecord(record);
  }

  @Override
  public Account insertTransactional(Account acc, Configuration configuration) {
    var record = DSL.using(configuration)
        .insertInto(ACCOUNTS, ACCOUNTS.BALANCE, ACCOUNTS.CURRENCY, ACCOUNTS.NAME)
        .values(acc.getBalance(), acc.getCurrency().toString(), acc.getName())
        .returning(ACCOUNTS.asterisk())
        .fetchOne();

    return Account.fromAccountRecord(record);
  }

  @Override
  public void update(Account acc) {
    db.ctx()
        .update(ACCOUNTS)
        .set(ACCOUNTS.BALANCE, acc.getBalance())
        .set(ACCOUNTS.NAME, acc.getName())
        .set(ACCOUNTS.CURRENCY, acc.getCurrency().toString())
        .where(ACCOUNTS.ID.eq(acc.getId()))
        .execute();
  }

  @Override
  public void updateTransactional(Account acc, Configuration configuration) {
    DSL.using(configuration)
        .update(ACCOUNTS)
        .set(ACCOUNTS.BALANCE, acc.getBalance())
        .set(ACCOUNTS.NAME, acc.getName())
        .set(ACCOUNTS.CURRENCY, acc.getCurrency().toString())
        .where(ACCOUNTS.ID.eq(acc.getId()))
        .execute();
  }

  @Override
  public void delete(Integer id) {
    db.ctx()
        .deleteFrom(ACCOUNTS)
        .where(ACCOUNTS.ID.eq(id))
        .execute();
  }

  @Override
  public void deleteTransactional(Integer id, Configuration configuration) {
    DSL.using(configuration)
        .deleteFrom(ACCOUNTS)
        .where(ACCOUNTS.ID.eq(id))
        .execute();
  }
}
