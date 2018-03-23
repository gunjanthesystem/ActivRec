package org.activity.ui.temp;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class CanvasApp extends Application
{

	@Override
	public void start(Stage stage) throws Exception
	{

		long t0 = System.currentTimeMillis();
		/*
		 * Create some random data for my life span.
		 */
		ObservableList<YearEntry> data = FXCollections.observableArrayList();

		// for (int year = 1969; year <= 2015; year++)
		for (int year = 1; year <= 1000000; year++)
		{
			YearEntry entry = new YearEntry(year);
			for (int day = 0; day < 5; day++)
			{
				entry.getValues().add(Math.random() * 100);
			}
			data.add(entry);
		}

		ListView<YearEntry> listView = new ListView<>(data);
		listView.setCellFactory(param -> new CanvasCell());
		listView.setFixedCellSize(200);
		listView.setOrientation(Orientation.HORIZONTAL);

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
