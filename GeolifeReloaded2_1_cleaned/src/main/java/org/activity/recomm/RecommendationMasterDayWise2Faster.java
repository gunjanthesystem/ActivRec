// a full curtain over the thing just to prevent annoying compilation error
// package org.activity.recomm;
//
// import java.sql.Date;
// import java.sql.Time;
// import java.sql.Timestamp;
// import java.util.ArrayList;
// import java.util.LinkedHashMap;
// import java.util.Map;
// import java.util.regex.Pattern;
//
// import org.activity.distances.AlignmentBasedDistance;
// import org.activity.distances.HJEditDistance;
// import org.activity.evaluation.Evaluation;
// import org.activity.io.WritingToFile;
// import org.activity.objects.ActivityObject;
// import org.activity.objects.Pair;
// import org.activity.objects.Triple;
// import org.activity.objects.UserDayTimeline;
// import org.activity.util.Constant;
// import org.activity.util.StringCode;
// import org.activity.util.TimelineUtils;
// import org.activity.util.UtilityBelt;
//
/// * Recreating the cleaner version of RecommendationMaster used in IIWAS experiment. This uses daywise timelines*/
// public class RecommendationMasterDayWise2Faster
// {
// LinkedHashMap<Date, UserDayTimeline> trainingTimelines;
// LinkedHashMap<Date, UserDayTimeline> testTimelines;
// LinkedHashMap<Date, UserDayTimeline> candidateTimelines;
//
// UserDayTimeline userDayTimelineAtRecomm;
//
// Date dateAtRecomm;
// Time timeAtRecomm;
// String userAtRecomm;
// String userIDAtRecomm;
//
// ArrayList<ActivityObject> activitiesGuidingRecomm; // sequence of activity events happening one or before the recomm point on that day
// ActivityObject activityAtRecommPoint;
// // Date dateOfMostSimilar
//
// String singleNextRecommendedActivity;
// String topRecommendedActivities;
//
// LinkedHashMap<Date, String> topNames;
//
// /**
// * (Cand TimelineID, (End point index of least distant subsequence, String containing the trace of edit operations performed, edit distance of least distant subsequence)) this
// * LinkedHashMap is sorted by the value of edit distance in ascending order (second component of the Pair (value))
// *
// */
// LinkedHashMap<Date, Triple<Integer, String, Double>> editDistancesSortedMap;
//
// LinkedHashMap<String, Double> recommendationRankscorePairs;
// String rankedRecommendationWithRankScores;
// String rankedRecommendationWithoutRankScores;
//
// boolean hasCandidateTimelines, hasCandidateTimelinesBelowThreshold;
//
// boolean nextActivityJustAfterRecommPointIsInvalid;
// double thresholdAsDistance = 99999999;
//
// boolean thresholdPruningNoEffect;
//
// public int totalNumberOfProbableCands;
// public int numCandsRejectedDueToNoCurrentActivityAtNonLast;
// public int numCandsRejectedDueToNoNextActivity;
//
// public static String commonPath;// = Constant.commonPath;
//
// /*
// * threshold for choosing candidate timelines, those candidate timelines whose distance from the 'activity events guiding recommendations' is higher than the cost of replacing
// * 'percentageDistanceThresh' % of Activity Events in the activities guiding recommendation are pruned out from set of candidate timelines
// */
//
// public RecommendationMasterDayWise2Faster(LinkedHashMap<Date, UserDayTimeline> trainingTimelines,
// LinkedHashMap<Date, UserDayTimeline> testTimelines, String dateAtRecomm, String timeAtRecomm, int userAtRecomm,
// double thresholdVal, String typeOfThreshold)
// {
// commonPath = Constant.getCommonPath();
// LinkedHashMap<Date, Pair<Integer, Double>> editDistancesMapUnsorted;
//
// hasCandidateTimelines = true;
// nextActivityJustAfterRecommPointIsInvalid = false;
//
// String[] splittedDate = dateAtRecomm.split("/"); // dd/mm/yyyy
// this.dateAtRecomm = new Date(Integer.parseInt(splittedDate[2]) - 1900, Integer.parseInt(splittedDate[1]) - 1,
// Integer.parseInt(splittedDate[0]));
// String[] splittedTime = timeAtRecomm.split(":"); // hh:mm:ss
// this.timeAtRecomm = Time.valueOf(timeAtRecomm);
// this.userAtRecomm = Integer.toString(userAtRecomm);
// this.userIDAtRecomm = Integer.toString(userAtRecomm);
//
// System.out.println("\n^^^^^^^^^^^^^^^^Inside RecommendationMasterDayWise2Faster");
// System.out.println(" User at Recomm = " + this.userAtRecomm);
// System.out.println(" Date at Recomm = " + this.dateAtRecomm);
// System.out.println(" Time at Recomm = " + this.timeAtRecomm);
//
// // $$21Oct
// userDayTimelineAtRecomm = TimelineUtils.getUserDayTimelineByDateFromMap(testTimelines, this.dateAtRecomm);
//
// if (userDayTimelineAtRecomm == null)
// {
// System.err.println("Error: day timeline not found (in test timelines) for the recommendation day");
// }
//
// System.out.println("The Activities on the recomm day are:");
// userDayTimelineAtRecomm.printActivityObjectNamesWithTimestampsInSequence();
// System.out.println();
//
// Timestamp timestampPointAtRecomm =
// new Timestamp(this.dateAtRecomm.getYear(), this.dateAtRecomm.getMonth(), this.dateAtRecomm.getDate(),
// this.timeAtRecomm.getHours(), this.timeAtRecomm.getMinutes(), this.timeAtRecomm.getSeconds(), 0);
// System.out.println("timestampPointAtRecomm = " + timestampPointAtRecomm);
//
// this.activitiesGuidingRecomm = userDayTimelineAtRecomm.getActivityObjectsStartingOnBeforeTime(timestampPointAtRecomm);
// System.out.println("Activity starting on or before the time to recommend (on recommendation day) are: \t");
// for (int i = 0; i < activitiesGuidingRecomm.size(); i++)
// {
// System.out.print(activitiesGuidingRecomm.get(i).getActivityName() + " ");
// }
//
// this.activityAtRecommPoint = activitiesGuidingRecomm.get(activitiesGuidingRecomm.size() - 1);
// System.out.println("\nActivity at Recomm point =" + this.activityAtRecommPoint.getActivityName());
//
// // All check OK
// candidateTimelines = getCandidateTimelines(trainingTimelines, this.activitiesGuidingRecomm, this.dateAtRecomm);
//
// System.out.println("Number of candidate timelines =" + candidateTimelines.size());
// System.out.println("the candidate timelines are as follows:");
// // $$traverseMapOfDayTimelines(candidateTimelines);
//
// if (candidateTimelines.size() == 0)
// {
// System.out.println("Warning: not making recommendation for " + userAtRecomm + " on date:" + dateAtRecomm + " at time:"
// + timeAtRecomm + " because there are no candidate timelines");
// // this.singleNextRecommendedActivity = null;
// hasCandidateTimelines = false;
// this.topRecommendedActivities = null;
// this.thresholdPruningNoEffect = true;
// this.topNames = null;
// return;
// }
// else
// {
// hasCandidateTimelines = true;
// }
// //
//
// editDistancesSortedMap = getEditDistancesForCandidateTimelines(candidateTimelines, activitiesGuidingRecomm);
//
// // ########Sanity check
// if (editDistancesSortedMap.size() != candidateTimelines.size())
// {
// System.err.println("Error at Sanity 10: editDistancesSortedMap.size() != candidateTimelines.size()");
// }
// // ##############
//
// // /// REMOVE candidate timelines which are above the distance THRESHOLD. (actually here we remove the entry for such candidate timelines from the distance scores map
// if (typeOfThreshold.equalsIgnoreCase("Global"))
// {
// this.thresholdAsDistance = thresholdVal;
// }
// else if (typeOfThreshold.equalsIgnoreCase("Percent"))
// {
// double maxEditDistance = (new AlignmentBasedDistance()).maxEditDistance(activitiesGuidingRecomm);
// this.thresholdAsDistance = maxEditDistance * (thresholdVal / 100);
// }
// else
// {
// System.err.println("Error: type of threshold unknown in recommendation master");
// System.exit(-2);
// }
//
// int countCandBeforeThresholdPruning = editDistancesSortedMap.size();
//
// editDistancesSortedMap = TimelineUtils.removeAboveThresholdDISD(editDistancesSortedMap, thresholdAsDistance);
// int countCandAfterThresholdPruning = editDistancesSortedMap.size();
// this.thresholdPruningNoEffect = (countCandBeforeThresholdPruning == countCandAfterThresholdPruning);
// // ////////////////////////////////
//
// if (editDistancesSortedMap.size() == 0)
// {
// System.out.println("Warning: No candidate timelines below threshold distance");
// hasCandidateTimelinesBelowThreshold = false;
// return;
// }
//
// else
// {
// hasCandidateTimelinesBelowThreshold = true;
// }
//
// // //////////////////////////////
//
// // endPointIndexWithLeastDistanceForCandidateTimelines = getEndPointIndicesWithLeastDistanceInCandidateTimeline(candidateTimelines,activitiesGuidingRecomm);
// editDistancesSortedMap =
// (LinkedHashMap<Date, Triple<Integer, String, Double>>) UtilityBelt.sortTripleByThirdValueAscending6(editDistancesSortedMap);
//
// this.topRecommendedActivities =
// getTopNextRecommendedActivityNameEditDistance(activitiesGuidingRecomm, editDistancesSortedMap, trainingTimelines);
// setTopNextRecommendedActivityNameEditDistanceArray(activitiesGuidingRecomm, editDistancesSortedMap, trainingTimelines);
// // this.topRecommendedActivitiesWithDistance = =
// // getTopNextRecommendedActivityNamesWithDistanceEditDistance(activitiesGuidingRecomm,editDistancesSortedMap,dayTimelinesForUser);
//
// // ########Sanity check
// String sanity11[] = topRecommendedActivities.split(Pattern.quote("__"));
// if (sanity11.length - 1 != editDistancesSortedMap.size())
// {
// System.err.println("Error at Sanity 11: num of topRecommendedActivities (without wtd ranking)(=" + (sanity11.length - 1)
// + " is not equal to the number of dates for which distance has been calculated:(=" + editDistancesSortedMap.size()
// + ")");
// }
// else
// {
// System.out.println("Sanity Check 11 Passed");
// }
// // ##############
//
// createRankedTopRecommendation(this.topRecommendedActivities);
// // System.out.println("Next recommended 5 activity is: "+this.topFiveRecommendedActivities);
//
// int indexOfRecommPointInDayTimeline = userDayTimelineAtRecomm.getIndexOfActivityObjectsAtTime(timestampPointAtRecomm);
// if (indexOfRecommPointInDayTimeline + 1 > userDayTimelineAtRecomm.getActivityObjectsInDay().size() - 1)
// {
// System.err.println("Error in Recommendation Master: No activity event after recomm point");
// }
//
// else
// {
// ActivityObject activityJustAfterRecommPoint =
// userDayTimelineAtRecomm.getActivityObjectAtPosition(indexOfRecommPointInDayTimeline + 1);
// if (activityJustAfterRecommPoint.getActivityName().equals("Unknown")
// || activityJustAfterRecommPoint.getActivityName().equals("Others"))
// {
// nextActivityJustAfterRecommPointIsInvalid = true;
// }
// }
//
// if (!nextActivityJustAfterRecommPointIsInvalid)
// {
// removeRecommPointActivityFromRankedRecomm();
// System.out.println("removing recomm point activity");
// }
//
// WritingToFile.writeDistanceScoresSortedMap(this.userAtRecomm, this.dateAtRecomm, this.timeAtRecomm, this.editDistancesSortedMap,
// this.candidateTimelines, this.topNames, this.activitiesGuidingRecomm);
// System.out.println("\n^^^^^^^^^^^^^^^^Exiting Recommendation Master");
// }
//
// public int getNumberOfValidActivitiesInActivitesGuidingRecommendation()
// {
// int count = 0;
// for (ActivityObject ae : this.activitiesGuidingRecomm)
// {
// if (!((ae.getActivityName().equalsIgnoreCase("Others")) || (ae.getActivityName().equalsIgnoreCase("Unknown"))))
// {
// count++;
// }
// }
// return count++;
// }
//
// public int getNumberOfActivitiesInActivitesGuidingRecommendation()
// {
// return this.activitiesGuidingRecomm.size();
// }
//
// public int getNumberOfCandidateTimelinesBelowThreshold() // satisfying threshold
// {
// // if(hasCandidateTimelinesBelowThreshold==false)
// // {
// // System.err.println("Error: Sanity Check RM60 failed: trying to get number of candidate timelines below threshold while there is no candidate below threshold, u shouldnt
// // have called this function");
// // }
// return editDistancesSortedMap.size();
// }
//
// public double getThresholdAsDistance()
// {
// return thresholdAsDistance;
// }
//
// public boolean isNextActivityJustAfterRecommPointIsInvalid()
// {
// return this.nextActivityJustAfterRecommPointIsInvalid;
// }
//
// public boolean hasThresholdPruningNoEffect()
// {
// return thresholdPruningNoEffect;
// }
//
// public boolean hasCandidateTimelinesBelowThreshold()
// {
// return hasCandidateTimelinesBelowThreshold;
// }
//
// public LinkedHashMap<Date, Triple<Integer, String, Double>> getDistanceScoresSorted()
// {
// return this.editDistancesSortedMap;
// }
//
// public ActivityObject getActivityAtRecomm()
// {
// return this.activityAtRecommPoint;
// }
//
// public String getActivitiesGuidingRecomm()
// {
// String res = "";
//
// for (ActivityObject ae : activitiesGuidingRecomm)
// {
// res += ">>" + ae.getActivityName();
// }
// return res;
// }
//
// public String createRankedTopRecommendation(String topRecommendationsWithDistance)
// {
// String topRankedString = new String();
// String[] splitted1 = topRecommendationsWithDistance.split("__");
//
// int numberOfRecommendations = splitted1.length - 1;
//
// System.out.println("Debug inside createRankedTopRecommendation: topRecommendationsWithDistance=" + topRecommendationsWithDistance);
// System.out.println(
// "Debug inside createRankedTopRecommendation: numberOfRecommendations=splitted1.length-1=" + numberOfRecommendations);
// System.out.print("Debug inside createRankedTopRecommendation: the read recommendation are: ");
//
// LinkedHashMap<String, Double> recommendationRankscorePairs = new LinkedHashMap<String, Double>();
// Double maxDistanceScore = 0d;
//
// for (int i = 1; i < splitted1.length; i++)
// {
// String[] recommendationSplitted = splitted1[i].split(Pattern.quote(":"));
//
// Double distanceScore = Double.valueOf(recommendationSplitted[1]);
//
// if (distanceScore > maxDistanceScore)
// {
// maxDistanceScore = distanceScore;
// }
// }
//
// for (int i = 1; i < splitted1.length; i++)
// {
// String[] recommendationSplitted = splitted1[i].split(Pattern.quote(":"));
// String recommActivityname = recommendationSplitted[0];
// Double distanceScore = Double.valueOf(recommendationSplitted[1]);
// System.out.print(recommActivityname + " ");
// Double rankScore = 1d - (distanceScore / maxDistanceScore);
//
// if (recommendationRankscorePairs.containsKey(recommActivityname) == false)
// {
// recommendationRankscorePairs.put(recommActivityname, rankScore);
// }
//
// else
// {
// recommendationRankscorePairs.put(recommActivityname, recommendationRankscorePairs.get(recommActivityname) + rankScore);
// }
// }
//
// System.out.println();
// recommendationRankscorePairs = (LinkedHashMap<String, Double>) UtilityBelt.sortByValue(recommendationRankscorePairs); // Sorted in descending order of ranked score: higher
// // ranked score
// // means
// // higher rank
//
// this.recommendationRankscorePairs = recommendationRankscorePairs;
//
// // for (Map.Entry<String, Double> entry : recommendationRankscorePairs.entrySet())
// // {
// // topRankedString+= "__"+entry.getKey()+":"+TestStats.round(entry.getValue(),4);
// // }
// //
// // this.rankedRecommendationWithRankScores =topRankedString;
//
// // String rankedRecommendationWithoutRankScores=new String();
// // for (Map.Entry<String, Double> entry : recommendationRankscorePairs.entrySet())
// // {
// // rankedRecommendationWithoutRankScores+= "__"+entry.getKey();
// // }
// //
// // this.rankedRecommendationWithoutRankScores = rankedRecommendationWithoutRankScores;
// //
// topRankedString = getRankedRecommendationWithRankScores();
// System.out.println("Debug inside createRankedTopRecommendation: topRankedString= " + topRankedString);
// System.out.println(
// "Debug inside createRankedTopRecommendation: topRankedStringWithoutScore= " + getRankedRecommendationWithoutRankScores());
//
// return topRankedString;
//
// }
//
// public void removeRecommPointActivityFromRankedRecomm()
// {
// String activityNameAtRecommPoint = activityAtRecommPoint.getActivityName();
//
// this.recommendationRankscorePairs.remove(activityNameAtRecommPoint);
// }
//
// public String getRankedRecommendationWithRankScores()
// {
// String topRankedString = new String();
//
// for (Map.Entry<String, Double> entry : recommendationRankscorePairs.entrySet())
// {
// topRankedString += "__" + entry.getKey() + ":" + Evaluation.round(entry.getValue(), 4);
// }
//
// this.rankedRecommendationWithRankScores = topRankedString;
//
// return rankedRecommendationWithRankScores;
// }
//
// public String getRankedRecommendationWithoutRankScores()
// {
// String rankedRecommendationWithoutRankScores = new String();
// for (Map.Entry<String, Double> entry : recommendationRankscorePairs.entrySet())
// {
// rankedRecommendationWithoutRankScores += "__" + entry.getKey();
// }
//
// this.rankedRecommendationWithoutRankScores = rankedRecommendationWithoutRankScores;
//
// return rankedRecommendationWithoutRankScores;
// }
//
// public LinkedHashMap<String, Double> getRecommendationRankscorePairs()
// {
// return this.recommendationRankscorePairs;
// }
//
// public boolean hasCandidateTimeslines()
// {
// return hasCandidateTimelines;
// }
//
// /*
// * public String getSingleNextRecommendedActivity() { return this.singleNextRecommendedActivity; }
// */
//
// public String getTopFiveRecommendedActivities()
// {
// return this.topRecommendedActivities;
// }
//
// public String getTopRecommendedActivitiesWithoutDistance()
// {
// String result = "";
// String topActivities = this.topRecommendedActivities;
// String[] splitted1 = topActivities.split("__");
//
// for (int i = 1; i < splitted1.length; i++)
// {
// String[] splitted2 = splitted1[i].split(":");
// result = result + "__" + splitted2[0];
// }
//
// return result;
// }
//
// // //
//
// // public int getEndPointIndexOfSubsequenceAsCodeWithHighestSimilarity(String activitySequenceAsCode, String activitiesGuidingAsStringCode)
//
// // getTopFivWithDistanceeRecommendedActivities getTopNextRecommendedActivityNamesWithDistanceEditDistance
//
// // IMPORTANT : returns all posssibilities , not just top 5 but top as much as possible
// public String getTopNextRecommendedActivityNameEditDistance(ArrayList<ActivityObject> activitiesGuidingRecomm,
// LinkedHashMap<Date, Triple<Integer, String, Double>> distanceScoresSorted,
// LinkedHashMap<Date, UserDayTimeline> dayTimelinesForUser)
// {
// String topNames = new String();
// // LinkedHashMap<Date,String> topNextForThisCand= new LinkedHashMap<Date,String>();
// UserDayTimeline topSimilarUserDayTimeline = null;
// System.out.println("\n-----------------Inside get top next recomm activities");
// int topCount = 0;
//
// if (distanceScoresSorted.size() < 5)
// {
// System.err.println("\nWarning: the number of similar timelines =" + distanceScoresSorted.size() + " < " + 5);
// }
//
// for (Map.Entry<Date, Triple<Integer, String, Double>> distEntryOfCand : distanceScoresSorted.entrySet())
// {
// int endPointIndexOfCand = distEntryOfCand.getValue().getFirst();
// Double distanceOfSimilarTimeline = distEntryOfCand.getValue().getThird();
// Date dateOfSimilarTimeline = distEntryOfCand.getKey();
//
// topSimilarUserDayTimeline = TimelineUtils.getUserDayTimelineByDateFromMap(dayTimelinesForUser, dateOfSimilarTimeline);
//
// ActivityObject nextValidAO = topSimilarUserDayTimeline.getNextValidActivityAfterActivityAtThisPosition(endPointIndexOfCand);
//
// // topNextForThisCand.put(distEntryOfCand.getKey(), nextValidAO.getActivityName());
//
// topNames = topNames + "__" + nextValidAO.getActivityName() + ":" + distanceOfSimilarTimeline;
// topCount++;
//
// System.out.println("-----------------------------------------------");
// }
//
// System.out.println("-------exiting getTopNextRecommendedActivityNameEditDistance\n");
// return topNames;
// }
//
// public LinkedHashMap<Date, String> setTopNextRecommendedActivityNameEditDistanceArray(ArrayList<ActivityObject> activitiesGuidingRecomm,
// LinkedHashMap<Date, Triple<Integer, String, Double>> distanceScoresSorted,
// LinkedHashMap<Date, UserDayTimeline> dayTimelinesForUser)
// {
// this.topNames = new LinkedHashMap<Date, String>();
// // LinkedHashMap<Date,String> topNextForThisCand= new LinkedHashMap<Date,String>();
// UserDayTimeline topSimilarUserDayTimeline = null;
// System.out.println("\n-----------------Inside getTopNextRecommendedActivityNameEditDistanceArray");
// int topCount = 0;
//
// if (distanceScoresSorted.size() < 5)
// {
// System.err.println("\nWarning: the number of similar timelines =" + distanceScoresSorted.size() + " < " + 5);
// }
//
// for (Map.Entry<Date, Triple<Integer, String, Double>> distEntryOfCand : distanceScoresSorted.entrySet())
// {
// int endPointIndexOfCand = distEntryOfCand.getValue().getFirst();
// Double distanceOfSimilarTimeline = distEntryOfCand.getValue().getThird();
// Date dateOfSimilarTimeline = distEntryOfCand.getKey();
//
// topSimilarUserDayTimeline = TimelineUtils.getUserDayTimelineByDateFromMap(dayTimelinesForUser, dateOfSimilarTimeline);
//
// ActivityObject nextValidAO = topSimilarUserDayTimeline.getNextValidActivityAfterActivityAtThisPosition(endPointIndexOfCand);
//
// // topNextForThisCand.put(distEntryOfCand.getKey(), nextValidAO.getActivityName());
//
// topNames.put(distEntryOfCand.getKey(), nextValidAO.getActivityName() + ":" + distanceOfSimilarTimeline);
// // topNames =topNames+ "__" +nextValidAO.getActivityName()+":"+distanceOfSimilarTimeline;
// topCount++;
//
// // System.out.println("-----------------------------------------------");
// }
//
// System.out.println("-------exiting getTopNextRecommendedActivityNameEditDistanceArray\n");
// return topNames;
// }
//
// public static String getNextValidActivityNameAsCode(String topSimilarUserDayActivitiesAsStringCode,
// int endPointIndexForSubsequenceWithHighestSimilarity)
// {
// String unknownAsCode = StringCode.getStringCodeFromActivityName("Unknown");
// String othersAsCode = StringCode.getStringCodeFromActivityName("Others");
// String nextValidActivity = null;
// for (int i = endPointIndexForSubsequenceWithHighestSimilarity + 1; i < topSimilarUserDayActivitiesAsStringCode.length(); i++)
// {
// if (String.valueOf(topSimilarUserDayActivitiesAsStringCode.charAt(i)).equals(unknownAsCode)
// || String.valueOf(topSimilarUserDayActivitiesAsStringCode.charAt(i)).equals(othersAsCode))
// {
// continue;
// }
// else
// {
// nextValidActivity = String.valueOf(topSimilarUserDayActivitiesAsStringCode.charAt(i));
// break;// Added on 27th October.... ALERT
// }
// }
// return nextValidActivity;
// }
//
// public LinkedHashMap<Date, Triple<Integer, String, Double>> getEditDistancesForCandidateTimelines(
// LinkedHashMap<Date, UserDayTimeline> candidateTimelines, ArrayList<ActivityObject> activitiesGuidingRecomm)
// {
// // <Date of CandidateTimeline, Distance score for that timeline>
// LinkedHashMap<Date, Triple<Integer, String, Double>> distanceScores = new LinkedHashMap<Date, Triple<Integer, String, Double>>();
//
// for (Map.Entry<Date, UserDayTimeline> entry : candidateTimelines.entrySet())
// {
// // similarityScores.put(entry.getKey(), getSimilarityScore(entry.getValue(),activitiesGuidingRecomm));
// // (Activity Events in Candidate Day, activity events on or before recomm on recomm day)
// Triple score = getDistanceScoreModifiedEdit(entry.getValue(), activitiesGuidingRecomm);
//
// distanceScores.put(entry.getKey(), score);
// // System.out.println("now we put "+entry.getKey()+" and score="+score);
// }
//
// return distanceScores;
// }
//
// /**
// * Get distance scores using modified edit distance.
// *
// * <end point index, edit operations trace, edit distance The distance score is the distance between the activities guiding recommendation and the least distant subcandidate
// * (which has a valid activity after it) from the candidate timeline. (subcandidate is a subsequence from candidate timeline, from the start of the candidate timeline to any
// * occurrence of the ActivityGuiding Recomm or current activity.
// *
// * @param userDayTimeline
// * @param activitiesGuidingRecomm
// * @return
// */
// public Triple<Integer, String, Double> getDistanceScoreModifiedEdit(UserDayTimeline userDayTimeline,
// ArrayList<ActivityObject> activitiesGuidingRecomm)
// {
// System.out.println("Inside getDistanceScoreModifiedEdit");
// // find the end points in the userDayTimeline
// // ///////
// String activityAtRecommPointAsStringCode =
// String.valueof(activitiesGuidingRecomm.get(activitiesGuidingRecomm.size() - 1).getStringCode());
// String activitiesGuidingAsStringCode = ActivityObject.getStringCodeForActivityObjects(activitiesGuidingRecomm);
// String userDayTimelineAsStringCode = userDayTimeline.getActivityObjectsAsStringCode();
//
// // IMPORTANAT CHANGE on 24 OCt
// // old
// ArrayList<Integer> indicesOfEndPointActivityInDay1 =
// getIndicesOfEndPointActivityInDayButNotAsLast(userDayTimelineAsStringCode, activityAtRecommPointAsStringCode);
//
// // new
// ArrayList<Integer> indicesOfEndPointActivityInDay =
// getIndicesOfEndPointActivityInDayButNotLastValid(userDayTimelineAsStringCode, activityAtRecommPointAsStringCode);
//
// // ///
// // $$WritingToFile.writeEndPoinIndexCheck24Oct(activityAtRecommPointAsStringCode,userDayTimelineAsStringCode,indicesOfEndPointActivityInDay1,indicesOfEndPointActivityInDay);
//
// // $$ System.out.println("Debug oct 24 1pm: indicesOfEndPointActivityInDay1"+indicesOfEndPointActivityInDay1);
// // $$System.out.println("Debug oct 24 1pm: indicesOfEndPointActivityInDay"+indicesOfEndPointActivityInDay);
// // $$System.out.println("same="+indicesOfEndPointActivityInDay1.toString().equals(indicesOfEndPointActivityInDay.toString()));
// // //////
//
// /** index of end point, edit operations trace, edit distance **/
// LinkedHashMap<Integer, Pair<String, Double>> distanceScoresForEachSubsequence = new LinkedHashMap<Integer, Pair<String, Double>>();
//
// // getting distance scores for each subcandidate
// for (int i = 0; i < indicesOfEndPointActivityInDay1.size(); i++)
// {
// HJEditDistance editSimilarity = new HJEditDistance();
//
// Pair<String, Double> distance = editSimilarity.getHJEditDistanceWithTrace(
// userDayTimeline.getActivityObjectsInDayFromToIndex(0, indicesOfEndPointActivityInDay1.get(i) + 1),
// activitiesGuidingRecomm);
// /*
// * public final Pair<String, Double> getHJEditDistanceWithTrace(ArrayList<ActivityObject> activityObjects1Original, ArrayList<ActivityObject> activityObjects2Original,
// * String userAtRecomm, String dateAtRecomm, String timeAtRecomm, Integer candidateTimelineId)
// */
// distanceScoresForEachSubsequence.put(indicesOfEndPointActivityInDay1.get(i), distance);
//
// // System.out.println("Distance between:\n activitiesGuidingRecomm:"+UtilityBelt.getActivityNamesFromArrayList(activitiesGuidingRecomm)+
// // "\n and subsequence of
// // Cand:"+UtilityBelt.getActivityNamesFromArrayList(userDayTimeline.getActivityObjectsInDayFromToIndex(0,indicesOfEndPointActivityInDay1.get(i)+1)));
//
// }
// // ///////////////////
//
// distanceScoresForEachSubsequence =
// (LinkedHashMap<Integer, Pair<String, Double>>) UtilityBelt.sortByValueAscendingIntStrDoub(distanceScoresForEachSubsequence);
// // $$WritingToFile.writeEditDistancesOfAllEndPoints(activitiesGuidingRecomm,userDayTimeline,distanceScoresForEachSubsequence);
//
// for (Map.Entry<Integer, Pair<String, Double>> entry1 : distanceScoresForEachSubsequence.entrySet()) // we only consider the most similar timeline . i.e. the first entry in
// // this Map
// {
// System.out.println("End point= " + entry1.getKey() + " distance=" + entry1.getValue().getSecond());// +" trace="+distance.getFirst());
// }
//
// if (distanceScoresForEachSubsequence.size() == 0)
// {
// System.err.println("Error in getDistanceScoreModifiedEdit: no subsequence to be considered for similarity");
// }
//
// int endPointIndexForSubsequenceWithHighestSimilarity = -1;
// double distanceScoreForSubsequenceWithHighestSimilarity = -9999;
// String traceEditOperationsForSubsequenceWithHighestSimilarity = "";
//
// // finding the end point index with highest similarity WHICH HAS A VALID ACTIVITY AFTER IT
// for (Map.Entry<Integer, Pair<String, Double>> entry1 : distanceScoresForEachSubsequence.entrySet()) // we only consider the most similar timeline . i.e. the first entry in
// // this Map
// {
// endPointIndexForSubsequenceWithHighestSimilarity = entry1.getKey().intValue();
//
// if (getNextValidActivityNameAsCode(userDayTimelineAsStringCode,
// endPointIndexForSubsequenceWithHighestSimilarity) == null)/*
// * there DOES NOT exists a valid activity after this end point
// */
// {
// continue;
// }
//
// else
// {
// endPointIndexForSubsequenceWithHighestSimilarity = entry1.getKey().intValue();
// traceEditOperationsForSubsequenceWithHighestSimilarity = entry1.getValue().getFirst();
// distanceScoreForSubsequenceWithHighestSimilarity = entry1.getValue().getSecond().doubleValue();
// break;
// }
// }
//
// // to check: remove later
// System.out.println("end points indices not last:" + indicesOfEndPointActivityInDay1.toString());
// System.out.println("end point indices not last valid:" + indicesOfEndPointActivityInDay.toString());
//
// indicesOfEndPointActivityInDay1.removeAll(indicesOfEndPointActivityInDay);
// System.out.println("Contained in not last but no valid after= " + indicesOfEndPointActivityInDay1);
//
// if (indicesOfEndPointActivityInDay1.contains(endPointIndexForSubsequenceWithHighestSimilarity))
// {
// System.out.println("ajooba: the end point index with highest similarity has not valid activity after it in day");
// }
// // /////
//
// System.out.println("---Debug: getDistanceScoreModifiedEdit---- ");
// System.out.println("activitiesGuidingAsStringCode=" + activitiesGuidingAsStringCode);
// System.out.println("activityAtRecommPointAsStringCode=" + activityAtRecommPointAsStringCode);
// System.out.println("userDayTimelineAsStringCode=" + userDayTimelineAsStringCode);
//
// System.out.println("endPointIndexForSubsequenceWithHighestSimilarity=" + endPointIndexForSubsequenceWithHighestSimilarity);
// System.out.println("distanceScoreForSubsequenceWithHighestSimilarity=" + distanceScoreForSubsequenceWithHighestSimilarity);
//
// distanceScoreForSubsequenceWithHighestSimilarity = UtilityBelt.round(distanceScoreForSubsequenceWithHighestSimilarity, 4);
//
// return new Triple(endPointIndexForSubsequenceWithHighestSimilarity, traceEditOperationsForSubsequenceWithHighestSimilarity,
// distanceScoreForSubsequenceWithHighestSimilarity);
// }
//
// /**
// * Find Candidate timelines, which are the timelines which contain the activity at the recommendation point (current Activity). Also, this candidate timeline must contain the
// * activityAtRecomm point at non-last position and there is atleast one valid activity after this activityAtRecomm point
// *
// * @param dayTimelinesForUser
// * @param activitiesGuidingRecomm
// * @return
// */
// public LinkedHashMap<Date, UserDayTimeline> getCandidateTimelines(LinkedHashMap<Date, UserDayTimeline> dayTimelinesForUser,
// ArrayList<ActivityObject> activitiesGuidingRecomm, Date dateAtRecomm)
// {
// LinkedHashMap<Date, UserDayTimeline> candidateTimelines = new LinkedHashMap<Date, UserDayTimeline>();
// int count = 0;
//
// totalNumberOfProbableCands = 0;
// numCandsRejectedDueToNoCurrentActivityAtNonLast = 0;
// numCandsRejectedDueToNoNextActivity = 0;
//
// for (Map.Entry<Date, UserDayTimeline> entry : dayTimelinesForUser.entrySet())
// {
// totalNumberOfProbableCands += 1;
//
// // Check if the timeline contains the activityAtRecomm point at non-last and the timeline is not same for the day to be recommended (this should nt be the case because
// // test and
// // trainin set
// // are diffferent)
// // and there is atleast one valid activity after this activityAtRecomm point
// if (entry.getValue().countContainsActivityButNotAsLast(this.activityAtRecommPoint) > 0)// && (entry.getKey().toString().equals(dateAtRecomm.toString())==false))
// {
// if (entry.getKey().toString().equals(dateAtRecomm.toString()) == true)
// {
// System.err.println(
// "Error: a prospective candidate timelines is of the same date as the dateToRecommend. Thus, not using training and test set correctly");
// continue;
// }
//
// if (entry.getValue().containsOnlySingleActivity() == false
// && entry.getValue().hasAValidActivityAfterFirstOccurrenceOfThisActivity(this.activityAtRecommPoint) == true)
// {
// candidateTimelines.put(entry.getKey(), entry.getValue());
// count++;
// }
// else
// numCandsRejectedDueToNoNextActivity += 1;
// }
// else
// numCandsRejectedDueToNoCurrentActivityAtNonLast += 1;
// }
// if (count == 0)
// System.err.println("No candidate timelines found");
// return candidateTimelines;
// }
//
// public ArrayList<Integer> getIndicesOfEndPointActivityInDayButNotAsLast(String userDayActivitiesAsStringCode,
// String codeOfEndPointActivity)
// {
// // System.out.println("\nDebug getIndicesOfEndPointActivityInDayButNotAsLast: userDayActivitiesAsStringCode="+userDayActivitiesAsStringCode+" and
// // codeOfEndPointActivity="+codeOfEndPointActivity);
// ArrayList<Integer> indicesOfEndPointActivityInDay = new ArrayList<Integer>();
//
// int index = userDayActivitiesAsStringCode.indexOf(codeOfEndPointActivity);
//
// while (index >= 0)
// {
// // System.out.println(index);
// if (index != (userDayActivitiesAsStringCode.length() - 1)) // not last index
// {
// indicesOfEndPointActivityInDay.add(index);
// }
// index = userDayActivitiesAsStringCode.indexOf(codeOfEndPointActivity, index + 1);
// }
// return indicesOfEndPointActivityInDay;
// }
//
// /**
// * Indices of current activity occurrence with atleast one valid activity after it.
// *
// * @param userDayActivitiesAsStringCode
// * @param codeOfEndPointActivity
// * @return
// */
// public ArrayList<Integer> getIndicesOfEndPointActivityInDayButNotLastValid(String userDayActivitiesAsStringCode,
// String codeOfEndPointActivity)
// {
// System.out.println("\nDebug getIndicesOfEndPointActivityInDayButNotLastValid: userDayActivitiesAsStringCode="
// + userDayActivitiesAsStringCode + " and codeOfEndPointActivity=" + codeOfEndPointActivity);
// // getCharCodeFromActivityID
// // String codeUn = StringCode.getStringCodeFromActivityName(Constant.INVALID_ACTIVITY1);// "Unknown");
// String codeUn = StringCode.getStringCodeFromActivityName(Constant.INVALID_ACTIVITY1);// "Unknown");
//
// // String codeO = StringCode.getStringCodeFromActivityName(Constant.INVALID_ACTIVITY2);// ("Others");
// String codeO = StringCode.getStringCodeFromActivityName(Constant.INVALID_ACTIVITY2);// ("Others");
//
// // get indices of valid activity ActivityNames
// ArrayList<Integer> indicesOfValids = new ArrayList<Integer>();
//
// for (int i = 0; i < userDayActivitiesAsStringCode.length(); i++)
// {
// String codeToCheck = userDayActivitiesAsStringCode.substring(i, i + 1); // only one character
//
// if (codeToCheck.endsWith(codeUn) || codeToCheck.equals(codeO))
// {
// continue;
// }
// else
// indicesOfValids.add(i);
// }
//
// ArrayList<Integer> endPoints = new ArrayList<Integer>();
// for (int i = 0; i < indicesOfValids.size() - 1; i++) // skip the last valid because there is no valid activity to recommend after that.
// {
// int indexOfValid = indicesOfValids.get(i);
//
// if (userDayActivitiesAsStringCode.substring(indexOfValid, indexOfValid + 1).equals(codeOfEndPointActivity))
// {
// endPoints.add(indexOfValid);
// }
// }
//
// System.out.println("indices of valids=" + indicesOfValids);
// System.out.println("end points considered" + endPoints);
//
// return endPoints;
// }
//
// public LinkedHashMap<Date, UserDayTimeline> getCandidateTimeslines()
// {
// return this.candidateTimelines;
// }
//
// public void traverseMapOfDayTimelines(LinkedHashMap<Date, UserDayTimeline> map)
// {
// System.out.println("traversing map of day timelines");
// for (Map.Entry<Date, UserDayTimeline> entry : map.entrySet())
// {
// System.out.print("Date: " + entry.getKey());
// entry.getValue().printActivityObjectNamesInSequence();
// System.out.println();
// }
// System.out.println("-----------");
// }
// }
