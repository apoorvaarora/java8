package com.mslc.training.pauseresumethreads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PauseResumeDemo {

	private volatile boolean pause = false;

	private final Object lock = new Object();
	List<Thread> pausedThreads = new ArrayList<Thread>();

	public static void main(String[] args) {

		new PauseResumeDemo().demo();
	}

	public void demo() {

		ExecutorService service = Executors.newFixedThreadPool(3);

		for (int i = 0; i < 5; i++) {

			service.execute(new Runnable() {

				@Override
				public void run() {

					for (int i = 0; i < 1000; i++) {
						System.out.println("Executing Task in Thread : "
								+ Thread.currentThread().getName());
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (pause) {
							synchronized (lock) {
								try {
									System.out.println("Pausing : "
											+ Thread.currentThread().getName());
									pausedThreads.add(Thread.currentThread());
									lock.wait();
									System.out.println("Resuming : "
											+ Thread.currentThread().getName());
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}

					}

				}

			});
		}

		new Thread() {

			{
				setDaemon(true);
			}

			public void run() {

				boolean toggle = false;
				while (true) {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (toggle) {

						pause = false;
						/*
						 * for (Thread t : pausedThreads) { t.interrupt(); }
						 */
						synchronized (lock) {
							lock.notify();
						}

					} else {
						pause = true;

					}

					if (toggle) {
						toggle = false;
					} else {
						toggle = true;
					}

				}

			};

		}.start();

	}

}
