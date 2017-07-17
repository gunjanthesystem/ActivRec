package org.activity.plotting;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

import org.activity.objects.Pair;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Tooltip;

public class FXUtils
{

	/**
	 * 
	 * @param listOfPairData
	 * @return
	 */
	public static ObservableList<XYChart.Data<Double, Double>> toObservableCollectionOfPairData(
			ArrayList<Pair<Double, Double>> listOfPairData)
	{
		ObservableList<XYChart.Data<Double, Double>> obslistOfXYChartData = listOfPairData.stream()
				.map(pair -> new XYChart.Data<>(pair.getFirst(), pair.getSecond()))
				.collect(Collectors.toCollection(FXCollections::observableArrayList));

		return obslistOfXYChartData;
	}

	public static void getTooltippeXYChartData(double a, double b)
	{
		XYChart.Data dataPoint = new XYChart.Data<>(a, b);
		Tooltip t = new Tooltip(new String(a + "," + b));
		// Tooltip.install(dataPoint, t);
	}

	/**
	 * 
	 * @param listOfPairData
	 * @param seriesName
	 * @return
	 */
	public static Series<Double, Double> toSeriesOfPairData(ArrayList<Pair<Double, Double>> listOfPairData,
			String seriesName)
	{
		ObservableList<XYChart.Data<Double, Double>> obslistOfXYChartData = (ObservableList<Data<Double, Double>>) toObservableCollectionOfPairData(
				listOfPairData);
		Series<Double, Double> result = new LineChart.Series<Double, Double>(seriesName, obslistOfXYChartData);
		return result;// new LineChart.Series<Double, Double>(seriesName, obslistOfXYChartData);
	}

	/**
	 * 
	 * @param listOfListOfPairData
	 *            list of pairs(SeriesName, list of pairs(x,y)) for t
	 * @return
	 */
	public static ObservableList<Series<Double, Double>> toObservableListOfSeriesOfPairData(
			ArrayList<Pair<String, ArrayList<Pair<Double, Double>>>> listOfListOfPairData)
	{

		ObservableList<XYChart.Series<Double, Double>> observableListOfSeriesOfPairData = listOfListOfPairData.stream()
				.map(aPairForASeries -> toSeriesOfPairData(aPairForASeries.getSecond(), aPairForASeries.getFirst()))
				.collect(Collectors.toCollection(FXCollections::observableArrayList));
		return observableListOfSeriesOfPairData;
	}

	/**
	 * 
	 * @param numOfDataSeries
	 * @param numOfDataPointsInEachSeries
	 * @return {("A",(1,2)),("B",(2,3))}
	 */
	public static ArrayList<Pair<String, ArrayList<Pair<Double, Double>>>> getSyntheticData(int numOfDataSeries,
			int numOfDataPointsInEachSeries)
	{
		// {("A",(1,2)),("B",(2,3))}
		ArrayList<Pair<String, ArrayList<Pair<Double, Double>>>> data = new ArrayList<Pair<String, ArrayList<Pair<Double, Double>>>>();
		Random r = new Random();
		for (double series = 1; series <= numOfDataSeries; series++)
		{
			ArrayList<Pair<Double, Double>> dataForASeries = new ArrayList<>();

			for (double x = 1; x <= numOfDataPointsInEachSeries; x++)
			{
				int yIncrease = r.nextInt(100);
				// System.out.println("yIncrease = " + yIncrease);
				dataForASeries.add(new Pair<Double, Double>(x + 10 + series, x + yIncrease));
			}
			data.add(new Pair<String, ArrayList<Pair<Double, Double>>>(String.valueOf(series), dataForASeries));
		}
		return data;
	}

}
