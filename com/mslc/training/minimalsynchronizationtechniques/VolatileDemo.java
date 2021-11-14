package com.mslc.training.minimalsynchronizationtechniques;

public class VolatileDemo {

	public static void main(String[] args) throws InterruptedException {

		 ConfigurationManager cm = new ConfigurationManager();
		 new Thread(cm).start();
		 Thread.sleep(2000);
		 
		 cm.initializeConfig();

		/*Thread readThread = new Thread() {

			public void run() {
				cm.useConfiguration();

			}

		};
		
		
		Thread writeThread = new Thread() {

			public void run() {
				cm.initializeConfig();

			}

		};
		
		readThread.start();
		Thread.sleep(3000);
		
		writeThread.start(); */

		
	}

}

class ConfigurationManager implements Runnable {

	private volatile boolean initialized = false;
	
	public void justLoop() {
		while (!initialized) {
			
		}
	}

	public void useConfiguration() {

		while (!initialized) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("The thread is sleeping....");

		}

		System.out.println("Reading the configuration");
	}

	public void initializeConfig() {
		initialized = true;
	}

	public void run() {
		this.useConfiguration();
		
	}

}
