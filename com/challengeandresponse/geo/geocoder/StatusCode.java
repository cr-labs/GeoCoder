package com.challengeandresponse.geo.geocoder;

/**
 * <p>Translates and enumerates response-status values for geocoder responses.
 * Additional code(s) have been added here to include status for local extensions.
 * The class also defines an isOK method... classes returning GeocodedLocation objects 
 * must set their status codes to a value that is recognized by this class as "OK" for their
 * responses to be used by the caller</p>
 * <p>As of version 0.42 the response codes that return isOK() = true are GEO_SUCCESS
 * indicating a server was queried and a response obtained, and SUCCESS_NO_SERVER indicating
 * no server was consulted, but the results should be trustworthy anyway (though in this
 * case the returned GeocodedLocation object may not have all its fields filled out)
 * 
 * Based on Google's status codes: http://www.google.com/apis/maps/documentation/reference.html#GGeoStatusCode
 * @author jim
 * @version 0.42
 */

public class StatusCode {
	public static final String COPYRIGHT="Copyright (c) 2007 Challenge/Response, LLC, Cambridge, MA";

	public static final int G_CODE_NOT_SET = -1;
	
	public static final int GEO_SUCCESS = 200;
	public static final int GEO_SERVER_ERROR = 500;
	public static final int GEO_MISSING_ADDRESS = 601;
	public static final int GEO_UNKNOWN_ADDRESS = 602;
	public static final int UNAVAILABLE_ADDRESS = 603;
	public static final int GEO_BAD_KEY = 610;
	
	/**
	 * A protocol or communication error occurred, e.g. IO Exception or malformed URL
	 */
	public static final int G_COMM_ERROR = 9997;
	/**
	 * An error in a geocoding module (not necessarily a server error)
	 */
	public static final int	G_GEOCODER_ERROR = 9998; 
	/**
	 * No geocoding was necessary, so server was not consulted (e.g. a Lat/Lon were passed in)
	 */
	public static final int	SUCCESS_NO_SERVER = 9999;
	
	/**
	 * Return the official Google text explaining each response code
	 * @param code
	 * @return the textual description of the value in 'code'
	 */
	public static String getText(int code) {
		switch (code) {
		case G_CODE_NOT_SET:
			return "A status code has not been set.";
		case GEO_SUCCESS:
			return "No errors occurred; the address was successfully parsed and its geocode has been returned.";
		case GEO_SERVER_ERROR:
			return "A geocoding request could not be successfully processed, yet the exact reason for the failure is not known.";
		case GEO_MISSING_ADDRESS:
			return "The HTTP q parameter was either missing or had no value.";
		case GEO_UNKNOWN_ADDRESS:
			return "No corresponding geographic location could be found for the specified address. This may be due to the fact that the address is relatively new, or it may be incorrect.";
		case UNAVAILABLE_ADDRESS:
			return "The geocode for the given address cannot be returned due to legal or contractual reasons.";
		case GEO_BAD_KEY:
			return "The given key is either invalid or does not match the domain for which it was given.";
		case G_COMM_ERROR:
			return "Communication error.";
		case G_GEOCODER_ERROR:
			return "Geocoder error.";
		case SUCCESS_NO_SERVER:
			return "A Lat,Lon pair was provided, so the server was not consulted.";
		default:
			return ""+code;
		}
	}
	
	/**
	 * 
	 * @param code
	 * @return true if 'code' indicates the object has a usable location value, false otherwise
	 * At present the codes for "OK" are G_GEO_SUCCESS and G_SERVER_NOT_CONSULTED
	 */
	public static boolean isOK(int code) {
		switch (code) {
		case GEO_SUCCESS:
		case SUCCESS_NO_SERVER:
			return true;
		default:
			return false;
		}
	}
	
}