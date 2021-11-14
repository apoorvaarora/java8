package com.mslc.training.deadlock;

class A {
	synchronized void methodA(B b) {
		b.last();
	}

	synchronized void last() {
		System.out.println("Inside A.last()");
	}
}

class B {
	synchronized void methodB(A a) {
		a.last();
	}

	synchronized void last() {
		System.out.println(" Inside B.last()");
	}
}

public class DeadLock implements Runnable {
	A a = new A();
	B b = new B();

	// Constructor
	DeadLock() {
		Thread t = new Thread(this); // modified
		t.start();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(); // any instruction to delay
		a.methodA(b);
	}

	public void run() {
		b.methodB(a);
	}

	public static void main(String args[]) {
		new DeadLock();
	}
}
