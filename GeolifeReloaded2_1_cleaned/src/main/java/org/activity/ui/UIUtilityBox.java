package org.activity.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.tree.DefaultMutableTreeNode;

import org.activity.io.Serializer;
import org.activity.io.WritingToFile;
import org.activity.tools.JSONProcessingGowallaCatHierachy;
import org.activity.util.RegexUtils;

import javafx.scene.control.TreeItem;

/**
 * 
 * @author gunjan
 *
 */
public class UIUtilityBox
{

	public static void main(String args[])
	{
		// Path treeAsStringFile = Paths.get("/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep6/TreeAsString.txt");
		try
		{
			// $$checkNodesAtGivenDepth(1);
			// checkNodesAtGivenDepth(2);
			// checkNodesAtGivenDepth(3);
			// $$checkConversionOfTreeItemToTreeNode();
			// checkTreeSearch();
			checkTreeSearch_1();
			// Set<String> s = new HashSet<>();
			// s.add("gunjan");
			// s.add("supertramp");
			// s.add("synergy");
			// s.add("monaco");
			// System.out.println(s.stream().reduce((t, u) -> t + "__" + u).get());

			// $$serialiseCatIDNameDictionary();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param depth
	 */
	public static void checkNodesAtGivenDepth(int depth)
	{
		// int depth = 3;
		String categoryHierarchyTreeFileName = org.activity.generator.DatabaseCreatorGowallaQuicker0.categoryHierarchyTreeFileName;
		DefaultMutableTreeNode rootOfCategoryTree = (DefaultMutableTreeNode) Serializer
				.deSerializeThis(categoryHierarchyTreeFileName);

		LinkedHashSet<String> res = getNodesAtGivenDepth(depth, rootOfCategoryTree);

		System.out.println("num of nodes at depth " + depth + " are: " + res.size());

		// for (String s : res)
		// {
		// System.out.println(s.toString());
		// }
	}

	/**
	 * 
	 * @param givenDepth
	 * @param rootOfCategoryTree
	 * @return set of cat ids at level depth
	 */
	public static LinkedHashSet<String> getNodesAtGivenDepth(int givenDepth, DefaultMutableTreeNode rootOfCategoryTree)
	{
		LinkedHashSet<String> res = new LinkedHashSet<String>();

		String serialisableTreeAsString = treeToString(0, rootOfCategoryTree, new StringBuffer());
		String serialisableTreeAsStringNoTabs = serialisableTreeAsString.replaceAll("\t", "");

		String[] splitted = serialisableTreeAsStringNoTabs.split("\n");

		for (String s : splitted)
		{
			if (s.contains("depth" + givenDepth) == true)
			{
				String[] splittedLine = RegexUtils.patternColon.split(s);// s.split(":");
				res.add(splittedLine[1].trim());// [2] is cat name
			}
		}

		return res;
	}

	/**
	 * Check sanity of tree search
	 */
	public static void checkTreeSearch_1()
	{
		final String commonPath = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb20/TreeSanity/";
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov29/TreeSanity/";//
		// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep15CatTree/";
		DefaultMutableTreeNode rootOfCategoryTree = (DefaultMutableTreeNode) Serializer
				.deSerializeThis("./dataToRead/Nov22/RootOfCategoryTree24Nov2016.DMTreeNode");//
		// "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/RootOfCategoryTree24Nov2016.DMTreeNode");
		// "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep9_2/RootOfCategoryTree9Sep2016.DMTreeNode");

		String serialisableTreeAsString = treeToString(0, rootOfCategoryTree, new StringBuffer());

		String serialisableTreeAsStringNoTabs = serialisableTreeAsString.replaceAll("\t", "");

		WritingToFile.writeToNewFile(serialisableTreeAsString, commonPath + "TreeOfTreeNodesAsString.txt");
		WritingToFile.writeToNewFile(serialisableTreeAsStringNoTabs, commonPath + "TreeOfTreeNodesAsStringNoTabs.txt");

		// recursiveDfs(rootOfCategoryTree, "195:Terrain Park", 0);
		// $$recursiveDfsMultipleOccurences(rootOfCategoryTree, "912:Snow Cones");
		ArrayList<DefaultMutableTreeNode> foundNodes = recursiveDfsMulipleOccurences2(rootOfCategoryTree,
				"912:Snow Cones", new ArrayList<DefaultMutableTreeNode>());

		System.out.println("----ajooba------------");
		System.out.println("num of matching nodes found = " + foundNodes.size());

		for (DefaultMutableTreeNode foundnode : foundNodes)
		{
			System.out.println("Foundnode = " + foundnode.toString());
			System.out.println("Foundnode depth = " + foundnode.getLevel());
			System.out.println("Foundnode path = " + Arrays.toString(foundnode.getPath()));
		}

		String catIDToSearch = "201";// "912";
		ArrayList<DefaultMutableTreeNode> foundNodes2 = recursiveDfsMulipleOccurences2OnlyCatID(rootOfCategoryTree,
				catIDToSearch, new ArrayList<DefaultMutableTreeNode>());

		System.out.println("----ajooba------------");
		System.out.println("num of matching nodes found = " + foundNodes2.size());

		for (DefaultMutableTreeNode foundnode : foundNodes2)
		{
			System.out.println("Foundnode = " + foundnode.toString());
			System.out.println("Foundnode depth = " + foundnode.getLevel());
			System.out.println("Foundnode path = " + Arrays.toString(foundnode.getPath()));
		}

		System.out.println("\n\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		int workingLevel = 1;
		Set<String> givenLevelOrAboveCatIDs = getGivenLevelOrAboveCatID(catIDToSearch, rootOfCategoryTree,
				workingLevel);
		for (String catID : givenLevelOrAboveCatIDs)
		{
			System.out.println("level " + workingLevel + " or above catid = " + catID);
		}
	}

	/**
	 * Return a map containing (catid, list of nodes from hierarchy tree which contains that cat id)
	 * <p>
	 * created to improve performance by reducing redundant computations
	 * <p>
	 * if a catid exists in cat id name dictionary but does not exists in hierrchy tree then the list if of size o.
	 * 
	 * @return
	 */
	public static LinkedHashMap<String, ArrayList<DefaultMutableTreeNode>> getCatIDsFoundNodesMap(
			DefaultMutableTreeNode rootOfCategoryTree, TreeMap<Integer, String> catIDNameDictionary)
	{
		LinkedHashMap<String, ArrayList<DefaultMutableTreeNode>> catIdFoundNodesMap = new LinkedHashMap<String, ArrayList<DefaultMutableTreeNode>>();

		for (Integer catIDToSearch : catIDNameDictionary.keySet())
		{
			ArrayList<DefaultMutableTreeNode> foundNodes = recursiveDfsMulipleOccurences2OnlyCatID(rootOfCategoryTree,
					String.valueOf(catIDToSearch), new ArrayList<DefaultMutableTreeNode>());

			catIdFoundNodesMap.put(String.valueOf(catIDToSearch), foundNodes);
		}
		return catIdFoundNodesMap;
	}

	/**
	 * 
	 * @param catIDToSearh
	 * @param rootOfCategoryTree
	 * @param level
	 * @return
	 */
	public static Set<String> getGivenLevelOrAboveCatID(String catIDToSearh, DefaultMutableTreeNode rootOfCategoryTree,
			int level)
	{
		ArrayList<DefaultMutableTreeNode> foundNodes2 = recursiveDfsMulipleOccurences2OnlyCatID(rootOfCategoryTree,
				catIDToSearh, new ArrayList<DefaultMutableTreeNode>());

		Set<String> givenLevelOrAboveCatIDs = new HashSet<>();

		// System.out.println("num of matching nodes found = " + foundNodes2.size());

		for (DefaultMutableTreeNode foundnode : foundNodes2)
		{
			if (foundnode.getLevel() <= level)
			{
				String splitted[] = RegexUtils.patternColon.split(foundnode.toString());// foundnode.toString().split(":");
				givenLevelOrAboveCatIDs.add(splitted[0]);
			}

			else
			{
				// if node was found in hierarchy but at a level higher than desired level, then go to lower levels.
				while (foundnode.getLevel() > level && foundnode != null)
				{
					// foundnode = (DefaultMutableTreeNode) foundnode.getParent(); // will it only work for 3 levels.
					DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) foundnode.getParent();
					while (parentNode.getLevel() > level)
					{
						parentNode = (DefaultMutableTreeNode) parentNode.getParent();
					}
					foundnode = parentNode;
				}
				String splitted[] = RegexUtils.patternColon.split(foundnode.toString());// foundnode.toString().split(":");
				givenLevelOrAboveCatIDs.add(splitted[0]);
			}
			// System.out.println("Foundnode = " + foundnode.toString());
			// System.out.println("Foundnode depth = " + foundnode.getLevel());
			// System.out.println("Foundnode path = " + Arrays.toString(foundnode.getPath()));
		}
		if (givenLevelOrAboveCatIDs.size() == 0)
		{
			if (foundNodes2.size() == 0)
			{
				System.err.println("Warning:" + catIDToSearh + " has not given level " + level
						+ " or above node because it was not in category hierarhcy tree");
			}
			else
			{
				System.err.println("Error:" + catIDToSearh + " has not given level " + level
						+ " or above node though it is in category hierarhcy tree");
			}
		}
		return givenLevelOrAboveCatIDs;
	}

	public static void checkConversionOfTreeItemToTreeNode()
	{
		// Path treeAsStringFile = Paths.get("/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep6/TreeAsString.txt");

		try
		{
			// stringToTree(Files.readAllLines(treeAsStringFile));
			final String commonPath = "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep9/";

			String checkinFileNameToRead = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/RSubsettedData/gw2CheckinsSpots1TargetUsersDatesOnly2Feb2017.csv";
			// $$"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/gw2CheckinsSpots1TargetUsersDatesOnlyNoDup.csv";

			JSONProcessingGowallaCatHierachy preProcessGowalla = new JSONProcessingGowallaCatHierachy(commonPath,
					checkinFileNameToRead);

			TreeItem<String> rootOfTree = preProcessGowalla.getRootOfCategoryHierarchyTree();
			DefaultMutableTreeNode rootOfSerializableTree = convertTreeItemsToTreeNodes(rootOfTree);

			String treeAsString = treeToString(0, rootOfTree, new StringBuffer());
			String serialisableTreeAsString = treeToString(0, rootOfSerializableTree, new StringBuffer());

			WritingToFile.writeToNewFile(treeAsString, commonPath + "TreeOfTreeItemsAsString.txt");
			WritingToFile.writeToNewFile(serialisableTreeAsString, commonPath + "TreeOfTreeNodesAsString.txt");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public static void serialiseCatIDNameDictionary()
	{
		try
		{
			// stringToTree(Files.readAllLines(treeAsStringFile));
			String checkinFileNameToRead = "/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Feb2/RSubsettedData/gw2CheckinsSpots1TargetUsersDatesOnly2Feb2017.csv";
			// $$"/home/gunjan/Documents/UCD/Projects/Gowalla/GowallaDataWorks/Nov22/gw2CheckinsSpots1TargetUsersDatesOnlyNoDup.csv";

			final String commonPath = "/run/media/gunjan/BoX2/GowallaSpaceSpace/Sep16DatabaseGenerationJava/";
			JSONProcessingGowallaCatHierachy preProcessGowalla = new JSONProcessingGowallaCatHierachy(commonPath,
					checkinFileNameToRead);

			TreeMap<Integer, String> dict = preProcessGowalla.getCatIDNameDictionary();

			Serializer.kryoSerializeThis(dict, commonPath + "CatIDNameDictionary2.kryo");

			// WritingToFile.writeToNewFile(dict.entr, commonPath + "TreeOfTreeItemsAsString.txt");

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Performs a depth-first search, starting from the given node, for any node that satisfies the condition
	 * isGoal(node), and prints the path from the goal back to the root.
	 * <p>
	 * ref:https://www.cis.upenn.edu/~matuszek/cit594-2003/Examples/TreeTraversals/TreeTraversals.java
	 * </p>
	 * 
	 * @param root
	 *            The root of the tree or subtree to be searched.
	 * @return True if a goal node could be found.
	 */
	public static boolean recursiveDfs(TreeItem<String> node, String valueToSearch)
	{
		if (node.getValue().toString().equals(valueToSearch))// isGoal(node))
		{
			// $$ System.out.println("Found goal node: " + node.getValue());
			return true;
		}

		for (TreeItem<String> childNode : node.getChildren())
		{
			if (recursiveDfs(childNode, valueToSearch))
			{
				// $$ System.out.println("Path contains node " + node.getValue());
				return true;
			}
		}
		return false;
	}

	/**
	 * Performs a depth-first search, starting from the given node, for any node that satisfies the condition
	 * isGoal(node), and prints the path from the goal back to the root.
	 * <p>
	 * ref:https://www.cis.upenn.edu/~matuszek/cit594-2003/Examples/TreeTraversals/TreeTraversals.java
	 * </p>
	 * 
	 * @param root
	 *            The root of the tree or subtree to be searched.
	 * @return True if a goal node could be found.
	 */
	public static boolean recursiveDfs(DefaultMutableTreeNode node, String valueToSearch, int depth)
	{
		// System.out.println("-- node.toString()= " + node.toString() + " valueToSearch= " + valueToSearch);
		if (node.toString().equals(valueToSearch))// isGoal(node))
		{
			System.out.println("Found goal node: " + node.toString() + " at depth= " + depth);
			return true;
		}

		depth += 1;
		for (int i = 0; i < node.getChildCount(); i++)
		{
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
			if (recursiveDfs(childNode, valueToSearch, depth))
			{
				System.out.println("->" + node.toString());
				return true;
			}
		}
		return false;
	}

	/**
	 * Use depth-first-search recursively to find if the valueToSearch is present in the tree with node as root. If
	 * value is found, print the level and path at which the goal node is found.
	 * 
	 * @param node
	 * @param valueToSearch
	 * @return
	 */
	public static boolean recursiveDfsMultipleOccurences(DefaultMutableTreeNode node, String valueToSearch)
	{
		if (node.toString().equals(valueToSearch))// isGoal(node))
		{
			System.out.println("Found goal node: " + node.toString());
			System.out.println("Level from treenode = " + node.getLevel());
			System.out.println("path = " + Arrays.toString(node.getPath()));
		}

		if (node.isLeaf() == false)
		{
			for (int i = 0; i < node.getChildCount(); i++)
			{
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
				recursiveDfsMultipleOccurences(childNode, valueToSearch);
			}
		}
		return false;
	}

	/**
	 * 
	 * @param node
	 * @param valueToSearch
	 * @param foundNodes
	 * @return
	 */
	public static ArrayList<DefaultMutableTreeNode> recursiveDfsMulipleOccurences2(DefaultMutableTreeNode node,
			String valueToSearch, ArrayList<DefaultMutableTreeNode> foundNodes)
	{
		if (node.toString().equals(valueToSearch))// isGoal(node))
		{
			// System.out.println("Found goal node: " + node.toString());
			// System.out.println("Level from treenode = " + node.getLevel());
			// System.out.println("path = " + Arrays.toString(node.getPath()));
			foundNodes.add(node);
		}

		if (node.isLeaf() == false)
		{
			for (int i = 0; i < node.getChildCount(); i++)
			{
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
				recursiveDfsMulipleOccurences2(childNode, valueToSearch, foundNodes);
			}
		}
		return foundNodes;
	}

	/**
	 * Use depth-first-search recursively to find if the valueToSearch is present in the tree with node as root.
	 * 
	 * @param node
	 * @param valueToSearch
	 * @param foundNodes
	 * @return
	 */
	public static ArrayList<DefaultMutableTreeNode> recursiveDfsMulipleOccurences2OnlyCatID(DefaultMutableTreeNode node,
			String valueToSearch, ArrayList<DefaultMutableTreeNode> foundNodes)
	{
		String splitted[] = RegexUtils.patternColon.split(node.toString());
		// node.toString().split(":");

		if (splitted[0].equals(valueToSearch))// isGoal(node))
		{
			// System.out.println("Found goal node: " + node.toString());
			// System.out.println("Level from treenode = " + node.getLevel());
			// System.out.println("path = " + Arrays.toString(node.getPath()));
			foundNodes.add(node);
		}

		if (node.isLeaf() == false)
		{
			for (int i = 0; i < node.getChildCount(); i++)
			{
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) node.getChildAt(i);
				recursiveDfsMulipleOccurences2OnlyCatID(childNode, valueToSearch, foundNodes);
			}
		}
		return foundNodes;
	}

	/**
	 * Convert a tree of javafx.scene.control.TreeItem(s) to a tree of javax.swing.tree.DefaultMutableTreeNode(s). A
	 * primary motivation for this could be that TreeNode is serializable (though not thread safe) while TreeItem is not
	 * serializable.
	 * <p>
	 * ref:https://stackoverflow.com/questions/16098362/how-to-deep-copy-a-tree
	 * 
	 * @param root
	 */
	public static DefaultMutableTreeNode convertTreeItemsToTreeNodes(TreeItem<String> oldNode)
	{
		// PopUps.showMessage("Inside convertTreeItemsToTreeNodes");

		if (oldNode == null)
		{
			return null;
		}

		// copy current node's data
		DefaultMutableTreeNode copiedNode = new DefaultMutableTreeNode(oldNode.getValue());

		// copy current node's children
		for (TreeItem<String> childNode : oldNode.getChildren())
		{
			copiedNode.add(convertTreeItemsToTreeNodes(childNode));
			// copiedNode.Children.Add(CopyTree(childNode));
		}
		return copiedNode;
	}

	/**
	 * 
	 * @param oldNode
	 * @return
	 */
	public static TreeItem<String> convertTreeNodesToTreeItems(DefaultMutableTreeNode oldNode)
	{
		if (oldNode == null)
		{
			return null;
		}

		// copy current node's data
		TreeItem<String> copiedNode = new TreeItem<>(oldNode.toString());

		// copy current node's children
		for (int i = 0; i < oldNode.getChildCount(); i++)
		// for (DefaultMutableTreeNode childNode : oldNode.getChildren())
		{
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) oldNode.getChildAt(i);
			copiedNode.getChildren().add(convertTreeNodesToTreeItems(childNode));
			// copiedNode.Children.Add(CopyTree(childNode));
		}
		return copiedNode;
	}

	/**
	 * Return a string representation of the tree of TreeItem nodes.
	 * <p>
	 * Each line represents a node as shown below.
	 * <p>
	 * depth0:TreeItem [ value: -1:root ]
	 * <p>
	 * \tdepth1:TreeItem [ value: 934:Community ]
	 * <p>
	 * \t\tdepth2:TreeItem [ value: 133:Campus Spot ]
	 * <p>
	 * \t\t\tdepth3:TreeItem [ value: 135:Administration ]
	 * <p>
	 * \t\t\tdepth3:TreeItem [ value: 138:Campus Commons ]
	 * <p>
	 * \t\t\tdepth3:TreeItem [ value: 153:Campus - Other ]
	 * 
	 * @param depth
	 * @param node
	 * @param str
	 * @return
	 */
	public static String treeToString(int depth, TreeItem<String> node, StringBuffer str)
	{
		System.out.println("Inside treeToString");

		if (node == null)
		{
			new Exception("Inside treeToString: the received node is null");
			return null;
		}

		else
		{
			System.out.println("node has " + node.getChildren().size() + " children");
		}

		for (int i = 0; i < depth; i++)
		{
			str.append("\t");
		}
		str.append("depth" + depth + ":" + node.toString() + "\n");

		if (node.isLeaf())
		{
			return str.toString();
		}

		depth += 1;// going down one level
		for (TreeItem<String> child : node.getChildren())
		{
			treeToString(depth, child, str);
		}

		return str.toString();
	}

	/**
	 * Return a string representation of the tree of TreeItem nodes.
	 * <p>
	 * Each line represents a node as shown below.
	 * <p>
	 * depth0: -1:root ]
	 * <p>
	 * 
	 * @param depth
	 * @param node
	 * @param str
	 * @return
	 */
	public static String treeToStringValue(int depth, TreeItem<String> node, StringBuffer str)
	{
		for (int i = 0; i < depth; i++)
		{
			str.append("\t");
		}
		str.append("depth" + depth + ":" + node.getValue() + "\n");

		if (node.isLeaf())
		{
			return str.toString();
		}

		depth += 1;// going down one level
		for (TreeItem<String> child : node.getChildren())
		{
			treeToString(depth, child, str);
		}

		return str.toString();
	}

	private static String treeToStringJSON(int depth, TreeItem<String> node, StringBuffer str)
	{

		return null;// str.toString();
	}

	/**
	 * <font color = red>INCOMPLETE</font>
	 * <p>
	 * Construct a tree of TreeItems from a file containing the string tree produced by UIUtilityBox.treeToString()
	 * <p>
	 * Expecting each line to represent a node as shown below.
	 * <p>
	 * depth0:TreeItem [ value: -1:root ]
	 * <p>
	 * \tdepth1:TreeItem [ value: 934:Community ]
	 * <p>
	 * \t\tdepth2:TreeItem [ value: 133:Campus Spot ]
	 * <p>
	 * \t\t\tdepth3:TreeItem [ value: 135:Administration ]
	 * <p>
	 * \t\t\tdepth3:TreeItem [ value: 138:Campus Commons ]
	 * <p>
	 * \t\t\tdepth3:TreeItem [ value: 153:Campus - Other ]
	 * 
	 * @param stringTree
	 * @return
	 */
	public static TreeItem stringToTree(List<String> stringTree)
	{
		System.out.println("Num of lines in the string tree " + stringTree.size());
		TreeItem<String> root = new TreeItem("-1:root"); // (catid:catName)

		int count = 0;
		String previousDepth = "";

		TreeItem currParentNode = root, previousParentNode = null;

		for (String currLine : stringTree)// int i = 0; i < lines.length; i++)
		{
			++count;
			TreeItem currParent;// = new TreeItem();

			String currLineSplitted[] = currLine.split("\\[");

			String tempValue0Splitted[] = currLineSplitted[0].split(":");
			String currDepth = tempValue0Splitted[0].trim(); // depth3

			String tempValue1Splitted[] = currLineSplitted[1].split(":"); // " value: 22:Historic Church ]"
			String catID = tempValue1Splitted[1].trim();
			String catName = tempValue1Splitted[2].trim();
			catName = catName.substring(0, catName.length() - 1).trim();// Historic Church
			String catIDNameNode = catID + ":" + catName;
			System.out.println(catIDNameNode);

			TreeItem<String> currentNode = new TreeItem(catIDNameNode);

			if (count == 0) // first line ,create root
			{
				root = currentNode;// root.setValue(catIDNameNode);
				currParentNode = root;
			}
			else
			{
				currParentNode.getChildren().add(currentNode);

				// if (currDepth.equals(previousDepth) == false)
				// {
				// previousParentNode = currParentNode;
				// }
				// else
				// {
				//
				// }

			}
			previousDepth = currDepth;

		}

		return null;
	}

	/**
	 * Return a string representation of the tree of DefaultMutableTreeNode nodes.
	 * 
	 * @param depth
	 * @param node
	 * @param str
	 * @return
	 */
	public static String treeToString(int depth, DefaultMutableTreeNode node, StringBuffer str)
	{
		// System.out.println("Inside treeToString");

		if (node == null)
		{
			new Exception("Inside treeToString: the received node is null");
			return null;
		}

		else
		{
			// $$ System.out.println("node has " + node.getChildCount() + " children");
		}

		for (int i = 0; i < depth; i++)
		{
			str.append("\t");
		}
		str.append("depth" + depth + ":" + node.toString() + "\n");

		if (node.isLeaf())
		{
			return str.toString();
		}

		depth += 1;// going down one level
		for (int i = 0; i < node.getChildCount(); i++)// DefaultMutableTreeNode child : node.getChildren())
		// for (DefaultMutableTreeNode child : node.getChildren())
		{
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
			treeToString(depth, child, str);
		}

		return str.toString();// + "\najooba DefaultMutableTreeNode";
	}

	///

	// /**
	// * Return a string representation of the tree of DefaultMutableTreeNode nodes.
	// *
	// * @param depth
	// * @param node
	// * @param str
	// * @return
	// */
	// public static LinkedHashSet<String> getNodesAtGivenDepth(int depth, int depthToSearch, DefaultMutableTreeNode
	// node,
	// LinkedHashSet<String> res)
	// {
	// System.out.println("Inside getNodesAtGivenDepth");
	//
	// if (node == null)
	// {
	// new Exception("Inside getNodesAtGivenDepth: the received node is null");
	// return null;
	// }
	//
	// else
	// {
	// // / System.out.println("node has " + node.getChildCount() + " children");
	// }
	//
	// if (node.getDepth() == depthToSearch)
	// {
	// res.add(node.toString());
	// }
	//
	// depth += 1;// going down one level
	// for (int i = 0; i < node.getChildCount(); i++)// DefaultMutableTreeNode child : node.getChildren())
	// // for (DefaultMutableTreeNode child : node.getChildren())
	// {
	// DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
	// getNodesAtGivenDepth(depth, depthToSearch, child, res);
	// }
	//
	// return res;// + "\najooba DefaultMutableTreeNode";
	// }
	//
	// public static LinkedHashSet<String> getNodesAtGivenDepth(int depthToSearch, DefaultMutableTreeNode node,
	// LinkedHashSet<String> res)
	// {
	// // System.out.println("Inside getNodesAtGivenDepth");
	//
	// if (node == null)
	// {
	// new Exception("Inside getNodesAtGivenDepth: the received node is null");
	// return null;
	// }
	//
	// // else
	// // {
	// // // System.out.println("node has " + node.getChildCount() + " children");
	// // }
	// System.out.print("Current node: " + node.toString() + " depth:" + node.getDepth());
	// if (node.getDepth() == depthToSearch)
	// {
	// res.add(node.toString());
	// System.out.print("adding");
	// }
	// System.out.println();
	//
	// // if (node.isLeaf())
	// // {
	// // return res;
	// // }
	//
	// // depth += 1;// going down one level
	// depthToSearch = node.getDepth() + 1;
	// for (int i = 0; i < node.getChildCount(); i++)// DefaultMutableTreeNode child : node.getChildren())
	// // for (DefaultMutableTreeNode child : node.getChildren())
	// {
	// DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
	// getNodesAtGivenDepth(depthToSearch, child, res);
	// }
	//
	// // System.out.println("Num of nodes at depth " + depth + " = " + res.size());
	// return res;// + "\najooba DefaultMutableTreeNode";
	// }
	//
	// /**
	// * // * Return a list of nodes (as string) as given level. // * // * @param depth // * @param node // * @param str
	// // * @return //
	// */
	// public static Set<String> getNodesAtGivenLevel(int depth, DefaultMutableTreeNode node, StringBuilder str)
	// {
	// System.out.println("Inside getNodesAtGivenLevel");
	// Set<String> res = new LinkedHashSet<String>();
	// boolean depthReached = false;
	// if (node == null)
	// {
	// new Exception("Inside getNodesAtGivenLevel: the received node is null");
	// return null;
	// }
	//
	// else
	// {
	// System.out.println("node has " + node.getChildCount() + " children");
	// }
	//
	// if (node.getDepth() == depth)
	// {
	// res.add(node.toString());
	// depthReached = true;
	// }
	// // str.append("depth" + depth + ":" + node.toString() + "\n");
	//
	// // while(depthReached==false)
	// // {
	// //
	// // }
	// if (node.isLeaf())
	// {
	// return res;
	// }
	//
	// depth += 1;// going down one level
	// for (int i = 0; i < node.getChildCount(); i++)// DefaultMutableTreeNode child : node.getChildren())
	// {
	// DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
	// treeToString(depth, child, str);
	// }
	//
	// return str.toString();// + "\najooba DefaultMutableTreeNode";
	// }

}
