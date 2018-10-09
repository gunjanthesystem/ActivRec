package org.activity.generator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.activity.io.ReadingFromFile;
import org.activity.io.Serializer;
import org.activity.util.RegexUtils;

public class DatabaseCreatorFSQuicker1
{

	String pathToAllCheckinFile = "/home/gunjan/RWorkspace/GowallaRWorks/FSNY2018-10-04AllTargetUsersDatesOnly.csv";

	public static void main(String[] args)
	{
		mapAlphaNumericPlaceCatIDToInts();

	}

	/**
	 * Convert alphanumeric place and cat ID to integer IDs.
	 * <p>
	 * Create catID name dictionary
	 */
	public static void mapAlphaNumericPlaceCatIDToInts()
	{
		String commonPathToRead = "/home/gunjan/RWorkspace/GowallaRWorks/";
		String commonPathToWrite = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/FSDataWorks/";
		String fileToRead = commonPathToRead + "FSNY2018-10-04AllTargetUsersDatesOnly.csv";
		String fileToWrite = commonPathToWrite + "FSNY2018-10-04AllTargetUsersDatesOnlyCatIDNameDict.csv";

		try
		{

			List<List<String>> allData = ReadingFromFile.nColumnReaderString(fileToRead, ",", true);
			// List<String> res = ReadingFromFile.twoColumnReaderString(fileToRead, ",", 2, 3, true);
			System.out.println("allData.size() = " + allData.size());
			Pattern comma = RegexUtils.patternComma;

			Map<String, Integer> placeIDANPlaceIDInt = new TreeMap<>();
			Map<Integer, String> placeIDIntPlaceIDAN = new TreeMap<>();
			Map<String, Integer> catIDANCatIDInt = new TreeMap<>();
			Map<Integer, String> catIDIntCatIDAN = new TreeMap<>();

			Set<String> allPlaceIDsAn = new TreeSet<>();
			Set<String> allCatIDsAn = new TreeSet<>();
			Map<String, String> catIDANCatName = new TreeMap<>();
			Map<Integer, String> catIDIntcatName = new TreeMap<>();

			for (List<String> line : allData)
			{
				allCatIDsAn.add(line.get(2));
				catIDANCatName.put(line.get(2), line.get(3));
				allPlaceIDsAn.add(line.get(1));
			}

			System.out.println("catIDsAn.size() = " + allCatIDsAn.size());
			System.out.println("placeIDsAn.size() = " + allPlaceIDsAn.size());
			System.out.println("catIDANcatName.size() = " + catIDANCatName.size());

			int catIDInt = 0;
			for (String catAN : allCatIDsAn)
			{
				catIDInt += 1;
				catIDANCatIDInt.put(catAN, catIDInt);
				catIDIntCatIDAN.put(catIDInt, catAN);
				catIDIntcatName.put(catIDInt, catIDANCatName.get(catAN));
			}

			int placeIDInt = 0;
			for (String placeIDAN : allPlaceIDsAn)
			{
				placeIDInt += 1;
				placeIDANPlaceIDInt.put(placeIDAN, placeIDInt);
				placeIDIntPlaceIDAN.put(placeIDInt, placeIDAN);
			}

			System.out.println("catIDANcatIDInt.size() = " + catIDANCatIDInt.size());
			System.out.println("catIDIntcatIDAN.size() = " + catIDIntCatIDAN.size());
			System.out.println("catIDIntcatName.size() = " + catIDIntcatName.size());
			System.out.println("placeIDANplaceIDInt.size() = " + placeIDANPlaceIDInt.size());
			System.out.println("placeIDIntplaceIDAN.size() = " + placeIDIntPlaceIDAN.size());

			Serializer.kryoSerializeThis(catIDANCatIDInt, commonPathToWrite + "catIDANCatIDInt.kryo");
			Serializer.kryoSerializeThis(catIDIntCatIDAN, commonPathToWrite + "catIDIntCatIDAN.kryo");
			Serializer.kryoSerializeThis(catIDIntcatName, commonPathToWrite + "catIDIntCatName.kryo");
			Serializer.kryoSerializeThis(placeIDANPlaceIDInt, commonPathToWrite + "placeIDANPlaceIDInt.kryo");
			Serializer.kryoSerializeThis(placeIDIntPlaceIDAN, commonPathToWrite + "placeIDInPlaceIDAN.kryo");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
