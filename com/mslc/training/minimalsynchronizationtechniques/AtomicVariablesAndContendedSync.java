package com.mslc.training.minimalsynchronizationtechniques;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicVariablesAndContendedSync {

	public static void main(String[] args) throws InterruptedException {

		final UsingSync s = new UsingSync();
		final UsingAtomicInteger a = new UsingAtomicInteger();
		List<Thread> syncThreads = new ArrayList<Thread>();
		List<Thread> atomicThreads = new ArrayList<Thread>();

		final int numberOfIncrements = 500;

		int numberOfThreads = 100;
		for (int i = 0; i < numberOfThreads; i++) {
			syncThreads.add(new Thread() {
				@Override
				public void run() {
					for (int n = 0; n < numberOfIncrements; n++) {
						s.increment();
					}

				}

			});
		}

		for (int i = 0; i < numberOfThreads; i++) {
			atomicThreads.add(new Thread() {
				@Override
				public void run() {
					for (int n = 0; n < numberOfIncrements; n++) {
						a.increment();
					}

				}

			});
		}

		long syncStartTime = System.currentTimeMillis();
		for (int i = 0; i < numberOfThreads; i++) {
			syncThreads.get(i).start();
		}

		for (int i = 0; i < numberOfThreads; i++) {
			syncThreads.get(i).join();
		}
		long syncStopTime = System.currentTimeMillis();
		System.out.println("Time taken with Synchronization by : "
				+ numberOfThreads + " - " + (syncStopTime - syncStartTime));

		long atomicStartTime = System.currentTimeMillis();
		for (int i = 0; i < numberOfThreads; i++) {
			atomicThreads.get(i).start();
		}

		for (int i = 0; i < numberOfThreads; i++) {
			atomicThreads.get(i).join();
		}
		long atomicStopTime = System.currentTimeMillis();
		System.out.println("Time taken with AtomicVariables by : "
				+ numberOfThreads + " - " + (atomicStopTime - atomicStartTime));

	}

}

class UsingSync {

	private int counter = 0;

	public synchronized void increment() {
		counter++;
	}

}

class UsingAtomicInteger {

	private AtomicInteger counter = new AtomicInteger();

	public void increment() {
		counter.getAndIncrement();
	}

}
