package org.activity.sanityChecks;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.activity.constants.Constant;
import org.activity.constants.Enums.PrimaryDimension;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.Pair;
import org.activity.ui.PopUps;

public class Sanity
{

	/**
	 * 
	 * @param val
	 * @param lowerLimitInclusive
	 * @param upperLimitInclusive
	 * @param errorMsg
	 * @return
	 * @since 31 Aug 2018
	 */
	public static boolean inRange(double val, double lowerLimitInclusive, double upperLimitInclusive, String errorMsg)
	{
		String msg = "";
		if (val < lowerLimitInclusive)
		{
			msg = "lower than " + lowerLimitInclusive;
		}
		if (val > upperLimitInclusive)
		{
			msg = "higher than " + upperLimitInclusive;
		}

		if (msg.length() > 0)
		{
			errorMsg += ("\t" + msg + "\n");
			System.err.println(PopUps.getTracedErrorMsg(errorMsg));
			PopUps.showError(errorMsg);
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @param errorMsg
	 * @return
	 */
	public static boolean eq(int a, int b, String errorMsgConsole)
	{
		if (a == b)
		{
			return true;
		}
		else
		{
			System.err.println(PopUps.getTracedErrorMsg(errorMsgConsole));
			PopUps.showError(errorMsgConsole);
			return false;
		}
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @param errorMsg
	 * @return true if equal
	 */
	public static boolean eq(double a, double b, String errorMsgConsole)
	{
		if (a == b)
		{
			return true;
		}
		else
		{
			errorMsgConsole += "a = " + a + "!= b=" + b;
			System.err.println(PopUps.getTracedErrorMsg(errorMsgConsole));
			System.out.println(PopUps.getTracedErrorMsg(errorMsgConsole));// TODO may be redundant added on 20 July 2018
			PopUps.showError(errorMsgConsole);
			return false;
		}
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @param errorMsg
	 * @return true if equal
	 */
	public static boolean eq(double a, double b, double c, String errorMsgConsole)
	{
		boolean v1 = eq(a, b, errorMsgConsole);
		boolean v2 = eq(a, c, errorMsgConsole);
		boolean v3 = eq(b, c, errorMsgConsole);

		if (v1 && v2 && v3)
		{
			return true;
		}
		else
		{
			// System.err.println(PopUps.getTracedErrorMsg(errorMsgConsole));
			PopUps.showError(errorMsgConsole);
			PopUps.printTracedErrorMsg(errorMsgConsole);
			return false;
		}
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @param errorMsg
	 * @return
	 */
	public static boolean leq(int a, int b, String errorMsgConsole)
	{
		if (a <= b)
		{
			return true;
		}
		else
		{
			System.err.println(PopUps.getTracedErrorMsg(errorMsgConsole));
			PopUps.showError(errorMsgConsole);
			return false;
		}
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @param errorMsg
	 * @return
	 */
	public static boolean gt(int a, int b, String errorMsgConsole)
	{
		if (a > b)
		{
			return true;
		}
		else
		{
			System.err.println(PopUps.getTracedErrorMsg(errorMsgConsole));
			PopUps.showError(errorMsgConsole);
			return false;
		}
	}

	/**
	 * If unequal, print and display error message
	 * 
	 * @param a
	 * @param b
	 * @param errorMsg
	 * @return
	 */
	public static boolean eq(String a, String b, String errorMsgConsole)
	{
		if (a.equals(b))
		{
			return true;
		}
		else
		{
			System.err.println(PopUps.getTracedErrorMsg(errorMsgConsole));
			PopUps.showError(errorMsgConsole);
			return false;
		}
	}

	/**
	 * If unequal, print and display error message
	 * 
	 * @param a
	 * @param b
	 * @param errorMsg
	 * @return
	 */
	public static boolean eq(boolean a, boolean b, String errorMsgConsole)
	{
		if (a == b)
		{
			return true;
		}
		else
		{
			System.err.println(PopUps.getTracedErrorMsg(errorMsgConsole));
			PopUps.showError(errorMsgConsole);
			return false;
		}
	}

	/**
	 * To check sanity of
	 * org.activity.evaluation.RecommendationTestsMar2017GenSeqCleaned2.buildRepresentativeAOsForUserPD()
	 * 
	 * @param repAOResult
	 * @param repAOResultActName
	 */
	public static void compare(
			Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> repAOResultPD,
			Pair<LinkedHashMap<String, ActivityObject2018>, LinkedHashMap<String, Pair<Double, Double>>> repAOResultActName)
	{
		if (Constant.primaryDimension.equals(PrimaryDimension.ActivityID) == false)
		{
			PopUps.printTracedErrorMsgWithExit(
					"Error: org.activity.sanityChecks.Sanity.compare() should not be called when primary dimension is : "
							+ Constant.primaryDimension);
		}
		System.out.println("Inside compare for repAOResultPD and repAOResultActName\n");
		LinkedHashMap<Integer, ActivityObject2018> repAOsPD = repAOResultPD.getFirst();
		LinkedHashMap<String, ActivityObject2018> repAOsActName = repAOResultActName.getFirst();

		Sanity.eq(repAOsPD.size(), repAOsActName.size(),
				"repAOsPD.size()=" + repAOsPD.size() + "repAOsActName.size()=" + repAOsActName.size());

		for (Entry<Integer, ActivityObject2018> repAOPDEntry : repAOsPD.entrySet())
		{
			Integer actID = repAOPDEntry.getKey();
			ActivityObject2018 repAOPD = repAOPDEntry.getValue();

			String actName = String.valueOf(actID);
			// corresponding entry from
			ActivityObject2018 repAOActName = repAOsActName.get(actName);

			if (repAOPD.equals(repAOActName))
			{
				// System.out.println("repAOPD==repAOActName");
			}
			else
			{
				System.err.println("Error: for actID:" + actID + ", repAOPD!=repAOActName\nrepAOPD="
						+ repAOPD.toStringAllGowallaTS() + "\nrepAOActName=" + repAOActName.toStringAllGowallaTS());
			}
		}

		LinkedHashMap<Integer, Pair<Double, Double>> medianPreSucDurationPD = repAOResultPD.getSecond();
		LinkedHashMap<String, Pair<Double, Double>> medianPreSucDurationActName = repAOResultActName.getSecond();

		Sanity.eq(medianPreSucDurationPD.size(), medianPreSucDurationActName.size(),
				"medianPreSucDurationPD.size()=" + medianPreSucDurationPD.size() + "medianPreSucDurationActName.size()="
						+ medianPreSucDurationActName.size());

		for (Entry<Integer, Pair<Double, Double>> medianPreSucDurationPDEntry : medianPreSucDurationPD.entrySet())
		{
			Integer actID = medianPreSucDurationPDEntry.getKey();
			Pair<Double, Double> medianPreSucPD = medianPreSucDurationPDEntry.getValue();

			String actName = String.valueOf(actID);
			// corresponding entry from
			Pair<Double, Double> medianPreSucActName = medianPreSucDurationActName.get(actName);

			if (medianPreSucPD.equals(medianPreSucActName))
			{
				// System.out.println("medianPreSucPD==medianPreSucActName");
			}
			else
			{
				System.err.println("Error: for actID:" + actID
						+ ", medianPreSucPD!=medianPreSucActName\nmedianPreSucPD=" + medianPreSucPD.toString()
						+ "\nmedianPreSucActName=" + medianPreSucActName.toString());
			}
		}

	}

	/**
	 * To check sanity of
	 * org.activity.evaluation.RecommendationTestsMar2017GenSeqCleaned2.buildRepresentativeAOsForUserPD()
	 * 
	 * @param repAOResult
	 * @param repAOResultActName
	 */
	public static void compareOnlyNonEmpty(
			Pair<LinkedHashMap<Integer, ActivityObject2018>, LinkedHashMap<Integer, Pair<Double, Double>>> repAOResultPD,
			Pair<LinkedHashMap<String, ActivityObject2018>, LinkedHashMap<String, Pair<Double, Double>>> repAOResultActName)
	{
		if (Constant.primaryDimension.equals(PrimaryDimension.ActivityID) == false)
		{
			PopUps.printTracedErrorMsgWithExit(
					"Error: org.activity.sanityChecks.Sanity.compare() should not be called when primary dimension is : "
							+ Constant.primaryDimension);
		}
		System.out.println("Inside compare for repAOResultPD and repAOResultActName\n");
		LinkedHashMap<Integer, ActivityObject2018> repAOsPD = repAOResultPD.getFirst();
		LinkedHashMap<String, ActivityObject2018> repAOsActName = repAOResultActName.getFirst();

		if (repAOsPD.size() != repAOsActName.size())
		{
			System.out.println(
					"Alert! repAOsPD.size()=" + repAOsPD.size() + "repAOsActName.size()=" + repAOsActName.size());
		}
		// their size can be unequal repAOsPD < repAOsActName because repAOsPD is only for distinct activity names in
		// training data for that user while repAOsActName for all distinct activity names in the dataset.
		// However the representive activity object should be same for each primary dimension val in repAOResultPD.
		for (Entry<Integer, ActivityObject2018> repAOPDEntry : repAOsPD.entrySet())
		{
			Integer actID = repAOPDEntry.getKey();
			ActivityObject2018 repAOPD = repAOPDEntry.getValue();

			String actName = String.valueOf(actID);
			// corresponding entry from
			ActivityObject2018 repAOActName = repAOsActName.get(actName);

			if (repAOPD.equals(repAOActName))
			{
				// System.out.println("repAOPD==repAOActName");
			}
			else
			{
				System.err.println("Error: for actID:" + actID + ", repAOPD!=repAOActName\nrepAOPD="
						+ repAOPD.toStringAllGowallaTS() + "\nrepAOActName=" + repAOActName.toStringAllGowallaTS());
			}
		}

		LinkedHashMap<Integer, Pair<Double, Double>> medianPreSucDurationPD = repAOResultPD.getSecond();
		LinkedHashMap<String, Pair<Double, Double>> medianPreSucDurationActName = repAOResultActName.getSecond();

		if (medianPreSucDurationPD.size() != medianPreSucDurationActName.size())
		{
			System.out.println("Alert! medianPreSucDurationPD.size()=" + medianPreSucDurationPD.size()
					+ "medianPreSucDurationActName.size()=" + medianPreSucDurationActName.size());
		}

		for (Entry<Integer, Pair<Double, Double>> medianPreSucDurationPDEntry : medianPreSucDurationPD.entrySet())
		{
			Integer actID = medianPreSucDurationPDEntry.getKey();
			Pair<Double, Double> medianPreSucPD = medianPreSucDurationPDEntry.getValue();

			String actName = String.valueOf(actID);
			// corresponding entry from
			Pair<Double, Double> medianPreSucActName = medianPreSucDurationActName.get(actName);

			if (medianPreSucPD.equals(medianPreSucActName))
			{
				// System.out.println("medianPreSucPD==medianPreSucActName");
			}
			else
			{
				System.err.println("Error: for actID:" + actID
						+ ", medianPreSucPD!=medianPreSucActName\nmedianPreSucPD=" + medianPreSucPD.toString()
						+ "\nmedianPreSucActName=" + medianPreSucActName.toString());
			}
		}

	}
}
