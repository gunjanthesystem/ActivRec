package org.activity.objects;

public class LocationObject
{
	int locationId;
	String latitude, longitude, altitude;
	public String locationName, locationCategory, city, county, country, continent;

	public String getLatitude()
	{
		return latitude;
	}

	public String getLongitude()
	{
		return longitude;
	}

	public static String getLocationName(int seed)
	{
		if (seed == -99)
		{
			return "dummy";
		}
		return "";
	}

	public void setAltitude(String alt)
	{
		this.altitude = alt;
	}

	public LocationObject(String lat, String lon, String locName, String locCat, String city, String county,
			String country, String continent)
	{
		latitude = lat;
		longitude = lon;
		locationName = locName;
		locationCategory = locCat;
		this.city = city;
		this.county = county;
		this.continent = continent;
	}

	public LocationObject()
	{

	}

	public int getLocationId()
	{
		return (this.locationId);
	}

	public void setAllLocationVariables(int locId, String lat, String lon, String locName, String locCat, String city,
			String county, String country, String continent)
	{
		this.locationId = locId;
		this.latitude = lat;
		this.longitude = lon;
		this.locationName = locName;
		this.locationCategory = locCat;
		this.city = city;
		this.county = county;
		this.country = country;
		this.continent = continent;
	}

	/**
	 * <p>
	 * Used to create synthetic/dummy location objects for first version of UI web application
	 * </p>
	 * <p>
	 * case 0: userName= "Tessa"; break; case 1: userName= "Yakub"; break; case 2: userName= "Rahul"; break; case 3:
	 * userName= "Christopher"; break; case 4: userName= "Maggi"; break; case 5: userName= "Liu"; break; case 6:
	 * userName= "Andrew"; break; default: userName= "Nefertiti"; break;
	 * </p>
	 * 
	 * @param userId
	 * @param seed
	 * @return
	 */
	public static LocationObject getSyntheticLocationObjectInstance(int userId, int seed) // seed is the activityId
	{
		LocationObject locationObject = new LocationObject();

		if (seed == 99) // other activity
		{
			locationObject.setAllLocationVariables(0, "0", "0", "", "", "", "", "", "");

		}

		else if (userId == 0)
		{
			switch (seed % 4)
			{
			case (0):
				locationObject.setAllLocationVariables(1, "50.87", "4.72", "KU Leuven", "Work Place", "Leuven",
						"Flemish Brabant", "Belgium", "Europe");
				break;
			case (1):
				locationObject.setAllLocationVariables(2, "50.87", "4.69", "Oude Markt", "Leisure Place", "Leuven",
						"Flemish Brabant", "Belgium", "Europe");
				break;
			case (2):
				locationObject.setAllLocationVariables(4, "50.88", "4.7", "Mathieu de Layensplein", "Home", "Leuven",
						"Flemish Brabant", "Belgium", "Europe");
				break;
			default:
				locationObject.setAllLocationVariables(5, "50.85", "4.35", "Brussels City Centre", "City Area",
						"Brussels", "Brussels Capital", "Belgium", "Europe");
				break;
			}
		}
		else if (userId == 1)
		{
			switch (seed % 4)
			{
			case (0):
				locationObject.setAllLocationVariables(6, "53.43", "-2.96", "Liverpool Football Club", "Work Place",
						"Liverpool", "Merseyside", "United Kingdom", "Europe");
				break;
			case (1):
				locationObject.setAllLocationVariables(7, "53.40", "-2.99", "Thomas Rigbys", "Leisure Place",
						"Liverpool", "Merseyside", "United Kingdom", "Europe");
				break;
			case (2):
				locationObject.setAllLocationVariables(8, "53.41", "-2.98", "Limekiln Ln", "Home", "Liverpool",
						"Merseyside", "United Kingdom", "Europe");
				break;
			default:
				locationObject.setAllLocationVariables(9, "53.40", "-2.98", "Saint Johns Shopping Centre", "City Area",
						"Liverpool", "Merseyside", "United Kingdom", "Europe");
				break;
			}
		}

		else if (userId == 2)
		{
			switch (seed % 4)
			{
			case (0):
				locationObject.setAllLocationVariables(10, "28.66", "77.22", "Ernst and Young", "Work Place",
						"New Delhi", "Captial Territory", "India", "Asia");
				break;
			case (1):
				locationObject.setAllLocationVariables(11, "28.63", "77.21", "Connaught Place", "Leisure Place",
						"New Delhi", "Captial Territory", "India", "Asia");
				break;
			case (2):
				locationObject.setAllLocationVariables(12, "28.57", "77.16", "Vasant Vihar", "Home", "New Delhi",
						"Captial Territory", "India", "Asia");
				break;
			default:
				locationObject.setAllLocationVariables(13, "28.65", "77.22", "Chandni Chowk", "City Area", "New Delhi",
						"Captial Territory", "India", "Asia");
				break;
			}
		}

		else if (userId == 3)
		{
			switch (seed % 4)
			{
			case (0):
				locationObject.setAllLocationVariables(14, "51.89", "-8.49", "UCC", "Work Place", "Cork", "Cork",
						"Ireland", "Europe");
				break;
			case (1):
				locationObject.setAllLocationVariables(15, "51.9", "-8.48", "The Franciscan Well", "Leisure Place",
						"Cork", "Cork", "Ireland", "Europe");
				break;
			case (2):
				locationObject.setAllLocationVariables(16, "51.9", "-8.49", "Rathanny", "Home", "Cork", "Cork",
						"Ireland", "Europe");
				break;
			default:
				locationObject.setAllLocationVariables(17, "51.88", "-8.51", "Wilton Shopping Centre", "City Area",
						"Cork", "Cork", "Ireland", "Europe");
				break;
			}
		}

		else if (userId == 4)
		{
			switch (seed % 4)
			{
			case (0):
				locationObject.setAllLocationVariables(18, "53.3", "-6.2", "DCU", "Work", "Dublin", "Dublin", "Ireland",
						"Europe");
				break;
			case (1):
				locationObject.setAllLocationVariables(19, "53.34", "-6.26", "Temple Bar", "Party", "Dublin", "Dublin",
						"Ireland", "Europe");
				break;
			case (2):
				locationObject.setAllLocationVariables(20, "53.34", "-6.17", "Blackrock", "Home", "Dublin", "Dublin",
						"Ireland", "Europe");
				break;
			default:
				locationObject.setAllLocationVariables(21, "51.89", "-8.47", "Grafton Street", "Shopping", "Dublin",
						"Dublin", "Ireland", "Europe");
				break;
			}
		}

		else if (userId == 5)
		{
			switch (seed % 4)
			{
			case (0):
				locationObject.setAllLocationVariables(22, "53.3", "-6.2", "UCD", "Work", "Dublin", "Dublin", "Ireland",
						"Europe");
				break;
			case (1):
				locationObject.setAllLocationVariables(23, "53.34", "-6.26", "Temple Bar", "Party", "Dublin", "Dublin",
						"Ireland", "Europe");
				break;
			case (2):
				locationObject.setAllLocationVariables(24, "53.34", "-6.17", "Blackrock", "Home", "Dublin", "Dublin",
						"Ireland", "Europe");
				break;
			default:
				locationObject.setAllLocationVariables(25, "51.89", "-8.47", "Grafton Street", "Shopping", "Dublin",
						"Dublin", "Ireland", "Europe");
				break;
			}
		}

		else if (userId == 6)
		{
			switch (seed % 4)
			{
			case (0):
				locationObject.setAllLocationVariables(26, "53.3", "-6.2", "UCD", "Work", "Dublin", "Dublin", "Ireland",
						"Europe");
				break;
			case (1):
				locationObject.setAllLocationVariables(27, "53.34", "-6.26", "Temple Bar", "Party", "Dublin", "Dublin",
						"Ireland", "Europe");
				break;
			case (2):
				locationObject.setAllLocationVariables(28, "53.34", "-6.17", "Blackrock", "Home", "Dublin", "Dublin",
						"Ireland", "Europe");
				break;
			default:
				locationObject.setAllLocationVariables(29, "51.89", "-8.47", "Grafton Street", "Shopping", "Dublin",
						"Dublin", "Ireland", "Europe");
				break;
			}
		}
		return locationObject;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((altitude == null) ? 0 : altitude.hashCode());
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((continent == null) ? 0 : continent.hashCode());
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((county == null) ? 0 : county.hashCode());
		result = prime * result + ((latitude == null) ? 0 : latitude.hashCode());
		result = prime * result + ((locationCategory == null) ? 0 : locationCategory.hashCode());
		result = prime * result + locationId;
		result = prime * result + ((locationName == null) ? 0 : locationName.hashCode());
		result = prime * result + ((longitude == null) ? 0 : longitude.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		LocationObject other = (LocationObject) obj;
		if (altitude == null)
		{
			if (other.altitude != null) return false;
		}
		else if (!altitude.equals(other.altitude)) return false;
		if (city == null)
		{
			if (other.city != null) return false;
		}
		else if (!city.equals(other.city)) return false;
		if (continent == null)
		{
			if (other.continent != null) return false;
		}
		else if (!continent.equals(other.continent)) return false;
		if (country == null)
		{
			if (other.country != null) return false;
		}
		else if (!country.equals(other.country)) return false;
		if (county == null)
		{
			if (other.county != null) return false;
		}
		else if (!county.equals(other.county)) return false;
		if (latitude == null)
		{
			if (other.latitude != null) return false;
		}
		else if (!latitude.equals(other.latitude)) return false;
		if (locationCategory == null)
		{
			if (other.locationCategory != null) return false;
		}
		else if (!locationCategory.equals(other.locationCategory)) return false;
		if (locationId != other.locationId) return false;
		if (locationName == null)
		{
			if (other.locationName != null) return false;
		}
		else if (!locationName.equals(other.locationName)) return false;
		if (longitude == null)
		{
			if (other.longitude != null) return false;
		}
		else if (!longitude.equals(other.longitude)) return false;
		return true;
	}

}
