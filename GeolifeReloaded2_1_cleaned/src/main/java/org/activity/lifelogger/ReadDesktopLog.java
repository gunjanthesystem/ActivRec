package org.activity.lifelogger;

import java.util.List;

import org.activity.io.ReadingFromFile;

public class ReadDesktopLog
{

	public ReadDesktopLog()
	{
		// TODO Auto-generated constructor stub
	}

	public static void main(String args[])
	{

		String fileToRead = "/home/gunjan/llogs/desktopLogsPhoenix_29Apr2017.bak";
		try
		{
			long t3 = System.currentTimeMillis();
			List<List<String>> readData2 = ReadingFromFile.readLinesIntoListOfLists(fileToRead, "\\|");
			long t4 = System.currentTimeMillis();

			long t1 = System.currentTimeMillis();
			List<List<String>> readData = ReadingFromFile.readLinesIntoListOfListsPrecompiledSplit(fileToRead, "\\|");
			long t2 = System.currentTimeMillis();

			System.out.println("readLinesIntoListOfListsPrecompiledSplit time taken = " + (t2 - t1) + "ms");
			System.out.println("readLinesIntoListOfLists time taken older = " + (t4 - t3) + "ms");

			System.out.println("time diff (precompiled - stringsplit) = " + ((t2 - t1) - (t4 - t3)) + "ms");
			System.out.println("time diff (precompiled - stringsplit) per line = "
					+ (((t2 - t1) - (t4 - t3)) * 1.0) / readData.size() + "ms");

			System.out.println("readData.size" + readData.size());
			System.out.println("readData2.size" + readData2.size());

			// StringBuilder sb1 = new StringBuilder();
			// readData.stream().limit(5).forEachOrdered(e -> sb1.append(e.toString() + "\n"));
			// System.out.println("sb1=\n" + sb1.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
