package com.challengeandresponse.geo.data;

import java.io.Serializable;

import com.challengeandresponse.geo.geocoder.PrecisionCode;
import com.challengeandresponse.geo.geocoder.StatusCode;

/**
 * Holds one response from a Geocoding request, plus the original query.
 * Implements Serializable to accommodate caching via the CachingGGeocoder class
 * 
 * PlaceName is the conformed PlaceName as returned by the geocoding service...
 * so it may differ from the placename that was sent to the geocoder
 * 
 * @author jim
 * @version 2009-11-05 v0.52
 *
 */
/*
 * REVISION HISTORY
 * 0.30 2007-01-28 renamed "altitude" to "height"
 * 0.35 2007-02-02 Now extends LLH for high resolution lat/lon/height and always normalized values
 * 0.50 2007-04-02 Now extends MMWhere which means it's UniversalIQ-ready for M2M communications
 * 0.51 2007-06-01 Minor changes to reference MMLocation (the new name of MMWhere)
 * 0.52 2007-06-01 Ironically goes back to a simpler design, no longer implements Personalizable or TimeBounded -- the agents should implement those interfaces in their MM objects if they want them to have those features - not every RPC object should (e.g. the ordinary usage of MMError, MMObject or MMList objects presents no need for time bounds or personalization)
 * 0.53 2009-11-05 Another simplification. All MM stuff is now removed because Rocketsync provides a wrapper for object communications
 * 0.54 2009-11-10 Added locationType {point, radius}, radiusMeters;
 */
public class GeocodedLocation 
extends Location 
implements Serializable {
	private static final long serialVersionUID = 1L;

	public static transient final String VERSION = "0.54";
	public static transient final String COPYRIGHT="Copyright (c) 2007-2009 Challenge/Response, LLC, Cambridge, MA";

	
	// GEOCODING
	private String placeName;
	private int serverResponse;                                                                                                                                                                                                                                                                                                                                                                                                                                     
	private int accuracy; // should be a PrecisionCode
	private String country;
	private String geocoder; // ID of the geocoder that made this record


	public GeocodedLocation(String placeName, int serverResponse, int accuracy, String country, double latitude, double longitude, double height) {
		super(latitude,longitude,height);
		setPlaceName(placeName);
		setServerResponse(serverResponse);
		setAccuracy(accuracy);
		setCountry(country);
		setGeocoder("");
	}
	
	public GeocodedLocation(String placeName, int serverResponse, int accuracy, String country, String latitude, String longitude, String height) {
		super(latitude,longitude,height);
		setPlaceName(placeName);
		setServerResponse(serverResponse);
		setAccuracy(accuracy);
		setCountry(country);
		setGeocoder("");
	}

	/**
	 * This constructor is primarily intended for use with the named places collection in AgentWhereis
	 * (specifically it's useful for creating query objects that are used to retrieve a subject's
	 * named places)
	 * @param placeName
	 */
	public GeocodedLocation(String placeName) {
		super();
		setPlaceName(placeName);
		setServerResponse(StatusCode.G_CODE_NOT_SET);
		setAccuracy(PrecisionCode.UNKNOWN_LOCATION);
		setCountry("");
		setGeocoder("");
	}


	/**
	 * @return true if the serverResponse field is an OK status code
	 */
	public boolean isOK() {
		return StatusCode.isOK(serverResponse);
	}

	public String getPlaceName() {
		return this.placeName;
	}

	public int getServerResponse() {
		return this.serverResponse;
	}

	public int getAccuracy() {
		return this.accuracy;
	}
	
	public String getCountryCode() {
		return this.country;
	}
	
	
	
	public void setAccuracy(int accuracy) {
		this.accuracy = accuracy;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setPlaceName(String placeName) {
		this.placeName = placeName;
	}

	public void setServerResponse(int serverResponse) {
		this.serverResponse = serverResponse;
	}

	public String getGeocoder() {
		return this.geocoder;
	}

	public void setGeocoder(String geocoder) {
		this.geocoder = geocoder;
	}
	
	

	
	@Override
	public String toString() {
		return "GeocodedLocation [accuracy=" + accuracy + ", country="
				+ country + ", geocoder=" + geocoder + ", placeName="
				+ placeName + ", serverResponse=" + serverResponse
				+ ", super=" + super.toString() + "]";
	}
	
	
	
}