package org.activity.postfilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.constants.DomainConstants;
import org.activity.constants.PathConstants;
import org.activity.constants.VerbosityConstants;
import org.activity.evaluation.EvalDataReader;
import org.activity.io.Serializer;
import org.activity.io.WToFile;
import org.activity.objects.Triple;
import org.activity.spatial.SpatialUtils;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.ComparatorUtils;
import org.activity.util.DateTimeUtils;
import org.activity.util.RegexUtils;

public class PostFilter1
{
	static final String pathToLocGridIDsPerActID = "./dataToRead/actIDLocGridIDsMap.kryo";// TODO SANITY CHECK THIS
	static String pathToWriteLog;// = "./dataWritten/July26PF/";// July20PF/";

	public static void main(String args[])
	{
		String pathToReadRecommendationResultsFrom = "/run/media/gunjan/BackupVault/GOWALLA/GowallaResults/AUG9ED1.0AllActsFDStFilter0hrs100RTVPNN500SNN50/";
		// "./dataWritten/JUL26ED1.0AllActsFDStFilter0hrs100RTVNoTTFilter/";// 100R/";
		// "/run/media/gunjan/BackupVault/GOWALLA/GowallaResults/JUL26ED1.0AllActsFDStFilter0hrs100RTV500PDNTh50SDNTh/100R/";
		new PostFilter1(pathToReadRecommendationResultsFrom, "gowalla1");// second argument added on Nov 3 2018.
	}

	/**
	 * Note: the post filtered recommendations are written back to the same results path.
	 * 
	 * @param commonPathToReadFrom
	 * @param databaseName
	 *            (added on 2 Nov 2018)
	 */
	public PostFilter1(String commonPathToReadFrom, String databaseName)
	{
		String groupsOf100UsersLabels = "";
		if (Constant.useRandomlySampled100Users)
		{
			groupsOf100UsersLabels = DomainConstants.gowalla100RandomUsersLabel;
		}
		if (Constant.runForAllUsersAtOnce)
		{
			groupsOf100UsersLabels = "All";
		}

		String pathToReadRecommendationResultsFrom = commonPathToReadFrom + groupsOf100UsersLabels + "/";
		pathToWriteLog = pathToReadRecommendationResultsFrom + DateTimeUtils.getMonthDateHourMinLabel() + "PF/";
		WToFile.createDirectoryIfNotExists(pathToWriteLog);

		WToFile.redirectConsoleOutput(pathToWriteLog + "ConsoleLog.csv");
		// map of <act id, <list of locations>>
		TreeMap<Integer, TreeSet<Integer>> actIDLocGridIDsMap = (TreeMap<Integer, TreeSet<Integer>>) Serializer
				.kryoDeSerializeThis(pathToLocGridIDsPerActID);

		// map of <location grid id, <list of actIDs>>
		TreeMap<Integer, TreeSet<Integer>> locIDSupportedActIDsMap = getLocIDSupportedActIDMap(actIDLocGridIDsMap,
				pathToWriteLog, false);

		// System.exit(0);

		boolean verbose = false;
		String outerPath = pathToReadRecommendationResultsFrom;
		// "/run/media/gunjan/BackupVault/GOWALLA/GowallaResults/JUL25ED1.0AllActsFDStFilter0hrs100RTV500PDNTh100SDNTh/100R/";
		// "/run/media/gunjan/BackupVault/GOWALLA/GowallaResults/JUL19ED1.0STimeLocAllActsFDStFilter0hrs100RTV/100R/";
		Constant.setCommonPath(outerPath);
		TimeZone.setDefault(TimeZone.getTimeZone("UTC")); // added on April 21, 2016
		Constant.setDefaultTimeZone("UTC");
		PathConstants.initialise(Constant.For9kUsers, databaseName);
		Constant.initialise(outerPath, databaseName, PathConstants.pathToSerialisedCatIDsHierDist,
				PathConstants.pathToSerialisedCatIDNameDictionary, PathConstants.pathToSerialisedLocationObjects,
				PathConstants.pathToSerialisedUserObjects, PathConstants.pathToSerialisedGowallaLocZoneIdMap, false);
		// String[] dimensionPhrases = { "", "SecDim" };

		double[] muArray = Constant.getMatchingUnitArray(Constant.lookPastType, Constant.altSeqPredictor);
		for (double mu : muArray)
		{

			Constant.setCommonPath(outerPath + "MatchingUnit" + (int) (mu) + ".0/");
			String pathToReadResults = Constant.getCommonPath();
			System.out.println("pathToReadResults= " + pathToReadResults);

			int firstIndex = 0;
			String dimensionPhrase = "";
			Triple<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> readArraysPredSeq = EvalDataReader
					.readDataMetaActualTopK(pathToReadResults,
							"dataRankedRecommendation" + dimensionPhrase + "WithScores" + firstIndex + ".csv",
							"dataActual" + dimensionPhrase + firstIndex + ".csv", pathToReadResults, verbose);

			ArrayList<ArrayList<String>> arrayMeta = readArraysPredSeq.getFirst();
			ArrayList<ArrayList<String>> arrayRecommended = readArraysPredSeq.getThird();
			ArrayList<ArrayList<String>> arrayActual = readArraysPredSeq.getSecond();

			dimensionPhrase = "SecDim";
			Triple<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> readArraysPredSeqSecDim = EvalDataReader
					.readDataMetaActualTopK(pathToReadResults,
							"dataRankedRecommendation" + dimensionPhrase + "WithScores" + firstIndex + ".csv",
							"dataActual" + dimensionPhrase + firstIndex + ".csv", pathToReadResults, verbose);

			// ArrayList<ArrayList<String>> arrayMetaSecDim = readArraysPredSeqSecDim.getFirst();
			ArrayList<ArrayList<String>> arrayRecommendedSecDim = readArraysPredSeqSecDim.getThird();
			ArrayList<ArrayList<String>> arrayActualSecDim = readArraysPredSeqSecDim.getSecond();

			doPostFilteringsOfPrimDimBySecDim(arrayMeta, arrayRecommended, arrayActual, arrayRecommendedSecDim,
					arrayActualSecDim, actIDLocGridIDsMap, locIDSupportedActIDsMap, verbose);

			// System.exit(0);
		}
		PopUps.showMessage("All postfilterings done");
		// System.exit(0);
	}

	/**
	 * 
	 * @param fileNamePhrase
	 * @param timeCategory
	 * @param arrayMeta
	 * @param arrayTopK
	 * @param arrayActual
	 * @param actIDLocGridIDsMap
	 * @param locIDSupportedActIDsMap
	 * @param levelAtWhichToMatch
	 * @param ver
	 * @return ArrayList<ArrayList<ArrayList<Integer>>> arrayDirectAgreements
	 */
	private static ArrayList<ArrayList<ArrayList<Integer>>> doPostFilteringsOfPrimDimBySecDim(
			ArrayList<ArrayList<String>> arrayMeta, ArrayList<ArrayList<String>> arrayTopK,
			ArrayList<ArrayList<String>> arrayActual, ArrayList<ArrayList<String>> arrayTopKSecDim,
			ArrayList<ArrayList<String>> arrayActualSecDim, TreeMap<Integer, TreeSet<Integer>> actIDLocGridIDsMap,
			TreeMap<Integer, TreeSet<Integer>> locIDSupportedActIDsMap, boolean verbose)
	{
		ArrayList<ArrayList<ArrayList<Integer>>> arrayDirectAgreements = new ArrayList<>();
		int startUserIndex = 0;// TODO 0

		StringBuilder sbPDWtdAlpha5_5_5PF = new StringBuilder();
		StringBuilder sbPDWtdAlpha5_5_25PF = new StringBuilder();
		StringBuilder sbPDWtdAlpha5_10_5PF = new StringBuilder();
		StringBuilder sbPDFltr_on_Top5LocsPF = new StringBuilder();
		StringBuilder sbPDFltr_on_Top10LocsPF = new StringBuilder();
		StringBuilder sbPDFltr_on_ActualLocPF = new StringBuilder();
		StringBuilder sbPDFltr_on_RandomLocPF = new StringBuilder();
		StringBuilder sbPDFltr_on_Random2LocPF = new StringBuilder();
		StringBuilder sbPDFltr_on_Random10LocPF = new StringBuilder();
		StringBuilder sbPDFltr_on_Random20LocPF = new StringBuilder();
		StringBuilder sbPDFltr_on_Random50LocPF = new StringBuilder();
		StringBuilder sbPDFltr_on_Top1LocPF = new StringBuilder();

		// boolean verbose = false;

		try
		{
			System.out.println("size of meta array=" + arrayMeta.size() + "     size of arrayRecommendedSeq array="
					+ arrayTopK.size() + "   size of arrayActualSeq array=" + arrayActual.size());

			for (int i = startUserIndex; i < arrayMeta.size(); i++) // iterating over users (or rows)
			{
				ArrayList<String> currentMetaLineArray = arrayMeta.get(i);
				int countOfRecommendationTimesConsidered = 0; // =count of meta entries considered
				// ArrayList<ArrayList<Integer>> directAgreementsForThisUser = new ArrayList<>();

				int totalNumofRTsForThisUser = currentMetaLineArray.size();

				for (int j = startUserIndex; j < currentMetaLineArray.size(); j++)// iterating over RTs (or columns)
				{

					countOfRecommendationTimesConsidered++;
					String actual = arrayActual.get(i).get(j);
					String actualSecDim = arrayActualSecDim.get(i).get(j);

					double distMaxThreshold = 500;

					String randomNeighbouringSecDim = getRandomNeighbouringIncorrectLocGridID(actualSecDim, 2.0,
							distMaxThreshold);
					// String random2NeighbouringSecDim = ProbabilityUtilityBelt.trueWithProbability(0.02)
					// ? getRandomNeighbouringIncorrectLocGridID(actualSecDim, 2.0, distMaxThreshold)
					// : actualSecDim;
					// String random10NeighbouringSecDim = ProbabilityUtilityBelt.trueWithProbability(0.10)
					// ? getRandomNeighbouringIncorrectLocGridID(actualSecDim, 2.0, distMaxThreshold)
					// : actualSecDim;
					// String random20NeighbouringSecDim = ProbabilityUtilityBelt.trueWithProbability(0.2)
					// ? getRandomNeighbouringIncorrectLocGridID(actualSecDim, 2.0, distMaxThreshold)
					// : actualSecDim;
					// String random50NeighbouringSecDim = ProbabilityUtilityBelt.trueWithProbability(0.5)
					// ? getRandomNeighbouringIncorrectLocGridID(actualSecDim, 2.0, distMaxThreshold)
					// : actualSecDim;

					String random2NeighbouringSecDim = (countOfRecommendationTimesConsidered < Math
							.ceil(0.02 * totalNumofRTsForThisUser))
									? getRandomNeighbouringIncorrectLocGridID(actualSecDim, 2.0, distMaxThreshold)
									: actualSecDim;
					String random10NeighbouringSecDim = (countOfRecommendationTimesConsidered < Math
							.ceil(0.10 * totalNumofRTsForThisUser))
									? getRandomNeighbouringIncorrectLocGridID(actualSecDim, 2.0, distMaxThreshold)
									: actualSecDim;
					String random20NeighbouringSecDim = (countOfRecommendationTimesConsidered < Math
							.ceil(0.20 * totalNumofRTsForThisUser))
									? getRandomNeighbouringIncorrectLocGridID(actualSecDim, 2.0, distMaxThreshold)
									: actualSecDim;
					String random50NeighbouringSecDim = (countOfRecommendationTimesConsidered < Math
							.ceil(0.50 * totalNumofRTsForThisUser))
									? getRandomNeighbouringIncorrectLocGridID(actualSecDim, 2.0, distMaxThreshold)
									: actualSecDim;

					LinkedHashMap<String, Double> topKWithScorePrimDim = getTopKWithScore(arrayTopK.get(i).get(j));
					LinkedHashMap<String, Double> topKWithScoreSecDim = getTopKWithScore(arrayTopKSecDim.get(i).get(j));

					LinkedHashMap<String, Double> top5WithScorePrimDim = trimToTopN(topKWithScorePrimDim, 5);
					LinkedHashMap<String, Double> top10WithScorePrimDim = trimToTopN(topKWithScorePrimDim, 10);

					LinkedHashMap<String, Double> top3WithScoreSecDim = trimToTopN(topKWithScoreSecDim, 3);
					LinkedHashMap<String, Double> top5WithScoreSecDim = trimToTopN(topKWithScoreSecDim, 5);
					LinkedHashMap<String, Double> top10WithScoreSecDim = trimToTopN(topKWithScoreSecDim, 10);

					top5WithScorePrimDim = minMaxNormalise(top5WithScorePrimDim);
					top10WithScorePrimDim = minMaxNormalise(top10WithScorePrimDim);// TODO INCOMLETE

					top3WithScoreSecDim = minMaxNormalise(top3WithScoreSecDim);// TODO INCOMLETE
					top5WithScoreSecDim = minMaxNormalise(top5WithScoreSecDim);
					top10WithScoreSecDim = minMaxNormalise(top10WithScoreSecDim);// TODO INCOMLETE

					LinkedHashMap<String, Double> locGridAvailabilityScoreForEachActIDTop3 = getLocGridAvailabilityScoreForEachActID(
							top5WithScorePrimDim, top3WithScoreSecDim, actIDLocGridIDsMap, verbose);

					LinkedHashMap<String, Double> locGridAvailabilityScoreForEachActIDTop5 = getLocGridAvailabilityScoreForEachActID(
							top5WithScorePrimDim, top5WithScoreSecDim, actIDLocGridIDsMap, verbose);

					LinkedHashMap<String, Double> locGridAvailabilityScoreForEachActIDTop10 = getLocGridAvailabilityScoreForEachActID(
							top10WithScorePrimDim, top10WithScoreSecDim, actIDLocGridIDsMap, verbose);

					/////////////////////
					LinkedHashMap<String, Double> recommPDWtdAlphaPF = weightedCombine(top5WithScorePrimDim,
							locGridAvailabilityScoreForEachActIDTop5, 0.5, true, verbose);
					recommPDWtdAlphaPF.entrySet().stream()
							.forEachOrdered(e -> sbPDWtdAlpha5_5_5PF.append("__" + e.getKey()));
					sbPDWtdAlpha5_5_5PF.append(",");
					/////////////////////
					// added on 9 Aug 2018
					LinkedHashMap<String, Double> recommPDWtdAlpha25PF = weightedCombine(top5WithScorePrimDim,
							locGridAvailabilityScoreForEachActIDTop5, 0.25, true, verbose);
					recommPDWtdAlpha25PF.entrySet().stream()
							.forEachOrdered(e -> sbPDWtdAlpha5_5_25PF.append("__" + e.getKey()));
					sbPDWtdAlpha5_5_25PF.append(",");
					/////////////////////
					/////////////////////
					// added on 9 Aug 2018
					// LinkedHashMap<String, Double> recommPDWtdAlpha25PF = weightedCombine(top5WithScorePrimDim,
					// locGridAvailabilityScoreForEachActIDTop5, 0.25, true, verbose);
					// recommPDWtdAlpha25PF.entrySet().stream()
					// .forEachOrdered(e -> sbPDWtdAlpha5_5_25PF.append("__" + e.getKey()));
					// sbPDWtdAlpha5_5_25PF.append(",");
					/////////////////////

					LinkedHashMap<String, Double> recommPDFltr_on_Top5LocsPF = filterKeepingPrimDimScore(
							top5WithScorePrimDim, locGridAvailabilityScoreForEachActIDTop5, true, verbose);
					recommPDFltr_on_Top5LocsPF.entrySet().stream()
							.forEachOrdered(e -> sbPDFltr_on_Top5LocsPF.append("__" + e.getKey()));
					sbPDFltr_on_Top5LocsPF.append(",");
					/////////////////////
					/////////////////////
					// added on Aug 9 2018
					LinkedHashMap<String, Double> recommPDFltr_on_Top10LocsPF = filterKeepingPrimDimScore(
							top10WithScorePrimDim, locGridAvailabilityScoreForEachActIDTop10, true, verbose);
					recommPDFltr_on_Top10LocsPF.entrySet().stream()
							.forEachOrdered(e -> sbPDFltr_on_Top10LocsPF.append("__" + e.getKey()));
					sbPDFltr_on_Top10LocsPF.append(",");
					/////////////////////

					LinkedHashMap<String, Double> recommPDFltr_on_ActualLocPF = filterActsOnOneLocation(
							top5WithScorePrimDim, Integer.valueOf(actualSecDim), verbose, locIDSupportedActIDsMap,
							"Fltr_on_ActualLocPF");
					recommPDFltr_on_ActualLocPF.entrySet().stream()
							.forEachOrdered(e -> sbPDFltr_on_ActualLocPF.append("__" + e.getKey()));
					sbPDFltr_on_ActualLocPF.append(",");
					/////////////////////

					/////////////////////
					// add on 9 Aug 2018
					LinkedHashMap<String, Double> recommPDFltr_on_RandomLocPF = filterActsOnOneLocation(
							top5WithScorePrimDim, Integer.valueOf(randomNeighbouringSecDim), verbose,
							locIDSupportedActIDsMap, "Fltr_on_RandomLocPF");
					recommPDFltr_on_RandomLocPF.entrySet().stream()
							.forEachOrdered(e -> sbPDFltr_on_RandomLocPF.append("__" + e.getKey()));
					sbPDFltr_on_RandomLocPF.append(",");
					/////////////////////
					// add on 9 Aug 2018
					LinkedHashMap<String, Double> recommPDFltr_on_Random2LocPF = filterActsOnOneLocation(
							top5WithScorePrimDim, Integer.valueOf(random2NeighbouringSecDim), verbose,
							locIDSupportedActIDsMap, "Fltr_on_Random2LocPF");
					recommPDFltr_on_Random2LocPF.entrySet().stream()
							.forEachOrdered(e -> sbPDFltr_on_Random2LocPF.append("__" + e.getKey()));
					sbPDFltr_on_Random2LocPF.append(",");
					/////////////////////
					// add on 9 Aug 2018
					LinkedHashMap<String, Double> recommPDFltr_on_Random10LocPF = filterActsOnOneLocation(
							top5WithScorePrimDim, Integer.valueOf(random10NeighbouringSecDim), verbose,
							locIDSupportedActIDsMap, "Fltr_on_Random10LocPF");
					recommPDFltr_on_Random10LocPF.entrySet().stream()
							.forEachOrdered(e -> sbPDFltr_on_Random10LocPF.append("__" + e.getKey()));
					sbPDFltr_on_Random10LocPF.append(",");
					/////////////////////
					// add on 9 Aug 2018
					LinkedHashMap<String, Double> recommPDFltr_on_Random20LocPF = filterActsOnOneLocation(
							top5WithScorePrimDim, Integer.valueOf(random20NeighbouringSecDim), verbose,
							locIDSupportedActIDsMap, "Fltr_on_Random20LocPF");
					recommPDFltr_on_Random20LocPF.entrySet().stream()
							.forEachOrdered(e -> sbPDFltr_on_Random20LocPF.append("__" + e.getKey()));
					sbPDFltr_on_Random20LocPF.append(",");
					/////////////////////
					// add on 9 Aug 2018
					LinkedHashMap<String, Double> recommPDFltr_on_Random50LocPF = filterActsOnOneLocation(
							top5WithScorePrimDim, Integer.valueOf(random50NeighbouringSecDim), verbose,
							locIDSupportedActIDsMap, "Fltr_on_Random50LocPF");
					recommPDFltr_on_Random50LocPF.entrySet().stream()
							.forEachOrdered(e -> sbPDFltr_on_Random50LocPF.append("__" + e.getKey()));
					sbPDFltr_on_Random50LocPF.append(",");
					/////////////////////

					Integer top1LocGridID = top5WithScoreSecDim.entrySet().stream()
							.map(e -> Integer.valueOf(e.getKey())).limit(1).collect(Collectors.toList()).get(0);

					System.out.println("topKWithScoreSecDim = " + top5WithScoreSecDim);
					System.out.println("top1LocGridID = " + top1LocGridID);

					LinkedHashMap<String, Double> recommPDFltr_on_Top1LocsPF = filterActsOnOneLocation(
							top5WithScorePrimDim, Integer.valueOf(top1LocGridID), verbose, locIDSupportedActIDsMap,
							"Fltr_on_Top1LocsPF");
					recommPDFltr_on_Top1LocsPF.entrySet().stream()
							.forEachOrdered(e -> sbPDFltr_on_Top1LocPF.append("__" + e.getKey()));
					sbPDFltr_on_Top1LocPF.append(",");

					// LinkedHashMap<String, Double> recommFilterActs_on_Top1Locs = filterBasedOnGivenLocation(
					// topKWithScorePrimDim, , verbose);
					// recommPrimDimPFStrongFilter.entrySet().stream()
					// .forEachOrdered(e -> sbFilterAllLocsPF.append("__" + e.getKey()));
					// sbFilterAllLocsPF.append(",");

					// System.exit(0);

					if (VerbosityConstants.verbose)
					{
						System.out.println("\tarrayRecommendedSeq=" + arrayTopK.get(i).get(j));
						System.out.println("\tarrayActualSeq string =" + arrayActual.get(i).get(j));
						// System.out.println("\tdirectAgreement" + directAgreement);
						System.out.println("\n-------------------");
					}
					// end of if for this time categegory
				} // end of current line array
				sbPDWtdAlpha5_5_5PF.append("\n");
				sbPDFltr_on_Top5LocsPF.append("\n");
				sbPDFltr_on_Top10LocsPF.append("\n");
				sbPDFltr_on_ActualLocPF.append("\n");
				sbPDFltr_on_RandomLocPF.append("\n");
				sbPDFltr_on_Random2LocPF.append("\n");
				sbPDFltr_on_Random10LocPF.append("\n");
				sbPDFltr_on_Random20LocPF.append("\n");
				sbPDFltr_on_Random50LocPF.append("\n");
				sbPDFltr_on_Top1LocPF.append("\n");
			} // end of loop over user

			// WtdAlphaPF, Fltr_on_TopKLocsPF, Fltr_on_ActualLocPF,Fltr_on_Top1Loc
			WToFile.writeToNewFile(sbPDWtdAlpha5_5_5PF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationWtdAlphaPFWithoutScores0.csv");
			WToFile.writeToNewFile(sbPDFltr_on_Top5LocsPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationFltr_on_TopKLocsPFWithoutScores0.csv");
			WToFile.writeToNewFile(sbPDFltr_on_Top10LocsPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationFltr_on_Top10LocsPFWithoutScores0.csv");
			WToFile.writeToNewFile(sbPDFltr_on_ActualLocPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationFltr_on_ActualLocPFWithoutScores0.csv");
			WToFile.writeToNewFile(sbPDFltr_on_RandomLocPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationFltr_on_RandomLocPFWithoutScores0.csv");
			WToFile.writeToNewFile(sbPDFltr_on_Random2LocPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationFltr_on_Random2LocPFWithoutScores0.csv");
			WToFile.writeToNewFile(sbPDFltr_on_Random10LocPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationFltr_on_Random10LocPFWithoutScores0.csv");
			WToFile.writeToNewFile(sbPDFltr_on_Random20LocPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationFltr_on_Random20LocPFWithoutScores0.csv");
			WToFile.writeToNewFile(sbPDFltr_on_Random50LocPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationFltr_on_Random50LocPFWithoutScores0.csv");
			WToFile.writeToNewFile(sbPDFltr_on_Top1LocPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationFltr_on_Top1LocWithoutScores0.csv");
			// PopUps.showMessage("Writing to path:\n" + Constant.getCommonPath()
			// + "dataRankedRecommendationFltr_on_Top1LocWithoutScores0.csv");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return arrayDirectAgreements;
	}

	/**
	 * 
	 * @param fileNamePhrase
	 * @param timeCategory
	 * @param arrayMeta
	 * @param arrayTopK
	 * @param arrayActual
	 * @param actIDLocGridIDsMap
	 * @param locIDSupportedActIDsMap
	 * @param levelAtWhichToMatch
	 * @param ver
	 * @return ArrayList<ArrayList<ArrayList<Integer>>> arrayDirectAgreements
	 * @since 9 Aug 2018 TODO INCOMPLETE
	 */
	private static ArrayList<ArrayList<ArrayList<Integer>>> doPostFilteringsOfSecDimByPrimDim(
			ArrayList<ArrayList<String>> arrayMeta, ArrayList<ArrayList<String>> arrayTopK,
			ArrayList<ArrayList<String>> arrayActual, ArrayList<ArrayList<String>> arrayTopKSecDim,
			ArrayList<ArrayList<String>> arrayActualSecDim, TreeMap<Integer, TreeSet<Integer>> actIDLocGridIDsMap,
			TreeMap<Integer, TreeSet<Integer>> locIDSupportedActIDsMap, boolean verbose)
	{
		ArrayList<ArrayList<ArrayList<Integer>>> arrayDirectAgreements = new ArrayList<>();
		int startUserIndex = 0;// TODO 0

		StringBuilder sbPDWtdAlphaPF = new StringBuilder();
		StringBuilder sbPDFltr_on_TopKLocsPF = new StringBuilder();
		StringBuilder sbPDFltr_on_ActualLocPF = new StringBuilder();
		StringBuilder sbPDFltr_on_RandomLocPF = new StringBuilder();
		StringBuilder sbPDFltr_on_Random2LocPF = new StringBuilder();
		StringBuilder sbPDFltr_on_Random10LocPF = new StringBuilder();
		StringBuilder sbPDFltr_on_Random20LocPF = new StringBuilder();
		StringBuilder sbPDFltr_on_Random50LocPF = new StringBuilder();
		StringBuilder sbPDFltr_on_Top1LocPF = new StringBuilder();

		// boolean verbose = false;

		try
		{
			System.out.println("size of meta array=" + arrayMeta.size() + "     size of arrayRecommendedSeq array="
					+ arrayTopK.size() + "   size of arrayActualSeq array=" + arrayActual.size());

			for (int i = startUserIndex; i < arrayMeta.size(); i++) // iterating over users (or rows)
			{
				ArrayList<String> currentMetaLineArray = arrayMeta.get(i);
				int countOfRecommendationTimesConsidered = 0; // =count of meta entries considered
				// ArrayList<ArrayList<Integer>> directAgreementsForThisUser = new ArrayList<>();

				int totalNumofRTsForThisUser = currentMetaLineArray.size();

				for (int j = startUserIndex; j < currentMetaLineArray.size(); j++)// iterating over RTs (or columns)
				{

					countOfRecommendationTimesConsidered++;
					String actual = arrayActual.get(i).get(j);
					String actualSecDim = arrayActualSecDim.get(i).get(j);

					double distMaxThreshold = 500;

					String randomNeighbouringSecDim = getRandomNeighbouringIncorrectLocGridID(actualSecDim, 2.0,
							distMaxThreshold);
					// String random2NeighbouringSecDim = ProbabilityUtilityBelt.trueWithProbability(0.02)
					// ? getRandomNeighbouringIncorrectLocGridID(actualSecDim, 2.0, distMaxThreshold)
					// : actualSecDim;
					// String random10NeighbouringSecDim = ProbabilityUtilityBelt.trueWithProbability(0.10)
					// ? getRandomNeighbouringIncorrectLocGridID(actualSecDim, 2.0, distMaxThreshold)
					// : actualSecDim;
					// String random20NeighbouringSecDim = ProbabilityUtilityBelt.trueWithProbability(0.2)
					// ? getRandomNeighbouringIncorrectLocGridID(actualSecDim, 2.0, distMaxThreshold)
					// : actualSecDim;
					// String random50NeighbouringSecDim = ProbabilityUtilityBelt.trueWithProbability(0.5)
					// ? getRandomNeighbouringIncorrectLocGridID(actualSecDim, 2.0, distMaxThreshold)
					// : actualSecDim;

					String random2NeighbouringSecDim = (countOfRecommendationTimesConsidered < Math
							.ceil(0.02 * totalNumofRTsForThisUser))
									? getRandomNeighbouringIncorrectLocGridID(actualSecDim, 2.0, distMaxThreshold)
									: actualSecDim;
					String random10NeighbouringSecDim = (countOfRecommendationTimesConsidered < Math
							.ceil(0.10 * totalNumofRTsForThisUser))
									? getRandomNeighbouringIncorrectLocGridID(actualSecDim, 2.0, distMaxThreshold)
									: actualSecDim;
					String random20NeighbouringSecDim = (countOfRecommendationTimesConsidered < Math
							.ceil(0.20 * totalNumofRTsForThisUser))
									? getRandomNeighbouringIncorrectLocGridID(actualSecDim, 2.0, distMaxThreshold)
									: actualSecDim;
					String random50NeighbouringSecDim = (countOfRecommendationTimesConsidered < Math
							.ceil(0.50 * totalNumofRTsForThisUser))
									? getRandomNeighbouringIncorrectLocGridID(actualSecDim, 2.0, distMaxThreshold)
									: actualSecDim;

					LinkedHashMap<String, Double> topKWithScorePrimDim = getTopKWithScore(arrayTopK.get(i).get(j));
					LinkedHashMap<String, Double> topKWithScoreSecDim = getTopKWithScore(arrayTopKSecDim.get(i).get(j));

					topKWithScorePrimDim = trimToTopN(topKWithScorePrimDim, 5);
					topKWithScoreSecDim = trimToTopN(topKWithScoreSecDim, 5);

					topKWithScorePrimDim = minMaxNormalise(topKWithScorePrimDim);
					topKWithScoreSecDim = minMaxNormalise(topKWithScoreSecDim);

					LinkedHashMap<String, Double> locGridAvailabilityScoreForEachActID = getLocGridAvailabilityScoreForEachActID(
							topKWithScorePrimDim, topKWithScoreSecDim, actIDLocGridIDsMap, verbose);

					LinkedHashMap<String, Double> recommPDWtdAlphaPF = weightedCombine(topKWithScorePrimDim,
							locGridAvailabilityScoreForEachActID, 0.5, true, verbose);
					recommPDWtdAlphaPF.entrySet().stream()
							.forEachOrdered(e -> sbPDWtdAlphaPF.append("__" + e.getKey()));
					sbPDWtdAlphaPF.append(",");
					/////////////////////

					LinkedHashMap<String, Double> recommPDFltr_on_TopKLocsPF = filterKeepingPrimDimScore(
							topKWithScorePrimDim, locGridAvailabilityScoreForEachActID, true, verbose);
					recommPDFltr_on_TopKLocsPF.entrySet().stream()
							.forEachOrdered(e -> sbPDFltr_on_TopKLocsPF.append("__" + e.getKey()));
					sbPDFltr_on_TopKLocsPF.append(",");
					/////////////////////

					LinkedHashMap<String, Double> recommPDFltr_on_ActualLocPF = filterActsOnOneLocation(
							topKWithScorePrimDim, Integer.valueOf(actualSecDim), verbose, locIDSupportedActIDsMap,
							"Fltr_on_ActualLocPF");
					recommPDFltr_on_ActualLocPF.entrySet().stream()
							.forEachOrdered(e -> sbPDFltr_on_ActualLocPF.append("__" + e.getKey()));
					sbPDFltr_on_ActualLocPF.append(",");
					/////////////////////

					/////////////////////
					// add on 9 Aug 2018
					LinkedHashMap<String, Double> recommPDFltr_on_RandomLocPF = filterActsOnOneLocation(
							topKWithScorePrimDim, Integer.valueOf(randomNeighbouringSecDim), verbose,
							locIDSupportedActIDsMap, "Fltr_on_RandomLocPF");
					recommPDFltr_on_RandomLocPF.entrySet().stream()
							.forEachOrdered(e -> sbPDFltr_on_RandomLocPF.append("__" + e.getKey()));
					sbPDFltr_on_RandomLocPF.append(",");
					/////////////////////
					// add on 9 Aug 2018
					LinkedHashMap<String, Double> recommPDFltr_on_Random2LocPF = filterActsOnOneLocation(
							topKWithScorePrimDim, Integer.valueOf(random2NeighbouringSecDim), verbose,
							locIDSupportedActIDsMap, "Fltr_on_Random2LocPF");
					recommPDFltr_on_Random2LocPF.entrySet().stream()
							.forEachOrdered(e -> sbPDFltr_on_Random2LocPF.append("__" + e.getKey()));
					sbPDFltr_on_Random2LocPF.append(",");
					/////////////////////
					// add on 9 Aug 2018
					LinkedHashMap<String, Double> recommPDFltr_on_Random10LocPF = filterActsOnOneLocation(
							topKWithScorePrimDim, Integer.valueOf(random10NeighbouringSecDim), verbose,
							locIDSupportedActIDsMap, "Fltr_on_Random10LocPF");
					recommPDFltr_on_Random10LocPF.entrySet().stream()
							.forEachOrdered(e -> sbPDFltr_on_Random10LocPF.append("__" + e.getKey()));
					sbPDFltr_on_Random10LocPF.append(",");
					/////////////////////
					// add on 9 Aug 2018
					LinkedHashMap<String, Double> recommPDFltr_on_Random20LocPF = filterActsOnOneLocation(
							topKWithScorePrimDim, Integer.valueOf(random20NeighbouringSecDim), verbose,
							locIDSupportedActIDsMap, "Fltr_on_Random20LocPF");
					recommPDFltr_on_Random20LocPF.entrySet().stream()
							.forEachOrdered(e -> sbPDFltr_on_Random20LocPF.append("__" + e.getKey()));
					sbPDFltr_on_Random20LocPF.append(",");
					/////////////////////
					// add on 9 Aug 2018
					LinkedHashMap<String, Double> recommPDFltr_on_Random50LocPF = filterActsOnOneLocation(
							topKWithScorePrimDim, Integer.valueOf(random50NeighbouringSecDim), verbose,
							locIDSupportedActIDsMap, "Fltr_on_Random50LocPF");
					recommPDFltr_on_Random50LocPF.entrySet().stream()
							.forEachOrdered(e -> sbPDFltr_on_Random50LocPF.append("__" + e.getKey()));
					sbPDFltr_on_Random50LocPF.append(",");
					/////////////////////

					Integer top1LocGridID = topKWithScoreSecDim.entrySet().stream()
							.map(e -> Integer.valueOf(e.getKey())).limit(1).collect(Collectors.toList()).get(0);

					System.out.println("topKWithScoreSecDim = " + topKWithScoreSecDim);
					System.out.println("top1LocGridID = " + top1LocGridID);

					LinkedHashMap<String, Double> recommPDFltr_on_Top1LocsPF = filterActsOnOneLocation(
							topKWithScorePrimDim, Integer.valueOf(top1LocGridID), verbose, locIDSupportedActIDsMap,
							"Fltr_on_Top1LocsPF");
					recommPDFltr_on_Top1LocsPF.entrySet().stream()
							.forEachOrdered(e -> sbPDFltr_on_Top1LocPF.append("__" + e.getKey()));
					sbPDFltr_on_Top1LocPF.append(",");

					// LinkedHashMap<String, Double> recommFilterActs_on_Top1Locs = filterBasedOnGivenLocation(
					// topKWithScorePrimDim, , verbose);
					// recommPrimDimPFStrongFilter.entrySet().stream()
					// .forEachOrdered(e -> sbFilterAllLocsPF.append("__" + e.getKey()));
					// sbFilterAllLocsPF.append(",");

					// System.exit(0);

					if (VerbosityConstants.verbose)
					{
						System.out.println("\tarrayRecommendedSeq=" + arrayTopK.get(i).get(j));
						System.out.println("\tarrayActualSeq string =" + arrayActual.get(i).get(j));
						// System.out.println("\tdirectAgreement" + directAgreement);
						System.out.println("\n-------------------");
					}
					// end of if for this time categegory
				} // end of current line array
				sbPDWtdAlphaPF.append("\n");
				sbPDFltr_on_TopKLocsPF.append("\n");
				sbPDFltr_on_ActualLocPF.append("\n");
				sbPDFltr_on_RandomLocPF.append("\n");
				sbPDFltr_on_Random2LocPF.append("\n");
				sbPDFltr_on_Random10LocPF.append("\n");
				sbPDFltr_on_Random20LocPF.append("\n");
				sbPDFltr_on_Random50LocPF.append("\n");
				sbPDFltr_on_Top1LocPF.append("\n");
			} // end of loop over user

			// WtdAlphaPF, Fltr_on_TopKLocsPF, Fltr_on_ActualLocPF,Fltr_on_Top1Loc
			WToFile.writeToNewFile(sbPDWtdAlphaPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationWtdAlphaPFWithoutScores0.csv");
			WToFile.writeToNewFile(sbPDFltr_on_TopKLocsPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationFltr_on_TopKLocsPFWithoutScores0.csv");
			WToFile.writeToNewFile(sbPDFltr_on_ActualLocPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationFltr_on_ActualLocPFWithoutScores0.csv");
			WToFile.writeToNewFile(sbPDFltr_on_RandomLocPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationFltr_on_RandomLocPFWithoutScores0.csv");
			WToFile.writeToNewFile(sbPDFltr_on_Random2LocPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationFltr_on_Random2LocPFWithoutScores0.csv");
			WToFile.writeToNewFile(sbPDFltr_on_Random10LocPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationFltr_on_Random10LocPFWithoutScores0.csv");
			WToFile.writeToNewFile(sbPDFltr_on_Random20LocPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationFltr_on_Random20LocPFWithoutScores0.csv");
			WToFile.writeToNewFile(sbPDFltr_on_Random50LocPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationFltr_on_Random50LocPFWithoutScores0.csv");
			WToFile.writeToNewFile(sbPDFltr_on_Top1LocPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationFltr_on_Top1LocWithoutScores0.csv");
			// PopUps.showMessage("Writing to path:\n" + Constant.getCommonPath()
			// + "dataRankedRecommendationFltr_on_Top1LocWithoutScores0.csv");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return arrayDirectAgreements;
	}

	/**
	 * 
	 * @param actualLocGridIDS
	 * @param distMinThresholdInKms
	 * @param distMaxThresholdInKms
	 * @return
	 * @since 9 Aug 2018
	 */
	private static String getRandomNeighbouringIncorrectLocGridID(String actualLocGridIDS, double distMinThresholdInKms,
			double distMaxThresholdInKms)
	{
		double maxThresh = distMinThresholdInKms;
		Random r = new Random();
		String res = null;
		while (res == null && maxThresh <= distMaxThresholdInKms)
		{
			maxThresh += 1 + r.nextInt(4 + 1);
			res = getRandomNeighbouringIncorrectLocGridID2(actualLocGridIDS, distMinThresholdInKms, maxThresh);
		}

		if (res == null)
		{
			PopUps.printTracedErrorMsgWithExit(
					"No Neighbour found for " + actualLocGridIDS + " within distMinThresholdInKms = "
							+ distMinThresholdInKms + " distMaxThresholdInKms=" + distMaxThresholdInKms);
		}
		return res;
	}

	/**
	 * 
	 * @param actualLocGridIDS
	 * @param distMinThresholdInKms
	 * @param distMaxThresholdInKms
	 * @return
	 * @since 9 Aug 2018
	 */
	private static String getRandomNeighbouringIncorrectLocGridID2(String actualLocGridIDS,
			double distMinThresholdInKms, double distMaxThresholdInKms)
	{
		Integer actualLocGridID = Integer.valueOf(actualLocGridIDS);

		boolean foundNeighbour = false;

		Random r = new Random();
		int gridIndexMaxHopDist = 1;
		int gridIndexDist = 1;

		double actualLatLon[] = DomainConstants.getRGridLatLonForGridID(actualLocGridID);

		while (!foundNeighbour)
		{
			// gridIndexDist += 1 + r.nextInt(3); // increment by either 1 or 2.
			// if (gridIndexDist > 30)
			// {
			// break;
			// }
			gridIndexDist += 1;

			if (gridIndexDist > 10000)
			{
				break;
			}

			Integer sampleGridID1 = actualLocGridID + gridIndexDist;
			Integer sampleGridID2 = actualLocGridID - gridIndexDist;

			if (DomainConstants.isPresentInGowallaDataset(sampleGridID1))
			{
				double sampleLatLon[] = DomainConstants.getRGridLatLonForGridID(sampleGridID1);
				double hdist = SpatialUtils.haversineFastMathV3NoRound(actualLatLon[0], actualLatLon[1],
						sampleLatLon[0], sampleLatLon[1]);
				if (hdist >= distMinThresholdInKms && hdist <= distMaxThresholdInKms)
				{
					System.out.println("Found random neighbour for " + actualLocGridIDS + " as " + sampleGridID1
							+ " at " + hdist + "kms");
					foundNeighbour = true;
					return String.valueOf(sampleGridID1);
				}
			}
			else if (DomainConstants.isPresentInGowallaDataset(sampleGridID2))
			{
				double sampleLatLon[] = DomainConstants.getRGridLatLonForGridID(sampleGridID2);
				double hdist = SpatialUtils.haversineFastMathV3NoRound(actualLatLon[0], actualLatLon[1],
						sampleLatLon[0], sampleLatLon[1]);

				if (hdist >= distMinThresholdInKms && hdist <= distMaxThresholdInKms)
				{
					System.out.println("Found random neighbour for " + actualLocGridIDS + " as " + sampleGridID1
							+ " at " + hdist + "kms");
					foundNeighbour = true;
					return String.valueOf(sampleGridID2);
				}
			}

		}

		// if (foundNeighbour == false)
		// {
		// PopUps.printTracedErrorMsgWithExit(
		// "No Neighbour found for " + actualLocGridIDS + " withing gridIndexDist = " + gridIndexDist);
		// }
		return null;
	}

	/**
	 * 
	 * @param topKWithScorePrimDim
	 * @param locGridAvailabilityScoreForEachActID
	 * @param strongFilter
	 * @param verbose
	 * @return
	 */
	private static LinkedHashMap<String, Double> filterKeepingPrimDimScore(
			LinkedHashMap<String, Double> topKWithScorePrimDim,
			LinkedHashMap<String, Double> locGridAvailabilityScoreForEachActID, boolean strongFilter, boolean verbose)
	{
		// alpha not user here, keeping primary dimension ranking score
		if (verbose)
		{
			System.out.println("filterKeepingPrimDimScore called.");
		}
		return weightedCombine(topKWithScorePrimDim, locGridAvailabilityScoreForEachActID, -9999999, strongFilter,
				verbose, true);
	}

	/**
	 * 
	 * @param topKWithScorePrimDim
	 * @param locGridAvailabilityScoreForEachActID
	 * @param alpha
	 * @param strongFilter
	 * @param verbose
	 * @return
	 */
	private static LinkedHashMap<String, Double> weightedCombine(LinkedHashMap<String, Double> topKWithScorePrimDim,
			LinkedHashMap<String, Double> locGridAvailabilityScoreForEachActID, double alpha, boolean strongFilter,
			boolean verbose)
	{
		if (verbose)
		{
			System.out.println("weightedCombine called.");
		}
		return weightedCombine(topKWithScorePrimDim, locGridAvailabilityScoreForEachActID, alpha, strongFilter, verbose,
				false);
	}

	/**
	 * 
	 * @param topKWithScorePrimDim
	 * @param locationGridID
	 * @param verbose
	 * @param locIDSupportedActIDsMap
	 * @param pfNamePhrase
	 *            for writing log file
	 * @return
	 */
	private static LinkedHashMap<String, Double> filterActsOnOneLocation(
			LinkedHashMap<String, Double> topKWithScorePrimDim, Integer locationGridID, boolean verbose,
			TreeMap<Integer, TreeSet<Integer>> locIDSupportedActIDsMap, String pfNamePhrase)
	{
		// boolean verbose = true;
		int countOfFilteredOutActs = 0;
		StringBuilder sb = new StringBuilder();

		sb.append("Inside filterActsOnOneLocation:- locationGridID =" + locationGridID + "\n");

		TreeSet<Integer> actIDsSupportedByGivenLocationGridID = locIDSupportedActIDsMap.get(locationGridID);
		sb.append("actIDsSupportedByGivenLocationGridID.sizer() = " + actIDsSupportedByGivenLocationGridID.size()
				+ " actIDs supported =\n" + actIDsSupportedByGivenLocationGridID + "\n");

		LinkedHashMap<String, Double> resScoreToReturn = new LinkedHashMap<>();

		for (Entry<String, Double> pd : topKWithScorePrimDim.entrySet())
		{
			double primDimScore = pd.getValue();
			Integer actID = Integer.valueOf(pd.getKey());

			if (actIDsSupportedByGivenLocationGridID.contains(actID))
			{
				sb.append("actID " + actID + " supported by locationGridID=" + locationGridID + "\n");
				resScoreToReturn.put(String.valueOf(actID), primDimScore);
			}
			else
			{
				countOfFilteredOutActs += 1;
			}
		}

		if (verbose)
		{
			sb.append("topKWithScorePrimDim = " + topKWithScorePrimDim + "\n");
			sb.append("resScoreToReturn = " + resScoreToReturn + "\n");
			sb.append("Exiting weightedCombine\n");
			System.out.println(sb.toString());
		}

		System.out.println("countOfFilteredOutActs = " + countOfFilteredOutActs);

		// if (keepPrimDimRecommScore)
		{
			WToFile.appendLineToFileAbs(countOfFilteredOutActs + "\n",
					pathToWriteLog + pfNamePhrase + "filterActsOnOneLocationCountOfFilteredOutActs.csv");
		}
		return resScoreToReturn;
	}

	/**
	 * 
	 * @param topKWithScorePrimDim
	 * @param locGridAvailabilityScoreForEachActID
	 * @param alpha
	 * @param strongFilter
	 * @param verbose
	 * @param keepPrimDimRecommScore
	 *            whether to use combined score or retain primary dimension score.
	 * @return
	 */
	private static LinkedHashMap<String, Double> weightedCombine(LinkedHashMap<String, Double> topKWithScorePrimDim,
			LinkedHashMap<String, Double> locGridAvailabilityScoreForEachActID, double alpha, boolean strongFilter,
			boolean verbose, boolean keepPrimDimRecommScore)
	{
		// boolean verbose = true;
		int countOfFilteredOutActs = 0;
		StringBuilder sb = new StringBuilder();
		if ((alpha > 1 || alpha < 0) && keepPrimDimRecommScore == false)
		{
			PopUps.showError("Error: alpha out of range = " + alpha + " strongFilter= " + strongFilter);
			System.exit(-1);
		}

		sb.append("Inside weightedCombine:- alpha =" + alpha + " strongFilter= " + strongFilter + "\n");
		double beta = (1 - alpha);

		LinkedHashMap<String, Double> normPrimDimWithScore = minMaxNormalise(topKWithScorePrimDim);
		LinkedHashMap<String, Double> normLocGridScore = minMaxNormalise(locGridAvailabilityScoreForEachActID);
		LinkedHashMap<String, Double> combinedScoreToReturn = new LinkedHashMap<>();

		for (Entry<String, Double> pd : normPrimDimWithScore.entrySet())
		{
			double primDimScore = pd.getValue();
			String actID = pd.getKey();
			Double locAvailScore = normLocGridScore.get(actID);

			if (locAvailScore == null)// no location grids support that act
			{
				beta = 0;
				if (strongFilter)// remove that act from list itself
				{
					countOfFilteredOutActs += 1;
					continue;// skip this actID
				}
			}

			double combinedScore = primDimScore * alpha + locAvailScore * beta;
			sb.append("actID = " + actID + "\n");
			sb.append("primDimScore = " + primDimScore + " alpha = " + alpha + " locAvailScore = " + locAvailScore
					+ " beta = " + beta + "combinedScore = " + combinedScore + "\n\n");

			if (keepPrimDimRecommScore)
			{
				combinedScoreToReturn.put(actID, primDimScore);
			}
			else
			{
				combinedScoreToReturn.put(actID, combinedScore);
			}
		}

		if (verbose)
		{
			sb.append("normPrimDimWithScore = " + normPrimDimWithScore + "\n");
			sb.append("normLocGridScore = " + normLocGridScore + "\n");
			sb.append("combinedScoreToReturn = " + combinedScoreToReturn + "\n");
			sb.append("Exiting weightedCombine\n");
			System.out.println(sb.toString());
		}

		System.out.println("countOfFilteredOutActs = " + countOfFilteredOutActs);

		if (keepPrimDimRecommScore)
		{
			WToFile.appendLineToFileAbs(countOfFilteredOutActs + "\n", pathToWriteLog + "countOfFilteredOutActs.csv");
		}
		return combinedScoreToReturn;
	}

	/**
	 * 
	 * @param topKWithScorePrimDim
	 * @param topKWithScoreSecDim
	 * @param actIDLocGridIDsMap
	 * @param verbose
	 * @return {recommPrimDimID, secDimScoreForThisRecommendedID}
	 */
	private static LinkedHashMap<String, Double> getLocGridAvailabilityScoreForEachActID(
			LinkedHashMap<String, Double> topKWithScorePrimDim, LinkedHashMap<String, Double> topKWithScoreSecDim,
			TreeMap<Integer, TreeSet<Integer>> actIDLocGridIDsMap, boolean verbose)
	{
		// Set<Integer> setOfRecommLocGridIDs = topKWithScoreSecDim.entrySet().stream()
		// .map(e -> Integer.valueOf(e.getKey())).collect(Collectors.toSet());
		// System.out.println("setOfRecommLocGridIDs = \n" + setOfRecommLocGridIDs);

		// {recommended actID,score}
		LinkedHashMap<String, Double> resultToReturn = new LinkedHashMap<>();

		StringBuilder sb = new StringBuilder();

		sb.append("topKWithScorePrimDim = " + topKWithScorePrimDim + "\n");
		sb.append("topKWithScoreSecDim = " + topKWithScoreSecDim + "\n");

		for (Entry<String, Double> entryPrimaryDim : topKWithScorePrimDim.entrySet())
		{
			// actID
			Integer recommPrimDimID = Integer.valueOf(entryPrimaryDim.getKey());
			TreeSet<Integer> locGridIDsForThisAct = actIDLocGridIDsMap.get(recommPrimDimID);
			sb.append("recommPrimDimID (actID) = " + recommPrimDimID + " locGridIDsForThisAct= " + locGridIDsForThisAct
					+ "\n");
			// locGridScore
			double secDimScoreForThisRecommendedID = 0;

			for (Entry<String, Double> entrySecDim : topKWithScoreSecDim.entrySet())
			{
				Integer locGridID = Integer.valueOf(entrySecDim.getKey());
				if (locGridIDsForThisAct.contains(locGridID))
				{
					sb.append(
							"Matched locGridID: " + locGridID + " with recomm score: " + entrySecDim.getValue() + "\n");
					secDimScoreForThisRecommendedID += entrySecDim.getValue();
				}
			}
			resultToReturn.put(String.valueOf(recommPrimDimID), secDimScoreForThisRecommendedID);
			// System.out.println("" + setOfRecommLocGridIDs.contains(locGridIDsForThisAct));
		}

		resultToReturn = (LinkedHashMap<String, Double>) ComparatorUtils.sortByValueDescNoShuffle(resultToReturn);

		sb.append("resultToReturn= " + resultToReturn + "\n");
		if (verbose)
		{
			System.out.println(sb.toString());
		}
		return resultToReturn;
	}

	private static LinkedHashMap<String, Double> trimToTopN(LinkedHashMap<String, Double> topKWithScorePrimDim, int i)
	{
		LinkedHashMap<String, Double> res = new LinkedHashMap<>();

		int count = 0;
		for (Entry<String, Double> e : topKWithScorePrimDim.entrySet())
		{
			res.put(e.getKey(), e.getValue());
			count += 1;
			if (count == i)
			{
				break;
			}
		}
		return res;
	}

	private static LinkedHashMap<String, Double> minMaxNormalise(LinkedHashMap<String, Double> topKWithScorePrimDim)
	{
		boolean verbose = false;
		double max = topKWithScorePrimDim.entrySet().stream().mapToDouble(e -> e.getValue()).max().getAsDouble();
		double min = topKWithScorePrimDim.entrySet().stream().mapToDouble(e -> e.getValue()).min().getAsDouble();
		LinkedHashMap<String, Double> res = new LinkedHashMap<>();

		for (Entry<String, Double> e : topKWithScorePrimDim.entrySet())
		{
			res.put(e.getKey(), StatsUtils.minMaxNorm(e.getValue(), max, min));
		}

		if (verbose)
		{
			System.out.println("\noriginal=\n" + topKWithScorePrimDim);
			System.out.println("\nnormalised=\n" + res);
		}
		return res;
	}

	private static LinkedHashMap<String, Double> getTopKWithScore(String topKFullString)
	{
		String[] topKStringSplitted = RegexUtils.patternDoubleUnderScore.split(topKFullString);
		LinkedHashMap<String, Double> topKWithScore = new LinkedHashMap<>();

		for (int m = 1; m < topKStringSplitted.length; m++)// skip first emptu
		{
			String[] splittedVal = RegexUtils.patternColon.split(topKStringSplitted[m]);
			topKWithScore.put(splittedVal[0], Double.valueOf(splittedVal[1]));
		}

		if (false)
		{
			System.out.println("arrayTopK.get(i).get(j) = " + topKFullString);
			System.out.println("topKStringSplitted = " + Arrays.asList(topKStringSplitted));
			System.out.println("topKWithScore = " + topKWithScore);
		}
		return topKWithScore;
	}

	/**
	 * 
	 * @param actIDLocGridIDsMap
	 * @param pathToWrite
	 * @param writeToFile
	 * @return
	 */
	private TreeMap<Integer, TreeSet<Integer>> getLocIDSupportedActIDMap(
			TreeMap<Integer, TreeSet<Integer>> actIDLocGridIDsMap, String pathToWrite, boolean writeToFile)
	{
		TreeMap<Integer, TreeSet<Integer>> res = new TreeMap<>();

		for (Entry<Integer, TreeSet<Integer>> actEntry : actIDLocGridIDsMap.entrySet())
		{
			Integer actID = actEntry.getKey();
			TreeSet<Integer> locGridIDsForThisActID = actEntry.getValue();

			for (Integer gridID : locGridIDsForThisActID)
			{
				TreeSet<Integer> actIDsForThisGridID = res.get(gridID);
				if (actIDsForThisGridID == null)
				{
					actIDsForThisGridID = new TreeSet<>();
				}
				actIDsForThisGridID.add(actID);
				res.put(gridID, actIDsForThisGridID);
			}
		}

		if (writeToFile)
		{
			if (true)
			{
				StringBuilder sb1 = new StringBuilder("ActID,#UniqueLocGridIDs,UniqueLocGridIDs\n");
				for (Entry<Integer, TreeSet<Integer>> actEntry : actIDLocGridIDsMap.entrySet())
				{
					sb1.append(actEntry.getKey() + "," + actEntry.getValue().size() + ",");
					sb1.append(actEntry.getValue().stream().map(v -> String.valueOf(v)).collect(Collectors.joining(","))
							+ "\n");
				}
				WToFile.writeToNewFile(sb1.toString(), pathToWrite + "actIDUniqueLocGridIDsMap.csv");

				StringBuilder sb2 = new StringBuilder("GridID,#UniqueActIDs,UniqueActIDs\n");
				for (Entry<Integer, TreeSet<Integer>> gridEntry : res.entrySet())
				{
					sb2.append(gridEntry.getKey() + "," + gridEntry.getValue().size() + ",");
					sb2.append(
							gridEntry.getValue().stream().map(v -> String.valueOf(v)).collect(Collectors.joining(","))
									+ "\n");
				}
				WToFile.writeToNewFile(sb2.toString(), pathToWrite + "gridIDUniqueActIDsMap.csv");
			}
			else
			{
				StringBuilder sb1 = new StringBuilder("ActID,#UniqueLocGridIDs \n");
				for (Entry<Integer, TreeSet<Integer>> actEntry : actIDLocGridIDsMap.entrySet())
				{
					sb1.append(actEntry.getKey() + "," + actEntry.getValue().size() + "\n");
				}
				WToFile.writeToNewFile(sb1.toString(), pathToWrite + "actIDUniqueLocGridIDsMapR.csv");

				StringBuilder sb2 = new StringBuilder("GridID,#UniqueActIDs\n");
				for (Entry<Integer, TreeSet<Integer>> gridEntry : res.entrySet())
				{
					sb2.append(gridEntry.getKey() + "," + gridEntry.getValue().size() + "\n");
				}
				WToFile.writeToNewFile(sb2.toString(), pathToWrite + "gridIDUniqueActIDsMapR.csv");
			}

		}
		return res;
	}

	/**
	 * 
	 * @param fileNamePhrase
	 * @param timeCategory
	 * @param arrayMeta
	 * @param arrayRecommendedSeq
	 * @param arrayActualSeq
	 * @param levelAtWhichToMatch
	 * @return ArrayList<ArrayList<ArrayList<Integer>>> arrayDirectAgreements
	 */
	private static ArrayList<ArrayList<ArrayList<Integer>>> computeDirectAgreements(
			ArrayList<ArrayList<String>> arrayMeta, ArrayList<ArrayList<String>> arrayRecommendedSeq,
			ArrayList<ArrayList<String>> arrayActualSeq)
	{
		ArrayList<ArrayList<ArrayList<Integer>>> arrayDirectAgreements = new ArrayList<>();

		try
		{
			System.out.println("size of meta array=" + arrayMeta.size() + "     size of arrayRecommendedSeq array="
					+ arrayRecommendedSeq.size() + "   size of arrayActualSeq array=" + arrayActualSeq.size());

			for (int i = 0; i < arrayMeta.size(); i++) // iterating over users (or rows)
			{
				ArrayList<String> currentMetaLineArray = arrayMeta.get(i);
				int countOfRecommendationTimesConsidered = 0; // =count of meta entries considered
				// ArrayList<ArrayList<Integer>> directAgreementsForThisUser = new ArrayList<>();

				for (int j = 0; j < currentMetaLineArray.size(); j++)// iterating over RTs (or columns)
				{
					// int hourOfTheDay = EvaluationSeq.getHourFromMetaString(currentMetaLineArray.get(j));

					// if (DateTimeUtils.getTimeCategoryOfDay(hourOfTheDay).equalsIgnoreCase(timeCategory)
					// || timeCategory.equals("All")) // TODO check why ALL
					{
						countOfRecommendationTimesConsidered++;

						String[] splittedActualSequence = RegexUtils.patternGreaterThan
								.split(arrayActualSeq.get(i).get(j));

						String[] splittedRecommSequence = RegexUtils.patternGreaterThan
								.split(arrayRecommendedSeq.get(i).get(j));

						if (splittedActualSequence.length != splittedRecommSequence.length)
						{
							PopUps.printTracedErrorMsg(
									"splittedActualSequence.length != splittedRecommSequence.length");
							// System.err.println(PopUps.getTracedErrorMsg("splittedActualSequence.length !=
							// splittedRecommSequence.length"));
						}
						ArrayList<Integer> directAgreement = new ArrayList<>();

						if (VerbosityConstants.verboseEvaluationMetricsToConsole)
						{
							System.out.print("\tsplittedRecomm = ");
						}
						for (int y = 0; y < splittedActualSequence.length; y++)
						{
							// removing score
							String splittedRecommY[] = RegexUtils.patternColon.split(splittedRecommSequence[y]);

							if (VerbosityConstants.verboseEvaluationMetricsToConsole)
							{// Suppressing this output since 16 Nov 2017
								System.out.print(">" + splittedRecommY[0]);// + "levelAtWhichToMatch = " +
																			// levelAtWhichToMatch);
							}

							if (splittedActualSequence[y] == null)
							{
								System.out.println("splittedActualSequence");
							}
							// if (isAgree(splittedActualSequence[y], splittedRecommY[0], levelAtWhichToMatch))//
							// splittedActualSequence[y].equals(splittedRecomm[0]))
							// {
							// if (VerbosityConstants.verboseEvaluationMetricsToConsole)
							// {
							// System.out.print("Eureka!");
							// }
							// directAgreement.add(1);
							// }
							// else
							// {
							// directAgreement.add(0);
							// }
						}
						// System.out.println();
						// directAgreementsForThisUser.add(directAgreement);

						if (VerbosityConstants.verbose)
						{
							System.out.println("\tarrayRecommendedSeq=" + arrayRecommendedSeq.get(i).get(j));
							System.out.println("\tarrayActualSeq string =" + arrayActualSeq.get(i).get(j));
							System.out.println("\tdirectAgreement" + directAgreement);
							System.out.println("\n-------------------");
						}
					} // end of if for this time categegory
				} // end of current line array
					/// arrayDirectAgreements.add(directAgreementsForThisUser);
					// bwRR.write("\n");
			} // end of loop over user
				// bwRR.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return arrayDirectAgreements;
	}
}
