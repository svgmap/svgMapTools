package org.svgmap.shape2svgmap;

import java.util.*;
import java.io.*;

// 日本のメッシュコードを入出力します
// 2007/04/06 Satoru Takagi

public class JpMesh{

	static final double latMin = 24;
	static final double latMax = 46;
	static final double longMin = 122;
	static final double longMax = 149;
	
	
	public String mesh , mesh1 , mesh2 , mesh3 , mesh4;
	public int m1u , m1d , m2u , m2d , m3u , m3d , m4u , m4d;
	public double latitude , longitude;
	
	static final double m1lat = 1.0 / 1.5;
	static final double m1long = 1.0;
	
	static final double m2lat = m1lat / 8.0;
	static final double m2long = m1long / 8.0;
	
	static final double m3lat = m2lat / 10.0;
	static final double m3long = m2long / 10.0;
	
	static final double m4lat = m3lat / 10.0;
	static final double m4long = m3long / 10.0;
	
	public static void main( String argv[] ) {
		if (argv.length == 3 ){
			long ml = Long.parseLong(argv[0]);
			JpMesh jm = new JpMesh(ml);
			System.out.println( "lat:" + jm.getLatitude() + " long:" + jm.getLongitude() );
			System.out.println( "code4:" + jm.toLongMesh4Code() + " code35:" + jm.toLongMesh35Code() );
		} else if (argv.length == 2 ){
			JpMesh jm = new JpMesh(Double.parseDouble(argv[0]) , Double.parseDouble(argv[1]));
			System.out.println( "mesh4:" + jm );
		} else {
			JpMesh jm = new JpMesh( argv[0]);
			System.out.println( "lat:" + jm.getLatitude() + " long:" + jm.getLongitude() );
			System.out.println( "code4:" + jm.toLongMesh4Code() + " code35:" + jm.toLongMesh35Code() );
		}
	}
	
	public JpMesh(double lati , double longi){
		latitude = lati;
		longitude = longi;
		
		double latm = latitude * 1.5;
		double lonm = longitude - 100.0;
		
		m1u = (int)(latm);
		m1d = (int)(lonm);
		
		latm = ( latm - m1u ) * 8.0;
		lonm = ( lonm - m1d ) * 8.0;
		
		m2u =(int)latm;
		m2d =(int)lonm;
		
		
		latm = ( latm - m2u ) * 10.0;
		lonm = ( lonm - m2d ) * 10.0;
		
		m3u = (int)latm;
		m3d = (int)lonm;
		
		latm = ( latm - m3u ) * 10.0;
		lonm = ( lonm - m3d ) * 10.0;
		
		m4u = (int)latm;
		m4d = (int)lonm;
		
	}
	
	public JpMesh(long meshNo){
		long mt = meshNo;
		long tt , tq;
		
		if ( meshNo > 1000000000L ){ // mesh4
			tt = mt / 10;
			m4d = (int)(mt - tt * 10);
			
			mt = tt;
			tt = mt / 10;
			m4u = (int)(mt - tt * 10);
			mt = tt;
			
		} else if ( meshNo > 100000000L ){ // 3.5次メッシュ？　微妙な
			tt = mt / 10;
			int m35 = (int)(mt - tt * 10);
			mt = tt;
			switch ( m35 ) {
			case 0:
				m4u = 0;
				m4d = 0;
				break;
			case 1:
				m4u = 5;
				m4d = 0;
				break;
			case 2:
				m4u = 5;
				m4d = 5;
				break;
			case 3:
				m4u = 0;
				m4d = 5;
				break;
			default:
				m4u = 0;
				m4d = 0;
				break;
			}
		} else {
			m4u = 0;
			m4d = 0;
		}
		
		
		if ( meshNo > 10000000L ){ // mesh3
			tt = mt / 10;
			m3d = (int)(mt - tt * 10);
			
			mt = tt;
			tt = mt / 10;
			m3u = (int)(mt - tt * 10);
			mt = tt;
			
		} else {
			m3u = 0;
			m3d = 0;
		}
		
		if ( meshNo > 100000L ){ // mesh2
			tt = mt / 10;
			m2d = (int)(mt - tt * 10);
			
			mt = tt;
			tt = mt / 10;
			m2u = (int)(mt - tt * 10);
			mt = tt;
			
		} else { // mesh1
			m2u = 0;
			m2d = 0;
		}
		
		tt = mt / 100;
		m1d = (int)(mt - tt * 100);
		m1u = (int)(tt);
	}
	
	
	public JpMesh(String meshNo){
		mesh = meshNo;
		mesh1 = mesh.substring(0,4);
		
		m1u = Integer.parseInt(mesh1.substring(0,2));
		m1d = Integer.parseInt(mesh1.substring(2,4));
		
		if (mesh.length() > 5) {
			mesh2 = mesh.substring(4,6);
			m2u = Integer.parseInt(mesh2.substring(0,1));
			m2d = Integer.parseInt(mesh2.substring(1,2));
		} else {
			mesh2 = "";
			m2u = 0;
			m2d = 0;
		}
		if (mesh.length() > 7) {
			mesh3 = mesh.substring(6,8);
			m3u = Integer.parseInt(mesh3.substring(0,1));
			m3d = Integer.parseInt(mesh3.substring(1,2));
		} else {
			mesh3 = "";
			m3u = 0;
			m3d = 0;
		}
		
		if (mesh.length() == 10) {
			mesh4 = mesh.substring(8);
			m4u = Integer.parseInt(mesh4.substring(0,1));
			m4d = Integer.parseInt(mesh4.substring(1,2));
		} else if ( mesh.length() == 9){
			String m35 = mesh.substring(8);
			switch ( m35 ){
			case "0":
				m4u = 0;
				m4d = 0;
				break;
			case "1":
				m4u = 5;
				m4d = 0;
				break;
			case "2":
				m4u = 5;
				m4d = 5;
				break;
			case "3":
				m4u = 0;
				m4d = 5;
				break;
			default:
				m4u = 0;
				m4d = 0;
				break;
			}
		} else {
			mesh4 = "";
			m4u = 0;
			m4d = 0;
		}
	}
	
	public double getLatitude(){
		latitude = ((double)m1u + (double)m2u / 8.0 + (double)m3u / 80.0 + (double)m4u / 800.0 ) / 1.5;
		return ( latitude );
	}
	public double getLongitude(){
		longitude = (double)m1d + (double)m2d / 8.0 + (double)m3d / 80.0 + (double)m4d / 800.0 + 100.0;
		return ( longitude );
	}
	
	public long toLongMesh4Code(){ // 注意 10ケタなのでLongです
		long meshLong =
			  (long)m1u * 100000000L
			+ (long)m1d * 1000000L
			+ (long)m2u * 100000L
			+ (long)m2d * 10000L
			+ (long)m3u * 1000L
			+ (long)m3d * 100L
			+ (long)m4u * 10L
			+ (long)m4d;
		return ( meshLong );
	}
	
	public long toLongMesh35Code(){ // 注意 9ケタなのでLongです
		long meshLong =
			  (long)m1u * 10000000L
			+ (long)m1d * 100000L
			+ (long)m2u * 10000L
			+ (long)m2d * 1000L
			+ (long)m3u * 100L
			+ (long)m3d * 10L;
		if ( m4u < 5 && m4d < 5){
			meshLong += 0;
		} else if ( m4u >=5 && m4d < 5 ){
			meshLong += 1;
		} else if ( m4u >=5 && m4d >= 5 ){
			meshLong += 2;
		} else if ( m4u < 5 && m4d >= 5 ){
			meshLong += 3;
		}
		return ( meshLong );
	}
	
	public int toIntMesh3Code(){
		int meshInt = m1u * 1000000 + m1d * 10000 + m2u * 1000 + m2d * 100 + m3u * 10 + m3d;
		return ( meshInt );
	}
	
	public int toIntMesh2Code(){
		int meshInt = m1u * 10000 + m1d * 100 + m2u * 10 + m2d ;
		return ( meshInt );
	}
	
	public int toIntMesh1Code(){
		int meshInt = m1u * 100 + m1d ;
		return ( meshInt );
	}
	
	public int getUpMesh3(){
		int p1u = 0;
		int p2u = 0;
		int p3u = 0;
		if ( m3u == 9 ){
			if ( m2u == 7 ){
				p1u = 1;
			} else {
				p2u = 1;
			}
		} else {
			p3u = 1;
		}
		int meshInt = (m1u + p1u) * 1000000 + m1d * 10000 + (m2u + p2u) * 1000 + m2d * 100 + (m3u + p3u) * 10 + m3d;
		return ( meshInt );
		
	}
	
	public int getRightMesh3(){
		int p1d = 0;
		int p2d = 0;
		int p3d = 0;
		if ( m3d == 9 ){
			if ( m2d == 7 ){
				p1d = 1;
			} else {
				p2d = 1;
			}
		} else {
			p3d = 1;
		}
		int meshInt = m1u * 1000000 + (m1d + p1d) * 10000 + m2u * 1000 + (m2d + p2d) * 100 + m3u * 10 + (m3d + p3d);
		return ( meshInt );
		
	}
	
	public int getUpRightMesh3(){
		int p1d = 0;
		int p2d = 0;
		int p3d = 0;
		int p1u = 0;
		int p2u = 0;
		int p3u = 0;
		if ( m3u == 9 ){
			if ( m2u == 7 ){
				p1u = 1;
			} else {
				p2u = 1;
			}
		} else {
			p3u = 1;
		}
		
		if ( m3d == 9 ){
			if ( m2d == 7 ){
				p1d = 1;
			} else {
				p2d = 1;
			}
		} else {
			p3d = 1;
		}
		int meshInt = (m1u + p1u) * 1000000 + (m1d + p1d) * 10000 + (m2u + p2u) * 1000 + (m2d + p2d)* 100 + (m3u + p3u) * 10 + (m3d + p3d);
		return ( meshInt );
	}
	
	public String toString(){
		mesh1 = Integer.toString(m1u) + Integer.toString(m1d);
		mesh2 = Integer.toString(m2u) + Integer.toString(m2d);
		mesh3 = Integer.toString(m3u) + Integer.toString(m3d);
		mesh4 = Integer.toString(m4u) + Integer.toString(m4d);
		mesh = mesh1 + mesh2 + mesh3 + mesh4;
		return (mesh);
	}
	
	public String toMesh1String(){
		mesh1 = Integer.toString(m1u) + Integer.toString(m1d);
		return (mesh1 );
	}
	
	public String toMesh2String(){
		mesh1 = Integer.toString(m1u) + Integer.toString(m1d);
		mesh2 = Integer.toString(m2u) + Integer.toString(m2d);
		return (mesh1 + mesh2);
	}
	
	public String toMesh3String(){
		mesh1 = Integer.toString(m1u) + Integer.toString(m1d);
		mesh2 = Integer.toString(m2u) + Integer.toString(m2d);
		mesh3 = Integer.toString(m3u) + Integer.toString(m3d);
		return (mesh1 + mesh2 + mesh3);
	}
	
	public double getMesh1Lat(){
		return (  ((double)m1u  ) / 1.5 );
	}
	public double getMesh1Long(){
		return (   (double)m1d  + 100.0 );
	}
	
	public double getMesh2Lat(){
		return (  ((double)m1u + (double)m2u / 8.0  ) / 1.5  );
	}
	public double getMesh2Long(){
		return (  (double)m1d + (double)m2d / 8.0  + 100.0 );
	}
	
	public double getMesh3Lat(){
		return (  ((double)m1u + (double)m2u / 8.0 + (double)m3u / 80.0 ) / 1.5  );
	}
	public double getMesh3Long(){
		return (  (double)m1d + (double)m2d / 8.0 + (double)m3d / 80.0 + 100.0 );
	}
	
	public double getMesh4Lat(){
		return (  ((double)m1u + (double)m2u / 8.0 + (double)m3u / 80.0 + (double)m4u / 800.0 ) / 1.5  );
	}
	public double getMesh4Long(){
		return (  (double)m1d + (double)m2d / 8.0 + (double)m3d / 80.0 + (double)m4d / 800.0 + 100.0 );
	}
	
	
//	public Envelope getMesh2Envelope(){}
		
	
}