package org.activity.generator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.activity.io.WritingToFile;
import org.activity.objects.DataEntry;
import org.activity.objects.Pair;
import org.activity.objects.TrackListenEntry;
import org.activity.util.Constant;
import org.activity.util.UtilityBelt;

public abstract class DatabaseCreator
{
	// ******************PARAMETERS TO SET*****************************//
	static String commonPath;

	static String rawPathToRead;// =
	// "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Research/datasets/last.fm
	// dataset/lastfm-dataset/lastfm-dataset-1K/";
	static String nameForMapToBeSerialised;// = "mapForAllDataMergedPlusDurationLastFMApril28.map";

	static int continuityThresholdInSeconds;// = 5 * 60; // changed from 30 min in DCU dataset...., if two timestamps
											// are separated by less than equal to this value

	// have same mode name,
	// then they are assumed to be continuos
	static int assumeContinuesBeforeNextInSecs;// = 2 * 60; // changed from 30 min in DCU dataset we assume that
	// if two activities have a start time gap of more than 'assumeContinuesBeforeNextInSecs' seconds ,
	// then the first activity continues for 'assumeContinuesBeforeNextInSecs' seconds before the next activity starts.
	static int thresholdForMergingNotAvailables;// = 5 * 60;
	static int thresholdForMergingSandwiches;// = 10 * 60;

	static int timeDurationForLastSingletonTrajectoryEntry;// = 2 * 60;

	// ******************PARAMETERS TO SET END*****************************//
	// //////////////////
	/**
	 * Writes all the Data Entries for each user to a file and all users together to other file.
	 * 
	 * @param data
	 * @param filenameEndPhrase
	 * @param headers
	 * @param printHeaders
	 */
	public static long writeDataToFile2WithHeaders(LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> data,
			String filenameEndPhrase, String headers, boolean printHeaders, String delimiter)
	{
		long numOfLinesOfData = 0;
		try
		{
			BufferedWriter bwAllUsers = WritingToFile
					.getBufferedWriterForNewFile(Constant.getCommonPath() + "AllUsers" + filenameEndPhrase + ".csv");

			if (printHeaders)
			{
				bwAllUsers.write(headers + "\n");
				// System.out.println("printing header :" + headers);
			}

			for (Map.Entry<String, TreeMap<Timestamp, DataEntry>> entryForUser : data.entrySet())
			{
				try
				{
					String userName = entryForUser.getKey();
					// System.out.println("\nUser =" + entryForUser.getKey());
					BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(
							Constant.getCommonPath() + userName + filenameEndPhrase + ".csv");

					TreeMap<Timestamp, String> mapForEachUser = new TreeMap<Timestamp, String>();

					if (printHeaders)
					{
						bw.write(headers + "\n");
						// System.out.println("printing header :" + headers);
					}

					for (Map.Entry<Timestamp, DataEntry> entry : entryForUser.getValue().entrySet())
					{
						numOfLinesOfData++;
						// $$System.out.println(entry.getKey()+","+entry.getValue());
						bw.write(entry.getValue().toStringWithoutHeadersWithTrajID(delimiter) + "\n");
						bwAllUsers.write(entry.getValue().toStringWithoutHeadersWithTrajID(delimiter) + "\n");
						// "StartTimestamp,EndTimestamp,Mode,Latitude,Longitude,Altitude,DifferenceWithNextInSeconds,DurationInSeconds,BreakOverDaysCount"
					}

					bw.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			bwAllUsers.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return numOfLinesOfData;
	}

	/**
	 * do not write "Unknown"
	 * 
	 * @param data
	 * @param filenameEndPhrase
	 * @param headers
	 * @param printHeaders
	 * @param delimiter
	 * @return
	 */
	public static long writeDataToFile2WithHeadersSlimmer(LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> data,
			String filenameEndPhrase, String headers, boolean printHeaders, String delimiter)
	{
		long numOfLinesOfData = 0;
		try
		{
			BufferedWriter bwAllUsers = WritingToFile
					.getBufferedWriterForNewFile(Constant.getCommonPath() + "AllUsers" + filenameEndPhrase + ".csv");

			if (printHeaders)
			{
				bwAllUsers.write(headers + "\n");
				// System.out.println("printing header :" + headers);
			}

			for (Map.Entry<String, TreeMap<Timestamp, DataEntry>> entryForUser : data.entrySet())
			{
				try
				{
					String userName = entryForUser.getKey();
					// System.out.println("\nUser =" + entryForUser.getKey());
					BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(
							Constant.getCommonPath() + userName + filenameEndPhrase + ".csv");

					TreeMap<Timestamp, String> mapForEachUser = new TreeMap<Timestamp, String>();

					if (printHeaders)
					{
						bw.write(headers + "\n");
						// System.out.println("printing header :" + headers);
					}

					for (Map.Entry<Timestamp, DataEntry> entry : entryForUser.getValue().entrySet())
					{
						numOfLinesOfData++;

						// DataEntry<TrackListenEntry> de= entry.getValue();
						//
						// if(de.getTrack)
						// if()
						// $$System.out.println(entry.getKey()+","+entry.getValue());
						bw.write(entry.getValue().toStringWithoutHeadersWithTrajID(delimiter) + "\n");
						bwAllUsers.write(entry.getValue().toStringWithoutHeadersWithTrajID(delimiter) + "\n");
						// "StartTimestamp,EndTimestamp,Mode,Latitude,Longitude,Altitude,DifferenceWithNextInSeconds,DurationInSeconds,BreakOverDaysCount"
					}

					bw.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			bwAllUsers.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return numOfLinesOfData;
	}

	/**
	 * Takes in the mapForAllData comprising of TrajectoryEntries, adds 'time difference with next in seconds' to all
	 * the TrajectoryEntries in it and returns the enriched map. And writes time difference between consecutive
	 * trajectory entries to a file names '..TimeDifferenceAll.csv' with columns UserID,TimeDifferenceWithNextInSeconds.
	 * 
	 * @param mapForAllData
	 * @return
	 */
	public static LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> getDataEntriesWithTimeDifferenceWithNext(
			LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> mapForAllData)
	{
		// <username , <start timestamp, data entry>
		LinkedHashMap<String, TreeMap<Timestamp, DataEntry>> mapForAllDataNotMergedPlusDuration = new LinkedHashMap<String, TreeMap<Timestamp, DataEntry>>();

		ArrayList<Pair<String, Long>> timeDifferencesBetweenDataPointAllUsers = new ArrayList<Pair<String, Long>>();

		System.out.println("inside getDataEntriesWithTimeDifferenceWithNext");
		for (Map.Entry<String, TreeMap<Timestamp, DataEntry>> entryForUser : mapForAllData.entrySet())
		{
			String userID = entryForUser.getKey();
			// System.out.println("\nUser ="+entryForUser.getKey());

			TreeMap<Timestamp, DataEntry> mapContinuousNotMerged = new TreeMap<Timestamp, DataEntry>();

			long diffWithNextInSeconds = 0;

			ArrayList<DataEntry> dataForCurrentUser = (ArrayList<DataEntry>) UtilityBelt
					.treeMapToArrayList(entryForUser.getValue());

			for (int i = 0; i < dataForCurrentUser.size(); i++)
			{
				DataEntry te = dataForCurrentUser.get(i);

				// System.out.println("--> te.getTime = " + te.getTimestamp());

				Timestamp currentTimestamp = te.getTimestamp();
				// String currentActivityName = te.getMode(); // probably this line is not needed TODO check

				if (i < dataForCurrentUser.size() - 1) // is not the last element of arraylist
				{
					Timestamp nextTimestamp = (dataForCurrentUser.get(i + 1)).getTimestamp();
					diffWithNextInSeconds = (nextTimestamp.getTime() - currentTimestamp.getTime()) / 1000;

					te.setDifferenceWithNextInSeconds(diffWithNextInSeconds);
					te.setDurationInSeconds(diffWithNextInSeconds);

					mapContinuousNotMerged.put(currentTimestamp, te);
					timeDifferencesBetweenDataPointAllUsers.add(new Pair(userID, diffWithNextInSeconds));
				}
				else
				{
					te.setDifferenceWithNextInSeconds(0);
					te.setDurationInSeconds(diffWithNextInSeconds);
					timeDifferencesBetweenDataPointAllUsers.add(new Pair(userID, diffWithNextInSeconds));
					mapContinuousNotMerged.put(currentTimestamp, te);
				}

			}
			mapForAllDataNotMergedPlusDuration.put(entryForUser.getKey(), mapContinuousNotMerged);
			System.out.println("put, User:" + userID + ", #DataEntries:" + mapContinuousNotMerged.size());
		}

		WritingToFile.writeArrayList2(timeDifferencesBetweenDataPointAllUsers, "TimeDifferenceAll",
				"UserID,TimeDifferenceWithNextInSeconds");
		System.out.println("exiting getTrajectoryEntriesWithTimeDifferenceWithNext");

		return mapForAllDataNotMergedPlusDuration;
	}

	/**
	 * Takes in the mapForAllData comprising of TrajectoryEntries, adds 'time difference with next in seconds' to all
	 * the TrajectoryEntries in it and returns the enriched map. And writes time difference between consecutive
	 * trajectory entries to a file names '..TimeDifferenceAll.csv' with columns UserID,TimeDifferenceWithNextInSeconds.
	 * 
	 * @param mapForAllData
	 * @return
	 */
	public static LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>> getDataEntriesWithTimeDifferenceWithNextTLE(
			LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>> mapForAllData)
	{
		// <username , <start timestamp, data entry>
		LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>> mapForAllDataNotMergedPlusDuration = new LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>>();

		ArrayList<Pair<String, Long>> timeDifferencesBetweenDataPointAllUsers = new ArrayList<Pair<String, Long>>();

		System.out.println("inside getDataEntriesWithTimeDifferenceWithNext");
		for (Map.Entry<String, TreeMap<Timestamp, TrackListenEntry>> entryForUser : mapForAllData.entrySet())
		{
			String userID = entryForUser.getKey();
			// System.out.println("\nUser ="+entryForUser.getKey());

			TreeMap<Timestamp, TrackListenEntry> mapContinuousNotMerged = new TreeMap<Timestamp, TrackListenEntry>();

			long diffWithNextInSeconds = 0;

			ArrayList<TrackListenEntry> dataForCurrentUser = (ArrayList<TrackListenEntry>) UtilityBelt
					.treeMapToArrayList(entryForUser.getValue());

			for (int i = 0; i < dataForCurrentUser.size(); i++)
			{
				TrackListenEntry te = dataForCurrentUser.get(i);

				// System.out.println("--> te.getTime = " + te.getTimestamp());

				Timestamp currentTimestamp = te.getTimestamp();
				// String currentActivityName = te.getMode();
				diffWithNextInSeconds = 0;
				if (i < dataForCurrentUser.size() - 1) // is not the last element of arraylist
				{
					Timestamp nextTimestamp = (dataForCurrentUser.get(i + 1)).getTimestamp();
					diffWithNextInSeconds = (nextTimestamp.getTime() - currentTimestamp.getTime()) / 1000;

					te.setDifferenceWithNextInSeconds(diffWithNextInSeconds);
					te.setDurationInSeconds(diffWithNextInSeconds);

					mapContinuousNotMerged.put(currentTimestamp, te);
					timeDifferencesBetweenDataPointAllUsers.add(new Pair(userID, diffWithNextInSeconds));
				}
				else
				{
					te.setDifferenceWithNextInSeconds(0);
					te.setDurationInSeconds(diffWithNextInSeconds);
					timeDifferencesBetweenDataPointAllUsers.add(new Pair(userID, diffWithNextInSeconds));
					mapContinuousNotMerged.put(currentTimestamp, te);
				}

			}
			mapForAllDataNotMergedPlusDuration.put(entryForUser.getKey(), mapContinuousNotMerged);
			System.out.println("put, User:" + userID + ", #DataEntries:" + mapContinuousNotMerged.size());
		}

		WritingToFile.writeArrayList2(timeDifferencesBetweenDataPointAllUsers, "TimeDifferenceAll",
				"UserID,TimeDifferenceWithNextInSeconds");
		System.out.println("exiting getTrajectoryEntriesWithTimeDifferenceWithNext");

		return mapForAllDataNotMergedPlusDuration;
	}

	/**
	 * Takes in the mapForAllData comprising of TrajectoryEntries, adds 'time difference with next in seconds' to all
	 * the TrajectoryEntries in it and returns the enriched map. And writes time difference between consecutive
	 * trajectory entries to a file names '..TimeDifferenceAll.csv' with columns UserID,TimeDifferenceWithNextInSeconds.
	 * 
	 * @param mapForAllData
	 * @return
	 */
	public static TreeMap<Timestamp, TrackListenEntry> getDataEntriesWithTimeDifferenceWithNextTLE(
			TreeMap<Timestamp, TrackListenEntry> mapForAllData)
	{
		// <username , <start timestamp, data entry>
		System.out.println("inside getDataEntriesWithTimeDifferenceWithNext");

		TreeMap<Timestamp, TrackListenEntry> mapContinuousNotMerged = new TreeMap<Timestamp, TrackListenEntry>();

		long diffWithNextInSeconds = 0;

		ArrayList<TrackListenEntry> dataForCurrentUser = (ArrayList<TrackListenEntry>) UtilityBelt
				.treeMapToArrayList(mapForAllData);

		for (int i = 0; i < dataForCurrentUser.size(); i++)
		{
			TrackListenEntry te = dataForCurrentUser.get(i);

			// System.out.println("--> te.getTime = " + te.getTimestamp());

			Timestamp currentTimestamp = te.getTimestamp();
			// String currentActivityName = te.getMode();
			diffWithNextInSeconds = 0;
			if (i < dataForCurrentUser.size() - 1) // is not the last element of arraylist
			{
				Timestamp nextTimestamp = (dataForCurrentUser.get(i + 1)).getTimestamp();
				diffWithNextInSeconds = (nextTimestamp.getTime() - currentTimestamp.getTime()) / 1000;

				te.setDifferenceWithNextInSeconds(diffWithNextInSeconds);
				te.setDurationInSeconds(diffWithNextInSeconds);

				mapContinuousNotMerged.put(currentTimestamp, te);

			}
			else
			{
				te.setDifferenceWithNextInSeconds(0);
				te.setDurationInSeconds(diffWithNextInSeconds);

				mapContinuousNotMerged.put(currentTimestamp, te);
			}

		}
		// System.out.println("put, User:" + userID + ", #DataEntries:" + mapContinuousNotMerged.size());

		System.out.println("exiting getTrajectoryEntriesWithTimeDifferenceWithNext");

		return mapContinuousNotMerged;
	}

	public static int differenceInSeconds(Timestamp previousTimestamp, Timestamp nextTimestamp)
	{
		int differenceInSeconds = 0;

		if (previousTimestamp.getTime() != 0)
		{
			differenceInSeconds = (int) (nextTimestamp.getTime() - previousTimestamp.getTime()) / 1000;

			if (differenceInSeconds < 1)
				System.err.println("Error in differenceInSeconds(): (nextTimestamp-previousTimestamp) is negative");
		}

		return differenceInSeconds;
	}

	/**
	 * Return true of the two timestamps have a time difference of less than a the 'continuity threshold in seconds'
	 * 
	 * @param timestamp1
	 * @param timestamp2
	 */
	public static boolean areContinuous(Timestamp timestamp1, Timestamp timestamp2)
	{
		long differenceInSeconds = Math.abs(timestamp1.getTime() - timestamp2.getTime()) / 1000;

		if (differenceInSeconds <= continuityThresholdInSeconds)
			return true;
		else
			return false;
	}

	/**
	 * Reads for the files from the folder for a given user and create the following files for them: 1)
	 * <username>_JPGFiles.txt: containing the list of names of all jpg files for that user 2) one files for each of the
	 * Activity names <username>_<categoryname>.txt containing the names of JPG files for this category.
	 * 
	 * @param folder
	 * @param path
	 * @param userName
	 */
	public static void listFilesForFolder(final File folder, String path, String userName)
	{
		// int count=0;
		String categories[] = { "badImages", "Commuting", "Computer", "Eating", "Exercising", "Housework",
				"On the Phone", "Preparing Food", "Shopping", "Socialising", "Watching TV" };
		int countOfJPG = 0;// , countOfCategoryAssignments=0;;
		int countOfActivityFilesFound = 0;
		int countOfJPGFilesMentionedInAllActivityFiles = 0;

		path = commonPath + "AllTogether7July/";

		for (File fileEntry : folder.listFiles())
		{
			if (fileEntry.isDirectory())
			{
				System.out.print("Directory: " + fileEntry + "");
				if (fileEntry.getName().toString().contains("thumbs"))
				{
					System.out.println("found thumbs");
				}
				else
					listFilesForFolder(fileEntry, path, userName);
			}
			else
			{
				System.out.print("Files (not directory)" + fileEntry.getName() + "");

				// check if the file name is for jpg files, if yes then add it to the list of jpg files.
				if (fileEntry.getName().toString().contains("jpg") || fileEntry.getName().toString().contains("JPG")
						|| fileEntry.getName().toString().contains("JPEG"))
				{
					countOfJPG++;
					appendStringToFile(path + userName + "_JPGFiles.txt", fileEntry.getName());
				}

				// check if it is a 'Listing of jpg files for category' files, like commuting.ann
				for (int i = 0; i < categories.length; i++)
				{
					String categoryName = categories[i];
					// System.out.println("Category Name check = "+categoryName+"\n");
					if (fileEntry.getName().toString().contains(categoryName))
					{
						countOfActivityFilesFound++;

						System.out.println(fileEntry.getName() + " is an 'Listing of jpg in category' files");
						// countOfCategoryAssignments;
						// System.out.println(fileEntry.getAbsolutePath());

						countOfJPGFilesMentionedInAllActivityFiles += appendFileContentsToFile(
								path + userName + "_" + categoryName + ".txt", fileEntry.getAbsolutePath(), userName);
					}
				}
			}
		}

		// writeInStats("\nFor user: "+userName+"\n\tTotal count of JPG files="+countOfJPG+" Total count of Activity
		// Files found="+countOfActivityFilesFound+" Total count of JPG
		// files mentioned in all activity files"
		// + "="+countOfJPGFilesMentionedInAllActivityFiles);
		System.out.println("*** ");
	}

	public static void appendStringToFile(String fileName, String textToWrite)
	{
		FileWriter output = null;
		try
		{
			output = new FileWriter(fileName, true);
			BufferedWriter writer = new BufferedWriter(output);

			writer.append(textToWrite + "\n");
			writer.close();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		finally
		{
			if (output != null)
			{
				try
				{
					output.flush();
					output.close();
				}
				catch (IOException e)
				{

				}
			}
		}
	}

	/**
	 * Append the contents of a given file to the end of another files
	 * 
	 * @param fileToWriteTo
	 * @param fileToRead
	 * @param userName
	 * @return
	 */
	public static int appendFileContentsToFile(String fileToWriteTo, String fileToRead, String userName)
	{
		BufferedReader br = null;
		int countNonEmptyLines = 0;

		try
		{
			String currentLine;
			// System.out.println("OOOO writing activity file file ="+fileToWriteTo);
			br = new BufferedReader(new FileReader(fileToRead));

			if ((currentLine = br.readLine()) == null)
			{
				System.out.println(fileToRead + " is empty");
				appendStringToFile(fileToWriteTo, "empty");
				br.close();
				return 0;
			}

			while ((currentLine = br.readLine()) != null)
			{
				// System.out.println("bazooka"+cu<<<rrentLine);
				if (currentLine.trim().isEmpty())
				{
					System.err.println("Reading contents from file:" + fileToRead + ", current lines is empty");
				}

				else
				{
					appendStringToFile(fileToWriteTo, currentLine);
					countNonEmptyLines++;
				}

			}
			br.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return countNonEmptyLines;

	}

	public static void writeInStats(String content)
	{
		try
		{
			File file = new File(commonPath + "stats.csv");

			if (!file.exists())
			{
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();

		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * For TrackListenEntries
	 * 
	 * @param data
	 * @param filenameEndPhrase
	 * @param headers
	 * @param printHeaders
	 * @param delimiter
	 * @return
	 */
	public static long writeDataToFile2WithHeadersTLE(LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>> data,
			String filenameEndPhrase, String headers, boolean printHeaders, String delimiter)
	{
		long numOfLinesOfData = 0;
		try
		{
			BufferedWriter bwAllUsers = WritingToFile
					.getBufferedWriterForNewFile(Constant.getCommonPath() + "AllUsers" + filenameEndPhrase + ".csv");

			if (printHeaders)
			{
				bwAllUsers.write(headers + "\n");
				// System.out.println("printing header :" + headers);
			}

			for (Map.Entry<String, TreeMap<Timestamp, TrackListenEntry>> entryForUser : data.entrySet())
			{
				try
				{
					String userName = entryForUser.getKey();
					// System.out.println("\nUser =" + entryForUser.getKey());
					BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(
							Constant.getCommonPath() + userName + filenameEndPhrase + ".csv");

					TreeMap<Timestamp, String> mapForEachUser = new TreeMap<Timestamp, String>();

					if (printHeaders)
					{
						bw.write(headers + "\n");
						// System.out.println("printing header :" + headers);
					}

					for (Map.Entry<Timestamp, TrackListenEntry> entry : entryForUser.getValue().entrySet())
					{
						numOfLinesOfData++;
						// $$System.out.println(entry.getKey()+","+entry.getValue());
						bw.write(entry.getValue().toStringWithoutHeadersWithTrajID(delimiter) + "\n");
						bwAllUsers.write(entry.getValue().toStringWithoutHeadersWithTrajID(delimiter) + "\n");
						// "StartTimestamp,EndTimestamp,Mode,Latitude,Longitude,Altitude,DifferenceWithNextInSeconds,DurationInSeconds,BreakOverDaysCount"
					}

					bw.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			bwAllUsers.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return numOfLinesOfData;
	}

	public static long writeDataToFile2WithHeadersTLEOnlyAll(
			LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>> data, String filenameEndPhrase, String headers,
			boolean printHeaders, String delimiter)
	{
		long numOfLinesOfData = 0;
		try
		{
			BufferedWriter bwAllUsers = WritingToFile
					.getBufferedWriterForNewFile(Constant.getCommonPath() + "AllUsers" + filenameEndPhrase + ".csv");

			if (printHeaders)
			{
				bwAllUsers.write(headers + "\n");
				// System.out.println("printing header :" + headers);
			}

			for (Map.Entry<String, TreeMap<Timestamp, TrackListenEntry>> entryForUser : data.entrySet())
			{
				try
				{
					String userName = entryForUser.getKey();
					// System.out.println("\nUser =" + entryForUser.getKey());
					// BufferedWriter bw =
					// WritingToFile.getBufferedWriterForNewFile(Constant.getCommonPath() + userName + filenameEndPhrase
					// + ".csv");

					TreeMap<Timestamp, String> mapForEachUser = new TreeMap<Timestamp, String>();

					// if (printHeaders)
					// {
					//// bw.write(headers + "\n");
					// // System.out.println("printing header :" + headers);
					// }

					for (Map.Entry<Timestamp, TrackListenEntry> entry : entryForUser.getValue().entrySet())
					{
						numOfLinesOfData++;
						// $$System.out.println(entry.getKey()+","+entry.getValue());
						// bw.write(entry.getValue().toStringWithoutHeadersWithTrajID(delimiter) + "\n");
						bwAllUsers.write(entry.getValue().toStringWithoutHeadersWithTrajID(delimiter) + "\n");
						// "StartTimestamp,EndTimestamp,Mode,Latitude,Longitude,Altitude,DifferenceWithNextInSeconds,DurationInSeconds,BreakOverDaysCount"
					}

					// bw.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			bwAllUsers.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return numOfLinesOfData;
	}

	public static long writeDataToFile2WithHeadersTLEOnlyAll(TreeMap<Timestamp, TrackListenEntry> data,
			String filenameEndPhrase, String headers, boolean printHeaders, String delimiter, String splitID)
	{
		long numOfLinesOfData = 0;
		try
		{
			BufferedWriter bw = WritingToFile
					.getBufferedWriterForNewFile(Constant.getCommonPath() + splitID + filenameEndPhrase + ".csv");

			if (printHeaders)
			{
				bw.write(headers + "\n");
				// System.out.println("printing header :" + headers);
			}

			for (Map.Entry<Timestamp, TrackListenEntry> entry : data.entrySet())
			{
				numOfLinesOfData++;
				// $$System.out.println(entry.getKey()+","+entry.getValue());
				// bw.write(entry.getValue().toStringWithoutHeadersWithTrajID(delimiter) + "\n");
				bw.write(entry.getValue().toStringWithoutHeadersWithTrajID(delimiter) + "\n");
				// "StartTimestamp,EndTimestamp,Mode,Latitude,Longitude,Altitude,DifferenceWithNextInSeconds,DurationInSeconds,BreakOverDaysCount"
			}

			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return numOfLinesOfData;

	}

	public static long writeDataToFile2WithHeadersTLEOnlyAllSlimmer(
			LinkedHashMap<String, TreeMap<Timestamp, TrackListenEntry>> data, String filenameEndPhrase, String headers,
			boolean printHeaders, String delimiter)
	{
		long numOfLinesOfData = 0;
		try
		{
			BufferedWriter bwAllUsers = WritingToFile
					.getBufferedWriterForNewFile(Constant.getCommonPath() + "AllUsers" + filenameEndPhrase + ".csv");

			if (printHeaders)
			{
				bwAllUsers.write(headers + "\n");
				// System.out.println("printing header :" + headers);
			}

			for (Map.Entry<String, TreeMap<Timestamp, TrackListenEntry>> entryForUser : data.entrySet())
			{
				try
				{
					String userName = entryForUser.getKey();
					// System.out.println("\nUser =" + entryForUser.getKey());
					// BufferedWriter bw =
					// WritingToFile.getBufferedWriterForNewFile(Constant.getCommonPath() + userName + filenameEndPhrase
					// + ".csv");

					TreeMap<Timestamp, String> mapForEachUser = new TreeMap<Timestamp, String>();

					// if (printHeaders)
					// {
					//// bw.write(headers + "\n");
					// // System.out.println("printing header :" + headers);
					// }

					for (Map.Entry<Timestamp, TrackListenEntry> entry : entryForUser.getValue().entrySet())
					{
						numOfLinesOfData++;
						// $$System.out.println(entry.getKey()+","+entry.getValue());
						// bw.write(entry.getValue().toStringWithoutHeadersWithTrajID(delimiter) + "\n");
						bwAllUsers.write(entry.getValue().toStringWithoutHeadersWithTrajIDSlimmer(delimiter) + "\n");
						// "StartTimestamp,EndTimestamp,Mode,Latitude,Longitude,Altitude,DifferenceWithNextInSeconds,DurationInSeconds,BreakOverDaysCount"
					}

					// bw.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			bwAllUsers.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return numOfLinesOfData;
	}

	public static long writeDataToFile2WithHeadersTLEOnlyAllSlimmer(TreeMap<Timestamp, TrackListenEntry> data,
			String filenameEndPhrase, String headers, boolean printHeaders, String delimiter, String splitID)
	{
		long numOfLinesOfData = 0;
		try
		{
			BufferedWriter bw = WritingToFile
					.getBufferedWriterForNewFile(Constant.getCommonPath() + splitID + filenameEndPhrase + ".csv");
			try
			{
				if (printHeaders)
				{
					bw.write(headers + "\n");
					// System.out.println("printing header :" + headers);
				}

				for (Map.Entry<Timestamp, TrackListenEntry> entry : data.entrySet())
				{
					numOfLinesOfData++;
					// $$System.out.println(entry.getKey()+","+entry.getValue());
					// bw.write(entry.getValue().toStringWithoutHeadersWithTrajID(delimiter) + "\n");
					bw.write(entry.getValue().toStringWithoutHeadersWithTrajIDSlimmer(delimiter) + "\n");
					// "StartTimestamp,EndTimestamp,Mode,Latitude,Longitude,Altitude,DifferenceWithNextInSeconds,DurationInSeconds,BreakOverDaysCount"
				}

				bw.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return numOfLinesOfData;

	}
}
