package com.revolut.service;

import com.revolut.domain.Exceptions.CouldNotAcquiredLockException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

//cannot be used in a distributed system
public class Lock {

  private static final long TIMEOUT_IN_SECONDS = 10;
  private static final ConcurrentHashMap<Integer, ReentrantLock> accLocks = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, ReentrantLock> txLocks = new ConcurrentHashMap<>();

  private void init(Integer... ids) {
    Arrays
        .stream(ids)
        .forEach(id -> accLocks.putIfAbsent(id, new ReentrantLock()));
  }

  private void init(String txId) {
    txLocks.putIfAbsent(txId, new ReentrantLock());
  }

  public void runWithLock(Integer id, String txId, Runnable runnable) {
    init(id);
    init(txId);
    var accLock = accLocks.get(id);
    var txLock = txLocks.get(txId);
    try {
      lockOrThrow(accLock);
      lockOrThrow(txLock);
      runnable.run();
    } finally {
      unlockIfHeldByCurrentThread(accLock);
      unlockIfHeldByCurrentThread(txLock);
    }
  }

  public void runWithLock(Integer id, Runnable runnable) {
    init(id);
    var accLock = accLocks.get(id);
    try {
      lockOrThrow(accLock);
      runnable.run();
    } finally {
      unlockIfHeldByCurrentThread(accLock);
    }
  }

  public void runWithLock(Integer firstId, Integer secondId, String txId, Runnable runnable) {
    init(firstId, secondId);
    init(txId);
    //we always need the same order to prevent deadlocks
    var smallerId = firstId <= secondId ? firstId : secondId;
    var biggerId = firstId > secondId ? firstId : secondId;
    var firstAccLock = accLocks.get(smallerId);
    var secondAccLock = accLocks.get(biggerId);
    var txLock = txLocks.get(txId);
    try {
      lockOrThrow(firstAccLock);
      lockOrThrow(secondAccLock);
      lockOrThrow(txLock);
      runnable.run();
    } finally {
      unlockIfHeldByCurrentThread(firstAccLock);
      unlockIfHeldByCurrentThread(secondAccLock);
      unlockIfHeldByCurrentThread(txLock);
    }
  }

  private void lockOrThrow(ReentrantLock lock) {
    boolean locked;
    try {
      locked = lock.tryLock(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      locked = false;
    }
    if (!locked) {
      throw new CouldNotAcquiredLockException("Cannot acquire lock!");
    }
  }

  private void unlockIfHeldByCurrentThread(ReentrantLock lock) {
    if (lock.isHeldByCurrentThread()) {
      lock.unlock();
    }
  }
}
