package org.activity.ui;

import java.util.ArrayList;
import java.util.List;

import org.activity.plotting.DataGenerator;
import org.activity.plotting.TimelineChartAppCanvas;
import org.activity.plotting.TimelineChartAppGeneric;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
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
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Popup;
import javafx.stage.Stage;

/**
 * Fork of Dashboard2
 * 
 * @since 12 March 2018
 * @author gunjan
 *
 */
public class Dashboard3 extends Application
{
	Stage stage;
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
		ScreenDetails.printScreensDetails();

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

		TabPane tabPane = createTabs();
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
		scene.getStylesheets().add("./jfxtras/styles/jmetro8/GJMetroLightTheme.css");// gsheetNative.css");

		// scene.getStylesheets().add(getClass().getResource("gsheet1.css").toExternalForm());
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
	 * @return
	 */
	private TabPane createTabs()
	{
		TabPane tabPane = new TabPane();
		List<Tab> tabsToAdd = new ArrayList<>();
		try
		{

			///

			// List<List<List<String>>> timelineData = DataGenerator.getData3(10, 1000, 12, 5, 200, 10, 50);

			List<List<List<String>>> timelineData = DataGenerator.getData3(10, 50, 12, 5, 864000, 60 * 20, 10800);

			long tTimeline0 = System.currentTimeMillis();
			Tab timelineTabCircle = new Tab("timelineTabCircle Historical Timelines All Users");
			TimelineChartAppGeneric tcC = new TimelineChartAppGeneric(timelineData, true, "ActivityCircle");
			timelineTabCircle.setContent(tcC.getVbox());// timelinesVBox2);
			timelineTabCircle.setClosable(true);
			tabsToAdd.add(timelineTabCircle);
			long tTimelinen = System.currentTimeMillis();
			System.out.println("Time taken TimelineChartAppGeneric = " + (tTimelinen - tTimeline0) + " ms");

			long tTimelineCanvas0 = System.currentTimeMillis();
			Tab timelineTabCanvas = new Tab("timelineTabCanvas Historical Timelines All Users");
			TimelineChartAppCanvas tcCanvas = new TimelineChartAppCanvas(timelineData, false);
			timelineTabCanvas.setContent(tcCanvas.getVbox());// timelinesVBox2);
			timelineTabCanvas.setClosable(true);
			tabsToAdd.add(timelineTabCanvas);
			long tTimelineCanvasn = System.currentTimeMillis();
			System.out.println("Time taken TimelineChartAppCanvas = " + (tTimelineCanvasn - tTimelineCanvas0) + " ms");

			if (false)
			{

				Tab timelineTabD = new Tab("timelineTabD Historical Timelines All Users");
				TimelineChartAppGeneric tcD = new TimelineChartAppGeneric(timelineData, true, "ActivityBox");
				// TODO: Issue: not scaling correctly with range change.
				timelineTabD.setContent(tcD.getVbox());// timelinesVBox2);
				timelineTabD.setClosable(true);
				tabsToAdd.add(timelineTabD);

				Tab timelineTabE = new Tab("timelineTabE Historical Timelines All Users");
				TimelineChartAppGeneric tcE = new TimelineChartAppGeneric(timelineData, true, "LineChart");
				timelineTabE.setContent(tcE.getVbox());// timelinesVBox2);
				timelineTabE.setClosable(true);
				tabsToAdd.add(timelineTabE);

				Tab timelineTabScattter = new Tab("timelineTabScattter Historical Timelines All Users");
				TimelineChartAppGeneric tcScattter = new TimelineChartAppGeneric(timelineData, true, "ScatterChart");
				timelineTabScattter.setContent(tcScattter.getVbox());// timelinesVBox2);
				timelineTabScattter.setClosable(true);
				tabsToAdd.add(timelineTabScattter);
			}
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
			// String absFileNameForLatLonToReadAsMarker =
			// "/run/media/gunjan/BackupVault/GOWALLA/GowallaDataWorks/Mar15/locIDsWithNoTimezone.csv";
			// // "./dataToRead/Mar12/gowalla_spots_subset1_fromRaw28Feb2018smallerFileWithSampleWithTZ1.csv";
			// String delimiter = ",";
			// int latColIndex = 2, lonColIndex = 1, labelColIndex = 0;
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
			long ttOSMmap1 = System.currentTimeMillis();
			Tab osmMapTab = new Tab("Locations OSM Map");
			GluonOSMMap osmapPane = new GluonOSMMap();

			String absFileNameForLatLonToReadAsMarker2 = "./dataToRead/Mar12/gowalla_spots_subset1_fromRaw28Feb2018smallerFileWithSampleWithTZ1.csv";
			String absFileNameForLatLonToReadAsMarkerAll = "/home/gunjan/JupyterWorkspace/data/gowalla_spots_subset1_fromRaw28Feb2018.csv";
			String absFileNameForLatLonToReadAsMarkerTargetLocs = "/home/gunjan/RWorkspace/GowallaRWorks/gw2CheckinsAllTargetUsersDatesOnly_ChicNWLA_ByPids_Mar31.csv";
			// "/home/gunjan/RWorkspace/GowallaRWorks/gw2CheckinsAllTargetUsersDatesOnly_ChicagoTZ_OnlyUsersWith_GTE75C_GTE54Pids_ByPids_Mar30.csv";

			// "/home/gunjan/RWorkspace/GowallaRWorks/gw2CheckinsAllTargetUsersDatesOnly_ChicagoTZ_OnlyUsersWith_GTE75C_GTE54Pids_SlimmedForMap.csv";//
			// gw2CheckinsAllTargetUsersDatesOnly_ChicagoTZ_OnlyUsersWith_GTE75C_GTE54Pids_ByPids_Mar30.csv";

			// "/home/gunjan/RWorkspace/GowallaRWorks/gw2CheckinsAllTargetUsersDatesOnly_ChicagoTZ_OnlyUsersWith_GTE75C_GTE54Pids_ByPids_uniquePid_Mar29_DistFromChicago.csv";

			String delimiter2 = ",";

			// int latColIndex2 = 3, lonColIndex2 = 2, labelColIndex2 = 1, labelColIndex3 = 0;
			int latColIndex2 = 1, lonColIndex2 = 2, labelColIndex2 = 3, fillIndex = 3;
			BorderPane bp2 = osmapPane.getMapPane(absFileNameForLatLonToReadAsMarkerTargetLocs, delimiter2,
					latColIndex2, lonColIndex2, labelColIndex2, 5, Color.rgb(0, 105, 106, 0.3), false);// Color.rgb(193,
																										// 49,
			// 34, 0.3));

			// $$ BorderPane bp2 = osmapPane.getMapPane2(absFileNameForLatLonToReadAsMarkerTargetLocs, delimiter2,
			// latColIndex2, lonColIndex2, labelColIndex2, fillIndex, 6);// , Color.rgb(0, 105, 106, 0.3));

			osmMapTab.setContent(bp2);
			osmMapTab.setClosable(false);
			tabsToAdd.add(osmMapTab);
			long ttOSMmap2 = System.currentTimeMillis();
			System.out.println("osm map = " + (ttOSMmap2 - ttOSMmap1));
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

}
