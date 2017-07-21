package org.activity.distances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.activity.constants.Constant;
import org.activity.constants.DomainConstants;
import org.activity.constants.Enums;
import org.activity.constants.Enums.PrimaryDimension;
import org.activity.constants.VerbosityConstants;
import org.activity.io.WritingToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.Pair;
import org.activity.objects.TraceMatrixLeaner1;
import org.activity.stats.StatsUtils;
import org.activity.ui.PopUps;
import org.activity.util.DateTimeUtils;
import org.activity.util.StringUtils;
import org.activity.util.UtilityBelt;
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

	double wtActivityName;// = 3d;
	double wtStartTime;// = 1d;// 0.6d;
	double wtDuration;// = 0.5d;// 0.2d;
	double wtDistanceTravelled;// = 3d;// //1d;
	double wtStartGeo;// = 0.3d;
	double wtEndGeo;// = 0.3d;
	double wtAvgAltitude;// = 0.2d;
	double wtLocation;
	double wtLocPopularity;

	double[][][] distMatrix;

	double wtFullActivityObject;// = wtActivityName + wtStartTime + wtDuration + wtDistanceTravelled + wtStartGeo +
								// wtEndGeo + +wtAvgAltitude;

	double costInsertActivityObject;// = 1d * wtFullActivityObject; // cost of insert operation 4.5
	double costDeleteActivityObject;// = 1d * wtFullActivityObject; // cost of delete operation 4.5
	double costReplaceActivityObject;// = 2d * wtFullActivityObject; // cost of replace operation 9

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
	public AlignmentBasedDistance()
	{
		setWeightsAndCosts(); // VERY IMPORTANT

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
	 * Set weight for feature, weight forfull activity object based on the database used, weight for insertion, deletion
	 * and replacement of activity object, and cost of insertion, deletion and replacement of activity object
	 */
	public void setWeightsAndCosts()
	{
		wtActivityName = 2d;// 3d;
		wtStartTime = 1d;// 0.8d;// 0.6d;

		wtDuration = 0.5d;// 0.2d;
		wtDistanceTravelled = 3d;// //1d;
		wtStartGeo = 0.3d;
		wtEndGeo = 0.3d;
		wtAvgAltitude = 0.2d;

		wtLocation = 1d;
		wtLocPopularity = 1d;

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
				wtFullActivityObject = StatsUtils.round(wtActivityName + wtStartTime + wtLocation + wtLocPopularity, 4);
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
	 * 
	 * @return
	 */
	public String getAllWeightsOfFeatures()
	{
		String allWts = "\nWt of Activity Name:" + wtActivityName + "\nWt of Start Time:" + wtStartTime
				+ "\nWt of Duration:" + wtDuration + "\nWt of Distance Travelled:" + wtDistanceTravelled
				+ "\nWt of Start Geo Location:" + wtStartGeo + "\nWt of End Geo Location:" + wtEndGeo
				+ "\nWt of Avg Altitude:" + wtAvgAltitude + "\nWt of full Activity Object:" + wtFullActivityObject;
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
			dfeat = getFeatureLevelDistanceGowallaPD(ao1, ao2);
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
	 * 
	 * @param ao1
	 * @param ao2
	 * @return
	 */
	public double getFeatureLevelDistanceGowallaPD(ActivityObject ao1, ActivityObject ao2)
	{
		double dfeat = 0;
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
				}
				else // absTimeDiffInSeconds > startTimeToleranceInSeconds
				{
					dfeat += (1.0 * wtStartTime);
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
				}
			}
			// $$ added on 3rd march 2017 end

			// System.out.println("@@ ao1.getLocationIDs() = " + ao1.getLocationIDs());
			// System.out.println("@@ ao2.getLocationIDs() = " + ao2.getLocationIDs());
			// System.out.println("@@ UtilityBelt.getIntersection(ao1.getLocationIDs(), ao2.getLocationIDs()).size() = "
			// + UtilityBelt.getIntersection(ao1.getLocationIDs(), ao2.getLocationIDs()).size());

			if (Constant.primaryDimension.equals(PrimaryDimension.ActivityID))
			{
				if (UtilityBelt.getIntersection(ao1.getUniqueLocationIDs(), ao2.getUniqueLocationIDs()).size() == 0)
				// ao1.getLocationIDs() != ao2.getLocationIDs()) // if no matching locationIDs then add wt to dfeat
				{
					dfeat += wtLocation;
				}
			}
			else if (Constant.primaryDimension.equals(PrimaryDimension.LocationID))
			{
				if (ao1.getActivityID() == ao2.getActivityID())
				{
					dfeat += wtActivityName;
				}
			}

			double c1 = ao1.getCheckins_count();
			double c2 = ao2.getCheckins_count();
			double popularityDistance = (Math.abs(c1 - c2) / Math.max(c1, c2));
			/// 1 - (Math.abs(c1 - c2) / Math.max(c1, c2));

			// add more weight if they are more different, popDistance should be higher if they are more different
			dfeat += popularityDistance * this.wtLocPopularity;
		}
		else
		{
			PopUps.printTracedErrorMsgWithExit(
					"Error: getFeatureLevelDistanceGowallaPD() called for database: " + Constant.getDatabaseName());
		}

		return dfeat;
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

		double diffGeo = StatsUtils.haversine(lat1, lon1, lat2, lon2);

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

		double distBetweenCentroids = StatsUtils.haversine(centroid1.getFirst(), centroid1.getSecond(),
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
			dp[i][0] = dp[i - 1][0] + StatsUtils.haversine(vals1[i - 1].getFirst(), vals1[i - 1].getSecond(),
					centroid2.getFirst(), centroid2.getSecond());
			traceMatrix[i][0].append(traceMatrix[i - 1][0] + "_D(" + (i) + "-" + "0)");
		}

		for (int j = 1; j <= len2; j++)
		{
			dp[0][j] = dp[0][j - 1] + StatsUtils.haversine(vals2[j - 1].getFirst(), vals2[j - 1].getSecond(),
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

					double distBetweenPoints = StatsUtils.haversine(vals1[i].getFirst(), vals1[i].getSecond(),
							vals2[j].getFirst(), vals2[j].getSecond());
					if (VerbosityConstants.verboseLevenstein)
					{
						System.out.println("Difference of vals = " + distBetweenPoints + "kms");
					}
					double distBetweenPoint1AndCentroid = StatsUtils.haversine(vals1[i].getFirst(),
							vals1[i].getSecond(), centroid2.getFirst(), centroid2.getSecond());
					double distBetweenPoint2AndCentroid = StatsUtils.haversine(vals2[j].getFirst(),
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

		WritingToFile.appendLineToFileAbsolute(
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
	public static Pair<String, Double> getMySimpleLevenshteinDistance(String word1, String word2, int insertWt,
			int deleteWt, int replaceWt)// , TraceMatrix traceMatrix)
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
						replace = dist[i][j]
								+ replaceWt * catIDsHierarchicalDistance.get(String.valueOf(c1) + String.valueOf(c2));
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
	 * Compute levenshtein dist between the words in two lists and return the result for the least dist.
	 * 
	 * @since May 19, 2017
	 * @param word1
	 * @param word2
	 * @param insertWt
	 * @param deleteWt
	 * @param replaceWt
	 * @return Pair{Levenshtein distance,trace of operations}
	 */
	public static Pair<String, Double> getLowestMySimpleLevenshteinDistance(ArrayList<String> word1s,
			ArrayList<String> word2s, int insertWt, int deleteWt, int replaceWt)// , TraceMatrix traceMatrix)
	{
		Pair<String, Double> lowestRes = new Pair<>();

		ArrayList<Pair<String, Double>> levenshteinDists = new ArrayList<>();

		for (String word1 : word1s)
		{
			for (String word2 : word2s)
			{
				levenshteinDists.add(getMySimpleLevenshteinDistance(word1, word2, insertWt, deleteWt, replaceWt));
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

}
