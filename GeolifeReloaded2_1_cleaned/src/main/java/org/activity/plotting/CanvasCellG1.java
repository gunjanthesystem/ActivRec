package org.activity.plotting;

import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.StackPane;

public class CanvasCellG1 extends ListCell<TimelineEntry>
{

	private Label userLabel;
	private ResizableCanvasG1 canvas;

	public CanvasCellG1()
	{
		/*
		 * Important, otherwise we will keep seeing a horizontal scrollbar.
		 */
		setStyle("-fx-padding: 10px;");

		userLabel = new Label();
		userLabel.setStyle("-fx-padding: 10px; -fx-font-size: 2.5em; -fx-font-weight: bold;");
		StackPane.setAlignment(userLabel, Pos.CENTER_LEFT);

		/*
		 * Create a resizable canvas and bind its width and height to the width and height of the table cell.
		 */
		canvas = new ResizableCanvasG1();
		canvas.widthProperty().bind(widthProperty());
		canvas.heightProperty().bind(heightProperty());

		StackPane pane = new StackPane();
		pane.getChildren().addAll(userLabel, canvas);

		setGraphic(pane);
		setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
	}

	@Override
	protected void updateItem(TimelineEntry entry, boolean empty)
	{
		if (empty || entry == null)
		{
			// userLabel.setText("");
			// canvas.setData(Collections.emptyList());
			// canvas.draw();
			// setText(null);
			// setGraphic(null);
		}
		else
		{
			userLabel.setText(entry.getUserID());
			canvas.setData(entry.getValues());
			canvas.draw();
		}
	}
}
