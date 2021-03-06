package org.activity.stats;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.activity.constants.Constant;
import org.activity.constants.Enums.SummaryStat;
import org.activity.io.ReadingFromFile;
import org.activity.io.WToFile;
import org.activity.objects.Pair;
import org.activity.objects.Triple;
import org.activity.ui.PopUps;
import org.activity.util.ComparatorUtils;
import org.activity.util.StringUtils;
import org.activity.util.UtilityBelt;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.stat.descriptive.rank.Percentile;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

public final class StatsUtils
{

	// public static final int ThresholdFrequency = 2;
	public static final double radiusOfEarthInKMs = 6372.8; // In kilometers radius of earth

	private StatsUtils()
	{

	}

	/**
	 * 
	 * @param n
	 * @return
	 */
	public static int factorial(int n)
	{
		return (n == 0 ? 1 : (n * factorial(n - 1)));
	}
	// 5, 15,20, 50, 100, 55, 70,101,-1 . max = 100, min = 0, binSize =10
	// maxVal is maxValExcluding
	// bins: [0,10),[10,20), ....... ,[90,100)
	// num of bins:= 10 = 0, 1,......9
	// num of bins = (max-min)/binSize
	// floor((5-min)/binsize) = 5/10 = 0.5 --> 0 = bin 0
	// floor((0-min)/binsize) = 0/10 = 0 --> 0 = bin 0
	// floor((10-min)/binsize) = 10/10 = 1 --> 1 = bin 1
	// floor((9.99-min)/binsize) = 0.99/10 = 0.99 --> 0 = bin 0
	// floor(-1-0)/binsize = -1/10 = -0.1--> -1 error
	// floor(100-0)/binsize = 100/10 = 10--> 10 --> bin 10 // rror
	// floor(101-0)/binsize = 101/10 = 10.1--> 10 --> bin 10 // error

	/**
	 * Assign each val to a bin
	 * 
	 * @param vals
	 * @param binSize
	 * @param verbose
	 * @return {Pair{Triple {valToBinIndex, binIndexBoundary, binIndexListOfVals}}, numOfBins}
	 * @since 1 April 2018
	 */
	public static Pair<Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>>, Integer> binValuesByBinSize(
			List<Double> vals, double binSize, boolean verbose)
	{
		// 100, 101, 100.5,100.99
		double maxVal = vals.stream().mapToDouble(e -> Double.valueOf(e)).max().getAsDouble();
		double minVal = vals.stream().mapToDouble(e -> Double.valueOf(e)).min().getAsDouble();
		double maxValExcluding = Math.floor(maxVal) + 1; // 101, 102, 101
		int numOfBins = (int) Math.ceil((maxValExcluding - minVal) / binSize);

		Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>> res = binVals(
				vals, numOfBins, binSize, maxVal, minVal, maxValExcluding, verbose);
		List<Pair<Double, Integer>> valToBinIndex = res.getFirst();
		// return new Pair<List<Pair<Double, Integer>>, Integer>(valToBinIndex, numOfBins);
		return new Pair<Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>>, Integer>(
				res, numOfBins);
	}

	/**
	 * Assign each val to a bin
	 * 
	 * @param vals
	 * @param binSize
	 * @param verbose
	 * @param minValueFixed
	 * @return {Pair{Triple {valToBinIndex, binIndexBoundary, binIndexListOfVals}, numOfBins}
	 *
	 */
	public static Pair<Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>>, Integer> binValuesByBinSize(
			List<Double> vals, double binSize, boolean verbose, double minValueFixed, String title)
	{
		// 100, 101, 100.5,100.99
		if (verbose)
		{
			System.out.println("~~~~~~~  Histogram of numOfLocIDsInEachGridID ~~~~~~~~~~ ");
		}

		double maxVal = vals.stream().mapToDouble(e -> Double.valueOf(e)).max().getAsDouble();
		double minVal = minValueFixed;
		double maxValExcluding = Math.floor(maxVal) + 1; // 101, 102, 101
		int numOfBins = (int) Math.ceil((maxValExcluding - minVal) / binSize);

		Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>> res = binVals(
				vals, numOfBins, binSize, maxVal, minVal, maxValExcluding, verbose);

		if (verbose)
		{
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ");
		}

		List<Pair<Double, Integer>> valToBinIndex = res.getFirst();
		// return new Pair<List<Pair<Double, Integer>>, Integer>(valToBinIndex, numOfBins);
		return new Pair<Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>>, Integer>(
				res, numOfBins);
	}

	/**
	 * Assign each val to a bin
	 * 
	 * @param vals
	 * @param binSize
	 * @param verbose
	 * @return Triple{valToBinIndex, binIndexBoundary, binIndexListOfVals}
	 */
	public static Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>> binValuesByBinSize2(
			List<Double> vals, double binSize, boolean verbose)
	{
		// 100, 101, 100.5,100.99
		double maxVal = vals.stream().mapToDouble(e -> Double.valueOf(e)).max().getAsDouble();
		double minVal = vals.stream().mapToDouble(e -> Double.valueOf(e)).min().getAsDouble();
		double maxValExcluding = Math.floor(maxVal) + 1; // 101, 102, 101
		int numOfBins = (int) Math.ceil((maxValExcluding - minVal) / binSize);

		return binVals(vals, numOfBins, binSize, maxVal, minVal, maxValExcluding, verbose);
	}

	// 5, 15,20, 50, 100, 55, 70,101,-1 . max = 100, min = 0, binSize =10
	// maxVal is maxValExcluding
	// bins: [0,10),[10,20), ....... ,[90,100)
	// num of bins:= 10 = 0, 1,......9
	// num of bins = (max-min)/binSize
	// floor((5-min)/binsize) = 5/10 = 0.5 --> 0 = bin 0
	// floor((0-min)/binsize) = 0/10 = 0 --> 0 = bin 0
	// floor((10-min)/binsize) = 10/10 = 1 --> 1 = bin 1
	// floor((9.99-min)/binsize) = 0.99/10 = 0.99 --> 0 = bin 0
	// floor(-1-0)/binsize = -1/10 = -0.1--> -1 error
	// floor(100-0)/binsize = 100/10 = 10--> 10 --> bin 10 // rror
	// floor(101-0)/binsize = 101/10 = 10.1--> 10 --> bin 10 // error

	/**
	 * Assign each val to a bin
	 * 
	 * @param vals
	 * @param numOfBins
	 * @param verbose
	 * @return {Pair{Triple {valToBinIndex, binIndexBoundary, binIndexListOfVals}}, binSize}
	 * @since 1 April 2018
	 */
	public static Pair<Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>>, Double> binValuesByNumOfBins(
			List<Double> vals, int numOfBins, boolean verbose)
	{

		double maxVal = vals.stream().mapToDouble(e -> Double.valueOf(e)).max().getAsDouble();
		double minVal = vals.stream().mapToDouble(e -> Double.valueOf(e)).min().getAsDouble();
		double maxValExcluding = Math.floor(maxVal) + 1; // 101, 102, 101
		double binSize = (int) Math.ceil((maxValExcluding - minVal) / numOfBins);

		Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>> res = binVals(
				vals, numOfBins, binSize, maxVal, minVal, maxValExcluding, verbose);
		List<Pair<Double, Integer>> valToBinIndex = res.getFirst();

		return new Pair<Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>>, Double>(
				res, binSize);
		// return new Pair<List<Pair<Double, Integer>>, Double>(valToBinIndex, binSize);
	}

	/**
	 * Assign each val to a bin
	 * 
	 * @param vals
	 * @param numOfBins
	 * @param verbose
	 * @param minValueFixed
	 * @return {Pair{Triple {valToBinIndex, binIndexBoundary, binIndexListOfVals}}, binSize}
	 * @since 11 July 2018
	 */
	public static Pair<Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>>, Double> binValuesByNumOfBins(
			List<Double> vals, int numOfBins, boolean verbose, int minValueFixed)
	{

		double maxVal = vals.stream().mapToDouble(e -> Double.valueOf(e)).max().getAsDouble();
		double minVal = minValueFixed;
		double maxValExcluding = Math.floor(maxVal) + 1; // 101, 102, 101
		double binSize = (int) Math.ceil((maxValExcluding - minVal) / numOfBins);

		Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>> res = binVals(
				vals, numOfBins, binSize, maxVal, minVal, maxValExcluding, verbose);
		List<Pair<Double, Integer>> valToBinIndex = res.getFirst();

		return new Pair<Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>>, Double>(
				res, binSize);
		// return new Pair<List<Pair<Double, Integer>>, Double>(valToBinIndex, binSize);
	}

	/**
	 * Assign each val to a bin
	 * 
	 * @param vals
	 * @param numOfBins
	 * @param verbose
	 * @return Triple{valToBinIndex, binIndexBoundary, binIndexListOfVals}
	 * @since 1 April 2018
	 */
	public static Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>> binValuesByNumOfBins2(
			List<Double> vals, int numOfBins, boolean verbose)
	{

		double maxVal = vals.stream().mapToDouble(e -> Double.valueOf(e)).max().getAsDouble();
		double minVal = vals.stream().mapToDouble(e -> Double.valueOf(e)).min().getAsDouble();
		double maxValExcluding = Math.floor(maxVal) + 1; // 101, 102, 101
		double binSize = (int) Math.ceil((maxValExcluding - minVal) / numOfBins);

		return (binVals(vals, numOfBins, binSize, maxVal, minVal, maxValExcluding, verbose));

	}

	/**
	 * 
	 * @param vals
	 * @param numOfBins
	 * @param binSize
	 * @param maxVal
	 * @param minVal
	 * @param maxValExcluding
	 * @param verbose
	 * @return Triple {valToBinIndex, binIndexBoundary, binIndexListOfVals}
	 */
	private static Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>> binVals(
			List<Double> vals, int numOfBins, double binSize, double maxVal, double minVal, double maxValExcluding,
			boolean verbose)
	{
		List<Pair<Double, Integer>> valToBinIndex = new ArrayList<>();
		Map<Integer, Pair<Double, Double>> binIndexBoundary = new TreeMap<>();
		Map<Integer, List<Double>> binIndexListOfVals = new TreeMap<>();

		int index = 0;
		for (double d = minVal; d < maxValExcluding;)
		{
			binIndexBoundary.put(index, new Pair<>(d, d + binSize));
			d += binSize;
			index += 1;
		}

		// initialise with empty list of vals for each bin index
		binIndexBoundary.keySet().forEach(e -> binIndexListOfVals.put(e, new ArrayList<>()));

		for (double v : vals)
		{
			int binIndex = (int) (Math.floor(v - minVal) / binSize);

			if (binIndex < 0)
			{
				PopUps.printTracedWarningMsg("val:" + v + " is < min:" + minVal);
			}
			else if (binIndex > numOfBins)
			{
				PopUps.printTracedWarningMsg("val:" + v + " is > maxValExcluding:" + maxValExcluding);
			}
			else
			{
				valToBinIndex.add(new Pair(v, binIndex));
			}
		}

		// Get number of values in each bin for histogram
		for (Pair<Double, Integer> e : valToBinIndex)
		{
			binIndexListOfVals.get(e.getSecond()).add(e.getFirst());
		}
		//

		if (verbose && false)// disabled as replaced by better verbose DO NOT DELETE
		{
			System.out.println("\nnumOfVals=" + vals.size() + "\nnumOfBins=" + numOfBins + "\nmaxVal=" + maxVal
					+ "\nmaxValExcluding=" + maxValExcluding + "\nminVal=" + minVal + "\nbinSize=" + binSize);

			StringBuilder sbT1 = new StringBuilder();

			sbT1.append("\nBin boundaries are:\n");
			binIndexBoundary.entrySet().stream().forEachOrdered(
					e -> sbT1.append("[" + e.getValue().getFirst() + "," + e.getValue().getSecond() + "),\t"));

			if (false)
			{
				sbT1.append("\nBinned fill vals:\n\tval - binIndex\n");
				valToBinIndex.stream()
						.forEachOrdered(e -> sbT1.append("\t" + e.getFirst() + " - " + e.getSecond() + " : " + "\n"));

				sbT1.append("\nVals in each bin are:\n\tbinIndex - Vals\n");
				binIndexListOfVals.entrySet().stream()
						.forEachOrdered(e -> sbT1.append("\t" + e.getKey() + " - " + e.getValue() + " : " + "\n"));
			}
			sbT1.append("\nNumOfVals in each bin are:\n\tbinIndex - NumOfVals\n");
			binIndexListOfVals.entrySet().stream()
					.forEachOrdered(e -> sbT1.append("\t" + e.getKey() + " - " + e.getValue().size() + " : " + "\n"));

			sbT1.append("\nSum of vals= "
					+ binIndexListOfVals.entrySet().stream().mapToInt(e -> e.getValue().size()).sum() + "\n");

			System.out.println(sbT1.toString());

		}

		if (verbose)
		{
			StringBuilder sbT1 = new StringBuilder("-------------------\n");

			sbT1.append("\nnumOfVals=" + vals.size() + "\nnumOfBins=" + numOfBins + "\nbinSize=" + binSize + "\nminVal="
					+ minVal + "\nmaxVal=" + maxVal + "\nmaxValExcluding=" + maxValExcluding + "\n");

			sbT1.append("\nbinIndex - BinBoundary - %OfTotalVals - NumOfVals\n");

			int sumOfNumOfValsInEachBin = binIndexListOfVals.entrySet().stream().mapToInt(e -> e.getValue().size())
					.sum();
			int maxOfNumOfValsInABin = binIndexListOfVals.entrySet().stream().mapToInt(e -> e.getValue().size()).max()
					.getAsInt();
			String plotChar = "|";
			int maxBarWidth = 180;
			// double numOfCharsForEachVal = maxBarWidth / maxNumOfValsInABin;

			for (Entry<Integer, List<Double>> e : binIndexListOfVals.entrySet())
			{
				Integer binIndex = e.getKey();
				int numOfValuesInThisBin = e.getValue().size();
				Pair<Double, Double> binBoundary = binIndexBoundary.get(binIndex);

				double percentageOfTotalValues = (numOfValuesInThisBin * 100.0 / sumOfNumOfValsInEachBin);

				String string = StringUtils.fixedLengthString(binIndex + "-" + "[" + binBoundary.getFirst() + ","
						+ binBoundary.getSecond() + ")- " + StatsUtils.round(percentageOfTotalValues, 2) + "% -", 28);

				int widthOfBar = ((maxBarWidth * numOfValuesInThisBin) / maxOfNumOfValsInABin);

				String bar1 = new String(new char[widthOfBar]).replace("\0", plotChar);
				sbT1.append(string + bar1 + "\t" + numOfValuesInThisBin + "\n");

			}

			sbT1.append("\nSum of vals= " + sumOfNumOfValsInEachBin + "\n-------------------\n");

			System.out.println(sbT1.toString());
		}
		return new Triple<>(valToBinIndex, binIndexBoundary, binIndexListOfVals);
	}

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
	 * <P>
	 * TODO: REDUNDANT: remove safely later
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

	/**
	 * Create org.apache.commons.math3.stat.descriptive.DescriptiveStatistics object from the ArrayList(Double)
	 * 
	 * @param valsReceived
	 * @return
	 */
	public static DescriptiveStatistics getDescriptiveStatistics(List<Double> valsReceived)
	{
		double vals[] = new double[valsReceived.size()];
		for (int i = 0; i < valsReceived.size(); i++)
		{
			vals[i] = valsReceived.get(i);
		}
		return new DescriptiveStatistics(vals);
	}

	public static String toStringDescriptiveStats(List<Double> valsReceived, String label)
	{
		return toStringDescriptiveStats(getDescriptiveStatistics(valsReceived), label);
	}

	/**
	 * 
	 * @param d
	 * @param label
	 * @return
	 * @since 29 Nov 2018
	 */
	public static String toStringDescriptiveStats(DescriptiveStatistics d, String label)
	{

		String endl = "\n";
		StringBuilder outBuffer = new StringBuilder("=============== " + label + " ===============" + endl);
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(5);

		outBuffer.append("DescriptiveStatistics:").append(endl);
		outBuffer.append("n:\t").append(d.getN()).append(endl);
		outBuffer.append("min:\t").append(df.format(d.getMin())).append(endl);
		outBuffer.append("max:\t").append(df.format(d.getMax())).append(endl);
		outBuffer.append("mean:\t").append(df.format(d.getMean())).append(endl);
		outBuffer.append("std dev:\t").append(df.format(d.getStandardDeviation())).append(endl);
		try
		{
			// No catch for MIAE because actual parameter is valid below
			outBuffer.append("25thP:\t").append(df.format(d.getPercentile(25))).append(endl);
			outBuffer.append("median:\t").append(df.format(d.getPercentile(50))).append(endl);
			outBuffer.append("75thP:\t").append(df.format(d.getPercentile(75))).append(endl);
			outBuffer.append("IQR:\t").append(df.format(d.getPercentile(75) - d.getPercentile(25))).append(endl);
		}
		catch (MathIllegalStateException ex)
		{
			outBuffer.append("median:\tunavailable").append(endl);
		}

		List<Double> percentilesList = Arrays.asList(1d, 5d, 10d, 20d, 25d, 30d, 40d, 50d, 60d, 70d, 80d, 90d, 100d);
		percentilesList.stream().forEachOrdered(p -> outBuffer.append(p + "\t"));
		outBuffer.append(endl);
		percentilesList.stream().forEachOrdered(p -> outBuffer.append(df.format(d.getPercentile(p)) + "\t"));
		outBuffer.append(endl);

		outBuffer.append("skewness:\t").append(df.format(d.getSkewness())).append(endl);
		outBuffer.append("kurtosis:\t").append(df.format(d.getKurtosis())).append(endl);

		return outBuffer.toString();

	}

	public static void checkPercentile()
	{
		// compare percentile computation
		List<Double> vals0 = new Random().doubles(1000, 0, 100).map(e -> StatsUtils.round(e, 1)).boxed()
				.collect(Collectors.toList());

		List<Integer> valsInt = Arrays.asList(45360, 48960, 51360, 13440, 38160, 35760, 41760, 31560, 1500, 1500, 4500,
				5100, 18900, 26100, 28500, 24900, 4500, 8100, 14100, 14100, 8100, 15300, 24900, 21300, 21300, 14100);
		List<Double> vals = valsInt.stream().map(i -> new Double(i)).collect(Collectors.toList());

		System.out.println("vals = " + vals);

		double quantile = 75;

		System.out.println(getDescriptiveStatistics(vals));
		System.out.println("1e-55=" + getPercentile(vals, 1e-55));
		System.out.println("0.000000000000000000001=" + getPercentile(vals, 0.000000000000000000001));
		System.out.println("0.00009=" + getPercentile(vals, 0.00009));
		System.out.println("0.1=" + getPercentile(vals, 0.1));
		System.out.println("1=" + getPercentile(vals, 1));
		System.out.println("25=" + getPercentile(vals, 25));
		System.out.println("50=" + getPercentile(vals, 50));
		System.out.println("75=" + getPercentile(vals, 75));
		System.out.println("99=" + getPercentile(vals, 99));
		System.out.println("100=" + getPercentile(vals, 100));

		long t3 = System.nanoTime();
		System.out.println(getPercentileSlower(vals, quantile));
		long t4 = System.nanoTime();

		long t1 = System.nanoTime();
		System.out.println(getPercentile(vals, quantile));
		long t2 = System.nanoTime();

		System.out.println("getPercentile: " + ((t2 - t1) * 1.0) / 1000000 + " ns");
		System.out.println("getPercentileSlower:" + ((t4 - t3) * 1.0) / 1000000 + " ns");
	}

	public static void main(String args[])
	{
		checkBinnning();
		// checkPercentile();
		// System.out.println(factorial(10));
		// checkSeqOfSameScores();
		// analaysePossibleReorderingsNov29_2018();
	}

	public static void analaysePossibleReorderingsNov29_2018()
	{

		// EDAlpha1 String fileToRead =
		// "/mnt/sshServers/theengine/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/geolife1_NOV28H17M17ED1.0STimeDurDistTrStartGeoEndGeoAvgAltAllActsFDStFilter0hrsFEDPerFS_10F_RTVNoTTFilter/All/MatchingUnit1.0/dataRankedRecommendationWithScores0.csv";
		String commonPathToRead = // "/mnt/sshServers/theengine/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/geolife1_NOV28H17M17ED1.0STimeDurDistTrStartGeoEndGeoAvgAltAllActsFDStFilter0hrsFEDPerFS_10F_RTVNoTTFilter/All/";
				"/mnt/sshServers/theengine/GowallaWorkspace/JavaWorkspace/GeolifeReloaded2_1_cleaned/dataWritten/geolife1_NOV28H17M17ED1.0STimeDurDistTrStartGeoEndGeoAvgAltAllActsFDStFilter0hrsFEDPerFS_10F_RTVNoTTFilter/All/";

		String[] splittedCommonPath = commonPathToRead.split("/");
		String expLabel = splittedCommonPath[splittedCommonPath.length - 2];
		// PopUps.showMessage("splittedCommonPath= " + Arrays.asList(splittedCommonPath) + "\nexpLabel= " + expLabel);

		// MatchingUnit8.0/dataRankedRecommendationWithScores0.csv";
		String commonPathToWrite = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/PossibleReorderingInvestogationsNov29/";
		StringBuilder sb = new StringBuilder("mu,NumOfNon1PossibleRorderings,%RTsAffetectedOverAllUsers\n");
		for (double mu : Constant.matchingUnitAsPastCountFixed)
		{
			String fileToRead = commonPathToRead + "/MatchingUnit" + mu + "/dataRankedRecommendationWithScores0.csv";
			Pair<Long, Double> res = findNumOfPossOrderingForEachRTInDataRankedRecommendationWithScores0(fileToRead,
					commonPathToWrite + expLabel + "MU" + mu + "PossReorderings");
			sb.append(mu + "," + res.getFirst() + "," + res.getSecond() + "\n");
		}
		WToFile.writeToNewFile(sb.toString(), commonPathToWrite + expLabel + "PossReorderingByMUsOverUsers.csv");
	}

	/**
	 * 
	 * @param absFileNameToRead
	 * @param absFileToWrite
	 * @return
	 * @since Nov 29 2018
	 */
	public static Pair<Long, Double> findNumOfPossOrderingForEachRTInDataRankedRecommendationWithScores0(
			String absFileNameToRead, String absFileToWrite)
	{
		// __6:1.1667__5:0.8333__11:0.0__10:0.0__7:0.0__9:0.0
		List<List<String>> allData = ReadingFromFile.readLinesIntoListOfLists(absFileNameToRead, ",");
		StringBuilder sb = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		StringBuilder sbNumOfRts = new StringBuilder(
				"NumOfRtsAffectedForUser,TotalNumOfRtsForUser,PercentageAffected\n");
		long totalNumOfNon1ReOrderings = 0;
		int totalNumOfRtsAffected = 0;
		int totalNumOfRts = 0;
		for (List<String> line : allData)
		{
			int totalNumOfRTsForThisUser = line.size();
			totalNumOfRts += totalNumOfRTsForThisUser;
			int numOfRTsWithReOrderingPossibleForThisUser = 0;

			for (String eachCell : line)
			{
				String[] splittedCell = eachCell.split("__");
				System.out.println("splittedCell= " + Arrays.asList(splittedCell));
				LinkedHashMap<String, Double> recommScorePairs = new LinkedHashMap<>();
				for (String s : splittedCell)
				{
					if (s.trim().length() == 0)
					{
						continue;
					}
					else
					{
						String[] splittedVals = s.split(":");
						recommScorePairs.put(splittedVals[0], Double.valueOf(splittedVals[1]));
					}
				}
				System.out.println("cell  = " + eachCell);
				System.out.println("recommScorePairs  = " + recommScorePairs + "\n");

				List<Integer> seqofSames = seqOfSameScores2(recommScorePairs);

				sb2.append(seqofSames.stream().map(e -> String.valueOf(e)).collect(Collectors.joining("__")) + ",");
				int n = numOfPossibleOrderings(seqofSames);
				if (n > 1)
				{
					sb.append(n);
					totalNumOfNon1ReOrderings += n;
					numOfRTsWithReOrderingPossibleForThisUser += 1;
				}
				sb.append(",");
			}

			sb.append("\n");
			sb2.append("\n");
			sbNumOfRts
					.append(numOfRTsWithReOrderingPossibleForThisUser + "," + totalNumOfRTsForThisUser + ","
							+ StatsUtils.round(
									(numOfRTsWithReOrderingPossibleForThisUser * 100.0) / totalNumOfRTsForThisUser, 2)
							+ "\n");
			totalNumOfRtsAffected += numOfRTsWithReOrderingPossibleForThisUser;
		}

		double percentageOfRTsAffected = StatsUtils.round((totalNumOfRtsAffected * 100.0) / totalNumOfRts, 2);
		sbNumOfRts.append("\n\n" + totalNumOfRtsAffected + "," + totalNumOfRts + "," + percentageOfRTsAffected + "\n");

		WToFile.writeToNewFile(sb.toString() + "\n\n\n,,,,,,,,,,,,,,,Read file=" + absFileNameToRead,
				absFileToWrite + ".csv");
		WToFile.writeToNewFile(sb2.toString() + "\n\n\n,,,,,,,,,,,,,,,Read file=" + absFileNameToRead,
				absFileToWrite + "SequenceOfSamess.csv");
		WToFile.writeToNewFile(sbNumOfRts.toString() + "\n\n\n,,,,,,,,,,,,,,,Read file=" + absFileNameToRead,
				absFileToWrite + "NumOfRTsAffected.csv");

		System.out.println(
				"In result:" + absFileNameToRead + " \ntotalNumOfNon1ReOrderings = " + totalNumOfNon1ReOrderings);

		return new Pair<>(totalNumOfNon1ReOrderings, percentageOfRTsAffected);

	}

	public static void checkSeqOfSameScores()
	{
		LinkedHashMap<String, Double> data = new LinkedHashMap<>();

		// - A:2_B:1_C:1_D:1_E:0 _F:0
		// - A:2_B:1_D:1_C:1_E:0 _F:0
		// - A:2_D:1_B:1_C:1_E:0 _F:0
		// - A:2_D:1_C:1_B:1_E:0 _F:0
		// - A:2_C:1_D:1_B:1_E:0 _F:0
		// - A:2_C:1_B:1_D:1_E:0 _F:0

		data.put("A", 2d);
		data.put("B", 1d);
		data.put("C", 1d);
		data.put("D", 1d);
		data.put("E", 0d);
		data.put("G", 0d);
		data.put("H", 0d);
		System.out.println("data = " + data);
		List<Integer> r = seqOfSameScores2(data);
		System.out.println("seqOfSameScores2 = " + r);
		System.out.println(numOfPossibleOrderings(r));
	}

	// start of Nov 28 2018
	// public static List<Integer> seqOfSameScores(LinkedHashMap<String, Double> stringScorePairs)
	// {
	// List<Integer> seqOfSameScores = new ArrayList<>();
	// double prevScore = -9999;
	// int countOfSameInSeq = 1;
	//
	// for (Map.Entry<String, Double> entry : stringScorePairs.entrySet())
	// {
	// double roundedRankScore = StatsUtils.round(entry.getValue(), 4);
	//
	// if (Math.abs(roundedRankScore - prevScore) <= 1.0E-10)
	// {// same score as previous
	// countOfSameInSeq += 1;
	// System.out.println("Same as prev");
	// }
	// else
	// {
	// System.out.println("NOT same as prev");
	// seqOfSameScores.add(countOfSameInSeq);
	// countOfSameInSeq = 1;
	// }
	//
	// System.out.println("seqOfSameScores = " + seqOfSameScores + "\n");
	// prevScore = roundedRankScore;
	// }
	//
	// return seqOfSameScores;
	// }

	public static List<Integer> seqOfSameScores2(LinkedHashMap<String, Double> stringScorePairs)
	{
		List<Integer> seqOfSameScores = new ArrayList<>();
		double prevScore = -9999;
		int countOfSameInSeq = 1;
		int index = -1;

		for (Map.Entry<String, Double> entry : stringScorePairs.entrySet())
		{
			index += 1;
			double roundedRankScore = StatsUtils.round(entry.getValue(), 4);
			// System.out.println("index = " + index);
			if (index == 0)
			{
				prevScore = roundedRankScore;
				continue;
			}
			else
			{
				if (Math.abs(roundedRankScore - prevScore) <= 1.0E-10)
				{// same score as previous
					countOfSameInSeq += 1;
					// System.out.println("Same as prev");
				}
				else
				{
					// System.out.println("NOT same as prev");
					seqOfSameScores.add(countOfSameInSeq);
					countOfSameInSeq = 1;
				}
				prevScore = roundedRankScore;
				// System.out.println("seqOfSameScores = " + seqOfSameScores + "\n");
			}
		}
		seqOfSameScores.add(countOfSameInSeq);

		return seqOfSameScores;
	}

	public static int numOfPossibleOrderings(List<Integer> vals)
	{
		int res = 1;

		for (int v : vals)
		{
			if (v > 1)
			{
				res = res * StatsUtils.factorial(v);
			}
		}

		return res;
	}

	// end of Nov 28 2018

	private static void checkBinnning()
	{

		double[] intArr = new double[] { 5, 10, 15, 20, 50, 100, 55, 70, 98 };// , 101, -1 };
		Double[] doubleArray = ArrayUtils.toObject(intArr);
		List<Double> doubleList = Arrays.asList(doubleArray);
		System.out.println("Vals  = " + Arrays.asList(doubleList));

		// Triple{valToBinIndex, binIndexBoundary, binIndexListOfVals}

		// public static Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer,
		// List<Double>>>
		Pair<Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>>, Double> res = binValuesByNumOfBins(
				doubleList, 10, true);
		Pair<Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>>, Integer> res2 = binValuesByBinSize(
				doubleList, 10, true);

		// 5, 15,20, 50, 100, 55, 70,101,-1 . max = 100, min = 0, binSize =10
		// maxVal is maxValExcluding
		// bins: [0,10),[10,20), ....... ,[90,100)
		// num of bins:= 10 = 0, 1,......9
		// num of bins = (max-min)/binSize
		// floor((5-min)/binsize) = 5/10 = 0.5 --> 0 = bin 0
		// floor((0-min)/binsize) = 0/10 = 0 --> 0 = bin 0
		// floor((10-min)/binsize) = 10/10 = 1 --> 1 = bin 1
		// floor((9.99-min)/binsize) = 0.99/10 = 0.99 --> 0 = bin 0
		// floor(-1-0)/binsize = -1/10 = -0.1--> -1 error
		// floor(100-0)/binsize = 100/10 = 10--> 10 --> bin 10 // rror
		// floor(101-0)/binsize = 101/10 = 10.1--> 10 --> bin 10 // error

	}

	/**
	 * 
	 * @param vals
	 * @param percentile
	 *            range: 0 -100
	 * @return
	 */
	public static double getPercentile(List<Double> valsReceived, double percentile)
	{
		double vals[] = new double[valsReceived.size()];
		for (int i = 0; i < valsReceived.size(); i++)
		{
			vals[i] = valsReceived.get(i);
		}
		DescriptiveStatistics ds = new DescriptiveStatistics(vals);
		return ds.getPercentile(percentile);
	}

	/**
	 * 
	 * @param vals
	 * @param percentile
	 *            range: 0 -100
	 * @return
	 */
	public static double getPercentileInt(List<Integer> valsReceived, double percentile)
	{
		double vals[] = new double[valsReceived.size()];
		for (int i = 0; i < valsReceived.size(); i++)
		{
			vals[i] = valsReceived.get(i);
		}
		DescriptiveStatistics ds = new DescriptiveStatistics(vals);
		return ds.getPercentile(percentile);
	}

	/**
	 * 
	 * @param vals
	 * @param percentile
	 *            range: 0 -100
	 * @return
	 */
	public static double getPercentileL(List<Long> valsReceived, double percentile)
	{
		double vals[] = new double[valsReceived.size()];
		for (int i = 0; i < valsReceived.size(); i++)
		{
			vals[i] = valsReceived.get(i);
		}
		DescriptiveStatistics ds = new DescriptiveStatistics(vals);
		return ds.getPercentile(percentile);
	}

	public static <T> T mostCommon(List<T> list)
	{
		Map<T, Integer> map = new HashMap<>();

		for (T t : list)
		{
			Integer val = map.get(t);
			map.put(t, val == null ? 1 : val + 1);
		}

		Entry<T, Integer> max = null;

		for (Entry<T, Integer> e : map.entrySet())
		{
			if (max == null || e.getValue() > max.getValue()) max = e;
		}

		return max.getKey();
	}

	/**
	 * 
	 * @param vals
	 * @param percentile
	 *            range: 0 -100
	 * @return
	 */
	public static double getPercentileS(List<String> valsReceived, double percentile)
	{
		double vals[] = new double[valsReceived.size()];
		for (int i = 0; i < valsReceived.size(); i++)
		{
			vals[i] = Double.parseDouble(valsReceived.get(i));
		}
		DescriptiveStatistics ds = new DescriptiveStatistics(vals);
		return ds.getPercentile(percentile);
	}

	/**
	 * 
	 * Is Slower
	 * 
	 * @param vals
	 * @param percentile
	 *            range: 0 -100
	 * @return
	 */
	public static double getPercentileSlower(List<Double> valsReceived, double percentile)
	{
		double vals[] = new double[valsReceived.size()];
		for (int i = 0; i < valsReceived.size(); i++)
		{
			vals[i] = valsReceived.get(i);
		}
		Percentile ds = new Percentile(percentile);
		return ds.evaluate(vals);
	}

	public static double getSD(double[] vals)
	{
		DescriptiveStatistics ds = new DescriptiveStatistics(vals);
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
	public static double getPearsonCorrelation2(ArrayList<Double> arr1, ArrayList<Double> arr2)
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

		WToFile.writeSimpleMapToFile(fd, Descriptive.commonPath + "TimeDifference_Frequency distribution.csv", "Value",
				"Frequency Count");

		return (LinkedHashMap<String, Long>) fd;

	}

	/**
	 * 
	 * @param values
	 * @param nameForValue
	 * @param fileNameToWrite
	 * @param writeStatsToFile
	 * @return
	 */
	public static DescriptiveStatistics getDescriptiveStatistics(double[] values, String nameForValue,
			String fileNameToWrite, boolean writeStatsToFile)
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

		if (writeStatsToFile)
		{
			WToFile.writeToNewFile(message.toString(), Constant.getCommonPath() + "Stats_" + fileNameToWrite);
		} // TODO check if this works corrcetly System.out.println(m1);

		return dstats;
	}

	/**
	 * 
	 * @param valsReceived
	 * @param nameForValue
	 * @param fileNameToWrite
	 * @param writeToFile
	 * @return
	 */
	public static DescriptiveStatistics getDescriptiveStatisticsDouble(ArrayList<Double> valsReceived,
			String nameForValue, String fileNameToWrite, boolean writeToFile)
	{

		double values[] = new double[valsReceived.size()];
		for (int i = 0; i < valsReceived.size(); i++)
		{
			values[i] = valsReceived.get(i);
		}

		DescriptiveStatistics dstats = new DescriptiveStatistics(values);
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumIntegerDigits(30);

		StringBuilder message = new StringBuilder("-------------------------------\nDescriptive stats for "
				+ nameForValue + "\n-------------------------------\n");

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

		if (writeToFile)
		{
			WToFile.writeToNewFile(message.toString(), Constant.getCommonPath() + "Stats_" + fileNameToWrite);
		} // TODO check if this works corrcetly System.out.println(m1);

		return dstats;
	}

	/**
	 * 
	 * @param valsReceived
	 * @param nameForValue
	 * @param fileNameToWrite
	 * @return
	 */
	public static DescriptiveStatistics getDescriptiveStatisticsLong(ArrayList<Long> valsReceived, String nameForValue,
			String fileNameToWrite, boolean writeStatsToFile)
	{

		double values[] = new double[valsReceived.size()];
		for (int i = 0; i < valsReceived.size(); i++)
		{
			values[i] = valsReceived.get(i);
		}

		DescriptiveStatistics dstats = new DescriptiveStatistics(values);

		if (dstats.getN() != values.length)
		{
			PopUps.printTracedErrorMsg("Error in getDescriptiveStatistics:dstats.getN() != values.length ");
		}

		if (writeStatsToFile)
		{
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumIntegerDigits(30);

			StringBuilder message = new StringBuilder("-------------------------------\nDescriptive stats for "
					+ nameForValue + "\n-------------------------------\n");
			String m1 = "Count = " + nf.format(dstats.getN()) + "\n" + "Maximum = " + nf.format(dstats.getMax()) + "\n"
					+ "Minimum = " + nf.format(dstats.getMin()) + "\n" + "Arithmetic mean = "
					+ nf.format(dstats.getMean()) + "\n" + "Standard deviation = "
					+ nf.format(dstats.getStandardDeviation()) + "\n" + "Q1 = " + nf.format(dstats.getPercentile(25))
					+ "\n" + "Q2(median) = " + nf.format(dstats.getPercentile(50)) + "\n" + "Q3 = "
					+ nf.format(dstats.getPercentile(75)) + "\n" + "Skewness = " + nf.format(dstats.getSkewness())
					+ "\n" + "Kurtosis = " + nf.format(dstats.getKurtosis()) + "\n" + "Sum = "
					+ nf.format(dstats.getSum()) + "\n" + "-------------------------------\n";
			message.append(m1);

			WToFile.writeToNewFile(message.toString(), Constant.getCommonPath() + "Stats_" + fileNameToWrite);
		} // TODO check if this works corrcetly System.out.println(m1);

		return dstats;
	}

	public static double meanOfArrayList(ArrayList<Double> arr, int roundOffToPlaces)
	{
		if (arr.size() == 0) return -9999;// changed to -9999 on 17 Jan 2019

		double[] vals = new double[arr.size()];

		for (int i = 0; i < arr.size(); i++)
		{
			vals[i] = arr.get(i); // java 1.5+ style (outboxing)
		}
		// System.out.println("mean called");
		return round(StatUtils.mean(vals), roundOffToPlaces);

	}

	public static double meanOfArrayListInt(ArrayList<Integer> arr, int roundOffToPlaces)
	{
		if (arr.size() == 0) return -9999;// changed to -9999 on 17 Jan 2019

		double[] vals = new double[arr.size()];

		for (int i = 0; i < arr.size(); i++)
		{
			vals[i] = arr.get(i); // java 1.5+ style (outboxing)
		}

		return round(StatUtils.mean(vals), roundOffToPlaces);
	}

	/**
	 * 
	 * @param arr
	 * @param roundOffToPlaces
	 * @return
	 */
	public static double medianOfArrayListInt(ArrayList<Integer> arr, int roundOffToPlaces)
	{
		if (arr.size() == 0) return -9999;// changed to -9999 on 17 Jan 2019

		double[] vals = new double[arr.size()];

		for (int i = 0; i < arr.size(); i++)
		{
			vals[i] = arr.get(i); // java 1.5+ style (outboxing)
		}

		return round(StatUtils.percentile(vals, 50), roundOffToPlaces);
	}

	/**
	 * 
	 * @param arr
	 * @param roundOffToPlaces
	 * @return
	 */
	public static double medianOfArrayList(ArrayList<Double> arr, int roundOffToPlaces)
	{
		if (arr.size() == 0) return -9999;// changed to -9999 on 17 Jan 2019

		double[] vals = new double[arr.size()];

		for (int i = 0; i < arr.size(); i++)
		{
			vals[i] = arr.get(i); // java 1.5+ style (outboxing)
		}

		// System.out.println("median called");
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
		// long t1 = System.nanoTime();
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

		//// Start of added on 11 Feb 2018
		// WritingToFile.appendLineToFileAbsolute(value + "," + bd.doubleValue() + "," + (System.nanoTime() - t1) +
		// "\n",
		// Constant.getOutputCoreResultsPath() + "RoundedValues.csv");
		// WritingToFile.appendLineToFileAbsolute(PopUps.getCurrentStackTracedWarningMsg(""),
		// Constant.getOutputCoreResultsPath() + "RoundedCalledBy.csv");
		/// end of added on 11 Fenb 2018

		return bd.doubleValue();
	}

	/**
	 * <font color = blue> Note: using the preferred way to convert double to BigDecimal</font>
	 * 
	 * @param value
	 * @param places
	 * @return
	 */
	public static String roundAsString(double value, int places)
	{

		// long t1 = System.nanoTime();
		if (Double.isInfinite(value))
		{
			return "99999";
		}
		if (Double.isNaN(value))
		{
			return "0";
		}

		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = BigDecimal.valueOf(value);// new BigDecimal(value); //change on 22 Nov 2016
		bd = bd.setScale(places, RoundingMode.HALF_UP);

		//// Start of added on 11 Feb 2018
		// WritingToFile.appendLineToFileAbsolute(value + "," + bd.doubleValue() + "," + (System.nanoTime() - t1) +
		// "\n",
		// Constant.getOutputCoreResultsPath() + "RoundedValues.csv");
		// WritingToFile.appendLineToFileAbsolute(PopUps.getCurrentStackTracedWarningMsg(""),
		// Constant.getOutputCoreResultsPath() + "RoundedCalledBy.csv");
		/// end of added on 11 Fenb 2018

		return bd.toPlainString();
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

	public static double randomInRange(double min, double max)
	{
		return (min + (Math.random() * ((max - min) + 1)));
	}

	/**
	 * Return a random in within the given range
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public static long randomInRange(long min, long max)
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
	 * @deprecated on 18 July 2018
	 */
	public static double minMaxNormV0(double val, double max, double min)
	{
		// if ((max - min) > 0.0000000000000000)
		if ((max - min) > 0.0000000000000000000000000001)
		{
			return round(((val - min) / (max - min)), 4);
		}
		else if ((min - max) > 0.0000000000000000000000000001)
		{
			PopUps.printTracedErrorMsgWithExit(("Error: Warning: Alert!! val=" + val + ", minMaxNorm: max(" + max
					+ ")- min(" + min + ") <=0 =" + (max - min)));
			return 0;
		}

		else
		{
			if (Math.abs(max - min) <= 1e-10)
			{
				System.err.println(("Warning: Alert!! val=" + val + ", minMaxNorm: max(" + max + ")- min(" + min
						+ ") <=0 =" + (max - min)));
			}
			// Warning: Alert!! val0.25 = minMaxNorm: max(0.25)- min(0.25) <=0 =0.0
			// val = 0.25, max = 0.25, min =0.25, max-min = 0;
			return 0;
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
		// if ((max - min) > 0.0000000000000000)
		double maxMinusMin = max - min;
		double effectiveZero = 1.0E-50;

		if (maxMinusMin > effectiveZero)// changed from 0.0000000000000000000000000001 to 1.0E-50 on May 8 2018
		{// max > min wrt effective zero.
			return round(((val - min) / maxMinusMin), 4);
		}
		// else if ((min - max) > 1.0E-50)/ // else if (min > max)
		else if ((min - max) > effectiveZero)
		{// min > max wrt to effective zero
			PopUps.printTracedErrorMsgWithExit(("Error: Warning: Alert!! min > max wrt to effective zero val=" + val
					+ ", minMaxNorm: max(" + max + ")- min(" + min + ") <=0 =" + maxMinusMin));
			return 0;
		}

		else // min == max wrt to effective zero, i.e., (Math.abs(max - min) <= 1.0e-50)
		{ // if (Math.abs(max - min) <= 1.0e-10)// changed from > to <= on May 8 2018
			System.err.println(("Warning: Alert!! min == max wrt to effective zero val=" + val + ", minMaxNorm: max("
					+ max + ")- min(" + min + ") <=0 =" + maxMinusMin));
			// Warning: Alert!! val0.25 = minMaxNorm: max(0.25)- min(0.25) <=0 =0.0
			// val = 0.25, max = 0.25, min =0.25, max-min = 0;
			return 0;
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
	 * @deprecated on 18 July 2018
	 */
	public static double minMaxNormWORoundV0(double val, double max, double min)
	{
		// if ((max - min) > 0.0000000000000000)
		double maxMinusMin = max - min;
		if (maxMinusMin > 1.0E-50)// changed from 0.0000000000000000000000000001 to 1.0E-50 on May 8 2018
		{
			return ((val - min) / maxMinusMin);
		}
		// else if ((min - max) > 1.0E-50)/ // else if (min > max)
		else if ((min - max) > 1.0E-50)
		{
			PopUps.printTracedErrorMsgWithExit(("Error: Warning: Alert!! val=" + val + ", minMaxNorm: max(" + max
					+ ")- min(" + min + ") <=0 =" + maxMinusMin));
			return 0;
		}

		else // min == max, i.e., (Math.abs(max - min) <= 1.0e-50)
		{ // if (Math.abs(max - min) <= 1.0e-10)// changed from > to <= on May 8 2018
			System.err.println(("Warning: Alert!! val=" + val + ", minMaxNorm: max(" + max + ")- min(" + min + ") <=0 ="
					+ maxMinusMin));
			// Warning: Alert!! val0.25 = minMaxNorm: max(0.25)- min(0.25) <=0 =0.0
			// val = 0.25, max = 0.25, min =0.25, max-min = 0;
			return 0;
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
	public static double minMaxNormWORound(double val, double max, double min)
	{
		// if ((max - min) > 0.0000000000000000)
		double maxMinusMin = max - min;
		double effectiveZero = 1.0E-50;

		if (maxMinusMin > effectiveZero)// changed from 0.0000000000000000000000000001 to 1.0E-50 on May 8 2018
		{// max > min wrt effective zero.
			return ((val - min) / maxMinusMin);
		}
		// else if ((min - max) > 1.0E-50)/ // else if (min > max)
		else if ((min - max) > effectiveZero)
		{// min > max wrt to effective zero
			PopUps.printTracedErrorMsgWithExit(("Error: Warning: Alert!! min > max wrt to effective zero val=" + val
					+ ", minMaxNorm: max(" + max + ")- min(" + min + ") <=0 =" + maxMinusMin));
			return 0;
		}

		else // min == max wrt to effective zero, i.e., (Math.abs(max - min) <= 1.0e-50)
		{ // if (Math.abs(max - min) <= 1.0e-10)// changed from > to <= on May 8 2018
			System.err.println(("Warning: Alert!! min == max wrt to effective zero val=" + val + ", minMaxNorm: max("
					+ max + ")- min(" + min + ") <=0 =" + maxMinusMin));
			// Warning: Alert!! val0.25 = minMaxNorm: max(0.25)- min(0.25) <=0 =0.0
			// val = 0.25, max = 0.25, min =0.25, max-min = 0;
			return 0;
		}

	}

	/**
	 * Returns min max norm if max - min >0 else return 0 (as distance) ...leading to 1 as similarity (rounded off to 4
	 * decimal places)
	 * 
	 * @param val
	 * @param max
	 * @param min
	 * @param upperbound
	 * @param withWarningForEqualMinMax
	 * @return
	 */
	public static double minMaxNormWORoundWithUpperBound(double val, double max, double min, double upperbound,
			boolean withWarningForEqualMinMax)
	{
		double maxMinusMin = max - min;

		if (maxMinusMin > 1.0E-50)// changed from 0.0000000000000000000000000001 to 1.0E-50 on May 8 2018
		{
			double res = ((val - min) / maxMinusMin);
			if (res > upperbound)
			{
				return upperbound;
			}
			else
			{
				return res;
			}
		}
		// else if ((min - max) > 1.0E-50)// 0.0000000000000000000000000001)
		else if ((min - max) > 1.0E-50)
		{
			PopUps.printTracedErrorMsgWithExit(("Error: Warning: Alert!! val=" + val + ", minMaxNorm: max(" + max
					+ ")- min(" + min + ") <=0 =" + maxMinusMin));
			return 0;
		}

		else // min == max, i.e., (Math.abs(max - min) <= 1.0e-50)
		{
			if (withWarningForEqualMinMax)
			{ // if (Math.abs(max - min) <= 1.0e-10)// changed from > to <= on May 8 2018
				System.err.println(("Warning: Alert!! val=" + val + ", minMaxNorm: max(" + max + ")- min(" + min
						+ ") <=0 =" + maxMinusMin));
			}
			return 0;
		}

	}

	/**
	 * 
	 * @param fileToRead
	 * @param numOfColumns
	 * @param roundToPlaces
	 * @param stat
	 *            Mean, Media
	 * @return
	 */
	public static ArrayList<Double> getColumnSummaryStatDouble(String fileToRead, int numOfColumns, int roundToPlaces,
			SummaryStat stat)
	{
		int[] columnIndicesToRead = IntStream.range(0, numOfColumns).toArray();
		ArrayList<ArrayList<Double>> columnWiseVals = ReadingFromFile.allColumnsReaderDouble(fileToRead, ",",
				columnIndicesToRead, false);

		ArrayList<Double> columnWiseSummary = new ArrayList<>();

		switch (stat)
		{
		case Mean:
			for (ArrayList<Double> valsForAColumn : columnWiseVals)
			{
				columnWiseSummary.add(meanOfArrayList(valsForAColumn, roundToPlaces));
			}
			break;
		case Median:
			for (ArrayList<Double> valsForAColumn : columnWiseVals)
			{
				columnWiseSummary.add(medianOfArrayList(valsForAColumn, roundToPlaces));
			}
			break;
		default:
			System.err.println(
					PopUps.getTracedErrorMsg("Unknown stat: " + stat.toString() + " reading file: " + fileToRead));
			System.exit(-1);
		}

		return columnWiseSummary;
	}
}
