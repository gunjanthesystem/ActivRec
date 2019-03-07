package org.activity.plotting;

import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.BorderPane;

public class CanvasCellG2 extends ListCell<TimelineEntry>
{

	private Label userLabel;
	private ResizeableCanvasG1 canvas;

	public CanvasCellG2()
	{
		/*
		 * Important, otherwise we will keep seeing a horizontal scrollbar.
		 */
		setStyle("-fx-padding: 10px;");

		userLabel = new Label();
		userLabel.setStyle("-fx-padding: 10px; -fx-font-size: 1em; -fx-font-weight: bold;");
		// StackPane.setAlignment(userLabel, Pos.CENTER_LEFT);

		/*
		 * Create a resizable canvas and bind its width and height to the width and height of the table cell.
		 */
		canvas = new ResizeableCanvasG1();
		canvas.widthProperty().bind(widthProperty());
		canvas.heightProperty().bind(heightProperty());

		BorderPane pane = new BorderPane();
		// double borderWidth = 15;// 0.20 * canvas.getHeight();// perhaps we can also bind to canvas height.
		// System.out.println("borderWidth= " + borderWidth);
		// pane.setB
		pane.setStyle("-fx-border-color : seashell;-fx-border-width : 10 10 10 10");// " + borderWidth + " 0 " +
																					// borderWidth + " 0 ");//
		pane.setLeft(userLabel);
		pane.setCenter(canvas);
		BorderPane.setAlignment(userLabel, Pos.CENTER);
		// pane.getChildren().addAll(userLabel, canvas);

		setGraphic(pane);
		setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
	}

	@Override
	protected void updateItem(TimelineEntry entry, boolean empty)
	{
		// TODO THIS METHOD NEED TO BE CONFIRMED FOR CORRECTNESS and might be responsible for rendering issues.
		// super.updateItem(entry, empty);
		// System.out.println("entry userID = " + entry.getUserID());
		if (empty || entry == null)
		{
			if (entry == null)
			{
				System.out.println("entry is null");
			}
			else
			{
				System.out.println("empty = " + empty);
			}
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
