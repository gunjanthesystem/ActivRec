package org.activity.util;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HTTPUtils
{
	OkHttpClient client;
	int delayInms;

	public HTTPUtils(int delay)
	{
		client = new OkHttpClient();
		this.delayInms = delay;
	}

	public String getResponse(String url) throws IOException
	{
		if (delayInms > 0)
		{
			try
			{
				Thread.sleep(delayInms);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		Request request = new Request.Builder().url(url).build();
		try (Response response = client.newCall(request).execute())
		{
			return response.body().string();
		}

	}

	public static void main0(String args[])
	{

		HTTPUtils httpUtils = new HTTPUtils(-1);
		try
		{
			System.out.println(httpUtils
					.getResponse("https://maps.google.com/maps/api/geocode/xml?address=Malvern+City+of+Stonnington"));
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}