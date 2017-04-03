package org.activity.util;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.activity.objects.Pair;
import org.activity.stats.StatsUtils;

public class PerformanceAnalytics
{

	public static void main(String[] args)
	{
		System.out.println(getHeapInformation());

	}

	/**
	 * Time (in nano seconds) a function that accepts one argument and produces a result
	 * 
	 * @param fun
	 * @param objToApply
	 * @return Pair{returned value, time taken in nano secs}
	 */
	public static <R, T> Pair<R, Long> timeThisFunctionInns(Function<T, R> fun, T objToApply, String label)
	{
		System.out.println("~~~~~~~~~\nTiming function: " + label);

		long t1 = System.nanoTime();
		R res = fun.apply(objToApply);
		long timeDiff = System.nanoTime() - t1;
		// StackTraceElement[] stackVals = Thread.currentThread().getStackTrace();

		// for (StackTraceElement stackElement : stackVals)
		// {
		// System.out.println("stackElement= " + stackElement);
		// System.out.println("stackElement.getMethodName()= " + stackElement.getMethodName());
		// }
		// System.out.println("time taken by function: " + Thread.currentThread().getStackTrace()[1].getMethodName()
		// + " = " + timeDiff + " ns");
		System.out.println("time taken: " + timeDiff + " ns\n~~~~~~~~~");
		return new Pair<R, Long>(res, timeDiff);
	}

	public static void timeThisFunction(Runnable fun, String label)
	{
		System.out.println("~~~~~~~~~\nTiming function: " + label);

		long t1 = System.nanoTime();
		fun.run();
		long timeDiff = System.nanoTime() - t1;
		// StackTraceElement[] stackVals = Thread.currentThread().getStackTrace();

		// for (StackTraceElement stackElement : stackVals)
		// {
		// System.out.println("stackElement= " + stackElement);
		// System.out.println("stackElement.getMethodName()= " + stackElement.getMethodName());
		// }
		// System.out.println("time taken by function: " + Thread.currentThread().getStackTrace()[1].getMethodName()
		// + " = " + timeDiff + " ns");
		System.out.println("time taken: " + timeDiff + " ns\n~~~~~~~~~");
	}

	/**
	 * Time (in nano seconds) a function that accepts one argument and produces a result
	 * 
	 * @param fun
	 * @param objToApply
	 * @return Pair{returned value, time taken in nano secs}
	 */
	public static <R, T, U> Pair<R, Long> timeThisFunctionInns(BiFunction<T, U, R> fun, T objToApply1, U objToApply2,
			String label)
	{
		System.out.println("~~~~~~~~~\nTiming function: " + label);
		long t1 = System.nanoTime();
		R res = fun.apply(objToApply1, objToApply2);
		long timeDiff = System.nanoTime() - t1;
		// StackTraceElement[] stackVals = Thread.currentThread().getStackTrace();

		// for (StackTraceElement stackElement : stackVals)
		// {
		// System.out.println("stackElement= " + stackElement);
		// System.out.println("stackElement.getMethodName()= " + stackElement.getMethodName());
		// }
		// System.out.println("time taken by function: " + Thread.currentThread().getStackTrace()[1].getMethodName()
		// + " = " + timeDiff + " ns");
		System.out.println("time taken: " + timeDiff + " ns\n~~~~~~~~~");
		return new Pair<R, Long>(res, timeDiff);
	}

	/**
	 * Time (in nano seconds) a function that accepts one argument and produces a result
	 * 
	 * @param fun
	 * @param objToApply
	 * @return Pair{returned value, time taken in nano secs}
	 */
	public static <R, T> Pair<R, Long> timeThisFunctionInns(Function<T[], R> fun, String label, T... objsToApply)
	{
		System.out.println("~~~~~~~~~\nTiming function: " + label);

		long t1 = System.nanoTime();
		R res = fun.apply(objsToApply);
		long timeDiff = System.nanoTime() - t1;
		StackTraceElement[] stackVals = Thread.currentThread().getStackTrace();

		// for (StackTraceElement stackElement : stackVals)
		// {
		// System.out.println("stackElement= " + stackElement);
		// System.out.println("stackElement.getMethodName()= " + stackElement.getMethodName());
		// }
		// System.out.println("time taken by function: " + Thread.currentThread().getStackTrace()[1].getMethodName()
		// + " = " + timeDiff + " ns");
		System.out.println("time taken: " + timeDiff + " ns\n~~~~~~~~~");
		return new Pair<R, Long>(res, timeDiff);
	}

	/**
	 * 
	 * @return
	 */
	public static String getHeapInformation()
	{
		StringBuilder s = new StringBuilder();

		// Get current size of heap in mega bytes
		double heapSize = (Runtime.getRuntime().totalMemory()) / (1024 * 1024);
		s.append("--------\n" + "Current heap size = " + heapSize + " MB");

		// Get maximum size of heap in mega bytes. The heap cannot grow beyond this size.// Any attempt will result in
		// an OutOfMemoryException.
		double heapMaxSize = Runtime.getRuntime().maxMemory() / (1024 * 1024);
		s.append("\nMaximum size of heap = " + heapMaxSize + " MB");

		// Get amount of free memory within the heap in mega bytes. This size will increase // after garbage collection
		// and decrease as new objects are created.
		double heapFreeSize = Runtime.getRuntime().freeMemory() / (1024 * 1024);
		s.append("\nFree memory within the heap = " + heapFreeSize + " MB");

		double usedMem = heapSize - heapFreeSize;
		s.append("\nUsed memory within the heap = " + usedMem + " MB");
		s.append("\n--------\n");
		return s.toString();
	}

	/**
	 * 
	 * @return
	 */
	public static String getCurrentHeap()
	{
		// Get current size of heap in mega bytes
		return ("--------" + "Current heap size = " + (Runtime.getRuntime().totalMemory()) / (1024 * 1024) + " MB\n");
	}

	/**
	 * Not sure if correct
	 * 
	 * @return
	 */
	public static String getHeapPercentageFree()
	{
		StringBuilder s = new StringBuilder();

		// Get current size of heap in mega bytes
		double heapSize = (Runtime.getRuntime().totalMemory()) / (1024 * 1024);

		// Get maximum size of heap in mega bytes. The heap cannot grow beyond this size.// Any attempt will result in
		// an OutOfMemoryException.
		double heapMaxSize = Runtime.getRuntime().maxMemory() / (1024 * 1024);

		// Get amount of free memory within the heap in mega bytes. This size will increase // after garbage collection
		// and decrease as new objects are created.
		double heapFreeSize = Runtime.getRuntime().freeMemory() / (1024 * 1024);

		double percentageFreeOfCurrent = StatsUtils.round((heapFreeSize / heapSize) * 100, 2);
		double percentageFreeOfMax = StatsUtils.round((heapFreeSize / heapMaxSize) * 100, 2);

		double usedMem = heapSize - heapFreeSize;
		s.append("--% free(current heap) = " + percentageFreeOfCurrent);
		s.append("\t% free(in max heap) = " + percentageFreeOfMax);
		s.append("--");
		return s.toString();
	}

}
