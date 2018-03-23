// package org.activity.ui.temp;
//
// import java.util.Collections;
// import java.util.List;
//
// import javafx.collections.FXCollections;
// import javafx.collections.ObservableList;
// import javafx.geometry.Pos;
// import javafx.scene.control.ContentDisplay;
// import javafx.scene.control.Label;
// import javafx.scene.control.ListCell;
// import javafx.scene.layout.StackPane;
//
// public class TimelineCell extends ListCell<TimelineEntry>
// {
//
// private Label timelineLabel;
// private ObservableList<ResizableCanvas> canvases;
//
// public TimelineCell(List<Integer> actIDs)
// {
// /*
// * Important, otherwise we will keep seeing a horizontal scrollbar.
// */
// setStyle("-fx-padding: 0px;");
//
// timelineLabel = new Label();
// timelineLabel.setStyle("-fx-padding: 10px; -fx-font-size: 1.2em; -fx-font-weight: bold;");
// StackPane.setAlignment(timelineLabel, Pos.TOP_LEFT);
//
// /*
// * Create a resizable canvas and bind its width and height to the width and height of the table cell.
// */
// canvases = FXCollections.observableArrayList();
//
// for (int actID : actIDs)
// {
// ResizableCanvas canvas = new ResizableCanvas();
// canvas.widthProperty().bind(widthProperty());
// canvas.heightProperty().bind(heightProperty());
// }
//
// StackPane pane = new StackPane();
// pane.getChildren().addAll(timelineLabel, canvases);
//
// setGraphic(pane);
// setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
// }
//
// @Override
// protected void updateItem(TimelineEntry entry, boolean empty)
// {
// if (empty || entry == null)
// {
// timelineLabel.setText("");
// canvas.setData(Collections.emptyList());
// canvas.draw();
// }
// else
// {
// timelineLabel.setText(Integer.toString(entry.getUserID()));
// canvas.setData(entry.getValues());
// canvas.draw();
// }
// }
// }
