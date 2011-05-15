package com.challengeandresponse.geo.geocoder;


/**
 * Translates and enumerates precision declarations for geocoder responses
 * 
 * Based on Google's "GeoAddressAccuracy" (UNKNOWN_LOCATION thru ADDRESS)
 * and extended to accommodate other geocoders and local needs (e.g. UNKNOWN_ACCURACY; CONTINENT)
 * <br />
 * Source: http://www.google.com/apis/maps/documentation/reference.html#GGeoAddressAccuracy
 * 
 * 0.22 2007-01-27 renamed to the more correct PrecisionCode from AccuracyCode
 * 
 * @author jim
 * @version 0.22 2007-01-27
 */
public class PrecisionCode {
	public static final String COPYRIGHT="Copyright (c) 2007 Challenge/Response, LLC, Cambridge, MA";

	public static final int UNKNOWN_LOCATION = 0;
	public static final int COUNTRY = 1;
	public static final int REGION = 2;
	public static final int SUB_REGION = 3;
	public static final int TOWN = 4;
	public static final int POSTCODE = 5;
	public static final int STREET = 6;
	public static final int INTERSECTION = 7;
	public static final int ADDRESS = 8;
	
	public static final int AREA = 96;
	public static final int	CONTINENT = 97;
	public static final int	UNKNOWN_ACCURACY = 98;
	public static final int LAT_LON = 99;

	/**
	 * Return text explaining each accuracy value. Google-based codes use the original Google text.
	 * 
	 * @param code the code to look up
	 * @return the textual description of the value in 'code' or the code by itself, if no extended textual description exists
	 */
	public static String getText(int code) {
		switch (code) {
		case UNKNOWN_LOCATION:
			return "Unknown location.";
		case COUNTRY:
			return "Country level accuracy";
		case REGION:
			return "Region (state, province, prefecture, etc.) level accuracy.";
		case SUB_REGION:
			return "Sub-region (county, municipality, etc.) level accuracy.";
		case TOWN:
			return "Town (city, village) level accuracy.";
		case POSTCODE:
			return "Post code (zip code) level accuracy.";
		case STREET:
			return "Street level accuracy.";
		case INTERSECTION:
			return "Intersection level accuracy.";
		case ADDRESS:
			return "Address level accuracy.";
		case AREA:
			return "Non-politically-defined region or place, unknown accuracy.";
		case CONTINENT:
			return "Continent.";
		case UNKNOWN_ACCURACY:
			return "Unknown accuracy.";
		case LAT_LON:
			return "Input data were provided as Latitude,Longitude; source data accuracy.";
		default:
			return ""+code;
		}
	}

}