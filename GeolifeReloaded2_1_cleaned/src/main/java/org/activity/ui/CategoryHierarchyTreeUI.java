package org.activity.ui;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.tree.DefaultMutableTreeNode;

import org.activity.io.Serializer;
import org.activity.io.WritingToFile;
import org.activity.tools.JSONProcessingGowallaTryingNonStatic;
import org.activity.util.ComparatorUtils;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Display the category hierarchy tree, allow addition and deletion of catid:catname to the hierarchy tree.
 * 
 * @author gunjan
 *
 */
public class CategoryHierarchyTreeUI extends Application
{
	final String commonPath = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/UI/";
	// $$ "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/";//
	// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep9_2/";
	PrintStream consoleLogStream;
	TreeItem<String> rootNode;

	private final TreeView<String> treeView = new TreeView<>();
	private final TextArea msgLogFld = new TextArea();
	private ListView<String> listForCatIDsToAdd;// = new ListView();

	private final String serializedCatTreeFileNamePhrase = "RootOfCategoryTree25Nov2016.DMTreeNode";// "RootOfCategoryTree9Sep2016.DMTreeNode";

	private final String whichRootToUse = "updated";// "originalRaw";// "updated

	public static void main(String[] args)
	{
		System.setProperty("prism.allowhidpi", "true");
		launch(args);
	}

	@Override
	public void start(Stage primaryStage)
	{
		String checkinFileNameToRead = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/RSubsettedData/gw2CheckinsSpots1TargetUsersDatesOnly2Feb2017.csv";
		// $$"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/gw2CheckinsSpots1TargetUsersDatesOnlyNoDup.csv";

		try
		{
			// consoleLogStream = WritingToFile.redirectConsoleOutput(commonPath + "consoleLog2.txt");
			// Select the root node
			treeView.getSelectionModel().selectFirst();

			JSONProcessingGowallaTryingNonStatic preProcessGowalla = new JSONProcessingGowallaTryingNonStatic(
					commonPath, checkinFileNameToRead);

			/////////
			TreeMap<Integer, String> dict = preProcessGowalla.getCatIDNameDictionary();
			Serializer.kryoSerializeThis(dict, commonPath + "CatIDNameDictionary.kryo");
			/////////
			// Serializer.serializeThis(preProcessGowalla, commonPath + "SerializedPreProcessGowalla24Aug.obj");
			// JSONProcessingGowallaTryingNonStatic preProcessGowalla =
			// (JSONProcessingGowallaTryingNonStatic) Serializer.deSerializeThis(commonPath +
			///////// "SerializedPreProcessGowalla24Aug.obj");

			// ~~~~~~` get deserialised tree.. (the one in which we have already manually added some nodes)
			if (whichRootToUse.equals("updated"))
			{
				DefaultMutableTreeNode rootOfCategoryTreeTN = (DefaultMutableTreeNode) Serializer.deSerializeThis(
						"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/RootOfCategoryTree24Nov2016.DMTreeNode");
				// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep9_2/RootOfCategoryTree9Sep2016.DMTreeNode");

				TreeItem<String> rootOfCategoryTree = UIUtilityBox.convertTreeNodesToTreeItems(rootOfCategoryTreeTN);

				treeView.setRoot(rootOfCategoryTree);
			}
			// ~~~~~~`
			else
			{
				// Set the root node for the TreeViww
				treeView.setRoot(preProcessGowalla.getRootOfCategoryHierarchyTree());
			}
			VBox rightPane = getRightPane();
			// PopUps.showMessage("before second right pane");
			HBox rightPane2 = getPaneForCatIDsNotInHierarchy2(preProcessGowalla.getCheckinCountResultsTogether(),
					preProcessGowalla.getCatIDNameDictionary());

			ScrollPane sp2 = new ScrollPane();
			sp2.setContent(rightPane2);

			HBox root = new HBox(treeView, rightPane, sp2);
			root.setSpacing(20);
			root.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;"
					+ "-fx-border-insets: 5;" + "-fx-border-radius: 5;" + "-fx-border-color: blue;");

			Scene scene = new Scene(root);// , 1600, 900);

			///////////
			// size of the screen
			Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
			primaryStage.setX(visualBounds.getMinX());
			primaryStage.setY(visualBounds.getMinY());
			primaryStage.setWidth(visualBounds.getWidth());
			primaryStage.setHeight(visualBounds.getHeight());
			primaryStage.setMinHeight(200);
			primaryStage.setMinWidth(400);
			primaryStage.setOpacity(0.5); // A half-translucent stage

			//////////
			primaryStage.setTitle("Category Hierarchy");
			primaryStage.setScene(scene);
			// PopUps.showMessage("before staging");
			primaryStage.show();

			System.out.println("The stage must be showing by now");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			// consoleLogStream.close();
		}
	}

	public VBox getRightPane()
	{
		// TextField itemFld = new TextField();

		Button removeItemBtn = new Button("Remove Selected Item");
		removeItemBtn.setOnAction(e -> this.removeItem());

		msgLogFld.setPrefRowCount(15);
		msgLogFld.setPrefColumnCount(25);

		Button saveNewTreeBtn = new Button("Save this tree");
		saveNewTreeBtn.setOnAction(e -> this.saveCurrentTree());

		VBox box = new VBox(new Label("Select an item to add to or remove."), saveNewTreeBtn, removeItemBtn,
				new Label("Message Log:"), msgLogFld);
		box.setSpacing(10);
		return box;
	}

	public VBox getPaneForCatIDsNotInHierarchy(Map<String, TreeMap<Integer, Long>> catIDCountCheckinResultsMap,
			TreeMap<Integer, String> catIDNameDictionary)
	{
		// UtilityBelt.sortByValue(map)
		System.out.println("Inside getPaneForCatIDsNotInHierarchy");
		// PopUps.showMessage("Inside getPaneForCatIDsNotInHierarchy");
		TreeMap<Integer, Long> catIDInCheckinsButNotInAnyLevel = catIDCountCheckinResultsMap
				.get("noneLevelCheckinCountMap");// .keySet();
		long totalNumOfCheckinsNotInAnyLevel = catIDInCheckinsButNotInAnyLevel.values().stream()
				.mapToLong(Number::longValue).sum();

		// PopUps.showMessage("totalNumOfCheckinsNotInAnyLevel= " + totalNumOfCheckinsNotInAnyLevel);
		Map<Integer, Long> catIDInCheckinsButNotInAnyLevelSorted = (Map<Integer, Long>) ComparatorUtils
				.sortByValue(catIDInCheckinsButNotInAnyLevel, true);

		ArrayList<Button> buttonsForCatIDsToAdd = new ArrayList<Button>();

		int count = 0;

		// PopUps.showMessage("Here00");
		for (Integer catid : catIDInCheckinsButNotInAnyLevelSorted.keySet())
		{
			// PopUps.showMessage("Here01");
			count++;

			if (count > 300) break;

			String catName = "not found";
			catName = catIDNameDictionary.get(catid);
			// PopUps.showMessage("Here02 catid=" + catid + " catname=" + catName);
			System.out.println("before adding btn-" + count);

			long numOfCheckinsNotInAnyLevelForThisCatID = catIDInCheckinsButNotInAnyLevel.get(catid);
			// PopUps.showMessage("numOfCheckinsNotInAnyLevelForThisCatID= " + numOfCheckinsNotInAnyLevelForThisCatID);

			double percentageOfTotalCheckinsNotInAnyLevel = ((double) numOfCheckinsNotInAnyLevelForThisCatID
					/ (double) totalNumOfCheckinsNotInAnyLevel) * 100;
			// PopUps.showMessage("percentageOfTotalCheckinsNotInAnyLevel= " + percentageOfTotalCheckinsNotInAnyLevel);

			Button btn = new Button(catid + ":" + catName.trim() + " = " + numOfCheckinsNotInAnyLevelForThisCatID + "__"
					+ (percentageOfTotalCheckinsNotInAnyLevel) + "%"); // NAL: not in any level

			btn.setOnAction(e -> this.newCatToAddSelected());
			// PopUps.showMessage("Here04");
			buttonsForCatIDsToAdd.add(btn);
			System.out.println("after:  adding btn-" + count);
		}
		// PopUps.showMessage("Here05");
		// Button btn = new Button("test");
		// btn.setOnAction(e -> this.newCatToAddSelected());
		// buttonsForCatIDsToAdd.add(btn);

		System.out.println("Num of buttons added, i.e., num of catid in checkins but not in any levels = "
				+ buttonsForCatIDsToAdd.size());

		VBox box = new VBox();
		box.setSpacing(5);

		box.getChildren().addAll(buttonsForCatIDsToAdd);
		// PopUps.showMessage("Here06");
		// VBox box = new VBox(new Label("Select an item to add to or remove."), new HBox(new Label("Item:"), itemFld,
		// addItemBtn),
		// removeItemBtn, new Label("Message Log:"), msgLogFld);
		System.out.println("Exiting getPaneForCatIDsNotInHierarchy");
		return box;
	}

	/**
	 * ListView instead of buttons
	 * 
	 * @param catIDCountCheckinResultsMap
	 * @param catIDNameDictionary
	 * @return
	 */
	public HBox getPaneForCatIDsNotInHierarchy2(Map<String, TreeMap<Integer, Long>> catIDCountCheckinResultsMap,
			TreeMap<Integer, String> catIDNameDictionary)
	{
		// UtilityBelt.sortByValue(map)
		System.out.println("Inside getPaneForCatIDsNotInHierarchy2");
		// PopUps.showMessage("Inside getPaneForCatIDsNotInHierarchy");
		TreeMap<Integer, Long> catIDInCheckinsButNotInAnyLevel = catIDCountCheckinResultsMap
				.get("noneLevelCheckinCountMap");// .keySet();
		long totalNumOfCheckinsNotInAnyLevel = catIDInCheckinsButNotInAnyLevel.values().stream()
				.mapToLong(Number::longValue).sum();

		// $$PopUps.showMessage("totalNumOfCheckinsNotInAnyLevel= " + totalNumOfCheckinsNotInAnyLevel);
		Map<Integer, Long> catIDInCheckinsButNotInAnyLevelSorted = (Map<Integer, Long>) ComparatorUtils
				.sortByValue(catIDInCheckinsButNotInAnyLevel, true);

		listForCatIDsToAdd = new ListView<>();// <Button>();
		listForCatIDsToAdd.setPrefSize(600, 970);

		listForCatIDsToAdd.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

		int count = 0;

		// PopUps.showMessage("Here00");
		for (Integer catid : catIDInCheckinsButNotInAnyLevelSorted.keySet())
		{
			// PopUps.showMessage("Here01");
			count++;

			// if (count > 300)
			// break;

			String catName = "not found";
			catName = catIDNameDictionary.get(catid);
			// PopUps.showMessage("Here02 catid=" + catid + " catname=" + catName);
			// System.out.println("before adding btn-" + count);

			long numOfCheckinsNotInAnyLevelForThisCatID = catIDInCheckinsButNotInAnyLevel.get(catid);
			// PopUps.showMessage("numOfCheckinsNotInAnyLevelForThisCatID= " + numOfCheckinsNotInAnyLevelForThisCatID);

			double percentageOfTotalCheckinsNotInAnyLevel = ((double) numOfCheckinsNotInAnyLevelForThisCatID
					/ (double) totalNumOfCheckinsNotInAnyLevel) * 100;
			// PopUps.showMessage("percentageOfTotalCheckinsNotInAnyLevel= " + percentageOfTotalCheckinsNotInAnyLevel);

			String labelForCatID = (count + "||" + catid + ":" + catName.trim() + " || "
					+ numOfCheckinsNotInAnyLevelForThisCatID + "__" + (percentageOfTotalCheckinsNotInAnyLevel) + "%");

			// Button btn = new Button(catid + ":" + catName.trim() + " || " + numOfCheckinsNotInAnyLevelForThisCatID +
			// "__"
			// + (percentageOfTotalCheckinsNotInAnyLevel) + "%"); // NAL: not in any level
			listForCatIDsToAdd.getItems().add(labelForCatID);
			// btn.setOnAction(e -> this.newCatToAddSelected(e));
			// PopUps.showMessage("Here04");
			// buttonsForCatIDsToAdd.add(btn);
			// System.out.println("after: adding btn-" + count);
		}
		// PopUps.showMessage("Here05");
		// Button btn = new Button("test");
		// btn.setOnAction(e -> this.newCatToAddSelected());
		// buttonsForCatIDsToAdd.add(btn);

		// System.out.println("Num of buttons added, i.e., num of catid in checkins but not in any levels = " +
		// buttonsForCatIDsToAdd.size());
		Button choose = new Button("Add selected cat to selected node");
		choose.setOnAction(e -> this.newCatToAddSelected());

		HBox box = new HBox();
		box.setSpacing(5);

		box.getChildren().addAll(listForCatIDsToAdd, choose);
		// PopUps.showMessage("Here06");
		// VBox box = new VBox(new Label("Select an item to add to or remove."), new HBox(new Label("Item:"), itemFld,
		// addItemBtn),
		// removeItemBtn, new Label("Message Log:"), msgLogFld);
		System.out.println("Exiting getPaneForCatIDsNotInHierarchy");
		return box;
	}

	public void newCatToAddSelected()
	{

		// this.logMsg("newCatToAddSelected pressed");
		String selectedCatItemLabelString = listForCatIDsToAdd.getSelectionModel().getSelectedItem();

		// String splittedString[] = selectedCatItemLabelString.split("\\|\\|");
		// String selectedCatStringToUse = splittedString[1].replaceAll("\\*", "");

		String selectedCatStringToUse = getCatStringFromLabel(selectedCatItemLabelString);
		// String selectedNode = t
		this.logMsg("SelectedItem = " + selectedCatStringToUse);

		this.logMsg("Selected Tree Node = " + selectedCatStringToUse);

		this.addItem(selectedCatStringToUse);
		// this.logMsg("SelectedItem = " + selectedCatStringToUse);
		// return null;
	}

	/**
	 * 
	 * @param label
	 * @return
	 */
	public String getCatStringFromLabel(String label)
	{
		String splittedString[] = label.split("\\|\\|");
		return splittedString[1].replaceAll("\\*", "");
	}

	public void addItem(String value)
	{
		if (value == null || value.trim().equals(""))
		{
			this.logMsg("Item cannot be empty.");
			return;
		}

		TreeItem<String> parent = treeView.getSelectionModel().getSelectedItem();
		if (parent == null)
		{
			this.logMsg("Select a node to add this item to.");
			return;
		}

		// Check for duplicate
		for (TreeItem<String> child : parent.getChildren())
		{
			if (child.getValue().equals(value))
			{
				this.logMsg(value + " already exists under " + parent.getValue());
				return;
			}
		}

		TreeItem<String> newItem = new TreeItem<String>(value);
		parent.getChildren().add(newItem);
		if (!parent.isExpanded())
		{
			parent.setExpanded(true);
		}
	}

	public void removeItem()
	{
		TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();
		if (item == null)
		{
			this.logMsg("Select a node to remove.");
			return;
		}

		TreeItem<String> parent = item.getParent();
		if (parent == null)
		{
			this.logMsg("Cannot remove the root node.");
		}
		else
		{
			parent.getChildren().remove(item);
		}
	}

	/**
	 * 
	 */
	public void disableAlreadyAddedInToAdd()
	{
		ObservableList<String> items = listForCatIDsToAdd.getItems();// .getChildrenUnmodifiable();

		for (String item : items)
		{
			if (UIUtilityBox.recursiveDfs(treeView.getRoot(), item))
			{
				Callback<ListView<String>, ListCell<String>> listk = listForCatIDsToAdd.getCellFactory();
			}
		}
	}

	private EventHandler<ActionEvent> saveCurrentTree()
	{
		this.logMsg("Should save the currently displayed tree");

		TreeItem<String> rootNoteT = treeView.getRoot();
		this.logMsg("root node has " + treeView.getRoot().getChildren().size() + " children");
		// this.logMsg("root node has " + rootNode.getChildren().size() + " children");
		DefaultMutableTreeNode serializableRoot = UIUtilityBox.convertTreeItemsToTreeNodes(rootNoteT);

		String treeAsString = UIUtilityBox.treeToString(0, rootNoteT, new StringBuffer());
		String serialisableTreeAsString = UIUtilityBox.treeToString(0, serializableRoot, new StringBuffer());

		WritingToFile.writeToNewFile(treeAsString, commonPath + "TreeOfTreeItemsAsString.txt");
		WritingToFile.writeToNewFile(serialisableTreeAsString, commonPath + "TreeOfTreeNodesAsString.txt");

		Serializer.serializeThis(serializableRoot, commonPath + serializedCatTreeFileNamePhrase);

		PopUps.showMessage("Save event handling finished");
		return null;
	}

	public void branchExpanded(TreeItem.TreeModificationEvent<String> e)
	{
		String nodeValue = e.getSource().getValue();
		this.logMsg("Event: " + nodeValue + " expanded.");
	}

	public void branchCollapsed(TreeItem.TreeModificationEvent<String> e)
	{
		String nodeValue = e.getSource().getValue();
		this.logMsg("Event: " + nodeValue + " collapsed.");
	}

	public void childrenModification(TreeItem.TreeModificationEvent<String> e)
	{
		if (e.wasAdded())
		{
			for (TreeItem<String> item : e.getAddedChildren())
			{
				this.logMsg("Event: " + item.getValue() + " has been added.");
			}
		}

		if (e.wasRemoved())
		{
			for (TreeItem<String> item : e.getRemovedChildren())
			{
				this.logMsg("Event: " + item.getValue() + " has been removed.");
			}
		}
	}

	public void logMsg(String msg)
	{
		this.msgLogFld.appendText(msg + "\n");
	}

	// @Override
	// public void start(Stage primaryStage)
	// {
	// try
	// {
	//
	// // Select the root node
	// treeView.getSelectionModel().selectFirst();
	//
	// // // Create the root node and adds event handler to it
	// // TreeItem<String> depts = new TreeItem<>("Departments");
	// //
	// // depts.addEventHandler(TreeItem.<String>branchExpandedEvent(), this::branchExpanded);
	// // depts.addEventHandler(TreeItem.<String>branchCollapsedEvent(), this::branchCollapsed);
	// // depts.addEventHandler(TreeItem.<String>childrenModificationEvent(), this::childrenModification);
	//
	// ////////
	// String catHierarchyFileNameToRead =
	// "/run/media/gunjan/OS/Users/gunjan/Documents/UCD/Projects/Gowalla/link to Gowalla dataset/another
	// source/gowalla/gowalla_category_structure.json";
	//
	// Pair<TreeMap<Integer, TreeMap<Integer, TreeSet<Integer>>>, TreeItem<String>> pairedResult =
	// JSONProcessingGowallaTryingNonStatic.getThreeLevelCategoryHierarchyTreeFromJSON(catHierarchyFileNameToRead);
	// //////
	//
	// // Set the root node for the TreeViww
	// treeView.setRoot(pairedResult.getSecond());
	// VBox rightPane = getRightPane();
	//
	// HBox root = new HBox(treeView, rightPane);
	// root.setSpacing(20);
	// root.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;" +
	// "-fx-border-insets: 5;"
	// + "-fx-border-radius: 5;" + "-fx-border-color: blue;");
	//
	// Scene scene = new Scene(root);// , 350, 150);
	//
	// primaryStage.setTitle("Category Hierarchy");
	// primaryStage.setScene(scene);
	// primaryStage.show();
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// }

}
