// package org.activity.util;
//
// import eu.bitm.NominatimReverseGeocoding.Address;
// import eu.bitm.NominatimReverseGeocoding.NominatimReverseGeocodingJAPI;
//
// public class GeoUtils
// {
// static final NominatimReverseGeocodingJAPI nominatim = new NominatimReverseGeocodingJAPI();;
//
// public GeoUtils()
// {
// // TODO Auto-generated constructor stub
// }
//
// public static void main(String args[])
// {
// // NominatimReverseGeocodingJAPI nominatim1 = new NominatimReverseGeocodingJAPI(); // create instance with
// // default
// // // zoom level (18)
// // // NominatimReverseGeocodingJAPI nominatim2 = new NominatimReverseGeocodingJAPI(zoom); // create instance
// // with
// // // given zoom level
// // nominatim1.getAdress(lat, lon); // returns Address object for the given position
// System.out.println(getAddressFromLatLon(53.3408699, -6.2694497).getCity());
// System.out.println(getAddressFromLatLon(53.3408699, -6.2694497).toString());
// }
//
// public static Address getAddressFromLatLon(double lat, double lon)
// {
// return (nominatim.getAdress(lat, lon));
// }
//
// public static String getCityFromLatLon(String latS, String lonS)
// {
// String city = "";
// try
// {
// double lat = Double.valueOf(latS);
// double lon = Double.valueOf(lonS);
// city = nominatim.getAdress(lat, lon).toString();
// }
// catch (Exception e)
// {
// e.printStackTrace();
// }
// return (city);
// }
// }
