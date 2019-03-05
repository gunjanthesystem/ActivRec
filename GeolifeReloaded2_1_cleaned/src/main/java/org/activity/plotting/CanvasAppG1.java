package org.activity.plotting;

import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class CanvasAppG1 extends Application
{

	@Override
	public void start(Stage stage) throws Exception
	{

		long t0 = System.currentTimeMillis();
		/*
		 * Create some random data for my life span.
		 */
		ObservableList<TimelineEntry> observableData = FXCollections.observableArrayList();

		List<List<List<String>>> dataReceived = DataGenerator.getData3(50, 500, 12, 5, 864000, 60 * 20, 10800);

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
		listView.setFixedCellSize(200);
		// listView.setOrientation(Orientation.HORIZONTAL);

		Scene scene = new Scene(listView);

		stage.setTitle("Canvas Cell");
		stage.setScene(scene);
		stage.setWidth(600);
		stage.setHeight(600);
		stage.show();
		long tn = System.currentTimeMillis();
		System.out.println("time taken =" + (tn - t0) + " ms");
	}

	public static void main(String[] args)
	{
		launch(args);
	}
}
