package org.svgmap.shape2svgmap;

import java.awt.geom.*;
public class Transform {
	// SVGの１次座標変換行列のクラス
	public double a,b,c,d,e,f;
	public double x,y;
	public Transform() {
		a = 1;
		b = 0;
		c = 0;
		d = 1;
		e = 0;
		f = 0;
		x = 0;
		y = 0;
	}
	
	public Transform(double aa , double bb , double cc , double dd , double ee , double ff ) {
		a = aa;
		b = bb;
		c = cc;
		d = dd;
		e = ee;
		f = ff;
		x = 0;
		y = 0;
	}
	
	public void copy(Transform t){
		a=t.a;
		b=t.b;
		c=t.c;
		d=t.d;
		e=t.e;
		f=t.f;
		x = 0;
		y = 0;
	}
	
	public Point2D.Double doTransform ( Point2D.Double in ){
		Point2D.Double ans = new Point2D.Double();
		ans.x = a * in.x + c * in.y + e;
		ans.y = b * in.x + d * in.y + f;
		return ( ans );
	}
	
	public void calcTransform ( double ix , double iy ){
		x = a * ix + c * iy + e;
		y = b * ix + d * iy + f;
	}
	
	public double getDet(){
		return ( a * d - b * c );
	}
	
	public void setInv(Transform t){
		double det = t.getDet();
		if ( det != 0.0 ){
			a = t.d / det;
			b = - t.b / det;
			c = - t.c / det;
			d = t.a / det;
			e = ( t.c * t.f  - t.d * t.e ) / det;
			f = ( t.b * t.e  - t.a * t.f ) / det;
		}
	}
	
	public String toString(){
		return ( "[" + a + "," + b + "," + c + "," + d + "," + e + "," + f + "]" );
	}
	
	public Rectangle2D.Double getTransformedBBox( Rectangle2D.Double inBB ){
		double p0x,p0y,w,h;
		Point2D.Double p1 = this.doTransform( new Point2D.Double(inBB.x , inBB.y));
		Point2D.Double p2 = this.doTransform( new Point2D.Double(inBB.x + inBB.width , inBB.y + inBB.height));
		if ( p1.x < p2.x ){
			p0x = p1.x;
		} else {
			p0x = p2.x;
		}
		if ( p1.y < p2.y ){
			p0y = p1.y;
		} else {
			p0y = p2.y;
		}
		w = Math.abs( p2.x - p1.x);
		h = Math.abs( p2.y - p1.y);
		
		return ( new Rectangle2D.Double( p0x , p0y , w , h ) );
	}

}

