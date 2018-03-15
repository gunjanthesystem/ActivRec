package org.activity.plotting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class DataGenerator
{

	public DataGenerator()
	{
		// TODO Auto-generated constructor stub
	}

	// public static List<Pair<String, List<Double>>> getTimelineData()
	// {
	// List<Pair<String, List<Double>>> allData = new ArrayList<>();
	//
	// allData.add(new Pair<String, List<Double>>("User1",
	// Stream.of(1.0, 25.0, 20.0, 32.0, 16.0, 20.0).collect(Collectors.toList())));
	//
	// allData.add(new Pair<String, List<Double>>("User2",
	// Stream.of(1.0, 35.0, 20.0, 32.0, 16.0, 20.0).collect(Collectors.toList())));
	//
	// return allData;
	// }

	/**
	 * 
	 * @return
	 */
	public static List<List<String>> getData()
	{
		List<List<String>> data = new ArrayList<>();
		// data.add(Arrays.asList("Running", "1499707991", "1499717991", "UCD"));
		// data.add(Arrays.asList("Eating", "1499718991", "1499728991", "Rathgar"));
		data.add(Arrays.asList("User1", "12", "17", "UCD", "Running", "1"));
		data.add(Arrays.asList("User1", "8", "9", "UCD", "Running", "1"));
		data.add(Arrays.asList("User2", "18", "21", "Rathgar", "Eating", "2"));
		data.add(Arrays.asList("User3", "22", "25", "Rathgar", "Running", "1"));
		data.add(Arrays.asList("User4", "29", "40", "Rathgar", "Swimming", "3"));
		data.add(Arrays.asList("User4", "7", "15", "Rathgar", "Swimming", "3"));
		data.add(Arrays.asList("User4", "22", "28", "Rathgar", "Swimming", "3"));
		data.add(Arrays.asList("User5", "45", "47", "Rathgar", "Reading", "4"));

		// String[][] datagenerated = { { "Running", "1499707991" }, { "Eating", "1499727991" } };
		return data;
	}

	public static List<List<String>> getData2()
	{
		int numOfUsers = 10;

		int numOfActsPerUser = 100;
		int numOfUniqueActs = 5;

		int startTime = 5, endTime = 800;

		List<List<String>> data = new ArrayList<>();

		Random rd = new Random();

		for (int u = 0; u < numOfUsers; u++)
		{
			String user = "User" + u;
			int st = startTime, et = -99;

			for (int a = 0; a <= numOfActsPerUser; a++)
			{
				et = st + 20 + rd.nextInt(70);
				int actID = rd.nextInt(6);

				List<String> actData = new ArrayList<>();
				actData.add(user);
				actData.add(String.valueOf(st));
				actData.add(String.valueOf(et));
				actData.add(String.valueOf(""));// location name
				actData.add(String.valueOf(actID));// location name
				actData.add(String.valueOf(actID));// location name

				data.add(actData);
				st = et + 1;
			}
		}

		return data;
	}

	/**
	 * 
	 * @param numOfUsers
	 * @param numOfActsPerUser
	 * @param numOfUniqueActs
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static List<List<String>> getData2(int numOfUsers, int numOfActsPerUser, int numOfUniqueActs, int startTime,
			int endTime, int minDuration, int maxDuration)
	{

		List<List<String>> data = new ArrayList<>();

		Random rd = new Random();

		for (int u = 0; u < numOfUsers; u++)
		{
			String user = "U" + u;
			int st = startTime, et = -99;

			for (int a = 0; a < numOfActsPerUser; a++)
			{
				et = st + minDuration + rd.nextInt(maxDuration);
				// et = st + 1;
				int actID = rd.nextInt(numOfUniqueActs);

				List<String> actData = new ArrayList<>();
				actData.add(user);// UserID
				actData.add(String.valueOf(st));// start ts
				actData.add(String.valueOf(et));// end ts
				actData.add(String.valueOf(""));// location name
				actData.add(String.valueOf(actID));// act name
				actData.add(String.valueOf(actID));// act ID

				data.add(actData);
				st = et + 1;
			}
		}

		StringBuilder sb = new StringBuilder();
		data.stream().forEachOrdered(d -> sb.append(d.stream().collect(Collectors.joining(",")) + "\n"));
		System.out.println("data=\n" + sb.toString());
		return data;
	}

	/**
	 * 
	 * @param numOfUsers
	 * @param numOfActsPerUser
	 * @param numOfUniqueActs
	 * @param startTime
	 * @param endTime
	 * @return {user,{list of acts for each users{ list vals for each act}}}
	 */
	public static List<List<List<String>>> getData3(int numOfUsers, int numOfActsPerUser, int numOfUniqueActs,
			int startTime, int endTime, int minDuration, int maxDuration)
	{

		List<List<List<String>>> dataForAllUsers = new ArrayList<>();

		Random rd = new Random();

		for (int u = 0; u < numOfUsers; u++)
		{
			String user = "U" + u;
			int st = startTime, et = -99;
			List<List<String>> dataForAUser = new ArrayList<>();

			for (int a = 0; a < numOfActsPerUser; a++)
			{
				et = st + minDuration + rd.nextInt(maxDuration);
				// et = st + 1;
				int actID = rd.nextInt(numOfUniqueActs);

				List<String> actData = new ArrayList<>();
				actData.add(user);// UserID
				actData.add(String.valueOf(st));// start ts
				actData.add(String.valueOf(et));// end ts
				actData.add(String.valueOf(""));// location name
				actData.add(String.valueOf(actID));// act name
				actData.add(String.valueOf(actID));// act ID

				dataForAUser.add(actData);
				st = et + 1;
			}
			dataForAllUsers.add(dataForAUser);
		}

		// StringBuilder sb = new StringBuilder();
		//
		// for (List<List<String>> data : dataForAllUsers)
		// {
		// data.stream().forEachOrdered(d -> sb.append(d.stream().collect(Collectors.joining(",")) + "\n"));
		// }
		//
		// System.out.println("data=\n" + sb.toString());
		return dataForAllUsers;
	}

	/**
	 * 
	 * @param numOfUsers
	 * @param numOfActsPerUser
	 * @param numOfUniqueActs
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static List<List<String>> getData2ForAUser(int userID, int numOfActsPerUser, int numOfUniqueActs,
			int startTime, int endTime)
	{

		List<List<String>> data = new ArrayList<>();

		Random rd = new Random();

		// for (int u = 0; u < numOfUsers; u++)
		{
			String user = "User" + userID;
			int st = startTime, et = -99;

			for (int a = 0; a < numOfActsPerUser; a++)
			{
				// et = st + 20 + rd.nextInt(70);
				et = st + 1;
				int actID = rd.nextInt(6);

				List<String> actData = new ArrayList<>();
				actData.add(user);
				actData.add(String.valueOf(st));
				actData.add(String.valueOf(et));
				actData.add(String.valueOf(""));// location name
				actData.add(String.valueOf(actID));// location name
				actData.add(String.valueOf(actID));// location name

				data.add(actData);
				st = et + 1;
			}
		}

		// StringBuilder sb = new StringBuilder();
		// data.stream().forEachOrdered(d -> sb.append(d.stream().collect(Collectors.joining(",")) + "\n"));
		// System.out.println("data=\n" + sb.toString());
		return data;
	}

}
