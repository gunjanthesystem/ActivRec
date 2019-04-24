package org.activity.tools;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.activity.io.ReadingFromFile;
import org.activity.io.WToFile;
import org.activity.sanityChecks.Sanity;
import org.activity.util.RegexUtils;

public class Snippets
{

	public Snippets()
	{
		// TODO Auto-generated constructor stub
	}

	public static void main(String args[])
	{
		// $$getActCountOverAllUsers(); // disabled on 22 April 2019
		// april22();
		april24();
	}

	public static void april24()
	{
		String fileToRead = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsToReadMar7Geolife4_newAKOM.csv";
		String regexForFileToRead = "Constant";

		String fileToWrite = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsToReadMar7Geolife4_newAKOM_daywiseCompat.csv";
		try
		{
			List<String> pathsToRead = Files.lines(Paths.get(fileToRead)).map(l -> l.split(",")[2])
					.collect(Collectors.toList());
			List<Path> filesToRead = new ArrayList<>();

			for (String pathToRead : pathsToRead)
			{
				filesToRead.addAll(Files.list(Paths.get(pathToRead)).filter(Files::isRegularFile)
						.filter(f -> f.toString().contains(regexForFileToRead)).limit(1).collect(Collectors.toList()));
			}

			Sanity.eq(pathsToRead.size(), filesToRead.size(),
					"Error: unexpected exactly one file to read per path but found more or less");

			System.out.println("pathsToRead = \n" + pathsToRead.stream().collect(Collectors.joining("\n")));
			System.out.println(
					"filesToRead = \n" + filesToRead.stream().map(e -> e.toString()).collect(Collectors.joining("\n")));

			List<String> matchedLines = readMatchedLineFromPaths(filesToRead,
					"ensureHasDaywiseCandsForEvalCompatibility = ", fileToWrite);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private static List<String> readMatchedLineFromPaths(List<Path> filesToRead, String stringToMatchInLine,
			String fileToWrite)
	{
		List<String> matchedLines = new ArrayList<>();
		StringBuilder sbToWrite = new StringBuilder();

		try
		{
			for (Path f : filesToRead)
			{
				List<String> selectedLines = Files.lines(f).filter(s -> s.contains(stringToMatchInLine))
						.collect(Collectors.toList());

				if (selectedLines.size() == 0)
				{
					System.out.println("Warning " + f.getFileName() + " didnt have any selected line");
				}
				selectedLines.stream().forEachOrdered(s -> sbToWrite.append(f.toString() + "\t" + s + "\n"));
				matchedLines.addAll(selectedLines);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		WToFile.writeToNewFile(sbToWrite.toString(), fileToWrite);
		return matchedLines;
	}

	/**
	 * Skip selective columns
	 * 
	 * @since April 22 1019
	 */
	public static void april22()
	{
		String commonPathToRead = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ChosenResultsRecommNext/DCU/";
		String pathToWrite = commonPathToRead + "VsBaselines/";
		WToFile.createDirectoryDeleteFormerIfExists(pathToWrite);
		// WToFile.createDirectoryDeleteFormerIfExists(pathToCreate);
		// System.out.println("here");
		// String newFileNamePhase = "VsBaselines";

		try
		{
			List<Path> filesInDirectory = Files.list(Paths.get(commonPathToRead)).filter(Files::isRegularFile)
					.collect(Collectors.toList());

			System.out.println("filesInDirectory\n"
					+ filesInDirectory.stream().map(e -> e.toString()).collect(Collectors.joining("\n")));

			// Set<Integer> indicesOfLinesToSkip = new HashSet<>(Arrays.asList(IntStream.range(7, 18)));
			// decrease by 1 to get real index
			Set<Integer> indicesOfLinesToSkip = IntStream.range(2, 7).mapToObj(i -> Integer.valueOf(i - 1))
					.collect(Collectors.toSet());

			// indicesOfLinesToSkip.add(16);
			// indicesOfLinesToSkip.add(11);
			indicesOfLinesToSkip.add(17);

			// indicesOfLinesToSkip = indicesOfLinesToSkip.stream().map(i -> i - 1).collect(Collectors.toSet());
			System.out.println("indicesOfLinesToSkip = " + indicesOfLinesToSkip);
			for (Path f : filesInDirectory)
			{
				StringBuilder sbToWrite = new StringBuilder();

				String fileNameToWrite = f.getFileName().toString();
				String fileExtension = fileNameToWrite.substring(fileNameToWrite.length() - 4,
						fileNameToWrite.length());

				fileNameToWrite = pathToWrite + fileNameToWrite.substring(0, fileNameToWrite.length() - 4)
						+ fileExtension;

				List<String> linesInFile = Files.lines(f).collect(Collectors.toList());

				for (int i = 0; i < linesInFile.size(); i++)
				{
					if (indicesOfLinesToSkip.contains(Integer.valueOf(i)) == false)
					{
						String line = linesInFile.get(i);
						line = line.replace("RecNH", "RecHH");
						sbToWrite.append(line + "\n");
					}
				}
				WToFile.writeToNewFile(sbToWrite.toString(), fileNameToWrite);
			}
		}
		catch (Exception e)
		{

		}
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
