package org.activity.io;

import java.nio.ByteBuffer;

import com.zaxxer.nuprocess.NuAbstractProcessHandler;
import com.zaxxer.nuprocess.NuProcess;

public class ProcessHandler extends NuAbstractProcessHandler
{
	private NuProcess nuProcess;

	@Override
	public void onStart(NuProcess nuProcess)
	{
		this.nuProcess = nuProcess;
	}

	public void onStdout(ByteBuffer buffer, boolean closed)
	{
		if (!closed)
		{
			byte[] bytes = new byte[buffer.remaining()];
			// You must update buffer.position() before returning (either implicitly,
			// like this, or explicitly) to indicate how many bytes your handler has consumed.
			buffer.get(bytes);
			System.out.println(new String(bytes));

			// For this example, we're done, so closing STDIN will cause the "cat" process to exit
			nuProcess.closeStdin(true);
		}
	}
}
