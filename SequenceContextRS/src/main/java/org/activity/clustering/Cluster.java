package org.activity.clustering;

import java.util.ArrayList;
import java.util.HashSet;

import org.activity.ui.PopUps;
import org.activity.util.UtilityBelt;

public class Cluster
{
	// int centroidIndex; // index of centroid amongst membersW
	ArrayList<DataPoint> members; // note: members includes the centroid datapoint
	DataPoint centroid;
	double intraClusterVariance, intraClusterStandardDeviation;

	// AlignmentBasedDistance distance;
	/**
	 * Check if same centroid and same set of data members
	 * 
	 * @param other
	 * @return
	 */
	public boolean equals(Object otherObject)
	{
		Cluster other = (Cluster) otherObject;
		// HashSet<DataPoint> s1 = new HashSet<DataPoint>(this.members);
		// HashSet<DataPoint> s2 = new HashSet<DataPoint>(other.getMembers());
		//
		// return (s1.equals(s2) && this.centroid.equals(other.getCentroid()));
		boolean membersAreSame = (this.members.containsAll(other.getMembers())
				&& (other.getMembers().containsAll(this.members)));
		boolean centroidsAreSame = this.getCentroid().equals(other.getCentroid());

		return (membersAreSame && centroidsAreSame);
	}

	public void removeAllMembers()
	{
		members.clear();
	}

	Cluster()
	{
		// this.centroid = centroid;
		this.members = new ArrayList<DataPoint>();
		// distance = new AlignmentBasedDistance();
	}

	public boolean contains(DataPoint a)
	{
		return members.contains(a);
	}

	/**
	 * 
	 * @param distancesMap
	 * @return
	 */
	public boolean updateCentroid(DistancesMap distancesMap)
	{
		boolean centroidChanged = true;
		double minAvgDistance = Double.MAX_VALUE;

		DataPoint newCentroid = getCentroid(); // initialise with old centroid

		ArrayList<DataPoint> allPoints = new ArrayList<DataPoint>(members);
		// allPoints.add(getCentroid());

		for (int i = 0; i < allPoints.size(); i++)
		{
			double sumDistances = 0;
			for (int j = 0; j < allPoints.size(); j++)
			{
				if (j != i)
				{
					sumDistances += distancesMap.getDistanceBetweenDataPoints(allPoints.get(i), allPoints.get(j));// getDistance(allPoints.get(i),
																													// allPoints.get(j));
				}
			}
			double avgDistance = sumDistances / (allPoints.size() - 1);
			if (avgDistance < minAvgDistance)
			{
				minAvgDistance = avgDistance;
				// indexOfCentroidInMembers = i;
				newCentroid = allPoints.get(i);
			}
		}

		// if (indexOfCentroidInMembers == centroidIndex)
		if (distancesMap.getDistanceBetweenDataPoints(newCentroid, centroid) == 0) // check whether the centroid changed
		{
			centroidChanged = false;
			System.out.println("Inside cluster.updateCentroid: same centroid ="
					+ this.getCentroid().getLabel().toString() + " but with avg dist from others = " + minAvgDistance);
		}
		else
		{
			centroidChanged = true;
			setCentroid(newCentroid);
			System.out.println("Inside cluster.updateCentroid: updated new centroid ="
					+ this.getCentroid().getLabel().toString() + " with avg dist from others = " + minAvgDistance);
			// setCentroidInMembers(indexOfCentroidInMembers);
		}
		// System.out.println("Inside cluster.updateCentroid: updated centroid =" + this.getCentroid().toString() + "
		// with avg dist from others = " + minAvgDistance);
		return centroidChanged;
	}

	// private double getDistance(DataPoint a, DataPoint b)
	// {
	// double dist = AlignmentBasedDistance.getMySimpleLevenshteinDistanceWithoutTrace(a.toString(), b.toString(), 1, 1,
	// 2);
	// // System.out.println("\n\t\tdist between " + a.toString() + " and " + b.toString() + " = " + dist);
	// return (dist);
	// }

	/**
	 * Checks if the cluster does not contain any duplicate DataPoints
	 * 
	 * @return
	 */
	public boolean noDuplicates()
	{
		HashSet<DataPoint> hs = new HashSet<DataPoint>(members);
		return (hs.size() == members.size());// if the set is same size as list, this means there are no duplicates in
												// list
	}

	public int size()
	{
		return this.members.size();
	}

	public void addToCluster(DataPoint t)
	{
		this.members.add(t);
	}

	public DataPoint getCentroid()
	{
		// return members.get(centroidIndex);
		if (centroid == null)
		{
			System.err.println("Centroid is null");
		}
		return centroid;
	}

	// public void setCentroid(DataPoint centroid)
	// {
	// this.centroid = centroid;
	// }

	// public void setCentroidInMembers(int index)
	// {
	// this.centroidIndex = index;
	// }

	public void setCentroid(DataPoint centroid)
	{
		this.centroid = centroid;
	}

	public ArrayList<DataPoint> getMembers()
	{
		return members;
	}

	public void setMembers(ArrayList<DataPoint> members)
	{
		this.members = members;
	}

	// public String toString()
	// {
	// String s;
	// // s = "Centroid: " + getCentroid().toString() + "\n";
	// s = "\nCentroid: " + getCentroid().getLabel().toString() + "\n";
	// s += "Member data points: ";
	// for (int i = 0; i < members.size(); i++)
	// {
	// s += " " + "[" + members.get(i).getLabel().toString() + "];";// + members.get(i).toString() + ";";
	// // s += " " + members.get(i).toString() + ";";
	// }
	// return s;
	// }

	public String toString()
	{
		String s;
		// s = "Centroid: " + getCentroid().toString() + "\n";
		s = "<" + getCentroid().getLabel().toString() + ">";
		// s += "Member data points:";
		// List<DataPoint> members = new List<DataPoint>();
		// members = this.members;
		// Collections.sort(members);

		for (int i = 0; i < members.size(); i++)

		{
			s += "  " + "" + members.get(i).getLabel().toString() + "_";// + members.get(i).toString() + ";";
			// s += " " + members.get(i).toString() + ";";
		}
		return s;
	}

	public String toStringDataValue()
	{
		String s;
		// s = "Centroid: " + getCentroid().toString() + "\n";
		s = "Centroid: " + getCentroid().getDataValue().toString() + "\n";
		s += "Member data points: ";
		for (int i = 0; i < members.size(); i++)
		{
			s += "  " + "[" + members.get(i).getDataValue().toString() + "];";// + members.get(i).toString() + ";";
			// s += " " + members.get(i).toString() + ";";
		}
		return s;
	}

	public String toStringUserWise()
	{
		String s = "";

		// // s = "Centroid: " + getCentroid().toString() + "\n";
		// s = "Centroid: User " + (Integer.valueOf(getCentroid().getLabel().toString()) + 1) + "\t";
		// s += "Members: ";
		// for (int i = 0; i < members.size(); i++)
		// {
		// s += " " + "User " + (Integer.valueOf(members.get(i).getLabel().toString()) + 1) + ";";// +
		// members.get(i).toString() + ";";
		// // s += " " + members.get(i).toString() + ";";
		// }
		//
		//
		// s = "Centroid: " + getCentroid().toString() + "\n";
		int userIDForCentroid = UtilityBelt.getIndexOfUserID(Integer.valueOf(getCentroid().getLabel().toString())) + 1;

		s = "Centroid:  User " + userIDForCentroid + "\t";
		s += "Members: ";
		for (int i = 0; i < members.size(); i++)
		{
			int userIDForMember = UtilityBelt.getIndexOfUserID(Integer.valueOf(members.get(i).getLabel().toString()))
					+ 1;
			s += "  " + "User " + userIDForMember + ";";// + members.get(i).toString() + ";";
			// s += " " + members.get(i).toString() + ";";
		}

		return s;
	}

	public String toStringUserWiseToPrint()
	{
		String s;
		// Centroid
		// s = "User " + (Integer.valueOf(getCentroid().getLabel().toString()) + 1) + ",";
		// // Members;
		// for (int i = 0; i < members.size(); i++)
		// {
		// s += "__" + "User " + (Integer.valueOf(members.get(i).getLabel().toString()) + 1);// +
		// members.get(i).toString() + ";";
		// }
		// s += ",";
		int userIDForCentroid = UtilityBelt.getIndexOfUserID(Integer.valueOf(getCentroid().getLabel().toString())) + 1;
		s = "User " + userIDForCentroid + ",";
		// Members;
		for (int i = 0; i < members.size(); i++)
		{
			int userIDForMember = UtilityBelt.getIndexOfUserID(Integer.valueOf(members.get(i).getLabel().toString()))
					+ 1;
			s += "__" + "User " + userIDForMember;// + members.get(i).toString() + ";";
		}
		s += ",";
		return s;
	}

	/**
	 * Population variance
	 * 
	 * @return
	 */
	public double getIntraClusterVariance(DistancesMap distances)
	{
		if (members.size() == 0)
		{
			System.err.println("Error in org.activity.clustering.Cluster.getVariance() members.size()(" + members.size()
					+ ") == 0");
			PopUps.showError("Error in org.activity.clustering.Cluster.getVariance() members.size()(" + members.size()
					+ ") == 0");
			return -9999;
		}

		// System.out.println("Inside getVariance()");
		double variance = 0;
		// double[] vals = new double[members.size()];
		int count = 0;

		if (members.size() <= 1)
		{
			this.intraClusterVariance = 0;
			return 0;
		}

		else
		{
			for (int i = 0; i < members.size(); i++)
			{
				for (int j = 0; j < members.size(); j++)
				{
					if (i != j)
					{
						double distanceBetweenMembers = distances.getDistanceBetweenDataPoints(members.get(i),
								members.get(j));// AlignmentBasedDistance.getMySimpleLevenshteinDistanceWithoutTrace(members.get(i).toString(),
												// members.get(j).toString(),
												// 1, 1, 2);
						variance += Math.pow(distanceBetweenMembers, 2); // square of distance or
						count++;
					}
				}
			}
			variance = variance / count;

			System.out.println("Intra cluster Variance =" + variance);
			// System.out.println("Inside getVariance()");
			this.intraClusterVariance = variance;
			return variance;// stdev.;
		}
	}

	public double getIntraClusterStandardDeviation()
	{
		// return Math.sqrt(getIntraClusterVariance());
		return Math.sqrt(intraClusterVariance); // to save computation
	}
}
