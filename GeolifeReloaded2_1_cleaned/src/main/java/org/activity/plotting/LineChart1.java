package org.activity.plotting;

/**
 * Copyright (c) 2008, 2012 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 */
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

/**
 * An advanced line chart with a variety of actions and settable properties.
 *
 * @see javafx.scene.chart.LineChart
 * @see javafx.scene.chart.Chart
 * @see javafx.scene.chart.NumberAxis
 * @see javafx.scene.chart.XYChart
 */
public class LineChart1 extends Application
{

	private void init(Stage primaryStage)
	{
		//
		Group root = new Group();
		Scene scene = new Scene(root, 1000, 1000);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		primaryStage.setTitle("Hello World");
		primaryStage.setScene(scene);
		primaryStage.setHeight(1000);
		primaryStage.setWidth(1000);
		//
		// Group root = new Group();
		// primaryStage.setScene(new Scene(root));
		root.getChildren().add(createChart());
	}

	protected LineChart<Number, Number> createChart()
	{
		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		final LineChart<Number, Number> lc = new LineChart<Number, Number>(xAxis, yAxis);
		// setup chart
		lc.setTitle("Basic LineChart");
		xAxis.setLabel("X Axis");
		yAxis.setLabel("Y Axis");
		// add starting data
		XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
		series.setName("Data Series 1");
		series.getData().add(new XYChart.Data<Number, Number>(20d, 50d));
		series.getData().add(new XYChart.Data<Number, Number>(40d, 80d));
		series.getData().add(new XYChart.Data<Number, Number>(50d, 90d));
		series.getData().add(new XYChart.Data<Number, Number>(70d, 30d));
		series.getData().add(new XYChart.Data<Number, Number>(170d, 122d));

		XYChart.Series<Number, Number> series2 = new XYChart.Series<Number, Number>();
		series2.setName("Data Series 2");
		series2.getData().add(new XYChart.Data<Number, Number>(220d, 50d));
		series2.getData().add(new XYChart.Data<Number, Number>(320d, 80d));
		series2.getData().add(new XYChart.Data<Number, Number>(220d, 90d));
		series2.getData().add(new XYChart.Data<Number, Number>(170d, 30d));
		series2.getData().add(new XYChart.Data<Number, Number>(170d, 122d));

		lc.getData().add(series);
		lc.getData().add(series2);
		lc.getData().add(series2);
		return lc;
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
