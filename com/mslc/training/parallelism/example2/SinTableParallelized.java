package com.mslc.training.parallelism.example2;

public class SinTableParallelized implements Runnable {

	int totalCount = 1000;

	private class SinTableRange {
		public int start, end;
	}

	private float lookupValues[];
	private Thread lookupThreads[];
	private int startLoop, endLoop, curLoop;
	private int numThreads = 8;

	public SinTableParallelized() {

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
	}

	public void run() {
		SinTableRange str;
		while ((str = getRangeToProcess()) != null) {
			processGivenRange(str.start, str.end);
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
		
		System.out.println(Runtime.getRuntime().availableProcessors());

		System.out.println("Starting Example 2 (Threaded Example)");

		SinTableParallelized st = new SinTableParallelized();
		long start = System.currentTimeMillis();

		float results[] = st.getValues();
		long end = System.currentTimeMillis();
		System.out.println("Time taken is : " + (end - start));

		System.out.println("Results: " + results[0] + ", " + results[1] + ", "
				+ results[2] + ", " + "...");
		System.out.println("Done");
	}
}
