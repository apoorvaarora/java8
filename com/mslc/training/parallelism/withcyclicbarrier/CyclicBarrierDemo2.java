package com.mslc.training.parallelism.withcyclicbarrier;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class CyclicBarrierDemo2 {

	public static void main(String[] args) {
		
		int threads = 3;

		final CyclicBarrier barrier = new CyclicBarrier(threads, new Runnable() {

			@Override
			public void run() {

				System.out.println("Barrier Action...");

			}

		});
		
		for (int i = 0; i < threads; i++) {
			new Thread() {
				public void run() {
					
					System.out.println("Stage 1");
					try {
						barrier.await();
					} catch (InterruptedException | BrokenBarrierException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					

					System.out.println("Stage 2");
					try {
						barrier.await();
					} catch (InterruptedException | BrokenBarrierException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					

					System.out.println("Stage 3");
					try {
						barrier.await();
					} catch (InterruptedException | BrokenBarrierException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					
				};
				
				
			}.start();
		}
	
		
		

	}

}
