package org.activity.distances;

public class DistanceUtils
{
	/**
	 * Ensure that both vectors are of same length
	 * 
	 * @param vec1
	 * @param vec2
	 * @return
	 */
	public static double getChebyshevDistance(double[] vector1, double[] vector2)
	{
		if (vector1.length != vector2.length)
		{
			System.err.println("Error in getChebyshevDistance: compared vectors of different length");
			return -1;
		}

		if (vector1.length == 0)
		{
			System.err.println("Error in getChebyshevDistance: compared vectors are of zero length");
			return -1;
		}

		double maxDiff = -1d;

		for (int i = 0; i < vector1.length; i++)
		{
			double diff = Math.abs(vector1[i] - vector2[i]);
			if (diff > maxDiff)
			{
				maxDiff = diff;
			}
		}

		return maxDiff;
	}
}
