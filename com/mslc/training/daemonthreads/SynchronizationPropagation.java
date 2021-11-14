package com.mslc.training.daemonthreads;

public class SynchronizationPropagation {

	/**
	 * Scenario one : Structure2.set2 is not synchronized What to expect: There
	 * can be race condition leaving the data corrupted by 2 threads
	 * simultaneously accessing it.
	 * 
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {

		final MyDataStructure ds = new MyDataStructure();
		final Structure2 s2 = new Structure2();

		new Thread() {
			@Override
			public void run() {

				ds.set(s2);
			}

		}.start();

		Thread.sleep(100);
		new Thread() {
			public void run() {

				synchronized (ds) {

					s2.set2();

				}

			}

		}.start();

	}
}

class MyDataStructure {

	public synchronized void set(Structure2 s2) {
		System.out.println("set is executed by : "
				+ Thread.currentThread().getName());

		s2.set2();

		System.out.println(Thread.currentThread().getName()
				+ " done with set including set2");
	}
}

class Structure2 {

	public synchronized void set2() {

		System.out.println(Thread.currentThread().getName() + " Is executing");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
