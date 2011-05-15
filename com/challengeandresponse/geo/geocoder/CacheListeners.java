package com.challengeandresponse.geo.geocoder;

import net.sf.ehcache.*;
import net.sf.ehcache.event.CacheEventListener;

import com.challengeandresponse.geo.data.GeocodedLocation;


/**
 * Implements only notifyElementPut() to set short timers on 
 * Elements that are negative responses, so they are only
 * cached briefly
 * 
 * @author jim
 *
 */

public class CacheListeners implements CacheEventListener {
	
	private int negativeCacheTTLSec = 10;
	
	public CacheListeners() { }
	
	/**
	 * Set the TTL in seconds for "negative cached" records... that is, failed responses that are cached
	 * The default is 10 sec but it could and should probably be longer in many cases.
	 * @param ttlSec
	 */
	public void setNegativeCacheTTLSec(int ttlSec) {
		negativeCacheTTLSec = ttlSec;
	}
	
	
	public void dispose() {
	}

	public void notifyElementEvicted(Ehcache arg0, Element arg1) {
	}

	public void notifyElementExpired(Ehcache arg0, Element arg1) {
	}

	/**
	 * Set short timers on elements that are "negative" responses,
	 * by changing their TimeToIdle and TimeToLive to 'negativeCacheTTLSec'
	 */
	public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
		if (element.getValue() instanceof GeocodedLocation) {
			GeocodedLocation gcl = (GeocodedLocation) element.getValue();
			if (! gcl.isOK()) {
				element.setTimeToIdle(negativeCacheTTLSec);
				element.setTimeToLive(negativeCacheTTLSec);
				cache.putQuiet(element); // replace the element with short timers
			}
		}
	}

	public void notifyElementRemoved(Ehcache arg0, Element arg1) throws CacheException {
	}

	public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
		if (element.getValue() instanceof GeocodedLocation) {
			GeocodedLocation gcl = (GeocodedLocation) element.getValue();
			if (! gcl.isOK()) {
				element.setTimeToIdle(negativeCacheTTLSec);
				element.setTimeToLive(negativeCacheTTLSec);
				cache.putQuiet(element); // replace the element with short timers
			}
		}
	}

	public void notifyRemoveAll(Ehcache arg0) {
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
}