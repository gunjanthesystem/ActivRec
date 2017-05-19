package org.activity.objects;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import org.activity.ui.PopUps;

/**
 * 
 * @author gunjan
 *
 */
public class BalancedStringTree
{
	DefaultMutableTreeNode root = null;

	private DefaultMutableTreeNode getRoot()
	{
		return root;
	}

	public BalancedStringTree()
	{
		// root = getSampleTree();
	}

	public int getNumOfStrings()
	{
		return root.getLeafCount();
	}

	public void setRoot(DefaultMutableTreeNode r)
	{
		this.root = r;
	}

	/**
	 * 
	 * @param c
	 */
	public void addToAllLeaves(char c)
	{
		if (root == null)
		{
			root = new DefaultMutableTreeNode(c);
		}
		else
		{
			this.getAllLeaves().stream().forEach(l -> l.add(new DefaultMutableTreeNode(c)));
		}
	}

	/**
	 * 
	 * @param cArr
	 */
	public void addToAllLeaves(char cArr[])
	{
		if (root == null)
		{
			root = new DefaultMutableTreeNode(cArr);
		}
		else
		{
			ArrayList<DefaultMutableTreeNode> allLeaves = this.getAllLeaves();
			for (DefaultMutableTreeNode leaf : allLeaves)
			{
				for (char c : cArr)
				{
					leaf.add(new DefaultMutableTreeNode(c));
				}
			}
		}
	}

	/**
	 * 
	 * @param cArr
	 */
	public void addToAllLeaves(ArrayList<String> cArr)
	{
		if (root == null)
		{
			root = new DefaultMutableTreeNode(cArr);
		}
		else
		{
			ArrayList<DefaultMutableTreeNode> allLeaves = this.getAllLeaves();
			for (DefaultMutableTreeNode leaf : allLeaves)
			{
				cArr.stream().forEach(c -> leaf.add(new DefaultMutableTreeNode(c)));
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	private ArrayList<DefaultMutableTreeNode> getAllLeaves()
	{
		ArrayList<DefaultMutableTreeNode> leaves = new ArrayList<DefaultMutableTreeNode>(root.getLeafCount());
		Enumeration enumeration = root.depthFirstEnumeration();
		while (enumeration.hasMoreElements())
		{
			DefaultMutableTreeNode t = (DefaultMutableTreeNode) enumeration.nextElement();
			// System.out.println("-" + t.toString());
			if (t.isLeaf())
			{
				leaves.add(t);
			}
		}
		if (root.getLeafCount() != leaves.size())
		{
			System.err.println(PopUps.getCurrentStackTracedErrorMsg(
					"root.getLeafCount()" + root.getLeafCount() + "!=leaves.size()" + leaves.size()));
		}
		return leaves;
	}

	// public ArrayList<String> getAllStrings()
	// {
	// ArrayList<DefaultMutableTreeNode> allLeaves = this.getAllLeaves();
	// ArrayList<String> allStrings = new ArrayList<>();
	// int strlen = root.getDepth();
	//
	// for (DefaultMutableTreeNode leaf : allLeaves)
	// {
	// StringBuilder sb = new StringBuilder(strlen);
	// Enumeration<DefaultMutableTreeNode> e = leaf.pathFromAncestorEnumeration(root);
	// while (e.hasMoreElements())
	// {
	// sb.append(e.nextElement().toString());
	// }
	// allStrings.add(sb.toString());
	// }
	//
	// return allStrings;
	// }

	public ArrayList<ArrayList<Character>> getAllStrings()
	{
		ArrayList<DefaultMutableTreeNode> allLeaves = this.getAllLeaves();
		ArrayList<ArrayList<Character>> allStrings = new ArrayList<ArrayList<Character>>();
		int strlen = root.getDepth();

		for (DefaultMutableTreeNode leaf : allLeaves)
		{
			ArrayList word = new ArrayList<>(strlen);

			Enumeration<DefaultMutableTreeNode> e = leaf.pathFromAncestorEnumeration(root);
			while (e.hasMoreElements())
			{
				word.add(e.nextElement().toString());
			}
			allStrings.add(word);
		}

		return allStrings;
	}

	// /**
	// *
	// * @param cArr
	// */
	// public void addToAllLeaves(ArrayList<Character> cArr)
	// {
	// if (root == null)
	// {
	// root = new DefaultMutableTreeNode(cArr);
	// }
	// else
	// {
	// ArrayList<DefaultMutableTreeNode> allLeaves = this.getAllLeaves();
	// for (DefaultMutableTreeNode leaf : allLeaves)
	// {
	// cArr.stream().forEach(c -> leaf.add(new DefaultMutableTreeNode(c)));
	// }
	// }
	// }

	// /**
	// * Sample tree for sanity checks
	// *
	// * @return
	// */
	// public static DefaultMutableTreeNode getSampleTree()
	// {
	// DefaultMutableTreeNode a = new DefaultMutableTreeNode('a');
	// DefaultMutableTreeNode b = new DefaultMutableTreeNode('b');
	// DefaultMutableTreeNode c = new DefaultMutableTreeNode('c');
	// DefaultMutableTreeNode d = new DefaultMutableTreeNode('d');
	// DefaultMutableTreeNode e = new DefaultMutableTreeNode('e');
	// DefaultMutableTreeNode f = new DefaultMutableTreeNode('f');
	// DefaultMutableTreeNode g = new DefaultMutableTreeNode('g');
	// DefaultMutableTreeNode h = new DefaultMutableTreeNode('h');
	// DefaultMutableTreeNode i = new DefaultMutableTreeNode('i');
	// DefaultMutableTreeNode j = new DefaultMutableTreeNode('j');
	// DefaultMutableTreeNode k = new DefaultMutableTreeNode('k');
	// a.add(b);
	// a.add(c);
	// b.add(d);
	// b.add(e);
	// c.add(f);
	// c.add(g);
	// d.add(h);
	// e.add(i);
	// f.add(j);
	// g.add(k);
	// return a;
	// }

	// public static void main(String args[])
	// {
	// BalancedStringTree bt = new BalancedStringTree();
	// // bt.setRoot(getSampleTree());
	// ArrayList<DefaultMutableTreeNode> allLeaves;// = bt.getAllLeaves();
	// // System.out.println("all leaves = " + allLeaves.toString());
	// //
	// ArrayList<String> allStrings;// = bt.getAllStrings();
	// // System.out.println("allStrings = " + allStrings.toString());
	//
	// bt.addToAllLeaves('z');
	//
	// allLeaves = bt.getAllLeaves();
	// System.out.println("all leaves = " + allLeaves.toString());
	//
	// allStrings = bt.getAllStrings();
	// System.out.println("allStrings = " + allStrings.toString());
	//
	// System.out.println("----");
	//
	// ArrayList<String> cList = new ArrayList<>();
	// cList.add("m");
	// cList.add("n");
	//
	// char[] cArr = new char[] { 'm', 'n' };
	//
	// bt.addToAllLeaves(cList);
	//
	// allLeaves = bt.getAllLeaves();
	// System.out.println("all leaves = " + allLeaves.toString());
	//
	// allStrings = bt.getAllStrings();
	// System.out.println("allStrings = " + allStrings.toString());
	//
	// System.out.println("----");
	//
	// // bt.recursiveDfs(bt.getRoot());
	// }

}
