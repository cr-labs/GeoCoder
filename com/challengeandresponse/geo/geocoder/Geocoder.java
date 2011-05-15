package com.challengeandresponse.geo.geocoder;

import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

import com.challengeandresponse.geo.data.GeocodedLocation;
import com.challengeandresponse.geo.geocoders.*;

/**
 * An Ÿber geocoder that calls specialized Geocoders to attempt to resolve a placename to a location
 * 
 * <p>It ALSO implements the GeocoderI interface, so any geocoder could be called
 * directly, OR they can all be called together through this class.</p>
 * 
 * <p>New geocoders are added through the addGeocoder() methods. The geocoders
 * are consulted in the order they are added. Also, an addGeocoder(geocoder,N) method
 * is provided to force a geocoder into position N of the calling sequence.</p>
 * 
 * <p>If a geocoder provides a "success" response (see StatusCode class), this response
 * is returned and no more geocoders are called. If at least one geocoder is called
 * but no geocoder returns a GeocodedLocation object with a success value, then
 * the reponse from the last geocoder called is returned. If no geocoders were
 * called, then a GeocodedLocation with GEO_UNKNOWN_ADDRESS as its status is returned.</p>
 * 
 * <p>Brilliant!</p>
 * 
 * @author jim
 * @version 0.21 2007-02-02
 */

/*
 * REVISION HISTORY
 * 0.21 2007-02-02 Updated for LLH version of GeocodedLocation
 * 0.31 2009-11-06 Updated to use the simpler geo.data objects
 */

public class Geocoder implements GeocoderI {
	public static final String	PRODUCT_SHORT 	= "Geocoder";
	public static final String PRODUCT_LONG	= "Challenge/Response Geocoder";
	public static final String	VERSION_SHORT	= "0.31";
	public static final String	VERSION_LONG 	=  PRODUCT_LONG + " " + VERSION_SHORT;
	public static final String	COPYRIGHT		= "Copyright (c) 2007-2009 Challenge/Response, LLC, Cambridge, MA";

	private Vector <GeocoderI> geocoders;

	public Geocoder() { 
		geocoders = new Vector <GeocoderI>();
	}
	
	public String getVersion() {
		return VERSION_SHORT;
	}
	
	/**
	 * Step through all geocoders to try to Geocode the provided address, returning a GeocodedLocation with a status code indicating how things went
	 * @param placeName The placeName to geocode
	 * @return a GeocodedLocation object indicating the lat/lon of the address, accuracy, and server response code; if the lookup fails but at least one geocoder was consulted,this method returns the result from the last geocoder called. If no geocoders were called, it returns a GeocodedLocation object with GEO_UNKNOWN_ADDRESS as the status
	 */	
	public GeocodedLocation geocode(String placeName) {
		// try the registered geocoders
		Iterator <GeocoderI> i = geocoders.iterator();
		while (i.hasNext()) {
			 GeocoderI gc = i.next();
			 GeocodedLocation gcl = gc.geocode(placeName);
			// return the first successful result, as the geocoders are in preference order
			if (gcl.isOK()) {
				gcl.setGeocoder(gc.getClass().getName()+" "+gc.getVersion());
				return gcl;
			}
		}
		// if it fell through to here, the result was not success
		GeocodedLocation gcl = new GeocodedLocation(
				placeName.trim(),
				StatusCode.GEO_UNKNOWN_ADDRESS,PrecisionCode.UNKNOWN_LOCATION,
				"",0.0D,0.0D,0.0D);
		gcl.setGeocoder(this.getClass().getName()+" "+this.getVersion());
		return gcl;
	}

	
	
	/**
	 * Step through all geocoders to try to Geocode the provided address, revising the provided MMGeocodedLocation with details and a status code indicating how things went
	 * @param gcl The MMGeocodedLocation object to overwrite with fresh geocode data, based on its placeName field
	 */	

	public GeocodedLocation geocode(GeocodedLocation gcl) {
		// if it falls through, the result was not success
		gcl.setServerResponse(StatusCode.GEO_UNKNOWN_ADDRESS);
		gcl.setAccuracy(PrecisionCode.UNKNOWN_LOCATION);
		gcl.setCountry("");
		gcl.set(0.0D,0.0D,0.0D);
		gcl.setGeocoder(this.getClass().getName()+" "+this.getVersion());

		// try the registered geocoders
		Iterator <GeocoderI> i = geocoders.iterator();
		while (i.hasNext()) {
			 GeocoderI gc = i.next();
			 gc.geocode(gcl);
			// return the first successful result, as the geocoders are in preference order
			if (gcl.isOK()) {
				gcl.setGeocoder(gc.getClass().getName()+" "+gc.getVersion());
				break;
			}
		}
		return gcl;
	}


	

	/**
	 * Add a geocoder to the end of call stack. Coders are called in order until one
	 * of them returns a GeocodedLocation with a success code.
	 * @param geocoder the geocoder to add to the end of the call stack
	 */
	public void addGeocoder(GeocoderI geocoder) {
		geocoders.add(geocoder);
	}
	
	
	/**
	 * Add a geocoder to the call stack at a given position.
	 * @param geocoder the geocoder to add to the end of the call stack
	 * @param position add the geocoder at 'position' in the stack. If position is > the size of the stack, the geocoder is added to the end
	 * @throws GeocoderException if position is < 0, or geocoder is null
	 */
	public void addGeocoder(GeocoderI geocoder, int position)
	throws GeocoderException {
		if ((position < 0) || (geocoder == null))
			throw new GeocoderException("Invalid parameter");
		geocoders.add(Math.min(geocoders.size(),position), geocoder);
	}



	// for testing
	public static void main(String[] args)
	throws GeocoderException, IOException {
		Geocoder gc = new Geocoder();
		gc.addGeocoder(new IPV4Geocoder(null,IPV4Geocoder.RETURN_PLACENAME));
		gc.addGeocoder(new LatLonGeocoder(),0);
		gc.addGeocoder(new GoogleGeocoder("ABQIAAAATpIP0nQMoP5fnPlNJxbLDBSPCd7MrKeM6w_jwe7QFLxKoZJC8xQyh58lNpVG1DTdJcKkrbIPUMpofA"));
		gc.addGeocoder(new YahooGeocoder("eyqBFmfIkY1wnsE_EUd1cIQfIvnvI2PisDg-"));
		gc.addGeocoder(new GeonamesGeocoder());
		
		GeocodedLocation resp;
//		resp = gc.geocode("860 Nancy Street, Niles, OH");
//		resp = gc.geocode("Colombo, Sri Lanka");
//		resp = gc.geocode("London, UK");
//		resp = gc.geocode("  42.64  ,     399.77"); // this should fail - out of bounds longitude value
//		resp = gc.geocode("  42,    -71.23");
//		resp = gc.geocode("10 Greycoat Place, London, England");
//		resp = gc.geocode("18.85.2.181");
		resp = gc.geocode("Eastwood Mall");
		System.out.println(resp.getPlaceName());
		System.out.println("Server response: "+StatusCode.getText(resp.getServerResponse()));
		System.out.println("Accuracy: "+PrecisionCode.getText(resp.getAccuracy()));
		System.out.println("Latitude: "+resp.getStringLatitude());
		System.out.println("Longitude: "+resp.getStringLongitude());
		System.out.println("Height: "+resp.getStringHeight());
		System.out.println("Geocoder: "+resp.getGeocoder());
	}



}