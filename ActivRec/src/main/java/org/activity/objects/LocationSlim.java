package org.activity.objects;

import java.io.Serializable;

public class LocationSlim implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double lat, lon;
	private long locID, actID;

	// /**
	// *
	// * @param lat
	// * @param lon
	// * @param locID
	// * @param actID
	// */
	// public LocationSlim(double lat, double lon, long locID, long actID)
	// {
	// super();
	// this.lat = lat;
	// this.lon = lon;
	// this.locID = locID;
	// this.actID = actID;
	// }

	/**
	 * 
	 * @param lat
	 * @param lon
	 * @param locID
	 * @param actID
	 */
	public LocationSlim(String lat, String lon, String locID, String actID)
	{
		super();
		this.lat = Double.parseDouble(lat);
		this.lon = Double.parseDouble(lon);
		this.locID = Long.parseLong(locID);
		this.actID = Long.parseLong(actID);
	}

	public double getLatitude()
	{
		return lat;
	}

	public double getLongitude()
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

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (actID ^ (actID >>> 32));
		long temp;
		temp = Double.doubleToLongBits(lat);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (locID ^ (locID >>> 32));
		temp = Double.doubleToLongBits(lon);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		LocationSlim other = (LocationSlim) obj;
		if (actID != other.actID) return false;
		if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat)) return false;
		if (locID != other.locID) return false;
		if (Double.doubleToLongBits(lon) != Double.doubleToLongBits(other.lon)) return false;
		return true;
	}

}
