package org.activity.sanityChecks;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.activity.constants.Constant;
import org.activity.constants.VerbosityConstants;
import org.activity.io.WToFile;
import org.activity.objects.CheckinEntry;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.DateTimeUtils;
import org.activity.util.RegexUtils;
import org.activity.util.UtilityBelt;
import org.apache.commons.math.util.FastMath;

/**
 * Just to check/run some random snippets of code NOT IMPORTANT
 * 
 * @author gunjan
 *
 */
public class TestDummy2
{
	public static void main1(String args[])
	{
		// checkWritePerformance();

		String commonPath = "./";

		String currentPath = System.getProperty("user.dir") + "/";
		String separator = File.separator;
		System.out.println(currentPath + separator);

		WToFile.appendLineToFileAbs("test", currentPath + separator + "DeleteMe.txt");

		WToFile.appendLineToFileAbs("test", "DeleteMe2.txt");
		// String str = " 112:Music^^ || 12600__2.1891152325934935%";
		//
		// String splitted[] = str.split("\\|\\|");
		//
		// System.out.println("splitted[0] = " + splitted[0]);

		// System.out.println(Double.NaN);
		//
		// System.out.println(("gunjan".equals("gunjan")));
		// System.out.println("gunjan".equals("manali"));
		// String s[] = { "1", "101", "201", "301", "401", "501", "601" };
		//
		// for (int i = 0; i < s.length; i++)
		// {
		// int startUserIndex = Integer.valueOf(s[i]) - 1;// 100
		// int endUserIndex = startUserIndex + 99; // 199
		//
		// int countOfSampleUsers = 0;
		// System.out.println("startUserIndex=" + startUserIndex + " endUserIndex" + endUserIndex);
		// }

		// byte c = 70;
		// byte c1 = 84;
		//
		// System.out.println("c = " + " c1=" + c1);
		// String[] b1 = { "true", "True", "1", "t" };
		//
		// for (String s : b1)
		// {
		// System.out.println(Boolean.parseBoolean(s));
		// }

	}

	public static void checkIntersection()
	{
		ArrayList<Integer> a1 = (ArrayList<Integer>) Stream.of(1, 2, 3).collect(Collectors.toList());
		ArrayList<Integer> a2 = (ArrayList<Integer>) Stream.of(12, 2, 35).collect(Collectors.toList());
		System.out.println(UtilityBelt.getIntersection(a1, a2));
		System.out.println(UtilityBelt.getIntersection(a1, a2).size());
	}

	public static void tryingOutTimelineIDs()
	{

		Timestamp t1 = new Timestamp(System.currentTimeMillis());
		Timestamp t2 = new Timestamp(System.currentTimeMillis());

		System.out.println("1_" + t1 + "_" + t2);
		System.out.println("1_" + t1.getTime() + "_" + t2.getTime());

	}

	public static void mapCasts()
	{
		LinkedHashMap<Integer, String> linkedHashMap = new LinkedHashMap<>();
		LinkedHashMap<Integer, String> linkedHashMap2 = new LinkedHashMap<>();
		linkedHashMap.put(1, "One");
		linkedHashMap.put(2, "Two");

		// linkedHashMap2 = (LinkedHashMap<Integer, String>) linkedHashMap.entrySet().parallelStream()
		// .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

		linkedHashMap2 = linkedHashMap.entrySet().parallelStream().collect(Collectors.toMap(e -> e.getKey(),
				e -> e.getValue(), (oldValue, newValue) -> newValue, LinkedHashMap::new));
		// HashMap<Integer, String> hashMap = new HashMap<>();
	}

	public static void main(String args[])
	{
		Random r = new Random();

		int numOfIterations = 100000;
		ArrayList<Long> fastMathTimes = new ArrayList<>(numOfIterations + 10);
		ArrayList<Long> mathTimes = new ArrayList<>(numOfIterations + 10);

		for (int i = 0; i < numOfIterations; i++)
		{
			double d = StatsUtils.randomInRange(0.1, 200);

			long t1 = System.nanoTime();
			double vFast = FastMath.asin(d);
			long t2 = System.nanoTime();
			double vMath = Math.asin(d);
			long t3 = System.nanoTime();

			fastMathTimes.add(t2 - t1);
			mathTimes.add(t3 - t2);
		}

		System.out.println(fastMathTimes.stream().mapToLong(v -> v).average());
		System.out.println(mathTimes.stream().mapToLong(v -> v).average());
	}

	public static void main0(String args[])
	{
		// mapCasts();
		int numOfIterations = 1000;
		checkEDCombinePerformance(numOfIterations);
		checkEDCombinePerformance2(numOfIterations);
		checkEDCombinePerformance3(numOfIterations);
		// tryingOutTimelineIDs();
		// checkIntersection();
		// checkRegexUtils();
		// checkStackTrace();
		// testDirectoryDeletion();
		// List<String> list = Arrays.asList(new String[] { "gunjan", "manali", "sukany" });
		// System.out.println(list.toString());
		// list.stream().forEach(e -> System.out.print(e.toString() + "_"));

		// steamFunctionParameter(2);
		// streamExp1();
		// checkString1();

		// checkRegexUtils();
		// StringBuilder sb = new StringBuilder("Manali");
		// sb.append(" is ").append(" a good ").append(" girl.");
		// sb.append(" Really? ");
		// System.out.println(sb.toString());

		/////////////////
		// String[] list = new String[] { "Manali", "Gunjan", "Manali", "Gunjan", "Manali" };
		// System.out.println(StringUtils.fCat(new StringBuilder("test\n"), list).toString());
		/////////////////

		// Timestamp t1 = new Timestamp(2000, 12, 1, 10, 10, 10, 10);
		// Timestamp t2 = new Timestamp(2000, 12, 1, 10, 10, 10, 10);
		//
		// checkSQLDate0(t1, t2);
		// // checkSQLDate(t1, t2);
		//
		// t1 = new Timestamp(2000, 12, 1, 5, 10, 10, 10);
		// t2 = new Timestamp(2000, 12, 1, 10, 10, 10, 10);
		//
		// checkSQLDate0(t1, t2);
		// checkSQLDate(t1, t2);

		// List<Integer> range = IntStream.rangeClosed(1, 10).boxed().collect(Collectors.toList());
		// range.forEach(System.out::println);
	}

	public static void checkEDCombinePerformance(int numOfIterations)
	{
		Random r = new Random();

		ArrayList<Double> timeTaken1 = new ArrayList<>(numOfIterations);

		long tS = System.nanoTime();
		for (int i = 0; i < numOfIterations; i++)
		{
			double dAct = 0 + r.nextDouble() * r.nextInt(10);
			double dFeat = 0 + r.nextDouble() * r.nextInt(5);

			int size1 = 5 + r.nextInt(7);
			int size2 = 5 + r.nextInt(7);
			long t1 = System.nanoTime();
			combineActAndFeatLevelDistance(dAct, dFeat, size1, size2, 0.5);
			long t2 = System.nanoTime();
			long timeTaken = t2 - t1;
			timeTaken1.add((double) (timeTaken / 1000));
		}

		System.out.println((System.nanoTime() - tS) + "ns");
		System.out.println(timeTaken1.toString());
		System.out.println(StatsUtils.getDescriptiveStatistics(timeTaken1).toString());
	}

	public static void checkEDCombinePerformance2(int numOfIterations)
	{
		Random r = new Random();

		ArrayList<Double> timeTaken1 = new ArrayList<>(numOfIterations);

		long tS = System.nanoTime();
		for (int i = 0; i < numOfIterations; i++)
		{
			double dAct = 0 + r.nextDouble() * r.nextInt(10);
			double dFeat = 0 + r.nextDouble() * r.nextInt(5);

			int size1 = 5 + r.nextInt(7);
			int size2 = 5 + r.nextInt(7);
			long t1 = System.nanoTime();
			combineActAndFeatLevelDistance2(dAct, dFeat, size1, size2, 0.5);
			long t2 = System.nanoTime();
			long timeTaken = t2 - t1;
			timeTaken1.add((double) (timeTaken / 1000));
		}

		System.out.println((System.nanoTime() - tS) + "ns");
		System.out.println(timeTaken1.toString());
		System.out.println(StatsUtils.getDescriptiveStatistics(timeTaken1).toString());
	}

	public static void checkEDCombinePerformance3(int numOfIterations)
	{
		Random r = new Random();

		ArrayList<Double> timeTaken1 = new ArrayList<>(numOfIterations);

		long tS = System.nanoTime();
		for (int i = 0; i < numOfIterations; i++)
		{
			double dAct = 0 + r.nextDouble() * r.nextInt(10);
			double dFeat = 0 + r.nextDouble() * r.nextInt(5);

			int size1 = 5 + r.nextInt(7);
			int size2 = 5 + r.nextInt(7);
			long t1 = System.nanoTime();
			combineActAndFeatLevelDistance2(dAct, dFeat, size1, size2, 0.5);
			long t2 = System.nanoTime();
			long timeTaken = t2 - t1;
			timeTaken1.add((double) (timeTaken / 1000));
		}

		System.out.println((System.nanoTime() - tS) + "ns");
		System.out.println(timeTaken1.toString());
		System.out.println(StatsUtils.getDescriptiveStatistics(timeTaken1).toString());
	}

	/**
	 * 
	 * @param dAct
	 * @param dFeat
	 * @param size1
	 * @param size2
	 * @param alpha
	 * @return
	 */
	private static double combineActAndFeatLevelDistance(double dAct, double dFeat, int size1, int size2, double alpha)
	{
		double distanceTotal = -1;
		double costReplaceActivityObject = 2, wtStartTime = 1, wtLocation = 1, wtLocPopularity = 1;
		// (length of current timeline-1)*replaceWt*WtObj
		double maxActLevelDistance = Math.max((Math.max(size1, size2) - 1), 1) * costReplaceActivityObject;
		// = (length of current timeline)*(wtStartTime + wtLocation + wtLocPopularity)
		double maxFeatLevelDistance = Math.max(size1, size2) * (wtStartTime + wtLocation + wtLocPopularity);

		if (dAct > maxActLevelDistance || dFeat > maxFeatLevelDistance)
		{
			PopUps.printTracedErrorMsg("Error in combineActAndFeatLevelDistance : dAct" + dAct + " maxActLevelDistance="
					+ maxActLevelDistance + " dFeat=" + dFeat + " maxFeatLevelDistance=" + maxFeatLevelDistance
					+ " size1=" + size1 + " size2=" + size2 + " alpha=" + alpha);
			return -1;
		}
		distanceTotal = StatsUtils.round(
				alpha * (dAct / maxActLevelDistance) + (1 - alpha) * (dFeat / maxFeatLevelDistance),
				Constant.RoundingPrecision);

		if (VerbosityConstants.verboseCombinedEDist)
		{
			// WritingToFile.appendLineToFileAbsolute(
			// distanceTotal + "," + dAct + "," + dFeat + "," + size1 + "," + size2 + "\n",
			// Constant.getCommonPath() + "DistanceTotalAlpha" + alpha + ".csv");
		}

		return distanceTotal;
	}

	private static double combineActAndFeatLevelDistance2(double dAct, double dFeat, int size1, int size2, double alpha)
	{
		double distanceTotal = -1;
		double costReplaceActivityObject = 2, wtStartTime = 1, wtLocation = 1, wtLocPopularity = 1;
		// (length of current timeline-1)*replaceWt*WtObj
		double maxActLevelDistance = Math.max((Math.max(size1, size2) - 1), 1) * costReplaceActivityObject;
		// = (length of current timeline)*(wtStartTime + wtLocation + wtLocPopularity)
		double maxFeatLevelDistance = Math.max(size1, size2) * (wtStartTime + wtLocation + wtLocPopularity);

		if (dAct > maxActLevelDistance || dFeat > maxFeatLevelDistance)
		{
			PopUps.printTracedErrorMsg("Error in combineActAndFeatLevelDistance : dAct" + dAct + " maxActLevelDistance="
					+ maxActLevelDistance + " dFeat=" + dFeat + " maxFeatLevelDistance=" + maxFeatLevelDistance
					+ " size1=" + size1 + " size2=" + size2 + " alpha=" + alpha);
			return -1;
		}
		distanceTotal = alpha * (dAct / maxActLevelDistance) + (1 - alpha) * (dFeat / maxFeatLevelDistance);

		if (VerbosityConstants.verboseCombinedEDist)
		{
			// WritingToFile.appendLineToFileAbsolute(
			// distanceTotal + "," + dAct + "," + dFeat + "," + size1 + "," + size2 + "\n",
			// Constant.getCommonPath() + "DistanceTotalAlpha" + alpha + ".csv");
		}

		return distanceTotal;
	}

	private static double combineActAndFeatLevelDistance3(double dAct, double dFeat, int size1, int size2, double alpha)
	{
		double distanceTotal = -1;
		double costReplaceActivityObject = 2, wtStartTime = 1, wtLocation = 1, wtLocPopularity = 1;
		// (length of current timeline-1)*replaceWt*WtObj
		double maxActLevelDistance = Math.max((Math.max(size1, size2) - 1), 1) * costReplaceActivityObject;
		// = (length of current timeline)*(wtStartTime + wtLocation + wtLocPopularity)
		double maxFeatLevelDistance = Math.max(size1, size2) * (wtStartTime + wtLocation + wtLocPopularity);

		distanceTotal = alpha * (dAct / maxActLevelDistance) + (1 - alpha) * (dFeat / maxFeatLevelDistance);

		if (VerbosityConstants.verboseCombinedEDist)
		{
			// WritingToFile.appendLineToFileAbsolute(
			// distanceTotal + "," + dAct + "," + dFeat + "," + size1 + "," + size2 + "\n",
			// Constant.getCommonPath() + "DistanceTotalAlpha" + alpha + ".csv");
		}

		return distanceTotal;
	}
	// public static void checkSQLDate(Timestamp t1, Timestamp t2)
	// {
	// System.out.println("---------getDateSafely------");
	// Date d1 = DateTimeUtils.getDateSafely(t1);
	// Date d2 = DateTimeUtils.getDateSafely(t2);
	//
	// System.out.println("t1=" + t1 + "\nt2=" + t2 + "\nd1=" + d1 + "\nd2=" + d2);
	//
	// System.out.println("d1 == d2:" + (d1 == d2));
	// System.out.println("d1.equals(d2):" + d1.equals(d2));
	// }

	public static void checkSQLDate0(Timestamp t1, Timestamp t2)
	{
		System.out.println("-------getDate--------");
		Date d1 = DateTimeUtils.getDate(t1);
		Date d2 = DateTimeUtils.getDate(t2);

		System.out.println("t1=" + t1 + "\nt2=" + t2 + "\nd1=" + d1 + "\nd2=" + d2);

		System.out.println("d1 == d2:" + (d1 == d2));
		System.out.println("d1.equals(d2):" + d1.equals(d2));
	}

	public static void checkRegexUtils()
	{
		String s = "__a__b__c__d__e";
		String[] topKString = RegexUtils.patternDoubleUnderScore.split(s);
		String[] topKString2 = s.split("__");

		System.out.println(Arrays.asList(topKString) + "\n" + Arrays.asList(topKString2));

		// $$ s.get(i).get(j).split("__");
		// topK is of the form string: __a__b__c__d__e is of length 6...

	}

	public static void checkString1()
	{
		String codeUn = "A";
		String codeO = "B";
		String userDayActivitiesAsStringCode = "DAHBAUQWEDAA";
		// get indices of valid activity ActivityNames
		ArrayList<Integer> indicesOfValids = new ArrayList<Integer>();

		for (int i = 0; i < userDayActivitiesAsStringCode.length(); i++)
		{
			String codeToCheck = userDayActivitiesAsStringCode.substring(i, i + 1); // only one character
			// here the codeToCheck is only of length 1, hence, using endsWith or equals below shouldn't make difference
			if (codeToCheck.endsWith(codeUn) || codeToCheck.equals(codeO))
			{
				continue;
			}
			else
				indicesOfValids.add(i);
		}
		indicesOfValids.stream().forEach(System.out::println);
	}

	public static void checkString1Spartanized()
	{
		String codeUn = "A";
		String codeO = "B";
		String userDayActivitiesAsStringCode = "DAHBAUQWEDAA";
		// get indices of valid activity ActivityNames
		ArrayList<Integer> indicesOfValids = new ArrayList<Integer>();

		for (int i = 0; i < userDayActivitiesAsStringCode.length(); ++i)
		{
			String codeToCheck = userDayActivitiesAsStringCode.substring(i, i + 1);
			if (!codeToCheck.endsWith(codeUn) && !codeToCheck.equals(codeO))
			{
				indicesOfValids.add(i);
			}
		}
		indicesOfValids.stream().forEach(System.out::println);
	}

	public static void testDirectoryDeletion()
	{
		WToFile.deleteNonEmptyDirectory(Paths.get("/home/gunjan/Downloads/test/"));
	}

	public static void sets()
	{

	}

	public static void checkStackTrace()
	{
		checkStackTrace1();
	}

	public static void checkStackTrace1()
	{
		checkStackTrace2();
	}

	public static void checkStackTrace2()
	{
		checkStackTrace3();
	}

	public static void checkStackTrace3()
	{
		System.out.println(PopUps.getTracedErrorMsg("XYZ error occurred"));
		// StackTraceElement[] stacktraceElements = Thread.currentThread().getStackTrace();
		//
		// Arrays.stream(stacktraceElements).forEach(e -> System.out.println(e.toString()));
		//
		// // System.out.println(Thread.currentThread().getStackTrace());
		// System.out.println("--------------");
		// System.out.println(ExceptionUtils.getFullStackTrace(throwable));
	}

	/**
	 * 
	 * @param choice
	 */
	public static void steamFunctionParameter(int choice)
	{
		try
		{
			Function<CheckinEntry, String> funVariable = ce -> String.valueOf(ce.getActivityID());// new Function<>();

			if (choice == 1)
			{
				funVariable = ce -> String.valueOf(ce.getActivityID());
			}
			else if (choice == 2)
			{
				funVariable = ce -> String.valueOf(ce.getTimestamp());
			}

			for (int i = 0; i < 10; i++)
			{

				CheckinEntry ce = new CheckinEntry("1", i, Timestamp.valueOf(LocalDateTime.now()), "1.1", "3.3", 15,
						"5");
				System.out.println(funVariable.apply(ce));
				System.out.println(funVariable.toString());
				// compares the choice variable for each of the 100 times, even though value of choice is known before
				// the loop evn starts.
				// Can we store the function to be called in a variable and assign it before the for loop.
				// if (choice == 1)
				// {
				// System.out.println(ce.getActivityID());
				// }
				// else if (choice == 2)
				// {
				// System.out.println(ce.getTimestamp());
				// }
			}

		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void streamExp1()
	{
		LinkedHashMap<String, ArrayList<Integer>> map = new LinkedHashMap<String, ArrayList<Integer>>();

		ArrayList<Integer> a1 = new ArrayList<Integer>();
		ArrayList<Integer> a2 = new ArrayList<Integer>();
		ArrayList<Integer> a3 = new ArrayList<Integer>();

		for (int i = 1; i <= 5; i++)
		{
			a1.add(i);
		}

		for (int i = 1; i <= 8; i++)
		{
			a2.add(i);
		}

		for (int i = 1; i <= 3; i++)
		{
			a3.add(i);
		}

		map.put("A1", a1);
		map.put("A2", a2);
		map.put("A3", a3);

		OptionalInt max = map.entrySet().stream().mapToInt(e -> e.getValue().size()).max();

		System.out.println("max = " + max.getAsInt());

		long count = map.entrySet().stream().mapToInt(e -> e.getValue().size()).sum();

		System.out.println("count = " + count);

		map.entrySet().stream().map(e -> (e.getKey() + ":" + e.getValue())).forEach(System.out::println);

		System.out.println("-------------");
		Map<String, ArrayList<Integer>> map2 = map.entrySet().stream().limit(2)
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
		map2.entrySet().stream().map(e -> (e.getKey() + ":" + e.getValue())).forEach(System.out::println);

		System.out.println("-------------");
		// LinkedHashMap<String, ArrayList<Integer>> map3 = (LinkedHashMap<String, ArrayList<Integer>>) map2;
		LinkedHashMap<String, ArrayList<Integer>> map3 = new LinkedHashMap<String, ArrayList<Integer>>(map2);
		map3.entrySet().stream().map(e -> (e.getKey() + ":" + e.getValue())).forEach(System.out::println);
	}

	public static void checkWritePerformance()
	{
		long maxIteration = 80000000;
		long st1 = System.currentTimeMillis();

		String fileName = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Test/writePerformance.txt";
		String msg = "nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn";
		try
		{
			BufferedWriter bwSimple = WToFile.getBufferedWriterForNewFile(fileName, 3072);

			for (int i = 0; i < maxIteration; i++)
			{
				bwSimple.write(msg + "\n");
			}

			bwSimple.close();
			long st2 = System.currentTimeMillis();

			System.out.println("file written with bwSimple in " + ((st2 - st1) * 1.0 / 1000) + " secs");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
		}
	}

	public static void streamExp1Spartanized()
	{
		LinkedHashMap<String, ArrayList<Integer>> map = new LinkedHashMap<String, ArrayList<Integer>>();

		ArrayList<Integer> a1 = new ArrayList<Integer>(), a2 = new ArrayList<Integer>(), a3 = new ArrayList<Integer>();
		for (int ¢ = 1; ¢ <= 5; ++¢)
			a1.add(¢);

		for (int ¢ = 1; ¢ <= 8; ++¢)
			a2.add(¢);

		for (int ¢ = 1; ¢ <= 3; ++¢)
			a3.add(¢);

		map.put("A1", a1);
		map.put("A2", a2);
		map.put("A3", a3);

		System.out.println("max = " + map.entrySet().stream().mapToInt(λ -> λ.getValue().size()).max().getAsInt());
	}

}
