package com.jpmc.concurrency.httpserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

public class ForkAndJoinDemo {

	public static void main(String[] args) throws InterruptedException {

		ForkJoinPool fp = new ForkJoinPool(Runtime.getRuntime()
				.availableProcessors() + 1);

		// Create ForkJoinPool using the default constructor.
		ForkJoinPool pool = new ForkJoinPool();
		// Create three FolderProcessor tasks. Initialize each one with a
		// different folder path.
		FolderProcessor server1 = new FolderProcessor(
				"/working/projects/mslc/liferay-portal-6.2-ce-ga1", "log");
		FolderProcessor server2 = new FolderProcessor(
				"/working/projects/elearningstack/liferay/liferay-portal-6.2.0-ce-ga1",
				"log");
		// FolderProcessor documents = new FolderProcessor(
		// "C:\\Documents And Settings", "log");
		// Execute the three tasks in the pool using the execute() method.
		pool.execute(server1);
		pool.execute(server2);
		// pool.execute(documents);
		// Write to the console information about the status of the pool every
		// second
		// until the three tasks have finished their execution.
		do {
			System.out.printf("******************************************\n");
			System.out.printf("Main: Parallelism: %d\n", pool.getParallelism());
			System.out.printf("Main: Active Threads: %d\n",
					pool.getActiveThreadCount());
			System.out.printf("Main: Task Count: %d\n",
					pool.getQueuedTaskCount());
			System.out.printf("Main: Steal Count: %d\n", pool.getStealCount());
			System.out.printf("******************************************\n");
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while ((!server1.isDone()) || (!server2.isDone()));
		// Shut down ForkJoinPool using the shutdown() method.
		pool.shutdown();
		// Write the number of results generated by each task to the console.
		List<String> results;
		results = server1.join();
		System.out.printf("Server 1: %d files found.\n", results.size());
		results = server2.join();
		System.out.printf("Server 2: %d files found.\n", results.size());
		// results = documents.join();
		// System.out.printf("Documents: %d files found.\n", results.size());

	}
}

class FolderProcessor extends RecursiveTask<List<String>> {

	private static final long serialVersionUID = 1L;
	// This attribute will store the full path of the folder this task is going
	// to process.
	private final String path;
	// This attribute will store the name of the extension of the files this
	// task is going to look for.
	private final String extension;

	// Implement the constructor of the class to initialize its attributes
	public FolderProcessor(String path, String extension) {
		this.path = path;
		this.extension = extension;
	}

	@Override
	protected List<String> compute() {

		List<String> list = new ArrayList<String>();
		// FolderProcessor tasks to store the subtasks that are going to process
		// the subfolders stored in the folder
		List<FolderProcessor> tasks = new ArrayList<FolderProcessor>();
		// Get the content of the folder.
		File file = new File(path);
		File content[] = file.listFiles();
		// For each element in the folder, if there is a subfolder, create a new
		// FolderProcessor object
		// and execute it asynchronously using the fork() method.
		if (content != null) {
			for (int i = 0; i < content.length; i++) {
				if (content[i].isDirectory()) {

					FolderProcessor task = new FolderProcessor(
							content[i].getAbsolutePath(), extension);
					task.fork();
					tasks.add(task);
				}
				// Otherwise, compare the extension of the file with the
				// extension you are looking for using the checkFile() method
				// and, if they are equal, store the full path of the file in
				// the list of strings declared earlier.
				else {
					if (checkFile(content[i].getName())) {
						list.add(content[i].getAbsolutePath());
					}
				}
			}
		}
		// If the list of the FolderProcessor subtasks has more than 50
		// elements,
		// write a message to the console to indicate this circumstance.
		if (tasks.size() > 50) {
			System.out.printf("  > " + file.getAbsolutePath() + " -- "
					+ tasks.size());
		}
		// add to the list of files the results returned by the subtasks
		// launched by this task.
		addResultsFromTasks(list, tasks);
		// Return the list of strings
		return list;
	}

	private void addResultsFromTasks(List<String> list,
			List<FolderProcessor> tasks) {
		for (FolderProcessor item : tasks) {
			list.addAll(item.join());
		}
	}

	private boolean checkFile(String name) {
		return name.endsWith(extension);
	}

}