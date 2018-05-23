diff --git a/GeolifeReloaded2_1_cleaned/bin/jfxtras/styles/jmetro8/GJMetroLightTheme.css b/GeolifeReloaded2_1_cleaned/bin/jfxtras/styles/jmetro8/GJMetroLightTheme.css
index 1cbb7cd5..53dc2658 100644
--- a/GeolifeReloaded2_1_cleaned/bin/jfxtras/styles/jmetro8/GJMetroLightTheme.css
+++ b/GeolifeReloaded2_1_cleaned/bin/jfxtras/styles/jmetro8/GJMetroLightTheme.css
@@ -755,10 +755,11 @@
  * RangeSlider                                                                      *
  *                                                                             *
  ******************************************************************************/
-
+/*
 .range-slider {
-    /*-fx-skin: "jfxtras.styles.jmetro8.FilledSliderSkin"; NOT WORKING*/
+    -fx-skin: "jfxtras.styles.jmetro8.FilledSliderSkin";
 }
+*/
 
 .range-slider .thumb {
     -fx-background-color: black;
@@ -794,12 +795,12 @@
 
 /*https://stackoverflow.com/questions/28721542/javafx-controlsfx-css-for-rangeslider
  * https://bitbucket.org/emxsys/javafx-chart-extensions/wiki/CSS%20Chart%20Styles*/
-/*.range-slider .low-thumb {
+.range-slider .low-thumb {
     -fx-background-radius: 0;
 }
 .range-slider .high-thumb {
     -fx-background-radius: 0;
-}*/
+}
 
 /*******************************************************************************
  *                                                                             *
diff --git a/GeolifeReloaded2_1_cleaned/bin/org/activity/constants/Constant.class b/GeolifeReloaded2_1_cleaned/bin/org/activity/constants/Constant.class
index f51aad7e..ba40ee80 100644
Binary files a/GeolifeReloaded2_1_cleaned/bin/org/activity/constants/Constant.class and b/GeolifeReloaded2_1_cleaned/bin/org/activity/constants/Constant.class differ
diff --git a/GeolifeReloaded2_1_cleaned/bin/org/activity/controller/ControllerWithoutServer.class b/GeolifeReloaded2_1_cleaned/bin/org/activity/controller/ControllerWithoutServer.class
index b51e0d01..8592c439 100644
Binary files a/GeolifeReloaded2_1_cleaned/bin/org/activity/controller/ControllerWithoutServer.class and b/GeolifeReloaded2_1_cleaned/bin/org/activity/controller/ControllerWithoutServer.class differ
diff --git a/GeolifeReloaded2_1_cleaned/bin/org/activity/controller/SuperController.class b/GeolifeReloaded2_1_cleaned/bin/org/activity/controller/SuperController.class
index d6c42417..81e8a054 100644
Binary files a/GeolifeReloaded2_1_cleaned/bin/org/activity/controller/SuperController.class and b/GeolifeReloaded2_1_cleaned/bin/org/activity/controller/SuperController.class differ
diff --git a/GeolifeReloaded2_1_cleaned/bin/org/activity/desktop/DesktopActivityDashBoard.class b/GeolifeReloaded2_1_cleaned/bin/org/activity/desktop/DesktopActivityDashBoard.class
index 0b209846..12b7e087 100644
Binary files a/GeolifeReloaded2_1_cleaned/bin/org/activity/desktop/DesktopActivityDashBoard.class and b/GeolifeReloaded2_1_cleaned/bin/org/activity/desktop/DesktopActivityDashBoard.class differ
diff --git a/GeolifeReloaded2_1_cleaned/bin/org/activity/featureExtraction/TimelinesAttributesExtraction.class b/GeolifeReloaded2_1_cleaned/bin/org/activity/featureExtraction/TimelinesAttributesExtraction.class
index 149a83dd..c90642f3 100644
Binary files a/GeolifeReloaded2_1_cleaned/bin/org/activity/featureExtraction/TimelinesAttributesExtraction.class and b/GeolifeReloaded2_1_cleaned/bin/org/activity/featureExtraction/TimelinesAttributesExtraction.class differ
diff --git a/GeolifeReloaded2_1_cleaned/bin/org/activity/featureExtraction/TimelinesAttributesExtractionCleaned1.class b/GeolifeReloaded2_1_cleaned/bin/org/activity/featureExtraction/TimelinesAttributesExtractionCleaned1.class
index 2dde8495..b1db04d9 100644
Binary files a/GeolifeReloaded2_1_cleaned/bin/org/activity/featureExtraction/TimelinesAttributesExtractionCleaned1.class and b/GeolifeReloaded2_1_cleaned/bin/org/activity/featureExtraction/TimelinesAttributesExtractionCleaned1.class differ
diff --git a/GeolifeReloaded2_1_cleaned/bin/org/activity/io/CSVUtils.class b/GeolifeReloaded2_1_cleaned/bin/org/activity/io/CSVUtils.class
index 8498dbd2..13280862 100644
Binary files a/GeolifeReloaded2_1_cleaned/bin/org/activity/io/CSVUtils.class and b/GeolifeReloaded2_1_cleaned/bin/org/activity/io/CSVUtils.class differ
diff --git a/GeolifeReloaded2_1_cleaned/bin/org/activity/io/WToFile.class b/GeolifeReloaded2_1_cleaned/bin/org/activity/io/WToFile.class
index 940e18d6..dd56964a 100644
Binary files a/GeolifeReloaded2_1_cleaned/bin/org/activity/io/WToFile.class and b/GeolifeReloaded2_1_cleaned/bin/org/activity/io/WToFile.class differ
diff --git a/GeolifeReloaded2_1_cleaned/bin/org/activity/objects/ActivityObject.class b/GeolifeReloaded2_1_cleaned/bin/org/activity/objects/ActivityObject.class
index 8f8ebc1d..6bd7dcef 100644
Binary files a/GeolifeReloaded2_1_cleaned/bin/org/activity/objects/ActivityObject.class and b/GeolifeReloaded2_1_cleaned/bin/org/activity/objects/ActivityObject.class differ
diff --git a/GeolifeReloaded2_1_cleaned/bin/org/activity/objects/Timeline.class b/GeolifeReloaded2_1_cleaned/bin/org/activity/objects/Timeline.class
index 1eee24ca..ca77c63a 100644
Binary files a/GeolifeReloaded2_1_cleaned/bin/org/activity/objects/Timeline.class and b/GeolifeReloaded2_1_cleaned/bin/org/activity/objects/Timeline.class differ
diff --git a/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/ActivityBoxExtraValues.class b/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/ActivityBoxExtraValues.class
index c7768d73..3ce07f2e 100644
Binary files a/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/ActivityBoxExtraValues.class and b/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/ActivityBoxExtraValues.class differ
diff --git a/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/ActivityCircle.class b/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/ActivityCircle.class
index 337e5ac4..ea4262a0 100644
Binary files a/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/ActivityCircle.class and b/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/ActivityCircle.class differ
diff --git a/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/ColorPalette.class b/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/ColorPalette.class
index c6edce87..26c84e32 100644
Binary files a/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/ColorPalette.class and b/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/ColorPalette.class differ
diff --git a/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/TimelineChart2.class b/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/TimelineChart2.class
index b9f0d07a..b4ece90a 100644
Binary files a/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/TimelineChart2.class and b/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/TimelineChart2.class differ
diff --git a/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/TimelineChartAppGeneric.class b/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/TimelineChartAppGeneric.class
index f59e212b..90cd45bc 100644
Binary files a/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/TimelineChartAppGeneric.class and b/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/TimelineChartAppGeneric.class differ
diff --git a/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/TimelineChartCircle.class b/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/TimelineChartCircle.class
index 78b3a1a1..9ee670a6 100644
Binary files a/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/TimelineChartCircle.class and b/GeolifeReloaded2_1_cleaned/bin/org/activity/plotting/TimelineChartCircle.class differ
diff --git a/GeolifeReloaded2_1_cleaned/bin/org/activity/stats/TimelineStats$1.class b/GeolifeReloaded2_1_cleaned/bin/org/activity/stats/TimelineStats$1.class
index e81a764e..0405babf 100644
Binary files a/GeolifeReloaded2_1_cleaned/bin/org/activity/stats/TimelineStats$1.class and b/GeolifeReloaded2_1_cleaned/bin/org/activity/stats/TimelineStats$1.class differ
diff --git a/GeolifeReloaded2_1_cleaned/bin/org/activity/stats/TimelineStats.class b/GeolifeReloaded2_1_cleaned/bin/org/activity/stats/TimelineStats.class
index a6796c82..7d69ed99 100644
Binary files a/GeolifeReloaded2_1_cleaned/bin/org/activity/stats/TimelineStats.class and b/GeolifeReloaded2_1_cleaned/bin/org/activity/stats/TimelineStats.class differ
diff --git a/GeolifeReloaded2_1_cleaned/bin/org/activity/ui/Dashboard3.class b/GeolifeReloaded2_1_cleaned/bin/org/activity/ui/Dashboard3.class
index 519fab19..30b96e91 100644
Binary files a/GeolifeReloaded2_1_cleaned/bin/org/activity/ui/Dashboard3.class and b/GeolifeReloaded2_1_cleaned/bin/org/activity/ui/Dashboard3.class differ
diff --git a/GeolifeReloaded2_1_cleaned/bin/org/activity/util/TimelineUtils.class b/GeolifeReloaded2_1_cleaned/bin/org/activity/util/TimelineUtils.class
index 924245fb..85d3c890 100644
Binary files a/GeolifeReloaded2_1_cleaned/bin/org/activity/util/TimelineUtils.class and b/GeolifeReloaded2_1_cleaned/bin/org/activity/util/TimelineUtils.class differ
diff --git a/GeolifeReloaded2_1_cleaned/build.gradle b/GeolifeReloaded2_1_cleaned/build.gradle
index 178dc1b0..98da80f6 100644
--- a/GeolifeReloaded2_1_cleaned/build.gradle
+++ b/GeolifeReloaded2_1_cleaned/build.gradle
@@ -86,8 +86,10 @@ dependencies
 	// compile 'org.controlsfx:controlsfx-samples:8.40.13'
 
 	// material design for javafx
-    //compile 'com.jfoenix:jfoenix:8.0.1'
+    compile 'com.jfoenix:jfoenix:8.0.1'
 	
+	//Icon packs for Java applications
+	compile 'org.kordamp.ikonli:ikonli-javafx:2.1.1'
 	//for amidst toolkit
 	//compile 'eu.amidst:module-all:0.6.1'
     //compile 'com.hugin:hugin:1.0.0'
diff --git a/GeolifeReloaded2_1_cleaned/src/main/java/jfxtras/styles/jmetro8/GJMetroLightTheme.css b/GeolifeReloaded2_1_cleaned/src/main/java/jfxtras/styles/jmetro8/GJMetroLightTheme.css
index 1cbb7cd5..53dc2658 100644
--- a/GeolifeReloaded2_1_cleaned/src/main/java/jfxtras/styles/jmetro8/GJMetroLightTheme.css
+++ b/GeolifeReloaded2_1_cleaned/src/main/java/jfxtras/styles/jmetro8/GJMetroLightTheme.css
@@ -755,10 +755,11 @@
  * RangeSlider                                                                      *
  *                                                                             *
  ******************************************************************************/
-
+/*
 .range-slider {
-    /*-fx-skin: "jfxtras.styles.jmetro8.FilledSliderSkin"; NOT WORKING*/
+    -fx-skin: "jfxtras.styles.jmetro8.FilledSliderSkin";
 }
+*/
 
 .range-slider .thumb {
     -fx-background-color: black;
@@ -794,12 +795,12 @@
 
 /*https://stackoverflow.com/questions/28721542/javafx-controlsfx-css-for-rangeslider
  * https://bitbucket.org/emxsys/javafx-chart-extensions/wiki/CSS%20Chart%20Styles*/
-/*.range-slider .low-thumb {
+.range-slider .low-thumb {
     -fx-background-radius: 0;
 }
 .range-slider .high-thumb {
     -fx-background-radius: 0;
-}*/
+}
 
 /*******************************************************************************
  *                                                                             *
diff --git a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/constants/Constant.java b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/constants/Constant.java
index 754f5ba7..ac0f88b8 100644
--- a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/constants/Constant.java
+++ b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/constants/Constant.java
@@ -345,6 +345,8 @@ public final class Constant
 	static String outputCoreResultsPath = "";
 
 	static String[] activityNames;
+	static Map<Integer, Integer> actIDNameIndexMap;// <actID, index of actID in activityNames array>
+
 	static Set<Integer> uniqueActivityIDs;
 	static Set<Integer> uniqueLocationIDs;
 
@@ -620,6 +622,8 @@ public final class Constant
 		{
 			editDistancesMemorizer = new EditDistanceMemorizer(Constant.editDistancesMemorizerBufferSize);
 		} // Constant.setDistanceUsed("HJEditDistance");
+
+		setActIDNameIndexMap(databaseName, Constant.getActivityNames());
 	}
 
 	//
@@ -660,6 +664,58 @@ public final class Constant
 	// }
 
 	// /////////
+	/**
+	 * Set actIDNameIndexMap which is map of <actID, index of actID in activityNames array>
+	 * 
+	 * @param databaseName
+	 * @param activityNames
+	 * @return
+	 */
+	private static boolean setActIDNameIndexMap(String databaseName, String[] activityNames)
+	{
+		Map<Integer, Integer> res = new LinkedHashMap<>(activityNames.length);
+
+		if (databaseName.equals("gowalla1"))
+		{
+			int index = 0;
+			for (String activityName : activityNames)
+			{ // since in gowalla dataset act name is act id
+				res.put(Integer.valueOf(activityName), index++);
+			}
+			actIDNameIndexMap = res;
+			return true;
+		}
+		else
+		{
+			PopUps.printTracedErrorMsgWithExit("Not checked correctness for databaseName=" + databaseName);
+		}
+		return false;
+
+	}
+
+	/**
+	 * Get actIDNameIndexMap which is map of <actID, index of actID in activityNames array>
+	 * 
+	 * @return
+	 */
+	public static Map<Integer, Integer> getActIDNameIndexMap()
+	{
+		return actIDNameIndexMap;
+	}
+
+	/**
+	 * Get index of actID in activityNames.
+	 * <p>
+	 * This can be obtained from the map of <actID, index of actID in activityNames array>
+	 */
+	public static Integer getIndexOfActIDInActNames(Integer actID)
+	{
+		// System.out.println("actIDNameIndexMap.size()=" + actIDNameIndexMap.size() + " given actID=" + actID
+		// + " contains=" + actIDNameIndexMap.containsKey(actID));
+
+		// System.out.println(actIDNameIndexMap);
+		return actIDNameIndexMap.get(actID);
+	}
 
 	public static Classifier getClassifierUsed()
 	{
@@ -936,7 +992,8 @@ public final class Constant
 					activityNames = res.toArray(new String[res.size()]);
 
 					// StringBuilder sb = new StringBuilder();
-					System.out.println(Arrays.asList(activityNames).stream().collect(Collectors.joining(",")));
+					System.out.println("Constant.activityNames=\n"
+							+ Arrays.asList(activityNames).stream().collect(Collectors.joining(",")));
 
 					// activityNamesGowallaLabels = (ArrayList<String>) Arrays.asList(activityNames).stream()
 					// .map(a -> DomainConstants.catIDNameDictionary.get(a)).collect(Collectors.toList());
diff --git a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/controller/ControllerWithoutServer.java b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/controller/ControllerWithoutServer.java
index 1f9f1f33..b2dce957 100644
--- a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/controller/ControllerWithoutServer.java
+++ b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/controller/ControllerWithoutServer.java
@@ -77,6 +77,7 @@ public class ControllerWithoutServer
 			// pathToLatestSerialisedTimelines = "", pathForLatestSerialisedTimelines = "", commonPath = "";
 
 			setPathsSerialisedJSONTimelines(databaseName);
+
 			// new ConnectDatabase(Constant.getDatabaseName()); // all method and variable in this class are static
 			// new Constant(commonPath, Constant.getDatabaseName());
 
@@ -91,7 +92,7 @@ public class ControllerWithoutServer
 			Constant.initialise(commonPath, databaseName, PathConstants.pathToSerialisedCatIDsHierDist,
 					PathConstants.pathToSerialisedCatIDNameDictionary, PathConstants.pathToSerialisedLocationObjects,
 					PathConstants.pathToSerialisedUserObjects, PathConstants.pathToSerialisedGowallaLocZoneIdMap);
-
+			String commonBasePath = Constant.getCommonPath();
 			System.out.println("Just after Constant.initialise:\n" + PerformanceAnalytics.getHeapInformation() + "\n"
 					+ PerformanceAnalytics.getHeapPercentageFree());
 
@@ -100,7 +101,7 @@ public class ControllerWithoutServer
 			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal = createAllTimelines(
 					databaseName, Constant.toSerializeJSONArray, Constant.toDeSerializeJSONArray,
 					Constant.toCreateTimelines, Constant.toSerializeTimelines, Constant.toDeSerializeTimelines);
-
+			//PopUps.showMessage("here0");
 			////////// ~~~~~~~~~~~~~~~~~`
 			//////////// for Gowalla start
 			// WritingToFile.writeUsersDayTimelinesSameFile(usersDayTimelinesOriginal, "usersDayTimelinesOriginal",
@@ -113,17 +114,19 @@ public class ControllerWithoutServer
 				if (Constant.For9kUsers)// For 9k users
 				{
 					usersCleanedDayTimelines = reduceAndCleanTimelines2(databaseName, usersDayTimelinesOriginal, true,
-							Constant.getCommonPath(), 10, 7, 500);
+							commonBasePath, 10, 7, 500);
 				}
 				else // For 916 users
 				{
-					usersCleanedDayTimelines = reduceAndCleanTimelines(databaseName, usersDayTimelinesOriginal, true);
+					usersCleanedDayTimelines = reduceAndCleanTimelines(databaseName, usersDayTimelinesOriginal, true,
+							commonBasePath);
 				}
 			}
 			else// in this case, we are expecting the data is already subsetting and cleaned
 			{
 				usersCleanedDayTimelines = usersDayTimelinesOriginal;
-				writeTimelineStats(usersCleanedDayTimelines, false, true, true, true, "usersCleanedDayTimelines");
+				writeTimelineStats(usersCleanedDayTimelines, false, true, true, true, "UsersCleanedDayTimelines",
+						commonBasePath);
 			}
 
 			usersDayTimelinesOriginal = null; // null this out so as to be ready for garbage collection.
@@ -160,19 +163,14 @@ public class ControllerWithoutServer
 			// String groupsOf100UsersLabels[] = { "1", "101", "201", "301", "401", "501", "601", "701", "801", "901" };
 			// ,// "1001" };
 			// System.out.println("List of all users:\n" + usersCleanedDayTimelines.keySet().toString() + "\n");
+			// String commonBasePath = Constant.getCommonPath();
+			//PopUps.showMessage("here01");
+			TimelineStats.writeNumOfDaysPerUsersDayTimelinesSameFile(usersCleanedDayTimelines,
+					commonBasePath + "NumOfDaysPerUsersDayTimelines.csv");
 
-			String s1;
-			String s2;
-			s2 = Constant.getCommonPath();
-			s1 = s2 + "NumOfDaysPerUsersDayTimelines.csv";
-			TimelineStats.writeNumOfDaysPerUsersDayTimelinesSameFile(usersCleanedDayTimelines, s1);
-			String commonBasePath;
-			commonBasePath = Constant.getCommonPath();
-			String s3;
-			String s4 = PerformanceAnalytics.getHeapInformation();
-			s3 = "Before sampleUsersExec\n" + s4;
-			System.out.println(s3);
-			
+			System.out.println("Before sampleUsersExec\n" + PerformanceAnalytics.getHeapInformation());
+			//PopUps.showMessage("here02");
+			///
 
 			///////////////////
 			// TimelineUtils.writeNumOfNullTZCinsPerUserPerLocID(usersCleanedDayTimelines,
@@ -185,73 +183,23 @@ public class ControllerWithoutServer
 			Constant.setUniqueLocIDs(TimelineUtils.getUniqueLocIDs(usersCleanedDayTimelines, true));
 			Constant.setUniqueActivityIDs(TimelineUtils.getUniqueActivityIDs(usersCleanedDayTimelines, true));
 			TimelineUtils.getUniquePDValPerUser(usersCleanedDayTimelines, true, "NumOfUniquePDValPerUser.csv");
+			writeActIDNamesInFixedOrder(Constant.getCommonPath() + "CatIDNameMap.csv");
 
 			if (false)// temporary
 			{
 				TimelineUtils.writeAllActObjs(usersCleanedDayTimelines, Constant.getCommonPath() + "AllActObjs.csv");
 				TimelineUtils.writeLocationObjects(Constant.getUniqueLocIDs(),
 						DomainConstants.getLocIDLocationObjectDictionary(),
-						Constant.getCommonPath() + "UniqueLocationObjects.csv");
+						commonBasePath + "UniqueLocationObjects.csv");
 				// SpatialUtils.createLocationDistanceDatabase(DomainConstants.getLocIDLocationObjectDictionary());
 				TimelineUtils.writeUserObjects(usersCleanedDayTimelines.keySet(),
-						DomainConstants.getUserIDUserObjectDictionary(),
-						Constant.getCommonPath() + "UniqueUserObjects.csv");
+						DomainConstants.getUserIDUserObjectDictionary(), commonBasePath + "UniqueUserObjects.csv");
 				System.exit(0);
 			}
 
 			if (false)// temporary for 22 feb 2018,
-			{// temporary for 22 feb 2018, to find the unique locations in the training timelines (most recent five
-				// days) and test timelines, this chunk of code has been borrowed from
-				// RecommendationtestsMar2017GenSeq3Nov2017.java
-				LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersDW = null;
-				// training test timelines for all users continuous
-				LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuous = null;
-
-				long tt1 = System.currentTimeMillis();
-				if (Constant.collaborativeCandidates)
-				{
-					trainTestTimelinesForAllUsersDW = TimelineUtils.splitAllUsersTestTrainingTimelines(
-							usersCleanedDayTimelines, Constant.percentageInTraining,
-							Constant.cleanTimelinesAgainInsideTrainTestSplit);
-
-					if (Constant.filterTrainingTimelinesByRecentDays)
-					{
-						trainTimelinesAllUsersContinuous = RecommendationTestsMar2017GenSeqCleaned3Nov2017
-								.getContinousTrainingTimelinesWithFilterByRecentDaysV2(trainTestTimelinesForAllUsersDW,
-										Constant.getRecentDaysInTrainingTimelines());
-					}
-					else
-					{
-						// sampledUsersTimelines
-						trainTimelinesAllUsersContinuous = RecommendationTestsMar2017GenSeqCleaned3Nov2017
-								.getContinousTrainingTimelines(trainTestTimelinesForAllUsersDW);
-					}
-				}
-				System.out.println("time take for timeline train test splitting which might be save in experiment ="
-						+ ((System.currentTimeMillis() - tt1) * 1.0) / 1000 + " secs");
-
-				Set<Integer> uniqueLocTrains = TimelineUtils.getUniqueLocIDs(trainTimelinesAllUsersContinuous, true,
-						Constant.getCommonPath() + "UniqueLocIDs5DaysTrain.csv");
-				Set<Integer> uniqueLocTests = TimelineUtils.getUniqueLocIDsFromTestOnly(trainTestTimelinesForAllUsersDW,
-						true, Constant.getCommonPath() + "UniqueLocIDsTest.csv");
-
-				Set<Integer> uniqueLocTrainsTests = new TreeSet<>();
-				uniqueLocTrainsTests.addAll(uniqueLocTrains);
-				uniqueLocTrainsTests.addAll(uniqueLocTests);
-				WToFile.writeToNewFile(uniqueLocTrainsTests.stream().map(e -> e.toString())
-						.collect(Collectors.joining("\n")).toString(),
-						Constant.getCommonPath() + "UniqueLocIDs5DaysTrainTest.csv");
-
-				TimelineUtils.writeLocationObjects(uniqueLocTrainsTests,
-						DomainConstants.getLocIDLocationObjectDictionary(),
-						Constant.getCommonPath() + "UniqueLocationObjects5DaysTrainTest.csv");
-
-				TimelineUtils.writeAllActObjs(trainTimelinesAllUsersContinuous,
-						Constant.getCommonPath() + "AllActObjs5DaysTrain.csv");
-				TimelineUtils.writeAllActObjsFromTestOnly(trainTestTimelinesForAllUsersDW,
-						Constant.getCommonPath() + "AllActObjsTest.csv");
-				// TimelineUtils.countNumOfMultipleLocationIDs(usersCleanedDayTimelines);
-				System.exit(0);
+			{
+				findUniqueLocationsInTrainTest(usersCleanedDayTimelines, true);
 			}
 			// Curtain 8 Feb 2018 start
 			// $$TimelineUtils.writeAllActObjs(usersCleanedDayTimelines, Constant.getCommonPath() + "AllActObjs.csv");
@@ -274,51 +222,27 @@ public class ControllerWithoutServer
 			// // important curtain 1 start 21 Dec 2017 10 Feb 2017
 			DomainConstants.clearGowallaLocZoneIdMap();// to save memory
 
-			if (Constant.For9kUsers)
+			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayToyTimelines = TimelineUtils
+					.createToyUserTimelinesFromCheckinEntriesGowallaFaster1_V2(usersCleanedDayTimelines);
 			{
-				// Start of curtain Aug 14 2017
-				// $$selectGivenUsersExecuteRecommendationTests(usersCleanedDayTimelines, IntStream
-				// $$ .of(DomainConstants.gowallaUserIDInUserGroup1Users).boxed().collect(Collectors.toList()),
-				// $$commonBasePath, "1");
-				// End of curtain Aug 14 2017
-				boolean useSampledUsersFromFile = true;
-				ArrayList<ArrayList<String>> listOfSampledUserIDs = null;
-				if (useSampledUsersFromFile)
-				{
-					String sampledUsersListFile = "./dataToRead/Jan16/randomlySampleUsers.txt";
-					System.out.println("Reading Sampled users from " + sampledUsersListFile);
-					listOfSampledUserIDs = ReadingFromFile.readRandomSamplesIntoListOfLists(sampledUsersListFile, 13,
-							21, ",");
+				Serializer.kryoSerializeThis(usersCleanedDayToyTimelines,
+						Constant.getCommonPath() + "ToyTimelines21May.kryo");
+				System.exit(0);
+			}
 
-					int listNum = 0;
-					for (ArrayList<String> l : listOfSampledUserIDs)
-					{
-						System.out.println("List num:" + (++listNum));
-						System.out.println(l.toString());
-					}
-				}
-				else
-				{
-					System.out.println("New Randomly Sampling users");
-					listOfSampledUserIDs = randomlySampleUsersIDs(usersCleanedDayTimelines, 9, 1000);
-				}
+			TimelineStats.timelineStatsController(usersCleanedDayToyTimelines);
+			PopUps.showMessage("here");
+			WToFile.writeUsersDayTimelinesSameFile(usersCleanedDayToyTimelines, "usersCleanedDayToyTimelines", false,
+					false, false, "GowallaUserDayToyTimelines.csv", commonBasePath);
+			PopUps.showMessage("here2");
+			// $$TimelineStats.timelineStatsController(usersCleanedDayTimelines);
+			System.exit(0);
+			// End of Moved here on 18 May 2018
 
-				for (int sampleID = 0; sampleID < listOfSampledUserIDs.size(); sampleID++)
-				{
-					System.out.println(" listOfSampledUserIDs.get(sampleID)= " + listOfSampledUserIDs.get(sampleID));
-					System.out.println(
-							" listOfSampledUserIDs.get(sampleID).size= " + listOfSampledUserIDs.get(sampleID).size());
-
-					LinkedHashMap<String, LinkedHashMap<Date, Timeline>> sampledUserCleanedDayTimelines = getDayTimelinesForUserIDsV2(
-							usersCleanedDayTimelines, listOfSampledUserIDs.get(sampleID));
-					System.out
-							.println("sampledUserCleanedDayTimelines.size()=" + sampledUserCleanedDayTimelines.size());
-					sampleUsersExecuteRecommendationTests(sampledUserCleanedDayTimelines,
-							DomainConstants.gowallaUserGroupsLabels, commonBasePath + "Sample" + sampleID + "/");
-
-					new EvaluationSeq(3, commonBasePath + "Sample" + sampleID + "/",
-							Constant.getMatchingUnitArray(Constant.lookPastType, Constant.altSeqPredictor));
-				}
+			if (Constant.For9kUsers)
+			{
+				sampleUsersExecuteExperimentsFor9kUsers(commonBasePath, usersCleanedDayTimelines,
+						Constant.getMatchingUnitArray(Constant.lookPastType, Constant.altSeqPredictor));
 				// System.exit(0);
 			}
 			else
@@ -327,7 +251,6 @@ public class ControllerWithoutServer
 				{
 					List<String> sampledUserIndicesStr = ReadingFromFile
 							// .oneColumnReaderString("./dataToRead/RandomlySample100Users/Mar1_2018.csv", ",", 0,
-							// false);
 							// .oneColumnReaderString("./dataToRead/RandomlySample100UsersApril24_2018.csv", ",", 0,
 							.oneColumnReaderString(Constant.pathToRandomLySampleUserIndices, ",", 0, false);
 					System.out.println("pathToRandomLySampleUserIndices=" + Constant.pathToRandomLySampleUserIndices);
@@ -460,6 +383,143 @@ public class ControllerWithoutServer
 
 	}
 
+	private void writeActIDNamesInFixedOrder(String absFileNameToWrite)
+	{
+		String[] activityNames = Constant.getActivityNames();
+		StringBuilder sb = new StringBuilder("ActID,ActName\n");
+		for (String a : activityNames)
+		{
+			sb.append(a + "," + DomainConstants.catIDNameDictionary.get(Integer.valueOf(a)) + "\n");
+		}
+		WToFile.writeToNewFile(sb.toString(), absFileNameToWrite);// Constant.getCommonPath() + "CatIDNameMap.csv");
+	}
+
+	/**
+	 * <p>
+	 * logic extracted to a method on 18 May 2018, before it was part of ControllerWithoutServer constructor. Not sure
+	 * whether it should be static or non-static. However, keeping it static at the moment to avoid chances of unwanted
+	 * state changes.
+	 * 
+	 * @param commonBasePath
+	 * @param usersCleanedDayTimelines
+	 * @param muArray
+	 * @throws IOException
+	 */
+	private static void sampleUsersExecuteExperimentsFor9kUsers(String commonBasePath,
+			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, double[] muArray)
+			throws IOException
+	{
+		// Start of curtain Aug 14 2017
+		// $$selectGivenUsersExecuteRecommendationTests(usersCleanedDayTimelines, IntStream
+		// $$ .of(DomainConstants.gowallaUserIDInUserGroup1Users).boxed().collect(Collectors.toList()),
+		// $$commonBasePath, "1");
+		// End of curtain Aug 14 2017
+		boolean useSampledUsersFromFile = true;
+		ArrayList<ArrayList<String>> listOfSampledUserIDs = null;
+		if (useSampledUsersFromFile)
+		{
+			String sampledUsersListFile = "./dataToRead/Jan16/randomlySampleUsers.txt";
+			System.out.println("Reading Sampled users from " + sampledUsersListFile);
+			listOfSampledUserIDs = ReadingFromFile.readRandomSamplesIntoListOfLists(sampledUsersListFile, 13, 21, ",");
+
+			int listNum = 0;
+			for (ArrayList<String> l : listOfSampledUserIDs)
+			{
+				System.out.println("List num:" + (++listNum));
+				System.out.println(l.toString());
+			}
+		}
+		else
+		{
+			System.out.println("New Randomly Sampling users");
+			listOfSampledUserIDs = randomlySampleUsersIDs(usersCleanedDayTimelines, 9, 1000);
+		}
+
+		for (int sampleID = 0; sampleID < listOfSampledUserIDs.size(); sampleID++)
+		{
+			System.out.println(" listOfSampledUserIDs.get(sampleID)= " + listOfSampledUserIDs.get(sampleID));
+			System.out
+					.println(" listOfSampledUserIDs.get(sampleID).size= " + listOfSampledUserIDs.get(sampleID).size());
+
+			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> sampledUserCleanedDayTimelines = getDayTimelinesForUserIDsV2(
+					usersCleanedDayTimelines, listOfSampledUserIDs.get(sampleID));
+
+			System.out.println("sampledUserCleanedDayTimelines.size()=" + sampledUserCleanedDayTimelines.size());
+
+			sampleUsersExecuteRecommendationTests(sampledUserCleanedDayTimelines,
+					DomainConstants.gowallaUserGroupsLabels, commonBasePath + "Sample" + sampleID + "/");
+
+			new EvaluationSeq(3, commonBasePath + "Sample" + sampleID + "/", muArray);
+		}
+
+	}
+
+	/**
+	 * // temporary for 22 feb 2018, to find the unique locations in the training timelines (most recent five // days)
+	 * and test timelines, this chunk of code has been borrowed from // RecommendationtestsMar2017GenSeq3Nov2017.java
+	 * 
+	 * @param usersCleanedDayTimelines
+	 * @param exit
+	 * @since 22 feb 2018
+	 */
+	private void findUniqueLocationsInTrainTest(
+			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, boolean exit)
+	{
+		// temporary for 22 feb 2018, to find the unique locations in the training timelines (most recent five
+		// days) and test timelines, this chunk of code has been borrowed from
+		// RecommendationtestsMar2017GenSeq3Nov2017.java
+		LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersDW = null;
+		// training test timelines for all users continuous
+		LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuous = null;
+
+		long tt1 = System.currentTimeMillis();
+		if (Constant.collaborativeCandidates)
+		{
+			trainTestTimelinesForAllUsersDW = TimelineUtils.splitAllUsersTestTrainingTimelines(usersCleanedDayTimelines,
+					Constant.percentageInTraining, Constant.cleanTimelinesAgainInsideTrainTestSplit);
+
+			if (Constant.filterTrainingTimelinesByRecentDays)
+			{
+				trainTimelinesAllUsersContinuous = RecommendationTestsMar2017GenSeqCleaned3Nov2017
+						.getContinousTrainingTimelinesWithFilterByRecentDaysV2(trainTestTimelinesForAllUsersDW,
+								Constant.getRecentDaysInTrainingTimelines());
+			}
+			else
+			{
+				// sampledUsersTimelines
+				trainTimelinesAllUsersContinuous = RecommendationTestsMar2017GenSeqCleaned3Nov2017
+						.getContinousTrainingTimelines(trainTestTimelinesForAllUsersDW);
+			}
+		}
+		System.out.println("time take for timeline train test splitting which might be save in experiment ="
+				+ ((System.currentTimeMillis() - tt1) * 1.0) / 1000 + " secs");
+
+		Set<Integer> uniqueLocTrains = TimelineUtils.getUniqueLocIDs(trainTimelinesAllUsersContinuous, true,
+				Constant.getCommonPath() + "UniqueLocIDs5DaysTrain.csv");
+		Set<Integer> uniqueLocTests = TimelineUtils.getUniqueLocIDsFromTestOnly(trainTestTimelinesForAllUsersDW, true,
+				Constant.getCommonPath() + "UniqueLocIDsTest.csv");
+
+		Set<Integer> uniqueLocTrainsTests = new TreeSet<>();
+		uniqueLocTrainsTests.addAll(uniqueLocTrains);
+		uniqueLocTrainsTests.addAll(uniqueLocTests);
+		WToFile.writeToNewFile(
+				uniqueLocTrainsTests.stream().map(e -> e.toString()).collect(Collectors.joining("\n")).toString(),
+				Constant.getCommonPath() + "UniqueLocIDs5DaysTrainTest.csv");
+
+		TimelineUtils.writeLocationObjects(uniqueLocTrainsTests, DomainConstants.getLocIDLocationObjectDictionary(),
+				Constant.getCommonPath() + "UniqueLocationObjects5DaysTrainTest.csv");
+
+		TimelineUtils.writeAllActObjs(trainTimelinesAllUsersContinuous,
+				Constant.getCommonPath() + "AllActObjs5DaysTrain.csv");
+		TimelineUtils.writeAllActObjsFromTestOnly(trainTestTimelinesForAllUsersDW,
+				Constant.getCommonPath() + "AllActObjsTest.csv");
+		// TimelineUtils.countNumOfMultipleLocationIDs(usersCleanedDayTimelines);
+		if (exit)
+		{
+			System.exit(0);
+		}
+	}
+
 	/**
 	 * 
 	 * @param usersCleanedDayTimelines
@@ -467,7 +527,7 @@ public class ControllerWithoutServer
 	 * @param sizeOfEachSublist
 	 * @return
 	 */
-	private ArrayList<ArrayList<String>> randomlySampleUsersIDs(
+	private static ArrayList<ArrayList<String>> randomlySampleUsersIDs(
 			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, int numOfSublists,
 			int sizeOfEachSublist)
 	{
@@ -603,7 +663,7 @@ public class ControllerWithoutServer
 	 * @param commonBasePath
 	 * @throws IOException
 	 */
-	private void sampleUsersExecuteRecommendationTests(
+	private static void sampleUsersExecuteRecommendationTests(
 			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines,
 			String[] groupsOf100UsersLabels, String commonBasePath) throws IOException
 	{
@@ -615,7 +675,7 @@ public class ControllerWithoutServer
 		{
 			System.out.println("-- iteration start for groupsOf100UsersLabel = " + groupsOf100UsersLabel);
 			// important so as to wipe the previously assigned user ids
-			Constant.initialise(commonPath, Constant.getDatabaseName());
+			Constant.initialise(commonBasePath, Constant.getDatabaseName());
 
 			int startUserIndex = Integer.valueOf(groupsOf100UsersLabel) - 1;// 100
 			int endUserIndex = startUserIndex + 99;// $$ should be 99;// 199;// 140; // 199
@@ -1053,7 +1113,7 @@ public class ControllerWithoutServer
 	 * @param userIDsToSelect
 	 * @return day timelines for given user ids
 	 */
-	public LinkedHashMap<String, LinkedHashMap<Date, Timeline>> getDayTimelinesForUserIDsV2(
+	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> getDayTimelinesForUserIDsV2(
 			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines,
 			ArrayList<String> userIDsToSelect)
 	{
@@ -1082,8 +1142,14 @@ public class ControllerWithoutServer
 	}
 
 	/**
-	 * Sets pathToLatestSerialisedJSONArray, pathForLatestSerialisedJSONArray, pathToLatestSerialisedTimelines,
-	 * pathForLatestSerialisedTimelines
+	 * Sets the following paths:
+	 * <p>
+	 * <ul>
+	 * <li>pathToLatestSerialisedJSONArray</li>
+	 * <li>pathForLatestSerialisedJSONArray</li>
+	 * <li>pathToLatestSerialisedTimelines</li>
+	 * <li>commonPath</li>
+	 * </ul>
 	 * 
 	 * @param databaseName
 	 */
@@ -1306,7 +1372,8 @@ public class ControllerWithoutServer
 		// Originally received timelines
 		if (writeToFile)
 		{
-			writeTimelineStats(usersDayTimelinesOriginal, false, true, true, true, "OriginalBeforeReduceClean");
+			writeTimelineStats(usersDayTimelinesOriginal, false, true, true, true, "OriginalBeforeReduceClean",
+					commonPath);
 		}
 
 		if (databaseName.equals("gowalla1"))
@@ -1322,7 +1389,7 @@ public class ControllerWithoutServer
 				.cleanUsersDayTimelines(usersDayTimelinesOriginal);
 		if (writeToFile)
 		{
-			writeTimelineStats(usersCleanedDayTimelines, false, true, true, false, "Cleaned");
+			writeTimelineStats(usersCleanedDayTimelines, false, true, true, false, "Cleaned", commonPath);
 		}
 		///
 
@@ -1334,7 +1401,7 @@ public class ControllerWithoutServer
 
 		if (writeToFile)
 		{
-			writeTimelineStats(usersDayTimelinesOriginal, true, true, true, true, "cleaned reduced3");
+			writeTimelineStats(usersDayTimelinesOriginal, true, true, true, true, "cleaned reduced3", commonPath);
 		}
 
 		return usersCleanedDayTimelines;
@@ -1359,21 +1426,24 @@ public class ControllerWithoutServer
 	 * @param databaseName
 	 * @param usersDayTimelinesOriginal
 	 * @param writeToFile
+	 * @param commonPathToWrite
 	 * @return
 	 */
 	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> reduceAndCleanTimelines(String databaseName,
-			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal, boolean writeToFile)
+			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal, boolean writeToFile,
+			String commonPathToWrite)
 	{
 		// Originally received timelines
 		if (writeToFile)
 		{
-			writeTimelineStats(usersDayTimelinesOriginal, false, true, true, true, "OriginalBeforeReduceClean");
+			writeTimelineStats(usersDayTimelinesOriginal, false, true, true, true, "OriginalBeforeReduceClean",
+					commonPathToWrite);
 		}
 
 		if (databaseName.equals("gowalla1"))
 		{
 			usersDayTimelinesOriginal = reduceGowallaTimelinesByActDensity(databaseName, usersDayTimelinesOriginal,
-					true, 10, 50);
+					true, 10, 50, commonPathToWrite);
 		}
 
 		///// clean timelines
@@ -1384,7 +1454,7 @@ public class ControllerWithoutServer
 		if (writeToFile)
 		{
 			writeTimelineStats(usersCleanedDayTimelines, false, true, true, false,
-					"RemovedLT10ActPerDayLT50DaysCleaned");
+					"RemovedLT10ActPerDayLT50DaysCleaned", commonPathToWrite);
 		}
 
 		if (databaseName.equals("gowalla1"))
@@ -1396,7 +1466,7 @@ public class ControllerWithoutServer
 			if (writeToFile)
 			{
 				writeTimelineStats(usersCleanedDayTimelines, false, true, true, false,
-						"RemovedLT10ActPerDayLT50DaysCleanedLT50Days");
+						"RemovedLT10ActPerDayLT50DaysCleanedLT50Days", commonPathToWrite);
 			}
 
 			/////////
@@ -1408,7 +1478,7 @@ public class ControllerWithoutServer
 			if (writeToFile)
 			{
 				writeTimelineStats(usersCleanedDayTimelines, true, true, true, true,
-						"RemovedLT10ActPerDayLT50DaysCleanedLT50DaysBlUsers");
+						"RemovedLT10ActPerDayLT50DaysCleanedLT50DaysBlUsers", commonPathToWrite);
 			}
 		}
 		return usersCleanedDayTimelines;
@@ -1422,11 +1492,12 @@ public class ControllerWithoutServer
 	 * @param writeNumOfDaysPerUsersDayTimelines
 	 * @param writeNumOfDistinctValidActsPerUsersDayTimelines
 	 * @param labelEnd
+	 * @param commonPathToWrite
 	 */
 	public static void writeTimelineStats(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> timelines,
 			boolean writeSubsetOfTimelines, boolean writeNumOfActsPerUsersDayTimelines,
 			boolean writeNumOfDaysPerUsersDayTimelines, boolean writeNumOfDistinctValidActsPerUsersDayTimelines,
-			String labelEnd)
+			String labelEnd, String commonPathToWrite)
 	{
 		// Writing user day timelines. big file ~ 17.3GB
 		// WritingToFile.writeUsersDayTimelinesSameFile(timelines,"usersCleanedDayTimelinesReduced"+labelEnd,false,
@@ -1440,23 +1511,24 @@ public class ControllerWithoutServer
 			WToFile.writeUsersDayTimelinesSameFile(
 					new LinkedHashMap<String, LinkedHashMap<Date, Timeline>>(timelinesSampled),
 					"usersDayTimelines" + labelEnd + "First2UsersOnly", false, false, false,
-					"GowallaUserDayTimelines" + labelEnd + "First2UsersOnly.csv");// users
+					"GowallaUserDayTimelines" + labelEnd + "First2UsersOnly.csv", commonPathToWrite);// users
 		}
 		if (writeNumOfActsPerUsersDayTimelines)
 		{
 			TimelineStats.writeNumOfActsPerUsersDayTimelinesSameFile(timelines, "usersDayTimelines" + labelEnd,
-					"GowallaPerUserDayNumOfActs" + labelEnd + ".csv");
+					"GowallaPerUserDayNumOfActs" + labelEnd + ".csv", commonPathToWrite);
 		}
 		if (writeNumOfDaysPerUsersDayTimelines)
 		{
 			TimelineStats.writeNumOfDaysPerUsersDayTimelinesSameFile(timelines,
-					Constant.getCommonPath() + "NumOfDaysPerUser" + labelEnd + ".csv");
+					commonPathToWrite + "NumOfDaysPerUser" + labelEnd + ".csv");
 
 		}
 		if (writeNumOfDistinctValidActsPerUsersDayTimelines)
 		{
-			TimelineStats.writeNumOfDistinctValidActsPerUsersDayTimelinesSameFile(timelines, "usersDayTimelines" + labelEnd,
-					"GowallaPerUserDayNumOfDistinctValidActs" + labelEnd + ".csv");
+			TimelineStats.writeNumOfDistinctValidActsPerUsersDayTimelinesSameFile(timelines,
+					"usersDayTimelines" + labelEnd, "GowallaPerUserDayNumOfDistinctValidActs" + labelEnd + ".csv",
+					commonPathToWrite);
 		}
 		System.out.println(" Num of users" + labelEnd + "= " + timelines.size());
 	}
@@ -1500,7 +1572,7 @@ public class ControllerWithoutServer
 					commonPath + "removeDayTimelinesWithLessThan" + actsPerDayLowerLimit + "ActLog.csv", writeLogs);
 			if (writeToFile)
 			{
-				writeTimelineStats(usersDayTimelinesOriginal, false, true, true, false, labelEnd);
+				writeTimelineStats(usersDayTimelinesOriginal, false, true, true, false, labelEnd, commonPath);
 			}
 		}
 		//////////
@@ -1516,7 +1588,7 @@ public class ControllerWithoutServer
 
 			if (writeToFile)
 			{
-				writeTimelineStats(usersDayTimelinesOriginal, false, true, true, false, labelEnd);
+				writeTimelineStats(usersDayTimelinesOriginal, false, true, true, false, labelEnd, commonPath);
 			}
 		}
 		//////////
@@ -1531,7 +1603,7 @@ public class ControllerWithoutServer
 					commonPath + "removeDayTimelinesWithLessThan" + numOfSuchDaysLowerLimit + "DaysLog.csv");
 			if (writeToFile)
 			{
-				writeTimelineStats(usersDayTimelinesOriginal, false, true, true, false, labelEnd);
+				writeTimelineStats(usersDayTimelinesOriginal, false, true, true, false, labelEnd, commonPath);
 
 			}
 		}
@@ -1552,11 +1624,12 @@ public class ControllerWithoutServer
 	 * @param writeToFile
 	 * @param actsPerDayThreshold
 	 * @param suchDaysThreshold
+	 * @param commonPath
 	 * @return
 	 */
 	public static LinkedHashMap<String, LinkedHashMap<Date, Timeline>> reduceGowallaTimelinesByActDensity(
 			String databaseName, LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesOriginal,
-			boolean writeToFile, int actsPerDayThreshold, int suchDaysThreshold)
+			boolean writeToFile, int actsPerDayThreshold, int suchDaysThreshold, String commonPath)
 	{// formerly reduceGowallaTimelines
 		System.out.println("Inside reduceGowallaTimelinesByActDensity");
 
@@ -1580,7 +1653,7 @@ public class ControllerWithoutServer
 			if (writeToFile)
 			{
 				writeTimelineStats(usersDayTimelinesOriginal, false, true, true, false,
-						"RemovedLT" + actsPerDayThreshold + "ActPerDay");
+						"RemovedLT" + actsPerDayThreshold + "ActPerDay", commonPath);
 			}
 
 			//////////
@@ -1593,7 +1666,7 @@ public class ControllerWithoutServer
 			if (writeToFile)
 			{
 				writeTimelineStats(usersDayTimelinesOriginal, false, true, true, false,
-						"RemovedLT" + actsPerDayThreshold + "ActPerDayLT" + suchDaysThreshold + "Days");
+						"RemovedLT" + actsPerDayThreshold + "ActPerDayLT" + suchDaysThreshold + "Days", commonPath);
 			}
 
 			return usersDayTimelinesOriginal;
diff --git a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/controller/SuperController.java b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/controller/SuperController.java
index 420abcc7..b110e8cc 100644
--- a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/controller/SuperController.java
+++ b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/controller/SuperController.java
@@ -353,7 +353,7 @@ public class SuperController
 		// String commonPath = "./dataWritten/Nov6_NCount916U916N100T/";// Aug17/";
 		// $$String commonPath = "./dataWritten/Nov12_NCount916U916N1C500T/";// Aug17/";
 		System.out.println("commonPath = " + commonPath);
-		String commonPathGowalla = commonPath;
+		String outputCoreResultsPathGowalla = commonPath;
 		// + "./dataWrittenNGramBaselineForUserNumInvestigation/";// dataWrittenSeqEditL1
 		// RecommUnmergedNCount/";
 		// $$"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/Timelines/";
@@ -421,8 +421,9 @@ public class SuperController
 
 		Constant.setDatabaseName("gowalla1");// ("dcu_data_2");// "geolife1"
 		// Constant.caseType = Enums.CaseType.SimpleV3;/// "SimpleV3";// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
-		Constant.setOutputCoreResultsPath(commonPathGowalla);// commonPathGeolife;// commonPathDCU + "SimpleV3/";//
-																// "/home/gunjan/DCU/SimpleV3/";//
+		Constant.setOutputCoreResultsPath(outputCoreResultsPathGowalla);// commonPathGeolife;// commonPathDCU +
+																		// "SimpleV3/";//
+		// "/home/gunjan/DCU/SimpleV3/";//
 		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April16_2015/DCUData/SimpleV3/";
 		Constant.setDistanceUsed("HJEditDistance");
 
diff --git a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/desktop/DesktopActivityDashBoard.java b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/desktop/DesktopActivityDashBoard.java
index 4af8b3cd..030765d0 100644
--- a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/desktop/DesktopActivityDashBoard.java
+++ b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/desktop/DesktopActivityDashBoard.java
@@ -152,7 +152,7 @@ public class DesktopActivityDashBoard extends Application
 			long tTimeline0 = System.currentTimeMillis();
 			Tab timelineTabCircle = new Tab("timelineTabCircle Historical Timelines All Users");
 			TimelineChartAppGeneric tcC = new TimelineChartAppGeneric(timelineData, true, "ActivityCircle");
-			timelineTabCircle.setContent(tcC.getVbox());// timelinesVBox2);
+			timelineTabCircle.setContent(tcC.getVBox());// timelinesVBox2);
 			timelineTabCircle.setClosable(true);
 			tabsToAdd.add(timelineTabCircle);
 			long tTimelinen = System.currentTimeMillis();
@@ -173,19 +173,19 @@ public class DesktopActivityDashBoard extends Application
 				Tab timelineTabD = new Tab("timelineTabD Historical Timelines All Users");
 				TimelineChartAppGeneric tcD = new TimelineChartAppGeneric(timelineData, true, "ActivityBox");
 				// TODO: Issue: not scaling correctly with range change.
-				timelineTabD.setContent(tcD.getVbox());// timelinesVBox2);
+				timelineTabD.setContent(tcD.getVBox());// timelinesVBox2);
 				timelineTabD.setClosable(true);
 				tabsToAdd.add(timelineTabD);
 
 				Tab timelineTabE = new Tab("timelineTabE Historical Timelines All Users");
 				TimelineChartAppGeneric tcE = new TimelineChartAppGeneric(timelineData, true, "LineChart");
-				timelineTabE.setContent(tcE.getVbox());// timelinesVBox2);
+				timelineTabE.setContent(tcE.getVBox());// timelinesVBox2);
 				timelineTabE.setClosable(true);
 				tabsToAdd.add(timelineTabE);
 
 				Tab timelineTabScattter = new Tab("timelineTabScattter Historical Timelines All Users");
 				TimelineChartAppGeneric tcScattter = new TimelineChartAppGeneric(timelineData, true, "ScatterChart");
-				timelineTabScattter.setContent(tcScattter.getVbox());// timelinesVBox2);
+				timelineTabScattter.setContent(tcScattter.getVBox());// timelinesVBox2);
 				timelineTabScattter.setClosable(true);
 				tabsToAdd.add(timelineTabScattter);
 			}
diff --git a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/featureExtraction/TimelinesAttributesExtraction.java b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/featureExtraction/TimelinesAttributesExtraction.java
index 0b298f94..a97e1189 100644
--- a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/featureExtraction/TimelinesAttributesExtraction.java
+++ b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/featureExtraction/TimelinesAttributesExtraction.java
@@ -76,7 +76,8 @@ public class TimelinesAttributesExtraction
 
 		// PopUps.showMessage("num of users for feature extraction = " + usersDayTimelines.size());
 		// WritingToFile.writeUsersDayTimelines(usersDayTimelines, "users", true, true, true);// users
-		TimelineStats.writeNumOfActivityObjectsInTimelines(usersDayTimelines, "NumOfActivityObjectsInCleanedTimelines");
+		TimelineStats.writeNumOfAOsInTimelines(usersDayTimelines,
+				pathToWrite + "NumOfActivityObjectsInCleanedTimelines.csv");
 
 		initialiseTimelineAttributeVectors(usersDayTimelines);
 		// $$ addDoubleFeatureToFeatureVectors(getSequenceEntropyAfterExpungingInvalids(usersDayTimelines),
@@ -466,7 +467,7 @@ public class TimelinesAttributesExtraction
 			for (Map.Entry<Integer, LinkedHashMap<String, Double>> entryNGram : entryUser.getValue().entrySet())
 			{
 				WToFile.appendLineToFile("\tfor " + entryNGram.getKey() + "-Gram: " + "\n", fileNamePhrase);// +
-																													// entryUser.getKey());
+																											// entryUser.getKey());
 				WToFile.appendLineToFile("  features are:" + "\n", fileNamePhrase);
 				for (Map.Entry<String, Double> entryF : entryNGram.getValue().entrySet())
 				{
diff --git a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/featureExtraction/TimelinesAttributesExtractionCleaned1.java b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/featureExtraction/TimelinesAttributesExtractionCleaned1.java
index 9385fb55..f27f0b4e 100644
--- a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/featureExtraction/TimelinesAttributesExtractionCleaned1.java
+++ b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/featureExtraction/TimelinesAttributesExtractionCleaned1.java
@@ -69,7 +69,8 @@ public class TimelinesAttributesExtractionCleaned1
 
 		// PopUps.showMessage("num of users for feature extraction = " + usersDayTimelines.size());
 		// WritingToFile.writeUsersDayTimelines(usersDayTimelines, "users", true, true, true);// users
-		TimelineStats.writeNumOfActivityObjectsInTimelines(usersDayTimelines, "NumOfActivityObjectsInCleanedTimelines");
+		TimelineStats.writeNumOfAOsInTimelines(usersDayTimelines,
+				pathToWrite + "NumOfActivityObjectsInCleanedTimelines.csv");
 
 		initialiseTimelineAttributeVectors(usersDayTimelines);
 		// $$ addDoubleFeatureToFeatureVectors(getSequenceEntropyAfterExpungingInvalids(usersDayTimelines),
@@ -461,7 +462,7 @@ public class TimelinesAttributesExtractionCleaned1
 			for (Map.Entry<Integer, LinkedHashMap<String, Double>> entryNGram : entryUser.getValue().entrySet())
 			{
 				WToFile.appendLineToFile("\tfor " + entryNGram.getKey() + "-Gram: " + "\n", fileNamePhrase);// +
-																													// entryUser.getKey());
+																											// entryUser.getKey());
 				WToFile.appendLineToFile("  features are:" + "\n", fileNamePhrase);
 				for (Map.Entry<String, Double> entryF : entryNGram.getValue().entrySet())
 				{
diff --git a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/io/CSVUtils.java b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/io/CSVUtils.java
index 2a66bfe8..567524ae 100644
--- a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/io/CSVUtils.java
+++ b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/io/CSVUtils.java
@@ -900,6 +900,77 @@ public class CSVUtils
 
 	}
 
+	/**
+	 * 
+	 * @param listOfAbsFileNames
+	 * @param hasColumnHeader
+	 *            to make sure columnHeadersAreNotRepeated
+	 * 
+	 * @param absfileToWrite
+	 * @param delimiter
+	 * @param rowHeaderPerFile
+	 * @since May 18 2018
+	 */
+	public static void concatCSVFilesWithRowHeaderPerFile(ArrayList<String> listOfAbsFileNames, boolean hasColumnHeader,
+			String absfileToWrite, char delimiter, ArrayList<String> rowHeaderPerFile)
+	{
+		int countOfFiles = 0, countOfTotalLines = 0;
+		try
+		{
+			for (String fileToRead : listOfAbsFileNames)
+			{
+				countOfFiles += 1;
+				List<CSVRecord> csvRecords = CSVUtils.getCSVRecords(fileToRead, delimiter);
+
+				// System.out.println("read records from " + fileToRead + " are :");
+
+				BufferedWriter bw = WToFile.getBufferedWriterForExistingFile(absfileToWrite);
+				CSVPrinter printer = new CSVPrinter(bw, CSVFormat.DEFAULT.withDelimiter(delimiter).withQuote(null));
+
+				int countOfLines = 0;
+				for (CSVRecord r : csvRecords)
+				{
+					countOfLines += 1;
+
+					// dont write the header for non-first files
+					if (hasColumnHeader && countOfFiles != 1 && countOfLines == 1)
+					{
+						continue;
+					}
+					// System.out.println(r.toString());
+
+					ArrayList<String> valsToWriteForThisRow = new ArrayList<>();
+
+					if (hasColumnHeader && countOfFiles == 1 && countOfLines == 1)
+					{
+						valsToWriteForThisRow.add("rowHeaderPerFile");
+					}
+					else// for non header lines only
+					{
+						valsToWriteForThisRow.add(rowHeaderPerFile.get(countOfFiles - 1));
+					}
+
+					for (int i = 0; i < r.size(); i++)
+					{
+						valsToWriteForThisRow.add(r.get(i));
+					}
+					printer.printRecord(valsToWriteForThisRow);
+
+				}
+				System.out.println(countOfLines + " lines read for this user");
+				countOfTotalLines += countOfLines;
+
+				printer.close();
+			}
+		}
+
+		catch (Exception e)
+		{
+			e.printStackTrace();
+		}
+
+	}
+
 	/**
 	 * 
 	 * @param absFileToSplit
diff --git a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/io/WToFile.java b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/io/WToFile.java
index 81129c81..4cafc276 100644
--- a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/io/WToFile.java
+++ b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/io/WToFile.java
@@ -3441,14 +3441,16 @@ public class WToFile
 	 * @param writeStartEndGeocoordinates
 	 * @param writeDistanceTravelled
 	 * @param writeAvgAltitude
+	 * @param fileName
+	 * @param commonPath
 	 */
 	public static void writeUsersDayTimelinesSameFile(
 			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String timelinesPhrase,
 			boolean writeStartEndGeocoordinates, boolean writeDistanceTravelled, boolean writeAvgAltitude,
-			String fileName)
+			String fileName, String commonPath)
 	{
 		// System.out.println("Common path=" + commonPath);
-		String commonPath = Constant.getCommonPath();//
+		// String commonPath = Constant.getCommonPath();//
 		System.out.println(
 				"Inside writeUsersDayTimelinesSameFile(): num of users received = " + usersDayTimelines.size());
 		System.out.println("Common path=" + commonPath);
@@ -3715,398 +3717,6 @@ public class WToFile
 		}
 	}
 
-	/////
-	/**
-	 * Sums the duration in seconds of activities for each of the days of given day timelines and writes it to a file
-	 * and sums the duration activities over all days of given timelines and return it as a LinkedHashMap
-	 * 
-	 * @param userName
-	 * @param userTimelines
-	 * @param fileNamePhrase
-	 * @return duration of activities over all days of given timelines
-	 */
-	public static LinkedHashMap<String, Long> writeActivityDurationInGivenDayTimelines(String userName,
-			LinkedHashMap<Date, Timeline> userTimelines, String fileNamePhrase)
-	{
-		String commonPath = Constant.getCommonPath();//
-		String[] activityNames = Constant.getActivityNames();// activityNames;
-		LinkedHashMap<String, Long> activityNameDurationPairsOverAllDayTimelines = new LinkedHashMap<String, Long>();
-		// count over all the days
-
-		try
-		{
-			// String userName=entryForUser.getKey();
-			// System.out.println("\nUser ="+entryForUser.getKey());
-			String fileName = commonPath + userName + "ActivityDuration" + fileNamePhrase + ".csv";
-
-			if (VerbosityConstants.verbose)
-			{
-				System.out.println("writing " + userName + "ActivityDuration" + fileNamePhrase + ".csv");
-			}
-
-			StringBuilder toWrite = new StringBuilder();
-
-			toWrite.append(",");
-			// bw.write(",");
-
-			for (String activityName : activityNames)
-			{
-				if (UtilityBelt.isValidActivityName(activityName) == false)
-				// (activityName.equals("Unknown")|| activityName.equals("Others"))
-				{
-					continue;
-				}
-				toWrite.append("," + activityName);
-				// bw.write("," + activityName);
-				activityNameDurationPairsOverAllDayTimelines.put(activityName, new Long(0));
-			}
-			toWrite.append("\n");
-			// bw.newLine();
-
-			for (Map.Entry<Date, Timeline> entry : userTimelines.entrySet())
-			{
-				// System.out.println("Date =" + entry.getKey());
-				// bw.write(entry.getKey().toString());
-				// bw.write("," + (DateTimeUtils.getWeekDayFromWeekDayInt(entry.getKey().getDay())));
-
-				toWrite.append(entry.getKey().toString() + ","
-						+ (DateTimeUtils.getWeekDayFromWeekDayInt(entry.getKey().getDay())));
-
-				ArrayList<ActivityObject> activitiesInDay = entry.getValue().getActivityObjectsInDay();
-				LinkedHashMap<String, Long> activityNameDurationPairs = new LinkedHashMap<String, Long>();
-
-				for (String activityName : activityNames) // written beforehand to maintain the same order of activity
-															// names
-				{
-					if (UtilityBelt.isValidActivityName(activityName))
-					// if((activityName.equalsIgnoreCase("Others")||activityName.equalsIgnoreCase("Unknown"))==false)
-					{
-						activityNameDurationPairs.put(activityName, new Long(0));
-					}
-				}
-
-				for (ActivityObject actEvent : activitiesInDay)
-				{
-					if (UtilityBelt.isValidActivityName(actEvent.getActivityName()))
-					// if((actEvent.getActivityName().equalsIgnoreCase("Unknown") ||
-					// actEvent.getActivityName().equalsIgnoreCase("Others") ) ==false)
-					{
-						Long durationInSecondsForActivity = actEvent.getDurationInSeconds();
-						// summing of duration for current day
-						activityNameDurationPairs.put(actEvent.getActivityName(),
-								activityNameDurationPairs.get(actEvent.getActivityName())
-										+ durationInSecondsForActivity);
-
-						// accumulative duration over all days
-						activityNameDurationPairsOverAllDayTimelines.put(actEvent.getActivityName(),
-								activityNameDurationPairsOverAllDayTimelines.get(actEvent.getActivityName())
-										+ durationInSecondsForActivity);
-					}
-				}
-
-				// write the activityNameDurationPairs to the file
-				for (Map.Entry<String, Long> entryWrite : activityNameDurationPairs.entrySet())
-				{
-					// bw.write("," + entryWrite.getValue());
-					toWrite.append("," + entryWrite.getValue());
-				}
-				toWrite.append("\n");
-				// bw.newLine();
-			}
-			// File file = new File(fileName);
-			// file.delete();
-			// FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
-			// BufferedWriter bw = new BufferedWriter(fw);
-			WToFile.writeToNewFile(toWrite.toString(), fileName);
-			// bw.close();
-		}
-		catch (Exception e)
-		{
-			e.printStackTrace();
-			System.exit(-5);
-		}
-
-		writeSimpleLinkedHashMapToFileAppend(activityNameDurationPairsOverAllDayTimelines,
-				commonPath + "ActivityNameDurationPairsOver" + fileNamePhrase + ".csv", "Activity", "Duration");
-		// TODO check if it indeed should be an append
-
-		return activityNameDurationPairsOverAllDayTimelines;
-
-	}
-
-	// ///////////////////
-	/**
-	 * Counts activities for each of the days of given day timelines and writes it to a file and counts activities over
-	 * all days of given timelines and return it as a LinkedHashMap (fileName = commonPath + userName + "ActivityCounts"
-	 * + fileNamePhrase + ".csv")
-	 * 
-	 * @param userName
-	 * @param userTimelines
-	 * @param fileNamePhrase
-	 * @return count of activities over all days of given timelines
-	 */
-	public static LinkedHashMap<String, Long> writeActivityCountsInGivenDayTimelines(String userName,
-			LinkedHashMap<Date, Timeline> userTimelines, String fileNamePhrase)
-	{
-		String commonPath = Constant.getCommonPath();//
-
-		if (VerbosityConstants.verbose) System.out.println("Inside writeActivityCountsInGivenDayTimelines");
-
-		/* <Activity Name, count over all days> */
-		LinkedHashMap<String, Long> activityNameCountPairsOverAllDayTimelines = new LinkedHashMap<String, Long>();
-		// count over all the days
-		String[] activityNames = Constant.getActivityNames();// .activityNames;
-		try
-		{
-			// String userName=entryForUser.getKey();
-			// System.out.println("\nUser ="+entryForUser.getKey());
-			String fileName = commonPath + userName + "ActivityCounts" + fileNamePhrase + ".csv";
-
-			if (VerbosityConstants.verbose)
-			{
-				System.out.println("writing " + userName + "ActivityCounts" + fileNamePhrase + ".csv");
-			}
-			// BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(fileName);// new BufferedWriter(fw);
-
-			StringBuilder bwString = new StringBuilder();
-			bwString.append(",");
-			// bw.write(",");
-
-			for (String activityName : activityNames)
-			{
-				if (UtilityBelt.isValidActivityName(activityName) == false) // if(activityName.equals("Unknown")||
-																			// activityName.equals("Not Available"))
-				{
-					continue;
-				}
-				// bw.write("," + activityName);
-				bwString.append("," + activityName);
-				// System.out.println("ajooba:activityName = " + activityName + " bwString" + bwString.toString());
-				activityNameCountPairsOverAllDayTimelines.put(activityName, new Long(0));
-			}
-			// bw.newLine();
-			bwString.append("\n");
-
-			for (Map.Entry<Date, Timeline> entry : userTimelines.entrySet())
-			{
-				// System.out.println("Date =" + entry.getKey());
-				// bw.write(entry.getKey().toString());
-				// bw.write("," + (DateTimeUtils.getWeekDayFromWeekDayInt(entry.getKey().getDay())));
-
-				bwString.append(entry.getKey().toString());
-				bwString.append("," + (DateTimeUtils.getWeekDayFromWeekDayInt(entry.getKey().getDay())));
-
-				ArrayList<ActivityObject> activitiesInDay = entry.getValue().getActivityObjectsInDay();
-
-				/* <Activity Name, count for the current day> */
-				LinkedHashMap<String, Integer> activityNameCountPairs = new LinkedHashMap<String, Integer>();
-
-				// written beforehand to maintain the same order of activity names
-				for (String activityName : activityNames)
-				{
-					if (UtilityBelt.isValidActivityName(activityName))
-					// if((activityName.equalsIgnoreCase("Not
-					// Available")||activityName.equalsIgnoreCase("Unknown"))==false)
-					{
-						// System.out.println(" putting down -" + activityName + "- in activityNameCountPairs");
-						activityNameCountPairs.put(activityName, 0);
-					}
-				}
-
-				for (ActivityObject actEvent : activitiesInDay)
-				{
-					if (UtilityBelt.isValidActivityName(actEvent.getActivityName()))
-					// if((actEvent.getActivityName().equalsIgnoreCase("Unknown") ||
-					// actEvent.getActivityName().equalsIgnoreCase("Not Available") ) ==false)
-					{
-						String actName = actEvent.getActivityName();
-						// System.out.println(activityNameCountPairs.size());
-
-						// Integer val;
-						// if (activityNameCountPairs.get(actName) == null)
-						// {
-						// val = 0;
-						// }
-						// else
-						// {
-						// val = activityNameCountPairs.get(actName);
-						// }
-						Integer val = activityNameCountPairs.get(actName);
-						if (val == null)
-						{
-							new Exception(
-									"Exception in org.activity.io.WritingToFile.writeActivityCountsInGivenDayTimelines(String, LinkedHashMap<Date, Timeline>, String) : actName = "
-											+ actName + " has null val");// System.out.println("actName = " + actName);
-						}
-
-						// System.out.println("val:" + val);
-						Integer newVal = new Integer(val.intValue() + 1);
-						// count for current day
-						activityNameCountPairs.put(actName, newVal);
-
-						// accumulative count over all days
-						activityNameCountPairsOverAllDayTimelines.put(actEvent.getActivityName(),
-								activityNameCountPairsOverAllDayTimelines.get(actEvent.getActivityName()) + 1);
-					}
-				}
-
-				// write the activityNameCountPairs to the file
-				for (Map.Entry<String, Integer> entryWrite : activityNameCountPairs.entrySet())
-				{
-					// bw.write("," + entryWrite.getValue());
-					bwString.append("," + entryWrite.getValue());
-				}
-
-				bwString.append("\n");
-				// bw.newLine();
-			}
-			WToFile.writeToNewFile(bwString.toString(), fileName);
-			// bw.write(bwString.toString());
-			// bw.close();
-		}
-		catch (Exception e)
-		{
-			e.printStackTrace();
-			System.exit(-5);
-		}
-
-		writeSimpleLinkedHashMapToFileAppend(activityNameCountPairsOverAllDayTimelines,
-				commonPath + "ActivityNameCountPairsOver" + fileNamePhrase + ".csv", "Activity", "Count");
-		// TODO check if it indeed should be an append
-
-		if (VerbosityConstants.verbose) System.out.println("Exiting writeActivityCountsInGivenDayTimelines");
-
-		return activityNameCountPairsOverAllDayTimelines;
-
-	}
-
-	// ///////////////////
-	/**
-	 * percentage of timelines in which the activity occurrs and counts activities over all days of given timelines and
-	 * return it as a LinkedHashMap
-	 * 
-	 * @param userName
-	 * @param userTimelines
-	 * @param fileNamePhrase
-	 * @return count of activities over all days of given timelines
-	 */
-	public static LinkedHashMap<String, Double> writeActivityOccPercentageOfTimelines(String userName,
-			LinkedHashMap<Date, Timeline> userTimelines, String fileNamePhrase)
-	{
-		String commonPath = Constant.getCommonPath();//
-		LinkedHashMap<String, Double> activityNameCountPairsOverAllDayTimelines = new LinkedHashMap<String, Double>();
-		String[] activityNames = Constant.getActivityNames();// .activityNames;
-		try
-		{
-			// String userName=entryForUser.getKey();
-			// System.out.println("\nUser ="+entryForUser.getKey());
-			String fileName = commonPath + userName + "ActivityOccPerTimelines" + fileNamePhrase + ".csv";
-
-			if (VerbosityConstants.verbose)
-			{
-				System.out.println("writing " + userName + "ActivityOccPerTimelines" + fileNamePhrase + ".csv");
-			}
-
-			StringBuilder toWrite = new StringBuilder();
-			// bw.write(",");
-
-			int actIndex = -1;
-			for (String activityName : activityNames)
-			{
-				actIndex += 1;
-				if (UtilityBelt.isValidActivityName(activityName) == false)
-				// if(activityName.equals("Unknown")|| activityName.equals("Not Available"))
-				{
-					continue;
-				}
-
-				if (Constant.getDatabaseName().equals("gowalla1"))
-				{
-					// bw.write("," + activityName);
-					toWrite.append("," + Constant.activityNamesGowallaLabels.get(actIndex));
-				}
-				else
-				{
-					// bw.write("," + activityName);
-					toWrite.append("," + activityName);
-
-				}
-
-				activityNameCountPairsOverAllDayTimelines.put(activityName, new Double(0));
-			}
-			// bw.newLine();
-			toWrite.append("\n");
-
-			double numOfTimelines = userTimelines.size();
-
-			for (String activityName : activityNames) // written beforehand to maintain the same order of activity names
-			{
-				if (UtilityBelt.isValidActivityName(activityName))
-				// if((activityName.equalsIgnoreCase("Not Available")||activityName.equalsIgnoreCase("Unknown"))==false)
-				{
-					activityNameCountPairsOverAllDayTimelines.put(activityName, new Double(0));
-				}
-			}
-
-			for (Map.Entry<Date, Timeline> entry : userTimelines.entrySet())
-			{
-				// System.out.println("Date =" + entry.getKey());
-				// bw.write(entry.getKey().toString());
-				// bw.write("," + (UtilityBelt.getWeekDayFromWeekDayInt(entry.getKey().getDay())));
-
-				ArrayList<ActivityObject> activitiesInDay = entry.getValue().getActivityObjectsInDay();
-
-				// written beforehand to maintain the same order of activity names
-				for (String activityName : activityNames)
-				{
-					if (UtilityBelt.isValidActivityName(activityName))
-					{
-						if (entry.getValue().hasActivityName(activityName) == true)
-						{
-							activityNameCountPairsOverAllDayTimelines.put(activityName,
-									activityNameCountPairsOverAllDayTimelines.get(activityName) + 1);
-						}
-					}
-				}
-			}
-
-			// write the activityNameCountPairs to the file
-			for (Map.Entry<String, Double> entryWrite : activityNameCountPairsOverAllDayTimelines.entrySet())
-			{
-				String actName = entryWrite.getKey();
-				Double val = entryWrite.getValue();
-				double percentageOccurrenceOverTimeline = ((double) activityNameCountPairsOverAllDayTimelines
-						.get(actName) / (double) numOfTimelines) * 100;
-				activityNameCountPairsOverAllDayTimelines.put(actName, percentageOccurrenceOverTimeline);
-				// bw.write("," + percentageOccurrenceOverTimeline);
-				toWrite.append("," + percentageOccurrenceOverTimeline);
-			}
-			// bw.newLine();
-			toWrite.append("\n");
-
-			// File file = new File(fileName);
-			// file.delete();
-			// FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
-			// BufferedWriter bw = new BufferedWriter(fw);
-			WToFile.writeToNewFile(toWrite.toString(), fileName);
-			// bw.close();
-		}
-
-		catch (Exception e)
-		{
-			e.printStackTrace();
-			System.exit(-5);
-		}
-
-		writeSimpleLinkedHashMapToFileAppend(activityNameCountPairsOverAllDayTimelines,
-				commonPath + "ActivityOccPerTimelines" + fileNamePhrase + ".csv", "Activity", "Count");// TODO check if
-																										// it indeed
-		// should be an append
-
-		return activityNameCountPairsOverAllDayTimelines;
-
-	}
-
 	/**
 	 * 
 	 * This method is called from DCU_DataLoader
@@ -4595,32 +4205,32 @@ public class WToFile
 
 			if (timelinesSet.equals("TrainingTimelines"))
 			{
-				activityNameCountPairsOverAllTrainingDays = WToFile.writeActivityCountsInGivenDayTimelines(userName,
-						timelinesCursor, timelinesSet);
+				activityNameCountPairsOverAllTrainingDays = TimelineStats
+						.writeActivityCountsInGivenDayTimelines(userName, timelinesCursor, timelinesSet, commonPath);
 				activityNameCountPairsOverAllTrainingDays = (LinkedHashMap<String, Long>) ComparatorUtils
 						.sortByValueDesc(activityNameCountPairsOverAllTrainingDays);
 				resultsToReturn.put("activityNameCountPairsOverAllTrainingDays",
 						activityNameCountPairsOverAllTrainingDays);
 
-				activityNameDurationPairsOverAllTrainingDays = WToFile
-						.writeActivityDurationInGivenDayTimelines(userName, timelinesCursor, timelinesSet);
+				activityNameDurationPairsOverAllTrainingDays = TimelineStats
+						.writeActivityDurationInGivenDayTimelines(userName, timelinesCursor, timelinesSet, commonPath);
 				activityNameDurationPairsOverAllTrainingDays = (LinkedHashMap<String, Long>) ComparatorUtils
 						.sortByValueDesc(activityNameDurationPairsOverAllTrainingDays);
 				resultsToReturn.put("activityNameDurationPairsOverAllTrainingDays",
 						activityNameDurationPairsOverAllTrainingDays);
 
-				activityNameOccPercentageOverAllTrainingDays = WToFile.writeActivityOccPercentageOfTimelines(userName,
-						timelinesCursor, timelinesSet);
+				activityNameOccPercentageOverAllTrainingDays = TimelineStats
+						.writeActivityOccPercentageOfTimelines(userName, timelinesCursor, timelinesSet, commonPath);
 			}
 
 			else
 			{
-				LinkedHashMap<String, Long> actCountRes1 = WToFile.writeActivityCountsInGivenDayTimelines(userName,
-						timelinesCursor, timelinesSet);
-				LinkedHashMap<String, Long> actDurationRes1 = WToFile.writeActivityDurationInGivenDayTimelines(userName,
-						timelinesCursor, timelinesSet);
-				LinkedHashMap<String, Double> actOccPercentageRes1 = WToFile
-						.writeActivityOccPercentageOfTimelines(userName, timelinesCursor, timelinesSet);
+				LinkedHashMap<String, Long> actCountRes1 = TimelineStats
+						.writeActivityCountsInGivenDayTimelines(userName, timelinesCursor, timelinesSet, commonPath);
+				LinkedHashMap<String, Long> actDurationRes1 = TimelineStats
+						.writeActivityDurationInGivenDayTimelines(userName, timelinesCursor, timelinesSet, commonPath);
+				LinkedHashMap<String, Double> actOccPercentageRes1 = TimelineStats
+						.writeActivityOccPercentageOfTimelines(userName, timelinesCursor, timelinesSet, commonPath);
 
 				writeSimpleLinkedHashMapToFileAppend(actCountRes1,
 						commonPath + "ActivityCounts" + timelinesSet + ".csv", "dummy", "dummy");
diff --git a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/objects/ActivityObject.java b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/objects/ActivityObject.java
index 0e796156..19fb07cb 100644
--- a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/objects/ActivityObject.java
+++ b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/objects/ActivityObject.java
@@ -99,6 +99,16 @@ public class ActivityObject implements Serializable
 	long durInSecFromPrev, durInSecFromNext;
 	ZoneId timeZoneId;
 
+	/**
+	 * For Serialisation purposes.
+	 * 
+	 * @since 21 May 2018
+	 */
+	public ActivityObject()
+	{
+
+	}
+
 	/**
 	 * @since April 9 2018
 	 * @return
diff --git a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/objects/Timeline.java b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/objects/Timeline.java
index 4ea7c9b0..4fff1446 100644
--- a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/objects/Timeline.java
+++ b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/objects/Timeline.java
@@ -47,6 +47,16 @@ public class Timeline implements Serializable
 		this.activityObjectsInTimeline.add(aoToAppend);
 	}
 
+	/**
+	 * For serialisation purposes
+	 * 
+	 * @since May 21 2018
+	 */
+	public Timeline()
+	{
+
+	}
+
 	/**
 	 * Create Timeline from given Activity Objects
 	 * 
diff --git a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/ActivityBoxExtraValues.java b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/ActivityBoxExtraValues.java
index 07ffe292..0050ca5f 100644
--- a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/ActivityBoxExtraValues.java
+++ b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/ActivityBoxExtraValues.java
@@ -62,11 +62,21 @@ public class ActivityBoxExtraValues
 		return startLatitude;
 	}
 
-	private static final String FORMAT = "ActivityBoxExtraValues{end=%f, ActName=%s, ActID=%f, locationName=%s}";
+	public double getEndLatitude()
+	{
+		return endLatitude;
+	}
+
+	private static final String FORMAT = "ActivityBoxExtraValues{end=%f, ActName=%s, ActID=%d, LocName=%s}";
 
 	@Override
 	public String toString()
 	{
-		return String.format(FORMAT, endTimestamp, activityName, activityID, locationName);
+		// String endTSString = new SimpleDateFormat("MM/dd/yy HH:mm:ss").format(endTimestamp);
+		//
+		// return "endTS="+endTimestamp+","
+		return "ajooba";
+		// return String.format(FORMAT, endTimestamp, activityName, activityID, locationName);
+
 	}
 }
diff --git a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/ActivityCircle.java b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/ActivityCircle.java
index 040710ff..ce527cf5 100644
--- a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/ActivityCircle.java
+++ b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/ActivityCircle.java
@@ -2,6 +2,7 @@ package org.activity.plotting;
 
 import java.util.Arrays;
 
+import org.activity.constants.Constant;
 import org.activity.ui.UIUtilityBox;
 
 import javafx.scene.CacheHint;
@@ -80,12 +81,16 @@ public class ActivityCircle extends Group
 		/// Start of moved from updateStyleClasses() to avoid repeated calls
 		getStyleClass().setAll("activitybox-box", seriesStyleClass, dataStyleClass);
 		actID = actExtraVals.getActivityID();
+		// System.out.println("actExtraVals= " + actExtraVals.toString());
 		setBackGround();
 	}
 
 	private void setBackGround()
 	{
-		circle.setFill(ColorPalette.getInsightSecondaryColor(actID % 11));
+		// circle.setFill(ColorPalette.getInsightSecondaryColor(actID % 11));
+		// circle.setFill(ColorPalette.getColors269Color(actID));
+		circle.setFill(ColorPalette.getColors269Color(Constant.getIndexOfActIDInActNames(actID)));// only works for real
+																									// data
 	}
 
 	/**
diff --git a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/ColorPalette.java b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/ColorPalette.java
index d1119383..534a9c8d 100644
--- a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/ColorPalette.java
+++ b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/ColorPalette.java
@@ -78,7 +78,7 @@ public class ColorPalette
 	// #ref :
 	// https://graphicdesign.stackexchange.com/questions/3682/where-can-i-find-a-large-palette-set-of-contrasting-colors-for-coloring-many-d
 
-	static String[] colors269 = new String[] { "#000000", "#FFFF00", "#1CE6FF", "#FF34FF", "#FF4A46", "#008941",
+	static final String[] colors269 = new String[] { "#000000", "#FFFF00", "#1CE6FF", "#FF34FF", "#FF4A46", "#008941",
 			"#006FA6", "#A30059", "#FFDBE5", "#7A4900", "#0000A6", "#63FFAC", "#B79762", "#004D43", "#8FB0FF",
 			"#997D87", "#5A0007", "#809693", "#FEFFE6", "#1B4400", "#4FC601", "#3B5DFF", "#4A3B53", "#FF2F80",
 			"#61615A", "#BA0900", "#6B7900", "#00C2A0", "#FFAA92", "#FF90C9", "#B903AA", "#D16100", "#DDEFFF",
diff --git a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/TimelineChart2.java b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/TimelineChart2.java
index b6c8511d..3c55700f 100644
--- a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/TimelineChart2.java
+++ b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/TimelineChart2.java
@@ -22,6 +22,7 @@ public class TimelineChart2 extends XYChart<Number, String>
 {
 
 	// double heightOfActivityBox;
+	boolean verbose = false;
 
 	/**
 	 * Construct a new TimelineChart with the given axis.
@@ -105,17 +106,21 @@ public class TimelineChart2 extends XYChart<Number, String>
 
 				// to find best height start
 				// System.out.println("item= " + item);
-				System.out.println("displayedXVal= " + displayedXVal);
-				System.out.println("displayedYVal= " + displayedYVal);
-
-				System.out.println("xDispPosition= " + xDispPosition);
-				System.out.println("yDispPosition= " + yDispPosition);
-				// System.out.println("--> yAxis.getMaxHeight() =" + yAxis.getMaxHeight());
-				// System.out.println("--> yAxis.getMinHeight() =" + yAxis.getMinHeight());
-				// System.out.println("--> yAxis.getPrefHeight() =" + yAxis.getPrefHeight());
-				// System.out.println("--> yAxis.autoRangingProperty() =" + yAxis.autoRangingProperty());//true
-				// $$ System.out.println("--> this.height() =" + this.getHeight());
-				// $$ System.out.println("--> heightOfActBox = " + heightOfActBox);
+				if (verbose)
+				{
+					System.out.println("displayedXVal= " + displayedXVal);
+					System.out.println("displayedYVal= " + displayedYVal);
+
+					System.out.println("xDispPosition= " + xDispPosition);
+					System.out.println("yDispPosition= " + yDispPosition);
+					// System.out.println("--> yAxis.getMaxHeight() =" + yAxis.getMaxHeight());
+					// System.out.println("--> yAxis.getMinHeight() =" + yAxis.getMinHeight());
+					// System.out.println("--> yAxis.getPrefHeight() =" + yAxis.getPrefHeight());
+					// System.out.println("--> yAxis.autoRangingProperty() =" + yAxis.autoRangingProperty());//true
+					// $$ System.out.println("--> this.height() =" + this.getHeight());
+					// $$ System.out.println("--> heightOfActBox = " + heightOfActBox);
+				}
+
 				// heightOfActBox+emptySpaceAboveActBox+emptySpaceBelowActBox = (heightOfChart/numOfUsers)
 				// heightOfActBox+0.25*heightOfActBox+0.25*heightOfActBox = (heightOfChart/numOfUsers)
 				// to find best height end
@@ -129,6 +134,9 @@ public class TimelineChart2 extends XYChart<Number, String>
 				if (itemNode instanceof ActivityBox2 && extra != null)
 				{
 					double endTS = xAxis.getDisplayPosition(extra.getEndTimestamp());
+					
+					if (verbose)
+					{
 					System.out.println("extra.getEndTimestamp()= " + extra.getEndTimestamp());
 					System.out.println("endTS= " + endTS);
 
@@ -137,6 +145,7 @@ public class TimelineChart2 extends XYChart<Number, String>
 					IntStream.rangeClosed(1, 10).map(i -> i * 5)
 							.forEachOrdered(i -> sb.append("\nxval= " + i + " pos=" + xAxis.getDisplayPosition(i)));
 					System.out.println(sb.toString());
+					}
 					// double high = xAxis.getDisplayPosition(extra.getHigh());
 					// double low = xAxis.getDisplayPosition(extra.getLow());
 					// calculate activity box width
diff --git a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/TimelineChartAppGeneric.java b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/TimelineChartAppGeneric.java
index 3f2ffee6..f85173f9 100644
--- a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/TimelineChartAppGeneric.java
+++ b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/TimelineChartAppGeneric.java
@@ -1,8 +1,15 @@
 package org.activity.plotting;
 
+import java.sql.Date;
 import java.util.Collections;
+import java.util.LinkedHashMap;
 import java.util.List;
+import java.util.Map.Entry;
+import java.util.TreeMap;
 
+import org.activity.constants.DomainConstants;
+import org.activity.objects.ActivityObject;
+import org.activity.objects.Timeline;
 import org.activity.objects.Triple;
 import org.activity.ui.EpochStringConverter;
 import org.activity.ui.PopUps;
@@ -50,7 +57,7 @@ public class TimelineChartAppGeneric extends Pane
 	// static List<Pair<String, List<Double>>> allData;
 	private VBox vbox;
 
-	public VBox getVbox()
+	public VBox getVBox()
 	{
 		return vbox;
 	}
@@ -88,7 +95,60 @@ public class TimelineChartAppGeneric extends Pane
 				PopUps.showError("Unrecognised typeOfTimelineChart = " + typeOfTimelineChart);
 		}
 
-		Triple<XYChart<Number, String>, Long, Long> res = createTimelineContent(dataReceived, timelineChart);
+		Triple<ObservableList<Series<Number, String>>, Long, Long> seriesForAllUsersRes = actDataToSeries(dataReceived);
+		Triple<XYChart<Number, String>, Long, Long> res = createTimelineContent(seriesForAllUsersRes, timelineChart);
+		XYChart<Number, String> timelineChart = res.getFirst();
+		this.maxXAxis = res.getSecond();
+		this.minXAxis = res.getThird();
+
+		// formatXAxis(maxXAxis, minXAxis);
+
+		vbox.getChildren().add(timelineChart);
+		vbox.getChildren().add(createXAxisRangeSlider((NumberAxis) timelineChart.getXAxis(), maxXAxis, minXAxis));
+		VBox.setVgrow(timelineChart, Priority.ALWAYS);
+		// return vbox;
+		this.getChildren().add(vbox);
+	}
+
+	/**
+	 * 
+	 * @param userDayTimelines
+	 * @param hasXAxisRangeSlider
+	 * @param typeOfTimelineChart
+	 * @since May 20 2018
+	 */
+	public TimelineChartAppGeneric(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> userDayTimelines,
+			boolean hasXAxisRangeSlider, String typeOfTimelineChart)
+	{
+		vbox = new VBox();
+		xAxis = new NumberAxis();
+		yAxis = new CategoryAxis();
+
+		xAxis.setLabel("Timestamp");
+		yAxis.setLabel("Users' Timelines");
+
+		switch (typeOfTimelineChart)
+		{
+			case "ActivityBox":
+				timelineChart = new TimelineChart2(xAxis, yAxis);
+				break;
+			case "ActivityCircle":
+				timelineChart = new TimelineChartCircle(xAxis, yAxis);
+				break;
+			case "LineChart":
+				timelineChart = new LineChart<Number, String>(xAxis, yAxis);
+				break;
+			case "ScatterChart":
+				timelineChart = new ScatterChart<Number, String>(xAxis, yAxis);
+				break;
+			default:
+				PopUps.showError("Unrecognised typeOfTimelineChart = " + typeOfTimelineChart);
+		}
+		// Triple{seriesForAllUsers, maxXAxis, minXAxis}//dataToSeries(dataReceived);
+		Triple<ObservableList<Series<Number, String>>, Long, Long> seriesForAllUsers = userDayTimelinesToSeries20May(
+				userDayTimelines);
+		Triple<XYChart<Number, String>, Long, Long> res = createTimelineContent(seriesForAllUsers/* dataReceived */,
+				timelineChart);
 		XYChart<Number, String> timelineChart = res.getFirst();
 		this.maxXAxis = res.getSecond();
 		this.minXAxis = res.getThird();
@@ -148,8 +208,8 @@ public class TimelineChartAppGeneric extends Pane
 
 		// Works but slow response.
 		// // Curtain A1 start
-		// Bindings.bindBidirectional(hSlider.lowValueProperty(), axis.lowerBoundProperty());
-		// Bindings.bindBidirectional(hSlider.highValueProperty(), axis.upperBoundProperty());
+		Bindings.bindBidirectional(hSlider.lowValueProperty(), axis.lowerBoundProperty());
+		Bindings.bindBidirectional(hSlider.highValueProperty(), axis.upperBoundProperty());
 		// // Curtain A1 end
 
 		Button updateButton = new Button("Apply");
@@ -217,17 +277,19 @@ public class TimelineChartAppGeneric extends Pane
 	/**
 	 * One series for each users
 	 * 
-	 * @param dataReceived
+	 * 
+	 * @param seriesForAllUsersRes
 	 * @param timelineChart
 	 * @return
 	 */
-	private Triple<XYChart<Number, String>, Long, Long> createTimelineContent(List<List<List<String>>> dataReceived,
-			XYChart<Number, String> timelineChart)
+	private Triple<XYChart<Number, String>, Long, Long> createTimelineContent(
+			Triple<ObservableList<Series<Number, String>>, Long, Long> seriesForAllUsersRes,
+			XYChart<Number, String> timelineChart)// List<List<List<String>>> dataReceived
 	{
 		System.out.println("createTimelineContent() called");
-		Triple<ObservableList<Series<Number, String>>, Long, Long> res = dataToSeries(dataReceived);
+		// Triple<ObservableList<Series<Number, String>>, Long, Long> dataReceived;// = dataToSeries(dataReceived);
 
-		ObservableList<XYChart.Series<Number, String>> seriesForAllUsers = res.getFirst();
+		ObservableList<XYChart.Series<Number, String>> seriesForAllUsers = seriesForAllUsersRes.getFirst();
 		// this.maxXAxis = res.getSecond();
 		// this.minXAxis = res.getThird();
 		ObservableList<XYChart.Series<Number, String>> data = timelineChart.getData();
@@ -245,7 +307,7 @@ public class TimelineChartAppGeneric extends Pane
 		}
 
 		timelineChart.setLegendVisible(false);
-		System.out.println("dataReceived.size()=" + dataReceived.size());
+		System.out.println("seriesForAllUsers.size()=" + seriesForAllUsers.size());
 		System.out.println("seriesForAllUsers.getData().size()=" + seriesForAllUsers.size());
 		System.out.println("chart.getData().size()=" + timelineChart.getData().size());
 		System.out.println("Inside chart: xAxis.getLowerBound()=" + xAxis.getLowerBound());
@@ -256,7 +318,8 @@ public class TimelineChartAppGeneric extends Pane
 		// dataReceived.clear();
 		// dataReceived = null;
 
-		return new Triple<XYChart<Number, String>, Long, Long>(timelineChart, res.getSecond(), res.getThird());
+		return new Triple<XYChart<Number, String>, Long, Long>(timelineChart, seriesForAllUsersRes.getSecond(),
+				seriesForAllUsersRes.getThird());
 	}
 
 	/**
@@ -294,7 +357,7 @@ public class TimelineChartAppGeneric extends Pane
 	 * @param dataReceived
 	 * @return Triple{seriesForAllUsers, maxXAxis, minXAxis}
 	 */
-	private static final Triple<ObservableList<XYChart.Series<Number, String>>, Long, Long> dataToSeries(
+	private static final Triple<ObservableList<XYChart.Series<Number, String>>, Long, Long> actDataToSeries(
 			List<List<List<String>>> dataReceived)
 	{
 		long maxXAxis = 0, minXAxis = Long.MAX_VALUE;
@@ -345,4 +408,71 @@ public class TimelineChartAppGeneric extends Pane
 				minXAxis);
 	}
 
+	/**
+	 * 
+	 * @param dataReceived
+	 * @return Triple{seriesForAllUsers, maxXAxis, minXAxis}
+	 * @since 20 May 2018
+	 */
+	private static final Triple<ObservableList<XYChart.Series<Number, String>>, Long, Long> userDayTimelinesToSeries20May(
+			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> userDayTimelines)
+	{
+		long maxXAxis = 0, minXAxis = Long.MAX_VALUE;
+
+		ObservableList<XYChart.Series<Number, String>> seriesForAllUsers = FXCollections.observableArrayList();
+		TreeMap<Integer, String> catIdNameDictioary = DomainConstants.catIDNameDictionary;
+
+		// (List<List<String>>eachUserData: dataReceived)
+		for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : userDayTimelines.entrySet())
+		{
+			XYChart.Series<Number, String> seriesForAUser = new XYChart.Series<Number, String>();
+			String userID = userEntry.getKey();
+
+			// for (List<String> d : eachUserEntry)
+			for (Entry<Date, Timeline> timelineEntry : userEntry.getValue().entrySet())
+			{
+				String date = timelineEntry.getKey().toString();
+				Timeline t = timelineEntry.getValue();
+				for (ActivityObject ao : t.getActivityObjectsInTimeline())
+				{
+					// String userID = d.get(0);
+					long startTS = ao.getStartTimestampInms();// Double.valueOf(d.get(1));
+					long endTS = ao.getEndTimestampInms();// Double.valueOf(d.get(2));
+					String locName = ao.getLocationIDs(',');// d.get(3);
+					Integer actID = ao.getActivityID();// Integer.valueOf(d.get(5));
+					String actName = catIdNameDictioary.getOrDefault(actID, "ID:" + actID);
+					String startLat = ao.getStartLatitude();
+					// String endLat = ao.getEndLatitude();
+
+					// end timestamp
+					final ActivityBoxExtraValues extras = new ActivityBoxExtraValues(endTS, actName, actID, locName);
+
+					// start timeestamp, username, {end timestamp, actname, }
+					seriesForAUser.getData().add(new XYChart.Data<Number, String>(startTS, userID, extras));
+
+					long xValST = (long) (startTS);
+					long xValET = (long) (endTS);
+
+					if (maxXAxis < xValET)
+					{
+						maxXAxis = xValET;
+					}
+
+					if (minXAxis > xValST)
+					{
+						minXAxis = xValST;
+					}
+				}
+			} // end of loop over day timelines for this user
+			seriesForAllUsers.add(seriesForAUser);
+		}
+
+		// upper bound is maxXAxis rounded to ceiling multiple of 10
+		maxXAxis = (long) (Math.ceil(maxXAxis / 100d) * 100);
+		// upper bound is maxXAxis rounded to ceiling multiple of 10
+		minXAxis = (long) (Math.floor(minXAxis / 100d) * 100);
+		return new Triple<ObservableList<XYChart.Series<Number, String>>, Long, Long>(seriesForAllUsers, maxXAxis,
+				minXAxis);
+	}
+
 }
diff --git a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/TimelineChartCircle.java b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/TimelineChartCircle.java
index bc95a91a..90aae8f6 100644
--- a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/TimelineChartCircle.java
+++ b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/plotting/TimelineChartCircle.java
@@ -21,7 +21,7 @@ public class TimelineChartCircle extends XYChart<Number, String>
 {
 
 	// double heightOfActivityBox;
-
+boolean verbose=false;
 	/**
 	 * Construct a new TimelineChart with the given axis.
 	 *
@@ -66,7 +66,7 @@ public class TimelineChartCircle extends XYChart<Number, String>
 	@Override
 	protected void layoutPlotChildren()
 	{
-		System.out.println("\nTimelineChart2.layoutPlotChildren() called");
+		System.out.println("\nTimelineChartCircle.layoutPlotChildren() called");
 
 		// we have nothing to layout if no data is present
 		if (getData() == null)
diff --git a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/stats/TimelineStats.java b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/stats/TimelineStats.java
index 4e42228b..82388c29 100644
--- a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/stats/TimelineStats.java
+++ b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/stats/TimelineStats.java
@@ -27,8 +27,10 @@ import org.activity.clustering.KCentroids;
 import org.activity.clustering.KCentroidsTimelines;
 import org.activity.constants.Constant;
 import org.activity.constants.DomainConstants;
+import org.activity.constants.VerbosityConstants;
 import org.activity.distances.AlignmentBasedDistance;
 import org.activity.distances.HJEditDistance;
+import org.activity.io.CSVUtils;
 import org.activity.io.WToFile;
 import org.activity.objects.ActivityObject;
 import org.activity.objects.Pair;
@@ -38,6 +40,7 @@ import org.activity.stats.entropy.SampleEntropyG;
 import org.activity.ui.PopUps;
 import org.activity.util.ComparatorUtils;
 import org.activity.util.ConnectDatabase;
+import org.activity.util.DateTimeUtils;
 import org.activity.util.RegexUtils;
 import org.activity.util.StringCode;
 import org.activity.util.TimelineTransformers;
@@ -62,13 +65,13 @@ public class TimelineStats
 {
 	// public static void main(String args[]) {
 	// traverseHashMap(frequencyDistributionOfNGramSubsequences("ABACDAACCCDCDABDAB", 1));}
-	public static final int intervalInSecs = 1, numOfClusters = 2, numOfKCentroidsExperiments = 25;
+	static final int intervalInSecs = 1, numOfClusters = 2, numOfKCentroidsExperiments = 25;
 
 	/**
 	 * Show only the top 5 clusterings based on intra-cluster variance. The tighter the clusters, the better the
 	 * clustering.
 	 */
-	public static final boolean clusteringOnlyTop5 = false;
+	static final boolean clusteringOnlyTop5 = false;
 
 	static String pathToWrite;// , directoryToWrite;// =
 								// "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/GeoLife/link to Geolife
@@ -80,7 +83,7 @@ public class TimelineStats
 	 * ActivityRegularityAnalysisTwoLevel";// "ActivityRegularityAnalysisOneLevel";// "Clustering";// "Clustering";// //
 	 * NGramAnalysis"; // "TimeSeriesAnalysis", "FeatureAnalysis"
 	 */
-	public static final String typeOfAnalysis = "TimelineStats";// "NGramAnalysis";
+	static final String typeOfAnalysis = "TimelineStats";// "NGramAnalysis";
 	// "TimelineStats";// "NGramAnalysis";// "TimeSeriesCorrelationAnalysis";// "SampleEntropyPerMAnalysis";//
 	// "SampleEntropyPerMAnalysis";//
 	// "TimeSeriesAnalysis";// "AlgorithmicAnalysis2"; "Clustering";// "ClusteringTimelineHolistic";// "
@@ -95,7 +98,7 @@ public class TimelineStats
 	 * @param usersTimelines
 	 */
 	public static void timelineStatsController(
-			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesAll)
+			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesGiven)
 	{
 		Constant.setCommonPath(Constant.getOutputCoreResultsPath());
 		// PopUps.showMessage("Inside timelineStats controller");
@@ -109,11 +112,11 @@ public class TimelineStats
 		// if userid is not set in constant class, in case of gowalla
 		if (userIDs == null || userIDs.length == 0)
 		{
-			userIDs = new int[usersDayTimelinesAll.size()];// System.out.println("usersTimelines.size() = " +
-															// usersTimelines.size());
+			userIDs = new int[usersDayTimelinesGiven.size()];// System.out.println("usersTimelines.size() = " +
+																// usersTimelines.size());
 			System.out.println("UserIDs not set, hence extracting user ids from usersTimelines keyset");
 			int count = 0;
-			for (String userS : usersDayTimelinesAll.keySet())
+			for (String userS : usersDayTimelinesGiven.keySet())
 			{
 				userIDs[count++] = Integer.valueOf(userS);
 			}
@@ -132,46 +135,33 @@ public class TimelineStats
 		new File(directoryToWrite).mkdir();
 		pathToWrite = directoryToWrite + "/";
 		Constant.setCommonPath(pathToWrite);
+		String databaseName = Constant.getDatabaseName();
 		PopUps.showMessage("path to write: " + pathToWrite);
 		PrintStream consoleLogStream = WToFile.redirectConsoleOutput(pathToWrite + "ConsoleLog.txt");
 		// /////////////////////
-		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines = new LinkedHashMap<String, LinkedHashMap<Date, Timeline>>();
-		if (!Constant.getDatabaseName().equals("gowalla1"))
+		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelinesCleaned = new LinkedHashMap<String, LinkedHashMap<Date, Timeline>>();
+		if (!databaseName.equals("gowalla1"))
 		{
-
-			writeNumOfActivityObjectsInTimelines(usersDayTimelinesAll, "NumOfActivityObjectsInUncleanedTimelines");
-			writeNumOfValidActivityObjectsInTimelines(usersDayTimelinesAll,
-					"NumOfValidActivityObjectsInUncleanedTimelines");
-			writeNumOfDaysInTimelines(usersDayTimelinesAll, "NumOfDaysInUncleanedTimelines");
-			writeAvgNumOfDistinctActsPerDayInTimelines(usersDayTimelinesAll,
-					"AvgNumOfDistinctActsInUncleanedTimelines");
-			writeAvgNumOfTotalActsPerDayInTimelines(usersDayTimelinesAll, "AvgNumOfTotalActsInUncleanedTimelines");
+			writeTimelineStats(usersDayTimelinesGiven, databaseName, pathToWrite, "Uncleaned");
 			// //////////////////
 
-			usersDayTimelines = TimelineUtils.cleanUsersDayTimelines(usersDayTimelinesAll);
-			usersDayTimelines = TimelineUtils.rearrangeDayTimelinesOrderForDataset(usersDayTimelines);// UtilityBelt.dayTimelinesToCleanedExpungedRearrangedTimelines(usersDayTimelines);
+			usersDayTimelinesCleaned = TimelineUtils.cleanUsersDayTimelines(usersDayTimelinesGiven);
+			usersDayTimelinesCleaned = TimelineUtils.rearrangeDayTimelinesOrderForDataset(usersDayTimelinesCleaned);// UtilityBelt.dayTimelinesToCleanedExpungedRearrangedTimelines(usersDayTimelines);
 			System.out.println("ALERT: CLEANING AND REARRANGING USERS DAY TIMELINES !!");
 
-			WToFile.writeUsersDayTimelines(usersDayTimelines, "users", true, true, true);// users
+			WToFile.writeUsersDayTimelines(usersDayTimelinesCleaned, "users", true, true, true);// users
 		}
 		else
 		{
 			System.out.println("NOTE: NOT cleaning and rearranging users day timelines !!");
-			usersDayTimelines = usersDayTimelinesAll;
+			usersDayTimelinesCleaned = usersDayTimelinesGiven;
 		}
 		// usersDayTimelines = UtilityBelt.reformatUserIDs(usersDayTimelines);
 
-		writeNumOfActivityObjectsInTimelines(usersDayTimelines, "NumOfActivityObjectsInCleanedTimelines");
-		writeNumOfValidActivityObjectsInTimelines(usersDayTimelines, "NumOfValidActivityObjectsInCleanedTimelines");
-		writeNumOfDaysInTimelines(usersDayTimelines, "NumOfDaysInCleanedTimelines");
-		writeAvgNumOfDistinctActsPerDayInTimelines(usersDayTimelines, "AvgNumOfDistinctActsInCleanedTimelines");
-		writeAvgNumOfTotalActsPerDayInTimelines(usersDayTimelines, "AvgNumOfTotalActsInCleanedTimelines");
-		writeAllNumOfDistinctActsPerDayInTimelines(usersDayTimelines, "AllNumOfDistinctActsInCleanedTimelines");
-		writeAllNumOfTotalActsPerDayInTimelines(usersDayTimelines, "AllNumOfTotalActsInCleanedTimelines");
-		writeStatsNumOfDistinctActsPerDayInTimelines(usersDayTimelines, "StatsNumOfDistinctActsInCleanedTimelines");
-		writeStatsNumOfTotalActsPerDayInTimelines(usersDayTimelines, "StatsNumOfTotalActsInCleanedTimelines");
-
-		writeActivityStats(usersDayTimelines, "ActivityStats");
+		writeTimelineStats(usersDayTimelinesCleaned, databaseName, pathToWrite, "Cleaned");
+
+		writeActivityStats(usersDayTimelinesCleaned, "ActivityStats", pathToWrite);
+
 		switch (typeOfAnalysis)
 		{
 			/**
@@ -179,22 +169,22 @@ public class TimelineStats
 			 */
 			case "TimeSeriesCorrelationAnalysis":
 			{
-				transformAndWriteAsTimeseries(UtilityBelt.reformatUserIDs(usersDayTimelines));
-				performTimeSeriesCorrelationAnalysis(UtilityBelt.reformatUserIDs(usersDayTimelines));
+				transformAndWriteAsTimeseries(UtilityBelt.reformatUserIDs(usersDayTimelinesCleaned));
+				performTimeSeriesCorrelationAnalysis(UtilityBelt.reformatUserIDs(usersDayTimelinesCleaned));
 				break;
 			}
 
 			case "SampleEntropyPerMAnalysis":
 			{
-				transformAndWriteAsTimeseries(UtilityBelt.reformatUserIDs(usersDayTimelines));
+				transformAndWriteAsTimeseries(UtilityBelt.reformatUserIDs(usersDayTimelinesCleaned));
 				// String pathForStoredTimelines
-				performSampleEntropyVsMAnalysis2(UtilityBelt.reformatUserIDs(usersDayTimelines), 2, 3);
+				performSampleEntropyVsMAnalysis2(UtilityBelt.reformatUserIDs(usersDayTimelinesCleaned), 2, 3);
 				break;
 			}
 			case "ClusteringTimelineHolistic": // applying Kcentroids with two-level edit distance
 			{
 				LinkedHashMap<String, Timeline> usersTimelines = TimelineUtils
-						.dayTimelinesToTimelines(usersDayTimelines);
+						.dayTimelinesToTimelines(usersDayTimelinesCleaned);
 				LinkedHashMap<String, Timeline> usersTimelinesInvalidsExpunged = TimelineUtils
 						.expungeInvalids(usersTimelines);
 
@@ -205,7 +195,7 @@ public class TimelineStats
 			case "ActivityRegularityAnalysisOneLevel":
 			{
 				LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> sequenceAll = TimelineTransformers
-						.transformToSequenceDayWise(usersDayTimelines);
+						.transformToSequenceDayWise(usersDayTimelinesCleaned);
 				LinkedHashMap<String, String> sequenceCharInvalidsExpungedNoTS = TimelineTransformers
 						.toCharsFromActivityObjectsNoTimestamp(sequenceAll, true);
 
@@ -222,26 +212,26 @@ public class TimelineStats
 			case "ActivityRegularityAnalysisTwoLevel":
 			{
 				LinkedHashMap<String, LinkedHashMap<String, TreeMap<Double, Double>>> regularityForEachTargetActivityTwo = applyActivityRegularityAnalysisTwoLevel(
-						usersDayTimelines);
+						usersDayTimelinesCleaned);
 				// < User, ActivityName, MU, Avg pairwise distance of back-segments>
 				writeActivityRegularity(regularityForEachTargetActivityTwo, typeOfAnalysis);
 				break;
 			}
 			case "TimeSeriesAnalysis":
 			{
-				transformAndWriteAsTimeseries(UtilityBelt.reformatUserIDs(usersDayTimelines));// UtilityBelt.reformatUserIDs(usersDayTimelines)
+				transformAndWriteAsTimeseries(UtilityBelt.reformatUserIDs(usersDayTimelinesCleaned));// UtilityBelt.reformatUserIDs(usersDayTimelines)
 				break;
 			}
 			case "TimeSeriesEntropyAnalysis":// TimeSeriesAnalysis2
 			{
-				performTimeSeriesEntropyAnalysis(usersDayTimelines);
+				performTimeSeriesEntropyAnalysis(usersDayTimelinesCleaned);
 				break;
 			}
 
 			case "Clustering":
 			{
 				LinkedHashMap<String, LinkedHashMap<Timestamp, ActivityObject>> sequenceAll = TimelineTransformers
-						.transformToSequenceDayWise(usersDayTimelines);// , false);
+						.transformToSequenceDayWise(usersDayTimelinesCleaned);// , false);
 				LinkedHashMap<String, LinkedHashMap<Timestamp, String>> sequenceCharInvalidsExpunged = TimelineTransformers
 						.toCharsFromActivityObjects(sequenceAll, true);
 				LinkedHashMap<String, String> sequenceCharInvalidsExpungedNoTS = TimelineTransformers
@@ -254,7 +244,7 @@ public class TimelineStats
 			case "NGramAnalysis":
 			{
 				LinkedHashMap<String, Timeline> userTimelines = TimelineUtils
-						.dayTimelinesToTimelines(usersDayTimelines);
+						.dayTimelinesToTimelines(usersDayTimelinesCleaned);
 
 				if (Constant.hasInvalidActivityNames)
 				{
@@ -271,7 +261,7 @@ public class TimelineStats
 			case "AlgorithmicAnalysis":
 			{
 				LinkedHashMap<String, Timeline> userTimelines = TimelineUtils
-						.dayTimelinesToTimelines(usersDayTimelines);
+						.dayTimelinesToTimelines(usersDayTimelinesCleaned);
 				userTimelines = TimelineUtils.expungeInvalids(userTimelines);
 
 				performAlgorithmicAnalysis(userTimelines, 1, 20, (pathToWrite));
@@ -280,7 +270,7 @@ public class TimelineStats
 			case "AlgorithmicAnalysis2":
 			{
 				LinkedHashMap<String, Timeline> userTimelines = TimelineUtils
-						.dayTimelinesToTimelines(usersDayTimelines);
+						.dayTimelinesToTimelines(usersDayTimelinesCleaned);
 				userTimelines = TimelineUtils.expungeInvalids(userTimelines);
 
 				performAlgorithmicAnalysis2(userTimelines, 1, 20, (pathToWrite));
@@ -289,7 +279,7 @@ public class TimelineStats
 			case "FeatureAnalysis":
 			{
 				LinkedHashMap<String, Timeline> userTimelines = TimelineUtils
-						.dayTimelinesToTimelines(usersDayTimelines);
+						.dayTimelinesToTimelines(usersDayTimelinesCleaned);
 
 				userTimelines = TimelineUtils.expungeInvalids(userTimelines);
 				writeAllFeaturesValues(userTimelines, pathToWrite);
@@ -304,6 +294,56 @@ public class TimelineStats
 		consoleLogStream.close();
 	}
 
+	/**
+	 * 
+	 * <ol>
+	 * <li>writeNumOfAOsInTimelines</li>
+	 * <li>writeNumOfValidAOsInTimelines</li>
+	 * <li>writeNumOfDaysInTimelines</li>
+	 * <li>writeStatsNumOfDistinctActsPerDayInTimelines</li>
+	 * <li>writeStatsNumOfTotalActsPerDayInTimelines</li>
+	 * </ol>
+	 * 
+	 * @param usersDayTimelines
+	 * @param databaseName
+	 * @param pathToWrite
+	 * @param timelineCleanLabel
+	 */
+	private static void writeTimelineStats(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines,
+			String databaseName, String pathToWrite, String timelineCleanLabel)
+	{
+		writeNumOfAOsInTimelines(usersDayTimelines,
+				pathToWrite + databaseName + "NumOfAOsIn" + timelineCleanLabel + "Timelines.csv");
+		writeNumOfValidAOsInTimelines(usersDayTimelines,
+				pathToWrite + databaseName + "NumOfValidAOsIn" + timelineCleanLabel + "Timelines.csv");
+		writeNumOfDaysInTimelines(usersDayTimelines,
+				pathToWrite + databaseName + "NumOfDaysIn" + timelineCleanLabel + "Timelines.csv");
+
+		writeAllNumOfDistinctActsPerDayInTimelines(usersDayTimelines,
+				pathToWrite + databaseName + "AllNumOfDistinctActsInCleanedTimelines.csv");
+
+		writeAllNumOfTotalActsPerDayInTimelines(usersDayTimelines,
+				pathToWrite + databaseName + "AllNumOfTotalActsInCleanedTimelines.csv");
+
+		writeStatsNumOfDistinctActsPerDayInTimelines(usersDayTimelines,
+				pathToWrite + databaseName + "StatsNumOfDistinctActsInCleanedTimelines.csv");
+
+		writeStatsNumOfTotalActsPerDayInTimelines(usersDayTimelines,
+				pathToWrite + databaseName + "StatsNumOfTotalActsInCleanedTimelines.csv");
+
+		// disabled because writeStatsNumOfDistinctActsPerDayInTimelines and
+		// writeStatsNumOfTotalActsPerDayInTimelines are provided superset of the information given by the
+		// following two methods
+		if (false)
+		{
+			writeAvgNumOfDistinctActsPerDayInTimelines(usersDayTimelines,
+					pathToWrite + databaseName + "AvgNumOfDistinctActsIn" + timelineCleanLabel + "Timelines.csv");
+			writeAvgNumOfTotalActsPerDayInTimelines(usersDayTimelines,
+					pathToWrite + databaseName + "AvgNumOfTotalActsIn" + timelineCleanLabel + "Timelines.csv");
+
+		}
+	}
+
 	/**
 	 * For each user, for sequence of each feature, get Sample Entropy vs m (segment length)
 	 * 
@@ -1146,15 +1186,7 @@ public class TimelineStats
 			{
 				HJEditDistance hjDist = new HJEditDistance();
 				Pair<String, Double> dist = hjDist.getHJEditDistanceWithTrace(segments.get(i), segments.get(j), "", "",
-						"", "1");// new
-									// Pair("a",
-									// new
-									// Double(2));//
-									// ALERT
-									// ALERT
-									// ALTER
-									// ALERT
-									// ALERT
+						"", "1");// new Pair("a", new Double(2));// ALERT ALERT ALTER ALERT ALERT
 				distances.add(dist.getSecond());// , 1, 1, 2);
 				count++;
 			}
@@ -2422,9 +2454,10 @@ public class TimelineStats
 	 * Writes the num of activity objects in timeline
 	 * 
 	 * @param usersDayTimelines
+	 * @param absFileNameToWrite
 	 */
-	public static void writeNumOfActivityObjectsInTimelines(
-			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String fileNamePhrase)
+	public static void writeNumOfAOsInTimelines(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines,
+			String absFileNameToWrite)
 	{
 		LinkedHashMap<String, Timeline> usersTimelines = TimelineUtils.dayTimelinesToTimelines(usersDayTimelines);
 
@@ -2436,38 +2469,66 @@ public class TimelineStats
 					+ "," + entry.getValue().size());
 		}
 
-		WToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
+		// WToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
+		WToFile.writeToNewFile(s.toString(), absFileNameToWrite);
 	}
 
 	/**
+	 * TODO: needs to be improved (18 May 2018)
+	 * <p>
 	 * writeActivityCountsInGivenDayTimelines, writeActivityDurationInGivenDayTimelines,
 	 * writeActivityOccPercentageOfTimelines
 	 * 
 	 * @param usersDayTimelines
 	 * @param fileNamePhrase
+	 * @param pathToWrite
 	 */
 	public static void writeActivityStats(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines,
-			String fileNamePhrase)
+			String fileNamePhrase, String pathToWrite)
 	{
+		ArrayList<String> actCountUserFilesToConcatenate = new ArrayList<>(usersDayTimelines.size());
+		ArrayList<String> actDurationUserFilesToConcatenate = new ArrayList<>(usersDayTimelines.size());
+		ArrayList<String> actOccuPerFilesToConcatenate = new ArrayList<>(usersDayTimelines.size());
+		ArrayList<String> rowHeaderForEachUser = new ArrayList<>(usersDayTimelines.size());
+
 		for (Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
 		{
 			String userName = entry.getKey();
-			WToFile.writeActivityCountsInGivenDayTimelines(userName, entry.getValue(), "AllTimelines");
+			TimelineStats.writeActivityCountsInGivenDayTimelines(userName, entry.getValue(), "AllTimelines",
+					pathToWrite);
 
 			if (!Constant.getDatabaseName().equals("gowalla1"))
 			{// since gowalla data does not have duration
-				WToFile.writeActivityDurationInGivenDayTimelines(userName, entry.getValue(), "AllTimelines");
+				TimelineStats.writeActivityDurationInGivenDayTimelines(userName, entry.getValue(), "AllTimelines",
+						pathToWrite);
 			}
-			WToFile.writeActivityOccPercentageOfTimelines(userName, entry.getValue(), "AllTimelines");
+			TimelineStats.writeActivityOccPercentageOfTimelines(userName, entry.getValue(), "AllTimelines",
+					pathToWrite);
+
+			// list of user files to concatenate to get results over all users in same file
+			actCountUserFilesToConcatenate.add(pathToWrite + userName + "ActivityCountsAllTimelines.csv");
+			actDurationUserFilesToConcatenate.add(pathToWrite + userName + "ActivityDurationAllTimelines.csv");
+			actOccuPerFilesToConcatenate.add(pathToWrite + userName + "ActivityOccPerTimelinesAllTimelines.csv");
+			rowHeaderForEachUser.add(userName);
 		}
+
+		CSVUtils.concatCSVFilesWithRowHeaderPerFile(actCountUserFilesToConcatenate, true,
+				pathToWrite + "AllUsersActivityCountsAllTimelines.csv", ',', rowHeaderForEachUser);
+
+		CSVUtils.concatCSVFilesWithRowHeaderPerFile(actDurationUserFilesToConcatenate, true,
+				pathToWrite + "AllUsersActivityDurationAllTimelines.csv", ',', rowHeaderForEachUser);
+
+		CSVUtils.concatCSVFilesWithRowHeaderPerFile(actOccuPerFilesToConcatenate, true,
+				pathToWrite + "AllUsersActivityOccPerAllTimelines.csv", ',', rowHeaderForEachUser);
 	}
 
 	/**
 	 * 
 	 * @param usersDayTimelines
+	 * @param absFileNameToWrite
 	 */
-	public static void writeNumOfValidActivityObjectsInTimelines(
-			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String fileNamePhrase)
+	public static void writeNumOfValidAOsInTimelines(
+			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String absFileNameToWrite)
 	{
 		StringBuilder s = new StringBuilder();
 		// PopUps.showMessage("inside writeavgdis");
@@ -2483,16 +2544,17 @@ public class TimelineStats
 					+ "," + numOfTotalValidActs);
 		}
 
-		WToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
+		// WToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
+		WToFile.writeToNewFile(s.toString(), absFileNameToWrite);
 	}
 
 	/**
-	 * 
 	 * 
 	 * @param usersDayTimelines
+	 * @param absfileNameToWrite
 	 */
 	public static void writeNumOfDaysInTimelines(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines,
-			String fileNamePhrase)
+			String absfileNameToWrite)
 	{
 		StringBuilder s = new StringBuilder();
 		s.append("User, User, NumOfDays, NumOfWeekDays,NumOfWeekends,%OfWeekdays");
@@ -2518,12 +2580,17 @@ public class TimelineStats
 			s.append("\n" + entry.getKey() + "," + (Constant.getIndexOfUserID(Integer.valueOf(entry.getKey())) + 1)
 					+ "," + entry.getValue().size() + "," + numOfWeekDays + "," + numOfWeekends + "," + percentage);
 		}
-
-		WToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
+		// WToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
+		WToFile.writeToNewFile(s.toString(), absfileNameToWrite);
 	}
 
+	/**
+	 * 
+	 * @param usersDayTimelines
+	 * @param absFileNameToWrite
+	 */
 	public static void writeStatsNumOfDistinctActsPerDayInTimelines(
-			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String fileNamePhrase)
+			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String absFileNameToWrite)
 	{
 		StringBuilder s = new StringBuilder();
 		// PopUps.showMessage("inside writeavgdis");
@@ -2552,16 +2619,17 @@ public class TimelineStats
 					+ ds.getSum() + "," + TimelineUtils.countNumberOfDistinctActivities(allActObjs));
 		}
 
-		WToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
+		// WToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
+		WToFile.writeToNewFile(s.toString(), absFileNameToWrite);
 	}
 
 	/**
 	 * 
 	 * @param usersDayTimelines
-	 * @param fileNamePhrase
+	 * @param absFileNameToWrite
 	 */
 	public static void writeAllNumOfDistinctActsPerDayInTimelines(
-			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String fileNamePhrase)
+			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String absFileNameToWrite)
 	{
 		StringBuilder s = new StringBuilder();
 		for (Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
@@ -2577,16 +2645,17 @@ public class TimelineStats
 			s.append(numOfDistinctActsPerDay.substring(0, numOfDistinctActsPerDay.length() - 1) + "\n");
 		}
 
-		WToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
+		// WToFile.appendLineToFile(s.toString(), absFileNameToWrite);
+		WToFile.writeToNewFile(s.toString(), absFileNameToWrite);
 	}
 
 	/**
-	 * 
 	 * 
 	 * @param usersDayTimelines
+	 * @param absFileNameToWrite
 	 */
 	public static void writeAvgNumOfDistinctActsPerDayInTimelines(
-			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String fileNamePhrase)
+			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String absFileNameToWrite)
 	{
 		StringBuilder s = new StringBuilder();
 		// PopUps.showMessage("inside writeavgdis");
@@ -2610,16 +2679,17 @@ public class TimelineStats
 					+ StatsUtils.iqrOfArrayListInt(numOfDistinctActsPerDay, 2));
 		}
 
-		WToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
+		// WToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
+		WToFile.writeToNewFile(s.toString(), absFileNameToWrite);
 	}
 
 	/**
-	 * 
 	 * 
 	 * @param usersDayTimelines
+	 * @param absFileNameToWrite
 	 */
 	public static void writeStatsNumOfTotalActsPerDayInTimelines(
-			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String fileNamePhrase)
+			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String absFileNameToWrite)
 	{
 		StringBuilder s = new StringBuilder();
 		// PopUps.showMessage("inside writeavgdis");
@@ -2647,13 +2717,19 @@ public class TimelineStats
 					+ ds.getSum() + "," + totalNumOfActsOverAllTimelines);
 		}
 
-		WToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
+		WToFile.writeToNewFile(s.toString(), absFileNameToWrite);
+		// WToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + absFileNameToWrite);
 	}
 
 	//
 
+	/**
+	 * 
+	 * @param usersDayTimelines
+	 * @param absFileNameToWrite
+	 */
 	public static void writeAllNumOfTotalActsPerDayInTimelines(
-			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String fileNamePhrase)
+			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String absFileNameToWrite)
 	{
 		StringBuilder s = new StringBuilder();
 		for (Entry<String, LinkedHashMap<Date, Timeline>> entry : usersDayTimelines.entrySet())
@@ -2667,16 +2743,17 @@ public class TimelineStats
 			s.append(numOfTotalActsPerDay.substring(0, numOfTotalActsPerDay.length() - 1) + "\n");
 		}
 
-		WToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
+		// WToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
+		WToFile.writeToNewFile(s.toString(), absFileNameToWrite);
 	}
 
 	/**
-	 * 
 	 * 
 	 * @param usersDayTimelines
+	 * @param absFileNameToWrite
 	 */
 	public static void writeAvgNumOfTotalActsPerDayInTimelines(
-			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String fileNamePhrase)
+			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String absFileNameToWrite)
 	{
 		StringBuilder s = new StringBuilder();
 		// PopUps.showMessage("inside writeavgdis");
@@ -2700,7 +2777,8 @@ public class TimelineStats
 					+ StatsUtils.iqrOfArrayListInt(numOfTotalActsPerDay, 2));
 		}
 
-		WToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
+		// WToFile.appendLineToFile(s.toString(), Constant.getDatabaseName() + fileNamePhrase);
+		WToFile.writeToNewFile(s.toString(), absFileNameToWrite);
 	}
 
 	public static LinkedHashMap<String, Complex[]> getFTInt(LinkedHashMap<String, LinkedHashMap<Timestamp, Integer>> ts)// ,
@@ -3033,13 +3111,14 @@ public class TimelineStats
 	 * @param usersDayTimelines
 	 * @param timelinesPhrase
 	 * @param fileName
+	 * @param commonPath
 	 */
 	public static void writeNumOfActsPerUsersDayTimelinesSameFile(
 			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String timelinesPhrase,
-			String fileName)
+			String fileName, String commonPath)
 	{
 		// System.out.println("Common path=" + commonPath);
-		String commonPath = Constant.getCommonPath();//
+		// String commonPath = Constant.getCommonPath();//
 		System.out.println("Inside writeNumOfActsPerUsersDayTimelinesSameFile(): num of users received = "
 				+ usersDayTimelines.size());
 		System.out.println("Common path=" + commonPath);
@@ -3064,13 +3143,14 @@ public class TimelineStats
 	 * @param usersDayTimelines
 	 * @param timelinesPhrase
 	 * @param fileName
+	 * @param commonPath
 	 */
 	public static void writeNumOfDistinctValidActsPerUsersDayTimelinesSameFile(
 			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersDayTimelines, String timelinesPhrase,
-			String fileName)
+			String fileName, String commonPath)
 	{
 		// System.out.println("Common path=" + commonPath);
-		String commonPath = Constant.getCommonPath();//
+		// String commonPath = Constant.getCommonPath();//
 		System.out.println("Inside writeNumOfDistinctValidActsPerUsersDayTimelinesSameFile(): num of users received = "
 				+ usersDayTimelines.size());
 		System.out.println("Common path=" + commonPath);
@@ -3229,6 +3309,401 @@ public class TimelineStats
 		return new Pair<Long, Long>(numOfActWithMultipleWorkingLevelCatID, numOfAOs);
 	}
 
+	// ///////////////////
+	/**
+	 * Counts activities for each of the days of given day timelines and writes it to a file and counts activities over
+	 * all days of given timelines and return it as a LinkedHashMap (fileName = commonPath + userName + "ActivityCounts"
+	 * + fileNamePhrase + ".csv")
+	 * 
+	 * @param userName
+	 * @param userTimelines
+	 * @param fileNamePhrase
+	 * @param commonPathToWrite
+	 * @return count of activities over all days of given timelines
+	 */
+	public static LinkedHashMap<String, Long> writeActivityCountsInGivenDayTimelines(String userName,
+			LinkedHashMap<Date, Timeline> userTimelines, String fileNamePhrase, String commonPathToWrite)
+	{
+		String commonPath = commonPathToWrite;//
+
+		if (VerbosityConstants.verbose) System.out.println("Inside writeActivityCountsInGivenDayTimelines");
+
+		/* <Activity Name, count over all days> */
+		LinkedHashMap<String, Long> activityNameCountPairsOverAllDayTimelines = new LinkedHashMap<>();
+		// count over all the days
+		String[] activityNames = Constant.getActivityNames();// .activityNames;
+		try
+		{
+			// String userName=entryForUser.getKey();
+			// System.out.println("\nUser ="+entryForUser.getKey());
+			String fileName = commonPath + userName + "ActivityCounts" + fileNamePhrase + ".csv";
+
+			if (VerbosityConstants.verbose)
+			{
+				System.out.println("writing " + userName + "ActivityCounts" + fileNamePhrase + ".csv");
+			}
+			// BufferedWriter bw = WritingToFile.getBufferedWriterForNewFile(fileName);// new BufferedWriter(fw);
+
+			StringBuilder bwString = new StringBuilder();
+			bwString.append(",");
+			// bw.write(",");
+
+			for (String activityName : activityNames)
+			{
+				if (UtilityBelt.isValidActivityName(activityName) == false)
+				{
+					continue;
+				}
+				// bw.write("," + activityName);
+				bwString.append("," + activityName);
+				// System.out.println("ajooba:activityName = " + activityName + " bwString" + bwString.toString());
+				activityNameCountPairsOverAllDayTimelines.put(activityName, new Long(0));
+			}
+			// bw.newLine();
+			bwString.append("\n");
+
+			for (Map.Entry<Date, Timeline> entry : userTimelines.entrySet())
+			{
+				// System.out.println("Date =" + entry.getKey());
+				// bw.write(entry.getKey().toString());
+				// bw.write("," + (DateTimeUtils.getWeekDayFromWeekDayInt(entry.getKey().getDay())));
+
+				bwString.append(entry.getKey().toString());
+				bwString.append("," + (DateTimeUtils.getWeekDayFromWeekDayInt(entry.getKey().getDay())));
+
+				ArrayList<ActivityObject> activitiesInDay = entry.getValue().getActivityObjectsInDay();
+
+				/* <Activity Name, count for the current day> */
+				LinkedHashMap<String, Integer> activityNameCountPairs = new LinkedHashMap<String, Integer>();
+
+				// written beforehand to maintain the same order of activity names
+				for (String activityName : activityNames)
+				{
+					if (UtilityBelt.isValidActivityName(activityName))
+					// if((activityName.equalsIgnoreCase("Not
+					// Available")||activityName.equalsIgnoreCase("Unknown"))==false)
+					{
+						// System.out.println(" putting down -" + activityName + "- in activityNameCountPairs");
+						activityNameCountPairs.put(activityName, 0);
+					}
+				}
+
+				for (ActivityObject actEvent : activitiesInDay)
+				{
+					if (UtilityBelt.isValidActivityName(actEvent.getActivityName()))
+					// if((actEvent.getActivityName().equalsIgnoreCase("Unknown") ||
+					// actEvent.getActivityName().equalsIgnoreCase("Not Available") ) ==false)
+					{
+						String actName = actEvent.getActivityName();
+						// System.out.println(activityNameCountPairs.size());
+
+						// Integer val;
+						// if (activityNameCountPairs.get(actName) == null)
+						// {
+						// val = 0;
+						// }
+						// else
+						// {
+						// val = activityNameCountPairs.get(actName);
+						// }
+						Integer val = activityNameCountPairs.get(actName);
+						if (val == null)
+						{
+							new Exception(
+									"Exception in org.activity.io.WritingToFile.writeActivityCountsInGivenDayTimelines(String, LinkedHashMap<Date, Timeline>, String) : actName = "
+											+ actName + " has null val");// System.out.println("actName = " + actName);
+						}
+
+						// System.out.println("val:" + val);
+						Integer newVal = new Integer(val.intValue() + 1);
+						// count for current day
+						activityNameCountPairs.put(actName, newVal);
+
+						// accumulative count over all days
+						activityNameCountPairsOverAllDayTimelines.put(actEvent.getActivityName(),
+								activityNameCountPairsOverAllDayTimelines.get(actEvent.getActivityName()) + 1);
+					}
+				}
+
+				// write the activityNameCountPairs to the file
+				for (Map.Entry<String, Integer> entryWrite : activityNameCountPairs.entrySet())
+				{
+					// bw.write("," + entryWrite.getValue());
+					bwString.append("," + entryWrite.getValue());
+				}
+
+				bwString.append("\n");
+				// bw.newLine();
+			}
+			WToFile.writeToNewFile(bwString.toString(), fileName);
+			// bw.write(bwString.toString());
+			// bw.close();
+		}
+		catch (Exception e)
+		{
+			e.printStackTrace();
+			System.exit(-5);
+		}
+
+		WToFile.writeSimpleLinkedHashMapToFileAppend(activityNameCountPairsOverAllDayTimelines,
+				commonPath + "ActivityNameCountPairsOver" + fileNamePhrase + ".csv", "Activity", "Count");
+		// TODO check if it indeed should be an append
+
+		if (VerbosityConstants.verbose) System.out.println("Exiting writeActivityCountsInGivenDayTimelines");
+
+		return activityNameCountPairsOverAllDayTimelines;
+
+	}
+
+	// ///////////////////
+	/**
+	 * percentage of timelines in which the activity occurrs and counts activities over all days of given timelines and
+	 * return it as a LinkedHashMap
+	 * 
+	 * @param userName
+	 * @param userTimelines
+	 * @param fileNamePhrase
+	 * @param pathToWrite
+	 * @return count of activities over all days of given timelines
+	 */
+	public static LinkedHashMap<String, Double> writeActivityOccPercentageOfTimelines(String userName,
+			LinkedHashMap<Date, Timeline> userTimelines, String fileNamePhrase, String pathToWrite)
+	{
+		String commonPath = pathToWrite;/// Constant.getCommonPath();//
+		LinkedHashMap<String, Double> activityNameCountPairsOverAllDayTimelines = new LinkedHashMap<String, Double>();
+		String[] activityNames = Constant.getActivityNames();// .activityNames;
+		try
+		{
+			// String userName=entryForUser.getKey();
+			// System.out.println("\nUser ="+entryForUser.getKey());
+			String fileName = commonPath + userName + "ActivityOccPerTimelines" + fileNamePhrase + ".csv";
+
+			if (VerbosityConstants.verbose)
+			{
+				System.out.println("writing " + userName + "ActivityOccPerTimelines" + fileNamePhrase + ".csv");
+			}
+
+			StringBuilder toWrite = new StringBuilder();
+			// bw.write(",");
+
+			int actIndex = -1;
+			for (String activityName : activityNames)
+			{
+				actIndex += 1;
+				if (UtilityBelt.isValidActivityName(activityName) == false)
+				// if(activityName.equals("Unknown")|| activityName.equals("Not Available"))
+				{
+					continue;
+				}
+
+				if (Constant.getDatabaseName().equals("gowalla1"))
+				{
+					// bw.write("," + activityName);
+					// $$disabled on 18 May //toWrite.append("," + Constant.activityNamesGowallaLabels.get(actIndex));
+					toWrite.append("," + activityName);
+				}
+				else
+				{
+					// bw.write("," + activityName);
+					toWrite.append("," + activityName);
+
+				}
+
+				activityNameCountPairsOverAllDayTimelines.put(activityName, new Double(0));
+			}
+			// bw.newLine();
+			toWrite.append("\n");
+
+			double numOfTimelines = userTimelines.size();
+
+			for (String activityName : activityNames) // written beforehand to maintain the same order of activity names
+			{
+				if (UtilityBelt.isValidActivityName(activityName))
+				// if((activityName.equalsIgnoreCase("Not Available")||activityName.equalsIgnoreCase("Unknown"))==false)
+				{
+					activityNameCountPairsOverAllDayTimelines.put(activityName, new Double(0));
+				}
+			}
+
+			for (Map.Entry<Date, Timeline> entry : userTimelines.entrySet())
+			{
+				// System.out.println("Date =" + entry.getKey());
+				// bw.write(entry.getKey().toString());
+				// bw.write("," + (UtilityBelt.getWeekDayFromWeekDayInt(entry.getKey().getDay())));
+
+				ArrayList<ActivityObject> activitiesInDay = entry.getValue().getActivityObjectsInDay();
+
+				// written beforehand to maintain the same order of activity names
+				for (String activityName : activityNames)
+				{
+					if (UtilityBelt.isValidActivityName(activityName))
+					{
+						if (entry.getValue().hasActivityName(activityName) == true)
+						{
+							activityNameCountPairsOverAllDayTimelines.put(activityName,
+									activityNameCountPairsOverAllDayTimelines.get(activityName) + 1);
+						}
+					}
+				}
+			}
+
+			// write the activityNameCountPairs to the file
+			for (Map.Entry<String, Double> entryWrite : activityNameCountPairsOverAllDayTimelines.entrySet())
+			{
+				String actName = entryWrite.getKey();
+				Double val = entryWrite.getValue();
+				double percentageOccurrenceOverTimeline = ((double) activityNameCountPairsOverAllDayTimelines
+						.get(actName) / (double) numOfTimelines) * 100;
+				activityNameCountPairsOverAllDayTimelines.put(actName, percentageOccurrenceOverTimeline);
+				// bw.write("," + percentageOccurrenceOverTimeline);
+				toWrite.append("," + percentageOccurrenceOverTimeline);
+			}
+			// bw.newLine();
+			toWrite.append("\n");
+
+			// File file = new File(fileName);
+			// file.delete();
+			// FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
+			// BufferedWriter bw = new BufferedWriter(fw);
+			WToFile.writeToNewFile(toWrite.toString(), fileName);
+			// bw.close();
+		}
+
+		catch (Exception e)
+		{
+			e.printStackTrace();
+			System.exit(-5);
+		}
+
+		WToFile.writeSimpleLinkedHashMapToFileAppend(activityNameCountPairsOverAllDayTimelines,
+				commonPath + "ActivityOccPerTimelines" + fileNamePhrase + ".csv", "Activity", "Count");// TODO check if
+																										// it indeed
+		// should be an append
+
+		return activityNameCountPairsOverAllDayTimelines;
+
+	}
+
+	/////
+	/**
+	 * Sums the duration in seconds of activities for each of the days of given day timelines and writes it to a file
+	 * and sums the duration activities over all days of given timelines and return it as a LinkedHashMap
+	 * 
+	 * @param userName
+	 * @param userTimelines
+	 * @param fileNamePhrase
+	 * @param pathToWrite
+	 * @return duration of activities over all days of given timelines
+	 */
+	public static LinkedHashMap<String, Long> writeActivityDurationInGivenDayTimelines(String userName,
+			LinkedHashMap<Date, Timeline> userTimelines, String fileNamePhrase, String pathToWrite)
+	{
+		String commonPath = pathToWrite;// Constant.getCommonPath();//
+		String[] activityNames = Constant.getActivityNames();// activityNames;
+		LinkedHashMap<String, Long> activityNameDurationPairsOverAllDayTimelines = new LinkedHashMap<String, Long>();
+		// count over all the days
+
+		try
+		{
+			// String userName=entryForUser.getKey();
+			// System.out.println("\nUser ="+entryForUser.getKey());
+			String fileName = commonPath + userName + "ActivityDuration" + fileNamePhrase + ".csv";
+
+			if (VerbosityConstants.verbose)
+			{
+				System.out.println("writing " + userName + "ActivityDuration" + fileNamePhrase + ".csv");
+			}
+
+			StringBuilder toWrite = new StringBuilder();
+
+			toWrite.append(",");
+			// bw.write(",");
+
+			for (String activityName : activityNames)
+			{
+				if (UtilityBelt.isValidActivityName(activityName) == false)
+				// (activityName.equals("Unknown")|| activityName.equals("Others"))
+				{
+					continue;
+				}
+				toWrite.append("," + activityName);
+				// bw.write("," + activityName);
+				activityNameDurationPairsOverAllDayTimelines.put(activityName, new Long(0));
+			}
+			toWrite.append("\n");
+			// bw.newLine();
+
+			for (Map.Entry<Date, Timeline> entry : userTimelines.entrySet())
+			{
+				// System.out.println("Date =" + entry.getKey());
+				// bw.write(entry.getKey().toString());
+				// bw.write("," + (DateTimeUtils.getWeekDayFromWeekDayInt(entry.getKey().getDay())));
+
+				toWrite.append(entry.getKey().toString() + ","
+						+ (DateTimeUtils.getWeekDayFromWeekDayInt(entry.getKey().getDay())));
+
+				ArrayList<ActivityObject> activitiesInDay = entry.getValue().getActivityObjectsInDay();
+				LinkedHashMap<String, Long> activityNameDurationPairs = new LinkedHashMap<String, Long>();
+
+				for (String activityName : activityNames) // written beforehand to maintain the same order of activity
+															// names
+				{
+					if (UtilityBelt.isValidActivityName(activityName))
+					// if((activityName.equalsIgnoreCase("Others")||activityName.equalsIgnoreCase("Unknown"))==false)
+					{
+						activityNameDurationPairs.put(activityName, new Long(0));
+					}
+				}
+
+				for (ActivityObject actEvent : activitiesInDay)
+				{
+					if (UtilityBelt.isValidActivityName(actEvent.getActivityName()))
+					// if((actEvent.getActivityName().equalsIgnoreCase("Unknown") ||
+					// actEvent.getActivityName().equalsIgnoreCase("Others") ) ==false)
+					{
+						Long durationInSecondsForActivity = actEvent.getDurationInSeconds();
+						// summing of duration for current day
+						activityNameDurationPairs.put(actEvent.getActivityName(),
+								activityNameDurationPairs.get(actEvent.getActivityName())
+										+ durationInSecondsForActivity);
+
+						// accumulative duration over all days
+						activityNameDurationPairsOverAllDayTimelines.put(actEvent.getActivityName(),
+								activityNameDurationPairsOverAllDayTimelines.get(actEvent.getActivityName())
+										+ durationInSecondsForActivity);
+					}
+				}
+
+				// write the activityNameDurationPairs to the file
+				for (Map.Entry<String, Long> entryWrite : activityNameDurationPairs.entrySet())
+				{
+					// bw.write("," + entryWrite.getValue());
+					toWrite.append("," + entryWrite.getValue());
+				}
+				toWrite.append("\n");
+				// bw.newLine();
+			}
+			// File file = new File(fileName);
+			// file.delete();
+			// FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
+			// BufferedWriter bw = new BufferedWriter(fw);
+			WToFile.writeToNewFile(toWrite.toString(), fileName);
+			// bw.close();
+		}
+		catch (Exception e)
+		{
+			e.printStackTrace();
+			System.exit(-5);
+		}
+
+		WToFile.writeSimpleLinkedHashMapToFileAppend(activityNameDurationPairsOverAllDayTimelines,
+				commonPath + "ActivityNameDurationPairsOver" + fileNamePhrase + ".csv", "Activity", "Duration");
+		// TODO check if it indeed should be an append
+
+		return activityNameDurationPairsOverAllDayTimelines;
+
+	}
+
 	// /**
 	// * Fork of
 	// * org.activity.generator.DatabaseCreatorGowallaQuicker0.countConsecutiveSimilarActivities2(LinkedHashMap<String,
diff --git a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/ui/Dashboard3.java b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/ui/Dashboard3.java
index 50c77215..48d266c4 100644
--- a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/ui/Dashboard3.java
+++ b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/ui/Dashboard3.java
@@ -1,8 +1,14 @@
 package org.activity.ui;
 
+import java.sql.Date;
 import java.util.ArrayList;
+import java.util.LinkedHashMap;
 import java.util.List;
 
+import org.activity.constants.Constant;
+import org.activity.constants.PathConstants;
+import org.activity.io.Serializer;
+import org.activity.objects.Timeline;
 import org.activity.plotting.DataGenerator;
 import org.activity.plotting.TimelineChartAppCanvas;
 import org.activity.plotting.TimelineChartAppGeneric;
@@ -50,6 +56,7 @@ import javafx.stage.Stage;
 public class Dashboard3 extends Application
 {
 	Stage stage;
+	String pathToToyTimelines = "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/MAY21ED0.35STimeLocPopDistPrevDurPrevAllActsFDStFilter0hrs75RTV/ToyTimelines21May.kryo";
 	// MenuBar menuBar;
 
 	// private final TableView treeView = new TableView();
@@ -66,6 +73,13 @@ public class Dashboard3 extends Application
 		long t0 = System.currentTimeMillis();
 		ScreenDetails.printScreensDetails();
 
+		PathConstants.intialise(Constant.For9kUsers);
+		Constant.initialise("./", "gowalla1", PathConstants.pathToSerialisedCatIDsHierDist,
+				PathConstants.pathToSerialisedCatIDNameDictionary, PathConstants.pathToSerialisedLocationObjects,
+				PathConstants.pathToSerialisedUserObjects, PathConstants.pathToSerialisedGowallaLocZoneIdMap);
+		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayToyTimelines = (LinkedHashMap<String, LinkedHashMap<Date, Timeline>>) Serializer
+				.kryoDeSerializeThis(pathToToyTimelines);
+
 		// final Stage stageRef = stage;
 		// StageStyle stageStyle = StageStyle.DECORATED;
 		/////////////////////////////////////////////
@@ -82,7 +96,7 @@ public class Dashboard3 extends Application
 
 		HBox hBoxMenus = new HBox(generateMenuBar());
 
-		TabPane tabPane = createTabs();
+		TabPane tabPane = createTabs(false, usersCleanedDayToyTimelines);
 		// tabPane.setPrefHeight(getHeight());
 
 		// VBox mainPane = new VBox();
@@ -107,9 +121,11 @@ public class Dashboard3 extends Application
 		Scene scene = new Scene(borderPane);// createTabs());// createContent(DataGenerator.getData2()));// , 270, 370);
 
 		scene.setFill(Color.TRANSPARENT);
-		scene.getStylesheets().add("./jfxtras/styles/jmetro8/GJMetroLightTheme.css");// gsheetNative.css");
 
+		// disabled on May 21,
+		// $$scene.getStylesheets().add("./jfxtras/styles/jmetro8/GJMetroLightTheme.css");// gsheetNative.css");
 		// scene.getStylesheets().add(getClass().getResource("gsheet1.css").toExternalForm());
+		scene.getStylesheets().add("gsheetNative.css");
 		stage.setScene(scene);
 		stage.setTitle("Dashboard");
 		stage.setWidth(600);
@@ -132,32 +148,44 @@ public class Dashboard3 extends Application
 
 	/**
 	 * 
+	 * @param usersCleanedDayToyTimelines
+	 * @param useSyntheticData
 	 * @return
 	 */
-	private TabPane createTabs()
+	private TabPane createTabs(boolean useSyntheticData,
+			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayToyTimelines)
 	{
 		TabPane tabPane = new TabPane();
 		List<Tab> tabsToAdd = new ArrayList<>();
 		try
 		{
-
-			///
-
 			// List<List<List<String>>> timelineData = DataGenerator.getData3(10, 1000, 12, 5, 200, 10, 50);
-
 			List<List<List<String>>> timelineData = DataGenerator.getData3(10, 50, 12, 5, 864000, 60 * 20, 10800);
 
-			long tTimeline0 = System.currentTimeMillis();
-			Tab timelineTabCircle = new Tab("timelineTabCircle Historical Timelines All Users");
-			TimelineChartAppGeneric tcC = new TimelineChartAppGeneric(timelineData, true, "ActivityCircle");
-			timelineTabCircle.setContent(tcC.getVbox());// timelinesVBox2);
-			timelineTabCircle.setClosable(true);
-			tabsToAdd.add(timelineTabCircle);
-			long tTimelinen = System.currentTimeMillis();
-			System.out.println("Time taken TimelineChartAppGeneric = " + (tTimelinen - tTimeline0) + " ms");
+			if (false)
+			{
+				long tTimeline0 = System.currentTimeMillis();
+				Tab timelineTabCircle = new Tab("(Synth-Circle) Historical Timelines All Users");
+				TimelineChartAppGeneric tcC = new TimelineChartAppGeneric(timelineData, true, "ActivityCircle");
+				timelineTabCircle.setContent(tcC.getVBox());// timelinesVBox2);
+				timelineTabCircle.setClosable(true);
+				tabsToAdd.add(timelineTabCircle);
+				long tTimelinen = System.currentTimeMillis();
+				System.out.println("Time taken TimelineChartAppGeneric = " + (tTimelinen - tTimeline0) + " ms");
+			}
+			long tTimelineReal0 = System.currentTimeMillis();
+			Tab timelineTabCircleReal = new Tab("(Toy-Circle) Historical Timelines All Users");
+			TimelineChartAppGeneric tcCReal = new TimelineChartAppGeneric(usersCleanedDayToyTimelines, true,
+					"ActivityCircle");
+			timelineTabCircleReal.setContent(tcCReal.getVBox());// timelinesVBox2);
+			timelineTabCircleReal.setClosable(true);
+			tabsToAdd.add(timelineTabCircleReal);
+			long tTimelineRealn = System.currentTimeMillis();
+			System.out
+					.println("Time taken TimelineChartAppGeneric real = " + (tTimelineRealn - tTimelineReal0) + " ms");
 
 			long tTimelineCanvas0 = System.currentTimeMillis();
-			Tab timelineTabCanvas = new Tab("timelineTabCanvas Historical Timelines All Users");
+			Tab timelineTabCanvas = new Tab("(Synth-Canvas) Historical Timelines All Users");
 			TimelineChartAppCanvas tcCanvas = new TimelineChartAppCanvas(timelineData, false);
 			timelineTabCanvas.setContent(tcCanvas.getVbox());// timelinesVBox2);
 			timelineTabCanvas.setClosable(true);
@@ -165,23 +193,24 @@ public class Dashboard3 extends Application
 			long tTimelineCanvasn = System.currentTimeMillis();
 			System.out.println("Time taken TimelineChartAppCanvas = " + (tTimelineCanvasn - tTimelineCanvas0) + " ms");
 
-			Tab timelineTabD = new Tab("timelineTabD Historical Timelines All Users");
-			TimelineChartAppGeneric tcD = new TimelineChartAppGeneric(timelineData, true, "ActivityBox");
+			Tab timelineTabD = new Tab("(Synth-Box) Historical Timelines All Users");
+			TimelineChartAppGeneric tcD = new TimelineChartAppGeneric(/* usersCleanedDayToyTimelines */ timelineData,
+					true, "ActivityBox");
 			// TODO: Issue: not scaling correctly with range change.
-			timelineTabD.setContent(tcD.getVbox());// timelinesVBox2);
+			timelineTabD.setContent(tcD.getVBox());// timelinesVBox2);
 			timelineTabD.setClosable(true);
 			tabsToAdd.add(timelineTabD);
 			if (false)
 			{
 				Tab timelineTabE = new Tab("timelineTabE Historical Timelines All Users");
 				TimelineChartAppGeneric tcE = new TimelineChartAppGeneric(timelineData, true, "LineChart");
-				timelineTabE.setContent(tcE.getVbox());// timelinesVBox2);
+				timelineTabE.setContent(tcE.getVBox());// timelinesVBox2);
 				timelineTabE.setClosable(true);
 				tabsToAdd.add(timelineTabE);
 
 				Tab timelineTabScattter = new Tab("timelineTabScattter Historical Timelines All Users");
 				TimelineChartAppGeneric tcScattter = new TimelineChartAppGeneric(timelineData, true, "ScatterChart");
-				timelineTabScattter.setContent(tcScattter.getVbox());// timelinesVBox2);
+				timelineTabScattter.setContent(tcScattter.getVBox());// timelinesVBox2);
 				timelineTabScattter.setClosable(true);
 				tabsToAdd.add(timelineTabScattter);
 			}
diff --git a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/util/TimelineUtils.java b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/util/TimelineUtils.java
index e44076c4..7187374b 100644
--- a/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/util/TimelineUtils.java
+++ b/GeolifeReloaded2_1_cleaned/src/main/java/org/activity/util/TimelineUtils.java
@@ -8,7 +8,6 @@ import java.time.LocalDate;
 import java.time.ZoneId;
 import java.util.ArrayList;
 import java.util.Arrays;
-import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
@@ -605,12 +604,15 @@ public class TimelineUtils
 			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> userDaytimelinesGiven)
 	{
 		long ct1 = System.currentTimeMillis();
-		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> userDaytimelinesUnmodView = (LinkedHashMap<String, LinkedHashMap<Date, Timeline>>) Collections
-				.unmodifiableMap(userDaytimelinesGiven);
+		// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> userDaytimelinesUnmodView = (LinkedHashMap<String,
+		// LinkedHashMap<Date, Timeline>>) Collections
+		// .unmodifiableMap(userDaytimelinesGiven);
+		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> userDaytimelinesUnmodView = (userDaytimelinesGiven);
 		int numOfUsers = 5, minNumOfDaysPerUser = 5, maxNumOfDaysPerUser = 7, numOfUniqueActs = 5,
 				minNumOfUniqueActIDsPerDay = 3;
 
 		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> toyTimelines = new LinkedHashMap<>();
+
 		List<String> selectedUsers = userDaytimelinesUnmodView.keySet().stream().limit(numOfUsers)
 				.collect(Collectors.toList());
 
@@ -619,11 +621,19 @@ public class TimelineUtils
 			if (selectedUsers.contains(uEntry.getKey()))
 			{
 				LinkedHashMap<Date, Timeline> dayTimelinesForThisUser = uEntry.getValue();
+				// LinkedHashMap<Date, Timeline> dayTimelinesWithMinUniqueActIDs = new LinkedHashMap<>();
 
 				Map<Date, Timeline> dayTimelinesWithMinUniqueActIDs = dayTimelinesForThisUser.entrySet().stream()
 						.filter(dE -> getUniqueActIDsInTimeline(dE.getValue()).size() >= minNumOfUniqueActIDsPerDay)
-						.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
+						.collect(Collectors.toMap(e -> (Date) e.getKey(), e -> (Timeline) e.getValue(), (e1, e2) -> e1,
+								LinkedHashMap::new));
 
+				// for (Entry<Date, Timeline> e : dayTimelinesForThisUser.entrySet())
+				// {
+				// Timeline t = e.getValue();
+				// if(e.getValue())
+				//
+				// }
 				// LinkedHashMap<Date, Timeline> selectedToyDayTimelinesForThisUser
 				// =dayTimelinesWithMinUniqueActIDs.collect(Collectors.toM)
 				// int numOfDayForThisUser = StatsUtils.randomInRange(minNumOfDaysPerUser, maxNumOfDaysPerUser);
@@ -649,7 +659,7 @@ public class TimelineUtils
 
 		// find the frequency count of each act for each user.
 
-		return null;
+		return toyTimelines;
 	}
 	// tt
 
@@ -2886,6 +2896,10 @@ public class TimelineUtils
 
 	////
 	/**
+	 * 
+	 * shouldBelongToSingleDay: false
+	 * <p>
+	 * shouldBelongToSingleUser: true
 	 * 
 	 * @param usersTimelines
 	 * @return LinkedHashMap<User ID as String, Timeline of the user with user id as integer as timeline id>
@@ -4806,12 +4820,12 @@ public class TimelineUtils
 	 * 
 	 * @param usersCleanedDayTimelines
 	 * @param writeToFile
-	 * @param fileName
+	 * @param absFileNameToWrite
 	 * @return
 	 */
 	public static LinkedHashMap<String, TreeSet<Integer>> getUniquePDValPerUser(
 			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, boolean writeToFile,
-			String fileName)
+			String absFileNameToWrite)
 	{
 		StringBuilder sb = new StringBuilder();
 		sb.append("User,#UniquePDVals\n");
@@ -4834,7 +4848,7 @@ public class TimelineUtils
 
 			if (writeToFile)
 			{
-				WToFile.writeToNewFile(sb.toString(), Constant.getOutputCoreResultsPath() + fileName);// "NumOfUniquePDValPerUser.csv");
+				WToFile.writeToNewFile(sb.toString(), absFileNameToWrite);// "NumOfUniquePDValPerUser.csv");
 			}
 		}
 		catch (Exception e)
