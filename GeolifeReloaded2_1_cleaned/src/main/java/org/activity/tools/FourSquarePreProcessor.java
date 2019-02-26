package org.activity.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.tree.DefaultMutableTreeNode;

import org.activity.io.ReadingFromFile;
import org.activity.io.Serializer;
import org.activity.io.WToFile;
import org.activity.objects.LeakyBucket;
import org.activity.objects.Pair;
import org.activity.objects.Triple;
import org.activity.spatial.SpatialUtils;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.ui.UIUtilityBox;
import org.activity.util.UtilityBelt;
import org.json.JSONArray;
import org.json.JSONObject;

import javafx.scene.control.TreeItem;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.io.csv.CsvWriteOptions;

public class FourSquarePreProcessor
{
	public static void main(String args[])
	{
		// mainBefore24Feb2019();
		String commonPathToWrite = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/FSDataWorks/DataProcessingFeb25_2019/";
		jsonProcessorFoursquare(commonPathToWrite);
	}

	public static void mainBefore24Feb2019(String args[])
	{

		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		// fourSquarePreprocessor1();
		// $$sortData();//
		String commonPath = "/run/media/gunjan/BackupVault/DatasetsInBackupVault/FoursquareDatasets/dataset_tsmc2014/";
		addDistanceAndDurationFromPrev(commonPath + "dataset_TSMC2014_NYC_Processed1_NoDupSorted2.txt",
				commonPath + "dataset_TSMC2014_NYC_Processed1_NoDupSorted2DistDur.csv", commonPath);

	}

	/**
	 * Fork of org.activity.ui.CategoryHierarchyTreeUI2.start(Stage)
	 */
	public static void catHierarchyProcessing(String checkinFileNameToRead, String commonPathToWrite)
	{
		// String checkinFileNameToRead =
		// "/home/gunjan/RWorkspace/GowallaRWorks/gw2CheckinsAllTargetUsersDatesOnlyMar29.csv";
		try
		{
			// consoleLogStream = WritingToFile.redirectConsoleOutput(commonPath + "consoleLog2.txt");
			// Select the root node
			// treeView.getSelectionModel().selectFirst();
			// JSONProcessingGowallaCatHierachy preProcessGowallaWRTJsonCatHierarchy = new
			// JSONProcessingGowallaCatHierachy(
			// commonPath, checkinFileNameToRead);

			/////////
			// TreeMap<Integer, String> dict = preProcessGowallaWRTJsonCatHierarchy.getCatIDNameDictionary();
			// Serializer.kryoSerializeThis(dict, commonPath + "CatIDNameDictionary.kryo");
			LinkedHashMap<Integer, String> dict = (LinkedHashMap<Integer, String>) Serializer
					.kryoDeSerializeThis(commonPathToWrite + "catIDNameMap.kryo");
			/////////
			// Serializer.serializeThis(preProcessGowalla, commonPath + "SerializedPreProcessGowalla24Aug.obj");
			// JSONProcessingGowallaTryingNonStatic preProcessGowalla =
			// (JSONProcessingGowallaTryingNonStatic) Serializer.deSerializeThis(commonPath +
			///////// "SerializedPreProcessGowalla24Aug.obj");
			TreeItem<String> rootOfCategoryTree = (TreeItem<String>) Serializer
					.kryoDeSerializeThis(commonPathToWrite + "rootOfCategoryHierarchyTree.kryo");
			DefaultMutableTreeNode rootOfCategoryTreeTN = UIUtilityBox.convertTreeItemsToTreeNodes(rootOfCategoryTree);
			String treeAsString = UIUtilityBox.treeToString(0, rootOfCategoryTree, new StringBuffer());
			String serialisableTreeAsString = UIUtilityBox.treeToString(0, rootOfCategoryTreeTN, new StringBuffer());
			WToFile.writeToNewFile(treeAsString, commonPathToWrite + "TreeOfTreeItemsAsString.txt");
			WToFile.writeToNewFile(serialisableTreeAsString, commonPathToWrite + "TreeOfTreeNodesAsString.txt");
			Serializer.kryoSerializeThis(rootOfCategoryTreeTN,
					commonPathToWrite + "rootOfCategoryHierarchyTreeAsDefaultMutableTreeNode.kryp");

			// ~~~~~~` get deserialised tree.. (the one in which we have already manually added some nodes)

			// if (whichRootToUse.equals("updated"))
			// {
			// rootOfCategoryTreeTN = (DefaultMutableTreeNode) Serializer.deSerializeThis(
			// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/RootOfCategoryTree24Nov2016.DMTreeNode");
			// // "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep9_2/RootOfCategoryTree9Sep2016.DMTreeNode");
			//
			// rootOfCategoryTree = UIUtilityBox.convertTreeNodesToTreeItems(rootOfCategoryTreeTN);
			//
			// treeView.setRoot(rootOfCategoryTree);
			// }
			// // ~~~~~~`
			// else
			// {
			// Set the root node for the TreeViww
			// treeView.setRoot(preProcessGowallaWRTJsonCatHierarchy.getRootOfCategoryHierarchyTree());
			// }
			// VBox rightPane = getRightPane();
			// VBox.setVgrow(rightPane, Priority.ALWAYS);
			// PopUps.showMessage("before second right pane");
			// TreeMap<Integer, Long> catIDInCheckinsButNotInAnyLevelOfJSONHierarchy =
			// preProcessGowallaWRTJsonCatHierarchy
			// .getCheckinCountResultsTogether().get("noneLevelCheckinCountMap");

			//////////// Start of added on April 2 2018
			// remove catIDs which have been added to the JSON hierarhcy tree, i.e., which have been added in the
			// updated category hierarhcy tree.

			// get list of nodes in the current ("updated") tree
			List<Triple<Integer, String, String>> listOfCatIDsInTree = UIUtilityBox.treeToListOfCatIDs(0,
					rootOfCategoryTreeTN, new ArrayList<Triple<Integer, String, String>>());

			StringBuilder sbt2 = new StringBuilder("Depth,CatID,CatName\n");
			listOfCatIDsInTree.stream()
					.forEachOrdered(e -> sbt2.append(e.getFirst() + "," + e.getSecond() + "," + e.getThird() + "\n"));
			WToFile.writeToNewFile(sbt2.toString(), commonPathToWrite + "CatIDsInNov2016HierarchyTree.csv");

			// TreeMap<Integer, Long> catIDInCinsButNotInAnyLevelOfJSONOrNov2016Hierarchy = new TreeMap<>();
			// List<Integer> catIDsInNewCatHierarchyButNotInJSONHierarchy = new ArrayList<>();
			//
			// // StringBuilder sbt1 = new StringBuilder();
			// for (Entry<Integer, Long> c : catIDInCheckinsButNotInAnyLevelOfJSONHierarchy.entrySet())
			// {
			// Integer catIDNotInJSONHierarchy = c.getKey();
			// boolean newHierarchyContainsThisCatID = false;
			//
			// // Check if catIDNotInJSONHierarchy is present in listOfCatIDsInTree
			// for (Triple<Integer, String, String> catIDInTreeEntry : listOfCatIDsInTree)
			// {
			// Integer catIDInTree = Integer.valueOf(catIDInTreeEntry.getSecond());
			// if (catIDInTree.equals(catIDNotInJSONHierarchy))
			// {
			// newHierarchyContainsThisCatID = true;
			// break;
			// }
			// // sbt1.append("Debug: catIDNotInJSONHierarchy = " + catIDNotInJSONHierarchy + " catIDInTree="
			// // + catIDInTree + " newHierarchyContainsThisCatID=" + newHierarchyContainsThisCatID + "\n");
			// }
			//
			// // if this catID is contained in the new hierarchy tree
			// // listOfCatIDsInTree.stream().anyMatch(e -> e.getFirst().equals(catID)) == true)
			// if (newHierarchyContainsThisCatID)
			// {
			// catIDsInNewCatHierarchyButNotInJSONHierarchy.add(catIDNotInJSONHierarchy);
			// }
			// else
			// {
			// // PopUps.showMessage("here4");
			// catIDInCinsButNotInAnyLevelOfJSONOrNov2016Hierarchy.put(catIDNotInJSONHierarchy, c.getValue());
			// }
			// }
			// // WritingToFile.writeToNewFile(sbt1.toString(),
			// // commonPath + "DebugApril2catIDInCheckinsButNotInAnyLevelOfJSONOrNewHierarchy.csv");
			//
			// System.out.println("catIDInCheckinsButNotInAnyLevelOfJSONHierarchy.size()="
			// + catIDInCheckinsButNotInAnyLevelOfJSONHierarchy.size());
			// System.out.println("catIDsInNewCatHierarchyButNotInJSONHierarchy.size()="
			// + catIDsInNewCatHierarchyButNotInJSONHierarchy.size());
			// System.out.println("catIDInCinsButNotInAnyLevelOfJSONOrNov2016Hierarchy.size()="
			// + catIDInCinsButNotInAnyLevelOfJSONOrNov2016Hierarchy.size());
			// if (true)
			// {
			// StringBuilder sb1 = new StringBuilder("CatIDNotInJSONButAddedInNewHierarchy,CatName\n");
			// catIDsInNewCatHierarchyButNotInJSONHierarchy.stream()
			// .forEachOrdered(e -> sb1.append(e.toString() + "," + dict.get(e) + "\n"));
			// WToFile.writeToNewFile(sb1.toString(), commonPath + "CatIDNotInJSONButAddedInNewHierarchy.csv");
			// }

			//////////// End of added on April 2 2018

		}
		catch (

		Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * @since 24 Feb 2019
	 */
	public static void jsonProcessorFoursquare(String commonPathToWrite)
	{

		String checkinFileNameToRead = "/home/gunjan/RWorkspace/GowallaRWorks/FSNY2018-10-04AllTargetUsersDatesOnly.csv";

		Triple<Pair<TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>>, TreeItem<String>>, Triple<LinkedHashMap<Integer, String>, LinkedHashMap<Integer, String>, LinkedHashMap<Integer, String>>, Pair<LinkedHashMap<Integer, String>, LinkedHashMap<String, Integer>>> catJSONRes = getThreeLevelCategoryHierarchyTreeFromJSON_FS(
				"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/FSCat2.json");

		/** Get three separate catid name maps for each of the three level from the provided json hierarchy file **/
		Triple<LinkedHashMap<Integer, String>, LinkedHashMap<Integer, String>, LinkedHashMap<Integer, String>> catIDNamesFor3Levels = catJSONRes
				.getSecond();

		LinkedHashMap<Integer, String> level1CatIDNames = catIDNamesFor3Levels.getFirst();
		LinkedHashMap<Integer, String> level2CatIDNames = catIDNamesFor3Levels.getSecond();
		LinkedHashMap<Integer, String> level3CatIDNames = catIDNamesFor3Levels.getThird();

		//////////////////////////////////////

		/** Get cat id map as hierarchy tree from the provided json hierarchy file **/
		// TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> catHierarchyMap =
		// getThreeLevelCategoryHierarchyFromJSON(catHierarchyFileNameToRead);
		Pair<TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>>, TreeItem<String>> hierarchyProcessingResult = catJSONRes
				.getFirst();

		TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> categoryHierarchyMap = hierarchyProcessingResult
				.getFirst();
		TreeItem<String> rootOfCategoryHierarchyTree = hierarchyProcessingResult.getSecond();
		//////////////////////////////////////

		LinkedHashMap<Integer, String> catIDNameMap = catJSONRes.getThird().getFirst();
		LinkedHashMap<String, Integer> catNameIDMap = catJSONRes.getThird().getSecond();

		/////////
		WToFile.writeToNewFile(level1CatIDNames.entrySet().stream().map(e -> e.getKey() + "," + e.getValue())
				.collect(Collectors.joining("\n")), commonPathToWrite + "level1CatIDNames.csv");
		WToFile.writeToNewFile(level2CatIDNames.entrySet().stream().map(e -> e.getKey() + "," + e.getValue())
				.collect(Collectors.joining("\n")), commonPathToWrite + "level2CatIDNames.csv");
		WToFile.writeToNewFile(level3CatIDNames.entrySet().stream().map(e -> e.getKey() + "," + e.getValue())
				.collect(Collectors.joining("\n")), commonPathToWrite + "level3CatIDNames.csv");

		WToFile.writeToNewFile(catIDNameMap.entrySet().stream().map(e -> e.getKey() + "," + e.getValue())
				.collect(Collectors.joining("\n")), commonPathToWrite + "catIDNameMap.csv");
		WToFile.writeToNewFile(catNameIDMap.entrySet().stream().map(e -> e.getKey() + "," + e.getValue())
				.collect(Collectors.joining("\n")), commonPathToWrite + "catNameIDMap.csv");

		Serializer.kryoSerializeThis(catIDNameMap, commonPathToWrite + "catIDNameMap.kryo");
		Serializer.kryoSerializeThis(catNameIDMap, commonPathToWrite + "catNameIDMap.kryo");
		Serializer.kryoSerializeThis(rootOfCategoryHierarchyTree,
				commonPathToWrite + "rootOfCategoryHierarchyTree.kryo");

		///////////// replaces org.activity.ui.CategoryHierarchyTreeUI2.start(Stage) for FSQNY1 dataset
		DefaultMutableTreeNode rootOfCategoryTreeTN = UIUtilityBox
				.convertTreeItemsToTreeNodes(rootOfCategoryHierarchyTree);
		String treeAsString = UIUtilityBox.treeToString(0, rootOfCategoryHierarchyTree, new StringBuffer());
		String serialisableTreeAsString = UIUtilityBox.treeToString(0, rootOfCategoryTreeTN, new StringBuffer());
		WToFile.writeToNewFile(treeAsString, commonPathToWrite + "TreeOfTreeItemsAsString.txt");
		WToFile.writeToNewFile(serialisableTreeAsString, commonPathToWrite + "TreeOfTreeNodesAsString.txt");
		Serializer.kryoSerializeThis(rootOfCategoryTreeTN,
				commonPathToWrite + "rootOfCategoryHierarchyTreeAsDefaultMutableTreeNode.kryo");
		///////////////////

		//////////////////////////////////////////////////////////////////////////////////////////////

		// Issue: 16 cats are in dataset but not in cat_hierarchy
		// Remedies: Will replace these catNames with mapping in CatsNotInJSONSynonymMap.csv
		List<List<String>> synonymData = ReadingFromFile.readLinesIntoListOfLists(
				"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/FSDataWorks/DataProcessingFeb25_2019/CatsNotInJSONSynonymMap.csv",
				",");
		Map<String, String> oldNameNewNameMap = synonymData.stream().skip(1)
				.collect(Collectors.toMap(e -> e.get(0), e -> e.get(1)));

		LinkedHashMap<String, Integer> catIDsInDataButNotInJSONCatTree = replaceFormerCatIDWithNewCatIDsFromJSONProcessing(
				catNameIDMap, checkinFileNameToRead,
				commonPathToWrite + "FSNY2018-10-04AllTargetUsersDatesOnlyReplaceCatIDNames.csv", 2, 3,
				oldNameNewNameMap);

		WToFile.writeToNewFile(catIDsInDataButNotInJSONCatTree.entrySet().stream()
				.map(e -> e.getKey() + "," + e.getValue()).collect(Collectors.joining("\n")),
				commonPathToWrite + "catIDsInDataButNotInJSONCatTree.csv");

		/////////////////////////////////////////////////////////////////////////////////////
		///// Replace alphanumeric placeID with integer placeIDs

		Pair<LinkedHashMap<String, Integer>, LinkedHashMap<Integer, Pair<String, String>>> res = replaceANPlaceIDWithNewIntPlaceIDs(
				commonPathToWrite + "FSNY2018-10-04AllTargetUsersDatesOnlyReplaceCatIDNames.csv",
				commonPathToWrite + "FSNY2018-10-04AllTargetUsersDatesOnlyReplaceCatIDNamesReplacePlaceID.csv", 1, 4,
				5);
		LinkedHashMap<String, Integer> originalPlaceIDNewPlaceIDMap = res.getFirst();
		LinkedHashMap<Integer, Pair<String, String>> placeIDLatLonMap = res.getSecond();
		WToFile.writeToNewFile(
				"originalPlaceID,NewPlaceID\n" + originalPlaceIDNewPlaceIDMap.entrySet().stream()
						.map(e -> e.getKey() + "," + e.getValue()).collect(Collectors.joining("\n")),
				commonPathToWrite + "originalPlaceIDNewPlaceIDMap.csv");
		WToFile.writeToNewFile("PlaceID,Lat,Lon\n" + placeIDLatLonMap.entrySet().stream()
				.map(e -> e.getKey() + "," + e.getValue().getFirst() + "," + e.getValue().getSecond())
				.collect(Collectors.joining("\n")), commonPathToWrite + "placeIDLatLonMap.csv");

		Serializer.kryoSerializeThis(originalPlaceIDNewPlaceIDMap,
				commonPathToWrite + "originalPlaceIDNewPlaceIDMap.kryo");
		Serializer.kryoSerializeThis(catNameIDMap, commonPathToWrite + "placeIDLatLonMap.kryo");

	}

	/**
	 * 
	 * @param catNameIDMap
	 * @param checkinFileNameToRead
	 * @param checkingFileNameToWrite
	 * @param indexOfCatID
	 * @param indexOfCatName
	 * @param synonymData
	 *            Issue: 16 cats are in dataset but not in cat_hierarchy, Remedies: Will replace these catNames with
	 *            mapping in this.
	 * @return catIDsInDataButNotInJSONCatTree
	 * @since 25 Feb 2019
	 */
	private static LinkedHashMap<String, Integer> replaceFormerCatIDWithNewCatIDsFromJSONProcessing(
			LinkedHashMap<String, Integer> catNameIDMap, String checkinFileNameToRead, String checkingFileNameToWrite,
			int indexOfCatID, int indexOfCatName, Map<String, String> synonymMap)
	{

		LinkedHashMap<String, Integer> catIDsInDataButNotInJSONCatTree = new LinkedHashMap<>();

		try
		{
			List<List<String>> dataRead = ReadingFromFile
					.nColumnReaderStringLargeFile(new FileInputStream(checkinFileNameToRead), ",", false, false);

			StringBuilder sbToWrite = new StringBuilder();
			int lineNum = 0;
			for (List<String> line : dataRead)
			{
				ArrayList<String> modifiedLine = new ArrayList<>(line);
				if (++lineNum != 1)// skip the header
				{
					String catName = line.get(indexOfCatName).trim();
					if (synonymMap.containsKey(catName))
					{ // replace with synonym such as Subway to Metro Station
						catName = synonymMap.get(catName);
					}

					Integer catIDFromJSONProcessing = catNameIDMap.get(catName);

					if (catIDFromJSONProcessing == null)
					{
						System.out.println("Warning: " + catName + " not found in catNameIDMap");
						catIDsInDataButNotInJSONCatTree.put(catName,
								catIDsInDataButNotInJSONCatTree.getOrDefault(catName, 0) + 1);
						catIDFromJSONProcessing = -9999;
					}
					modifiedLine.remove(indexOfCatID);
					modifiedLine.add(indexOfCatID, String.valueOf(catIDFromJSONProcessing));

					modifiedLine.remove(indexOfCatName);
					modifiedLine.add(indexOfCatName, catName);
				}
				sbToWrite.append(modifiedLine.stream().collect(Collectors.joining(",")) + "\n");
			}

			WToFile.writeToNewFile(sbToWrite.toString(), checkingFileNameToWrite);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("catIDsInDataButNotInJSONCatTree.size()= " + catIDsInDataButNotInJSONCatTree.size());
		return catIDsInDataButNotInJSONCatTree;
	}

	/**
	 * Replace alphanumeric placeIDs with (manually assigned) integer placeID
	 * 
	 * @param checkinFileNameToRead
	 * @param checkingFileNameToWrite
	 * @param indexOfPlaceID
	 * @param indexOfLat
	 * @param indexOfLon
	 * @return Pair(originalPlaceIDNewPlaceIDMap, placeIDLatLonMap);
	 */
	private static Pair<LinkedHashMap<String, Integer>, LinkedHashMap<Integer, Pair<String, String>>> replaceANPlaceIDWithNewIntPlaceIDs(
			String checkinFileNameToRead, String checkingFileNameToWrite, int indexOfPlaceID, int indexOfLat,
			int indexOfLon)
	{
		LinkedHashMap<String, Integer> originalPlaceIDNewPlaceIDMap = new LinkedHashMap<>();
		LinkedHashMap<Integer, Pair<String, String>> placeIDLatLonMap = new LinkedHashMap<>();

		try
		{
			List<List<String>> dataRead = ReadingFromFile
					.nColumnReaderStringLargeFile(new FileInputStream(checkinFileNameToRead), ",", false, false);

			StringBuilder sbToWrite = new StringBuilder();
			int lineNum = 0;
			int lastPlaceIDAssigned = 0;

			for (List<String> line : dataRead)
			{
				ArrayList<String> modifiedLine = new ArrayList<>(line);
				if (++lineNum != 1)// skip the header
				{
					String originalPlaceID = line.get(indexOfPlaceID).trim();
					Integer newPlaceID = originalPlaceIDNewPlaceIDMap.get(originalPlaceID);// placeID already
																							// encountered

					if (newPlaceID == null)
					{
						newPlaceID = lastPlaceIDAssigned + 1;
						lastPlaceIDAssigned = newPlaceID;
						placeIDLatLonMap.put(newPlaceID,
								new Pair<>(line.get(indexOfLat).trim(), line.get(indexOfLon).trim()));
						originalPlaceIDNewPlaceIDMap.put(originalPlaceID, newPlaceID);
					}
					modifiedLine.remove(indexOfPlaceID);
					modifiedLine.add(indexOfPlaceID, String.valueOf(newPlaceID));
				}
				sbToWrite.append(modifiedLine.stream().collect(Collectors.joining(",")) + "\n");
			}

			WToFile.writeToNewFile(sbToWrite.toString(), checkingFileNameToWrite);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println(
				"Num of unique placeIDs = " + placeIDLatLonMap.size() + " = " + originalPlaceIDNewPlaceIDMap.size());

		return new Pair<LinkedHashMap<String, Integer>, LinkedHashMap<Integer, Pair<String, String>>>(
				originalPlaceIDNewPlaceIDMap, placeIDLatLonMap);
	}

	/**
	 * Fork of org.activity.tools.JSONProcessingGowallaCatHierachy.getThreeLevelCategoryHierarchyTreeFromJSON(String)
	 * <p>
	 * 
	 * Returns the
	 * 
	 * @param catHierarchyFileNameToRead
	 * @return a Triple where the first item is
	 *         <p>
	 *         {a pair with first item as "a category hierarchy tree as composition of maps" and second item as "a
	 *         category hierarchy tree as TreeItems (root returned)" Each node of the tree is a string of format
	 *         "(catID3) + ":" + (catID3Name)"}
	 *         <p>
	 *         the second item is
	 *         <p>
	 *         {a Triple containing the three hashmaps, one for each of the three category levels. The map contains
	 *         (catergryID,categoryName) for that level.}
	 *         <p>
	 *         > the thirs item is
	 *         <p>
	 *         { a pair containing catIDNameMap and catNameIDMap }
	 * 
	 * @since 24 Feb 2019
	 */
	public static Triple<Pair<TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>>, TreeItem<String>>, Triple<LinkedHashMap<Integer, String>, LinkedHashMap<Integer, String>, LinkedHashMap<Integer, String>>, Pair<LinkedHashMap<Integer, String>, LinkedHashMap<String, Integer>>> getThreeLevelCategoryHierarchyTreeFromJSON_FS(
			String catHierarchyFileNameToRead)
	{
		// the original catIDs are alphanumerics, hence will assign new catIDs in order of parsing.
		LinkedHashMap<Integer, String> catIDNameMap = new LinkedHashMap<>();
		LinkedHashMap<String, Integer> catNameIDMap = new LinkedHashMap<>();
		int numOfCatIDs = 0;
		/////////////////////////////////////////

		String fileNameToRead = catHierarchyFileNameToRead;
		// "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/Gowalla/link to Gowalla dataset/another
		// source/gowalla/gowalla_category_structure.json";
		int countOfLines = -1;
		System.out.println("Inside getThreeLevelCategoryHierarchyTreeFromJSON()");

		/**
		 * keys are level 1 catid, and values are level2 and level3 children for correspnding level 1 catids.
		 **/
		TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>> categoryHierarchyMapLevel1 = new TreeMap<>();
		TreeItem<String> root = new TreeItem<>("-1:root"); // (catid:catName)
		///
		LinkedHashMap<Integer, String> level1Map = new LinkedHashMap<Integer, String>();
		LinkedHashMap<Integer, String> level2Map = new LinkedHashMap<Integer, String>();
		LinkedHashMap<Integer, String> level3Map = new LinkedHashMap<Integer, String>();
		///

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
			System.out.println("level0Keys = " + level0Keys);// level0Keys = [meta, response]
			level0Keys.remove("meta");// deleting "meta"
			System.out.println("level0Keys = " + level0Keys);
			if (level0Keys.size() != 1)
			{
				System.err.println("Error: incorrect tree: level0Keys.size() =" + level0Keys.size());
			}

			for (String level0Key : level0Keys) // only 1 key : response
			{
				JSONObject objForResponse = jObj.getJSONObject(level0Key);
				Set<String> keysInsideResponse = objForResponse.keySet();
				System.out.println("keysInsideResponse.keySet()=" + keysInsideResponse);// only 1 key categories

				if (keysInsideResponse.size() != 1)
				{
					PopUps.printTracedErrorMsgWithExit(
							"Error: incorrect tree: keysInsideResponse.size() =" + keysInsideResponse.size());
				}

				for (String keyInsideResponse : keysInsideResponse)//
				{
					System.out.println("keyInsideResponse = " + keyInsideResponse);// categories

					JSONArray level0Array = objForResponse.getJSONArray(keyInsideResponse);

					System.out.println("keyInsideResponse: key=" + keyInsideResponse + ", has an array of size ="
							+ level0Array.length());
					// level0: key=spot_categories, has an array of size =7

					for (int i = 0; i < level0Array.length(); i++)// 7
					{
						JSONObject level1Object = level0Array.getJSONObject(i);
						// String[] urlSplitted = level1Object.get("url").toString().split("/");
						// Integer catID1 = -1;// Integer.valueOf(urlSplitted[urlSplitted.length - 1]);
						String catID1Name = level1Object.get("name").toString().trim();
						Integer catID1 = catNameIDMap.get(catID1Name);
						if (catID1 == null)
						{
							catID1 = numOfCatIDs += 1;
						}
						System.out.println("\tlevel1: catID1Name = " + catID1Name + "; catID1 = " + catID1);
						// level1: catID1Name = Arts & Entertainment catID1 = 1

						if (categoryHierarchyMapLevel1.containsKey(catID1))
						{
							System.out.println("Alert in level1: catID " + catID1 + " already in level 1");
						}

						// level1 id, children of level1id
						TreeMap<Integer, TreeSet<Integer>> childrenOfThisLevel1ID = new TreeMap<Integer, TreeSet<Integer>>();
						TreeItem<String> level1IDT = new TreeItem<String>(String.valueOf(catID1) + ":" + catID1Name);

						/////
						if (level1Map.containsKey(catID1))
						{
							System.out.println("Alert in level1: catID " + catID1 + " already in level 1");
						}
						level1Map.put(catID1, catID1Name);
						catIDNameMap.put(catID1, catID1Name);
						catNameIDMap.put(catID1Name, catID1);
						/////

						JSONArray level1Array = (JSONArray) level1Object.get("categories");
						for (int j = 0; j < level1Array.length(); j++)
						{
							JSONObject level2Object = level1Array.getJSONObject(j);// e.g. Campus spot object

							// String[] urlSplitted2 = level2Object.get("url").toString().split("/");

							String catID2Name = level2Object.get("name").toString().trim();
							// Integer catID2 = -1;// Integer.valueOf(urlSplitted2[urlSplitted2.length - 1]);
							Integer catID2 = catNameIDMap.get(catID2Name);
							if (catID2 == null)
							{
								catID2 = numOfCatIDs += 1;
							}

							System.out.println("\t\tlevel2: name = " + catID2Name + " , catID = " + catID2);
							// level2: name = Campus Spot , catID = 133
							if (childrenOfThisLevel1ID.containsKey(catID2))
							{
								System.out.print("Alert! catID2 = " + catID2
										+ " is mentioned multiple times for level 1 catID " + catID1 + "\n");
							}
							// of level 2 // object e.g.,Campus spot
							TreeSet<Integer> childrenOfThisLevel2ID = new TreeSet<Integer>();
							TreeItem<String> level2IDT = new TreeItem<String>(
									String.valueOf(catID2) + ":" + catID2Name);
							/////
							if (level2Map.containsKey(catID2))
							{
								System.out.println("Alert in level2: catID " + catID2 + " already in level 2");
							}
							level2Map.put(catID2, catID2Name);
							catIDNameMap.put(catID2, catID2Name);
							catNameIDMap.put(catID2Name, catID2);
							/////

							JSONArray level2Array = (JSONArray) level2Object.get("categories");
							for (int k = 0; k < level2Array.length(); k++)
							{
								JSONObject level3Object = level2Array.getJSONObject(k);

								// String[] urlSplitted3 = level3Object.get("url").toString().split("/");
								// Integer catID3 = -1;// Integer.valueOf(urlSplitted3[urlSplitted3.length - 1]);
								String catID3Name = level3Object.get("name").toString().trim();
								Integer catID3 = catNameIDMap.get(catID3Name);
								if (catID3 == null)
								{
									catID3 = numOfCatIDs += 1;
								}

								System.out.println("\t\t\tlevel3: name = " + catID3Name + " ,  catID = " + catID3);

								if (childrenOfThisLevel2ID.contains(catID3))
								{
									System.out.print("Alert! catID3 = " + catID3
											+ " is mentioned multiple times for level 1 catID " + catID1 + " (catID2 = "
											+ catID2 + ")\n");
								}

								childrenOfThisLevel2ID.add(catID3);
								level2IDT.getChildren()
										.add(new TreeItem<String>(String.valueOf(catID3) + ":" + catID3Name));

								/////
								if (level3Map.containsKey(catID3))
								{
									System.out.println("Alert in level3: catID " + catID3 + " already in level 3");
								}
								level3Map.put(catID3, catID3Name);
								catIDNameMap.put(catID3, catID3Name);
								catNameIDMap.put(catID3Name, catID3);
								/////
							} // end of loop over array of level2 object... the array objects here are of level 3
							childrenOfThisLevel1ID.put(catID2, childrenOfThisLevel2ID);
							level1IDT.getChildren().add(level2IDT);
						} // end of loop over array of level1 object... the array objects here are of level 2
						categoryHierarchyMapLevel1.put(catID1, childrenOfThisLevel1ID);
						root.getChildren().add(level1IDT);
					}
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
				System.out.println(
						"Level 1 ID = " + level1CatID + " has " + level2Children.size() + " level 2 children.");

				for (Integer level2CatID : level2Children.keySet())
				{
					countOfCatIDsInThisMap += 1;
					allUniqueIDs.add(level2CatID);
					listOfAllIDs.add(level2CatID);

					TreeSet<Integer> level3Children = level2Children.get(level2CatID);
					System.out.println(
							"Level 2 ID = " + level2CatID + " has " + level3Children.size() + " level 3 children.");

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
			Set<Integer> duplicateIDs = UtilityBelt.findDuplicates(listOfAllIDs);
			System.out.println("Total num of duplicate cat id in categoryHierarchyMap = " + duplicateIDs.size());

			System.out.println("Duplicate IDs  = " + Arrays.toString(duplicateIDs.toArray()));
			System.out.println("=========================================");
			br.close();

			////////
			System.out.println("===========================");
			System.out.println("Num of level 1 catIDs = " + level1Map.size());
			System.out.println("Num of level 2 catIDs = " + level2Map.size());
			System.out.println("Num of level 3 catIDs = " + level3Map.size());

			Set<Integer> l1Keys = level1Map.keySet();
			Set<Integer> l2Keys = level2Map.keySet();
			Set<Integer> l3Keys = level3Map.keySet();
			System.out.println("Intersection of l1Keys and l2Keys = "
					+ Arrays.toString(UtilityBelt.getIntersection(l1Keys, l2Keys).toArray()));
			System.out.println("Intersection of l2Keys and l3Keys ="
					+ Arrays.toString(UtilityBelt.getIntersection(l2Keys, l3Keys).toArray()));
			System.out.println("Intersection of l1Keys and l3Keys ="
					+ Arrays.toString(UtilityBelt.getIntersection(l1Keys, l3Keys).toArray()));
			// Arrays.toString(children.toArray()
			System.out.println("Total num of catIDs = " + (level1Map.size() + level2Map.size() + level3Map.size()));

			HashMap<Integer, String> allUniques = new HashMap<Integer, String>();
			allUniques.putAll(level1Map);
			allUniques.putAll(level2Map);
			allUniques.putAll(level3Map);

			System.out.println("Total num of unique catIDs = " + (allUniques.size()));
			System.out.println("===========================");
			////////
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Exiting getThreeLevelCategoryHierarchyTreeFromJSON()\n");

		Pair<TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>>, TreeItem<String>> res1 = new Pair<>(
				categoryHierarchyMapLevel1, root);
		Triple<LinkedHashMap<Integer, String>, LinkedHashMap<Integer, String>, LinkedHashMap<Integer, String>> res2 = new Triple<>(
				level1Map, level2Map, level3Map);

		Pair<LinkedHashMap<Integer, String>, LinkedHashMap<String, Integer>> res3 = new Pair<LinkedHashMap<Integer, String>, LinkedHashMap<String, Integer>>(
				catIDNameMap, catNameIDMap);

		return new Triple<Pair<TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>>, TreeItem<String>>, Triple<LinkedHashMap<Integer, String>, LinkedHashMap<Integer, String>, LinkedHashMap<Integer, String>>, Pair<LinkedHashMap<Integer, String>, LinkedHashMap<String, Integer>>>(
				res1, res2, res3);
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
