package com.challengeandresponse.geo.geocoders;

import com.challengeandresponse.geo.data.GeocodedLocation;
import com.challengeandresponse.geo.geocoder.*;


/**
 * A geocoder that recognizes Lat,Lon pairs and simply returns them without calling a server.

 * A lat/lon is recognized as two numbers separated by spaces or commas,
 * parseable as Doubles, having values between -360.0 and 360.0 (not inclusive).<br />
 * valid values: -360.0 < LATorLON < 360.0<br />
 * If a third parseable number is also present (and separated by spaces or commas) it is interpreted as height. This is optional.
 * 
 * @author jim
 * @version 0.40 2007-01-28
 */

/*
 * REVISION HISTORY
 * 0.40 2007-01-28 Now recognizes "height" in lat/lon strings.<br />
 * 0.42 2007-02-02 Now works with the new BigDecimal GeocodedLocation. Added trim() to preparse to better detect lat/lon strings<br />
 * 
 */

/*
 * KNOWN ISSUES
 * When making the GeocodedLocation object, use the original strings rather than the parsed doubles.
 */

public class LatLonGeocoder implements GeocoderI {
	public static final String	PRODUCT_SHORT 	= "LatLonGeocoder";
	public static final String PRODUCT_LONG	= "Challenge/Response Lat/Lon Geocoder";
	public static final String	VERSION_SHORT	= "0.43";
	public static final String	VERSION_LONG 	=  PRODUCT_LONG + " " + VERSION_SHORT;
	public static final String	COPYRIGHT		= "Copyright (c) 2007 Challenge/Response, LLC, Cambridge, MA";

	public LatLonGeocoder() { 
	}

	public String getVersion() {
		return VERSION_SHORT;
	}

	/**
	 * If placeName seems to be a Lat,Lon encode that, otherwise return GEO_UNKNOWN_ADDRESS
	 * A lat/lon is recognized as two numbers separated by non-number stuff (spaces, comma, other characters comma, parseable as Doubles, having values between -360.0 and 360.0
	 * @param placeName The place to geocode
	 * @return a LatLonResponse object indicating the lat/lon of the address, accuracy, and server response code
	 */	
	public GeocodedLocation geocode(String placeName) {
		try {
			String[] s = placeName.trim().split("[,\\s]+");
			if ( (s.length != 2) && (s.length != 3))
				throw new Exception();
			double latCandidate = Double.parseDouble(s[0]);
			double lonCandidate = Double.parseDouble(s[1]);
			double heightCandidate = (s.length == 3) ? Double.parseDouble(s[2]) : 0.0D;
			// test for reasonableness
			if ( (latCandidate < 360.0D) && (latCandidate > -360.0D) &&
					(lonCandidate < 360.0D) && (lonCandidate > -360.0))
				return new GeocodedLocation(placeName, StatusCode.SUCCESS_NO_SERVER, 
						PrecisionCode.LAT_LON, "",latCandidate, lonCandidate, heightCandidate);
		}
		catch (Exception e) {
		}
		return new GeocodedLocation(placeName.trim(),StatusCode.GEO_UNKNOWN_ADDRESS,
				PrecisionCode.UNKNOWN_LOCATION,"",0D,0D,0D);
	}



	/**
	 * Geocode the provided placeName, and update a provided GeocodedLocation object with fresh geocoding stuff.
	 * This method calls the geocode(String) method to do the actual work, then transmutes the relevant results into the object gcl
	 * @param gcl a GeocodedLocation object to revise with new data, based on its placeName field
	 */	
	public GeocodedLocation geocode(GeocodedLocation gcl) {
		GeocodedLocation gcl2 = geocode(gcl.getPlaceName());
		// stuff 'gcl' with new info
		// 	GeocodedLocation(String placeName, int serverResponse, int accuracy, String country, double latitude, double longitude, double height)
		gcl.setPlaceName(gcl2.getPlaceName());
		gcl.setServerResponse(gcl2.getServerResponse());
		gcl.setAccuracy(gcl2.getAccuracy());
		gcl.setCountry(gcl2.getCountryCode());
		gcl.set(gcl2.getDoubleLatitude(), gcl2.getDoubleLongitude(), gcl2.getDoubleHeight());
		return gcl;
	}



	// for testing
	public static void main(String[] args) {
		LatLonGeocoder gc = new LatLonGeocoder();

//		GeocodedLocation resp = gc.geocode("42.987,    71.1234,      100 ");
//		GeocodedLocation resp = gc.geocode("    42.987,   -71.1234  ");
		GeocodedLocation resp = gc.geocode("    42.987,    288.1234  ");
		System.out.println(resp.getPlaceName());
		System.out.println("Server response: "+StatusCode.getText(resp.getServerResponse()));
		System.out.println("Conformed Placename: "+resp.getPlaceName());
		System.out.println("Accuracy: "+PrecisionCode.getText(resp.getAccuracy()));
		System.out.println("Latitude: "+resp.getStringLatitude());
		System.out.println("Longitude: "+resp.getStringLongitude());
		System.out.println("Height: "+resp.getStringHeight());

//		resp = gc.geocode("0559.999999,-71.1234");
//		System.out.println(resp.getPlaceName());
//		System.out.println("Server response: "+StatusCode.getText(resp.getServerResponse()));
//		System.out.println("Conformed Placename: "+resp.getPlaceName());
//		System.out.println("Accuracy: "+PrecisionCode.getText(resp.getAccuracy()));
//		System.out.println("Latitude: "+resp.getLatitude());
//		System.out.println("Longitude: "+resp.getLongitude());
//		System.out.println("Height: "+resp.getHeight());

//		resp = llgc.geocode("Boston, MA");
//		System.out.println(resp.getPlaceName());
//		System.out.println("Server response: "+StatusCode.getText(resp.getServerResponse()));
//		System.out.println("Conformed Placename: "+resp.getPlaceName());
//		System.out.println("Accuracy: "+PrecisionCode.getText(resp.getAccuracy()));
//		System.out.println("Latitude: "+resp.getLatitude());
//		System.out.println("Longitude: "+resp.getLongitude());
//		System.out.println("Height: "+resp.getHeight());

	

		System.out.println("Testing geocode(GeocodedLocation)");
		GeocodedLocation gcl = new GeocodedLocation("    42.987,    288.1234  ");
		gc.geocode(gcl);
		System.out.println(resp.getPlaceName());
		System.out.println("Server response: "+StatusCode.getText(resp.getServerResponse()));
		System.out.println("Conformed Placename: "+resp.getPlaceName());
		System.out.println("Accuracy: "+PrecisionCode.getText(resp.getAccuracy()));
		System.out.println("Latitude: "+resp.getStringLatitude());
		System.out.println("Longitude: "+resp.getStringLongitude());
		System.out.println("Height: "+resp.getStringHeight());
	}
	
}