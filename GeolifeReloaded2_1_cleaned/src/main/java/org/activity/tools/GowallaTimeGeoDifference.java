package org.activity.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.activity.io.WritingToFile;
import org.activity.spatial.SpatialUtils;
import org.activity.stats.StatsUtils;
import org.activity.util.DateTimeUtils;

/**
 * Computes the time and geo difference between consecutive checkins
 * 
 * @author gunjan
 *
 */
public class GowallaTimeGeoDifference
{

	public GowallaTimeGeoDifference(String checkinFileName, String processedFileName)
	{
		try
		{
			int lineCount = 0;
			BufferedReader br = new BufferedReader(new FileReader(checkinFileName));
			BufferedWriter bw = WritingToFile.getBWForNewFile(processedFileName);
			// BufferedWriter bw2 = WritingToFile.getBufferedWriterForNewFile(processedFile + "slim");

			bw.write("UserID, PlaceID,TS,Date,Lat,Lon,SpotCategoryID,SpotCategoryIDName,DistInM,DurationInSecs\n");

			StringBuffer toWriteInBatch = new StringBuffer();

			String currentLineRead;
			// , partOfCurrentLineToWrite;
			StringBuilder partOfCurrentLineToWrite = new StringBuilder();

			String prevLat = "", prevLon = "", prevUser = "";
			Timestamp prevTime = null, currentTime = null;
			String currentLat = "", currentLon = "", currUser = "";

			while ((currentLineRead = br.readLine()) != null)
			{
				lineCount++;
				if (lineCount == 1)
				{
					System.out.println("Skipping first line");
					continue; // skip the first line
				}

				else if (lineCount % 10000 == 0)
				{
					System.out.println("Lines read = " + lineCount);
				}

				String[] splittedString = currentLineRead.split(",");
				// System.out.println("currentLineRead = " + currentLineRead);

				currentTime = DateTimeUtils.getTimestampLastFMData(splittedString[2]);
				currUser = splittedString[0];
				currentLat = splittedString[4];
				currentLon = splittedString[5];

				partOfCurrentLineToWrite.setLength(0);// reset

				// don't write the last 2 columns, diff of geo and time again as they are to be recomputed here
				for (int i = 0; i < (splittedString.length - 2); i++)
				{
					partOfCurrentLineToWrite.append(splittedString[i] + ",");
				}

				double distFromPrevInMeters;
				long durationFromPrevInSeconds;

				if (prevUser.equals(currUser))
				{
					distFromPrevInMeters = SpatialUtils.haversine(currentLat, currentLon, prevLat, prevLon);
					distFromPrevInMeters = distFromPrevInMeters * 1000;
					distFromPrevInMeters = StatsUtils.round(distFromPrevInMeters, 2);

					durationFromPrevInSeconds = -(currentTime.getTime() - prevTime.getTime()) / 1000;
				}

				else
				{
					distFromPrevInMeters = -99;
					durationFromPrevInSeconds = -99;
				}

				if (Double.valueOf(currentLat) > -777 && Double.valueOf(currentLon) > -777)
				{// when not found in spots 1 or spots 2
					prevLat = currentLat;
					prevLon = currentLon;
				}

				prevTime = currentTime;
				prevUser = currUser;

				String towrite = partOfCurrentLineToWrite.toString() + distFromPrevInMeters + ","
						+ durationFromPrevInSeconds + "\n";

				toWriteInBatch.append(towrite);

				if (lineCount % 70870 == 0)// 48260 == 0) // 24130 find divisors of 36001960 using
				// http://www.javascripter.net/math/calculators/divisorscalculator.htm
				{
					bw.write(toWriteInBatch.toString());
					toWriteInBatch.setLength(0);
				}
			}

			// write any leftovers
			if (toWriteInBatch.length() > 0)
			{
				bw.write(toWriteInBatch.toString());
				toWriteInBatch.setLength(0);
			}
			System.out.println("Num of lines read = " + lineCount);
			br.close();
			bw.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		String fileToRead = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/CuckooFiltered0000000000001/NoDuplicateprocessedCheckIns.csv";
		String fileToWrite = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/NoDuplicateprocessedCheckInsWithReDiff.csv";

		try
		{
			PrintStream consoleLogStream = new PrintStream(
					new File("/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/"
							+ "consoleLogTimeGeoDiffGowalla.txt"),
					"US-ASCII");
			System.out.println("Current DateTime: " + LocalDateTime.now());
			new GowallaTimeGeoDifference(fileToRead, fileToWrite);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

}
