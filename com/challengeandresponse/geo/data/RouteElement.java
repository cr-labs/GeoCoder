package com.challengeandresponse.geo.data;

import java.io.Serializable;

import org.joda.time.MutableInterval;

/**
 * A RouteElement is one node in a GeocodedRoute.
 * A GeocodedRoute is an ordered list of GeocodedLocations, each of which may also carry a time interval that indicates when the entity 
 * whose travel is described by the route will/did appear at the given location.
 * 
 * This is a naive implementation of the object. It will probably change based on experience and real-world needs of nodes that have an
 * interest in GeocodedRoute objects. 
 * 
 * @author jim
 * @version 2010-07-11
 */

/*
 * REVISION HISTORY
 * 2010-07-11 class moved from RocketSync3: com.rocketsync.core to CR-Geocoder: com.challengeandresponse.geo.data
 * 
 */
public class RouteElement
implements Serializable {
	private static final long serialVersionUID = 1L;

	private GeocodedLocation location;
	private MutableInterval time;

	public RouteElement() {
	}
	
	public RouteElement(GeocodedLocation location, MutableInterval time) {
		this.location = location;
		this.time = time;
	}
	
	public GeocodedLocation getLocation() {
		return location;
	}

	public MutableInterval getTime() {
		return time;
	}

	@Override
	public String toString() {
		return "RouteElement [location=" + location + ", time=" + time + "]";
	}

	
	
	
}