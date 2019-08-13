package org.jmotif.timeseries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;

import org.jmotif.algorithm.MatrixFactory;
import org.jmotif.sax.alphabet.Alphabet;

/**
 * Implements algorithms for low-level data manipulation.
 * 
 * @author Pavel Senin
 * 
 */
public final class TSUtils
{

	/** The latin alphabet, lower case letters a-z. */
	static final char[] ALPHABET = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
			'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	/**
	 * Constructor.
	 */
	private TSUtils()
	{
		super();
	}

	/**
	 * Reads timeseries from a file. Assumes that file has a single double value on every line. Assigned timestamps are
	 * the line numbers.
	 * 
	 * @param filename
	 *            The file to read from.
	 * @param sizeLimit
	 *            The number of lines to read.
	 * @return Timeseries.
	 * @throws NumberFormatException
	 *             if error occurs.
	 * @throws IOException
	 *             if error occurs.
	 * @throws TSException
	 *             if error occurs.
	 */
	public static Timeseries readTS(String filename, int sizeLimit)
			throws NumberFormatException, IOException, TSException
	{
		BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
		String line = null;
		double[] values = new double[sizeLimit];
		long[] tstamps = new long[sizeLimit];
		int i = 0;
		while ((line = br.readLine()) != null)
		{
			values[i] = Double.valueOf(line);
			tstamps[i] = (long) i;
			i++;
		}
		br.close();
		return new Timeseries(values, tstamps);
	}

	/**
	 * Finds the maximal value in timeseries.
	 * 
	 * @param series
	 *            The timeseries.
	 * @return The max value.
	 */
	public static double max(Timeseries series)
	{
		if (countNaN(series) == series.size())
		{
			return Double.NaN;
		}
		double[] values = series.values();
		double max = Double.MIN_VALUE;
		for (int i = 0; i < values.length; i++)
		{
			if (max < values[i])
			{
				max = values[i];
			}
		}
		return max;
	}

	/**
	 * Finds the maximal value in timeseries.
	 * 
	 * @param series
	 *            The timeseries.
	 * @return The max value.
	 */
	public static double max(double[] series)
	{
		if (countNaN(series) == series.length)
		{
			return Double.NaN;
		}
		double max = Double.MIN_VALUE;
		for (int i = 0; i < series.length; i++)
		{
			if (max < series[i])
			{
				max = series[i];
			}
		}
		return max;
	}

	/**
	 * Finds the minimal value in timeseries.
	 * 
	 * @param series
	 *            The timeseries.
	 * @return The min value.
	 */
	public static double min(Timeseries series)
	{
		if (countNaN(series) == series.size())
		{
			return Double.NaN;
		}
		double[] values = series.values();
		double min = Double.MAX_VALUE;
		for (int i = 0; i < values.length; i++)
		{
			if (min > values[i])
			{
				min = values[i];
			}
		}
		return min;
	}

	/**
	 * Finds the minimal value in timeseries.
	 * 
	 * @param series
	 *            The timeseries.
	 * @return The min value.
	 */
	public static double min(double[] series)
	{
		if (countNaN(series) == series.length)
		{
			return Double.NaN;
		}
		double min = Double.MAX_VALUE;
		for (int i = 0; i < series.length; i++)
		{
			if (min > series[i])
			{
				min = series[i];
			}
		}
		return min;
	}

	/**
	 * Computes the mean value of timeseries.
	 * 
	 * @param series
	 *            The timeseries.
	 * @return The mean value.
	 */
	public static double mean(Timeseries series)
	{
		double res = 0D;
		int count = 0;
		for (TPoint tp : series)
		{
			if (Double.isNaN(tp.value()) || Double.isInfinite(tp.value()))
			{
				continue;
			}
			else
			{
				res += tp.value();
				count += 1;
			}
		}
		if (count > 0)
		{
			return res / ((Integer) count).doubleValue();
		}
		return Double.NaN;
	}

	/**
	 * Computes the mean value of timeseries.
	 * 
	 * @param series
	 *            The timeseries.
	 * @return The mean value.
	 */
	public static double mean(double[] series)
	{
		double res = 0D;
		int count = 0;
		for (double tp : series)
		{
			if (Double.isNaN(tp) || Double.isInfinite(tp))
			{
				continue;
			}
			else
			{
				res += tp;
				count += 1;
			}
		}
		if (count > 0)
		{
			return res / ((Integer) count).doubleValue();
		}
		return Double.NaN;
	}

	/**
	 * Compute the variance of timeseries.
	 * 
	 * @param series
	 *            The timeseries.
	 * @return The variance.
	 */
	public static double var(Timeseries series)
	{
		double res = 0D;
		double mean = mean(series);
		if (Double.isNaN(mean) || Double.isInfinite(mean))
		{
			return Double.NaN;
		}
		int count = 0;
		for (TPoint tp : series)
		{
			if (Double.isNaN(tp.value()) || Double.isInfinite(tp.value()))
			{
				continue;
			}
			else
			{
				res += (tp.value() - mean) * (tp.value() - mean);
				count += 1;
			}
		}
		if (count > 0)
		{
			return res / ((Integer) (count - 1)).doubleValue();
		}
		return Double.NaN;
	}

	/**
	 * Compute the variance of timeseries.
	 * 
	 * @param series
	 *            The timeseries.
	 * @return The variance.
	 */
	public static double var(double[] series)
	{
		double res = 0D;
		double mean = mean(series);
		if (Double.isNaN(mean) || Double.isInfinite(mean))
		{
			return Double.NaN;
		}
		int count = 0;
		for (double tp : series)
		{
			if (Double.isNaN(tp) || Double.isInfinite(tp))
			{
				continue;
			}
			else
			{
				res += (tp - mean) * (tp - mean);
				count += 1;
			}
		}
		if (count > 0)
		{
			return res / ((Integer) (count - 1)).doubleValue();
		}
		return Double.NaN;
	}

	/**
	 * Computes the standard deviation of timeseries.
	 * 
	 * @param series
	 *            The timeseries.
	 * @return the standard deviation.
	 */
	public static double stDev(Timeseries series)
	{
		double num0 = 0D;
		double sum = 0D;
		int count = 0;
		for (TPoint tp : series)
		{
			if (Double.isNaN(tp.value()) || Double.isInfinite(tp.value()))
			{
				continue;
			}
			else
			{
				num0 = num0 + tp.value() * tp.value();
				sum = sum + tp.value();
				count += 1;
			}
		}
		if (count > 0)
		{
			double len = ((Integer) count).doubleValue();
			return Math.sqrt((len * num0 - sum * sum) / (len * (len - 1)));
		}
		return Double.NaN;
	}

	/**
	 * Computes the standard deviation of timeseries.
	 * 
	 * @param series
	 *            The timeseries.
	 * @return the standard deviation.
	 */
	public static double stDev(double[] series)
	{
		double num0 = 0D;
		double sum = 0D;
		int count = 0;
		for (double tp : series)
		{
			if (Double.isNaN(tp) || Double.isInfinite(tp))
			{
				continue;
			}
			else
			{
				num0 = num0 + tp * tp;
				sum = sum + tp;
				count += 1;
			}
		}
		if (count > 0)
		{
			double len = ((Integer) count).doubleValue();
			return Math.sqrt((len * num0 - sum * sum) / (len * (len - 1)));
		}
		return Double.NaN;
	}

	/**
	 * Z-Normalize timeseries to the mean zero and standard deviation of one.
	 * 
	 * @param series
	 *            The timeseries.
	 * @return Z-normalized time-series.
	 * @throws TSException
	 *             if error occurs.
	 * @throws CloneNotSupportedException
	 */
	public static Timeseries zNormalize(Timeseries series) throws TSException, CloneNotSupportedException
	{

		// resulting series
		//
		Timeseries res = series.clone();

		// here I will extract doubles and normailize those, joining together later
		//
		double seriesValues[] = new double[series.size()];
		int idx = 0;
		for (TPoint p : series)
		{
			seriesValues[idx] = p.value();
			idx++;
		}
		double[] normalValues = zNormalize(seriesValues);

		// get things together
		//
		idx = 0;
		for (TPoint p : res)
		{
			p.setValue(normalValues[idx]);
			idx++;
		}
		return res;
	}

	/**
	 * Z-Normalize timeseries to the mean zero and standard deviation of one.
	 * 
	 * @param series
	 *            The timeseries.
	 * @return Z-normalized time-series.
	 * @throws TSException
	 *             if error occurs.
	 */
	public static double[] zNormalize(double[] series) throws TSException
	{

		// this is the resulting normalization
		//
		double[] res = new double[series.length];

		// get mean and sdev, NaN's will be handled
		//
		double mean = mean(series);
		double sd = stDev(series);

		// check if we hit special case, where something got NaN
		//
		if (Double.isInfinite(mean) || Double.isNaN(mean) || Double.isInfinite(sd) || Double.isNaN(sd))
		{

			// case[1] single value within the timeseries, normalize this value to 1.0 - magic number
			//
			int nanNum = countNaN(series);
			if ((series.length - nanNum) == 1)
			{
				for (int i = 0; i < res.length; i++)
				{
					if (Double.isInfinite(series[i]) || Double.isNaN(series[i]))
					{
						res[i] = Double.NaN;
					}
					else
					{
						res[i] = 1.0D;
					}
				}
			}

			// case[2] all values are NaN's
			//
			else if (series.length == nanNum)
			{
				for (int i = 0; i < res.length; i++)
				{
					res[i] = Double.NaN;
				}
			}
		}

		// another special case, where SD happens to be close to a zero, i.e. they all are the same for
		// example
		//
		else if (sd <= 0.001D)
		{

			// here I assign another magic value - 0.001D which makes to middle band of the normal
			// Alphabet
			//
			for (int i = 0; i < res.length; i++)
			{
				if (Double.isInfinite(series[i]) || Double.isNaN(series[i]))
				{
					res[i] = series[i];
				}
				else
				{
					res[i] = 0.1D;
				}
			}
		}

		// normal case, everything seems to be fine
		//
		else
		{
			// sd and mean here, - go-go-go
			for (int i = 0; i < res.length; i++)
			{
				res[i] = (series[i] - mean) / sd;
			}
		}
		return res;

	}

	/**
	 * Z-Normalize timeseries to the mean zero and standard deviation of one.
	 * 
	 * @param series
	 *            The timeseries.
	 * @param mean
	 *            The mean values.
	 * @param sd
	 *            The standard deviation.
	 * @return Z-normalized time-series.
	 * @throws TSException
	 *             if error occurs.
	 */
	public static double[] zNormalize(double[] series, double mean, double sd) throws TSException
	{

		// this is the resulting normalization
		//
		double[] res = new double[series.length];

		// check if we hit special case, where something got NaN
		//
		if (Double.isInfinite(mean) || Double.isNaN(mean) || Double.isInfinite(sd) || Double.isNaN(sd))
		{

			// case[1] single value within the timeseries, normalize this value to 1.0 - magic number
			//
			int nanNum = countNaN(series);
			if ((series.length - nanNum) == 1)
			{
				for (int i = 0; i < res.length; i++)
				{
					if (Double.isInfinite(series[i]) || Double.isNaN(series[i]))
					{
						res[i] = Double.NaN;
					}
					else
					{
						res[i] = 1.0D;
					}
				}
			}

			// case[2] all values are NaN's
			//
			else if (series.length == nanNum)
			{
				for (int i = 0; i < res.length; i++)
				{
					res[i] = Double.NaN;
				}
			}
		}

		// another special case, where SD happens to be close to a zero, i.e. they all are the same for
		// example
		//
		else if (sd <= 0.001D)
		{

			// here I assign another magic value - 0.001D which makes to middle band of the normal
			// Alphabet
			//
			for (int i = 0; i < res.length; i++)
			{
				if (Double.isInfinite(series[i]) || Double.isNaN(series[i]))
				{
					res[i] = series[i];
				}
				else
				{
					res[i] = 0.1D;
				}
			}
		}

		// normal case, everything seems to be fine
		//
		else
		{
			// sd and mean here, - go-go-go
			for (int i = 0; i < res.length; i++)
			{
				res[i] = (series[i] - mean) / sd;
			}
		}
		return res;

	}

	/**
	 * Approximate the timeseries using PAA. If the timeseries has some NaN's they are handled as follows: 1) if all
	 * values of the piece are NaNs - the piece is approximated as NaN, 2) if there are some (more or equal one) values
	 * happened to be in the piece - algorithm will handle it as usual - getting the mean.
	 * 
	 * @param ts
	 *            The timeseries to approximate.
	 * @param paaSize
	 *            The desired length of approximated timeseries.
	 * @return PAA-approximated timeseries.
	 * @throws TSException
	 *             if error occurs.
	 * @throws CloneNotSupportedException
	 *             if error occurs.
	 */
	public static Timeseries paa(Timeseries ts, int paaSize) throws TSException, CloneNotSupportedException
	{
		// fix the length
		int len = ts.size();
		// check for the trivial case
		if (len == paaSize)
		{
			return ts.clone();
		}
		else
		{
			// get values and timestamps
			double[][] vals = ts.valuesAsMatrix();
			long[] tStamps = ts.tstamps();
			// work out PAA by reshaping arrays
			double[][] res;
			if (len % paaSize == 0)
			{
				res = MatrixFactory.reshape(vals, len / paaSize, paaSize);
			}
			else
			{
				double[][] tmp = new double[paaSize][len];
				// System.out.println(Matrix.toString(tmp));
				for (int i = 0; i < paaSize; i++)
				{
					for (int j = 0; j < len; j++)
					{
						tmp[i][j] = vals[0][j];
					}
				}
				// System.out.println(Matrix.toString(tmp));
				double[][] expandedSS = MatrixFactory.reshape(tmp, 1, len * paaSize);
				// System.out.println(Matrix.toString(expandedSS));
				res = MatrixFactory.reshape(expandedSS, len, paaSize);
				// System.out.println(Matrix.toString(res));
			}
			//
			// now, here is a new trick comes in game - because we have so many
			// "lost" values
			// PAA game rules will change - we will omit NaN values and put NaNs
			// back to PAA series
			//
			//
			// this is the old line of code here:
			// double[] newVals = MatrixFactory.colMeans(res);
			//
			// i will need to test this though
			//
			//
			double[] newVals = MatrixFactory.colMeans(res);

			// work out timestamps
			long start = tStamps[0];
			long interval = tStamps[len - 1] - start;
			double increment = Long.valueOf(interval).doubleValue() / Long.valueOf(paaSize).doubleValue();
			long[] newTstamps = new long[paaSize];
			for (int i = 0; i < paaSize; i++)
			{
				newTstamps[i] = start + Double.valueOf(increment / 2.0D + i * increment).longValue();
			}
			return new Timeseries(newVals, newTstamps);
		}
	}

	/**
	 * Approximate the timeseries using PAA. If the timeseries has some NaN's they are handled as follows: 1) if all
	 * values of the piece are NaNs - the piece is approximated as NaN, 2) if there are some (more or equal one) values
	 * happened to be in the piece - algorithm will handle it as usual - getting the mean.
	 * 
	 * @param ts
	 *            The timeseries to approximate.
	 * @param paaSize
	 *            The desired length of approximated timeseries.
	 * @return PAA-approximated timeseries.
	 * @throws TSException
	 *             if error occurs.
	 */
	public static double[] paa(double[] ts, int paaSize) throws TSException
	{
		// fix the length
		int len = ts.length;
		// check for the trivial case
		if (len == paaSize)
		{
			return Arrays.copyOf(ts, ts.length);
		}
		else
		{
			// get values and timestamps
			double[][] vals = asMatrix(ts);
			// work out PAA by reshaping arrays
			double[][] res;
			if (len % paaSize == 0)
			{
				res = MatrixFactory.reshape(vals, len / paaSize, paaSize);
			}
			else
			{
				double[][] tmp = new double[paaSize][len];
				for (int i = 0; i < paaSize; i++)
				{
					for (int j = 0; j < len; j++)
					{
						tmp[i][j] = vals[0][j];
					}
				}
				double[][] expandedSS = MatrixFactory.reshape(tmp, 1, len * paaSize);
				res = MatrixFactory.reshape(expandedSS, len, paaSize);
			}
			double[] newVals = MatrixFactory.colMeans(res);

			return newVals;
		}

	}

	/**
	 * Approximate the timeseries using PAA. If the timeseries has some NaN's they are handled as follows: 1) if all
	 * values of the piece are NaNs - the piece is approximated as NaN, 2) if there are some (more or equal one) values
	 * happened to be in the piece - algorithm will handle it as usual - getting the mean.
	 * 
	 * @param ts
	 *            The timeseries to approximate.
	 * @param paaSize
	 *            The desired length of approximated timeseries.
	 * @return PAA-approximated timeseries.
	 * @throws TSException
	 *             if error occurs.
	 */
	public static double[] optimizedPaa(double[] ts, int paaSize) throws TSException
	{
		// fix the length
		int len = ts.length;
		// check for the trivial case
		if (len == paaSize)
		{
			return Arrays.copyOf(ts, ts.length);
		}
		else
		{
			if (len % paaSize == 0)
			{
				return MatrixFactory.colMeans(MatrixFactory.reshape(asMatrix(ts), len / paaSize, paaSize));
			}
			else
			{
				// res = new double[len][paaSize];
				// for (int j = 0; j < len; j++) {
				// for (int i = 0; i < paaSize; i++) {
				// int idx = j * paaSize + i;
				// int row = idx % len;
				// int col = idx / len;
				// res[row][col] = ts[j];
				// }
				// }
				double[] paa = new double[paaSize];
				for (int i = 0; i < len * paaSize; i++)
				{
					int idx = i / len; // the spot
					int pos = i / paaSize; // the col spot
					paa[idx] = paa[idx] + ts[pos];
				}
				for (int i = 0; i < paaSize; i++)
				{
					paa[i] = paa[i] / (double) len;
				}
				return paa;
			}
		}

	}

	public static double[] paaZeroNA(double[] ts, int paaSize)
	{
		for (int i = 0; i < ts.length; i++)
		{
			if (ts[i] == 0)
			{
				ts[i] = Double.NaN;
			}
		}
		// fix the length
		int len = ts.length;
		// check for the trivial case
		if (len == paaSize)
		{
			return Arrays.copyOf(ts, ts.length);
		}
		else
		{
			if (len % paaSize == 0)
			{
				return MatrixFactory.colMeans(MatrixFactory.reshape(asMatrix(ts), len / paaSize, paaSize));
			}
			else
			{
				// res = new double[len][paaSize];
				// for (int j = 0; j < len; j++) {
				// for (int i = 0; i < paaSize; i++) {
				// int idx = j * paaSize + i;
				// int row = idx % len;
				// int col = idx / len;
				// res[row][col] = ts[j];
				// }
				// }
				double[] paa = new double[paaSize];
				short[] paaLen = new short[paaSize];
				for (int i = 0; i < len * paaSize; i++)
				{
					int idx = i / len; // the spot
					int pos = i / paaSize; // the col spot
					if (0.0D == ts[pos])
					{
						paa[idx] = paa[idx];
					}
					else
					{
						paa[idx] = paa[idx] + ts[pos];
						paaLen[idx]++;
					}
				}
				for (int i = 0; i < paaSize; i++)
				{
					if (paa[i] == 0 && paaLen[i] == 0)
					{
						paa[i] = Double.NaN;
					}
					paa[i] = paa[i] / (double) len;
				}
				return paa;
			}
		}
	}

	/**
	 * Converts a timeseries into the string using alphabet cuts.
	 * 
	 * @param series
	 *            The timeseries to convert.
	 * @param alphabet
	 *            The alphabet to use.
	 * @param alphabetSize
	 *            The alphabet size.
	 * @return Symbolic (SAX) representation of timeseries.
	 * @throws TSException
	 *             if error occurs.
	 */
	public static char[] ts2String(Timeseries series, Alphabet alphabet, int alphabetSize) throws TSException
	{
		double[] cuts = alphabet.getCuts(alphabetSize);
		char[] res = new char[series.size()];
		for (int i = 0; i < series.size(); i++)
		{
			res[i] = num2char(series.elementAt(i).value(), cuts);
		}
		return res;
	}

	/**
	 * Converts the timeseries into string using given cuts intervals. Useful for not-normal distribution cuts.
	 * 
	 * @param vals
	 *            The timeseries.
	 * @param cuts
	 *            The cut intervals.
	 * @return The timeseries SAX representation.
	 */
	public static char[] ts2String(double[] vals, double[] cuts)
	{
		char[] res = new char[vals.length];
		for (int i = 0; i < vals.length; i++)
		{
			res[i] = num2char(vals[i], cuts);
		}
		return res;
	}

	/**
	 * Converts a timeseries into the string paying attention to NaN values.
	 * 
	 * @param series
	 *            The timeseries to convert.
	 * @param alphabet
	 *            The alphabet to use.
	 * @param alphabetSize
	 *            The alphabet size in use.
	 * @return SAX representation of timeseries.
	 * @throws TSException
	 *             if error occurs.
	 */
	public static char[] ts2StringWithNaN(Timeseries series, Alphabet alphabet, int alphabetSize) throws TSException
	{
		double[] cuts = alphabet.getCuts(alphabetSize);
		return ts2StringWithNaNByCuts(series, cuts);
	}

	/**
	 * Converts a timeseries into the string paying attention to NaN values.
	 * 
	 * @param series
	 *            The timeseries to convert.
	 * @param cuts
	 *            The cuts for alphabet.
	 * @return SAX representation of timeseries.
	 * @throws TSException
	 *             if error occurs.
	 */
	public static char[] ts2StringWithNaNByCuts(Timeseries series, double[] cuts) throws TSException
	{
		char[] res = new char[series.size()];
		for (int i = 0; i < series.size(); i++)
		{
			if (Double.isNaN(series.elementAt(i).value()) || Double.isInfinite(series.elementAt(i).value()))
			{
				res[i] = '_';
			}
			else
			{
				res[i] = num2char(series.elementAt(i).value(), cuts);
			}
		}
		return res;
	}

	/**
	 * Get mapping of a number to char.
	 * 
	 * @param value
	 *            the value to map.
	 * @param cuts
	 *            the array of intervals.
	 * @return character corresponding to numeric value.
	 */
	public static char num2char(double value, double[] cuts)
	{
		int count = 0;
		while ((count < cuts.length) && (cuts[count] <= value))
		{
			count++;
		}
		return ALPHABET[count];
	}

	/**
	 * Converts index into char.
	 * 
	 * @param idx
	 *            The index value.
	 * @return The char by index.
	 */
	public static char num2char(int idx)
	{
		return ALPHABET[idx];
	}

	/**
	 * Convert the timeseries into the index using SAX cuts.
	 * 
	 * @param series
	 *            The timeseries to convert.
	 * @param alphabet
	 *            The alphabet to use.
	 * @param alphabetSize
	 *            The alphabet size in use.
	 * @return SAX representation of timeseries.
	 * @throws TSException
	 *             if error occurs.
	 */
	public static int[] ts2Index(Timeseries series, Alphabet alphabet, int alphabetSize) throws TSException
	{
		double[] cuts = alphabet.getCuts(alphabetSize);
		int[] res = new int[series.size()];
		for (int i = 0; i < series.size(); i++)
		{
			res[i] = num2index(series.elementAt(i).value(), cuts);
		}
		return res;
	}

	/**
	 * Get mapping of number to cut index.
	 * 
	 * @param value
	 *            the value to map.
	 * @param cuts
	 *            the array of intervals.
	 * @return character corresponding to numeric value.
	 */
	public static int num2index(double value, double[] cuts)
	{
		int count = 0;
		while ((count < cuts.length) && (cuts[count] <= value))
		{
			count++;
		}
		return count;
	}

	/**
	 * Counts the number of NaNs' in the timeseries.
	 * 
	 * @param series
	 *            The timeseries.
	 * @return The count of NaN values.
	 */
	public static int countNaN(double[] series)
	{
		int res = 0;
		for (double d : series)
		{
			if (Double.isInfinite(d) || Double.isNaN(d))
			{
				res += 1;
			}
		}
		return res;
	}

	/**
	 * Counts the number of NaNs' in the timeseries.
	 * 
	 * @param series
	 *            The timeseries.
	 * @return The count of NaN values.
	 */
	private static int countNaN(Timeseries series)
	{
		int res = 0;
		for (TPoint tp : series)
		{
			if (Double.isInfinite(tp.value()) || Double.isNaN(tp.value()))
			{
				res += 1;
			}
		}
		return res;
	}

	/**
	 * Converts the vector into one-row matrix.
	 * 
	 * @param vector
	 *            The vector.
	 * @return The matrix.
	 */
	public static double[][] asMatrix(double[] vector)
	{
		double[][] res = new double[1][vector.length];
		for (int i = 0; i < vector.length; i++)
		{
			res[0][i] = vector[i];
		}
		return res;
	}

	/**
	 * Extract subseries out of series.
	 * 
	 * @param series
	 *            The series array.
	 * @param start
	 *            Start position
	 * @param length
	 *            Length of subseries to extract.
	 * @return The subseries.
	 * @throws IndexOutOfBoundsException
	 *             If error occurs.
	 */
	public static double[] subseries(double[] series, int start, int length) throws IndexOutOfBoundsException
	{
		if (start + length > series.length)
		{
			throw new IndexOutOfBoundsException("Unable to extract subseries, series length: " + series.length
					+ ", start: " + start + ", subseries length: " + length);
		}
		double[] res = new double[length];
		for (int i = 0; i < length; i++)
		{
			res[i] = series[start + i];
		}
		return res;
	}

	/**
	 * Extract subseries out of series.
	 * 
	 * @param series
	 *            The series array.
	 * @param start
	 *            Start position
	 * @param length
	 *            Length of subseries to extract.
	 * @return The subseries.
	 * @throws IndexOutOfBoundsException
	 *             If error occurs.
	 */
	public static double[] subseries(Double[] series, int start, int length) throws IndexOutOfBoundsException
	{
		if (start + length > series.length)
		{
			throw new IndexOutOfBoundsException("Unable to extract subseries, series length: " + series.length
					+ ", start: " + start + ", subseries length: " + length);
		}
		double[] res = new double[length];
		for (int i = 0; i < length; i++)
		{
			res[i] = series[start + i];
		}
		return res;
	}

	/**
	 * Implements Gaussian smoothing.
	 * 
	 * @param series
	 *            Data to process.
	 * @param filterWidth
	 *            The filter width.
	 * @return smoothed series.
	 * @throws TSException
	 *             if error occurs.
	 */
	public static double[] gaussFilter(double[] series, double filterWidth) throws TSException
	{

		double[] smoothedSignal = new double[series.length];
		double sigma = filterWidth / 2D;
		int maxShift = (int) Math.floor(4D * sigma); // Gaussian curve is reasonably > 0

		if (maxShift < 1)
		{
			throw new TSException("NOT smoothing: filter width too small - " + filterWidth);
		}
		for (int i = 0; i < smoothedSignal.length; i++)
		{
			smoothedSignal[i] = series[i];

			if (maxShift < 1)
			{
				continue;
			}
			for (int j = 1; j <= maxShift; j++)
			{

				double gaussFilter = Math.exp(-(j * j) / (2. * sigma * sigma));
				double leftAmpl, rightAmpl;

				// go left
				if ((i - j) >= 0)
				{
					leftAmpl = series[i - j];
				}
				else
				{
					leftAmpl = series[i];
				}

				// go right
				if ((i + j) <= smoothedSignal.length - 1)
				{
					rightAmpl = series[i + j];
				}
				else
				{
					rightAmpl = series[i];
				}

				smoothedSignal[i] += gaussFilter * (leftAmpl + rightAmpl);

			}

			double normalizingCoef = Math.sqrt(2. * Math.PI) * sigma;
			smoothedSignal[i] /= normalizingCoef;

		}
		return smoothedSignal;
	}

	public double gaussian(double x, double filterWidth)
	{
		double sigma = filterWidth / 2.;
		return Math.exp(-(x * x) / (2. * sigma * sigma));
	}

	public static String seriesToString(double[] series, NumberFormat df)
	{
		StringBuffer sb = new StringBuffer();
		sb.append('[');
		for (double d : series)
		{
			sb.append(df.format(d)).append(',');
		}
		sb.delete(sb.length() - 2, sb.length() - 1).append("]");
		return sb.toString();
	}

}
