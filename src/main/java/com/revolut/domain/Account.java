package com.revolut.domain;

import com.revolut.db.tables.records.AccountsRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "Account", description = "POJO that represents an account entity.")
public final class Account {

  @Schema(required = true, description = "unique id of the account")
  private Integer id;
  @Schema(required = true, description = "name of the account holder")
  private String name;
  @Schema(required = true, description = "currency of the account")
  private Currency currency;
  @Schema(required = true, description = "balance of the account with 2 decimal places at the end (10000 means 100.00)")
  private Long balance;

  public Account(String name, Currency currency, Long balance) {
    this.name = name;
    this.currency = currency;
    this.balance = balance;
  }

  public void removeBalance(Long amount) {
    balance -= amount;
  }

  public void addBalance(Long amount) {
    balance += amount;
  }

  public static Account fromAccountRecord(AccountsRecord record) {
    return new Account(record.getId(), record.getName(), Currency.valueOf(record.getCurrency()),
        record.getBalance());
  }
}
