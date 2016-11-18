package org.activity.clustering;

import org.activity.objects.Timeline;

public class DataPoint
{
	String label;
	Object dataValue;
	
	public boolean equals(Object otherObject)
	{
		DataPoint other = (DataPoint) otherObject;
		return ((this.label.equals(other.getLabel())) && (this.dataValue.equals(other.getDataValue())));
	}
	
	public DataPoint(String s, Object o)
	{
		this.label = s;
		dataValue = o;
	}
	
	public Timeline toTimeline()
	{
		return (Timeline) dataValue;// new Timeline(Integer.valueOf(label), dataValue);
	}
	
	public String toString()
	
	{
		return (dataValue.toString());
	}
	
	public String getLabel()
	{
		return label;
	}
	
	public void setLabel(String label)
	{
		this.label = label;
	}
	
	public Object getDataValue()
	{
		return dataValue;
	}
	
	public void setDataValue(Object dataValue)
	{
		this.dataValue = dataValue;
	}
	
	public int compareTo(DataPoint dp)
	{
		return Integer.valueOf(this.getLabel()).compareTo(Integer.valueOf(dp.getLabel()));
	}
	
}
