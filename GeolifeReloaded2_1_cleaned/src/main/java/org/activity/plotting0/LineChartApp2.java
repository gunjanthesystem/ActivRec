package org.activity.plotting0;

import java.util.ArrayList;

import org.activity.objects.Pair;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.stage.Stage;

/**
 * A chart in which lines connect a series of data points. Useful for viewing data trends over time.
 */
public class LineChartApp2 extends Application
{

	public static LineChart<Double, Double> createLineChart()
	{
		NumberAxis xAxis = new NumberAxis("Values for X-Axis", 0, 3, 1);
		NumberAxis yAxis = new NumberAxis("Values for Y-Axis", 0, 3, 1);

		ObservableList<XYChart.Series<Double, Double>> lineChartData = FXCollections.observableArrayList(
				new LineChart.Series<>("Series 1",
						FXCollections.observableArrayList(new XYChart.Data<>(0.0, 1.0), new XYChart.Data<>(1.2, 1.4),
								new XYChart.Data<>(2.2, 1.9), new XYChart.Data<>(2.7, 2.3),
								new XYChart.Data<>(2.9, 0.5))),
				new LineChart.Series<>("Series 2",
						FXCollections.observableArrayList(new XYChart.Data<>(0.0, 1.6), new XYChart.Data<>(0.8, 0.4),
								new XYChart.Data<>(1.4, 2.9), new XYChart.Data<>(2.1, 1.3),
								new XYChart.Data<>(2.6, 0.9))));

		return new LineChart(xAxis, yAxis, lineChartData);
	}

	/**
	 * 
	 * @param listOfListOfPairData
	 *            list of pairs(SeriesName, list of pairs(x,y)) for t
	 * @return
	 */
	public static LineChart<Double, Double> createLineChart2(
			ArrayList<Pair<String, ArrayList<Pair<Double, Double>>>> listOfListOfPairData)
	{
		NumberAxis xAxis = new NumberAxis("Values for X-Axis", 0, 3, 1);
		NumberAxis yAxis = new NumberAxis("Values for Y-Axis", 0, 3, 1);

		ObservableList<XYChart.Series<Double, Double>> lineChartData = FXCollections.observableArrayList(
				new LineChart.Series<>("Series 1",
						FXCollections.observableArrayList(new XYChart.Data<>(0.0, 1.0), new XYChart.Data<>(1.2, 1.4),
								new XYChart.Data<>(2.2, 1.9), new XYChart.Data<>(2.7, 2.3),
								new XYChart.Data<>(2.9, 0.5))),
				new LineChart.Series<>("Series 2",
						FXCollections.observableArrayList(new XYChart.Data<>(0.0, 1.6), new XYChart.Data<>(0.8, 0.4),
								new XYChart.Data<>(1.4, 2.9), new XYChart.Data<>(2.1, 1.3),
								new XYChart.Data<>(2.6, 0.9))));

		return new LineChart(xAxis, yAxis, lineChartData);
	}

	// public static void getLineChart(NumberAxis xAxis, NumberAxis yAxis, List<Pair<Double, Double>> data)
	// {
	// return null;
	// }

	// public static <T> void toObservableArrayList(ArrayList<Pair<T, T>> data)
	// {
	// Series series = new Series();
	// data.stream().map(data -> )
	// }

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		ObservableList<Series<Double, Double>> listOfSeries = FXUtils
				.toObservableListOfSeriesOfPairData(FXUtils.getSyntheticData(3, 10));

		NumberAxis xAxis = new NumberAxis();// "Values for X-Axis", 0, 3, 1);
		NumberAxis yAxis = new NumberAxis();// "Values for Y-Axis", 0, 3, 1);

		primaryStage.setScene(new Scene(new LineChart(xAxis, yAxis, listOfSeries)));
		// primaryStage.setScene(new Scene(createLineChart()));
		primaryStage.show();
	}

	/**
	 * Java main for when running without JavaFX launcher
	 */
	public static void main(String[] args)
	{
		launch(args);
	}

}