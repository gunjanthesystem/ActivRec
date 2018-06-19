package org.activity.controller;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.activity.constants.Constant;
import org.activity.constants.Enums;
import org.activity.constants.Enums.AltSeqPredictor;
import org.activity.evaluation.EvaluationSeq;
import org.activity.io.CSVUtils;
import org.activity.io.ReadingFromFile;
import org.activity.io.WToFile;
import org.activity.objects.Triple;
import org.activity.ui.PopUps;
import org.activity.util.PerformanceAnalytics;
import org.activity.util.Searcher;
import org.nd4j.jita.conf.Configuration;
import org.nd4j.jita.conf.CudaEnvironment;
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

	private static void setupCUDAEnviron()
	{
		// see
		// https://github.com/deeplearning4j/nd4j/blob/78c96a8a3f7aab948af84902c144e1b2123c4436/nd4j-backends/nd4j-backend-impls/nd4j-cuda/src/test/java/jcuda/jcublas/ops/DevicesTests.java#L18-L18
		// https://github.com/deeplearning4j/deeplearning4j/issues/2374
		Configuration config = CudaEnvironment.getInstance().getConfiguration();
		config.allowCrossDeviceAccess(false);
		config.allowMultiGPU(false);

		System.out.println("availableDevices = " + config.getAvailableDevices());
		System.out.println("bannedDevices = " + config.getBannedDevices());
		System.out.println("isCrossDeviceAccessAllowed = " + config.isCrossDeviceAccessAllowed());
		System.out.println("isForcedSingleGPU = " + config.isForcedSingleGPU());
		List<Integer> cudaDevices = findCudaDevices();
		System.out.println("cudaDevices = " + cudaDevices);

		int numOfGPUsToUse = 1;
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

		// config.useDevices(0);
		// config.banDevice(1, 2, 3);
		System.out.println("availableDevices = " + config.getAvailableDevices());
		System.out.println("bannedDevices = " + config.getBannedDevices());
		System.out.println("Device list " + config.getAvailableDevices().toString());

		// Configuration cudaConfig = CudaEnvironment.getInstance().getConfiguration();
		// cudaConfig.allowMultiGPU(false).allowCrossDeviceAccess(false).useDevices(1);
		// CudaEnvironment.getInstance().getConfiguration().GPU

		// allow large cache: upto 6GiB, see: https://deeplearning4j.org/gpu
		config.setMaximumDeviceCacheableLength(1024 * 1024 * 1024L).setMaximumDeviceCache(6L * 1024 * 1024 * 1024L)
				.setMaximumHostCacheableLength(1024 * 1024 * 1024L).setMaximumHostCache(6L * 1024 * 1024 * 1024L);

		System.out.println("Exiting setupCUDAEnvrion.");// : printing environ:-\n" + printEnvironmentInformation());
	}

	/**
	 * 
	 * @param args
	 */
	public static void main(String args[])// _importantMay10
	{
		setupCUDAEnviron();
		// searchContentInFile();
		// sftp://claritytrec.ucd.ie/home/gunjankumar/SyncedWorkspace/Aug2Workspace/GeolifeReloaded2_1_cleaned
		// cleanUpSpace("./dataWritten/Mar5ED0.0STimeStFilter0hrs/", 0.9);
		// runAllAKOMExperiments();
		// $ cleanUpSpace("/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWrittenNGramBaseline/", 0.9);
		// $cleanUpSpace("/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWrittenNGramBaselineForUserNumInvestigation/",0.9);
		// cleanUpSpace("/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWrittenClosestTimeBaseline/", 0.9);
		// cleanUp(new String[] { "/dataWritten/Aug3_Only1CandPU/", "/dataWritten/Aug3_Only1CandPU2/",
		// "/dataWritten/Feb23NCount_5Day_NN500MedRepCinsNormEDAlpha0.5DistDurOnlyFeature_1/",
		// "/dataWritten/Feb25NCount_5Day_NN500MedRepCinsNormEDAlpha0.75DistDurOnlyFeature/",
		// "/dataWritten/Feb12NCount_5DayFilter_ThreshNN500MedianRepCinsFiltrdByCurrActTime",
		// "/dataWritten/Feb27ED0.5DurFPDistFPStFilter3hrs/", "/dataWritten/Mar1ED0.25DurFPDistFPStFilter3hrs/",
		// "/dataWritten/Mar1ED0.5DurFPDistFPStFilter3hrs_part2/" });

		String[] sampledUserIndicesSets = { "./dataToRead/RandomlySample100UsersApril24_2018.csv" };
		// ,
		// "./dataToRead/RandomlySample100UsersApril24_2018.SetB",
		// "./dataToRead/RandomlySample100UsersApril24_2018.SetC",
		// "./dataToRead/RandomlySample100UsersApril24_2018.SetD",
		// "./dataToRead/RandomlySample100UsersApril24_2018.SetE" };

		double[] EDAlphas = { -1/* 0.35, 0.75, 1, 0.15, 0, */ };// 1, 0 };

		for (double edAlphaForAnExp : EDAlphas)
		{
			if (Constant.useToyTimelines)
			{
				// PopUps.showMessage("here inside toy!");
				// Note: sampledUserIndicesSets are irrelavant for the toy timelines
				main0(sampledUserIndicesSets[0], edAlphaForAnExp);
			}
			else
			{
				for (String sampledUserIndicesSet : sampledUserIndicesSets)
				{
					main0(sampledUserIndicesSet, edAlphaForAnExp);
				}
			}
		}
		// cleanUp(new String[] { "./dataWritten/Dec20_AKOM_1DayFilter_Order3_todelete",
		// "./dataWritten/Dec20_Ncount_100U_9kN_1C_ThreshNN-750", "./dataWritten/Jan23_SameSamples_AKOM5DayOrder1",
		// "./dataWritten/Jan23_SameSamples_AKOM5DayOrder3", "./dataWritten/Jan3_Sampling_AKOM1DayOrder1",
		// "./dataWritten/Jan3_Sampling_AKOM1DayOrder1_withErrors",
		// "./dataWritten/Jan3_Sampling_AKOM1DayOrder1_error6Jan",
		// "./dataWritten/Jan3_Sampling_AKOM1DayOrder1_withErrors_run2",
		// "./dataWritten/Jan31_AKOM_5DayFilter_Order5", "./dataWritten/Jan31_AKOM_1DayFilter_Order5",
		// "./dataWritten/Jan31_AKOM_5DayFilter_Order3", "./dataWritten/Jan31_AKOM_1DayFilter_Order3",
		// "./dataWritten/Jan31_AKOM_1DayFilter_Order1", "./dataWritten/Jan31_AKOM_5DayFilter_Order1",
		// "./dataWritten/Dec20_AKOM_5DayFilter_Order5", "./dataWritten/Dec26_AKOM_5DayFilter_Order3",
		// "./dataWritten/Dec20_AKOM_1DayFilter_Order5_part1", "./dataWritten/Dec20_AKOM_1DayFilter_Order3",
		// "./dataWritten/Dec20_AKOM_1DayFilter_Order5", "./dataWritten/Dec20_AKOM_5DayFilter_Order3_incomplete",
		// "./dataWritten/Dec20_AKOM_5DayFilter_Order3_part1", "./dataWritten/Dec20_AKOM_AllDayFilter_Order1",
		// "./dataWritten/Dec15_PureAKOM_NoCandDayFIlter_Order1_part1",
		// "./dataWritten/Dec15_PureAKOM_NoCandDayFIlter_Order1_part2",
		// "./dataWritten/Jan18_Sampling_Ncount10DayThreshold50",
		// "./dataWritten/Dec15_PureAKOM_NoCandDayFIlter_Order1", });
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

				runExperiments(commonPath, false, true, true, "gowalla1");
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
	 * <li>Sets Constant.pathToRandomlySampledUserIndices, Constant.EDAlpha, labelForExperimentConfig</li>
	 * <li>call runExperiments for different commonPaths</li>
	 * </ul>
	 * 
	 * @param sampledUserIndicesSetFile
	 * @param EDAlphaForThisExperiment
	 *            >-1 if we want to set in here instead of hardcoding it in the Constant class.
	 */
	public static void main0(String sampledUserIndicesSetFile, double EDAlphaForThisExperiment)
	{
		System.out.println("For this experiment: Java Version:" + System.getProperty("java.version"));
		System.out.println("sampledUserIndicesSetFile=" + sampledUserIndicesSetFile);
		Constant.pathToRandomlySampledUserIndices = sampledUserIndicesSetFile;
		System.out.println("Constant.pathToRandomLySampleUserIndices=" + sampledUserIndicesSetFile);

		if (EDAlphaForThisExperiment > -1)// when EDAlphaForThisExperiment is <=-1, means we do not need to set it here.
		{
			Constant.EDAlpha = EDAlphaForThisExperiment;// Constant.setEDAlpha(EDAlphaForThisExperiment);//
			System.out.println("SETTING EDAlpha dynamically");
		}
		else
		{
			System.out.println("NOT SETTING EDAlpha dynamically");
		}
		System.out.println("Constant.EDAlpha=" + Constant.EDAlpha);

		String labelForExperimentConfig = getLabelForExperimentConfig(sampledUserIndicesSetFile);

		String[] commonPaths = // { "/run/media/gunjan/BackupVault/GOWALLA/GowallaResults/"
				{ "./dataWritten/" + LocalDateTime.now().getMonth().toString().substring(0, 3)
						+ LocalDateTime.now().getDayOfMonth() + labelForExperimentConfig + "/" };

		// String[] commonPaths = { "/run/media/gunjan/BackupVault/GOWALLA/GowallaResults/Mar2ED" + Constant.EDAlpha
		// + "StFilter" + (Constant.filterCandByCurActTimeThreshInSecs / (60 * 60)) + "hrs/" };
		/// run/media/gunjan/BackupVault/GOWALLA/GowallaResults/Mar1ED
		// "/run/media/gunjan/Buffer/Vault/GowallaResults/March1/" };
		//
		// Feb26NCount_5Day_NN500MedRepCinsNormEDAlpha0.5DistDurOnlyFeature_EDAnalysis
		// "./dataWritten/Feb23NCount_5Day_NN500MedRepCinsNormEDAlpha0.5DistDurOnlyFeature/" }; //
		// "/run/media/gunjan/BufferVault/GowallaResults/Feb22/"
		// "/run/media/gunjan/BufferVault/GowallaResults/Feb25/" };
		// "/run/media/gunjan/BufferVault/GowallaResults/Feb24/" };
		// + "Feb23NCount_5Day_NN500MedRepCinsNormEDAlpha0.5DistDurOnlyFeature/" };
		// + "Feb18NCount_5DayFilter_ThreshNN500MedianRepCinsNormEDAlpha0.4/" };
		// + "/Feb12NCount_5DayFilter_ThreshNN500MedianRepCinsFiltrdByCurrActTime/" };
		// + "/run/media/gunjan/BufferVault/GowallaResults/Feb13/" };
		// "./dataWritten/Feb12NCount_5DayFilter_ThreshNN500MedianRepCinsNormEDAlpha0.6/" };
		// + "Feb12NCount_5DayFilter_ThreshNN500MedianRepCinsFiltrdByCurrActTime5hrs/" };
		// + "Feb12NCount_5DayFilter_ThreshNN500MedianRepCinsFiltrdByCurrActTime/" };
		// + "//Feb10NCount_5DayFilter_ThreshNN500MedianRepCinsNormEDAlpha0.8_part1/" };
		// Feb11NCount_5DayFilter_ThreshNN500MedianRepCinsHierED/" };
		// + "Feb9NCount_5DayFilter_ThreshNN500MedianRepCinsNormEDAlpha0.5/" };//
		// + "Feb10NCount_5DayFilter_ThreshNN500MedianRepCinsNormEDAlpha0.6/" };
		// Feb8NCount_5DayFilter_ThreshNN500EditDists/" };
		// + "//Jan31_SameSamples_AKOM5DayOrder3Test/" };
		// + "Jan23_SameSamples_AKOM5DayOrder1/" };
		// Jan6_Sampling_Ncount5DayThreshold500
		// AKOM_916U_915N_5dayC_Order-3/" };//
		// ""/run/media/gunjan/BufferVault/GowallaResults/Dec20Attribus/" };
		// + "Dec20_AKOM_1Cand_Order1/" };
		// "./dataWritten/Ncount_100U_9kN_1C_ThreshNN-750/" };
		// "./dataWrittenDec20_AKOM_1Cand_Order1/" };
		// "./dataWritten/Dec20_NCount_AllCand5DayFilter/" };
		// "./dataWritten/Dec16_PureAKOM_NoCandDayFIlter_Order3/" };
		// "/run/media/gunjan/BufferVault/GowallaResults/Dec14_PureAKOM_NoCandDayFilter_Order1/" };
		// ./dataWritten/Dec11NGram/" };// { "./dataWritten/Nov16_AKOM3_916U_10cand/",
		// "./dataWritten/Nov16_AKOM3_916U_50cand/", "./dataWritten/Nov16_AKOM3_916U_100cand/" };
		// int[] numOfCandsPerUser = { 10, 50, 100 }; { "/dataWritten/Nov10_AKOM1_9k1cand/"};
		// { "./dataWritten/Nov12_NCount916U916N1C1500T/", "./dataWritten/Nov12_NCount916U916N1C750T/",
		// "./dataWritten/Nov12_NCount916U916N1C500T/", "./dataWritten/Nov12_NCount916U916N1C250T/" };

		for (int i = 0; i <= commonPaths.length - 1; i++)
		{
			File directory = new File(commonPaths[i]);
			if (!directory.exists())
			{
				directory.mkdir();
				// If you require it to make the entire directory path including parents,
				// use directory.mkdirs(); here instead.
			}
			// Constant.numOfCandsFromEachCollUser = numOfCandsPerUser[i];
			runExperiments(commonPaths[i], true, true, true, "gowalla1");
			// cleanUpSpace(commonPaths[i], 0.90);
			System.out.println("finished for commonPath = " + commonPaths[i]);
		}

		System.out.println(" Exiting SuperController");
		// End
		// cleanUpSpace("./dataWritten/Aug14Filter500/",0.80);
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
				StFilterLabel = "", sampledUserSetLabel = "", nearestNeighbourCandLabel = "";

		// String sampledUserSetLabel;
		String userSetLabelSplitted[] = sampledUserIndicesSetFile.split("\\.");
		System.out.println("userSetLabelSplitted=" + Arrays.asList(userSetLabelSplitted));
		sampledUserSetLabel = userSetLabelSplitted[2];
		if (sampledUserSetLabel.equals("csv"))
		{
			sampledUserSetLabel = "";
		}
		System.out.println("sampledUserSetLabel=" + sampledUserSetLabel);

		if (Constant.typeOfCandThreshold.equals(Enums.TypeOfCandThreshold.NearestNeighbour))
		{
			if (Constant.nearestNeighbourCandEDThreshold != 500)
			{
				nearestNeighbourCandLabel = String.valueOf(Constant.nearestNeighbourCandEDThreshold);
			}
		}
		// End of adding on May6 2018

		if (Constant.altSeqPredictor.equals(AltSeqPredictor.PureAKOM))
		{
			predictorLabel = AltSeqPredictor.PureAKOM.toString();
			predictorLabel += "Order" + Constant.getAKOMHighestOrder();
		}

		else if (Constant.altSeqPredictor.equals(AltSeqPredictor.RNN1))
		{
			predictorLabel = AltSeqPredictor.RNN1.toString();
			predictorLabel += Constant.numOfHiddenLayersInRNN1 + "HL" + Constant.numOfNeuronsInEachHiddenLayerInRNN1
					+ "Neu" + Constant.numOfTrainingEpochsInRNN1 + "Epochs";
		}

		else
		{
			EDAlphaLabel = "ED" + Constant.EDAlpha;
			StFilterLabel = "StFilter" + (Constant.filterCandByCurActTimeThreshInSecs / (60 * 60)) + "hrs";

			// if (Constant.useActivityNameInFED)
			// {
			// featuresUsedLabel += "ActName";
			// }
			if (Constant.useStartTimeInFED)
			{
				featuresUsedLabel += "STime";
			}
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

			if (Constant.useFeatureDistancesOfAllActs)
			{
				featuresUsedLabel += "AllActsFD";
			}

			if (Constant.useRTVerseNormalisationForED)
			{
				distNormalisationLabel = "RTV";
				if (Constant.percentileForRTVerseMaxForEDNorm > -1)
				{
					distNormalisationLabel = (int) Constant.percentileForRTVerseMaxForEDNorm + distNormalisationLabel;
				}
			}
		}

		return sampledUserSetLabel + predictorLabel + EDAlphaLabel + featuresUsedLabel + StFilterLabel
				+ distNormalisationLabel + nearestNeighbourCandLabel;
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
	 * <li>Set databasename,outputCoreResultsPath, distanceUsed</li>
	 * <li>Run ControllerWithoutServer</li>
	 * <li>Run EvaluationSeq</li>
	 * <li>Check for errors/exceptions in Log files</li>
	 * </ul>
	 * 
	 * @param commonPath
	 * @param recommendation
	 * @param evaluation
	 * @param hasMUs
	 * @param databaseName
	 **/
	public static void runExperiments(String commonPath, boolean recommendation, boolean evaluation, boolean hasMUs,
			String databaseName)
	{
		long at = System.currentTimeMillis();
		// $$TimeZone.setDefault(TimeZone.getTimeZone("UTC"y)); // added on April 21, 2016
		System.out.println("Beginning runExperiments:\n" + PerformanceAnalytics.getHeapInformation() + "\n"
				+ PerformanceAnalytics.getHeapPercentageFree());
		// String commonPath = "./dataWritten/Nov6_NCount916U916N100T/";// Aug17/";
		// $$String commonPath = "./dataWritten/Nov12_NCount916U916N1C500T/";// Aug17/";
		System.out.println("commonPath = " + commonPath);
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

		Constant.setDatabaseName(databaseName);// "gowalla1");// ("dcu_data_2");// "geolife1"
		// Constant.caseType = Enums.CaseType.SimpleV3;/// "SimpleV3";// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		Constant.setOutputCoreResultsPath(commonPath);// commonPathGeolife;// commonPathDCU + "SimpleV3/";//
		// "/home/gunjan/DCU/SimpleV3/";//
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April16_2015/DCUData/SimpleV3/";
		Constant.setDistanceUsed("HJEditDistance");

		if (recommendation)
		{
			// //curtain may 19 2017 start
			System.out.println("Doing recommendation...");
			ControllerWithoutServer controllerWithoutServer = new ControllerWithoutServer(Constant.getDatabaseName());
			// //curtain may 19 2017 end
		}

		if (evaluation)
		{// curtain may 26 2017 start
			System.out.println("Doing evaluation...");
			if (hasMUs)
			{// new EvaluationSeq(3, commonPath, Constant.matchingUnitAsPastCount, new int[] { 30, 50, 60, 70, 90 });
				new EvaluationSeq(3, commonPath,
						Constant.getMatchingUnitArray(Constant.lookPastType, Constant.altSeqPredictor));
				// , new int[] {30, 50, 60, 70, 90// });
			}
			else
			{
				new EvaluationSeq(3, commonPath);// , Constant.matchingUnitAsPastCount, new int[] { 30, 50, 60, 70, 90
													// });
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

		long bt = System.currentTimeMillis();
		System.out.println("All done in " + ((bt - at) / 1000) + " seconds");
		PopUps.showMessage("All done in " + ((bt - at) / 1000) + " seconds");
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
		// Timestamp

		// .getMonth().toString().substring(0, 3) + LocalDateTime.now().getDayOfMonth()
		WToFile.writeToNewFile(deleteConsoleLogs, commonPath + "CleanUpSafelyRandomlyDeleteConsoleLogsForSpace"
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".txt");
	}
}
