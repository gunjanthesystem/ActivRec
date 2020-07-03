package org.activity.objects;

import java.io.Serializable;

public class OpenStreetAddress implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// cityOrTownOrVillageOrHamlet in order, i.e. if not city, then town, then village, then hamlet then locality then
	// city_districy then city-named-county then suburb
	String /* house_number, */ roadOrPedestrian, cityTownVillHamLocaCityDisCityNamedCountySuburb, county, stateOrRegion,
			postcode, country, country_code;

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
		this.cityTownVillHamLocaCityDisCityNamedCountySuburb = city;
		this.county = county;
		this.stateOrRegion = state;
		this.postcode = postcode;
		this.country = country;
		this.country_code = country_code;
	}

	public boolean isEmptyAddress()
	{
		return (new String(roadOrPedestrian + cityTownVillHamLocaCityDisCityNamedCountySuburb + county + stateOrRegion
				+ postcode + country + country_code).trim().length() == 0);
	}

	@Override
	public String toString()
	{
		return roadOrPedestrian + ", " + cityTownVillHamLocaCityDisCityNamedCountySuburb + ", " + county + ", "
				+ stateOrRegion + ", " + postcode + ", " + country + ", " + country_code;
	}

	public String toString(char delim)
	{
		return roadOrPedestrian + delim + cityTownVillHamLocaCityDisCityNamedCountySuburb + delim + county + delim
				+ stateOrRegion + delim + postcode + delim + country + delim + country_code;
	}

	public String getRoad()
	{
		return roadOrPedestrian;
	}

	public String getCity()
	{
		return cityTownVillHamLocaCityDisCityNamedCountySuburb;
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
