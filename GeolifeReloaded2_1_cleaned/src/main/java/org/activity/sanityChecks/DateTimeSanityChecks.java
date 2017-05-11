package org.activity.sanityChecks;

import java.sql.Timestamp;

import org.activity.ui.PopUps;

public class DateTimeSanityChecks
{

	public static boolean assertEqualsTimestamp(int date, int month, int year, Timestamp endTimeStamp)
	{

		Timestamp temp1 = new Timestamp(year - 1900, month - 1, date, endTimeStamp.getHours(),
				endTimeStamp.getMinutes(), endTimeStamp.getSeconds(), 0);

		System.out.println("Debug: timestamp concern line 534 temp1.equals(endTimeStamp)" + temp1.equals(endTimeStamp));
		if (!temp1.equals(endTimeStamp))
		{
			System.err
					.println(PopUps.getCurrentStackTracedErrorMsg("Error in sanity check timestamp concern line 534 "));
			return false;
		}

		else
		{
			return true;
		}
	}

}
