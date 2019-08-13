package org.activity.distances;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.activity.constants.Enums.PrimaryDimension;
import org.activity.objects.ActivityObject2018;
import org.activity.sanityChecks.Sanity;

public class JaccardDistance implements StringDistI
{
	public static final JaccardDistance STATIC = new JaccardDistance();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.activity.distances.DistMetric#getDistance(java.util.ArrayList, java.util.ArrayList,
	 * org.activity.constants.Enums.PrimaryDimension)
	 */
	public double getDistance(ArrayList<ActivityObject2018> t1, ArrayList<ActivityObject2018> t2,
			PrimaryDimension givenDimension)
	{
		Set<Integer> pdVals1 = t1.stream().map(ao -> ao.getGivenDimensionVal(givenDimension)).flatMap(v -> v.stream())
				.collect(Collectors.toSet());

		Set<Integer> pdVals2 = t2.stream().map(ao -> ao.getGivenDimensionVal(givenDimension)).flatMap(v -> v.stream())
				.collect(Collectors.toSet());

		return getDistance(pdVals1, pdVals2);
	}

	public double getDistance(String word1, String word2)
	{
		Set<Character> charSet1 = word1.chars().mapToObj(i -> (char) i).collect(Collectors.toSet());
		Set<Character> charSet2 = word2.chars().mapToObj(i -> (char) i).collect(Collectors.toSet());

		Set<Character> intersection = new HashSet<>(charSet1);
		Set<Character> union = new HashSet<>(charSet1);

		intersection.retainAll(charSet2);
		union.addAll(charSet2);

		double jaccardIndex = (intersection.size() * 1.0) / union.size();
		double jaccardDistance = 1 - jaccardIndex;

		Sanity.inRange(jaccardDistance, 0, 1, "Jaccard outside [0,1]");
		return jaccardDistance;
	}

	public double getDistance(Set<Integer> pdVals1, Set<Integer> pdVals2)
	{
		Set<Integer> intersection = new HashSet<>(pdVals1);
		Set<Integer> union = new HashSet<>(pdVals1);

		intersection.retainAll(pdVals2);
		union.addAll(pdVals2);

		double jaccardIndex = (intersection.size() * 1.0) / union.size();
		double jaccardDistance = 1 - jaccardIndex;

		Sanity.inRange(jaccardDistance, 0, 1, "Jaccard outside [0,1]");
		return jaccardDistance;
	}
}
