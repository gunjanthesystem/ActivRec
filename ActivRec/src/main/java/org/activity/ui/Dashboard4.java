package org.activity.ui;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.activity.constants.Constant;
import org.activity.constants.PathConstants;
import org.activity.controller.ControllerWithoutServer;
import org.activity.io.ReadingFromFile;
import org.activity.io.Serializer;
import org.activity.objects.ActivityObject2018;
import org.activity.objects.Pair;
import org.activity.objects.Timeline;
import org.activity.objects.Triple;
import org.activity.plotting.DataGenerator;
import org.activity.plotting.ResizeableCanvasForLinePlot;
import org.activity.plotting.TimelineChartAppCanvas;
import org.activity.plotting.TimelineChartAppGeneric;
import org.activity.plotting0.FXUtils;
import org.activity.ui.colors.ColorPalette;
import org.activity.util.TimelineTransformers;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Fork of Dashboard3
 * 
 * @since 15 Feb 2019
 * @author gunjan
 *
 */
public class Dashboard4 extends Application
{
	public static Map<Integer, Integer> actIDIndexMap;
	Stage stage;
	ReusableElements reuse;

	// String pathToSerialisedDCUTimelines =
	// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/dcu_data_2_written/";
	// String pathToSerialisedGeolfieTimelines =
	// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/geolife1_written/";
	// String pathToSerialisedGowallaTimelines =
	// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/gowalla1_written/";
	// String pathToToyTimelines = ;
	// "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/MAY21ED0.35STimeLocPopDistPrevDurPrevAllActsFDStFilter0hrs75RTV/ToyTimelines21May.kryo";
	// MenuBar menuBar;

	// private final TableView treeView = new TableView();
	// private final TextArea msgLogFld = new TextArea();

	public static void main(String[] args)
	{
		// System.setProperty("prism.allowhidpi", "true");
		Application.launch(args);
	}

	public void start(Stage stage)
	{
		long t0 = System.currentTimeMillis();
		String databaseName = "gowalla1";

		ScreenDetails.printScreensDetails();
		reuse = new ReusableElements();

		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayToyTimelines = null;
		LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines = null;

		////////////////////////////////////////////////////////
		if (true)
		{
			if (databaseName.equals("gowalla1"))
			{
				UIConstants.haveTooltip = false;
			}
			else
			{
				UIConstants.haveTooltip = true;
			}
			Constant.setDatabaseName(databaseName);
			PathConstants.initialise(Constant.For9kUsers, Constant.getDatabaseName());
			Constant.initialise("./", databaseName, PathConstants.pathToSerialisedCatIDsHierDist,
					PathConstants.pathToSerialisedCatIDNameDictionary, PathConstants.pathToSerialisedLocationObjects,
					PathConstants.pathToSerialisedUserObjects, PathConstants.pathToSerialisedGowallaLocZoneIdMap,
					false);

			usersCleanedDayTimelines = PathConstants.deserializeAndGetCleanedTimelinesFeb2019(databaseName);
			// (LinkedHashMap<String, LinkedHashMap<Date, Timeline>>) Serializer
			// .kryoDeSerializeThis(pathToSerialisedGeolfieTimelines + "usersCleanedDayTimelines.kryo");

			/////////////////
			ControllerWithoutServer.setDataVarietyConstants(usersCleanedDayTimelines, true, "UsersCleanedDTs_", true,
					false, databaseName);
			// writeActIDNamesInFixedOrder(Constant.getCommonPath() + "CatIDNameMap.csv");
			List<Integer> uniqueActIDs = new ArrayList<>(Constant.getUniqueActivityIDs());
			ColorPalette.setColors("Brewer", uniqueActIDs.size());// Insight //Paired//InsightSecondary
			actIDIndexMap = IntStream.range(0, uniqueActIDs.size()).boxed()
					.collect(Collectors.toMap(i -> uniqueActIDs.get(i), Function.identity()));
			System.out.println("actIDIndexMap=\n" + actIDIndexMap);
			/////////////////
		}
		////////////////////////////////////////////////////////
		// disabled on 24 July 2018, as i getting deserialisation error, perhaps because ActivityObject class
		// has changes this serialised toy timelines being read were created. remedy to do later, create toy
		// timelines again.
		if (false)
		{
			usersCleanedDayToyTimelines = (LinkedHashMap<String, LinkedHashMap<Date, Timeline>>) Serializer
					.kryoDeSerializeThis(PathConstants.pathToToyTimelines12AUG);
			ControllerWithoutServer.setDataVarietyConstants(usersCleanedDayToyTimelines, true, "ToyTs_", true, true,
					Constant.getDatabaseName());

			List<Integer> uniqueActIDs = new ArrayList<>(Constant.getUniqueActivityIDs());
			ColorPalette.setColors("Paired", uniqueActIDs.size());
			actIDIndexMap = IntStream.range(0, uniqueActIDs.size()).boxed()
					.collect(Collectors.toMap(i -> uniqueActIDs.get(i), Function.identity()));
			System.out.println("actIDIndexMap=\n" + actIDIndexMap);

		}

		// final Stage stageRef = stage;
		// StageStyle stageStyle = StageStyle.DECORATED;
		/////////////////////////////////////////////

		// Button toBackButton = new Button("toBack()");
		// Button closeButton = new Button("close()");
		// closeButton.setOnAction(e -> stageRef.close());
		ScrollPane sidePane = new ScrollPane();
		VBox vBoxSidePane = new VBox();// toBackButton, closeButton);// contentBox.setLayoutX(30); //
										// contentBox.setLayoutY(20);
		vBoxSidePane.setSpacing(20);
		vBoxSidePane.getChildren().add(TreeViewUtil.getTreeView());
		/////////////////////////////////////////////

		HBox hBoxMenus = new HBox(generateMenuBar());

		TabPane tabPane = createTabs(true, usersCleanedDayTimelines, databaseName);// usersCleanedDayToyTimelines);
		// usersCleanedDayTimelines.clear();// To save memory
		// tabPane.setPrefHeight(getHeight());

		// VBox mainPane = new VBox();
		//
		// mainPane.getChildren().add(hBoxMenus);
		// mainPane.getChildren().add(tabPane);
		VBox.setVgrow(tabPane, Priority.ALWAYS);
		VBox.setVgrow(vBoxSidePane, Priority.ALWAYS);

		// Curtain A start
		BorderPane borderPane = new BorderPane();
		borderPane.setTop(hBoxMenus);
		// borderPane.setLeft(vBoxSidePane);
		borderPane.setCenter(tabPane);
		// borderPane.setBottom(generateMenuBar());
		// Curtain A end

		// Group rootGroup = new Group(tabPane);// mainPane);// ', contentBox);

		// Scene scene = new Scene(tabPane);// rootGroup);// , 270, 370);

		Scene scene = new Scene(borderPane);// createTabs());// createContent(DataGenerator.getData2()));// , 270, 370);

		scene.setFill(Color.TRANSPARENT);

		// disabled on May 21,
		// ##scene.getStylesheets().add("./jfxtras/styles/jmetro8/GJMetroLightTheme.css");// gsheetNative.css");
		// $$scene.getStylesheets().add("./org/activity/ui/resources/css/gsheetNative.css");
		// System.out.println("Working Directory = " + System.getProperty("user.dir"));
		// System.out.println("Dashboard3.class=" + Dashboard3.class);
		if (true)
		{
			final ObservableList<String> stylesheets = scene.getStylesheets();
			stylesheets.addAll(// "./org/activity/ui/resources/css/jfoenix-main-demo.css",
					// "./org/activity/ui/resources/css/gsheetNative.css",
					// $"./org/activity/ui/resources/css/jfoenix-design.css", // jfoenix-design.css",
					// $"./org/activity/ui/resources/css/jfoenix-components.css",
					"./org/activity/ui/resources/css/gsheetNative01.css");
		}
		// scene.getStylesheets().add("./org/activity/ui/resources/css/gsheetNative01.css");
		// URL cssURL = Dashboard3.class.getResource("/css/gsheetNative.css");// .toExternalForm();
		// System.out.println("cssURL=" + cssURL);

		stage.setScene(scene);
		stage.setTitle("Dashboard");
		stage.setWidth(600);
		stage.setMinHeight(200);
		stage.setMinWidth(200);
		stage.setHeight(600);
		// stage.initStyle(stageStyle);
		stage.show();
		long tn = System.currentTimeMillis();
		System.out.println("tn-t1=" + (tn - t0) + " ms");
		// stage.setFullScreen(true);
		//
		// Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		// stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
		// stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 4);

	}

	/////////////

	/**
	 * 
	 * @param usersCleanedDayTimelines
	 * @param useSyntheticData
	 * @param databaseName
	 * @return
	 */
	private TabPane createTabs(boolean useSyntheticData,
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayTimelines, String databaseName)
	{
		TabPane tabPane = new TabPane();
		List<Tab> tabsToAdd = new ArrayList<>();
		// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> toyTimelines = toOnlySeqOfActIDs(
		// usersCleanedDayToyTimelines);
		System.out.println("Entered createTabs()");

		final boolean doGivenDataCircleTimelines = false;
		final boolean doGivenDataLineTimelines = false;
		// final boolean doGivenDataCanvasTimelines = true;//not implemented
		final boolean doGivenDataOnlyActIDSeq = false;
		final boolean doGivenDataLinePlotFeatures = false;
		final boolean doGivenDataMapPlot = true;

		final boolean doSyntheticDataCircleTimelines = false;// true;
		final boolean doSyntheticDataCanvasTimelines = false;
		final boolean doSyntheticDataBoxTimelines = false;
		final boolean doSyntheticDataLineTimelines = false;

		try
		{
			if (doGivenDataCircleTimelines)
			{
				long tTimelineReal0 = System.currentTimeMillis();
				Tab timelineTabCircleReal = new Tab(databaseName + ": All users Timelines");// (Toy-Circle)
				TimelineChartAppGeneric tcCReal = new TimelineChartAppGeneric(usersCleanedDayTimelines, true,
						"ActivityCircle");
				timelineTabCircleReal.setContent(tcCReal.getVBox());// timelinesVBox2);
				timelineTabCircleReal.setClosable(true);
				tabsToAdd.add(timelineTabCircleReal);
				long tTimelineRealn = System.currentTimeMillis();
				System.out.println(
						"Time taken TimelineChartAppGeneric real = " + (tTimelineRealn - tTimelineReal0) + " ms");
			}
			if (doGivenDataLineTimelines)
			{
				Tab timelineTabE = new Tab("timelineTabE Historical Timelines All Users");
				TimelineChartAppGeneric tcE = new TimelineChartAppGeneric(usersCleanedDayTimelines, true, "LineChart");
				timelineTabE.setContent(tcE.getVBox());// timelinesVBox2);
				timelineTabE.setClosable(true);
				tabsToAdd.add(timelineTabE);

				Tab timelineTabScattter = new Tab("timelineTabScattter Historical Timelines All Users");
				TimelineChartAppGeneric tcScattter = new TimelineChartAppGeneric(usersCleanedDayTimelines, true,
						"ScatterChart");
				timelineTabScattter.setContent(tcScattter.getVBox());// timelinesVBox2);
				timelineTabScattter.setClosable(true);
				tabsToAdd.add(timelineTabScattter);
			}

			if (doGivenDataOnlyActIDSeq)
			{
				Tab onlyActIDsAsRects = new Tab("Only ActIDs Sequence");
				onlyActIDsAsRects.setContent(createOnlyActIDsAsRects(usersCleanedDayTimelines));
				onlyActIDsAsRects.setClosable(true);
				tabsToAdd.add(onlyActIDsAsRects);
			}

			if (doGivenDataOnlyActIDSeq)
			{
				Tab onlyActIDsAsRects = new Tab("Only ActIDs Sequence2");
				onlyActIDsAsRects.setContent(createOnlyActIDsAsRectsV2(usersCleanedDayTimelines));
				onlyActIDsAsRects.setClosable(true);
				tabsToAdd.add(onlyActIDsAsRects);
			}

			if (doGivenDataOnlyActIDSeq)
			{
				Tab onlyActIDsAsRects = new Tab("Only ActIDs Sequence3");
				onlyActIDsAsRects.setContent(createOnlyActIDsAsRectsV3(usersCleanedDayTimelines));
				onlyActIDsAsRects.setClosable(true);
				tabsToAdd.add(onlyActIDsAsRects);
			}

			if (doGivenDataLinePlotFeatures)
			{
				// String dirToWrite = "./dataWritten/Temp" + DateTimeUtils.getMonthDateLabel() + databaseName + "/";
				// WToFile.createDirectoryIfNotExists(dirToWrite);
				// Constant.setCommonPath(dirToWrite);
				//
				// if (databaseName.equals("gowalla1"))
				// {
				// TimelineStats.transformAndWriteAsTimeseriesGowalla(usersCleanedDayTimelines);//
				// }
				// else if (databaseName.equals("geolife1"))
				// {
				// TimelineStats.transformAndWriteAsTimeseries(usersCleanedDayTimelines);//
				// }
				// else
				// {
				// PopUps.printTracedErrorMsgWithExit("Unknown database: " + databaseName);
				// }
				ScrollPane multiSeriesPlots = createMultiSeriesChartsFromAllAOs(databaseName);
				Tab timelineTabMultiSeries = new Tab("multiSeriesPlots");
				timelineTabMultiSeries.setContent(multiSeriesPlots);
				timelineTabMultiSeries.setClosable(true);
				tabsToAdd.add(timelineTabMultiSeries);
			}
			/////////////////////////////////////////////////////////////////
			// List<List<List<String>>> timelineData = DataGenerator.getData3(10, 1000, 12, 5, 200, 10, 50);
			List<List<List<String>>> timelineData = DataGenerator.getData3(50, 5000, 12, 5, 864000, 60 * 20, 10800);
			System.out.println("timelineData.size() = " + timelineData.size());
			if (doSyntheticDataCircleTimelines)
			{
				long tTimeline0 = System.currentTimeMillis();
				Tab timelineTabCircle = new Tab("(Synth-Circle) Historical Timelines All Users");
				TimelineChartAppGeneric tcC = new TimelineChartAppGeneric(timelineData, true, "ActivityCircle");
				timelineTabCircle.setContent(tcC.getVBox());// timelinesVBox2);
				timelineTabCircle.setClosable(true);
				tabsToAdd.add(timelineTabCircle);
				long tTimelinen = System.currentTimeMillis();
				System.out.println("Time taken TimelineChartAppGeneric = " + (tTimelinen - tTimeline0) + " ms");

			}

			if (doSyntheticDataCanvasTimelines)
			{
				long tTimelineCanvas0 = System.currentTimeMillis();
				Tab timelineTabCanvas = new Tab("(Synth-Canvas) Historical Timelines All Users");
				TimelineChartAppCanvas tcCanvas = new TimelineChartAppCanvas(timelineData, false);
				timelineTabCanvas.setContent(tcCanvas.getVbox());// timelinesVBox2);
				timelineTabCanvas.setClosable(true);
				tabsToAdd.add(timelineTabCanvas);
				long tTimelineCanvasn = System.currentTimeMillis();
				System.out.println(
						"Time taken TimelineChartAppCanvas = " + (tTimelineCanvasn - tTimelineCanvas0) + " ms");
			}

			if (doSyntheticDataBoxTimelines)
			{
				Tab timelineTabD = new Tab("(Synth-Box) Historical Timelines All Users");
				TimelineChartAppGeneric tcD = new TimelineChartAppGeneric(
						/* usersCleanedDayToyTimelines */ timelineData, true, "ActivityBox");
				// TODO: Issue: not scaling correctly with range change.
				timelineTabD.setContent(tcD.getVBox());// timelinesVBox2);
				timelineTabD.setClosable(true);
				tabsToAdd.add(timelineTabD);
			}
			if (doSyntheticDataLineTimelines)
			{
				Tab timelineTabE = new Tab("timelineTabE Historical Timelines All Users");
				TimelineChartAppGeneric tcE = new TimelineChartAppGeneric(timelineData, true, "LineChart");
				timelineTabE.setContent(tcE.getVBox());// timelinesVBox2);
				timelineTabE.setClosable(true);
				tabsToAdd.add(timelineTabE);

				Tab timelineTabScattter = new Tab("timelineTabScattter Historical Timelines All Users");
				TimelineChartAppGeneric tcScattter = new TimelineChartAppGeneric(timelineData, true, "ScatterChart");
				timelineTabScattter.setContent(tcScattter.getVBox());// timelinesVBox2);
				timelineTabScattter.setClosable(true);
				tabsToAdd.add(timelineTabScattter);
			}
			/////////////////////////////////////////////////////////////////

			///
			// long tTimeline0 = System.currentTimeMillis();
			// Tab timelineTab = new Tab("Historical Timelines All Users");
			// // VBox timelinesVBox2 = new VBox();
			// TimelineChartApp2 tcA = new TimelineChartApp2();// DataGenerator.getData2(), true);
			// // timelineTab.setContent(tcA2.createContent(DataGenerator.getData2(), true));//
			// // .createContent(DataGenerator.getData2()));
			// // timelineTab.setContent(tcA2.createContent(DataGenerator.getData2ForAUser(1, 10, 5, 5, 100), true));//
			// // .createContent(DataGenerator.getData2()));
			// // timelineTab.setContent(tcA2_2.createContent(DataGenerator.getData2ForAUser(2, 10, 5, 5, 100),true));//
			// // .createContent(DataGenerator.getData2()));
			// // timelinesVBox2.getChildren().add(tcA.createContent(DataGenerator.getData2(10, 20, 5, 5, 200, 2, 10),
			// // true));// .createContent(DataGenerator.getData2()));
			// Pane p = (tcA.createContentV2(DataGenerator.getData3(10, 20, 12, 5, 200, 2, 10), true));//
			// .createContent(DataGenerator.getData2()));
			// // TODO: Issue: not scaling correctly with range change.
			// timelineTab.setContent(p);// timelinesVBox2);
			// timelineTab.setClosable(false);
			// tabsToAdd.add(timelineTab);
			// long tTimelinen = System.currentTimeMillis();
			// System.out.println("Timeto create timelines chart = " + (tTimelinen - tTimeline0) + " ms");
			///

			///
			// long tTimeline1_0 = System.currentTimeMillis();
			// Tab timelineTabB = new Tab("Historical Timelines All Users");
			// // VBox timelinesVBox2 = new VBox();
			// TimelineChartAppCircle tcB = new TimelineChartAppCircle();// DataGenerator.getData2(), true);
			// // TODO: Issue: circle positions
			// // timelineTab.setContent(tcA2.createContent(DataGenerator.getData2(), true));//
			// // .createContent(DataGenerator.getData2()));
			// // timelineTab.setContent(tcA2.createContent(DataGenerator.getData2ForAUser(1, 10, 5, 5, 100), true));//
			// // .createContent(DataGenerator.getData2()));
			// // timelineTab.setContent(tcA2_2.createContent(DataGenerator.getData2ForAUser(2, 10, 5, 5, 100),
			// true));//
			// // .createContent(DataGenerator.getData2()));
			// // timelinesVBox2.getChildren().add(tcA.createContent(DataGenerator.getData2(10, 20, 5, 5, 200, 2, 10),
			// // true));// .createContent(DataGenerator.getData2()));
			// Pane pB = (tcB.createContentV2(DataGenerator.getData3(1, 1, 12, 5, 200, 2, 10), true));//
			// .createContent(DataGenerator.getData2()));
			//
			// timelineTabB.setContent(pB);// timelinesVBox2);
			// timelineTabB.setClosable(false);
			// tabsToAdd.add(timelineTabB);
			// long tTimeline1_n = System.currentTimeMillis();
			// System.out.println("Timeto create timelines chart = " + (tTimeline1_n - tTimeline1_0) + " ms");
			///

			/*
			 * Tab timelineTabOneForEachUser = new Tab("Historical Timelines One For Each User"); VBox timelinesVBox =
			 * new VBox(); TimelineChartApp2 tcA2 = new TimelineChartApp2();// DataGenerator.getData2(), true); //
			 * timelineTab.setContent(tcA2.createContent(DataGenerator.getData2(), true));// //
			 * .createContent(DataGenerator.getData2())); TimelineChartApp2 tcA2_2 = new TimelineChartApp2();//
			 * DataGenerator.getData2(), true);//TEMP //
			 * timelineTab.setContent(tcA2.createContent(DataGenerator.getData2ForAUser(1, 10, 5, 5, 100), true));// //
			 * .createContent(DataGenerator.getData2())); //
			 * timelineTab.setContent(tcA2_2.createContent(DataGenerator.getData2ForAUser(2, 10, 5, 5, 100), true));//
			 * // .createContent(DataGenerator.getData2()));
			 * timelinesVBox.getChildren().add(tcA2.createContent(DataGenerator.getData2ForAUser(1, 10, 5, 5, 100),
			 * true));// .createContent(DataGenerator.getData2()));
			 * timelinesVBox.getChildren().add(tcA2_2.createContent(DataGenerator.getData2ForAUser(2, 10, 5, 5, 100),
			 * true));// .createContent(DataGenerator.getData2())); timelineTabOneForEachUser.setContent(timelinesVBox);
			 * timelineTabOneForEachUser.setClosable(false); tabsToAdd.add(timelineTabOneForEachUser);
			 */
			// long tLine0 = System.currentTimeMillis();
			// Tab lineChartTab = new Tab("lineChart");
			// lineChartTab.setContent(ChartUtils.createLineChart(
			// FXUtils.toObservableListOfSeriesOfPairData(FXUtils.getSyntheticData(10, 500)), "title", "ajooba"));
			// lineChartTab.setClosable(true);
			// tabsToAdd.add(lineChartTab);
			// long tLineN = System.currentTimeMillis();
			// System.out.println("Time to create lines chart = " + (tLineN - tLine0) + " ms");

			// long tLine1_0 = System.currentTimeMillis();
			// // Tab lineChartTabB = new Tab("lineChart");
			// // lineChartTabB.setContent(
			// // ChartUtils.createLineChart2(DataGenerator.getData3(10, 50, 12, 5, 200, 2, 10), "title", "ajooba"));
			// // lineChartTabB.setClosable(true);
			// // tabsToAdd.add(lineChartTabB);
			// Tab timelineTabC = new Tab("Historical Timelines All Users");
			// TimelineChartAppLineChart tcC = new TimelineChartAppLineChart();// DataGenerator.getData2(), true);
			// Pane pC = (tcC.createContentV2(DataGenerator.getData3(10, 50, 12, 5, 200, 2, 10), true));//
			// .createContent(DataGenerator.getData2()));
			// timelineTabC.setContent(pC);// timelinesVBox2);
			// timelineTabC.setClosable(false);
			// tabsToAdd.add(timelineTabC);
			//
			// long tLine1_N = System.currentTimeMillis();
			// System.out.println("Time to create lines chart = " + (tLine1_N - tLine1_0) + " ms");
			//
			// long tScatter0 = System.currentTimeMillis();
			// Tab scatterChartTab = new Tab("scatterChart");
			// scatterChartTab.setContent(ChartUtils.createScatterChart(
			// FXUtils.toObservableListOfSeriesOfPairData(FXUtils.getSyntheticData(10, 5000)), "title", "ajooba"));
			// scatterChartTab.setClosable(true);
			// tabsToAdd.add(scatterChartTab);
			// long tScatterN = System.currentTimeMillis();
			// System.out.println("Time to create scatter chart = " + (tScatterN - tScatter0) + " ms");

			// Tab chooseSourcesTab = new Tab("Timelines");
			// // chooseSourcesTab.setContent(createChooseSourcesTable());// createChooseSources());
			// chooseSourcesTab.setClosable(true);

			/***********************************************/
			/* Disabled temporarily on Mar 15 2018 */
			// long ttGmap1 = System.currentTimeMillis();
			// Tab mapTab = new Tab("Google Map: Locations with No TZ");
			// GoogleMapApp mapPane = new GoogleMapApp();
			//
			// // String absFileNameForLatLonToReadAsMarker =
			// // "/run/media/gunjan/BackupVault/GOWALLA/GowallaDataWorks/Mar15/locIDsWithNoTimezone.csv";
			// String absFileNameForLatLonToReadAsMarker =
			// "/home/gunjan/RWorkspace/GowallaRWorks/gwCinsTarUDOnly_Merged_TarUDOnly_ChicagoTZ_TargetUsersDatesOnly_NVFUsers_ByPids_April6_DistFromChicago.csv";
			//
			// // "./dataToRead/Mar12/gowalla_spots_subset1_fromRaw28Feb2018smallerFileWithSampleWithTZ1.csv";
			// String delimiter = ",";
			// // int latColIndex = 2, lonColIndex = 1, labelColIndex = 0;
			// int latColIndex = 1, lonColIndex = 2, labelColIndex = 5;
			// BorderPane bp = mapPane.getMapPane(absFileNameForLatLonToReadAsMarker, delimiter, latColIndex,
			// lonColIndex,
			// labelColIndex, false);
			// mapTab.setContent(bp);
			// mapTab.setClosable(false);
			// tabsToAdd.add(mapTab);
			// long ttGmap2 = System.currentTimeMillis();
			// System.out.println("google map = " + (ttGmap2 - ttGmap1));

			/***********************************************/

			/***********************************************/
			// $$ Start of disabled on Mar 17 2018

			if (doGivenDataMapPlot)
			{
				long ttOSMmap1 = System.currentTimeMillis();
				Tab osmMapTab = new Tab("Locations OSM Map");
				GluonOSMMap osmapPane = new GluonOSMMap();

				BorderPane bp2 = null;
				/// home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/FSDataWorks/DataProcessingFeb25_2019/placeIDLatLonMap.csv
				if (false)
				{
					bp2 = showFSNYAllAOsLocationsFeb18(osmapPane);
				}
				else
				{
					if (databaseName.equals("geolife1"))
					{
						bp2 = showGeolifeAllAOsStartLocationsFeb18(osmapPane);
					}
					else if (databaseName.equals("gowalla1"))
					{
						if (false)// disabled on June 23
						{
							boolean showOnlyUnique = true;
							bp2 = showGowallaAOLocationsFeb18(osmapPane, showOnlyUnique);
						}
						bp2 = showGowallaLocations(osmapPane);
					}
					else
					{
						PopUps.showError("Uknown database: " + databaseName);
					}
				}
				// $$showGowallaLocations(osmapPane);//disabled on 18 Feb 2019
				// Color.rgb(193, 49, 34, 0.3));
				// $$ BorderPane bp2 = osmapPane.getMapPane2(absFileNameForLatLonToReadAsMarkerTargetLocs, delimiter2,
				// latColIndex2, lonColIndex2, labelColIndex2, fillIndex, 6);// , Color.rgb(0, 105, 106, 0.3));
				osmMapTab.setContent(bp2);
				osmMapTab.setClosable(true);
				tabsToAdd.add(osmMapTab);
				long ttOSMmap2 = System.currentTimeMillis();
				System.out.println("osm map = " + (ttOSMmap2 - ttOSMmap1));
			}
			// $$ End of disabled on Mar 17 2018
			/***********************************************/

			/*
			 * Tab tableTab = new Tab("TableView"); tableTab.setContent(createTableDemoNode());
			 * tableTab.setClosable(false); tabsToAdd.add(tableTab);
			 */
			// Tab accordionTab = new Tab("Accordion/TitledPane");
			// accordionTab.setContent(createAccordionTitledDemoNode());
			// accordionTab.setClosable(false);
			// tabsToAdd.add(accordionTab);

			/*
			 * Tab splitTab = new Tab("SplitPane/TreeView/ListView");
			 * splitTab.setContent(createSplitTreeListDemoNode()); splitTab.setClosable(false); tabsToAdd.add(splitTab);
			 */

			/*
			 * Tab treeTableTab = new Tab("TreeTableView"); treeTableTab.setContent(createTreeTableDemoNode());
			 * treeTableTab.setClosable(false); tabsToAdd.add(treeTableTab);
			 */
			/*
			 * Tab scrollTab = new Tab("ScrollPane/Miscellaneous"); scrollTab.setContent(createScrollMiscDemoNode());
			 * scrollTab.setClosable(false); tabsToAdd.add(scrollTab);
			 */
			/*
			 * Tab htmlTab = new Tab("HTMLEditor"); htmlTab.setContent(createHtmlEditorDemoNode());
			 * htmlTab.setClosable(false); tabsToAdd.add(htmlTab);
			 */

			/*
			 * final WebView webView = new WebView(); Tab webViewTab = new Tab("WebView");
			 * webViewTab.setContent(webView); webViewTab.setClosable(false); webViewTab.setOnSelectionChanged(e -> {
			 * String randomWebSite = "https://www.google.com"; if (webViewTab.isSelected()) {
			 * webView.getEngine().load(randomWebSite); System.out.println("WebView tab is selected, loading: " +
			 * randomWebSite); } }); tabsToAdd.add(webViewTab);
			 */

			tabPane.getTabs().addAll(tabsToAdd);

			// timelineTab, lineChartTab, scatterChartTab, tableTab, accordionTab, splitTab,
			// treeTableTab, scrollTab, htmlTab, webViewTab);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return tabPane;
	}

	/**
	 * @param osmapPane
	 * @return
	 */
	private static BorderPane showGowallaLocations(GluonOSMMap osmapPane)
	{
		String absFileNameForLatLonToReadAsMarker2 = "./dataToRead/Mar12/gowalla_spots_subset1_fromRaw28Feb2018smallerFileWithSampleWithTZ1.csv";
		String absFileNameForLatLonToReadAsMarkerAll = "/home/gunjan/JupyterWorkspace/data/gowalla_spots_subset1_fromRaw28Feb2018.csv";
		String absFileNameForLatLonToReadAsMarkerTargetLocs = "/home/gunjan/RWorkspace/GowallaRWorks/gw2CheckinsAllTargetUsersDatesOnly_ChicNWLA_ByPids_Mar31.csv";
		String absFileNameForLatLonToReadAsMarkerTargetLocsApril6 = "/home/gunjan/RWorkspace/GowallaRWorks/gwCinsTarUDOnly_Merged_TarUDOnly_ChicagoTZ_TargetUsersDatesOnly_NVFUsers_ByPids_April6_DistFromChicago.csv";
		String pathToLocationAnalysis = "/home/gunjan/git/GeolifeReloaded2_1_cleaned/dataWritten/JUL10ForLocationAnalysis2/";
		String absFileNameForLatLon5MostRecenTrainTestJul10 = pathToLocationAnalysis
				+ "UniqueLocationObjects5DaysTrainTest.csv";
		String absFileNameForLatLonAllJul10 = pathToLocationAnalysis + "UniqueLocationObjects.csv";
		// "/home/gunjan/RWorkspace/GowallaRWorks/gw2CheckinsAllTargetUsersDatesOnly_ChicagoTZ_OnlyUsersWith_GTE75C_GTE54Pids_ByPids_Mar30.csv";
		// "/home/gunjan/RWorkspace/GowallaRWorks/gw2CheckinsAllTargetUsersDatesOnly_ChicagoTZ_OnlyUsersWith_GTE75C_GTE54Pids_SlimmedForMap.csv";//
		// gw2CheckinsAllTargetUsersDatesOnly_ChicagoTZ_OnlyUsersWith_GTE75C_GTE54Pids_ByPids_Mar30.csv";
		// "/home/gunjan/RWorkspace/GowallaRWorks/gw2CheckinsAllTargetUsersDatesOnly_ChicagoTZ_OnlyUsersWith_GTE75C_GTE54Pids_ByPids_uniquePid_Mar29_DistFromChicago.csv";
		// int latColIndex2 = 3, lonColIndex2 = 2, labelColIndex2 = 1, labelColIndex3 = 0;
		// int latColIndex2 = 1, lonColIndex2 = 2, labelColIndex2 = 3, fillIndex = 3;
		// int latColIndex2 = 1, lonColIndex2 = 2, labelColIndex2 = 5;
		int latColIndex2 = 9, lonColIndex2 = 10, labelColIndex2 = 12;

		List<Triple<Double, Double, String>> listOfLocs = ReadingFromFile
				.readListOfLocationsV2(absFileNameForLatLonAllJul10, ",", latColIndex2, lonColIndex2, labelColIndex2);// absFileNameForLatLon5MostRecenTrainTestJul10

		BorderPane bp2 = osmapPane.getMapPaneForListOfLocations(listOfLocs, 5, Color.rgb(0, 105, 106, 0.75), false,
				false, "\t\tShowing UniqueLocationsTrainTest");
		return bp2;
	}

	/**
	 * @param osmapPane
	 * @param onlyUnique
	 * @return
	 */
	private static BorderPane showGowallaAOLocationsFeb18(GluonOSMMap osmapPane, boolean onlyUnique)
	{
		PopUps.showMessage("Inside showGowallaAOLocationsFeb18");
		String fileToRead = PathConstants.pathToSerialisedGowallaCleanedTimelines12Feb2019 + "AllActObjs.csv";
		int latColIndex2 = 10, lonColIndex2 = 11, labelColIndex2 = 4;

		List<Triple<Double, Double, String>> listOfLocs = ReadingFromFile.readListOfLocationsV2(fileToRead, ",",
				latColIndex2, lonColIndex2, labelColIndex2);

		System.out.println("listOfLocs.size() = " + listOfLocs.size());
		PopUps.showMessage("listOfLocs.size() = " + listOfLocs.size());

		if (onlyUnique)
		{
			Set<Triple<Double, Double, String>> listOfUniqueLocs = new HashSet<>();
			listOfUniqueLocs.addAll(listOfLocs);
			System.out.println("listOfUniqueLocs.size() = " + listOfUniqueLocs.size());
			listOfLocs.clear();
			listOfLocs.addAll(listOfUniqueLocs);
		}
		BorderPane bp2 = osmapPane.getMapPaneForListOfLocations(listOfLocs, 5, Color.rgb(0, 105, 106, 0.75), false,
				false, "\t\tShowing Gowalla AO Locations");
		return bp2;
	}

	/**
	 * @param osmapPane
	 * @return
	 */
	private static BorderPane showFSNYAllAOsLocationsFeb18(GluonOSMMap osmapPane)
	{
		String fileToRead = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/FSDataWorks/DataProcessingFeb25_2019/FSNY2018-10-04AllTargetUsersDatesOnlyReplaceCatIDNamesReplacePlaceID.csv";
		int latColIndex2 = 4, lonColIndex2 = 5, labelColIndex2 = 2;

		List<Triple<Double, Double, String>> listOfLocs = ReadingFromFile.readListOfLocationsV2(fileToRead, ",",
				latColIndex2, lonColIndex2, labelColIndex2);

		BorderPane bp2 = osmapPane.getMapPaneForListOfLocations(listOfLocs, 5, Color.rgb(0, 105, 106, 0.75), false,
				false, "\t\tShowing Foursquare NY  AO Locations");
		return bp2;
	}

	/**
	 * @param osmapPane
	 * @return
	 */
	private static BorderPane showGeolifeAllAOsStartLocationsFeb18(GluonOSMMap osmapPane)
	{
		String fileToRead = PathConstants.pathToSerialisedGeolifeCleanedTimelines12Feb2019 + "AllActObjs.csv";
		int latColIndex2 = 11, lonColIndex2 = 12, labelColIndex2 = 4;

		List<Triple<Double, Double, String>> listOfLocs = ReadingFromFile.readListOfLocationsV2(fileToRead, ",",
				latColIndex2, lonColIndex2, labelColIndex2);

		BorderPane bp2 = osmapPane.getMapPaneForListOfLocations(listOfLocs, 5, Color.rgb(0, 105, 106, 0.75), false,
				false, "\t\tShowing Geolife Start Locations");
		return bp2;
	}

	// private LinkedHashMap<String, LinkedHashMap<Date, Timeline>> toOnlySeqOfActIDs(
	// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayToyTimelines)
	// {
	// long dummySTTSinms = 0;
	// long dummyDurationinms = 4 * 1000;
	//
	// LinkedHashMap<String, LinkedHashMap<Date, Timeline>> onlyActIDs = new LinkedHashMap<>();
	//
	// for (Entry<String, LinkedHashMap<Date, Timeline>> uEntry : usersCleanedDayToyTimelines.entrySet())
	// {
	// long startTSForUser = 0;
	//
	// for (Entry<Date, Timeline> dateEntry : uEntry.getValue().entrySet())
	// {
	// ArrayList<ActivityObject> allAOsInDay = new ArrayList<>();
	// for (ActivityObject ao : dateEntry.getValue().getActivityObjectsInTimeline())
	// {
	// ao.setStartTimestamp(new Timestamp(startTSForUser));
	// ao.setEndTimestamp(new Timestamp(startTSForUser + dummyDurationinms));
	// startTSForUser = startTSForUser + dummyDurationinms + 1 * 1000;
	// }
	// Timeline t = new Timeline(allAOsInDay, false, true);
	//
	// }
	// }
	// return null;
	// }

	private Node createOnlyActIDsAsRects(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayToyTimelines)
	{
		LinkedHashMap<String, Timeline> continousTimelines = TimelineTransformers
				.dayTimelinesToTimelines(usersCleanedDayToyTimelines);

		double widthOfActRect = 50, widthOfUserRect = 150, heightOfActRect = 50;

		ScrollPane s1 = new ScrollPane();
		s1.setPannable(true);

		VBox vBox = new VBox();
		vBox.setSpacing(10);
		vBox.setAlignment(Pos.CENTER);

		for (Entry<String, Timeline> userEntry : continousTimelines.entrySet())
		{
			HBox hBox = new HBox();
			// hBox.setSpacing(3);
			hBox.setAlignment(Pos.CENTER_LEFT);

			hBox.getChildren().add(createStackPane(Color.WHITE, null, "User " + userEntry.getKey(), widthOfUserRect, "",
					"rectangle", null, heightOfActRect, true));

			for (ActivityObject2018 ao : userEntry.getValue().getActivityObjectsInTimeline())
			{
				Color actColor = ColorPalette.getColor(Dashboard4.actIDIndexMap.get(ao.getActivityID()));
				hBox.getChildren().add(createStackPane(actColor, null, String.valueOf(ao.getActivityID()),
						widthOfActRect, getTooltipFromAO(ao), "circle", null, heightOfActRect, true));
			}
			vBox.getChildren().add(hBox);
		}
		s1.setContent(vBox);
		s1.setFitToHeight(true);
		s1.setFitToWidth(true);
		return s1;
	}

	/**
	 * Fork of org.activity.ui.Dashboard3.createOnlyActIDsAsRects() to also indicate days
	 * 
	 * @param usersCleanedDayToyTimelines
	 * @return
	 */
	private Node createOnlyActIDsAsRectsV2(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayToyTimelines)
	{
		double widthOfActRect = 50, widthOfUserRect = 150, heightOfActRect = 50;

		ScrollPane s1 = new ScrollPane();
		s1.setPannable(true);

		VBox vBox = new VBox();
		vBox.setSpacing(10);
		vBox.setAlignment(Pos.CENTER);

		for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayToyTimelines.entrySet())
		{
			HBox hBox = new HBox();
			// hBox.setSpacing(6);
			hBox.setAlignment(Pos.CENTER_LEFT);
			hBox.getChildren().add(createStackPane(Color.WHITE, null, "User " + userEntry.getKey(), widthOfUserRect, "",
					"rectangle", null, heightOfActRect, true));

			boolean altDayToggle = false;

			for (Entry<Date, Timeline> dateEntry : userEntry.getValue().entrySet())
			{
				altDayToggle = !altDayToggle; // to identify days by differently colored border

				Date date = dateEntry.getKey();
				String dateString = date.toString();
				Border borderForDay = null;

				borderForDay = altDayToggle ? reuse.getBottomBorder1() : reuse.getBottomBorder2();

				for (ActivityObject2018 ao : dateEntry.getValue().getActivityObjectsInTimeline())
				{
					Color actColor = ColorPalette.getColor(Dashboard4.actIDIndexMap.get(ao.getActivityID()));
					hBox.getChildren().add(createStackPane(actColor, null, String.valueOf(ao.getActivityID()),
							widthOfActRect, getTooltipFromAO(ao), "rectangle", borderForDay, heightOfActRect, true));
				}

			}

			vBox.getChildren().add(hBox);
		}
		s1.setContent(vBox);
		s1.setFitToHeight(true);
		s1.setFitToWidth(true);
		return s1;
	}

	/**
	 * Fork of org.activity.ui.Dashboard3.createOnlyActIDsAsRectsV2() to also indicate days
	 * 
	 * @param usersCleanedDayToyTimelines
	 * @return
	 * @since 6 March 2019
	 */
	private Node createOnlyActIDsAsRectsV3(
			LinkedHashMap<String, LinkedHashMap<Date, Timeline>> usersCleanedDayToyTimelines)
	{
		double widthOfActRect = 5/* 50 */, widthOfUserRect = 150, heightOfActRect = 15;

		ScrollPane s1 = new ScrollPane();
		s1.setPannable(true);

		VBox vBox = new VBox();
		vBox.setSpacing(10);
		vBox.setAlignment(Pos.CENTER);

		HBox hBoxActIDs = new HBox();
		// HBox.set
		// hBoxActIDs.setSpacing(2);
		hBoxActIDs.setAlignment(Pos.CENTER_LEFT);// Pos.CENTER_LEFT);
		hBoxActIDs.getChildren().add(createStackPane(Color.WHITE, null, "ActIDs: ", widthOfUserRect, "", "rectangle",
				null, heightOfActRect, true));

		HBox hBoxDuration = new HBox();
		// hBoxDuration.setSpacing(2);
		hBoxDuration.setAlignment(Pos.CENTER_LEFT);
		hBoxDuration.getChildren().add(createStackPane(Color.WHITE, null, "Duration: ", widthOfUserRect, "",
				"rectangle", null, heightOfActRect, true));

		Stream<ActivityObject2018> streamOfAOs = usersCleanedDayToyTimelines.entrySet().parallelStream()
				.flatMap(e -> e.getValue().entrySet().parallelStream()
						.flatMap(f -> f.getValue().getActivityObjectsInTimeline().parallelStream()));

		LongSummaryStatistics durInSecsStats = streamOfAOs.mapToLong(ao -> ao.getDurationInSeconds())
				.summaryStatistics();

		for (Entry<String, LinkedHashMap<Date, Timeline>> userEntry : usersCleanedDayToyTimelines.entrySet())
		{

			boolean altDayToggle = false;

			for (Entry<Date, Timeline> dateEntry : userEntry.getValue().entrySet())
			{
				altDayToggle = !altDayToggle; // to identify days by differently colored border

				Date date = dateEntry.getKey();
				String dateString = date.toString();
				Border borderForDay = null;

				borderForDay = altDayToggle ? reuse.getBottomBorder1() : reuse.getBottomBorder2();

				for (ActivityObject2018 ao : dateEntry.getValue().getActivityObjectsInTimeline())
				{
					Color actColor = ColorPalette.getColor(Dashboard4.actIDIndexMap.get(ao.getActivityID()));
					hBoxActIDs.getChildren().add(createStackPane(actColor, null, String.valueOf(ao.getActivityID()),
							widthOfActRect, getTooltipFromAO(ao), "rectangle", borderForDay, heightOfActRect, false));

					///////////////////////
					long durInMins = ao.getDurationInSeconds();
					hBoxDuration.getChildren()
							.add(createStackPane2(actColor, null, String.valueOf(durInMins), widthOfActRect,
									getTooltipFromAO(ao), "rectangle", borderForDay, (double) durInSecsStats.getMax(),
									(double) durInSecsStats.getMin(), 200, false));
				}
			}

			/////////////

		}
		vBox.getChildren().add(hBoxActIDs);
		vBox.getChildren().add(hBoxDuration);
		s1.setContent(vBox);
		s1.setFitToHeight(true);
		s1.setFitToWidth(true);
		return s1;
	}

	public static String getTooltipFromAO(ActivityObject2018 ao)
	{
		// String toolTipText = "st: " + ao.getStartTimestamp().toString() + "\nlocG: "
		// + ao.getGivenDimensionVal("|", PrimaryDimension.LocationGridID) + "\ndistP (km): "
		// + (ao.getDistanceInMFromPrev() / 1000) + "\ndurP (min): " + (ao.getDurationInSecondsFromPrev() / 60)
		// + "\nphotos: " + ao.getPhotos_count();

		return ao.toStringAllGowallaTSWithNameForHeaded24Dec("\n");

		// return toolTipText;
	}

	// private Rectangle createRectangle(Color fillColor, Color strokeColor, double width)
	// {
	// Rectangle rect1 = new Rectangle(0, 45, width, 50);
	// if (strokeColor != null)
	// {
	// rect1.setStroke(strokeColor);
	// rect1.setStrokeWidth(2);
	// }
	// rect1.setFill(fillColor);
	// return rect1;
	// }

	private Rectangle createRectangle2(Color fillColor, Color strokeColor, double width, double height)
	{
		Rectangle rect1 = new Rectangle(0, 45, width, height);
		if (strokeColor != null)
		{
			rect1.setStroke(strokeColor);
			rect1.setStrokeWidth(2);
		}
		rect1.setFill(fillColor);
		return rect1;
	}

	private Circle createCircle(Color fillColor, Color strokeColor, double width)
	{
		Circle circ = new Circle();
		circ.setCenterX(0);
		circ.setCenterY(0);
		circ.setRadius(width / 2);
		if (strokeColor != null)
		{
			circ.setStroke(strokeColor);
			circ.setStrokeWidth(2);
		}
		circ.setFill(fillColor);
		return circ;
	}

	private Text createText(String text)
	{
		Text t = new Text();
		// t.setFont(new Font(20));
		t.setFont(Font.font(null, FontWeight.BOLD, 20));
		t.setText(text);
		return t;
	}

	/**
	 * 
	 * @param tooltipText
	 * @return
	 */
	public static Tooltip createTooltip(String tooltipText)
	{
		Tooltip tooltip2 = new Tooltip();
		UIUtilityBox.hackTooltipStartTiming(tooltip2);
		tooltip2.setText(tooltipText);
		return tooltip2;
	}

	/**
	 * 
	 * @param fillColor
	 * @param strokeColor
	 * @param labelText
	 * @param width
	 * @param tooltipText
	 * @param shape
	 * @param border
	 * @param height
	 * @return
	 */
	private StackPane createStackPane(Color fillColor, Color strokeColor, String labelText, double width,
			String tooltipText, String shape, Border border, double height, boolean addLabel)
	{
		final StackPane stack = new StackPane();
		Node shapedNode = null;

		if (border != null)
		{
			// stack.setBackground(bg);
			stack.setBorder(border);
			// stack.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
		}
		switch (shape.toLowerCase())
		{
		case "rectangle":
			shapedNode = createRectangle2(fillColor, strokeColor, width, height);
			break;
		case "circle":
			shapedNode = createCircle(fillColor, strokeColor, width);
			break;
		default:
			PopUps.showError("Error: unkown shape " + shape);
		}

		stack.getChildren().add(shapedNode);// , createText(labelText));
		if (addLabel)
		{
			stack.getChildren().add(createText(labelText));
		}
		Tooltip.install(stack, createTooltip(tooltipText));
		return stack;
	}

	/**
	 * 
	 * @param fillColor
	 * @param strokeColor
	 * @param labelText
	 * @param width
	 * @param tooltipText
	 * @param shape
	 * @param minVal
	 * @param maxVal,
	 * @return
	 * @since 6 March 2019
	 */
	private StackPane createStackPane2(Color fillColor, Color strokeColor, String labelText, double width,
			String tooltipText, String shape, Border border, double maxVal, double minVal, double maxHeight,
			boolean addLabel)
	{
		final StackPane stack = new StackPane();
		stack.setAlignment(Pos.BOTTOM_CENTER);
		Node shapedNode = null;
		// double maxHeight = 500;

		double normHeight = maxHeight * ((maxVal - Double.valueOf(labelText)) / (maxVal - minVal));

		if (border != null)
		{
			// stack.setBackground(bg);
			stack.setBorder(border);
			// stack.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
		}
		switch (shape.toLowerCase())
		{
		case "rectangle":
			shapedNode = createRectangle2(fillColor, strokeColor, width, normHeight);
			break;
		case "circle":
			shapedNode = createCircle(fillColor, strokeColor, width);
			break;
		default:
			PopUps.showError("Error: unkown shape " + shape);
		}

		Text t = new Text();
		// t.setFont(new Font(20));
		Font f = Font.font(null, FontWeight.BOLD, 12);
		t.setFont(f);
		t.setRotate(-90);
		t.setText(labelText);

		stack.getChildren().add(shapedNode);// , t);
		if (addLabel)
		{
			stack.getChildren().add(t);
		}
		Tooltip.install(stack, createTooltip(tooltipText));
		return stack;
	}

	Node createTableDemoNode()
	{
		// TableView table = new TableView(model.getTeamMembers());
		// TableColumn firstNameColumn = new TableColumn("First Name");
		// firstNameColumn.setCellValueFactory(new PropertyValueFactory("firstName"));
		// firstNameColumn.setPrefWidth(180);
		// TableColumn lastNameColumn = new TableColumn("Last Name");
		// lastNameColumn.setCellValueFactory(new PropertyValueFactory("lastName"));
		// lastNameColumn.setPrefWidth(180);
		// TableColumn phoneColumn = new TableColumn("Phone Number");
		// phoneColumn.setCellValueFactory(new PropertyValueFactory("phone"));
		// phoneColumn.setPrefWidth(180);
		// table.getColumns().addAll(firstNameColumn, lastNameColumn, phoneColumn);
		// table.getSelectionModel().selectedItemProperty().addListener((ObservableValue observable, Object oldValue,
		// Object newValue) -> {
		// Person selectedPerson = (Person) newValue;
		// System.out.println(selectedPerson + " chosen in TableView");
		// });
		// return table;
		return new Button("test");
	}

	Node createAccordionTitledDemoNode()
	{
		TitledPane paneA = new TitledPane("TitledPane A", new TextArea("TitledPane A content"));
		TitledPane paneB = new TitledPane("TitledPane B", new TextArea("TitledPane B content"));
		TitledPane paneC = new TitledPane("TitledPane C", new TextArea("TitledPane C content"));
		Accordion accordion = new Accordion();
		accordion.getPanes().addAll(paneA, paneB, paneC);
		accordion.setExpandedPane(paneA);
		return accordion;
	}

	Node createSplitTreeListDemoNode()
	{
		// TreeItem animalTree = new TreeItem("Animal");
		// animalTree.getChildren().addAll(new TreeItem("Lion"), new TreeItem("Tiger"), new TreeItem("Bear"));
		// TreeItem mineralTree = new TreeItem("Mineral");
		// mineralTree.getChildren().addAll(new TreeItem("Copper"), new TreeItem("Diamond"), new TreeItem("Quartz"));
		// TreeItem vegetableTree = new TreeItem("Vegetable");
		// vegetableTree.getChildren().addAll(new TreeItem("Arugula"), new TreeItem("Broccoli"), new
		// TreeItem("Cabbage"));
		//
		// TreeItem root = new TreeItem("Root");
		// root.getChildren().addAll(animalTree, mineralTree, vegetableTree);
		// TreeView treeView = new TreeView(root);
		// treeView.setMinWidth(150);
		// treeView.setShowRoot(false);
		// treeView.setEditable(false);
		//
		// ListView listView = new ListView(model.listViewItems);
		//
		// SplitPane splitPane = new SplitPane();
		// splitPane.getItems().addAll(treeView, listView);
		//
		// treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		// treeView.getSelectionModel().selectedItemProperty().addListener((ObservableValue observable, Object oldValue,
		// Object newValue) -> {
		// TreeItem treeItem = (TreeItem) newValue;
		// if (newValue != null && treeItem.isLeaf())
		// {
		// model.listViewItems.clear();
		// for (int i = 1; i <= 10000; i++)
		// {
		// model.listViewItems.add(treeItem.getValue() + " " + i);
		// }
		// }
		// });
		//
		// return splitPane;
		return new Button("dummy");
	}

	Node createTreeTableDemoNode()
	{
		// TreeTableView<Person> treeTableView = new TreeTableView(model.getFamilyTree());
		// TreeTableColumn<Person, String> firstNameColumn = new TreeTableColumn("First Name");
		// firstNameColumn.setCellValueFactory(new TreeItemPropertyValueFactory("firstName"));
		// firstNameColumn.setPrefWidth(180);
		// TreeTableColumn lastNameColumn = new TreeTableColumn("Last Name");
		// lastNameColumn.setCellValueFactory(new TreeItemPropertyValueFactory("lastName"));
		// lastNameColumn.setPrefWidth(180);
		// TreeTableColumn phoneColumn = new TreeTableColumn("Phone Number");
		// phoneColumn.setCellValueFactory(new TreeItemPropertyValueFactory("phone"));
		// phoneColumn.setPrefWidth(180);
		// treeTableView.getColumns().addAll(firstNameColumn, lastNameColumn, phoneColumn);
		// treeTableView.getSelectionModel().selectedItemProperty().addListener(
		// (ObservableValue<? extends TreeItem<Person>> observable, TreeItem<Person> oldValue, TreeItem<Person>
		// newValue) -> {
		// Person selectedPerson = newValue.getValue();
		// System.out.println(selectedPerson + " chosen in TreeTableView");
		// });
		// treeTableView.setShowRoot(false);
		// return treeTableView;
		return new Button("dummy createTreeTableDemoNode ");
	}

	Node createScrollMiscDemoNode()
	{
		Button button = new Button("Button");
		button.setOnAction(e -> System.out.println(e.getEventType() + " occurred on Button"));
		final CheckBox checkBox = new CheckBox("CheckBox");
		checkBox.setOnAction(e ->
			{
				System.out.print(e.getEventType() + " occurred on CheckBox");
				System.out.print(", and selectedProperty is: ");
				System.out.println(checkBox.selectedProperty().getValue());
			});

		final ToggleGroup radioToggleGroup = new ToggleGroup();
		RadioButton radioButton1 = new RadioButton("RadioButton1");
		radioButton1.setToggleGroup(radioToggleGroup);
		RadioButton radioButton2 = new RadioButton("RadioButton2");
		radioButton2.setToggleGroup(radioToggleGroup);
		HBox radioBox = new HBox(10, radioButton1, radioButton2);

		Hyperlink link = new Hyperlink("Hyperlink");
		link.setOnAction(e -> System.out.println(e.getEventType() + " occurred on Hyperlink"));

		// ChoiceBox choiceBox;
		// choiceBox = new ChoiceBox(model.choiceBoxItems);
		// choiceBox.getSelectionModel().selectFirst();
		// choiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
		// System.out.println(newValue + " chosen in ChoiceBox");
		// });
		//
		// MenuItem menuA = new MenuItem("MenuItem A");
		// menuA.setOnAction(e -> System.out.println(e.getEventType() + " occurred on Menu Item A"));
		// MenuItem menuB = new MenuItem("MenuItem B");
		// MenuButton menuButton = new MenuButton("MenuButton");
		// menuButton.getItems().addAll(menuA, menuB);
		//
		// MenuItem splitMenuA = new MenuItem("MenuItem A");
		// splitMenuA.setOnAction(e -> System.out.println(e.getEventType() + " occurred on Menu Item A"));
		// MenuItem splitMenuB = new MenuItem("MenuItem B");
		// SplitMenuButton splitMenuButton = new SplitMenuButton(splitMenuA, splitMenuB);
		// splitMenuButton.setText("SplitMenuButton");
		// splitMenuButton.setOnAction(e -> System.out.println(e.getEventType() + " occurred on SplitMenuButton"));
		//
		// final TextField textField = new TextField();
		// textField.setPromptText("Enter user name");
		// textField.setPrefColumnCount(16);
		// textField.textProperty().addListener((ov, oldValue, newValue) -> {
		// System.out.println("TextField text is: " + textField.getText());
		// });
		//
		// final PasswordField passwordField = new PasswordField();
		// passwordField.setPromptText("Enter password");
		// passwordField.setPrefColumnCount(16);
		// passwordField.focusedProperty().addListener((ov, oldValue, newValue) -> {
		// if (!passwordField.isFocused())
		// {
		// System.out.println("PasswordField text is: " + passwordField.getText());
		// }
		// });
		//
		// final TextArea textArea = new TextArea();
		// textArea.setPrefColumnCount(12);
		// textArea.setPrefRowCount(4);
		// textArea.focusedProperty().addListener((ov, oldValue, newValue) -> {
		// if (!textArea.isFocused())
		// {
		// System.out.println("TextArea text is: " + textArea.getText());
		// }
		// });
		//
		// LocalDate today = LocalDate.now();
		// DatePicker datePicker = new DatePicker(today);
		// datePicker.setOnAction(e -> System.out.println("Selected date: " + datePicker.getValue()));
		//
		// ColorPicker colorPicker = new ColorPicker(Color.BLUEVIOLET);
		// colorPicker.setOnAction(e -> System.out.println("Selected color: " + colorPicker.getValue()));
		//
		// final ProgressIndicator progressIndicator = new ProgressIndicator();
		// progressIndicator.setPrefWidth(200);
		// progressIndicator.progressProperty().bind(model.rpm.divide(model.maxRpm));
		//
		// final Slider slider = new Slider(-1, model.maxRpm, 0);
		// slider.setPrefWidth(200);
		// slider.valueProperty().bindBidirectional(model.rpm);
		//
		// final ProgressBar progressBar = new ProgressBar();
		// progressBar.setPrefWidth(200);
		// progressBar.progressProperty().bind(model.kph.divide(model.maxKph));
		//
		// final ScrollBar scrollBar = new ScrollBar();
		// scrollBar.setPrefWidth(200);
		// scrollBar.setMin(-1);
		// scrollBar.setMax(model.maxKph);
		// scrollBar.valueProperty().bindBidirectional(model.kph);
		//
		// VBox variousControls = new VBox(20, button, checkBox, radioBox, link, choiceBox, menuButton, splitMenuButton,
		// textField,
		// passwordField, new HBox(10, new Label("TextArea:"), textArea), datePicker, colorPicker, progressIndicator,
		// slider,
		// progressBar, scrollBar);
		//
		// variousControls.setPadding(new Insets(10, 10, 10, 10));
		// radioToggleGroup.selectToggle(radioToggleGroup.getToggles().get(0));
		// radioToggleGroup.selectedToggleProperty().addListener((ov, oldValue, newValue) -> {
		// RadioButton rb = ((RadioButton) radioToggleGroup.getSelectedToggle());
		// if (rb != null)
		// {
		// System.out.println(rb.getText() + " selected");
		// }
		// });
		//
		// MenuItem contextA = new MenuItem("MenuItem A");
		// contextA.setOnAction(e -> System.out.println(e.getEventType() + " occurred on Menu Item A"));
		// MenuItem contextB = new MenuItem("MenuItem B");
		// final ContextMenu contextMenu = new ContextMenu(contextA, contextB);
		//
		// ScrollPane scrollPane = new ScrollPane(variousControls);
		// scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		// scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		// scrollPane.setOnMousePressed((MouseEvent me) -> {
		// if (me.getButton() == MouseButton.SECONDARY)
		// {
		// contextMenu.show(stage, me.getScreenX(), me.getScreenY());
		// }
		// });
		//
		// return scrollPane;
		return button;
	}

	Node createHtmlEditorDemoNode()
	{
		final BorderPane htmlEditorDemo;
		final HTMLEditor htmlEditor = new HTMLEditor();
		htmlEditor.setHtmlText("<p>Replace this text</p>");
		Button viewHtmlButton = new Button("View HTML");
		viewHtmlButton.setOnAction(e ->
			{
				Popup alertPopup = createAlertPopup(htmlEditor.getHtmlText());
				alertPopup.show(stage, (stage.getWidth() - alertPopup.getWidth()) / 2 + stage.getX(),
						(stage.getHeight() - alertPopup.getHeight()) / 2 + stage.getY());
			});
		htmlEditorDemo = new BorderPane();
		htmlEditorDemo.setCenter(htmlEditor);
		htmlEditorDemo.setBottom(viewHtmlButton);

		BorderPane.setAlignment(viewHtmlButton, Pos.CENTER);
		BorderPane.setMargin(viewHtmlButton, new Insets(10, 0, 10, 0));
		return htmlEditorDemo;
	}

	Popup createAlertPopup(String text)
	{
		Popup alertPopup = new Popup();

		final Label htmlLabel = new Label(text);
		htmlLabel.setWrapText(true);
		htmlLabel.setMaxWidth(280);
		htmlLabel.setMaxHeight(140);

		Button okButton = new Button("OK");
		okButton.setOnAction(e -> alertPopup.hide());

		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(htmlLabel);
		borderPane.setBottom(okButton);

		Rectangle rectangle = new Rectangle(300, 200, Color.LIGHTBLUE);
		rectangle.setArcHeight(20);
		rectangle.setArcWidth(20);
		rectangle.setStroke(Color.GRAY);
		rectangle.setStrokeWidth(2);
		StackPane contentPane = new StackPane(rectangle, borderPane);

		alertPopup.getContent().add(contentPane);

		BorderPane.setAlignment(okButton, Pos.CENTER);
		BorderPane.setMargin(okButton, new Insets(10, 0, 10, 0));
		return alertPopup;
	}

	/**
	 * 
	 * @return
	 */
	private MenuBar generateMenuBar()
	{
		////////////////////////////
		MenuItem newMenuItem = new MenuItem("New");
		newMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+n"));
		newMenuItem.setOnAction(e -> System.out.println(e.getEventType() + " occurred on MenuItem New"));

		MenuItem saveMenuItem = new MenuItem("Save");
		saveMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+s"));

		MenuItem exitMenuItem = new MenuItem("Exit");
		exitMenuItem.setAccelerator(KeyCombination.keyCombination("Ctrl+q"));
		exitMenuItem.setOnAction(e -> Platform.exit());
		////////////////////////////

		Menu fileMenu = new Menu("File");
		fileMenu.getItems().addAll(newMenuItem, saveMenuItem, exitMenuItem);

		////////////////////////////

		MenuItem itemCut = new MenuItem("Cut");
		itemCut.setAccelerator(KeyCombination.keyCombination("Ctrl+x"));
		itemCut.setOnAction(e -> System.out.println(e.getEventType() + " occurred on MenuItem New"));

		MenuItem itemCopy = new MenuItem("Copy");
		itemCopy.setAccelerator(KeyCombination.keyCombination("Ctrl+c"));
		itemCopy.setOnAction(e -> System.out.println(e.getEventType() + " occurred on MenuItem New"));

		MenuItem itemPaste = new MenuItem("Paste");
		itemPaste.setAccelerator(KeyCombination.keyCombination("Ctrl+p"));
		itemPaste.setOnAction(e -> System.out.println(e.getEventType() + " occurred on MenuItem New"));

		////////////////////////////

		Menu editMenu = new Menu("Edit");
		editMenu.getItems().addAll(itemCut, itemCopy, itemPaste);

		////////////////////////////

		MenuItem itemAbout = new MenuItem("About");
		itemPaste.setOnAction(e -> System.out.println(e.getEventType() + " occurred on MenuItem New"));

		////////////////////////////

		Menu helpMenu = new Menu("Help");
		helpMenu.getItems().addAll(itemAbout);

		////////////////////////////

		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(fileMenu, editMenu, helpMenu);
		return menuBar;
	}

	public static ArrayList<Pair<String, ArrayList<Pair<Double, Double>>>> getSequenceForChosenVariable(
			List<List<String>> allAOData, List<String> header, String featureHeader)
	{

		ArrayList<Pair<String, ArrayList<Pair<Double, Double>>>> listOfListOfPairData = new ArrayList<>();// singleton
		listOfListOfPairData
				.add(new Pair<>(featureHeader, getSequenceForChosenVariable(header.indexOf(featureHeader), allAOData)));
		return listOfListOfPairData;
	}

	public static ArrayList<Pair<String, ArrayList<Pair<Double, String>>>> getSequenceForChosenVariableString(
			List<List<String>> allAOData, List<String> header, String featureHeader)
	{

		ArrayList<Pair<String, ArrayList<Pair<Double, String>>>> listOfListOfPairData = new ArrayList<>();// singleton
		listOfListOfPairData.add(new Pair<>(featureHeader,
				getSequenceForChosenVariableString(header.indexOf(featureHeader), allAOData)));
		return listOfListOfPairData;
	}

	/**
	 * 
	 * @param indexOfChosenVariable
	 * @param allAOData
	 * @return
	 */
	private static ArrayList<Pair<Double, Double>> getSequenceForChosenVariable(int indexOfChosenVariable,
			List<List<String>> allAOData)
	{
		int rowIndex = 0;
		ArrayList<Pair<Double, Double>> seriesForChosenVariable = new ArrayList<>(allAOData.size());
		for (List<String> rowEntry : allAOData)
		{
			seriesForChosenVariable
					.add(new Pair<>((double) rowIndex, Double.valueOf(rowEntry.get(indexOfChosenVariable))));
			rowIndex += 1;
		}
		return seriesForChosenVariable;

	}

	/**
	 * 
	 * @param indexOfChosenVariable
	 * @param allAOData
	 * @return
	 */
	private static ArrayList<Pair<Double, String>> getSequenceForChosenVariableString(int indexOfChosenVariable,
			List<List<String>> allAOData)
	{
		int rowIndex = 0;
		ArrayList<Pair<Double, String>> seriesForChosenVariable = new ArrayList<>(allAOData.size());
		for (List<String> rowEntry : allAOData)
		{
			seriesForChosenVariable.add(new Pair<>((double) rowIndex, (rowEntry.get(indexOfChosenVariable))));
			rowIndex += 1;
		}
		return seriesForChosenVariable;
	}

	public ScrollPane createMultiSeriesChartsFromAllAOs(String databaseName)
	{
		List<List<String>> allAOData = ReadingFromFile.readLinesIntoListOfLists(
				PathConstants.getPathToCleanedTimelinesFeb2019(databaseName) + "AllActObjs.csv", ",");
		List<String> header = allAOData.remove(0);
		System.out.println("allAOData.size() = " + allAOData.size());

		System.out.println("List of headers = " + header.toString());
		Map<String, String> colNameType = new LinkedHashMap<>(header.size());

		VBox vboxOfCharts = new VBox();
		// vboxOfCharts.getChildren().add((chart_SThourOfDay));
		// vboxOfCharts.getChildren().add((chart_durationInSeconds));
		// vboxOfCharts.getChildren().add((chart_distanceTravelledInKm));
		// vboxOfCharts.setAlignment(Pos.BASELINE_RIGHT);

		int testRowIndex = 5;

		// sizeOfData*maxX/desiredPointInOneScreen

		double maxX = Screen.getPrimary().getVisualBounds().getMaxX();
		double maxY = Screen.getPrimary().getVisualBounds().getMaxY();
		int numOfDesiredDataPointsInOneScreen = 250;

		double prefHeight = maxY / 3;
		double prefWidth = allAOData.size() * maxX / numOfDesiredDataPointsInOneScreen;
		// Screen.getPrimary().getVisualBounds().getMaxX() * 3;

		for (int i = 1; i < 4; /* header.size(); */ i++)
		{
			try
			{
				NumberAxis xAxis = new NumberAxis();//

				if (true)
				{
					xAxis.setTickMarkVisible(true);
					// xAxis.setTickUnit(100);
					xAxis.setAutoRanging(false);
					int maxXToShow = ((allAOData.size() + 99) / 100) * 100;
					xAxis.setUpperBound(maxXToShow);
					int tickUnit = maxXToShow / 100;
					xAxis.setTickUnit(5);// Math.max(tickUnit, 1));
					xAxis.setMinorTickCount(5);
					System.out.println("xAxis.getTickUnit() = " + xAxis.getTickUnit());
				}

				Double.parseDouble(allAOData.get(testRowIndex).get(i));// to see if it is a number
				// is double
				ObservableList<Series<Double, Double>> listOfSeries_SThourOfDay = FXUtils
						.toObservableListOfSeriesOfPairData(
								getSequenceForChosenVariable(allAOData, header, header.get(i)));

				NumberAxis yAxis = new NumberAxis();//
				yAxis.setLabel(header.get(i));
				yAxis.setTickLabelRotation(270);

				LineChart chart = new LineChart(xAxis, yAxis, listOfSeries_SThourOfDay);
				// ScatterChart chart = new ScatterChart(xAxis, yAxis, listOfSeries_SThourOfDay);
				chart.setLegendVisible(false);
				// chart.setLegendSide(Side.LEFT);
				// chart.lege
				// chart.setCreateSymbols(false); // this part is the one that consumes more time
				// chart.setPrefSize(prefWidth, prefHeight);
				chart.setMinSize(prefWidth, prefHeight);
				chart.setCache(true);
				chart.setCacheHint(CacheHint.SPEED);
				chart.setAnimated(false);
				chart.setVerticalGridLinesVisible(false);// for performance
				chart.setHorizontalGridLinesVisible(false);// for performance
				// chart.setScaleShape(false);
				// chart.setStyle(value);
				// chart.setStyle("-fx-background-color: slateblue; -fx-text-fill: white;");
				// .chart-vertical-grid-lines {
				// -fx-stroke: #3278fa;
				// }

				// chart.snapshot(callback, params, image);
				// chart.setLegendVisible(false);

				if (false) //
				{
					// Not able to implement/finish the following javafx task for high performance charts:
					// JavaFX charts --> WritableImage --> Canvas

					// Create an ImageView with the URL of the image source
					// ImageView imageView = new ImageView(
					// // "https://cdn.emojidex.com/emoji/px128/rhinoceros.png?1449235185");
					// "OTE_WebsiteHeader.jpg");
					// SnapshotParameters snapshotParameters = new SnapshotParameters();
					// snapshotParameters.setTransform(new Scale(10, 10));

					// take snap for performance
					SnapshotParameters snP = new SnapshotParameters();
					// Create a viewport located at (0, 0) and of isze 200
					Rectangle2D viewport = new Rectangle2D(100, 100, 1000, 1000);
					snP.setViewport(viewport);// Screen.getPrimary().getVisualBounds());
					// WritableImage wi = new WritableImage((int) maxX, (int) maxY);
					// wi = chart.snapshot(null, null);
					//

					final Canvas cv = new Canvas(maxX, maxY);
					GraphicsContext gc = cv.getGraphicsContext2D();
					// gc.setFill(Color.BLUE);
					// gc.fillRect(75, 75, 100, 100);
					gc.drawImage(chart.snapshot(snP, null), 0, 0);
					// ImageView unscaled = new ImageView(chart.snapshot(snP, null));
					vboxOfCharts.getChildren().add(cv);// cv);
				}
				else if (false)
				{
					ResizeableCanvasForLinePlot canvas = new ResizeableCanvasForLinePlot();
					canvas.setData(listOfSeries_SThourOfDay);

					final Canvas cv = new Canvas(maxX, maxY);
					GraphicsContext gc = cv.getGraphicsContext2D();

					// double width = getWidth();
					// double height = getHeight();
					//
					// gc.clearRect(0, 0, width, height);
					gc.strokeRoundRect(10, 10, 50, 50, 10, 10);
					// gc.setFill(Color.BLUE);
					// gc.fillRect(75, 75, 100, 100);

					vboxOfCharts.getChildren().add(cv);// d(cv);
				}
				else
				{
					// HBox hboxOfSingleChart = new HBox();
					// hboxOfSingleChart.getChildren().add(chart);
					// hboxOfSingleChart.setAlignment(Pos.BASELINE_RIGHT);
					vboxOfCharts.getChildren().add(chart);
				}
			}
			catch (NumberFormatException n)
			{
				System.out.println(header.get(i) + " is not double");
				// is String
				// ObservableList<Series<Double, String>> listOfSeries_SThourOfDay = FXUtils
				// .toObservableListOfSeriesOfPairDataString(
				// getSequenceForChosenVariableString(allAOData, header, header.get(i)));
				// LineChart chart = new LineChart(new NumberAxis(), new NumberAxis(), listOfSeries_SThourOfDay);
				// vboxOfCharts.getChildren().add(chart);
			}

		}

		// ObservableList<Series<Double, Double>> listOfSeries_SThourOfDay = FXUtils
		// .toObservableListOfSeriesOfPairData(getSequenceForChosenVariable(allAOData, header, "SThourOfDay"));
		// // System.out.println("listOfSeries.size() = " + listOfSeries.size());
		//
		// ObservableList<Series<Double, Double>> listOfSeries_durationInSeconds = FXUtils
		// .toObservableListOfSeriesOfPairData(
		// getSequenceForChosenVariable(allAOData, header, "durationInSeconds"));
		// // System.out.println("listOfSeries.size() = " + listOfSeries.size());
		//
		// ObservableList<Series<Double, Double>> listOfSeries_distanceTravelledInKm = FXUtils
		// .toObservableListOfSeriesOfPairData(
		// getSequenceForChosenVariable(allAOData, header, "distanceTravelledInKm"));
		//
		// // vbox.setPadding(new Insets(10));
		// // vbox.setSpacing(8);
		//
		// LineChart chart_SThourOfDay = new LineChart(new NumberAxis(), new NumberAxis(), listOfSeries_SThourOfDay);
		// // HBox hbox_SThourOfDay = new HBox(chart_SThourOfDay);
		// // hbox_SThourOfDay.getChildren().add();
		//
		// LineChart chart_durationInSeconds = new LineChart(new NumberAxis(), new NumberAxis(),
		// listOfSeries_durationInSeconds);
		// LineChart chart_distanceTravelledInKm = new LineChart(new NumberAxis(), new NumberAxis(),
		// listOfSeries_distanceTravelledInKm);

		// distanceTravelledInKm

		// VBox vboxOfCharts = new VBox();
		// vboxOfCharts.getChildren().add((chart_SThourOfDay));
		// vboxOfCharts.getChildren().add((chart_durationInSeconds));
		// vboxOfCharts.getChildren().add((chart_distanceTravelledInKm));
		// vboxOfCharts.setAlignment(Pos.CENTER);

		ScrollPane s1 = new ScrollPane();
		// s1.setStyle("-fx-rotate:90");

		if (false)
		{
			WritableImage vboxOfChartsImage = vboxOfCharts.snapshot(null, null);
			final Canvas cv = new Canvas(1000, 1000);
			GraphicsContext gc = cv.getGraphicsContext2D();
			gc.setFill(Color.BLUE);
			// gc.fillRect(75, 75, 100, 100);
			gc.drawImage(vboxOfChartsImage, 0, 0);
			s1.setContent(cv);
		}
		else
		{
			s1.setContent(vboxOfCharts);
		}
		s1.setFitToHeight(true);
		s1.setFitToWidth(true);
		// s1.setAnimated(false);

		return s1;
	}

	// /**
	// * Converts charts to Java {@link WritableImage}s
	// *
	// * @param charts
	// * the charts to be converted to {@link WritableImage}s
	// * @return a {@link List} of {@link WritableImage}s
	// */
	// private List<WritableImage> chartsToImages(List<Chart> charts)
	// {
	// List<WritableImage> chartImages = new ArrayList<>();
	//
	// // Scaling the chart image gives it a higher resolution
	// // which results in a better image quality when the
	// // image is exported to the pdf
	// SnapshotParameters snapshotParameters = new SnapshotParameters();
	// snapshotParameters.setTransform(new Scale(2, 2));
	//
	// for (Chart chart : charts)
	// {
	// chartImages.add(chart.snapshot(snapshotParameters, null));
	// }
	//
	// return chartImages;
	// }
}
