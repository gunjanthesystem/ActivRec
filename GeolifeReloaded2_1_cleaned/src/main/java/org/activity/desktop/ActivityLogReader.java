package org.activity.desktop;

import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.activity.io.ReadingFromFile;
import org.activity.io.WToFile;

public class ActivityLogReader
{

	public ActivityLogReader()
	{
		// TODO Auto-generated constructor stub
	}

	public static void main(String args[])
	{

		String desktopLogsFile = "/home/gunjan/llogs/desktopLogsPhoenix_29Apr2017.bak";
		Pattern patternHyphen = Pattern.compile("-");
		Pattern patternSpace = Pattern.compile(" ");
		Map<LocalDateTime, DesktopEntry> checkins = new TreeMap<>();

		// myString.split("\\s+");
		try
		{
			// List<List<String>> logData = ReadingFromFile.readLinesIntoListOfLists(desktopLogsFile,
			// Pattern.quote("||"));

			List<List<String>> logData2 = ReadingFromFile
					.nColumnReaderStringLargeFile(new FileInputStream(desktopLogsFile), "||", false, false);

			List<String> linesIgnoredWithOnlyTS = new ArrayList<>(), linesWithNoTitle = new ArrayList<>();

			for (List<String> e : logData2)
			{
				System.out.println("line read= " + e.toString());
				String timeStampString = e.get(0), titleString = "";

				if (e.size() > 2)
				{// likely multiple occurrences of || in line read
					// System.out.println("e.size()=" + e.size());
					// System.out.println("&&&& e=" + e.toString());
					// Join the part expcept first timestamp
					titleString = e.stream().skip(1).collect(Collectors.joining("||"));
					// System.out.println("t=" + titleString);
				}

				if (e.size() < 2)
				{
					// ignore lines of length 1, usually just the ts
					linesIgnoredWithOnlyTS.add(e.toString());
					continue;
				}

				// Sanity.eq(e.size(), 2, "Error: entry size is not 2 = " + e.size());
				titleString = e.get(1);
				// String splittedTS[] = patternSpace.split(timeStampString);
				List<String> splittedTitle = new ArrayList<>();

				int lastIndexOfHyphenInFullTitle = titleString.lastIndexOf("-");

				if (lastIndexOfHyphenInFullTitle == -1)
				{// no hyphen, likely only contains application name
					linesWithNoTitle.add(e.toString());
					splittedTitle.add("");
					splittedTitle.add(titleString);
				}
				else
				{
					splittedTitle.add(titleString.substring(0, lastIndexOfHyphenInFullTitle).trim());
					splittedTitle
							.add(titleString.substring(lastIndexOfHyphenInFullTitle + 1, titleString.length()).trim());
					// System.out.println("--> splittedTitle=" + splittedTitle.toString());
				}
				// String splittedTitle[] = patternHyphen.split(e.get(1));

				// System.out.println("timeStampString=" + timeStampString);
				LocalDateTime ts = splittedTSToTS(timeStampString);
				checkins.put(ts, new DesktopEntry(ts, splittedTitle.get(0), splittedTitle.get(1)));
				// System.out.println("splittedTitle=" + (splittedTitle));

			}

			// StringBuilder sb = new StringBuilder();
			// logData2.stream().forEachOrdered(e -> sb.append(e + "\n"));
			// System.out.println(sb.toString());
			WToFile.writeToNewFile(String.join("\n", linesIgnoredWithOnlyTS), "linesIgnoredWithOnlyTS.csv");
			WToFile.writeToNewFile(String.join("\n", linesWithNoTitle), "linesWithNoTitle.csv");
			WToFile.writeMapToNewFile(checkins, "TS,Entry", ",", "checkins.csv");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static LocalDateTime splittedTSToTS(String tsString)
	{// Sat Apr 29 23:07:37 IST 2017
		tsString = tsString.replaceAll("  ", " ");
		// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss z yyyy");
		LocalDateTime dateTime = LocalDateTime.parse(tsString, formatter);
		// System.out.println("--- tsString=" + tsString + " dateTime=" + dateTime);
		return dateTime;
	}

}
