package com.revolut.domain;

public class Exceptions {

  public static class CurrencyMismatchException extends RuntimeException {

    public CurrencyMismatchException(String msg) {
      super(msg);
    }
  }

  public static class NotEnoughBalanceException extends RuntimeException {

    public NotEnoughBalanceException(String msg) {
      super(msg);
    }
  }

  public static class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String msg) {
      super(msg);
    }
  }

  public static class InvalidTransactionId extends RuntimeException {

    public InvalidTransactionId(String msg) {
      super(msg);
    }
  }

  public static class SomethingWentWrongException extends RuntimeException {

    public SomethingWentWrongException(Exception e) {
      super(e);
    }

  }

  public static class CouldNotAcquiredLockException extends RuntimeException {

    public CouldNotAcquiredLockException(String msg) {
      super(msg);
    }
  }

}
