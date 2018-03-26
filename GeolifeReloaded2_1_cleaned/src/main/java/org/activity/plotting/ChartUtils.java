package org.activity.plotting;

import java.util.ArrayList;
import java.util.List;

import org.activity.ui.EpochStringConverter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Tooltip;
import javafx.util.StringConverter;

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
	 * @param chartTitle
	 * @param toolTipConstantLabel
	 * @return
	 */
	public static LineChart<Double, String> createLineChart2(
			List<List<List<String>>> dataReceived /* ObservableList<Series<Double, Double>> listOfSeries */,
			String chartTitle, String toolTipConstantLabel)
	{
		System.out.println("createTimelineContent() called");
		// xAxis = new NumberAxis();
		// long millis = 100;

		// String pattern = "dd MMMM yyyy, HH:mm:ss";
		// DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		// StringConverter<LocalDateTime> converter = new LocalDateTimeStringConverter(formatter, null);
		// // assertEquals("12 January 1985, 12:34:56", converter.toString(VALID_LDT_WITH_SECONDS));
		// LocalDateTime date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
		if (false)// for date formatted axis
		{
			StringConverter converter2 = new EpochStringConverter();
			// xAxis.setTickLabelFormatter(converter2);
		} // (new NumberAxis.DefaultFormatter(xAxis, "$", "*"));
			// (new NumberAxis.DefaultFormatter(yAxis, "$ ", null));

		// xAxis.setMinorTickCount(0);
		// yAxis = new CategoryAxis();
		// timelineChart = new TimelineChartCircle(xAxis, yAxis);

		// chart.setStyle("");
		// setup chart
		// xAxis.setLabel("Timestamp");
		// yAxis.setLabel("Timelines");

		// xAxis.setStyle(value);
		// add starting data

		// List<List<String>> dataReceived = DataGenerator.getData2();

		// long maxXAxis = 0, minXAxis = Long.MAX_VALUE;

		ObservableList<XYChart.Series<Number, String>> seriesForAllUsers = FXCollections.observableArrayList();

		for (List<List<String>> eachUserData : dataReceived)
		{
			XYChart.Series<Number, String> seriesForAUser = new XYChart.Series<Number, String>();

			for (List<String> d : eachUserData)
			{
				String userID = d.get(0);
				double startTS = Double.valueOf(d.get(1));
				double endTS = Double.valueOf(d.get(2));
				String locName = d.get(3);
				String actName = d.get(4);
				Integer actID = Integer.valueOf(d.get(5));
				double startLat = 0;

				// end timestamp
				final ActivityBoxExtraValues extras = new ActivityBoxExtraValues(endTS, actName, actID, startLat);// end

				// start timeestamp, username, {end timestamp, actname, }
				seriesForAUser.getData().add(new XYChart.Data<Number, String>(startTS, userID, extras));
			}
			seriesForAllUsers.add(seriesForAUser);
		}

		// // upper bound is maxXAxis rounded to ceiling multiple of 10
		// this.maxXAxis = (long) (Math.ceil(maxXAxis / 100d) * 100);
		// // upper bound is maxXAxis rounded to ceiling multiple of 10
		// this.minXAxis = (long) (Math.floor(minXAxis / 100d) * 100);
		// ObservableList<XYChart.Series<Number, String>> data = timelineChart.getData();

		//

		LineChart<Double, String> lineChart = new LineChart(new NumberAxis(), new CategoryAxis(), seriesForAllUsers);
		ObservableList<Series<Double, String>> lineChartDataSeriess = lineChart.getData();
		lineChart.setTitle(chartTitle);

		for (Series<Double, String> dataSeries : lineChartDataSeriess)
		{
			ObservableList<Data<Double, String>> dataForASeries = dataSeries.getData();

			for (Data<Double, String> d : dataForASeries)
			{
				Tooltip.install(d.getNode(), new Tooltip(toolTipConstantLabel + ": \n"));
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
