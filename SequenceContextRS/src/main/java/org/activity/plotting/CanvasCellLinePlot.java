package org.activity.plotting;

import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;

/**
 * MIGHT NOT BE COMPLETED
 * 
 * @author gunjan
 *
 */
public class CanvasCellLinePlot extends ListCell<LinePlotEntry>
{
	// ObservableList<Series<Double, Double>> data
	private Label titleLabel;
	private ResizeableCanvasForLinePlot canvas;

	public CanvasCellLinePlot()
	{
		/*
		 * Important, otherwise we will keep seeing a horizontal scrollbar.
		 */
		setStyle("-fx-padding: 10px;");

		titleLabel = new Label();
		titleLabel.setStyle("-fx-padding: 10px; -fx-font-size: 1em; -fx-font-weight: bold;");
		// StackPane.setAlignment(userLabel, Pos.CENTER_LEFT);

		/*
		 * Create a resizable canvas and bind its width and height to the width and height of the table cell.
		 */
		canvas = new ResizeableCanvasForLinePlot();
		canvas.widthProperty().bind(widthProperty());
		canvas.heightProperty().bind(heightProperty());

		BorderPane pane = new BorderPane();
		// double borderWidth = 15;// 0.20 * canvas.getHeight();// perhaps we can also bind to canvas height.
		// System.out.println("borderWidth= " + borderWidth);
		// pane.setB
		pane.setStyle("-fx-border-color : seashell;-fx-border-width : 10 10 10 10");// " + borderWidth + " 0 " +
																					// borderWidth + " 0 ");//
		pane.setLeft(titleLabel);
		pane.setCenter(canvas);
		BorderPane.setAlignment(titleLabel, Pos.CENTER);
		// pane.getChildren().addAll(userLabel, canvas);

		setGraphic(pane);
		setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
	}

	@Override
	protected void updateItem(LinePlotEntry entry, boolean empty)
	{
		if (empty || entry == null)
		{
			// titleLabel.setText("");
			// canvas.setData(null);// Collections.emptyList());
			// canvas.draw();
			setText(null);
			setGraphic(null);
		}
		else
		{
			titleLabel.setText(entry.getUserID());
			canvas.setData(entry.getValues());
			canvas.draw();
		}
	}
}
