package org.activity.plotting0;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activity.objects.Pair;
import org.activity.objects.Triple;
import org.activity.stats.StatsUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;

public class ChartUtil
{

	/**
	 * 
	 * @param number
	 * @return
	 */
	public static Integer calculateFloorPowerOfTen(Integer number)
	{
		Integer powerOfTen = 0;
		Double number2 = number / 10.0;
		while (number2 > 10)
		{
			powerOfTen++;
			number2 = number2 / 10;
		}
		Double d = new Double(Math.pow(10, powerOfTen));
		Integer result = new Double(Math.floor(number2) * d).intValue();
		if (result == 0)
		{
			result = 1;
		}
		return result;
	}

	/**
	 * 
	 * @param upperbound
	 * @param tickUnit
	 * @return
	 */
	public static Float calculateUpperbound(Float upperbound, Integer tickUnit)
	{
		float temp = upperbound / tickUnit;
		return new Float(Math.ceil(new Float(temp).doubleValue()) * tickUnit);
	}

	/**
	 * 
	 * @param upperbound
	 * @param tickUnit
	 * @return
	 */
	public static Float calculateLowerbound(Float upperbound, Integer tickUnit)
	{
		float temp = upperbound / tickUnit;
		return new Float(Math.floor(new Float(temp).doubleValue()) * tickUnit);
	}

	/**
	 * 
	 * @param doubleList
	 * @param numOfBins
	 * @param sizeOfBins
	 * @param numOrSizeOfBins
	 * @return
	 */
	public static Node getHistogramChart(List<Double> doubleList, int numOfBins, int sizeOfBins,
			boolean numOrSizeOfBins)
	{
		Map<Integer, List<Double>> binIndexListOfVals = null;
		Map<Integer, Pair<Double, Double>> binIndexBoundary = null;

		if (numOrSizeOfBins)
		{
			Pair<Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>>, Double> res = StatsUtils
					.binValuesByNumOfBins(doubleList, numOfBins, false);
			binIndexListOfVals = res.getFirst().getThird();
			binIndexBoundary = res.getFirst().getSecond();
		}
		else
		{
			Pair<Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer, List<Double>>>, Integer> res2 = StatsUtils
					.binValuesByBinSize(doubleList, sizeOfBins, false);

			binIndexListOfVals = res2.getFirst().getThird();
			binIndexBoundary = res2.getFirst().getSecond();
		}

		CategoryAxis xAxis = new CategoryAxis();
		NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel("Frequency Count");
		ObservableList<XYChart.Series<String, Double>> barChartData = FXCollections.observableArrayList();
		Series barChartSeries1 = new XYChart.Series();
		barChartSeries1.setName("FirstSeries");

		for (Entry<Integer, Pair<Double, Double>> binEntry : binIndexBoundary.entrySet())
		{
			barChartSeries1.getData()
					.add(new XYChart.Data(
							"[" + binEntry.getValue().getFirst() + "," + binEntry.getValue().getSecond() + ")",
							binIndexListOfVals.get(binEntry.getKey()).size()));
		}

		barChartData.addAll(barChartSeries1);
		BarChart chart = new BarChart(xAxis, yAxis, barChartData, 5);
		chart.setLegendVisible(false);
		// chart.setBarGap(5);
		// chart.setCategoryGap(5);
		return chart;
	}

}