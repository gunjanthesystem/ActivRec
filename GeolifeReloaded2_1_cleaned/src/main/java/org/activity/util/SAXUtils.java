package org.activity.util;

import java.sql.Timestamp;

import org.activity.jmotif.sax.SAXFactory;
import org.activity.jmotif.sax.alphabet.NormalAlphabet;
import org.activity.jmotif.timeseries.TSException;
import org.activity.jmotif.timeseries.Timeseries;

import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.SAXProcessor;

public class SAXUtils
{

	public static void main(String[] args)
	{
		/*
		 * To use: public static String ts2string(Timeseries ts, int paaSize, Alphabet alphabet, int alphabetSize)
		 * throws TSException, CloneNotSupportedException {
		 */
		Timestamp a = new Timestamp(2014, 4, 4, 4, 4, 4, 0);

		double vals[] = { 12, 14, 23.5, 22, 4 };
		long stamps[] = new long[5];

		System.out.println("Timeseries:");
		for (int i = 0; i < 5; i++)
		{
			stamps[i] = a.getTime() + i * 60 * 1000;
		}

		for (int i = 0; i < 5; i++)
		{
			System.out.println(vals[i] + ":" + stamps[i]);
		}

		System.out.println("///////////");

		try
		{
			// Timeseries ts = new Timeseries(vals, stamps);
			//
			// String resultant = SAXFactory.ts2string(ts, 5, new NormalAlphabet(), 10);
			//

			System.out.println("String representation = " + getSAXString(vals, stamps, 5, 10));
			System.out.println("String representation = " + getSAXStringNewVersion(vals, stamps, 5, 10));
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param vals
	 * @param stamps
	 * @param numDataPoints
	 *            num of points used in PAA reduction of time series
	 * @param alphabetSize
	 *            alphabet size used
	 * @return
	 */
	public static String getSAXString(double vals[], long stamps[], int numDataPoints, int alphabetSize)
	{
		String res = null;
		try
		{
			Timeseries ts = new Timeseries(vals, stamps);
			res = SAXFactory.ts2string(ts, numDataPoints, new NormalAlphabet(), 10);
		}
		catch (TSException | CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * Using the new SAX version available on github which does deprecated some previous classes like Timeseries and
	 * change some methods. (16 Nov 2016)
	 * 
	 * @param vals
	 * @param stamps
	 * @param numDataPoints
	 *            num of points used in PAA reduction of time series
	 * @param alphabetSize
	 *            alphabet size used
	 * @return
	 */
	public static String getSAXStringNewVersion(double vals[], long stamps[], int numDataPoints, int alphabetSize)
	{
		// ### 3.1 Discretizing time-series *by chunking*:

		// instantiate classes
		NormalAlphabet na = new NormalAlphabet();
		SAXProcessor sp = new SAXProcessor();

		// read the input file
		// double[] ts = TSProcessor.readFileColumn(dataFName, 0, 0);

		// perform the discretization
		String strs = "";
		try
		{
			// note: nThredhold: normalisation threshold for znorm = 0d, if std dev is below this val then all 0s are
			// returned. Currently, my aim is to keep it consistent with the
			// previous verion of the implementation.
			strs = sp.ts2saxByChunking(vals, numDataPoints, na.getCuts(alphabetSize), 0d).getSAXString(",");
		}

		catch (SAXException | TSException e)
		{
			e.printStackTrace();
		}

		// print the output
		// System.out.println(strs);
		return strs;
	}
}
