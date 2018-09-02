package org.activity.distances;

import java.util.ArrayList;

import org.activity.constants.Enums.PrimaryDimension;
import org.activity.objects.ActivityObject;

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
	double getDistance(ArrayList<ActivityObject> t1, ArrayList<ActivityObject> t2, PrimaryDimension givenDimension);

}