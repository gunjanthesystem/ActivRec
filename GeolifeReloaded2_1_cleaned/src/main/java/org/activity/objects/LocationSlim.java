package org.activity.objects;

public class LocationSlim
{
	private double lat, lon;
	private long locID, actID;

	public LocationSlim(double lat, double lon, long locID, long actID)
	{
		super();
		this.lat = lat;
		this.lon = lon;
		this.locID = locID;
		this.actID = actID;
	}

	public double getLat()
	{
		return lat;
	}

	public double getLon()
	{
		return lon;
	}

	public long getLocID()
	{
		return locID;
	}

	public long getActID()
	{
		return actID;
	}

}
