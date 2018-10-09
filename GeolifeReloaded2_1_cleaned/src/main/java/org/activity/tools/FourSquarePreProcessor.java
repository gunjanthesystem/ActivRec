package org.activity.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.activity.io.ReadingFromFile;
import org.activity.io.WToFile;
import org.activity.objects.LeakyBucket;
import org.activity.spatial.SpatialUtils;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;

import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.io.csv.CsvWriteOptions;

public class FourSquarePreProcessor
{
	public static void main(String args[])
	{
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		// fourSquarePreprocessor1();
		// $$sortData();//

		String commonPath = "/run/media/gunjan/BackupVault/DatasetsInBackupVault/FoursquareDatasets/dataset_tsmc2014/";
		addDistanceAndDurationFromPrev(commonPath + "dataset_TSMC2014_NYC_Processed1_NoDupSorted2.txt",
				commonPath + "dataset_TSMC2014_NYC_Processed1_NoDupSorted2DistDur.csv", commonPath);

	}

	/**
	 * Derived from
	 * org.activity.generator.DatabaseCreatorGowallaQuickerPreprocessor.preprocessCheckInWithDateCategoryOnlySpots1FasterWithTimeZone()
	 * 
	 * <p>
	 * <b>Expecting all timestamps to be in the UTC timezone</b>
	 * 
	 * 
	 * @param checkinFileNameToRead
	 * @param fileNameToWrite
	 * @param commonPathToWrite
	 * @return
	 */
	private static String addDistanceAndDurationFromPrev(String checkinFileNameToRead, String fileNameToWrite,
			String commonPathToWrite)
	{
		// long countOfSpots1 = 0, countRejNotFoundInSpots1 = 0,; countRejCheckinsWithPlaceWithNoTZ = 0;
		// Map<String, Long> userZoneHoppingCount = new LinkedHashMap<>();
		long countRejWrongGeoCoords = 0;
		Map<String, Long> userCheckinsCount = new LinkedHashMap<>();
		Pattern dlimPatrn = Pattern.compile(",");
		System.out.println("addDistanceAndDurationFromPrev called with checkinfilename = " + checkinFileNameToRead);

		try
		{
			int lineCount = 0;
			BufferedReader brCurrent = new BufferedReader(new FileReader(checkinFileNameToRead));
			LeakyBucket lbToWrite = new LeakyBucket(5000, fileNameToWrite, false);
			lbToWrite.addToLeakyBucketWithNewline(
					"UserID,PlaceID,CatID,CatName,Lat,Lon,TZOffset,UTCTime,LocalTime,EpochSecs,TimeZonedToCorrectLocalDate,DistInMFromPrevLine,DurationInSecsFromPrevLine");

			// StringBuilder toWriteInBatch = new StringBuilder();
			// boolean isCurrGeoCoordsValid = false, isPrevGeoCoordsValid = false;

			String currentLineRead;// , nextLineRead;
			String currUser = "", prevLineUser = "";// , nextLineUser = "";
			Instant currentInstant = null, prevLineInstant = null; // Instant is to be from Zulu timestamp.
			// LocalDateTime currentLocalTime = null, prevLineLocalTime = null;// , nextLineTime = null;
			String currentLat = "", prevLineLat = "";// , nextLineLat = "";
			String currentLon = "", prevLineLon = "";// , nextLineLon = "";
			// to find time diff and users and instances of timezonehopping
			// ZoneId currentZoneId = null, prevLineZoneId = null;// , nextLineZoneId = null;

			// String spotCatID = "";String spotCatName = "";

			long numOfValidCheckinsForPrevUser = 0;

			while ((currentLineRead = brCurrent.readLine()) != null)
			{
				// System.out.println("\n--> currentLineRead=\n" + currentLineRead);
				lineCount++;
				if (lineCount == 1)
				{
					System.out.println("Skipping first line of brCurrent ");
					continue;
				}
				else if (lineCount % 10000 == 0)
				{
					System.out.println("Lines read = " + lineCount);
				}

				// clearing current variables
				currUser = "";
				currentInstant = null;// currentLocalTime = null;
				currentLat = "";
				currentLon = "";// currentZoneId = null;
				// spotCatID = "NA";spotCatName = "NA";
				double distFromPrevLineInMeters = -999;
				long durFromPrevLineInSecs = -999;

				String[] splittedString = dlimPatrn.split(currentLineRead);// currentLineRead.split(",");// dlimPatrn);
				String placeID = splittedString[1];
				// System.out.println("place id to search for " + placeID);

				// // Ignore checkins into places with no timezone
				// if (locTimeZoneMap.containsKey(placeID) == false)
				// { countRejCheckinsWithPlaceWithNoTZ += 1;continue;
				// }else { currentZoneId = locTimeZoneMap.get(placeID).toZoneId();

				// substring to remove the last .000
				// Read the local time so that the program thinks it is UTC time.
				String localTimeString = splittedString[8].substring(0, splittedString[8].length() - 2) + "Z";
				currentInstant = Instant.parse(localTimeString);
				// DataUtils.s
				// currentLocalTime = DateTimeUtils.instantToTimeZonedLocalDateTime(currentInstant, currentZoneId);
				// // .zuluToTimeZonedLocalDateTime(splittedString[2], currentZoneId);
				// } ArrayList<String> vals1 = spots1.get(placeID);
				// Ignore checkin into places into in spots subset1
				// if (vals1 == null)
				// { countRejNotFoundInSpots1 += 1; currentLat = "-777"; // not found
				// currentLon = "-777"; // not found
				// continue;}else{ countOfSpots1++;
				currentLat = splittedString[4];
				currentLon = splittedString[5];
				// spotCatID = splittedString[2];spotCatName = splittedString[3];// }

				// Ignore checkin into places into with incorrect lat lon
				if (Math.abs(Double.valueOf(currentLat)) > 90 || Math.abs(Double.valueOf(currentLon)) > 180)
				{
					// System.out.println("Invalid geo coordinate becauce. lat ="+currentLat+" long="+currentLat);
					countRejWrongGeoCoords += 1;
					continue;
				}

				// if reached here then VALID CHECKIN
				currUser = splittedString[0];

				// if same user, compute distance & duration from next (∵ read lines are in decreasing order by time)
				// System.out.println("prevLineUser.length() = " + prevLineUser.length() + " prevLineUser = "
				// + prevLineUser + "\ncurrentInstant=" + currentInstant + " prevLineInstant=" + prevLineInstant);

				if (prevLineUser.equals(currUser) && prevLineUser.length() > 0)
				{
					distFromPrevLineInMeters = SpatialUtils.haversineFastMath(currentLat, currentLon, prevLineLat,
							prevLineLon);//
					distFromPrevLineInMeters *= 1000;
					distFromPrevLineInMeters = StatsUtils.round(distFromPrevLineInMeters, 2);

					durFromPrevLineInSecs = currentInstant.getEpochSecond() - prevLineInstant.getEpochSecond();

					// find time zone hoppings.
					// if (currentZoneId.getId().equals(prevLineZoneId.getId()) == false)
					// { long prevHoppingsCount = 0;if (userZoneHoppingCount.containsKey(currUser))
					// { prevHoppingsCount = userZoneHoppingCount.get(currUser);}
					// userZoneHoppingCount.put(currUser, prevHoppingsCount + 1);}
				}

				else
				{
					distFromPrevLineInMeters = -99;// 0;
					durFromPrevLineInSecs = -99;// 0;

					// expecting read file to be sorted by users
					if (userCheckinsCount.containsKey(prevLineUser))
					{
						PopUps.showError("userCheckinsCount already contains user:" + prevLineUser
								+ "- Seems read file was not sorted by user id");
					}

					if (prevLineUser.length() > 0)// not empty, to discard the first empty previous usee
					{
						userCheckinsCount.put(prevLineUser, numOfValidCheckinsForPrevUser);
					}
					numOfValidCheckinsForPrevUser = 0;
				}

				prevLineUser = currUser;
				prevLineInstant = currentInstant;// prevLineLocalTime = currentLocalTime;
				prevLineLat = currentLat;
				prevLineLon = currentLon;// prevLineZoneId = currentZoneId;
				numOfValidCheckinsForPrevUser += 1;

				lbToWrite.addToLeakyBucketWithNewline(
						currentLineRead + "," + distFromPrevLineInMeters + "," + durFromPrevLineInSecs);
			} // end of while over lines

			lbToWrite.flushLeakyBucket();// should also write leftover
			userCheckinsCount.put(prevLineUser, numOfValidCheckinsForPrevUser);// since its writing for prev user

			System.out.println("Num of checkins lines read = " + lineCount);
			// System.out.println("Count of checkins in spots1 = " + countOfSpots1);
			System.out.println("Rejected checkins checking for rejection condition in following order");
			// System.out.println("countRejCheckinsWithPlaceWithNoTZ = " + countRejCheckinsWithPlaceWithNoTZ);
			// System.out.println("countRejNotFoundInSpots1 = " + countRejNotFoundInSpots1);
			System.out.println("countRejWrongGeoCoords = " + countRejWrongGeoCoords);

			// System.out.println("userZoneHoppingCount sum = "
			// + userZoneHoppingCount.entrySet().stream().mapToLong(e -> e.getValue()).sum());
			// WToFile.writeSimpleMapToFile(userZoneHoppingCount, commonPathToWrite + "userZoneHoppingCount.csv",
			// "User", "TimezoneHoppingCount");
			WToFile.writeSimpleMapToFile(userCheckinsCount, commonPathToWrite + "userCheckinsCount.csv", "User",
					"CheckinsCount");

			brCurrent.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return fileNameToWrite;

	}

	/**
	 * 
	 * @param absInputFileSortedAscUserTimestamp
	 * @param absOutputfile
	 * @deprecated NOT USED replaced by org.activity.tools.FourSquarePreProcessor.addDistanceAndDurationFromPrev()
	 */
	public static void addDistanceFromPrevDurationFromPrev()
	{
		String commonPath = "";
		String absInputFileSortedAscUserTimestamp = commonPath + "";
		String absOutputfile = commonPath + "";
		String colSep = ",";
		int indexOfUser = 0, indexOfLat = 0, indexOfLon = 0, indexOfTimestamp = 0;
		int countRejWrongGeoCoords = 0;

		try
		{
			List<List<String>> allData = ReadingFromFile.nColumnReaderString(absInputFileSortedAscUserTimestamp, colSep,
					false);
			System.out.println("Num of lines read = " + allData.size());

			String prevUserID = "", currUserID = "";
			double prevLat = -9999;
			double currLat = -9999;
			double prevLon = -9999;
			double currLon = -9999;

			Instant prevInstant = null;
			Instant currInstant = null;
			int distFromPrevLineInMeters = -9999;

			for (List<String> currRow : allData)
			{
				currUserID = currRow.get(indexOfUser);
				currLat = Double.valueOf(currRow.get(indexOfLat));
				currLon = Double.valueOf(currRow.get(indexOfLon));
				currInstant = Instant.parse(currRow.get(indexOfTimestamp));

				// Ignore checkin into places into with incorrect lat lon
				if (Math.abs(Double.valueOf(currLat)) > 90 || Math.abs(Double.valueOf(currLon)) > 180)
				{
					// System.out.println("Invalid geo coordinate becauce. lat ="+currentLat+" long="+currentLat);
					countRejWrongGeoCoords += 1;
				}
				else
				{
					if (prevUserID.equals(currUserID))
					{
						// distFromPrevLineInMeters = SpatialUtils.haversineFastMath(currentLat, currentLon,
						// prevLineLat,
						// prevLineLon);//
						// distFromPrevLineInMeters *= 1000;
						// distFromPrevLineInMeters = StatsUtils.round(distFromPrevLineInMeters, 2);
						// durFromPrevLineInSecs = prevLineInstant.getEpochSecond() - currentInstant.getEpochSecond();
					}

				}

				prevUserID = currUserID;
				prevLat = currLat;
				prevLon = currLon;
				prevInstant = currInstant;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public static void sortData()
	{
		String commonPath = "/home/gunjan/Documents/UCD/Datasets/link to DatasetsInBackupVault/FoursquareDatasets/dataset_tsmc2014/";
		String fileToRead = commonPath + "dataset_TSMC2014_NYC_Processed1_NoDup.txt";
		String fileToWrite = commonPath + "dataset_TSMC2014_NYC_Processed1_NoDupSorted2.txt";
		String colSep = "\t";

		try
		{

			// List<List<String>> allData = ReadingFromFile.nColumnReaderString(fileToRead, colSep, false);
			// System.out.println("Num of lines read = " + allData.size());

			CsvReadOptions.Builder builder = CsvReadOptions.builder(fileToRead).separator('\t').header(false);
			CsvReadOptions options = builder.build();
			Table dataRead = Table.read().csv(options);

			System.out.println("x shape =\n" + dataRead.shape());
			System.out.println("x structure =\n" + dataRead.structure());

			Table dataSorted = dataRead.sortAscendingOn("C0", "C8");// sort by userID, timestamp
			System.out.println("x shape =\n" + dataSorted.shape());
			System.out.println("x structure =\n" + dataSorted.structure());

			CsvWriteOptions.Builder builder2 = CsvWriteOptions.builder(fileToWrite).separator('\t').header(false);
			CsvWriteOptions options2 = builder2.build();
			dataSorted.write().csv(options2);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * Parse timestamp, add offset to get local time for each checkin and epochs in seconds and append as two new
	 * columns.
	 * 
	 * @since 13 Sep 2018
	 */
	public static void fourSquarePreprocessor1()
	{
		try
		{
			TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
			// LocalDateTime date = LocalDateTime.parse("Mar 23 1994", DateTimeFormatter.ofPattern("MMM d yyyy"));
			// LocalDateTime date2 = LocalDateTime.parse("Apr 03 18:14:03 2012",
			// DateTimeFormatter.ofPattern("MMM d HH:mm:ss yyyy"));

			DateTimeFormatter dtFormatter1 = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss Z yyyy");
			LocalDateTime date = LocalDateTime.parse("Tue Apr 03 18:14:03 +0000 2012", dtFormatter1);

			// Tue Apr 03 18:14:03 +0000 2012/

			// as.POSIXct("Tue Apr 03 18:14:03 2012", format ="%a %b %d %H:%M:%S %Y")
			// System.out.println(date);
			// System.out.println(date2);
			String commonPath = "/home/gunjan/Documents/UCD/Datasets/link to DatasetsInBackupVault/FoursquareDatasets/dataset_tsmc2014/";
			String fileToRead = commonPath + "dataset_TSMC2014_NYC.txt";
			String colSep = "\t";
			List<List<String>> allData = ReadingFromFile.nColumnReaderString(fileToRead, colSep, false);
			// .readLinesIntoListOfLists(fileToRead, colSep);
			// .readLinesIntoListOfLists(fileToRead, 0, 5, colSep);
			//
			System.out.println("Num of lines read = " + allData.size());

			int indexOfTimeStamp = 7;
			int indexOfOffsetInMinutes = 6;

			// Sz = ZoneId.of("UTC");
			LeakyBucket lb = new LeakyBucket(5000, commonPath + "dataset_TSMC2014_NYC_Processed1.txt", false);
			for (List<String> line : allData)
			{
				LocalDateTime actualTimeAsLocalTime = LocalDateTime.parse(line.get(indexOfTimeStamp), dtFormatter1)
						.plusMinutes(Integer.valueOf(line.get(indexOfOffsetInMinutes)));

				String newLine = line.stream().collect(Collectors.joining(colSep)) + colSep
						+ actualTimeAsLocalTime.toString() + colSep
						+ actualTimeAsLocalTime.atZone(ZoneId.of("UTC")).toEpochSecond() + colSep
						+ actualTimeAsLocalTime.toLocalDate();
				lb.addToLeakyBucketWithNewline(newLine);
			}
			lb.flushLeakyBucket();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
