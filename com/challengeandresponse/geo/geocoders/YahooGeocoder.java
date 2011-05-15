package com.challengeandresponse.geo.geocoders;

import java.net.URL;
import java.net.URLEncoder;
import java.util.Hashtable;

import com.challengeandresponse.cheapws.*;
import com.challengeandresponse.geo.data.GeocodedLocation;
import com.challengeandresponse.geo.geocoder.*;

/**
 * A geocoder that uses Yahoo's geocoder API to get the latitude and longitude
 * of an address or address fragment that's acceptable to the Yahoo coder
 * 
 * example:<br />
 * http://api.local.yahoo.com/MapsService/V1/geocode?appid=APPID&location=FREEFORM_LOCATION&output=xml<br />
 * 
 * @author jim
 * @version 0.25 2007-02-02
 */

/*
 * REVISION HISTORY
 * 0.25 2007-02-02  Rewritten to directly call the string constructor of GeocodedLocation
 */

public class YahooGeocoder implements GeocoderI {
	public static final String	PRODUCT_SHORT 	= "YahooGeocoder";
	public static final String PRODUCT_LONG	= "Challenge/Response Yahoo Geocoder";
	public static final String	VERSION_SHORT	= "0.26";
	public static final String	VERSION_LONG 	=  PRODUCT_LONG + " " + VERSION_SHORT;
	public static final String	COPYRIGHT		= "Copyright (c) 2007 Challenge/Response, LLC, Cambridge, MA";

	// output selectors
	private static final String OUTPUT_XML = "xml";
//	private static final String OUTPUT_PHP = "php";

	
	// PRECISION codes
	private static final Hashtable <String,Integer>ACCURACY_CODES = new Hashtable <String,Integer>();
	static {
		ACCURACY_CODES.put("address",new Integer(PrecisionCode.ADDRESS));
		ACCURACY_CODES.put("street",new Integer(PrecisionCode.STREET));
		ACCURACY_CODES.put("zip+4",new Integer(PrecisionCode.POSTCODE));
		ACCURACY_CODES.put("zip+2",new Integer(PrecisionCode.POSTCODE));
		ACCURACY_CODES.put("zip",new Integer(PrecisionCode.POSTCODE));
		ACCURACY_CODES.put("city",new Integer(PrecisionCode.TOWN));
		ACCURACY_CODES.put("state",new Integer(PrecisionCode.REGION));
		ACCURACY_CODES.put("country",new Integer(PrecisionCode.COUNTRY));

	}
	
	/** encoding for the REST query */
	public static String URLENCODING="UTF-8";

	/** The URL of Yahoo Maps API, it is hardcoded not final, so could be overridden if necessary */
	public static String GEOCODER_URL="http://api.local.yahoo.com/MapsService/V1/geocode";


	/// PRIVATE INSTANCE...
	// the Yahoo Application ID
	private String appID;
	// a CheapXPath instance
	private CheapXPath cheapxp;
	// and a CheapREST instance
	private CheapREST cheapREST;

	
	/// CONSTRUCTORS 
	
	/**
	 * @param appID The Yahoo Application ID assigned by Yahoo
	 */
	public YahooGeocoder(String appID) {
		this.appID = appID;
		cheapxp = new CheapXPath();
		cheapREST = new CheapREST();
	}


	/// METHODS
	
	public String getVersion() {
		return VERSION_SHORT;
	}
	
	
	/**
	 * Geocode the provided address, returning a GeocodedLocation
	 * @param placeName The place to geocode
	 * @return a GeocodedLocation object for that location, or an object with status code set, even if there was an error
	 */	
	public GeocodedLocation geocode(String placeName) {
		int statusCode = StatusCode.G_CODE_NOT_SET;
		try {
			String encodedURL = GEOCODER_URL+"?appid="+appID+"&output="+OUTPUT_XML+"&location="+URLEncoder.encode(placeName,URLENCODING);
			cheapxp.loadDocument(cheapREST.getREST(new URL(encodedURL)));

			String address = cheapxp.digXMLText("Result/Address");
			String city = cheapxp.digXMLText("Result/City");
			String state = cheapxp.digXMLText("Result/State");
			String country = cheapxp.digXMLText("Result/Country");
			StringBuffer composedAddress = new StringBuffer();
			if (address.length() > 0) composedAddress.append(address).append(", ");
			if (city.length() > 0) composedAddress.append(city).append(", ");
			if (state.length() > 0) composedAddress.append(state).append(", ");
			if (country.length() > 0) composedAddress.append(country);
			if (composedAddress.lastIndexOf(", ") == (composedAddress.length()-1))
				composedAddress.delete(composedAddress.length()-2,composedAddress.length()-1);

			return new GeocodedLocation(
					composedAddress.toString(),
					StatusCode.GEO_SUCCESS,
					((Integer) ACCURACY_CODES.get(cheapxp.digXMLAttribute("Result","precision"))).intValue(),
					cheapxp.digXMLText("Result/Country"),
					cheapxp.digXMLText("Result/Latitude"), 	// latitude
					cheapxp.digXMLText("Result/Longitude"), // longitude
					"0.0" // altitude (yahoo does not provide)
			);
		}
		catch (CheapWSException wse) {
			return new GeocodedLocation(placeName.trim(),
					(statusCode == StatusCode.G_CODE_NOT_SET ? StatusCode.G_COMM_ERROR : statusCode),
					PrecisionCode.UNKNOWN_LOCATION,"",0.0D,0.0D,0.0D);
		}
		catch (Exception e) { // XML parser error or NumberFormatException or URL formatting exception
			return new GeocodedLocation(placeName.trim(),
					(statusCode == StatusCode.G_CODE_NOT_SET ? StatusCode.G_GEOCODER_ERROR : statusCode),
					PrecisionCode.UNKNOWN_LOCATION,"",0.0D,0.0D,0.0D);
		}
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
	public static void main(String[] args)
	throws GeocoderException {
		YahooGeocoder gc = new YahooGeocoder("eyqBFmfIkY1wnsE_EUd1cIQfIvnvI2PisDg-");

		
		System.out.println("Testing geocode(String placename)");
//		GeocodedLocation resp = gc.geocode("860 Nancy Street, Niles, OH");
//		GeocodedLocation resp = gc.geocode("Nancy Street, Niles, OH");
//		GeocodedLocation resp = gc.geocode("London, England");
//		GeocodedLocation resp = gc.geocode("Paris, France");
//		GeocodedLocation resp = gc.geocode("205 Tun Hwa North Road, Taipei 105, Taiwan");
//		GeocodedLocation resp = gc.geocode("Taichung Shih,TAIWAN");
//		GeocodedLocation resp = gc.geocode(" this is bad input, this cannot be geocoded");
		GeocodedLocation resp = gc.geocode("Colombo, Sri Lanka");

		System.out.println(resp.getPlaceName());
		System.out.println("Server response: "+StatusCode.getText(resp.getServerResponse()));
		System.out.println("Place name: "+resp.getPlaceName());
		System.out.println("Accuracy: "+PrecisionCode.getText(resp.getAccuracy()));
		System.out.println("Latitude: "+resp.getStringLatitude());
		System.out.println("Longitude: "+resp.getStringLongitude());
		
		System.out.println("Testing geocode(GeocodedLocation)");
		GeocodedLocation gcl = new GeocodedLocation("Colombo, Sri Lanka");
		gc.geocode(gcl);
		System.out.println(resp.getPlaceName());
		System.out.println("Server response: "+StatusCode.getText(gcl.getServerResponse()));
		System.out.println("Place name: "+gcl.getPlaceName());
		System.out.println("Accuracy: "+PrecisionCode.getText(gcl.getAccuracy()));
		System.out.println("Latitude: "+gcl.getStringLatitude());
		System.out.println("Longitude: "+gcl.getStringLongitude());
		

	}



}