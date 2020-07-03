package org.activity.clustering;

import java.util.ArrayList;

import org.activity.constants.Constant;
import org.activity.distances.FeatureWiseEditDistance;
import org.activity.objects.Timeline;

public class DistancesMatrix
{
	ArrayList<Timeline> timelines;
	double[][] precomputedDistances;
	String distanceUsed;
	FeatureWiseEditDistance featureWiseEditDistance;

	public DistancesMatrix(ArrayList<Timeline> timelines)
	{
		this.timelines = timelines;
		precomputedDistances = new double[timelines.size()][timelines.size()];

		Constant.considerAllFeaturesForFeatureWiseEditDistance = false;
		Constant.setFeatureToConsiderForFeatureWiseEditDistance(true, false, false, false, false, false, false);

		featureWiseEditDistance = new FeatureWiseEditDistance(Constant.primaryDimension);
	}

	public int precomputeDistance()
	{
		for (int i = 0; i < timelines.size(); i++)
		{
			for (int j = 0; j < timelines.size(); j++)
			{
				if (i == j)
				{
					precomputedDistances[i][j] = 0;
				}
				else
				{
					precomputedDistances[i][j] = featureWiseEditDistance
							.getFeatureWiseEditDistanceWithTraceSingleFeature(
									timelines.get(i).getActivityObjectsInTimeline(),
									timelines.get(j).getActivityObjectsInTimeline())
							.getSecond();
				}
			}
		}
		return 0;
	}

	public double getDistance(int i, int j)
	{
		return precomputedDistances[i][j];
	}
}
