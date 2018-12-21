package org.activity.evaluation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.activity.constants.Constant;
import org.activity.constants.DomainConstants;
import org.activity.constants.Enums;
import org.activity.constants.Enums.PrimaryDimension;
import org.activity.constants.VerbosityConstants;
import org.activity.io.WToFile;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.recomm.RecommendationMasterI;
import org.activity.recomm.RecommendationMasterMultiDI;
import org.activity.stats.StatsUtils;
import org.activity.util.StringUtils;
import org.activity.util.TimelineTransformers;

/**
 * To keep common methods to different RecommendationTests classes together
 * <p>
 * Moving in method from different RecommendationTests
 * 
 * @author gunjan
 * @since 20 Dec 2018
 */
public class RecommendationTestsUtils
{

	// public static ActivityObject2018 computeRepresentativeAOsDec2018(Integer topPrimaryDimensionVal, int userId,
	// Timestamp recommendationTimestamp, PrimaryDimension primaryDimension,
	// LinkedHashMap<String, Timeline> trainTimelinesAllUsersContinuousFiltrd, boolean isCollaborative)
	/**
	 * 
	 * @param topPrimaryDimensionVal
	 * @param userId
	 * @param recommendationTimestamp
	 * @param primaryDimension
	 * @param trainTimelinesAllUsersContinuousFiltrd
	 * @return
	 * @since 20 Dec 2018
	 */

	/**
	 * 
	 * @param userId
	 * @param databaseName
	 * @return
	 */
	static boolean isBlackListedUser(int userId)
	{
		boolean isBlackListed = false;
		if (Constant.getDatabaseName().equals("gowalla1"))
		{
			if (Arrays.asList(DomainConstants.gowallaUserIDsWithGT553MaxActsPerDay).contains(userId))
			{
				System.out.println("Skipping user: " + userId + " as in gowallaUserIDsWithGT553MaxActsPerDay");
				// PopUps.showMessage("Skipping user: " + userId + " as in gowallaUserIDsWithGT553MaxActsPerDay");
				isBlackListed = true;
			}
		}
		else
		{
			System.err.println("Warning: Constant.blacklistingUsersWithLargeMaxActsPerDay= "
					+ Constant.blacklistingUsersWithLargeMaxActsPerDay
					+ " but blacklisted user not defined for this database, hence returning isBlacklister = false");
		}
		return isBlackListed;
	}

	/**
	 * Closes multiple BufferedReaders
	 * 
	 * @param br1
	 * @param brs
	 * @throws IOException
	 */
	public static void closeBWs(BufferedWriter br1, BufferedWriter... brs) throws IOException
	{
		br1.close();
		for (BufferedWriter br : brs)
		{
			br.close();
		}
	}

	/**
	 * 
	 * @param dateToRecomm
	 * @param weekDay
	 * @param endTimeStamp
	 * @param recommP1
	 * @param actActualDone
	 * @param activityAtRecommPoint
	 * @return
	 * 
	 * @deprecated Not writing this anymore as similar information is written by:
	 *             org.activity.io.WritingToFile.writeEditDistancesPerRtPerCand()
	 */
	private static String getRTsWithDistancesToWrite(String dateToRecomm, String weekDay, Timestamp endTimeStamp,
			RecommendationMasterI recommP1, String actActualDone, ActivityObject2018 activityAtRecommPoint)
	{
		StringBuilder rtsWithEditDistancesMsg = new StringBuilder();

		for (Map.Entry<String, Pair<String, Double>> entryDistance : recommP1.getDistancesSortedMap().entrySet())
		{
			String candidateTimelineID = entryDistance.getKey();

			Timeline candidateTimeline = recommP1.getCandidateTimeline(candidateTimelineID);
			// recommP1.getCandidateTimeslines() .get(candidateTimelineID);

			int endPointIndexThisCandidate = candidateTimeline.getActivityObjectsInTimeline().size() - 1;
			ActivityObject2018 endPointActivityInCandidate = candidateTimeline.getActivityObjectsInTimeline()
					.get(endPointIndexThisCandidate);

			// difference in start time of end point activity of candidate and start
			// time of current activity
			long diffStartTimeForEndPointsCand_n_GuidingInSecs = (activityAtRecommPoint.getStartTimestamp().getTime()
					- endPointActivityInCandidate.getStartTimestamp().getTime()) / 1000;
			// difference in end time of end point activity of candidate and end time of
			// current activity
			long diffEndTimeForEndPointsCand_n_GuidingInSecs = (activityAtRecommPoint.getEndTimestamp().getTime()
					- endPointActivityInCandidate.getEndTimestamp().getTime()) / 1000;

			// ("DateOfRecomm"+",TimeOfRecomm,"+"TargetActivity,EditDistanceOfCandidateTimeline,Diff_Start_Time,Diff_End_Time,CandidateTimeline,WeekDayOfRecomm");
			// bwRecommTimesWithEditDistances.write
			rtsWithEditDistancesMsg.append(dateToRecomm + "," + endTimeStamp.getHours() + ":"
					+ endTimeStamp.getMinutes() + ":" + endTimeStamp.getSeconds() + "," + candidateTimelineID + ","
					+ actActualDone + "," + entryDistance.getValue().getSecond() + ","
					// +dateOfCand+","
					+ diffStartTimeForEndPointsCand_n_GuidingInSecs + "," + diffEndTimeForEndPointsCand_n_GuidingInSecs
					+ "," + endPointIndexThisCandidate + "," + candidateTimeline.getActivityObjectNamesInSequence()
					+ "," + weekDay + "\n");
			// UtilityBelt.getWeekDayFromWeekDayInt(entry.getKey().getDay())
			// +"," +UtilityBelt.getWeekDayFromWeekDayInt(entryScore.getKey().getDay())
			// bwRecommTimesWithEditDistances.newLine();
		}

		return rtsWithEditDistancesMsg.toString();// +"\n"
	}

	/**
	 * Computes the median of the seconds since midnight for the given timestamps and returns a Timestamp with dummy
	 * year, month, date while hours, mins, seconds extracted from the median.
	 * 
	 * @param tss
	 * @param userID
	 * @param actName
	 *            just used for naming the filename to write result to
	 * @return
	 */
	static Timestamp getMedianSecondsSinceMidnightTimestamp(List<Timestamp> tss, int userID, String actName)
	{
		Timestamp ts = null;
		try
		{
			double secs = getMedianSeconds(tss, userID, actName);

			int hours = (int) secs / 3600;
			secs = secs - (hours * 3600);
			int minutes = (int) secs / 60;
			secs = secs - (minutes * 60);
			ts = new Timestamp(1900, 0, 1, hours, minutes, (int) secs, 0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return ts;
	}

	public static String getActivityNameCountPairsWithCount(LinkedHashMap<String, Long> nameCountPairsSorted)
	{
		// String result = "";
		StringBuilder result = new StringBuilder();

		for (Map.Entry<String, Long> entry : nameCountPairsSorted.entrySet())
		{
			result.append("__").append(entry.getKey()).append(":").append(entry.getValue());
			// result += "__" + entry.getKey() + ":" + entry.getValue();
		}
		return result.toString();
	}

	public static String getActivityNameCountPairsWithoutCount(LinkedHashMap<String, Long> nameCountPairsSorted)
	{
		// String result = "";
		StringBuilder result = new StringBuilder();

		for (Map.Entry<String, Long> entry : nameCountPairsSorted.entrySet())
		{
			result.append("__").append(entry.getKey());
			// result += "__" + entry.getKey();
		}
		return result.toString();
	}

	public static String getActivityNameDurationPairsWithDuration(LinkedHashMap<String, Long> nameDurationPairsSorted)
	{
		// String result = "";
		StringBuilder result = new StringBuilder();
		for (Map.Entry<String, Long> entry : nameDurationPairsSorted.entrySet())
		{
			result.append("__").append(entry.getKey()).append(":").append(entry.getValue());
			// result += "__" + entry.getKey() + ":" + entry.getValue();
		}
		return result.toString();
	}

	public static String getActivityNameDurationPairsWithoutDuration(
			LinkedHashMap<String, Long> nameDurationPairsSorted)
	{
		// String result = "";
		StringBuilder result = new StringBuilder();
		for (Map.Entry<String, Long> entry : nameDurationPairsSorted.entrySet())
		{
			result.append("__").append(entry.getKey());
			// result += "__" + entry.getKey();
		}
		return result.toString();
	}

	/**
	 * Take only recent numOfRecentDays days for each user
	 * 
	 * 
	 * @param trainTestTimelinesForAllUsersDW
	 * @param numOfRecentDays
	 * @return
	 */
	private final static LinkedHashMap<String, Timeline> getContinousTrainingTimelinesWithFilterByRecentDays(
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersDW,
			int numOfRecentDays)
	{
		LinkedHashMap<String, Timeline> trainTimelineForAllUsers = new LinkedHashMap<>();

		StringBuilder sb = new StringBuilder("Debug 10 Dec\t");

		for (Entry<String, List<LinkedHashMap<Date, Timeline>>> trainTestForAUser : trainTestTimelinesForAllUsersDW
				.entrySet())
		{
			LinkedHashMap<Date, Timeline> trainingTimelineForThisUserDate = trainTestForAUser.getValue().get(0);

			TreeMap<Date, Timeline> trainingTimelineForThisUserDateDescByDate = new TreeMap<Date, Timeline>(
					Collections.reverseOrder());

			trainingTimelineForThisUserDateDescByDate.putAll(trainingTimelineForThisUserDate);

			LinkedHashMap<Date, Timeline> filteredDayTrainingTimelineForThisUser = trainingTimelineForThisUserDateDescByDate
					.entrySet().stream().limit(numOfRecentDays)
					.collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);

			Timeline filteredTrainingTimelineForThisUser = TimelineTransformers
					.dayTimelinesToATimeline(filteredDayTrainingTimelineForThisUser, false, true);
			// convert datetime to continouse timeline
			trainTimelineForAllUsers.put(trainTestForAUser.getKey(), filteredTrainingTimelineForThisUser);

			// start of debug print
			if (VerbosityConstants.WriteFilterTrainTimelinesByRecentDays)
			{
				sb.append("\n-->User =" + trainTestForAUser.getKey() + "\n. original day timelines:");
				trainingTimelineForThisUserDate.entrySet().stream().forEachOrdered(e -> sb.append(
						"\n" + e.getKey() + "--" + e.getValue().getActivityObjectNamesWithTimestampsInSequence()));
				sb.append("\n-->User =" + trainTestForAUser.getKey() + "\n. sorted day timelines:");
				trainingTimelineForThisUserDateDescByDate.entrySet().stream().forEachOrdered(e -> sb.append(
						"\n" + e.getKey() + "--" + e.getValue().getActivityObjectNamesWithTimestampsInSequence()));
				sb.append("\\n-->User =" + trainTestForAUser.getKey() + "\n. filtered day timelines:");
				sb.append("\n" + filteredTrainingTimelineForThisUser.getActivityObjectNamesWithTimestampsInSequence()
						+ "\n--\n");
			}
			// end of debug print

		}
		WToFile.appendLineToFileAbs(sb.toString() + "\n--\n", Constant.getCommonPath() + "FilteringOfTrainingDays.csv");

		return trainTimelineForAllUsers;
	}

	/**
	 * Analyse activity names which are not in training timelines for each user.
	 * 
	 * @param userId
	 * @param allPossibleActivityNames
	 * @param distinctActNamesEncounteredInTraining
	 * @param fileNameToWrite
	 * @param userTestTimelines
	 */
	private static void analyseActNameNotInTraining(int userId, String[] allPossibleActivityNames,
			LinkedHashSet<String> distinctActNamesEncounteredInTraining, String fileNameToWrite,
			String fileNameToWrite2, Timeline userTestTimelines)
	{
		System.out.println("#Distinct act names encountered in training for this user = "
				+ distinctActNamesEncounteredInTraining.size());
		System.out.println("#All possible act names = " + allPossibleActivityNames.length);

		int numOfActNamesNotInTraining = (allPossibleActivityNames.length
				- distinctActNamesEncounteredInTraining.size());
		System.out.println("#act names not in training timelines for this user = " + numOfActNamesNotInTraining);
		System.out.println("Acts not in trainings: ");
		LinkedHashSet<String> actNamesNotInTraining = new LinkedHashSet<String>(
				Arrays.asList(allPossibleActivityNames));
		actNamesNotInTraining.removeAll(distinctActNamesEncounteredInTraining);

		String actNameNotInTrainingSB = actNamesNotInTraining.stream().collect(Collectors.joining("__"));
		WToFile.appendLineToFileAbs(
				"U:" + userId + "," + numOfActNamesNotInTraining + "," + actNameNotInTrainingSB.toString() + "\n",
				fileNameToWrite);

		////////////// Look at act names in Test set and see if they were in training timelines or not.
		LinkedHashSet<String> actNamesInTestSet = new LinkedHashSet<String>();
		userTestTimelines.getActivityObjectsInTimeline().stream()
				.forEach(ao -> actNamesInTestSet.add(ao.getActivityName()));

		LinkedHashSet<String> actNamesInTestButNotInTraining = new LinkedHashSet<String>(actNamesInTestSet);
		actNamesInTestButNotInTraining.removeAll(distinctActNamesEncounteredInTraining);
		System.out.println(
				"#act names in test but not in training  for this user = " + actNamesInTestButNotInTraining.size());

		String actNamesInTestButNotInTrainingS = actNamesInTestButNotInTraining.stream()
				.collect(Collectors.joining("__"));

		WToFile.appendLineToFileAbs("U:" + userId + "," + actNamesInTestButNotInTraining.size() + ","
				+ actNamesInTestButNotInTrainingS + "\n", fileNameToWrite2);

	}

	/**
	 * Take only recent numOfRecentDays days for each user
	 * 
	 * <p>
	 * Sanity checked
	 * 
	 * @param trainTestTimelinesForAllUsersDW
	 * @param numOfRecentDays
	 * @return map of {userID, filteredTrainingTimelineForThisUser}
	 */
	public final static LinkedHashMap<String, Timeline> getContinousTrainingTimelinesWithFilterByRecentDaysV2(
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersDW,
			int numOfRecentDays)
	{
		LinkedHashMap<String, Timeline> trainTimelineForAllUsers = new LinkedHashMap<>();
		System.out.println("----- Filtering by recent " + numOfRecentDays + " Days");
		StringBuilder sb = new StringBuilder("Debug 10 Dec\t");

		for (Entry<String, List<LinkedHashMap<Date, Timeline>>> trainTestForAUser : trainTestTimelinesForAllUsersDW
				.entrySet())
		{
			LinkedHashMap<Date, Timeline> trainingTimelineForThisUserDate = trainTestForAUser.getValue().get(0);

			// Get most recent numOfRecentDays dates
			List<Date> trainingDatesForThisUserList = new ArrayList<>();
			trainingDatesForThisUserList.addAll(trainingTimelineForThisUserDate.keySet());
			Collections.sort(trainingDatesForThisUserList, Collections.reverseOrder());

			Set<Date> setOfSelectedDatesForThisUser = trainingDatesForThisUserList.stream().limit(numOfRecentDays)
					.collect(Collectors.toSet());

			if (VerbosityConstants.verbose)// added on 29 June 2018 to reduce verbosity of output
			{
				System.out.println("UserID=" + trainTestForAUser.getKey() + " numOfTrainingDays="
						+ trainingDatesForThisUserList.size() + "\ttrainingDatesForThisUserList= \n"
						+ trainingDatesForThisUserList);
				System.out.println("numOfSelectedTrainingDays= " + setOfSelectedDatesForThisUser.size()
						+ "\t\tsetOfSelectedDatesForThisUser= \n" + setOfSelectedDatesForThisUser);
			}

			// filter by date in setOfSelectedDatesForThisUser
			LinkedHashMap<Date, Timeline> filteredDayTrainingTimelineForThisUser = trainingTimelineForThisUserDate
					.entrySet().stream().filter(e -> setOfSelectedDatesForThisUser.contains(e.getKey()))
					.collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);

			// convert datetime to continouse timeline
			Timeline filteredTrainingTimelineForThisUser = TimelineTransformers
					.dayTimelinesToATimeline(filteredDayTrainingTimelineForThisUser, false, true);

			trainTimelineForAllUsers.put(trainTestForAUser.getKey(), filteredTrainingTimelineForThisUser);

			// start of debug print
			if (VerbosityConstants.WriteFilterTrainTimelinesByRecentDays)
			{
				sb.append("\n-->User =" + trainTestForAUser.getKey() + "\n. original day timelines: size = "
						+ trainingTimelineForThisUserDate.size());
				trainingTimelineForThisUserDate.entrySet().stream().forEachOrdered(e -> sb.append(
						"\n" + e.getKey() + "--" + e.getValue().getActivityObjectNamesWithTimestampsInSequence()));
				sb.append("\n-->User =" + trainTestForAUser.getKey() + "\n. selected dates:");
				setOfSelectedDatesForThisUser.stream().forEachOrdered(e -> sb.append("\n" + e.toString() + "--"));
				sb.append("\n-->User =" + trainTestForAUser.getKey() + "\n. filtered day timelines: size= "
						+ filteredTrainingTimelineForThisUser.size());
				sb.append("\n" + filteredTrainingTimelineForThisUser.getActivityObjectNamesWithTimestampsInSequence()
						+ "\n--\n");
				// System.out.println(sb.toString() + "\n--\n");
			}
			// end of debug print
			WToFile.appendLineToFileAbs(sb.toString() + "\n--\n",
					Constant.getCommonPath() + "FilteringOfTrainingDays.csv");
		}

		return trainTimelineForAllUsers;
	}

	/**
	 * 
	 * @param trainTestTimelinesForAllUsersDW
	 * @return
	 */
	public final static LinkedHashMap<String, Timeline> getContinousTrainingTimelines(
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersDW)
	{
		LinkedHashMap<String, Timeline> trainTimelineForAllUsers = new LinkedHashMap<>();

		for (Entry<String, List<LinkedHashMap<Date, Timeline>>> trainTestForAUser : trainTestTimelinesForAllUsersDW
				.entrySet())
		{
			LinkedHashMap<Date, Timeline> trainingTimelineForThisUserDate = trainTestForAUser.getValue().get(0);

			Timeline trainingTimelineForThisUser = TimelineTransformers
					.dayTimelinesToATimeline(trainingTimelineForThisUserDate, false, true);
			// convert datetime to continouse timeline
			trainTimelineForAllUsers.put(trainTestForAUser.getKey(), trainingTimelineForThisUser);
		}

		return trainTimelineForAllUsers;
	}

	/**
	 * Write some raw data/logs to Raw.csv
	 * 
	 * @param userName
	 * @param dateToRecomm
	 * @param weekDay
	 * @param sbsRawToWriteForThisUserDate
	 * @param timeCategory
	 * @param recommMaster
	 * @param recommTimesString
	 * @param actsActualDoneInSeq
	 * @param rankedRecommWithScoreForThisRTIter
	 */
	static void writeRawLogs(String userName, String dateToRecomm, String weekDay,
			StringBuilder sbsRawToWriteForThisUserDate, String timeCategory, RecommendationMasterI recommMaster,
			String recommTimesString, String actActualDoneInSeq, String rankedRecommWithScoreForThisRTIter)
	{
		String currentActName = recommMaster.getActivityObjectAtRecomm().getActivityName();
		sbsRawToWriteForThisUserDate.append(userName + "," + dateToRecomm + "," + recommTimesString + "," + timeCategory
				+ "," + recommMaster.getActivityNamesGuidingRecomm()/* withTimestamps */
				+ "," + currentActName + "," + Integer.toString(recommMaster.getNumOfValidActsInActsGuidingRecomm())
				+ "," + Integer.toString(recommMaster.getNumOfActsInActsGuidingRecomm()) + ","
				+ Integer.toString(recommMaster.getNumOfCandTimelinesBelowThresh()) + "," + weekDay + ","
				+ actActualDoneInSeq + "," + rankedRecommWithScoreForThisRTIter + ","
				+ Integer.toString(recommMaster.getNumOfDistinctRecommendations()) + ",,,,,,"
				// recommMasters[2].getActivityNamesGuidingRecomm() ,
				// recommP1.getRestAndEndSimilaritiesCorrelation() , "," ,
				// recommP1.getAvgRestSimilarity() , "," ,
				// recommP1.getSDRestSimilarity() , "," ,
				// recommP1.getAvgEndSimilarity()
				// , "," , recommP1.getSDEndSimilarity()
				+ Boolean.toString((currentActName.equals(actActualDoneInSeq))) + "\n");// ,",",recommP1.getActivitiesGuidingRecomm());
	}

	/**
	 * Write some raw data/logs to Raw.csv
	 * 
	 * @param userName
	 * @param dateToRecomm
	 * @param weekDay
	 * @param sbsRawToWriteForThisUserDate
	 * @param timeCategory
	 * @param recommMaster
	 * @param recommTimesString
	 * @param actsActualDoneInSeq
	 * @param rankedRecommWithScoreForThisRTIter
	 * @since 18 July 2018
	 */
	static void writeRawLogsSecDim(String userName, String dateToRecomm, String weekDay,
			StringBuilder sbsRawToWriteForThisUserDate, String timeCategory, RecommendationMasterMultiDI recommMaster,
			String recommTimesString, String actActualDoneInSeq, String rankedRecommWithScoreForThisRTIter,
			PrimaryDimension secondaryDimension)
	{
		String currentActName = recommMaster.getActivityObjectAtRecomm().getGivenDimensionVal("|", secondaryDimension);

		sbsRawToWriteForThisUserDate.append(userName + "," + dateToRecomm + "," + recommTimesString + "," + timeCategory
				+ ","
				+ TimelineTransformers.getActivityGDGuidingRecomm(secondaryDimension,
						recommMaster.getActsGuidingRecomm()) /* withTimestamps */
				+ "," + currentActName + "," + Integer.toString(recommMaster.getNumOfValidActsInActsGuidingRecomm())
				+ "," + Integer.toString(recommMaster.getNumOfActsInActsGuidingRecomm()) + ","
				+ Integer.toString(recommMaster.getNumOfSecDimCandTimelines()) + "," + weekDay + ","
				+ actActualDoneInSeq + "," + rankedRecommWithScoreForThisRTIter + ","
				+ Integer.toString(recommMaster.getNumOfDistinctSecDimRecommendations()) + ",,,,,,"
				// recommMasters[2].getActivityNamesGuidingRecomm() ,
				// recommP1.getRestAndEndSimilaritiesCorrelation() , "," ,
				// recommP1.getAvgRestSimilarity() , "," ,
				// recommP1.getSDRestSimilarity() , "," ,
				// recommP1.getAvgEndSimilarity()
				// , "," , recommP1.getSDEndSimilarity()
				+ Boolean.toString((currentActName.equals(actActualDoneInSeq))) + "\n");// ,",",recommP1.getActivitiesGuidingRecomm());

		if (Constant.debug18July2018)
		{
			System.out.println("Debug18July2018: Inside writeRawLogsSecDim: sbsRawToWriteForThisUserDate.length()="
					+ sbsRawToWriteForThisUserDate.length());
		}
	}

	/**
	 * Return a new ArrayList with 'recommSeqLength number of new StringBuilders
	 * 
	 * @param recommSeqLength
	 * @return
	 */
	static ArrayList<StringBuilder> createListOfStringBuilders(int recommSeqLength)
	{
		ArrayList<StringBuilder> sbsRawToWriteForThisUserDate = new ArrayList<>(recommSeqLength);
		IntStream.range(0, recommSeqLength).forEachOrdered(i -> sbsRawToWriteForThisUserDate.add(new StringBuilder()));
		return sbsRawToWriteForThisUserDate;
	}

	/**
	 * 
	 * @param matchingUnit
	 * @param lookPastType
	 * @param outputCoreResultsPath
	 */
	private static String computeCommonPath(double matchingUnit, Enums.LookPastType lookPastType,
			String outputCoreResultsPath)
	{
		String commonPath = null;
		// matching unit is only relevant if it is not daywise
		if (lookPastType.equals(Enums.LookPastType.NCount) || lookPastType.equals(Enums.LookPastType.NHours))
		{
			String dirToCreate = outputCoreResultsPath + "/MatchingUnit" + String.valueOf(matchingUnit);
			WToFile.createDirectory(dirToCreate);// Creating the directory for that matching unit
			commonPath = dirToCreate + "/";
		}
		else // daywise //baseline closest time
		{
			commonPath = outputCoreResultsPath;
		}
		return commonPath;
	}

	/**
	 * 
	 * @param matchingUnit
	 * @param lookPastType
	 * @param outputCoreResultsPath
	 */
	static String computeCommonPath(double matchingUnit, Enums.LookPastType lookPastType, String outputCoreResultsPath,
			int thresholdVal)
	{
		String commonPath = null;
		// matching unit is only relevant if it is not daywise
		if (lookPastType.equals(Enums.LookPastType.NCount) || lookPastType.equals(Enums.LookPastType.NHours))
		{
			String dirToCreate;
			if (Constant.useiiWASThreshold)
			{
				dirToCreate = outputCoreResultsPath + thresholdVal + "/MatchingUnit" + String.valueOf(matchingUnit);
				WToFile.createDirectory(outputCoreResultsPath + thresholdVal);
				WToFile.createDirectory(
						outputCoreResultsPath + thresholdVal + "/MatchingUnit" + String.valueOf(matchingUnit));
			}
			else
			{
				dirToCreate = outputCoreResultsPath + "/MatchingUnit" + String.valueOf(matchingUnit);
				WToFile.createDirectory(outputCoreResultsPath + "/MatchingUnit" + String.valueOf(matchingUnit));

			}

			// Creating the directory for that matching unit
			commonPath = dirToCreate + "/";
		}
		else // daywise //baseline closest time
		{
			commonPath = outputCoreResultsPath;
		}
		return commonPath;
	}

	/**
	 * Returns the median seconds since midnight for the given timestamps
	 * 
	 * @param tss
	 * @param userID
	 * @param actName
	 *            just used for nameing the filename to write the result to
	 * @return
	 */
	static double getMedianSeconds(List<Timestamp> tss, int userID, String actName)
	{
		double medianSecsSinceMidnight = -1;
		try
		{
			double[] secsSinceMidnight = tss.stream()
					.mapToDouble(t -> t.getHours() * 60 * 60 + t.getMinutes() * 60 + t.getSeconds()).toArray();

			medianSecsSinceMidnight = StatsUtils.getDescriptiveStatistics(secsSinceMidnight, "secsSinceMidnight",
					userID + "__" + actName + "secsSinceMidnight.txt", false).getPercentile(50);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return medianSecsSinceMidnight;
	}

	static String getUserNameFromUserID(int userId, String databaseName)
	{
		String userName = "";
		// if (databaseName.equals("gowalla1"))
		{
			userName = String.valueOf(userId);
		}
		// else
		// {
		// userName = ConnectDatabase.getUserName(userId);// ConnectDatabase.getUserNameFromDatabase(userId);
		// }
		return userName;
	}

	/**
	 * 
	 * @param numActsInEachCandbw
	 * @param userId
	 * @param dateToRecomm
	 * @param recommMasters
	 * @param recommTimesStrings
	 * @throws IOException
	 */
	protected static void writeNumOfActsInEachCand(BufferedWriter numActsInEachCandbw, int userId, String dateToRecomm,
			RecommendationMasterI[] recommMasters, String[] recommTimesStrings) throws IOException
	{
		StringBuilder tmpWriter = new StringBuilder();
		// System.out.println("\tIterating over candidate timelines:");
		for (int i = 0; i < recommMasters.length; i++)
		{
			RecommendationMasterI recommMaster = recommMasters[i];
			for (Timeline candtt1 : recommMaster.getOnlyCandidateTimeslines())// candidateTimelines.entrySet())
			{
				// Timeline candtt1 = entryAjooba.getValue(); ArrayList<ActivityObject>
				// aa1=candtt1.getActivityObjectsInTimeline();
				// excluding the current activity at the end of the candidate timeline
				// System.out.println("Number of activity Objects in this timeline
				// (except the end current activity) is: "+sizez1);
				tmpWriter = StringUtils.fCat(tmpWriter, String.valueOf(candtt1.countNumberOfValidActivities() - 1), ",",
						candtt1.getTimelineID(), ",", Integer.toString(userId), ",", dateToRecomm, ",",
						recommTimesStrings[i], ",", candtt1.getActivityObjectNamesWithTimestampsInSequence(), ",");
			}
		}
		numActsInEachCandbw.write(tmpWriter.toString() + "\n");
	}

	/**
	 * 
	 * @param mapOfRepAOs
	 * @param mapOfMedianPreSuccDuration
	 */
	protected static void writeRepAOs(LinkedHashMap<Integer, LinkedHashMap<String, ActivityObject2018>> mapOfRepAOs,
			LinkedHashMap<Integer, LinkedHashMap<String, Pair<Double, Double>>> mapOfMedianPreSuccDuration,
			String commonPath)
	{

		StringBuilder sbTemp = new StringBuilder();

		WToFile.appendLineToFileAbs("UserID,ActID,RepAO\n", commonPath + "RepAO.txt");

		for (Entry<Integer, LinkedHashMap<String, ActivityObject2018>> uEntry : mapOfRepAOs.entrySet())
		{
			// sbTemp.append("\nUser:" + uEntry.getKey()).append("Rep AOs:\n");
			uEntry.getValue().entrySet().stream().forEachOrdered(e -> sbTemp
					.append(uEntry.getKey() + "," + e.getKey() + "," + e.getValue().toStringAllGowallaTS() + "\n"));
		}
		WToFile.appendLineToFileAbs(sbTemp.toString(), commonPath + "RepAO.txt");

		StringBuilder sbTemp2 = new StringBuilder();
		WToFile.appendLineToFileAbs("UserID,ActID,PreceedingMedian,SuceedingMedian",
				commonPath + "PreSucMedianDuration.txt");
		for (Entry<Integer, LinkedHashMap<String, Pair<Double, Double>>> uEntry : mapOfMedianPreSuccDuration.entrySet())
		{
			// sbTemp2.append("\nUser:" + uEntry.getKey()).append("\nMedian Prec Succeeding durations:\n");
			uEntry.getValue().entrySet().stream().forEachOrdered(e -> sbTemp2.append(uEntry.getKey() + "," + e.getKey()
					+ "," + e.getValue().getFirst() + "," + e.getValue().getSecond() + "\n"));

		}
		WToFile.appendLineToFileAbs(sbTemp2.toString(), commonPath + "PreSucMedianDuration.txt");

	}

	/**
	 * Make sure train test split for current user is same for both approaches
	 * <p>
	 * Check OKAY as of April 11 2018
	 * 
	 * @param trainTestTimelinesForAllUsersDW
	 * @param userId
	 * @param userTrainingTimelines
	 */
	static void sanityCheckTrainTestSplitSameForCollNonColl(
			LinkedHashMap<String, List<LinkedHashMap<Date, Timeline>>> trainTestTimelinesForAllUsersDW, int userId,
			LinkedHashMap<Date, Timeline> userTrainingTimelines)
	{
		long t1 = System.currentTimeMillis();
		if (Constant.collaborativeCandidates)
		{// start of sanity check
			// make sure train test split for current user is same for both approaches
			List<LinkedHashMap<Date, Timeline>> trainTestTimelinesCollForThisUser = trainTestTimelinesForAllUsersDW
					.get(Integer.toString(userId));
			// get the training test timelines for current user
			LinkedHashMap<Date, Timeline> userTrainingTimelinesColl = trainTestTimelinesCollForThisUser.get(0);
			// LinkedHashMap<Date, Timeline> userTestTimelinesColl =
			// trainTestTimelinesCollForThisUser.get(1);
			if (!userTrainingTimelinesColl.equals(userTrainingTimelines))
			{
				System.err.println("Error: !userTrainingTimelinesColl.equals(userTrainingTimelines) for user:" + userId
						+ " userTrainingTimelinesColl.size()=" + userTrainingTimelinesColl.size()
						+ "userTestTimelinesColl.size()=" + trainTestTimelinesCollForThisUser.get(1).size());
			}
			else
			{
				System.out.println("Timesplit OK. timetaken (secs) = " + (System.currentTimeMillis() - t1) / 1000);
			}
		} // end of sanity check
	}

	/**
	 * 
	 * @param aosForEachPDVal
	 * @param PDVal
	 * @return
	 */
	public static Integer getMedianCheckinsCountForGivePDVal(
			LinkedHashMap<Integer, ArrayList<ActivityObject2018>> aosForEachPDVal, Integer PDVal)
	{
		int res = -1;
		ArrayList<ActivityObject2018> aos = aosForEachPDVal.get(PDVal);

		ArrayList<Double> cinCounts = (ArrayList<Double>) aos.stream().map(ao -> (double) ao.getCheckins_count())
				.collect(Collectors.toList());

		res = (int) StatsUtils.getDescriptiveStatistics(cinCounts).getPercentile(50);
		System.out.println("Inside getMedianCheckinsCountForGivePDVal: median cin for PDVal:" + PDVal + " = " + res);
		return res;
	}

}
