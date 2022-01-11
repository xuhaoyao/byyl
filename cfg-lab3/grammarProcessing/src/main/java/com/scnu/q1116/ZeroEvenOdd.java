package com.scnu.q1116;

class ZeroEvenOdd {

    public static void main(String[] args) {
        ZeroEvenOdd zeo = new ZeroEvenOdd(5);
        IntConsumer ic = new IntConsumer();
        new Thread(() -> {
            try {
                zeo.zero(ic);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"input").start();

        new Thread(() -> {
            try {
                zeo.even(ic);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"input").start();

        new Thread(() -> {
            try {
                zeo.odd(ic);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"input").start();
    }

    private int n,x;
    private final Object mutex = new Object();
    private int flag,cnt;
    private boolean even;
    
    public ZeroEvenOdd(int n) {
        this.n = n;
    }

    // printNumber.accept(x) outputs "x", where x is an integer.
    public void zero(IntConsumer printNumber) throws InterruptedException {
        while(cnt < 2 * n){
            synchronized(mutex){
                while(flag != 0){
                    mutex.wait();
                }
                if(cnt < 2 * n)
                    printNumber.accept(0);
                else{
                    mutex.notifyAll();
                }
                flag = even ? 2 : 1;
                even = !even;
                ++cnt;
                mutex.notifyAll();
            }
        }
    }

    public void even(IntConsumer printNumber) throws InterruptedException {
        while(cnt < 2 * n){
            synchronized(mutex){
                while(flag != 2){
                    mutex.wait();
                }
                if(cnt < 2 * n)
                    printNumber.accept(++x);
                else{
                    mutex.notifyAll();
                }
                flag = 0;
                ++cnt;
                mutex.notifyAll();
            }
        }
    }

    public void odd(IntConsumer printNumber) throws InterruptedException {
        while(cnt < 2 * n){
            synchronized(mutex){
                while(flag != 1){
                    mutex.wait();
                }
                if(cnt < 2 * n)
                    printNumber.accept(++x);
                else{
                    mutex.notifyAll();
                }
                flag = 0;
                ++cnt;
                mutex.notifyAll();
            }
        }
    }
}