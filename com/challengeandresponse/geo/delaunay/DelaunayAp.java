package com.challengeandresponse.geo.delaunay;

import com.challengeandresponse.geo.data.LocationBD;



/*
 * Copyright (c) 2005 by L. Paul Chew.
 * 
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, subject to the following 
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */


/**
 * The Delauany applet.
 * Creates and displays a Delaunay Triangulation (DT) or a Voronoi Diagram (VoD).
 * Has a main program so it is an application as well as an applet.
 * 
 * @author Jim Youll
 * @version 0.10 2007-02-01 Derived from Paul Chew's original code
 * 
 */
public class DelaunayAp {

	public static void main (String[] args) {
		DelaunayTriangulation dt;     // The Delaunay triangulation
		Simplex<Pnt> initialTriangle; // The large initial triangle
		int initialSize = 720;      	// Controls size of initial triangle

		/**
		 * Create and initialize the DT.
		 */
		initialTriangle = new Simplex<Pnt>(new Pnt("minus,minus",-initialSize, -initialSize),
				new Pnt("plus,minus", initialSize, -initialSize),
				new Pnt("zero,plus", 0,  initialSize));
		dt = new DelaunayTriangulation(initialTriangle);

//		Simplex<Pnt> tri = new Simplex<Pnt>(new Pnt(-220,220), new Pnt(120,10), new Pnt(0,-100));
//		System.out.println("Triangle created: " + tri);
		System.out.println("DelaunayTriangulation created: " + dt);
//		dt.delaunayPlace(new Pnt(0,0));
//		dt.delaunayPlace(new Pnt(1,0));
//		dt.delaunayPlace(new Pnt(0,1));
//		Pnt point = new Pnt(2,2);
//		dt.delaunayPlace(point);
//		System.out.println("After adding 3 points, the DelaunayTriangulation is a " + dt);
		dt.printStuff();
		
		Pnt[] points = new Pnt[3];
		LocationBD a = new LocationBD(0.0D,0.0D,0.0D);
		points[0] = new Pnt(a);
//		points[0] = new Pnt(0,0);
//		points[0].setPayload("ORIGIN");
		points[1] = new Pnt("BOSTON",42,-71);
//		points[1].setPayload("BOSTON");
		points[2] = new Pnt(42,-25);
		points[2].setPayload("rather south of boston");
		
		for (int i = 0; i < 3; i++)
			dt.delaunayPlace(points[i]);

		dt.printStuff();
		
		System.out.println("NODES");
		for (int i = 0; i < 3; i++)
			System.out.println(points[i]+" "+dt.locate (points[i]));
		
		
		System.out.println("finding closest");
//		System.out.println("1,0 "+dt.locate (new Pnt(1,0)));
//		System.out.println("42,20 "+dt.locate (new Pnt(-42,20)));
//
		System.out.println("42,180");
		Simplex <Pnt> s = dt.locate(new Pnt(42,180));
		Simplex.moreInfo = true;
		System.out.println("simplex-> "+s);
		System.out.println(dt.neighbors(s));

	}


}

