package org.activity.jmotif.sax.trie;

import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

/**
 * The convenient way to keep track of visited locations.
 * 
 * @author Pavel Senin.
 */
public class VisitRegistry
{

	private final TreeSet<Integer> unvisited;
	private final TreeSet<Integer> visited;
	private Random randomizer;
	private int capacity;

	/**
	 * Constructor.
	 * 
	 * @param capacity
	 *            The initial capacity.
	 */
	public VisitRegistry(int capacity)
	{
		this.capacity = capacity;
		this.unvisited = new TreeSet<Integer>();
		this.visited = new TreeSet<Integer>();
		for (int i = 0; i < capacity; i++)
		{
			this.unvisited.add(i);
		}
	}

	/**
	 * Mark as visited certain location.
	 * 
	 * @param i
	 *            The location to mark.
	 */
	public void markVisited(Integer i)
	{
		if (i < this.capacity && i >= 0 && this.isNotVisited(i))
		{
			this.visited.add(i);
			this.unvisited.remove(i);
		}
	}

	public void markVisited(int start, int end)
	{
		for (int i = start; i < end; i++)
		{
			markVisited(i);
		}
	}

	/**
	 * Get the next random unvisited position.
	 * 
	 * @return The next unvisited position.
	 */
	public int getNextRandomUnvisitedPosition()
	{
		if (this.unvisited.isEmpty())
		{
			return -1;
		}
		int size = this.unvisited.size();
		if (null == this.randomizer)
		{
			this.randomizer = new Random();
		}
		int item = this.randomizer.nextInt(size);
		int i = 0;
		Iterator<Integer> iter = this.unvisited.iterator();
		int res = iter.next();
		while (i < item)
		{
			res = iter.next();
			i++;
		}
		return res;
	}

	/**
	 * Check if position is not visited.
	 * 
	 * @param i
	 *            The index.
	 * @return true if not visited.
	 */
	public boolean isNotVisited(Integer i)
	{
		return this.unvisited.contains(i);
	}

	/**
	 * Check if position was visited.
	 * 
	 * @param i
	 *            The position.
	 * @return True if visited.
	 */
	public boolean isVisited(Integer i)
	{
		return this.visited.contains(i);
	}

	/**
	 * Get the list of unvisited positions.
	 * 
	 * @return list of unvisited positions.
	 */
	public TreeSet<Integer> getUnvisited()
	{
		return this.unvisited;
	}

	/**
	 * Get the list of visited positions.
	 * 
	 * @return list of visited positions.
	 */
	public TreeSet<Integer> getVisited()
	{
		return this.visited;
	}

	/**
	 * Transfers all visited entries to this registry.
	 * 
	 * @param discordRegistry
	 *            The discords registry to copy from.
	 */
	public void transferVisited(VisitRegistry discordRegistry)
	{
		for (Integer v : discordRegistry.getVisited())
		{
			this.markVisited(v);
		}
	}

}
