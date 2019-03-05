package org.activity.plotting0;

import java.util.ArrayList;
import java.util.List;

import org.activity.stats.StatsUtils;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

public class XChart01
{

	public XChart01()
	{
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws Exception
	{

		List<Double> v1s = new ArrayList<>();
		List<Double> v2s = new ArrayList<>();

		for (int i = 0; i < 10000; i++)
		{
			v1s.add((double) StatsUtils.randomInRange(0, 50));
			v2s.add((double) StatsUtils.randomInRange(0, 200));
		}
		double[] xData = v1s.stream().mapToDouble(i -> i).toArray();// new double[] { 0.0, 1.0, 2.0 };
		double[] yData = v2s.stream().mapToDouble(i -> i).toArray();// new double[] { 2.0, 1.0, 0.0 };

		// Create Chart
		XYChart chart = QuickChart.getChart("Sample Chart", "X", "Y", "y(x)", xData, yData);

		// Show it
		new SwingWrapper(chart).displayChart();

	}

	// v1s
}
