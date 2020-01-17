package com.revolut.repo;

import com.revolut.domain.Account;
import java.util.List;
import org.jooq.Configuration;

public interface AccountRepo {

  Account get(Integer id);

  Account insert(Account acc);

  Account insertTransactional(Account acc, Configuration configuration);

  List<Account> getAll();

  void update(Account acc);

  void updateTransactional(Account acc, Configuration configuration);

  void delete(Integer id);

  void deleteTransactional(Integer id, Configuration configuration);
}
