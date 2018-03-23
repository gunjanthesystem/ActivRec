// package org.activity.ui.temp;
//
// import javafx.application.Application;
// import javafx.collections.FXCollections;
// import javafx.collections.ObservableList;
// import javafx.scene.Scene;
// import javafx.scene.control.ListView;
// import javafx.stage.Stage;
//
// public class CanvasApp2 extends Application
// {
//
// @Override
// public void start(Stage stage) throws Exception
// {
//
// long t0 = System.currentTimeMillis();
// /*
// * Create some random data for my life span.
// */
// ObservableList<TimelineEntry> data = FXCollections.observableArrayList();
//
// // for (int year = 1969; year <= 2015; year++)
// for (int userID = 1; userID <= 1000; userID++)
// {
// TimelineEntry entry = new TimelineEntry(userID);
// for (int actIndex = 0; actIndex < 365; actIndex++)
// {
// entry.getValues().add(Math.random() * 100);
// }
// data.add(entry);
// }
//
// ListView<TimelineEntry> listView = new ListView<>(data);
// listView.setCellFactory(param -> new TimelineCell());
// listView.setFixedCellSize(200);
//
// Scene scene = new Scene(listView);
//
// stage.setTitle("Canvas Cell");
// stage.setScene(scene);
// stage.setWidth(600);
// stage.setHeight(600);
// stage.show();
// long tn = System.currentTimeMillis();
// System.out.println("time taken =" + (tn - t0) + " ms");
// }
//
// public static void main(String[] args)
// {
// launch(args);
// }
// }
