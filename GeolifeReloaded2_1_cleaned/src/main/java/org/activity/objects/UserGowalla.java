package org.activity.objects;

import java.io.Serializable;

/**
 * 
 * @author gunjan
 *
 */
public class UserGowalla extends UserObject implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int bookmarked_spots_count, challenge_pin_count, country_pin_count, highlights_count, items_count, photos_count,
			pins_count, province_pin_count, region_pin_count, state_pin_count, trips_count, friends_count, stamps_count,
			checkin_num, places_num;

	/**
	 * 
	 * @param userID
	 * @param userName
	 * @param personalityTags
	 * @param profession
	 * @param ageCategory
	 * @param userAge
	 * @param bookmarked_spots_count
	 * @param challenge_pin_count
	 * @param country_pin_count
	 * @param highlights_count
	 * @param items_count
	 * @param photos_count
	 * @param pins_count
	 * @param province_pin_count
	 * @param region_pin_count
	 * @param state_pin_count
	 * @param trips_count
	 * @param friends_count
	 * @param stamps_count
	 * @param checkin_num
	 * @param places_num
	 */
	public UserGowalla(String userID, String userName, String personalityTags, String profession, String ageCategory,
			int userAge, int bookmarked_spots_count, int challenge_pin_count, int country_pin_count,
			int highlights_count, int items_count, int photos_count, int pins_count, int province_pin_count,
			int region_pin_count, int state_pin_count, int trips_count, int friends_count, int stamps_count,
			int checkin_num, int places_num)
	{
		super(userID, userName, personalityTags, profession, ageCategory, userAge);
		this.bookmarked_spots_count = bookmarked_spots_count;
		this.challenge_pin_count = challenge_pin_count;
		this.country_pin_count = country_pin_count;
		this.highlights_count = highlights_count;
		this.items_count = items_count;
		this.photos_count = photos_count;
		this.pins_count = pins_count;
		this.province_pin_count = province_pin_count;
		this.region_pin_count = region_pin_count;
		this.state_pin_count = state_pin_count;
		this.trips_count = trips_count;
		this.friends_count = friends_count;
		this.stamps_count = stamps_count;
		this.checkin_num = checkin_num;
		this.places_num = places_num;
	}

	public UserGowalla(String userID)
	{
		super(userID);
	}

	public UserGowalla()
	{
		super();
	}

	@Override
	public String toString()
	{
		return userID + "," + bookmarked_spots_count + ", " + challenge_pin_count + ", " + country_pin_count + ", "
				+ highlights_count + ", " + items_count + ", " + photos_count + ", " + pins_count + ", "
				+ province_pin_count + ", " + region_pin_count + ", " + state_pin_count + ", " + trips_count + ", "
				+ friends_count + ", " + stamps_count + ", " + checkin_num + ", " + places_num;
	}

	public static String toStringHeader()
	{
		return "userID,bookmarked_spots_count , challenge_pin_count , country_pin_count , highlights_count, items_count , photos_count , pins_count , province_pin_count , region_pin_count , state_pin_count , trips_count , friends_count , stamps_count , checkin_num , places_num";
	}

	public int getBookmarked_spots_count()
	{
		return bookmarked_spots_count;
	}

	public int getChallenge_pin_count()
	{
		return challenge_pin_count;
	}

	public int getCountry_pin_count()
	{
		return country_pin_count;
	}

	public int getHighlights_count()
	{
		return highlights_count;
	}

	public int getItems_count()
	{
		return items_count;
	}

	public int getPhotos_count()
	{
		return photos_count;
	}

	public int getPins_count()
	{
		return pins_count;
	}

	public int getProvince_pin_count()
	{
		return province_pin_count;
	}

	public int getRegion_pin_count()
	{
		return region_pin_count;
	}

	public int getState_pin_count()
	{
		return state_pin_count;
	}

	public int getTrips_count()
	{
		return trips_count;
	}

	public int getFriends_count()
	{
		return friends_count;
	}

	public int getStamps_count()
	{
		return stamps_count;
	}

	public int getCheckin_num()
	{
		return checkin_num;
	}

	public int getPlaces_num()
	{
		return places_num;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + bookmarked_spots_count;
		result = prime * result + challenge_pin_count;
		result = prime * result + checkin_num;
		result = prime * result + country_pin_count;
		result = prime * result + friends_count;
		result = prime * result + highlights_count;
		result = prime * result + items_count;
		result = prime * result + photos_count;
		result = prime * result + pins_count;
		result = prime * result + places_num;
		result = prime * result + province_pin_count;
		result = prime * result + region_pin_count;
		result = prime * result + stamps_count;
		result = prime * result + state_pin_count;
		result = prime * result + trips_count;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		UserGowalla other = (UserGowalla) obj;
		if (bookmarked_spots_count != other.bookmarked_spots_count) return false;
		if (challenge_pin_count != other.challenge_pin_count) return false;
		if (checkin_num != other.checkin_num) return false;
		if (country_pin_count != other.country_pin_count) return false;
		if (friends_count != other.friends_count) return false;
		if (highlights_count != other.highlights_count) return false;
		if (items_count != other.items_count) return false;
		if (photos_count != other.photos_count) return false;
		if (pins_count != other.pins_count) return false;
		if (places_num != other.places_num) return false;
		if (province_pin_count != other.province_pin_count) return false;
		if (region_pin_count != other.region_pin_count) return false;
		if (stamps_count != other.stamps_count) return false;
		if (state_pin_count != other.state_pin_count) return false;
		if (trips_count != other.trips_count) return false;
		return true;
	}

}
