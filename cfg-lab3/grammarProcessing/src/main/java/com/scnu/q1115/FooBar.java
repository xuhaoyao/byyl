package com.scnu.q1115;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class FooBar {

    public static void main(String[] args) throws InterruptedException {
        System.out.println((char)48);
//        FooBar fooBar = new FooBar(5);
//        new Thread(() -> {
//            try {
//                fooBar.fooPrint(() -> System.out.print("foo"));
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        },"input").start();
//
//        new Thread(() -> {
//            try {
//                fooBar.barPrint(() -> System.out.println("bar"));
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        },"input").start();
    }

    private int n;
    private BlockingQueue<Integer> foo = new LinkedBlockingQueue<>(1);
    private BlockingQueue<Integer> bar = new LinkedBlockingQueue<>(1);

    public FooBar(int n) {
        this.n = n;
    }

    public void fooPrint(Runnable printFoo) throws InterruptedException {
        
        for (int i = 0; i < n; i++) {
            foo.put(i);
        	// printFoo.run() outputs "foo". Do not change or remove this line.
        	printFoo.run();
            bar.put(i);
        }
    }

    public void barPrint(Runnable printBar) throws InterruptedException {
        
        for (int i = 0; i < n; i++) {
            bar.take();
            // printBar.run() outputs "bar". Do not change or remove this line.
        	printBar.run();
            foo.take();
        }
    }
}