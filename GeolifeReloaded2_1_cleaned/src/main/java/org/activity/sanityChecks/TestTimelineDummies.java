package org.activity.sanityChecks;

// package org.activity.util;
//
// import java.sql.Timestamp;
// import java.util.ArrayList;
// import java.util.Random;
//
// import org.activity.distances.HJEditDistance;
//
// public class TestTimelineDummies
// {
// public static final boolean randomTimeDuration = true;
// public static final String[] setOfalphabetsOfActivityNames = { "A", "AB", "ABC", "ABCD", "ABCDE", "ABCDEFG",
// "ABCDEFGHI", "ABCDEFGHIJK", "ABCDEFGHIJKLMNO" };
// static String alphabetOfActivityNames;// = "ABCD";
// static final int numOfActivityObjs = 70;
// static final int numOfTimelines = 40;
// static final int maxMU = 70;
// static HJEditDistance es;
//
// public static void main(String args[])
// {
// es = new HJEditDistance();
// System.out.println("dummies");
//
// // System.out.println(getRandomChar());
// for (int alphaS = 0; alphaS < setOfalphabetsOfActivityNames.length; alphaS++)
// {
// alphabetOfActivityNames = setOfalphabetsOfActivityNames[alphaS];
//
// ArrayList<Timeline> timelines = createDummyTimelines(numOfTimelines);
// ArrayList<ArrayList<Double>> allMUEditDistances = new ArrayList<ArrayList<Double>>();// index indicates matching unit
// traverseTimelines(timelines);
//
// for (int mu = 1; mu <= maxMU; mu++)
// {
// ArrayList<Double> editDistances = new ArrayList<Double>(); // index indicates pair of timelines
//
// System.out.println("Comparing for mu:" + mu);
//
// for (int i = 1; i <= numOfTimelines; i++)
// {
// // System.out.println("Comparing:" + i + " to rest ");
//
// for (int j = i + 1; j <= numOfTimelines; j++)
// {
// // System.out.println("Comparing:" + i + " " + j);
//
// editDistances.add(getEditDistance(timelines.get(i - 1), timelines.get(j - 1), mu));
// }
// }
//
// WritingToFile.writeArrayList(editDistances, "SynthTimelinesEditDistances" + alphabetOfActivityNames.length() + "MU" +
// mu, "EditDistance",
// "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/GeoLife/link to Geolife Data Works/SyntheticTimelines2/");
//
// allMUEditDistances.add(editDistances);
// }
//
// // traverseArrayOfArray(allMUEditDistances);
// // WritingToFile.writeArrayListOfArrayList(allMUEditDistances, "SyntheticTimelinesEditDistances" +
// alphabetOfActivityNames.length(), "MatchingUnit,PairIndex,EditDistance",
// // "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/GeoLife/link to Geolife Data
// Works/SyntheticTimelines/");
// }
// }
//
// public static void traverseArrayOfArray(ArrayList<ArrayList<Double>> arr)
// {
// for (int mu = 0; mu < arr.size(); mu++)
// {
// ArrayList<Double> arrL2 = arr.get(mu);
//
// for (int pair = 0; pair < arrL2.size(); pair++)
// {
// System.out.println(arrL2.get(pair));
// }
// }
// }
//
// /**
// *
// * @param t1
// * @param t1
// * @param mu
// */
// public static Double getEditDistance(Timeline t1, Timeline t2, int mu)
// {
// // Pair<String, Double> editDistances = new Pair<String, Double>();
// //
// // Pair<String, Double> editDistance = es.getHJEditWithTrace(t1.getActivityObjectsInTimelineFromToIndex(0, mu - 1),
// t2.getActivityObjectsInTimelineFromToIndex(0, mu - 1));
// // editDistances.add(editDistance);
//
// return UtilityBelt.round((es.getHJEditDistanceWithTrace(t1.getActivityObjectsInTimelineFromToIndex(0, mu - 1),
// t2.getActivityObjectsInTimelineFromToIndex(0, mu - 1))).getSecond(), 4);
// }
//
// public static void traverseTimelines(ArrayList<Timeline> timelines)
// {
// System.out.println("Traversing timelines");
// for (Timeline t : timelines)
// {
// System.out.print("Timeline id:" + t.getTimelineID());
// t.printActivityObjectNamesWithTimestampsInSequence();
// System.out.println();
// }
// }
//
// public static ArrayList<Timeline> createDummyTimelines(int n)
// {
// ArrayList<Timeline> timelines = new ArrayList<Timeline>();
//
// Timestamp absStartTimestamp = new Timestamp(2014 - 1900, 3, 1, 0, 0, 0, 0);
//
// for (int i = 0; i < n; i++)
// {
// // System.out.println("Creating " + i + "th timeline");
// timelines.add(new Timeline(i, createSyntheticActivityObjects(absStartTimestamp, numOfActivityObjs)));
// }
// return timelines;
// }
//
// /**
// * Note: the activity objects are different with respect to activity name and activity duration (iff
// randomTimeDuration is true), other features are same.
// *
// * @param beginTS
// * @param count
// * @return
// */
// public static ArrayList<ActivityObject> createSyntheticActivityObjects(Timestamp beginTS, int count)
// {
// ArrayList<ActivityObject> arrAOs = new ArrayList<ActivityObject>();
// Random r = new Random();
//
// Timestamp startTS = beginTS;
// int alt = 20;
// int slat = 50, slon = 50;
// int elat = 100, elon = 100;
// int timeDurationInSeconds = 60 * (r.nextInt(4 * 60) + 10); // between 10 minutes and 4hrs 10 minutes//2 * 60 * 60 +
// r.nextInt(10 * 60);// between 2 hrs and 2hrs plus 10 minutes 60 *
// // (r.nextInt(2 * 60) + 10)//3 * 60 * 60; // 3 hours
// Timestamp endTS = new Timestamp(startTS.getTime() + timeDurationInSeconds * 1000);
//
// String activityName = "A"; // the first activity name
//
// for (int i = 0; i < count; i++)
// {
// ActivityObject ao = new ActivityObject();
//
// ao.setActivityName(activityName);// Character.toString(getRandomChar()));
//
// ao.setStartTimestamp(startTS);
// ao.setEndTimestamp(endTS);
//
// ao.setStartLatitude(String.valueOf(slat));
// ao.setEndLatitude(String.valueOf(elat));
//
// ao.setStartLongitude(String.valueOf(slon));
// ao.setEndLongitude(String.valueOf(elon));
//
// ao.setStartAltitude(String.valueOf(alt));
// ao.setEndAltitude(String.valueOf(alt));
// ao.setAvgAltitude(String.valueOf(alt));
//
// ao.setDistanceTravelled(20);
//
// arrAOs.add(ao);
//
// // generate values for next object
// activityName = Character.toString(getRandomChar());
//
// if (randomTimeDuration)
// {
// timeDurationInSeconds = 60 * (r.nextInt(4 * 60) + 10); // between 10 minutes and 4hrs 10 minutes
// }
//
// startTS = new Timestamp(endTS.getTime() + 1000);
// endTS = new Timestamp(startTS.getTime() + timeDurationInSeconds * 1000);
// }
//
// return arrAOs;
// }
//
// public static char getRandomChar()
// {
// Random r = new Random();
//
// String alphabet = alphabetOfActivityNames;
//
// return alphabet.charAt(r.nextInt(alphabet.length()));
// // prints 50 random characters from alphabet
//
// }
// }
