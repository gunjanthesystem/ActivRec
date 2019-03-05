package org.activity.plotting;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Series;

public class LinePlotEntry
{

	private String titleLabel;
	/**
	 * {For this time, a list of actEntries, where each actEntry is a long of Strings
	 */
	private ObservableList<Series<Double, Double>> values;

	public LinePlotEntry(String userID)
	{
		this.titleLabel = userID;
		values = FXCollections.emptyObservableList();// new ArrayList<>();
	}

	public String getUserID()
	{
		return titleLabel;
	}

	/**
	 * Stores the values shown in the chart.
	 * 
	 * @return
	 */
	public ObservableList<Series<Double, Double>> getValues()
	{
		return values;
	}

	public void setValues(ObservableList<Series<Double, Double>> values)
	{
		this.values = values;
	}
}