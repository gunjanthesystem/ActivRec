package org.activity.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TimeZone;

import org.activity.io.WritingToFile;
import org.activity.objects.Triple;
import org.activity.util.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class JSONProcessingAOTM
{
	
	//// < "Level1Name|Level1CatID|Level1Descritption", <"Level2Name|Level2CatID|Level2Descritption", <"Level3Name|Level3CatID|Level3Descritption">>>
	static LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> hierarchyTree;
	
	public static void main(String args[])
	{
		// writeCatLevelInfo();
		// main2();
		getAOTMFromJSON();
	}
	
	public static Triple getAOTMFromJSON()
	{
		
		String fileNameToRead = "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Research/datasets/AOTmix/aotm2011_playlists.json";
		String fileNameToWrite = "/run/media/gunjan/BoX1/LastFMSpaceSpace/AOTMSpace/Jul1/ProcessedATOM1Slim2.csv";
		
		int countOfLines = 0;
		int numOfPlaylists = -1;
		// level0Map = new LinkedHashMap<String, Object>();
		LinkedHashMap<Integer, String> level1Map = new LinkedHashMap<Integer, String>();
		LinkedHashMap<Integer, String> level2Map = new LinkedHashMap<Integer, String>();
		LinkedHashMap<Integer, String> level3Map = new LinkedHashMap<Integer, String>();
		
		LinkedHashMap<String, Long> songsMap = new LinkedHashMap<String, Long>();
		LinkedHashMap<String, Long> songsWithMSDIDMap = new LinkedHashMap<String, Long>();
		LinkedHashMap<String, Long> songsWithoutMSDIDMap = new LinkedHashMap<String, Long>();
		// String mixid = "";
		// String mixid = "";
		// int numOfSongs = -1;
		// int numOfSongWithMSDID = -1;
		// int sizeOfFilteredList = -1;
		// Timestamp tsOfCreation;
		// String category;
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(fileNameToRead));
			BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(fileNameToWrite);
			String lineRead;
			StringBuffer jsonStringBuf = new StringBuffer();
			StringBuffer toWriteMsg = new StringBuffer();
			while ((lineRead = br.readLine()) != null)
			{
				countOfLines += 1;
				// WritingToFile.appendLineToFileAbsolute(lineRead, "/run/media/gunjan/BoX1/LastFMSpaceSpace/AOTMSpace/Jul1/Log.txt");// ("line read = " + lineRead);
				jsonStringBuf.append(lineRead);
			}
			
			System.out.println("Num of lines read: " + countOfLines);
			JSONArray jsonArr = new JSONArray(jsonStringBuf.toString());
			System.out.println("Num of elements in JSON Array = " + jsonArr.length()); // num of playlist
			numOfPlaylists = jsonArr.length();
			
			toWriteMsg.append("mixid,username,numOfSongs,sizeOfFilteredList,tsOfCreation,category, numOfSongWithMSDID\n");// ,MSDIDSeqString,artistSongSeqString\n");
			for (int i = 0; i < jsonArr.length(); i++)/// *jsonArr.length(*/
			{
				JSONObject jObj = jsonArr.getJSONObject(i);// .toString());// .toString());
				
				Long mixid = jObj.getLong("mix_id");
				
				JSONArray songsArray = jObj.getJSONArray("playlist");
				JSONArray MSDIDArray = jObj.getJSONArray("filtered_lists");
				
				int numOfSongs = songsArray.length();
				
				int numOfSongWithMSDID = 0;
				
				int sizeOfFilteredList = MSDIDArray.length();
				String tsOfCreation = jObj.getString("timestamp");
				String artistSongSeqString = "";
				String MSDIDSeqString = "";
				
				String category = jObj.getString("category");
				
				JSONObject userObj = jObj.getJSONObject("user");
				String userName = userObj.getString("name").replaceAll(",", "-");
				// if (i == 101342)
				// // {
				// // System.out.println(jObj.toString());
				// {
				// System.out.println(mixid + "," + numOfSongs + "," + sizeOfFilteredList + "," + tsOfCreation + "," + category + "\n");
				// }
				for (int j = 0; j < songsArray.length(); j++)
				{
					JSONArray singleSongEntry = songsArray.getJSONArray(j);
					JSONArray singleSongArtistName = singleSongEntry.getJSONArray(0);
					
					String artistName = singleSongArtistName.get(0).toString();
					String songTitle = singleSongArtistName.get(1).toString();
					
					artistSongSeqString += "_" + artistName + "|" + songTitle;
					// System.out.println(artistSongSeqString);
					
					String MSDID = singleSongEntry.get(1).toString();
					
					if (MSDID.contains("null") == false)
					{
						numOfSongWithMSDID += 1;
					}
					
					MSDIDSeqString += ("_" + MSDID);
					// System.out.println(MSDIDSeqString);
					
					String key = (artistName + "|" + songTitle);
					if (songsMap.containsKey(key))
					{
						songsMap.put(key, songsMap.get(key) + 1);
					}
					else
						songsMap.put(key, (long) 1);
					// songsMap.put(artistSongSeqString, new Long(songsMap.getOrDefault(artistSongSeqString, 0)) + 1);
					
				}
				
				// System.out.println(mixid + "," + numOfSongs + "," + sizeOfFilteredList + "," + tsOfCreation + "," + category + ","
				// + numOfSongWithMSDID + "," + MSDIDSeqString + "," + artistSongSeqString + "\n");
				toWriteMsg.append(mixid + "," + userName + "," + numOfSongs + "," + sizeOfFilteredList + "," + tsOfCreation + "," + category
						+ "," + numOfSongWithMSDID + "\n");// + "," + MSDIDSeqString + "," + artistSongSeqString + "\n");
				
				// if (i % 2000 == 0)
				// {
				bw.write(toWriteMsg.toString());
				toWriteMsg.setLength(0);
				// }
				
			}
			// bw.write(toWriteMsg.toString());
			// toWriteMsg.setLength(0);
			// bw.flush();
			bw.close();
			br.close();
			
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return new Triple(level1Map, level2Map, level3Map);
	}
	
	public static void main2()// String args[])
	{
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		
		String fileNameToRead =
				"/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/gw2CheckinsSpots1Slim1TargetUsersDatesOnlyPerUserDateDWithLevels.csv";
		String fileNameToWrite =
				"/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/gw2CheckinsSpots1Slim1TargetUsersDatesOnlyPerUserDateDWithLevelsDiff.csv";
		int countOfLines = 0;
		StringBuffer sbuf = new StringBuffer();
		String lineRead;
		String currentUser = "NA", prevUser = "NA", currentDate = "NA", prevDate = "NA";
		
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(fileNameToRead));
			BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(fileNameToWrite);
			
			while ((lineRead = br.readLine()) != null)
			{
				countOfLines += 1;
				// int isLevel1 = 0, isLevel2 = 0, isLevel3 = 0;
				// int foundInLevels = 0;
				
				String diffOfDays = "NA";
				
				if (countOfLines == 1)
				{
					sbuf.append(
							"UserID,Date,NumOfDistinctCategoryIDs, NumOfCheckins,NumOfLevel1s,NumOfLevel2s,NumOfLevel3s, DayOfWeek, IsWeekEnd, IsWeekDay, NumOfDays ,DiffFromNextDay\n");
					continue;
				}
				
				String[] splittedLine = lineRead.split(",");
				// System.out.println("splittedLine[3] =" + splittedLine[3]);
				// System.out.println("splittedLine[1] =" + splittedLine[1]);
				// Integer catID = Integer.valueOf(splittedLine[3].replaceAll("\"", ""));
				
				currentDate = splittedLine[2];
				currentUser = splittedLine[1];
				
				// if (prevUser.equals("NA") && prevDate.equals("NA"))
				// {
				// diffOfDays = "NA";// UtilityBelt.getRoughDiffOfDates(date1, date2)
				// }
				
				if (currentUser.equals(prevUser) && prevUser.equals("NA") == false)
				{
					long rdif = DateTimeUtils.getRoughDiffOfDates(prevDate, currentDate);
					
					// System.out.println("rdif = " + rdif);
					diffOfDays = String.valueOf(DateTimeUtils.getRoughDiffOfDates(prevDate, currentDate));
				}
				
				// if (level1Map.containsKey(catID))
				// {
				// isLevel1 = 1;
				// foundInLevels++;
				// }
				// if (level2Map.containsKey(catID))
				// {
				// isLevel2 = 1;
				// foundInLevels++;
				// }
				//
				// if (level3Map.containsKey(catID))
				// {
				// isLevel3 = 1;
				// foundInLevels++;
				// }
				
				String startS = "";
				
				for (int i = 1; i <= 11; i++)
				{
					startS += splittedLine[i] + ",";
				}
				
				sbuf.append(startS + diffOfDays + "\n");
				
				prevUser = currentUser;
				prevDate = currentDate;
				
				// if (countOfLines % 4000 == 0)
				// {
				bw.write(sbuf.toString());
				sbuf.setLength(0);
				// }
			}
			
			br.close();
			bw.close();
			System.out.println("Num of lines read: " + (countOfLines));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public static void writeCatLevelInfo()
	{
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		
		Triple catLevelMaps = getCategoryMapsFromJSON();
		LinkedHashMap<Integer, String> level1Map = (LinkedHashMap<Integer, String>) catLevelMaps.getFirst();
		LinkedHashMap<Integer, String> level2Map = (LinkedHashMap<Integer, String>) catLevelMaps.getSecond();
		LinkedHashMap<Integer, String> level3Map = (LinkedHashMap<Integer, String>) catLevelMaps.getThird();
		
		String fileNameToRead = "/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/gw2CheckinsSpots1Slim1TargetUsersDatesOnly.csv";
		String fileNameToWrite =
				"/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/gw2CheckinsSpots1Slim1TargetUsersDatesOnlyWithLevelsV2.csv";
		int countOfLines = 0;
		StringBuffer sbuf = new StringBuffer();
		String lineRead;
		
		int l1Count = 0, l2Count = 0, l3Count = 0, notFoundInAnyLevel = 0;
		ArrayList<Integer> catIDsNotFoundInAnyLevel = new ArrayList<Integer>();
		// int lengthOfReadTokens = -1;
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(fileNameToRead));
			BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(fileNameToWrite);
			
			while ((lineRead = br.readLine()) != null)
			{
				countOfLines += 1;
				int isLevel1 = 0, isLevel2 = 0, isLevel3 = 0;
				int foundInLevels = 0;
				
				String[] splittedLine = lineRead.split(",");
				
				if (countOfLines == 1)
				{
					sbuf.append(splittedLine[1] + "," + splittedLine[2] + "," + splittedLine[3] + "," + splittedLine[4] + ","
							+ splittedLine[5] + ",IsLevel1,IsLevel2,IsLevel3\n");
					continue;
				}
				// System.out.println("splittedLine[3] =" + splittedLine[3]);
				// System.out.println("splittedLine[1] =" + splittedLine[1]);
				Integer catID = Integer.valueOf(splittedLine[3].replaceAll("\"", ""));
				
				if (level1Map.containsKey(catID))
				{
					isLevel1 = 1;
					foundInLevels++;
					l1Count++;
				}
				if (level2Map.containsKey(catID))
				{
					isLevel2 = 1;
					foundInLevels++;
					l2Count++;
				}
				
				if (level3Map.containsKey(catID))
				{
					isLevel3 = 1;
					foundInLevels++;
					l3Count++;
				}
				
				if (foundInLevels == 0)
				{
					catIDsNotFoundInAnyLevel.add(catID);
					notFoundInAnyLevel++;
				}
				
				if (foundInLevels > 1 && catID != 201)
				{
					System.err.println("Error: catID " + catID + " found in multiple levels " + isLevel1 + "," + isLevel3 + "," + isLevel3);
				}
				
				sbuf.append(splittedLine[1] + "," + splittedLine[2] + "," + splittedLine[3] + "," + splittedLine[4] + "," + splittedLine[5]
						+ "," + isLevel1 + "," + isLevel2 + "," + isLevel3 + "\n");
				
				// if (countOfLines % 4000 == 0)
				// {
				bw.write(sbuf.toString());
				sbuf.setLength(0);
				// }
			}
			
			bw.close();
			br.close();
			
			System.out.println("Num of checkins read: " + (countOfLines - 1));
			System.out.println("Num of level1 in checkins: " + l1Count);
			System.out.println("Num of level2 in checkins: " + l2Count);
			System.out.println("Num of level3 in checkins: " + l3Count);
			System.out.println("Num of checkins with catID in no levelMap: " + notFoundInAnyLevel);
			
			WritingToFile.appendLineToFileAbsolute(StringUtils.join(catIDsNotFoundInAnyLevel.toArray(), ","),
					"/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/CatsInNoMaps.csv");
			
			// catIDsNotFoundInAnyLevel
			// bw.write(sbuf.toString());
			// sbuf.setLength(0);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	// public static void main(String[] args)
	public static Triple getCategoryMapsFromJSON()
	{
		
		String fileNameToRead =
				"/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/Gowalla/link to Gowalla dataset/another source/gowalla/gowalla_category_structure.json";
		int countOfLines = -1;
		
		// level0Map = new LinkedHashMap<String, Object>();
		LinkedHashMap<Integer, String> level1Map = new LinkedHashMap<Integer, String>();
		LinkedHashMap<Integer, String> level2Map = new LinkedHashMap<Integer, String>();
		LinkedHashMap<Integer, String> level3Map = new LinkedHashMap<Integer, String>();
		
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(fileNameToRead));
			String lineRead;
			StringBuffer jsonStringBuf = new StringBuffer();
			while ((lineRead = br.readLine()) != null)
			{
				countOfLines += 1;
				jsonStringBuf.append(lineRead);
			}
			
			System.out.println("Num of lines read: " + countOfLines);
			
			JSONObject jObj = new JSONObject(jsonStringBuf.toString());
			
			// System.out.println(" Json string regenerated:" + jObj.toString());
			
			Set<String> level0Keys = jObj.keySet();
			// System.out.println("Num of level0Keys = " + level0Keys.size());
			if (level0Keys.size() != 1)
			{
				System.err.println("Error: incorrcet tree: level0Keys.size() =" + level0Keys.size());
			}
			
			for (String level0Key : level0Keys)
			{
				JSONArray level0Array = jObj.getJSONArray(level0Key);// JSONArray(jObj.get(level0Key).toString());
				System.out.println("level0: key=" + level0Key + ", has an array of size =" + level0Array.length());
				
				for (int i = 0; i < level0Array.length(); i++)
				{
					JSONObject level1Object = level0Array.getJSONObject(i);// new JSONObject(level0Array.get(i).toString());
					
					String[] urlSplitted = level1Object.get("url").toString().split("/");
					String catID1 = urlSplitted[urlSplitted.length - 1];
					System.out.println("\tlevel1: name = " + level1Object.get("name") + " , catID = " + catID1);
					
					if (level1Map.containsKey(catID1))
					{
						System.out.println("Alert in level1: catID " + catID1 + " already in level 1");
					}
					level1Map.put(Integer.valueOf(catID1), level1Object.get("name").toString());
					
					JSONArray level1Array = (JSONArray) level1Object.get("spot_categories");
					
					for (int j = 0; j < level1Array.length(); j++)
					{
						JSONObject level2Object = level1Array.getJSONObject(j);// new JSONObject(level0Array.get(i).toString());
						
						// System.out.print("\t\tlevel2: name = " + level2Object.get("name") + " , ");
						
						String[] urlSplitted2 = level2Object.get("url").toString().split("/");
						String catID2 = urlSplitted2[urlSplitted2.length - 1];
						System.out.println("\t\tlevel2: name = " + level2Object.get("name") + " , catID = " + catID2);
						
						if (level2Map.containsKey(catID2))
						{
							System.out.println("Alert in level2: catID " + catID2 + " already in level 2");
						}
						level2Map.put(Integer.valueOf(catID2), level2Object.get("name").toString());
						
						JSONArray level2Array = (JSONArray) level2Object.get("spot_categories");
						
						for (int k = 0; k < level2Array.length(); k++)
						{
							JSONObject level3Object = level2Array.getJSONObject(k);// new JSONObject(level0Array.get(i).toString());
							
							String[] urlSplitted3 = level3Object.get("url").toString().split("/");
							String catID3 = urlSplitted3[urlSplitted3.length - 1];
							System.out.println("\t\t\tlevel3: name = " + level3Object.get("name") + " ,  catID = " + catID3);
							
							if (level3Map.containsKey(catID3))
							{
								System.out.println("Alert in level3: catID " + catID3 + " already in level 3");
							}
							level3Map.put(Integer.valueOf(catID3), level3Object.get("name").toString());
							// if (level3Object.get("spot_categories") != null)
							// System.out
							// .println("Alert: Level 4 detected " + ((JSONArray) level3Object.get("spot_categories")).toString());
						}
					}
					
				}
			}
			
			System.out.println("Num of level 1 catIDs = " + level1Map.size());
			System.out.println("Num of level 2 catIDs = " + level2Map.size());
			System.out.println("Num of level 3 catIDs = " + level3Map.size());
			
			Set<Integer> l1Keys = level1Map.keySet();
			Set<Integer> l2Keys = level2Map.keySet();
			Set<Integer> l3Keys = level3Map.keySet();
			
			System.out.println("Intersection of l1Keys and l2Keys" + getIntersection(l1Keys, l2Keys).size());
			System.out.println("Intersection of l2Keys and l3Keys" + getIntersection(l2Keys, l3Keys).size());
			System.out.println("Intersection of l1Keys and l3Keys" + getIntersection(l1Keys, l3Keys).size());
			// System.out.println("Traversing level3map");
			// for (Map.Entry<Integer, String> e : level3Map.entrySet())
			// {
			// System.out.println(e.getKey() + "," + e.getValue());
			// }
			
			// for (Object level2Obj : s0Array)
			// {
			//
			// }
			
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return new Triple(level1Map, level2Map, level3Map);
	}
	
	public static Set getIntersection(Set s1, Set s2)
	{
		Set intersection = new HashSet(s1);
		intersection.retainAll(s2);
		
		return intersection;
	}
	// public static
}
