package org.activity.sanityChecks;

import org.activity.stats.StatsUtils;

public class Testsplit
{

	public static void main(String[] args)
	{
		// String s = "id,lat,lng,name,city_state,,";
		// s =
		// "id,created_at,lng,lat,photos_count,checkins_count,users_count,radius_meters,highlights_count,items_count,max_items_count,spot_categories";
		// String[] splittedString = s.split(",");
		// System.out.println(splittedString.length);

		double num = StatsUtils.haversine("-0.4536151886", "51.47269335", "-0.45792812", "51.47069346");

		System.out.println(num);
		// 0.4536151886 , 51.4711990231 --- -0.45792812,51.47069346

	}

}
