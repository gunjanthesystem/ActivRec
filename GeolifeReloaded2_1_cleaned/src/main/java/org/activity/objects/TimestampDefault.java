package org.activity.objects;

import java.sql.Timestamp;
import java.util.TimeZone;

public class TimestampDefault extends Timestamp
{
	private static final long serialVersionUID = 1L;
	
	public TimestampDefault(int year, int month, int date, int hour, int minute, int second, int nano)
	{
		super(year, month, date, hour, minute, second, nano);
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
	
}
