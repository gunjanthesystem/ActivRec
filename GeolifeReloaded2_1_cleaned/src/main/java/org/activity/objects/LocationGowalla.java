package org.activity.objects;

import java.io.Serializable;

/**
 * Location objects for Gowalla checkins
 * 
 * @author gunjan
 *
 */
public class LocationGowalla extends LocationObject implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String locationID;
	int photos_count, checkins_count, users_count, radius_meters, highlights_count, items_count, max_items_count;// spot_categories;
	
	public LocationGowalla()
	{
		super();
	}
	
	// public LocationGowalla(double lat, double lon, String locName, String locCat, String city, String county, String country,
	// String continent)
	// {
	// super(lat, lon, locName, locCat, city, county, country, continent);
	// }
	
	/**
	 * 
	 * @param lat
	 * @param lon
	 * @param locName
	 * @param locCat
	 *            spotcategories
	 * @param city
	 * @param county
	 * @param country
	 * @param continent
	 * @param locationID
	 * @param photos_count
	 * @param checkins_count
	 * @param users_count
	 * @param radius_meters
	 * @param highlights_count
	 * @param items_count
	 * @param max_items_count
	 */
	public LocationGowalla(String lat, String lon, String locName, String locCat, String city, String county, String country,
			String continent, String locationID, int photos_count, int checkins_count, int users_count, int radius_meters,
			int highlights_count, int items_count, int max_items_count)
	{
		super(lat, lon, locName, locCat, city, county, country, continent);
		
		this.locationID = locationID;
		this.photos_count = photos_count;
		this.checkins_count = checkins_count;
		this.users_count = users_count;
		this.radius_meters = radius_meters;
		this.highlights_count = highlights_count;
		this.items_count = items_count;
		this.max_items_count = max_items_count;
	}
	
	public String getLocationID()
	{
		return locationID;
	}
	
	public int getPhotos_count()
	{
		return photos_count;
	}
	
	public int getCheckins_count()
	{
		return checkins_count;
	}
	
	public int getUsers_count()
	{
		return users_count;
	}
	
	public int getRadius_meters()
	{
		return radius_meters;
	}
	
	public int getHighlights_count()
	{
		return highlights_count;
	}
	
	public int getItems_count()
	{
		return items_count;
	}
	
	public int getMax_items_count()
	{
		return max_items_count;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + checkins_count;
		result = prime * result + highlights_count;
		result = prime * result + items_count;
		result = prime * result + ((locationID == null) ? 0 : locationID.hashCode());
		result = prime * result + max_items_count;
		result = prime * result + photos_count;
		result = prime * result + radius_meters;
		result = prime * result + users_count;
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LocationGowalla other = (LocationGowalla) obj;
		if (checkins_count != other.checkins_count)
			return false;
		if (highlights_count != other.highlights_count)
			return false;
		if (items_count != other.items_count)
			return false;
		if (locationID == null)
		{
			if (other.locationID != null)
				return false;
		}
		else if (!locationID.equals(other.locationID))
			return false;
		if (max_items_count != other.max_items_count)
			return false;
		if (photos_count != other.photos_count)
			return false;
		if (radius_meters != other.radius_meters)
			return false;
		if (users_count != other.users_count)
			return false;
		return true;
	}
	
}
