package com.mslc.training.parallelism.example1;

public class SinTable {
	private float lookupValues[] = null;

	public synchronized float[] getValues() {
		int totalCount = 100; 
		
		if (lookupValues == null) {
			lookupValues = new float[360 * totalCount];
			for (int i = 0; i < (360 * totalCount); i++) {
				float sinValue = (float) Math.sin((i % 360) * Math.PI / 180.0);
				lookupValues[i] = sinValue * (float) i / 180.0f;
			}
		}
		return lookupValues;
	}

	public static void main(String args[]) {

		System.out.println("Starting Example 1 (Control Example)");

		SinTable st = new SinTable();
		long start = System.currentTimeMillis();

		float results[] = st.getValues();
		long end = System.currentTimeMillis();
		System.out.println("Time taken is : " + (end - start));

		System.out.println("Results: " + results[0] + ", " + results[1] + ", "
				+ results[2] + ", " + "...");

		System.out.println("Done");

		System.out.println(" ----- ");

		int i = 35;
		float sinValue = (float) Math.sin((i % 360) * Math.PI / 180.0);
		System.out.println(sinValue);
		System.out.println(sinValue * ((float) i / 180.0f));

	}
}
