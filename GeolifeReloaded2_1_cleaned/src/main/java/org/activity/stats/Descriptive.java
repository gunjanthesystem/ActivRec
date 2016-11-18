package org.activity.stats;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.activity.io.ReadingFromFile;
import org.activity.io.WritingToFile;
import org.activity.util.Constant;
import org.activity.util.UtilityBelt;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class Descriptive
{
	public final static String commonPath = "/run/media/gunjan/HOME/gunjan/Geolife Data Works/";
	final static String outputPath = commonPath + "results/";
	
	public static void main(String[] args)
	{
		Constant.setCommonPath(commonPath);
		// List<Long> raw= ReadingFromFile.oneColumnReader("TimeDifferenceAllBack.csv",",",1,true);
		// double[] rawSortedArray = raw.stream().sorted().mapToDouble(l -> l.doubleValue()).toArray();
		//
		//
		// //getDescriptiveStatistics(rawSortedArray,"Time difference between Data Points (in seconds)","TimeDiffBetweenDataPoints.csv");
		// getFrequencyDistribution(rawSortedArray);
		
		//
		// List<Long> raw= ReadingFromFile.oneColumnReader("UnknownMergedContinuousSandwiches.csv",",",3,true);
		// double[] rawSortedArray = raw.stream().sorted().mapToDouble(l -> l.doubleValue()).toArray();
		// getDescriptiveStatistics(rawSortedArray,"Duration of (sandwiched) Unknowns in secs","UnknownsSandwichedDurations.txt");
		//
		
		// List<Long> raw= ReadingFromFile.oneColumnReader("UnknownMergedContinuous.csv",",",3,true);
		// double[] rawSortedArray = raw.stream().sorted().mapToDouble(l -> l.doubleValue()).toArray();
		// getDescriptiveStatistics(rawSortedArray,"Duration of (all) Unknowns in secs","UnknownsDurations.txt");
		
		// List<Long> raw= ReadingFromFile.oneColumnReader("Not_AvailableMergedContinuous.csv",",",3,true);
		// double[] rawSortedArray = raw.stream().sorted().mapToDouble(l -> l.doubleValue()).toArray();
		// getDescriptiveStatistics(rawSortedArray,"Duration of (all) NotAvailable in secs","NotAvailableDurations.txt");
		
		// List<Long> raw= ReadingFromFile.oneColumnReader("Not_AvailableMergedContinuousSandwiches.csv",",",3,true);
		// double[] rawSortedArray = raw.stream().sorted().mapToDouble(l -> l.doubleValue()).toArray();
		// getDescriptiveStatistics(rawSortedArray,"Duration of (sandwiched) NotAvalaible in secs","NotAvailableSandwichedDurations.csv");
		
		String[] userIDs = { "062", "084", "052", "068", "167", "179", "153", "085", "128", "010" };
		
		// ArrayList
		long rawStartAllCount = 0, rawEndAllCount = 0;
		for (String user : userIDs)
		{
			List<Double> raw = ReadingFromFile.oneColumnReaderDouble(Constant.getCommonPath() + user + "startDiff.csv",
					",", 0, false);// start diff contains the pair wise diff betwen the start
									// geolocations between all value
			// for a user.
			double[] rawArray = raw.stream().mapToDouble(l -> l.doubleValue()).toArray();
			DescriptiveStatistics dsStart = getDescriptiveStatistics(rawArray, "Difference of StartGeo location in km",
					user + "Stats_StartDiff.txt");
			WritingToFile.appendLineToFile(String.valueOf(dsStart.getPercentile(75)),
					"ThirdQuartileStartEndGeoDiff.csv");
			rawStartAllCount += raw.size();
			
			List<Double> raw2 = ReadingFromFile.oneColumnReaderDouble(Constant.getCommonPath() + user + "endDiff.csv",
					",", 0, false);
			double[] rawArray2 = raw2.stream().mapToDouble(l -> l.doubleValue()).toArray();
			DescriptiveStatistics dsEnd = getDescriptiveStatistics(rawArray2, "Difference of EndGeo location in km",
					user + "Stats_EndDiffStats.txt");
			WritingToFile.appendLineToFile(String.valueOf(dsEnd.getPercentile(75)), "ThirdQuartileStartEndGeoDiff.csv");
			
			rawEndAllCount += raw2.size();
		}
		
		System.out.println("rawStartAllCount = " + rawStartAllCount + ", rawEndAllCount = " + rawEndAllCount);
		
		// List<Long> raw= ReadingFromFile.oneColumnReader("ValidsOnlyMergedContinuous.csv",",",3,true);
		// double[] rawSortedArray = raw.stream().sorted().mapToDouble(l -> l.doubleValue()).toArray();
		// getDescriptiveStatistics(rawSortedArray,"Duration of Valids in secs","ValidsOnlyDurations.csv");
		//
		
		// getFrequencyDistribution(rawSortedArray);
	}
	
	public static void fun()
	{
		
	}
	
	// /**
	// *
	// *
	// * @param values
	// * @param numOfBins
	// * @return
	// */
	// // lowEdge,highEdge,countForEdge
	// public static ArrayList<Triple<Double, Double, Integer>> getHistogramData(double[] values, int numOfBins)
	// {
	// ArrayList<Triple<Double, Double, Integer>> fd = new ArrayList<Triple<Double, Double, Integer>>();
	//
	// double lowestEdge = 0;
	// double highestEdge = (Arrays.stream(values).max()).orElse(0);
	//
	// highestEdge = UtilityBelt.ceilNearestMultipleOf(highestEdge, 5); // since a bin of histogram is excludes values
	// on outer edge
	//
	// if (lowestEdge == highestEdge)
	// {
	// System.err.println("Warning: in getHistogramData");
	// }
	//
	// else
	// {
	// for (double i = lowestEdge; i <= highestEdge; i++)
	// {
	//
	// }
	//
	// }
	// return fd;
	//
	// }
	
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
		
		fd = UtilityBelt.sortByValue(fd);
		
		WritingToFile.writeSimpleMapToFile(fd, commonPath + "TimeDifference_Frequency distribution.csv", "Value",
				"Frequency Count");
		
		return (LinkedHashMap<String, Long>) fd;
		
	}
	
	public static DescriptiveStatistics getDescriptiveStatistics(double[] values, String nameForValue,
			String fileNameToWrite)
	{
		
		DescriptiveStatistics dstats = new DescriptiveStatistics(values);
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumIntegerDigits(30);
		
		StringBuffer message = new StringBuffer("-------------------------------\nDescriptive stats for "
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
		WritingToFile.writeToNewFile(message.toString(), Constant.getCommonPath() + "Stats_" + fileNameToWrite); // TODO
																													// check
																													// if
																													// this
																													// works
																													// corrcetly
		// System.out.println(m1);
		
		return dstats;
	}
	
}
