package org.activity.ui;

import java.util.ArrayList;
import java.util.List;

import org.activity.objects.Pair;
import org.activity.plotting.ChartUtils;
import org.activity.plotting0.FXUtils;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * Fork of DashBoard2
 * 
 * @author gunjan
 *
 */
public class ChartBoard3 extends Application
{
	ChartBoardScene cb;

	/**
	 * 
	 * @param listOfNodesToAddToTabPaneWithTitles
	 */
	public ChartBoard3(ChartBoardScene cb)
	{
		System.out.println("ChartBoard3(cb) called");
		this.cb = cb;
		System.out.println("ChartBoard3(cb) exited");
	}

	public ChartBoard3()
	{
		System.out.println("ChartBoard3(): Why this called");
	}

	public static void main(String[] args)
	{
		List<Pair<Node, String>> listOfChatNodesToAdd = new ArrayList<>();
		listOfChatNodesToAdd.add(new Pair<>(
				ChartUtils.createLineChart(FXUtils.toObservableListOfSeriesOfPairData(FXUtils.getSyntheticData(50, 50)),
						"OLaLineChartTitle", "ajooba"),
				"LineChart"));
		listOfChatNodesToAdd.add(new Pair<>(ChartUtils.createScatterChart(
				FXUtils.toObservableListOfSeriesOfPairData(FXUtils.getSyntheticData(50, 50)), "ScatterChartTitle",
				"ajooba"), "ScatterChart"));

		new ChartBoard3(new ChartBoardScene(listOfChatNodesToAdd));
		Application.launch(args);

	}

	public void init()
	{
		System.out.println("init() called");
	}

	public void start(Stage stage)
	{
		System.out.println("start() called");
		ScreenDetails.printScreensDetails();

		stage.setScene(this.cb.getScene());
		stage.setTitle("ChartBoard");
		// stage.initStyle(stageStyle);
		stage.show();
		// stage.setFullScreen(true);
	}

}
