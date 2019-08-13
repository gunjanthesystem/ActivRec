package org.jmotif.sax.datastructures;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The discord records collection.
 * 
 * @author Pavel Senin
 * 
 */
public class DiscordRecords implements Iterable<DiscordRecord>
{

	/** Default capacity. */
	private static final Integer defaultCapacity = 10;

	/** Storage container. */
	private final LinkedList<DiscordRecord> discords;

	// max capacity var
	private Integer maxCapacity;

	/**
	 * Constructor.
	 */
	public DiscordRecords()
	{
		this.maxCapacity = defaultCapacity;
		this.discords = new LinkedList<DiscordRecord>();
	}

	/**
	 * Constructor.
	 * 
	 * @param capacity
	 *            The initial capacity.
	 */
	public DiscordRecords(int capacity)
	{
		this.maxCapacity = capacity;
		this.discords = new LinkedList<DiscordRecord>();
	}

	/**
	 * Set the max capacity of this collection. The overflow elements will be pushed into the collection pushing out
	 * ones with less distance value.
	 * 
	 * @param newSize
	 *            The size to set.
	 */
	public void setMaxCapacity(int newSize)
	{
		this.maxCapacity = newSize;
	}

	/**
	 * Add a new discord to the list. Here is a trick. This method will also check if the current distance is less than
	 * best so far (best in the table). If so - there is no need to continue that inner loop - the MAGIC optimization.
	 * 
	 * @param discord
	 *            The discord instance to add.
	 * @return if the discord got added.
	 */
	public boolean add(DiscordRecord discord)
	{

		// System.out.println(" + discord record " + discord);

		// check if here is still a room for the new element
		//
		if (this.discords.size() < this.maxCapacity)
		{
			this.discords.add(discord);
		}
		else
		{
			// more complicated - need to check if it will fit in there
			// DiscordRecord last = discords.get(discords.size() - 1);
			DiscordRecord first = discords.get(0);
			if (first.getDistance() < discord.getDistance())
			{
				// System.out.println(" - discord record " + discords.get(0));
				discords.remove(0);
				discords.add(discord);
			}
		}
		Collections.sort(discords);
		if (this.discords.get(0).compareTo(discord) > 0)
		{
			return true;
		}
		return false;
	}

	/**
	 * Returns the number of the top hits.
	 * 
	 * @param num
	 *            The number of instances to return. If the number larger than the storage size - returns the storage as
	 *            is.
	 * @return the top discord hits.
	 */
	public List<DiscordRecord> getTopHits(Integer num)
	{
		Collections.sort(discords);
		if (num >= this.discords.size())
		{
			return this.discords;
		}
		List<DiscordRecord> res = this.discords.subList(this.discords.size() - num, this.discords.size());
		Collections.reverse(res);
		return res;
	}

	/**
	 * Get the minimal distance found among all instances in the collection.
	 * 
	 * @return The minimal distance found among all instances in the collection.
	 */
	public double getMinDistance()
	{
		if (this.discords.size() > 0)
		{
			return discords.get(0).getDistance();
		}
		return -1;
	}

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer(1024);
		for (DiscordRecord r : discords)
		{
			sb.append("discord \"" + r.getPayload() + "\", at " + r.getPosition() + " distance to closest neighbor: "
					+ r.getDistance() + "\n");
		}
		return sb.toString();
	}

	@Override
	public Iterator<DiscordRecord> iterator()
	{
		return this.discords.iterator();
	}

	public int getSize()
	{
		return this.discords.size();
	}

	public double getWorstDistance()
	{
		if (this.discords.isEmpty())
		{
			return 0D;
		}
		double res = Double.MAX_VALUE;
		for (DiscordRecord r : discords)
		{
			if (r.getDistance() < res)
			{
				res = r.getDistance();
			}
		}
		return res;
	}

	public String toCoordinates()
	{
		StringBuffer sb = new StringBuffer();
		for (DiscordRecord r : discords)
		{
			sb.append(r.getPosition() + ",");
		}
		return sb.delete(sb.length() - 1, sb.length()).toString();
	}

	public String toPayloads()
	{
		StringBuffer sb = new StringBuffer();
		for (DiscordRecord r : discords)
		{
			sb.append("\"" + r.getPayload() + "\",");
		}
		return sb.delete(sb.length() - 1, sb.length()).toString();
	}

	public String toDistances()
	{
		NumberFormat nf = new DecimalFormat("##0.####");
		StringBuffer sb = new StringBuffer();
		for (DiscordRecord r : discords)
		{
			sb.append(nf.format(r.getDistance()) + ",");
		}
		return sb.delete(sb.length() - 1, sb.length()).toString();
	}
}
