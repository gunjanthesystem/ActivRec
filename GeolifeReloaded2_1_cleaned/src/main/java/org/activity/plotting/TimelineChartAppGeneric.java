package org.activity.plotting;

import java.sql.Date;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.activity.constants.DomainConstants;
import org.activity.objects.ActivityObject;
import org.activity.objects.Timeline;
import org.activity.objects.Triple;
import org.activity.ui.EpochStringConverter;
import org.activity.ui.PopUps;
import org.controlsfx.control.RangeSlider;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
//import javafx.geometry.Insets;
//import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
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
	private XYChart<Number, String> timelineChart;
	private NumberAxis xAxis;
	private CategoryAxis yAxis;
	long maxXAxis, minXAxis;

	/**
	 * Set inside createXAxisRangeSlider()
	 */
	RangeSlider hSlider;

	// variables for storing initial position of pressed mouse before dragging
	private double mousePressedInitX;
	private double mousePressedInitY;
	private Point2D dragAnchor;

	public VBox getVBox()
	{
		return vbox;
	}

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

		Triple<ObservableList<Series<Number, String>>, Long, Long> seriesForAllUsersRes = actDataToSeries(dataReceived);
		Triple<XYChart<Number, String>, Long, Long> res = createTimelineContent(seriesForAllUsersRes, timelineChart);
		XYChart<Number, String> timelineChart = res.getFirst();
		this.maxXAxis = res.getSecond();
		this.minXAxis = res.getThird();

		formatXAxis(maxXAxis, minXAxis, 50);
		// this.setXAxis(maxXAxis, minXAxis);

		vbox.getChildren().add(timelineChart);
		vbox.getChildren().add(createXAxisRangeSlider((NumberAxis) timelineChart.getXAxis(), maxXAxis, minXAxis));
		VBox.setVgrow(timelineChart, Priority.ALWAYS);
		// return vbox;
		this.getChildren().add(vbox);

		bindScrollToRangeSliderV2(hSlider, timelineChart, 25000, xAxis);
	}

	/**
	 * 
	 * @param userDayTimelines
	 * @param hasXAxisRangeSlider
	 * @param typeOfTimelineChart
	 * @since May 20 2018
	 */
	public TimelineChartAppGeneric(LinkedHashMap<String, LinkedHashMap<Date, Timeline>> userDayTimelines,
			boolean hasXAxisRangeSlider, String typeOfTimelineChart)
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
		// Triple{seriesForAllUsers, maxXAxis, minXAxis}//dataToSeries(dataReceived);
		Triple<ObservableList<Series<Number, String>>, Long, Long> seriesForAllUsers = userDayTimelinesToSeries20May(
				userDayTimelines);
		Triple<XYChart<Number, String>, Long, Long> res = createTimelineContent(seriesForAllUsers/* dataReceived */,
				timelineChart);
		XYChart<Number, String> timelineChart = res.getFirst();
		this.maxXAxis = res.getSecond();
		this.minXAxis = res.getThird();

		formatXAxis(maxXAxis, minXAxis, 50);

		vbox.getChildren().add(timelineChart);
		vbox.getChildren().add(createXAxisRangeSlider((NumberAxis) timelineChart.getXAxis(), maxXAxis, minXAxis));
		VBox.setVgrow(timelineChart, Priority.ALWAYS);
		// return vbox;
		this.getChildren().add(vbox);

		bindScrollToRangeSliderV2(hSlider, timelineChart, 25000, xAxis);
	}

	// public void handle(InputEvent event)
	// {
	// System.out.println("Handling event " + event.getEventType());
	// event.consume();
	// }

	/**
	 * Listen to ScrollEvent(s) in timelineChart and translate hSlider thumbs accordingly
	 * 
	 * @param hSlider
	 * @param timelineChart
	 * @param scrollDeltaSteps
	 *            controls the rate of translation of slider, higher means higher sensitiveness of slider to scrolling
	 * @param xAxis
	 */
	private void bindScrollToRangeSlider(RangeSlider hSlider, XYChart<Number, String> timelineChart,
			double scrollDeltaSteps, NumberAxis xAxis)
	{

		double sliderMax = hSlider.getMax();
		double sliderMin = hSlider.getMin();
		double translateMultiplier = (sliderMax - sliderMin) / scrollDeltaSteps;
		double zoomMultiplier = scrollDeltaSteps * 500;

		EventHandler<ScrollEvent> eventHandler = new EventHandler<ScrollEvent>()
			{
				@Override
				public void handle(ScrollEvent scrollEvent)
				{
					double sliderHigh = hSlider.getHighValue();
					double sliderLow = hSlider.getLowValue();
					double translateX, translateY;

					// double diffOfHighLowToBeKeptConstant = hSlide

					translateX = scrollEvent.getDeltaX();
					translateY = scrollEvent.getDeltaY();

					System.out.println("Scrolled, deltaX: " + translateX + ", deltaY: " + translateY);

					if (Math.abs(translateX) > 0)
					{
						// System.out.println("Scrolled, deltaX: " + event.getDeltaX() + ", deltaY: " + translateY
						// + ", multiplier: " + multiplier);
						double addendum = translateMultiplier * translateX;
						// hSlider.lowValueProperty().bind(hSlider.getLowValue() + timelineChart.getOnScroll().);
						double sliderNewHigh = sliderHigh + addendum;
						double sliderNewLow = sliderLow + addendum;

						// For translating the slider in response to horizontal scroll
						// restrict slider's max-min width changing.
						if (sliderNewHigh <= sliderMax && sliderNewLow >= sliderMin)
						{
							hSlider.setLowValue(sliderNewLow);
							hSlider.setHighValue(sliderNewHigh);
						}
					}

					// For zoomin the slider in response to vertical scroll
					if (Math.abs(translateY) > 0)
					{
						// System.out.println("event.x=" + scrollEvent.getX());
						System.out.println(String.format("(%.2f)", xAxis.getValueForDisplay(scrollEvent.getX())));
						// yAxis.getValueForDisplay(scrollEvent.getY())));

						double newCentreXPoint = (double) xAxis.getValueForDisplay(scrollEvent.getX());
						double highMinusLowHalf = (sliderHigh - sliderLow) / 2;
						double addendumHalf = (zoomMultiplier * translateY) / 2;

						// for zooming in according to the current range in slider
						double sliderNewHigh = sliderHigh + addendumHalf;
						double sliderNewLow = sliderLow - addendumHalf;

						// for centering under the x axis point under mouse
						// double sliderNewHigh = newCentreXPoint + highMinusLowHalf + addendumHalf;
						// double sliderNewLow = newCentreXPoint - highMinusLowHalf - addendumHalf;

						// System.out.println("sliderHigh= " + sliderHigh + " sliderLow= " + sliderLow);
						// System.out.println("sliderNewHigh= " + sliderNewHigh + " sliderNewLow= " + sliderNewLow);
						// System.out.println("newCentreXPoint= " + newCentreXPoint + " highMinusLowHalf= "
						// + highMinusLowHalf + " addendumHalf=" + addendumHalf);

						if (sliderNewHigh <= sliderMax)
						{
							hSlider.setHighValue(sliderNewHigh);
						}
						else
						{
							hSlider.setHighValue(sliderMax);
						}
						if (sliderNewLow >= sliderMin)
						{
							hSlider.setLowValue(sliderNewLow);
						}
						else
						{
							hSlider.setLowValue(sliderMin);
						}

					}
				}
			};
		timelineChart.addEventHandler(ScrollEvent.SCROLL, eventHandler);

		/////////////////////////////////////////
		// timelineChart.setOnScroll((
		// ScrollEvent event)->{
		// double translateX = event.getDeltaX();
		// double scrollDeltaSteps = 10;
		// double multiplier = (hSlider.getMax() - hSlider.getMin()) / scrollDeltaSteps;
		// hSlider.lowValueProperty().bind(hSlider.getLowValue() + timelineChart.getOnScroll().);
		// hSlider.setLowValue(hSlider.getLowValue() + (multiplier * translateX));
		// hSlider.setHighValue(hSlider.getHighValue() + (multiplier * translateX));
	}

	/**
	 * Listen to ScrollEvent(s) and DragEvents in timelineChart and translate/zoom hSlider thumbs (which are bound to
	 * x-axis) accordingly.
	 * 
	 * @param hSlider
	 * @param timelineChart
	 * @param scrollDeltaSteps
	 *            controls the rate of translation of slider, higher means higher sensitiveness of slider to scrolling
	 * @param xAxis
	 */
	private void bindScrollToRangeSliderV2(RangeSlider hSlider, XYChart<Number, String> timelineChart,
			double scrollDeltaSteps, NumberAxis xAxis)
	{
		double sliderMax = hSlider.getMax();
		double sliderMin = hSlider.getMin();
		double translateMultiplier = (sliderMax - sliderMin) / scrollDeltaSteps;
		double zoomMultiplier = scrollDeltaSteps * 500;

		EventHandler<ScrollEvent> scrollEventHandler = new EventHandler<ScrollEvent>()
			{
				@Override
				public void handle(ScrollEvent scrollEvent)
				{
					double translateX = scrollEvent.getDeltaX();
					double translateY = scrollEvent.getDeltaY();
					// $$System.out.println("Scrolled, deltaX: " + translateX + ", deltaY: " + translateY);

					if (Math.abs(translateX) > 0)
					{
						translateSlider(hSlider, translateMultiplier, translateX, sliderMax, sliderMin);
					}

					// For zoomin the slider in response to vertical scroll
					if (Math.abs(translateY) > 0)
					{
						zoomSlider(hSlider, xAxis, sliderMax, sliderMin, zoomMultiplier, scrollEvent, translateY);
					}
				}

			};

		EventHandler<MouseEvent> dragEventHandler = new EventHandler<MouseEvent>()
			{
				@Override
				public void handle(MouseEvent me)
				{

					double dragX = me.getSceneX() - dragAnchor.getX();
					double dragY = me.getSceneY() - dragAnchor.getY();
					// calculate new position of the circle
					double newXPosition = mousePressedInitX + dragX;

					// $$ System.out.println("dragEventHandler active: getX(): " + me.getX() + ", getY(): " + me.getY()
					// $$ + " dragX= " + dragX + " dragY= " + dragY);

					if (Math.abs(dragX) > 0)
					{
						translateSlider(hSlider, translateMultiplier, -dragX, sliderMax, sliderMin);
					}

					// For zoomin the slider in response to vertical scroll
					// if (Math.abs(dragY) > 0)
					// {
					// zoomSlider(hSlider, xAxis, sliderMax, sliderMin, zoomMultiplier, me, dragY);
					// }

					// double newYPosition = mousePressedInitY + dragY;
					// if new position do not exceeds borders of the rectangle, translate to this position
					// if ((newXPosition >= circle.getRadius()) && (newXPosition <= RECT_WIDTH - circle.getRadius()))
					// {
					// circle.setTranslateX(newXPosition);
					// }
				}

			};

		// EventHandler<MouseEvent> mousePressedEventHandler = new EventHandler<MouseEvent>()
		// {
		// @Override
		// public void handle(MouseEvent mousePressedEvent)
		// {
		// // System.out.println("dragEventHandler active: .getX(): " + scrollEvent.getX()
		// // + ", scrollEvent.getY(): " + scrollEvent.getY());
		// }};
		timelineChart.setOnMousePressed((MouseEvent me) ->
			{
				// when mouse is pressed, store initial position
				mousePressedInitX = timelineChart.getTranslateX();
				mousePressedInitY = timelineChart.getTranslateY();

				dragAnchor = new Point2D(me.getSceneX(), me.getSceneY());
				// System.out.println("mousePressedInitX= " + mousePressedInitX + " mousePressedInitY= "
				// + mousePressedInitY + " dragAnchor= " + dragAnchor);
			});

		timelineChart.setOnScroll(scrollEventHandler);
		// timelineChart.setOnMouseDragged(dragEventHandler);
		timelineChart.setOnMouseDragged(dragEventHandler);

		// circle.setOnMouseDragged((MouseEvent me) ->
		// {
		// double dragX = me.getSceneX() - dragAnchor.getX();
		// double dragY = me.getSceneY() - dragAnchor.getY();
		// // calculate new position of the circle
		// double newXPosition = initX + dragX;
		// double newYPosition = initY + dragY;
		// // if new position do not exceeds borders of the rectangle, translate to this position
		// if ((newXPosition >= circle.getRadius()) && (newXPosition <= RECT_WIDTH - circle.getRadius()))
		// {
		// circle.setTranslateX(newXPosition);
		// }
		// if ((newYPosition >= circle.getRadius()) && (newYPosition <= RECT_HEIGHT - circle.getRadius()))
		// {
		// circle.setTranslateY(newYPosition);
		// }
		// showOnConsole(name + " was dragged (x:" + dragX + ", y:" + dragY + ")");
		// });
		// timelineChart.setOnDrage
		// timelineChart.addEventHandler(ScrollEvent.SCROLL, scrollEventHandler);
		// timelineChart.addEventHandler(DragEvent.d, scrollEventHandler);

		/////////////////////////////////////////
		// timelineChart.setOnScroll((
		// ScrollEvent event)->{
		// double translateX = event.getDeltaX();
		// double scrollDeltaSteps = 10;
		// double multiplier = (hSlider.getMax() - hSlider.getMin()) / scrollDeltaSteps;
		// hSlider.lowValueProperty().bind(hSlider.getLowValue() + timelineChart.getOnScroll().);
		// hSlider.setLowValue(hSlider.getLowValue() + (multiplier * translateX));
		// hSlider.setHighValue(hSlider.getHighValue() + (multiplier * translateX));
	}

	/**
	 * 
	 * @param hSlider
	 * @param xAxis
	 * @param sliderMax
	 * @param sliderMin
	 * @param zoomMultiplier
	 * @param scrollEvent
	 * @param translateY
	 */
	private void zoomSlider(RangeSlider hSlider, NumberAxis xAxis, double sliderMax, double sliderMin,
			double zoomMultiplier, ScrollEvent scrollEvent, double translateY)
	{
		double sliderHigh = hSlider.getHighValue();
		double sliderLow = hSlider.getLowValue();

		// System.out.println("event.x=" + scrollEvent.getX());
		// System.out.println(String.format("(%.2f)", xAxis.getValueForDisplay(scrollEvent.getX())));
		// yAxis.getValueForDisplay(scrollEvent.getY())));

		double newCentreXPoint = (double) xAxis.getValueForDisplay(scrollEvent.getX());
		double highMinusLowHalf = (sliderHigh - sliderLow) / 2;
		double addendumHalf = (zoomMultiplier * translateY) / 2;

		// for zooming in according to the current range in slider
		double sliderNewHigh = sliderHigh + addendumHalf;
		double sliderNewLow = sliderLow - addendumHalf;

		// for centering under the x axis point under mouse
		// double sliderNewHigh = newCentreXPoint + highMinusLowHalf + addendumHalf;
		// double sliderNewLow = newCentreXPoint - highMinusLowHalf - addendumHalf;

		// System.out.println("sliderHigh= " + sliderHigh + " sliderLow= " + sliderLow);
		// System.out.println("sliderNewHigh= " + sliderNewHigh + " sliderNewLow= " + sliderNewLow);
		// System.out.println("newCentreXPoint= " + newCentreXPoint + " highMinusLowHalf= "
		// + highMinusLowHalf + " addendumHalf=" + addendumHalf);

		if (sliderNewHigh <= sliderMax)
		{
			hSlider.setHighValue(sliderNewHigh);
		}
		else
		{
			hSlider.setHighValue(sliderMax);
		}
		if (sliderNewLow >= sliderMin)
		{
			hSlider.setLowValue(sliderNewLow);
		}
		else
		{
			hSlider.setLowValue(sliderMin);
		}
	}

	private void translateSlider(RangeSlider hSlider, double translateMultiplier, double translateX, double sliderMax,
			double sliderMin)
	{
		double sliderHigh = hSlider.getHighValue();
		double sliderLow = hSlider.getLowValue();

		double addendum = translateMultiplier * translateX;

		double sliderNewHigh = sliderHigh + addendum;
		double sliderNewLow = sliderLow + addendum;

		// For translating the slider in response to horizontal scroll
		// restrict slider's max-min width changing.
		if (sliderNewHigh <= sliderMax && sliderNewLow >= sliderMin)
		{
			hSlider.setLowValue(sliderNewLow);
			hSlider.setHighValue(sliderNewHigh);
		}
	}

	/**
	 * Sets hSlider
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
		hSlider = new RangeSlider(minXAxis, maxXAxis, minXAxis, maxXAxis);
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
		StringConverter converter2 = new EpochStringConverter();
		hSlider.setLabelFormatter(converter2);

		final Label caption = new Label("Select Time Range");
		caption.setStyle("-fx-font-weight: bold");
		// caption.setAl

		// final TextField minValue = new TextField(Double.toString(hSlider.getMin()));
		final JFXTextField minValue = new JFXTextField(Double.toString(hSlider.getMin()));
		minValue.setLabelFloat(true);
		minValue.setPromptText("Min time");
		minValue.setPrefWidth(120);

		final JFXTextField maxValue = new JFXTextField(Double.toString(hSlider.getMax()));
		maxValue.setLabelFloat(true);
		maxValue.setPromptText("Max time");
		maxValue.setPrefWidth(120);

		// ref:
		// https://stackoverflow.com/questions/21450328/how-to-bind-two-different-javafx-properties-string-and-double-with-stringconve
		// Bind values in text field to range slider
		StringConverter<Number> converter = new NumberStringConverter();
		Bindings.bindBidirectional(minValue.textProperty(), hSlider.lowValueProperty(), converter);
		Bindings.bindBidirectional(maxValue.textProperty(), hSlider.highValueProperty(), converter);

		// axis.setAutoRanging(false);

		// Works but slow response.
		// // Curtain A1 start
		// Bind high/low values in range slider to axis bound
		Bindings.bindBidirectional(hSlider.lowValueProperty(), axis.lowerBoundProperty());
		Bindings.bindBidirectional(hSlider.highValueProperty(), axis.upperBoundProperty());
		// // Curtain A1 end

		// Button updateButton = new Button("Apply");
		JFXButton updateButton = new JFXButton("Apply");
		// updateButton.getStyleClass().add("button-raised");
		// updateButton.setStyle("-fx-font-size: 18pt;");

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

	/**
	 * 
	 * @param maxX
	 * @param minX
	 */
	public void setXAxis(long maxX, long minX)
	{
		int numberOfTicks = 100;
		xAxis.setAutoRanging(false);
		xAxis.setUpperBound(maxX);
		xAxis.setLowerBound(minX);

		xAxis.setTickUnit((maxX - minX) / numberOfTicks);
		// xAxis.setTickUnit((maxXAxis - minXAxis) / 800);
		xAxis.setMinorTickVisible(true);
		xAxis.setMinorTickCount(10);
		// xAxis.setMinorTickLength(20);
	}

	/**
	 * One series for each users
	 * 
	 * 
	 * @param seriesForAllUsersRes
	 * @param timelineChart
	 * @return {maxXAxis,minXAxis}
	 */
	private Triple<XYChart<Number, String>, Long, Long> createTimelineContent(
			Triple<ObservableList<Series<Number, String>>, Long, Long> seriesForAllUsersRes,
			XYChart<Number, String> timelineChart)// List<List<List<String>>> dataReceived
	{
		System.out.println("createTimelineContent() called");
		// Triple<ObservableList<Series<Number, String>>, Long, Long> dataReceived;// = dataToSeries(dataReceived);

		ObservableList<XYChart.Series<Number, String>> seriesForAllUsers = seriesForAllUsersRes.getFirst();
		// this.maxXAxis = res.getSecond();
		// this.minXAxis = res.getThird();
		long maxXAxis = seriesForAllUsersRes.getSecond();
		long minXAxis = seriesForAllUsersRes.getThird();

		ObservableList<XYChart.Series<Number, String>> data = timelineChart.getData();
		// Reverse List to User0 is at top (further from 0,0)
		Collections.reverse(seriesForAllUsers);
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

		System.out.println("seriesForAllUsers.size()=" + seriesForAllUsers.size());
		System.out.println("seriesForAllUsers.getData().size()=" + seriesForAllUsers.size());
		System.out.println("chart.getData().size()=" + timelineChart.getData().size());
		System.out.println("Inside chart: xAxis.getLowerBound()=" + xAxis.getLowerBound());
		System.out.println("Inside chart: xAxis.getUpperBound()=" + xAxis.getUpperBound());

		// System.out.println("Inside chart: xAxis.getUpperBound()=" + chart.updateAxisRange());

		// // save memory and clear reference
		// dataReceived.clear();
		// dataReceived = null;

		return new Triple<XYChart<Number, String>, Long, Long>(timelineChart, maxXAxis, minXAxis);
	}

	/**
	 * 
	 * @param maxXAxis
	 * @param minXAxis
	 */
	private void formatXAxis(long maxXAxis, long minXAxis, int numberOfTicks)
	{
		// int numberOfTicks = 100;
		// String pattern = "dd MMMM yyyy, HH:mm:ss";
		// DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		// StringConverter<LocalDateTime> converter = new LocalDateTimeStringConverter(formatter, null);
		// // assertEquals("12 January 1985, 12:34:56", converter.toString(VALID_LDT_WITH_SECONDS));
		// LocalDateTime date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
		if (true)// for date formatted axis
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
		xAxis.setTickUnit((maxXAxis - minXAxis) / numberOfTicks);
		// xAxis.setTickUnit((maxXAxis - minXAxis) / 800);
		xAxis.setMinorTickVisible(true);
		// xAxis.setMinorTickLength(20);
	}

	/**
	 * 
	 * @param dataReceived
	 * @return Triple{seriesForAllUsers, maxXAxis, minXAxis}
	 */
	private static final Triple<ObservableList<XYChart.Series<Number, String>>, Long, Long> actDataToSeries(
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

	/**
	 * 
	 * @param dataReceived
	 * @return Triple{seriesForAllUsers, maxXAxis, minXAxis}
	 * @since 20 May 2018
	 */
	private static final Triple<ObservableList<XYChart.Series<Number, String>>, Long, Long> userDayTimelinesToSeries20May(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> userDayTimelines)
	{
		long maxXAxis = 0, minXAxis = Long.MAX_VALUE;

		ObservableList<XYChart.Series<Number, String>> seriesForAllUsers = FXCollections.observableArrayList();
		TreeMap<Integer, String> catIdNameDictioary = DomainConstants.catIDNameDictionary;

		// (List<List<String>>eachUserData: dataReceived)
		for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : userDayTimelines.entrySet())
		{
			XYChart.Series<Number, String> seriesForAUser = new XYChart.Series<Number, String>();
			String userID = userEntry.getKey();

			// for (List<String> d : eachUserEntry)
			for (Entry<Date, Timeline> timelineEntry : userEntry.getValue().entrySet())
			{
				String date = timelineEntry.getKey().toString();
				Timeline t = timelineEntry.getValue();
				for (ActivityObject ao : t.getActivityObjectsInTimeline())
				{
					// String userID = d.get(0);
					long startTS = ao.getStartTimestampInms();// Double.valueOf(d.get(1));
					long endTS = ao.getEndTimestampInms();// Double.valueOf(d.get(2));
					String locName = ao.getLocationIDs(',');// d.get(3);
					Integer actID = ao.getActivityID();// Integer.valueOf(d.get(5));
					String actName = catIdNameDictioary.getOrDefault(actID, "ID:" + actID);
					String startLat = ao.getStartLatitude();
					// String endLat = ao.getEndLatitude();

					// end timestamp
					final ActivityBoxExtraValues extras = new ActivityBoxExtraValues(endTS, actName, actID, locName);

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
			} // end of loop over day timelines for this user
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
