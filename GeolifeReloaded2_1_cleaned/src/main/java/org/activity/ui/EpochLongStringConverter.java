package org.activity.ui;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javafx.util.converter.LongStringConverter;

/**
 * Convert epochs in ms to String
 * 
 * @author gunjan
 *
 */
public class EpochLongStringConverter extends LongStringConverter
{

	/** {@inheritDoc} */
	@Override
	public Long fromString(String value)
	{
		// If the specified value is null or zero-length, return null
		if (value == null)
		{
			return null;
		}

		value = value.trim();

		if (value.length() < 1)
		{
			return null;
		}

		return Long.valueOf(value);
	}

	/** {@inheritDoc} */
	@Override
	public String toString(Long longValue)
	{
		String res = "";
		// If the specified value is null, return a zero-length String
		if (longValue != null)
		{
			// Long longValue = Long.parseLong(String.format("%.0f", value));
			// String pattern = "dd-MM-yy HH:mm:ss";
			String pattern = "dd-MM HH:mm:ss";
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
			// StringConverter<LocalDateTime> converter = new LocalDateTimeStringConverter(formatter, null);
			// // assertEquals("12 January 1985, 12:34:56", converter.toString(VALID_LDT_WITH_SECONDS));
			LocalDateTime date = Instant.ofEpochMilli(longValue).atZone(ZoneId.systemDefault()).toLocalDateTime();
			res = date.format(formatter);
		}
		return res;
	}

}
