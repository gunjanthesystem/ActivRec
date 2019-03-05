package org.activity.plotting0;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activity.objects.Pair;
import org.activity.objects.Triple;
import org.activity.stats.DescriptiveStatisticsG;
import org.activity.stats.StatsUtils;
import org.apache.commons.lang3.ArrayUtils;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

public class BarChart3 extends Application
{

	private BarChart chart;
	private CategoryAxis xAxis;
	private NumberAxis yAxis;

	public Parent createContent()
	{
		String[] years = { "2007", "2008", "2009" };
		xAxis = new CategoryAxis();
		xAxis.setCategories(FXCollections.<String>observableArrayList(years));
		yAxis = new NumberAxis("Units Sold", 0.0d, 3000.0d, 1000.0d);
		ObservableList<BarChart.Series> barChartData = FXCollections.observableArrayList(
				new BarChart.Series("Apples",
						FXCollections.observableArrayList(new BarChart.Data(years[0], 567d),
								new BarChart.Data(years[1], 1292d), new BarChart.Data(years[2], 1292d))),
				new BarChart.Series("Lemons",
						FXCollections.observableArrayList(new BarChart.Data(years[0], 956),
								new BarChart.Data(years[1], 1665), new BarChart.Data(years[2], 2559))),
				new BarChart.Series("Oranges", FXCollections.observableArrayList(new BarChart.Data(years[0], 1154),
						new BarChart.Data(years[1], 1927), new BarChart.Data(years[2], 2774))));
		chart = new BarChart(xAxis, yAxis, barChartData, 25.0d);
		return chart;
	}

	public Parent createContent2()
	{
		//////////////////////////////////////////////////////////
		double[] intArr = new double[] { 5, 10, 15, 20, 50, 100, 55, 70, 98 };// , 101, -1 };
		Double[] doubleArray = ArrayUtils.toObject(intArr);
		List<Double> doubleList = Arrays.asList(doubleArray);
		System.out.println("Vals  = " + Arrays.asList(doubleList));

		// Triple{valToBinIndex, binIndexBoundary, binIndexListOfVals}

		// public static Triple<List<Pair<Double, Integer>>, Map<Integer, Pair<Double, Double>>, Map<Integer,
		// List<Double>>>
		// {Pair{Triple {valToBinIndex, binIndexBoundary, binIndexListOfVals}}, binSize}

		int numOfBins = 10;

		return getHistogramChart(doubleList, numOfBins, 50, true);

	}

	public static Parent getHistogramChart(List<Double> doubleList, int numOfBins, int sizeOfBins,
			boolean numOrSizeOfBins)
	{
		Map<Integer, List<Double>> binIndexListOfVals = null;
		Map<Integer, Pair<Double, Double>> binIndexBoundary = null;

		DescriptiveStatisticsG ds = new DescriptiveStatisticsG(new ArrayList<>(doubleList));
		double median = ds.getPercentile(50);

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
		// xAxis.setTickLabelFont(value);
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

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		// primaryStage.setScene(new Scene(createContent()));
		primaryStage.setScene(new Scene(createContent2()));
		primaryStage.show();
	}

	/**
	 * Java main for when running without JavaFX launcher
	 * 
	 * @param args
	 *            command line arguments
	 */
	public static void main(String[] args)
	{
		launch(args);
	}
}
