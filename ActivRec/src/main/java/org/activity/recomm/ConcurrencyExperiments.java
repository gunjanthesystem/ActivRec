package org.activity.recomm;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * To improve and test understanding and capability of using concurrency and parallisation
 * 
 * @author gunjan
 * @since 18 Feb 2018
 */
public class ConcurrencyExperiments
{

	public ConcurrencyExperiments()
	{
	}

	public static void main(String args[])
	{
		// concurrentThreads();
		executors1();
	}

	public static void executors1()
	{
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.submit(() ->
			{
				String threadName = Thread.currentThread().getName();
				System.out.println("Hello " + threadName);
			});
		executor.shutdown();
	}

	/**
	 * ref:http://winterbe.com/posts/2015/04/07/java8-concurrency-tutorial-thread-executor-examples/
	 */
	public static void concurrentThreads()
	{

		Runnable task = () ->
			{
				String threadName = Thread.currentThread().getName();
				System.out.println("Hello " + threadName);
			};

		task.run();

		Thread thread = new Thread(task);
		thread.start();

		System.out.println("Done!");
	}
}
