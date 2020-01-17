package com.revolut.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jetty.util.component.LifeCycle.stop;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

public class LockTest {

  @Test
  public void shouldWaitForLockAcquisition() throws InterruptedException {
    Lock lock = new Lock();
    ExecutorService executor = Executors.newFixedThreadPool(2);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch completedLatch = new CountDownLatch(1);
    List inputs = new ArrayList<Integer>();

    executor.submit(
        () -> lock.runWithLock(1, () -> {
          inputs.add(1);
          try {
            startLatch.await();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }));

    executor.submit(
        () -> lock.runWithLock(1, () ->
        {
          inputs.add(2);
          completedLatch.countDown();
        }));
    assertThat(inputs).hasSize(1);
    startLatch.countDown();
    completedLatch.await();
    assertThat(inputs).hasSize(2);
    stop(executor);
  }

  @Test
  public void shouldWaitForLockAcquisitionBruteForce() throws InterruptedException {
    Lock lock = new Lock();
    int nThreads = 1000;
    ExecutorService executor = Executors.newFixedThreadPool(nThreads);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch completedLatch = new CountDownLatch(nThreads);
    List inputs = new ArrayList<String>();
    IntStream.range(0, nThreads).forEach(i ->
        executor.submit(
            () -> {
              try {
                startLatch.await();
                lock.runWithLock(1, () -> {
                  inputs.add(i + "");
                  inputs.remove(i + "");
                });
              } catch (InterruptedException e) {
              } finally {
                completedLatch.countDown();
              }
            }));
    startLatch.countDown();
    completedLatch.await();
    assertThat(inputs).hasSize(0);
  }

  @Test
  public void shouldNotBeDeadlocked() throws InterruptedException {
    Lock lock = new Lock();
    int nThreads = 100;
    ExecutorService executor = Executors.newFixedThreadPool(nThreads);
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch completedLatch = new CountDownLatch(nThreads);
    List inputs = new ArrayList<Integer>();
    IntStream.range(0, nThreads).forEach(i ->
        executor.submit(
            () -> {
              var firstId = i % 2 == 0 ? 1 : 2;
              var secondId = i % 2 == 1 ? 1 : 2;
              try {
                startLatch.await();
                lock.runWithLock(firstId, secondId, "tx " + i, () -> {
                  inputs.add(i);
                });
              } catch (InterruptedException e) {
              } finally {
                completedLatch.countDown();
              }
            }));
    startLatch.countDown();
    completedLatch.await();
    assertThat(inputs).hasSize(nThreads);
  }
}
