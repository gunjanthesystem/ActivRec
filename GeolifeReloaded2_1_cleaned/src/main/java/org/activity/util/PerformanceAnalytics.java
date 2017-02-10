package org.activity.util;

public class PerformanceAnalytics
{

	public PerformanceAnalytics()
	{
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args)
	{
		System.out.println(getHeapInformation());

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
		s.append("\n--------\n");
		return s.toString();
	}

}
