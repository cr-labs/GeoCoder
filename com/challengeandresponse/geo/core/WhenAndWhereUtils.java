package com.challengeandresponse.geo.core;

import com.challengeandresponse.geo.data.Location;

/**
 * PLACE and TIME utility methods... formatting, display, things like that.
 * 
 * @author jim
 *
 */
public class WhenAndWhereUtils {

	// precalculated constants for geo calculations
	private static final double GEO_A = 6378137.0;
	private static final double GEO_E2 = 0.00669437999013;	// Eccentricity squared
	private static final double R_A = (GEO_A * Math.sqrt(1 - GEO_E2)); // solve part of R in advance because it's constant
	private static final double PI_TIMES_2 = (Math.PI * 2.0D);

	/**
	 * @param seconds	number of seconds to convert to days/hours/minutes/seconds
	 * @param showSeconds set true to include seconds, false to stop at nearest minute
	 * @return seconds converted to a nice "xx days, xx hours, xx minutes, xx seconds" string
	 */
	public static String hoursAndMinutesFromSeconds(int seconds, boolean showSeconds) {
		return hoursAndMinutesFromMilliseconds((long) (seconds * 1000),showSeconds);
	}


	/**
	 * @param milliseconds	number of seconds to convert to days/hours/minutes/seconds
	 * @param showSeconds set true to include seconds, false to stop at nearest minute
	 * @return milliseconds converted to a nice "xx days, xx hours, xx minutes, xx seconds" string
	 */
	public static String hoursAndMinutesFromMilliseconds(long milliseconds, boolean showSeconds) {
		int uptime = (int) (milliseconds/1000);
		int days = (uptime / 60 / 60 / 24);
		uptime -= days * 60 * 60 *24;
		int hours = (uptime / 60 / 60);
		uptime -= hours * 60 * 60;
		int minutes = uptime / 60;
		uptime -= minutes * 60;
		int seconds = uptime;

		StringBuffer s = new StringBuffer();
		if (days > 0) {
			s.append(days);
			s.append(" day");
			s.append(days > 1 ? "s ":" ");
		}
		if (hours > 0) {
			s.append(hours);
			s.append(" hour");
			s.append(hours > 1 ? "s ":" ");
		}
		// show minutes if any units follow
		if (minutes > 0) {
			s.append(minutes);
			s.append(" minute");
			s.append(minutes != 1 ? "s":"");
		}
		if (showSeconds) {
			s.append(" "+seconds);
			s.append(" second");
			s.append(seconds != 1 ? "s ":"");
		}
		return s.toString();
	}




	
	/**
	 * Calculates the distance between two points, <tt>origin</tt> and <tt>end</tt> using regular primitive math operations, for speed
	 * Derived from information provided at  http://www.census.gov/cgi-bin/geo/gisfaq?Q5.1
	 * and with a nod to original Perl code by Darrell Kindred <dkindred@cmu.edu> (1998)
	 * which he has released into the public domain.

	 * From Darrell Kindred's notes:
	 *	Calculations assume a spherical Earth with radius 6367 km.  
	 *	(I think this should cause no more than 0.2% error.)
	 *	For a good discussion of the formula used here for calculating distances,
	 *	as well as several more and less accurate techniques, see
	 *  http://www.best.com/~williams/avform.htm and http://www.census.gov/cgi-bin/geo/gisfaq?Q5.1

	 * From the census website cited above:

	 * Otherwise, presuming a spherical Earth with radius R (see below), and 
	 * the locations of the two points in spherical coordinates (longitude 
	 * and latitude) are lon1,lat1 and lon2,lat2 then the 
	 * Haversine Formula (from R.W. Sinnott, "Virtues of the Haversine", 
	 * Sky and Telescope, vol. 68, no. 2, 1984, p. 159): 

 	 * dlon = lon2 - lon1 
	 * dlat = lat2 - lat1 
	 * a = (sin(dlat/2))^2 + cos(lat1) * cos(lat2) * (sin(dlon/2))^2 
	 * c = 2 * arcsin(min(1,sqrt(a))) 
	 * d = R * c 

	 * will give mathematically and computationally exact results. The 
	 * intermediate result c is the great circle distance in radians. The 
	 * great circle distance d will be in the same units as R. 

	 * 5.1a: What value should I use for the radius of the Earth, R? 

	 * The historical definition of a "nautical mile" is "one minute of arc 
	 * of a great circle of the earth". Since the earth is not a perfect 
	 * sphere, that definition is ambiguous. However, the internationally 
	 * accepted (SI) value for the length of a nautical mile is (exactly, by 
	 * definition) 1.852 km or exactly 1.852/1.609344 international miles 
	 * (that is, approximately 1.15078 miles - either "international" or 
	 * "U.S. statute"). Thus, the implied "official" circumference is 360 
	 * degrees times 60 minutes/degree times 1.852 km/minute = 40003.2 km. 
	 * The implied radius is the circumference divided by 2 pi: 

	 * R = 6367 km = 3956 mi 

	 * As noted above this is an approximation for "spherical" earth
	 * This would have been fine for our purposes, but a better calculation for
	 * R came along, and I'm using it!
	 * average that doesn't require calculation, is pretty close: r=6366707;

	 *@param origin start point
	 *@param end end point
	 *@return Geographical distance in meters between <tt>origin</tt> and <tt>end</tt>, with double precision
	 */
public static double distance(Location origin, Location end){
	// measures come in as degrees. The trig functions want radians.
	double rlat1=Math.toRadians(origin.getDoubleLatitude()); 
	double rlon1=Math.toRadians(origin.getDoubleLongitude());
	double rlat2=Math.toRadians(end.getDoubleLatitude());
	double rlon2=Math.toRadians(end.getDoubleLongitude());

	// calculate r, the earth's radius at this latitude
	double r = R_A / (1 - (GEO_E2 * Math.pow(Math.sin(rlat1),2)));

	double drlat = Math.min ( Math.abs(rlat2-rlat1), Math.abs(PI_TIMES_2-rlat2-rlat1) );
	double drlon = Math.min ( Math.abs(rlon2-rlon1), Math.abs(PI_TIMES_2-rlon2-rlon1) );

	double a = Math.pow(Math.sin(drlat/2),2) + Math.cos(rlat1) * Math.cos(rlat2) * Math.pow(Math.sin(drlon/2),2); 
	return r * (2 * Math.asin(Math.min(1,Math.sqrt(a))));
}





}
