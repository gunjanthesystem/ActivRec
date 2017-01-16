package org.activity.stats.entropy;

import java.util.Vector;

public class SampleEntropyIQM
{

	private int numbDataPoints = 0; // length of 1D data series

	public SampleEntropyIQM()
	{
	}

	/**
	 * This method calculates the mean of a data series.
	 * 
	 * @param data1D
	 * @return the mean
	 */
	private Double calcMean(Vector<Double> data1D)
	{
		double sum = 0;
		for (double d : data1D)
		{
			sum += d;
		}
		return sum / data1D.size();
	}

	/**
	 * This method calculates the variance of a data series.
	 * 
	 * @param data1D
	 * @return the variance
	 */
	private double calcVariance(Vector<Double> data1D)
	{
		double mean = calcMean(data1D);
		double sum = 0;
		for (double d : data1D)
		{
			sum = sum + ((d - mean) * (d - mean));
		}
		return sum / (data1D.size() - 1); // 1/(n-1) is used by histo.getStandardDeviation() too
	}

	/**
	 * This method calculates the standard deviation of a data series.
	 * 
	 * @param data1D
	 * @return the standard deviation
	 */
	private double calcStandardDeviation(Vector<Double> data1D)
	{
		double variance = this.calcVariance(data1D);
		return Math.sqrt(variance);
	}

	/**
	 * This method calculates new data series
	 * 
	 * @param data1D
	 *            1D data vector
	 * @param m
	 *            number of newly calculated time series (m = 2, Pincus et al.1994)
	 * @param d
	 *            delay
	 * @return Vector of Series (vectors)
	 * 
	 */
	private Vector<Vector<Double>> calcNewSeries(Vector<Double> data1D, int m, int d)
	{
		int numSeries = numbDataPoints - (m - 1) * d;
		Vector<Vector<Double>> newDataSeries = new Vector<Vector<Double>>(numSeries);
		for (int i = 0; i < numSeries; i++)
		{
			Vector<Double> vec = new Vector<Double>();
			for (int ii = i; ii <= i + (m - 1) * d; ii = ii + d)
			{ // get m data points stepwidth = delay
				vec.add(data1D.get(ii));
			}
			newDataSeries.add(vec);
		}
		return newDataSeries; // size of newDataSeries = N_Series
	}

	/**
	 * This method calculates the number of correlations
	 * 
	 * @param newDataSeries
	 *            vector of 1D vectors
	 * @param m
	 * @param distR
	 *            distance in %of SD
	 * @return Vector (Number of Correlations)
	 * 
	 */
	private Vector<Integer> calcNumberOfCorrelations(Vector<Vector<Double>> newDataSeries, int m, double distR)
	{

		int numSeries = newDataSeries.size();
		Vector<Integer> numberOfCorrelations = new Vector<Integer>(numSeries);
		for (int i = 0; i < numSeries; i++)
		{ // initialize Vector
			numberOfCorrelations.add(0);
		}
		for (int i = 0; i < numSeries; i++)
		{
			for (int j = 0; j < numSeries; j++)
			{
				if (i != j)
				{
					Vector<Double> seriesI = newDataSeries.get(i);
					Vector<Double> seriesJ = newDataSeries.get(j);
					double distMax = 0;
					for (int k = 1; k <= m; k++)
					{
						double dist = Math.abs(seriesI.get(k - 1) - seriesJ.get(k - 1));
						if (dist > distMax)
						{
							distMax = dist;
						}
					}
					if (distMax <= distR)
					{
						numberOfCorrelations.set(i, numberOfCorrelations.get(i) + 1);
					}
				}
			}
		}

		// for (int i = 0; i < numSeries; i++){ //initialize Vector
		// System.out.println("numberOfCorrelations.get[i]: " +
		// numberOfCorrelations.get(i));
		// }
		return numberOfCorrelations;
	}

	/**
	 * This method calculates correlations
	 * 
	 * @param numberOfCorrelations
	 *            vector of mumbers
	 * @param m
	 *            number of newly calculated time series (m = 2, Pincus et al.1994)
	 * @param d
	 *            delay
	 * @return Vector correlations
	 * 
	 */
	private Vector<Double> calcCorrelations(Vector<Integer> numberOfCorrelations, int m, double d)
	{

		Vector<Double> correlations = new Vector<Double>();

		for (int n = 0; n < numberOfCorrelations.size(); n++)
		{
			correlations.add((double) numberOfCorrelations.get(n) / (numbDataPoints - (m - 1) * d - 1)); // -1 because
																											// i=j was
																											// not
																											// allowed
		}
		return correlations;
	}

	/**
	 * This method calculates the sum of correlations
	 * 
	 * @param correlations
	 *            vector of correlations
	 * @param m
	 *            number of newly calculated time series (m = 2, Pincus et al.1994)
	 * @param d
	 *            delay
	 * @return sumOfCorrelations double
	 * 
	 */
	private double calcSumOfCorrelation(Vector<Double> correlations, int m, int d)
	{
		double sumOfCorrelations = 0;

		for (int n = 0; n < correlations.size(); n++)
		{
			sumOfCorrelations = sumOfCorrelations + correlations.get(n);
		}
		sumOfCorrelations = sumOfCorrelations / (numbDataPoints - (m - 1) * d);
		return sumOfCorrelations;
	}

	/**
	 * This method calculates the sample entropy
	 * 
	 * @param data1D
	 *            1D data vector
	 * @param m
	 *            number of new calculated time series (m = 2, Pincus et al.1994) m should not be greater than N/3 (N
	 *            number of data points)!
	 * @param r
	 *            maximal distance radius r (10%sd < r < 25%sd sd = standard deviation of time series, Pincus et al.
	 *            1994)
	 * @param d
	 *            delay
	 * @return Sample Entropy (single double value)
	 * 
	 */
	public double calcSampleEntropy(Vector<Double> data1D, int m, double r, int d)
	{
		// System.out.println("inside calcSampleEntropy: m = " + m);
		numbDataPoints = data1D.size();
		// System.out.println("inumbDataPoints = " + numbDataPoints);
		// System.out.println("inumbDataPoints/3 = " + numbDataPoints / 3);

		if (m > (numbDataPoints / 3))
		{
			m = numbDataPoints / 3;
			System.err.println("parameter m too large, automatically set to data length/3");
		}
		if (m < 1)
		{
			System.err.println("parameter m too small, Sample entropy cannot be calulated");
			return 99999999d;
		}
		if (d < 0)
		{
			System.err.println("delay too small, Sample entropy cannot be calulated");
			return 999999999d;
		}

		double sampleEntropy = 999999999999d;
		double[] fmr = new double[2];

		double distR = this.calcStandardDeviation(data1D) * r;

		for (int mm = m; mm <= m + 1; mm++)
		{ // two times
			Vector<Vector<Double>> newDataSeries = this.calcNewSeries(data1D, mm, d);
			Vector<Integer> numberOfCorrelations = this.calcNumberOfCorrelations(newDataSeries, mm, distR);
			Vector<Double> correlations = this.calcCorrelations(numberOfCorrelations, mm, d);
			double sumOfCorrelations = this.calcSumOfCorrelation(correlations, mm, d);
			fmr[mm - m] = sumOfCorrelations;
		}

		// System.out.println("grrre");
		// System.out.println("fmr[0] = " + fmr[0] + " fmr[1] = " + fmr[1] + " fmr[0]/fmr[1] = " + fmr[0] / fmr[1]);
		sampleEntropy = (Math.log(fmr[0] / fmr[1]));// d; //Gaussian noise can lead to log(0/x)=infinity or even
													// log(0/0)=NaN for larger m
		return sampleEntropy;
	}
}
