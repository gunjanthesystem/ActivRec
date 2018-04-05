package org.activity.desktop;

import java.io.FileInputStream;
import java.util.List;

import org.activity.io.ReadingFromFile;

public class ActivityLogReader
{

	public ActivityLogReader()
	{
		// TODO Auto-generated constructor stub
	}

	public static void main(String args[])
	{

		String desktopLogsFile = "/home/gunjan/llogs/desktopLogsPhoenix_29Apr2017.bak";

		try
		{
			// List<List<String>> logData = ReadingFromFile.readLinesIntoListOfLists(desktopLogsFile,
			// Pattern.quote("||"));

			List<List<String>> logData2 = ReadingFromFile
					.nColumnReaderStringLargeFile(new FileInputStream(desktopLogsFile), "||", false, false);

			StringBuilder sb = new StringBuilder();
			logData2.stream().forEachOrdered(e -> sb.append(e + "\n"));
			System.out.println(sb.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
