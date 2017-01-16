package org.activity.util;

import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class DescriptiveStatisticsG extends DescriptiveStatistics
{
	public DescriptiveStatisticsG(ArrayList<Double> valsD)
	{
		super(listToArray(valsD));
	}

	public static double[] listToArray(ArrayList<Double> valsD)
	{
		double valsArr[] = new double[valsD.size()];

		for (int i = 0; i < valsD.size(); i++)
		{
			valsArr[i] = valsD.get(i);
		}

		return valsArr;
	}
}
