package org.activity.controller;

import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.activity.constants.Constant;
import org.activity.constants.DomainConstants;
import org.activity.constants.Enums;
import org.activity.constants.Enums.AltSeqPredictor;
import org.activity.constants.Enums.LookPastType;
import org.activity.constants.Enums.TypeOfCandThreshold;
import org.activity.constants.PathConstants;
import org.activity.evaluation.EvalMetrics;
import org.activity.evaluation.EvaluationSeq;
import org.activity.evaluation.ResultsDistributionEvaluation;
import org.activity.io.CSVUtils;
import org.activity.io.ReadingFromFile;
import org.activity.io.WToFile;
import org.activity.nn.LSTMCharModelling_SeqRecJun2018;
import org.activity.objects.Triple;
import org.activity.postfilter.PostFilter1;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.DateTimeUtils;
import org.activity.util.PerformanceAnalytics;
import org.activity.util.Searcher;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.nd4j.jita.conf.CudaEnvironment;
//import org.nd4j.jita.conf.Configuration;
//import org.nd4j.jita.conf.CudaEnvironment;
import org.nd4j.linalg.factory.Nd4j;

public class SuperController
{
	public static void main2(String args[])
	{
		String[] files = { "MedianAllPerDirectTopKAgreementsL1_.csv", "MedianAllPerDirectTopKAgreements_.csv",
				"MeanAllPerDirectTopKAgreementsL1_.csv", "MeanAllPerDirectTopKAgreements_.csv" };

		for (String s : files)
		{
			ReadingFromFile.concat18Jan(s);
		}
	}

	/**
	 * Check is the files with specific string in names have lines containing the given string
	 * 
	 * @since 16 Feb 2018
	 */
	public static void searchContentInFile()
	{
		String rootPathToSearch = "./dataWritten/Feb15NCount_5Day_NN500MedRepCinsNormEDAlpha0.6FiltrdByCurrActTime1hr/";
		String contentToMatch = "EDAlpha:";
		String fileNamePatternToSearch = "Config.csv";
		String absFileNameOfMatchedLinesToWrite = rootPathToSearch + "LinesContaining.txt";

		System.out.println("absFileNameOfMatchedLinesToWrite = " + absFileNameOfMatchedLinesToWrite);

		Triple<Set<Path>, Set<Path>, String> res = Searcher.search2(rootPathToSearch, fileNamePatternToSearch,
				contentToMatch, absFileNameOfMatchedLinesToWrite);

		System.out.println("Files with matching filenames:" + res.getFirst().toString());
		System.out.println("Files with matching filenames and string in line:" + res.getSecond().toString());
		System.out.println("log:" + res.getThird().toString());
	}

	public static void concat18Jan(String filename)
	{
		String commonPath = "./dataWritten/Jan3_Sampling_AKOM1DayOrder1/";
		ArrayList<String> fileNamesToConcactenate = new ArrayList<String>();
		int sampleIDStart = 0;
		int sampleIDEnd = 8;

		for (int i = sampleIDStart; i <= sampleIDEnd; i++)
		{
			// String userID = String.format("%03d", i);
			String fileName = commonPath + "Sample" + i + "/" + filename;
			fileNamesToConcactenate.add(fileName);
		}
		CSVUtils.concatenateCSVFiles(fileNamesToConcactenate, true, commonPath + filename);
	}

	public static void main__(String args[])
	{
		String absFileNameWithRootPathsToSearch = "./dataWritten/clarityPathsToCheckForErrMay10.csv";
		String commonPathToWrite = "./dataWritten/";
		int expectedNumOfColumnsInRoothPathFile = 3;
		Searcher.searchErrorExceptsInPaths(absFileNameWithRootPathsToSearch, commonPathToWrite,
				expectedNumOfColumnsInRoothPathFile);
	}

	private static List<Integer> findCudaDevices()
	{
		System.out.println("Inside findCudaDevices");
		List<Integer> availableDevices = new ArrayList<>();
		int cnt = Nd4j.getAffinityManager().getNumberOfDevices();
		// Nd4j.getAffinityManager().device
		if (cnt == 0)
		{
			System.err.println("No CUDA devices were found in system");
		}
		else
		{
			System.out.println("getNumberOfDevices =" + cnt);
		}

		for (int i = 0; i < cnt; i++)
		{
			availableDevices.add(i);
		}
		return availableDevices;
	}

	public static void setupCUDAEnviron()
	{
		// see
		// https://github.com/deeplearning4j/nd4j/blob/78c96a8a3f7aab948af84902c144e1b2123c4436/nd4j-backends/nd4j-backend-impls/nd4j-cuda/src/test/java/jcuda/jcublas/ops/DevicesTests.java#L18-L18
		// https://github.com/deeplearning4j/deeplearning4j/issues/2374
		org.nd4j.jita.conf.Configuration config = CudaEnvironment.getInstance().getConfiguration();
		config.allowCrossDeviceAccess(true);
		config.allowMultiGPU(true);

		System.out.println("availableDevices = " + config.getAvailableDevices());
		System.out.println("bannedDevices = " + config.getBannedDevices());
		System.out.println("isCrossDeviceAccessAllowed = " + config.isCrossDeviceAccessAllowed());
		System.out.println("isForcedSingleGPU = " + config.isForcedSingleGPU());
		List<Integer> cudaDevices = findCudaDevices();
		System.out.println("cudaDevices = " + cudaDevices);

		if (false)// not relevant as masking GPUs at environment level
		{
			int numOfGPUsToUse = 2;
			int numOfGPUsSelected = 0;

			for (int dev : cudaDevices)
			{
				if (numOfGPUsSelected < numOfGPUsToUse)
				{
					config.useDevice(dev);
					numOfGPUsSelected += 1;
				}
				else
				{
					// System.out.println("Banning device: " + dev);
					config.banDevice(dev);
				}
			}
		}
		// config.useDevices(0);
		// config.banDevice(1, 2, 3);
		System.out.println("availableDevices = " + config.getAvailableDevices());
		System.out.println("bannedDevices = " + config.getBannedDevices());
		System.out.println("Device list " + config.getAvailableDevices().toString());

		// Configuration cudaConfig = CudaEnvironment.getInstance().getConfiguration();
		// cudaConfig.allowMultiGPU(false).allowCrossDeviceAccess(false).useDevices(1);
		// CudaEnvironment.getInstance().getConfiguration().GPU

		// allow large cache: upto 6GiB, see: https://deeplearning4j.org/gpu
		config.setMaximumDeviceCacheableLength(1024 * 1024 * 1024L).setMaximumDeviceCache(10L * 1024 * 1024 * 1024L)
				.setMaximumHostCacheableLength(1024 * 1024 * 1024L).setMaximumHostCache(10L * 1024 * 1024 * 1024L);

		System.out.println("Exiting setupCUDAEnvrion.");// : printing environ:-\n" + printEnvironmentInformation());
	}

	/**
	 * <ul>
	 * <li>Set default timezone</li>
	 * <li>Set path constants</li>
	 * <li>Set constants</li>
	 * </ul>
	 * 
	 * @param whoCalled
	 * @param for9kUsers
	 * @param databaseName
	 * @return
	 * @since 12 Oct 2018
	 */
	public static String initializeConstants(String whoCalled, boolean for9kUsers, String databaseName)
	{
		String message = "-- >> StarterKit started by " + whoCalled + "at: " + LocalDateTime.now() + "\n";
		System.out.println("For this experiment: Java Version:" + System.getProperty("java.version"));
		System.out.println(
				PerformanceAnalytics.getHeapInformation() + "\nRunning experiments for database: " + databaseName);
		TimeZone.setDefault(TimeZone.getTimeZone("UTC")); // added on April 21, 2016
		Constant.setDefaultTimeZone("UTC");
		PathConstants.intialise(for9kUsers, databaseName);
		Constant.initialise(databaseName, PathConstants.pathToSerialisedCatIDsHierDist,
				PathConstants.pathToSerialisedCatIDNameDictionary, PathConstants.pathToSerialisedLocationObjects,
				PathConstants.pathToSerialisedUserObjects, PathConstants.pathToSerialisedGowallaLocZoneIdMap, true);

		if (Constant.altSeqPredictor.equals(Enums.AltSeqPredictor.RNN1))
		{
			setupCUDAEnviron();
		}

		return message;
	}

	public static void mainForFindingWtsForFeatures(String args)
	{
		// double minValue = 0, maxValue = 1, stepSize = 0.25;
		double possibleVals[] = { 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1 };
		for (int iterationCountFeatWtSearch = 0; iterationCountFeatWtSearch < 5; iterationCountFeatWtSearch++)
		{// randomly generate index and select from the possibleVals
			CustodianOfFeatWts.givenWtActivityName = 2d;
			CustodianOfFeatWts.givenWtStartTime = possibleVals[StatsUtils.randomInRange(0, possibleVals.length)];
			CustodianOfFeatWts.givenWtDuration = possibleVals[StatsUtils.randomInRange(0, possibleVals.length)];
			CustodianOfFeatWts.givenWtDistanceTravelled = possibleVals[StatsUtils.randomInRange(0,
					possibleVals.length)];
			CustodianOfFeatWts.givenWtEndGeo = possibleVals[StatsUtils.randomInRange(0, possibleVals.length)];
			CustodianOfFeatWts.givenWtAvgAltitude = possibleVals[StatsUtils.randomInRange(0, possibleVals.length)];
		}
		// boolean random = true;
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String args[])
	{
		superControllerMain(args);
	}

	/**
	 * <h2>In this method:</h2>
	 * 
	 * <ul>
	 * <li>setup CUDAEnviron if RNN is to used</li>
	 * <li>set sampledUserIndices (in groups of 100) to be used</li>
	 * <li>set of EDAlphas to be use</li>
	 * <li>Can execute multiple experiments of the same experiment</li>
	 * <li>Make a call to main0 for each iteration, each EDAlpha, each set of randomly sampled 100 users</li>
	 * </ul>
	 * 
	 * @param args
	 */
	public static void superControllerMain(String args[])// _importantMay10
	{
		String databaseName = Constant.getDatabaseName();
		initializeConstants("SuperController", Constant.For9kUsers, databaseName);
		boolean doRecommendation = true;
		boolean doEvaluation = true;
		boolean hasMUs = true;

		// searchContentInFile();
		// sftp://claritytrec.ucd.ie/home/gunjankumar/SyncedWorkspace/Aug2Workspace/GeolifeReloaded2_1_cleaned
		// cleanUpSpace("./dataWritten/July23_incomplete/", 0.9);
		// runAllAKOMExperiments();
		// $$cleanUpSpace("./dataWritten/Feb27ED0.5DurFPDistFPStFilter3hrs", 0.97);//
		// $$cleanUpSpace("./dataWritten/xyz", 0.97, "Raw.csv");//
		// cleanUpSpace("./dataWritten/Feb28ED0.75DurFPDistFPStFilter3hrs ", 0.97);//
		// $cleanUpSpace("/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWrittenNGramBaselineForUserNumInvestigation/",0.9);
		// cleanUpSpace("/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWrittenClosestTimeBaseline/", 0.9);
		// cleanUp(new String[] {});
		// sampledUserIndicesSets renamed to setOfSampledUserIndicesForExp

		// Select set of sampled user indices to run for
		// when runForAllUsersAtOnce, we are not using sample user indices, hence we need to run it only once.
		String[] setOfSampledUserIndicesForExp = null;

		if (databaseName.equals("gowalla1"))
		{
			setOfSampledUserIndicesForExp = Constant.runForAllUsersAtOnce
					? Arrays.copyOfRange(PathConstants.pathToSetsOfRandomlySampled100Users, 0, 1)
					: Arrays.copyOfRange(PathConstants.pathToSetsOfRandomlySampled100Users, 0, 1);
		}

		// Select set of ED Alphas to run for
		double[] EDAlphas = Constant.EDAlphas;// Arrays.copyOfRange(Constant.EDAlphas, 0, 1);
		// num of times same exp to be repeated to smooth out variation due to ties, randomness, etc.
		final int numOfIterSameExp = 1;
		System.out.println("setOfSampledUserIndicesForExp = " + Arrays.toString(setOfSampledUserIndicesForExp)
				+ "\nEDAlphas = " + Arrays.toString(EDAlphas) + "\nnumOfIterSameExp = " + numOfIterSameExp);

		// added on 29 July 2018 when running for multiple iterations
		for (int iteration = 0; iteration < numOfIterSameExp; iteration++)
		{
			for (double edAlphaForAnExp : EDAlphas)
			{
				if (Constant.useToyTimelines)
				{// PopUps.showMessage("here inside toy!");
					// Note: sampledUserIndicesSets are irrelavant for the toy timelines
					runExperimentForGivenUsersAndConfig(setOfSampledUserIndicesForExp[0], edAlphaForAnExp, "",
							doRecommendation, doEvaluation, hasMUs);
				}
				else
				{
					if (databaseName.equals("gowalla1"))
					{
						for (String sampledUserIndicesSet : setOfSampledUserIndicesForExp)
						{
							runExperimentForGivenUsersAndConfig(sampledUserIndicesSet, edAlphaForAnExp, "",
									doRecommendation, doEvaluation, hasMUs);// "iter" +
							// iteration);
							// if (Constant.runForAllUsersAtOnce){break;}// here we are not using sample user indices,
							// hence
							// we need to run it only once.
						}
					}
					else
					{
						runExperimentForGivenUsersAndConfig("", edAlphaForAnExp, "", doRecommendation, doEvaluation,
								hasMUs);// "iter" +
					}
				}
			}
		}

		// if (new File("").isFile())
		{

		}

		// PopUps.showMessage("Exiting main");
		System.exit(0);
	}

	/**
	 * @since Feb 1, 2018
	 */
	public static void runAllAKOMExperiments()
	{
		System.out.println("Java Version:" + System.getProperty("java.version"));
		int orders[] = { 5, 4, 3, 2, 1 };
		int numOfDays[] = { 1, 5 };

		String rootPath = "./dataWritten/Feb1_AKOM_";

		for (int order : orders)
		{
			for (int numOfDay : numOfDays)
			{
				String commonPath = rootPath + numOfDay + "DayFilter_Order" + order;
				WToFile.createDirectory(commonPath);
				commonPath += "/";

				Constant.setAKOMHighestOrder(order);
				Constant.setRecentDaysInTrainingTimelines(numOfDay);

				runExperiment(commonPath, false, true, true, "gowalla1", "HJEditDistance");
				// cleanUpSpace(commonPath, 0.80);
				System.out.println("finished for commonPath = " + commonPath);
			}
		}

		System.out.println(" Exiting SuperController");
		// cleanUpSpace("./dataWritten/Aug14Filter500/",0.80);
	}

	/**
	 * Precursor to running experiments for the users in sampledUserIndicesSetFile
	 * <ul>
	 * <li>Sets Constant.pathToRandomlySampledUserIndices, Constant.dynamicEDAlpha, labelForExperimentConfig</li>
	 * <li>call runExperiments for different commonPaths</li>
	 * </ul>
	 * 
	 * @param sampledUserIndicesSetFile
	 * @param EDAlphaForThisExperiment
	 *            >-1 if we want to set in here instead of hardcoding it in the Constant class.
	 * @param iterationLabel
	 * @param doRecommendation
	 * @param doEvaluation
	 * @param hasMUS
	 */
	public static void runExperimentForGivenUsersAndConfig(String sampledUserIndicesSetFile,
			double EDAlphaForThisExperiment, String iterationLabel, boolean doRecommendation, boolean doEvaluation,
			boolean hasMUS)
	{
		Constant.setDynamicPathToRandomlySampledUserIndices(sampledUserIndicesSetFile);
		// if (EDAlphaForThisExperiment > -1)// when EDAlphaForThisExperiment is <=-1, means we do not need to set it
		// here.
		{
			Constant.setDynamicEDAlpha(EDAlphaForThisExperiment);// Constant.setEDAlpha(EDAlphaForThisExperiment);
			System.out.println("SETTING EDAlpha dynamically");
		}
		// else
		// {
		// System.out.println("NOT SETTING EDAlpha dynamically");
		// }
		System.out.println("sampledUserIndicesSetFile=" + sampledUserIndicesSetFile);
		System.out.println("Constant.pathToRandomLySampleUserIndices=" + sampledUserIndicesSetFile);
		System.out.println("Constant.EDAlpha=" + Constant.getDynamicEDAlpha());
		// String[] commonPaths = { "/run/media/gunjan/BackupVault/GOWALLA/GowallaResults/"
		// { "./dataWritten/"+ DateTimeUtils.getMonthDateLabel() + labelForExperimentConfig + iterationLabel + "/" };

		// adding loop to search for best feature weight
		// start of added on 21 Nov
		double possibleVals[] = { 0, 0.2, 0.4, 0.6, 0.8, 1, 1.2, 1.4, 1.6, 1.8, 2 };// 0.2, 0.5 };// 0.1, 0.2, 0.3, 0.4,
																					// 0.5, 0.6, 0.7, 0.8, 0.9, 1 };
		// IntStream.range(0, 11).mapToDouble(i -> i * 0.1);

		int iterationCountFeatWtSearchLimit = Constant.searchForOptimalFeatureWts ? 1000 : 1;
		int repetitionCheckLimit = 10;

		for (int iterationCountFeatWtSearch = 0; iterationCountFeatWtSearch < iterationCountFeatWtSearchLimit; iterationCountFeatWtSearch++)
		{
			if (Constant.searchForOptimalFeatureWts)
			{
				// randomly generate index and select from the possibleVals
				int numOfRepetitions = 0;
				boolean isAlreadyTried = false;
				do // to avoid repeating same configs
				{
					CustodianOfFeatWts.givenWtActivityName = 2d;
					CustodianOfFeatWts.givenWtStartTime = possibleVals[StatsUtils.randomInRange(0,
							possibleVals.length - 1)];
					CustodianOfFeatWts.givenWtDuration = possibleVals[StatsUtils.randomInRange(0,
							possibleVals.length - 1)];
					CustodianOfFeatWts.givenWtDistanceTravelled = possibleVals[StatsUtils.randomInRange(0,
							possibleVals.length - 1)];
					CustodianOfFeatWts.givenWtStartGeo = possibleVals[StatsUtils.randomInRange(0,
							possibleVals.length - 1)];
					CustodianOfFeatWts.givenWtEndGeo = possibleVals[StatsUtils.randomInRange(0,
							possibleVals.length - 1)];
					CustodianOfFeatWts.givenWtAvgAltitude = possibleVals[StatsUtils.randomInRange(0,
							possibleVals.length - 1)];
					isAlreadyTried = CustodianOfFeatWts.isAlreadyTried(CustodianOfFeatWts.toStringWts());
					if (isAlreadyTried)
					{
						numOfRepetitions += 1;
						if (numOfRepetitions > repetitionCheckLimit)// probably no more new config of wts possible
						{
							iterationCountFeatWtSearch = iterationCountFeatWtSearchLimit + 1;// to make this last
																								// iteration
						}
					}
				}
				while (isAlreadyTried);
				CustodianOfFeatWts.addToAlreadyTriedWts(CustodianOfFeatWts.toStringWts());
				System.out.println("searchForOptimalFeatureWts is true: feature weight in iteration "
						+ iterationCountFeatWtSearch + " =" + CustodianOfFeatWts.toStringWts());
			} // end of added on 21 Nov

			String extraLabel = Constant.searchForOptimalFeatureWts ? "Iter" + iterationCountFeatWtSearch : "";

			String dataWrittenFolder =
					// "/run/media/gunjan/BackupVault/GOWALLA/GowallaResults/"
					// "/run/media/gunjan/BackupVault/Geolife2018Results/"
					// "/run/media/gunjan/iiWASDrive/gunjan/GeolifeNov2018/";
					// "/run/media/gunjan/My Passport/GeolifeNov2018/";
					"./dataWritten/";
			// "/mnt/sshServers/theengine/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/";
			// "/run/media/gunjan/MB2016/gunjan/GeolifeNov2018/";
			String commonPath = dataWrittenFolder + Constant.getDatabaseName() + "_"
					+ DateTimeUtils.getMonthDateHourMinLabel()// DateTimeUtils.getMonthDateLabel()//
					+ getLabelForExperimentConfig(sampledUserIndicesSetFile) + iterationLabel + extraLabel + "/";
			WToFile.createDirectoryIfNotExists(commonPath);

			if (false)// TEMP 15 Jan 2019 TODO
			{
				// commonPath = //
				// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/dcu_data_2_JAN11H14M48ED1.0AllActsFDStFilter0hrsRTVNoTTFilterNC/";
				// "/run/media/gunjan/My
				// Passport/GeolifeNov2018/dcu_data_2_DEC26H1M14ED0.5STimeDurStFilter0hrsFEDPerFS_RTVNoTTFilterDY/";
				doRecommendation = false;
				doEvaluation = true;

				// $List<String> pathsToRead = ReadingFromFile.oneColumnReaderString(
				// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsToReadJan15DCUSubset3.csv",
				// "./dataToRead/ResultsToReadJan15GeolifeSubset3Engine.csv",
				// $
				// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsToReadJan15GeolifeSubset3.csv",
				// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/ResultsToReadJan15DCUSubset3.csv",
				// "./dataToRead/ResultsToReadJan11Gowalla1ForEngineEval.csv",
				// ResultsToReadJan15GeolifeSubset3.csv"
				// $ ",", 2, false);
				String databaseName = "dcu_data_2";// dcu_data_2
				Constant.setDatabaseName(databaseName);
				Constant.setDatabaseSpecificConstants(databaseName);
				Constant.setConstantsForTimelineCreation(databaseName);

				String[] pathsToReadPython = {
						// "/mnt/sshServers/theengine/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/dcu_data_2_DEC31H20M25HighDurNoTTFilter/HGRU4RecRecommender/",
						// "/mnt/sshServers/theengine/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/dcu_data_2_DEC31H20M25HighDurNoTTFilter/GRU4RecRecommender/",
						// "/mnt/sshServers/theengine/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/dcu_data_2_DEC31H20M25HighDurNoTTFilter/FPMCRecommender/",
						// "/mnt/sshServers/theengine/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/dcu_data_2_DEC31H20M25HighDurNoTTFilter/Prod2VecRecommender/",
						// "/mnt/sshServers/theengine/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/dcu_data_2_DEC31H20M25HighDurNoTTFilter/PopularityRecommender/"
						// };

						// "/mnt/sshServers/theengine/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/dcu_data_2_DEC31H20M25HighDurNoTTFilter/MixedMarkovRecommender"
						// };
						// "/mnt/sshServers/theengine/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/gowalla1_DEC31H20M29HighDurPNN500coll/HGRU4RecRecommender",
						// "/mnt/sshServers/theengine/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/gowalla1_DEC31H20M29HighDurPNN500coll/PopularityRecommender",
						// "/mnt/sshServers/theengine/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/gowalla1_DEC31H20M29HighDurPNN500coll/Prod2VecRecommender",
						// "/mnt/sshServers/theengine/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/gowalla1_DEC31H20M29HighDurPNN500coll/FPMCRecommender",
						// "/mnt/sshServers/theengine/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/gowalla1_DEC31H20M29HighDurPNN500coll/GRU4RecRecommender",
						// "/mnt/sshServers/theengine/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/gowalla1_DEC31H20M29HighDurPNN500coll/MixedMarkovRecommender"
						// };

						// "/home/gunjan/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/gowalla1_DEC31H20M29HighDurPNN500coll/HGRU4RecRecommender/",
						// "/home/gunjan/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/gowalla1_DEC31H20M29HighDurPNN500coll/PopularityRecommender/",
						// "/home/gunjan/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/gowalla1_DEC31H20M29HighDurPNN500coll/Prod2VecRecommender/",
						// "/home/gunjan/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/gowalla1_DEC31H20M29HighDurPNN500coll/FPMCRecommender/",
						// "/home/gunjan/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/gowalla1_DEC31H20M29HighDurPNN500coll/GRU4RecRecommender/",
						// "/home/gunjan/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/gowalla1_DEC31H20M29HighDurPNN500coll/MixedMarkovRecommender/"
						// };

						"/home/gunjan/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/geolife1_DEC31H20M15HighDurPNN500NoTTFilter/HGRU4RecRecommender/",
						"/home/gunjan/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/geolife1_DEC31H20M15HighDurPNN500NoTTFilter/PopularityRecommender/",
						"/home/gunjan/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/geolife1_DEC31H20M15HighDurPNN500NoTTFilter/Prod2VecRecommender/",
						"/home/gunjan/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/geolife1_DEC31H20M15HighDurPNN500NoTTFilter/FPMCRecommender/",
						"/home/gunjan/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/geolife1_DEC31H20M15HighDurPNN500NoTTFilter/GRU4RecRecommender/",
						"/home/gunjan/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/geolife1_DEC31H20M15HighDurPNN500NoTTFilter/MixedMarkovRecommender/" };

				// geolife1_DEC31H20M15HighDurPNN500NoTTFilter

				// for (String commonPath1 : pathsToReadPython)// pathsToRead)
				{
					// PopUps.showMessage(commonPath1);
					String commonPath1 = "/mnt/sshServers/theengine/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/dcu_data_2_DEC31H20M25HighDurNoTTFilter/MixedMarkovRecommender/";
					// "/mnt/sshServers/theengine/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/gowalla1_DEC31H20M29HighDurPNN500coll/HGRU4RecRecommender/";
					// "/mnt/sshServers/theengine/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/geolife1_DEC31H20M15HighDurPNN500NoTTFilter/HGRU4RecRecommender/";
					// "/mnt/sshServers/theengine/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/AUG12ED1.0LocAllActsFDStFilter0hrs100RTVPNN500SNN500
					// (copy 1)/";
					// "/mnt/sshServers/theengine/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/AUG17ED0.5STimeLocDistPrevDurPrevAllActsFDStFilter0hrs75FRTV75ARTVPNN500SNN500
					// (copy 1)/";
					// "/mnt/sshServers/theengine/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/geolife1_JAN3H13M33ED0.5STimeDurDistTrStartGeoEndGeoAvgAltDistPrevDurPrevAllActsFDStFilter0hrsFEDPerFS_10F_RTVPNN500NoTTFilterNC/";
					// "/mnt/sshServers/theengine/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/geolife1_DEC31H20M15HighDurPNN500NoTTFilter/PopularityRecommender/";
					// "/mnt/sshServers/theengine/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/dcu_data_2_DEC31H20M25HighDurNoTTFilter/HGRU4RecRecommender/";
					// hasMUS = false;
					// if (commonPath1.contains("PythonWorkspace") == false)
					// {
					// continue;
					// }

					PopUps.showMessage(commonPath1);
					if (commonPath1.contains("NC/") || commonPath1.contains("PureAKOM")// NCcoll/
							|| commonPath1.contains("PythonWorkspace") || commonPath1.contains("NCcoll/"))
					{
						Constant.lookPastType = LookPastType.NCount;
						/// run/media/gunjan/My Passport/GeolifeNov2018/dcu_data_2_JAN2H2M36PureAKOMOrder3NoTTFilter/
						if (commonPath1.contains("PureAKOM"))
						{
							Constant.altSeqPredictor = Enums.AltSeqPredictor.PureAKOM;
							String[] splitted1 = commonPath1.split("Order");
							Constant.setAKOMHighestOrder(Integer.valueOf(splitted1[1].subSequence(0, 1).toString()));
						}
						// hasMUS = true;
					}
					else if (commonPath1.contains("NH/") || commonPath1.contains("NHcoll/"))
					{
						Constant.lookPastType = LookPastType.NHours;
						// hasMUS = true;
					}
					else if (commonPath1.contains("DY/") || commonPath1.contains("DYcoll/")
							|| commonPath1.contains("HighDur") || commonPath1.contains("HighOccur"))
					{
						Constant.lookPastType = LookPastType.Daywise;
					}

					else if (commonPath1.contains("CT/") || commonPath1.contains("CTcoll/"))
					{
						Constant.lookPastType = LookPastType.ClosestTime;
					}
					//
					hasMUS = true;
					// Constant.numOfCandsFromEachCollUser = numOfCandsPerUser[i];

					runExperiment(commonPath1, doRecommendation, doEvaluation, hasMUS, Constant.getDatabaseName(),
							"HJEditDistance");
					// cleanUpSpace(commonPaths[i], 0.90);
					System.out.println("finished runExperimentForGivenUsersAndConfig for commonPath = " + commonPath1);

					// start of added on 21 Nov 2018
					List<List<String>> mrrStatsBestMUs = ReadingFromFile
							.readLinesIntoListOfLists(commonPath1 + "mrrStatsOverUsersBestMUs.csv", ":");
					String meanMRROverUsersBestMU = mrrStatsBestMUs.get(4).get(1);
					String medianMRROverUsersBestMU = mrrStatsBestMUs.get(6).get(1);

					if (Constant.searchForOptimalFeatureWts)
					{
						String custodianFeatWtsInfo = iterationCountFeatWtSearch + "," + CustodianOfFeatWts.toCSVWts()
								+ "," + meanMRROverUsersBestMU + "," + medianMRROverUsersBestMU + "\n";
						WToFile.writeToNewFile(custodianFeatWtsInfo, commonPath1 + "CustodianOfFeatWts.csv");
						WToFile.appendLineToFileAbs(custodianFeatWtsInfo, dataWrittenFolder + "CustodianOfFeatWts.csv");
					}
					// break;
				}
			}
			else
			{
				// for (int i = 0; i <= commonPaths.length - 1; i++){
				// start of added on 31 Dec 2018 to eval Jupyter baseline results
				if (false)// evaluate jupyter baselines
				{
					String commonJupyterPath = "/mnt/sshServers/theengine/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/";
					String dcuFolder = "dcu_data_2_DEC31H20M25HighDurNoTTFilter/";
					String geolifeFolder = "geolife1_DEC31H20M15HighDurPNN500NoTTFilter/";
					String gowallaFolder = "gowalla1_DEC31H20M29HighDurPNN500coll/";
					// TODO: SET CORRECT DATABASE NAME IN CONSTANT
					commonJupyterPath += gowallaFolder;
					// "/mnt/sshServers/theengine/PythonWorkspace/RecSysTutorial/sars_tutorial/datasets/gowalla1_DEC31H20M29HighDurPNN500coll/";
					String[] jupyterBaselineLabels = { "PopularityRecommender/", "Prod2VecRecommender/",
							"FPMCRecommender/", "GRU4RecRecommender/", "MixedMarkovRecommender/",
							"HGRU4RecRecommender/" };
					// "FSMRecommender","MixedMarkovRecommender", "FPMCRecommender", "GRU4RecRecommender",
					// "HGRU4RecRecommender" };

					doRecommendation = false;
					doEvaluation = true;

					for (String jupyterBaselineLabel : jupyterBaselineLabels)
					{
						commonPath = commonJupyterPath + jupyterBaselineLabel;
						runExperiment(commonPath, doRecommendation, doEvaluation, hasMUS, Constant.getDatabaseName(),
								"HJEditDistance");
						// cleanUpSpace(commonPaths[i], 0.90);
						System.out
								.println("finished runExperimentForGivenUsersAndConfig for commonPath = " + commonPath);

						// start of added on 21 Nov 2018
						List<List<String>> mrrStatsBestMUs = ReadingFromFile
								.readLinesIntoListOfLists(commonPath + "mrrStatsOverUsersBestMUs.csv", ":");
						String meanMRROverUsersBestMU = mrrStatsBestMUs.get(4).get(1);
						String medianMRROverUsersBestMU = mrrStatsBestMUs.get(6).get(1);

						if (Constant.searchForOptimalFeatureWts)
						{
							String custodianFeatWtsInfo = iterationCountFeatWtSearch + ","
									+ CustodianOfFeatWts.toCSVWts() + "," + meanMRROverUsersBestMU + ","
									+ medianMRROverUsersBestMU + "\n";
							WToFile.writeToNewFile(custodianFeatWtsInfo, commonPath + "CustodianOfFeatWts.csv");
							WToFile.appendLineToFileAbs(custodianFeatWtsInfo,
									dataWrittenFolder + "CustodianOfFeatWts.csv");
						}
					}
				}
				// end of added on 31 Dec 2018
				else
				{
					// Constant.numOfCandsFromEachCollUser = numOfCandsPerUser[i];
					runExperiment(commonPath, doRecommendation, doEvaluation, hasMUS, Constant.getDatabaseName(),
							"HJEditDistance");
					// cleanUpSpace(commonPaths[i], 0.90);
					System.out.println("finished runExperimentForGivenUsersAndConfig for commonPath = " + commonPath);

					// start of added on 21 Nov 2018
					List<List<String>> mrrStatsBestMUs = ReadingFromFile
							.readLinesIntoListOfLists(commonPath + "mrrStatsOverUsersBestMUs.csv", ":");
					String meanMRROverUsersBestMU = mrrStatsBestMUs.get(4).get(1);
					String medianMRROverUsersBestMU = mrrStatsBestMUs.get(6).get(1);

					if (Constant.searchForOptimalFeatureWts)
					{
						String custodianFeatWtsInfo = iterationCountFeatWtSearch + "," + CustodianOfFeatWts.toCSVWts()
								+ "," + meanMRROverUsersBestMU + "," + medianMRROverUsersBestMU + "\n";
						WToFile.writeToNewFile(custodianFeatWtsInfo, commonPath + "CustodianOfFeatWts.csv");
						WToFile.appendLineToFileAbs(custodianFeatWtsInfo, dataWrittenFolder + "CustodianOfFeatWts.csv");
					}
				} // ed of added on 21 Nov 2018
			}
		}
		System.out.println("Exiting runExperimentForGivenUsersAndConfig");
		// PopUps.showMessage("Exiting main0");
		// End
	}

	/**
	 * Create the label for the experiment. Notes: Depends on several (static) paramaters from the Constant class.
	 * 
	 * @param sampledUserIndicesSetFile
	 * @return
	 */
	private static String getLabelForExperimentConfig(String sampledUserIndicesSetFile)
	{
		String featuresUsedLabel = "", distNormalisationLabel = "", predictorLabel = "", EDAlphaLabel = "",
				StFilterLabel = "", sampledUserSetLabel = "", candThresholdingLabel = "",
				filterTrainingTimelinesLabel = "", toyTimelinesLabel = "", wtdEditDistanceLabel = "",
				timeDecayLabel = "", collLabel = "";
		String databaseName = Constant.getDatabaseName();

		// added on 6 Aug 2018
		if (Constant.useToyTimelines)
		{
			toyTimelinesLabel = "Toy";
		}
		if (Constant.doWeightedEditDistanceForSecDim)
		{
			wtdEditDistanceLabel = "WtdSecDim";
		}

		// String sampledUserSetLabel;

		if (sampledUserIndicesSetFile == null || sampledUserIndicesSetFile.length() > 0)
		{
			String userSetLabelSplitted[] = sampledUserIndicesSetFile.split("\\.");
			System.out.println("userSetLabelSplitted=" + Arrays.asList(userSetLabelSplitted));
			sampledUserSetLabel = userSetLabelSplitted[2];
		}
		else
		{
			sampledUserSetLabel = "";
		}

		if (sampledUserSetLabel.equals("csv"))
		{
			sampledUserSetLabel = "";
		}
		System.out.println("sampledUserSetLabel=" + sampledUserSetLabel);

		TypeOfCandThreshold typeOfCandThresholdPrimDim = Constant.typeOfCandThresholdPrimDim;
		TypeOfCandThreshold typeOfCandThresholdSecDim = Constant.typeOfCandThresholdSecDim;

		// if (typeOfCandThresholdPrimDim.equals(Enums.TypeOfCandThreshold.NearestNeighbour))
		// {if (Constant.nearestNeighbourCandEDThresholdPrimDim != 500
		// || Constant.nearestNeighbourCandEDThresholdSecDim != 500)
		// {candThresholdingLabel = String.valueOf(Constant.nearestNeighbourCandEDThresholdPrimDim) + "PDNTh"
		// + String.valueOf(Constant.nearestNeighbourCandEDThresholdSecDim) + "SDNTh";}}
		// End of adding on May6 2018

		// start of added on 8 Aug 2018
		if (!typeOfCandThresholdPrimDim.equals(TypeOfCandThreshold.None))
		{
			switch (typeOfCandThresholdPrimDim)
			{
			case NearestNeighbour:
				candThresholdingLabel += "PNN" + Constant.nearestNeighbourCandEDThresholdPrimDim;
				break;
			case NearestNeighbourWithEDValThresh:
				candThresholdingLabel += "PNNWED" + Constant.nearestNeighbourCandEDThresholdPrimDim + "|"
						+ Constant.candEDValThresholdPrimDim;
				break;
			case Percentile:
				candThresholdingLabel += "PPer" + Constant.percentileCandEDThreshold;
				break;
			}
		}

		if (!typeOfCandThresholdSecDim.equals(TypeOfCandThreshold.None))
		{
			switch (typeOfCandThresholdSecDim)
			{
			case NearestNeighbour:
				candThresholdingLabel += "SNN" + Constant.nearestNeighbourCandEDThresholdSecDim;
				break;
			case NearestNeighbourWithEDValThresh:
				candThresholdingLabel += "SNNWED" + Constant.nearestNeighbourCandEDThresholdSecDim + "|"
						+ Constant.candEDValThresholdSecDim;
				break;
			case Percentile:
				candThresholdingLabel += "SPer" + Constant.percentileCandEDThreshold;
				break;
			}
		}

		// end of added on 8 Aug 2018

		// Start of adding on 25 July 2018
		if (Constant.filterTrainingTimelinesByRecentDays == false)
		{
			filterTrainingTimelinesLabel = "NoTTFilter";
		}
		else
		{
			if (Constant.getRecentDaysInTrainingTimelines() != 5)
			{
				filterTrainingTimelinesLabel = Constant.getRecentDaysInTrainingTimelines() + "DaysTTFilter";
			}
		}
		// End of adding on 25 July 2018
		if (Constant.altSeqPredictor.equals(AltSeqPredictor.HighOccur))
		{
			predictorLabel += "HighOccur";
		}
		else if (Constant.altSeqPredictor.equals(AltSeqPredictor.HighDur))
		{
			predictorLabel += "HighDur";
		}
		else if (Constant.altSeqPredictor.equals(AltSeqPredictor.PureAKOM))
		{
			predictorLabel += AltSeqPredictor.PureAKOM.toString();
			predictorLabel += "Order" + Constant.getAKOMHighestOrder();
		}

		else if (Constant.altSeqPredictor.equals(AltSeqPredictor.RNN1))
		{
			predictorLabel = AltSeqPredictor.RNN1.toString();
			predictorLabel += LSTMCharModelling_SeqRecJun2018.getRNN1Label();
			// Constant.numOfHiddenLayersInRNN1 + "HL" + Constant.numOfNeuronsInEachHiddenLayerInRNN1
			// + "Neu" + Constant.numOfTrainingEpochsInRNN1 + "Epochs";
		}

		else
		{
			EDAlphaLabel = "ED" + Constant.getDynamicEDAlpha();// .dynamicEDAlpha;
			StFilterLabel = "StFilter" + (Constant.filterCandByCurActTimeThreshInSecs / (60 * 60)) + "hrs";

			// if (Constant.useActivityNameInFED)
			// {
			// featuresUsedLabel += "ActName";
			// }

			if (Constant.getDynamicEDAlpha() != 1)
			{
				if (Constant.useStartTimeInFED)
				{
					featuresUsedLabel += "STime";
				}

				if (databaseName.equals("gowalla1"))
				{
					if (Constant.useLocationInFED)
					{
						featuresUsedLabel += "Loc";
					}
					if (Constant.usePopularityInFED)
					{
						featuresUsedLabel += "Pop";
					}
					if (Constant.useDistFromPrevInFED)
					{
						featuresUsedLabel += "DistPrev";
					}
					if (Constant.useDurationFromPrevInFED)
					{
						featuresUsedLabel += "DurPrev";
					}
				}
				if (databaseName.equals("geolife1"))// added on 18 Nov 2018
				{
					if (Constant.useDurationInFED)
					{
						featuresUsedLabel += "Dur";
					}
					if (Constant.useDistTravelledInFED)
					{
						featuresUsedLabel += "DistTr";
					}
					if (Constant.useStartGeoInFED)
					{
						featuresUsedLabel += "StartGeo";
					}
					if (Constant.useEndGeoInFED)
					{
						featuresUsedLabel += "EndGeo";
					}
					if (Constant.useAvgAltitudeInFED)
					{
						featuresUsedLabel += "AvgAlt";
					}
					if (Constant.useDistFromPrevInFED)
					{
						featuresUsedLabel += "DistPrev";
					}
					if (Constant.useDurationFromPrevInFED)
					{
						featuresUsedLabel += "DurPrev";
					}
				}

				if (databaseName.equals("dcu_data_2"))// added on 18 Nov 2018
				{
					if (Constant.useDurationInFED)
					{
						featuresUsedLabel += "Dur";
					}
					// if (Constant.useEndTimeInFED)
					// {
					// featuresUsedLabel += "ET";
					// }
				}
			}

			if (Constant.useFeatureDistancesOfAllActs)
			{
				featuresUsedLabel += "AllActsFD";
			}

			if (Constant.useRTVerseNormalisationForED)
			{
				if (Constant.getDynamicEDAlpha() != 1)
				{
					if (Constant.computeFEDForEachAOInRTVerse)
					{
						distNormalisationLabel += "FEDPerAO_";
					}
					if (Constant.computeFEDForEachFeatureSeqInRTVerse)
					{
						distNormalisationLabel += "FEDPerFS";
						if (Constant.useMSDInFEDInRTVerse)
						{
							distNormalisationLabel += "MSD";
						}
						if (Constant.useLog2InFEDInRTVerse)
						{
							distNormalisationLabel += "Log2";
						}
						distNormalisationLabel += "_";
					}
					if (Constant.computeFEDForEachAOInRTVerse && Constant.computeFEDForEachFeatureSeqInRTVerse)
					{
						PopUps.printTracedErrorMsgWithExit(
								"Error both Constant.computeFEDForEachAOInRTVerse & Constant.computeFEDForEachFeatureSeqInRTVerse should not be true");
					}

					if (Constant.fixedValPerFeatForRTVerseMaxMinForFEDNorm)
					{
						distNormalisationLabel += "FixedMaxMinF_";// RTV
					}
					if (Constant.percentileForRTVerseMaxForFEDNorm > -1)
					{
						distNormalisationLabel += (int) Constant.percentileForRTVerseMaxForFEDNorm + "F_";// RTV
					}
					if (Constant.threshNormFEDForCand != -1)
					{
						distNormalisationLabel += Constant.threshNormFEDForCand + "FT";
					}
				}

				if (Constant.percentileForRTVerseMaxForAEDNorm > -1)
				{
					distNormalisationLabel += distNormalisationLabel + (int) Constant.percentileForRTVerseMaxForAEDNorm
							+ "A_";
				}
				if (Constant.threshNormAEDForCand != -1)
				{
					distNormalisationLabel += Constant.threshNormAEDForCand + "AT";
				}
				distNormalisationLabel += "RTV";

			}

			if (Constant.useTimeDecayInAED)
			{
				timeDecayLabel = "decayA";
			}

			if (Constant.noAED)
			{
				collLabel += "NoAED";
			}
			if (Constant.noFED)
			{
				collLabel += "NoFED";
			}
			if (Constant.lookPastType.equals(Enums.LookPastType.NCount))
			{
				collLabel += "NC";
			}
			if (Constant.lookPastType.equals(Enums.LookPastType.NHours))
			{
				collLabel += "NH";
			}
			if (Constant.lookPastType.equals(Enums.LookPastType.Daywise))
			{
				collLabel += "DY";
			}
			if (Constant.lookPastType.equals(Enums.LookPastType.ClosestTime))
			{
				collLabel += "CT";
			}
		}

		if (Constant.collaborativeCandidates)
		{
			collLabel += "coll";
		}

		if (Constant.lookPastType.equals(Enums.LookPastType.NGram))
		{
			collLabel += "NG";
		}

		if (Constant.useJan7DistanceComputations)
		{
			collLabel += Constant.actLevelDistType.toString();

			if (Constant.useForeignAwareLevenshtein)
			{
				collLabel += "FW";
			}
		}

		if (Constant.ensureCandEndWithCurrAct == false)
		{
			collLabel += "NotEnsurEndCA";
		}

		if (Constant.purelyRandomPredictionNov25)
		{
			return sampledUserSetLabel + predictorLabel + "PurelyRandomly" + toyTimelinesLabel + "";
		}
		else
		{
			return sampledUserSetLabel + predictorLabel + EDAlphaLabel + featuresUsedLabel + StFilterLabel
					+ distNormalisationLabel + candThresholdingLabel + filterTrainingTimelinesLabel + toyTimelinesLabel
					+ wtdEditDistanceLabel + timeDecayLabel + collLabel;
		}
	}

	public static void cleanUp(String[] pathsToClean)
	{
		for (String pathToClean : pathsToClean)
		{
			cleanUpSpace(pathToClean, 0.90);
		} // All correct
	}

	/**
	 * Run experiments:
	 * <ul>
	 * <li>Set outputCoreResultsPath, distanceUsed</li>
	 * <li>Run ControllerWithoutServer</li>
	 * <li>Run EvaluationSeq</li>
	 * <li>Check for errors/exceptions in Log files</li>
	 * </ul>
	 * 
	 * @param commonPath
	 * @param doRecommendation
	 * @param doEvaluation
	 * @param hasMUs
	 * @param databaseName
	 * @param distanceUsed
	 *            e.g. "HJEditDistance"
	 **/
	public static void runExperiment(String commonPath, boolean doRecommendation, boolean doEvaluation, boolean hasMUs,
			String databaseName, String distanceUsed)
	{
		long at = System.currentTimeMillis();
		boolean doPostFiltering = (doEvaluation == false) ? false : true;
		// PopUps.showMessage("hasMUs = " + hasMUs);
		// $$TimeZone.setDefault(TimeZone.getTimeZone("UTC"y)); // added on April 21, 2016
		System.out.println("Beginning runExperiments:\n" + PerformanceAnalytics.getHeapInformation() + "\n"
				+ PerformanceAnalytics.getHeapPercentageFree());
		// String commonPath = "./dataWritten/Nov6_NCount916U916N100T/";// Aug17/";
		// $$String commonPath = "./dataWritten/Nov12_NCount916U916N1C500T/";// Aug17/";
		// String outputCoreResultsPathGowalla = commonPath;
		// + "./dataWrittenNGramBaselineForUserNumInvestigation/";// dataWrittenSeqEditL1
		// RecommUnmergedNCount/";
		// $$"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/Timelines/";
		// $$"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Jan22/";
		// $$"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/";
		// "/run/media/gunjan/BoX2/GowallaSpaceSpace/CheckJavaSqlDuplicateDateIssue/";
		// $$"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/Timelines/";
		// $$"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Jan22/";
		// $$/ home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/GowallaWeather/";
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/";///
		// home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov25/";
		// "/run/media/gunjan/BoX1/GowallaSpaceSpaceSpace/GowallaDataWorksSep19/";//
		// "/run/media/gunjan/BoX2/GowallaSpaceSpace/GowallaDataWorksSep16/";
		// $String commonPathDCU = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/MovingTimelinesMatching/Test1/";
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/";//
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/June7FeatureWiseEdit/DCU/";
		// $String commonPathGeolife = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/GeolifePerformance/Test/";
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April21/Test/";
		// String performanceFileName = "/run/media/gunjan/HOME/gunjan/Geolife Data
		// Works/GeolifePerformance/Test/Performance.csv"; String performanceStringHeader = "
		// UserAtRecomm,DateAtRecomm,TimeAtRecomm,MatchingUnit,NumOfTrainingDays,NumOfValidActObjsInTrainingTimelines,NumOfValidActObjsInCurrentTimelines,NumOfCandidateTimelines,SumOfValidActObjsInAllCandTimelines,TotalTimeForRecomm,
		// TimeToGenerateCands,TimeToComputeEdistDistances\n";

		// to quantify the run time performance
		// $WritingToFile.appendLineToFileAbsolute(performanceStringHeader, performanceFileName);

		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April21/DaywiseGeolife/";//
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ForStats/";//
		// /run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April21/MUExperimentsBLNCount/";//
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ForStats/ValidatingDays/";//
		// Feb20ImpBLNCount/";// TimelineStats18Feb/";// BLNCount/";//
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/June25Experiments/FeatureWiseWeightedDistance/Geolife/";//
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/June18HJDistance/Geo/";//
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/June7FeatureWiseEdit/Geo/";

		// CURRENT START
		// for (int i = 11; i <= 20; i++)
		// {
		// commonPathGeolife = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April21/MUExperimentsBLNCount/";//
		// Feb20ImpBLNCount/";// Feb11Imp/";
		// UtilityBelt.createDirectory(commonPathGeolife + "Iteration" + String.valueOf(i));
		// commonPathGeolife = commonPathGeolife + "Iteration" + String.valueOf(i);// + "/";
		//
		// Constant.setDatabaseName("geolife1");// Constant.DATABASE_NAME = "geolife1";// "geolife1";//"dcu_data_2";//
		// Constant.caseType = "SimpleV3";// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		// Constant.outputCoreResultsPath = commonPathGeolife;// + "SimpleV3/";// "/home/gunjan/DCU/SimpleV3/";//
		// // "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April16_2015/DCUData/SimpleV3/";
		// Constant.setDistanceUsed("HJEditDistance");
		//
		// new ControllerWithoutServer();
		// }
		// CURRENT END//

		// $$ CURRENT START
		// Constant.setDatabaseName("geolife1");// Constant.DATABASE_NAME = "geolife1";// "geolife1";//"dcu_data_2";//
		// Constant.caseType = "SimpleV3";// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		// Constant.outputCoreResultsPath = commonPathGeolife;// + "SimpleV3/";// "/home/gunjan/DCU/SimpleV3/";//
		// // "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April16_2015/DCUData/SimpleV3/";
		// Constant.setDistanceUsed("HJEditDistance");
		// new ControllerWithoutServer();
		// $$ CURRENT END

		// Constant.setDatabaseName(databaseName);// commented out on 12 Oct 2018 as already set in starter kit
		// Constant.caseType = Enums.CaseType.SimpleV3;/// "SimpleV3";// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		System.out.println("commonPath = " + commonPath);
		Constant.setOutputCoreResultsPath(commonPath);// commonPathGeolife;// commonPathDCU + "SimpleV3/";//
		// "/home/gunjan/DCU/SimpleV3/";//
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April16_2015/DCUData/SimpleV3/";
		Constant.setDistanceUsed(distanceUsed);// "HJEditDistance");
		Constant.reflectTheConfigInConstantFile(commonPath + "Constant" + DateTimeUtils.getMonthDateLabel() + ".java");

		if (doRecommendation)
		{
			// //curtain may 19 2017 start
			System.out.println("Doing recommendation...");
			ControllerWithoutServer controllerWithoutServer = new ControllerWithoutServer(Constant.getDatabaseName(),
					commonPath, false);
			// //curtain may 19 2017 end
		}

		Constant.releaseHeavyObjectsNotNeededAfterRecommendation();

		// PopUps.showMessage("Recommendations done");

		if (doEvaluation)
		{
			if (!Constant.getDatabaseName().equals("gowalla1"))
			{// added on 1 Jan as needed to set catIDNameDict needed for standalone eval for dcu and geolife
				ControllerWithoutServer controllerWithoutServer = new ControllerWithoutServer(
						Constant.getDatabaseName(), commonPath, true);
				// Popups.showMessage("Before starting evaluation: " + DomainConstants.catIDNameDictionary.toString());
			}

			// curtain may 26 2017 start
			System.out.println("Doing evaluation...");
			boolean evalPrimaryDimension = true, evalSecondaryDimension = Constant.doSecondaryDimension;
			int lengthOfRecommendedSequence = Constant.lengthOfRecommendedSequence;
			LookPastType lookPastType = Constant.lookPastType;
			AltSeqPredictor altSeqPredictor = Constant.altSeqPredictor;

			if (hasMUs)
			{
				double[] muArray = Constant.getMatchingUnitArray(lookPastType, altSeqPredictor);
				// PopUps.showMessage(
				// "SuperController muArray = " + Arrays.toString(muArray) + "\n lookPastType = " + lookPastType);
				// boolean evalPostFiltering = false;// boolean evalSeqPrediction = true;
				// String dimensionPhrase = "SecDim";
				// TODO Why running all three at once is resultsing in empty files. Has it got something to do with
				// program not exiting upon completion?
				if (evalPrimaryDimension)// primary dimension
				{
					new EvaluationSeq(lengthOfRecommendedSequence, commonPath, muArray, "", false, true);

					if (evalSecondaryDimension)// secondary dimension
					{
						new EvaluationSeq(lengthOfRecommendedSequence, commonPath, muArray, "SecDim", false, true);
					}
				}
				// PopUps.showMessage("Done Evaluation");

				if (doPostFiltering && evalSecondaryDimension)
				{
					new PostFilter1(commonPath, Constant.getDatabaseName()); // requires results from preceeding
																				// EvaluationSeq
					// Evaluate postfiltering
					String[] pfFilterNames = { "Fltr_on_Top1Loc", "Fltr_on_ActualLocPF", "Fltr_on_TopKLocsPF",
							"WtdAlphaPF", "Fltr_on_Random2LocPF", "Fltr_on_Random10LocPF", "Fltr_on_Random20LocPF",
							"Fltr_on_Random50LocPF", "Fltr_on_RandomLocPF" };
					for (String pfPhrase : pfFilterNames)// postfiltering
					{
						new EvaluationSeq(lengthOfRecommendedSequence, commonPath, muArray, pfPhrase, true, false);
					}
				}

				extractAggregateEvalResultsOverMUs(commonPath, "");// for primary dimension
				if (evalSecondaryDimension)// secondary dimension
				{
					extractAggregateEvalResultsOverMUs(commonPath, "SecDim");// for primary dimension
				}
				// if (true)
				// { if (Constant.doSecondaryDimension)
				// { // for (String pfPhrase : pfFilterNames)
				// {new EvaluationSeq(Constant.lengthOfRecommendedSequence, commonPath,
				// Constant.getMatchingUnitArray(Constant.lookPastType, Constant.altSeqPredictor),
				// "SecDim", evalPostFiltering, evalSeqPrediction);// "SecDim");
				// }}}
				// if (false)// temporarily disable for debugging TODO
				// { // new EvaluationSeq(3, commonPath, Constant.matchingUnitAsPastCount, new int[] { 30, 50, 60, 70,
				// 90
				// // });
				// new EvaluationSeq(Constant.lengthOfRecommendedSequence, commonPath,
				// Constant.getMatchingUnitArray(Constant.lookPastType, Constant.altSeqPredictor), "",
				// evalPostFiltering, evalSeqPrediction);}
				// , new int[] {30, 50, 60, 70, 90// });
			}
			else
			{
				new EvaluationSeq(lengthOfRecommendedSequence, commonPath);
				// , Constant.matchingUnitAsPastCount, new int[] { 30, 50, 60, 70, 90});
				// Start of added on 15 Jan 2019
				String splitted[] = commonPath.split("/");
				String resultsLabel = splitted[splitted.length - 1];
				List<List<String>> MRRValsForBestMULines = ReadingFromFile.readLinesIntoListOfLists(
						commonPath + resultsLabel + "_AllMeanReciprocalRank_MinMUWithMaxFirst0Aware.csv", ",");
				MRRValsForBestMULines.remove(0);// remove header

				List<List<String>> RRForOptimalMUAllUsers = new ArrayList<>();
				for (List<String> l : MRRValsForBestMULines)
				{
					String userIndex = l.get(0);
					// String bestMU = l.get(1);
					// bestMU = bestMU.contains(".") ? bestMU : bestMU + ".0";
					/// /All/MatchingUnit3.0/AlgoStep0AllReciprocalRankUnrolled.csv
					String RRFileForBestMUForThisUser = commonPath + "All/" + "AlgoStep0AllReciprocalRankUnrolled.csv";
					List<List<String>> RRForOptimalMU = ReadingFromFile
							.readLinesIntoListOfLists(RRFileForBestMUForThisUser, ",");
					RRForOptimalMU.remove(0);// remove header
					RRForOptimalMUAllUsers.addAll(RRForOptimalMU);
				}
				WToFile.writeToNewFile("UserID,RTDate,RTTime,ActualAct,RR\n" + RRForOptimalMUAllUsers.stream()
						.map(v -> (v.stream().collect(Collectors.joining(",")))).collect(Collectors.joining("\n")),
						commonPath + resultsLabel + "_AllReciprocalRank_MinMUWithMaxFirst0Aware.csv");
				// End of added on 15 Jan 2019
			} // //curtain may 26 2017 end
		}
		// **************************************************************************************************************//
		// Constant.DATABASE_NAME = "dcu_data_2";// geolife1
		// Constant.caseType = "SimpleV3";// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		// Constant.outputCoreResultsPath = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/testJune10/";//
		// commonPathGeolife + "SimpleV3ActivityName/";//
		// Constant.setDistanceUsed("OTMDSAMEditDistance");// "FeatureWiseWeightedEditDistance");// ("HJEditDistance");
		// // Constant.verboseLevenstein = true;
		// Constant.verboseOTMDSAM = true;
		// Constant.verboseDistance = true;
		// Constant.verboseSAX = true;
		// // Constant.setFeatureToConsiderForFeatureWiseEditDistance(true, false, false, false, false, false, false);
		// // Constant.WriteNormalisationsSeparateLines = true;
		// // Constant.WriteNormalisation = true;
		// new ControllerWithoutServer();
		// // Constant.WriteNormalisationsSeparateLines = false;
		// Constant.WriteNormalisation = true;
		//
		// Constant.DATABASE_NAME = "dcu_data_2";//
		// Constant.caseType = "SimpleV3";// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		// Constant.outputCoreResultsPath = commonPathDCU + "SimpleV3ActivityName/";//
		// Constant.setDistanceUsed("FeatureWiseEditDistance");// "FeatureWiseWeightedEditDistance");//
		// ("HJEditDistance");
		// Constant.setFeatureToConsiderForFeatureWiseEditDistance(true, false, false, false, false, false, false);
		// new ControllerWithoutServer();

		// //////////////////////////////////////////////////////////////////////////////////////////
		// Constant.DATABASE_NAME = "geolife1";//
		// Constant.caseType = "SimpleV3";// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		// Constant.outputCoreResultsPath = commonPathGeolife + "SimpleV3StartTime/";//
		// Constant.setDistanceUsed("FeatureWiseEditDistance");// "FeatureWiseWeightedEditDistance");//
		// ("HJEditDistance");
		// Constant.setFeatureToConsiderForFeatureWiseEditDistance(false, true, false, false, false, false, false);
		// // Constant.verboseNormalisation = true;
		// new ControllerWithoutServer();
		// // Constant.verboseNormalisation = false;
		//
		// Constant.DATABASE_NAME = "dcu_data_2";//
		// Constant.caseType = "SimpleV3";// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		// Constant.outputCoreResultsPath = commonPathDCU + "SimpleV3StartTime/";//
		// Constant.setDistanceUsed("FeatureWiseEditDistance");// "FeatureWiseWeightedEditDistance");//
		// ("HJEditDistance");
		// Constant.setFeatureToConsiderForFeatureWiseEditDistance(false, true, false, false, false, false, false);
		// new ControllerWithoutServer();
		// // //////////////////////////////////////////////////////////////////////////////////////////
		//
		// Constant.DATABASE_NAME = "geolife1";//
		// Constant.caseType = "SimpleV3";// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		// Constant.outputCoreResultsPath = commonPathGeolife + "SimpleV3Duration/";//
		// Constant.setDistanceUsed("FeatureWiseEditDistance");// "FeatureWiseWeightedEditDistance");//
		// ("HJEditDistance");
		// Constant.setFeatureToConsiderForFeatureWiseEditDistance(false, false, true, false, false, false, false);
		// new ControllerWithoutServer();
		//
		// Constant.DATABASE_NAME = "dcu_data_2";//
		// Constant.caseType = "SimpleV3";// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		// Constant.outputCoreResultsPath = commonPathDCU + "SimpleV3Duration/";//
		// Constant.setDistanceUsed("FeatureWiseEditDistance");// "FeatureWiseWeightedEditDistance");//
		// ("HJEditDistance");
		// Constant.setFeatureToConsiderForFeatureWiseEditDistance(false, false, true, false, false, false, false);
		// new ControllerWithoutServer();
		// // //////////////////////////////////////////////////////////////////////////////////////////
		// Constant.DATABASE_NAME = "geolife1";//
		// Constant.caseType = "SimpleV3";// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		// Constant.outputCoreResultsPath = commonPathGeolife + "SimpleV3DistanceTravelled/";//
		// Constant.setDistanceUsed("FeatureWiseEditDistance");// "FeatureWiseWeightedEditDistance");//
		// ("HJEditDistance");
		// Constant.setFeatureToConsiderForFeatureWiseEditDistance(false, false, false, true, false, false, false);
		// new ControllerWithoutServer();
		// // //////////////////////////////////////////////////////////////////////////////////////////
		// Constant.DATABASE_NAME = "geolife1";//
		// Constant.caseType = "SimpleV3";// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		// Constant.outputCoreResultsPath = commonPathGeolife + "SimpleV3StartEndGeoCoordinates/";//
		// Constant.setDistanceUsed("FeatureWiseEditDistance");// "FeatureWiseWeightedEditDistance");//
		// ("HJEditDistance");
		// Constant.setFeatureToConsiderForFeatureWiseEditDistance(false, false, true, false, true, true, false);
		// new ControllerWithoutServer();
		// //////////////////////////////////////////////////////////////////////////////////////////
		// Constant.DATABASE_NAME = "geolife1";//
		// Constant.caseType = "SimpleV3";// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		// Constant.outputCoreResultsPath = commonPathGeolife + "SimpleV3AvgAltitude/";//
		// Constant.setDistanceUsed("FeatureWiseEditDistance");// "FeatureWiseWeightedEditDistance");//
		// ("HJEditDistance");
		// Constant.setFeatureToConsiderForFeatureWiseEditDistance(false, false, false, false, false, false, true);
		// new ControllerWithoutServer();
		// //////////////////////////////////////////////////////////////////////////////////////////
		// **************************************************************************************************************//

		// Constant.DATABASE_NAME = "geolife1";//
		// Constant.caseType = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		// Constant.outputCoreResultsPath = commonPathGeolife + "Product/"; //
		// // "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April16_2015/Geolife/CaseBased/Sum/";
		// Constant.setDistanceUsed("HJEditDistance");
		// Constant.rankScoring = "product";// product"; // "sum"
		// new ControllerWithoutServer();
		//
		// Constant.DATABASE_NAME = "dcu_data_2";// "geolife1";//
		// Constant.caseType = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		// Constant.outputCoreResultsPath = commonPathDCU + "Product/";
		// // "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April16_2015/DCUData/CaseBased/Sum/";
		// Constant.rankScoring = "product";// product"; // "sum"
		// Constant.setDistanceUsed("HJEditDistance");
		// new ControllerWithoutServer();
		// //
		// String[] arrBeta = { "0.25", "0.5", "0.75", "1", "0" };
		// // // //
		// // // HALTED AT PROD0 FOR GEO DATA
		// for (int i = 0; i < arrBeta.length; i++)
		// {
		// Constant.ALPHA = Double.parseDouble(arrBeta[i]);
		//
		// Constant.DATABASE_NAME = "geolife1";//
		// Constant.caseType = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		// Constant.outputCoreResultsPath = commonPathGeolife + "Sum" + arrBeta[i] + "/"; // "/home/gunjan/Geolife/Sum"
		// + arrBeta[i] + "/"; //
		// // Constant.outputCoreResultsPath =
		// // "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April16_2015/Geolife/CaseBased/Sum" + arrBeta[i] + "/";
		//
		// // Constant.outputCoreResultsPath =
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April16_2015/Geolife/CaseBased/Sum" + arrBeta[i] + "/";
		// Constant.setDistanceUsed("HJEditDistance");
		// Constant.rankScoring = "sum"; // "sum"
		// new ControllerWithoutServer();
		//
		// Constant.DATABASE_NAME = "dcu_data_2";// "geolife1";//
		// Constant.caseType = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		// Constant.outputCoreResultsPath = commonPathDCU + "Sum" + arrBeta[i] + "/";
		// // Constant.outputCoreResultsPath =
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April16_2015/DCUData/CaseBased/Prod" + arrBeta[i] + "/";
		// Constant.setDistanceUsed("HJEditDistance");
		// Constant.rankScoring = "sum"; // "sum"
		// new ControllerWithoutServer();
		//
		// }
		//
		// Constant.DATABASE_NAME = "dcu_data_2";// "geolife1";//
		// Constant.caseType = "SimpleV3";// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		// Constant.outputCoreResultsPath = commonPathDCU + "SimpleV3ActivityNameStarttime/";// SimpleV3_2
		// "/home/gunjan/DCU/SimpleV3/";//
		// // "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April16_2015/DCUData/SimpleV3/";
		// Constant.setDistanceUsed("FeatureWiseEditDistance");
		// Constant.WriteNormalisationsSeparateLines = true;
		// new ControllerWithoutServer();
		// Constant.WriteNormalisationsSeparateLines = false;

		// // ////////for NGram analysis
		// Constant.DATABASE_NAME = "geolife1";// dcu_data_2";// "geolife1";// "geolife1";//
		// Constant.caseType = "dummy";
		// Constant.rankScoring = "dummy";
		// Constant.outputCoreResultsPath = "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/GeoLife/link to
		// Geolife Data Works/stats/More/";//
		// "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/GeoLife/link to Geolife Data
		// Works/stats/NGramAnalysis/";//
		// // DCU/Product/";
		// new ControllerWithoutServer();
		// ///////////////////////

		// ////////for NGram analysis
		// Constant.DATABASE_NAME = "geolife1";// dcu_data_2";// "geolife1";// "geolife1";//
		// Constant.caseType = "dummy";
		// Constant.rankScoring = "dummy";
		// Constant.outputCoreResultsPath = "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/GeoLife/link to
		// Geolife Data Works/stats/More/";//
		// "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/GeoLife/link to Geolife Data
		// Works/stats/NGramAnalysis/";//
		// DCU/Product/";
		// new ControllerWithoutServer();

		// // //////for NGram analysis
		//
		// Constant.DATABASE_NAME = "dcu_data_2";// dcu_data_2";// "geolife1";// "geolife1";//
		// Constant.caseType = "dummy";
		// Constant.rankScoring = "dummy";
		// Constant.outputCoreResultsPath = "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/GeoLife/link to
		// Geolife Data Works/stats/TimeSeries2/";//
		// TimeSeries2/";//
		//
		// // "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/GeoLife/link to Geolife Data
		// Works/stats/NGramAnalysis/";//
		// // DCU/Product/";
		// new ControllerWithoutServer();

		// ///////////
		// Constant.DATABASE_NAME = "geolife1";// dcu_data_2";// "geolife1";// "geolife1";//
		// Constant.caseType = "dummy";
		// Constant.rankScoring = "dummy";
		// Constant.outputCoreResultsPath = "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/GeoLife/link to
		// Geolife Data Works/stats/TimeSeries2/";//
		// TimeSeries2/";//
		// // PopUps.showMessage("Eureka");
		// // "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/GeoLife/link to Geolife Data
		// Works/stats/NGramAnalysis/";//
		// // DCU/Product/";
		// new ControllerWithoutServer();

		// ///////////////////////

		/*
		 * String errors = Searcher.search(commonPath, "consoleLog", "rror"); String exceptions =
		 * Searcher.search(commonPath, "consoleLog", "xception"); WritingToFile.writeToNewFile(errors + "\n" +
		 * exceptions, commonPath + "ErrorsExceptions.txt");
		 */

		Triple<Set<Path>, Set<Path>, String> errors = Searcher.search2(commonPath, "Log", "rror", "");
		Triple<Set<Path>, Set<Path>, String> exceptions = Searcher.search2(commonPath, "Log", "xception", "");
		WToFile.writeToNewFile(errors.getThird() + "\n" + exceptions.getThird(), commonPath + "ErrorsExceptions2.txt");

		if (errors.getSecond().size() > 1 || exceptions.getSecond().size() > 1)
		{
			WToFile.writeToNewFile(errors.getSecond() + "\n" + exceptions.getSecond(),
					commonPath + "HasErrorsExceptions2.txt");
		}

		// String deleteConsoleLogs = Searcher.searchAndRandomDelete(commonPath, "consoleLog", "rror", 0.65);
		// WritingToFile.writeToNewFile(deleteConsoleLogs, commonPath + "SafelyRandomlyDeleteConsoleLogsForSpace.txt");

		String msg = "runExperiment All done in " + ((System.currentTimeMillis() - at) / 1000) + " seconds";
		System.out.println(msg);
		// Popups.showMessage(msg);
	}

	/**
	 * 
	 * @param commonPath
	 * @param dimensionPhrase
	 */
	private static void extractAggregateEvalResultsOverMUs(String commonPath, String dimensionPhrase)
	{
		// Start of added on 20 Nov 2018
		String fileForChosenMU = "";
		// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/NOV19ResultsDistributionFirstToMax1/FiveDays/geolife1_NOV19ED0.5STimeDurDistTrStartGeoEndGeoAvgAltAllActsFDStFilter0hrsNoTTFilter_AllMeanReciprocalRank_MinMUWithMaxFirst0Aware.csv";

		PrintStream debugLog = WToFile.redirectConsoleOutput(commonPath + "extractAggregateEvalResultsOverMUsLog.txt");
		try
		{
			// Parse and aggregate results for multiple MUs, find optimal MUs for each user for each metric.
			ResultsDistributionEvaluation.runNov20Results(commonPath, 1, dimensionPhrase, fileForChosenMU, true, true,
					false);

			String splitted[] = commonPath.split("/");
			String resultsLabel = splitted[splitted.length - 1];

			fileForChosenMU = commonPath + resultsLabel + "_" + "AllMeanReciprocalRank_" + "MinMUWithMaxFirst0Aware"
					+ dimensionPhrase + ".csv";// added on 17 Jan 2019
			// use the optimal MU for MRR as fixed (chosen) MU to get corresponding values for other metrics,
			ResultsDistributionEvaluation.runNov20Results(commonPath, 1, dimensionPhrase, fileForChosenMU, false, false,
					true);// added on 17 Jan 2019

			String[] groupsOf100UserLabels = DomainConstants.getGroupsOf100UsersLabels();

			for (String groupOf100User : groupsOf100UserLabels)
			{
				// PopUps.showMessage("commonPath = " + commonPath);
				// TODO: for more than 1 user groups, check this if this works correctly
				String commonPathToUse = commonPath;// groupOf100User.equals("All") ? commonPath : commonPath +
													// groupOf100User;
				// PopUps.showMessage("commonPath = " + commonPath);
				// for reading earlier results in All/ or 100R folders
				String commonPathWithUserGroupLabel = commonPath + groupOf100User;
				if (false)
				{
					// List<Double> MRRValsForChosenMU = ReadingFromFile.oneColumnReaderDouble(
					// commonPathToUse + resultsLabel + "_AllMeanReciprocalRank_ChosenMU.csv", ",", 2, true);
					// DescriptiveStatistics mrrStatsOverUsersChosenMUs = StatsUtils
					// .getDescriptiveStatistics(MRRValsForChosenMU);
					// WToFile.writeToNewFile(mrrStatsOverUsersChosenMUs.toString(),
					// commonPathToUse + "mrrStatsOverUsersChosenMUs.csv");
					// System.out.println("mrrStatsOverUsersChosenMUs = " + mrrStatsOverUsersChosenMUs);
				}

				////////////////////////////////////////////////////////////////////////////////////////////////////
				// Read optimal MUs as per highest Mean RR (per user)
				List<Double> MRRValsForBestMU = ReadingFromFile.oneColumnReaderDouble(fileForChosenMU, ",", 2, true);
				MRRValsForBestMU.remove(0);// remove header
				// commonPathToUse + resultsLabel + "_AllMeanReciprocalRank_MinMUWithMaxFirst0Aware.csv",

				DescriptiveStatistics mrrStatsOverUsersBestnMUs = StatsUtils.getDescriptiveStatistics(MRRValsForBestMU);
				// PopUps.showMessage("commonPathToUse = " + commonPathToUse);
				WToFile.writeToNewFile(mrrStatsOverUsersBestnMUs.toString(),
						commonPathToUse + "mrrStatsOverUsersBestMUs.csv");

				System.out.println("mrrStatsOverUsersBestMUs = " + mrrStatsOverUsersBestnMUs);
				// End of added on 20 Nov 2018

				// Start of added on 15 Jan 2019
				List<List<String>> MRRValsForBestMULines = ReadingFromFile.readLinesIntoListOfLists(fileForChosenMU,
						",");
				MRRValsForBestMULines.remove(0);// remove header
				////////////////////////////////////////////////////////////////////////////////////////////////////

				List<List<String>> RRForOptimalMUAllUsers = new ArrayList<>();
				// PopUps.showMessage("here1_1");
				for (List<String> l : MRRValsForBestMULines)
				{
					String userIndex = l.get(0);
					String bestMU = l.get(1);
					bestMU = bestMU.contains(".") ? bestMU : bestMU + ".0";

					String RRFileForBestMUForThisUser = commonPathWithUserGroupLabel + "/MatchingUnit" + bestMU
							+ "/AlgoStep0AllReciprocalRankUnrolled" + dimensionPhrase + ".csv";

					if (bestMU.contains("999"))
					{
						RRFileForBestMUForThisUser = commonPathWithUserGroupLabel
								+ "/AlgoStep0AllReciprocalRankUnrolled.csv";
					}
					if (Constant.lookPastType.equals(LookPastType.NHours) && (bestMU.trim().equals("0.0")))
					{
						RRFileForBestMUForThisUser = commonPathWithUserGroupLabel + "/MatchingUnit0.5"
								+ "/AlgoStep0AllReciprocalRankUnrolled" + dimensionPhrase + ".csv";
					}
					// PopUps.showMessage("here1_2: RRFileForBestMUForThisUser=\n" + RRFileForBestMUForThisUser
					// + "\nbestMU=" + bestMU);
					// PopUps.showMessage("RRFileForBestMUForThisUse=\n" + RRFileForBestMUForThisUser);
					List<List<String>> RRForOptimalMU = ReadingFromFile
							.readLinesIntoListOfLists(RRFileForBestMUForThisUser, ",");
					RRForOptimalMU.remove(0);// remove header

					// keep RR row only for current user //start of added 24 Jan 2019
					List<List<String>> RRForOptimalMUOnlyCurrUser = new ArrayList<>();
					int numOfUsersToSkip = Integer.valueOf(userIndex);
					LinkedHashSet<String> uniqueUsersInSeq = new LinkedHashSet<>();
					for (List<String> rrline : RRForOptimalMU)
					{
						uniqueUsersInSeq.add(rrline.get(0));
						if (uniqueUsersInSeq.size() <= numOfUsersToSkip)
						{
							continue;
						}
						if (uniqueUsersInSeq.size() >= (numOfUsersToSkip + 2))
						{
							break;
						}
						RRForOptimalMUOnlyCurrUser.add(rrline);
					}
					// end of added 24 Jan 2019
					RRForOptimalMUAllUsers.addAll(RRForOptimalMUOnlyCurrUser);// RRForOptimalMU);
				}
				// PopUps.showMessage("here1_3");
				WToFile.writeToNewFile("UserID,RTDate,RTTime,ActualAct,RR\n" + RRForOptimalMUAllUsers.stream()
						.map(v -> (v.stream().collect(Collectors.joining(",")))).collect(Collectors.joining("\n")),
						commonPathToUse + resultsLabel + "_AllReciprocalRank_MinMUWithMaxFirst0Aware" + dimensionPhrase
								+ ".csv");
				// PopUps.showMessage("here1_4");
				// End of added on 15 Jan 2019

				// Start of added on 13 Feb 2019
				// aggregated RR results (which have been extracted for best MU for each user) by target activity and by
				// users (note: the user wise aggregation
				// should be identical to ".._AllMeanReciprocalRank_MinMUWithMaxFirst0Aware.csv" which was obtained
				// through
				// an alternative approach. This can be used as a sanity check).
				// ReadingFromFile.readLinesIntoListOfLists(commonPathToUse + resultsLabel +
				// "_AllReciprocalRank_MinMUWithMaxFirst0Aware" + dimensionPhrase
				// + ".csv", ",");
				EvalMetrics.groupByUserAndActual(
						resultsLabel + "_AllReciprocalRank_MinMUWithMaxFirst0Aware" + dimensionPhrase, commonPathToUse);
				// End of added on 13 Feb 2019
			}

		}
		catch (Exception e)
		{
			PopUps.showException(e, "org.activity.controller.SuperController.extractAggregateEvalResultsOverMUs()");
		}
		debugLog.close();

	}

	/**
	 * Delete a given % of files including "consoleLog" in their name and having no error and exception to save space.
	 * 
	 * @param commonPath
	 * @param ratioOfPercentageFilesToDelete
	 *            % of files including "consoleLog" in their name and having no error and exception to delete
	 * 
	 **/
	public static void cleanUpSpace(String commonPath, double ratioOfPercentageFilesToDelete)
	{
		// String commonPath = "./dataWritten/Aug14Filter500/";
		String s = ("cleanUpSpace called on commonPath=" + commonPath + "\n");
		System.out.println(s);

		String deleteConsoleLogs = Searcher.searchAndRandomDelete2(commonPath, "consoleLog",
				Arrays.asList("rror", "xception"), ratioOfPercentageFilesToDelete);

		System.out.println("result= " + deleteConsoleLogs);

		// .getMonth().toString().substring(0, 3) + LocalDateTime.now().getDayOfMonth()
		WToFile.writeToNewFile(deleteConsoleLogs, commonPath + "CleanUpSafelyRandomlyDeleteConsoleLogsForSpace"
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".txt");
	}

	/**
	 * Delete a given % of files including "fileNamePattern" in their name and having no error and exception to save
	 * space.
	 * 
	 * @param commonPath
	 * @param ratioOfPercentageFilesToDelete
	 *            % of files including "fileNamePattern" in their name and having no error and exception to delete
	 * @param fileNamePattern
	 * 
	 **/
	public static void cleanUpSpace(String commonPath, double ratioOfPercentageFilesToDelete, String fileNamePattern)
	{
		System.out.println("cleanUpSpace called on commonPath=" + commonPath + "\n");

		String deleteFilesMatchingFileNamePattern = Searcher.searchAndRandomDelete2(commonPath, fileNamePattern,
				Arrays.asList("rror", "xception"), ratioOfPercentageFilesToDelete);

		System.out.println("result= " + deleteFilesMatchingFileNamePattern);

		// .getMonth().toString().substring(0, 3) + LocalDateTime.now().getDayOfMonth()
		WToFile.writeToNewFile(deleteFilesMatchingFileNamePattern,
				commonPath + "CleanUpSafelyRandomlyDelete" + fileNamePattern + "ForSpace"
						+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".txt");
	}

}
