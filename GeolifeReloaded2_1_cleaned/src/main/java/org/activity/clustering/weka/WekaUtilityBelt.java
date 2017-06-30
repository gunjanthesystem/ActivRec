package org.activity.clustering.weka;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.activity.constants.ClusteringConstants;
import org.activity.constants.Constant;
import org.activity.io.ReadingFromFile;
import org.activity.ui.PopUps;

import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.RemoveByName;
import weka.filters.unsupervised.instance.RemoveRange;

public class WekaUtilityBelt
{

	static ClustersRangeScheme clustersRangeSchemeProvided; // used for sanity checks

	/**
	 * Clustering0: 0-1, 2-5, 6-inf (original, in Dec2015 paper draft, ManualClustering2);</br>
	 * Clustering1: 0, 1-3, 4-inf ; </br>
	 * Clustering2: 0-1, 2-4, 5-inf
	 * 
	 * @author gunjan
	 *
	 */
	public enum ClustersRangeScheme
	{
		CLUSTERING0, CLUSTERING1, CLUSTERING2
	}

	public static String getClustersRangeSchemeString(ClustersRangeScheme clustersRangeScheme)
	{
		String s = null, c = null;

		s += " Clusters Range Scheme: " + clustersRangeScheme.toString() + "\n";
		switch (clustersRangeScheme)
		{
		case CLUSTERING0:
			c = "\tFirstCl\tSecondCl\tThirdCl\n\t0-1\t2-5\t6-inf\n";
			break;
		case CLUSTERING1:
			c = "\tFirstCl\tSecondCl\tThirdCl\n\t0\t1-3\t4-inf\n";
			break;
		case CLUSTERING2:
			c = "\tFirstCl\tSecondCl\tThirdCl\n\t0-1\t2-4\t5-inf\n";
			break;
		default:
			PopUps.showError("Error in getClustersRangeSchemeString().\n Unrecognised clustering scheme passed: "
					+ clustersRangeScheme.toString());
		}

		return (s + c + "\n");
	}

	/**
	 * Returns the cluster label based on the given best MU value
	 *
	 * @param MUVal
	 * @param clustersRangeScheme
	 * @return
	 */
	public static String getClusterLabel(Double MUVal, ClustersRangeScheme clustersRangeScheme)
	{
		clustersRangeSchemeProvided = clustersRangeScheme;
		switch (clustersRangeScheme)
		{
		case CLUSTERING0:
			return getClusterLabelClustering0(MUVal);
		case CLUSTERING1:
			return getClusterLabelClustering1(MUVal);
		case CLUSTERING2:
			return getClusterLabelClustering2(MUVal);
		default:
			PopUps.showError(
					"Error in org.activity.tests.MUEvaluationUtils.getClusterLabelForMajorityMUs().\n Unrecognised clustering scheme passed: "
							+ clustersRangeScheme.toString());
		}
		return null;
	}

	/**
	 * Returns the cluster label based on the given best MU value
	 * 
	 * @param MUVal
	 * @return
	 */
	public static String getClusterLabelClustering0(Double MUVal)
	{
		if (clustersRangeSchemeProvided.equals(ClustersRangeScheme.CLUSTERING0) == false)
		{
			PopUps.showError("This shouldnt be called");
		}

		String clusterLabel = null;

		// if (MUVal >= Constant.cluster1Min && MUVal <= Constant.cluster1Max)
		// clusterLabel = "FirstCluster";
		// else if (MUVal >= Constant.cluster2Min && MUVal <= Constant.cluster2Max)
		// clusterLabel = "SecondCluster";
		// else if (MUVal >= Constant.cluster3Min && MUVal <= Constant.cluster3Max)
		// clusterLabel = "ThirdCluster";
		if (MUVal >= 0 && MUVal <= 1)
			clusterLabel = "FirstCluster";
		else if (MUVal >= 2 && MUVal <= 4) // CHANGED TO 4
			clusterLabel = "SecondCluster";
		else if (MUVal >= 5 && MUVal <= Integer.MAX_VALUE)
			clusterLabel = "ThirdCluster";
		else
			PopUps.showError("Error in getClusterLabelClustering0. " + MUVal + " not falling inside any cluster");

		return clusterLabel;
	}

	/**
	 * Returns the cluster label based on the given best MU value
	 * 
	 * @param MUVal
	 * @return
	 */
	public static String getClusterLabelClustering1(Double MUVal)
	{
		// PopUps.showError("This shouldnt be called");
		if (clustersRangeSchemeProvided.equals(ClustersRangeScheme.CLUSTERING1) == false)
		{
			PopUps.showError("This shouldnt be called");
		}

		String clusterLabel = null;

		if (MUVal == 0)
			clusterLabel = "FirstCluster";
		else if (MUVal >= 1 && MUVal <= 3)
			clusterLabel = "SecondCluster";
		else if (MUVal >= 4 && MUVal <= Integer.MAX_VALUE)
			clusterLabel = "ThirdCluster";
		else
			PopUps.showError("Error in getClusterLabelClustering1. " + MUVal + " not falling inside any cluster");

		return clusterLabel;
	}

	/**
	 * Returns the cluster label based on the given best MU value
	 * 
	 * @param MUVal
	 * @return
	 */
	public static String getClusterLabelClustering2(Double MUVal)
	{
		// PopUps.showError("This shouldnt be called");
		if (clustersRangeSchemeProvided.equals(ClustersRangeScheme.CLUSTERING2) == false)
		{
			PopUps.showError("This shouldnt be called");
		}

		String clusterLabel = null;

		if (MUVal >= 0 && MUVal <= 1)
			clusterLabel = "FirstCluster";
		else if (MUVal >= 2 && MUVal <= 4)
			clusterLabel = "SecondCluster";
		else if (MUVal >= 5 && MUVal <= ClusteringConstants.cluster3Max)
			clusterLabel = "ThirdCluster";
		else
		{
			clusterLabel = "NoCluster";
			System.err.println(PopUps.getTracedErrorMsg(
					"Error in getClusterLabelClustering2 " + MUVal + " not falling inside any cluster"));
		}
		return clusterLabel;
	}

	/**
	 * Reads the given file and the column at the given index is read as the manual clusters to be used as class labels
	 * for the user. Returns the map containg (raw user id, manual cluster) </br>
	 * <font color="red">ALERT: ensure that the order of users is correct in ground truth file</font>
	 * 
	 * @param indexOfColumn
	 *            starts from 0
	 * @param selectedUsers
	 * @param absFileNameToRead
	 * @param hasColHeader
	 *            whether the file to be read has column header
	 * 
	 * @return the map containg (raw user id, manual cluster)
	 */
	public static LinkedHashMap<String, String> getGroundTruth(int indexOfColumn, ArrayList<String> selectedUsers,
			String absFileNameToRead, boolean hasColHeader)
	{
		LinkedHashMap<String, String> clustering1 = new LinkedHashMap<String, String>();
		String fileName = absFileNameToRead;// "/run/media/gunjan/HOME/gunjan/Geolife Data
											// Works/stats/wekaResults/ManualClustersUserAbove10RTs.csv";

		System.out.println("\n Ground truth is being read from file: " + fileName + " at column " + indexOfColumn);
		List<String> clusterLabel = ReadingFromFile.oneColumnReaderString(fileName, ",", indexOfColumn, hasColHeader); // ALERT:
																														// ensure
																														// that
																														// the
																														// order
		// of user is correct (alternative use two column reader with LinkadedHashMap

		int[] userIDs = Constant.getUserIDs(); // getting the raw user IDs

		for (int i = 0; i < clusterLabel.size(); i++)
		{
			if (selectedUsers.contains(String.valueOf(userIDs[i])))
			{
				String clusterLabelForThisUser;
				if (clusterLabel.get(i).split("__").length > 1)
				{
					// PopUps.showError("Error in getManualClustering. More than one cluster labels");
					clusterLabelForThisUser = getOneRandomlyIfMoreThanOne(clusterLabel.get(i), "__");
				}

				clusterLabelForThisUser = clusterLabel.get(i).replace("__", "");

				clustering1.put(String.valueOf(userIDs[i]), clusterLabelForThisUser);
				System.out.println("user id = " + userIDs[i] + "\t manual cluster label = " + clusterLabelForThisUser);
			}
		}
		return clustering1;
	}

	/**
	 * 
	 * @param indexOfUser
	 * @param indexOfGroupLabel
	 * @param absFileNameToRead
	 * @param hasColHeader
	 * @return
	 */
	public static LinkedHashMap<String, String> getGroundTruthGowalla(int indexOfUser, int indexOfGroupLabel,
			String absFileNameToRead, boolean hasColHeader)
	{
		LinkedHashMap<String, String> clustering1 = new LinkedHashMap<String, String>();
		String fileName = absFileNameToRead;// "/run/media/gunjan/HOME/gunjan/Geolife Data
											// Works/stats/wekaResults/ManualClustersUserAbove10RTs.csv";

		System.out.println("\n Ground truth is being read from file: " + fileName + " at column " + indexOfGroupLabel);
		List<String> clusterLabels =
				ReadingFromFile.oneColumnReaderString(fileName, "\t", indexOfGroupLabel, hasColHeader);
		System.out.println("clusterLabels= " + clusterLabels.toString());

		List<String> userLabels = ReadingFromFile.oneColumnReaderString(fileName, "\t", indexOfUser, hasColHeader);
		System.out.println("userLabels= " + userLabels.toString());

		for (int i = 0; i < userLabels.size(); i++)
		{
			// String userID = userLabels.get(i).replace("U", "");
			clustering1.put(userLabels.get(i), clusterLabels.get(i));
		}

		return clustering1;

	}

	/**
	 * If there are more than one predicted class and then pick one randonly
	 * 
	 * @param bigString
	 * @param delimiter
	 * @return
	 */
	private static String getOneRandomlyIfMoreThanOne(String bigString, String delimiter)
	{
		String splitted[] = bigString.split(delimiter);
		int len = splitted.length;
		if (len == 1)
		{
			return bigString;
		}
		else
		{
			int randomNum = 0 + (int) (Math.random() * (len - 1));
			return splitted[randomNum];
		}

	}

	/**
	 * Removes attribute at the given indices from the given data instances
	 * 
	 * @param originalDataInstances
	 * @param attributeRangeList
	 *            a string representing the list of attribute indices (beginning from 1).<br>
	 *            eg: first-3,5,6-last
	 * @return
	 */
	public static Instances removeAttributesByRangeList(Instances originalDataInstances, String attributeRangeList)
	{
		Instances instNew = null;
		Remove remove;

		remove = new Remove();
		remove.setAttributeIndices(attributeRangeList);

		// remove.setInvertSelection(new Boolean(args[2]).booleanValue());
		try
		{
			remove.setInputFormat(originalDataInstances);
			instNew = Filter.useFilter(originalDataInstances, remove);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return (instNew);
	}

	/**
	 * 
	 * @param originalDataInstances
	 * @param attributeRangeList
	 * @return
	 */
	public static Instances selectAttributesByRangeList(Instances originalDataInstances, String attributeRangeList)
	{
		// PopUps.showMessage("Inside selectAttributesByRangeList");
		Instances instNew = null;
		Remove remove;

		remove = new Remove();
		remove.setAttributeIndices(attributeRangeList);
		remove.setInvertSelection(true);
		// remove.setInvertSelection(new Boolean(args[2]).booleanValue());
		try
		{
			boolean result = remove.setInputFormat(originalDataInstances);
			instNew = Filter.useFilter(originalDataInstances, remove);

			// PopUps.showMessage("Output format collected okay = " + result);
			// PopUps.showMessage("Remove info: \n" + remove.globalInfo() + " " + remove.getAttributeIndices());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// PopUps.showMessage("Exiting selectAttributesByRangeList");

		return (instNew);
	}

	/**
	 * 
	 * @param originalDataInstances
	 * @param attributeRangeList
	 * @return
	 */
	public static Instances selectAttributesByRegex(Instances originalDataInstances, String regex)
	{
		// PopUps.showMessage("Inside selectAttributesByRangeList");
		Instances instNew = null;
		RemoveByName remove;

		remove = new RemoveByName();
		remove.setExpression(regex);
		PopUps.showMessage("Regular expression = " + remove.getExpression());
		remove.setInvertSelection(true);
		// remove.setInvertSelection(new Boolean(args[2]).booleanValue());
		try
		{
			boolean result = remove.setInputFormat(originalDataInstances);
			instNew = Filter.useFilter(originalDataInstances, remove);
			PopUps.showMessage("received " + originalDataInstances.size() + " filtered in " + instNew.size() + " atts= "
					+ instNew.numAttributes());
			PopUps.showMessage("Output format collected okay = " + result);
			PopUps.showMessage("Remove info: \n" + remove.globalInfo() + " ");// + remove.getAttributeIndices());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// PopUps.showMessage("Exiting selectAttributesByRangeList");

		return (instNew);
	}

	public static void main(String args[])
	{
		try
		{
			String s = "humbapumpa jim";
			System.out.println(s.matches(".*(jim|joe).*"));
			s = "humbapumpa jom";
			System.out.println(s.matches(".*(jim|joe).*"));
			s = "humbaPumpa joe";
			System.out.println(s.matches(".*(jim|joe).*"));
			s = "humbapumpa joe jim";
			System.out.println(s.matches(".*(jim|joe).*"));

			BufferedReader reader = new BufferedReader(new FileReader(
					"/run/media/gunjan/HOME/gunjan/Geolife Data Works/stats/wekaResults/temp/PrunedTimelineFeatureVectors.arff"));
			Instances ins = new Instances(reader);
			reader.close();

			ArrayList<String> substringsToMatch = new ArrayList<String>();
			substringsToMatch.add("SampEn");
			substringsToMatch.add("2");

			ArrayList<String> substringsToMatch2 = new ArrayList<String>();
			substringsToMatch2.add("ManualClustering");

			Instances n = selectAttributesBySubstrings(ins, substringsToMatch, substringsToMatch2, true);
			System.out.println(n.size() + " " + n.numAttributes());

			ArrayList<String> substringsToMatch21 = new ArrayList<String>();
			substringsToMatch21.add("SampEn");
			substringsToMatch21.add("3");

			ArrayList<String> substringsToMatch22 = new ArrayList<String>();
			substringsToMatch22.add("ManualClustering");

			Instances n2 = selectAttributesBySubstrings(ins, substringsToMatch21, substringsToMatch22, true);
			System.out.println(n2.size() + " " + n2.numAttributes());

			ArrayList<String> substringsToMatch31 = new ArrayList<String>();
			substringsToMatch31.add("Gram");

			ArrayList<String> substringsToMatch32 = new ArrayList<String>();
			substringsToMatch32.add("ManualClustering");

			Instances n3 = selectAttributesBySubstrings(ins, substringsToMatch31, substringsToMatch32, true);
			System.out.println(n3.size() + " " + n3.numAttributes());

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Since the method by weka is not working i wrote my own method as this one
	 * 
	 * 
	 * @param originalDataInstances
	 * @param andSubstrings1
	 * @param andSubstrings2
	 * @param hasInstanceID
	 * @return
	 */
	public static Instances selectAttributesBySubstrings(Instances originalDataInstances,
			ArrayList<String> andSubstrings1, ArrayList<String> andSubstrings2, boolean hasInstanceID)
	{
		// PopUps.showMessage("Inside selectAttributesByRangeList");
		Instances instNew = null;
		Set<Integer> positionsToKeep = new TreeSet<Integer>();// starts from 1

		if (hasInstanceID)
		{
			positionsToKeep.add(1);
		}

		try
		{
			Enumeration attnamesEnums = originalDataInstances.enumerateAttributes();

			int pos = 1;
			while (attnamesEnums.hasMoreElements())
			{
				boolean matched1 = true, matched2 = true;

				String attName = attnamesEnums.nextElement().toString();
				// System.out.println(attName + " -- " + pos);

				for (String s : andSubstrings1)
				{
					if (attName.contains(s) == false)
					{
						matched1 = false;
					}
				}

				for (String s : andSubstrings2)
				{
					if (attName.contains(s) == false)
					{
						matched2 = false;
					}
				}

				if (matched1 || matched2)
				{
					// System.out.println("matched = true");
					positionsToKeep.add(pos);
				}
				pos++;
			}
			if (originalDataInstances.classIndex() != -1)
			{
				positionsToKeep.add(originalDataInstances.classIndex() + 1);
			}

			String rangeList = positionsToKeep.stream().map(Object::toString).collect(Collectors.joining(","));
			// System.out.println(rangeList);
			instNew = selectAttributesByRangeList(originalDataInstances, rangeList);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// PopUps.showMessage("Exiting selectAttributesByRangeList");

		return (instNew);
	}

	/**
	 * 
	 * @param originalDataInstances
	 * @param attributeRangeList
	 * @return
	 */
	public static Instances removeAttributesByRegex(Instances originalDataInstances, String regex)
	{
		// PopUps.showMessage("Inside selectAttributesByRangeList");
		Instances instNew = null;
		RemoveByName remove;

		remove = new RemoveByName();
		remove.setExpression(regex);
		PopUps.showMessage("Regular expression = " + remove.getExpression());
		remove.setInvertSelection(false);
		// remove.setInvertSelection(new Boolean(args[2]).booleanValue());
		try
		{
			boolean result = remove.setInputFormat(originalDataInstances);
			instNew = Filter.useFilter(originalDataInstances, remove);
			PopUps.showMessage("received " + originalDataInstances.size() + " filtered in " + instNew.size() + " atts= "
					+ instNew.numAttributes());
			PopUps.showMessage("Output format collected okay = " + result);
			PopUps.showMessage("Remove info: \n" + remove.globalInfo() + " ");// + remove.getAttributeIndices());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// PopUps.showMessage("Exiting selectAttributesByRangeList");

		return (instNew);
	}

	/**
	 * Removes instances in the given range list from the given data instances
	 * 
	 * @param originalDataInstances
	 * @param rangeList
	 *            indices (starting from 1) of the instances to be removed
	 * @return reduced instances
	 */
	public static Instances removeInstancesByRangeList(Instances originalDataInstances, String rangeList)
	{
		Instances instNew = null;
		RemoveRange remove;

		remove = new RemoveRange();
		remove.setInstancesIndices(rangeList);

		// remove.setInvertSelection(new Boolean(args[2]).booleanValue());
		try
		{
			boolean result = remove.setInputFormat(originalDataInstances);
			// PopUps.showMessage("Output format collcted okay = " + result);
			// PopUps.showMessage("Remove info: \n" + remove.globalInfo() + " " + remove.getInstancesIndices());
		}
		catch (Exception e)
		{
			PopUps.showException(e,
					"org.activity.util.weka.WekaUtilityBelt.removeInstancesByRangeList(Instances, String): remove.setInputFormat(original)");
			e.printStackTrace();
		}

		try
		{
			instNew = Filter.useFilter(originalDataInstances, remove);
			// PopUps.showMessage("Okay after filter");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showError("error in Filter.useFilter(original, remove) " + e.getMessage());
		}
		return (instNew);
	}

	/**
	 * Selects only the instances in the given range list from the given data instances. TODO CHECK IF THIS WORKS
	 * CORRECT
	 * 
	 * @param originalDataInstances
	 * @param rangeList
	 *            indices (starting from 1) of the instances to be retained
	 * @return
	 */
	public static Instances selectInstancesByRangeList(Instances originalDataInstances, String rangeList)
	{
		Instances instNew = null;
		RemoveRange remove;

		remove = new RemoveRange();
		remove.setInstancesIndices(rangeList);
		remove.setInvertSelection(true);
		// remove.setInvertSelection(new Boolean(args[2]).booleanValue());
		try
		{

			boolean result = remove.setInputFormat(originalDataInstances);

			PopUps.showMessage("Output format collected okay = " + result);
			PopUps.showMessage("Remove info: \n" + remove.globalInfo() + " " + remove.getInstancesIndices());

		}
		catch (Exception e)
		{
			PopUps.showException(e,
					"org.activity.util.weka.WekaUtilityBelt.keepInstancesByRangeList(Instances, String)");
		}

		try
		{
			instNew = Filter.useFilter(originalDataInstances, remove);
			PopUps.showMessage("Okay after filter");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			PopUps.showError("error in Filter.useFilter(original, remove) " + e.getMessage());
		}
		return (instNew);
	}

	/**
	 * 
	 * @param eval
	 * @return
	 */
	public static String getRelevantEvaluationMetricsAsString(Evaluation eval)
	{
		StringBuffer results = new StringBuffer();

		try
		{
			results.append("----------------------------------------------------\n");
			results.append("WeightedAreaUnderROC =" + eval.weightedAreaUnderROC() + "\n");

			double[][] confusionMatrix = eval.confusionMatrix();
			double sizeOfConfusionMatrix = confusionMatrix.length * confusionMatrix[0].length;
			String confusionMatrixString = new String();
			for (int i = 0; i < confusionMatrix.length; i++)
			{
				for (int j = 0; j < confusionMatrix[i].length; j++)
				{

					String val = String.format("%.0f", confusionMatrix[i][j]);
					confusionMatrixString = confusionMatrixString + val + ",";
				}
				confusionMatrixString += "\n";
			}
			results.append("Confusion Matrix:\n" + confusionMatrixString + "\n");

			results.append("Classwise result: " + eval.toClassDetailsString());
			results.append("\n----------------------------------------------------\n");
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}

		return results.toString();
	}

	/**
	 * Writes the data (Instances) in arff format to the given filename
	 * 
	 * @param data
	 * @param absoluteFilename
	 */
	public static void writeArffAbsolute(Instances data, String absoluteFilename)
	{
		ArffSaver saver = new ArffSaver();

		try
		{
			saver.setInstances(data);
			saver.setFile(new File(absoluteFilename));
			saver.setDestination(new File(absoluteFilename));
			saver.writeBatch();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// / NOT actively used:
	/**
	 * Reads the manually assigned clusters (Alert: reads local file, absolute file path) </br>
	 * (<font color="red">ALERT: ensure that the order of user is correct in the file being read for cluster
	 * labels</font>)
	 * 
	 * @param indexOfColumn
	 *            index of colum containinf the manual clustering
	 * @return
	 */
	public static LinkedHashMap<String, String> getManualClustering(int indexOfColumn)
	{
		LinkedHashMap<String, String> clustering1 = new LinkedHashMap<String, String>();
		String fileName =
				"/run/media/gunjan/HOME/gunjan/Geolife Data Works/stats/wekaResults/ManualClustersUserAbove10RTs.csv";

		List<String> clusterLabel = ReadingFromFile.oneColumnReaderString(fileName, ",", indexOfColumn, true); // ALERT:
																												// ensure
																												// that
																												// the
																												// order
																												// of
																												// user
																												// is
																												// correct
																												// (alternative
																												// use
																												// two
																												// column
																												// reader
																												// with
																												// LinkadedHashMap

		int[] userIDs = Constant.getUserIDs();
		for (int i = 0; i < clusterLabel.size(); i++)
		{
			clustering1.put(String.valueOf(userIDs[i]), clusterLabel.get(i));

			System.out.println("user id =" + userIDs[i] + " cluster label = " + clusterLabel.get(i));

		}

		return clustering1;
	}
	// /**
	// * TODO why i am doing third to 2 and second to 3. Infact why is this method needed in the first place?
	// *
	// * @param clustering
	// * @return
	// */
	// public static LinkedHashMap<String, String> getUserActualClusterMap(LinkedHashMap<String, String> clustering)
	// {
	// LinkedHashMap<String, String> userIDClass = new LinkedHashMap<String, String>();
	//
	// for (Entry<String, String> entry : clustering.entrySet())
	// {
	//
	// String classLab = "";
	//
	// String classLabel = entry.getValue();
	// if (classLabel.contains("First"))
	// {
	// classLab = "1";
	// }
	// else if (classLabel.contains("Third"))
	// {
	// classLab = "2";
	// }
	// else if (classLabel.contains("Second"))
	// {
	// classLab = "3";
	// }
	// userIDClass.put(entry.getKey(), classLab);
	// }
	// return userIDClass;
	// }
}
