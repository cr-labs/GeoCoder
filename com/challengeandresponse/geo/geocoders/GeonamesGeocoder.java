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
 * @version 0.20 2007-02-02
 */
/*
 * REVISION HISTORY
 * 0.20 2007-02-02 Updated to use BigDecimal LLH version of GeocodedLocation, and to pass strings to the constructor to save time
 */
public class GeonamesGeocoder implements GeocoderI {
	public static final String	PRODUCT_SHORT 	= "GeonamesGeocoder";
	public static final String	PRODUCT_LONG	= "Challenge/Response Geonames Geocoder";
	public static final String	VERSION_SHORT	= "0.21";
	public static final String	VERSION_LONG 	=  PRODUCT_LONG + " " + VERSION_SHORT;
	public static final String	COPYRIGHT		= "Copyright (c) 2007 Challenge/Response, LLC, Cambridge, MA";
 
	// output selectors
	private static final String OUTPUT_XML = "xml";
//	private static final String OUTPUT_RDF = "rdf";
//	private static final String OUTPUT_JSON = "json";
	
	// output style selectors
//	private static final String	OUTPUT_STYLE_FULL = "FULL";
//	private static final String	OUTPUT_STYLE_LONG = "LONG";
//	private static final String	OUTPUT_STYLE_MEDIUM = "MEDIUM";
	private static final String	OUTPUT_STYLE_SHORT = "SHORT";

	
	// PRECISION codes
	// mapping GeoNames feature classes and codes to AccuracyCodes
	// note that sometimes the whole CLASS is encoded here, e.g. "H"
	// search for code first. If no match, search for the feature class instead
	private static final Hashtable <String, Integer> ACCURACY_CODES = new Hashtable <String,Integer>();
	static {
		ACCURACY_CODES.put("ADM1",new Integer(PrecisionCode.REGION));
		ACCURACY_CODES.put("ADM2",new Integer(PrecisionCode.SUB_REGION));
		ACCURACY_CODES.put("ADM3",new Integer(PrecisionCode.SUB_REGION));
		ACCURACY_CODES.put("ADM4",new Integer(PrecisionCode.SUB_REGION));
		ACCURACY_CODES.put("ADMD",new Integer(PrecisionCode.REGION));
		ACCURACY_CODES.put("LTER",new Integer(PrecisionCode.REGION));

		ACCURACY_CODES.put("PCL",new Integer(PrecisionCode.SUB_REGION));
		ACCURACY_CODES.put("PCLD",new Integer(PrecisionCode.COUNTRY));
		ACCURACY_CODES.put("PCLF",new Integer(PrecisionCode.COUNTRY));
		ACCURACY_CODES.put("PCLI",new Integer(PrecisionCode.COUNTRY));
		ACCURACY_CODES.put("PCLIX",new Integer(PrecisionCode.COUNTRY));
		ACCURACY_CODES.put("PCLS",new Integer(PrecisionCode.COUNTRY));

		ACCURACY_CODES.put("PRSH",new Integer(PrecisionCode.SUB_REGION));
		ACCURACY_CODES.put("TERR",new Integer(PrecisionCode.REGION));
		
		ACCURACY_CODES.put("ZN",new Integer(PrecisionCode.AREA));
		ACCURACY_CODES.put("ZNB",new Integer(PrecisionCode.AREA));
	
		// H class are ALL "areas"
		ACCURACY_CODES.put("H",new Integer(PrecisionCode.AREA));

		// L class has some specific codes and many areas
		ACCURACY_CODES.put("CMN",new Integer(PrecisionCode.STREET));
		ACCURACY_CODES.put("CONT",new Integer(PrecisionCode.CONTINENT));
		ACCURACY_CODES.put("CTRB",new Integer(PrecisionCode.STREET));
		ACCURACY_CODES.put("DEVH",new Integer(PrecisionCode.STREET));
		ACCURACY_CODES.put("LCTY",new Integer(PrecisionCode.TOWN));
		ACCURACY_CODES.put("NVB",new Integer(PrecisionCode.STREET));
		ACCURACY_CODES.put("REP",new Integer(PrecisionCode.COUNTRY));
		ACCURACY_CODES.put("L",new Integer(PrecisionCode.AREA));

		// P class has cities and villages
		ACCURACY_CODES.put("PPL",new Integer(PrecisionCode.TOWN));
		ACCURACY_CODES.put("PPLA",new Integer(PrecisionCode.TOWN));
		ACCURACY_CODES.put("PPLC",new Integer(PrecisionCode.TOWN));
		ACCURACY_CODES.put("PPLG",new Integer(PrecisionCode.TOWN));
		ACCURACY_CODES.put("PPLL",new Integer(PrecisionCode.TOWN));
		ACCURACY_CODES.put("PPLQ",new Integer(PrecisionCode.TOWN));
		ACCURACY_CODES.put("PPLR",new Integer(PrecisionCode.TOWN));
		ACCURACY_CODES.put("PPLS",new Integer(PrecisionCode.TOWN));
		ACCURACY_CODES.put("PPLW",new Integer(PrecisionCode.AREA));
		ACCURACY_CODES.put("PPLX",new Integer(PrecisionCode.POSTCODE));
		ACCURACY_CODES.put("STLMT",new Integer(PrecisionCode.TOWN));
		
		// R , S, T, U, V classes are all "areas"
		ACCURACY_CODES.put("R",new Integer(PrecisionCode.AREA));
		ACCURACY_CODES.put("S",new Integer(PrecisionCode.AREA));
		ACCURACY_CODES.put("T",new Integer(PrecisionCode.AREA));
		ACCURACY_CODES.put("U",new Integer(PrecisionCode.AREA));
		ACCURACY_CODES.put("V",new Integer(PrecisionCode.AREA));

	}

	/** encoding for the REST query */
	public static String URLENCODING="UTF-8";

	/** The URL of Geonames API, it is hardcoded not final, so could be overridden if necessary */
	public static String GEOCODER_URL="http://ws.geonames.org/search";


	/// PRIVATE INSTANCE...
	private CheapXPath cheapxp;
	private CheapREST cheapREST;

	
	/// CONSTRUCTOR	
	public GeonamesGeocoder() { 
		cheapxp = new CheapXPath();
		cheapREST = new CheapREST();
	}



	/// METHODS
	public String getVersion() {
		return VERSION_SHORT;
	}
	
	
	/**
	 * Geocode the provided placeName, returning a GeocodedLocation
	 * @param placeName The place to geocode
	 * @return a GeocodedLocation object for that location, or an object with status code set, even if there was an error
	 */	
	public GeocodedLocation geocode(String placeName) {
		int statusCode = StatusCode.G_CODE_NOT_SET;
		try {
			String encodedURL = GEOCODER_URL+"?maxRows=1&style="+OUTPUT_STYLE_SHORT+"&type="+OUTPUT_XML+"&q="+URLEncoder.encode(placeName,URLENCODING);
			cheapxp.loadDocument(cheapREST.getREST(new URL(encodedURL)));

			// Try to get an accuracy measure from the table of ACCURACY_CODES or return UNKNOWN_ACCURACY if no match
			// fcl and fcode are the descriptive terms in the response from geonames.org
			String fcl = cheapxp.digXMLText("geoname/fcl");
			String fcode = cheapxp.digXMLText("geoname/fcode");
			int accuracy = PrecisionCode.UNKNOWN_ACCURACY;
			
			// first try the more specific fcode
			Object accuracyCode = ACCURACY_CODES.get(fcode);
			// if fcode didn't match anything, try the more general fcl
			if (accuracyCode == null)
				accuracyCode = ACCURACY_CODES.get(fcl);
			// if an accuracy code was found, use it
			if (accuracyCode != null)
				accuracy = ((Integer) accuracyCode).intValue();
			
			return new GeocodedLocation(
					cheapxp.digXMLText("geoname/name"),
					StatusCode.GEO_SUCCESS,
					accuracy,
					cheapxp.digXMLText("geoname/countryCode"),
					cheapxp.digXMLText("geoname/lat"), 	// latitude
					cheapxp.digXMLText("geoname/lng"), // longitude
					"0.0" // altitude (geonames does not provide)
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
		GeonamesGeocoder gc = new GeonamesGeocoder();
//		GeocodedLocation resp = gc.geocode("860 Nancy Street, Niles, OH");
//		GeocodedLocation resp = gc.geocode("Nancy Street, Niles, OH");
//		GeocodedLocation resp = gc.geocode("Colombo, Sri Lanka");
//		GeocodedLocation resp = gc.geocode("London, England");
//		GeocodedLocation resp = gc.geocode("Paris, France");
//		GeocodedLocation resp = gc.geocode("205 Tun Hwa North Road, Taipei 105, Taiwan");
//		GeocodedLocation resp = gc.geocode("Taichung Shih,TAIWAN");
//		GeocodedLocation resp = gc.geocode(" this is bad input, this cannot be geocoded");

		System.out.println("Testing geocode(String placeName)");
		GeocodedLocation resp = gc.geocode("Eastwood Mall");
		System.out.println(resp.getPlaceName());
		System.out.println("Server response: "+StatusCode.getText(resp.getServerResponse()));
		System.out.println("Place name: "+resp.getPlaceName());
		System.out.println("Accuracy: "+PrecisionCode.getText(resp.getAccuracy()));
		System.out.println("Latitude: "+resp.getStringLatitude());
		System.out.println("Longitude: "+resp.getStringLongitude());

		System.out.println("Testing geocode(MMGeocodedLocation)");
		GeocodedLocation gcl = new GeocodedLocation("Eastwood Mall");
		gc.geocode(gcl);
		System.out.println(resp.getPlaceName());
		System.out.println("Server response: "+StatusCode.getText(gcl.getServerResponse()));
		System.out.println("Place name: "+gcl.getPlaceName());
		System.out.println("Accuracy: "+PrecisionCode.getText(gcl.getAccuracy()));
		System.out.println("Latitude: "+gcl.getStringLatitude());
		System.out.println("Longitude: "+gcl.getStringLongitude());
	}



}