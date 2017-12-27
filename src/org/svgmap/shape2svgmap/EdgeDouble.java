package org.svgmap.shape2svgmap;

import java.awt.*;
import java.awt.geom.*;
/**
 * Edge.java represents a half plane defined by an edge of a clip polygon for 
 * a polygon clipping algorithm
 *
 * Created: Mon Aug 16 23:40:36 1999
 *
 * @author Tim Lambert
 */

class EdgeDouble {
  private double x1,y1; //start point
  private double x2,y2; //end point
  private double a,b,c; // (x,y) is inside the edge if a*x + b*y + c > 0;
  //we use ints to store the equation to avoid round off problems

  EdgeDouble(double x1, double y1, double x2, double y2) {
    a = y2 - y1;
    b = x1 - x2;
    c = -a*x1 - b*y1;
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
  }

  /* is this point inside the half plane defined by the edge? */
  public boolean inside(Point2D.Double p){
  	if ( a>0 || (a==0 && b<0)){
	    return a*p.x + b*p.y + c > 0;
  	} else {
//  		return a*p.x + b*p.y + c >= 0;
	    return a*p.x + b*p.y + c > 0;
  	}
  	
  }

  /* return the intersection of this edge with another one from p1 to p2 */
  /* rounded to nearest int */
  public Point2D.Double intersect(Point2D.Double p1, Point2D.Double p2) {
    double d = p2.y - p1.y;
    double e = p1.x - p2.x;
    double f = -d*p1.x - e*p1.y;
    double denom = e*a - b*d;
    double x = ((b*f - e*c)/denom);
    double y = ((d*c - a*f)/denom);
    return new Point2D.Double(x,y);
  }


  /* Convert to a string - handy for debugging */
  public String toString() {
    return "("+x1+","+y1+")-("+x2+","+y2+")";
  }

}
