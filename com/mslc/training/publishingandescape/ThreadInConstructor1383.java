package com.mslc.training.publishingandescape;

public class ThreadInConstructor1383 {

	private int id;

	public ThreadInConstructor1383() {

		new Thread() {
			@Override
			public void run() {

				// can access the state before the state is fully initialized
				// (object as escaped)
				// bad design
				System.out.println(id);
			}

		};

	}

	public static void main(String[] args) {

	}

}
