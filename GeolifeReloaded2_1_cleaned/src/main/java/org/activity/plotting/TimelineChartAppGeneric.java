package org.activity.plotting;

import java.util.List;

import org.activity.objects.Triple;
import org.activity.ui.EpochStringConverter;
import org.activity.ui.PopUps;
import org.controlsfx.control.RangeSlider;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
//import javafx.geometry.Insets;
//import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
//import com.beust.jcommander.converters.StringConverter;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;

/**
 * 
 * <p>
 * 
 * @see javafx.scene.chart.NumberAxis
 * 
 * @see javafx.scene.chart.XYChart
 * @see javafx.scene.shape.LineTo
 * @see javafx.scene.shape.MoveTo
 *
 */
public class TimelineChartAppGeneric extends Pane
{

	/// ActName, Val1, Val2, Val3,
	// static List<Pair<String, List<Double>>> allData;
	private VBox vbox;

	public VBox getVbox()
	{
		return vbox;
	}

	private XYChart<Number, String> timelineChart;
	private NumberAxis xAxis;
	private CategoryAxis yAxis;
	long maxXAxis, minXAxis;

	public TimelineChartAppGeneric(List<List<List<String>>> dataReceived, boolean hasXAxisRangeSlider,
			String typeOfTimelineChart)
	{
		vbox = new VBox();
		xAxis = new NumberAxis();
		yAxis = new CategoryAxis();

		xAxis.setLabel("Timestamp");
		yAxis.setLabel("Users' Timelines");

		switch (typeOfTimelineChart)
		{
			case "ActivityBox":
				timelineChart = new TimelineChart2(xAxis, yAxis);
				break;
			case "ActivityCircle":
				timelineChart = new TimelineChartCircle(xAxis, yAxis);
				break;
			case "LineChart":
				timelineChart = new LineChart<Number, String>(xAxis, yAxis);
				break;
			case "ScatterChart":
				timelineChart = new ScatterChart<Number, String>(xAxis, yAxis);
				break;
			default:
				PopUps.showError("Unrecognised typeOfTimelineChart = " + typeOfTimelineChart);
		}

		Triple<XYChart<Number, String>, Long, Long> res = createTimelineContent(dataReceived, timelineChart);
		XYChart<Number, String> timelineChart = res.getFirst();
		this.maxXAxis = res.getSecond();
		this.minXAxis = res.getThird();

		// formatXAxis(maxXAxis, minXAxis);

		vbox.getChildren().add(timelineChart);
		vbox.getChildren().add(createXAxisRangeSlider((NumberAxis) timelineChart.getXAxis(), maxXAxis, minXAxis));
		VBox.setVgrow(timelineChart, Priority.ALWAYS);
		// return vbox;
		this.getChildren().add(vbox);
	}

	/**
	 * 
	 * @param axis
	 * @return
	 */
	private BorderPane createXAxisRangeSlider(NumberAxis axis, long maxXAxis, long minXAxis)
	{
		BorderPane pane = new BorderPane();
		// axis.getUpperBound();

		// System.out.println("(axis.getLowerBound()=" + axis.getLowerBound());
		// System.out.println("(axis.getUpperBound()=" + axis.getUpperBound());
		final RangeSlider hSlider = new RangeSlider(minXAxis, maxXAxis, minXAxis, maxXAxis);
		// axis.getLowerBound(), axis.getUpperBound(), axis.getLowerBound(),
		// axis.getUpperBound());/
		// System.out.println("hSlider.getStyleClass() =" + hSlider.getStyleClass());

		hSlider.setSnapToTicks(true);

		// hSlider.setMajorTickUnit((int) ((axis.getUpperBound() - axis.getLowerBound()) / 100));
		// hSlider.setMinorTickUnit((int) ((axis.getUpperBound() - axis.getLowerBound()) / 100));
		// hSlider.setBlockIncrement(200);// (int) ((axis.getUpperBound() - axis.getLowerBound()) / 100));

		hSlider.setBlockIncrement((maxXAxis - minXAxis) / 200);
		hSlider.setMajorTickUnit(Math.max((maxXAxis - minXAxis) / 20, 1));
		hSlider.setShowTickMarks(true);
		hSlider.setShowTickLabels(true);

		final Label caption = new Label("Select Time Range");
		caption.setStyle("-fx-font-weight: bold");
		// caption.setAl
		final TextField minValue = new TextField(Double.toString(hSlider.getMin()));
		minValue.setPrefWidth(120);
		final TextField maxValue = new TextField(Double.toString(hSlider.getMax()));
		maxValue.setPrefWidth(120);

		// ref:
		// https://stackoverflow.com/questions/21450328/how-to-bind-two-different-javafx-properties-string-and-double-with-stringconve
		StringConverter<Number> converter = new NumberStringConverter();
		Bindings.bindBidirectional(minValue.textProperty(), hSlider.lowValueProperty(), converter);
		Bindings.bindBidirectional(maxValue.textProperty(), hSlider.highValueProperty(), converter);

		// axis.setAutoRanging(false);

		// Works but slow response.
		// // Curtain A1 start
		// Bindings.bindBidirectional(hSlider.lowValueProperty(), axis.lowerBoundProperty());
		// Bindings.bindBidirectional(hSlider.highValueProperty(), axis.upperBoundProperty());
		// // Curtain A1 end

		Button updateButton = new Button("Apply");
		// JFXButton updateButton = new JFXButton("Apply");
		// updateButton.getStyleClass().add("button-raised");

		updateButton.setOnAction(e ->
			{
				axis.setAutoRanging(false);
				axis.setLowerBound(hSlider.getLowValue());
				axis.setUpperBound(hSlider.getHighValue());
			});

		// minValue.textProperty().bindBidirectional(hSlider.lowValueProperty(), new DoubleStringConverter());

		// hSlider.lowValueProperty().addListener(new ChangeListener<Number>()
		// {
		// public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val)
		// {
		// // cappuccino.setOpacity(new_val.doubleValue());
		// minValue.setText(String.format("%.2f", new_val));
		// axis.setLowerBound((double) new_val);
		// }
		// });
		// hSlider.highValueProperty().addListener(new ChangeListener<Number>()
		// {
		// public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val)
		// {
		// // cappuccino.setOpacity(new_val.doubleValue());
		// maxValue.setText(String.format("%.2f", new_val));
		// axis.setUpperBound((double) new_val);
		// }
		// });

		// HBox bottom = new HBox(statusLbl);
		BorderPane.setMargin(hSlider, new Insets(0, 20, 0, 20));
		BorderPane.setMargin(minValue, new Insets(0, 2, 0, 10));
		BorderPane.setMargin(maxValue, new Insets(0, 10, 0, 2));
		BorderPane.setAlignment(updateButton, Pos.CENTER);
		BorderPane.setAlignment(caption, Pos.CENTER);
		// BorderPane.setMargin(caption, new Insets(0, 00, 0, 500));

		// pane.setPadding(new Insets(12, 12, 12, 12));
		pane.setCenter(hSlider);
		pane.setLeft(minValue);
		pane.setRight(maxValue);
		pane.setTop(caption);
		pane.setBottom(updateButton);

		return pane;
	}

	public void setXAxis(long maxX, long minX)
	{
		xAxis.setAutoRanging(false);
		xAxis.setUpperBound(maxX);
		xAxis.setLowerBound(minX);
		xAxis.setTickUnit((maxX - minX) / 50);
		// xAxis.setTickUnit((maxXAxis - minXAxis) / 800);
		xAxis.setMinorTickVisible(true);
		xAxis.setMinorTickCount(10);
		xAxis.setMinorTickLength(20);
	}

	/**
	 * One series for each users
	 * 
	 * @param dataReceived
	 * @param timelineChart
	 * @return
	 */
	private Triple<XYChart<Number, String>, Long, Long> createTimelineContent(List<List<List<String>>> dataReceived,
			XYChart<Number, String> timelineChart)
	{
		System.out.println("createTimelineContent() called");
		Triple<ObservableList<Series<Number, String>>, Long, Long> res = dataToSeries(dataReceived);

		ObservableList<XYChart.Series<Number, String>> seriesForAllUsers = res.getFirst();
		// this.maxXAxis = res.getSecond();
		// this.minXAxis = res.getThird();
		ObservableList<XYChart.Series<Number, String>> data = timelineChart.getData();
		// timelineChart = new TimelineChart2(xAxis, yAxis);
		if (data == null)
		{
			data = FXCollections.observableArrayList(seriesForAllUsers);
			timelineChart.setData(data);
		}
		else
		{
			timelineChart.getData().addAll(seriesForAllUsers);
		}

		timelineChart.setLegendVisible(false);
		System.out.println("dataReceived.size()=" + dataReceived.size());
		System.out.println("seriesForAllUsers.getData().size()=" + seriesForAllUsers.size());
		System.out.println("chart.getData().size()=" + timelineChart.getData().size());
		System.out.println("Inside chart: xAxis.getLowerBound()=" + xAxis.getLowerBound());
		System.out.println("Inside chart: xAxis.getUpperBound()=" + xAxis.getUpperBound());
		// System.out.println("Inside chart: xAxis.getUpperBound()=" + chart.updateAxisRange());

		// // save memory and clear reference
		// dataReceived.clear();
		// dataReceived = null;

		return new Triple<XYChart<Number, String>, Long, Long>(timelineChart, res.getSecond(), res.getThird());
	}

	/**
	 * 
	 * @param maxXAxis
	 * @param minXAxis
	 */
	private void formatXAxis(long maxXAxis, long minXAxis)
	{
		// String pattern = "dd MMMM yyyy, HH:mm:ss";
		// DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		// StringConverter<LocalDateTime> converter = new LocalDateTimeStringConverter(formatter, null);
		// // assertEquals("12 January 1985, 12:34:56", converter.toString(VALID_LDT_WITH_SECONDS));
		// LocalDateTime date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
		if (false)// for date formatted axis
		{
			StringConverter converter2 = new EpochStringConverter();
			xAxis.setTickLabelFormatter(converter2);
		} // (new NumberAxis.DefaultFormatter(xAxis, "$", "*"));
			// (new NumberAxis.DefaultFormatter(yAxis, "$ ", null));
			// chart.setStyle("");
			// setup chart

		// xAxis.setStyle(value);
		xAxis.setAutoRanging(false);
		xAxis.setUpperBound(maxXAxis);
		xAxis.setLowerBound(minXAxis);
		xAxis.setTickUnit((maxXAxis - minXAxis) / 20);
		// xAxis.setTickUnit((maxXAxis - minXAxis) / 800);
		xAxis.setMinorTickVisible(false);
	}

	/**
	 * 
	 * @param dataReceived
	 * @return Triple{seriesForAllUsers, maxXAxis, minXAxis}
	 */
	private static final Triple<ObservableList<XYChart.Series<Number, String>>, Long, Long> dataToSeries(
			List<List<List<String>>> dataReceived)
	{
		long maxXAxis = 0, minXAxis = Long.MAX_VALUE;
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
																													// ts

				// start timeestamp, username, {end timestamp, actname, }
				seriesForAUser.getData().add(new XYChart.Data<Number, String>(startTS, userID, extras));

				long xValST = (long) (startTS);
				long xValET = (long) (endTS);

				if (maxXAxis < xValET)
				{
					maxXAxis = xValET;
				}

				if (minXAxis > xValST)
				{
					minXAxis = xValST;
				}
			}
			seriesForAllUsers.add(seriesForAUser);
		}

		// upper bound is maxXAxis rounded to ceiling multiple of 10
		maxXAxis = (long) (Math.ceil(maxXAxis / 100d) * 100);
		// upper bound is maxXAxis rounded to ceiling multiple of 10
		minXAxis = (long) (Math.floor(minXAxis / 100d) * 100);
		return new Triple<ObservableList<XYChart.Series<Number, String>>, Long, Long>(seriesForAllUsers, maxXAxis,
				minXAxis);
	}

}
