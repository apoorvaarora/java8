package com.mslc.training.parallelism.withcyclicbarrier;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class SinTableWithCyclicBarrier implements Runnable {

	private class SinTableRange {
		public int start, end;
	}

	private int totalCount = 100;
	private float lookupValues[];
	private Thread lookupThreads[];
	private int startLoop, endLoop, curLoop;
	private int numThreads = Runtime.getRuntime().availableProcessors();
	private CyclicBarrier barrier = null;

	public SinTableWithCyclicBarrier() {
		lookupValues = new float[360 * totalCount];
		lookupThreads = new Thread[numThreads];
		startLoop = curLoop = 0;
		endLoop = (360 * totalCount);

	}

	private synchronized SinTableRange getRangeToProcess() {
		if (curLoop >= endLoop)
			return null;
		SinTableRange ret = new SinTableRange();
		ret.start = curLoop;
		curLoop += (endLoop - startLoop) / numThreads + 1;
		ret.end = (curLoop < endLoop) ? curLoop : endLoop;
		return ret;
	}

	private void processGivenRange(int start, int end) {
		for (int i = start; i < end; i += 1) {
			float sinValue = (float) Math.sin((i % 360) * Math.PI / 180.0);
			lookupValues[i] = sinValue * (float) i / 180.0f;
		}
		System.out.println("Range : " + start + " End : " + end + " Done.");
		try {
			barrier.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		SinTableRange range;
		while (true) {
			range = getRangeToProcess();
			if (range == null) {
				break;
			}
			processGivenRange(range.start, range.end);
		}
	}

	public float[] getValues() {
		for (int i = 0; i < numThreads; i++) {
			lookupThreads[i] = new Thread(this);
			lookupThreads[i].start();
		}
		for (int i = 0; i < numThreads; i++) {
			try {
				lookupThreads[i].join();
			} catch (InterruptedException iex) {
			}
		}
		return lookupValues;
	}

	public static void main(String args[]) {

		SinTableWithCyclicBarrier st = new SinTableWithCyclicBarrier();
		long start = System.currentTimeMillis();
		st.registerBarrier();

		float results[] = st.getValues();
		long end = System.currentTimeMillis();
		System.out.println("Time taken is : " + (end - start));

		System.out.println("Results: " + results[0] + ", " + results[1] + ", "
				+ results[2] + ", " + "...");
	}

	private void registerBarrier() {
		barrier = new CyclicBarrier(4, new Runnable() {

			@Override
			public void run() {
				System.out.println("************** Range Complete....");

			}

		});
	}

}
