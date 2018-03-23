package org.activity.ui.temp;

import java.util.Collections;

import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.StackPane;

public class CanvasCell extends ListCell<YearEntry>
{

	private Label yearLabel;
	private ResizableCanvas canvas;

	public CanvasCell()
	{
		/*
		 * Important, otherwise we will keep seeing a horizontal scrollbar.
		 */
		setStyle("-fx-padding: 0px;");

		yearLabel = new Label();
		yearLabel.setStyle("-fx-padding: 10px; -fx-font-size: 1.2em; -fx-font-weight: bold;");
		StackPane.setAlignment(yearLabel, Pos.TOP_LEFT);

		/*
		 * Create a resizable canvas and bind its width and height to the width and height of the table cell.
		 */
		canvas = new ResizableCanvas();
		canvas.widthProperty().bind(widthProperty());
		canvas.heightProperty().bind(heightProperty());

		StackPane pane = new StackPane();
		pane.getChildren().addAll(yearLabel, canvas);

		setGraphic(pane);
		setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
	}

	@Override
	protected void updateItem(YearEntry entry, boolean empty)
	{
		if (empty || entry == null)
		{
			yearLabel.setText("");
			canvas.setData(Collections.emptyList());
			canvas.draw();
		}
		else
		{
			yearLabel.setText(Integer.toString(entry.getYear()));
			canvas.setData(entry.getValues());
			canvas.draw();
		}
	}
}
