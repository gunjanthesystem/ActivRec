package org.activity.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.activity.io.ReadingFromFile;
import org.activity.io.WToFile;
import org.activity.util.RegexUtils;

public class Snippets
{

	public Snippets()
	{
		// TODO Auto-generated constructor stub
	}

	public static void main(String args[])
	{
		getActCountOverAllUsers();
	}

	public static LinkedHashMap<String, String> getCatIDNameDict(String commonPath)
	{
		LinkedHashMap<String, String> actIDNameMap = new LinkedHashMap<>();
		try
		{
			List<List<String>> dataRead = ReadingFromFile
					.readLinesIntoListOfLists(commonPath + "CatIDNameDictionary.csv", ",");
			dataRead.stream().forEachOrdered(e -> actIDNameMap.put(e.get(0), e.get(1)));

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return actIDNameMap;
	}

	/**
	 * @since 20 March 2019
	 */
	public static void getActCountOverAllUsers()
	{
		String commonPath = "/mnt/sshServers/theengine/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/SelectedMar20/gowalla1_MAR20H20M9HighOccurPNN100coll/";
		String fileToRead = commonPath + "100R/dataBaseLineOccurrenceWithScore.csv";

		LinkedHashMap<String, Double> actCountMap = new LinkedHashMap<>();

		try
		{
			List<String> dataRead = ReadingFromFile.oneColumnReaderString(fileToRead, ",", 0, false);
			for (String line : dataRead)
			{
				ArrayList<String> splittedLine = new ArrayList<>(
						Arrays.asList(RegexUtils.patternDoubleUnderScore.split(line)));
				if (splittedLine.get(0).length() == 0)
				{
					splittedLine.remove(0);// delete first empty
				}
				System.out.println(" --" + splittedLine);

				for (String cell : splittedLine)
				{
					ArrayList<String> splittedCell = new ArrayList<>(
							Arrays.asList(RegexUtils.patternColon.split(cell)));
					String key = splittedCell.get(0);
					Double val = Double.valueOf(splittedCell.get(1));

					Double prevCount = actCountMap.get(key);
					if (prevCount == null)
					{
						actCountMap.put(key, val);
					}
					else
					{
						actCountMap.put(key, prevCount + val);
					}
				}
			}

			LinkedHashMap<String, String> catIDNameDict = getCatIDNameDict(commonPath);

			String toWrite = actCountMap.entrySet().stream()
					.map(e -> e.getKey() + "," + catIDNameDict.get(e.getKey()) + "," + e.getValue())
					.collect(Collectors.joining("\n"));
			WToFile.writeToNewFile("ActID,ActName,SumOfCounts\n" + toWrite,
					commonPath + "dataBaseLineOccurrenceWithScoreAllUsersSum.csv");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
