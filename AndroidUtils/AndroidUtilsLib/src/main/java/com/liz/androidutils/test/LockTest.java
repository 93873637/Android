package com.liz.androidutils.test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockTest {
    public static final int MAX_COUNT = 10;
    public static final long THREAD_INLOCK_SLEEP = 1000L;
    public static final long THREAD_OUTLOCK_SLEEP = 100L;
    public static final long THREAD_COMSUMER_DELAY = 10L;

    public static class Producer implements Runnable {
        private Lock lock;

        public Producer(Lock lock) {
            this.lock = lock;
        }

        @Override
        public void run() {
            System.out.print("PRODUCER: running...\n");

            int count = 0;
            while (count < MAX_COUNT) {
                try {
                    System.out.print("PRODUCER: try lock...\n");
                    lock.lock();
                    count ++;
                    System.out.print("PRODUCER: in lock, count = " + count + "\n");
                    Thread.sleep(THREAD_INLOCK_SLEEP);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    lock.unlock();
                    System.out.print("PRODUCER: unlock\n");
                    try {
                        Thread.sleep(THREAD_OUTLOCK_SLEEP);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static class Consumer implements Runnable {
        private Lock lock;

        public Consumer(Lock lock) {
            this.lock = lock;
        }

        @Override
        public void run() {
            System.out.print("CONSUMER: running...\n");

            int count = 0;
            while (count < MAX_COUNT) {
                try {
                    System.out.print("CONSUMER: try lock...\n");
                    lock.lock();
                    count ++;
                    System.out.print("CONSUMER: in lock, count = " + count + "\n");
                    Thread.sleep(THREAD_INLOCK_SLEEP);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    lock.unlock();
                    System.out.print("CONSUMER: unlock\n");
                    try {
                        Thread.sleep(THREAD_OUTLOCK_SLEEP);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        Lock lock = new ReentrantLock();

        Producer producer = new Producer(lock);
        Consumer consumer = new Consumer(lock);

        new Thread(producer).start();

        //ensure producer start first
        try {
            Thread.sleep(THREAD_COMSUMER_DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new Thread(consumer).start();
    }
}
