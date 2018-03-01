package org.activity.generator;

import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.activity.constants.PathConstants;
import org.activity.io.ReadingFromFile;
import org.activity.io.Serializer;

/**
 * 
 * @author gunjan
 * @since 27 Feb 2018
 */
public class TimeZoneReader
{

	public TimeZoneReader()
	{
		// TODO Auto-generated constructor stub
	}

	public static void main(String args[])
	{
		PathConstants.intialise(false);
		Map<Integer, ZoneId> locIDZoneIdMap = createTimeZoneForGowallaLocations(
				PathConstants.pathToLocationTimezoneInfo);
		System.out.println("locIDZoneIdMap.size()=" + locIDZoneIdMap.size());
		Serializer.kryoSerializeThis(locIDZoneIdMap,
				"./dataToRead/Feb26/UniqueLocationObjects5DaysTrainTestWithTZUsingPy.kryo");
	}

	// countOfIncompleteData=6 of total 368077 locs
	// locIDZoneIdMap.size()=368070
	// Successfully kryo serialised ./dataToRead/Feb26/UniqueLocationObjects5DaysTrainTestWithTZUsingPy.kryo in 0 secs

	private static Map<Integer, ZoneId> createTimeZoneForGowallaLocations(String pathToLocationTimezoneInfo)
	{
		Map<Integer, ZoneId> locIDZoneIdMap = new LinkedHashMap<Integer, ZoneId>();
		try
		{
			List<List<String>> data = ReadingFromFile.readLinesIntoListOfLists(pathToLocationTimezoneInfo, ",");

			// List<List<String>> data = ReadingFromFile.nColumnReaderStringLargeFileSelectedColumns(
			// new FileInputStream(pathToLocationTimezoneInfo), ",", true, false);// , new int[] { 1, 4 }

			int countOfIncompleteData = 0;
			int countOfLines = 0;
			for (List<String> e : data)
			{
				++countOfLines;
				if (countOfLines == 1)
				{
					continue;
				}

				if (e.size() != 5)
				{
					System.out.println(e.toString());
					countOfIncompleteData += 1;
				}

				else
				{
					// System.out.println(e.toString());
					Integer locID = Integer.valueOf(e.get(1));
					ZoneId zoneId = ZoneId.of(e.get(4));
					locIDZoneIdMap.put(locID, zoneId);
					// System.out.println("LocID= " + locID + " zoneId=" + zoneId);
				}
			}
			System.out.println("countOfIncompleteData=" + countOfIncompleteData + " of total " + data.size() + " locs");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return locIDZoneIdMap;
	}

	// [259769, 1602600, -63.3209309534, -57.8998804092]
	// [328496, 6659803, 0.0, 0.0]
	// [339529, 6855216, 0.0, 0.0]
	// [339618, 6856581, 0.0, 0.0]
	// [351286, 7086791, -6.94444e-05, -6.94444e-05]
	// [354613, 7159757, 0.0, 0.0]
	// countOfIncompleteData=6 of total 368077 locs

}
