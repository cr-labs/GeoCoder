package com.challengeandresponse.geo.geocoder;

import com.challengeandresponse.geo.data.GeocodedLocation;

public interface GeocoderI {

	/**
	 * Given a locationDescription (a string placename), return a GeocodedLocation object.
	 * An object must always be returned, even if there was no match found.
	 * Set the appropriate StatusCode in the response object, to indicate "not found", if necessary
	 * 
	 * @param locationDescription The location to geocode
	 * @return an MMGeocodedLocation object with status code set
	 */
	public GeocodedLocation geocode(String locationDescription);

	
	/**
	 * Given an MMGeocodedLocation object with its placename set,
	 * populate that object with the geocoded details for its placename.
	 * This method is provided so that subclasses that have extended MMGeocodedLocation can be
	 * populated by the geocoders but retain their other fields...<br />
	 * An object must always be returned, even if there was no match found.
	 * Set the appropriate StatusCode in the response object, to indicate "not found", if necessary
	 * 
	 * @param location an MMGeocodedLocation object with its placeName set. This object's other fields will be overwritten as appropriate for the given placeName
	 * @return a GeocodedLocation object with status code set
	 */
	public GeocodedLocation geocode(GeocodedLocation location);

	
	/**
	 * @return a String representing the version of this geocoder, included in the geocoder field of GeocodedLocation objects
	 */
	public String getVersion();
	
}
