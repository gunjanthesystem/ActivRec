package org.activity.util;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HTTPUtils
{
	OkHttpClient client = new OkHttpClient();

	String getResponse(String url) throws IOException
	{
		Request request = new Request.Builder().url(url).build();

		try (Response response = client.newCall(request).execute())
		{
			return response.body().string();
		}
	}

	public static void main(String[] args) throws IOException
	{
		HTTPUtils example = new HTTPUtils();
		String response = example.getResponse(
				"https://nominatim.openstreetmap.org/reverse?format=xml&lat=40.774269&lon=-89.603806&zoom=18&addressdetails=1");
		System.out.println(response);
	}
}