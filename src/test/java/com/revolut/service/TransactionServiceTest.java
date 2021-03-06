package com.revolut.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.revolut.repo.TransactionRepo;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

public class TransactionServiceTest {


  @Test
  public void transactionIdShouldBeUnique() throws InterruptedException {
    var repo = mock(TransactionRepo.class);
    var service = new TransactionService(repo);
    int nThreads = 1000;
    ExecutorService executor = Executors.newFixedThreadPool(nThreads);
    //trick for ConcurrentHashSet
    var set = ConcurrentHashMap.newKeySet();
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch finishLatch = new CountDownLatch(nThreads);
    IntStream.range(0, nThreads).forEach(i ->
        executor.submit(() -> {
          try {
            startLatch.await();
            set.add(service.generateTxId());
          } catch (InterruptedException e) {
          } finally {
            finishLatch.countDown();
          }
        }));
    startLatch.countDown();
    finishLatch.await();
    assertThat(set).hasSize(nThreads);
  }

}
