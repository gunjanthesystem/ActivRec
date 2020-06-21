package org.activity.ui;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * Just for dummy experiments
 * 
 * @author gunjan
 *
 */
public class TreeViewUtil
{

	public static TreeView<String> getTreeView()
	{
		TreeItem<String> depts = new TreeItem<>("Departments");

		// Add items to depts
		TreeItem<String> isDept = new TreeItem<String>("Information Systems");
		TreeItem<String> claimsDept = new TreeItem<String>("Claims");
		TreeItem<String> underwritingDept = new TreeItem<String>("Underwriting");
		depts.getChildren().addAll(isDept, claimsDept, underwritingDept);

		// Add employees for each dept
		isDept.getChildren().addAll(new TreeItem<String>("Doug Dyer"), new TreeItem<String>("Jim Beeson"),
				new TreeItem<String>("Simon Ng"));
		claimsDept.getChildren().addAll(new TreeItem<String>("Lael Boyd"), new TreeItem<String>("Janet Biddle"));
		underwritingDept.getChildren().addAll(new TreeItem<String>("Ken McEwen"), new TreeItem<String>("Ken Mann"),
				new TreeItem<String>("Lola Ng"));

		// adding handlers

		// // Add BranchExpended event handler
		// depts.addEventHandler(TreeItem.<String>branchExpandedEvent(),
		// e -> System.out.println("Node expanded: " + e.getSource().getValue()));
		//
		// // Add BranchCollapsed event handler
		// depts.addEventHandler(TreeItem.<String>branchCollapsedEvent(),
		// e -> System.out.println("Node collapsed: " + e.getSource().getValue()));

		// Create a TreeView with depts as its root item
		TreeView<String> treeView = new TreeView<>(depts);

		return treeView;
	}
}
