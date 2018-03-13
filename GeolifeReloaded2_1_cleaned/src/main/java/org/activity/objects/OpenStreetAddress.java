package org.activity.objects;

public class OpenStreetAddress
{
	// cityOrTownOrVillageOrHamlet in order, i.e. if not city, then town and so on.
	String /* house_number, */ roadOrPedestrian, cityOrTownOrVillageOrHamlet, county, stateOrRegion, postcode, country,
			country_code;

	/**
	 * 
	 * @param road
	 * @param city
	 * @param county
	 * @param state
	 * @param postcode
	 * @param country
	 * @param country_code
	 */
	public OpenStreetAddress(String road, String city, String county, String state, String postcode, String country,
			String country_code)
	{
		super();
		// this.house_number = house_number;
		this.roadOrPedestrian = road;
		this.cityOrTownOrVillageOrHamlet = city;
		this.county = county;
		this.stateOrRegion = state;
		this.postcode = postcode;
		this.country = country;
		this.country_code = country_code;
	}

	public boolean isEmptyAddress()
	{
		return (new String(roadOrPedestrian + cityOrTownOrVillageOrHamlet + county + stateOrRegion + postcode + country
				+ country_code).trim().length() == 0);
	}

	@Override
	public String toString()
	{
		return roadOrPedestrian + ", " + cityOrTownOrVillageOrHamlet + ", " + county + ", " + stateOrRegion + ", "
				+ postcode + ", " + country + ", " + country_code;
	}

	public String toString(char delim)
	{
		return roadOrPedestrian + delim + cityOrTownOrVillageOrHamlet + delim + county + delim + stateOrRegion + delim
				+ postcode + delim + country + delim + country_code;
	}

	public String getRoad()
	{
		return roadOrPedestrian;
	}

	public String getCity()
	{
		return cityOrTownOrVillageOrHamlet;
	}

	public String getCounty()
	{
		return county;
	}

	public String getState()
	{
		return stateOrRegion;
	}

	public String getPostcode()
	{
		return postcode;
	}

	public String getCountry()
	{
		return country;
	}

	public String getCountry_code()
	{
		return country_code;
	}

}
