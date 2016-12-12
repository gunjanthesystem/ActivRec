package org.activity.evaluation;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.activity.io.WritingToFile;
import org.activity.objects.Pair;
import org.activity.util.UtilityBelt;
import org.activity.util.weka.WekaUtilityBelt;
import org.activity.util.weka.WekaUtilityBelt.ClustersRangeScheme;

/**
 * Addresses the task such as finding the best MUs and MRR in case of MU experiments. Hence useful for ground truth generation, etc..
 * 
 * @author gunjan
 *
 */
public class MUEvaluationUtils
{
	
	// final static ClustersRangeScheme clusteringRangeScheme = ClustersRangeScheme.CLUSTERING0;
	
	public static void main0(String args[])
	{
		// executeSingle();
		// final ClustersRangeScheme clusteringRangeScheme = ClustersRangeScheme.CLUSTERING0;// "CLUSTERING0";
		// //$$ UMAP submission experiments start
		// String iterationRootPath = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/Feb11ImpBLNCount/";// Feb7ImpIterations/";
		// String rootPathToWriteResults = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/GroundTruthComparisonsFeb22/Clustering0MUTil30/";// 8/Mod2/";
		// executeForMultipleIterationsOfExperiments(ClustersRangeScheme.CLUSTERING0, iterationRootPath, rootPathToWriteResults);
		//
		// rootPathToWriteResults = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/GroundTruthComparisonsFeb22/Clustering1MUTil30/";// 8/Mod2/";
		// executeForMultipleIterationsOfExperiments(ClustersRangeScheme.CLUSTERING1, iterationRootPath, rootPathToWriteResults);
		//
		// rootPathToWriteResults = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/GroundTruthComparisonsFeb22/Clustering2MUTil30/";// 8/Mod2/";
		// executeForMultipleIterationsOfExperiments(ClustersRangeScheme.CLUSTERING2, iterationRootPath, rootPathToWriteResults);
		// //$$UMAP Submission End
		
		// After UMAP corrected TZ experiments start
		String iterationRootPath = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/April21/MUExperimentsBLNCount/";// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/Feb11ImpBLNCount/";//
																														// Feb7ImpIterations/";
		String rootPathToWriteResults =
				"/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/GroundTruthComparisonsApril21/Clustering0MUTil30/";// 8/Mod2/";
		executeForMultipleIterationsOfExperiments(ClustersRangeScheme.CLUSTERING0, iterationRootPath, rootPathToWriteResults);
		
		rootPathToWriteResults = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/GroundTruthComparisonsApril21/Clustering1MUTil30/";// 8/Mod2/";
		executeForMultipleIterationsOfExperiments(ClustersRangeScheme.CLUSTERING1, iterationRootPath, rootPathToWriteResults);
		
		rootPathToWriteResults = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/GroundTruthComparisonsApril21/Clustering2MUTil30/";// 8/Mod2/";
		executeForMultipleIterationsOfExperiments(ClustersRangeScheme.CLUSTERING2, iterationRootPath, rootPathToWriteResults);
		// After UMAP corrected TZ experiments start
	}
	
	public static void main(String args[])
	{
		String commonPathToRead = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/Usable3MUButDWCompatibleRS_";
		String s[] = { "1", "101", "201", "301", "401", "501", "601", "701", "801", "901" };
		try
		{
			for (int i = 0; i < s.length; i++)
			{
				String pathToRead = commonPathToRead + s[i] + "/";
				String clustersRangeSchemeTitle = "CLUSTERING2";
				String pathToWrite = pathToRead + clustersRangeSchemeTitle;
				Files.createDirectories(Paths.get(pathToWrite));
				pathToWrite += "/";
				
				gowallaEvals(pathToRead, pathToWrite, ClustersRangeScheme.CLUSTERING2);
				gowallaEvalsBaselineOccurrence(pathToRead, pathToWrite, ClustersRangeScheme.CLUSTERING2);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void gowallaEvals(String commonPathToRead, String rootPathToWriteResults, ClustersRangeScheme clusteringRangeScheme)
	{
		// String commonPathToRead = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/";
		// String rootPathToWriteResults = commonPathToRead;//
		// "//home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/Usable2MUButDWCompatibleRTS//";
		
		//////////////////////
		WritingToFile.appendLineToFileAbsolute(
				"commonPathToRead = " + commonPathToRead + "\n\n" + WekaUtilityBelt.getClustersRangeSchemeString(clusteringRangeScheme),
				rootPathToWriteResults + "ClustersRangeSchemeUsed.txt");
		
		// (User, (ClusterLabel, Count of iterations in which it is resultant cluster label))
		// .. here the resultant cluster label for an iteration is the cluster label for minimal MU having max MRR
		TreeMap<String, TreeMap<String, Integer>> countsForClusterLabelAccToMinMUHavMaxMRR =
				new TreeMap<String, TreeMap<String, Integer>>(UtilityBelt.getUserIDComparator());
		
		// (User, (ClusterLabel, Count of iterations in which it is resultant cluster label))
		// .. here the resultant cluster label for an iteration is the majority cluster label over all MUs having max MRR
		TreeMap<String, TreeMap<String, Integer>> countsForClusterLabelAccToMajorityMUsHavMaxMRR =
				new TreeMap<String, TreeMap<String, Integer>>(UtilityBelt.getUserIDComparator());
		
		//////////////////////
		
		String mrrForAllUsersAllMUsFileName = rootPathToWriteResults + "AllMRR.csv";
		int numOfUsers = WritingToFile.writeMRRForAllUsersAllMUs(commonPathToRead, mrrForAllUsersAllMUsFileName, "Algo");
		
		String MUsByDescendingMRRFileName = rootPathToWriteResults + "MUsByDescendingMRR.csv";
		
		// (UserID, Pair( MUs having Max MRR, max MRR))
		LinkedHashMap<String, Pair<List<Double>, Double>> usersMaxMUMRRMap =
				WritingToFile.writeDescendingMRRs(mrrForAllUsersAllMUsFileName, MUsByDescendingMRRFileName, numOfUsers, true, true);
		// (String absFileNameToRead, String absFileNameToWrite, int numberOfUsers, boolean hasRowHeader, boolean booleanHasColHeader)
		
		String MUsWithMaxMRRFileName = rootPathToWriteResults + "MUsWithMaxMRR.csv";
		WritingToFile.appendLineToFileAbsolute(
				"User, MUsWithMaxMRR,MaxMRR, MinMUHavingMaxMRR, ClusterLabelAccToMinMUHavMaxMRR,ClusterLabelAccToMajorityMUsHavMaxMRR\n",
				MUsWithMaxMRRFileName);
		
		for (Entry<String, Pair<List<Double>, Double>> entryForUser : usersMaxMUMRRMap.entrySet()) // iterating over users
		{
			String user = entryForUser.getKey();
			List<Double> MUsHavingMaxMRR = entryForUser.getValue().getFirst();
			String MUsHavingMaxMRRAsString = MUsHavingMaxMRR.stream().map(Object::toString).collect(Collectors.joining("__"));
			
			Double maxMRR = entryForUser.getValue().getSecond();
			Double minMUHavingMaxMRR = Collections.min(MUsHavingMaxMRR);
			
			////////////
			String clusterLabelAccToMinMUHavMaxMRR = WekaUtilityBelt.getClusterLabel(minMUHavingMaxMRR, clusteringRangeScheme);// getClusterLabel(minMUHavingMaxMRR);
			
			String clusterLabelAccToMajorityMUsHavMaxMRR = getClusterLabelForMajorityMUs(MUsHavingMaxMRR, clusteringRangeScheme);
			////////////
			
			WritingToFile.appendLineToFileAbsolute(user + "," + MUsHavingMaxMRRAsString + "," + maxMRR + "," + minMUHavingMaxMRR + ","
					+ clusterLabelAccToMinMUHavMaxMRR + "," + clusterLabelAccToMajorityMUsHavMaxMRR + "\n", MUsWithMaxMRRFileName);
			
			TreeMap<String, Integer> mapOfClusterCountsForThisUserMinMU, mapOfClusterCountsForThisUserMajMU;
			
			countsForClusterLabelAccToMinMUHavMaxMRR =
					incrementCount(user, clusterLabelAccToMinMUHavMaxMRR, countsForClusterLabelAccToMinMUHavMaxMRR);
			countsForClusterLabelAccToMajorityMUsHavMaxMRR =
					incrementCount(user, clusterLabelAccToMajorityMUsHavMaxMRR, countsForClusterLabelAccToMajorityMUsHavMaxMRR);
			
		} // end of iteration over users. }
		
		////
		writeCounts(countsForClusterLabelAccToMinMUHavMaxMRR, rootPathToWriteResults + "CountsForClusterLabelAccToMinMUHavMaxMRR.csv");
		writeModeDistribution(countsForClusterLabelAccToMinMUHavMaxMRR,
				rootPathToWriteResults + "ModeDistributionForClusterLabelAccToMinMUHavMaxMRR.csv");
		
		writeCounts(countsForClusterLabelAccToMajorityMUsHavMaxMRR,
				rootPathToWriteResults + "CountsForClusterLabelAccToMajorityMUsHavMaxMRR.csv");
		writeModeDistribution(countsForClusterLabelAccToMajorityMUsHavMaxMRR,
				rootPathToWriteResults + "ModeDistributionForClusterLabelAccToMajorityMUsHavMaxMRR.csv");
	}
	
	public static void gowallaEvalsBaselineOccurrence(String commonPathToRead, String rootPathToWriteResults,
			ClustersRangeScheme clusteringRangeScheme)
	{
		// String commonPathToRead = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/";
		// String rootPathToWriteResults = commonPathToRead;//
		// "//home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov30_2/Usable2MUButDWCompatibleRTS//";
		
		//////////////////////
		WritingToFile.appendLineToFileAbsolute(
				"commonPathToRead = " + commonPathToRead + "\n\n" + WekaUtilityBelt.getClustersRangeSchemeString(clusteringRangeScheme),
				rootPathToWriteResults + "ClustersRangeSchemeUsed.txt");
		
		// (User, (ClusterLabel, Count of iterations in which it is resultant cluster label))
		// .. here the resultant cluster label for an iteration is the cluster label for minimal MU having max MRR
		TreeMap<String, TreeMap<String, Integer>> countsForClusterLabelAccToMinMUHavMaxMRR =
				new TreeMap<String, TreeMap<String, Integer>>(UtilityBelt.getUserIDComparator());
		
		// (User, (ClusterLabel, Count of iterations in which it is resultant cluster label))
		// .. here the resultant cluster label for an iteration is the majority cluster label over all MUs having max MRR
		TreeMap<String, TreeMap<String, Integer>> countsForClusterLabelAccToMajorityMUsHavMaxMRR =
				new TreeMap<String, TreeMap<String, Integer>>(UtilityBelt.getUserIDComparator());
		
		//////////////////////
		
		String mrrForAllUsersAllMUsFileName = rootPathToWriteResults + "BOAllMRR.csv";
		int numOfUsers = WritingToFile.writeMRRForAllUsersAllMUs(commonPathToRead, mrrForAllUsersAllMUsFileName, "BaselineOccurrence");
		
		String MUsByDescendingMRRFileName = rootPathToWriteResults + "BOMUsByDescendingMRR.csv";
		
		// (UserID, Pair( MUs having Max MRR, max MRR))
		LinkedHashMap<String, Pair<List<Double>, Double>> usersMaxMUMRRMap =
				WritingToFile.writeDescendingMRRs(mrrForAllUsersAllMUsFileName, MUsByDescendingMRRFileName, numOfUsers, true, true);
		// (String absFileNameToRead, String absFileNameToWrite, int numberOfUsers, boolean hasRowHeader, boolean booleanHasColHeader)
		
		String MUsWithMaxMRRFileName = rootPathToWriteResults + "BOMUsWithMaxMRR.csv";
		WritingToFile.appendLineToFileAbsolute(
				"User, MUsWithMaxMRR,MaxMRR, MinMUHavingMaxMRR, ClusterLabelAccToMinMUHavMaxMRR,ClusterLabelAccToMajorityMUsHavMaxMRR\n",
				MUsWithMaxMRRFileName);
		
		for (Entry<String, Pair<List<Double>, Double>> entryForUser : usersMaxMUMRRMap.entrySet()) // iterating over users
		{
			String user = entryForUser.getKey();
			List<Double> MUsHavingMaxMRR = entryForUser.getValue().getFirst();
			String MUsHavingMaxMRRAsString = MUsHavingMaxMRR.stream().map(Object::toString).collect(Collectors.joining("__"));
			
			Double maxMRR = entryForUser.getValue().getSecond();
			Double minMUHavingMaxMRR = Collections.min(MUsHavingMaxMRR);
			
			////////////
			String clusterLabelAccToMinMUHavMaxMRR = WekaUtilityBelt.getClusterLabel(minMUHavingMaxMRR, clusteringRangeScheme);// getClusterLabel(minMUHavingMaxMRR);
			
			String clusterLabelAccToMajorityMUsHavMaxMRR = getClusterLabelForMajorityMUs(MUsHavingMaxMRR, clusteringRangeScheme);
			////////////
			
			WritingToFile.appendLineToFileAbsolute(user + "," + MUsHavingMaxMRRAsString + "," + maxMRR + "," + minMUHavingMaxMRR + ","
					+ clusterLabelAccToMinMUHavMaxMRR + "," + clusterLabelAccToMajorityMUsHavMaxMRR + "\n", MUsWithMaxMRRFileName);
			
			TreeMap<String, Integer> mapOfClusterCountsForThisUserMinMU, mapOfClusterCountsForThisUserMajMU;
			
			countsForClusterLabelAccToMinMUHavMaxMRR =
					incrementCount(user, clusterLabelAccToMinMUHavMaxMRR, countsForClusterLabelAccToMinMUHavMaxMRR);
			countsForClusterLabelAccToMajorityMUsHavMaxMRR =
					incrementCount(user, clusterLabelAccToMajorityMUsHavMaxMRR, countsForClusterLabelAccToMajorityMUsHavMaxMRR);
			
		} // end of iteration over users. }
		
		////
		writeCounts(countsForClusterLabelAccToMinMUHavMaxMRR, rootPathToWriteResults + "BOCountsForClusterLabelAccToMinMUHavMaxMRR.csv");
		writeModeDistribution(countsForClusterLabelAccToMinMUHavMaxMRR,
				rootPathToWriteResults + "BOModeDistributionForClusterLabelAccToMinMUHavMaxMRR.csv");
		
		writeCounts(countsForClusterLabelAccToMajorityMUsHavMaxMRR,
				rootPathToWriteResults + "BOCountsForClusterLabelAccToMajorityMUsHavMaxMRR.csv");
		writeModeDistribution(countsForClusterLabelAccToMajorityMUsHavMaxMRR,
				rootPathToWriteResults + "BOModeDistributionForClusterLabelAccToMajorityMUsHavMaxMRR.csv");
	}
	
	/**
	 * For running over multiple iterations of recommendation experiments
	 * 
	 * @param clusteringRangeScheme
	 * @param iterationRootPath
	 * @param rootPathToWriteResults
	 */
	public static void executeForMultipleIterationsOfExperiments(ClustersRangeScheme clusteringRangeScheme, String iterationRootPath,
			String rootPathToWriteResults)
	{
		// String iterationRootPath = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/Feb11ImpBLNCount/";// Feb7ImpIterations/";
		// String rootPathToWriteResults = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/GroundTruthComparisonsFeb17/Clustering2MUTil15/";// 8/Mod2/";
		
		WritingToFile.appendLineToFileAbsolute(
				"iterationRootPath = " + iterationRootPath + "\n\n" + WekaUtilityBelt.getClustersRangeSchemeString(clusteringRangeScheme),
				rootPathToWriteResults + "ClustersRangeSchemeUsed.txt");
		
		// (User, (ClusterLabel, Count of iterations in which it is resultant cluster label))
		// .. here the resultant cluster label for an iteration is the cluster label for minimal MU having max MRR
		TreeMap<String, TreeMap<String, Integer>> countsForClusterLabelAccToMinMUHavMaxMRR =
				new TreeMap<String, TreeMap<String, Integer>>(UtilityBelt.getUserIDComparator());
		
		// (User, (ClusterLabel, Count of iterations in which it is resultant cluster label))
		// .. here the resultant cluster label for an iteration is the majority cluster label over all MUs having max MRR
		TreeMap<String, TreeMap<String, Integer>> countsForClusterLabelAccToMajorityMUsHavMaxMRR =
				new TreeMap<String, TreeMap<String, Integer>>(UtilityBelt.getUserIDComparator());
		
		for (int iter = 1; iter <= 10/* 20 */; iter++)
		{
			String iterationMURootPath = iterationRootPath + "Iteration" + iter + "/";
			
			String mrrForAllUsersAllMUsFileName = rootPathToWriteResults + "Iteration" + iter + "AllMRR.csv";
			int numOfUsers = WritingToFile.writeMRRForAllUsersAllMUs(iterationMURootPath, mrrForAllUsersAllMUsFileName, "Algo");
			
			String MUsByDescendingMRRFileName = rootPathToWriteResults + "Iteration" + iter + "MUsByDescendingMRR.csv";
			// (UserID, Pair( MUs having Max MRR, max MRR))
			LinkedHashMap<String, Pair<List<Double>, Double>> usersMaxMUMRRMap =
					WritingToFile.writeDescendingMRRs(mrrForAllUsersAllMUsFileName, MUsByDescendingMRRFileName, numOfUsers, true, true);
			// (String absFileNameToRead, String absFileNameToWrite, int numberOfUsers, boolean hasRowHeader, boolean booleanHasColHeader)
			
			String MUsWithMaxMRRFileName = rootPathToWriteResults + "Iteration" + iter + "MUsWithMaxMRR.csv";
			WritingToFile.appendLineToFileAbsolute(
					"User, MUsWithMaxMRR,MaxMRR, MinMUHavingMaxMRR, ClusterLabelAccToMinMUHavMaxMRR,ClusterLabelAccToMajorityMUsHavMaxMRR\n",
					MUsWithMaxMRRFileName);
			
			for (Entry<String, Pair<List<Double>, Double>> entryForUser : usersMaxMUMRRMap.entrySet()) // iterating over users
			{
				String user = entryForUser.getKey();
				List<Double> MUsHavingMaxMRR = entryForUser.getValue().getFirst();
				String MUsHavingMaxMRRAsString = MUsHavingMaxMRR.stream().map(Object::toString).collect(Collectors.joining("__"));
				
				Double maxMRR = entryForUser.getValue().getSecond();
				
				Double minMUHavingMaxMRR = Collections.min(MUsHavingMaxMRR);
				String clusterLabelAccToMinMUHavMaxMRR = WekaUtilityBelt.getClusterLabel(minMUHavingMaxMRR, clusteringRangeScheme);// getClusterLabel(minMUHavingMaxMRR);
				
				String clusterLabelAccToMajorityMUsHavMaxMRR = getClusterLabelForMajorityMUs(MUsHavingMaxMRR, clusteringRangeScheme);
				
				WritingToFile.appendLineToFileAbsolute(
						user + "," + MUsHavingMaxMRRAsString + "," + maxMRR + "," + minMUHavingMaxMRR + ","
								+ clusterLabelAccToMinMUHavMaxMRR + "," + clusterLabelAccToMajorityMUsHavMaxMRR + "\n",
						MUsWithMaxMRRFileName);
				
				TreeMap<String, Integer> mapOfClusterCountsForThisUserMinMU, mapOfClusterCountsForThisUserMajMU;
				
				countsForClusterLabelAccToMinMUHavMaxMRR =
						incrementCount(user, clusterLabelAccToMinMUHavMaxMRR, countsForClusterLabelAccToMinMUHavMaxMRR);
				countsForClusterLabelAccToMajorityMUsHavMaxMRR =
						incrementCount(user, clusterLabelAccToMajorityMUsHavMaxMRR, countsForClusterLabelAccToMajorityMUsHavMaxMRR);
				
			} // end of iteration over users.
			
		} // end of iteration over iteration of resuls
		
		writeCounts(countsForClusterLabelAccToMinMUHavMaxMRR, rootPathToWriteResults + "CountsForClusterLabelAccToMinMUHavMaxMRR.csv");
		writeModeDistribution(countsForClusterLabelAccToMinMUHavMaxMRR,
				rootPathToWriteResults + "ModeDistributionForClusterLabelAccToMinMUHavMaxMRR.csv");
		
		writeCounts(countsForClusterLabelAccToMajorityMUsHavMaxMRR,
				rootPathToWriteResults + "CountsForClusterLabelAccToMajorityMUsHavMaxMRR.csv");
		writeModeDistribution(countsForClusterLabelAccToMajorityMUsHavMaxMRR,
				rootPathToWriteResults + "ModeDistributionForClusterLabelAccToMajorityMUsHavMaxMRR.csv");
		// /
		//
	}
	
	/**
	 * For running over single iteration of recommendation experiment
	 */
	public static void executeForSingleExperiment(ClustersRangeScheme clusteringRangeScheme)
	{
		
		String iterationRootPath = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/June18HJDistance/Geolife/SimpleV3/";// "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/June";//Feb7ImpIterations/";
		String rootPathToWriteResults = "/run/media/gunjan/Space/GUNJAN/GeolifeSpaceSpace/ComparisonsFeb8/June18/";
		WritingToFile.appendLineToFileAbsolute(WekaUtilityBelt.getClustersRangeSchemeString(clusteringRangeScheme),
				rootPathToWriteResults + "ClustersRangeSchemeUsed.txt");
		
		String iterationMURootPath = iterationRootPath;// + "Iteration" + iter + "/";
		
		String mrrForAllUsersAllMUsFileName = rootPathToWriteResults + "AllMRR.csv";
		int numOfUsers = WritingToFile.writeMRRForAllUsersAllMUs(iterationMURootPath, mrrForAllUsersAllMUsFileName, "Algo");
		
		String MUsByDescendingMRRFileName = rootPathToWriteResults + "MUsByDescendingMRR.csv";
		
		// (UserID, Pair( MUs having Max MRR, max MRR))
		LinkedHashMap<String, Pair<List<Double>, Double>> usersMaxMUMRRMap =
				WritingToFile.writeDescendingMRRs(mrrForAllUsersAllMUsFileName, MUsByDescendingMRRFileName, numOfUsers, true, true);
		// (String absFileNameToRead, String absFileNameToWrite, int numberOfUsers, boolean hasRowHeader, boolean booleanHasColHeader)
		
		String MUsWithMaxMRRFileName = rootPathToWriteResults + "MUsWithMaxMRR.csv";
		
		WritingToFile.appendLineToFileAbsolute(
				"User, MUsWithMaxMRR,MaxMRR, MinMUHavingMaxMRR, ClusterLabelAccToMinMUHavMaxMRR,ClusterLabelAccToMajorityMUsHavMaxMRR\n",
				MUsWithMaxMRRFileName);
		
		for (Entry<String, Pair<List<Double>, Double>> entryForUser : usersMaxMUMRRMap.entrySet()) // iterating over users
		{
			String user = entryForUser.getKey();
			List<Double> MUsHavingMaxMRR = entryForUser.getValue().getFirst();
			String MUsHavingMaxMRRAsString = MUsHavingMaxMRR.stream().map(Object::toString).collect(Collectors.joining("__"));
			
			Double maxMRR = entryForUser.getValue().getSecond();
			
			Double minMUHavingMaxMRR = Collections.min(MUsHavingMaxMRR);
			// String clusterLabelAccToMinMUHavMaxMRR = WekaUtilityBelt.getClusterLabelClustering0(minMUHavingMaxMRR);// getClusterLabel(minMUHavingMaxMRR);
			String clusterLabelAccToMinMUHavMaxMRR = WekaUtilityBelt.getClusterLabel(minMUHavingMaxMRR, clusteringRangeScheme);// getClusterLabel(minMUHavingMaxMRR);
			String clusterLabelAccToMajorityMUsHavMaxMRR = getClusterLabelForMajorityMUs(MUsHavingMaxMRR, clusteringRangeScheme);
			
			WritingToFile.appendLineToFileAbsolute(user + "," + MUsHavingMaxMRRAsString + "," + maxMRR + "," + minMUHavingMaxMRR + ","
					+ clusterLabelAccToMinMUHavMaxMRR + "," + clusterLabelAccToMajorityMUsHavMaxMRR + "\n", MUsWithMaxMRRFileName);
			
			TreeMap<String, Integer> mapOfClusterCountsForThisUserMinMU, mapOfClusterCountsForThisUserMajMU;
			
		} // end of iteration over iteration of resuls
		
	}
	
	/**
	 * 
	 * @param countsForClusterLabelAccToMinMUHavMaxMRR
	 * @param fileNameToWrite
	 */
	private static void writeCounts(TreeMap<String, TreeMap<String, Integer>> countsForClusterLabelAccToMinMUHavMaxMRR,
			String fileNameToWrite)
	
	{
		WritingToFile.appendLineToFileAbsolute("User, FirstClusterCount,SecondClusterCount,ThirdClusterCount, ModeCluster\n",
				fileNameToWrite);
		
		int countFirstClusterAsMode = 0, countSecondClusterAsMode = 0, countThirdClusterAsMode = 0;
		
		for (Entry<String, TreeMap<String, Integer>> entryForuser : countsForClusterLabelAccToMinMUHavMaxMRR.entrySet())
		{
			// replacing null by zero
			int countForFirstCluster =
					entryForuser.getValue().get("FirstCluster") != null ? entryForuser.getValue().get("FirstCluster") : 0;
			int countForSecondCluster =
					entryForuser.getValue().get("SecondCluster") != null ? entryForuser.getValue().get("SecondCluster") : 0;
			int countForThirdCluster =
					entryForuser.getValue().get("ThirdCluster") != null ? entryForuser.getValue().get("ThirdCluster") : 0;
			
			String modeCluster = ""; // to find the maximum occurring clusters, note: allowing for more than one cluster to occur as max
			modeCluster += UtilityBelt.isMaximum(countForFirstCluster, countForFirstCluster, countForSecondCluster, countForThirdCluster)
					? "FirstCluster__" : "";
			modeCluster += UtilityBelt.isMaximum(countForSecondCluster, countForFirstCluster, countForSecondCluster, countForThirdCluster)
					? "SecondCluster__" : "";
			modeCluster += UtilityBelt.isMaximum(countForThirdCluster, countForFirstCluster, countForSecondCluster, countForThirdCluster)
					? "ThirdCluster__" : "";
			
			String msg = entryForuser.getKey() + "," + countForFirstCluster + "," + countForSecondCluster + "," + countForThirdCluster + ","
					+ modeCluster + "\n";
			
			WritingToFile.appendLineToFileAbsolute(msg, fileNameToWrite);
		}
		
	}
	
	/**
	 * Increment the count of the given cluster label in given count map.
	 * 
	 * This method is to be called for each user for each iteration of the MU experiments.
	 * 
	 * @param user
	 * @param clusterLabel
	 *            cluster label whose count need to be incremented. Note that in some cases like those of using majority cluster, there might be multiple cluster labels separated
	 *            by "__"
	 * @param countsForClusterLabels
	 *            TreeMap(User, (ClusterLabel, CountOfIterations which have this cluster label))
	 * @return TreeMap(User, (ClusterLabel, CountOfIterations which have this cluster label))
	 */
	private static TreeMap<String, TreeMap<String, Integer>> incrementCount(String user, String clusterLabelReceived,
			TreeMap<String, TreeMap<String, Integer>> countsForClusterLabels)
	{
		String splittedClusterLabels[] = clusterLabelReceived.split("__"); // to take care of multiple clusters
		
		for (int i = 0; i < splittedClusterLabels.length; i++)
		{
			String clusterLabel = splittedClusterLabels[i];
			
			// (ClusterLabel, Count over all iterations)
			TreeMap<String, Integer> mapForClusterLabelCounts;
			
			if (countsForClusterLabels.containsKey(user) == false)
			{
				mapForClusterLabelCounts = new TreeMap<String, Integer>();
			}
			else
			{
				mapForClusterLabelCounts = countsForClusterLabels.get(user);
			}
			
			if (mapForClusterLabelCounts.containsKey(clusterLabel) == false)
			{
				mapForClusterLabelCounts.put(clusterLabel, 1);
			}
			
			else
			{
				mapForClusterLabelCounts.put(clusterLabel, mapForClusterLabelCounts.get(clusterLabel) + 1);
			}
			
			countsForClusterLabels.put(user, mapForClusterLabelCounts);// update/replace
		}
		return countsForClusterLabels;
	}
	
	private static TreeMap<String, TreeMap<String, Integer>>
			initialiseCounts(TreeMap<String, TreeMap<String, Integer>> countsForClusterLabelAccToMinMUHavMaxMRR)
	{
		
		return countsForClusterLabelAccToMinMUHavMaxMRR;
		
	}
	
	/**
	 * Writes number of user in each cluster (where the clusters are assigned as the mode over all iterations)
	 * 
	 * @param countsForClusterLabelAccToMinMUHavMaxMRR
	 * @param fileNameToWrite
	 */
	private static void writeModeDistribution(TreeMap<String, TreeMap<String, Integer>> countsForClusterLabelAccToMinMUHavMaxMRR,
			String fileNameToWrite)
	
	{
		WritingToFile.appendLineToFileAbsolute("FirstClusterMode,SecondClusterMode,ThirdClusterMode\n", fileNameToWrite);
		
		LinkedHashMap<String, Integer> clusterCounts = new LinkedHashMap<String, Integer>();
		
		int countFirstClusterAsMode = 0, countSecondClusterAsMode = 0, countThirdClusterAsMode = 0;
		
		for (Entry<String, TreeMap<String, Integer>> entryForuser : countsForClusterLabelAccToMinMUHavMaxMRR.entrySet())
		{
			// replacing null by zero
			int countForFirstCluster =
					entryForuser.getValue().get("FirstCluster") != null ? entryForuser.getValue().get("FirstCluster") : 0;
			int countForSecondCluster =
					entryForuser.getValue().get("SecondCluster") != null ? entryForuser.getValue().get("SecondCluster") : 0;
			int countForThirdCluster =
					entryForuser.getValue().get("ThirdCluster") != null ? entryForuser.getValue().get("ThirdCluster") : 0;
			
			String modeCluster = ""; // to find the maximum occurring clusters, note: allowing for more than one cluster to occur as max
			modeCluster += UtilityBelt.isMaximum(countForFirstCluster, countForFirstCluster, countForSecondCluster, countForThirdCluster)
					? "FirstCluster__" : "";
			modeCluster += UtilityBelt.isMaximum(countForSecondCluster, countForFirstCluster, countForSecondCluster, countForThirdCluster)
					? "SecondCluster__" : "";
			modeCluster += UtilityBelt.isMaximum(countForThirdCluster, countForFirstCluster, countForSecondCluster, countForThirdCluster)
					? "ThirdCluster__" : "";
			
			if (modeCluster.contains("FirstCluster"))
			{
				countFirstClusterAsMode++;
			}
			if (modeCluster.contains("SecondCluster"))
			{
				countSecondClusterAsMode++;
			}
			if (modeCluster.contains("ThirdCluster"))
			{
				countThirdClusterAsMode++;
			}
			
		}
		String msg = countFirstClusterAsMode + "," + countSecondClusterAsMode + "," + countThirdClusterAsMode + "\n";
		
		WritingToFile.appendLineToFileAbsolute(msg, fileNameToWrite);
		
	}
	
	/**
	 * Finds cluster label associated with each of the given MUs and returns cluster labels (separated by "__") having highest occurrence. If multiple cluster labels have highest
	 * occurrence, then all of these are returned.
	 * 
	 * Returns the cluster labels which is the mode of cluster labels assigned to all the given MUs
	 * 
	 * @param mUsHavingMaxMRR
	 * @param clusteringRangeScheme
	 * @return
	 */
	private static String getClusterLabelForMajorityMUs(List<Double> mUsHavingMaxMRR, ClustersRangeScheme clusteringRangeScheme)
	{
		String resClusterLabel = "";
		
		TreeMap<String, Integer> countsForClusters = new TreeMap<String, Integer>();
		
		for (Double mu : mUsHavingMaxMRR)
		{
			String clusterLabel = WekaUtilityBelt.getClusterLabel(mu, clusteringRangeScheme);// getClusterLabel(mu);
			
			if (countsForClusters.containsKey(clusterLabel))
			{
				countsForClusters.put(clusterLabel, countsForClusters.get(clusterLabel) + 1);
			}
			else
			{
				countsForClusters.put(clusterLabel, 1);
			}
		}
		
		Integer clusterWithMaxCounts = Collections.max(countsForClusters.values());
		System.out.println("Inside getClusterLabelForMajorityMUs()\nAll Labels:  ");
		for (Entry<String, Integer> entryForCluster : countsForClusters.entrySet())
		{
			System.out.println(entryForCluster.getKey() + " -- count: " + entryForCluster.getValue());
			
			if (entryForCluster.getValue() == clusterWithMaxCounts)
			{
				resClusterLabel += entryForCluster.getKey() + "__";
			}
		}
		System.out.println("Resultant majority cluster: " + resClusterLabel);
		System.out.println("Exiting getClusterLabelForMajorityMUs()\n");
		return resClusterLabel;
	}
	
}
