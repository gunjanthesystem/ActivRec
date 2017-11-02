package org.activity.plotting;

import java.util.List;

import org.activity.objects.Pair;
import org.activity.ui.EpochStringConverter;
import org.controlsfx.control.RangeSlider;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
//import javafx.geometry.Insets;
//import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
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
public class TimelineChartApp2 extends Pane
{

	/// ActName, Val1, Val2, Val3,
	static List<Pair<String, List<Double>>> allData;
	private VBox vbox = new VBox();
	private TimelineChart2 chart;
	private NumberAxis xAxis;
	private CategoryAxis yAxis;
	long maxXAxis, minXAxis;

	/** List of properties in the sample that can be played with */
	// public PlaygroundProperty[] playgroundProperties;

	public Pane createContent(List<List<String>> dataReceived, boolean hasXAxisRangeSlider)
	{
		vbox = new VBox();

		XYChart<Number, String> timelineChart = createTimelineContent(dataReceived);
		vbox.getChildren().add(timelineChart);
		vbox.getChildren().add(createXAxisRangeSlider((NumberAxis) timelineChart.getXAxis()));

		VBox.setVgrow(timelineChart, Priority.ALWAYS);

		return vbox;
	}

	public BorderPane createXAxisRangeSlider(NumberAxis axis)
	{
		BorderPane pane = new BorderPane();
		// axis.getUpperBound();

		// System.out.println("(axis.getLowerBound()=" + axis.getLowerBound());
		// System.out.println("(axis.getUpperBound()=" + axis.getUpperBound());
		final RangeSlider hSlider = new RangeSlider(this.minXAxis, this.maxXAxis, this.minXAxis, this.maxXAxis);
		// axis.getLowerBound(), axis.getUpperBound(), axis.getLowerBound(),
		// axis.getUpperBound());/

		hSlider.setSnapToTicks(true);

		// hSlider.setMajorTickUnit((int) ((axis.getUpperBound() - axis.getLowerBound()) / 100));
		// hSlider.setMinorTickUnit((int) ((axis.getUpperBound() - axis.getLowerBound()) / 100));
		// hSlider.setBlockIncrement(200);// (int) ((axis.getUpperBound() - axis.getLowerBound()) / 100));

		hSlider.setBlockIncrement((this.maxXAxis - this.minXAxis) / 200);
		hSlider.setMajorTickUnit((this.maxXAxis - this.minXAxis) / 20);
		hSlider.setShowTickMarks(true);
		hSlider.setShowTickLabels(true);

		final Label caption = new Label("Select Time Range");
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
		// BorderPane.setMargin(caption, new Insets(0, 00, 0, 500));

		// pane.setPadding(new Insets(12, 12, 12, 12));
		pane.setCenter(hSlider);
		pane.setLeft(minValue);
		pane.setRight(maxValue);
		pane.setTop(caption);
		pane.setBottom(updateButton);

		return pane;
	}

	public XYChart<Number, String> createTimelineContent(List<List<String>> dataReceived)
	{
		System.out.println("createContent() called");

		xAxis = new NumberAxis();
		long millis = 100;

		// String pattern = "dd MMMM yyyy, HH:mm:ss";
		// DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		// StringConverter<LocalDateTime> converter = new LocalDateTimeStringConverter(formatter, null);
		// // assertEquals("12 January 1985, 12:34:56", converter.toString(VALID_LDT_WITH_SECONDS));
		// LocalDateTime date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
		StringConverter converter2 = new EpochStringConverter();

		xAxis.setTickLabelFormatter(converter2);
		// (new NumberAxis.DefaultFormatter(xAxis, "$", "*"));
		// (new NumberAxis.DefaultFormatter(yAxis, "$ ", null));

		// xAxis.setMinorTickCount(0);
		yAxis = new CategoryAxis();
		chart = new TimelineChart2(xAxis, yAxis);

		// chart.setStyle("");
		// setup chart
		xAxis.setLabel("Timestamp");
		yAxis.setLabel("Timelines");

		// xAxis.setStyle(value);
		// add starting data

		XYChart.Series<Number, String> series = new XYChart.Series<Number, String>();
		// List<List<String>> dataReceived = DataGenerator.getData2();

		long maxXAxis = 0, minXAxis = Long.MAX_VALUE;

		for (List<String> d : dataReceived)
		{
			// end timestamp
			final ActivityBoxExtraValues extras = new ActivityBoxExtraValues(Double.valueOf(d.get(2)), (d.get(4)),
					Integer.valueOf(d.get(2)), Double.valueOf(d.get(2)));// end ts

			// start timeestamp, username, {end timestamp, actname, }
			series.getData().add(new XYChart.Data<Number, String>(Double.valueOf(d.get(1)), d.get(0), extras));

			long xVal = Integer.valueOf(d.get(1));
			if (maxXAxis < xVal)
			{
				maxXAxis = xVal;
			}

			if (minXAxis > xVal)
			{
				minXAxis = xVal;
			}
		}

		this.maxXAxis = (long) (Math.ceil(maxXAxis / 100d) * 100);// upper bound is maxXAxis rounded to ceiling multiple
																	// of
																	// 10
		this.minXAxis = (long) (Math.floor(minXAxis / 100d) * 100);// upper bound is maxXAxis rounded to ceiling
																	// multiple of
																	// 10

		//
		ObservableList<XYChart.Series<Number, String>> data = chart.getData();

		if (data == null)
		{
			data = FXCollections.observableArrayList(series);
			chart.setData(data);
		}
		else
		{
			chart.getData().add(series);
		}

		System.out.println("Inside chart: xAxis.getLowerBound()=" + xAxis.getLowerBound());
		System.out.println("Inside chart: xAxis.getUpperBound()=" + xAxis.getUpperBound());
		// System.out.println("Inside chart: xAxis.getUpperBound()=" + chart.updateAxisRange());

		xAxis.setAutoRanging(false);
		xAxis.setUpperBound(this.maxXAxis);
		xAxis.setLowerBound(this.minXAxis);
		xAxis.setTickUnit((this.maxXAxis - this.minXAxis) / 20);
		// xAxis.setTickUnit((maxXAxis - minXAxis) / 800);
		xAxis.setMinorTickVisible(false);

		return chart;
	}

	// @Override
	// public void start(Stage primaryStage) throws Exception
	// {
	//
	// // primaryStage.setScene(new Scene(createContent()));
	// Scene scene = new Scene(createContent(DataGenerator.getData2()));// , 270, 370);
	//
	// // VBox vBoxSidePane = new VBox();
	// // // borderPane.setTop(hBoxMenus);
	// // // borderPane.setLeft(vBoxSidePane);
	// // vBoxSidePane.getChildren().add(createContent(DataGenerator.getData2()));
	// // // .setCenter(createContent(DataGenerator.getData2()));
	// // Group rootGroup = new Group(vBoxSidePane);
	// //
	// // // Scene scene = new Scene(rootGroup);// , 270, 370);
	//
	// scene.setFill(Color.TRANSPARENT);
	// scene.getStylesheets().add("gsheetNative.css");
	// // scene.getStylesheets().add(getClass().getResource("gsheet1.css").toExternalForm());
	// primaryStage.setScene(scene);
	// primaryStage.setTitle("DashboardA");
	// // stage.initStyle(stageStyle);
	//
	// primaryStage.show();
	// }

	// public void createTimeline

}
