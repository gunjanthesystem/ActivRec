package org.activity.distances;

import java.util.ArrayList;

import org.activity.constants.Enums.PrimaryDimension;
import org.activity.objects.ActivityObject2018;

/**
 * 
 * @author gunjan
 * @since 31 Aug 2018
 */
public interface DistMetricI
{

	/**
	 * 
	 * @param t1
	 * @param t2
	 * @param givenDimension
	 * @return
	 */
	double getDistance(ArrayList<ActivityObject2018> t1, ArrayList<ActivityObject2018> t2, PrimaryDimension givenDimension);

}