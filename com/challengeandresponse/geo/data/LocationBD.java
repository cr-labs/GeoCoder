package com.challengeandresponse.geo.data;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.challengeandresponse.geo.core.WhenAndWhereUtilsBD;


/**
 * THIS IS BASED ON THE ORIGINAL MMWHERE -- using BigDecimals for storage. This caused all kinds
 * of problems with db4o because BigDecimal objects have internally transient fields and
 * when pulled back from the database, they look empty. Sigh. So this class was 
 * renamed MMWhereBD and the new class MMWhere was created to use regular decimals...
 * 
 * <p><b>This class should probably be deprecated. Just not enough goodness in BigDecimals
 * to justify the overhead</b></p>
 * 
 * <p>A point, perhaps expandable later to include regions, but for now, this is a high precision 
 * Latitude/Longitude/Height object with normalized values. It is serializable.</p>
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
 * @version 2009-11-05 0.32
 */

/*
 * REVISION HISTORY
 *  2007-02-03 0.20 Made rounding mode and decimals private, and their accessor methods static
 *  2007-02-05 0.25 Added hashCode() and equals() methods for caching compatibility
 *  2007-04-02 0.30 Now extends UniversalIQ for compatibility with IMOperator M2M communications
 *  2007-06-01 0.31 Renamed to MMLocationBD, even though it's to be deprecated, to keep naming consistent with the MMLocation class (formerly MMWhere)
 *  2009-11-04 0.32 Another simplification. All MM stuff is now removed because Rocketsync provides a wrapper for object communications
 *  
 *  (c) 2007 -2009 Challenge/Response, LLC
 */

/*
 * KNOWN ISSUES
 * TODO: The normalize() method is not optimized well. The necessary transforms can be accomplished 
 * with fewer 'if' statements
 * TODO: Consider moving away from BigDecimal and back to regular doubles... they're possibly stable enough in the
 * high order bits (this class must provide stable ROUNDING on all get() operations).. however, no
 * pretty rounding as with BigDecimal
 */

public class LocationBD 
implements Serializable {
	private static final long serialVersionUID = 1L;

	private BigDecimal	latitude;
	private BigDecimal	longitude;
	private BigDecimal	height;
	
	  /**
	  * Default ROUNDING_MODE is ROUND_HALF_EVEN.
	  */
	  private static int roundingMode = BigDecimal.ROUND_HALF_EVEN;

	  /**
	  * Default number of decimals to retain, also referred to as "scale". Default is 4.
	  */
	  private static int decimalPlaces = 4;

	
	public LocationBD(double latitude, double longitude, double height) {
		set(latitude,longitude,height);
	}

	public LocationBD(String latitude, String longitude, String height) {
		set(latitude,longitude,height);
	}

	public LocationBD(Point2D.Double latlon, double height) {
		set(latlon.x,latlon.y,height);
	}
	
	public LocationBD() {
		set(0.0D,0.0D,0.0D);
	}


	
	/**
	 * Set latitude, longitude and height from double values
	 * @param latitude
	 * @param longitude
	 * @param height
	 */
	public void set(double latitude, double longitude, double height) {
		this.latitude = new BigDecimal(latitude);
		this.longitude = new BigDecimal(longitude);
		this.height = new BigDecimal(height);
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
		this.latitude = new BigDecimal(latitude);
		this.longitude = new BigDecimal(longitude);
		this.height = new BigDecimal(height);
		normalize();
	}

	/**
	 * Set the number of decimal places for values returned by get() calls
	 * @param decimalPlaces the number of decimal places. If not explicitly set, the default is 4 places
	 */
	public static void setDecimalPlaces(int decimalPlaces) {
		LocationBD.decimalPlaces = decimalPlaces;
	}
	
	/**
	 * Get the number of decimal places for all get() calls to LLH values
	 */
	public static int getDecimalPlaces() {
		return LocationBD.decimalPlaces;
	}
	
	/**
	 * Set the rounding mode for values returned by get() calls. If not explicitly set, the default is ROUND_HALF_EVEN. See BigDecimal for other rounding values
	 * @param rounding
	 */
	public static void setRoundingMode(int rounding) {
		LocationBD.roundingMode = rounding;
	}

	/**
	 * See BigDecimal for the rounding modes and their constant values.
	 * @return the currently set rounding mode. 
	 */
	public static int getRoundingMode() {
		return LocationBD.roundingMode;
	}
	
	/**
	 * @return the latitude, rounded by ROUNDING_MODE to DECIMALS places
	 */
	public BigDecimal getLatitude() {
		return round(this.latitude);
	}

	/**
	 * @return the longitude, rounded by ROUNDING_MODE to DECIMALS places
	 */
	public BigDecimal getLongitude() {
		return round(this.longitude);
	}

	/**
	 * @return the height, rounded by ROUNDING_MODE to DECIMALS places
	 */
	public BigDecimal getHeight() {
		return round(this.height);
	}

	/**
	 * @return  the latitude as a primitive double, rounded by ROUNDING_MODE to DECIMALS places
	 */
	public double getDoubleLatitude() {
		return round(this.latitude).doubleValue();
	}

	/**
	 * @return the longitude as a primitive double, rounded by ROUNDING_MODE to DECIMALS places
	 */
	public double getDoubleLongitude() {
		return round(this.longitude).doubleValue();
	}

	/**
	 * @return the height as a primitive double, rounded by ROUNDING_MODE to DECIMALS places
	 */
	public double getDoubleHeight() {
		return round(this.height).doubleValue();
	}


	/**
	 * Distance from this LLH to another, using a fast algorithm operating on primitive doubles
	 * @param otherPoint
	 * @return the distance from here to there
	 */
	public double distance(LocationBD otherPoint) {
		return WhenAndWhereUtilsBD.distance(this,otherPoint);
	}
	
		
	private BigDecimal round(BigDecimal value){
		return value.setScale(decimalPlaces, roundingMode);
	}
	
	
	public String toString() {
		return getDoubleLatitude() + " " + getDoubleLongitude() + " " + getDoubleHeight();
	}
	
	
	/**
	 * Generate a hashcode based on the content of the object... default behaviour
	 * is to use the OID, which prevents comparisons across execution instances, as well
	 * as across multiple objects that have the same content.
	 */
	public int hashCode() {
		return (latitude.toString()+" "+longitude.toString()+" "+height.toString()).hashCode();
	}
	
	
	/**
	 * Compare the hashes
	 */
	public boolean equals(Object o) {
		LocationBD w = (LocationBD) o;
		return ( 
			w.latitude.equals(this.latitude) &&
			w.longitude.equals(this.longitude) &&
			w.height.equals(this.height)
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
		final BigDecimal _180 = new BigDecimal("180.0");
		final BigDecimal _360 = new BigDecimal("360.0");

		BigInteger adjust;
		double dblVal;

		// LATITUDE: rein in to { -90.0 < latitude < 90.0 }
		// First, pull down to (-360.0 < latitude < 360.0) range
		adjust = (latitude.divide(_360,BigDecimal.ROUND_DOWN)).toBigInteger();
		latitude = latitude.subtract((new BigDecimal(adjust)).multiply(_360));
		
		// convert to { -90.0 < n < 90.0 }
		dblVal=latitude.doubleValue();

		if ((dblVal >= 0.0D) && (dblVal < 90.0D)) {
			// do nothing, it's fine
		}
		else if ((dblVal >= 90.0D) && (dblVal < 180.0D)) {
			latitude = _180.subtract(latitude);
		}
		else if ((dblVal >= 180.0D) && (dblVal < 270.0D)) {
			latitude = latitude.subtract(_180);
			latitude = latitude.negate();
		}
		else if ((dblVal >= 270.0D) && (dblVal < 360.0D)) {
			latitude = _360.subtract(latitude);
			latitude = latitude.negate();
		}
		else if ((dblVal <= 0.0D) && (dblVal > -90.0D)) {
			// do nothing, it's fine
		}
		else if ((dblVal <= -90.0D) && (dblVal > -180.0D)) {
			latitude = _180.add(latitude);
			latitude = latitude.negate();
		}
		else if ((dblVal <= -180.0D) && (dblVal > -270.0D)) {
			latitude = latitude.add(_180);
			latitude = latitude.negate();
		}
		else if ((dblVal <= -270.0D) && (dblVal > -360.0D)) {
			latitude = latitude.add(_360);
		}

		
		// LONGITUDE: rein in to { -180.0 < longitude <= 180.0 }
		// First, pull down to (-360.0 < longitude < 360.0) range
		adjust = (longitude.divide(_360,BigDecimal.ROUND_DOWN)).toBigInteger();
		longitude = longitude.subtract((new BigDecimal(adjust)).multiply(_360));
		
		// convert to { -180.0 < n < 180.0 }
		dblVal=longitude.doubleValue();

		if ((dblVal >= 0.0D) && (dblVal <= 180.0D)) {
			// do nothing, it's fine
		}
		else if ((dblVal > 180.0D) && (dblVal < 360.0D)) {
			longitude = longitude.subtract(_360);
		}
		else if ((dblVal <= 0.0D) && (dblVal > -180.0D)) {
			// do nothing, it's fine
		}
		else if ((dblVal <= -180.0D) && (dblVal > -360.0D)) {
			longitude = longitude.add(_360);
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

		LocationBD w1 = new LocationBD("42","-71","0");
		LocationBD w2 = new LocationBD(403.0D,289.0D,0.0D);
		w1.set("42.01","-71.02","3");
		w1.set(42.04,-71.05,6);
		System.out.println(w1);
		System.out.println(w2);
		System.out.println(w1.distance(w2));
		
		System.out.println(w1.getDoubleHeight());
		System.out.println(w1.getDoubleLatitude());
		System.out.println(w1.getDoubleLongitude());
		
		System.out.println(w2.getDoubleHeight());
		System.out.println(w2.getDoubleLatitude());
		System.out.println(w2.getDoubleLongitude());
	
	}
	
	
	
}
