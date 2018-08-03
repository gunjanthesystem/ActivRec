package org.activity.postfilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.constants.DomainConstants;
import org.activity.constants.PathConstants;
import org.activity.constants.VerbosityConstants;
import org.activity.evaluation.EvaluationSeq;
import org.activity.io.Serializer;
import org.activity.io.WToFile;
import org.activity.objects.Triple;
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
		String pathToReadRecommendationResultsFrom = "./dataWritten/JUL26ED1.0AllActsFDStFilter0hrs100RTVNoTTFilter/";// 100R/";
		// "/run/media/gunjan/BackupVault/GOWALLA/GowallaResults/JUL26ED1.0AllActsFDStFilter0hrs100RTV500PDNTh50SDNTh/100R/";
		new PostFilter1(pathToReadRecommendationResultsFrom);
	}

	/**
	 * Note: the post filtered recommendations are written back to the same results path.
	 * 
	 * @param commonPathToReadFrom
	 */
	public PostFilter1(String commonPathToReadFrom)
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
		PathConstants.intialise(Constant.For9kUsers);
		Constant.initialise(outerPath, "gowalla1", PathConstants.pathToSerialisedCatIDsHierDist,
				PathConstants.pathToSerialisedCatIDNameDictionary, PathConstants.pathToSerialisedLocationObjects,
				PathConstants.pathToSerialisedUserObjects, PathConstants.pathToSerialisedGowallaLocZoneIdMap);
		// String[] dimensionPhrases = { "", "SecDim" };

		double[] muArray = Constant.getMatchingUnitArray(Constant.lookPastType, Constant.altSeqPredictor);
		for (double mu : muArray)
		{

			Constant.setCommonPath(outerPath + "MatchingUnit" + (int) (mu) + ".0/");
			String pathToReadResults = Constant.getCommonPath();
			System.out.println("pathToReadResults= " + pathToReadResults);

			int firstIndex = 0;
			String dimensionPhrase = "";
			Triple<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> readArraysPredSeq = EvaluationSeq
					.readDataMetaActualTopK(pathToReadResults,
							"dataRankedRecommendation" + dimensionPhrase + "WithScores" + firstIndex + ".csv",
							"dataActual" + dimensionPhrase + firstIndex + ".csv", pathToReadResults, verbose);

			ArrayList<ArrayList<String>> arrayMeta = readArraysPredSeq.getFirst();
			ArrayList<ArrayList<String>> arrayRecommended = readArraysPredSeq.getThird();
			ArrayList<ArrayList<String>> arrayActual = readArraysPredSeq.getSecond();

			dimensionPhrase = "SecDim";
			Triple<ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>, ArrayList<ArrayList<String>>> readArraysPredSeqSecDim = EvaluationSeq
					.readDataMetaActualTopK(pathToReadResults,
							"dataRankedRecommendation" + dimensionPhrase + "WithScores" + firstIndex + ".csv",
							"dataActual" + dimensionPhrase + firstIndex + ".csv", pathToReadResults, verbose);

			// ArrayList<ArrayList<String>> arrayMetaSecDim = readArraysPredSeqSecDim.getFirst();
			ArrayList<ArrayList<String>> arrayRecommendedSecDim = readArraysPredSeqSecDim.getThird();
			ArrayList<ArrayList<String>> arrayActualSecDim = readArraysPredSeqSecDim.getSecond();

			doPostFilterings(arrayMeta, arrayRecommended, arrayActual, arrayRecommendedSecDim, arrayActualSecDim,
					actIDLocGridIDsMap, locIDSupportedActIDsMap, verbose);

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
	private static ArrayList<ArrayList<ArrayList<Integer>>> doPostFilterings(ArrayList<ArrayList<String>> arrayMeta,
			ArrayList<ArrayList<String>> arrayTopK, ArrayList<ArrayList<String>> arrayActual,
			ArrayList<ArrayList<String>> arrayTopKSecDim, ArrayList<ArrayList<String>> arrayActualSecDim,
			TreeMap<Integer, TreeSet<Integer>> actIDLocGridIDsMap,
			TreeMap<Integer, TreeSet<Integer>> locIDSupportedActIDsMap, boolean verbose)
	{
		ArrayList<ArrayList<ArrayList<Integer>>> arrayDirectAgreements = new ArrayList<>();
		int startUserIndex = 0;// TODO 0

		StringBuilder sbPDWtdAlphaPF = new StringBuilder();
		StringBuilder sbPDFltr_on_TopKLocsPF = new StringBuilder();
		StringBuilder sbPDFltr_on_ActualLocPF = new StringBuilder();
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

				for (int j = startUserIndex; j < currentMetaLineArray.size(); j++)// iterating over RTs (or columns)
				{

					countOfRecommendationTimesConsidered++;
					String actual = arrayActual.get(i).get(j);
					String actualSecDim = arrayActualSecDim.get(i).get(j);

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

					LinkedHashMap<String, Double> recommPDFltr_on_TopKLocsPF = filterKeepingPrimDimScore(
							topKWithScorePrimDim, locGridAvailabilityScoreForEachActID, true, verbose);
					recommPDFltr_on_TopKLocsPF.entrySet().stream()
							.forEachOrdered(e -> sbPDFltr_on_TopKLocsPF.append("__" + e.getKey()));
					sbPDFltr_on_TopKLocsPF.append(",");

					LinkedHashMap<String, Double> recommPDFltr_on_ActualLocPF = filterActsOnOneLocation(
							topKWithScorePrimDim, Integer.valueOf(actualSecDim), verbose, locIDSupportedActIDsMap,
							"Fltr_on_ActualLocPF");
					recommPDFltr_on_ActualLocPF.entrySet().stream()
							.forEachOrdered(e -> sbPDFltr_on_ActualLocPF.append("__" + e.getKey()));
					sbPDFltr_on_ActualLocPF.append(",");

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
				sbPDFltr_on_Top1LocPF.append("\n");
			} // end of loop over user

			// WtdAlphaPF, Fltr_on_TopKLocsPF, Fltr_on_ActualLocPF,Fltr_on_Top1Loc
			WToFile.writeToNewFile(sbPDWtdAlphaPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationWtdAlphaPFWithoutScores0.csv");
			WToFile.writeToNewFile(sbPDFltr_on_TopKLocsPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationFltr_on_TopKLocsPFWithoutScores0.csv");
			WToFile.writeToNewFile(sbPDFltr_on_ActualLocPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationFltr_on_ActualLocPFWithoutScores0.csv");
			WToFile.writeToNewFile(sbPDFltr_on_Top1LocPF.toString(),
					Constant.getCommonPath() + "dataRankedRecommendationFltr_on_Top1LocWithoutScores0.csv");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return arrayDirectAgreements;
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
