package com.mslc.training.parallelism.withcyclicbarrier;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierDemo {

	public static void main(String[] args) throws InterruptedException {

		final CyclicBarrier barrier = new CyclicBarrier(5, new Runnable() {

			@Override
			public void run() {

				System.out.println("Barrier Action Thread : "
						+ Thread.currentThread().getName() + " is executing");
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		});

		for (int i = 0; i < 5; i++) {
			final int count = i;

			Thread.sleep(1000);

			new Thread() {
				{
					setName("T " + count);
				}

				public void run() {

					System.out
							.println("Thread : " + getName() + " has arrived");
					if (count == 7) {
						// this.interrupt();
					}

					try {
						barrier.await();
						System.out.println("Thread : " + getName()
								+ " has passed the barrier");
					} catch (InterruptedException e) {
						System.out.println(getName()
								+ " has received an InterruptException");
					} catch (BrokenBarrierException e) {
						System.out.println(getName()
								+ " has received a BarrierBrokenException");
					}

				}

			}.start();

		}

	}

}
