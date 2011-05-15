/**
 * 
 */
package com.challengeandresponse.geo.data;

import java.io.Serializable;
import java.util.*;

import org.joda.time.MutableInterval;

/**
 * 
 * A GeocodedRoute is an ordered list of GeocodedLocations, each of which may also carry a time interval that indicates when the entity 
 * whose travel is described by the route will/did appear at the given location.
 * 
 * This is a naive implementation of the object. It will probably change based on experience and real-world needs of nodes that have an
 * interest in GeocodedRoute objects.
 * 
 * @author jim
 *
 */

/*
 * REVISION HISTORY
 * 2010-07-11 class moved from RocketSync3: com.rocketsync.core to CR-Geocoder: com.challengeandresponse.geo.data
 * 
 */
public class GeocodedRoute 
implements Serializable {
	private static final long serialVersionUID = 1L;


	private List <RouteElement> route;
	
	/**
	 * 
	 */
	public GeocodedRoute() {
		route = new ArrayList <RouteElement> ();
	}

	/**
	 * @param routeElements	A populated List of RouteElements to store in this object
	 * @throws IllegalArgumentException
	 */
	public GeocodedRoute(List <RouteElement> routeElements) 
	throws IllegalArgumentException {
		this.route = routeElements;
	}

	
	public Iterator <RouteElement> getRouteElementIterator() {
		return route.iterator();
	}
	
	/**
	 * Create a new RouteElement having the specified GeocodedLocation and time interval, and append it to this route
	 * @param gcl		The GeocodedLocation to append to this route
	 * @param interval	The valid interval for gcl on this route
	 */
	public void append(GeocodedLocation gcl, MutableInterval interval) {
		RouteElement re = new RouteElement(gcl,interval);
		this.route.add(re);
	}
	
	/**
	 * Append a pre-existing RouteElement to this route
	 * @param re the RouteElement to append
	 */
	public void append(RouteElement re) {
		this.route.add(re);
	}
	
	
	
	@Override
	public String toString() {
		return "BoundedGeocodedRoute [route=" + route + "]"+super.toString();
	}

	// for testing
	public static void main(String[] args) {
		GeocodedRoute bgr = new GeocodedRoute();
		bgr.append(new GeocodedLocation(""),new MutableInterval());
		System.out.println(bgr);
	}

}
