package com.challengeandresponse.geo.geocoder;

import java.io.IOException;

import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

import com.challengeandresponse.centralcache.CentralCache;
import com.challengeandresponse.centralcache.CentralCacheException;
import com.challengeandresponse.geo.data.GeocodedLocation;
import com.challengeandresponse.geo.geocoders.*;

/**
 * A caching General Geocoder that uses a Self-Populating EHCache.
 * 
 * Cached, geocoded address found: 28 to 30 msec<br />
 * Geocoding required, consulting Google only: 260 to 3600 msec<br /></p>
 * 
 * If using this class, the following libraries are needed:<br />
 * <code>
 * EHCache<br />
 * Commons Logging (a dependency of EHCache)<br />
 * 
 * @author jim
 */

public class CachingGeocoder extends Geocoder
implements CacheEntryFactory, GeocoderI {

	public static final int MINUTES = CentralCache.MINUTES;
	public static final int HOURS = MINUTES * 60;
	public static final int DAYS = HOURS * 24;
	

	private CentralCache ccache;
	private String cacheName;

	/**
	 * Create a Caching Geocoder with a self-populating cache based on EHCACHE
	 * @param cacheFilePath the path to the cache file
	 * @param cacheName the name of THIS cache
	 * @param maxElementsInMemory The maximum number of objects that will be maintained in memory for this cache
	 * @param eternal If true, TTL is ignored and the elements never expire
	 * @param timeToLiveSeconds If 0, an object can live forever. This is the max time between creation time and the time an object expires
	 * @param timeToIdleSeconds If 0, an object can idle forever. This is the max time between accesses before an object expires
	 * @param diskExpiryThreadIntervalSeconds Number of seconds between runs of the disk expiry thread. Default is 120 seconds.
	 * @param maxElementsOnDisk If 0, unlimited, otherwise the max number of objects that will be maintained in the DiskStore
	 * @param negativeCacheTTLSec number of seconds to cache negative query results (as DNS does). Much shorter than positive result caching
	 * @throws GeocoderException if an exception was thrown trying to create the new geocoder (probably a cache-related problem)
	 */
	public CachingGeocoder(String cacheFilePath, String cacheName,
			int maxElementsInMemory, boolean eternal, long timeToLiveSeconds, long timeToIdleSeconds,
			long diskExpiryThreadIntervalSeconds, int maxElementsOnDisk, int negativeCacheTTLSec)
	throws GeocoderException {
		// create the GGeocoder
		this.cacheName = cacheName;
		// create the cache
		ccache = new CentralCache(cacheFilePath);
		try {
			ccache.addSelfPopulatingCache(cacheName,maxElementsInMemory,eternal,timeToLiveSeconds,timeToIdleSeconds,
					diskExpiryThreadIntervalSeconds,maxElementsOnDisk,this);
			// create the cacheListeners for negative caching
			CacheListeners cl = new CacheListeners();
			cl.setNegativeCacheTTLSec(negativeCacheTTLSec);
			ccache.getRegisteredListeners(cacheName).registerListener(cl);
		}
		catch (CentralCacheException cce) {
			throw new GeocoderException(cce.getMessage());
		}
	}


	/**
	 * This is the pull-thru method to get a new item into the cache. It calls the superclass's GGeocode.geocode() on the placename
	 * @param placeName the place name to geocode and add to the cache. Must be non-null, and must be a String.
	 * @throws GeocoderException if the address is not a String object
	 */
	public Object createEntry(Object placeName)
	throws GeocoderException {
		if (! (placeName instanceof String))
			throw new GeocoderException("placeName must be a String, containing the location to geocode");
		return super.geocode((String) placeName);
	}


	/**
	 * Geocode a placename, retrieving it from the cache if possible or getting a new one if it's not in the cache
	 * @return the geocoded placename with status flags set
	 */
	public GeocodedLocation geocode(String placeName) {
		try {
			Element el = ccache.getEhcache(cacheName).get(placeName);
			if (el == null)
				return null;
			else
				return (GeocodedLocation) el.getValue();
		}
		catch (CentralCacheException cce) {
			return new GeocodedLocation(placeName.trim(),StatusCode.G_GEOCODER_ERROR,
					PrecisionCode.UNKNOWN_LOCATION,"",0.0D,0.0D,0.0D);
		}
	}	



	/**
	 * Shutdown the cache in an orderly way. Should call this whenever
	 * possible, when terminating the application, so caches are flushed to disk
	 */
	public void shutdown() {
		ccache.shutdown();
	}

	/**
	 * Return the Statistics object for this cache
	 * @return Statistics object for this cache
	 */	
	public Statistics getStatistics() {
		return ccache.getStatistics(cacheName);
	}

	
	
	
	
	
	/// TESTING
	public static void main(String[] args) {
		System.out.println("CachingGGeocoder test running");
		CachingGeocoder cgc = null;

		try {
			cgc = new CachingGeocoder(
					"/tmp/geocodercache",	// path to cache
					"geocodercache",		// cache name
					2000,					// keep up to 2,000 elements in memory
					false,					// elements are not eternal... they expire
					21 * 24 * 60 * 60, 		// TTL of any object = 21 days
					14 * 24 * 60 * 60, 		// Time to Idle  = 14 days, purge if not accessed in 14 days
					6 * 60 * 60, 			// run disk expiry thread every 6 hours
					40000,					// Keep up to 40,000 elements on disk
					40						// negative cache time 40 seconds
					); 				

			cgc.addGeocoder(new IPV4Geocoder("/Users/jim/Projects/RandD_Projects/RocketSync/Server/data/GeoLiteCity.dat",IPV4Geocoder.RETURN_IP));
			cgc.addGeocoder(new LatLonGeocoder());
			cgc.addGeocoder(new GoogleGeocoder("ABQIAAAATpIP0nQMoP5fnPlNJxbLDBSPCd7MrKeM6w_jwe7QFLxKoZJC8xQyh58lNpVG1DTdJcKkrbIPUMpofA"));
			cgc.addGeocoder(new GeonamesGeocoder());
			cgc.addGeocoder(new YahooGeocoder("eyqBFmfIkY1wnsE_EUd1cIQfIvnvI2PisDg-"));
		
			
			dump(cgc.geocode("860 Nancy Street, Niles, OH"));
			dump(cgc.geocode("860 Nancy Street, Niles, OH"));
			dump(cgc.geocode("955 Massachusetts Ave, Cambridge, MA"));
			dump(cgc.geocode("London, England"));
			dump(cgc.geocode("Hope, Arkansas"));
			dump(cgc.geocode("43512"));
			dump(cgc.geocode("42.128,-071.235"));
			dump(cgc.geocode("18.85.1.171"));
			dump(cgc.geocode("64.95.64.96"));
			dump(cgc.geocode("Cleveland, OH"));
			dump(cgc.geocode("Dali, China"));
			dump(cgc.geocode("Hope, Arkansas"));
			dump(cgc.geocode("*** this is bad input, this cannot be geocoded"));
			dump(cgc.geocode("Southern Park Mall"));
			dump(cgc.geocode("Mont Blanc Italy"));
			dump(cgc.geocode("12.48815,44.128812"));
		}
		catch (GeocoderException cce) {
			System.out.println("CentralCacheException: "+cce.getMessage());
		}
		catch (IOException ioe) {
			System.out.println("IO Exception: "+ioe.getMessage());
		}
		
		System.out.println("Cache statistics");
		Statistics stats = cgc.getStatistics();
		System.out.println("Object count "+stats.getObjectCount());
		System.out.println("Cache hits "+stats.getCacheHits());
		System.out.println("Cache misses "+stats.getCacheMisses());
		System.out.println("In memory hits "+stats.getInMemoryHits());
		System.out.println("On disk hits "+stats.getOnDiskHits());

		System.out.println("Shutting down cache");
		cgc.shutdown();
	} // end of TESTING


	static void dump(GeocodedLocation gcl) {
		System.out.println("placeName: "+gcl.getPlaceName());
		System.out.println("geocoder: "+gcl.getGeocoder());
		System.out.println("Server response: "+StatusCode.getText(gcl.getServerResponse()));
		System.out.println("Accuracy: "+PrecisionCode.getText(gcl.getAccuracy()));
		System.out.println("Latitude: "+gcl.getStringLatitude());
		System.out.println("Longitude: "+gcl.getStringLongitude());
		System.out.println("====================");
	}
	

}