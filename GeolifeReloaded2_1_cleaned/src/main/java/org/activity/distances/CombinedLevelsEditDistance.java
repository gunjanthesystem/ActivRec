package org.activity.distances;

import java.util.ArrayList;

import org.activity.io.WritingToFile;
import org.activity.objects.ActivityObject;
import org.activity.objects.Pair;
import org.activity.util.StringCode;

/**
 * Note: this has been modified for Geolife data set to account for the additional attributes from geolocation
 * 
 * @author gunjan
 *
 */
public class CombinedLevelsEditDistance extends AlignmentBasedDistance
{
	/**
	 * Sets the tolerance according the truth value of Constant.useTolerance
	 */
	public CombinedLevelsEditDistance()
	{
		super();
		System.out.println("Error: Using CombinedLevelEditDistance, we are not supposed to use this");
		// this error message is dependent on our current experimental setup
	}

	// //////////////
	/**
	 * Finds the Edit similarity between two given lists of ActivityObjects excluding the end point current activity
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @return
	 */
	public final Pair<String, Double> getEditDistanceWithoutEndCurrentActivity(
			ArrayList<ActivityObject> activityObjects1Original, ArrayList<ActivityObject> activityObjects2Original)
	{
		Pair<String, Double> result;
		ArrayList<ActivityObject> activityObjects1 = new ArrayList<ActivityObject>();
		activityObjects1.addAll(activityObjects1Original);

		ArrayList<ActivityObject> activityObjects2 = new ArrayList<ActivityObject>();
		activityObjects2.addAll(activityObjects2Original);

		if (activityObjects1.size() - 1 == 0 && activityObjects2.size() - 1 == 0)
		{
			result = new Pair<String, Double>("", new Double(0));
		}

		else if (activityObjects1.size() - 1 == 0 && activityObjects2.size() - 1 != 0)
		{
			result = new Pair<String, Double>("", new Double(activityObjects2.size() - 1));
		}

		else if (activityObjects1.size() - 1 != 0 && activityObjects2.size() - 1 == 0)
		{
			result = new Pair<String, Double>("", new Double(activityObjects1.size() - 1));
		}

		else
		{
			activityObjects1.remove(activityObjects1.size() - 1);
			activityObjects2.remove(activityObjects2.size() - 1);

			result = getEditDistanceWithTrace(activityObjects1, activityObjects2);
		}

		// $$WritingToFile.writeEditSimilarityCalculation(activityObjects1, activityObjects2, result.getSecond(),
		// result.getFirst());
		WritingToFile.writeOnlyTrace(result.getFirst());

		return result;
	}

	/**
	 * Finds the Edit similarity between two given lists of ActivityObjects
	 * 
	 * converting ActivityObject1 to ActivityObjects2 Version 23 July
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @return
	 */
	public final double getEditDistance(ArrayList<ActivityObject> activityObjects1,
			ArrayList<ActivityObject> activityObjects2)
	{
		// double similarity=0d;//,distance=0d;
		double editDistance = 0d;

		int numberOfInsertions = 0;
		int numberOfDeletions = 0;
		int numberOfReplacements = 0;
		// System.out.println("\n\tInside Edit Similarity Calculation");
		// $$System.out.println("Activity Objects 1:");

		if (activityObjects1 == null)
		{
			System.out.println(" Edit Similarity Calculation: activityObjects1 is null");
		}

		if (activityObjects2 == null)
		{
			System.out.println(" Edit Similarity Calculation: activityObjects2 is null");
		}

		activityObjects1 = pruneFirstUnknown(activityObjects1);
		activityObjects2 = pruneFirstUnknown(activityObjects2);

		// for(ActivityObject e: activityObjects1)
		// {
		// System.out.print("<"+e.getActivityName()//+e.getLocationName()
		// +" "+e.getStartTimestamp()
		// +" "+e.getDurationInSeconds()+">,");
		// }
		//
		// System.out.println("\nActivity Objects 2:");
		// for(ActivityObject e: activityObjects2)
		// {
		// System.out.print("<"+e.getActivityName()//+e.getLocationName()
		// +" "+e.getStartTimestamp()
		// +" "+e.getDurationInSeconds()+">,");
		// }
		// System.out.println();

		if ((costInsertActivityObject + costDeleteActivityObject) < costReplaceActivityObject)
		{
			System.err.println(
					"Error: In getEditSimilarity: cost for insertion, deletion and replacement not set properly");
		}

		if ((costInsertActivityObject != costDeleteActivityObject))
		{
			System.err.println(
					"Warning: In getEditSimilarity: cost for insertion and deletion are different. Are you sure about this? (because insertion from the persective of one object is deletion from the perspective of the other");
		}
		// to understand the programming logic: refer:
		// http://people.cs.pitt.edu/~kirk/cs1501/Pruhs/Spring2006/assignments/editdistance/Levenshtein%20Distance.htm
		// and
		// //http://www.programcreek.com/2013/12/edit-distance-in-java/
		int len1 = activityObjects1.size();
		int len2 = activityObjects2.size();

		if (len1 == 0)
		{
			editDistance = len2 * costDeleteActivityObject;
		}

		if (len2 == 0)
		{
			editDistance = len1 * costDeleteActivityObject;
		}

		else
		{
			distMatrix = new double[len1 + 1][len2 + 1][3]; // the third dimension stores count of deletion,
															// countInsertion,countReplacments

			// for (int i = 0; i <= len1; i++)
			// {
			// distMatrix[i][0][0] = i;
			// }
			//
			// for (int j = 0; j <= len2; j++)
			// {
			// distMatrix[0][j][1] = j;
			// }

			// initialisation
			distMatrix[0][0][0] = 0;
			distMatrix[0][0][1] = 0;
			distMatrix[0][0][2] = 0;

			for (int i = 1; i <= len1; i++)
			{
				distMatrix[i][0][0] = distMatrix[i - 1][0][0] + costDeleteActivityObject;
			}

			for (int j = 1; j <= len2; j++)
			{
				distMatrix[0][j][1] = distMatrix[0][j - 1][1] + costInsertActivityObject;
			}

			// /////

			for (int i = 0; i < len1; i++)
			{
				ActivityObject ae1 = activityObjects1.get(i);

				String activityName1 = ae1.getActivityName();
				// String location1=ae1.getLocationName();//ae1.getDimensionAttributeValue("Location_Dimension",
				// "Location_Name").toString();
				// String
				// startTime1=ae1.getStartTimestamp().toString();//ae1.getDimensionAttributeValue("Time_Dimension",
				// "Start_Time").toString();
				String startTime1 = ae1.getStartTimestamp().getHours() + ":" + ae1.getStartTimestamp().getMinutes()
						+ ":" + ae1.getStartTimestamp().getSeconds();
				long duration1 = ae1.getDurationInSeconds();

				for (int j = 0; j < len2; j++)
				{
					ActivityObject ae2 = activityObjects2.get(j);

					String activityName2 = ae2.getActivityName();
					// String location2=ae2.getLocationName();//getDimensionAttributeValue("Location_Dimension",
					// "Location_Name").toString();
					// String
					// startTime2=ae2.getStartTimestamp().toString();//getDimensionAttributeValue("Time_Dimension",
					// "Start_Time").toString();
					String startTime2 = ae2.getStartTimestamp().getHours() + ":" + ae2.getStartTimestamp().getMinutes()
							+ ":" + ae2.getStartTimestamp().getSeconds();
					long duration2 = ae2.getDurationInSeconds();

					// System.out.println("\nComparing Activity Name 1="+activityName1+" -- "+"Activity Name
					// 2="+activityName2);
					/*
					 * System.out.println("Location 1="+location1+" -- "+"Location 2="+location2);
					 * System.out.println("Start Time=1"+startTime1+" -- "+"Start Time2="+startTime2);
					 * System.out.println("Duration 1="+duration1+" -- "+"Duration 2="+duration2);
					 */

					double insertCost = 0;// = distMatrix[i][j + 1] + costInsertActivityObject; // value from the cell
											// above // whether this insertion or deletion depends on the which object's
											// perspective we are saying,
											// so the wt of insertion and deletion should be same
					double deleteCost = 0;// = distMatrix[i + 1][j] + costDeleteActivityObject; // value from the cell
											// on the left.
					double replaceCost = 0;// = distMatrix[i][j]; // valueFromDiag

					// if the two objects are same for name, location, starttime and duration attributes
					if (activityName1.equals(activityName2)// && location1.equals(location2)
							&& startTime1.equals(startTime2) && duration1 == duration2)
					{
						// replace += this.costReplaceEqualActivityObject; //=0, dummy because usually the replacement
						// wt when objects are equal will be 0.
						// replaceCost=0;
						// No operation required
						distMatrix[i + 1][j + 1] = distMatrix[i][j];
						// System.out.println("$$$$$$ adding no wt");
					}

					else
					{
						// replace += this.costReplaceActivityObject;

						//
						if (activityName1.equals(activityName2))
						{
							/*
							 * if(location1.equals(location2) ==false) { replace += this.costReplaceLocation;//
							 * System.out.print("\n\t~~~location different for "+activityName1+" "+activityName2);
							 * //System.out.println("$$$$$$ adding wtLoc"); }
							 */

							if (startTime1.equals(startTime2) == false)
							{
								replaceCost = wtStartTime;
								// replace += this.costReplaceStartTime;//System.out.print("\n\t~~~start time different
								// for "+activityName1+" "+activityName2);
								// //System.out.println("$$$$$$ adding wtStartTime");
							}

							if (duration1 != duration2)
							{
								replaceCost = replaceCost + wtDuration;
								// replace += this.costReplaceDuration;//System.out.print("\n\t~~~duration different
								// for"+activityName1+" "+activityName2);
								// //System.out.println("$$$$$$ adding wtDuration");
							}
						}

						else
						// !(activityName1.equals(activityName2))
						{
							replaceCost = costReplaceActivityObject;// System.out.println("$$$$$$ adding
																	// wtReplaceActivityObject");
						}

						//

						// ///
						double costOfReplacing = distMatrix[i][j][0] + distMatrix[i][j][1] + distMatrix[i][j][2]
								+ replaceCost;
						double costOfInserting = distMatrix[i + 1][j][0] + distMatrix[i + 1][j][1]
								+ distMatrix[i + 1][j][2] + costInsertActivityObject;
						double costOfDeleting = distMatrix[i][j + 1][0] + distMatrix[i][j + 1][1]
								+ distMatrix[i][j + 1][2] + costDeleteActivityObject;

						// System.out.print("cost: DIS="+costOfDeleting+" "+costOfInserting+" "+replaceCost);

						if (isMinimum(costOfDeleting, costOfDeleting, costOfInserting, costOfReplacing))// (costOfInserting
																										// <
																										// costOfReplacing
																										// &&
																										// costOfInserting<costOfDeleting)
						{
							distMatrix[i + 1][j + 1][0] = distMatrix[i][j + 1][0] + costDeleteActivityObject;// costOfDeleting;
							distMatrix[i + 1][j + 1][1] = distMatrix[i][j + 1][1] + 0;
							distMatrix[i + 1][j + 1][2] = distMatrix[i][j + 1][2] + 0;

							// System.out.println(" minimum cost is for deleting");
							numberOfDeletions++;
						}

						else if (isMinimum(costOfInserting, costOfDeleting, costOfInserting, costOfReplacing))// if(costOfInserting
																												// <
																												// costOfReplacing
																												// &&
																												// costOfInserting<costOfDeleting)
						{
							distMatrix[i + 1][j + 1][0] = distMatrix[i + 1][j][0] + 0;
							distMatrix[i + 1][j + 1][1] = distMatrix[i + 1][j][1] + costInsertActivityObject;// costOfInserting;
							distMatrix[i + 1][j + 1][2] = distMatrix[i + 1][j][2] + 0;
							// System.out.println(" minimum cost is for inserting");

							numberOfInsertions++;
						}

						else
						// if (isMinimum(costOfReplacing,costOfDeleting,costOfInserting,costOfReplacing))//else
						// //(costOfReplacing < costOfInserting && costOfReplacing<costOfDeleting)
						{
							distMatrix[i + 1][j + 1][0] = distMatrix[i][j][0] + 0;
							distMatrix[i + 1][j + 1][1] = distMatrix[i][j][1] + 0;
							distMatrix[i + 1][j + 1][2] = distMatrix[i][j][2] + replaceCost;// costOfReplacing;
							// System.out.println(" minimum cost is for replacing");

							numberOfReplacements++;
						}
					}
					// distMatrix[i + 1][j + 1] = minimum(replace,insert,delete);
				}
			}

			// System.out.println("\n-----Matrix for edit distance calculation---------");
			//
			// for(int i=0;i<=len1;i++)
			// {
			// for(int j=0;j<=len2;j++)
			// {
			// System.out.print(" "+distMatrix[i][j][0]+"/"+distMatrix[i][j][1]+"/"+distMatrix[i][j][2]);
			// }
			// System.out.println();
			// }
			// System.out.println("-------------");

			editDistance = distMatrix[len1][len2][0] + distMatrix[len1][len2][1] + distMatrix[len1][len2][2];
		}
		// similarity=100- ((double)editDistance/(Math.max(activityObjects1.size(), activityObjects2.size())
		// ))*100;//userDayActivitiesAsStringCode.subSequence(0,
		// indicesOfEndPointActivityInDay.get(i)).length() ))) *100;

		// System.out.println("\nDebug: the edit distance calculated is:"+editDistance);

		if (editDistance < 12)
		{
			System.out.println("Good for dry run");
		}

		// countNumberOfInsertionDeletions(activityObjects1, activityObjects2) ;

		WritingToFile.writeEditSimilarityCalculation(activityObjects1, activityObjects2, editDistance);
		// WritingToFile.writeEditDistance(editDistance);

		return editDistance;// similarity;
	}

	// ///// Oct 9
	/**
	 * Finds the Edit similarity between two given lists of ActivityObjects ignoring the invalid ActivityObjects
	 * 
	 * Version Oct 9
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @return
	 */
	public final double getEditDistanceInvalidsExpunged(ArrayList<ActivityObject> activityObjects1,
			ArrayList<ActivityObject> activityObjects2)
	{
		return getEditDistance(expungeInvalids(activityObjects1), expungeInvalids(activityObjects2));// similarity;
	}

	// /////

	// ///// Oct 10
	/**
	 * Finds the Edit similarity between two given lists of ActivityObjects ignoring the invalid ActivityObjects
	 * 
	 * Version Oct 9
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @return
	 */
	public final Pair<String, Double> getEditDistanceWithoutEndCurrentActivityInvalidsExpunged(
			ArrayList<ActivityObject> activityObjects1, ArrayList<ActivityObject> activityObjects2)
	{
		return getEditDistanceWithoutEndCurrentActivity(expungeInvalids(activityObjects1),
				expungeInvalids(activityObjects2));// similarity;
	}

	// ////////////

	/**
	 * Finds the Edit similarity between two given lists of ActivityObjects and traces the operations
	 * 
	 * converting ActivityObject1 to ActivityObjects2 Version 22 October
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @return
	 */
	public final Pair<String, Double> getEditDistanceWithTrace(ArrayList<ActivityObject> activityObjects1,
			ArrayList<ActivityObject> activityObjects2)
	{
		// double similarity=0d;//,distance=0d;
		String trace = "";
		double editDistance = 0d;

		// System.out.println("\n\tInside Edit Similarity Calculation");
		// $$System.out.println("Activity Objects 1:");

		if (activityObjects1 == null)
		{
			System.out.println(" Edit Similarity Calculation: activityObjects1 is null");
		}

		if (activityObjects2 == null)
		{
			System.out.println(" Edit Similarity Calculation: activityObjects2 is null");
		}

		activityObjects1 = pruneFirstUnknown(activityObjects1);
		activityObjects2 = pruneFirstUnknown(activityObjects2);

		// for(ActivityObject e: activityObjects1)
		// {
		// System.out.print("<"+e.getActivityName()//+e.getLocationName()
		// +" "+e.getStartTimestamp()
		// +" "+e.getDurationInSeconds()+">,");
		// }
		//
		// System.out.println("\nActivity Objects 2:");
		// for(ActivityObject e: activityObjects2)
		// {
		// System.out.print("<"+e.getActivityName()//+e.getLocationName()
		// +" "+e.getStartTimestamp()
		// +" "+e.getDurationInSeconds()+">,");
		// }
		// System.out.println();

		if ((costInsertActivityObject + costDeleteActivityObject) < costReplaceActivityObject)
		{
			System.err.println(
					"Error: In getEditSimilarity: cost for insertion, deletion and replacement not set properly");
		}

		if ((costInsertActivityObject != costDeleteActivityObject))
		{
			System.err.println(
					"Warning: In getEditSimilarity: cost for insertion and deletion are different. Are you sure about this? (because insertion from the persective of one object is deletion from the perspective of the other");
		}
		// to understand the programming logic: refer:
		// http://people.cs.pitt.edu/~kirk/cs1501/Pruhs/Spring2006/assignments/editdistance/Levenshtein%20Distance.htm
		// and
		// //http://www.programcreek.com/2013/12/edit-distance-in-java/
		int len1 = activityObjects1.size();
		int len2 = activityObjects2.size();

		if (len1 == 0)
		{
			editDistance = len2 * costDeleteActivityObject;
		}

		if (len2 == 0)
		{
			editDistance = len1 * costDeleteActivityObject;
		}

		else
		{
			distMatrix = new double[len1 + 1][len2 + 1][3]; // the third dimension stores count of deletion,
															// countInsertion,countReplacments
			StringBuffer[][] traceMatrix = new StringBuffer[len1 + 1][len2 + 1];
			for (int i = 0; i <= len1; i++)
			{
				for (int j = 0; j <= len2; j++)
				{
					traceMatrix[i][j] = new StringBuffer();
				}
			}
			// for (int i = 0; i <= len1; i++)
			// {
			// distMatrix[i][0][0] = i;
			// }
			//
			// for (int j = 0; j <= len2; j++)
			// {
			// distMatrix[0][j][1] = j;
			// }

			// initialisation
			distMatrix[0][0][0] = 0;
			distMatrix[0][0][1] = 0;
			distMatrix[0][0][2] = 0;

			for (int i = 1; i <= len1; i++)
			{
				distMatrix[i][0][0] = distMatrix[i - 1][0][0] + costDeleteActivityObject;
			}

			for (int j = 1; j <= len2; j++)
			{
				distMatrix[0][j][1] = distMatrix[0][j - 1][1] + costInsertActivityObject;
			}

			// /////

			for (int i = 0; i < len1; i++)
			{
				ActivityObject ae1 = activityObjects1.get(i);

				String activityName1 = ae1.getActivityName();
				// String location1=ae1.getLocationName();//ae1.getDimensionAttributeValue("Location_Dimension",
				// "Location_Name").toString();
				// String
				// startTime1=ae1.getStartTimestamp().toString();//ae1.getDimensionAttributeValue("Time_Dimension",
				// "Start_Time").toString();
				String startTime1 = ae1.getStartTimestamp().getHours() + ":" + ae1.getStartTimestamp().getMinutes()
						+ ":" + ae1.getStartTimestamp().getSeconds();
				long duration1 = ae1.getDurationInSeconds();

				for (int j = 0; j < len2; j++)
				{
					ActivityObject ae2 = activityObjects2.get(j);

					String activityName2 = ae2.getActivityName();
					// String location2=ae2.getLocationName();//getDimensionAttributeValue("Location_Dimension",
					// "Location_Name").toString();
					// String
					// startTime2=ae2.getStartTimestamp().toString();//getDimensionAttributeValue("Time_Dimension",
					// "Start_Time").toString();
					String startTime2 = ae2.getStartTimestamp().getHours() + ":" + ae2.getStartTimestamp().getMinutes()
							+ ":" + ae2.getStartTimestamp().getSeconds();
					long duration2 = ae2.getDurationInSeconds();

					// System.out.println("\nComparing Activity Name 1="+activityName1+" -- "+"Activity Name
					// 2="+activityName2);
					/*
					 * System.out.println("Location 1="+location1+" -- "+"Location 2="+location2);
					 * System.out.println("Start Time=1"+startTime1+" -- "+"Start Time2="+startTime2);
					 * System.out.println("Duration 1="+duration1+" -- "+"Duration 2="+duration2);
					 */

					double insertCost = 0;// = distMatrix[i][j + 1] + costInsertActivityObject; // value from the cell
											// above // whether this insertion or deletion depends on the which object's
											// perspective we are saying,
											// so the wt of insertion and deletion should be same
					double deleteCost = 0;// = distMatrix[i + 1][j] + costDeleteActivityObject; // value from the cell
											// on the left.
					double replaceCost = 0;// = distMatrix[i][j]; // valueFromDiag

					// if the two objects are same for name, location, starttime and duration attributes
					if (activityName1.equals(activityName2)// && location1.equals(location2)
							&& startTime1.equals(startTime2) && duration1 == duration2)
					{
						// replace += this.costReplaceEqualActivityObject; //=0, dummy because usually the replacement
						// wt when objects are equal will be 0.
						// replaceCost=0;
						// No operation required
						distMatrix[i + 1][j + 1] = distMatrix[i][j];
						traceMatrix[i + 1][j + 1].append(traceMatrix[i][j] + "_N(" + (i + 1) + "-" + (j + 1) + ")");
						// System.out.println("$$$$$$ adding no wt");
					}

					else
					{
						// replace += this.costReplaceActivityObject;

						//
						if (activityName1.equals(activityName2))
						{
							/*
							 * if(location1.equals(location2) ==false) { replace += this.costReplaceLocation;//
							 * System.out.print("\n\t~~~location different for "+activityName1+" "+activityName2);
							 * //System.out.println("$$$$$$ adding wtLoc"); }
							 */

							if (startTime1.equals(startTime2) == false)
							{
								replaceCost = wtStartTime;
								// replace += this.costReplaceStartTime;//System.out.print("\n\t~~~start time different
								// for "+activityName1+" "+activityName2);
								// //System.out.println("$$$$$$ adding wtStartTime");
							}

							if (duration1 != duration2)
							{
								replaceCost = replaceCost + wtDuration;
								// replace += this.costReplaceDuration;//System.out.print("\n\t~~~duration different
								// for"+activityName1+" "+activityName2);
								// //System.out.println("$$$$$$ adding wtDuration");
							}
						}

						else
						// !(activityName1.equals(activityName2))
						{
							replaceCost = costReplaceActivityObject;// System.out.println("$$$$$$ adding
																	// wtReplaceActivityObject");
						}

						//

						// ///
						double costOfReplacing = distMatrix[i][j][0] + distMatrix[i][j][1] + distMatrix[i][j][2]
								+ replaceCost;
						double costOfInserting = distMatrix[i + 1][j][0] + distMatrix[i + 1][j][1]
								+ distMatrix[i + 1][j][2] + costInsertActivityObject;
						double costOfDeleting = distMatrix[i][j + 1][0] + distMatrix[i][j + 1][1]
								+ distMatrix[i][j + 1][2] + costDeleteActivityObject;

						// System.out.print("cost: DIS="+costOfDeleting+" "+costOfInserting+" "+replaceCost);

						if (isMinimum(costOfDeleting, costOfDeleting, costOfInserting, costOfReplacing))// (costOfDeleting
																										// <
																										// costOfReplacing
																										// &&
																										// costOfDeleting<costOfInserting)
						{
							distMatrix[i + 1][j + 1][0] = distMatrix[i][j + 1][0] + costDeleteActivityObject;// costOfDeleting;
							distMatrix[i + 1][j + 1][1] = distMatrix[i][j + 1][1] + 0;
							distMatrix[i + 1][j + 1][2] = distMatrix[i][j + 1][2] + 0;

							traceMatrix[i + 1][j + 1]
									.append(traceMatrix[i][j + 1] + "_D(" + (i + 1) + "-" + (j + 1) + ")");
							// System.out.println(" minimum cost is for deleting");
							// numberOfDeletions++;
						}

						else if (isMinimum(costOfInserting, costOfDeleting, costOfInserting, costOfReplacing))// if(costOfInserting
																												// <
																												// costOfReplacing
																												// &&
																												// costOfInserting<costOfDeleting)
						{
							distMatrix[i + 1][j + 1][0] = distMatrix[i + 1][j][0] + 0;
							distMatrix[i + 1][j + 1][1] = distMatrix[i + 1][j][1] + costInsertActivityObject;// costOfInserting;
							distMatrix[i + 1][j + 1][2] = distMatrix[i + 1][j][2] + 0;
							// System.out.println(" minimum cost is for inserting");
							traceMatrix[i + 1][j + 1]
									.append(traceMatrix[i + 1][j] + "_I(" + (i + 1) + "-" + (j + 1) + ")");
							// numberOfInsertions++;
						}

						else
						// if (isMinimum(costOfReplacing,costOfDeleting,costOfInserting,costOfReplacing))//else
						// //(costOfReplacing < costOfInserting && costOfReplacing<costOfDeleting)
						{
							distMatrix[i + 1][j + 1][0] = distMatrix[i][j][0] + 0;
							distMatrix[i + 1][j + 1][1] = distMatrix[i][j][1] + 0;
							distMatrix[i + 1][j + 1][2] = distMatrix[i][j][2] + replaceCost;// costOfReplacing;

							String replaceCode = "";
							if (replaceCost == costReplaceActivityObject)
								replaceCode = "Sao";
							else if (replaceCost == wtStartTime)
								replaceCode = "Sst";
							else if (replaceCost == wtDuration)
								replaceCode = "Sd";
							else if (replaceCost == (wtDuration + wtStartTime)) replaceCode = "Sstd";
							traceMatrix[i + 1][j + 1].append(
									traceMatrix[i][j] + "_" + replaceCode + "(" + (i + 1) + "-" + (j + 1) + ")");
							// System.out.println(" minimum cost is for replacing");
							// numberOfReplacements++;
						}
					}
					// distMatrix[i + 1][j + 1] = minimum(replace,insert,delete);
				}
			}

			// System.out.println("\n-----Matrix for edit distance calculation---------");
			//
			// for(int i=0;i<=len1;i++)
			// {
			// for(int j=0;j<=len2;j++)
			// {
			// System.out.print(" "+distMatrix[i][j][0]+"/"+distMatrix[i][j][1]+"/"+distMatrix[i][j][2]);
			// }
			// System.out.println();
			// }
			// System.out.println("-------------");

			editDistance = distMatrix[len1][len2][0] + distMatrix[len1][len2][1] + distMatrix[len1][len2][2];
			trace = traceMatrix[len1][len2].toString();
		}
		// similarity=100- ((double)editDistance/(Math.max(activityObjects1.size(), activityObjects2.size())
		// ))*100;//userDayActivitiesAsStringCode.subSequence(0,
		// indicesOfEndPointActivityInDay.get(i)).length() ))) *100;

		// System.out.println("\nDebug: the edit distance calculated is:"+editDistance);

		if (editDistance < 12)
		{
			System.out.println("Good for dry run");
		}

		// countNumberOfInsertionDeletions(activityObjects1, activityObjects2) ;

		// $$WritingToFile.writeEditSimilarityCalculation(activityObjects1, activityObjects2, editDistance, trace);
		WritingToFile.writeOnlyTrace(trace);
		// WritingToFile.writeEditDistance(editDistance);

		return new Pair<String, Double>(trace, editDistance);// similarity;
	}

	/**
	 * 
	 * @param activityObjects1
	 * @param activityObjects2
	 * @return
	 */
	public final Pair<String, Double> getMyLevenshteinDistanceAsStringCodeWithTrace(
			ArrayList<ActivityObject> activityObjects1, ArrayList<ActivityObject> activityObjects2)
	{
		activityObjects1 = pruneFirstUnknown(activityObjects1);
		activityObjects2 = pruneFirstUnknown(activityObjects2);

		String stringCodeForActivityObjects1 = StringCode.getStringCodeForActivityObjects(activityObjects1);
		String stringCodeForActivityObjects2 = StringCode.getStringCodeForActivityObjects(activityObjects2);

		// double levenshteinDistance =StringUtils.getLevenshteinDistance(stringCodeForActivityObjects1,
		// stringCodeForActivityObjects2);
		Pair<String, Double> levenshteinDistance = getMySimpleLevenshteinDistancePair(stringCodeForActivityObjects1,
				stringCodeForActivityObjects2, 1, 1, 1);

		// $$WritingToFile.writeEditSimilarityCalculation(activityObjects1, activityObjects2,
		// levenshteinDistance.getSecond(), levenshteinDistance.getFirst());
		WritingToFile.writeOnlyTrace(levenshteinDistance.getFirst());

		// WritingToFile.writeEditSimilarityCalculation(activityObjects1,activityObjects2,levenshteinDistance);
		// WritingToFile.writeEditDistance(levenshteinDistance);
		return new Pair<String, Double>(levenshteinDistance.getFirst(), levenshteinDistance.getSecond());
	}

}

// ALL BELOW ARE COMMENTED
// public static final wt
/*
 * public static int getSimilarity(ActivityObject activityObject1, ActivityObject activityObject2) {
 * 
 * // for simplicity we will give equal wts to all dimension atttributes ArrayList<Dimension> dimensionsOf1=
 * activityObject1.getDimensions(); ArrayList<Dimension> dimensionsOf2= activityObject2.getDimensions();
 * 
 * for(int i=0;i<dimensionsOf1.size();i++) { //since each activity's dimensions are stored in same order and each
 * activity has same number of dimensions HashMap<String,Object>
 * attributeNameValuePairs1=dimensionsOf1.get(i).getAttributeNameValuePairs(); //name value pairs of that dimension, say
 * 'User Dimension' HashMap<String,Object> attributeNameValuePairs2=dimensionsOf2.get(i).getAttributeNameValuePairs();
 * //name value pairs of that dimension, say 'User Dimension' } return 0; }
 */

// /////////////////////////////////////////////

// /**
// * Returns case based similarity (score) between the two given activity objects
// *
// * @param activityObject1
// * @param activityObject2
// * @param userID
// * user for which recommendation is being done
// * @return
// */
// public final double getCaseBasedV1Similarity(ActivityObject activityObject1, ActivityObject activityObject2, int
// userID)
// {
// int ui = getIndexOfUserID(userID);
// double startTimeComponent = 0, durationComponent = 0, distanceTravelledComponent = 0, startGeoComponent = 0,
// endGeoComponent = 0, avgAltComponent = 0;
//
// // /////////////////////////////////////////////////////////////////////
// double startTimeAct1InSecs = activityObject1.getStartTimestamp().getHours() * 60 * 60 +
// (activityObject1.getStartTimestamp().getMinutes()) * 60 +
// (activityObject1.getStartTimestamp().getSeconds());
// double startTimeAct2InSecs = activityObject2.getStartTimestamp().getHours() * 60 * 60 +
// (activityObject2.getStartTimestamp().getMinutes()) * 60 +
// (activityObject2.getStartTimestamp().getSeconds());
// double absDifferenceOfStartTime = Math.abs(startTimeAct1InSecs - startTimeAct2InSecs);
// double maxStartTimeInSeconds = Math.max(startTimeAct1InSecs, startTimeAct2InSecs);
//
// if (maxStartTimeInSeconds <= 0)
// {
// maxStartTimeInSeconds = 1;
// if (maxStartTimeInSeconds < 0)
// {
// System.err.println("Error in getCaseBasedV1Similarity: maxStartTimeInSeconds=" + maxStartTimeInSeconds + "<0");
// }
// }
//
// startTimeComponent = (1 - absDifferenceOfStartTime / maxStartTimeInSeconds);
// // ///////////////////////////////////////////////////////
//
// double absDifferenceOfDuration = Math.abs(activityObject1.getDurationInSeconds() -
// activityObject2.getDurationInSeconds());
// double maxOfDurationInSeconds = Math.max(activityObject1.getDurationInSeconds(),
// activityObject2.getDurationInSeconds());
//
// if (maxOfDurationInSeconds <= 0)
// {
// maxOfDurationInSeconds = 1;
// if (maxOfDurationInSeconds < 0)
// {
// System.err.println("Error in getCaseBasedV1Similarity: maxOfDurationInSeconds=" + maxOfDurationInSeconds + "<0");
// }
// }
//
// durationComponent = (1 - absDifferenceOfDuration / maxOfDurationInSeconds);
// // ////////////////////////////////////////////////////////////////////////////////////////////
//
// double absDiffDistance = Math.abs(activityObject1.getDistanceTravelled() - activityObject2.getDistanceTravelled());
// double maxDistanceTravelled = Math.max(activityObject1.getDistanceTravelled(),
// activityObject2.getDistanceTravelled());
//
// if (maxDistanceTravelled <= 0)
// {
// maxDistanceTravelled = 1;
// if (maxDistanceTravelled < 0)
// {
// System.err.println("Error in getCaseBasedV1Similarity: maxDistanceTravelled=" + maxDistanceTravelled + "<0");
// }
// }
//
// distanceTravelledComponent = 0;
// if (absDiffDistance > distanceTravelledTolerance)
// {
// // absDiffDistance = absDiffDistance - distanceTravelledTolerance;
// distanceTravelledComponent = (1 - absDiffDistance / maxDistanceTravelled);
// }
// // ///////////////////////////////////////////////////////////////////////////////////////////
//
// double diffStartGeo = UtilityBelt.haversine(activityObject1.getStartLatitude(), activityObject1.getStartLongitude(),
// activityObject2.getStartLatitude(), activityObject2.getStartLongitude());
//
// startGeoComponent = 0;
//
// if (diffStartGeo > startGeoTolerance)
// {
// // diffStartGeo = diffStartGeo - startGeoTolerance;
// double iqr3StartGeo = thirdQuartilesStartEndGeoDiff[ui * 2];
// double dist = 0;
//
// if (diffStartGeo > iqr3StartGeo) // higher than third quartile..then it is considered to have no similarity, that is
// distance =1
// {
// dist = 1;
// }
// else
// {
// dist = diffStartGeo / iqr3StartGeo;
// }
// startGeoComponent = (1 - dist);
// }
// // ///////////////////////////////////////////////////////////////////////////////////////////
//
// double diffEndGeo = UtilityBelt.haversine(activityObject1.getEndLatitude(), activityObject1.getEndLongitude(),
// activityObject2.getEndLatitude(), activityObject2.getEndLongitude());
//
// endGeoComponent = 0;
//
// if (diffEndGeo > endGeoTolerance)
// {
// // diffEndGeo = diffEndGeo - endGeoTolerance;
// double iqr3endGeo = thirdQuartilesStartEndGeoDiff[(ui * 2) + 1];
// double dist = 0;
//
// if (diffEndGeo > iqr3endGeo) // higher than third quartile..then it is considered to have no similarity, that is
// distance =1
// {
// dist = 1;
// }
// else
// {
// dist = diffEndGeo / iqr3endGeo;
// }
// endGeoComponent = (1 - dist);
// }
//
// //
// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// // if(activityObject1.getAvgAltitude() )
// double absDiffAvgAlt = 0;
// double maxAvgAltitude = 0;
// if (Double.parseDouble(activityObject1.getAvgAltitude()) == 0 || Double.parseDouble(activityObject1.getAvgAltitude())
// == 0)
// {
// avgAltComponent = 0;
// }
//
// else
// {
// absDiffAvgAlt = Math.abs(activityObject1.getDifferenceAltitude(activityObject2));
// maxAvgAltitude = Math.max(Double.parseDouble(activityObject1.getAvgAltitude()),
// Double.parseDouble(activityObject2.getAvgAltitude()));
//
// if (maxAvgAltitude <= 0)
// {
// maxAvgAltitude = 1;
// if (maxAvgAltitude < 0)
// {
// System.err.println("Error in getCaseBasedV1Similarity: maxAvgAltitude=" + maxAvgAltitude + "<0" + "Does that data
// contain places below sea level?");
// }
// }
//
// avgAltComponent = 0;
// if (absDiffAvgAlt > avgAltTolerance)
// {
// // absDiffAvgAlt = absDiffAvgAlt - avgAltTolerance;
// avgAltComponent = (1 - absDiffAvgAlt / maxAvgAltitude);
// }
//
// if (absDiffAvgAlt > maxAvgAltitude)
// {
// System.err.println("Error:absDiffAvgAlt(" + absDiffAvgAlt + ") >maxAvgAltitude(" + maxAvgAltitude + ")");
// System.err.println(activityObject1.getAvgAltitude() + " " + activityObject2.getAvgAltitude());
// }
// }
//
// // ////////////////////////////////////////////////////////////////////////////////////////////////
//
// double result = // costReplaceStartTime * (1-absDifferenceOfStartTime/maxStartTimeInSeconds) + costReplaceDuration *
// (1- absDifferenceOfDuration/maxOfDurationInSeconds)
// costReplaceStartTime * startTimeComponent + costReplaceDuration * durationComponent + costReplaceDistanceTravelled *
// distanceTravelledComponent + costReplaceStartGeo * startGeoComponent
// + costReplaceEndGeo * endGeoComponent + costReplaceAvgAltitude * avgAltComponent;
// ;
//
// System.out.println("Inside getCaseBasedV1Similarity \n ActivityObject1:" + activityObject1.getActivityName() + "__" +
// activityObject1.getStartTimestamp() + "__" +
// activityObject1.getDurationInSeconds()
// + "__" + activityObject1.getDistanceTravelled() + "__" + activityObject1.getStartLatitude() + "," +
// activityObject1.getStartLongitude() + "__" + activityObject1.getEndLatitude() + ","
// + activityObject1.getEndLongitude() + "__" + activityObject1.getAvgAltitude()
//
// + "\n ActivityObject2:" + activityObject2.getActivityName() + "__" + activityObject2.getStartTimestamp() + "__" +
// activityObject2.getDurationInSeconds() + "__"
// + activityObject2.getDistanceTravelled() + "__" + activityObject2.getStartLatitude() + "," +
// activityObject2.getStartLongitude() + "__" + activityObject2.getEndLatitude() + ","
// + activityObject2.getEndLongitude() + "__" + activityObject2.getAvgAltitude());
//
// System.out.println("CaseSimilarity =" + result + " = " + costReplaceStartTime + " * (1-" + absDifferenceOfStartTime +
// "/" + maxStartTimeInSeconds + ") " + "+ " + costReplaceDuration + " * (1- "
// + absDifferenceOfDuration + "/" + maxOfDurationInSeconds + ")" + "+ " + costReplaceDistanceTravelled + " * (1- " +
// absDiffDistance + "/" + maxDistanceTravelled + ")" + "+" + costReplaceStartGeo
// + " * " + startGeoComponent + "+" + costReplaceEndGeo + " * " + endGeoComponent + "+ " + costReplaceAvgAltitude + " *
// (1- " + absDiffAvgAlt + "/" + maxAvgAltitude + ")");
//
// if (result < 0)
// {
// System.err.println("Error: Case similarity is negative");
// }
//
// return result;
// }

// public final double getLevenshteinDistanceAsStringCode(ArrayList<ActivityObject> activityObjects1,
// ArrayList<ActivityObject> activityObjects2)
// {
// activityObjects1=pruneFirstUnknown(activityObjects1);
// activityObjects2=pruneFirstUnknown(activityObjects2);
//
// String stringCodeForActivityObjects1= ActivityObject.getStringCodeForActivityObjects(activityObjects1);
// String stringCodeForActivityObjects2= ActivityObject.getStringCodeForActivityObjects(activityObjects2);
//
// // double levenshteinDistance =StringUtils.getLevenshteinDistance(stringCodeForActivityObjects1,
// stringCodeForActivityObjects2);
// double levenshteinDistance =minDistance(stringCodeForActivityObjects1, stringCodeForActivityObjects2);
// WritingToFile.writeEditSimilarityCalculation(activityObjects1,activityObjects2,levenshteinDistance);
// //WritingToFile.writeEditDistance(levenshteinDistance);
// return levenshteinDistance;
// }
// ///////////////

// }
