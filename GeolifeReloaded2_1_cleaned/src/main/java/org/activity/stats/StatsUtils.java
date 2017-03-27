package org.activity.stats;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.activity.constants.Constant;
import org.activity.io.WritingToFile;
import org.activity.ui.PopUps;
import org.activity.util.ComparatorUtils;
import org.activity.util.UtilityBelt;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.util.FastMath;

public class StatsUtils
{

	// public static final int ThresholdFrequency = 2;
	public static final double radiusOfEarthInKMs = 6372.8; // In kilometers radius of earth

	// public static NumberFormat getDecimalFormat(int numOfDecimalPlaces)
	// {
	// String d;
	//
	// for (int i = 0; i < numOfDecimalPlaces; i++)
	// {
	// d += "#";
	// }
	// return new DecimalFormat("##.###");
	// }
	/**
	 * Adds the given value to each element of the given array
	 * 
	 * @param array
	 * @param constantToAdd
	 * @return
	 */
	public static int[] addConstantToEach(int[] array, int constantToAdd)
	{
		int[] newArray = new int[array.length];

		for (int i = 0; i < array.length; i++)
		{
			newArray[i] = array[i] + constantToAdd;
		}
		return newArray;
	}

	/**
	 * Create org.apache.commons.math3.stat.descriptive.DescriptiveStatistics object from the ArrayList(Double)
	 * 
	 * @param valsReceived
	 * @return
	 */
	public static DescriptiveStatistics getDescriptiveStatistics(ArrayList<Double> valsReceived)
	{
		double vals[] = new double[valsReceived.size()];

		for (int i = 0; i < valsReceived.size(); i++)
		{
			vals[i] = valsReceived.get(i);
		}
		return new DescriptiveStatistics(vals);
	}

	public static double getSD(double[] vals)
	{
		DescriptiveStatistics ds = new DescriptiveStatistics(vals);
		double sd = ds.getStandardDeviation();
		return ds.getStandardDeviation();
	}

	public static double getSD(ArrayList<Double> valsArrayList)
	{
		double vals[] = new double[valsArrayList.size()];

		for (int i = 0; i < valsArrayList.size(); i++)
		{
			vals[i] = valsArrayList.get(i);
		}

		DescriptiveStatistics ds = new DescriptiveStatistics(vals);
		double sd = ds.getStandardDeviation();
		return ds.getStandardDeviation();
	}

	public static double getStandardDeviation(ArrayList<Double> array)
	{
		return (new StandardDeviation().evaluate(UtilityBelt.toPrimitive(array)));
		// PearsonsCorrelation pc = new PearsonsCorrelation();
		// , 0, normEditSimilarity.size()));// pc.correlation(UtilityBelt.toPrimitive(normEditSimilarity),
	}

	public static double iqrOfArrayListInt(ArrayList<Integer> arr, int roundOffToPlaces)
	{
		if (arr.size() == 0) return 0;

		double[] vals = new double[arr.size()];

		for (int i = 0; i < arr.size(); i++)
		{
			vals[i] = arr.get(i); // java 1.5+ style (outboxing)
		}

		return StatsUtils.round(StatUtils.percentile(vals, 75) - StatUtils.percentile(vals, 25), roundOffToPlaces);
	}

	/**
	 * Used for case-based approach
	 * <p>
	 * the correlation between the rest and end similarites of candidate timelines with current timelines
	 * 
	 * @param arr1
	 * @param arr2
	 * @return
	 */
	public static double getPearsonCorrelation(ArrayList<Double> arr1, ArrayList<Double> arr2)
	{
		PearsonsCorrelation pc = new PearsonsCorrelation();
		return (pc.correlation(UtilityBelt.toPrimitive(arr1), UtilityBelt.toPrimitive(arr2)));
	}

	/*
	 * TO create a copy or clone
	 */
	/*
	 * public static LinkedHashMap<Date, UserDayTimeline> cUserDayTimelines(LinkedHashMap<Date, UserDayTimeline>
	 * userDayTimelines) {
	 * 
	 * return userDayTimelines; }
	 */
	public static double getPearsonCorrelation(ArrayList<Double> a, ArrayList<Double> b)
	{
		if (a == null || b == null)
		{
			System.err.println("Error: in getPearsonCorrelation, ArrayList object are null (not set)");
			return -9999;
		}

		if (a.size() != b.size())
		{
			System.err.println("Warning: in getPearsonCorrelation, ArrayList object are of different sizes " + a.size()
					+ "!=" + b.size());
			return -9999;
		}

		PearsonsCorrelation pc = new PearsonsCorrelation();
		double ans = (pc.correlation(UtilityBelt.toPrimitive(a), UtilityBelt.toPrimitive(b)));
		if (!(ans == Double.NaN) && Double.isFinite(ans))
		{
			ans = StatsUtils.round(ans, 5);
		}
		return ans;
	}

	public static double getKendallTauCorrelation(ArrayList<Double> a, ArrayList<Double> b)
	{
		if (a == null || b == null)
		{
			System.err.println("Error: in getKendallTauCorrelation, ArrayList object are null (not set)");
			return -9999;
		}

		if (a.size() != b.size())
		{
			System.err.println("Warning: in getKendallTauCorrelation, ArrayList object are of different sizes "
					+ a.size() + "!=" + b.size());
			return -9999;
		}

		KendallsCorrelation pc = new KendallsCorrelation();
		double ans = (pc.correlation(UtilityBelt.toPrimitive(a), UtilityBelt.toPrimitive(b)));
		if (!(ans == Double.NaN) && Double.isFinite(ans))
		{
			ans = StatsUtils.round(ans, 5);
		}
		return ans;
	}

	/**
	 * convert it to bigdecimal form source:http://rosettacode.org/wiki/Haversine_formula#Java ? Not converting to
	 * BigDecimal for performance concerns,
	 * 
	 * This uses the ‘haversine’ formula to calculate the great-circle distance between two points – that is, the
	 * shortest distance over the earth’s surface – giving an ‘as-the-crow-flies’ distance between the points (ignoring
	 * any hills they fly over, of course!).</br>
	 * TODO LATER can use non-native math libraries for faster computation. User jafama or apache common maths.</br>
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return distance in Kilometers
	 */
	public static double haversine(String lat1s, String lon1s, String lat2s, String lon2s)
	{

		double lat1 = Double.parseDouble(lat1s);
		double lon1 = Double.parseDouble(lon1s);

		double lat2 = Double.parseDouble(lat2s);
		double lon2 = Double.parseDouble(lon2s);

		// System.out.println("inside haversine = " + lat1 + "," + lon1 + "--" + lat2 + "," + lon2);
		if (Math.abs(lat1) > 90 || Math.abs(lat2) > 90 || Math.abs(lon1) > 180 || Math.abs(lon2) > 180)
		{
			new Exception("Possible Error in haversin: latitude and/or longitude outside range: provided " + lat1s + ","
					+ lon1s + "  " + lat2s + "," + lon2s);
			if (Constant.checkForHaversineAnomaly)
			{
				PopUps.showError("Possible Error in haversin: latitude and/or longitude outside range: provided "
						+ lat1s + "," + lon1s + "  " + lat2s + "," + lon2s);
			}
			return Constant.unknownDistanceTravelled;// System.exit(-1);
		}

		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		// System.out.println("inside haversine = " + dLat + "," + dLon + "--" + lat2 + "," + lon2);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);

		// System.out.println("a = " + a);
		// double sqrtVal = Math.sqrt(a);
		//
		// if (Double.isNaN(sqrtVal))
		// {
		// PopUps.showException(new Exception("NaN sqrt: for a = " + a + " for latitude and/or longitude outside range:
		// provided " + lat1s
		// + "," + lon1s + " " + lat2s + "," + lon2), "org.activity.util.UtilityBelt.haversine(String, String, String,
		// String)");
		// }

		double c = 2 * Math.asin(Math.sqrt(a)); // TODO: #performanceEater
		// System.out.println("c = " + c);

		if (Constant.checkForDistanceTravelledAnomaly && (radiusOfEarthInKMs * c > Constant.distanceTravelledAlert))
		{
			System.err.println("Probable Error: haversine():+ distance >200kms (=" + radiusOfEarthInKMs * c
					+ " for latitude and/or longitude outside range: provided " + lat1s + "," + lon1s + "  " + lat2s
					+ "," + lon2s);
		}

		return StatsUtils.round(radiusOfEarthInKMs * c, 4);
	}

	/**
	 * 
	 * This uses the ‘haversine’ formula to calculate the great-circle distance between two points – that is, the
	 * shortest distance over the earth’s surface – giving an ‘as-the-crow-flies’ distance between the points (ignoring
	 * any hills they fly over, of course!).</br>
	 * uses FastMath from apache common maths as drop in replacement for java's standard Math.</br>
	 * 
	 * convert it to bigdecimal form source:http://rosettacode.org/wiki/Haversine_formula#Java ? Not converting to
	 * BigDecimal for performance concerns,
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return distance in Kilometers
	 */
	public static double haversineFastMath(String lat1s, String lon1s, String lat2s, String lon2s)
	{

		double lat1 = Double.parseDouble(lat1s);
		double lon1 = Double.parseDouble(lon1s);

		double lat2 = Double.parseDouble(lat2s);
		double lon2 = Double.parseDouble(lon2s);

		// System.out.println("inside haversine = " + lat1 + "," + lon1 + "--" + lat2 + "," + lon2);
		if (FastMath.abs(lat1) > 90 || FastMath.abs(lat2) > 90 || FastMath.abs(lon1) > 180 || Math.abs(lon2) > 180)
		{
			new Exception("Possible Error in haversin: latitude and/or longitude outside range: provided " + lat1s + ","
					+ lon1s + "  " + lat2s + "," + lon2s);
			if (Constant.checkForHaversineAnomaly)
			{
				PopUps.showError("Possible Error in haversin: latitude and/or longitude outside range: provided "
						+ lat1s + "," + lon1s + "  " + lat2s + "," + lon2s);
			}
			return Constant.unknownDistanceTravelled;// System.exit(-1);
		}

		double dLat = FastMath.toRadians(lat2 - lat1);
		double dLon = FastMath.toRadians(lon2 - lon1);
		lat1 = FastMath.toRadians(lat1);
		lat2 = FastMath.toRadians(lat2);

		// System.out.println("inside haversine = " + dLat + "," + dLon + "--" + lat2 + "," + lon2);

		double a = FastMath.sin(dLat / 2) * FastMath.sin(dLat / 2)
				+ FastMath.sin(dLon / 2) * FastMath.sin(dLon / 2) * FastMath.cos(lat1) * FastMath.cos(lat2);

		// System.out.println("a = " + a);
		// double sqrtVal = Math.sqrt(a);
		//
		// if (Double.isNaN(sqrtVal))
		// {
		// PopUps.showException(new Exception("NaN sqrt: for a = " + a + " for latitude and/or longitude outside range:
		// provided " + lat1s
		// + "," + lon1s + " " + lat2s + "," + lon2), "org.activity.util.UtilityBelt.haversine(String, String, String,
		// String)");
		// }

		double c = 2 * FastMath.asin(FastMath.sqrt(a)); // TODO: #performanceEater
		// System.out.println("c = " + c);

		if (Constant.checkForDistanceTravelledAnomaly && (radiusOfEarthInKMs * c > Constant.distanceTravelledAlert))
		{
			System.err.println("Probable Error: haversine():+ distance >200kms (=" + radiusOfEarthInKMs * c
					+ " for latitude and/or longitude outside range: provided " + lat1s + "," + lon1s + "  " + lat2s
					+ "," + lon2s);
		}

		return StatsUtils.round(radiusOfEarthInKMs * c, 4);
	}

	// /**
	// * Using non-native math libraries for faster computation. User jafama or apache common maths.</br>
	// * TODO <font color = red>HAVE NOT FINISHED converting java math functions to new libraries. </font>This uses the
	// * ‘haversine’ formula to calculate the great-circle distance between two points – that is, the shortest distance
	// * over the earth’s surface – giving an ‘as-the-crow-flies’ distance between the points (ignoring any hills they
	// fly
	// * over, of course!).
	// *
	// * @param lat1
	// * @param lon1
	// * @param lat2
	// * @param lon2
	// * @return distance in Kilometers
	// */
	// public static double haversineFasterIncomplete(String lat1s, String lon1s, String lat2s, String lon2s)
	// {
	//
	// double lat1 = Double.parseDouble(lat1s);
	// double lon1 = Double.parseDouble(lon1s);
	//
	// double lat2 = Double.parseDouble(lat2s);
	// double lon2 = Double.parseDouble(lon2s);
	//
	// // System.out.println("inside haversine = " + lat1 + "," + lon1 + "--" + lat2 + "," + lon2);
	// if (Math.abs(lat1) > 90 || Math.abs(lat2) > 90 || Math.abs(lon1) > 180 || Math.abs(lon2) > 180)
	// {
	// new Exception("Possible Error in haversin: latitude and/or longitude outside range: provided " + lat1s + ","
	// + lon1s + " " + lat2s + "," + lon2s);
	// if (Constant.checkForHaversineAnomaly)
	// {
	// PopUps.showError("Possible Error in haversin: latitude and/or longitude outside range: provided "
	// + lat1s + "," + lon1s + " " + lat2s + "," + lon2s);
	// }
	// return Constant.unknownDistanceTravelled;// System.exit(-1);
	// }
	//
	// double dLat = Math.toRadians(lat2 - lat1);
	// double dLon = Math.toRadians(lon2 - lon1);
	// lat1 = Math.toRadians(lat1);
	// lat2 = Math.toRadians(lat2);
	//
	// // System.out.println("inside haversine = " + dLat + "," + dLon + "--" + lat2 + "," + lon2);
	//
	// double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
	// + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
	//
	// // double c = 2 * Math.asin(Math.sqrt(a)); // : #performanceEater
	// double c = 2 * new Asin().value(Math.sqrt(a));
	// // System.out.println("c = " + c);
	//
	// if (Constant.checkForDistanceTravelledAnomaly && (radiusOfEarthInKMs * c > Constant.distanceTravelledAlert))
	// {
	// System.err.println("Probable Error: haversine():+ distance >200kms (=" + radiusOfEarthInKMs * c
	// + " for latitude and/or longitude outside range: provided " + lat1s + "," + lon1s + " " + lat2s
	// + "," + lon2s);
	// }
	//
	// return StatsUtils.round(radiusOfEarthInKMs * c, 4);
	// }

	/**
	 * 
	 * @param lat1s
	 * @param lon1s
	 * @param lat2s
	 * @param lon2s
	 * @param roundTheResult
	 * @return
	 */
	public static double haversine(String lat1s, String lon1s, String lat2s, String lon2s, boolean roundTheResult)
	{

		double lat1 = Double.parseDouble(lat1s);
		double lon1 = Double.parseDouble(lon1s);

		double lat2 = Double.parseDouble(lat2s);
		double lon2 = Double.parseDouble(lon2s);

		if (Math.abs(lat1) > 90 || Math.abs(lat2) > 90 || Math.abs(lon1) > 180 || Math.abs(lon2) > 180)
		{
			new Exception("Possible Error in haversin: latitude and/or longitude outside range: provided " + lat1s + ","
					+ lon1s + "  " + lat2s + "," + lon2s);

			if (Constant.checkForHaversineAnomaly)
			{
				PopUps.showError("Possible Error in haversin: latitude and/or longitude outside range: provided "
						+ lat1s + "," + lon1s + "  " + lat2s + "," + lon2s);
			}
			return Constant.unknownDistanceTravelled;
			// System.exit(-1);

		}

		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.asin(Math.sqrt(a));

		if (Constant.checkForDistanceTravelledAnomaly && (radiusOfEarthInKMs * c > Constant.distanceTravelledAlert))
		{
			System.err.println("Probable Error: haversine():+ distance >200kms (=" + radiusOfEarthInKMs * c
					+ " for latitude and/or longitude outside range: provided " + lat1s + "," + lon1s + "  " + lat2s
					+ "," + lon2s);
		}

		if (roundTheResult)
		{
			return StatsUtils.round(radiusOfEarthInKMs * c, 4);
		}

		else
		{
			return (radiusOfEarthInKMs * c);
		}

	}

	/**
	 * convert it to bigdecimal form source:http://rosettacode.org/wiki/Haversine_formula#Java
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return distance in Kilometers
	 */
	public static double haversine(double lat1, double lon1, double lat2, double lon2)
	{
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.asin(Math.sqrt(a));
		return radiusOfEarthInKMs * c;
	}

	/**
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return
	 */
	public static double haversineFastMath(double lat1, double lon1, double lat2, double lon2)
	{
		double dLat = FastMath.toRadians(lat2 - lat1);
		double dLon = FastMath.toRadians(lon2 - lon1);
		lat1 = FastMath.toRadians(lat1);
		lat2 = FastMath.toRadians(lat2);

		double a = FastMath.sin(dLat / 2) * FastMath.sin(dLat / 2)
				+ FastMath.sin(dLon / 2) * FastMath.sin(dLon / 2) * FastMath.cos(lat1) * FastMath.cos(lat2);
		double c = 2 * FastMath.asin(FastMath.sqrt(a));
		return radiusOfEarthInKMs * c;
	}

	/**
	 * Returns the average of the BigDecimal values in the given ArrayList note: checked OK
	 * 
	 * @param vals
	 * @return
	 */
	public static BigDecimal average(ArrayList<BigDecimal> vals)
	{
		BigDecimal res = new BigDecimal("-999999");

		BigDecimal sum = BigDecimal.ZERO;

		for (BigDecimal value : vals)
		{
			sum = sum.add(value);
		}

		res = sum.divide(new BigDecimal(vals.size()));
		// res = sum.divide(BigDecimal.valueOf(vals.size()));

		return res;

	}

	public static double averageOfListDouble(ArrayList<Double> vals)
	{
		double res = -9999999;

		double sum = 0;// Double.ZERO;

		for (double value : vals)
		{
			sum = sum + value;
		}

		res = sum / vals.size();

		return res;

	}

	public static double averageOfListInteger(ArrayList<Integer> vals)
	{
		double res = -9999999;

		double sum = 0;// Double.ZERO;

		for (double value : vals)
		{
			sum = sum + value;
		}

		res = sum / vals.size();

		return res;

	}

	/**
	 * Returns the average of the decimals stored as String values in the given ArrayList. It uses BigDecimal during
	 * calculation to have precise results note: ALWYAS create BigDecimal from String and not from number(e.g. double)
	 * for precision.
	 * 
	 * @param vals
	 * @return
	 */
	public static String averageDecimalsAsStrings(ArrayList<String> vals)
	{
		String res = new String("-999999");
		BigDecimal resultInDecimal = new BigDecimal("-999999");

		BigDecimal sumInDecimal = BigDecimal.ZERO;

		if (vals.size() == 0)
		{
			return "0";
		}
		try
		{
			for (String value : vals)
			{
				BigDecimal valueInDecimal = new BigDecimal(value);
				sumInDecimal = sumInDecimal.add(valueInDecimal);
			}
			resultInDecimal = sumInDecimal.divide(new BigDecimal(String.valueOf(vals.size())), 3, RoundingMode.HALF_UP); // ref:
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}

		return resultInDecimal.toString();

	}

	/**
	 * Return the next double value(with no decimal part) greater than the value and is a multiple of multiple
	 * 
	 * @param value
	 * @param multiple
	 * @return
	 */
	public static double ceilNearestMultipleOf(double value, int multiple)
	{
		double res = value + 1; // we have to get next value and not this value (even though if the values was a
								// multiple of multiple or not)

		System.out.println("res=" + res);
		double rem = res % multiple;

		System.out.println("rem=" + rem);
		if (rem != 0)
		{
			res = res + (multiple - rem);
		}
		System.out.println("res=" + res);
		return res;
	}

	public static boolean isMaximum(double tocheck, double a, double b, double c)
	{
		return tocheck == Math.max(Math.max(a, b), c);
	}

	public static LinkedHashMap<String, Long> getFrequencyDistribution(double[] values)
	{
		Map<String, Long> fd = new TreeMap<String, Long>();

		for (double a : values)
		{
			String key = Double.toString(a);

			if (fd.containsKey(key))
			{
				fd.put(key, fd.get(key) + 1);
			}
			else
			{
				fd.put(key, (long) 1);
			}

		}

		fd = ComparatorUtils.sortByValueDesc(fd);

		WritingToFile.writeSimpleMapToFile(fd, Descriptive.commonPath + "TimeDifference_Frequency distribution.csv",
				"Value", "Frequency Count");

		return (LinkedHashMap<String, Long>) fd;

	}

	public static DescriptiveStatistics getDescriptiveStatistics(double[] values, String nameForValue,
			String fileNameToWrite)
	{

		DescriptiveStatistics dstats = new DescriptiveStatistics(values);
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumIntegerDigits(30);

		StringBuffer message = new StringBuffer("-------------------------------\nDescriptive stats for " + nameForValue
				+ "\n-------------------------------\n");

		if (dstats.getN() != values.length)
		{
			System.err.println("Error in getDescriptiveStatistics:dstats.getN() != values.length ");
		}

		String m1 = "Count = " + nf.format(dstats.getN()) + "\n" + "Maximum = " + nf.format(dstats.getMax()) + "\n"
				+ "Minimum = " + nf.format(dstats.getMin()) + "\n" + "Arithmetic mean = " + nf.format(dstats.getMean())
				+ "\n" + "Standard deviation = " + nf.format(dstats.getStandardDeviation()) + "\n" + "Q1 = "
				+ nf.format(dstats.getPercentile(25)) + "\n" + "Q2(median) = " + nf.format(dstats.getPercentile(50))
				+ "\n" + "Q3 = " + nf.format(dstats.getPercentile(75)) + "\n" + "Skewness = "
				+ nf.format(dstats.getSkewness()) + "\n" + "Kurtosis = " + nf.format(dstats.getKurtosis()) + "\n"
				+ "Sum = " + nf.format(dstats.getSum()) + "\n" + "-------------------------------\n";

		message.append(m1);
		WritingToFile.writeToNewFile(message.toString(), Constant.getCommonPath() + "Stats_" + fileNameToWrite); // TODO
																													// check
																													// if
																													// this
																													// works
																													// corrcetly
		// System.out.println(m1);

		return dstats;
	}

	public static double meanOfArrayList(ArrayList<Double> arr, int roundOffToPlaces)
	{
		if (arr.size() == 0) return 0;

		double[] vals = new double[arr.size()];

		for (int i = 0; i < arr.size(); i++)
		{
			vals[i] = arr.get(i); // java 1.5+ style (outboxing)
		}

		return round(StatUtils.mean(vals), roundOffToPlaces);
	}

	public static double meanOfArrayListInt(ArrayList<Integer> arr, int roundOffToPlaces)
	{
		if (arr.size() == 0) return 0;

		double[] vals = new double[arr.size()];

		for (int i = 0; i < arr.size(); i++)
		{
			vals[i] = arr.get(i); // java 1.5+ style (outboxing)
		}

		return round(StatUtils.mean(vals), roundOffToPlaces);
	}

	public static double medianOfArrayListInt(ArrayList<Integer> arr, int roundOffToPlaces)
	{
		if (arr.size() == 0) return 0;

		double[] vals = new double[arr.size()];

		for (int i = 0; i < arr.size(); i++)
		{
			vals[i] = arr.get(i); // java 1.5+ style (outboxing)
		}

		return round(StatUtils.percentile(vals, 50), roundOffToPlaces);
	}

	/**
	 * <font color = blue> Note: using the preferred way to convert double to BigDecimal</font>
	 * 
	 * @param value
	 * @param places
	 * @return
	 */
	public static double round(double value, int places)
	{
		if (Double.isInfinite(value))
		{
			return 99999;
		}
		if (Double.isNaN(value))
		{
			return 0;
		}

		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = BigDecimal.valueOf(value);// new BigDecimal(value); //change on 22 Nov 2016
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	/**
	 * 
	 * @param value
	 * @param places
	 * @return
	 */
	public static String round(String value, int places)
	{
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.toPlainString();
	}

	// //////////
	/**
	 * returns the shannon entropy of the given string (courtesy: http://rosettacode.org/wiki/Entropy#Java)
	 * 
	 * @param s
	 * @return
	 */
	@SuppressWarnings("boxing")
	public static double getShannonEntropy(String s)
	{

		int n = 0;
		Map<Character, Integer> occ = new HashMap<>();

		for (int c_ = 0; c_ < s.length(); ++c_)
		{
			char cx = s.charAt(c_);
			if (occ.containsKey(cx))
			{
				occ.put(cx, occ.get(cx) + 1);
			}
			else
			{
				occ.put(cx, 1);
			}
			++n;
		}

		double e = 0.0;
		for (Map.Entry<Character, Integer> entry : occ.entrySet())
		{
			char cx = entry.getKey();
			double p = (double) entry.getValue() / n;
			e += p * log2(p);
		}

		System.out.println("\n\nEntropy for string: " + s + "\n\t is " + (-e));
		return -e;
	}

	public static double log2(double a)
	{
		return Math.log(a) / Math.log(2);
	}

	/**
	 * Return a random in within the given range
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public static int randomInRange(int min, int max)
	{
		return (min + (int) (Math.random() * ((max - min) + 1)));
	}

	/**
	 * Return a random in within the given range with bias for a particular integer value
	 * 
	 * @param min
	 * @param max
	 * @param biasNumber
	 * @return
	 */
	public static int randomInRangeWithBias(int min, int max, int biasNumber)
	{
		if (Math.random() < 0.35) // 35% bias approx
		{
			return biasNumber;
		}
		else
			return (min + (int) (Math.random() * ((max - min) + 1)));
	}

	/**
	 * Return a random in within the given range with bias for a particular integer value
	 * 
	 * 
	 * @param min
	 * @param max
	 * @param biasNumber
	 * @param biasInPercentage
	 *            between 0 and 1
	 * @return
	 */
	public static int randomInRangeWithBias(int min, int max, int biasNumber, double biasInPercentage)
	{
		if (Math.random() < biasInPercentage) // 35% bias approx
		{
			return biasNumber;
		}
		else
			return (min + (int) (Math.random() * ((max - min) + 1)));
	}

	/**
	 * 
	 * @param currentLat
	 * @param currentLon
	 * @return
	 */
	public static boolean isValidGeoCoordinate(String currentLat, String currentLon)
	{
		if (Math.abs(Double.valueOf(currentLat)) > 90 || Math.abs(Double.valueOf(currentLon)) > 180)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Returns min max norm if max - min >0 else return 0 (as distance) ...leading to 1 as similarity (rounded off to 4
	 * decimal places)
	 * 
	 * @param val
	 * @param max
	 * @param min
	 * @return
	 */
	public static double minMaxNorm(double val, double max, double min)
	{
		if ((max - min) > 0)
		{
			return round(((val - min) / (max - min)), 4);
		}
		else
		{
			System.err.println("Warning: Alert!! minMaxNorm: max - min <=0 =" + (max - min));
			return 0;
		}

	}
}
