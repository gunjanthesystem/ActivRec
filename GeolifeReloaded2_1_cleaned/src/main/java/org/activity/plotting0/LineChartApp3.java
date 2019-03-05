package org.activity.plotting0;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.activity.constants.PathConstants;
import org.activity.io.ReadingFromFile;
import org.activity.objects.Pair;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.io.csv.CsvReadOptions.Builder;

/**
 * Fork of LineChartApp2 A chart in which lines connect a series of data points. Useful for viewing data trends over
 * time.
 * 
 * @since 4 March 2019
 */
public class LineChartApp3 extends Application
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

	public static ArrayList<Pair<String, ArrayList<Pair<Double, Double>>>> getSequenceForChosenVariable(
			List<List<String>> allAOData, List<String> header, String featureHeader)
	{

		ArrayList<Pair<String, ArrayList<Pair<Double, Double>>>> listOfListOfPairData = new ArrayList<>();// singleton
		listOfListOfPairData
				.add(new Pair<>(featureHeader, getSequenceForChosenVariable(header.indexOf(featureHeader), allAOData)));
		return listOfListOfPairData;
	}

	/**
	 * 
	 * @param indexOfChosenVariable
	 * @param allAOData
	 * @return
	 */
	private static ArrayList<Pair<Double, Double>> getSequenceForChosenVariable(int indexOfChosenVariable,
			List<List<String>> allAOData)
	{
		int rowIndex = 0;
		ArrayList<Pair<Double, Double>> seriesForChosenVariable = new ArrayList<>(allAOData.size());
		for (List<String> rowEntry : allAOData)
		{
			seriesForChosenVariable
					.add(new Pair<>((double) rowIndex, Double.valueOf(rowEntry.get(indexOfChosenVariable))));
			rowIndex += 1;
		}
		return seriesForChosenVariable;

	}

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		String databaseName = "geolife1";

		List<List<String>> allAOData = ReadingFromFile.readLinesIntoListOfLists(
				PathConstants.getPathToCleanedTimelinesFeb2019(databaseName) + "AllActObjs.csv", ",");
		List<String> header = allAOData.remove(0);
		System.out.println("allAOData.size() = " + allAOData.size());

		ObservableList<Series<Double, Double>> listOfSeries_SThourOfDay = FXUtils
				.toObservableListOfSeriesOfPairData(getSequenceForChosenVariable(allAOData, header, "SThourOfDay"));
		// System.out.println("listOfSeries.size() = " + listOfSeries.size());

		ObservableList<Series<Double, Double>> listOfSeries_durationInSeconds = FXUtils
				.toObservableListOfSeriesOfPairData(
						getSequenceForChosenVariable(allAOData, header, "durationInSeconds"));
		// System.out.println("listOfSeries.size() = " + listOfSeries.size());

		ObservableList<Series<Double, Double>> listOfSeries_distanceTravelledInKm = FXUtils
				.toObservableListOfSeriesOfPairData(
						getSequenceForChosenVariable(allAOData, header, "distanceTravelledInKm"));

		// vbox.setPadding(new Insets(10));
		// vbox.setSpacing(8);

		LineChart chart_SThourOfDay = new LineChart(new NumberAxis(), new NumberAxis(), listOfSeries_SThourOfDay);
		// HBox hbox_SThourOfDay = new HBox(chart_SThourOfDay);
		// hbox_SThourOfDay.getChildren().add();

		LineChart chart_durationInSeconds = new LineChart(new NumberAxis(), new NumberAxis(),
				listOfSeries_durationInSeconds);
		LineChart chart_distanceTravelledInKm = new LineChart(new NumberAxis(), new NumberAxis(),
				listOfSeries_distanceTravelledInKm);

		// distanceTravelledInKm

		VBox vboxOfCharts = new VBox();
		vboxOfCharts.getChildren().add((chart_SThourOfDay));
		vboxOfCharts.getChildren().add((chart_durationInSeconds));
		vboxOfCharts.getChildren().add((chart_distanceTravelledInKm));
		vboxOfCharts.setAlignment(Pos.CENTER);

		ScrollPane s1 = new ScrollPane();
		s1.setContent(vboxOfCharts);
		s1.setFitToHeight(true);
		s1.setFitToWidth(true);

		primaryStage.setScene(new Scene(s1));

		// primaryStage.setScene(new Scene(createLineChart()));
		primaryStage.show();
	}

	/**
	 * Java main for when running without JavaFX launcher
	 */
	public static void main(String[] args)
	{
		launch(args);
		// fun1();
	}

	private static void fun1()
	{
		Builder builder = CsvReadOptions
				.builder(PathConstants.getPathToCleanedTimelinesFeb2019("geolife1") + "AllActObjs.csv").separator(',')
				.header(true) // no header
				.dateFormat("yyyy-MM-dd"); // the date format to use.//2008-06-24

		CsvReadOptions options = builder.build();

		try
		{
			Table t1 = Table.read().csv(options);
			System.out.println(t1.structure());
			System.out.println(t1.first(5));
			System.out.println(t1.shape());

			String colName = "durationInSeconds";
			System.out.println("Type = " + t1.column(colName).type());
			List<?> colAsList = t1.column(colName).asList();
			List<?> listOfPairs = IntStream.range(0, t1.rowCount()).mapToObj(i -> new Pair<>(i, colAsList.get(i)))
					.collect(Collectors.toList());

			System.out.println(listOfPairs.toString());
			// .mapToObject(i -> new Pair<>(i, res11.get(i)));
			// Plot.show(TimeSeriesPlot.create("Fox approval ratings", t1, "STdateOnly", "durInSecPrev"));

			// Plot.show(LinePlot.create("Monthly Boston Robberies: Jan 1966-Oct 1975", t1, "distanceTravelledInKm",
			// "durInSecPrev"));

			// Plot.show(Histogram.create("Distribution of prices", t1, "durationInSeconds"));
			// Plot.show(HorizontalBarPlot.create("fatalities by scale", // plot title
			// t1, // table
			// "activityName", // grouping column name
			// "sum[durationInSeconds]")); // numeric column name
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}