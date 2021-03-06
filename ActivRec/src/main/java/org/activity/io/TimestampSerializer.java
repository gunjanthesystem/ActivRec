package org.activity.io;

import java.sql.Timestamp;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * 
 * @author https://github.com/Cascading/meat-locker/blob/master/src/jvm/com/twitter/meatlocker/kryo/TimestampSerializer.java
 *
 */
public class TimestampSerializer extends Serializer<Timestamp>
{

	@Override
	public void write(Kryo kryo, Output output, Timestamp timestamp)
	{
		output.writeLong(timestamp.getTime(), true);
		output.writeInt(timestamp.getNanos(), true);
	}

	@Override
	public Timestamp read(Kryo kryo, Input input, Class<Timestamp> timestampClass)
	{
		Timestamp ts = new Timestamp(input.readLong(true));
		ts.setNanos(input.readInt(true));
		return ts;
	}
}