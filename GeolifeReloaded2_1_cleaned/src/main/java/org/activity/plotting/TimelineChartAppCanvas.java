package org.activity.plotting;

import java.util.List;

import org.controlsfx.control.RangeSlider;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
public class TimelineChartAppCanvas extends Pane
{

	/// ActName, Val1, Val2, Val3,
	// static List<Pair<String, List<Double>>> allData;
	private VBox vbox;

	public VBox getVbox()
	{
		return vbox;
	}

	long maxXAxis, minXAxis;

	public TimelineChartAppCanvas(List<List<List<String>>> dataReceived, boolean hasXAxisRangeSlider)
	{
		vbox = new VBox();

		ObservableList<TimelineEntry> observableData = FXCollections.observableArrayList();
		// Fill up the obervable data by creating TimelineEntries from the dataReceived
		for (List<List<String>> eachUserData : dataReceived)
		{
			// XYChart.Series<Number, String> seriesForAUser = new XYChart.Series<Number, String>();
			TimelineEntry entry = new TimelineEntry(eachUserData.get(0).get(0));
			entry.setValues(eachUserData);
			observableData.add(entry);
		}

		ListView<TimelineEntry> listView = new ListView<>(observableData);
		listView.setCellFactory(param -> new CanvasCellG2());

		double cellSize = Math.max(Math.min(this.getHeight() / dataReceived.size(), 200), 80);
		listView.setFixedCellSize(cellSize);
		System.out.println("cellSize =" + cellSize);
		vbox.getChildren().addAll(listView);
		// listView.setOrientation(Orientation.HORIZONTAL);
		// formatXAxis(maxXAxis, minXAxis);

		// vbox.getChildren().add(timelineChart);
		// vbox.getChildren().add(createXAxisRangeSlider((NumberAxis) timelineChart.getXAxis(), maxXAxis, minXAxis));
		// VBox.setVgrow(timelineChart, Priority.ALWAYS);
		// return vbox;
		VBox.setVgrow(listView, Priority.ALWAYS);
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

}
