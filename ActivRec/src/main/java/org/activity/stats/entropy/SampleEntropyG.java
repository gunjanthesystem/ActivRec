package org.activity.stats.entropy;

import java.util.List;

import org.activity.distances.DistanceUtils;
import org.activity.io.ReadingFromFile;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class SampleEntropyG
{
	int m;

	/**
	 * 
	 * @param input
	 * @param m
	 * @param tolerance
	 * @param infinityReplacement
	 *            if sample entropy is infinity, replace with this value.
	 * @param naNReplacement
	 * @return
	 */
	public static double getSampleEntropyGReplaceInfinityNaN(double[] input, int m, double tolerance,
			double infinityReplacement, double naNReplacement)
	{
		Double res = getSampleEntropyG(input, m, tolerance);
		if (res.isInfinite())
		{
			return (infinityReplacement);
		}
		else if (res.isNaN())
		{
			return (naNReplacement);
		}
		else
		{
			return res;
		}
	}

	/**
	 * Correct Based on Costa's definition which is equivalent to Richman's definition
	 * 
	 * @param input
	 * @param m
	 *            epoch length or segment length
	 * @param tolerance
	 * @return Sample Entropy
	 */
	public static Double getSampleEntropyG(double[] input, int m, double tolerance)
	{
		// System.out.println("Inside getSampleEntropy ");
		Double sampEn = null;/*-9999,*/
		double numOfCloseMPlusOneLenVectors = 0, numOfCloseMLenVectors = 0;
		int N = input.length;

		// System.out.println("\n N = " + N);
		// System.out.println(" m = " + m);
		// System.out.println(" N-m+1 =" + (N - m + 1));

		double[][] mLengthVectors = getSegmentVectors(input, m);
		// printSegmentVectors(mLengthVectors);

		double[][] mPlusOneLengthVectors = getSegmentVectors(input, m + 1);
		// printSegmentVectors(mPlusOneLengthVectors);

		for (int i = 0; i < N - m; i++)
		{
			numOfCloseMLenVectors += getNumOfCloseVectors(i, mLengthVectors, N - m, tolerance);// B
			numOfCloseMPlusOneLenVectors += getNumOfCloseVectors(i, mPlusOneLengthVectors, N - m, tolerance);// A
		}

		// System.out.println(
		// "\n\t\t numOfCloseMPlusOneLenVectors = " + numOfCloseMPlusOneLenVectors + " numOfCloseMLenVectors = "
		// + numOfCloseMLenVectors + " numOfCloseMPlusOneLenVectors/numOfCloseMLenVectors = "
		// + numOfCloseMPlusOneLenVectors / numOfCloseMLenVectors);
		sampEn = -Math.log(numOfCloseMPlusOneLenVectors / numOfCloseMLenVectors);// A/B

		// System.out.println("Sample entropy Gunjan = " + sampEn);

		if (sampEn == -0)
		{
			sampEn *= -1;
		}
		return sampEn;
	}

	public static double getSD(double[] vals)
	{
		DescriptiveStatistics ds = new DescriptiveStatistics(vals);
		double sd = ds.getStandardDeviation();
		return ds.getStandardDeviation();
	}

	/**
	 * 
	 * @param filePath
	 * @return
	 */
	public static double[] getTimeSeriesVals(String filePath)
	{
		List<Double> vals = ReadingFromFile.oneColumnReaderDouble(filePath, ",", 1, false);
		Double[] vals2 = vals.toArray(new Double[vals.size()]);
		double[] vals3 = ArrayUtils.toPrimitive(vals2);
		return vals3;
	}

	/**
	 * Tested OK
	 * 
	 * @param indexOfReferenceVector
	 * @param segmentVectors
	 * @param tolerance
	 * @return
	 */
	public static int getNumOfCloseVectors(int indexOfReferenceVector, double[][] segmentVectors, int NMinusM,
			double tolerance)
	{
		StringBuilder sbLog = new StringBuilder("Inside getNumOfCloseVectors");
		int numOfCloseVectors = 0;
		// System.out.println("Inside getNumOfCloseVectors: segmentVectors.length =" + segmentVectors.length);
		int countSelfRef = -1;
		for (int i = 0; i < NMinusM; i++)
		{
			sbLog.append("\ni = " + i);
			if (i == indexOfReferenceVector)
			{
				sbLog.append("\nskipping self reference");
				countSelfRef += 1;
				continue; // no self matches
			}
			else
			{
				if (isClose(segmentVectors[indexOfReferenceVector], segmentVectors[i], tolerance))
				{
					numOfCloseVectors += 1;
					sbLog.append("\nIs close");
				}
			}
		}
		if (countSelfRef > 1)
		{
			System.err.print("Error in getNumOfCloseVectors: more than one self reference");
		}
		// System.out.println("Exiting getNumOfCloseVectors");

		sbLog.append(
				"\nindexOfReferenceVector = " + indexOfReferenceVector + " numOfCloseVectors = " + numOfCloseVectors);

		if (false)
		{
			System.out.println(sbLog.toString() + "\n\n");
		}
		return numOfCloseVectors;
	}

	/**
	 * Tested OK
	 * 
	 * @param vector1
	 * @param vector2
	 * @param tolerance
	 * @return
	 */
	private static boolean isClose(double[] vector1, double[] vector2, double tolerance)
	{
		if (DistanceUtils.getChebyshevDistance(vector1, vector2) <= tolerance)
			return true;
		else
			return false;
	}

	/**
	 * Tested OK
	 * 
	 * @param inputData
	 * @param m
	 * @return
	 */
	public static double[][] getSegmentVectors(double[] inputData, int m)
	{
		int N = inputData.length;
		double[][] mLengthVectors = new double[N - m + 1][m];

		for (int i = 0; i < (N - m + 1); i++)
		{
			int k = 0;

			for (int j = i; j < i + m; j++)
			{
				mLengthVectors[i][k] = inputData[j];
				k++;
			}
		}
		return mLengthVectors;
	}

	/**
	 * Tested OK
	 * 
	 * @param segmentVectors
	 */
	public static void printSegmentVectors(double[][] segmentVectors)
	{
		System.out.println("\nPrinting " + segmentVectors[0].length + " length vectors:");

		int nRows = segmentVectors.length;
		int nCols = segmentVectors[0].length;

		for (int i = 0; i < nRows; i++)
		{
			System.out.print((i /* + 1 */) + ") -->  ");
			for (int j = 0; j < nCols; j++)
			{
				System.out.print(segmentVectors[i][j] + " ,");
			}
			System.out.println("");
		}
	}

	public SampleEntropyG()
	{
		// String timeSeriesFile = "/run/media/gunjan/HOME/gunjan/Geolife Data
		// Works/stats/TimeSeries2/geolife1JUL6TimeSeriesAnalysis_UsersAbove10RTs/testy1.csv";
		//
		// double[] input = getTimeSeriesVals(timeSeriesFile);// getTimeSeriesVals(timeSeriesFile);//
		// getRandomSeries(400);// getNaiveSeries(20);
		// // // //getRandomSeries(400);// getNaiveSeries(397);getPureRegularSeries(400);
		// int N = input.length;
		// System.out.println("Input data: ");
		// m = 2;
		// double tolerance = 0.2 * getSD(input);
		//
		// for (double v : input)
		// {
		// System.out.print(v + " ,");
		// }
		// System.out.println("\n " + N + " values");
		// System.out.println(" m=" + m);
		// System.out.println(" N-m+1 =" + (N - m + 1));
		//
		// for (int i = 0; i < 10; i++)
		// {
		// getSampleEntropy(input, m + i, tolerance);
		// // getAltSampleEntropy(input, m + i, tolerance);
		// }
		// double[][] mLengthVectors = getSegmentVectors(input, m);
		// printSegmentVectors(mLengthVectors);
		//
		// double[][] mPlusOneLengthVectors = getSegmentVectors(input, m + 1);
		// printSegmentVectors(mPlusOneLengthVectors);
		//
		// int numOfCloseVectors = getNumOfCloseVectors(0, mLengthVectors, 2);
		// System.out.println("num of close vectors = " + numOfCloseVectors);
		//
		// int numOfCloseVectors2 = getNumOfCloseVectors(1, mLengthVectors, 2);
		// System.out.println("num of close vectors = " + numOfCloseVectors2);
		//
		// int numOfCloseVectors3 = getNumOfCloseVectors(3, mLengthVectors, 3);
		// System.out.println("num of close vectors = " + numOfCloseVectors3);
		//
		// int numOfCloseVectors4 = getNumOfCloseVectors(4, mLengthVectors, 3);
		// System.out.println("num of close vectors = " + numOfCloseVectors4);
	}

	// /**
	// * Based on Richman's simplified definition WRONG WRONG WRONG
	// *
	// * @param input
	// * @param m
	// * @param tolerance
	// * @return
	// */
	// public static double getAltSampleEntropy(double[] input, int m, double tolerance)
	// {
	// // System.out.println("Inside getSampleEntropy ");
	// double sampEn = -9999;
	//
	// double A = 0; // number of mlength segment pairs which match in tolerance
	// double B = 0; // number of (m+1)length segment pairs which match in tolerance
	// int N = input.length;
	//
	// // System.out.println(" N = " + N);
	// // System.out.println(" m = " + m);
	// // System.out.println(" N-m+1 =" + (N - m + 1));
	//
	// double[][] mLengthVectors = getSegmentVectors(input, m);
	// // printSegmentVectors(mLengthVectors);
	//
	// double[][] mPlusOneLengthVectors = getSegmentVectors(input, m + 1);
	// // printSegmentVectors(mPlusOneLengthVectors);
	//
	// for (int i = 0; i < mLengthVectors.length; i++)
	// {
	// for (int j = i + 1; j < mLengthVectors.length; j++)
	// {
	// if (isClose(mLengthVectors[i], mLengthVectors[j], tolerance))
	// {
	// B += 1;
	// }
	// }
	// }
	//
	// for (int i = 0; i < mPlusOneLengthVectors.length; i++)
	// {
	// for (int j = i + 1; j < mPlusOneLengthVectors.length; j++)
	// {
	// if (isClose(mPlusOneLengthVectors[i], mPlusOneLengthVectors[j], tolerance))
	// {
	// A += 1;
	// }
	// }
	// }
	//
	// System.out.println(" A = " + A + " B = " + B + " A/B = " + A / B);
	// sampEn = -Math.log(A / B);
	//
	// System.out.println("Alt Sample entropy = " + sampEn);
	// return sampEn;
	// }
}
