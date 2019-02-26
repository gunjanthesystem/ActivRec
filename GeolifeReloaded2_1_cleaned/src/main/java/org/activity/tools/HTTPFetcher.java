package org.activity.tools;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HTTPFetcher
{
	public static void main(String args[])
	{
		try
		{
			okhttpTest("https://api.foursquare.com/v2/venues/categories");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void okhttpTest(String url) throws IOException
	{
		// &client_id=CLIENT_ID&client_secret=CLIENT_SECRET&v=YYYYMMDD

		// &client_id=12XW33IPCMNVMZZ1IJDUOABMJCC3ATUFYYIDDDYUUJBLQ4ZW&client_secret=CLIENT_SECRET&v=YYYYMMDD

		// Client ID
		// 12XW33IPCMNVMZZ1IJDUOABMJCC3ATUFYYIDDDYUUJBLQ4ZW
		// Client Secret
		// GKNA50J1ZZI1LXN3WP2L4MHZOFT4PEE0J21VDOPEERS3LOAF
		// v=YYYYMMDD
		OkHttpClient client = new OkHttpClient();

		HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
		urlBuilder.addQueryParameter("client_id", "12XW33IPCMNVMZZ1IJDUOABMJCC3ATUFYYIDDDYUUJBLQ4ZW");
		urlBuilder.addQueryParameter("client_secret", "GKNA50J1ZZI1LXN3WP2L4MHZOFT4PEE0J21VDOPEERS3LOAF");
		urlBuilder.addQueryParameter("v", "20181212");

		Request request = new Request.Builder().url(urlBuilder.build().toString()).build();
		Response response = client.newCall(request).execute();
		System.out.println("here:- \n" + response.body().string());
		// WToFile.writeToNewFile(response.body().string(), "./dataWritten/FSCats.json");
	}
}
