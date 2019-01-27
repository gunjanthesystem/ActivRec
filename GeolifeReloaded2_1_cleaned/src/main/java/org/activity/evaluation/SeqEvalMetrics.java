package org.activity.evaluation;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.activity.sanityChecks.Sanity;

/**
 * 
 * @author gunjan
 * @since 23 Jan 2019
 */
public class SeqEvalMetrics
{
	public static void main(String args[])
	{
		String predSeq = "ACA";
		Character target = 'A';

		System.out.println("RR = " + getReciprocalRank(target, predSeq));
	}

	public static void fun2(String targetSeq, String candSeq)
	{
		Sanity.eq(targetSeq.length(), candSeq.length(), "Error: unequal lengths of target and cand seq");
		double res = -99;
		int count = 0;
		for (int first = 0; first < candSeq.length(); first++)
		{
			Character targetChar = targetSeq.charAt(first);
			double RRForThisFirst = getReciprocalRank(targetChar, candSeq);
			res = (res == -99) ? RRForThisFirst : res + RRForThisFirst;
			count += 1;
		}
		res = res / count;
	}

	/**
	 * 
	 * @param candSeq
	 * @param targetChar
	 * @return
	 */
	public static double getReciprocalRank(Character targetChar, String candSeq)
	{
		int rank = -99;
		for (int y = 0; y < candSeq.length(); y++)
		{
			if (((Character) candSeq.charAt(y)).equals(targetChar))
			{
				// System.out.println("Equals, y = " + y);
				rank = y + 1;
				break;// assuming that the actual occurs only once in the recommended list
			}
		}
		if (rank != -99)
		{
			// System.out.println("(double) 1 / rank = " + (double) 1 / rank);
			return round((double) 1 / rank, 4);
		}
		else
		{
			return 0; // assuming the rank is at infinity
		}
	}

	//////// utility method below
	/**
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

		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	public static double log2(double a)
	{
		return Math.log(a) / Math.log(2);
	}
}
