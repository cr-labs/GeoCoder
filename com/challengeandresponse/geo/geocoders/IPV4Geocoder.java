package com.challengeandresponse.geo.geocoders;


import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.challengeandresponse.cheapws.CheapREST;
import com.challengeandresponse.geo.data.GeocodedLocation;
import com.challengeandresponse.geo.geocoder.*;
import com.maxmind.geoip.Location;
import com.maxmind.geoip.LookupService;

/**
 * A geocoder that uses gets the latitude and longitude connected to an IP address.
 * This coder embeds both the service at http://hostip.info/ and the offline
 * free city database from maxmind.com.
 * 
 * <P>This is actually two geocoders in one. Call the constructor that takes the file
 * path, to use the maxmind offline database. Call the empty constructor to use the 
 * hostip.info online geocoder.
 * 
 * @author jim
 * @version 0.20 2007-04-03
 */
/*
 * REVISION HISTORY
 * 0.10 2007-04-02 Created
 * 0.20 2007-04-03 Both coders running

 */
public class IPV4Geocoder implements GeocoderI {
	public static final String	PRODUCT_SHORT 	= "IPV4Geocoder";
	public static final String PRODUCT_LONG	= "Challenge/Response IPV4 Geocoder";
	public static final String	VERSION_SHORT	= "0.22";
	public static final String	VERSION_LONG 	=  PRODUCT_LONG + " " + VERSION_SHORT;
	public static final String	COPYRIGHT		= "Copyright (c) 2007 Challenge/Response, LLC, Cambridge, MA";

	
	/**
	 * Parameter for the constructor: geocode() should replace the IP address with 
	 * the place-name, if possible
	 */
	public static final int RETURN_PLACENAME = 1;
	
	/**
	 * Parameter for the constructor: geocode() should return the IP address as the place-name
	 */
	public static final int RETURN_IP = 2;
	
	/** The URL of HOSTIP.INFO API is hardcoded but not final, so could be overridden if necessary */
	public static String HOSTINFO_GEOCODER_URL="http://api.hostip.info";
	

	// and a CheapREST instance
	private CheapREST cheapREST;

	// The LookupService object, for MaxMind.com lookups
	private LookupService maxmindLookupService;
	
	// Return the IP address as the place name, or the real place name?
	private int returnType;

	// pattern for detecting a possible ipv4 address
	private static final Pattern IPV4_PATTERN = 
		Pattern.compile("(\\d{1,3})?\\.(\\d{1,3})?\\.(\\d{1,3})?\\.(\\d{1,3})?");

	
	/**
	 * Return a geocoder that will use EITHER the MaxMind city database locally, or the hostip.info live service.
	 * 
	 * @param maxmindDBLocation if NULL, then hostip.info is used. If non-null, this should be the full path to the MaxMind.com GeoLiteCity.dat database file
	 * @throws IOException if the file could not be opened
	 */
	public IPV4Geocoder(String maxmindDBLocation, int returnType)
	throws IOException {
		this.returnType = returnType;
		// if DB is null, configure for hostip.info
		if (maxmindDBLocation == null) {
			cheapREST = new CheapREST();
			maxmindLookupService = null;
		}
		// otherwise configure for the local database using the maxmind api
		else {
			cheapREST = null;
			maxmindLookupService = new LookupService(maxmindDBLocation,LookupService.GEOIP_MEMORY_CACHE | LookupService.GEOIP_CHECK_CACHE );
		}
	}


	public String getVersion() {
		return VERSION_SHORT;
	}
	
	
	/**
	 * Geocode the provided IP address, returning a GeocodedLocation
	 * @param ipv4Address The ipv4 adress to geocode
	 * @return a GeocodedLocation object for that location, or an object with status code set, even if there was an error
	 */	
	public GeocodedLocation geocode(String ipv4Address) {
		// first qualify the ipv4address -- is it really one? if not, don't even attempt to geocode it
		Matcher m = IPV4_PATTERN.matcher(ipv4Address);
		// does the pattern match generally - if not, bail
		boolean isValid = (m.matches() && (m.groupCount() == 4));
		if (isValid) {
			for (int i = 0; i < 4; i++) {
				if (Integer.parseInt(m.group(i+1)) > 255) {
					isValid = false;
					break;
				}
			}
		}
		
		// bail if this is not an ipv4 address
		if (! isValid)
			return new GeocodedLocation(ipv4Address,
					StatusCode.GEO_UNKNOWN_ADDRESS,
					PrecisionCode.UNKNOWN_LOCATION,"",0.0D,0.0D,0.0D);
		
		// geocode the IP with the configured coder
		if (cheapREST != null)
			return geocodeWithHostIPInfo(ipv4Address);
		else
			return geocodeWithMaxmindGeoIP(ipv4Address);
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


	/**
	 * Shutdown the geocoder, closing open files...
	 */
	public void shutdown() {
		if (maxmindLookupService != null)
			maxmindLookupService.close();
	}


	/**
	 * Geocode a provided IP address using the site hostip.info, returning a GeocodedLocation
	 * @param ipv4Address The ipv4 address to geocode
	 * @return a GeocodedLocation object for that location, or an object with status code set, even if there was an error
	 */	
	private GeocodedLocation geocodeWithHostIPInfo(String ipv4Address) {
		String country = "", city="", latitude="", longitude="";
		String encodedURL = HOSTINFO_GEOCODER_URL+"/get_html.php?ip="+ipv4Address+"&position=true";
		try {
			// fetch a place for this IP address
			String[] ss = cheapREST.getREST(new URL(encodedURL)).split("\n");
			// tease out the label:value pairs for the returned object
			for (int i = 0; i < ss.length; i++) {
				String[] labelValue = ss[i].split(":");
				String label = labelValue[0].trim().toLowerCase();
				if (label.equals("country"))
					country = labelValue[1].trim();
				else if (label.equals("city"))
					city = labelValue[1].trim();
				else if (label.equals("latitude"))
					latitude = labelValue[1].trim();
				else if (label.equals("longitude"))
					longitude = labelValue[1].trim();
			}
			if (returnType == RETURN_IP)
				return new GeocodedLocation(
						ipv4Address,StatusCode.GEO_SUCCESS,PrecisionCode.TOWN,country,latitude,longitude,"0");
			else
				return new GeocodedLocation(
						city,StatusCode.GEO_SUCCESS,PrecisionCode.TOWN,country,latitude,longitude,"0");
		}
		catch (Exception e) { // XML parser error or NumberFormatException or URL formatting exception
		}
		// exceptions fall through to here...
		return new GeocodedLocation(ipv4Address,
				StatusCode.G_GEOCODER_ERROR,
				PrecisionCode.UNKNOWN_LOCATION,"",0.0D,0.0D,0.0D);
	}




	/**
	 * Geocode a provided IP address using the the Maxmind.com GeoIP database, returning a GeocodedLocation
	 * @param ipv4Address The ipv4 address to geocode
	 * @return a GeocodedLocation object for that location, or an object with status code set, even if there was an error
	 */	
	private GeocodedLocation geocodeWithMaxmindGeoIP(String ipv4Address) {
		Location loc;
		try {
			if (maxmindLookupService != null) {
				loc = maxmindLookupService.getLocation(ipv4Address);
				if (returnType == RETURN_IP)
					return new GeocodedLocation(
							ipv4Address,StatusCode.GEO_SUCCESS, PrecisionCode.TOWN,loc.countryName,""+loc.latitude, ""+loc.longitude, "0");
				else {
					StringBuilder city = new StringBuilder();
					city.append(loc.city);
					if (loc.region.length()>0)
						city.append(", "+loc.region);
					if (loc.postalCode.length()>0)
						city.append(" "+loc.postalCode);
					return new GeocodedLocation(
							city.toString(),StatusCode.GEO_SUCCESS, PrecisionCode.TOWN,loc.countryCode,""+loc.latitude, ""+loc.longitude, "0");
				}
			}
		}
		catch (Exception e) { // XML parser error or NumberFormatException or URL formatting exception
		}
		// exceptions fall through to here...
		return new GeocodedLocation(ipv4Address,
				StatusCode.G_GEOCODER_ERROR,
				PrecisionCode.UNKNOWN_LOCATION,"",0.0D,0.0D,0.0D);
	}




	// for testing
	public static void main(String[] args)
	throws GeocoderException, IOException {
		IPV4Geocoder gc = new IPV4Geocoder("/Users/jim/Projects/RandD_Projects/RocketSync/Server/data/GeoLiteCity.dat",RETURN_PLACENAME);
//		IPV4Geocoder gc = new IPV4Geocoder(null,RETURN_IP);

		GeocodedLocation resp;
		resp = gc.geocode("64.95.64.110");
//		resp = gc.geocode("18.85.2.171");
//		resp = gc.geocode("193.111.201.100");
		System.out.println(resp.getPlaceName());
		System.out.println("Server response: "+StatusCode.getText(resp.getServerResponse()));
		System.out.println("Accuracy: "+PrecisionCode.getText(resp.getAccuracy()));
		System.out.println("Country code: "+resp.getCountryCode());
		System.out.println("Latitude: "+resp.getStringLatitude());
		System.out.println("Longitude: "+resp.getStringLongitude());


		System.out.println("Testing geocode(GeocodedLocation(69.181.106.153))");
		GeocodedLocation gcl = new GeocodedLocation("69.181.106.153");
		gc.geocode(gcl);
		System.out.println(resp.getPlaceName());
		System.out.println("Server response: "+StatusCode.getText(gcl.getServerResponse()));
		System.out.println("Place name: "+gcl.getPlaceName());
		System.out.println("Accuracy: "+PrecisionCode.getText(gcl.getAccuracy()));
		System.out.println("Country code: "+resp.getCountryCode());
		System.out.println("Latitude: "+gcl.getStringLatitude());
		System.out.println("Longitude: "+gcl.getStringLongitude());
		

		
		gc.shutdown();

	}



}