package org.activity.ui;

import org.activity.plotting.ChartUtils;
import org.activity.plotting.DataGenerator;
import org.activity.plotting.TimelineChartApp2;
import org.activity.plotting0.FXUtils;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
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
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.stage.Stage;

/**
 * Fork of DashBoard2
 * 
 * @author gunjan
 *
 */
public class ChartBoard extends Application
{
	Stage stage;
	TabPane tabPane;// = new TabPane();
	// MenuBar menuBar;
	// private final TableView treeView = new TableView();
	// private final TextArea msgLogFld = new TextArea();

	public ChartBoard()
	{
		// super();
		//
	}

	public static void main(String[] args)
	{
		new ChartBoard();
		// System.setProperty("prism.allowhidpi", "true");
		Application.launch(new String[] { "" });

	}

	public void createStage()
	{

	}

	public void start(Stage stage)
	{
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

		tabPane = new TabPane();
		createTabs2();
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
		scene.getStylesheets().add("gsheetNative.css");

		// scene.getStylesheets().add(getClass().getResource("gsheet1.css").toExternalForm());
		stage.setScene(scene);
		stage.setTitle("Dashboard");
		// stage.initStyle(stageStyle);
		stage.show();
		// stage.setFullScreen(true);
		//
		// Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
		// stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
		// stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 4);

	}

	/////////////

	public void addTabToChartBoardAndUpdateTabPane(Tab tabToAdd)
	{
		tabPane.getTabs().addAll(tabToAdd);
	}

	public void addTabToChartBoard(Node nodeToAdd, String tabTitle)
	{
		Tab tabToAdd = new Tab(tabTitle);
		tabToAdd.setContent(nodeToAdd);
		tabToAdd.setClosable(true);
		tabPane.getTabs().add(tabToAdd);
	}

	/**
	 * 
	 * @return
	 */
	private void createTabs2()
	{
		TimelineChartApp2 tcA2 = new TimelineChartApp2();// DataGenerator.getData2(), true);
		addTabToChartBoard(tcA2.createContent(DataGenerator.getData2(), true), "Historical Timelines");

		addTabToChartBoard(
				ChartUtils.createLineChart(FXUtils.toObservableListOfSeriesOfPairData(FXUtils.getSyntheticData(50, 50)),
						"LineChartTitle", "ajooba"),
				"LineChart");

		addTabToChartBoard(ChartUtils.createScatterChart(
				FXUtils.toObservableListOfSeriesOfPairData(FXUtils.getSyntheticData(50, 50)), "ScatterChartTitle",
				"ajooba"), "ScatterChart");

		// Tab tableTab = new Tab("TableView");
		// tableTab.setContent(createTableDemoNode());
		// tableTab.setClosable(false);
		// addTabToChartBoardAndUpdateTabPane(tableTab);
		//
		// Tab accordionTab = new Tab("Accordion/TitledPane");
		// accordionTab.setContent(createAccordionTitledDemoNode());
		// accordionTab.setClosable(false);
		// addTabToChartBoardAndUpdateTabPane(accordionTab);
		//
		// Tab splitTab = new Tab("SplitPane/TreeView/ListView");
		// splitTab.setContent(createSplitTreeListDemoNode());
		// splitTab.setClosable(false);
		// addTabToChartBoardAndUpdateTabPane(splitTab);
		//
		// Tab treeTableTab = new Tab("TreeTableView");
		// treeTableTab.setContent(createTreeTableDemoNode());
		// treeTableTab.setClosable(false);
		// addTabToChartBoardAndUpdateTabPane(treeTableTab);
		//
		// Tab scrollTab = new Tab("ScrollPane/Miscellaneous");
		// scrollTab.setContent(createScrollMiscDemoNode());
		// scrollTab.setClosable(false);
		// addTabToChartBoardAndUpdateTabPane(scrollTab);
		//
		// Tab htmlTab = new Tab("HTMLEditor");
		// htmlTab.setContent(createHtmlEditorDemoNode());
		// htmlTab.setClosable(false);
		// addTabToChartBoardAndUpdateTabPane(htmlTab);
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
