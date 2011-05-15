package com.challengeandresponse.geo.geocoders;


import java.net.URL;
import java.net.URLEncoder;

import com.challengeandresponse.cheapws.CheapREST;
import com.challengeandresponse.cheapws.CheapXPath;
import com.challengeandresponse.geo.data.GeocodedLocation;
import com.challengeandresponse.geo.geocoder.*;

/**
 * A geocoder that uses Google Maps and others API to get the latitude and longitude
 * of an address or address fragment that's acceptable to Google's geocoder
 * 
 * @author jim
 * @version 0.52 2007-01-26
 */
/*
 * REVISION HISTORY
 * 0.52 2007-02-02 Calls the String-style geolocation constructors now rather than pre-parsing
 */
public class GoogleGeocoder implements GeocoderI {
	public static final String	PRODUCT_SHORT 	= "GoogleGeocoder";
	public static final String PRODUCT_LONG	= "Challenge/Response Google Geocoder";
	public static final String	VERSION_SHORT	= "0.54";
	public static final String	VERSION_LONG 	=  PRODUCT_LONG + " " + VERSION_SHORT;
	public static final String	COPYRIGHT		= "Copyright (c) 2007 Challenge/Response, LLC, Cambridge, MA";

	// output selectors
	private static final String OUTPUT_XML = "xml";
//	private static final String OUTPUT_KML = "kml";
//	private static final String OUTPUT_CSV = "csv";
//	private static final String OUTPUT_JSON = "json";

	/** encoding for the REST query */
	public static String URLENCODING="UTF-8";

	/** The URL of Google Maps API is hardcoded but not final, so could be overridden if necessary */
	public static String GEOCODER_URL="http://maps.google.com/maps/geo";


	// the Google Maps API Key
	private String gmapsAPIKey;
	// a CheapXPath instance
	private CheapXPath cheapxp;
	// and a CheapREST instance
	private CheapREST cheapREST;

	
	/// CONSTRUCTORS 
	
	/**
	 * @param gmapsAPIKey The Google Maps API Key assigned by Google
	 */
	public GoogleGeocoder(String gmapsAPIKey) {
		this.gmapsAPIKey = gmapsAPIKey;
		cheapxp = new CheapXPath();
		cheapREST = new CheapREST();
	}


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
			String encodedURL = GEOCODER_URL+"?key="+gmapsAPIKey+"&output="+OUTPUT_XML+"&q="+URLEncoder.encode(placeName,URLENCODING);
			cheapxp.loadDocument(cheapREST.getREST(new URL(encodedURL)));
			// try to get a status code first, we will try to include it in the error response below if things don't work out
			statusCode = Integer.parseInt(cheapxp.digXMLText("Response/Status/code"));
			// splice out lat/lon/altitude from the "coordinates" section
			String[] lonLatAlt = cheapxp.digXMLText("Response/Placemark/Point/coordinates").split(",");
			return new GeocodedLocation(
					cheapxp.digXMLText("Response/Placemark/address"),
					statusCode,
					Integer.parseInt(cheapxp.digXMLAttribute("Response/Placemark/AddressDetails","Accuracy")),
					cheapxp.digXMLText("Response/Placemark/AddressDetails/Country/CountryNameCode"),
					lonLatAlt[1], 	// latitude
					lonLatAlt[0], 	// longitude
					lonLatAlt[2] 	// altitude
			);
		}
		catch (Exception e) { // XML parser error or NumberFormatException or URL formatting exception
			return new GeocodedLocation(placeName.trim(),
					(statusCode == StatusCode.G_CODE_NOT_SET ? StatusCode.G_GEOCODER_ERROR : statusCode),
					PrecisionCode.UNKNOWN_LOCATION,"",0.0D,0.0D,0.0D);
		}
	}


	/**
	 * Geocode the provided placeName, and update a provided MMGeocodedLocation object with fresh geocoding stuff.
	 * This method calls the geocode(String) method to do the actual work, then transmutes the relevant results into the object gcl
	 * @param gcl a GeocodedLocation object to revise with new data, based on its placeName field
	 */	
	public GeocodedLocation geocode(GeocodedLocation gcl) {
		GeocodedLocation gcl2 = geocode(gcl.getPlaceName());
		// stuff 'gcl' with new info
		// 	MMGeocodedLocation(String placeName, int serverResponse, int accuracy, String country, double latitude, double longitude, double height)
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
		GoogleGeocoder gc = new GoogleGeocoder("ABQIAAAATpIP0nQMoP5fnPlNJxbLDBSPCd7MrKeM6w_jwe7QFLxKoZJC8xQyh58lNpVG1DTdJcKkrbIPUMpofA");

		
		System.out.println("Testing geocode(String placename)");
//		GeocodedLocation resp = gc.geocode("860 Nancy Street, Niles, OH");
//		GeocodedLocation resp = gc.geocode("Colombo, Sri Lanka");
//		GeocodedLocation resp = gc.geocode("London, England");
//		GeocodedLocation resp = gc.geocode("42,-71");
//		GeocodedLocation resp = gc.geocode("Hope, Arkansas");

		GeocodedLocation resp = gc.geocode("Boston, MA");
		System.out.println(resp.getPlaceName());
		System.out.println("Server response: "+StatusCode.getText(resp.getServerResponse()));
		System.out.println("Accuracy: "+PrecisionCode.getText(resp.getAccuracy()));
		System.out.println("Country code: "+resp.getCountryCode());
		System.out.println("Latitude: "+resp.getStringLatitude());
		System.out.println("Longitude: "+resp.getStringLongitude());
		

		System.out.println("Testing geocode(MMGeocodedLocation)");
		GeocodedLocation gcl = new GeocodedLocation("Boston, MA");
		gc.geocode(gcl);
		System.out.println(resp.getPlaceName());
		System.out.println("Server response: "+StatusCode.getText(gcl.getServerResponse()));
		System.out.println("Place name: "+gcl.getPlaceName());
		System.out.println("Accuracy: "+PrecisionCode.getText(gcl.getAccuracy()));
		System.out.println("Latitude: "+gcl.getStringLatitude());
		System.out.println("Longitude: "+gcl.getStringLongitude());

	}



}