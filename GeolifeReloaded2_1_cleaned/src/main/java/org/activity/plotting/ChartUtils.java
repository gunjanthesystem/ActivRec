package org.activity.plotting;

import java.util.ArrayList;

import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Tooltip;

public class ChartUtils
{

	public ChartUtils()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 * @param listOfSeries
	 * @param chartTitle
	 * @param toolTipConstantLabel
	 * @return
	 */
	public static LineChart<Double, Double> createLineChart(ObservableList<Series<Double, Double>> listOfSeries,
			String chartTitle, String toolTipConstantLabel)
	{
		// ObservableList<Series<Double, Double>> listOfSeries = FXUtils
		// .toObservableListOfSeriesOfPairData(FXUtils.getSyntheticData(50, 50));

		LineChart<Double, Double> lineChart = new LineChart(new NumberAxis(), new NumberAxis(), listOfSeries);
		ObservableList<Series<Double, Double>> lineChartDataSeriess = lineChart.getData();
		lineChart.setTitle(chartTitle);

		for (Series<Double, Double> dataSeries : lineChartDataSeriess)
		{
			ObservableList<Data<Double, Double>> dataForASeries = dataSeries.getData();

			for (Data<Double, Double> d : dataForASeries)
			{
				Tooltip.install(d.getNode(), new Tooltip(toolTipConstantLabel + ": \n" + d.getXValue().doubleValue()
						+ "," + d.getYValue().doubleValue()));
				// String.format("%2.1f ^ 2 = %2.1f", d.getXValue().doubleValue(), d.getYValue().doubleValue())));
			}
		}
		return lineChart;
	}

	/**
	 * 
	 * @param listOfSeries
	 * @param title
	 * @param toolTipConstantLabel
	 * @return
	 */
	public static ScatterChart<Double, Double> createScatterChart(ObservableList<Series<Double, Double>> listOfSeries,
			String title, String toolTipConstantLabel)
	{
		// ObservableList<Series<Double, Double>> listOfSeries = FXUtils
		// .toObservableListOfSeriesOfPairData(FXUtils.getSyntheticData(50, 50));

		ScatterChart<Double, Double> chart = new ScatterChart(new NumberAxis(), new NumberAxis(), listOfSeries);
		ObservableList<Series<Double, Double>> chartDataSeries = chart.getData();
		chart.setTitle(title);

		for (Series<Double, Double> dataSeries : chartDataSeries)
		{
			ObservableList<Data<Double, Double>> dataForASeries = dataSeries.getData();

			for (Data<Double, Double> d : dataForASeries)
			{
				Tooltip.install(d.getNode(), new Tooltip(toolTipConstantLabel + ": \n" + d.getXValue().doubleValue()
						+ "," + d.getYValue().doubleValue()));
				// String.format("%2.1f ^ 2 = %2.1f", d.getXValue().doubleValue(), d.getYValue().doubleValue())));
			}
		}
		return chart;
	}

	public static ScatterChart<Double, Double> createScatterChart(ObservableList<Series<Double, Double>> listOfSeries,
			String title, String toolTipConstantLabel, ArrayList<ArrayList<String>> toolTipForEachData)
	{
		// ObservableList<Series<Double, Double>> listOfSeries = FXUtils
		// .toObservableListOfSeriesOfPairData(FXUtils.getSyntheticData(50, 50));

		ScatterChart<Double, Double> chart = new ScatterChart(new NumberAxis(), new NumberAxis(), listOfSeries);
		ObservableList<Series<Double, Double>> chartDataSeries = chart.getData();
		chart.setTitle(title);

		int seriesNum = 0;
		for (Series<Double, Double> dataSeries : chartDataSeries)
		{
			ObservableList<Data<Double, Double>> dataForASeries = dataSeries.getData();
			ArrayList<String> toolTipForEachDataInThisSeries = toolTipForEachData.get(seriesNum);

			int dataNum = 0;
			for (Data<Double, Double> d : dataForASeries)
			{
				Tooltip.install(d.getNode(),
						new Tooltip(toolTipConstantLabel + ":\n" + toolTipForEachDataInThisSeries.get(dataNum) + "\nx="
								+ d.getXValue().doubleValue() + ",y=" + d.getYValue().doubleValue()));
				// String.format("%2.1f ^ 2 = %2.1f", d.getXValue().doubleValue(), d.getYValue().doubleValue())));
			}
			seriesNum += 1;
		}
		return chart;
	}

	/**
	 * 
	 * @param listOfSeries
	 * @param title
	 * @param toolTipConstantLabel
	 * @param xLabel
	 * @param yLabel
	 * @return
	 */
	public static ScatterChart<Double, Double> createScatterChart(ObservableList<Series<Double, Double>> listOfSeries,
			String title, String toolTipConstantLabel, String xLabel, String yLabel,
			ArrayList<ArrayList<String>> toolTipForEachData)
	{
		ScatterChart<Double, Double> chart = createScatterChart(listOfSeries, title, toolTipConstantLabel,
				toolTipForEachData);
		chart.getXAxis().setLabel(xLabel);
		chart.getYAxis().setLabel(yLabel);
		// chart.setStyle("-fx-background-color: CHART_COLOR_6, white;\n" + " -fx-background-insets: 0, 2;\n"
		// + " -fx-background-radius: 5px;\n" + " -fx-padding: 5px;");
		return chart;
	}

}
