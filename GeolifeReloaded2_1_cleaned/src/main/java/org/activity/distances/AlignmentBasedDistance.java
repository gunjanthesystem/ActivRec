package org.activity.distances;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activity.constants.Constant;
import org.activity.constants.DomainConstants;
import org.activity.constants.Enums;
import org.activity.constants.Enums.GowallaFeatures;
import org.activity.constants.Enums.PrimaryDimension;
import org.activity.constants.VerbosityConstants;
import org.activity.io.WToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.Pair;
import org.activity.objects.TraceMatrixLeaner1;
import org.activity.objects.Triple;
import org.activity.spatial.SpatialUtils;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.DateTimeUtils;
import org.activity.util.StringCode;
import org.activity.util.StringUtils;
import org.activity.util.UtilityBelt;
import org.apache.commons.math.util.FastMath;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Note: this has been modified for Geolife data set to account for the additional attributes from geolocation
 * 
 * IT IS ADVISED NOT TO CREATE ANY STATIC METHOD IN THIS CLASS
 * 
 * @author gunjan
 *
 */
public class AlignmentBasedDistance
{
	// We must satisfy (costReplaceLocation + costReplaceStartTime + costReplaceDuration) < 1
	// double costReplaceLocation = 1;//0.2d; //REMOVE LOCATION AND CHANGE COST OF REPLACEMENT TO 2
	// double costReplaceStartTime =1;// 0.6d;
	// double costReplaceDuration = 1;//0.2d;
	// double costReplaceActivityName = 2;

	EnumMap<GowallaFeatures, Double> featureWeightMap;// added on April 17 2018

	protected final PrimaryDimension primaryDimension;
	private boolean shouldComputeFeatureLevelDistance;
	double wtActivityName;// = 3d;
	double wtStartTime;// = 1d;// 0.6d;
	double wtDuration;// = 0.5d;// 0.2d;
	double wtDistanceTravelled;// = 3d;// //1d;
	double wtStartGeo;// = 0.3d;
	double wtEndGeo;// = 0.3d;
	double wtAvgAltitude;// = 0.2d;
	double wtLocation;
	double wtLocPopularity;

	//
	double wtDistanceFromPrev;
	double wtDurationFromPrev;

	double wtDistanceFromNext;
	double wtDurationFromNext;

	double wtFullActivityObject;// = wtActivityName + wtStartTime + wtDuration + wtDistanceTravelled + wtStartGeo +
								// wtEndGeo + +wtAvgAltitude;

	double costInsertActivityObject;// = 1d * wtFullActivityObject; // cost of insert operation 4.5
	double costDeleteActivityObject;// = 1d * wtFullActivityObject; // cost of delete operation 4.5
	double costReplaceActivityObject;// = 2d * wtFullActivityObject; // cost of replace operation 9

	double[][][] distMatrix;

	final double defaultCostInsert = 1d;
	final double defaultCostDelete = 1d;
	final double defaultCostReplace = 2d;

	long startTimeToleranceInSeconds;// = 120; // in seconds
	long durationToleranceInSeconds;// = 120; // in seconds
	double distanceTravelledTolerance;// = 0.2d; // Kilometers, as our Haversine distance calculation uses this unit
	double startGeoTolerance;// = 0.2d;
	double endGeoTolerance;// = 0.2d;
	double avgAltTolerance;// / = 5d;// feets since raw data is in this unit
	double locationPopularityTolerance;

	double durationFromPrevTolerance;
	double distanceFromPrevTolerance;

	// Gowalla features to use in feature level of edit distance
	boolean useActivityNameInFED, useStartTimeInFED, useLocationInFED, usePopularityInFED, useDistFromPrevInFED,
			useDurationFromPrevInFED, useDistFromNextInFED, useDurationFromNextInFED;

	int numberOfInsertions = 0;
	int numberOfDeletions = 0;
	int numberOfReplacements = 0;

	// index 0 is StartGeo Diff for user 062, index 1 is EndGeo for user 062, index 2 is StartGeo Diff for user 084,
	// index 3 is EndGeo for user 084, and so on,
	private final static double thirdQuartilesStartEndGeoDiffForGeolife[] = { 50.16, 39.15, 45.66, 46.7, 11.34, 11.42,
			10.86, 10.91, 18.44, 18.27, 9.89, 9.81, 12.97, 12.89, 12.98, 13.01, 170.915, 263.39, 1581.13, 1581.0075 };

	/*
	 * 0...0, 1...2, 2....4 user start,end 0 --> 0,1 1 --> 2,3 2---> 4,5 3---> 6,7 4---> 8,9 9--> 18,19 for start geo
	 * quartile... find the index of userID, i.e. ui index of thirdquartile to be used= ui *2; for end geo quartile...
	 * find the index of userID, i.e. ui index of thirdquartile to be used= (ui *2)+1;
	 */

	/**
	 * Sets the tolerance according the truth value of Constant.useTolerance, sets with weights and costs to be used
	 * (using the default insert(=1), delete (=1), replace wts (=2))
	 * 
	 * @throws Exception
	 */
	public AlignmentBasedDistance(PrimaryDimension primaryDimension)
	{
		setGowallaFeaturesToUseInDistance();
		setWeightsAndCosts(); // VERY IMPORTANT
		this.primaryDimension = primaryDimension;
		setShouldComputeFeatureLevelDistance();
		if (!Constant.useTolerance)// == false)
		{
			setTolerancesToZero();
		}
		else
		{
			setTolerancesToDefault();
		}
	}

	/**
	 * 
	 * @return
	 */
	public EnumMap<GowallaFeatures, Double> getFeatureWeightMap()
	{
		return featureWeightMap;
	}

	/**
	 * @since 10 April 2018
	 */
	private void setShouldComputeFeatureLevelDistance()
	{
		shouldComputeFeatureLevelDistance = true;
		if ((1 - Constant.EDAlpha) == 0)
		{
			shouldComputeFeatureLevelDistance = false;
		}
		else if (getSumOfWeightOfFeaturesExceptPrimaryDimension() == 0)
		{
			System.err
					.println("Warning: -- Since no features are being used it is suggested to set Constant.EDAlpha=1.\n"
							+ "so that the computed values for dAct are not multiplied by EDAlpha and reduced.");
			shouldComputeFeatureLevelDistance = false;
		}
		System.out.println("IMPORTANT: shouldComputeFeatureLevelDistance= " + shouldComputeFeatureLevelDistance);
	}

	/**
	 * @since 10 April 2018
	 */
	public boolean getShouldComputeFeatureLevelDistance()
	{
		return shouldComputeFeatureLevelDistance;
	}

	/**
	 * Uses static values from Constant class
	 */
	private void setGowallaFeaturesToUseInDistance()
	{
		useActivityNameInFED = Constant.useActivityNameInFED;
		useStartTimeInFED = Constant.useStartTimeInFED;
		useLocationInFED = Constant.useLocationInFED;
		usePopularityInFED = Constant.usePopularityInFED;
		useDistFromPrevInFED = Constant.useDistFromPrevInFED;
		useDurationFromPrevInFED = Constant.useDurationFromPrevInFED;
		useDistFromNextInFED = Constant.useDistFromNextInFED;
		useDurationFromNextInFED = Constant.useDurationFromNextInFED;

	}

	/**
	 * Set weight for feature, weight forfull activity object based on the database used, weight for insertion, deletion
	 * and replacement of activity object, and cost of insertion, deletion and replacement of activity object
	 */
	public void setWeightsAndCosts()
	{
		System.out.println("setWeightsAndCosts()");
		wtActivityName = 2d;// 3d;//gowalla
		wtStartTime = 1d;// 0.8d;// 0.6d;//gowalla

		wtDuration = 0.5d;// 0.2d;
		wtDistanceTravelled = 3d;// //1d;
		wtStartGeo = 0.3d;
		wtEndGeo = 0.3d;
		wtAvgAltitude = 0.2d;

		wtLocation = 1d;// gowalla
		wtLocPopularity = 1d;// gowalla

		wtDistanceFromPrev = 1d;// gowalla
		wtDurationFromPrev = 1d;// gowalla

		wtDistanceFromNext = 1d;
		wtDurationFromNext = 1d;

		switch (Constant.getDatabaseName())
		{
		case "geolife1":
			wtFullActivityObject = StatsUtils.round(wtActivityName + wtStartTime + wtDuration + wtDistanceTravelled
					+ wtStartGeo + wtEndGeo + +wtAvgAltitude, 4);
			break;
		case "dcu_data_2":
			wtFullActivityObject = StatsUtils.round(wtActivityName + wtStartTime + wtDuration, 4);
			break;

		case "gowalla1":
			// Switch_23Feb TODO
			// $$wtFullActivityObject = StatsUtils.round(wtActivityName + wtStartTime + wtLocation +
			// wtLocPopularity, 4);
			wtFullActivityObject = 0;
			EnumMap<GowallaFeatures, Double> featureWeightMap = new EnumMap<>(GowallaFeatures.class);

			if (this.useActivityNameInFED)
			{
				wtFullActivityObject += wtActivityName;
				featureWeightMap.put(GowallaFeatures.ActNameF, wtActivityName);
			}
			if (this.useStartTimeInFED)
			{
				wtFullActivityObject += wtStartTime;
				featureWeightMap.put(GowallaFeatures.StartTimeF, wtStartTime);
			}
			if (this.useLocationInFED)
			{
				wtFullActivityObject += wtLocation;
				featureWeightMap.put(GowallaFeatures.LocationF, wtLocation);
			}

			if (this.usePopularityInFED)
			{
				wtFullActivityObject += wtLocPopularity;
				featureWeightMap.put(GowallaFeatures.PopularityF, wtLocPopularity);
			}

			if (this.useDistFromPrevInFED)
			{
				wtFullActivityObject += wtDistanceFromPrev;
				featureWeightMap.put(GowallaFeatures.DistFromPrevF, wtDistanceFromPrev);
			}

			if (this.useDurationFromPrevInFED)
			{
				wtFullActivityObject += wtDurationFromPrev;
				featureWeightMap.put(GowallaFeatures.DurationFromPrevF, wtDurationFromPrev);
			}

			if (this.useDistFromNextInFED)
			{
				wtFullActivityObject += wtDistanceFromNext;
			}

			if (this.useDurationFromNextInFED)
			{
				wtFullActivityObject += wtDurationFromNext;
			}
			wtFullActivityObject = StatsUtils.round(wtFullActivityObject, 4);
			// wtActivityName/* + wtStartTime + wtLocation + wtLocPopularity */ + wtDistanceFromPrev +
			// wtDurationFromPrev,4);
			this.featureWeightMap = featureWeightMap;

			break;

		default:
			System.err.println(PopUps.getTracedErrorMsg(
					"Error in org.activity.distances.AlignmentBasedDistance.setWtOfFullActivityObject(): unrecognised database name:"
							+ Constant.getDatabaseName()));
			// PopUps.showError(
			// "Error in org.activity.distances.AlignmentBasedDistance.setWtOfFullActivityObject(): unrecognised
			// database name:"
			// + Constant.getDatabaseName());
			break;
		}

		costInsertActivityObject = defaultCostInsert * wtFullActivityObject; // 1 cost of insert operation 4.5
		costDeleteActivityObject = defaultCostDelete * wtFullActivityObject; // 1 cost of delete operation 4.5
		costReplaceActivityObject = defaultCostReplace * wtFullActivityObject; // 2 cost of replace operation 9
	}

	/**
	 * Note that wtFullActivityObject will vary according to which features are used (set to be used by
	 * use_FeatureX_InFED). Hence the return value is regulated by which features are set to be used.
	 *
	 * @return
	 * @since April 10 2018
	 */
	public double getSumOfWeightOfFeaturesExceptPrimaryDimension()
	{
		if (primaryDimension.equals(PrimaryDimension.ActivityID))
		{
			return wtFullActivityObject - wtActivityName;
		}
		else if (primaryDimension.equals(PrimaryDimension.LocationID))
		{
			return wtFullActivityObject - wtLocation;
		}
		else if (primaryDimension.equals(PrimaryDimension.LocationGridID))
		{
			return wtFullActivityObject - wtLocation;
		}
		else
		{
			PopUps.showError("Error: unrecognised primary dimension =" + primaryDimension);
			return -1;
		}
	}

	// /**
	// * Note that wtFullActivityObject will vary according to which features are used (set to be used by
	// * use_FeatureX_InFED). Hence the return value is regulated by which features are set to be used.
	// *
	// * @return
	// * @since 17 July 2018
	// */
	// public double getSumOfWeightOfFeaturesExceptGivenDimension(PrimaryDimension givenDimension)
	// {
	// if (givenDimension.equals(PrimaryDimension.ActivityID))
	// {
	// return wtFullActivityObject - wtActivityName;
	// }
	// else if (givenDimension.equals(PrimaryDimension.LocationID))
	// {
	// return wtFullActivityObject - wtLocation;
	// }
	// else
	// {
	// PopUps.showError("Error: unrecognised primary dimension =" + givenDimension);
	// return -1;
	// }
	// }

	/**
	 * 
	 * @return
	 */
	public String getAllWeightsOfFeatures()
	{
		String allWts = "\nWt of Activity Name:" + wtActivityName + "\nWt of Start Time:" + wtStartTime
				+ "\nWt of Duration:" + wtDuration + "\nWt of Distance Travelled:" + wtDistanceTravelled
				+ "\nWt of Start Geo Location:" + wtStartGeo + "\nWt of End Geo Location:" + wtEndGeo
				+ "\nWt of Avg Altitude:" + wtAvgAltitude + "\nWt of Location:" + wtLocation + "\nWt of Popularity:"
				+ wtLocPopularity + "\nwtDistanceFromPrev" + wtDistanceFromPrev + "\nwtDurationFromPrev:"
				+ wtDurationFromPrev + "\nwtDistanceFromNext" + wtDistanceFromNext + "\nwtDurationFromNext:"
				+ wtDurationFromNext + "\nWt of full Activity Object:" + wtFullActivityObject
				+ "\ncostInsertActivityObject:" + costInsertActivityObject + "\ncostDeleteActivityObject:"
				+ costDeleteActivityObject + "\ncostReplaceActivityObject:" + costReplaceActivityObject;
		return allWts;
	}

	/**
	 * 
	 * @param i
	 * @param d
	 * @param r
	 */
	public void setWtsInsertDeleteReplace(double i, double d, double r)
	{
		this.costInsertActivityObject = i;
		this.costDeleteActivityObject = d;
		this.costReplaceActivityObject = r;
	}

	/**
	 * 
	 */
	public void setTolerancesToZero()
	{
		// System.out.println("Alert: setting all tolerance to zero");
		this.startTimeToleranceInSeconds = 0;
		this.durationToleranceInSeconds = 0;
		this.distanceTravelledTolerance = 0;
		this.startGeoTolerance = 0;
		this.endGeoTolerance = 0;
		this.avgAltTolerance = 0;
	}

	public void setTolerancesToDefault()
	{
		System.out.println("Setting default tolerances");

		if (Constant.getDatabaseName().equals("geolife1"))
		{
			startTimeToleranceInSeconds = 120; // in seconds
			durationToleranceInSeconds = 120; // in seconds
			distanceTravelledTolerance = 0.2d; // Kilometers, as our Haversine distance calculation uses this unit
			startGeoTolerance = 0.2d;
			endGeoTolerance = 0.2d;
			avgAltTolerance = 5d;// feets since raw data is in this unit
		}

		if (Constant.getDatabaseName().equals("gowalla1"))
		{
			startTimeToleranceInSeconds = 3600; // in seconds
			durationToleranceInSeconds = 3600; // //actually not needed , in seconds
			distanceTravelledTolerance = 0.2d; // Kilometers, as our Haversine distance calculation uses this unit
			startGeoTolerance = 0.5d; // actually not needed
			endGeoTolerance = 0.5d;// actually not needed
			avgAltTolerance = 5d;// //actually not needed, feets since raw data is in this unit
			locationPopularityTolerance = 0.2d;

			// Start of disabled on Mar 222 2018
			// distanceFromPrevTolerance = 1200;// meters as data in that unit.
			// durationFromPrevTolerance = 130;// sec
			// End of disabled on Mar 222 2018

			// Disabled on Mar 25 2018
			// distanceFromPrevTolerance = 200;// meters as data in that unit.
			// durationFromPrevTolerance = 130;// sec

			// added on Mar 25 2018
			distanceFromPrevTolerance = 0;// 200;// meters as data in that unit.
			durationFromPrevTolerance = 0;// 130;// sec
		}
	}

	/**
	 * Return the Edit Distance of given sequence of Activity Objects with an empty sequence of Activity Objects, thus
	 * this is the maximal edit distance possible for the given sequence. (in current case of implementation.)
	 * 
	 * 
	 * @param activityObjects1
	 *            the given sequence of Activity Objects
	 * @return Edit Distance of given sequence of Activity Objects with an empty sequence of Activity Objects
	 */
	public final double maxEditDistance(ArrayList<ActivityObject> activityObjects1)
	{
		return wtFullActivityObject * activityObjects1.size();
	}

	/**
	 * Returns case based similarity (score) between the two given activity objects (rounded to 4 decimal places).
	 * 
	 * @param activityObject1
	 * @param activityObject2
	 * @param userID
	 *            user for which recommendation is being done
	 * @return value should be between 0 and 1
	 */
	public final double getCaseBasedV1SimilarityGeolifeData(ActivityObject activityObject1,
			ActivityObject activityObject2, int userID)
	{
		if (VerbosityConstants.verbose)
		{
			System.out.println("Inside getCaseBasedV1Similarity \n ActivityObject1:" + activityObject1.getActivityName()
					+ "__" + activityObject1.getStartTimestamp() + "__" + activityObject1.getDurationInSeconds() + "__"
					+ activityObject1.getDistanceTravelled() + "__" + activityObject1.getStartLatitude() + ","
					+ activityObject1.getStartLongitude() + "__" + activityObject1.getEndLatitude() + ","
					+ activityObject1.getEndLongitude() + "__" + activityObject1.getAvgAltitude()

					+ "\n ActivityObject2:" + activityObject2.getActivityName() + "__"
					+ activityObject2.getStartTimestamp() + "__" + activityObject2.getDurationInSeconds() + "__"
					+ activityObject2.getDistanceTravelled() + "__" + activityObject2.getStartLatitude() + ","
					+ activityObject2.getStartLongitude() + "__" + activityObject2.getEndLatitude() + ","
					+ activityObject2.getEndLongitude() + "__" + activityObject2.getAvgAltitude());
		}
		// ////////////////////////

		int ui = UtilityBelt.getIndexOfUserID(userID);
		double startTimeSimComponent = 0, durationSimComponent = 0, distanceTravelledSimComponent = 0,
				startGeoSimComponent = 0, endGeoSimComponent = 0, avgAltSimComponent = 0;

		// /////////////////////////////////////////////////////////////////////
		double startTimeAct1InSecs = activityObject1.getStartTimestamp().getHours() * 60 * 60
				+ (activityObject1.getStartTimestamp().getMinutes()) * 60
				+ (activityObject1.getStartTimestamp().getSeconds());
		double startTimeAct2InSecs = activityObject2.getStartTimestamp().getHours() * 60 * 60
				+ (activityObject2.getStartTimestamp().getMinutes()) * 60
				+ (activityObject2.getStartTimestamp().getSeconds());

		startTimeSimComponent = getCaseSimilarityComponent(startTimeAct1InSecs, startTimeAct2InSecs,
				startTimeToleranceInSeconds, "StartTime");

		durationSimComponent = getCaseSimilarityComponent(activityObject1.getDurationInSeconds(),
				activityObject2.getDurationInSeconds(), durationToleranceInSeconds, "Duration");// (1 -
																								// absDifferenceOfDuration
																								// /
																								// maxOfDurationInSeconds);
		distanceTravelledSimComponent = getCaseSimilarityComponent(activityObject1.getDistanceTravelled(),
				activityObject2.getDistanceTravelled(), distanceTravelledTolerance, "DistanceTravelled");

		startGeoSimComponent = getCaseSimilarityComponentForGeoLocation(activityObject1.getStartLatitude(),
				activityObject1.getStartLongitude(), activityObject2.getStartLatitude(),
				activityObject2.getStartLongitude(), startGeoTolerance, "StartGeo", ui);

		endGeoSimComponent = getCaseSimilarityComponentForGeoLocation(activityObject1.getEndLatitude(),
				activityObject1.getEndLongitude(), activityObject2.getEndLatitude(), activityObject2.getEndLongitude(),
				endGeoTolerance, "EndGeo", ui);

		if (Double.parseDouble(activityObject1.getAvgAltitude()) <= 0
				|| Double.parseDouble(activityObject1.getAvgAltitude()) <= 0)
		{
			avgAltSimComponent = 1;
		}
		else
		{
			avgAltSimComponent = getCaseSimilarityComponent(Double.parseDouble(activityObject1.getAvgAltitude()),
					Double.parseDouble(activityObject2.getAvgAltitude()), avgAltTolerance, "AvgAltitude");
		}

		double weightedSum = wtStartTime * startTimeSimComponent + wtDuration * durationSimComponent
				+ wtDistanceTravelled * distanceTravelledSimComponent + wtStartGeo * startGeoSimComponent
				+ wtEndGeo * endGeoSimComponent + wtAvgAltitude * avgAltSimComponent;

		double sumOfWeights = wtStartTime + wtDuration + wtDistanceTravelled + wtStartGeo + wtEndGeo + wtAvgAltitude;

		double result = weightedSum / sumOfWeights;

		if (result < 0)
		{
			System.err.println("Error: Case similarity is negative");
		}

		return StatsUtils.round(result, 4);
	}

	/**
	 * Returns case based similarity (score) between the two given activity objects for the DCU_dataset (rounded to 4
	 * decimal places)
	 * 
	 * @param activityObject1
	 * @param activityObject2
	 * @param userID
	 *            user for which recommendation is being done
	 * @return (between 0 and 1)
	 */
	public final double getCaseBasedV1SimilarityDCUData(ActivityObject activityObject1, ActivityObject activityObject2,
			int userID)
	{
		if (VerbosityConstants.verbose)
		{
			System.out.println("Inside getCaseBasedV1Similarity \n ActivityObject1:" + activityObject1.getActivityName()
					+ "__" + activityObject1.getStartTimestamp() + "__" + activityObject1.getDurationInSeconds()
					+ "\n ActivityObject2:" + activityObject2.getActivityName() + "__"
					+ activityObject2.getStartTimestamp() + "__" + activityObject2.getDurationInSeconds());
		}
		// ////////////////////////

		int ui = UtilityBelt.getIndexOfUserID(userID);
		double startTimeSimComponent = 0, durationSimComponent = 0;

		// /////////////////////////////////////////////////////////////////////
		double startTimeAct1InSecs = activityObject1.getStartTimestamp().getHours() * 60 * 60
				+ (activityObject1.getStartTimestamp().getMinutes()) * 60
				+ (activityObject1.getStartTimestamp().getSeconds());
		double startTimeAct2InSecs = activityObject2.getStartTimestamp().getHours() * 60 * 60
				+ (activityObject2.getStartTimestamp().getMinutes()) * 60
				+ (activityObject2.getStartTimestamp().getSeconds());

		startTimeSimComponent = getCaseSimilarityComponent(startTimeAct1InSecs, startTimeAct2InSecs,
				startTimeToleranceInSeconds, "StartTime");

		durationSimComponent = getCaseSimilarityComponent(activityObject1.getDurationInSeconds(),
				activityObject2.getDurationInSeconds(), durationToleranceInSeconds, "Duration");// (1 -
																								// absDifferenceOfDuration
																								// /
																								// maxOfDurationInSeconds);
		double weightedSum = wtStartTime * startTimeSimComponent + wtDuration * durationSimComponent;

		double sumOfWeights = wtStartTime + wtDuration;

		double result = weightedSum / sumOfWeights;

		if (result < 0)
		{
			System.err.println("Error: Case similarity is negative");
		}

		return StatsUtils.round(result, 4);
	}

	/**
	 * Return the feature level distance between the two given Activity Objects. (note: this is NOT case-based) (note:
	 * DCU data has only two features while Geolife Data has 4 additional features. Alert: this method needs to be
	 * modified for different datasets.
	 * <p>
	 * TODO To make it generic, store the feature names in a data structure at the start of experiments.) NOT GENERIC
	 * 
	 * this is a fork of HJEditDistance.getFeatureLevelDistance()
	 * 
	 * @param ao1
	 * @param ao2
	 * @return
	 */
	public double getFeatureLevelDistance(ActivityObject ao1, ActivityObject ao2)
	{
		double dfeat = 0;
		// if(ao1.getStartTimestamp().getTime() != (ao2.getStartTimestamp().getTime()) )//is wrong since its comparing
		// timestamps and not time of days...however, results for our
		// experiments do not show any visible difference in results { dfeat+=costReplaceStartTime; }
		if (Constant.getDatabaseName().equals("gowalla1"))// (Constant.DATABASE_NAME.equals("geolife1"))
		{
			// $$dfeat = getFeatureLevelDistanceGowallaPD(ao1, ao2);//disabled on Feb 23 2018
			dfeat = getFeatureLevelDistanceGowallaPD25Feb2018(ao1, ao2);
			//// Sanity Checked pass OK 26 Feb 2018 Start
			// double dfeatTest = getFeatureLevelDistanceGowallaPD23Feb2018(ao1, ao2);
			// boolean sanityCheckPassed = Sanity.eq(dfeat, dfeatTest,
			// "Sanity Check Failed Error:'ndfeat= " + dfeat + " dfeatTest=" + dfeatTest);
			// WritingToFile.appendLineToFileAbsolute(sanityCheckPassed + "\n",
			// Constant.getCommonPath() + "SanityCheck25FebgetFeatureLevelDistanceGowallaPD25Feb2018.txt");
			// Sanity Checked pass OK 26 Feb 2018 End
		}
		else
		{
			if (DateTimeUtils.isSameTimeInTolerance(ao1.getStartTimestamp(), ao2.getStartTimestamp(),
					startTimeToleranceInSeconds) == false)
			{
				dfeat += wtStartTime;
			}

			if (Math.abs(ao1.getDurationInSeconds() - ao2.getDurationInSeconds()) > durationToleranceInSeconds)
			{
				dfeat += wtDuration;
			}

			if (Constant.getDatabaseName().equals("geolife1"))// (Constant.DATABASE_NAME.equals("geolife1"))
			{
				if (Math.abs(ao1.getDistanceTravelled() - ao2.getDistanceTravelled()) > distanceTravelledTolerance)
				{
					dfeat += wtDistanceTravelled;
				}
				if (Math.abs(ao1.getDifferenceStartingGeoCoordinates(ao2)) > startGeoTolerance)
				{
					dfeat += wtStartGeo;
				}
				if (Math.abs(ao1.getDifferenceEndingGeoCoordinates(ao2)) > endGeoTolerance)
				{
					dfeat += wtEndGeo;
				}
				if (Math.abs(Double.parseDouble(ao1.getAvgAltitude())
						- Double.parseDouble((ao2.getAvgAltitude()))) > avgAltTolerance)
				{
					dfeat += wtAvgAltitude;
				}
			}
		}
		return dfeat;
	}

	/**
	 * Selects the right feature level difference method to call for the current database.
	 * <p>
	 * Return the feature level differences between the two given Activity Objects. (note: this is NOT case-based)
	 * (note: DCU data has only two features while Geolife Data has 4 additional features. Alert: this method needs to
	 * be modified for different datasets.
	 * <p>
	 * TODO To make it generic, store the feature names in a data structure at the start of experiments.) NOT GENERIC
	 * 
	 * 
	 * @param ao1
	 * @param ao2
	 * @return EnumMap{GowallaFeatures, Double}, map of Gowalla features and corresonding feature's difference between
	 *         the two compared act objs ao1 and ao2
	 * @since April 14 2018
	 */
	public EnumMap<GowallaFeatures, Double> getFeatureLevelDifference(ActivityObject ao1, ActivityObject ao2)
	{
		double dfeat = 0;
		EnumMap<GowallaFeatures, Double> featureDiffs = null;
		// if(ao1.getStartTimestamp().getTime() != (ao2.getStartTimestamp().getTime()) )//is wrong since its comparing
		// timestamps and not time of days...however, results for our
		// experiments do not show any visible difference in results { dfeat+=costReplaceStartTime; }
		if (Constant.getDatabaseName().equals("gowalla1"))// (Constant.DATABASE_NAME.equals("geolife1"))
		{
			// $$dfeat = getFeatureLevelDistanceGowallaPD(ao1, ao2);//disabled on Feb 23 2018
			// dfeat = getFeatureLevelDistanceGowallaPD25Feb2018(ao1, ao2);
			featureDiffs = getFeatureLevelDifferenceGowallaPD13Apr2018(ao1, ao2);
			//// Sanity Checked pass OK 26 Feb 2018 Start
			// double dfeatTest = getFeatureLevelDistanceGowallaPD23Feb2018(ao1, ao2);
			// boolean sanityCheckPassed = Sanity.eq(dfeat, dfeatTest,
			// "Sanity Check Failed Error:'ndfeat= " + dfeat + " dfeatTest=" + dfeatTest);
			// WritingToFile.appendLineToFileAbsolute(sanityCheckPassed + "\n",
			// Constant.getCommonPath() + "SanityCheck25FebgetFeatureLevelDistanceGowallaPD25Feb2018.txt");
			// Sanity Checked pass OK 26 Feb 2018 End
		}
		else
		{
			PopUps.showError("Error: AlignmentBasedDistance.getFeatureLevelDifference() NOT IMPLEMENTED for database: "
					+ Constant.getDatabaseName());
		}
		return featureDiffs;
	}

	/**
	 * Used until Feb 23 2018
	 * 
	 * @param ao1
	 * @param ao2
	 * @return
	 */
	public double getFeatureLevelDistanceGowallaPD(ActivityObject ao1, ActivityObject ao2)
	{
		double dfeat = 0;
		StringBuilder sbLog = new StringBuilder();
		// if(ao1.getStartTimestamp().getTime() != (ao2.getStartTimestamp().getTime()) )//is wrong since its comparing
		// timestamps and not time of days...however, results for our
		// experiments do not show any visible difference in results { dfeat+=costReplaceStartTime; }
		if (Constant.getDatabaseName().equals("gowalla1"))// (Constant.DATABASE_NAME.equals("geolife1"))
		{
			// $$ curtain on 2 Mar 2017 start
			if (Constant.editDistTimeDistType.equals(Enums.EditDistanceTimeDistanceType.BinaryThreshold))
			{
				if (DateTimeUtils.isSameTimeInTolerance(ao1.getStartTimestamp(), ao2.getStartTimestamp(),
						startTimeToleranceInSeconds) == false) // if not same within 60mins then add wt to dfeat
				{
					dfeat += wtStartTime;
					sbLog.append("\ndtime=" + wtStartTime);
				}
			}
			// $$ curtain on 2 Mar 2017 end

			// $$ added on 2nd march 2017 start: nearerScaledTimeDistance
			else if (Constant.editDistTimeDistType.equals(Enums.EditDistanceTimeDistanceType.NearerScaled))
			{
				long absTimeDiffInSeconds = DateTimeUtils.getTimeDiffInSeconds(ao1.getStartTimestamp(),
						ao2.getStartTimestamp());
				if (absTimeDiffInSeconds <= startTimeToleranceInSeconds)
				{
					double timeDistance = absTimeDiffInSeconds / startTimeToleranceInSeconds;
					dfeat += (timeDistance * wtStartTime);
					sbLog.append("\ndtime=" + (timeDistance * wtStartTime));
				}
				else // absTimeDiffInSeconds > startTimeToleranceInSeconds
				{
					dfeat += (1.0 * wtStartTime);
					sbLog.append("\ndtime=" + (1.0 * wtStartTime));
				}
			}
			// $$ added on 2nd march 2017 end

			// $$ added on 3rd march 2017 start: furtherScaledTimeDistance
			// cost = 0 if diff <=1hr , cost (0,1) if diff in (1,3) hrs and cost =1 if diff >=3hrs
			else if (Constant.editDistTimeDistType.equals(Enums.EditDistanceTimeDistanceType.FurtherScaled))
			{
				long absTimeDiffInSeconds = DateTimeUtils.getTimeDiffInSeconds(ao1.getStartTimestamp(),
						ao2.getStartTimestamp());
				if (absTimeDiffInSeconds > startTimeToleranceInSeconds)
				{
					double timeDistance = absTimeDiffInSeconds / 10800;
					dfeat += (timeDistance * wtStartTime);
					sbLog.append("\ndtime=" + (timeDistance * wtStartTime));
				}
			}
			// $$ added on 3rd march 2017 end

			// System.out.println("@@ ao1.getLocationIDs() = " + ao1.getLocationIDs());
			// System.out.println("@@ ao2.getLocationIDs() = " + ao2.getLocationIDs());
			// System.out.println("@@ UtilityBelt.getIntersection(ao1.getLocationIDs(), ao2.getLocationIDs()).size() = "
			// + UtilityBelt.getIntersection(ao1.getLocationIDs(), ao2.getLocationIDs()).size());

			if (primaryDimension.equals(PrimaryDimension.ActivityID))
			{
				if (UtilityBelt.getIntersection(ao1.getUniqueLocationIDs(), ao2.getUniqueLocationIDs()).size() == 0)
				// ao1.getLocationIDs() != ao2.getLocationIDs()) // if no matching locationIDs then add wt to dfeat
				{
					dfeat += wtLocation;
					sbLog.append("\ndLoc=" + (wtLocation));
				}
			}
			else if (primaryDimension.equals(PrimaryDimension.LocationID))
			{
				if (ao1.getActivityID() == ao2.getActivityID())
				{
					dfeat += wtActivityName;
					sbLog.append("\ndActName" + (wtActivityName));
				}
			}

			double c1 = ao1.getCheckins_count();
			double c2 = ao2.getCheckins_count();
			double popularityDistance = (Math.abs(c1 - c2) / Math.max(c1, c2));
			/// 1 - (Math.abs(c1 - c2) / Math.max(c1, c2));

			// add more weight if they are more different, popDistance should be higher if they are more different
			dfeat += popularityDistance * this.wtLocPopularity;
			sbLog.append("\nao1.getCheckins_count()=" + c1 + "\nao2.getCheckins_count()=" + c2);
			sbLog.append("\ndPop=" + (popularityDistance * this.wtLocPopularity));
		}
		else
		{
			PopUps.printTracedErrorMsgWithExit(
					"Error: getFeatureLevelDistanceGowallaPD() called for database: " + Constant.getDatabaseName());
		}

		if (dfeat > 100)
		{
			System.out.println("Inside: dfeat= " + dfeat + " \nlog:\n" + sbLog.toString());
		}

		return dfeat;
	}

	/**
	 * 
	 * @param num
	 * @param base
	 * @return
	 */
	public static double fastLogOfBase(double num, double base)
	{
		return FastMath.log(num) / FastMath.log(base);
	}

	/**
	 * 
	 * @param ao1
	 * @param ao2
	 * @return
	 */
	public double getFeatureLevelDistanceGowallaPD23Feb2018Bare(ActivityObject ao1, ActivityObject ao2)
	{
		double dfeat = 0;
		if (Constant.getDatabaseName().equals("gowalla1"))// (Constant.DATABASE_NAME.equals("geolife1"))
		{
			{
				double diffOfDistFromPrev = FastMath.abs(ao1.getDistanceInMFromPrev() - ao2.getDistanceInMFromPrev());
				if (diffOfDistFromPrev > 46754)
				{
					dfeat += this.wtDistanceFromPrev;
				}
				else if (diffOfDistFromPrev > this.distanceFromPrevTolerance)
				{
					double val = fastLogOfBase(diffOfDistFromPrev, 46754) * this.wtDistanceFromPrev;
					dfeat += val;
				}
			}
			{
				double diffOfDurFromPrev = FastMath
						.abs(ao1.getDurationInSecondsFromPrev() - ao2.getDurationInSecondsFromPrev());
				if (diffOfDurFromPrev > 63092)
				{
					dfeat += this.wtDurationFromPrev;
				}
				else if (diffOfDurFromPrev > this.durationFromPrevTolerance)
				{
					double val = fastLogOfBase(diffOfDurFromPrev, 63092) * this.wtDurationFromPrev;
					dfeat += val;
				}
			}
		}
		else
		{
			PopUps.printTracedErrorMsgWithExit(
					"Error: getFeatureLevelDistanceGowallaPD() called for database: " + Constant.getDatabaseName());
		}

		if (dfeat > 100)
		{
			PopUps.printTracedErrorMsg("Inside: dfeat= " + dfeat);
		}

		return dfeat;
	}

	/**
	 * 
	 * @param ao1
	 * @param ao2
	 * @return
	 */
	public double getFeatureLevelDistanceGowallaPD23Feb2018(ActivityObject ao1, ActivityObject ao2)
	{
		double dfeat = 0;// , dStartTime = 0, dLocation = 0, dPopularity = 0,
		double dDistanceFromPrev = 0, dDurationFromPrev = 0;

		// boolean useStartTimeInFED = false, useLocationInFED = false, usePopularityInFED = false,
		// useDistFromPrevInFED = true, useDurationFromPrevInFED = true; TODO USE THESE AS SWITCHES, SET THEM IN
		// CONSTRUCTOR AND MAKE THEM CLASS VARIABLES

		StringBuilder sbLog = new StringBuilder();
		// if(ao1.getStartTimestamp().getTime() != (ao2.getStartTimestamp().getTime()) )//is wrong since its comparing
		// timestamps and not time of days...however, results for our
		// experiments do not show any visible difference in results { dfeat+=costReplaceStartTime; }
		if (Constant.getDatabaseName().equals("gowalla1"))// (Constant.DATABASE_NAME.equals("geolife1"))
		{
			if (false)// Switch_feb23 TODO
			// $$ curtain on 2 Mar 2017 start
			{
				Pair<Double, String> stDistRes = getStartTimeDistance(ao1.getStartTimestamp(), ao2.getStartTimestamp(),
						Constant.editDistTimeDistType, startTimeToleranceInSeconds, wtStartTime);
				dfeat += stDistRes.getFirst();
				sbLog.append("\ndST=" + stDistRes.getSecond());
				// $$ added on 3rd march 2017 end

				// System.out.println("@@ ao1.getLocationIDs() = " + ao1.getLocationIDs());
				// System.out.println("@@ ao2.getLocationIDs() = " + ao2.getLocationIDs());
				// System.out.println("@@ UtilityBelt.getIntersection(ao1.getLocationIDs(), ao2.getLocationIDs()).size()
				// = "
				// + UtilityBelt.getIntersection(ao1.getLocationIDs(), ao2.getLocationIDs()).size());

				if (primaryDimension.equals(PrimaryDimension.ActivityID))
				{
					if (UtilityBelt.getIntersection(ao1.getUniqueLocationIDs(), ao2.getUniqueLocationIDs()).size() == 0)
					// ao1.getLocationIDs() != ao2.getLocationIDs()) // if no matching locationIDs then add wt to dfeat
					{
						dfeat += wtLocation;
						sbLog.append("\ndLoc=" + (wtLocation));
					}
				}
				else if (primaryDimension.equals(PrimaryDimension.LocationID))
				{
					if (ao1.getActivityID() == ao2.getActivityID())
					{
						dfeat += wtActivityName;
						sbLog.append("\ndActName" + (wtActivityName));
					}
				}

				double c1 = ao1.getCheckins_count();
				double c2 = ao2.getCheckins_count();
				double popularityDistance = (Math.abs(c1 - c2) / Math.max(c1, c2));
				/// 1 - (Math.abs(c1 - c2) / Math.max(c1, c2));

				// add more weight if they are more different, popDistance should be higher if they are more different
				dfeat += popularityDistance * this.wtLocPopularity;
				sbLog.append("\nao1.getCheckins_count()=" + c1 + "\nao2.getCheckins_count()=" + c2);
				sbLog.append("\ndPop=" + (popularityDistance * this.wtLocPopularity));
			}

			double diffOfDistFromPrev = FastMath.abs(ao1.getDistanceInMFromPrev() - ao2.getDistanceInMFromPrev());
			if (diffOfDistFromPrev > 46754)
			{
				dDistanceFromPrev = this.wtDistanceFromPrev;
				sbLog.append("\tdDistanceFromPrev (more than thresh):" + (dDistanceFromPrev));
			}
			else if (diffOfDistFromPrev > this.distanceFromPrevTolerance)
			{
				double val = fastLogOfBase(diffOfDistFromPrev, 46754) * this.wtDistanceFromPrev;
				dDistanceFromPrev = val;
				sbLog.append("\tdDistanceFromPrev (log scaled):" + dDistanceFromPrev);
			}

			double diffOfDurFromPrev = FastMath
					.abs(ao1.getDurationInSecondsFromPrev() - ao2.getDurationInSecondsFromPrev());
			if (diffOfDurFromPrev > 63092)
			{
				dDurationFromPrev = this.wtDurationFromPrev;
				sbLog.append("\tdDurationFromPrev (more than thresh):" + (dDurationFromPrev));
			}
			else if (diffOfDurFromPrev > this.durationFromPrevTolerance)
			{
				double val = fastLogOfBase(diffOfDurFromPrev, 63092) * this.wtDurationFromPrev;
				dDurationFromPrev = val;
				sbLog.append("\tdDurationFromPrev (log scaled):" + dDurationFromPrev);
			}
			dfeat = dDistanceFromPrev + dDurationFromPrev;
		}
		else
		{
			PopUps.printTracedErrorMsgWithExit(
					"Error: getFeatureLevelDistanceGowallaPD() called for database: " + Constant.getDatabaseName());
		}

		if (dfeat > 100)
		{
			System.out.println("Inside: dfeat= " + dfeat + " \nlog:\n" + sbLog.toString());
		}

		if (Constant.debugFeb24_2018)
		{
			System.out.println("In dfeat: " + dfeat + " \tlog:\t" + sbLog.toString());
			// WritingToFile.appendLineToFileAbsolute("\ndfeat:" + dfeat + " \tlog:\t" + sbLog.toString(),
			// Constant.getCommonPath() + "FeatureLevelDistanceLog.csv");
			WToFile.appendLineToFileAbs(dfeat + "," + dDistanceFromPrev + "," + dDurationFromPrev,
					Constant.getCommonPath() + "FeatureLevelDistanceLog.csv");

		}

		return dfeat;
	}

	/**
	 * 
	 * @param ao1STInms
	 * @param ao2STInms
	 * @param zone1
	 * @param zone2
	 * @param editDistTimeDistType
	 * @param startTimeToleranceInSeconds
	 * @param wtStartTime
	 * @return
	 * @since 27 Feb 2018 extracted from the method getFeatureLevelDistanceGowallaPD23Feb2018()
	 */
	public static Pair<Double, String> getStartTimeDistanceZoned(long ao1STInms, long ao2STInms, ZoneId zone1,
			ZoneId zone2, Enums.EditDistanceTimeDistanceType editDistTimeDistType, long startTimeToleranceInSeconds,
			double wtStartTime)
	{
		if (zone1 == null || zone1 == null)
		{
			PopUps.showError("Null zoneID " + zone1 + " or " + zone2);
			return new Pair<>(wtStartTime, "Nullzone dtime=" + wtStartTime);
		}
		long absTimeDiffInSeconds = DateTimeUtils.getTimeDiffInSecondsZoned(ao1STInms, ao2STInms, zone1, zone2);

		if (editDistTimeDistType.equals(Enums.EditDistanceTimeDistanceType.BinaryThreshold))
		{
			// if not same within 60mins then add wt to dfeat
			// if (DateTimeUtils.isSameTimeInToleranceZoned(ao1STInms, ao2STInms, zone1, zone2,
			// startTimeToleranceInSeconds) == false)
			if (absTimeDiffInSeconds > startTimeToleranceInSeconds)
			{
				return new Pair<>(1.0 * wtStartTime, "\ndtime=" + wtStartTime);
			}
			else
			{
				return new Pair<>(0.0, "\ndtime=0");
			}
		}
		// $$ curtain on 2 Mar 2017 end

		// $$ added on 2nd march 2017 start: nearerScaledTimeDistance
		else if (Constant.editDistTimeDistType.equals(Enums.EditDistanceTimeDistanceType.NearerScaled))
		{
			if (absTimeDiffInSeconds <= startTimeToleranceInSeconds)
			{
				double timeDistance = absTimeDiffInSeconds / startTimeToleranceInSeconds;
				return new Pair<>((timeDistance * wtStartTime), "\ndtime=" + (timeDistance * wtStartTime));
			}
			else // absTimeDiffInSeconds > startTimeToleranceInSeconds
			{
				return new Pair<>(1.0 * wtStartTime, "\ndtime=" + (1.0 * wtStartTime));
			}
		}
		// $$ added on 2nd march 2017 end

		// $$ added on 3rd march 2017 start: furtherScaledTimeDistance
		// cost = 0 if diff <=1hr , cost (0,1) if diff in (1,3) hrs and cost =1 if diff >=3hrs
		else if (Constant.editDistTimeDistType.equals(Enums.EditDistanceTimeDistanceType.FurtherScaled))
		{
			if (absTimeDiffInSeconds > startTimeToleranceInSeconds)
			{
				double timeDistance = absTimeDiffInSeconds / 10800;
				return new Pair<>((timeDistance * wtStartTime), "\ndtime=" + (timeDistance * wtStartTime));
			}
		}
		return null;
	}

	/**
	 * Fork of getStartTimeDistanceZoned()-- improved code
	 * 
	 * @param ao1STInms
	 * @param ao2STInms
	 * @param zone1
	 * @param zone2
	 * @param editDistTimeDistType
	 * @param startTimeToleranceInSeconds
	 * @param wtStartTime
	 * @return Pair{StartTime_Distance,Log}
	 * @since 13 April 2018 extracted from the method getFeatureLevelDistanceGowallaPD23Feb2018()
	 */
	public static Pair<Double, String> getStartTimeDistanceZoned13Apr2018(long ao1STInms, long ao2STInms, ZoneId zone1,
			ZoneId zone2, Enums.EditDistanceTimeDistanceType editDistTimeDistType, long startTimeToleranceInSeconds,
			double wtStartTime)
	{
		if (zone1 == null || zone1 == null)
		{
			PopUps.showError("Error Null zoneID " + zone1 + " or " + zone2);
			return new Pair<>(wtStartTime, "Nullzone dtime=" + wtStartTime);
		}

		long absTimeDiffInSeconds = DateTimeUtils.getTimeDiffInSecondsZoned(ao1STInms, ao2STInms, zone1, zone2);
		double startTimeDistance = -9999;

		if (editDistTimeDistType.equals(Enums.EditDistanceTimeDistanceType.BinaryThreshold))
		{ // if not same within 60mins then add wt to dfeat
			if (absTimeDiffInSeconds > startTimeToleranceInSeconds)
			{
				startTimeDistance = 1.0;
			}
			else
			{
				startTimeDistance = 0.0;
			}
		}

		else if (Constant.editDistTimeDistType.equals(Enums.EditDistanceTimeDistanceType.NearerScaled))
		{
			if (absTimeDiffInSeconds <= startTimeToleranceInSeconds)
			{
				startTimeDistance = absTimeDiffInSeconds / startTimeToleranceInSeconds;
			}
			else // absTimeDiffInSeconds > startTimeToleranceInSeconds
			{
				startTimeDistance = 1.0;
			}
		}

		// cost = 0 if diff <=1hr , cost (0,1) if diff in (1,3) hrs and cost =1 if diff >=3hrs
		else if (Constant.editDistTimeDistType.equals(Enums.EditDistanceTimeDistanceType.FurtherScaled))
		{
			if (absTimeDiffInSeconds > startTimeToleranceInSeconds)
			{
				startTimeDistance = absTimeDiffInSeconds / 10800;
			}
		}

		// startTimeDistance should not be negative
		if (startTimeDistance < 0)
		{
			PopUps.showError("startTimeDistance (<0)=" + startTimeDistance);
		}
		return new Pair<>((startTimeDistance * wtStartTime), "\ndtime=" + (startTimeDistance * wtStartTime));
	}

	/**
	 * 
	 * @param ao1ST
	 * @param ao2ST
	 * @param editDistTimeDistType
	 * @param startTimeToleranceInSeconds
	 * @param wtStartTime
	 * @return
	 * @since 24 Feb 2018 extracted from the method getFeatureLevelDistanceGowallaPD23Feb2018()
	 */
	public static Pair<Double, String> getStartTimeDistance(Timestamp ao1ST, Timestamp ao2ST,
			Enums.EditDistanceTimeDistanceType editDistTimeDistType, long startTimeToleranceInSeconds,
			double wtStartTime)
	{
		if (editDistTimeDistType.equals(Enums.EditDistanceTimeDistanceType.BinaryThreshold))
		{
			// if not same within 60mins then add wt to dfeat
			if (DateTimeUtils.isSameTimeInTolerance(ao1ST, ao2ST, startTimeToleranceInSeconds) == false)
			{
				return new Pair<>(wtStartTime, "\ndtime=" + wtStartTime);
			}
		}
		// $$ curtain on 2 Mar 2017 end

		// $$ added on 2nd march 2017 start: nearerScaledTimeDistance
		else if (Constant.editDistTimeDistType.equals(Enums.EditDistanceTimeDistanceType.NearerScaled))
		{
			long absTimeDiffInSeconds = DateTimeUtils.getTimeDiffInSeconds(ao1ST, ao2ST);
			if (absTimeDiffInSeconds <= startTimeToleranceInSeconds)
			{
				double timeDistance = absTimeDiffInSeconds / startTimeToleranceInSeconds;
				return new Pair<>((timeDistance * wtStartTime), "\ndtime=" + (timeDistance * wtStartTime));
			}
			else // absTimeDiffInSeconds > startTimeToleranceInSeconds
			{
				return new Pair<>(1.0 * wtStartTime, "\ndtime=" + (1.0 * wtStartTime));
			}
		}
		// $$ added on 2nd march 2017 end

		// $$ added on 3rd march 2017 start: furtherScaledTimeDistance
		// cost = 0 if diff <=1hr , cost (0,1) if diff in (1,3) hrs and cost =1 if diff >=3hrs
		else if (Constant.editDistTimeDistType.equals(Enums.EditDistanceTimeDistanceType.FurtherScaled))
		{
			long absTimeDiffInSeconds = DateTimeUtils.getTimeDiffInSeconds(ao1ST, ao2ST);
			if (absTimeDiffInSeconds > startTimeToleranceInSeconds)
			{
				double timeDistance = absTimeDiffInSeconds / 10800;
				return new Pair<>((timeDistance * wtStartTime), "\ndtime=" + (timeDistance * wtStartTime));
			}
		}
		return null;
	}

	/**
	 * Computes the case-based similarity for two given numeric values and normalises using 'max normalisation'
	 * 
	 * @param val1
	 * @param val2
	 * @param tolerance
	 * @param componentName
	 * @return case-based similarity (max normalised)
	 */
	private static double getCaseSimilarityComponent(double val1, double val2, double tolerance, String componentName)
	{
		double simComponentVal = 0;
		double absDifference = Math.abs(val1 - val2);
		double max = Math.max(val1, val2);
		if (max <= 0)
		{
			if (max < 0)
			{
				System.err.println("Error in getSimilarityComponent " + componentName + ": max =" + max + "<0");
			}
			max = 1;
		}
		if (absDifference > tolerance)
		{
			simComponentVal = (1 - absDifference / max);
		}
		else
		{
			simComponentVal = 1;
		}
		if (VerbosityConstants.verbose)
		{
			System.out.println(componentName + "Similarity=" + simComponentVal + " for vals " + val1 + ", " + val2);
		}
		return simComponentVal;
	}

	/**
	 * Computes the case-based similarity for two given geolocation (latitue, longitude pairs) values and normalises
	 * using 'threshold normalisation' using Q3 of that component (over all values for that user).
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @param componentName
	 * @param userID
	 * @return case-based similarity (threshold normalised)
	 */
	private static double getCaseSimilarityComponentForGeoLocation(String lat1, String lon1, String lat2, String lon2,
			double tolerance, String componentName, int userID)
	{
		double simComponentVal = 0;

		double diffGeo = SpatialUtils.haversine(lat1, lon1, lat2, lon2);

		if (diffGeo <= tolerance)
		{
			return 1;
		}

		else
		{
			double thirdQuartile = -99;

			if (componentName.trim().equalsIgnoreCase("StartGeo"))
			{
				thirdQuartile = thirdQuartilesStartEndGeoDiffForGeolife[userID * 2];
			}

			else if (componentName.trim().equalsIgnoreCase("EndGeo"))
			{
				thirdQuartile = thirdQuartilesStartEndGeoDiffForGeolife[(userID * 2) + 1];
			}

			if (thirdQuartile < 0)
			{
				System.err.println("Error: third quartile is 0 for " + componentName);
			}

			if (diffGeo > thirdQuartile)
			{
				simComponentVal = 0;
			}
			else
			// this means more than tolerance but less than equals third quartile
			{
				simComponentVal = 1 - diffGeo / thirdQuartile;
			}
		}

		if (VerbosityConstants.verbose)
		{
			System.out.println(
					componentName + "Similarity=" + simComponentVal + " for geo locations apart by " + diffGeo);
		}
		return simComponentVal;
	}

	/**
	 * Removes the invalid activity objects from the given arraylist of activity objects. Invalid Activity Objects are
	 * Activity Objects with Activity Name as 'Others' or 'Unknown'
	 * 
	 * @param arrayToPrune
	 * @return
	 */
	public static ArrayList<ActivityObject> expungeInvalids(ArrayList<ActivityObject> arrayToPrune)
	{
		if (arrayToPrune == null)
		{
			System.err.println("Error inside expungeInvalids: arrayToPrune is null");
		}

		ArrayList<ActivityObject> arrPruned = new ArrayList<ActivityObject>();

		for (int i = 0; i < arrayToPrune.size(); i++)
		{
			if (arrayToPrune.get(i).isInvalidActivityName()) // if the first element is unknown, prune it
			{
				continue;
			}
			else
				arrPruned.add(arrayToPrune.get(i));
		}
		return arrPruned;
	}

	/**
	 * Removes the first activity object of the given arraylist of activity objects if that first activity object has
	 * activity name as 'Unknown'
	 * 
	 * @param arrayToPrune
	 * @return
	 */
	public static ArrayList<ActivityObject> pruneFirstUnknown(ArrayList<ActivityObject> arrayToPrune)
	{
		if (arrayToPrune == null)
		{
			System.err.println("Error inside pruneFirstUnknown: arrayToPrune is null");
		}
		ArrayList<ActivityObject> arrPruned = arrayToPrune;// new ArrayList<ActivityObject>();
		// if the first element is unknown, prune it
		if (arrPruned.get(0).getActivityName().equalsIgnoreCase("Unknown"))
		{
			arrPruned.remove(0);
		}
		// for (int i = 0; i < arrayToPrune.size(); i++)
		// { if ((i == 0) && arrayToPrune.get(i).getActivityName().equalsIgnoreCase("Unknown"))
		// {continue;}
		// else arrPruned.add(arrayToPrune.get(i));
		// }
		return arrPruned;
	}

	public static double minimum(double a, double b, double c)
	{
		return Math.min(Math.min(a, b), c);
	}

	public static boolean isMinimum(double tocheck, double a, double b, double c)
	{
		return tocheck == Math.min(Math.min(a, b), c);
	}

	// /**
	// * Computes Levenshtein distance between the given strings using the given weights. (partially from:
	// www.programcreek.com) DO NOT USE THIS:
	// *
	// * DOES NOT GIVE THE CORRECT TRACE OF EDIT OPERATIONS
	// *
	// * NOTE: GIVES CORRECT EDIT DISTANCE BUT WRONG TRACE OF EDIT OPERATIONS
	// *
	// * THIS WAS USED FOR all results before june 2 2015. It can only affect TwoLevel Edit Distance at Feature level.
	// WRONG WRONG WRONG WRONG WRONG WRONG
	// *
	// * @param word1
	// * @param word2
	// * @param insertWt
	// * @param deleteWt
	// * @param replaceWt
	// * @return Levenshtein distance with trace of operations
	// */
	// // WRONG WRONG WRONG WRONG WRONG WRONG
	// public Pair<String, Double> getSimpleLevenshteinDistance(String word1, String word2, int insertWt, int deleteWt,
	// int replaceWt)
	// {
	// if (Constant.verbose || Constant.verboseLevenstein)
	// {
	// System.out.println("inside getSimpleLevenshteinDistance for word1=" + word1 + " word2=" + word2 + " with
	// insertWt=" + insertWt + " with deleteWt=" + deleteWt
	// + " with replaceWt=" + replaceWt);
	// }
	// int len1 = word1.length();
	// int len2 = word2.length();
	//
	// // len1+1, len2+1, because finally return dp[len1][len2]
	// int[][] dp = new int[len1 + 1][len2 + 1];
	//
	// StringBuffer[][] traceMatrix = new StringBuffer[len1 + 1][len2 + 1];
	// for (int i = 0; i <= len1; i++)
	// {
	// for (int j = 0; j <= len2; j++)
	// {
	// traceMatrix[i][j] = new StringBuffer();
	// }
	// }
	//
	// dp[0][0] = 0;
	//
	// for (int i = 1; i <= len1; i++)
	// {
	// dp[i][0] = i;
	// traceMatrix[i][0].append(traceMatrix[i - 1][0] + "_D(" + (i) + "-" + "0)");
	// }
	//
	// for (int j = 1; j <= len2; j++)
	// {
	// dp[0][j] = j;
	// traceMatrix[0][j].append(traceMatrix[0][j - 1] + "_I(0" + "-" + j + ")");
	// }
	//
	// // iterate though, and check last char
	// for (int i = 0; i < len1; i++)
	// {
	// char c1 = word1.charAt(i);
	// for (int j = 0; j < len2; j++)
	// {
	// char c2 = word2.charAt(j);
	//
	// // if last two chars equal
	// if (c1 == c2)
	// {
	// // update dp value for +1 length
	// dp[i + 1][j + 1] = dp[i][j];
	// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_N(" + (i + 1) + "-" + (j + 1) + ")");
	// }
	// else
	// {
	// int replace = dp[i][j] + replaceWt;// 2;
	// int insert = dp[i][j + 1] + insertWt;// 1;
	// int delete = dp[i + 1][j] + deleteWt;// 1;
	//
	// // int min = replace > insert ? insert : replace;
	// // min = delete > min ? min : delete;
	// //
	// int min = -9999;
	//
	// if (isMinimum(delete, delete, insert, replace))
	// {
	// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_D(" + (i + 1) + "-" + (j + 1) + ")");
	// min = delete;
	// }
	//
	// else if (isMinimum(insert, delete, insert, replace))
	// {
	// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_I(" + (i + 1) + "-" + (j + 1) + ")");
	// min = insert;
	// }
	// else if (isMinimum(replace, delete, insert, replace))
	// {
	// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_S(" + (i + 1) + "-" + (j + 1) + ")");
	// min = replace;
	// }
	//
	// if (min == -9999)
	// {
	// System.out.println("Error in minDistance");
	// }
	//
	// dp[i + 1][j + 1] = min;
	// }
	// }
	// }
	//
	// if (Constant.verboseLevenstein)
	// // iterate though, and check last char
	// {
	// System.out.println(" Trace Matrix: ");
	// for (int i = 0; i <= len1; i++)
	// {
	// for (int j = 0; j <= len2; j++)
	// {
	// System.out.print(traceMatrix[i][j] + "||");
	// }
	// System.out.println();
	// }
	// System.out.println(" Distance Matrix: ");
	// for (int i = 0; i <= len1; i++)
	// {
	// for (int j = 0; j <= len2; j++)
	// {
	// System.out.print(dp[i][j] + "||");
	// }
	// System.out.println();
	// }
	//
	// System.out.println("Resultant Distance = " + new Double(dp[len1][len2]));
	// System.out.println("Resultant Trace = " + traceMatrix[len1][len2].toString());
	// System.out.println(" -------- ");
	// }
	//
	// return new Pair<String, Double>(traceMatrix[len1][len2].toString(), new Double(dp[len1][len2]));
	// }

	/**
	 * Using method of centroid of finite points. (mean of lat, mean of lon)
	 * 
	 * @param vals2
	 * @return
	 */
	public Pair<Double, Double> getCentroidGeoCoordinates(Pair<Double, Double> vals[])
	{
		double sumLats = 0;// [] = new double[vals.length];
		double sumLons = 0;// [] = new double[vals.length];

		for (int i = 0; i < vals.length; i++)
		{
			sumLats += vals[i].getFirst();
			sumLons += vals[i].getSecond();
		}

		double meanLats = StatsUtils.round(sumLats / vals.length, 6);
		double meanLons = StatsUtils.round(sumLons / vals.length, 6);
		return new Pair(meanLats, meanLons);
	}

	/**
	 * Computes Weighted Levenshtein distance between the given strings.</br>
	 * 
	 * Weight of insertion = insertWt * abs(haversinediff(insertedVal - medianValOfOtherString)) </br>
	 * Weight of deletion = deleteWt * abs(haversinediff(deletedVal - medianValOfOtherString)) </br>
	 * Weight of replacement = replaceWt * abs(haversinediff(replaceVal - original))
	 * 
	 * right to left: insertion? top to down: deletion
	 * 
	 * NOT SYMMETRIC
	 * 
	 * 
	 * @param word1
	 * @param word2
	 * @param insertWt
	 * @param deleteWt
	 * @param replaceWt
	 * @param vals1
	 * @param vals2
	 *            feature timeline vals for current timeline
	 * @return
	 */
	public Pair<String, Double> getWeightedLevenshteinDistanceRawValsForGeoCoordinates(String word1, String word2,
			int insertWt, int deleteWt, int replaceWt, Pair<Double, Double> vals1[], Pair<Double, Double> vals2[])
	{
		Pair<Double, Double> centroid2 = getCentroidGeoCoordinates(vals2); // median of seconds word, as word1 is being
																			// transformed to word2, this destroys the
																			// symmetry of edit
																			// distance
		Pair<Double, Double> centroid1 = getCentroidGeoCoordinates(vals1);

		double distBetweenCentroids = SpatialUtils.haversine(centroid1.getFirst(), centroid1.getSecond(),
				centroid2.getFirst(), centroid2.getSecond());

		if (VerbosityConstants.verbose || VerbosityConstants.verboseLevenstein)
		{
			System.out.println("inside getWeightedLevenshteinDistanceRawValsForGeoCoordinates  for word1=" + word1
					+ "  word2=" + word2 + " with insertWt=" + insertWt + " with deleteWt=" + deleteWt
					+ " with replaceWt=" + replaceWt + " vals1 = " + Arrays.toString(vals2) + " vals2 = "
					+ Arrays.toString(vals2) + " centroid of val2= " + centroid2.toString());
		}

		int len1 = word1.length();
		int len2 = word2.length();

		// len1+1, len2+1, because finally return dp[len1][len2]
		double[][] dp = new double[len1 + 1][len2 + 1];

		StringBuffer[][] traceMatrix = new StringBuffer[len1 + 1][len2 + 1];
		for (int i = 0; i <= len1; i++)
		{
			for (int j = 0; j <= len2; j++)
			{
				traceMatrix[i][j] = new StringBuffer();
			}
		}

		dp[0][0] = 0;

		for (int i = 1; i <= len1; i++)
		{
			dp[i][0] = dp[i - 1][0] + SpatialUtils.haversine(vals1[i - 1].getFirst(), vals1[i - 1].getSecond(),
					centroid2.getFirst(), centroid2.getSecond());
			traceMatrix[i][0].append(traceMatrix[i - 1][0] + "_D(" + (i) + "-" + "0)");
		}

		for (int j = 1; j <= len2; j++)
		{
			dp[0][j] = dp[0][j - 1] + SpatialUtils.haversine(vals2[j - 1].getFirst(), vals2[j - 1].getSecond(),
					centroid2.getFirst(), centroid2.getSecond());// j * distBetweenCentroids;
			traceMatrix[0][j].append(traceMatrix[0][j - 1] + "_I(0" + "-" + j + ")");
		}

		// iterate though, and check last char
		for (int i = 0; i < len1; i++)
		{
			char c1 = word1.charAt(i);
			for (int j = 0; j < len2; j++)
			{
				char c2 = word2.charAt(j);
				if (VerbosityConstants.verboseLevenstein)
				{
					System.out.println("\nComparing " + c1 + " and " + c2);
				} // if last two chars equal
				if (c1 == c2)
				{
					// update dp value for +1 length
					dp[i + 1][j + 1] = dp[i][j];
					traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_N(" + (i + 1) + "-" + (j + 1) + ")");
					if (VerbosityConstants.verboseLevenstein)
					{
						System.out.println("Equal" + " Trace " + traceMatrix[i + 1][j + 1]);// "_N(" + (i + 1) + "-" +
																							// (j + 1) + ")");
					}
				}
				else
				{
					// System.out.println("Difference of vals = "
					// + UtilityBelt.haversine(vals1[i].getFirst(), vals1[i].getSecond(), vals2[j].getFirst(),
					// vals2[j].getSecond()) + "kms");

					double distBetweenPoints = SpatialUtils.haversine(vals1[i].getFirst(), vals1[i].getSecond(),
							vals2[j].getFirst(), vals2[j].getSecond());
					if (VerbosityConstants.verboseLevenstein)
					{
						System.out.println("Difference of vals = " + distBetweenPoints + "kms");
					}
					double distBetweenPoint1AndCentroid = SpatialUtils.haversine(vals1[i].getFirst(),
							vals1[i].getSecond(), centroid2.getFirst(), centroid2.getSecond());
					double distBetweenPoint2AndCentroid = SpatialUtils.haversine(vals2[j].getFirst(),
							vals2[j].getSecond(), centroid2.getFirst(), centroid2.getSecond());

					double replace = dp[i][j] + replaceWt * distBetweenPoints;

					double delete = dp[i][j + 1] + deleteWt * distBetweenPoint1AndCentroid;
					// Math.abs(vals1[i - 1] - median2);// deletion --previous row, i.e, cell above
					double insert = dp[i + 1][j] + insertWt * distBetweenPoint2AndCentroid;// insertion --previous
																							// column, i.e, cell on left

					if (VerbosityConstants.verboseLevenstein)
					{
						System.out.println("replace =" + replace + " insert =" + insert + " deleteWt =" + delete);
					} // int min = replace > insert ? insert : replace;
						// min = delete > min ? min : delete;
						//
					double min = -9999;

					if (isMinimum(delete, delete, insert, replace))
					{
						traceMatrix[i + 1][j + 1].append(traceMatrix[i][j + 1] + "_D(" + (i + 1) + "-" + (j + 1) + ")");
						min = delete;
						if (VerbosityConstants.verboseLevenstein)
						{
							System.out.println("Delete is min:" + delete + " Trace " + traceMatrix[i + 1][j + 1]);// "
																													// Trace
																													// added=
																													// "
																													// +
																													// "_D("
																													// +
																													// (i
																													// +
																													// 1)
																													// +
																													// "-"
																													// +
																													// (j
																													// +
																													// 1)
																													// +
																													// ")");
						}
					}

					else if (isMinimum(insert, delete, insert, replace))
					{
						traceMatrix[i + 1][j + 1].append(traceMatrix[i + 1][j] + "_I(" + (i + 1) + "-" + (j + 1) + ")");
						min = insert;
						if (VerbosityConstants.verboseLevenstein)
						{
							System.out.println("Insert is min:" + insert + " Trace " + traceMatrix[i + 1][j + 1]);// "
																													// Trace
																													// added=
																													// "
																													// +
																													// "_I("
																													// +
																													// (i
																													// +
																													// 1)
																													// +
																													// "-"
																													// +
																													// (j
																													// +
																													// 1)
																													// +
																													// ")");
						}
					}
					else if (isMinimum(replace, delete, insert, replace))
					{
						traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_S(" + (i + 1) + "-" + (j + 1) + ")");
						min = replace;
						if (VerbosityConstants.verboseLevenstein)
						{
							System.out.println("replace is min:" + replace + " Trace " + traceMatrix[i + 1][j + 1]);// "
																													// Trace
																													// added=
																													// "
																													// +
																													// "_S("
																													// +
																													// (i
																													// +
																													// 1)
																													// +
																													// "-"
																													// +
																													// (j
																													// +
																													// 1)
																													// +
																													// ")");
						}
					}

					if (min == -9999)
					{
						System.out.println("Error in minDistance");
					}

					dp[i + 1][j + 1] = min;
				}
			}
		}

		if (VerbosityConstants.verboseLevenstein)
		// iterate though, and check last char
		{
			System.out.println("  Trace Matrix: ");
			for (int i = 0; i <= len1; i++)
			{
				for (int j = 0; j <= len2; j++)
				{
					System.out.print(traceMatrix[i][j] + "||");
				}
				System.out.println();
			}
			System.out.println("  Distance Matrix: ");
			for (int i = 0; i <= len1; i++)
			{
				for (int j = 0; j <= len2; j++)
				{
					System.out.print(dp[i][j] + "||");
				}
				System.out.println();
			}

			System.out.println("Resultant Distance = " + new Double(dp[len1][len2]));
			System.out.println("Resultant Trace = " + traceMatrix[len1][len2].toString());
			System.out.println(" -------- ");
		}

		return new Pair<String, Double>(traceMatrix[len1][len2].toString(), new Double(dp[len1][len2]));
	}

	public double getMedian(double[] vals)
	{
		if (vals.length == 1) return vals[0];

		double median = new DescriptiveStatistics(vals).getPercentile(50);

		// System.out.println(" vals are: " + Arrays.toString(vals) + " median= " + median);

		if (median == Double.NaN)
		{
			PopUps.showError("NAN Error" + (" vals are: " + Arrays.toString(vals) + " median= " + median));
		}

		return median;
	}

	/**
	 * Computes Weighted Levenshtein distance between the given strings.</br>
	 * 
	 * Weight of insertion = insertWt * abs(diff(insertedVal - medianValOfOtherString)) </br>
	 * Weight of deletion = deleteWt * abs(diff(deletedVal - medianValOfOtherString)) </br>
	 * Weight of replacement = replaceWt * abs(diff(replaceVal - original))
	 * 
	 * right to left: insertion? top to down: deletion
	 * 
	 * NOT SYMMETRIC
	 * 
	 * 
	 * @param word1
	 * @param word2
	 * @param insertWt
	 * @param deleteWt
	 * @param replaceWt
	 * @param vals1
	 * @param vals2
	 *            feature timeline vals for current timeline
	 * @return
	 */
	public Pair<String, Double> getWeightedLevenshteinDistanceRawVals(String word1, String word2, int insertWt,
			int deleteWt, int replaceWt, double[] vals1, double[] vals2)
	{
		double median2 = getMedian(vals2);// new DescriptiveStatistics(vals2).getPercentile(50); // median of seconds
											// word, as word1 is being transformed to word2, this destroys
											// the symmetry of
											// edit
											// distance

		if (VerbosityConstants.verbose || VerbosityConstants.verboseLevenstein)
		{
			System.out.println("inside getSimpleLevenshteinDistance  for word1=" + word1 + "  word2=" + word2
					+ " with insertWt=" + insertWt + " with deleteWt=" + deleteWt + " with replaceWt=" + replaceWt
					+ " vals1 = " + Arrays.toString(vals2) + " vals2 = " + Arrays.toString(vals2)
					+ "  median of vals2= " + median2);
		}

		int len1 = word1.length();
		int len2 = word2.length();

		// len1+1, len2+1, because finally return dp[len1][len2]
		double[][] dp = new double[len1 + 1][len2 + 1];

		StringBuffer[][] traceMatrix = new StringBuffer[len1 + 1][len2 + 1];
		for (int i = 0; i <= len1; i++)
		{
			for (int j = 0; j <= len2; j++)
			{
				traceMatrix[i][j] = new StringBuffer();
			}
		}

		dp[0][0] = 0;

		for (int i = 1; i <= len1; i++)
		{
			dp[i][0] = dp[i - 1][0] + Math.abs(vals1[i - 1] - median2);// i * median2;
			traceMatrix[i][0].append(traceMatrix[i - 1][0] + "_D(" + (i) + "-" + "0)");
		}

		for (int j = 1; j <= len2; j++)
		{
			dp[0][j] = dp[0][j - 1] + Math.abs(vals2[j - 1] - median2);// j * median2;
			traceMatrix[0][j].append(traceMatrix[0][j - 1] + "_I(0" + "-" + j + ")");
		}

		// iterate though, and check last char
		for (int i = 0; i < len1; i++)
		{
			char c1 = word1.charAt(i);
			for (int j = 0; j < len2; j++)
			{
				char c2 = word2.charAt(j);
				if (VerbosityConstants.verboseLevenstein)
				{
					System.out.println("\nComparing " + c1 + " and " + c2);
				}
				// if last two chars equal
				if (c1 == c2)
				{
					// update dp value for +1 length
					dp[i + 1][j + 1] = dp[i][j];
					traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_N(" + (i + 1) + "-" + (j + 1) + ")");
					if (VerbosityConstants.verboseLevenstein)
					{
						System.out.println("Equal" + " Trace " + traceMatrix[i + 1][j + 1]);// "_N(" + (i + 1) + "-" +
																							// (j + 1) + ")");
					}
				}
				else
				{

					double replace = dp[i][j] + replaceWt * Math.abs(vals1[i] - vals2[j]);//
					double delete = dp[i][j + 1] + deleteWt * Math.abs(vals1[i] - median2);// deletion --previous row,
																							// i.e, cell above
					double insert = dp[i + 1][j] + insertWt * Math.abs(vals2[j] - median2);// insertion --previous
																							// column, i.e, cell on left
					if (VerbosityConstants.verboseLevenstein)
					{
						System.out.println("Difference of vals = " + Math.abs(vals1[i] - vals2[j]));//
						System.out.println("replace =" + replace + " insert =" + insert + " deleteWt =" + delete);
					} // int min = replace > insert ? insert : replace;
						// min = delete > min ? min : delete;
						//
					double min = -9999;

					if (isMinimum(delete, delete, insert, replace))
					{
						traceMatrix[i + 1][j + 1].append(traceMatrix[i][j + 1] + "_D(" + (i + 1) + "-" + (j + 1) + ")");
						min = delete;
						if (VerbosityConstants.verboseLevenstein)
						{
							System.out.println("Delete is min:" + delete + " Trace " + traceMatrix[i + 1][j + 1]);
							// "
							// Trace
							// added=
							// "
							// +
							// "_D("
							// +
							// (i
							// +
							// 1)
							// +
							// "-"
							// +
							// (j
							// +
							// 1)
							// +
							// ")");
						}
					}

					else if (isMinimum(insert, delete, insert, replace))
					{
						traceMatrix[i + 1][j + 1].append(traceMatrix[i + 1][j] + "_I(" + (i + 1) + "-" + (j + 1) + ")");
						min = insert;
						if (VerbosityConstants.verboseLevenstein)
						{
							System.out.println("Insert is min:" + insert + " Trace " + traceMatrix[i + 1][j + 1]);
							// "
							// Trace
							// added=
							// "
							// +
							// "_I("
							// +
							// (i
							// +
							// 1)
							// +
							// "-"
							// +
							// (j
							// +
							// 1)
							// +
							// ")");
						}
					}
					else if (isMinimum(replace, delete, insert, replace))
					{
						traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_S(" + (i + 1) + "-" + (j + 1) + ")");
						min = replace;
						if (VerbosityConstants.verboseLevenstein)
						{
							System.out.println("replace is min:" + replace + " Trace " + traceMatrix[i + 1][j + 1]);
							// "
							// Trace
							// added=
							// "
							// +
							// "_S("
							// +
							// (i
							// +
							// 1)
							// +
							// "-"
							// +
							// (j
							// +
							// 1)
							// +
							// ")");
						}
					}

					if (min == -9999)
					{
						System.out.println("Error in minDistance");
					}

					dp[i + 1][j + 1] = min;
				}
			}
		}

		if (VerbosityConstants.verboseLevenstein)
		// iterate though, and check last char
		{
			System.out.println("  Trace Matrix: ");
			for (int i = 0; i <= len1; i++)
			{
				for (int j = 0; j <= len2; j++)
				{
					System.out.print(traceMatrix[i][j] + "||");
				}
				System.out.println();
			}
			System.out.println("  Distance Matrix: ");
			for (int i = 0; i <= len1; i++)
			{
				for (int j = 0; j <= len2; j++)
				{
					System.out.print(dp[i][j] + "||");
				}
				System.out.println();
			}

			System.out.println("Resultant Distance = " + new Double(dp[len1][len2]));
			System.out.println("Resultant Trace = " + traceMatrix[len1][len2].toString());
			System.out.println(" -------- ");
		}

		return new Pair<String, Double>(traceMatrix[len1][len2].toString(), new Double(dp[len1][len2]));
	}

	// /**
	// * Computes Weighted Levenshtein distance between the given strings.</br>
	// *
	// * Weight of insertion = insertWt * abs(diff(insertedVal - medianValOfOtherString)) </br> Weight of deletion =
	// deleteWt * abs(diff(deletedVal - medianValOfOtherString)) </br>
	// Weight of
	// replacement
	// * = replaceWt * abs(diff(replaceVal - original))
	// *
	// * right to left: insertion? top to down: deletion
	// *
	// * @param word1
	// * @param word2
	// * @param insertWt
	// * @param deleteWt
	// * @param replaceWt
	// * @return Levenshtein distance with trace of operations
	// */
	// public Pair<String, Double> getWeightedLevenshteinDistanceSymbolisedVals(String word1, String word2, int
	// insertWt, int deleteWt, int replaceWt)
	// {
	// if (Constant.verbose || Constant.verboseLevenstein)
	// {
	// System.out.println("inside getSimpleLevenshteinDistance for word1=" + word1 + " word2=" + word2 + " with
	// insertWt=" + insertWt + " with deleteWt=" + deleteWt
	// + " with replaceWt=" + replaceWt);
	// }
	// int len1 = word1.length();
	// int len2 = word2.length();
	//
	// // len1+1, len2+1, because finally return dp[len1][len2]
	// int[][] dp = new int[len1 + 1][len2 + 1];
	//
	// StringBuffer[][] traceMatrix = new StringBuffer[len1 + 1][len2 + 1];
	// for (int i = 0; i <= len1; i++)
	// {
	// for (int j = 0; j <= len2; j++)
	// {
	// traceMatrix[i][j] = new StringBuffer();
	// }
	// }
	//
	// dp[0][0] = 0;
	//
	// for (int i = 1; i <= len1; i++)
	// {
	// dp[i][0] = i;
	// traceMatrix[i][0].append(traceMatrix[i - 1][0] + "_D(" + (i) + "-" + "0)");
	// }
	//
	// for (int j = 1; j <= len2; j++)
	// {
	// dp[0][j] = j;
	// traceMatrix[0][j].append(traceMatrix[0][j - 1] + "_I(0" + "-" + j + ")");
	// }
	//
	// // iterate though, and check last char
	// for (int i = 0; i < len1; i++)
	// {
	// char c1 = word1.charAt(i);
	// for (int j = 0; j < len2; j++)
	// {
	// char c2 = word2.charAt(j);
	//
	// System.out.println("\nComparing " + c1 + " and " + c2);
	// // if last two chars equal
	// if (c1 == c2)
	// {
	// // update dp value for +1 length
	// dp[i + 1][j + 1] = dp[i][j];
	// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_N(" + (i + 1) + "-" + (j + 1) + ")");
	// System.out.println("Equal" + " Trace " + traceMatrix[i + 1][j + 1]);// "_N(" + (i + 1) + "-" + (j + 1) + ")");
	// }
	// else
	// {
	// System.out.println("Difference of vals = " + (Math.abs(c1 - c2)));
	//
	// int replace = dp[i][j] + replaceWt;// 2;
	// int delete = dp[i][j + 1] + deleteWt;// 1;//deletion --previous row, i.e, cell above
	// int insert = dp[i + 1][j] + insertWt;// 1;// insertion --previous column, i.e, cell on left
	//
	// System.out.println("replace =" + replace + " insert =" + insert + " deleteWt =" + delete);
	// // int min = replace > insert ? insert : replace;
	// // min = delete > min ? min : delete;
	// //
	// int min = -9999;
	//
	// if (isMinimum(delete, delete, insert, replace))
	// {
	// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j + 1] + "_D(" + (i + 1) + "-" + (j + 1) + ")");
	// min = delete;
	// System.out.println("Delete is min:" + delete + " Trace " + traceMatrix[i + 1][j + 1]);// " Trace added= " + "_D("
	// + (i + 1) + "-" + (j + 1) + ")");
	// }
	//
	// else if (isMinimum(insert, delete, insert, replace))
	// {
	// traceMatrix[i + 1][j + 1].append(traceMatrix[i + 1][j] + "_I(" + (i + 1) + "-" + (j + 1) + ")");
	// min = insert;
	// System.out.println("Insert is min:" + insert + " Trace " + traceMatrix[i + 1][j + 1]);// " Trace added= " + "_I("
	// + (i + 1) + "-" + (j + 1) + ")");
	// }
	// else if (isMinimum(replace, delete, insert, replace))
	// {
	// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_S(" + (i + 1) + "-" + (j + 1) + ")");
	// min = replace;
	// System.out.println("replace is min:" + replace + " Trace " + traceMatrix[i + 1][j + 1]);// " Trace added= " +
	// "_S(" + (i + 1) + "-" + (j + 1) + ")");
	// }
	//
	// if (min == -9999)
	// {
	// System.out.println("Error in minDistance");
	// }
	//
	// dp[i + 1][j + 1] = min;
	// }
	// }
	// }
	//
	// if (Constant.verboseLevenstein)
	// // iterate though, and check last char
	// {
	// System.out.println(" Trace Matrix: ");
	// for (int i = 0; i <= len1; i++)
	// {
	// for (int j = 0; j <= len2; j++)
	// {
	// System.out.print(traceMatrix[i][j] + "||");
	// }
	// System.out.println();
	// }
	// System.out.println(" Distance Matrix: ");
	// for (int i = 0; i <= len1; i++)
	// {
	// for (int j = 0; j <= len2; j++)
	// {
	// System.out.print(dp[i][j] + "||");
	// }
	// System.out.println();
	// }
	//
	// System.out.println("Resultant Distance = " + new Double(dp[len1][len2]));
	// System.out.println("Resultant Trace = " + traceMatrix[len1][len2].toString());
	// System.out.println(" -------- ");
	// }
	//
	// return new Pair<String, Double>(traceMatrix[len1][len2].toString(), new Double(dp[len1][len2]));
	// }

	/**
	 * Computes Levenshtein distance between the given strings.</br>
	 * 
	 * Weight of insertion = insertWt * abs(diff(insertedVal - medianValOfOtherString)) </br>
	 * Weight of deletion = deleteWt * abs(diff(deletedVal - medianValOfOtherString)) </br>
	 * Weight of replacement = replaceWt * abs(diff(replaceVal - original))
	 * 
	 * right to left: insertion? top to down: deletion
	 * 
	 * @param word1
	 * @param word2
	 * @param insertWt
	 * @param deleteWt
	 * @param replaceWt
	 * @return Levenshtein distance with trace of operations
	 */ // Correct: renamed from getMySimpleLevenshteinDistance() on 27 March 2017
	public static Pair<String, Double> getMySimpleLevenshteinDistanceSlower1(String word1, String word2, int insertWt,
			int deleteWt, int replaceWt)
	{
		long performanceTime1 = System.currentTimeMillis();
		if (VerbosityConstants.verboseLevenstein)// Constant.verbose ||
		{
			System.out.println("inside getMySimpleLevenshteinDistance  for word1=" + word1 + "  word2=" + word2
					+ " with insertWt=" + insertWt + " with deleteWt=" + deleteWt + " with replaceWt=" + replaceWt);
		}
		int len1 = word1.length();
		int len2 = word2.length();

		// len1+1, len2+1, because finally return dp[len1][len2]
		int[][] dist = new int[len1 + 1][len2 + 1];

		StringBuilder[][] traceMatrix = new StringBuilder[len1 + 1][len2 + 1];
		for (int i = 0; i <= len1; i++)
		{
			for (int j = 0; j <= len2; j++)
			{
				traceMatrix[i][j] = new StringBuilder();
			}
		}

		dist[0][0] = 0;

		for (int i = 1; i <= len1; i++)
		{
			dist[i][0] = i;
			traceMatrix[i][0].append(traceMatrix[i - 1][0] + "_D(" + (i) + "-" + "0)");
		}

		for (int j = 1; j <= len2; j++)
		{
			dist[0][j] = j;
			traceMatrix[0][j].append(traceMatrix[0][j - 1] + "_I(0" + "-" + j + ")");
		}

		// iterate though, and check last char
		for (int i = 0; i < len1; i++)
		{
			char c1 = word1.charAt(i);
			for (int j = 0; j < len2; j++)
			{
				char c2 = word2.charAt(j);

				// System.out.println("\nComparing " + c1 + " and " + c2);
				// if last two chars equal
				if (c1 == c2)
				{
					// update dp value for +1 length
					dist[i + 1][j + 1] = dist[i][j];
					traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_N(" + (i + 1) + "-" + (j + 1) + ")");
					// System.out.println("Equal" + " Trace " + traceMatrix[i + 1][j + 1]);// "_N(" + (i + 1) + "-" + (j
					// + 1) + ")");
				}
				else
				{
					int replace = dist[i][j] + replaceWt;// 2; //diagonally previous, see slides from STANFORD NLP on
															// min edit distance
					int delete = dist[i][j + 1] + deleteWt;// 1;//deletion --previous row, i.e, cell above
					int insert = dist[i + 1][j] + insertWt;// 1;// insertion --previous column, i.e, cell on left

					// System.out.println("replace =" + replace + " insert =" + insert + " deleteWt =" + delete);
					// int min = replace > insert ? insert : replace;
					// min = delete > min ? min : delete;
					//
					int min = -9999;

					if (isMinimum(delete, delete, insert, replace))
					{
						traceMatrix[i + 1][j + 1].append(traceMatrix[i][j + 1] + "_D(" + (i + 1) + "-" + (j + 1) + ")");
						min = delete;
						// System.out.println("Delete is min:" + delete + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_D(" + (i + 1) + "-" + (j + 1) + ")");
					}

					else if (isMinimum(insert, delete, insert, replace))
					{
						traceMatrix[i + 1][j + 1].append(traceMatrix[i + 1][j] + "_I(" + (i + 1) + "-" + (j + 1) + ")");
						min = insert;
						// System.out.println("Insert is min:" + insert + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_I(" + (i + 1) + "-" + (j + 1) + ")");
					}
					else if (isMinimum(replace, delete, insert, replace))
					{
						traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_S(" + (i + 1) + "-" + (j + 1) + ")");
						min = replace;
						// System.out.println("replace is min:" + replace + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_S(" + (i + 1) + "-" + (j + 1) + ")");
					}

					if (min == -9999)
					{
						System.out.println("Error in minDistance");
					}

					dist[i + 1][j + 1] = min;
				}
			}
		}

		if (VerbosityConstants.verboseLevenstein)
		// iterate though, and check last char
		{
			System.out.println("  Trace Matrix: ");
			for (int i = 0; i <= len1; i++)
			{
				for (int j = 0; j <= len2; j++)
				{
					System.out.print(traceMatrix[i][j] + "||");
				}
				System.out.println();
			}
			System.out.println("  Distance Matrix: ");
			for (int i = 0; i <= len1; i++)
			{
				for (int j = 0; j <= len2; j++)
				{
					System.out.print(dist[i][j] + "||");
				}
				System.out.println();
			}

			System.out.println("Resultant Distance = " + new Double(dist[len1][len2]));
			System.out.println("Resultant Trace = " + traceMatrix[len1][len2].toString());
			System.out.println(" -------- ");
		}
		long performanceTime2 = System.currentTimeMillis();

		WToFile.appendLineToFileAbs(
				Integer.toString(word1.length()) + "," + Integer.toString(word2.length()) + ","
						+ Long.toString(performanceTime2 - performanceTime1) + "\n",
				Constant.getCommonPath() + "MySimpleLevenshteinDistanceTimeTakenInms.csv");

		return new Pair<String, Double>(traceMatrix[len1][len2].toString(), new Double(dist[len1][len2]));
	}

	///// start of faster
	/**
	 * Fork of org.activity.distances.AlignmentBasedDistance.getMySimpleLevenshteinDistance(String, String, int, int,
	 * int) for faster performance by optimising string concatenation. ref:
	 * http://stackoverflow.com/questions/10078912/best-practices-performance-mixing-stringbuilder-append-with-string-concat
	 * <p>
	 * Computes Levenshtein distance between the given strings.</br>
	 * 
	 * Weight of insertion = insertWt * abs(diff(insertedVal - medianValOfOtherString)) </br>
	 * Weight of deletion = deleteWt * abs(diff(deletedVal - medianValOfOtherString)) </br>
	 * Weight of replacement = replaceWt * abs(diff(replaceVal - original))
	 * 
	 * right to left: insertion? top to down: deletion
	 * 
	 * @param word1
	 * @param word2
	 * @param insertWt
	 * @param deleteWt
	 * @param replaceWt
	 * @return Pair{Levenshtein distance,trace of operations}
	 */
	public static Pair<String, Double> getMySimpleLevenshteinDistanceSlower2(String word1, String word2, int insertWt,
			int deleteWt, int replaceWt)
	{
		// long performanceTime1 = System.currentTimeMillis();
		if (VerbosityConstants.verboseLevenstein)// Constant.verbose ||
		{
			System.out.println("inside getMySimpleLevenshteinDistance  for word1=" + word1 + "  word2=" + word2
					+ " with insertWt=" + insertWt + " with deleteWt=" + deleteWt + " with replaceWt=" + replaceWt);
		}
		int len1 = word1.length();
		int len2 = word2.length();

		// len1+1, len2+1, because finally return dp[len1][len2]
		int[][] dist = new int[len1 + 1][len2 + 1];

		StringBuilder[][] traceMatrix = new StringBuilder[len1 + 1][len2 + 1];

		for (int i = 0; i <= len1; i++)
		{
			for (int j = 0; j <= len2; j++)
			{
				traceMatrix[i][j] = new StringBuilder();
			}
		}

		dist[0][0] = 0;

		for (int i = 1; i <= len1; i++)
		{
			dist[i][0] = i;
			// traceMatrix[i][0].append(traceMatrix[i - 1][0] + "_D(" + (i) + "-" + "0)");
			traceMatrix[i][0].append(traceMatrix[i - 1][0].toString()).append("_D(").append(Integer.toString(i))
					.append("-0)");
		}

		for (int j = 1; j <= len2; j++)
		{
			dist[0][j] = j;
			// traceMatrix[0][j].append(traceMatrix[0][j - 1] + "_I(0" + "-" + j + ")");
			traceMatrix[0][j].append(traceMatrix[0][j - 1].toString()).append("_I(0-").append(Integer.toString(j))
					.append(")");
		}

		// iterate though, and check last char
		for (int i = 0; i < len1; i++)
		{
			char c1 = word1.charAt(i);
			for (int j = 0; j < len2; j++)
			{
				char c2 = word2.charAt(j);

				// System.out.println("\nComparing " + c1 + " and " + c2);
				// if last two chars equal
				if (c1 == c2)
				{
					// update dp value for +1 length
					dist[i + 1][j + 1] = dist[i][j];
					// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_N(" + (i + 1) + "-" + (j + 1) + ")");
					traceMatrix[i + 1][j + 1].append(traceMatrix[i][j].toString()).append("_N(")
							.append(Integer.toString(i + 1)).append("-").append(Integer.toString(j + 1)).append(")");
					// System.out.println("Equal" + " Trace " + traceMatrix[i + 1][j + 1]);// "_N(" + (i + 1) + "-" + (j
					// + 1) + ")");
				}
				else
				{
					int replace = dist[i][j] + replaceWt;// 2; //diagonally previous, see slides from STANFORD NLP on
															// min edit distance
					int delete = dist[i][j + 1] + deleteWt;// 1;//deletion --previous row, i.e, cell above
					int insert = dist[i + 1][j] + insertWt;// 1;// insertion --previous column, i.e, cell on left
					// System.out.println("replace =" + replace + " insert =" + insert + " deleteWt =" + delete);
					// int min = replace > insert ? insert : replace;
					// min = delete > min ? min : delete;
					//
					int min = -9999;

					if (isMinimum(delete, delete, insert, replace))
					{
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j + 1] + "_D(" + (i + 1) + "-" + (j + 1) +
						// ")");
						traceMatrix[i + 1][j + 1].append(traceMatrix[i][j + 1].toString()).append("_D(")
								.append(Integer.toString(i + 1)).append("-").append(Integer.toString(j + 1))
								.append(")");
						min = delete;
						// System.out.println("Delete is min:" + delete + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_D(" + (i + 1) + "-" + (j + 1) + ")");
					}

					else if (isMinimum(insert, delete, insert, replace))
					{
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i + 1][j] + "_I(" + (i + 1) + "-" + (j + 1) +
						// ")");
						traceMatrix[i + 1][j + 1].append(traceMatrix[i + 1][j].toString()).append("_I(")
								.append(Integer.toString(i + 1)).append("-").append(Integer.toString(j + 1))
								.append(")");
						min = insert;
						// System.out.println("Insert is min:" + insert + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_I(" + (i + 1) + "-" + (j + 1) + ")");
					}
					else if (isMinimum(replace, delete, insert, replace))
					{
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_S(" + (i + 1) + "-" + (j + 1) + ")");
						traceMatrix[i + 1][j + 1].append(traceMatrix[i][j].toString()).append("_S(")
								.append(Integer.toString(i + 1)).append("-").append(Integer.toString(j + 1))
								.append(")");
						min = replace;
						// System.out.println("replace is min:" + replace + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_S(" + (i + 1) + "-" + (j + 1) + ")");
					}

					if (min == -9999)
					{
						System.out.println(PopUps.getTracedErrorMsg("Error in minDistance"));
					}

					dist[i + 1][j + 1] = min;
				}
			}
		}

		if (VerbosityConstants.verboseLevenstein)
		// iterate though, and check last char
		{
			System.out.println("  Trace Matrix: ");
			for (int i = 0; i <= len1; i++)
			{
				for (int j = 0; j <= len2; j++)
				{
					System.out.print(traceMatrix[i][j] + "|");
				}
				System.out.println();
			}
			System.out.println("  Distance Matrix: ");
			for (int i = 0; i <= len1; i++)
			{
				for (int j = 0; j <= len2; j++)
				{
					System.out.print(dist[i][j] + "|");
				}
				System.out.println();
			}

			System.out.println("Resultant Distance = " + new Double(dist[len1][len2]));
			System.out.println("Resultant Trace = " + traceMatrix[len1][len2].toString());
			System.out.println(" -------- ");
		}

		// long performanceTime2 = System.currentTimeMillis();
		// WritingToFile.appendLineToFileAbsolute(
		// Integer.toString(word1.length()) + "," + Integer.toString(word2.length()) + ","
		// + Long.toString(performanceTime2 - performanceTime1) + "\n",
		// Constant.getCommonPath() + "MySimpleLevenshteinDistanceTimeTakenInms.csv");
		return new Pair<String, Double>(traceMatrix[len1][len2].toString(), new Double(dist[len1][len2]));
	}
	///// end of faster

	///// start of faster v2
	/**
	 * faster v2
	 * <p>
	 * Fork of org.activity.distances.AlignmentBasedDistance.getMySimpleLevenshteinDistance(String, String, int, int,
	 * int) for faster performance by optimising string concatenation. ref:
	 * http://stackoverflow.com/questions/10078912/best-practices-performance-mixing-stringbuilder-append-with-string-concat
	 * <p>
	 * Computes Levenshtein distance between the given strings.</br>
	 * 
	 * Weight of insertion = insertWt * abs(diff(insertedVal - medianValOfOtherString)) </br>
	 * Weight of deletion = deleteWt * abs(diff(deletedVal - medianValOfOtherString)) </br>
	 * Weight of replacement = replaceWt * abs(diff(replaceVal - original))
	 * 
	 * right to left: insertion? top to down: deletion
	 * 
	 * @since Mar 30, 2017
	 * @param word1
	 * @param word2
	 * @param insertWt
	 * @param deleteWt
	 * @param replaceWt
	 * @return Pair{Levenshtein distance,trace of operations}
	 */
	public static Pair<String, Double> getMySimpleLevenshteinDistanceV2(String word1, String word2, int insertWt,
			int deleteWt, int replaceWt)
	{
		// long performanceTime1 = System.currentTimeMillis();
		if (VerbosityConstants.verboseLevenstein)// Constant.verbose ||
		{
			System.out.println("inside getMySimpleLevenshteinDistance  for word1=" + word1 + "  word2=" + word2
					+ " with insertWt=" + insertWt + " with deleteWt=" + deleteWt + " with replaceWt=" + replaceWt);
		}
		int len1 = word1.length();
		int len2 = word2.length();

		// len1+1, len2+1, because finally return dp[len1][len2]
		int[][] dist = new int[len1 + 1][len2 + 1];

		StringBuilder[][] traceMatrix = new StringBuilder[len1 + 1][len2 + 1];

		for (int i = 0; i <= len1; i++)
		{
			for (int j = 0; j <= len2; j++)
			{
				traceMatrix[i][j] = new StringBuilder();
			}
		}

		dist[0][0] = 0;

		for (int i = 1; i <= len1; i++)
		{
			dist[i][0] = i;
			// traceMatrix[i][0].append(traceMatrix[i - 1][0] + "_D(" + (i) + "-" + "0)");
			traceMatrix[i][0] = StringUtils.fCat(traceMatrix[i][0], traceMatrix[i - 1][0].toString(), "_D(",
					Integer.toString(i), "-0)");
			// traceMatrix[i][0].append(traceMatrix[i - 1][0].toString()).append("_D(").append(Integer.toString(i))
			// .append("-0)");
		}

		for (int j = 1; j <= len2; j++)
		{
			dist[0][j] = j;
			// traceMatrix[0][j].append(traceMatrix[0][j - 1] + "_I(0" + "-" + j + ")");
			traceMatrix[0][j] = StringUtils.fCat(traceMatrix[0][j], traceMatrix[0][j - 1].toString(), "_I(0-",
					Integer.toString(j), ")");
			// traceMatrix[0][j].append(traceMatrix[0][j - 1].toString()).append("_I(0-").append(Integer.toString(j))
			// .append(")");
		}

		// iterate though, and check last char
		for (int i = 0; i < len1; i++)
		{
			char c1 = word1.charAt(i);
			for (int j = 0; j < len2; j++)
			{
				char c2 = word2.charAt(j);

				// System.out.println("\nComparing " + c1 + " and " + c2);
				// if last two chars equal
				if (c1 == c2)
				{
					// update dp value for +1 length
					dist[i + 1][j + 1] = dist[i][j];
					// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_N(" + (i + 1) + "-" + (j + 1) + ")");
					// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j].toString()).append("_N(")
					// .append(Integer.toString(i + 1)).append("-").append(Integer.toString(j + 1)).append(")");

					traceMatrix[i + 1][j + 1] = StringUtils.fCat(traceMatrix[i + 1][j + 1],
							traceMatrix[i][j].toString(), "_N(", Integer.toString(i + 1), "-", Integer.toString(j + 1),
							")");

					// System.out.println("Equal" + " Trace " + traceMatrix[i + 1][j + 1]);// "_N(" + (i + 1) + "-" + (j
					// + 1) + ")");
				}
				else
				{
					int replace = dist[i][j] + replaceWt;// 2; //diagonally previous, see slides from STANFORD NLP on
															// min edit distance
					int delete = dist[i][j + 1] + deleteWt;// 1;//deletion --previous row, i.e, cell above
					int insert = dist[i + 1][j] + insertWt;// 1;// insertion --previous column, i.e, cell on left
					// System.out.println("replace =" + replace + " insert =" + insert + " deleteWt =" + delete);
					// int min = replace > insert ? insert : replace;
					// min = delete > min ? min : delete;
					//
					int min = -9999;

					if (isMinimum(delete, delete, insert, replace))
					{
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j + 1] + "_D(" + (i + 1) + "-" + (j + 1) +
						// ")");
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j + 1].toString()).append("_D(")
						// .append(Integer.toString(i + 1)).append("-").append(Integer.toString(j + 1))
						// .append(")");

						traceMatrix[i + 1][j + 1] = StringUtils.fCat(traceMatrix[i + 1][j + 1],
								traceMatrix[i][j + 1].toString(), "_D(", Integer.toString(i + 1), "-",
								Integer.toString(j + 1), ")");

						min = delete;
						// System.out.println("Delete is min:" + delete + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_D(" + (i + 1) + "-" + (j + 1) + ")");
					}

					else if (isMinimum(insert, delete, insert, replace))
					{
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i + 1][j] + "_I(" + (i + 1) + "-" + (j + 1) +
						// ")");

						traceMatrix[i + 1][j + 1] = StringUtils.fCat(traceMatrix[i + 1][j + 1],
								traceMatrix[i + 1][j].toString(), "_I(", Integer.toString(i + 1), "-",
								Integer.toString(j + 1), ")");
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i + 1][j].toString()).append("_I(")
						// .append(Integer.toString(i + 1)).append("-").append(Integer.toString(j + 1))
						// .append(")");

						min = insert;
						// System.out.println("Insert is min:" + insert + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_I(" + (i + 1) + "-" + (j + 1) + ")");
					}
					else if (isMinimum(replace, delete, insert, replace))
					{
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_S(" + (i + 1) + "-" + (j + 1) + ")");

						traceMatrix[i + 1][j + 1] = StringUtils.fCat(traceMatrix[i + 1][j + 1],
								traceMatrix[i][j].toString(), "_S(", Integer.toString(i + 1), "-",
								Integer.toString(j + 1), ")");

						// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j].toString()).append("_S(")
						// .append(Integer.toString(i + 1)).append("-").append(Integer.toString(j + 1))
						// .append(")");

						min = replace;
						// System.out.println("replace is min:" + replace + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_S(" + (i + 1) + "-" + (j + 1) + ")");
					}

					if (min == -9999)
					{
						System.out.println(PopUps.getTracedErrorMsg("Error in minDistance"));
					}

					dist[i + 1][j + 1] = min;
				}
			}
		}

		if (VerbosityConstants.verboseLevenstein)
		// iterate though, and check last char
		{
			System.out.println("length of traces in Trace Matrix as multiple of 7: ");
			for (int i = 0; i <= len1; i++)
			{
				for (int j = 0; j <= len2; j++)
				{
					System.out.print(traceMatrix[i][j].length() / 7.0 + "|");
				}
				System.out.println();
			}

			// //start of temp curtain 3 April 2017
			// System.out.println(" Trace Matrix: ");
			// for (int i = 0; i <= len1; i++)
			// {
			// for (int j = 0; j <= len2; j++)
			// {
			// System.out.print(traceMatrix[i][j] + "|");
			// }
			// System.out.println();
			// }
			//
			//
			// System.out.println("length of traces in Trace Matrix: ");
			// for (int i = 0; i <= len1; i++)
			// {
			// for (int j = 0; j <= len2; j++)
			// {
			// System.out.print(traceMatrix[i][j].length() + "|");
			// }
			// System.out.println();
			// }

			//
			// System.out.println(" Distance Matrix: ");
			// for (int i = 0; i <= len1; i++)
			// {
			// for (int j = 0; j <= len2; j++)
			// {
			// System.out.print(dist[i][j] + "|");
			// }
			// System.out.println();
			// }
			//
			// System.out.println("Resultant Distance = " + new Double(dist[len1][len2]));
			// System.out.println("Resultant Trace = " + traceMatrix[len1][len2].toString());
			// //end of temp curtain 3 April 2017

			System.out.println(" -------- ");
		}

		// long performanceTime2 = System.currentTimeMillis();
		// WritingToFile.appendLineToFileAbsolute(
		// Integer.toString(word1.length()) + "," + Integer.toString(word2.length()) + ","
		// + Long.toString(performanceTime2 - performanceTime1) + "\n",
		// Constant.getCommonPath() + "MySimpleLevenshteinDistanceTimeTakenInms.csv");
		return new Pair<String, Double>(traceMatrix[len1][len2].toString(), new Double(dist[len1][len2]));
	}
	///// end of faster

	///// start of faster v3
	/**
	 * faster v3
	 * <p>
	 * Fork of org.activity.distances.AlignmentBasedDistance.getMySimpleLevenshteinDistance(String, String, int, int,
	 * int) for faster performance by optimising string concatenation. ref:
	 * http://stackoverflow.com/questions/10078912/best-practices-performance-mixing-stringbuilder-append-with-string-concat
	 * <p>
	 * Computes Levenshtein distance between the given strings.</br>
	 * 
	 * Weight of insertion = insertWt * abs(diff(insertedVal - medianValOfOtherString)) </br>
	 * Weight of deletion = deleteWt * abs(diff(deletedVal - medianValOfOtherString)) </br>
	 * Weight of replacement = replaceWt * abs(diff(replaceVal - original))
	 * 
	 * right to left: insertion? top to down: deletion
	 * 
	 * @since April 3, 2017
	 * @param word1
	 * @param word2
	 * @param insertWt
	 * @param deleteWt
	 * @param replaceWt
	 * @return Pair{Levenshtein distance,trace of operations}
	 */
	public static Pair<String, Double> getMySimpleLevenshteinDistanceBeforeMar1_2018(String word1, String word2,
			int insertWt, int deleteWt, int replaceWt)// , TraceMatrix traceMatrix)
	{
		// TraceMatrix traceMatrix = Constant.reusableTraceMatrix;
		// traceMatrix.resetLengthOfCells();
		boolean useHierarchicalDistance = Constant.useHierarchicalDistance;
		HashMap<String, Double> catIDsHierarchicalDistance = null;
		if (useHierarchicalDistance)
		{
			catIDsHierarchicalDistance = DomainConstants.catIDsHierarchicalDistance;
		}

		TraceMatrixLeaner1 traceMatrix = new TraceMatrixLeaner1(word1.length(), word2.length());

		// long performanceTime1 = System.currentTimeMillis();
		if (VerbosityConstants.verboseLevenstein)// Constant.verbose ||
		{
			System.out.println("inside getMySimpleLevenshteinDistance  for word1=" + word1 + "  word2=" + word2
					+ " with insertWt=" + insertWt + " with deleteWt=" + deleteWt + " with replaceWt=" + replaceWt);
		}
		int len1 = word1.length();
		int len2 = word2.length();

		// len1+1, len2+1, because finally return dp[len1][len2]
		double[][] dist = new double[len1 + 1][len2 + 1];
		// StringBuilder[][] traceMatrix = new StringBuilder[len1 + 1][len2 + 1];

		traceMatrix.resetLengthOfCells();
		// for (int i = 0; i <= len1; i++)
		// {
		// for (int j = 0; j <= len2; j++)
		// {
		// traceMatrix[i][j] = new StringBuilder();
		// }
		// }

		dist[0][0] = 0;

		for (int i = 1; i <= len1; i++)
		{
			dist[i][0] = i;
			// traceMatrix.addCharsToCell(i, 0, traceMatrix.getCellAtIndex(i - 1, 0), '_', 'D', '(', (char) (i + '0'),
			// '-', '0', ')');
			// (char)(i+'0') converts i to char i safely and not disturbed by ascii value;
			traceMatrix.addCharsToCell(i, 0, traceMatrix.getCellAtIndex(i - 1, 0), '_', 'D', '(', i, '-', 0, ')');
			// traceMatrix[i][0].append(traceMatrix[i - 1][0] + "_D(" + (i) + "-" + "0)");
		}

		for (int j = 1; j <= len2; j++)
		{
			dist[0][j] = j;
			// traceMatrix.addCharsToCell(0, j, traceMatrix.getCellAtIndex(0, j - 1), '_', 'I', '(', '0', '-',
			// (char) (j + '0'), ')');
			traceMatrix.addCharsToCell(0, j, traceMatrix.getCellAtIndex(0, j - 1), '_', 'I', '(', 0, '-', j, ')');
			// traceMatrix[0][j].append(traceMatrix[0][j - 1] + "_I(0" + "-" + j + ")");
		}

		// iterate though, and check last char
		for (int i = 0; i < len1; i++)
		{
			char c1 = word1.charAt(i);
			for (int j = 0; j < len2; j++)
			{
				char c2 = word2.charAt(j);

				// System.out.println("\nComparing " + c1 + " and " + c2);
				// if last two chars equal
				if (c1 == c2)
				{
					// update dp value for +1 length
					dist[i + 1][j + 1] = dist[i][j];

					traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j), '_', 'N', '(', i + 1,
							'-', j + 1, ')');
					// traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j), '_', 'N', '(',
					// (char) (i + 1 + '0'), '-', (char) (j + 1 + '0'), ')');
					// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_N(" + (i + 1) + "-" + (j + 1) + ")");
					// System.out.println("Equal" + " Trace " + traceMatrix[i + 1][j + 1]);// "_N(" + (i + 1) + "-" + (j
					// + 1) + ")");
				}
				else
				{
					double replace = dist[i][j] + replaceWt;// 2; //diagonally previous, see slides from STANFORD NLP
					// on // min edit distance
					if (useHierarchicalDistance)
					{
						// Double hierWt = catIDsHierarchicalDistance.get(String.valueOf(c1) + String.valueOf(c2));
						// TODO: check if it is actually using the hierwt, we change to StringBuilder after prv verified
						// version
						Double hierWt = catIDsHierarchicalDistance
								.get(new StringBuilder(2).append(c1).append(c2).toString());

						if (hierWt == null)
						{
							System.err.println(
									PopUps.getTracedErrorMsg("Error in levenshtein distance: no hier dist found for: "
											+ String.valueOf(c1) + String.valueOf(c2)) + " hierWt= " + hierWt);
						}
						replace = dist[i][j] + replaceWt * hierWt;// catIDsHierarchicalDistance.get(String.valueOf(c1) +
																	// String.valueOf(c2));
					}

					double delete = dist[i][j + 1] + deleteWt;// 1;//deletion --previous row, i.e, cell above
					double insert = dist[i + 1][j] + insertWt;// 1;// insertion --previous column, i.e, cell on left
					// System.out.println("replace =" + replace + " insert =" + insert + " deleteWt =" + delete);
					// int min = replace > insert ? insert : replace;
					// min = delete > min ? min : delete;
					//
					double min = -9999;

					if (isMinimum(delete, delete, insert, replace))
					{
						traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j + 1), '_', 'D', '(',
								i + 1, '-', j + 1, ')');
						// traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j + 1), '_', 'D', '(',
						// (char) (i + 1 + '0'), '-', (char) (j + 1 + '0'), ')');
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j + 1] + "_D(" + (i + 1) + "-" + (j + 1) +
						// ")");
						min = delete;
						// System.out.println("Delete is min:" + delete + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_D(" + (i + 1) + "-" + (j + 1) + ")");
					}

					else if (isMinimum(insert, delete, insert, replace))
					{
						traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i + 1, j), '_', 'I', '(',
								i + 1, '-', j + 1, ')');
						// traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i + 1, j), '_', 'I', '(',
						// (char) (i + 1 + '0'), '-', (char) (j + 1 + '0'), ')');
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i + 1][j] + "_I(" + (i + 1) + "-" + (j + 1) +
						// ")");
						min = insert;
						// System.out.println("Insert is min:" + insert + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_I(" + (i + 1) + "-" + (j + 1) + ")");
					}
					else if (isMinimum(replace, delete, insert, replace))
					{
						traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j), '_', 'S', '(', i + 1,
								'-', j + 1, ')');
						// traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j), '_', 'S', '(',
						// (char) (i + 1 + '0'), '-', (char) (j + 1 + '0'), ')');
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_S(" + (i + 1) + "-" + (j + 1) + ")");
						min = replace;
						// System.out.println("replace is min:" + replace + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_S(" + (i + 1) + "-" + (j + 1) + ")");
					}

					if (min == -9999)
					{
						System.out.println(PopUps.getTracedErrorMsg("Error in minDistance"));
					}

					dist[i + 1][j + 1] = min;
				}
			}
		}

		String resultantTrace = String.valueOf(traceMatrix.getCellAtIndex(len1, len2));
		Double resultantDistance = Double.valueOf(dist[len1][len2]);

		if (VerbosityConstants.verboseLevenstein)
		// iterate though, and check last char
		{
			System.out.println(" Trace Matrix: \n" + traceMatrix.toString());
			// for (int i = 0; i <= len1; i++)
			// {
			// for (int j = 0; j <= len2; j++)
			// {
			// System.out.print(traceMatrix[i][j] + "|");
			// }
			// System.out.println();
			// }

			System.out.println("  Distance Matrix: ");
			for (int i = 0; i <= len1; i++)
			{
				for (int j = 0; j <= len2; j++)
				{
					System.out.print(dist[i][j] + "|");
				}
				System.out.println();
			}

			System.out.println("Resultant Distance = " + resultantDistance);// new Double(dist[len1][len2]));
			System.out.println("Resultant Trace = " + resultantTrace);// traceMatrix[len1][len2].toString());
			System.out.println(" -------- ");
		}

		// long performanceTime2 = System.currentTimeMillis();
		// WritingToFile.appendLineToFileAbsolute(
		// Integer.toString(word1.length()) + "," + Integer.toString(word2.length()) + ","
		// + Long.toString(performanceTime2 - performanceTime1) + "\n",
		// Constant.getCommonPath() + "MySimpleLevenshteinDistanceTimeTakenInms.csv");
		return new Pair<String, Double>(resultantTrace, resultantDistance);
	}
	///// end of faster

	///// end of faster v3

	/**
	 * Created as a glue for older code in the process of refactoring to v4 of getMySimpleLevenshteinDistance
	 * 
	 * @since Mar 1 2018
	 * @param word1
	 * @param word2
	 * @param insertWt
	 * @param deleteWt
	 * @param replaceWt
	 * @return
	 */
	public static Pair<String, Double> getMySimpleLevenshteinDistancePair(String word1, String word2, int insertWt,
			int deleteWt, int replaceWt)// , TraceMatrix traceMatrix)
	{
		Triple<String, Double, Triple<char[], int[], int[]>> res = getMySimpleLevenshteinDistance(word1, word2,
				insertWt, deleteWt, replaceWt);
		return new Pair<>(res.getFirst(), res.getSecond());
	}

	///// start of faster v4
	/**
	 * faster v4: minimising splits to improve performance
	 * <p>
	 * Fork of org.activity.distances.AlignmentBasedDistance.getMySimpleLevenshteinDistance(String, String, int, int,
	 * int) for faster performance by optimising string concatenation. ref:
	 * http://stackoverflow.com/questions/10078912/best-practices-performance-mixing-stringbuilder-append-with-string-concat
	 * <p>
	 * Computes Levenshtein distance between the given strings.</br>
	 * 
	 * Weight of insertion = insertWt * abs(diff(insertedVal - medianValOfOtherString)) </br>
	 * Weight of deletion = deleteWt * abs(diff(deletedVal - medianValOfOtherString)) </br>
	 * Weight of replacement = replaceWt * abs(diff(replaceVal - original))
	 * 
	 * right to left: insertion? top to down: deletion
	 * 
	 * @since Mar 1, 2018
	 * @param word1
	 * @param word2
	 * @param insertWt
	 * @param deleteWt
	 * @param replaceWt
	 * @return Triple{resultantTrace, resultantDistance, Triple{DISNTrace,coordTraces.getFirst(),
	 *         coordTraces.getSecond()}}
	 *         <p>
	 *         Trace =_I(0-1)_I(0-2)_I(0-3)_D(1-3)_D(2-3)_D(3-3)_N(4-4) <br/>
	 *         simpleLevenshteinDistance112=6.0<br/>
	 *         DINSTrace=IIIDDDN <br/>
	 *         third_second=[0, 0, 0, 1, 2, 3, 4] <br/>
	 *         third_third=[1, 2, 3, 3, 3, 3, 4]
	 */
	public static Triple<String, Double, Triple<char[], int[], int[]>> getMySimpleLevenshteinDistance(String word1,
			String word2, int insertWt, int deleteWt, int replaceWt)// , TraceMatrix traceMatrix)
	{
		// TraceMatrix traceMatrix = Constant.reusableTraceMatrix;
		// traceMatrix.resetLengthOfCells();
		boolean useHierarchicalDistance = Constant.useHierarchicalDistance;
		HashMap<String, Double> catIDsHierarchicalDistance = null;
		if (useHierarchicalDistance)
		{
			catIDsHierarchicalDistance = DomainConstants.catIDsHierarchicalDistance;
		}

		TraceMatrixLeaner1 traceMatrix = new TraceMatrixLeaner1(word1.length(), word2.length());

		// long performanceTime1 = System.currentTimeMillis();
		if (VerbosityConstants.verboseLevenstein)// Constant.verbose ||
		{
			System.out.println("inside getMySimpleLevenshteinDistance  for word1=" + word1 + "  word2=" + word2
					+ " with insertWt=" + insertWt + " with deleteWt=" + deleteWt + " with replaceWt=" + replaceWt);
		}
		int len1 = word1.length();
		int len2 = word2.length();

		// len1+1, len2+1, because finally return dp[len1][len2]
		double[][] dist = new double[len1 + 1][len2 + 1];
		// StringBuilder[][] traceMatrix = new StringBuilder[len1 + 1][len2 + 1];

		traceMatrix.resetLengthOfCells();
		// for (int i = 0; i <= len1; i++)
		// {
		// for (int j = 0; j <= len2; j++)
		// {
		// traceMatrix[i][j] = new StringBuilder();
		// }
		// }

		dist[0][0] = 0;

		for (int i = 1; i <= len1; i++)
		{
			dist[i][0] = i;
			// traceMatrix.addCharsToCell(i, 0, traceMatrix.getCellAtIndex(i - 1, 0), '_', 'D', '(', (char) (i + '0'),
			// '-', '0', ')');
			// (char)(i+'0') converts i to char i safely and not disturbed by ascii value;
			traceMatrix.addCharsToCell(i, 0, traceMatrix.getCellAtIndex(i - 1, 0), '_', 'D', '(', i, '-', 0, ')');
			// traceMatrix[i][0].append(traceMatrix[i - 1][0] + "_D(" + (i) + "-" + "0)");
		}

		for (int j = 1; j <= len2; j++)
		{
			dist[0][j] = j;
			// traceMatrix.addCharsToCell(0, j, traceMatrix.getCellAtIndex(0, j - 1), '_', 'I', '(', '0', '-',
			// (char) (j + '0'), ')');
			traceMatrix.addCharsToCell(0, j, traceMatrix.getCellAtIndex(0, j - 1), '_', 'I', '(', 0, '-', j, ')');
			// traceMatrix[0][j].append(traceMatrix[0][j - 1] + "_I(0" + "-" + j + ")");
		}

		// iterate though, and check last char
		for (int i = 0; i < len1; i++)
		{
			char c1 = word1.charAt(i);
			for (int j = 0; j < len2; j++)
			{
				char c2 = word2.charAt(j);

				// System.out.println("\nComparing " + c1 + " and " + c2);
				// if last two chars equal
				if (c1 == c2)
				{
					// update dp value for +1 length
					dist[i + 1][j + 1] = dist[i][j];

					traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j), '_', 'N', '(', i + 1,
							'-', j + 1, ')');
					// traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j), '_', 'N', '(',
					// (char) (i + 1 + '0'), '-', (char) (j + 1 + '0'), ')');
					// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_N(" + (i + 1) + "-" + (j + 1) + ")");
					// System.out.println("Equal" + " Trace " + traceMatrix[i + 1][j + 1]);// "_N(" + (i + 1) + "-" + (j
					// + 1) + ")");
				}
				else
				{
					double replace = dist[i][j] + replaceWt;// 2; //diagonally previous, see slides from STANFORD NLP
					// on // min edit distance
					if (useHierarchicalDistance)
					{
						// Double hierWt = catIDsHierarchicalDistance.get(String.valueOf(c1) + String.valueOf(c2));
						// TODO: check if it is actually using the hierwt, we change to StringBuilder after prv verified
						// version
						Double hierWt = catIDsHierarchicalDistance
								.get(new StringBuilder(2).append(c1).append(c2).toString());

						if (hierWt == null)
						{
							System.err.println(
									PopUps.getTracedErrorMsg("Error in levenshtein distance: no hier dist found for: "
											+ String.valueOf(c1) + String.valueOf(c2)) + " hierWt= " + hierWt);
						}
						replace = dist[i][j] + replaceWt * hierWt;// catIDsHierarchicalDistance.get(String.valueOf(c1) +
																	// String.valueOf(c2));
					}

					double delete = dist[i][j + 1] + deleteWt;// 1;//deletion --previous row, i.e, cell above
					double insert = dist[i + 1][j] + insertWt;// 1;// insertion --previous column, i.e, cell on left
					// System.out.println("replace =" + replace + " insert =" + insert + " deleteWt =" + delete);
					// int min = replace > insert ? insert : replace;
					// min = delete > min ? min : delete;
					//
					double min = -9999;

					if (isMinimum(delete, delete, insert, replace))
					{
						traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j + 1), '_', 'D', '(',
								i + 1, '-', j + 1, ')');
						// traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j + 1), '_', 'D', '(',
						// (char) (i + 1 + '0'), '-', (char) (j + 1 + '0'), ')');
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j + 1] + "_D(" + (i + 1) + "-" + (j + 1) +
						// ")");
						min = delete;
						// System.out.println("Delete is min:" + delete + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_D(" + (i + 1) + "-" + (j + 1) + ")");
					}

					else if (isMinimum(insert, delete, insert, replace))
					{
						traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i + 1, j), '_', 'I', '(',
								i + 1, '-', j + 1, ')');
						// traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i + 1, j), '_', 'I', '(',
						// (char) (i + 1 + '0'), '-', (char) (j + 1 + '0'), ')');
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i + 1][j] + "_I(" + (i + 1) + "-" + (j + 1) +
						// ")");
						min = insert;
						// System.out.println("Insert is min:" + insert + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_I(" + (i + 1) + "-" + (j + 1) + ")");
					}
					else if (isMinimum(replace, delete, insert, replace))
					{
						traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j), '_', 'S', '(', i + 1,
								'-', j + 1, ')');
						// traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j), '_', 'S', '(',
						// (char) (i + 1 + '0'), '-', (char) (j + 1 + '0'), ')');
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_S(" + (i + 1) + "-" + (j + 1) + ")");
						min = replace;
						// System.out.println("replace is min:" + replace + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_S(" + (i + 1) + "-" + (j + 1) + ")");
					}

					if (min == -9999)
					{
						System.out.println(PopUps.getTracedErrorMsg("Error in minDistance"));
					}

					dist[i + 1][j + 1] = min;
				}
			}
		}

		String resultantTrace = String.valueOf(traceMatrix.getCellAtIndex(len1, len2));
		char[] DISNTrace = traceMatrix.getCellAtIndexOnlyDISN(len1, len2);
		Pair<int[], int[]> coordTraces = traceMatrix.getCellAtIndexOnlyCoordinates(len1, len2);

		Double resultantDistance = Double.valueOf(dist[len1][len2]);

		if (VerbosityConstants.verboseLevenstein)
		// iterate though, and check last char
		{
			System.out.println(" Trace Matrix here--: \n" + traceMatrix.toString());
			// for (int i = 0; i <= len1; i++)
			// {
			// for (int j = 0; j <= len2; j++)
			// {
			// System.out.print(traceMatrix[i][j] + "|");
			// }
			// System.out.println();
			// }

			System.out.println("  Distance Matrix: ");
			for (int i = 0; i <= len1; i++)
			{
				for (int j = 0; j <= len2; j++)
				{
					System.out.print(dist[i][j] + "|");
				}
				System.out.println();
			}

			System.out.println("Resultant Distance = " + resultantDistance);// new Double(dist[len1][len2]));
			System.out.println("Resultant Trace = " + resultantTrace);// traceMatrix[len1][len2].toString());
			System.out.println(" -------- ");
		}

		// long performanceTime2 = System.currentTimeMillis();
		// WritingToFile.appendLineToFileAbsolute(
		// Integer.toString(word1.length()) + "," + Integer.toString(word2.length()) + ","
		// + Long.toString(performanceTime2 - performanceTime1) + "\n",
		// Constant.getCommonPath() + "MySimpleLevenshteinDistanceTimeTakenInms.csv");
		return new Triple<>(resultantTrace, resultantDistance,
				new Triple<char[], int[], int[]>(DISNTrace, coordTraces.getFirst(), coordTraces.getSecond()));
	}
	///// end of faster

	///// end of faster v4

	// ~~~~~~~~~~~~~`
	/**
	 * faster v4: minimising splits to improve performance
	 * <p>
	 * Fork of org.activity.distances.AlignmentBasedDistance.getMySimpleLevenshteinDistance(String, String, int, int,
	 * int) for allowing replace wt modifier map
	 * <p>
	 * Computes Levenshtein distance between the given strings.</br>
	 * 
	 * Weight of insertion = insertWt * abs(diff(insertedVal - medianValOfOtherString)) </br>
	 * Weight of deletion = deleteWt * abs(diff(deletedVal - medianValOfOtherString)) </br>
	 * Weight of replacement = replaceWt * abs(diff(replaceVal - original))
	 * 
	 * right to left: insertion? top to down: deletion
	 * 
	 * @param word1
	 * @param word2
	 * @param insertWt
	 * @param deleteWt
	 * @param replaceWt
	 * @param replaceWtMultiplierMap
	 * @return Triple{resultantTrace, resultantDistance, Triple{DISNTrace,coordTraces.getFirst(),
	 *         coordTraces.getSecond()}}
	 *         <p>
	 *         Trace =_I(0-1)_I(0-2)_I(0-3)_D(1-3)_D(2-3)_D(3-3)_N(4-4) <br/>
	 *         simpleLevenshteinDistance112=6.0<br/>
	 *         DINSTrace=IIIDDDN <br/>
	 *         third_second=[0, 0, 0, 1, 2, 3, 4] <br/>
	 *         third_third=[1, 2, 3, 3, 3, 3, 4]
	 * @since Aug 3, 2018
	 */
	public static Triple<String, Double, Triple<char[], int[], int[]>> getMySimpleLevenshteinDistance(String word1,
			String word2, int insertWt, int deleteWt, int replaceWt, Map<String, Double> replaceWtMultiplierMap)
	{
		// boolean useHierarchicalDistance = Constant.useHierarchicalDistance;
		// HashMap<String, Double> catIDsHierarchicalDistance = null;
		// if (useHierarchicalDistance){ catIDsHierarchicalDistance = DomainConstants.catIDsHierarchicalDistance;}
		boolean hasReplaceWtModifierMap = replaceWtMultiplierMap == null ? false : true;

		TraceMatrixLeaner1 traceMatrix = new TraceMatrixLeaner1(word1.length(), word2.length());

		// long performanceTime1 = System.currentTimeMillis();
		if (VerbosityConstants.verboseLevenstein)// Constant.verbose ||
		{
			System.out.println("inside getMySimpleLevenshteinDistance  for word1=" + word1 + "  word2=" + word2
					+ " with insertWt=" + insertWt + " with deleteWt=" + deleteWt + " with replaceWt=" + replaceWt);
		}
		int len1 = word1.length();
		int len2 = word2.length();

		// len1+1, len2+1, because finally return dp[len1][len2]
		double[][] dist = new double[len1 + 1][len2 + 1];
		// StringBuilder[][] traceMatrix = new StringBuilder[len1 + 1][len2 + 1];

		traceMatrix.resetLengthOfCells();
		// for (int i = 0; i <= len1; i++){ for (int j = 0; j <= len2; j++){traceMatrix[i][j] = new StringBuilder();}}

		dist[0][0] = 0;

		for (int i = 1; i <= len1; i++)
		{
			dist[i][0] = i;
			// traceMatrix.addCharsToCell(i, 0, traceMatrix.getCellAtIndex(i - 1, 0), '_', 'D', '(', (char) (i + '0'),
			// '-', '0', ')');
			// (char)(i+'0') converts i to char i safely and not disturbed by ascii value;
			traceMatrix.addCharsToCell(i, 0, traceMatrix.getCellAtIndex(i - 1, 0), '_', 'D', '(', i, '-', 0, ')');
			// traceMatrix[i][0].append(traceMatrix[i - 1][0] + "_D(" + (i) + "-" + "0)");
		}

		for (int j = 1; j <= len2; j++)
		{
			dist[0][j] = j;
			// traceMatrix.addCharsToCell(0, j, traceMatrix.getCellAtIndex(0, j - 1), '_', 'I', '(', '0', '-',
			// (char) (j + '0'), ')');
			traceMatrix.addCharsToCell(0, j, traceMatrix.getCellAtIndex(0, j - 1), '_', 'I', '(', 0, '-', j, ')');
			// traceMatrix[0][j].append(traceMatrix[0][j - 1] + "_I(0" + "-" + j + ")");
		}

		// iterate though, and check last char
		for (int i = 0; i < len1; i++)
		{
			char c1 = word1.charAt(i);
			for (int j = 0; j < len2; j++)
			{
				char c2 = word2.charAt(j);

				// System.out.println("\nComparing " + c1 + " and " + c2);
				// if last two chars equal
				if (c1 == c2)
				{
					// update dp value for +1 length
					dist[i + 1][j + 1] = dist[i][j];

					traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j), '_', 'N', '(', i + 1,
							'-', j + 1, ')');
					// traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j), '_', 'N', '(',
					// (char) (i + 1 + '0'), '-', (char) (j + 1 + '0'), ')');
					// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_N(" + (i + 1) + "-" + (j + 1) + ")");
					// System.out.println("Equal" + " Trace " + traceMatrix[i + 1][j + 1]);// "_N(" + (i + 1) + "-" + (j
					// + 1) + ")");
				}
				else
				{
					double replaceWtMultiplier = 1;
					// Start of added on 3 Aug 2018
					if (hasReplaceWtModifierMap)
					{
						Double res = replaceWtMultiplierMap.get(c1 + "_" + c2);
						if (res == null)
						{
							PopUps.printTracedErrorMsgWithExit("Error: no entry found in replaceWtMultiplierMap for :"
									+ String.valueOf(c1) + "_" + String.valueOf(c2));
						}
						replaceWtMultiplier = res;
					}
					// End of added on 3 Aug 2018

					double replace = dist[i][j] + (replaceWtMultiplier * replaceWt);// 2; //diagonally previous, see
					// slides from STANFORD NLP on // min edit distance
					double delete = dist[i][j + 1] + deleteWt;// 1;//deletion --previous row, i.e, cell above
					double insert = dist[i + 1][j] + insertWt;// 1;// insertion --previous column, i.e, cell on left
					// System.out.println("replace =" + replace + " insert =" + insert + " deleteWt =" + delete);
					// int min = replace > insert ? insert : replace;
					// min = delete > min ? min : delete;
					double min = -9999;

					if (isMinimum(delete, delete, insert, replace))
					{
						traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j + 1), '_', 'D', '(',
								i + 1, '-', j + 1, ')');
						// traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j + 1), '_', 'D', '(',
						// (char) (i + 1 + '0'), '-', (char) (j + 1 + '0'), ')');
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j + 1] + "_D(" + (i + 1) + "-" + (j + 1) +
						// ")");
						min = delete;
						// System.out.println("Delete is min:" + delete + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_D(" + (i + 1) + "-" + (j + 1) + ")");
					}
					else if (isMinimum(insert, delete, insert, replace))
					{
						traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i + 1, j), '_', 'I', '(',
								i + 1, '-', j + 1, ')');
						// traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i + 1, j), '_', 'I', '(',
						// (char) (i + 1 + '0'), '-', (char) (j + 1 + '0'), ')');
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i + 1][j] + "_I(" + (i + 1) + "-" + (j + 1) +
						// ")");
						min = insert;
						// System.out.println("Insert is min:" + insert + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_I(" + (i + 1) + "-" + (j + 1) + ")");
					}
					else if (isMinimum(replace, delete, insert, replace))
					{
						traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j), '_', 'S', '(', i + 1,
								'-', j + 1, ')');
						// traceMatrix.addCharsToCell(i + 1, j + 1, traceMatrix.getCellAtIndex(i, j), '_', 'S', '(',
						// (char) (i + 1 + '0'), '-', (char) (j + 1 + '0'), ')');
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_S(" + (i + 1) + "-" + (j + 1) + ")");
						min = replace;
						// System.out.println("replace is min:" + replace + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_S(" + (i + 1) + "-" + (j + 1) + ")");
					}

					if (min == -9999)
					{
						System.out.println(PopUps.getTracedErrorMsg("Error in minDistance"));
					}

					dist[i + 1][j + 1] = min;
				}
			}
		}

		String resultantTrace = String.valueOf(traceMatrix.getCellAtIndex(len1, len2));
		char[] DISNTrace = traceMatrix.getCellAtIndexOnlyDISN(len1, len2);
		Pair<int[], int[]> coordTraces = traceMatrix.getCellAtIndexOnlyCoordinates(len1, len2);

		Double resultantDistance = Double.valueOf(dist[len1][len2]);

		if (VerbosityConstants.verboseLevenstein)
		// iterate though, and check last char
		{
			System.out.println(" Trace Matrix here--: \n" + traceMatrix.toString());
			// for (int i = 0; i <= len1; i++)
			// {
			// for (int j = 0; j <= len2; j++)
			// {
			// System.out.print(traceMatrix[i][j] + "|");
			// }
			// System.out.println();
			// }

			System.out.println("  Distance Matrix: ");
			for (int i = 0; i <= len1; i++)
			{
				for (int j = 0; j <= len2; j++)
				{
					System.out.print(dist[i][j] + "|");
				}
				System.out.println();
			}

			System.out.println("Resultant Distance = " + resultantDistance);// new Double(dist[len1][len2]));
			System.out.println("Resultant Trace = " + resultantTrace);// traceMatrix[len1][len2].toString());
			System.out.println(" -------- ");
		}

		// long performanceTime2 = System.currentTimeMillis();
		// WritingToFile.appendLineToFileAbsolute(
		// Integer.toString(word1.length()) + "," + Integer.toString(word2.length()) + ","
		// + Long.toString(performanceTime2 - performanceTime1) + "\n",
		// Constant.getCommonPath() + "MySimpleLevenshteinDistanceTimeTakenInms.csv");
		return new Triple<>(resultantTrace, resultantDistance,
				new Triple<char[], int[], int[]>(DISNTrace, coordTraces.getFirst(), coordTraces.getSecond()));
	}
	// ~~~~~~~~~~~~`

	/**
	 * Compute levenshtein dist between the words in two lists and return the result for the least dist.
	 * 
	 * @since May 19, 2017
	 * @until Mar 1, 2018
	 * @param word1
	 * @param word2
	 * @param insertWt
	 * @param deleteWt
	 * @param replaceWt
	 * @return Pair{Levenshtein distance,trace of operations}
	 */
	public static Pair<String, Double> getLowestMySimpleLevenshteinDistancePair(ArrayList<String> word1s,
			ArrayList<String> word2s, int insertWt, int deleteWt, int replaceWt)// , TraceMatrix traceMatrix)
	{
		Pair<String, Double> lowestRes = new Pair<>();

		ArrayList<Pair<String, Double>> levenshteinDists = new ArrayList<>();

		for (String word1 : word1s)
		{
			for (String word2 : word2s)
			{
				levenshteinDists.add(getMySimpleLevenshteinDistancePair(word1, word2, insertWt, deleteWt, replaceWt));
			}
		}

		double min = Double.MAX_VALUE;
		for (Pair<String, Double> p : levenshteinDists)
		{
			double val = p.getSecond();
			if (val < min)
			{
				lowestRes = p;
				min = val;
			}
		}

		if (VerbosityConstants.verboseLevenstein)
		{
			System.out.println("Word1s: " + word1s.toString() + "  Word2s:" + word2s.toString());
			if (word1s.size() > 1 || word2s.size() > 1)
			{
				System.out.println("more than one word!");
			}
			System.out.println("levenshteinDists = " + levenshteinDists);
			System.out.println("lowestRes = " + lowestRes.toString());
		}

		return lowestRes;
	}

	/**
	 * Compute levenshtein dist between the words in two lists and return the result for the least dist.
	 * <p>
	 * uses getLocallyUniqueCharCodeMap()
	 * <p>
	 * (This method abstracted some code from the method
	 * HJEditDistance.getHJEditDistanceWithTrace_CleanedApril13_2018())
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @param givenDimension
	 * @param insertWt
	 * @param deleteWt
	 * @param replaceWt
	 * @return Triple{resultantTrace, resultantDistance, Triple{DISNTrace,coordTraces.getFirst(),
	 *         coordTraces.getSecond()}}
	 *         <p>
	 *         Trace =_I(0-1)_I(0-2)_I(0-3)_D(1-3)_D(2-3)_D(3-3)_N(4-4) <br/>
	 *         simpleLevenshteinDistance112=6.0<br/>
	 *         DINSTrace=IIIDDDN <br/>
	 *         third_second=[0, 0, 0, 1, 2, 3, 4] <br/>
	 *         third_third=[1, 2, 3, 3, 3, 3, 4]
	 * 
	 * @since April 14, 2018
	 */
	public static Triple<String, Double, Triple<char[], int[], int[]>> getLowestMySimpleLevenshteinDistance(
			ArrayList<ActivityObject> activityObjects1, ArrayList<ActivityObject> activityObjects2,
			PrimaryDimension givenDimension, int insertWt, int deleteWt, int replaceWt)
	{
		// long t0, t2, t3, t4, t5, t6;t0 = t2 = t3 = t4 = t5 = t6 = Long.MIN_VALUE;t0 = System.nanoTime();
		// HashMap<Integer, Character> uniqueCharCodes = StringCode.getLocallyUniqueCharCodeMap(activityObjects1,
		// activityObjects2, primaryDimension);
		HashMap<Integer, Character> uniqueCharCodes = StringCode.getLocallyUniqueCharCodeMap17July2018(activityObjects1,
				activityObjects2, givenDimension);

		Map<String, Double> replaceWtMultiplierMap = null;

		// Start of added on 3 Aug 2018
		if (givenDimension.equals(PrimaryDimension.LocationGridID) && Constant.doWeightedEditDistanceForSecDim)
		{
			// disabled the print on Aug 7 to decrease size of consoleLogs
			// $$System.out.println("DebugAug3: will createReplaceWtModifierMapForDistSensitiveLocGrids!!");
			replaceWtMultiplierMap = createReplaceWtModifierMapForDistSensitiveLocGrids(uniqueCharCodes);
		}
		// End of added on 3 Aug 2018

		// Start of added on 26 July 2018
		// if (givenDimension.equals(PrimaryDimension.LocationGridID) && Constant.doWeightedEditDistanceForSecDim)
		// {
		// double maxDistanceBetweeenPairs = getMaxDistance(activityObjects1, activityObjects2, givenDimension);
		// }
		// End of added on 26 July 2018
		// t2 = System.nanoTime();
		// Int2CharOpenHashMap uniqueCharCodesFU = StringCode.getLocallyUniqueCharCodeMapFU(activityObjects1,
		// activityObjects2, primaryDimension);
		// t3 = System.nanoTime();
		// multiple string codes when an AO in the list has act name which at desired level can have multiple ids. For
		// example Vineyards is under Community as well as Food
		ArrayList<String> stringCodesForActivityObjects1, stringCodesForActivityObjects2;

		// //start of curtain 17 July 2017
		// if (Constant.HierarchicalCatIDLevelForEditDistance > 0)
		// {// TODO: need to implement this for multi dimensional case, e.g., recommending location
		// // PopUps.printTracedErrorMsgWithExit("Constant.HierarchicalLevelForEditDistance > 0) not implemented yet");
		// stringCodesForActivityObjects1 = StringCode.getStringCodeForActivityObjectsV2(activityObjects1,
		// Constant.HierarchicalCatIDLevelForEditDistance, false);
		// stringCodesForActivityObjects2 = StringCode.getStringCodeForActivityObjectsV2(activityObjects2,
		// Constant.HierarchicalCatIDLevelForEditDistance, false);
		// }
		// else
		// {
		// //end of curtain 17 July 2017
		// t4 = System.nanoTime();
		//// temp start
		// ArrayList<String> stringCodesForActivityObjects1FU = StringCode.getStringCodesForActivityObjectsFU(
		// activityObjects1, primaryDimension, uniqueCharCodesFU, VerbosityConstants.verbose);
		// ArrayList<String> stringCodesForActivityObjects2FU = StringCode.getStringCodesForActivityObjectsFU(
		// activityObjects2, primaryDimension, uniqueCharCodesFU, VerbosityConstants.verbose);
		// t6 = System.nanoTime();

		// String debug9Mar = (t2 - t0) + "," + (t3 - t2) + "," + (t5 - t4) + "," + (t6 - t5) + ","
		// + stringCodesForActivityObjects1.equals(stringCodesForActivityObjects1FU) + ","
		// + stringCodesForActivityObjects2.equals(stringCodesForActivityObjects2FU) + ","
		// + stringCodesForActivityObjects1 + "," + (stringCodesForActivityObjects1FU) + ","
		// + stringCodesForActivityObjects2 + "," + (stringCodesForActivityObjects2FU) + "\n";
		// WritingToFile.appendLineToFileAbsolute(debug9Mar.toString(),
		// Constant.getOutputCoreResultsPath() + "DebugMar9_2018.csv");
		/// temp end
		// }
		stringCodesForActivityObjects1 = StringCode.getStringCodesForActivityObjects17July2018(activityObjects1,
				givenDimension, uniqueCharCodes, VerbosityConstants.verbose);
		stringCodesForActivityObjects2 = StringCode.getStringCodesForActivityObjects17July2018(activityObjects2,
				givenDimension, uniqueCharCodes, VerbosityConstants.verbose);
		// t5 = System.nanoTime();

		return getLowestMySimpleLevenshteinDistance(stringCodesForActivityObjects1, stringCodesForActivityObjects2,
				insertWt, deleteWt, replaceWt, replaceWtMultiplierMap);
		// Map<String, Double> replaceWtModifierMap
		// getMySimpleLevenshteinDistance
	}

	// private static double getMaxDistance(ArrayList<ActivityObject> activityObjects1,
	// ArrayList<ActivityObject> activityObjects2, PrimaryDimension givenDimension)
	// {
	// Set<Integer> gridIndices1 = activityObjects1.stream().map(ao -> ao.getGivenDimensionVal(givenDimension))
	// .flatMap(l -> l.stream()).collect(Collectors.toSet());
	// Set<Integer> gridIndices2 = activityObjects2.stream().map(ao -> ao.getGivenDimensionVal(givenDimension))
	// .flatMap(l -> l.stream()).collect(Collectors.toSet());
	//
	// List<Integer>
	// return 0;
	// }

	/**
	 * 
	 * @param uniqueCharCodes
	 * @return
	 */
	private static Map<String, Double> createReplaceWtModifierMapForDistSensitiveLocGrids(
			HashMap<Integer, Character> uniqueCharCodes)
	{
		double distanceThresholdInKms;
		// HashMap<Integer, Character> uniqueCharCodes
		Map<String, Double> replaceWtModifierMap = new HashMap<>();

		List<Integer> listOfUniqueDimVals = new ArrayList<>(uniqueCharCodes.size());
		listOfUniqueDimVals.addAll(uniqueCharCodes.keySet());

		StringBuilder sb1 = new StringBuilder();// for debugging
		// Allowing redundancy as max num of unique loc grid cant be more than 8+8 (highest MU) and thus total size of
		// map 256 is manageable.
		for (int dimVal1 : listOfUniqueDimVals)
		{
			for (int dimVal2 : listOfUniqueDimVals)
			{
				String key = uniqueCharCodes.get(dimVal1) + "_" + uniqueCharCodes.get(dimVal2);
				Double dist = null;

				if (dimVal1 == dimVal2)
				{
					dist = 0d;
				}
				else
				{
					dist = DomainConstants.getHaversineDistForGridIndexPairs(dimVal1, dimVal2);
				}

				Double dissimilarityScore = getDissimilarityScoreForDist(dist);
				replaceWtModifierMap.put(key, dissimilarityScore);

				// start of for sanity check
				sb1.append(dimVal1 + "," + dimVal2 + "," + uniqueCharCodes.get(dimVal1) + "_"
						+ uniqueCharCodes.get(dimVal2) + "," + dist + "," + dissimilarityScore + "\n");
				// end of for sanity check
			}
		}

		if (VerbosityConstants.writeReplaceWtMultiplierMap)// TODO: temporary for debugging
		{
			WToFile.appendLineToFileAbs(sb1.toString() + "\n",
					Constant.getCommonPath() + "DebugReplaceWtMultiplierMap1.csv");

			StringBuilder sb = new StringBuilder();
			replaceWtModifierMap.entrySet().stream()
					.forEachOrdered(e -> sb.append(e.getKey() + "," + e.getValue() + "\n"));
			WToFile.appendLineToFileAbs(sb.toString() + "\n",
					Constant.getCommonPath() + "DebugReplaceWtMultiplierMap2.csv");
		}

		return replaceWtModifierMap;
	}

	/**
	 * score = 1 - ((thresholdInKms - dist) / thresholdInKms);
	 * 
	 * @param dist
	 * @since August 3 2018
	 */
	private static double getDissimilarityScoreForDist(double dist)
	{
		double thresholdInKms = Constant.maxDistanceThresholdForLocGridDissmilarity;
		double score = -1;

		if (dist > thresholdInKms)
		{
			score = 1;
		}
		else
		{
			score = 1 - ((thresholdInKms - dist) / thresholdInKms);
		}
		if (score > 1 || score < 0)
		{
			PopUps.printTracedErrorMsgWithExit(
					"Error in getDissimilarityScoreForDist: score out of range for dist: " + dist + " score =" + score);
		}
		return score;
	}

	/**
	 * Compute levenshtein dist between the words in two lists and return the result for the least dist.
	 * 
	 * @since Mar 1, 2018
	 * @param word1
	 * @param word2
	 * @param insertWt
	 * @param deleteWt
	 * @param replaceWt
	 * @param replaceWtMultiplierMap
	 *            {String, Double} {id1_id2,wt}
	 * @return Triple{resultantTrace, resultantDistance, Triple{DISNTrace,coordTraces.getFirst(),
	 *         coordTraces.getSecond()}}
	 *         <p>
	 *         Trace =_I(0-1)_I(0-2)_I(0-3)_D(1-3)_D(2-3)_D(3-3)_N(4-4) <br/>
	 *         simpleLevenshteinDistance112=6.0<br/>
	 *         DINSTrace=IIIDDDN <br/>
	 *         third_second=[0, 0, 0, 1, 2, 3, 4] <br/>
	 *         third_third=[1, 2, 3, 3, 3, 3, 4]
	 */
	public static Triple<String, Double, Triple<char[], int[], int[]>> getLowestMySimpleLevenshteinDistance(
			ArrayList<String> word1s, ArrayList<String> word2s, int insertWt, int deleteWt, int replaceWt,
			Map<String, Double> replaceWtMultiplierMap)
	{
		Triple<String, Double, Triple<char[], int[], int[]>> lowestRes = new Triple<>();

		ArrayList<Triple<String, Double, Triple<char[], int[], int[]>>> levenshteinDists = new ArrayList<>();

		for (String word1 : word1s)
		{
			for (String word2 : word2s)
			{
				levenshteinDists.add(getMySimpleLevenshteinDistance(word1, word2, insertWt, deleteWt, replaceWt,
						replaceWtMultiplierMap));
			}
		}

		double min = Double.MAX_VALUE;
		for (Triple<String, Double, Triple<char[], int[], int[]>> p : levenshteinDists)
		{
			double val = p.getSecond();
			if (val < min)
			{
				lowestRes = p;
				min = val;
			}
		}

		if (VerbosityConstants.verboseLevenstein)
		{
			System.out.println("Word1s: " + word1s.toString() + "  Word2s:" + word2s.toString());
			if (word1s.size() > 1 || word2s.size() > 1)
			{
				System.out.println("more than one word!");
			}
			// $$ System.out.println("levenshteinDists = " + levenshteinDists);

			StringBuilder sb = new StringBuilder();
			for (Triple<String, Double, Triple<char[], int[], int[]>> s : levenshteinDists)
			{
				Triple<char[], int[], int[]> third = s.getThird();

				sb.append("trace= " + s.getFirst() + ", dist=" + s.getSecond() + ", { DISNTrace="
						+ Arrays.toString(third.getFirst()) + ", coordTraces=" + Arrays.toString(third.getSecond())
						+ ", coordTraces=" + Arrays.toString(third.getThird()) + "}");

			}

			System.out.println("levenshteinDists = " + sb.toString());
			// System.out.println("lowestRes = " + lowestRes.toString());
			System.out.println("lowestRes = " + lowestRes.getFirst() + ", " + lowestRes.getSecond() + " , {"
					+ lowestRes.getThird().getFirst() + "," + Arrays.toString(lowestRes.getThird().getSecond()) + ","
					+ Arrays.toString(lowestRes.getThird().getThird()));
		}

		return lowestRes;
	}

	/**
	 * Computes Levenshtein distance between the given strings.</br>
	 * 
	 * Weight of insertion = insertWt * abs(diff(insertedVal - medianValOfOtherString)) </br>
	 * Weight of deletion = deleteWt * abs(diff(deletedVal - medianValOfOtherString)) </br>
	 * Weight of replacement = replaceWt * abs(diff(replaceVal - original))
	 * 
	 * right to left: insertion? top to down: deletion
	 * 
	 * @since 16 June 2015
	 * @param word1
	 * @param word2
	 * @param insertWt
	 * @param deleteWt
	 * @param replaceWt
	 * @return Levenshtein distance with trace of operations
	 */
	public static double getMySimpleLevenshteinDistanceWithoutTrace(String word1, String word2, int insertWt,
			int deleteWt, int replaceWt)
	{
		if (VerbosityConstants.verbose || VerbosityConstants.verboseLevenstein)
		{
			System.out.println("inside getMySimpleLevenshteinDistanceWithoutTrace  for word1=" + word1 + "  word2="
					+ word2 + " with insertWt=" + insertWt + " with deleteWt=" + deleteWt + " with replaceWt="
					+ replaceWt);
		}
		int len1 = word1.length();
		int len2 = word2.length();

		// len1+1, len2+1, because finally return dp[len1][len2]
		int[][] dist = new int[len1 + 1][len2 + 1];

		// StringBuffer[][] traceMatrix = new StringBuffer[len1 + 1][len2 + 1];
		// for (int i = 0; i <= len1; i++)
		// {
		// for (int j = 0; j <= len2; j++)
		// {
		// traceMatrix[i][j] = new StringBuffer();
		// }
		// }

		dist[0][0] = 0;

		for (int i = 1; i <= len1; i++)
		{
			dist[i][0] = i;
			// traceMatrix[i][0].append(traceMatrix[i - 1][0] + "_D(" + (i) + "-" + "0)");
		}

		for (int j = 1; j <= len2; j++)
		{
			dist[0][j] = j;
			// traceMatrix[0][j].append(traceMatrix[0][j - 1] + "_I(0" + "-" + j + ")");
		}

		// iterate though, and check last char
		for (int i = 0; i < len1; i++)
		{
			char c1 = word1.charAt(i);
			for (int j = 0; j < len2; j++)
			{
				char c2 = word2.charAt(j);

				// System.out.println("\nComparing " + c1 + " and " + c2);
				// if last two chars equal
				if (c1 == c2)
				{
					// update dp value for +1 length
					dist[i + 1][j + 1] = dist[i][j];
					// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_N(" + (i + 1) + "-" + (j + 1) + ")");
					// System.out.println("Equal" + " Trace " + traceMatrix[i + 1][j + 1]);// "_N(" + (i + 1) + "-" + (j
					// + 1) + ")");
				}
				else
				{
					int replace = dist[i][j] + replaceWt;// 2; //diagonally previous, see slides from STANFORD NLP on
															// min edit distance
					int delete = dist[i][j + 1] + deleteWt;// 1;//deletion --previous row, i.e, cell above
					int insert = dist[i + 1][j] + insertWt;// 1;// insertion --previous column, i.e, cell on left

					// System.out.println("replace =" + replace + " insert =" + insert + " deleteWt =" + delete);
					// int min = replace > insert ? insert : replace;
					// min = delete > min ? min : delete;
					//
					int min = -9999;

					if (isMinimum(delete, delete, insert, replace))
					{
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j + 1] + "_D(" + (i + 1) + "-" + (j + 1) +
						// ")");
						min = delete;
						// System.out.println("Delete is min:" + delete + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_D(" + (i + 1) + "-" + (j + 1) + ")");
					}

					else if (isMinimum(insert, delete, insert, replace))
					{
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i + 1][j] + "_I(" + (i + 1) + "-" + (j + 1) +
						// ")");
						min = insert;
						// System.out.println("Insert is min:" + insert + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_I(" + (i + 1) + "-" + (j + 1) + ")");
					}
					else if (isMinimum(replace, delete, insert, replace))
					{
						// traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_S(" + (i + 1) + "-" + (j + 1) + ")");
						min = replace;
						// System.out.println("replace is min:" + replace + " Trace " + traceMatrix[i + 1][j + 1]);// "
						// Trace added= " + "_S(" + (i + 1) + "-" + (j + 1) + ")");
					}

					if (min == -9999)
					{
						System.out.println("Error in minDistance");
					}

					dist[i + 1][j + 1] = min;
				}
			}
		}

		if (VerbosityConstants.verboseLevenstein)
		// iterate though, and check last char
		{
			// System.out.println(" Trace Matrix: ");
			// for (int i = 0; i <= len1; i++)
			// {
			// for (int j = 0; j <= len2; j++)
			// {
			// System.out.print(traceMatrix[i][j] + "||");
			// }
			// System.out.println();
			// }
			System.out.println("  Distance Matrix: ");
			for (int i = 0; i <= len1; i++)
			{
				for (int j = 0; j <= len2; j++)
				{
					System.out.print(dist[i][j] + "||");
				}
				System.out.println();
			}

			System.out.println("Resultant Distance = " + new Double(dist[len1][len2]));
			// System.out.println("Resultant Trace = " + traceMatrix[len1][len2].toString());
			System.out.println(" -------- ");
		}

		return new Double(dist[len1][len2]);
	}

	@Override
	public String toString()
	{
		if (VerbosityConstants.alignmentDistanceStringPrintedOnce == false)
		{
			VerbosityConstants.alignmentDistanceStringPrintedOnce = true;
			return "AlignmentBasedDistance [wtActivityName=" + wtActivityName + ", wtStartTime=" + wtStartTime
					+ ", wtDuration=" + wtDuration + ", wtDistanceTravelled=" + wtDistanceTravelled + ", wtStartGeo="
					+ wtStartGeo + ", wtEndGeo=" + wtEndGeo + ", wtAvgAltitude=" + wtAvgAltitude + ", wtLocation="
					+ wtLocation + ", wtLocPopularity=" + wtLocPopularity + ", wtDistanceFromPrev=" + wtDistanceFromPrev
					+ ", wtDurationFromPrev=" + wtDurationFromPrev + ", wtDistanceFromNext=" + wtDistanceFromNext
					+ ", wtDurationFromNext=" + wtDurationFromNext + ", wtFullActivityObject=" + wtFullActivityObject
					+ ", costInsertActivityObject=" + costInsertActivityObject + ", costDeleteActivityObject="
					+ costDeleteActivityObject + ", costReplaceActivityObject=" + costReplaceActivityObject
					+ ", distMatrix=" + Arrays.toString(distMatrix) + ", defaultCostInsert=" + defaultCostInsert
					+ ", defaultCostDelete=" + defaultCostDelete + ", defaultCostReplace=" + defaultCostReplace
					+ ", startTimeToleranceInSeconds=" + startTimeToleranceInSeconds + ", durationToleranceInSeconds="
					+ durationToleranceInSeconds + ", distanceTravelledTolerance=" + distanceTravelledTolerance
					+ ", startGeoTolerance=" + startGeoTolerance + ", endGeoTolerance=" + endGeoTolerance
					+ ", avgAltTolerance=" + avgAltTolerance + ", locationPopularityTolerance="
					+ locationPopularityTolerance + ", durationFromPrevTolerance=" + durationFromPrevTolerance
					+ ", distanceFromPrevTolerance=" + distanceFromPrevTolerance + ", numberOfInsertions="
					+ numberOfInsertions + ", numberOfDeletions=" + numberOfDeletions + ", numberOfReplacements="
					+ numberOfReplacements + "]";
		}
		else
		{
			return "";
		}
	}

	/**
	 * Fork of getFeatureLevelDistanceGowallaPD13Apr2018().
	 * <p>
	 * Store differences of each features (instead of normalised computed distances from differences), normalise it
	 * later when we values for all cands for a given RT
	 * 
	 * @param ao1
	 * @param ao2
	 * @return map of differences of Gowalla features
	 *         <p>
	 *         EnumMap{GowallaFeatures, Double}, map of Gowalla features and corresonding feature's difference between
	 *         the two compared act objs ao1 and ao2
	 * @since April 13 2018
	 */
	public EnumMap<GowallaFeatures, Double> getFeatureLevelDifferenceGowallaPD13Apr2018(ActivityObject ao1,
			ActivityObject ao2)
	{
		EnumMap<GowallaFeatures, Double> featureDiffMap = new EnumMap<>(GowallaFeatures.class);
		// StringBuilder sbLog = new StringBuilder();

		if (Constant.getDatabaseName().equals("gowalla1"))// (Constant.DATABASE_NAME.equals("geolife1"))
		{
			if (useStartTimeInFED)
			{
				featureDiffMap.put(GowallaFeatures.StartTimeF,
						(double) DateTimeUtils.getTimeDiffInSecondsZoned(ao1.getStartTimestampInms(),
								ao2.getStartTimestampInms(), ao1.getTimeZoneId(), ao2.getTimeZoneId()));

				if (ao1.getTimeZoneId() == null || ao2.getTimeZoneId() == null)
				{
					WToFile.appendLineToFileAbs("Null timezone for locid" + ao1.getLocationIDs(',') + " or "
							+ ao2.getLocationIDs(',') + "\n",
							Constant.getOutputCoreResultsPath() + "NullTimeZoneLog.txt");
				}
			}

			if (useLocationInFED)
			{
				if (primaryDimension.equals(PrimaryDimension.LocationID) == false)
				{
					// Start of disabled on 10 Aug 2018
					// double diffLoc;
					// if (UtilityBelt.getIntersection(ao1.getUniqueLocationIDs(), ao2.getUniqueLocationIDs()).size() ==
					// 0)
					// { // if no matching locationIDs then add wt to dfeat
					// diffLoc = 1;
					// }
					// else
					// {
					// diffLoc = 0;// even if one location matches, location distance is 0
					// }
					// featureDiffMap.put(GowallaFeatures.LocationF, diffLoc);
					// End of disabled on 10 Aug 2018
					// Start of added on 10 Aug 2018
					double diffLoc;
					if (Constant.useHaversineDistInLocationFED)
					{
						diffLoc = DomainConstants.getMinHaversineDistForGridIndicesPairs(
								ao1.getGivenDimensionVal(PrimaryDimension.LocationGridID),
								ao2.getGivenDimensionVal(PrimaryDimension.LocationGridID));
					}
					else
					{
						if (UtilityBelt.getIntersection(ao1.getGivenDimensionVal(PrimaryDimension.LocationGridID),
								ao2.getGivenDimensionVal(PrimaryDimension.LocationGridID)).size() == 0)
						{ // if no matching locationIDs then add wt to dfeat
							diffLoc = 1;
						}
						else
						{
							diffLoc = 0;// even if one location matches, location distance is 0
						}
					}
					featureDiffMap.put(GowallaFeatures.LocationF, diffLoc);
					// End of added on 10 Aug 2018
				}

			}
			if (useActivityNameInFED)
			{
				if (primaryDimension.equals(PrimaryDimension.ActivityID) == false)
				{
					double diffActID;
					if (ao1.getActivityID() != ao2.getActivityID())// incorrect version before Mar 21 2018
					{
						diffActID = 1;
					}
					else
					{
						diffActID = 0;
					}
					featureDiffMap.put(GowallaFeatures.ActNameF, diffActID);
				}
			}

			if (usePopularityInFED)
			{
				featureDiffMap.put(GowallaFeatures.PopularityF,
						(double) Math.abs(ao1.getCheckins_count() - ao2.getCheckins_count()));
			}

			if (useDistFromPrevInFED)
			{
				double diffOfDistFromPrev = FastMath.abs(ao1.getDistanceInMFromPrev() - ao2.getDistanceInMFromPrev());
				featureDiffMap.put(GowallaFeatures.DistFromPrevF, diffOfDistFromPrev);
			}

			if (useDurationFromPrevInFED)
			{
				double diffOfDurFromPrev = FastMath
						.abs(ao1.getDurationInSecondsFromPrev() - ao2.getDurationInSecondsFromPrev());
				featureDiffMap.put(GowallaFeatures.DurationFromPrevF, diffOfDurFromPrev);
			}
		}
		else
		{
			PopUps.printTracedErrorMsgWithExit(
					"Error: getFeatureLevelDistanceGowallaPD() called for database: " + Constant.getDatabaseName());
		}

		return featureDiffMap;
	}

	/**
	 * Fork of getFeatureLevelDistanceGowallaPD25Feb2018(). Cleaned and improved code
	 * 
	 * @param ao1
	 * @param ao2
	 * @return
	 * @since April 13 2018
	 */
	public double getFeatureLevelDistanceGowallaPD13Apr2018(ActivityObject ao1, ActivityObject ao2)
	{
		double dfeat = 0, dActivityName = 0, dStartTime = 0, dLocation = 0, dPopularity = 0;
		double dDistanceFromPrev = 0, dDurationFromPrev = 0;

		StringBuilder sbLogTemp1 = new StringBuilder();
		StringBuilder sbLog = new StringBuilder();

		if (Constant.getDatabaseName().equals("gowalla1"))// (Constant.DATABASE_NAME.equals("geolife1"))
		{
			if (useStartTimeInFED)
			{
				Pair<Double, String> stDistRes = getStartTimeDistanceZoned13Apr2018(ao1.getStartTimestampInms(),
						ao2.getStartTimestampInms(), ao1.getTimeZoneId(), ao2.getTimeZoneId(),
						Constant.editDistTimeDistType, startTimeToleranceInSeconds, wtStartTime);

				sbLog.append("useStartTimeInFED: stDistRes=" + stDistRes + "\n");
				if (ao1.getTimeZoneId() == null || ao2.getTimeZoneId() == null)
				{
					WToFile.appendLineToFileAbs("Null timezone for locid" + ao1.getLocationIDs(',') + " or "
							+ ao2.getLocationIDs(',') + "\n",
							Constant.getOutputCoreResultsPath() + "NullTimeZoneLog.txt");
				}

				dStartTime = stDistRes.getFirst();
				sbLog.append("\ndST=" + stDistRes.getSecond());
			}

			if (useLocationInFED)
			{ // if (Constant.primaryDimension.equals(PrimaryDimension.ActivityID))
				// probably better implementation
				if (primaryDimension.equals(PrimaryDimension.LocationID) == false)
				{
					sbLog.append("useLocationInFED:\n");
					if (UtilityBelt.getIntersection(ao1.getUniqueLocationIDs(), ao2.getUniqueLocationIDs()).size() == 0)
					{ // if no matching locationIDs then add wt to dfeat
						dLocation = wtLocation;
					}
					else
					{
						dLocation = 0;// even if one location matches, location distance is 0
					}
					sbLog.append("\ndLoc=" + (dLocation));
				}
			}
			if (useActivityNameInFED)
			{
				// if (Constant.primaryDimension.equals(PrimaryDimension.LocationID))
				// probably a better/more generic approach
				if (primaryDimension.equals(PrimaryDimension.ActivityID) == false)
				{
					sbLog.append("useActivityNameInFED:\n");
					// changed on 21 Mar 2018, was incorrect earlier, but did not affect our published results since we
					// have not used this in any of our publications
					// if (ao1.getActivityID() == ao2.getActivityID())//incorrect version before Mar 21 2018
					if (ao1.getActivityID() != ao2.getActivityID())// incorrect version before Mar 21 2018
					{
						dActivityName = wtActivityName;
					}
					else
					{
						dActivityName = 0;
					}
					sbLog.append("\ndActName" + (dActivityName));
				}
			}

			if (usePopularityInFED)
			{
				double c1 = ao1.getCheckins_count();
				double c2 = ao2.getCheckins_count();
				double popularityDistance = (Math.abs(c1 - c2) / Math.max(c1, c2));

				sbLog.append("usePopularityInFED: popularityDistanceRaw=" + popularityDistance + "\n");
				/// 1 - (Math.abs(c1 - c2) / Math.max(c1, c2));
				// add more weight if they are more different, popDistance should be higher if they are more different
				dPopularity = popularityDistance * this.wtLocPopularity;
				sbLog.append("\nao1.getCheckins_count()=" + c1 + "\nao2.getCheckins_count()=" + c2);
				sbLog.append("\ndPop=" + dPopularity);
			}

			if (useDistFromPrevInFED)
			{
				double diffOfDistFromPrev = FastMath.abs(ao1.getDistanceInMFromPrev() - ao2.getDistanceInMFromPrev());
				sbLog.append("useDistFromPrevInFED: diffOfDistFromPrev=" + diffOfDistFromPrev + "\n");
				if (diffOfDistFromPrev > 46754)
				{
					dDistanceFromPrev = this.wtDistanceFromPrev;
					sbLog.append("\tdDistanceFromPrev (more than thresh):" + (dDistanceFromPrev));
				}
				else if (diffOfDistFromPrev > this.distanceFromPrevTolerance)
				{
					double val = fastLogOfBase(diffOfDistFromPrev, 46754) * this.wtDistanceFromPrev;
					dDistanceFromPrev = val;
					sbLog.append("\tdDistanceFromPrev (log scaled):" + dDistanceFromPrev);
				}
				sbLogTemp1.append(ao1.getDistanceInMFromPrev() + "," + ao2.getDistanceInMFromPrev() + ","
						+ diffOfDistFromPrev + "," + dDistanceFromPrev + ",");
			}

			if (useDurationFromPrevInFED)
			{
				double diffOfDurFromPrev = FastMath
						.abs(ao1.getDurationInSecondsFromPrev() - ao2.getDurationInSecondsFromPrev());
				sbLog.append("useDurationFromPrevInFED: diffOfDurFromPrev=" + diffOfDurFromPrev + "\n");
				if (diffOfDurFromPrev > 63092)
				{
					dDurationFromPrev = this.wtDurationFromPrev;
					sbLog.append("\tdDurationFromPrev (more than thresh):" + (dDurationFromPrev));
				}
				else if (diffOfDurFromPrev > this.durationFromPrevTolerance)
				{
					double val = fastLogOfBase(diffOfDurFromPrev, 63092) * this.wtDurationFromPrev;
					dDurationFromPrev = val;
					sbLog.append("\tdDurationFromPrev (log scaled):" + dDurationFromPrev);
				}
				sbLogTemp1.append(ao1.getDurationInSecondsFromPrev() + "," + ao2.getDurationInSecondsFromPrev() + ","
						+ diffOfDurFromPrev + "," + dDurationFromPrev);
			}
			dfeat = dActivityName + dStartTime + dLocation + dPopularity + dDistanceFromPrev + dDurationFromPrev;
		}
		else
		{
			PopUps.printTracedErrorMsgWithExit(
					"Error: getFeatureLevelDistanceGowallaPD() called for database: " + Constant.getDatabaseName());
		}

		if (dfeat > 100)
		{
			System.out.println("Inside: dfeat= " + dfeat + " \nlog:\n" + sbLog.toString());
		}

		// if (Constant.debugFeb24_2018)
		// {
		// System.out.println("In dfeat: " + dfeat + " \tlog:\t" + sbLog.toString());
		// // WritingToFile.appendLineToFileAbsolute("\ndfeat:" + dfeat + " \tlog:\t" + sbLog.toString(),
		// // Constant.getCommonPath() + "FeatureLevelDistanceLog.csv");
		// WritingToFile.appendLineToFileAbsolute(
		// dfeat + "," + dActivityName + "," + dStartTime + "," + dLocation + "," + dPopularity + ","
		// + dDistanceFromPrev + "," + dDurationFromPrev,
		// Constant.getCommonPath() + "FeatureLevelDistanceLog.csv");
		// }

		// if (false)// TEMP FOR DEBUGGING
		// {
		// if (dfeat == 0)
		// {
		// // System.out.println("In dfeat: " + dfeat + " \tlog:\t" + );
		// // WritingToFile.appendLineToFileAbsolute("\ndfeat:" + dfeat + " \tlog:\t" + sbLog.toString(),
		// // Constant.getCommonPath() + "FeatureLevelDistanceLog.csv");
		// WritingToFile.appendLineToFileAbsolute(
		// "ao1= " + ao1.toString() + "\n" + "ao2= " + ao2.toString() + "\n" + dfeat + "," + dActivityName
		// + "," + dStartTime + "," + dLocation + "," + dPopularity + "," + dDistanceFromPrev + ","
		// + dDurationFromPrev + "\n" + sbLog.toString() + "\n\n",
		// Constant.getCommonPath() + "FeatureLevelDistanceWhyZeroLog.csv");
		// }
		// }
		// if (true)// TEMP FOR DEBUGGING
		// {
		// WritingToFile.appendLineToFileAbsolute(sbLogTemp1.toString() + "\n",
		// Constant.getCommonPath() + "FeatureLevelDistanceDistDurLog.csv");
		// }

		return dfeat;
	}

	/**
	 * 
	 * @param ao1
	 * @param ao2
	 * @return
	 */
	public double getFeatureLevelDistanceGowallaPD25Feb2018(ActivityObject ao1, ActivityObject ao2)
	{
		double dfeat = 0, dActivityName = 0, dStartTime = 0, dLocation = 0, dPopularity = 0;
		double dDistanceFromPrev = 0, dDurationFromPrev = 0;

		// Disabled on Mar 22 2018
		// boolean useActivityNameInFED = false, useStartTimeInFED = false, useLocationInFED = false,
		// usePopularityInFED = false, useDistFromPrevInFED = true, useDurationFromPrevInFED = true;
		// TODO USE THESE AS SWITCHES, SET THEM IN CONSTRUCTOR AND MAKE THEM CLASS VARIABLES

		// "ao1.DistFromPrev,ao2.DistFromPrev,diffDistFromPrev,ao1.DurationFromPrev,ao2.DurationFromPrev,diffDurationFromPrev\n"
		StringBuilder sbLogTemp1 = new StringBuilder();
		StringBuilder sbLog = new StringBuilder();
		// if(ao1.getStartTimestamp().getTime() != (ao2.getStartTimestamp().getTime()) )//is wrong since its comparing
		// timestamps and not time of days...however, results for our
		// experiments do not show any visible difference in results { dfeat+=costReplaceStartTime; }
		if (Constant.getDatabaseName().equals("gowalla1"))// (Constant.DATABASE_NAME.equals("geolife1"))
		{
			if (useStartTimeInFED)
			{
				// Pair<Double, String> stDistRes = getStartTimeDistance(ao1.getStartTimestamp(),
				// ao2.getStartTimestamp(),
				// Constant.editDistTimeDistType, startTimeToleranceInSeconds, wtStartTime);
				Pair<Double, String> stDistRes = getStartTimeDistanceZoned(ao1.getStartTimestampInms(),
						ao2.getStartTimestampInms(), ao1.getTimeZoneId(), ao2.getTimeZoneId(),
						Constant.editDistTimeDistType, startTimeToleranceInSeconds, wtStartTime);

				sbLog.append("useStartTimeInFED: stDistRes=" + stDistRes + "\n");
				if (ao1.getTimeZoneId() == null || ao2.getTimeZoneId() == null)
				// stDistRes.getSecond().length() <=15)// null timezone
				{
					WToFile.appendLineToFileAbs("Null timezone for locid" + ao1.getLocationIDs(',') + " or "
							+ ao2.getLocationIDs(',') + "\n",
							Constant.getOutputCoreResultsPath() + "NullTimeZoneLog.txt");
				}

				dStartTime = stDistRes.getFirst();
				sbLog.append("\ndST=" + stDistRes.getSecond());
			}

			if (useLocationInFED)
			{
				// if (Constant.primaryDimension.equals(PrimaryDimension.ActivityID))
				// probably better implementation
				if (primaryDimension.equals(PrimaryDimension.LocationID) == false)
				{
					sbLog.append("useLocationInFED:\n");
					if (UtilityBelt.getIntersection(ao1.getUniqueLocationIDs(), ao2.getUniqueLocationIDs()).size() == 0)
					{ // if no matching locationIDs then add wt to dfeat
						dLocation = wtLocation;
					}
					sbLog.append("\ndLoc=" + (dLocation));
				}
			}
			if (useActivityNameInFED)
			{
				// if (Constant.primaryDimension.equals(PrimaryDimension.LocationID))
				// probably a better/more generic approach
				if (primaryDimension.equals(PrimaryDimension.ActivityID) == false)
				{
					sbLog.append("useActivityNameInFED:\n");
					// changed on 21 Mar 2018, was incorrect earlier, but did not affect our published results since we
					// have not used this in any of our publications
					// if (ao1.getActivityID() == ao2.getActivityID())//incorrect version before Mar 21 2018
					if (ao1.getActivityID() != ao2.getActivityID())// incorrect version before Mar 21 2018
					{
						dActivityName = wtActivityName;
					}
					sbLog.append("\ndActName" + (dActivityName));
				}
			}

			if (usePopularityInFED)
			{
				double c1 = ao1.getCheckins_count();
				double c2 = ao2.getCheckins_count();
				double popularityDistance = (Math.abs(c1 - c2) / Math.max(c1, c2));

				sbLog.append("usePopularityInFED: popularityDistanceRaw=" + popularityDistance + "\n");
				/// 1 - (Math.abs(c1 - c2) / Math.max(c1, c2));
				// add more weight if they are more different, popDistance should be higher if they are more different
				dPopularity = popularityDistance * this.wtLocPopularity;
				sbLog.append("\nao1.getCheckins_count()=" + c1 + "\nao2.getCheckins_count()=" + c2);
				sbLog.append("\ndPop=" + dPopularity);
			}

			if (useDistFromPrevInFED)
			{
				double diffOfDistFromPrev = FastMath.abs(ao1.getDistanceInMFromPrev() - ao2.getDistanceInMFromPrev());
				sbLog.append("useDistFromPrevInFED: diffOfDistFromPrev=" + diffOfDistFromPrev + "\n");
				if (diffOfDistFromPrev > 46754)
				{
					dDistanceFromPrev = this.wtDistanceFromPrev;
					sbLog.append("\tdDistanceFromPrev (more than thresh):" + (dDistanceFromPrev));
				}
				else if (diffOfDistFromPrev > this.distanceFromPrevTolerance)
				{
					double val = fastLogOfBase(diffOfDistFromPrev, 46754) * this.wtDistanceFromPrev;
					dDistanceFromPrev = val;
					sbLog.append("\tdDistanceFromPrev (log scaled):" + dDistanceFromPrev);
				}
				sbLogTemp1.append(ao1.getDistanceInMFromPrev() + "," + ao2.getDistanceInMFromPrev() + ","
						+ diffOfDistFromPrev + "," + dDistanceFromPrev + ",");
			}

			if (useDurationFromPrevInFED)
			{
				double diffOfDurFromPrev = FastMath
						.abs(ao1.getDurationInSecondsFromPrev() - ao2.getDurationInSecondsFromPrev());
				sbLog.append("useDurationFromPrevInFED: diffOfDurFromPrev=" + diffOfDurFromPrev + "\n");
				if (diffOfDurFromPrev > 63092)
				{
					dDurationFromPrev = this.wtDurationFromPrev;
					sbLog.append("\tdDurationFromPrev (more than thresh):" + (dDurationFromPrev));
				}
				else if (diffOfDurFromPrev > this.durationFromPrevTolerance)
				{
					double val = fastLogOfBase(diffOfDurFromPrev, 63092) * this.wtDurationFromPrev;
					dDurationFromPrev = val;
					sbLog.append("\tdDurationFromPrev (log scaled):" + dDurationFromPrev);
				}
				sbLogTemp1.append(ao1.getDurationInSecondsFromPrev() + "," + ao2.getDurationInSecondsFromPrev() + ","
						+ diffOfDurFromPrev + "," + dDurationFromPrev);
			}
			dfeat = dActivityName + dStartTime + dLocation + dPopularity + dDistanceFromPrev + dDurationFromPrev;
		}
		else
		{
			PopUps.printTracedErrorMsgWithExit(
					"Error: getFeatureLevelDistanceGowallaPD() called for database: " + Constant.getDatabaseName());
		}

		if (dfeat > 100)
		{
			System.out.println("Inside: dfeat= " + dfeat + " \nlog:\n" + sbLog.toString());
		}

		// if (Constant.debugFeb24_2018)
		// {
		// System.out.println("In dfeat: " + dfeat + " \tlog:\t" + sbLog.toString());
		// // WritingToFile.appendLineToFileAbsolute("\ndfeat:" + dfeat + " \tlog:\t" + sbLog.toString(),
		// // Constant.getCommonPath() + "FeatureLevelDistanceLog.csv");
		// WritingToFile.appendLineToFileAbsolute(
		// dfeat + "," + dActivityName + "," + dStartTime + "," + dLocation + "," + dPopularity + ","
		// + dDistanceFromPrev + "," + dDurationFromPrev,
		// Constant.getCommonPath() + "FeatureLevelDistanceLog.csv");
		// }

		// if (false)// TEMP FOR DEBUGGING
		// {
		// if (dfeat == 0)
		// {
		// // System.out.println("In dfeat: " + dfeat + " \tlog:\t" + );
		// // WritingToFile.appendLineToFileAbsolute("\ndfeat:" + dfeat + " \tlog:\t" + sbLog.toString(),
		// // Constant.getCommonPath() + "FeatureLevelDistanceLog.csv");
		// WritingToFile.appendLineToFileAbsolute(
		// "ao1= " + ao1.toString() + "\n" + "ao2= " + ao2.toString() + "\n" + dfeat + "," + dActivityName
		// + "," + dStartTime + "," + dLocation + "," + dPopularity + "," + dDistanceFromPrev + ","
		// + dDurationFromPrev + "\n" + sbLog.toString() + "\n\n",
		// Constant.getCommonPath() + "FeatureLevelDistanceWhyZeroLog.csv");
		// }
		// }
		// if (true)// TEMP FOR DEBUGGING
		// {
		// WritingToFile.appendLineToFileAbsolute(sbLogTemp1.toString() + "\n",
		// Constant.getCommonPath() + "FeatureLevelDistanceDistDurLog.csv");
		// }

		return dfeat;
	}

}
