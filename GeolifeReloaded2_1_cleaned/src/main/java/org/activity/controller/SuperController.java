package org.activity.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

import org.activity.constants.Constant;
import org.activity.evaluation.EvaluationSeq;
import org.activity.io.CSVUtils;
import org.activity.io.ReadingFromFile;
import org.activity.io.WritingToFile;
import org.activity.ui.PopUps;
import org.activity.util.PerformanceAnalytics;
import org.activity.util.Searcher;

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

	public static void main(String args[])
	{
		runAllAKOMExperiments();
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
				WritingToFile.createDirectory(commonPath);
				commonPath += "/";

				Constant.setAKOMHighestOrder(order);
				Constant.setRecentDaysInTrainingTimelines(numOfDay);

				runExperiments(commonPath, true, true, true);
				cleanUpSpace(commonPath, 0.80);
				System.out.println("finished for commonPath = " + commonPath);
			}
		}

		System.out.println(" Exiting SuperController");
		// cleanUpSpace("./dataWritten/Aug14Filter500/",0.80);
	}

	public static void main31Jan(String args[])
	{
		System.out.println("Java Version:" + System.getProperty("java.version"));

		// Start
		String[] commonPaths = { "./dataWritten/Jan31_AKOM_5DayFilter_Order3/" };
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
			// Constant.numOfCandsFromEachCollUser = numOfCandsPerUser[i];
			runExperiments(commonPaths[i], true, false, true);
			System.out.println("finished for commonPath = " + commonPaths[i]);
		}
		System.out.println(" Exiting SuperController");
		// End
		// cleanUpSpace("./dataWritten/Aug14Filter500/",0.80);
	}

	// All correct
	/**
	 * 
	 * @param commonPath
	 * @param recommendation
	 * @param evaluation
	 * @param hasMUs
	 **/
	public static void runExperiments(String commonPath, boolean recommendation, boolean evaluation, boolean hasMUs)
	{
		long at = System.currentTimeMillis();
		// $$TimeZone.setDefault(TimeZone.getTimeZone("UTC"y)); // added on April 21, 2016
		System.out.println("Beginning runExperiments:\n" + PerformanceAnalytics.getHeapInformation() + "\n"
				+ PerformanceAnalytics.getHeapPercentageFree());
		// String commonPath = "./dataWritten/Nov6_NCount916U916N100T/";// Aug17/";
		// $$String commonPath = "./dataWritten/Nov12_NCount916U916N1C500T/";// Aug17/";
		System.out.println("commonPath = " + commonPath);
		String commonPathGowalla = commonPath;
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

		Constant.setDatabaseName("gowalla1");// ("dcu_data_2");// "geolife1"
		// Constant.caseType = Enums.CaseType.SimpleV3;/// "SimpleV3";// = "CaseBasedV1";// " CaseBasedV1 " or SimpleV3
		Constant.setOutputCoreResultsPath(commonPathGowalla);// commonPathGeolife;// commonPathDCU + "SimpleV3/";//
																// "/home/gunjan/DCU/SimpleV3/";//
		// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April16_2015/DCUData/SimpleV3/";
		Constant.setDistanceUsed("HJEditDistance");

		if (recommendation)
		{
			// //curtain may 19 2017 start
			ControllerWithoutServer controllerWithoutServer = new ControllerWithoutServer(Constant.getDatabaseName());
			// //curtain may 19 2017 end
		}

		if (evaluation)
		{// curtain may 26 2017 start
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

		String errors = Searcher.search(commonPath, "consoleLog", "rror");
		String exceptions = Searcher.search(commonPath, "consoleLog", "xception");
		WritingToFile.writeToNewFile(errors + "\n" + exceptions, commonPath + "ErrorsExceptions.txt");

		errors = Searcher.search(commonPath, "Log", "rror");
		exceptions = Searcher.search(commonPath, "Log", "xception");
		WritingToFile.writeToNewFile(errors + "\n" + exceptions, commonPath + "ErrorsExceptions2.txt");

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
		WritingToFile.writeToNewFile(deleteConsoleLogs, commonPath + "CleanUpSafelyRandomlyDeleteConsoleLogsForSpace"
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")) + ".txt");
	}
}
