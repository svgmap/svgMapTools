package org.svgmap.shape2svgmap;

// SVG Mapで使う、地理座標(XY含)からSVG座標へ変換するための座標変換システム
// 2007/11/1 S.Takagi
// 2010.08.19 geoTools 2.6.5 support

import java.io.*;
import java.io.IOException;
import java.util.*;
import java.lang.*;
import java.net.URL;
import java.net.URI;
import org.geotools.data.DataStore;
import java.text.NumberFormat ;
import java.util.regex.*;
import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.*;
import org.geotools.feature.type.*;


import org.geotools.data.Transaction;
import org.geotools.filter.*;
// import org.geotools.data.vpf.*;
import org.locationtech.jts.geom.*;


public class SvgMapAffineTransformer {
	public int xySys; // 大元のデータが日本のXY座標系のとき(1-19:m , -1 - -19:mm)
	public int datum; // 旧測地系のときGeoConverter.BESSEL , WGS84:GeoConverter.JGD2000
	public GeoConverter gconv;
	public Transform g2s; // WGS84系からSVG系への変換パラメータ
	
	SvgMapAffineTransformer( int dtm , int xys , Transform gs){
//		gconv.tiny = true; // WGS<>Bessel変換を簡易版に
		xySys = xys;
		datum = dtm;
		if ( datum == GeoConverter.BESSEL ) {
			gconv = new GeoConverter(GeoConverter.Tky2JGD);
		} else {
			gconv = new GeoConverter();
		}
		g2s = gs;
	}
	
	SvgMapAffineTransformer( int dtm , int xys ){
//		gconv = new GeoConverter();
//		gconv.tiny = true; // WGS<>Bessel変換を簡易版に
		xySys = xys;
		datum = dtm;
		if ( datum == GeoConverter.BESSEL ) {
			gconv = new GeoConverter(GeoConverter.Tky2JGD);
		} else {
			gconv = new GeoConverter();
		}
		g2s = new Transform ( 1.0 , 0.0 , 0.0 , -1.0 , 0.0 , 0.0 ); // とりあえずね・・
	}
	
	public void setCrsTransform( Transform gs ){
		g2s = gs;
	}
	
	synchronized public  Coordinate transCoordinate( Coordinate inCrd ){
		Coordinate outCrd;
		if ( xySys > 0 ){
			gconv.setXY(inCrd.y , inCrd.x , xySys , datum );
			LatLonAlt BL = gconv.toWGS84();
			g2s.calcTransform(BL.longitude , BL.latitude );
		} else if ( xySys < 0 ){
			gconv.setXY(inCrd.y / 1000.0 , inCrd.x / 1000.0 , -xySys , datum );
			LatLonAlt BL = gconv.toWGS84();
			g2s.calcTransform(BL.longitude , BL.latitude );
			
		} else {
			if ( datum == GeoConverter.BESSEL ){
				gconv.setLatLon(inCrd.y , inCrd.x , datum );
				LatLonAlt BL = gconv.toWGS84();
				g2s.calcTransform(BL.longitude , BL.latitude );
			} else {
				g2s.calcTransform(inCrd.x , inCrd.y );
			}
		}
		outCrd = new Coordinate( g2s.x , g2s.y );
		return (outCrd);
	}
	
	synchronized public PolygonDouble transCoordinates( Coordinate[] inCrd ){
		Coordinate crd;
		PolygonDouble outPol = new PolygonDouble(inCrd.length);
		if ( xySys > 0 ){
			LatLonAlt BL;
			for ( int i = 0 ; i < inCrd.length ; i++){
				gconv.setXY(inCrd[i].y , inCrd[i].x , xySys , datum );
				BL = gconv.toWGS84();
				g2s.calcTransform(BL.longitude , BL.latitude );
				outPol.addPoint( g2s.x , g2s.y );
			}
		} else if ( xySys < 0 ){
			LatLonAlt BL;
			for ( int i = 0 ; i < inCrd.length ; i++){
				gconv.setXY(inCrd[i].y / 1000.0 , inCrd[i].x / 1000.0 , -xySys , datum );
				BL = gconv.toWGS84();
				g2s.calcTransform(BL.longitude , BL.latitude );
				outPol.addPoint( g2s.x , g2s.y );
			}
		} else {
			if ( datum == GeoConverter.BESSEL ){
				LatLonAlt BL;
				for ( int i = 0 ; i < inCrd.length ; i++){
					gconv.setLatLon(inCrd[i].y , inCrd[i].x , datum );
					BL = gconv.toWGS84();
					g2s.calcTransform(BL.longitude , BL.latitude );
					outPol.addPoint( g2s.x , g2s.y );
				}
			} else {
				for ( int i = 0 ; i < inCrd.length ; i++){
					g2s.calcTransform(inCrd[i].x , inCrd[i].y );
					outPol.addPoint( g2s.x , g2s.y );
				}
			}
		}
		return (outPol);
	}
	
	
	// 座標参照系の違いを加味し、WGS84系でのBBOXを求める
	synchronized public Envelope getWgsBounds( Envelope env){
		Envelope bounds= new Envelope();
		if ( xySys > 0 ) {
			// XY系のとき
			// シェープファイルのXYは正規のXYと逆なんですよね・・・
			gconv.setXY(env.getMaxY() , env.getMaxX() , xySys , datum);
			LatLonAlt maxBL = gconv.toWGS84();
			gconv.setXY(env.getMinY() , env.getMinX() , xySys , datum);
			LatLonAlt minBL = gconv.toWGS84();
			
			bounds = new Envelope( maxBL.longitude , minBL.longitude , maxBL.latitude , minBL.latitude );
		} else if ( xySys < 0 ) {
			gconv.setXY(env.getMaxY()/1000.0 , env.getMaxX()/1000.0 , -xySys , datum);
			LatLonAlt maxBL = gconv.toWGS84();
			gconv.setXY(env.getMinY()/1000.0 , env.getMinX()/1000.0 , -xySys , datum);
			LatLonAlt minBL = gconv.toWGS84();
			
			bounds = new Envelope( maxBL.longitude , minBL.longitude , maxBL.latitude , minBL.latitude );
			
		} else {
			// XY系ではないとき
			if ( datum == GeoConverter.BESSEL ){
				gconv.setLatLon(env.getMaxY() , env.getMaxX() , datum);
				LatLonAlt maxBL = gconv.toWGS84();
				gconv.setLatLon(env.getMinY() , env.getMinX() , datum);
				LatLonAlt minBL = gconv.toWGS84();
				bounds = new Envelope( maxBL.longitude , minBL.longitude , maxBL.latitude , minBL.latitude );
			} else {
				bounds = env;
			}
		}
		return ( bounds );
	}
	
	
	// WGS84座標系に対するSVG座標でのBBOX算出
	synchronized public Envelope getSvgBoundsW( Envelope wgsEnv  ){
		// SVG のビューボックスを決めている。 
		double x , y;
		g2s.calcTransform( wgsEnv.getMinX() , wgsEnv.getMinY() );
		x = g2s.x;
		y = g2s.y;
		g2s.calcTransform( wgsEnv.getMaxX() , wgsEnv.getMaxY() );
		
		return ( new Envelope ( x , g2s.x , y , g2s.y ) );
	}
	
	// 座標参照系の違いを加味し、SVG座標でのBBOX算出 2007.10.23
	synchronized public Envelope getSvgBounds( Envelope env){
		double x , y;
		Envelope wgsEnv = getWgsBounds( env );
		return ( getSvgBoundsW( wgsEnv ) );
	}

}