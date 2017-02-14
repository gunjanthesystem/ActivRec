package org.activity.evaluation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

/**
 * based on // https://en.wikipedia.org/wiki/Discounted_cumulative_gain#Normalized_DCG
 * <p>
 * Implementing the wikipedia example
 * 
 * @author gunjan
 */
public class NDCG
{

	public static void main(String args[])
	{
		List<Integer> ideal = Arrays.asList(3, 3, 2, 2, 1, 0);
		List<Integer> given = Arrays.asList(3, 2, 3, 0, 1, 2);

		// System.out.println(compute(given, ideal, null));
		for (int k = 1; k <= 6; k++)
		{
			System.out.println(" -- at k = " + k + " NDCG = " + getNDCG(given, ideal, k, 5) + "\n");// , null));
		}
	}

	public static double getNDCG(List<Integer> givenRelevanceList, List<Integer> idealRelevanceList, int p,
			int decimalPlaces)
	{
		double ndcg = -1;
		double dcg = getDCG(givenRelevanceList, p);
		double idcg = getDCG(idealRelevanceList, p);

		ndcg = dcg / idcg;

		System.out.println("dcg = " + dcg + " idcg = " + idcg + " ndcg = " + ndcg);
		return round(ndcg, decimalPlaces);
	}

	public static double getDCG(List<Integer> givenRelevanceList, int k)
	{
		double dcg = -1;

		if (k > givenRelevanceList.size())
		{
			System.err.println("Error in getDCG: position p (=" + k + " > size of list" + givenRelevanceList.size());
		}
		else
		{
			dcg = 0;

			for (int i = 1; i <= k; i++)
			{
				double log2i1 = log2(i + 1);
				dcg += (givenRelevanceList.get(i - 1) / log2i1);
			}
		}
		return dcg;
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
