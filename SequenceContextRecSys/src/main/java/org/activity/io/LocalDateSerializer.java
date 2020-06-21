package org.activity.io;

import java.sql.Date;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * 
 * @author https://github.com/Cascading/meat-locker/blob/master/src/jvm/com/twitter/meatlocker/kryo/SqlDateSerializer.java
 *
 */

public class LocalDateSerializer extends Serializer<Date>
{

	@Override
	public void write(Kryo kryo, Output output, Date date)
	{
		output.writeLong(date.getTime(), true);
	}

	@Override
	public Date read(Kryo kryo, Input input, Class<Date> dateClass)
	{
		return new Date(input.readLong(true));
	}
}