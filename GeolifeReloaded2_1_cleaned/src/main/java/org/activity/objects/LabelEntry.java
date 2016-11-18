package org.activity.objects;

import java.sql.Timestamp;

public class LabelEntry
{
	String mode;
	Timestamp startTimestamp, endTimestamp;
	
	public LabelEntry(Timestamp st, Timestamp et, String mod)
	{
		mode = mod;
		startTimestamp = st;
		endTimestamp = et;
	}
	
	/**
	 * Note: change to GMT String on April 11 2016
	 */
	public String toString()
	{
		return "(" + startTimestamp.toGMTString() + " " + endTimestamp.toGMTString() + " " + mode + ")";
	}
	
	public String toStringRaw()
	{
		return "(" + startTimestamp.getTime() + " " + endTimestamp.getTime() + " " + mode + ", ("
				+ (endTimestamp.getTime() - startTimestamp.getTime()) / 1000 + " secs difference) )";
	}
	
	public String getMode()
	{
		return mode;
	}
	
	public boolean contains(Timestamp t)
	{
		boolean result;
		
		result = ((this.startTimestamp.getTime() <= t.getTime()) && (this.endTimestamp.getTime() >= t.getTime()));
		
		// System.out.println("checking contains method");
		// System.out.println("Starttimestamp:"+startTimestamp+" Endtimestamp:"+endTimestamp+"  point to check:"+t+"\nresult="+result+"\n\n");
		
		return result;
	}
	
	public boolean doesOverlap(Timestamp startInterval, Timestamp endInterval)
	{
		return ((this.startTimestamp.getTime() <= endInterval.getTime()) && (this.endTimestamp.getTime() >= startInterval.getTime()));
	}
}
