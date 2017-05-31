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
public class BalancedIntegerTree
{
	DefaultMutableTreeNode root = null;

	private DefaultMutableTreeNode getRoot()
	{
		return root;
	}

	public BalancedIntegerTree()
	{
		root = new DefaultMutableTreeNode();
		root.setUserObject(-1);
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
	public void addToAllLeaves(Integer c)
	{
		this.getAllLeaves().stream().forEach(l -> l.add(new DefaultMutableTreeNode(c)));
	}

	// /**
	// *
	// * @param cArr
	// */
	// public void addToAllLeaves(Integer cArr[])
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
	// for (Integer c : cArr)
	// {
	// leaf.add(new DefaultMutableTreeNode(c));
	// }
	// }
	// }
	// }

	/**
	 * 
	 * @param cArr
	 */
	public void addToAllLeaves(ArrayList<Integer> cArr)
	{
		// System.out.println("Debug: addToAllLeaves(ArrayList<>) called");
		ArrayList<DefaultMutableTreeNode> allLeaves = this.getAllLeaves();
		for (DefaultMutableTreeNode leaf : allLeaves)
		{
			for (Integer i : cArr)
			{
				DefaultMutableTreeNode n = new DefaultMutableTreeNode();
				n.setUserObject(i);
				leaf.add(n);// new DefaultMutableTreeNode(i));
			}
			// cArr.stream().forEach(c -> leaf.add(new DefaultMutableTreeNode(c)));
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
			System.err.println(PopUps.getTracedErrorMsg(
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

	public ArrayList<ArrayList<Integer>> getAllWords()
	{
		ArrayList<DefaultMutableTreeNode> allLeaves = this.getAllLeaves();
		ArrayList<ArrayList<Integer>> allStrings = new ArrayList<ArrayList<Integer>>();
		int strlen = root.getDepth();

		for (DefaultMutableTreeNode leaf : allLeaves)
		{
			ArrayList<Integer> word = new ArrayList<>(strlen);

			Enumeration<DefaultMutableTreeNode> e = leaf.pathFromAncestorEnumeration(root);
			DefaultMutableTreeNode skipRoot = e.nextElement(); // skip the -1 root

			while (e.hasMoreElements())
			{
				DefaultMutableTreeNode temp = e.nextElement();
				// System.out.print("\nDebug: temp.getUserObject().toString()" + temp.getUserObject().toString());
				word.add((Integer) temp.getUserObject());// e.nextElement().toString()));// TODO can we improve this?
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

	/**
	 * Sample tree for sanity checks
	 *
	 * @return
	 */
	public static DefaultMutableTreeNode getSampleTree()
	{
		DefaultMutableTreeNode a = new DefaultMutableTreeNode(1);
		DefaultMutableTreeNode b = new DefaultMutableTreeNode(2);
		DefaultMutableTreeNode c = new DefaultMutableTreeNode(3);
		DefaultMutableTreeNode d = new DefaultMutableTreeNode(4);
		DefaultMutableTreeNode e = new DefaultMutableTreeNode(5);
		DefaultMutableTreeNode f = new DefaultMutableTreeNode(6);
		DefaultMutableTreeNode g = new DefaultMutableTreeNode(7);
		DefaultMutableTreeNode h = new DefaultMutableTreeNode(8);
		DefaultMutableTreeNode i = new DefaultMutableTreeNode(9);
		DefaultMutableTreeNode j = new DefaultMutableTreeNode(10);
		DefaultMutableTreeNode k = new DefaultMutableTreeNode(11);
		a.add(b);
		a.add(c);
		b.add(d);
		b.add(e);
		c.add(f);
		c.add(g);
		d.add(h);
		e.add(i);
		f.add(j);
		g.add(k);
		return a;
	}

	public static void main(String args[])
	{
		BalancedIntegerTree bt = new BalancedIntegerTree();
		// bt.setRoot(getSampleTree());
		ArrayList<DefaultMutableTreeNode> allLeaves;// = bt.getAllLeaves();
		// System.out.println("all leaves = " + allLeaves.toString());
		//
		ArrayList<ArrayList<Integer>> allStrings;// = bt.getAllStrings();
		// System.out.println("allStrings = " + allStrings.toString());

		ArrayList<Integer> cList0 = new ArrayList<>();
		cList0.add(99);
		bt.addToAllLeaves(cList0);

		// bt.addToAllLeaves(99);

		allLeaves = bt.getAllLeaves();
		System.out.println("all leaves = " + allLeaves.toString());

		allStrings = bt.getAllWords();
		System.out.println("allWords = " + allStrings.toString());

		System.out.println("----");

		ArrayList<Integer> cList = new ArrayList<>();
		cList.add(22);
		// cList.add(33);

		char[] cArr = new char[] { 'm', 'n' };

		bt.addToAllLeaves(cList);

		allLeaves = bt.getAllLeaves();
		System.out.println("all leaves = " + allLeaves.toString());

		allStrings = bt.getAllWords();
		System.out.println("allWords = " + allStrings.toString());

		System.out.println("----");

		// bt.recursiveDfs(bt.getRoot());
	}

}
