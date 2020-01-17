package com.revolut.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public final class Transaction {

  private Integer id;
  private String transactionId;
  private TransactionType operationName;
  private Integer relatedEntityId;
  private Long amount;
  private Currency currency;
  private Long timestampMs;

  public enum TransactionType {
    DEPOSIT,
    WITHDRAW,
    TRANSFER_SOURCE,
    TRANSFER_DESTINATION
  }

}
