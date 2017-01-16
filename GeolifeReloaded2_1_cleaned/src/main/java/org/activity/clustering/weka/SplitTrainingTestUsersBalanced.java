package org.activity.clustering.weka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.activity.objects.Pair;
import org.activity.util.weka.WekaUtilityBelt;

/**
 * Splits the set of users into training and test set based on the class label (based on best matching-unit) assigned to
 * them.
 * 
 * @author gunjan
 *
 */
public class SplitTrainingTestUsersBalanced
{
	LinkedHashMap<String, ArrayList<String>> threeClasses;
	Pair<ArrayList<String>, ArrayList<String>> trainingTestUsers;
	static double percentageInTraining = 0.8;

	public SplitTrainingTestUsersBalanced()
	{
		LinkedHashMap<String, String> userClassMap = WekaUtilityBelt.getManualClustering(2);// TimelinesFeatureExtraction.getManualClustering(2);

		traverseMap(userClassMap);

		threeClasses = splitIntoClasses(userClassMap);

		trainingTestUsers = splitIntoTrainingTestEquiDistribution(threeClasses, percentageInTraining);

		System.out.println("-- > Training users = " + trainingTestUsers.getFirst().toString());
		System.out.println("-- > Test users = " + trainingTestUsers.getSecond().toString());
		// LinkedHashMap<String,ArrayList<Integer>> threeClasses
	}

	public Pair<ArrayList<String>, ArrayList<String>> getTrainingTestUsers()
	{
		return this.trainingTestUsers;
	}

	public ArrayList<Integer> getNUniqueRandomNumbers(int N, int lowRange, int highRange)
	{
		ArrayList<Integer> result = new ArrayList<Integer>();
		ArrayList<Integer> container = new ArrayList<Integer>();

		for (int i = lowRange; i <= highRange; i++)
		{
			container.add(i);
		}

		for (int i = 0; i < N; i++)
		{
			Collections.shuffle(container);
			result.add(container.get(0));
		}

		return result;
	}

	// public Pair<ArrayList<String>, ArrayList<String>> randomSplit(ArrayList<String> vals, int sizeOfFirstSplit)
	// {
	// Pair<ArrayList<String>, ArrayList<String>> result = new Pair<ArrayList<String>, ArrayList<String>>();
	//
	// return result;
	// }

	public Pair<ArrayList<String>, ArrayList<String>> splitIntoTrainingTestEquiDistribution(
			LinkedHashMap<String, ArrayList<String>> threeClasses, double percentageInTraining)
	{
		Pair<ArrayList<String>, ArrayList<String>> trainingTestUsers = new Pair<ArrayList<String>, ArrayList<String>>(); // first
																															// traing,
																															// second
																															// test
		//
		ArrayList<String> training = new ArrayList<String>();
		ArrayList<String> test = new ArrayList<String>();

		for (Map.Entry<String, ArrayList<String>> entry : threeClasses.entrySet())
		{
			String classLabel = entry.getKey();
			ArrayList<String> usersInClass = entry.getValue();
			int numOfUsers = entry.getValue().size();
			int numberOfUsersForTraining = (int) Math.round(numOfUsers * percentageInTraining);// floor
			int numberOfUsersForTest = numOfUsers - numberOfUsersForTraining;
			if (numberOfUsersForTest < 1)
			{
				System.out.println("Warning: for classLabel: " + classLabel
						+ "  num of test users was less than 1 and thus increasing to 1");
				numberOfUsersForTest = 1;
				numberOfUsersForTraining = numOfUsers - numberOfUsersForTest;
			}

			System.out.println("ClassLabel =" + classLabel + "  num of training usersd = " + numberOfUsersForTraining
					+ "num of test users= " + numberOfUsersForTest);

			Collections.shuffle(usersInClass); // randomise;

			List<String> usersInTraining = usersInClass.subList(0, numberOfUsersForTraining);
			List<String> usersInTest = usersInClass.subList(numberOfUsersForTraining, numOfUsers);

			training.addAll(usersInTraining);
			test.addAll(usersInTest);
		}

		trainingTestUsers.setFirst(training);
		trainingTestUsers.setSecond(test);

		return trainingTestUsers;
	}

	public LinkedHashMap<String, ArrayList<String>> getSplitClasses()
	{
		return this.threeClasses;
	}

	private void traverseMap(LinkedHashMap<String, String> userClassMap)
	{
		System.out.println("Traversing User Class map");
		for (Map.Entry<String, String> entry : userClassMap.entrySet()) // iterating over users
		{
			System.out.println("User: " + entry.getKey() + "   Class = " + entry.getValue() + "\n");
		}
	}

	private LinkedHashMap<String, ArrayList<String>> splitIntoClasses(LinkedHashMap<String, String> userClassMap)
	{
		// <Class label, list of userids for that class label>
		LinkedHashMap<String, ArrayList<String>> threeClasses = new LinkedHashMap<String, ArrayList<String>>();

		for (Map.Entry<String, String> entry : userClassMap.entrySet()) // iterating over users
		{
			String userID = entry.getKey();
			String classLabel = entry.getValue();

			// System.out.println("--Seeing user: " + userID + " of class: " + classLabel);

			if (threeClasses.containsKey(classLabel))
			{
				threeClasses.get(classLabel).add(userID);
				// System.out.println("--Add to exisiting entry");

			}
			else
			{
				threeClasses.put(classLabel, new ArrayList());
				threeClasses.get(classLabel).add(userID);
				// System.out.println("--Creating new entry adding user id");
			}
		}

		System.out.println("Iterating over the split classes");

		for (Map.Entry<String, ArrayList<String>> entry : threeClasses.entrySet()) // iterating over users
		{
			System.out.println("Class Label: " + entry.getKey() + "  num of users: " + entry.getValue().size()
					+ "  Users: " + entry.getValue().toString());
		}

		return threeClasses;

	}
	// public static void main(String args[])
	// {
	// Constant.set
	// Constant.setUserIDs();
	// new SplitTrainingTestUsersBalanced();
	// }
}
