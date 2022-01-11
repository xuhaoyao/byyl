package com.oyyy.test;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        SynchronousQueue<Integer> queue = new SynchronousQueue<>(true);
        new Thread(() -> {
            try {
                queue.put(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"input").start();

        try { TimeUnit.SECONDS.sleep(2); } catch (InterruptedException e) { e.printStackTrace(); }

        new Thread(() -> {
            try {
                System.out.println(queue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"input").start();
    }
}
