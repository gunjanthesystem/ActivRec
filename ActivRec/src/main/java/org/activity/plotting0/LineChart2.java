package org.activity.plotting0;

/**
 * Copyright (c) 2008, 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 */
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

/**
 * A chart in which lines connect a series of data points. Useful for viewing data trends over time.
 *
 * @see javafx.scene.chart.LineChart
 * @see javafx.scene.chart.Chart
 * @see javafx.scene.chart.Axis
 * @see javafx.scene.chart.NumberAxis
 * @related charts/area/AreaChart
 * @related charts/scatter/ScatterChart
 */
public class LineChart2 extends Application
{

	private void init(Stage primaryStage)
	{
		Group root = new Group();
		primaryStage.setScene(new Scene(root));
		NumberAxis xAxis = new NumberAxis("Values for X-Axis", 0, 3, 1);
		NumberAxis yAxis = new NumberAxis("Values for Y-Axis", 0, 3, 1);
		ObservableList<XYChart.Series<Double, Double>> lineChartData = FXCollections.observableArrayList(
				new LineChart.Series<Double, Double>("Series 1",
						FXCollections.observableArrayList(new XYChart.Data<Double, Double>(0.0, 1.0),
								new XYChart.Data<Double, Double>(1.2, 1.4), new XYChart.Data<Double, Double>(2.2, 1.9),
								new XYChart.Data<Double, Double>(2.7, 2.3),
								new XYChart.Data<Double, Double>(2.9, 0.5))),
				new LineChart.Series<Double, Double>("Series 2",
						FXCollections.observableArrayList(new XYChart.Data<Double, Double>(0.0, 1.6),
								new XYChart.Data<Double, Double>(0.8, 0.4), new XYChart.Data<Double, Double>(1.4, 2.9),
								new XYChart.Data<Double, Double>(2.1, 1.3),
								new XYChart.Data<Double, Double>(2.6, 0.9))));
		LineChart chart = new LineChart(xAxis, yAxis, lineChartData);
		root.getChildren().add(chart);
	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		init(primaryStage);
		primaryStage.show();
	}

	public static void main(String[] args)
	{
		launch(args);
	}
}
