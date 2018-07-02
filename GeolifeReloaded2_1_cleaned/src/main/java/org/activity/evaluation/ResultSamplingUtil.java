package org.activity.evaluation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.activity.io.ReadingFromFile;
import org.activity.io.WToFile;

public class ResultSamplingUtil
{
	// files containing indices for (randomly sampled) selected users.
	static final String[] sampledUserIndicesSets = { "./dataToRead/RandomlySample100UsersApril24_2018.csv",
			"./dataToRead/RandomlySample100UsersApril24_2018.SetB",
			"./dataToRead/RandomlySample100UsersApril24_2018.SetC",
			"./dataToRead/RandomlySample100UsersApril24_2018.SetD",
			"./dataToRead/RandomlySample100UsersApril24_2018.SetE" };

	public static void createDirIfNotExists(String folderToWrite)
	{
		File dir = new File(folderToWrite);
		if (!dir.exists())
		{
			dir.mkdirs();
		}
	}

	public static void main(String args[])
	{
		allUserResultsTo5SetsOf100RResults();
	}

	/**
	 * 
	 */
	public static void allUserResultsTo5SetsOf100RResults()
	{
		String rootPath = "./dataWritten/";
		String folderNameToRead = "JUN28RNN13HL500Neu500EpochsAll";

		// String folderToWrite = rootPath + "B";
		String[] targetFilesToSelectSamplesFrom = { "AllPerDirectTopKAgreements_0.csv",
				"AllPerDirectTopKAgreementsL1_0.csv" };

		try
		{
			for (String targetFileName : targetFilesToSelectSamplesFrom)
			{
				String fileNameToRead = rootPath + folderNameToRead + "/" + targetFileName;

				for (String sampledIndicesSet : sampledUserIndicesSets)
				{
					String userSetPhrase = sampledIndicesSet.substring(sampledIndicesSet.length() - 4,
							sampledIndicesSet.length()); // SetA or SetB ...
					System.out.println("userSetPhrase= " + userSetPhrase);
					// ./dataWritten/JUN28RNN13HL500Neu500EpochsAllSetA/
					String folderToWrite = rootPath + folderNameToRead + userSetPhrase + "/";

					System.out.println(fileNameToRead + "  \n" + folderToWrite + targetFileName);
					if (true)
					{
						createDirIfNotExists(folderToWrite);
						sampleOnlySpecificIndicesFromFile(fileNameToRead, sampledIndicesSet,
								folderToWrite + targetFileName);
					}
				}
			}
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void fun()
	{

		List<Long> concatenatedList = new ArrayList<>();

		for (String sampledIndicesSet : sampledUserIndicesSets)
		{
			List<Long> readVals = ReadingFromFile.oneColumnReaderLong(sampledIndicesSet, ",", 0, false);
			System.out.println(
					"Read values from " + sampledIndicesSet + " are (size=" + readVals.size() + "):\n" + readVals);

			concatenatedList.addAll(readVals);
		}

		System.out.println("concatenatedList values are (size=" + concatenatedList.size() + "):\n" + concatenatedList);

		StringBuilder sb = new StringBuilder();
		concatenatedList.stream().forEachOrdered(l -> sb.append(l + "\n"));

		WToFile.writeToNewFile(sb.toString(), "./dataToRead/RandomlySample100UsersApril24_2018AllTogether.csv");
	}

	/**
	 * Select only specific indices (read from another file) from the given file and writes then to a new file.
	 * <p>
	 * NOT expecting to take care of any header
	 * <p>
	 * 
	 * @param fileWithContentsToSample
	 * @param fileWithIndicesToSample
	 * @param fileNameToWrite
	 */
	public static void sampleOnlySpecificIndicesFromFile(String fileWithContentsToSample,
			String fileWithIndicesToSample, String fileNameToWrite)
	{

		List<Long> indicesToSample = ReadingFromFile.oneColumnReaderLong(fileWithIndicesToSample, ",", 0, false);

		StringBuilder sb = new StringBuilder();
		ReadingFromFile allLinesReadingFromFile;

		List<List<String>> allLines = ReadingFromFile.readLinesIntoListOfLists(fileWithContentsToSample, ",");
		// List<Long> sampledIndices = new ArrayList<>();
		StringBuilder sbToWrite = new StringBuilder();

		for (int index = 0; index < allLines.size(); index++)
		{
			Long indexL = new Long(index);

			if (indicesToSample.contains(indexL))
			{
				sbToWrite.append(String.join(",", allLines.get(index)) + "\n");
			}
		}

		System.out.println("Read values from " + fileWithIndicesToSample + " are (size=" + indicesToSample.size()
				+ "):\n" + indicesToSample + "\n sbToWrite.length()=" + sbToWrite.length());

		WToFile.writeToNewFile(sbToWrite.toString(), fileNameToWrite);
	}

}
