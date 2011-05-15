package com.challengeandresponse.geo.data;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.text.DecimalFormat;

import com.challengeandresponse.geo.core.WhenAndWhereUtils;


/**
 * A point, perhaps expandable later to include regions, but for now, this is a high precision 
 * Latitude/Longitude/Height object with normalized values. It is serializable.
 * 
 * The underlying form for the stored values is BigDecimal, allowing for precision and comparability,
 * and for compatibility with the GeoUtils Wherehoo library <b>but this library will be
 * transitioned away from BigDecimal for performance reasons - and as a practical matter, the
 * higher resolution of BigDecimal isn't necessarily useful compared with the high cost
 * of computation using BigDecimals.</b></p>
 * 
 * <p>Note that ROUNDING MODE and DECIMALS are static variables. When changed, all LLH objects
 * are affected.</p>
 * <p>When a latitude or longitude value is set (in a constructor or in a method), the values
 * are normalized so they always fall within the ranges below:<br />
 * LATITUDE: { -90.0 <= latitude <= 90.0 }<br>
 * LONGITUDE: { -180.0 <= longitude <= 180.0 }<br />
 * If initial values are outside these ranges, they are changed when set, so that LLH's accessors never
 * returns a value outside those ranges.<br />
 *
 * @author jim
 * @version 2009-11-05 0.42
 */

/*
 * REVISION HISTORY
 *  2007-02-03 0.20 Made rounding mode and decimals private, and their accessor methods static
 *  2007-02-05 0.25 Added hashCode() and equals() methods for caching compatibility
 *  2007-04-02 0.30 Now extends UniversalIQRPC for compatibility with IMOperator M2M communications
 *  2007-05-08 0.40 Converted to use all primitive 'double' values rather than BigDecimals
 *  2007-06-01 0.41 Renamed from MMWhere to MMLocation for consistency with peer- and sub-classes
 *  2009-11-05 0.42 Another simplification. All MM stuff is now removed because RocketSync2 provides the RSObject wrapper class for object communications
 *  
 *  (c) 2007 - 2009 Challenge/Response, LLC
 *
 */

/*
 * KNOWN ISSUES
 * TODO: The normalize() method is not optimized well. The necessary transforms can be accomplished with fewer 'if' statements
 */

public class Location implements Serializable {
	private static final long serialVersionUID = 1L;
	public static transient final String VERSION = "0.42";

	private double	latitude;
	private double	longitude;
	private double	height;
	private int 	locationType; // should be a constant from the Type class
	private int		radiusMeters; // only considered when locationType is RADIUS

	public static final class Type {
		public static final int POINT = 1;
		public static final int RADIUS = 2;
	}
	
	/**
	 * Default number of decimals to retain when rounding the doubles to make formatted strings with DecimalFormat.format
	 */
	private static transient int decimalPlaces = 4;


	public Location(double latitude, double longitude, double height, int locationType, int radiusMeters) {
		setLocationType(locationType);
		setRadiusMeters(radiusMeters);
		set(latitude,longitude,height);
	}

	public Location(double latitude, double longitude, double height) {
		setLocationType(Type.POINT);
		setRadiusMeters(0);
		set(latitude,longitude,height);
	}

	public Location(String latitude, String longitude, String height) {
		setLocationType(Type.POINT);
		setRadiusMeters(0);
		set(latitude,longitude,height);
	}

	public Location(Point2D.Double latlon, double height) {
		setLocationType(Type.POINT);
		setRadiusMeters(0);
		set(latlon.x,latlon.y,height);
	}
	
	public Location() {
		setLocationType(Type.POINT);
		setRadiusMeters(0);
		set(0.0D,0.0D,0.0D);
	}


	
	public int getLocationType() {
		return locationType;
	}

	public void setLocationType(int locationType) {
		this.locationType = locationType;
	}

	public int getRadiusMeters() {
		return radiusMeters;
	}

	public void setRadiusMeters(int radiusMeters) {
		this.radiusMeters = radiusMeters;
	}

	/**
	 * Set latitude, longitude and height from double values
	 * @param latitude
	 * @param longitude
	 * @param height
	 */
	public void set(double latitude, double longitude, double height) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.height = height;
		normalize();
	}

	/**
	 * Set latitude, longitude and height from String values (allows for no rounding error)
	 * @param latitude
	 * @param longitude
	 * @param height
	 * @throws NumberFormatException if any of the values can't be interpreted as BigDecimals
	 */
	public void set(String latitude, String longitude, String height)
	throws NumberFormatException {
		this.latitude = Double.parseDouble(latitude);
		this.longitude = Double.parseDouble(longitude);
		this.height = Double.parseDouble(height);
		normalize();
	}

	/**
	 * Set the number of decimal places for values returned by get() calls
	 * @param decimalPlaces the number of decimal places. If not explicitly set, the default is 4 places
	 */
	public static void setDecimalPlaces(int decimalPlaces) {
		Location.decimalPlaces = decimalPlaces;
	}
	
	/**
	 * Get the number of decimal places for all get() calls to LLH values
	 */
	public static int getDecimalPlaces() {
		return Location.decimalPlaces;
	}
	
	
	/**
	 * @return the latitude, rounded by ROUNDING_MODE to DECIMALS places, as a String
	 */
	public String getStringLatitude() {
		return roundToString(this.latitude);
	}

	/**
	 * @return the longitude, rounded by ROUNDING_MODE to DECIMALS places, as a String
	 */
	public String getStringLongitude() {
		return roundToString(this.longitude);
	}

	/**
	 * @return the height, rounded by ROUNDING_MODE to DECIMALS places, as a String
	 */
	public String getStringHeight() {
		return roundToString(this.height);
	}

	/**
	 * @return  the latitude as a primitive double, rounded by ROUNDING_MODE to DECIMALS places
	 */
	public double getDoubleLatitude() {
		return this.latitude;
	}

	/**
	 * @return the longitude as a primitive double, rounded by ROUNDING_MODE to DECIMALS places
	 */
	public double getDoubleLongitude() {
		return this.longitude;
	}

	/**
	 * @return the height as a primitive double, rounded by ROUNDING_MODE to DECIMALS places
	 */
	public double getDoubleHeight() {
		return this.height;
	}


	/**
	 * Distance from this LLH to another, using a fast algorithm operating on primitive doubles
	 * @param otherPoint
	 * @return the distance
	 */
	public double distance(Location otherPoint) {
		return WhenAndWhereUtils.distance(this,otherPoint);
	}
	
	/**
	 * Distance from this LLH to another, using a fast algorithm operating on primitive doubles,
	 * and rounded
	 * @param otherPoint
	 * @return the distance
	 */
	public String stringDistance(Location otherPoint) {
		return roundToString(WhenAndWhereUtils.distance(this,otherPoint));
	}
		
		
	public static String roundToString(double d) {
		DecimalFormat fmt = new DecimalFormat();
		fmt.setMinimumFractionDigits(decimalPlaces);
		fmt.setMaximumFractionDigits(decimalPlaces);
		return fmt.format(d);
	}

	
	
	
	@Override
	public String toString() {
		return "Location [height=" + height + ", latitude=" + latitude
				+ ", locationType=" + locationType + ", longitude=" + longitude
				+ ", radiusMeters=" + radiusMeters + "]";
	}
	
	
	/**
	 * Generate a hashcode based on the content of the object... default behaviour
	 * is to use the OID, which prevents comparisons across execution instances, as well
	 * as across multiple objects that have the same content.
	 */
	public int hashCode() {
		return (roundToString(latitude)+" "+roundToString(longitude)+" "+roundToString(height)).hashCode();
	}
	
	
	/**
	 * Compare the hashes
	 */
	public boolean equals(Object o) {
		Location w = (Location) o;
		return ( 
			(w.latitude == this.latitude) &&
			(w.longitude == this.longitude) &&
			(w.height == this.height)
		);
	}




	/**
	 * Normalizes the latitude and longitude values of this object so they fall within
	 * conventional ranges, which are:<br />
	 * 
	 * LATITUDE: { -90.0 <= latitude <= 90.0 }<br>
	 * LONGITUDE: { -180.0 <= longitude <= 180.0 }<br />
	 */
	private void normalize(){
		// LATITUDE: rein in to { -90.0 < latitude < 90.0 }
		// First, pull down to (-360.0 < latitude < 360.0) range
		int adjust = (int) (latitude / 360.0D);
		latitude = latitude - (360.0D * adjust);
		
		// convert to { -90.0 < n < 90.0 }
		if ((latitude >= 0.0D) && (latitude < 90.0D)) {
			// do nothing, it's fine
		}
		else if ((latitude >= 90.0D) && (latitude < 180.0D)) {
			latitude = 180.0D - latitude;
		}
		else if ((latitude >= 180.0D) && (latitude < 270.0D)) {
			latitude = latitude - 180.0D;
			latitude = -latitude;
		}
		else if ((latitude >= 270.0D) && (latitude < 360.0D)) {
			latitude = 360.0D - latitude;
			latitude = -latitude;
		}
		else if ((latitude <= 0.0D) && (latitude > -90.0D)) {
			// do nothing, it's fine
		}
		else if ((latitude <= -90.0D) && (latitude > -180.0D)) {
			latitude = 180.0D + latitude;
			latitude = -latitude;
		}
		else if ((latitude <= -180.0D) && (latitude > -270.0D)) {
			latitude = latitude + 180.0D;
			latitude = -latitude;
		}
		else if ((latitude <= -270.0D) && (latitude > -360.0D)) {
			latitude = latitude + 360.0D;
		}
		
		// LONGITUDE: rein in to { -180.0 < longitude <= 180.0 }
		// First, pull down to (-360.0 < longitude < 360.0) range
		adjust = (int) (longitude / 360.0D);
		longitude = longitude - (adjust * 360.0D);
		
		// convert to { -180.0 < n < 180.0 }
		if ((longitude >= 0.0D) && (longitude <= 180.0D)) {
			// do nothing, it's fine
		}
		else if ((longitude > 180.0D) && (longitude < 360.0D)) {
			longitude = longitude - 360.0D;
		}
		else if ((longitude <= 0.0D) && (longitude > -180.0D)) {
			// do nothing, it's fine
		}
		else if ((longitude <= -180.0D) && (longitude > -360.0D)) {
			longitude = longitude + 360.0D;
		}
	}

	


	
	
	
	// TESTING
	public static void main(String[] args) {
//		double d;
//		Where where = new Where(0.0D,0.0D,0.0D);
//		for (d = -360.0D; d <= 360.0D; d+=0.0011D) {
//			System.out.print(" ORIGINAL VAL: "+d);
//			where.set(d,d,0.0D);
//			System.out.println(" final Lat,Lon: "+where.getLatitude()+","+where.getLongitude());
//		}

		Location w1 = new Location();
		Location w2 = new Location();
		
		w1 = new Location("42","-71","0");
		w2 = new Location(403.0D,289.0D,0.0D);
		w1.set("42.01","-71.02","3");
//		w1.set(42.04,-71.05,6);
		System.out.println(w1);
		System.out.println(w2);
		System.out.println(w1.stringDistance(w2));
		
		System.out.println(w1.getDoubleLatitude());
		System.out.println(w1.getDoubleLongitude());
		System.out.println(w1.getDoubleHeight());
		System.out.println(w1.getStringLatitude());
		System.out.println(w1.getStringLongitude());
		System.out.println(w1.getStringHeight());
		
		System.out.println(w2.getDoubleLatitude());
		System.out.println(w2.getDoubleLongitude());
		System.out.println(w2.getDoubleHeight());
	}
	
	
	
}
