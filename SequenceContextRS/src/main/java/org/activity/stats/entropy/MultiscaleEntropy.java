package org.activity.stats.entropy;

import java.util.ArrayList;
import java.util.List;

/**
 * Courtesy: https://github.com/tjrantal/javaMSE/blob/master/src/edu/deakin/timo/MultiscaleEntropy.java
 * 
 * Class to calculate multiscale entropy. Ported from M. Costa's C implementation, visit
 * http://physionet.org/physiotools/mse/ for the original implementation, and further details. Ported from C to java by
 * Timo Rantalainen 2014 tjrantal at gmail dot com The C source written by M Costa 2004 copied from
 * http://physionet.org/physiotools/mse/ Licensed with the GPL.
 * 
 * There are two major steps in the calculations performed by mse: 1. Time series are coarse-grained. 2. Sample entropy
 * (SampEn) is calculated for each coarse-grained time series.
 * 
 * After the 2nd line there are several columns: the first column (of integers) is the scale factor. The following
 * columns are SampEn values for coarse-grained time series calculated for the values of r and m specified. If the
 * option for calculating MSE for several r values is chosen a new line containing the new r value and new columns with
 * the corresponding results are written.
 * 
 * @author Timo Rantalainen
 */

public class MultiscaleEntropy
{
	private double[][] mseResults; /* <Array for MSE results, accessed with @link #getMSE() after construction */
	private double[] dataIn; /* <Array to contain the signal in */
	private double r; /* <Relative tolerance */
	private int tau; /* <Number of coarseness levels */
	private int mMax; /* <Maximum m to calculate SEs for */

	/**
	 * Constructor
	 * 
	 * @param dataIn
	 *            Sarray for which the MSE is to be calculated
	 * @param r
	 *            tolerance (maximum distance, will be multiplied with dataIn SD)
	 * @param tau
	 *            the maximum length of the mean for the coarse-grained time series. Series with coarseness from 1 to
	 *            tau will be created
	 * @param mMax
	 *            the maximum m to calculate SE for. SEs from 1 to mMax will be calculated
	 */
	public MultiscaleEntropy(double[] dataIn, double r, int tau, int mMax)
	{
		this.dataIn = dataIn;
		this.r = r;
		this.tau = tau;
		this.mMax = mMax;
		runAnalysis();
	}

	/** A method to run the MSE analysis in simultaneous threads */
	private void runAnalysis()
	{
		double sd = std(dataIn);
		mseResults = new double[tau][mMax];
		List<Thread> threads = new ArrayList<Thread>();
		List<SERunnable> seRunnables = new ArrayList<SERunnable>();
		// The MSE threads for various coarseness levels created here
		for (int i = 1; i <= tau; ++i)
		{
			seRunnables.add(new SERunnable(dataIn, r, mMax, sd, i));
			threads.add(new Thread(seRunnables.get(i - 1)));
			threads.get(i - 1).start();

		}

		// Join (=wait for completion) the MSE threads of various coarseness levels
		for (int i = 0; i < threads.size(); ++i)
		{
			try
			{
				((Thread) threads.get(i)).join();
			}
			catch (Exception er)
			{
			}
			double[] se = seRunnables.get(i).getSE();
			for (int j = 0; j < se.length; ++j)
			{
				mseResults[i][j] = se[j];
			}
		}

	}

	/**
	 * return the MSE results array
	 * 
	 * @return The MSE results array
	 */
	public double[][] getMSE()
	{
		double[][] ret = new double[mseResults.length][mseResults[0].length];
		for (int i = 0; i < mseResults.length; ++i)
		{
			for (int j = 0; j < mseResults[i].length; ++j)
			{
				ret[i][j] = mseResults[i][j];
			}
		}
		return ret;
	}

	/**
	 * Standard deviation This method of calculating sd explained in e.g.
	 * http://www.johndcook.com/blog/2008/09/26/comparing-three-methods-of-computing-standard-deviation/
	 * 
	 * @param arr
	 *            array for which the sd is to be calculated
	 * @return standard deviation
	 */
	private double std(double[] arr)
	{
		double sum = 0, sum2 = 0;
		for (int i = 0; i < arr.length; ++i)
		{
			sum += arr[i];
			sum2 += arr[i] * arr[i];
		}
		return Math.sqrt((sum2 - sum * sum / ((double) arr.length)) / (((double) arr.length) - 1d));
	}

	/** A subclass to enable multi threading different coarseness level SE calculations */
	public class SERunnable implements Runnable
	{
		private double[] dataIn;
		private double r;
		private int tau;
		private int mMax;
		private double[] se;
		private double[] coarseGrain;
		private double sd;

		/**
		 * Constructor
		 * 
		 * @param dataIn
		 *            time series in (coarse-grained or otherwise)
		 * @param r
		 *            tolerance (maximum distance, will be multiplied by se)
		 * @param sd
		 *            standard deviation of the original signal
		 * @param mMax
		 *            the maximum m to calculate SE for
		 * @param tau
		 *            the length of the mean for the coarse-grained time series
		 */
		public SERunnable(double[] dataIn, double r, int mMax, double sd, int tau)
		{
			this.dataIn = dataIn;
			this.r = r;
			this.tau = tau;
			this.mMax = mMax;
			this.sd = sd;
			this.tau = tau;
		}

		/** Implement the runnable interface */
		public void run()
		{
			coarseGrain = coarseGraining(dataIn, tau);
			se = sampleEntropy(coarseGrain, r, sd, tau, mMax);
		}

		/**
		 * Return the calculated SE
		 * 
		 * @return SE
		 */
		public double[] getSE()
		{
			double[] ret = new double[se.length];
			for (int i = 0; i < se.length; ++i)
			{
				ret[i] = se[i];
			}
			return ret;
		}

		/**
		 * Calculate sample entropy
		 * 
		 * @param arr
		 *            time series in (coarse-grained or otherwise)
		 * @param r
		 *            tolerance (maximum distance)
		 * @param sd
		 *            standard deviation of the original signal
		 * @param tau
		 *            the length of the mean for the coarse-grained time series
		 * @param mMax
		 *            the maximum m to calculate SE for
		 * @return se sample entropies at m = 2 to mMax
		 */
		private double[] sampleEntropy(double[] arr, double r, double sd, int tau, int mMax)
		{
			double tolerance = r * sd;
			int[] cont = new int[mMax + 2];
			for (int i = 0; i < cont.length; ++i)
			{
				cont[i] = 0;
			}
			for (int i = 0; i < arr.length - mMax; ++i) // arr.length - mMax causing the anomaly: difference sampEn at
														// same m for different range of m. (for difference mmax)
			{
				for (int j = i + 1; j < arr.length - mMax; ++j)
				{
					int k = 0;
					while (k < mMax && Math.abs(arr[i + k] - arr[j + k]) <= tolerance)
					{
						++k;
						cont[k]++;
					}
					if (k == mMax && Math.abs(arr[i + mMax] - arr[j + mMax]) <= tolerance) cont[mMax + 1]++;
				}
			}

			double[] se = new double[mMax];
			for (int i = 1; i <= mMax; ++i)
			{
				if (cont[i + 1] == 0 || cont[i] == 0)
				{
					se[i - 1] = -Math
							.log(1d / (((double) (arr.length - mMax)) * (((double) (arr.length - mMax)) - 1d)));
				}
				else
				{
					se[i - 1] = -Math.log(((double) cont[i + 1]) / ((double) cont[i]));
				}
			}
			return se;
		}

		/**
		 * Create coarse-grained time series
		 * 
		 * @param arr
		 *            original time series in
		 * @param scaleFactor
		 *            the length of mean for the coarse-grained time series
		 * @return Coarse-grained time series
		 */
		private double[] coarseGraining(double[] arr, int scaleFactor)
		{
			double[] y = new double[arr.length / scaleFactor];
			for (int i = 0; i < arr.length / scaleFactor; ++i)
			{
				y[i] = 0;
				for (int k = 0; k < scaleFactor; ++k)
				{
					y[i] += arr[i * scaleFactor + k];
				}
				y[i] /= (double) scaleFactor;
			}
			return y;
		}
	}

}