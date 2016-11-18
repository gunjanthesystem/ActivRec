package org.activity.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.activity.io.WritingToFile;
import org.activity.objects.Pair;
import org.activity.objects.Triple;
import org.activity.util.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javafx.scene.control.TreeItem;

/**
 * CURRENTLY USED 6 sep, 2016. 26 AUG 9PM
 * 
 * @author gunjan
 *
 */
public class JSONProcessingGowallaTryingNonStatic implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	String commonPath = "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep1/";
	
	TreeMap<Integer, String> catIDNameDictionary;
	Map<String, TreeMap<Integer, Long>> checkinCountResultsTogether;
	TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> categoryHierarchyMap;
	TreeItem<String> rootOfCategoryHierarchyTree;
	
	public String getCommonPath()
	{
		return commonPath;
	}
	
	/**
	 * 
	 * @param catIDNames3Levels
	 * @param catIDNamesInNoLevel
	 */
	public void setCatIDNameDictionary(
			Triple<LinkedHashMap<Integer, String>, LinkedHashMap<Integer, String>, LinkedHashMap<Integer, String>> catIDNames3Levels,
			TreeMap<Integer, String> catIDNamesInNoLevel)
	{
		catIDNameDictionary = new TreeMap<Integer, String>();
		
		LinkedHashMap<Integer, String> level1CatIDName = (LinkedHashMap<Integer, String>) catIDNames3Levels.getFirst();
		LinkedHashMap<Integer, String> level2CatIDName = (LinkedHashMap<Integer, String>) catIDNames3Levels.getSecond();
		LinkedHashMap<Integer, String> level3CatIDName = (LinkedHashMap<Integer, String>) catIDNames3Levels.getThird();
		
		// put call cat id, name present in hierarchy tree, i.e., present in the three levels of hierarchy
		catIDNameDictionary.putAll(level1CatIDName);
		catIDNameDictionary.putAll(level2CatIDName);
		catIDNameDictionary.putAll(level3CatIDName);
		catIDNameDictionary.putAll(catIDNamesInNoLevel);
	}
	
	public TreeMap<Integer, String> getCatIDNameDictionary()
	{
		return catIDNameDictionary;
	}
	
	/**
	 * 
	 * @return
	 */
	public String toStringCatIDNameDictionary()
	{
		return catIDNameDictionary.entrySet().stream().map(entry -> entry.getKey() + "-" + entry.getValue())
				.collect(Collectors.joining(",\n"));
	}
	
	/**
	 * 
	 * @param catID
	 * @param catName
	 */
	public void updateCatIDNameDictionary(Integer catID, String catName)
	{
		if (catIDNameDictionary.containsKey(catID) == false)
		{
			catIDNameDictionary.put(catID, catName);
		}
	}
	
	/**
	 * 
	 * @return a map of map where the inner maps contains key as cat id at particular level and values as num of checkins for that catid. the innermaps are "level1CheckinCountMap,
	 *         level2CheckinCountMap, level3CheckinCountMap, noneLevelCheckinCountMap, level1OverallDistribution"
	 * 
	 *         noneLevelCheckinCountMap is a map with key as cat ids which are in checkins but not in any of the levels of hierarchy by Gowalla, and value as the number of times
	 *         each of those catids occur in checkins
	 */
	public Map<String, TreeMap<Integer, Long>> getCheckinCountResultsTogether()
	{
		return checkinCountResultsTogether;
	}
	
	public TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> getCategoryHierarchyMap()
	{
		return categoryHierarchyMap;
	}
	
	public TreeItem<String> getRootOfCategoryHierarchyTree()
	{
		return rootOfCategoryHierarchyTree;
	}
	
	public static void main(String args[])
	{
		// getCategoryMapsFromJSON();
		// getCategoryMapsFromJSON2();
		// writeCatLevelInfo3();// getCategoryMapsFromJSON2());
		// writeDiffFromNextDay();
		new JSONProcessingGowallaTryingNonStatic();
	}
	
	public JSONProcessingGowallaTryingNonStatic()
	{
		new JSONProcessingGowallaTryingNonStatic(commonPath);
	}
	
	/**
	 * 
	 * @param commonPathToWrite
	 */
	public JSONProcessingGowallaTryingNonStatic(String commonPathToWrite)
	{
		this.commonPath = commonPathToWrite;
		// $$PrintStream consoleLogStream = WritingToFile.redirectConsoleOutput(commonPath + "consoleLog.txt");
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		
		String catHierarchyFileNameToRead =
				"/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/Gowalla/link to Gowalla dataset/another source/gowalla/gowalla_category_structure.json";
		String checkinFileNameToRead = "/run/media/gunjan/BoX2/GowallaSpaceSpace/Aug22_2016/gw2CheckinsSpots1TargetUsersDatesOnly.csv";/// gw2CheckinsSpots1Slim1TargetUsersDatesOnly.csv";
		String checkinFileNameToWrite =
				"/run/media/gunjan/BoX2/GowallaSpaceSpace/Aug22_2016/gw2CheckinsSpots1TargetUsersDatesOnlyWithLevels.csv";
		String fileNameToWriteCatLevelDistro = commonPath + "gw2CheckinsSpots1Slim1TargetUsersDatesOnlyCatLevelDistributionJava_";
		
		try
		{
			Triple<LinkedHashMap<Integer, String>, LinkedHashMap<Integer, String>, LinkedHashMap<Integer, String>> catIDNamesFor3Levels =
					getCatIDNamesForEachLevelFromJSON();
			
			LinkedHashMap<Integer, String> level1CatIDNames = (LinkedHashMap<Integer, String>) catIDNamesFor3Levels.getFirst();
			LinkedHashMap<Integer, String> level2CatIDNames = (LinkedHashMap<Integer, String>) catIDNamesFor3Levels.getSecond();
			LinkedHashMap<Integer, String> level3CatIDNames = (LinkedHashMap<Integer, String>) catIDNamesFor3Levels.getThird();
			
			// TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> catHierarchyMap =
			// getThreeLevelCategoryHierarchyFromJSON(catHierarchyFileNameToRead);
			Pair<TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>>, TreeItem<String>> hierarchyProcessingResult =
					getThreeLevelCategoryHierarchyTreeFromJSON(catHierarchyFileNameToRead);
			
			categoryHierarchyMap = hierarchyProcessingResult.getFirst();
			rootOfCategoryHierarchyTree = hierarchyProcessingResult.getSecond();
			
			Pair<Map<String, TreeMap<Integer, Long>>, TreeMap<Integer, String>> preProcessingResult = processCheckinData(level1CatIDNames,
					level2CatIDNames, level3CatIDNames, categoryHierarchyMap, checkinFileNameToRead, checkinFileNameToWrite);
			checkinCountResultsTogether = preProcessingResult.getFirst();
			
			TreeMap<Integer, String> noneLevelCatIDNames = preProcessingResult.getSecond();
			
			setCatIDNameDictionary(catIDNamesFor3Levels, noneLevelCatIDNames);
			
			// writeCheckInDistributionOverCatIDs(checkinCountResultsTogether.get("level1CheckinCountMap"),
			// checkinCountResultsTogether.get("level2CheckinCountMap"), checkinCountResultsTogether.get("level3CheckinCountMap"),
			// checkinCountResultsTogether.get("noneLevelCheckinCountMap"),
			// checkinCountResultsTogether.get("level1OverallDistribution"), fileNameToWriteCatLevelDistro);
			System.out.print("catIDNameDictionary.size:" + catIDNameDictionary.size() + "\n");
			System.out.println(toStringCatIDNameDictionary());
			System.out.println("Exiting JSONProcessingGowallaTryingNonStatic()");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			// $$consoleLogStream.close();
		}
	}
	
	/**
	 * Iterate through the checkin data and generate count maps for level1, level2, level3 and nonelevel catids.
	 * 
	 * @param level1Map
	 * @param level2Map
	 * @param level3Map
	 * @param catHierarchyMap
	 * @param checkinFileNameToRead
	 * @param checkinFileNameToWrite
	 * @return A pair where the first element is:</br>
	 *         - a map of map where the inner maps contains key as cat id at particular level and values as num of checkins for that catid. the innermaps are
	 *         "level1CheckinCountMap, level2CheckinCountMap, level3CheckinCountMap, noneLevelCheckinCountMap, level1OverallDistribution" </br>
	 *         - noneLevelCheckinCountMap is a map with key as cat ids which are in checkins but not in any of the levels of hierarchy by Gowalla, and value as the number of times
	 *         each of those catids occur in checkins </br>
	 *         </br>
	 *         and the second element is:</br>
	 *         - a map of (catid,name**) for categories not in any of the levels, to be added to catid,name dictionary (note: ** added to these cat names to mark them as ones not
	 *         in category hierarchy.)
	 */
	public static Pair<Map<String, TreeMap<Integer, Long>>, TreeMap<Integer, String>> processCheckinData(
			LinkedHashMap<Integer, String> level1Map, LinkedHashMap<Integer, String> level2Map, LinkedHashMap<Integer, String> level3Map,
			TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> catHierarchyMap, String checkinFileNameToRead,
			String checkinFileNameToWrite)// earlier called writeCatLevelInfo3()
	{
		
		////////////////////////////////
		TreeMap<Integer, Long> level1CheckinCountMap = new TreeMap<Integer, Long>();// key: checkin cat id in level 1, value: num of checkins
		TreeMap<Integer, Long> level2CheckinCountMap = new TreeMap<Integer, Long>();// key: checkin cat id in level 2, value: num of checkins
		TreeMap<Integer, Long> level3CheckinCountMap = new TreeMap<Integer, Long>();// key: checkin cat id in level 3, value: num of checkins
		TreeMap<Integer, Long> noneLevelCheckinCountMap = new TreeMap<Integer, Long>();// key: checkin cat id in none of the levels, value: num of checkins
		TreeMap<Integer, String> noneLevelCatIDNameMap = new TreeMap<Integer, String>();// key: checkin cat id in none of the levels, value: name. used for updating dictionary
		// ** added at the end of cat name which was not in any of the levels
		////////////////////////////////
		
		////////////////////////////////
		TreeMap<Integer, Long> level1OverallDistribution = new TreeMap<Integer, Long>();
		TreeMap<Integer, Long> level2OverallDistribution = new TreeMap<Integer, Long>();
		TreeMap<Integer, Long> level3OverallDistribution = new TreeMap<Integer, Long>();
		////////////////////////////////
		
		// "/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/gw2CheckinsSpots1Slim1TargetUsersDatesOnlyWithLevelsV2_3.csv";
		
		// String fileNameToWriteCatLevelDistro = commonPath + "gw2CheckinsSpots1Slim1TargetUsersDatesOnlyCatLevelDistributionJava_";
		
		int countOfLines = 0;
		StringBuffer sbuf = new StringBuffer();
		String lineRead;
		
		int l1Count = 0, l2Count = 0, l3Count = 0, notFoundInAnyLevelCount = 0;
		int foundInFlatMap = 0;
		ArrayList<Integer> notFoundInFlatMap = new ArrayList<Integer>();
		ArrayList<Integer> catIDsNotFoundInAnyLevel = new ArrayList<Integer>();
		// int lengthOfReadTokens = -1;
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(checkinFileNameToRead));
			BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(checkinFileNameToWrite);
			
			while ((lineRead = br.readLine()) != null)
			{
				countOfLines += 1;
				int isLevel1 = 0, isLevel2 = 0, isLevel3 = 0;
				// int countFoundInLevels = 0;
				
				ArrayList<Integer> level1IDs, level2IDs, level3IDs; // IDs of current, parent and grandparent.// allowing for multiple parents and grandparents.
				
				String[] splittedLine = lineRead.split(",");
				
				if (countOfLines == 1) // skip the first
				{
					continue;
				}
				
				Integer catID = Integer.valueOf(splittedLine[7].replaceAll("\"", ""));
				
				if (level1Map.containsKey(catID))
				{
					isLevel1 = 1;
					// countFoundInLevels++;
					l1Count++;
					level1CheckinCountMap.put(catID, level1CheckinCountMap.getOrDefault(catID, new Long(0)) + 1);
				}
				else if (level2Map.containsKey(catID))
				{
					isLevel2 = 1;
					// countFoundInLevels++;
					l2Count++;
					level2CheckinCountMap.put(catID, level2CheckinCountMap.getOrDefault(catID, new Long(0)) + 1);
				}
				
				else if (level3Map.containsKey(catID))
				{
					isLevel3 = 1;
					// countFoundInLevels++;
					l3Count++;
					level3CheckinCountMap.put(catID, level3CheckinCountMap.getOrDefault(catID, new Long(0)) + 1);
				}
				
				if ((isLevel1 + isLevel2 + isLevel3) == 0) // does not belong in any of the levels
				{
					catIDsNotFoundInAnyLevel.add(catID);
					notFoundInAnyLevelCount++;
					noneLevelCheckinCountMap.put(catID, noneLevelCheckinCountMap.getOrDefault(catID, new Long(0)) + 1);
					String catName = String.valueOf(splittedLine[8].replaceAll("\"", ""));
					noneLevelCatIDNameMap.put(catID, catName + "**");
				}
				
				if ((isLevel1 + isLevel2 + isLevel3) > 1)// && catID != 201)
				{
					System.err.println("Warning: catID " + catID + " found in multiple levels , level1Found=" + isLevel1 + ",level2Found="
							+ isLevel2 + ",level3Found=" + isLevel3);
				}
				
				boolean isInCategoryHierarchy = false;
				
				// if (foundInLevels > 0)
				// {
				// // then search it in the category hierarchy tree
				// if()
				//
				// }
				//
				// for (Integer level1KeyID : catHierarchyMap.keySet())
				// {
				// if (isInCategoryHierarchy == true)
				// {
				// // break;
				// }
				//
				// if (catID == level1KeyID) // is level 1 ID
				// {
				// long count = 0;
				// if (level1OverallDistribution.get(level1KeyID) != null)
				// {
				// count = level1OverallDistribution.get(level1KeyID);
				// }
				// level1OverallDistribution.put(level1KeyID, count + 1);
				//
				// isInCategoryHierarchy = true;
				// foundInFlatMap++;
				// break;
				// }
				//
				// for (Integer childID : catHierarchyMap.get(level1KeyID)) // is child of level1 id
				// {
				// if (catID == childID)
				// {
				// long count = 0;
				// if (level1OverallDistribution.get(level1KeyID) != null)
				// {
				// count = level1OverallDistribution.get(level1KeyID);
				// }
				// level1OverallDistribution.put(level1KeyID, count + 1);
				//
				// isInCategoryHierarchy = true;
				// foundInFlatMap++;
				// break;
				// }
				// }
				// }
				
				if (!isInCategoryHierarchy)
				{
					notFoundInFlatMap.add(catID);
				}
				
			}
			
			System.out.println("-----------------------------------------");
			System.out.println("Num of lines read = " + countOfLines);
			
			System.out.println("Num of level 1 catids in checkins = " + level1CheckinCountMap.size());
			System.out.println("Num of level 2 catids in checkins = " + level2CheckinCountMap.size());
			System.out.println("Num of level 3 catids in checkins = " + level3CheckinCountMap.size());
			System.out.println("Num of catids not in any levels in checkins = " + noneLevelCheckinCountMap.size());
			
			System.out.println("Num of checkins at level 1 = " + l1Count);
			System.out.println("Num of checkins at level 2 = " + l2Count);
			System.out.println("Num of checkins at level 3 = " + l3Count);
			System.out.println("Num of checkins not found in any levels (i.e., not in category hierarchy) = " + notFoundInAnyLevelCount);
			
			// System.out.println("Num of checkins found at one of the levels = " + foundInFlatMap);
			// System.out.println("Num of checkins not found in hierarchy map = " + notFoundInFlatMap.size());
			System.out.println("-----------------------------------------");
		} // end
			// of
			// try
		catch (
		
		Exception e)
		{
			e.printStackTrace();
		}
		
		// writeCheckInDistributionOverCatIDs(level1CheckinCountMap, level2CheckinCountMap, level3CheckinCountMap, noneLevelCheckinCountMap,
		// level1OverallDistribution, fileNameToWriteCatLevelDistro); //removed from here to make methods more functional, less sideeffects.
		
		Map<String, TreeMap<Integer, Long>> countResultsTogether = new LinkedHashMap<>();
		
		countResultsTogether.put("level1CheckinCountMap", level1CheckinCountMap);
		countResultsTogether.put("level2CheckinCountMap", level2CheckinCountMap);
		countResultsTogether.put("level3CheckinCountMap", level3CheckinCountMap);
		countResultsTogether.put("noneLevelCheckinCountMap", noneLevelCheckinCountMap);
		countResultsTogether.put("level1OverallDistribution", level1OverallDistribution);
		
		return new Pair(countResultsTogether, noneLevelCatIDNameMap);
		// return noneLevelCheckinCountMap;
	}
	
	/**
	 * 
	 * @param commonPath
	 */
	public static void writeCatLevelInfo2(String commonPath)
	{
		PrintStream consoleLogStream = WritingToFile.redirectConsoleOutput(commonPath + "consoleLog.txt");
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		
		Triple catLevelMaps = getCatIDNamesForEachLevelFromJSON();
		
		LinkedHashMap<Integer, TreeSet<Integer>> flatMapLevel1 = getTwoLevelCategoryHierarchyFromJSON();
		TreeMap<Integer, Long> level1OverallDistribution = new TreeMap<Integer, Long>();
		
		LinkedHashMap<Integer, String> level1Map = (LinkedHashMap<Integer, String>) catLevelMaps.getFirst();
		LinkedHashMap<Integer, String> level2Map = (LinkedHashMap<Integer, String>) catLevelMaps.getSecond();
		LinkedHashMap<Integer, String> level3Map = (LinkedHashMap<Integer, String>) catLevelMaps.getThird();
		
		TreeMap<Integer, Long> level1CheckinCountMap = new TreeMap<Integer, Long>();
		TreeMap<Integer, Long> level2CheckinCountMap = new TreeMap<Integer, Long>();
		TreeMap<Integer, Long> level3CheckinCountMap = new TreeMap<Integer, Long>();
		TreeMap<Integer, Long> noneLevelCheckinCountMap = new TreeMap<Integer, Long>();
		
		String fileNameToRead = "/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/gw2CheckinsSpots1Slim1TargetUsersDatesOnly.csv";
		// String fileNameToWrite =
		// "/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/gw2CheckinsSpots1Slim1TargetUsersDatesOnlyWithLevelsV2_3.csv";
		
		String fileNameToWriteCatLevelDistro = commonPath + "gw2CheckinsSpots1Slim1TargetUsersDatesOnlyCatLevelDistributionJava_";
		
		int countOfLines = 0;
		StringBuffer sbuf = new StringBuffer();
		String lineRead;
		
		int l1Count = 0, l2Count = 0, l3Count = 0, notFoundInAnyLevel = 0;
		int foundInFlatMap = 0;
		ArrayList<Integer> notFoundInFlatMap = new ArrayList<Integer>();
		ArrayList<Integer> catIDsNotFoundInAnyLevel = new ArrayList<Integer>();
		// int lengthOfReadTokens = -1;
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(fileNameToRead));
			// BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(fileNameToWrite);
			
			while ((lineRead = br.readLine()) != null)
			{
				countOfLines += 1;
				int isLevel1 = 0, isLevel2 = 0, isLevel3 = 0;
				int foundInLevels = 0;
				
				String[] splittedLine = lineRead.split(",");
				
				if (countOfLines == 1)
				{
					// sbuf.append(splittedLine[1] + "," + splittedLine[2] + "," + splittedLine[3] + "," + splittedLine[4] + ","
					// + splittedLine[5] + ",IsLevel1,IsLevel2,IsLevel3\n");
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
					level1CheckinCountMap.put(catID, level1CheckinCountMap.getOrDefault(catID, new Long(0)) + 1);
				}
				else if (level2Map.containsKey(catID))
				{
					isLevel2 = 1;
					foundInLevels++;
					l2Count++;
					level2CheckinCountMap.put(catID, level2CheckinCountMap.getOrDefault(catID, new Long(0)) + 1);
				}
				
				else if (level3Map.containsKey(catID))
				{
					isLevel3 = 1;
					foundInLevels++;
					l3Count++;
					level3CheckinCountMap.put(catID, level3CheckinCountMap.getOrDefault(catID, new Long(0)) + 1);
				}
				
				if (foundInLevels == 0)
				{
					catIDsNotFoundInAnyLevel.add(catID);
					notFoundInAnyLevel++;
					noneLevelCheckinCountMap.put(catID, noneLevelCheckinCountMap.getOrDefault(catID, new Long(0)) + 1);
				}
				
				if (foundInLevels > 1 && catID != 201)
				{
					System.err.println("Error: catID " + catID + " found in multiple levels " + isLevel1 + "," + isLevel3 + "," + isLevel3);
				}
				
				boolean isInFlatMap = false;
				for (Integer level1KeyID : flatMapLevel1.keySet())
				{
					if (isInFlatMap == true)
					{
						break;
					}
					if (catID == level1KeyID) // is level 1 ID
					{
						long count = 0;
						if (level1OverallDistribution.get(level1KeyID) != null)
						{
							count = level1OverallDistribution.get(level1KeyID);
						}
						level1OverallDistribution.put(level1KeyID, count + 1);
						
						isInFlatMap = true;
						foundInFlatMap++;
						break;
					}
					
					for (Integer childID : flatMapLevel1.get(level1KeyID)) // is child of level1 id
					{
						if (catID == childID)
						{
							long count = 0;
							if (level1OverallDistribution.get(level1KeyID) != null)
							{
								count = level1OverallDistribution.get(level1KeyID);
							}
							level1OverallDistribution.put(level1KeyID, count + 1);
							
							isInFlatMap = true;
							foundInFlatMap++;
							break;
						}
					}
				}
				
				if (!isInFlatMap)
				{
					notFoundInFlatMap.add(catID);
				}
				
			}
			System.out.println("Num of checkins found in hierarchy map = " + foundInFlatMap);
			System.out.println("Num of checkins not found in hierarchy map = " + notFoundInFlatMap.size());
			System.out.println("Num of lines read = " + countOfLines);
		} // end of try
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			consoleLogStream.close();
		}
		
		writeCheckInDistributionOverCatIDs(level1CheckinCountMap, level2CheckinCountMap, level3CheckinCountMap, noneLevelCheckinCountMap,
				level1OverallDistribution, fileNameToWriteCatLevelDistro);
		
	}
	
	/**
	 * 
	 */
	public static void writeCatLevelInfo()
	{
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		
		Triple catLevelMaps = getCatIDNamesForEachLevelFromJSON();
		
		LinkedHashMap<Integer, String> level1Map = (LinkedHashMap<Integer, String>) catLevelMaps.getFirst();
		LinkedHashMap<Integer, String> level2Map = (LinkedHashMap<Integer, String>) catLevelMaps.getSecond();
		LinkedHashMap<Integer, String> level3Map = (LinkedHashMap<Integer, String>) catLevelMaps.getThird();
		
		TreeMap<Integer, Long> level1CkeckinCountMap = new TreeMap<Integer, Long>();// )
		TreeMap<Integer, Long> level2CkeckinCountMap = new TreeMap<Integer, Long>();
		TreeMap<Integer, Long> level3CkeckinCountMap = new TreeMap<Integer, Long>();
		TreeMap<Integer, Long> noneLevelCkeckinCountMap = new TreeMap<Integer, Long>();
		
		String fileNameToRead = "/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/gw2CheckinsSpots1Slim1TargetUsersDatesOnly.csv";
		String fileNameToWrite =
				"/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/gw2CheckinsSpots1Slim1TargetUsersDatesOnlyWithLevelsV2_2.csv";
		
		String fileNameToWriteCatLevelDistro =
				"/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/gw2CheckinsSpots1Slim1TargetUsersDatesOnlyCatLevelDistro";
		
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
					level1CkeckinCountMap.put(catID, level1CkeckinCountMap.getOrDefault(catID, new Long(0)) + 1);
				}
				if (level2Map.containsKey(catID))
				{
					isLevel2 = 1;
					foundInLevels++;
					l2Count++;
					level2CkeckinCountMap.put(catID, level2CkeckinCountMap.getOrDefault(catID, new Long(0)) + 1);
				}
				
				if (level3Map.containsKey(catID))
				{
					isLevel3 = 1;
					foundInLevels++;
					l3Count++;
					level3CkeckinCountMap.put(catID, level3CkeckinCountMap.getOrDefault(catID, new Long(0)) + 1);
				}
				
				if (foundInLevels == 0)
				{
					catIDsNotFoundInAnyLevel.add(catID);
					notFoundInAnyLevel++;
					noneLevelCkeckinCountMap.put(catID, noneLevelCkeckinCountMap.getOrDefault(catID, new Long(0)) + 1);
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
			
			// writeCheckInDistributionOverCatIDs(level1CkeckinCountMap, level2CkeckinCountMap, level3CkeckinCountMap,
			// noneLevelCkeckinCountMap, fileNameToWriteCatLevelDistro);
			// catIDsNotFoundInAnyLevel
			// bw.write(sbuf.toString());
			// sbuf.setLength(0);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @param level1CkeckinCountMap
	 * @param level2CkeckinCountMap
	 * @param level3CkeckinCountMap
	 * @param noneLevelCkeckinCountMap
	 * @param level1OverallDistribution
	 * @param absFileNameToUse
	 */
	public static void writeCheckInDistributionOverCatIDs(TreeMap<Integer, Long> level1CkeckinCountMap,
			TreeMap<Integer, Long> level2CkeckinCountMap, TreeMap<Integer, Long> level3CkeckinCountMap,
			TreeMap<Integer, Long> noneLevelCkeckinCountMap, TreeMap<Integer, Long> level1OverallDistribution, String absFileNameToUse)// , String userName)
	{
		try
		{
			BufferedWriter bwL1 = WritingToFile.getBufferedWriterForNewFile(absFileNameToUse + "L1.csv");
			BufferedWriter bwL2 = WritingToFile.getBufferedWriterForNewFile(absFileNameToUse + "L2.csv");
			BufferedWriter bwL3 = WritingToFile.getBufferedWriterForNewFile(absFileNameToUse + "L3.csv");
			BufferedWriter bwNone = WritingToFile.getBufferedWriterForNewFile(absFileNameToUse + "None.csv");
			BufferedWriter overallLevel1 = WritingToFile.getBufferedWriterForNewFile(absFileNameToUse + "OverallLevel1.csv");
			
			ArrayList<BufferedWriter> allBWToWrite = new ArrayList<BufferedWriter>();
			allBWToWrite.add(bwL1);
			allBWToWrite.add(bwL2);
			allBWToWrite.add(bwL3);
			allBWToWrite.add(bwNone);
			allBWToWrite.add(overallLevel1);
			
			String header = "CatID, NumOfCheckIns\n";
			
			for (BufferedWriter bw : allBWToWrite)
			{
				bw.write(header);
			}
			
			for (Entry<Integer, Long> entry : level1CkeckinCountMap.entrySet())
			{
				bwL1.write(entry.getKey() + "," + entry.getValue() + "\n");
			}
			for (Entry<Integer, Long> entry : level2CkeckinCountMap.entrySet())
			{
				bwL2.write(entry.getKey() + "," + entry.getValue() + "\n");
			}
			for (Entry<Integer, Long> entry : level3CkeckinCountMap.entrySet())
			{
				bwL3.write(entry.getKey() + "," + entry.getValue() + "\n");
			}
			for (Entry<Integer, Long> entry : noneLevelCkeckinCountMap.entrySet())
			{
				bwNone.write(entry.getKey() + "," + entry.getValue() + "\n");
			}
			
			for (Entry<Integer, Long> entry : level1OverallDistribution.entrySet())
			{
				overallLevel1.write(entry.getKey() + "," + entry.getValue() + "\n");
			}
			
			// for (Map.Entry<String, Integer> entry : ts.entrySet())
			// {
			// bw.write(entry.getKey() + "," + entry.getValue() + "\n");
			// }
			for (BufferedWriter bw : allBWToWrite)
			{
				bw.close();
			}
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// public static void main(String[] args)
	/**
	 * Get the three linkedhashmaps, one for each of the three category levels. The map contains (catergryID,categoryName) for that level.
	 * 
	 * @return a Triple containing the three hashmaps.
	 */
	public static Triple<LinkedHashMap<Integer, String>, LinkedHashMap<Integer, String>, LinkedHashMap<Integer, String>>
			getCatIDNamesForEachLevelFromJSON()
	{
		
		String fileNameToRead =
				"/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/Gowalla/link to Gowalla dataset/another source/gowalla/gowalla_category_structure.json";
		int countOfLines = -1;
		
		System.out.println("Entering getCategoryLevelsMapsFromJSON");
		// level0Map = new LinkedHashMap<String, Object>();
		// <CatID,NameOfCategory>
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
				System.err.println("Error: incorrect tree: level0Keys.size() =" + level0Keys.size());
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
							// System.out.println(
							// "Super Alert: Level 4 detected " + ((JSONArray) level3Object.get("spot_categories")).toString());
						}
					}
					
				}
			}
			
			System.out.println("===========================");
			System.out.println("Num of level 1 catIDs = " + level1Map.size());
			System.out.println("Num of level 2 catIDs = " + level2Map.size());
			System.out.println("Num of level 3 catIDs = " + level3Map.size());
			
			Set<Integer> l1Keys = level1Map.keySet();
			Set<Integer> l2Keys = level2Map.keySet();
			Set<Integer> l3Keys = level3Map.keySet();
			
			System.out.println("Intersection of l1Keys and l2Keys = " + Arrays.toString(getIntersection(l1Keys, l2Keys).toArray()));
			System.out.println("Intersection of l2Keys and l3Keys =" + Arrays.toString(getIntersection(l2Keys, l3Keys).toArray()));
			System.out.println("Intersection of l1Keys and l3Keys =" + Arrays.toString(getIntersection(l1Keys, l3Keys).toArray()));
			// Arrays.toString(children.toArray()
			System.out.println("Total num of catIDs = " + (level1Map.size() + level2Map.size() + level3Map.size()));
			
			HashMap<Integer, String> allUniques = new HashMap<Integer, String>();
			allUniques.putAll(level1Map);
			allUniques.putAll(level2Map);
			allUniques.putAll(level3Map);
			
			System.out.println("Total num of unique catIDs = " + (allUniques.size()));
			System.out.println("===========================");
			
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
		
		System.out.println("Exiting getCategoryLevelsMapsFromJSON\n");
		return new Triple<LinkedHashMap<Integer, String>, LinkedHashMap<Integer, String>, LinkedHashMap<Integer, String>>(level1Map,
				level2Map, level3Map);
	}
	
	/**
	 * Returns the category hierarchy as map with keys as level 1 ids and children as level 2 and level 3 ids. Note: this does not distinguish between a level 2 and level 3 child
	 * if a level 1.
	 * 
	 * @return keys are level 1 catid, and values are level2 and level3 children for correspnding level 1 catids.
	 */
	public static LinkedHashMap<Integer, TreeSet<Integer>> getTwoLevelCategoryHierarchyFromJSON()
	{
		
		String fileNameToRead =
				"/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/Gowalla/link to Gowalla dataset/another source/gowalla/gowalla_category_structure.json";
		int countOfLines = -1;
		
		System.out.println("Inside getTwoLevelCategoryHierarchyFromJSON()");
		/**
		 * keys are level 1 catid, and values are level2 and level3 children for correspnding level 1 catids.
		 **/
		LinkedHashMap<Integer, TreeSet<Integer>> flatMapLevel1 = new LinkedHashMap<Integer, TreeSet<Integer>>();
		
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
				System.err.println("Error: incorrect tree: level0Keys.size() =" + level0Keys.size());
			}
			
			for (String level0Key : level0Keys) // only 1 key : spot_categories
			{
				JSONArray level0Array = jObj.getJSONArray(level0Key); // array with first element as Community
				System.out.println("level0: key=" + level0Key + ", has an array of size =" + level0Array.length());
				// level0: key=spot_categories, has an array of size =7
				
				for (int i = 0; i < level0Array.length(); i++)// 7
				{
					JSONObject level1Object = level0Array.getJSONObject(i);// new JSONObject(level0Array.get(i).toString());
					
					String[] urlSplitted = level1Object.get("url").toString().split("/");
					Integer catID1 = Integer.valueOf(urlSplitted[urlSplitted.length - 1]);
					System.out.println("\tlevel1: name = " + level1Object.get("name") + " , catID = " + catID1);
					// level1: name = Community , catID = 934
					
					TreeSet<Integer> childrenOfThisLevel1ID = new TreeSet<Integer>();
					// flatMapLevel1.put(catID1, new ArrayList<Integer>()); // adding the level1 key and empty children list, note: level1 id will appear before any of its children
					// in the hierarchy file being read
					
					if (flatMapLevel1.containsKey(catID1))
					{
						System.out.println("Alert in level1: catID " + catID1 + " already in level 1");
					}
					
					JSONArray level1Array = (JSONArray) level1Object.get("spot_categories"); // of level 1 object e.g.,community
					
					for (int j = 0; j < level1Array.length(); j++)
					{
						JSONObject level2Object = level1Array.getJSONObject(j);// e.g. Campus spot object
						
						// System.out.print("\t\tlevel2: name = " + level2Object.get("name") + " , ");
						
						String[] urlSplitted2 = level2Object.get("url").toString().split("/");
						Integer catID2 = Integer.valueOf(urlSplitted2[urlSplitted2.length - 1]);
						System.out.println("\t\tlevel2: name = " + level2Object.get("name") + " , catID = " + catID2);
						// level2: name = Campus Spot , catID = 133
						if (childrenOfThisLevel1ID.contains(catID2))
						{
							System.out
									.print("Alert! catID2 = " + catID2 + " is mentioned multiple times for level 1 catID " + catID1 + "\n");
						}
						childrenOfThisLevel1ID.add(catID2);
						//
						// if (level2Map.containsKey(catID2))
						// {
						// System.out.println("Alert in level2: catID " + catID2 + " already in level 2");
						// }
						// level2Map.put(Integer.valueOf(catID2), level2Object.get("name").toString());
						
						JSONArray level2Array = (JSONArray) level2Object.get("spot_categories"); // of level 2 object e.g.,Campus spot
						
						for (int k = 0; k < level2Array.length(); k++)
						{
							JSONObject level3Object = level2Array.getJSONObject(k);// new JSONObject(level0Array.get(i).toString());
							
							String[] urlSplitted3 = level3Object.get("url").toString().split("/");
							Integer catID3 = Integer.valueOf(urlSplitted3[urlSplitted3.length - 1]);
							System.out.println("\t\t\tlevel3: name = " + level3Object.get("name") + " ,  catID = " + catID3);
							
							if (childrenOfThisLevel1ID.contains(catID3))
							{
								System.out.print("Alert! catID3 = " + catID3 + " is mentioned multiple times for level 1 catID " + catID1
										+ " (catID2 = " + catID2 + ")\n");
							}
							childrenOfThisLevel1ID.add(catID3);
							// if (level3Map.containsKey(catID3))
							// {
							// System.out.println("Alert in level3: catID " + catID3 + " already in level 3");
							// }
							// level3Map.put(Integer.valueOf(catID3), level3Object.get("name").toString());
							
							// if (level3Object.get("spot_categories") != null)
							// System.out
							// .println("Alert: Level 4 detected " + ((JSONArray) level3Object.get("spot_categories")).toString());
						} // end of loop over array of level2 object... the array objects here are of level 3
					} // end of loop over array of level1 object... the array objects here are of level 2
					flatMapLevel1.put(catID1, childrenOfThisLevel1ID);
				} // end of loop over array of level0 object... the array objects here are of level 1
			}
			
			System.out.println("Traversing the created level 1 flat map:");
			int countOfCatIDsInThisMap = 0;
			
			HashSet<Integer> allUniqueIDs = new HashSet<Integer>();
			ArrayList<Integer> findDuplicateIDs = new ArrayList<Integer>();
			
			for (Integer key1 : flatMapLevel1.keySet())
			{
				countOfCatIDsInThisMap += 1;
				TreeSet<Integer> children = flatMapLevel1.get(key1);
				countOfCatIDsInThisMap += children.size();
				System.out.println("Level 1 ID = " + key1 + " has " + children.size() + " children.");
				System.out.println("\t children IDs = " + Arrays.toString(children.toArray()));
				allUniqueIDs.add(key1);
				findDuplicateIDs.add(key1);
				allUniqueIDs.addAll(children);
				findDuplicateIDs.addAll(children);
				// System.out.println(Arrays.toString(stack.toArray()));
			}
			
			System.out.println("Total num of cat id in flatmap = " + countOfCatIDsInThisMap);
			System.out.println("Total num of unique cat id in flatmap = " + allUniqueIDs.size());
			System.out.println("Total num of cat id in flatmap = " + findDuplicateIDs.size());
			findDuplicateIDs.removeAll(allUniqueIDs);
			System.out.println("Total num of cat id in flatmap = " + findDuplicateIDs.size());
			System.out.println("Duplicate IDs = " + Arrays.toString(findDuplicateIDs.toArray()));
			
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Exiting getTwoLevelCategoryHierarchyFromJSON()");
		return flatMapLevel1;// new Triple(level1Map, level2Map, level3Map);
	}
	
	/**
	 * Returns the category hierarchy tree as composition of maps
	 * 
	 * @param catHierarchyFileNameToRead
	 * @return (level 1 map (level 2 map (level 3 set)))
	 */
	public static TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>>
			getThreeLevelCategoryHierarchyFromJSON(String catHierarchyFileNameToRead)
	{
		
		String fileNameToRead = catHierarchyFileNameToRead;
		// "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/Gowalla/link to Gowalla dataset/another source/gowalla/gowalla_category_structure.json";
		int countOfLines = -1;
		
		System.out.println("Inside getThreeLevelCategoryHierarchyFromJSON()");
		
		// level 1 map <level 2 map <level 3 set>>
		TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> categoryHierarchyMapLevel1 =
				new TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>>();
		
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
			
			Set<String> level0Keys = jObj.keySet(); // only 1 key 'spot catergories'
			// System.out.println("Num of level0Keys = " + level0Keys.size());
			if (level0Keys.size() != 1)
			{
				System.err.println("Error: incorrect tree: level0Keys.size() =" + level0Keys.size());
			}
			
			for (String level0Key : level0Keys) // only 1 key : spot_categories
			{
				JSONArray level0Array = jObj.getJSONArray(level0Key); // array with first element as Community
				System.out.println("level0: key=" + level0Key + ", has an array of size =" + level0Array.length());
				// level0: key=spot_categories, has an array of size =7
				
				for (int i = 0; i < level0Array.length(); i++)// 7
				{
					JSONObject level1Object = level0Array.getJSONObject(i);
					
					String[] urlSplitted = level1Object.get("url").toString().split("/");
					Integer catID1 = Integer.valueOf(urlSplitted[urlSplitted.length - 1]);
					System.out.println("\tlevel1: name = " + level1Object.get("name") + " , catID = " + catID1);// level1: name = Community , catID = 934
					
					if (categoryHierarchyMapLevel1.containsKey(catID1))
					{
						System.out.println("Alert in level1: catID " + catID1 + " already in level 1");
					}
					
					JSONArray level1Array = (JSONArray) level1Object.get("spot_categories"); // of level 1 object e.g.,community
					
					TreeMap<Integer, TreeSet<Integer>> childrenOfThisLevel1ID = new TreeMap<Integer, TreeSet<Integer>>();
					
					for (int j = 0; j < level1Array.length(); j++)
					{
						JSONObject level2Object = level1Array.getJSONObject(j);// e.g. Campus spot object
						
						String[] urlSplitted2 = level2Object.get("url").toString().split("/");
						Integer catID2 = Integer.valueOf(urlSplitted2[urlSplitted2.length - 1]);
						System.out.println("\t\tlevel2: name = " + level2Object.get("name") + " , catID = " + catID2);
						// level2: name = Campus Spot , catID = 133
						if (childrenOfThisLevel1ID.containsKey(catID2))
						{
							System.out
									.print("Alert! catID2 = " + catID2 + " is mentioned multiple times for level 1 catID " + catID1 + "\n");
						}
						
						JSONArray level2Array = (JSONArray) level2Object.get("spot_categories"); // of level 2 object e.g.,Campus spot
						TreeSet<Integer> childrenOfThisLevel2ID = new TreeSet<Integer>();
						
						for (int k = 0; k < level2Array.length(); k++)
						{
							JSONObject level3Object = level2Array.getJSONObject(k);
							
							String[] urlSplitted3 = level3Object.get("url").toString().split("/");
							Integer catID3 = Integer.valueOf(urlSplitted3[urlSplitted3.length - 1]);
							System.out.println("\t\t\tlevel3: name = " + level3Object.get("name") + " ,  catID = " + catID3);
							
							if (childrenOfThisLevel2ID.contains(catID3))
							{
								System.out.print("Alert! catID3 = " + catID3 + " is mentioned multiple times for level 1 catID " + catID1
										+ " (catID2 = " + catID2 + ")\n");
							}
							
							childrenOfThisLevel2ID.add(catID3);
						} // end of loop over array of level2 object... the array objects here are of level 3
						childrenOfThisLevel1ID.put(catID2, childrenOfThisLevel2ID);
					} // end of loop over array of level1 object... the array objects here are of level 2
					categoryHierarchyMapLevel1.put(catID1, childrenOfThisLevel1ID);
				} // end of loop over array of level0 object... the array objects here are of level 1
			}
			
			System.out.println("Traversing the created level 1 flat map:");
			int countOfCatIDsInThisMap = 0;
			
			Set<Integer> allUniqueIDs = new TreeSet<Integer>();
			List<Integer> listOfAllIDs = new ArrayList<Integer>();
			
			for (Integer level1CatID : categoryHierarchyMapLevel1.keySet())
			{
				countOfCatIDsInThisMap += 1;
				allUniqueIDs.add(level1CatID);
				listOfAllIDs.add(level1CatID);
				
				TreeMap<Integer, TreeSet<Integer>> level2Children = categoryHierarchyMapLevel1.get(level1CatID);
				System.out.println("Level 1 ID = " + level1CatID + " has " + level2Children.size() + " level 2 children.");
				
				for (Integer level2CatID : level2Children.keySet())
				{
					countOfCatIDsInThisMap += 1;
					allUniqueIDs.add(level2CatID);
					listOfAllIDs.add(level2CatID);
					
					TreeSet<Integer> level3Children = level2Children.get(level2CatID);
					System.out.println("Level 2 ID = " + level2CatID + " has " + level3Children.size() + " level 3 children.");
					
					for (Integer level3CatID : level3Children)
					{
						countOfCatIDsInThisMap += 1;
						allUniqueIDs.add(level3CatID);
						listOfAllIDs.add(level3CatID);
						
						System.out.println("Level 3 ID = " + level3CatID);
					}
				}
			}
			
			Collections.sort(listOfAllIDs);
			
			System.out.println("=========================================");
			System.out.println("Total num of cat id in categoryHierarchyMap = " + countOfCatIDsInThisMap);
			System.out.println("Total num of unique cat id in categoryHierarchyMap = " + allUniqueIDs.size());
			System.out.println("Total num of cat id in categoryHierarchyMap = " + listOfAllIDs.size());
			
			System.out.println("Unique IDs = " + Arrays.toString(allUniqueIDs.toArray()));
			System.out.println("All IDs = " + Arrays.toString(listOfAllIDs.toArray()));
			
			// listOfAllIDs.removeAll(allUniqueIDs);
			Set<Integer> duplicateIDs = findDuplicates(listOfAllIDs);
			System.out.println("Total num of duplicate cat id in categoryHierarchyMap = " + duplicateIDs.size());
			
			System.out.println("Duplicate IDs  = " + Arrays.toString(duplicateIDs.toArray()));
			System.out.println("=========================================");
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Exiting getThreeLevelCategoryHierarchyFromJSON()\n");
		return categoryHierarchyMapLevel1;
	}
	
	/**
	 * Returns the a pair with first item as "a category hierarchy tree as composition of maps" and second item as "a category hierarchy tree as TreeItems (root returned)" <br>
	 * Each node of the tree is a string of format "(catID3) + ":" + (catID3Name)"
	 * 
	 * @param catHierarchyFileNameToRead
	 * @return a Pair
	 */
	public static Pair<TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>>, TreeItem<String>>
			getThreeLevelCategoryHierarchyTreeFromJSON(String catHierarchyFileNameToRead)
	{
		
		String fileNameToRead = catHierarchyFileNameToRead;
		// "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/Gowalla/link to Gowalla dataset/another source/gowalla/gowalla_category_structure.json";
		int countOfLines = -1;
		
		System.out.println("Inside getThreeLevelCategoryHierarchyTreeFromJSON()");
		
		/**
		 * keys are level 1 catid, and values are level2 and level3 children for correspnding level 1 catids.
		 **/
		TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> categoryHierarchyMapLevel1 =
				new TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>>();
		TreeItem<String> root = new TreeItem("-1:root"); // (catid:catName)
		
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
			
			Set<String> level0Keys = jObj.keySet(); // only 1 key 'spot catergories'
			// System.out.println("Num of level0Keys = " + level0Keys.size());
			if (level0Keys.size() != 1)
			{
				System.err.println("Error: incorrect tree: level0Keys.size() =" + level0Keys.size());
			}
			
			for (String level0Key : level0Keys) // only 1 key : spot_categories
			{
				JSONArray level0Array = jObj.getJSONArray(level0Key); // array with first element as Community
				System.out.println("level0: key=" + level0Key + ", has an array of size =" + level0Array.length());
				// level0: key=spot_categories, has an array of size =7
				
				for (int i = 0; i < level0Array.length(); i++)// 7
				{
					JSONObject level1Object = level0Array.getJSONObject(i);
					
					String[] urlSplitted = level1Object.get("url").toString().split("/");
					Integer catID1 = Integer.valueOf(urlSplitted[urlSplitted.length - 1]);
					String catID1Name = level1Object.get("name").toString().trim();
					System.out.println("\tlevel1: name = " + catID1Name + " , catID = " + catID1);// level1: name = Community , catID = 934
					
					if (categoryHierarchyMapLevel1.containsKey(catID1))
					{
						System.out.println("Alert in level1: catID " + catID1 + " already in level 1");
					}
					
					JSONArray level1Array = (JSONArray) level1Object.get("spot_categories"); // of level 1 object e.g.,community
					
					// level1 id, children of level1id
					TreeMap<Integer, TreeSet<Integer>> childrenOfThisLevel1ID = new TreeMap<Integer, TreeSet<Integer>>();
					TreeItem<String> level1IDT = new TreeItem<String>(String.valueOf(catID1) + ":" + catID1Name);
					
					for (int j = 0; j < level1Array.length(); j++)
					{
						JSONObject level2Object = level1Array.getJSONObject(j);// e.g. Campus spot object
						
						String[] urlSplitted2 = level2Object.get("url").toString().split("/");
						Integer catID2 = Integer.valueOf(urlSplitted2[urlSplitted2.length - 1]);
						String catID2Name = level2Object.get("name").toString().trim();
						System.out.println("\t\tlevel2: name = " + catID2Name + " , catID = " + catID2);
						// level2: name = Campus Spot , catID = 133
						if (childrenOfThisLevel1ID.containsKey(catID2))
						{
							System.out
									.print("Alert! catID2 = " + catID2 + " is mentioned multiple times for level 1 catID " + catID1 + "\n");
						}
						
						JSONArray level2Array = (JSONArray) level2Object.get("spot_categories"); // of level 2 object e.g.,Campus spot
						TreeSet<Integer> childrenOfThisLevel2ID = new TreeSet<Integer>();
						TreeItem<String> level2IDT = new TreeItem<String>(String.valueOf(catID2) + ":" + catID2Name);
						
						for (int k = 0; k < level2Array.length(); k++)
						{
							JSONObject level3Object = level2Array.getJSONObject(k);
							
							String[] urlSplitted3 = level3Object.get("url").toString().split("/");
							Integer catID3 = Integer.valueOf(urlSplitted3[urlSplitted3.length - 1]);
							String catID3Name = level3Object.get("name").toString().trim();
							System.out.println("\t\t\tlevel3: name = " + catID3Name + " ,  catID = " + catID3);
							
							if (childrenOfThisLevel2ID.contains(catID3))
							{
								System.out.print("Alert! catID3 = " + catID3 + " is mentioned multiple times for level 1 catID " + catID1
										+ " (catID2 = " + catID2 + ")\n");
							}
							
							childrenOfThisLevel2ID.add(catID3);
							level2IDT.getChildren().add(new TreeItem<String>(String.valueOf(catID3) + ":" + catID3Name));
						} // end of loop over array of level2 object... the array objects here are of level 3
						childrenOfThisLevel1ID.put(catID2, childrenOfThisLevel2ID);
						level1IDT.getChildren().add(level2IDT);
					} // end of loop over array of level1 object... the array objects here are of level 2
					categoryHierarchyMapLevel1.put(catID1, childrenOfThisLevel1ID);
					root.getChildren().add(level1IDT);
				} // end of loop over array of level0 object... the array objects here are of level 1
			}
			
			System.out.println("Traversing the created level 1 flat map:");
			int countOfCatIDsInThisMap = 0;
			
			Set<Integer> allUniqueIDs = new TreeSet<Integer>();
			List<Integer> listOfAllIDs = new ArrayList<Integer>();
			
			for (Integer level1CatID : categoryHierarchyMapLevel1.keySet())
			{
				countOfCatIDsInThisMap += 1;
				allUniqueIDs.add(level1CatID);
				listOfAllIDs.add(level1CatID);
				
				TreeMap<Integer, TreeSet<Integer>> level2Children = categoryHierarchyMapLevel1.get(level1CatID);
				System.out.println("Level 1 ID = " + level1CatID + " has " + level2Children.size() + " level 2 children.");
				
				for (Integer level2CatID : level2Children.keySet())
				{
					countOfCatIDsInThisMap += 1;
					allUniqueIDs.add(level2CatID);
					listOfAllIDs.add(level2CatID);
					
					TreeSet<Integer> level3Children = level2Children.get(level2CatID);
					System.out.println("Level 2 ID = " + level2CatID + " has " + level3Children.size() + " level 3 children.");
					
					for (Integer level3CatID : level3Children)
					{
						countOfCatIDsInThisMap += 1;
						allUniqueIDs.add(level3CatID);
						listOfAllIDs.add(level3CatID);
						
						System.out.println("Level 3 ID = " + level3CatID);
					}
				}
			}
			
			Collections.sort(listOfAllIDs);
			
			System.out.println("=========================================");
			System.out.println("Total num of cat id in categoryHierarchyMap = " + countOfCatIDsInThisMap);
			System.out.println("Total num of unique cat id in categoryHierarchyMap = " + allUniqueIDs.size());
			System.out.println("Total num of cat id in categoryHierarchyMap = " + listOfAllIDs.size());
			
			System.out.println("Unique IDs = " + Arrays.toString(allUniqueIDs.toArray()));
			System.out.println("All IDs = " + Arrays.toString(listOfAllIDs.toArray()));
			
			// listOfAllIDs.removeAll(allUniqueIDs);
			Set<Integer> duplicateIDs = findDuplicates(listOfAllIDs);
			System.out.println("Total num of duplicate cat id in categoryHierarchyMap = " + duplicateIDs.size());
			
			System.out.println("Duplicate IDs  = " + Arrays.toString(duplicateIDs.toArray()));
			System.out.println("=========================================");
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Exiting getThreeLevelCategoryHierarchyTreeFromJSON()\n");
		return new Pair<TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>>, TreeItem<String>>(categoryHierarchyMapLevel1, root);
	}
	
	// /**
	// * Returns the category hierarchy as map with keys as level 1 ids and children as level 2 and level 3 ids. Note: this does not distinguish between a level 2 and level 3 child
	// * if a level 1.
	// *
	// * @return keys are level 1 catid, and values are level2 and level3 children for correspnding level 1 catids.
	// */
	// public static TreeItem getThreeLevelCategoryHierarchyTreeFromJSON(String catHierarchyFileNameToRead)
	// {
	//
	// String fileNameToRead = catHierarchyFileNameToRead;
	// // "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/Gowalla/link to Gowalla dataset/another source/gowalla/gowalla_category_structure.json";
	// int countOfLines = -1;
	//
	// System.out.println("Inside getThreeLevelCategoryHierarchyTreeFromJSON()");
	//
	// TreeItem<Integer> root = new TreeItem<Integer>(-1);
	//
	// try
	// {
	// BufferedReader br = new BufferedReader(new FileReader(fileNameToRead));
	// String lineRead;
	// StringBuffer jsonStringBuf = new StringBuffer();
	//
	// while ((lineRead = br.readLine()) != null)
	// {
	// countOfLines += 1;
	// jsonStringBuf.append(lineRead);
	// }
	// System.out.println("Num of lines read: " + countOfLines);
	//
	// JSONObject jObj = new JSONObject(jsonStringBuf.toString());
	// // System.out.println(" Json string regenerated:" + jObj.toString());
	//
	// Set<String> level0Keys = jObj.keySet(); // only 1 key 'spot catergories'
	// // System.out.println("Num of level0Keys = " + level0Keys.size());
	// if (level0Keys.size() != 1)
	// {
	// System.err.println("Error: incorrect tree: level0Keys.size() =" + level0Keys.size());
	// }
	//
	// for (String level0Key : level0Keys) // only 1 key : spot_categories
	// {
	// JSONArray level0Array = jObj.getJSONArray(level0Key); // array with first element as Community
	// System.out.println("level0: key=" + level0Key + ", has an array of size =" + level0Array.length());
	// // level0: key=spot_categories, has an array of size =7
	//
	// for (int i = 0; i < level0Array.length(); i++)// 7
	// {
	// JSONObject level1Object = level0Array.getJSONObject(i);
	//
	// String[] urlSplitted = level1Object.get("url").toString().split("/");
	// Integer catID1 = Integer.valueOf(urlSplitted[urlSplitted.length - 1]);
	// System.out.println("\tlevel1: name = " + level1Object.get("name") + " , catID = " + catID1);// level1: name = Community , catID = 934
	//
	// // if (categoryHierarchyMapLevel1.containsKey(catID1))
	// // {
	// // System.out.println("Alert in level1: catID " + catID1 + " already in level 1");
	// // }
	// JSONArray level1Array = (JSONArray) level1Object.get("spot_categories"); // of level 1 object e.g.,community
	//
	// TreeItem<Integer> childrenOfThisLevel1ID = new TreeItem<Integer>();
	//
	// for (int j = 0; j < level1Array.length(); j++)
	// {
	// JSONObject level2Object = level1Array.getJSONObject(j);// e.g. Campus spot object
	//
	// String[] urlSplitted2 = level2Object.get("url").toString().split("/");
	// Integer catID2 = Integer.valueOf(urlSplitted2[urlSplitted2.length - 1]);
	// System.out.println("\t\tlevel2: name = " + level2Object.get("name") + " , catID = " + catID2);// level2: name = Campus Spot , catID = 133
	// // if (childrenOfThisLevel1ID.containsKey(catID2))
	// // {
	// // System.out
	// // .print("Alert! catID2 = " + catID2 + " is mentioned multiple times for level 1 catID " + catID1 + "\n");
	// // }
	//
	// JSONArray level2Array = (JSONArray) level2Object.get("spot_categories"); // of level 2 object e.g.,Campus spot
	// TreeItem<Integer> childrenOfThisLevel2ID = new TreeItem<Integer>();
	//
	// for (int k = 0; k < level2Array.length(); k++)
	// {
	// JSONObject level3Object = level2Array.getJSONObject(k);
	//
	// String[] urlSplitted3 = level3Object.get("url").toString().split("/");
	// Integer catID3 = Integer.valueOf(urlSplitted3[urlSplitted3.length - 1]);
	// System.out.println("\t\t\tlevel3: name = " + level3Object.get("name") + " , catID = " + catID3);
	//
	// // if (childrenOfThisLevel2ID.contains(catID3))
	// // {
	// // System.out.print("Alert! catID3 = " + catID3 + " is mentioned multiple times for level 1 catID " + catID1
	// // + " (catID2 = " + catID2 + ")\n");
	// // }
	// childrenOfThisLevel2ID.getChildren().add(catID3);
	// childrenOfThisLevel2ID.add(catID3);
	// } // end of loop over array of level2 object... the array objects here are of level 3
	// childrenOfThisLevel1ID.put(catID2, childrenOfThisLevel2ID);
	// } // end of loop over array of level1 object... the array objects here are of level 2
	// categoryHierarchyMapLevel1.put(catID1, childrenOfThisLevel1ID);
	// } // end of loop over array of level0 object... the array objects here are of level 1
	// }
	//
	// System.out.println("Traversing the created level 1 flat map:");
	// int countOfCatIDsInThisMap = 0;
	//
	// Set<Integer> allUniqueIDs = new TreeSet<Integer>();
	// List<Integer> listOfAllIDs = new ArrayList<Integer>();
	//
	// for (Integer level1CatID : categoryHierarchyMapLevel1.keySet())
	// {
	// countOfCatIDsInThisMap += 1;
	// allUniqueIDs.add(level1CatID);
	// listOfAllIDs.add(level1CatID);
	//
	// TreeMap<Integer, TreeSet<Integer>> level2Children = categoryHierarchyMapLevel1.get(level1CatID);
	// System.out.println("Level 1 ID = " + level1CatID + " has " + level2Children.size() + " level 2 children.");
	//
	// for (Integer level2CatID : level2Children.keySet())
	// {
	// countOfCatIDsInThisMap += 1;
	// allUniqueIDs.add(level2CatID);
	// listOfAllIDs.add(level2CatID);
	//
	// TreeSet<Integer> level3Children = level2Children.get(level2CatID);
	// System.out.println("Level 2 ID = " + level2CatID + " has " + level3Children.size() + " level 3 children.");
	//
	// for (Integer level3CatID : level3Children)
	// {
	// countOfCatIDsInThisMap += 1;
	// allUniqueIDs.add(level3CatID);
	// listOfAllIDs.add(level3CatID);
	//
	// System.out.println("Level 3 ID = " + level3CatID);
	// }
	// }
	// }
	//
	// Collections.sort(listOfAllIDs);
	//
	// System.out.println("=========================================");
	// System.out.println("Total num of cat id in categoryHierarchyMap = " + countOfCatIDsInThisMap);
	// System.out.println("Total num of unique cat id in categoryHierarchyMap = " + allUniqueIDs.size());
	// System.out.println("Total num of cat id in categoryHierarchyMap = " + listOfAllIDs.size());
	//
	// System.out.println("Unique IDs = " + Arrays.toString(allUniqueIDs.toArray()));
	// System.out.println("All IDs = " + Arrays.toString(listOfAllIDs.toArray()));
	//
	// // listOfAllIDs.removeAll(allUniqueIDs);
	// Set<Integer> duplicateIDs = findDuplicates(listOfAllIDs);
	// System.out.println("Total num of duplicate cat id in categoryHierarchyMap = " + duplicateIDs.size());
	//
	// System.out.println("Duplicate IDs = " + Arrays.toString(duplicateIDs.toArray()));
	// System.out.println("=========================================");
	// }
	//
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// System.out.println("Exiting getThreeLevelCategoryHierarchyTreeFromJSON()\n");
	// return null;// categoryHierarchyMapLevel1;
	// }
	
	public static Set getIntersection(Set s1, Set s2)
	{
		Set intersection = new HashSet(s1);
		intersection.retainAll(s2);
		
		return intersection;
	}
	// public static
	
	/**
	 * To get the (day) difference between consecutive days of data for each user. Note: the input here is daywise data.
	 */
	public static void writeDiffFromNextDay()// String args[])
	{
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		
		// Triple catLevelMaps = getCategoryMapsFromJSON();
		// LinkedHashMap<Integer, String> level1Map = (LinkedHashMap<Integer, String>) catLevelMaps.getFirst();
		// LinkedHashMap<Integer, String> level2Map = (LinkedHashMap<Integer, String>) catLevelMaps.getSecond();
		// LinkedHashMap<Integer, String> level3Map = (LinkedHashMap<Integer, String>) catLevelMaps.getThird();
		//
		String fileNameToRead =
				"/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/gw2CheckinsSpots1Slim1TargetUsersDatesOnlyPerUserDateDWithLevels.csv";// D stands for daywise stats
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
	
	/**
	 * 
	 * @param list
	 * @return
	 */
	public static <T> Set<T> findDuplicates(Collection<T> list)
	{
		
		Set<T> duplicates = new LinkedHashSet<T>();
		Set<T> uniques = new HashSet<T>();
		
		for (T t : list)
		{
			if (!uniques.add(t))
			{
				duplicates.add(t);
			}
		}
		
		return duplicates;
	}
	// public static void writeCatLevelInfo()
	// {
	// TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	//
	// Triple catLevelMaps = getCategoryMapsFromJSON();
	// LinkedHashMap<Integer, String> level1Map = (LinkedHashMap<Integer, String>) catLevelMaps.getFirst();
	// LinkedHashMap<Integer, String> level2Map = (LinkedHashMap<Integer, String>) catLevelMaps.getSecond();
	// LinkedHashMap<Integer, String> level3Map = (LinkedHashMap<Integer, String>) catLevelMaps.getThird();
	//
	// TreeMap<Integer, Long> level1CkeckinCountMap = new TreeMap<Integer, Long>();// )
	// TreeMap<Integer, Long> level2CkeckinCountMap = new TreeMap<Integer, Long>();
	// TreeMap<Integer, Long> level3CkeckinCountMap = new TreeMap<Integer, Long>();
	// TreeMap<Integer, Long> noneLevelCkeckinCountMap = new TreeMap<Integer, Long>();
	//
	// String fileNameToRead = "/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/gw2CheckinsSpots1Slim1TargetUsersDatesOnly.csv";
	// String fileNameToWrite =
	// "/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/gw2CheckinsSpots1Slim1TargetUsersDatesOnlyWithLevelsV2_2.csv";
	//
	// String fileNameToWriteCatLevelDistro =
	// "/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/gw2CheckinsSpots1Slim1TargetUsersDatesOnlyCatLevelDistro";
	//
	// int countOfLines = 0;
	// StringBuffer sbuf = new StringBuffer();
	// String lineRead;
	//
	// int l1Count = 0, l2Count = 0, l3Count = 0, notFoundInAnyLevel = 0;
	// ArrayList<Integer> catIDsNotFoundInAnyLevel = new ArrayList<Integer>();
	// // int lengthOfReadTokens = -1;
	// try
	// {
	// BufferedReader br = new BufferedReader(new FileReader(fileNameToRead));
	// BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(fileNameToWrite);
	//
	// while ((lineRead = br.readLine()) != null)
	// {
	// countOfLines += 1;
	// int isLevel1 = 0, isLevel2 = 0, isLevel3 = 0;
	// int foundInLevels = 0;
	//
	// String[] splittedLine = lineRead.split(",");
	//
	// if (countOfLines == 1)
	// {
	// sbuf.append(splittedLine[1] + "," + splittedLine[2] + "," + splittedLine[3] + "," + splittedLine[4] + ","
	// + splittedLine[5] + ",IsLevel1,IsLevel2,IsLevel3\n");
	// continue;
	// }
	// // System.out.println("splittedLine[3] =" + splittedLine[3]);
	// // System.out.println("splittedLine[1] =" + splittedLine[1]);
	// Integer catID = Integer.valueOf(splittedLine[3].replaceAll("\"", ""));
	//
	// if (level1Map.containsKey(catID))
	// {
	// isLevel1 = 1;
	// foundInLevels++;
	// l1Count++;
	// level1CkeckinCountMap.put(catID, level1CkeckinCountMap.getOrDefault(catID, new Long(0)) + 1);
	// }
	// if (level2Map.containsKey(catID))
	// {
	// isLevel2 = 1;
	// foundInLevels++;
	// l2Count++;
	// level2CkeckinCountMap.put(catID, level2CkeckinCountMap.getOrDefault(catID, new Long(0)) + 1);
	// }
	//
	// if (level3Map.containsKey(catID))
	// {
	// isLevel3 = 1;
	// foundInLevels++;
	// l3Count++;
	// level3CkeckinCountMap.put(catID, level3CkeckinCountMap.getOrDefault(catID, new Long(0)) + 1);
	// }
	//
	// if (foundInLevels == 0)
	// {
	// catIDsNotFoundInAnyLevel.add(catID);
	// notFoundInAnyLevel++;
	// noneLevelCkeckinCountMap.put(catID, noneLevelCkeckinCountMap.getOrDefault(catID, new Long(0)) + 1);
	// }
	//
	// if (foundInLevels > 1 && catID != 201)
	// {
	// System.err.println("Error: catID " + catID + " found in multiple levels " + isLevel1 + "," + isLevel3 + "," + isLevel3);
	// }
	//
	// sbuf.append(splittedLine[1] + "," + splittedLine[2] + "," + splittedLine[3] + "," + splittedLine[4] + "," + splittedLine[5]
	// + "," + isLevel1 + "," + isLevel2 + "," + isLevel3 + "\n");
	//
	// // if (countOfLines % 4000 == 0)
	// // {
	// bw.write(sbuf.toString());
	// sbuf.setLength(0);
	// // }
	// }
	//
	// bw.close();
	// br.close();
	//
	// System.out.println("Num of checkins read: " + (countOfLines - 1));
	// System.out.println("Num of level1 in checkins: " + l1Count);
	// System.out.println("Num of level2 in checkins: " + l2Count);
	// System.out.println("Num of level3 in checkins: " + l3Count);
	// System.out.println("Num of checkins with catID in no levelMap: " + notFoundInAnyLevel);
	//
	// WritingToFile.appendLineToFileAbsolute(StringUtils.join(catIDsNotFoundInAnyLevel.toArray(), ","),
	// "/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/CatsInNoMaps.csv");
	//
	// writeCheckInDistributionOverCatIDs(level1CkeckinCountMap, level2CkeckinCountMap, level3CkeckinCountMap,
	// noneLevelCkeckinCountMap, fileNameToWriteCatLevelDistro);
	// // catIDsNotFoundInAnyLevel
	// // bw.write(sbuf.toString());
	// // sbuf.setLength(0);
	//
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	//
	// }
	//
	//
	// bw.close();
	// br.close();
	//
	// System.out.println("Num of checkins read: " + (countOfLines - 1));
	// System.out.println("Num of level1 in checkins: " + l1Count);
	// System.out.println("Num of level2 in checkins: " + l2Count);
	// System.out.println("Num of level3 in checkins: " + l3Count);
	// System.out.println("Num of checkins with catID in no levelMap: " + notFoundInAnyLevel);
	//
	// WritingToFile.appendLineToFileAbsolute(StringUtils.join(catIDsNotFoundInAnyLevel.toArray(), ","),
	// "/run/media/gunjan/BoX2/GowallaSpaceSpace/June30/CatsInNoMaps.csv");
	//
	// writeCheckInDistributionOverCatIDs(level1CkeckinCountMap, level2CkeckinCountMap, level3CkeckinCountMap,
	// noneLevelCkeckinCountMap, fileNameToWriteCatLevelDistro);
	// // catIDsNotFoundInAnyLevel
	// // bw.write(sbuf.toString());
	// // sbuf.setLength(0);
	//
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	//
	// }
	//
}
