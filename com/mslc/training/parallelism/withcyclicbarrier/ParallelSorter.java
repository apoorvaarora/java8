package com.mslc.training.parallelism.withcyclicbarrier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.RandomAccess;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public class ParallelSorter<E extends Comparable<E>> {
	private final int noOfThreads = Runtime.getRuntime().availableProcessors();
	private final int noOfSamplesPerThread = 16;
	private final AtomicLong randSeed = new AtomicLong(System.nanoTime());
	private volatile int stageNo = 0;
	private final int dataSize;
	private final List<E> data;
	private final List<E> splitPoints = new ArrayList<E>(noOfSamplesPerThread
			* noOfThreads);
	private final List<List<E>> bucketsToSort;
	private final ReadWriteLock dataLock;

	private final CyclicBarrier barrier = new CyclicBarrier(noOfThreads + 1,
			new Runnable() {
				public void run() {
					sortStageComplete();
				}
			});

	public ParallelSorter(List data, ReadWriteLock dataLock) {
		if (!(data instanceof RandomAccess))
			throw new IllegalArgumentException("List must be random access");
		this.data = data;
		this.dataLock = dataLock;
		this.dataSize = data.size();
		List<List<E>> tempList = new ArrayList<List<E>>(noOfThreads);
		for (int i = 0; i < noOfThreads; i++) {
			tempList.add(new ArrayList<E>(dataSize / noOfThreads));
		}
		bucketsToSort = Collections.unmodifiableList(tempList);
	}

	private class SorterThread extends Thread {
		private final int threadNo;
		private volatile Throwable error;

		SorterThread(int no) {
			this.threadNo = no;
		}

		public void run() {
			try {
				double div = (double) dataSize / noOfThreads;
				int startPos = (int) (div * threadNo), endPos = (int) (div * (threadNo + 1));

				gatherSplitPointSample(data, startPos, endPos);
				barrier.await();
				// System.out.println(Thread.currentThread().getName()
				// + " Arrived after gatheringSplitPointSample...");

				assignItemsToBuckets(data, threadNo, startPos, endPos);
				barrier.await();
				// System.out.println(Thread.currentThread().getName()
				// + " Arrived after putting items in Buckets...");

				sortMyBucket();
				barrier.await();
				// System.out.println(Thread.currentThread().getName()
				// + " Arrived after sorting the Buckets...");

			} catch (InterruptedException e) {
			} catch (BrokenBarrierException e) {
			} catch (Throwable t) {
				this.error = t;
				Thread.currentThread().interrupt();
				try {
					barrier.await();
				} catch (Exception e) {
				}
			}
		}

		private void sortMyBucket() {
			List<E> l = bucketsToSort.get(threadNo);
			synchronized (l) {
				Collections.sort(l);
			}
		}
	}

	private void sortStageComplete() {
		try {
			switch (stageNo) {
			case 0:
				amalgamateSplitPointData();
				break;
			case 1:
				clearData();
				break;
			case 2:
				combineBuckets();
				break;
			default:
				throw new RuntimeException("Don't expect to be "
						+ " called at stage " + stageNo);
			}
			stageNo++;
		} catch (RuntimeException rte) {
			completionStageError = rte;
			throw rte;
		}
	}

	private volatile RuntimeException completionStageError;

	private void gatherSplitPointSample(List<E> data, int startPos, int endPos) {
		Random rand = new Random(randSeed.getAndAdd(17));
		List<E> sample = new ArrayList<E>(noOfSamplesPerThread);
		Lock l = dataLock.readLock();
		l.lock();
		try {
			for (int i = 0; i < noOfSamplesPerThread; i++) {
				int n = rand.nextInt(endPos - startPos) + startPos;
				sample.add(data.get(n));
			}
		} finally {
			l.unlock();
		}
		synchronized (splitPoints) {
			splitPoints.addAll(sample);
		}
	}

	private void amalgamateSplitPointData() {
		synchronized (splitPoints) {
			List<E> spl = new ArrayList<E>(splitPoints);
			Collections.sort(spl);
			splitPoints.clear();
			for (int i = 1; i < noOfThreads; i++) {
				splitPoints.add(spl.get(i * noOfSamplesPerThread));
			}
			System.out.println("The Split points : " + splitPoints);
		}
	}

	private void assignItemsToBuckets(List<E> data, int threadNo, int startPos,
			int endPos) {
		List<E> spl;
		synchronized (splitPoints) {
			spl = new ArrayList<E>(splitPoints);
		}
		List<List<E>> bucketData = new ArrayList<List<E>>(noOfThreads);
		for (int i = 0; i < noOfThreads; i++) {
			bucketData.add(new ArrayList<E>(dataSize
					/ (noOfThreads * noOfThreads)));
		}

		Lock lck = dataLock.readLock();
		lck.lock();
		try {
			for (int i = startPos; i < endPos; i++) {
				E item = data.get(i);
				int bucket = Collections.binarySearch(spl, item);
				if (bucket < 0)
					bucket = (-bucket) - 1;
				if (bucket >= noOfThreads)
					bucket = noOfThreads - 1;
				bucketData.get(bucket).add(item);
			}
		} finally {
			lck.unlock();
		}
		for (int i = 0; i < noOfThreads; i++) {
			List<E> l = bucketsToSort.get(i);
			synchronized (l) {
				l.addAll(bucketData.get(i));
			}
		}
	}

	private void clearData() {
		Lock lck = dataLock.writeLock();
		lck.lock();
		try {
			data.clear();
		} finally {
			lck.unlock();
		}
	}

	private void combineBuckets() {
		Lock lck = dataLock.writeLock();
		lck.lock();
		try {
			for (int i = 0; i < noOfThreads; i++) {
				List<E> l = bucketsToSort.get(i);
				synchronized (l) {
					data.addAll(l);
				}
			}
		} finally {
			lck.unlock();
		}
	}

	public void sort() throws InterruptedException {
		// See if it's not worth doing a parallel sort
		Lock l = dataLock.writeLock();
		l.lockInterruptibly();
		try {
			if (data.size() < noOfSamplesPerThread * 4 * noOfThreads) {
				Collections.sort(data);
				return;
			}
		} finally {
			l.unlock();
		}

		// Start sorter threads going
		List<SorterThread> threads = new ArrayList<SorterThread>(noOfThreads);
		for (int i = 0; i < noOfThreads; i++) {
			SorterThread thr = new SorterThread(i);
			threads.add(thr);
			thr.start();
		}

		// Wait for sorter threads
		try {
			barrier.await();
			barrier.await();
			barrier.await();
		} catch (BrokenBarrierException bb) {
			// Find the error that caused the broken barrier
			for (int i = 0; i < noOfThreads; i++) {
				SorterThread thr = threads.get(i);
				Throwable t = thr.error;
				if (t != null)
					throw new RuntimeException("Error during sort", t);
			}
			if (completionStageError != null)
				throw completionStageError;
			else
				throw new RuntimeException("Misc error during sort", bb);
		}
	}

}
