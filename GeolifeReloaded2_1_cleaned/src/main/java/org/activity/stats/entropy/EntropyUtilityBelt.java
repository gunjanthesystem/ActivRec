package org.activity.stats.entropy;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.activity.stats.StatsUtils;

/**
 * Contains an assortment of methods useful for different kinds of entropies
 * 
 * @author gunjan
 *
 */
public class EntropyUtilityBelt
{
	public static double[] getRandomSeries(int size)
	{
		double vArray[] = new double[size];
		for (int i = 0; i < size; i++)
		{
			Random rn = new Random();
			double gg = 0 + rn.nextInt(100 - 0 + 1);
			vArray[i] = gg;
		}
		return vArray;
	}

	public static double[] getNaiveSeries(int size)
	{
		double vArray[] = new double[size];
		for (int i = 0; i < size; i++)
		{
			vArray[i] = i + 1;
		}
		return vArray;
	}

	public static double[] getPureRegularSeries(int size)
	{
		double vArray[] = new double[size];

		for (int i = 0; i < size;)
		{
			for (int j = 10; j <= 40;)
			{
				vArray[i] = j;
				j += 10;
				i++;
			}
		}

		return vArray;
	}

	/**
	 * Calculate sample entropy
	 * 
	 * Sample Entropy is a useful tool for investigating the dynamics of heart rate and other time series. Sample
	 * Entropy is the negative natural logarithm of an estimate of the conditional probability that subseries (epochs)
	 * of length m that match pointwise within a tolerance r also match at the next point.
	 * 
	 * @param arr
	 *            time series in (coarse-grained or otherwise)
	 * @param r
	 *            tolerance (maximum distance) (suggested value based on heart-rate series = 0.2, suggested as default
	 *            value or 0.15)
	 * @param sd
	 *            standard deviation of the original signal
	 * @param mMax
	 *            the maximum epoch length, m to calculate Sample Entropy for
	 * 
	 * @return se sample entropies at m = 2 to mMax (epoch length is varied from 2 to mMax)
	 * 
	 * @see Based on: https://github.com/tjrantal/javaMSE/blob/master/src/edu/deakin/timo/MultiscaleEntropy.java based
	 *      on http://www.physionet.org/physiotools/mse/mse.c
	 * @see http://physionet.org/physiotools/sampen/
	 * @see comapable r function: http://www.inside-r.org/packages/cran/pracma/docs/approx_entropy
	 * @changes removed the parameter tau
	 */
	public static double[] getSampleEntropies(double[] arr, double r, double sd, int mMax)
	{
		double tolerance = r * sd;
		int[] cont = new int[mMax + 2];

		for (int i = 0; i < cont.length; ++i)
		{
			cont[i] = 0;
		}

		for (int i = 0; i < arr.length - mMax; ++i)
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
				se[i - 1] = -Math.log(1d / (((double) (arr.length - mMax)) * (((double) (arr.length - mMax)) - 1d)));
			}
			else
			{
				se[i - 1] = -Math.log(((double) cont[i + 1]) / ((double) cont[i]));
			}
		}
		return se;
	}

	// //////////
	/**
	 * returns the shannon entropy of the given string (courtesy: http://rosettacode.org/wiki/Entropy#Java)
	 * 
	 * (duplicate method in UtilityBelt.java)
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
			e += p * StatsUtils.log2(p);
		}

		System.out.println("\n\nEntropy for string: " + s + "\n\t is " + (-e));
		return -e;
	}
}
