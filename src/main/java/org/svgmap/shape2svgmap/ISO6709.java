package org.svgmap.shape2svgmap;

// =============================================================================
//
// ISO6709; ISO6709 (Latitude, Longitude and Altitude format) Parser and Serializer
//           Copyright (C) 2006 Satoru Takagi, SVG Map organization
//          http://docudyne.weblogs.jp/svgmap_lab/iso6709/index.html
// =============================================================================




import java.io.*;
import java.util.*;
import java.text.NumberFormat ;

public class ISO6709 extends LatLonAlt{
	// 2006.6.8 Programmed by Satoru Takagi
	// CRS is also supported by draft spec. of ISO6709(200x)
	// example: +35.123+123.334+851.224CRSWGS84/
	// 2006.12.1 Rewrite ISO6709 parser
	//
	// See http://docudyne.weblogs.jp/svgmap_lab/iso6709/index.html
	//
//	public double latitude; // Latitude[deg] is declared at LatLonAlt
//	public double longitude; // Longitude[deg] is declared at LatLonAlt
//	public double altitude; // Altitude[m] is declared at LatLonAlt
	public String crs; // CRS (Coordinate Reference System) [String]
	public boolean hasAltitude;
	public boolean hasCrs;
	
	private static int d = 0;
	private static int dm = 1;
	private static int dms = 2;
	
	
	ISO6709(){}
	
	ISO6709( double lat , double lon , double alt , String cr ){
		latitude = lat;
		longitude = lon;
		altitude = alt;
		crs = cr;
		hasAltitude = true;
		hasCrs = true;
	}
	
	ISO6709( double lat , double lon , double alt ){
		latitude = lat;
		longitude = lon;
		altitude = alt;
		hasAltitude = true;
		hasCrs = false;
	}
	
	ISO6709( LatLonAlt lla , String cr ){
		latitude = lla.latitude;
		longitude = lla.longitude;
		altitude = lla.altitude;
		crs = cr;
		hasAltitude = true;
		hasCrs = true;
	}
	
	ISO6709( LatLonAlt lla  ){
		latitude = lla.latitude;
		longitude = lla.longitude;
		altitude = lla.altitude;
		hasAltitude = true;
		hasCrs = false;
	}
	
	ISO6709( double lat , double lon , String cr ){
		latitude = lat;
		longitude = lon;
		crs = cr;
		hasAltitude = false;
		hasCrs = true;
	}
	
	ISO6709( double lat , double lon ){
		latitude = lat;
		longitude = lon;
		hasAltitude = false;
		hasCrs = false;
	}
	

	
	// ISO6709 String parser	
	// Common variables for parser
	private String qstr; // Character string that parser accepted
	private int ppoint; // Pointer for parsed
	private double ans;
	
	// Constants for parser
	private static int degree = d;
	private static int minute = dm;
	private static int second = dms;
	private static int fraction = 3;
	private static int sign = 4;
	private static int number = 5;
	private static int term = 6;
	private static int crss = 7;

	ISO6709( String iso ) throws Exception{
		int format;
		double sig;
		hasAltitude = false;
		hasCrs = false;
		
		latitude =0.0;
		longitude =0.0;
//System.out.println("In:" + iso );
		qstr = iso;
		
		int parsed = -1;
		
		
		// Preprocessing
		if ( qstr != null ){
			qstr = (qstr.trim()).toUpperCase();
		} else {
			throw new NumberFormatException();
		}
		if ( qstr.length() < 7 ){
			throw new NumberFormatException();
		}
		
		if ( getNext6709Token(0) != sign ) {		//read latitude sign
			throw new NumberFormatException();
		}
		sig = ans;
		
		if ( getNext6709Token(2) != number ) {		//read latitude [degree]
			throw new NumberFormatException();
		}
		latitude = ans;
		format = d;
		
		parsed = getNext6709Token(2);
		if ( parsed == number ){		////read latitude [minute]
			latitude += ans / 60.0;
			format = dm;
			parsed = getNext6709Token(2);
		}
		
		if ( parsed == number ){		////read latitude [second]
			latitude += ans / 3600.0;
			format = dms;
			parsed = getNext6709Token(2);
		}
		
		if ( parsed == fraction ){		////read latitude fraction
			latitude += addFraction (format);
			parsed = getNext6709Token(0);
		}
		
		latitude *= sig;	//set latitude sign
		
		if ( latitude < -90.0 || latitude > 90.0 ){
			throw new NumberFormatException();
		}
		if (parsed != sign ){		//read longitude sign
			throw new NumberFormatException();
		}
		sig = ans;
		
		if ( getNext6709Token(3) != number ) {		//read longitude [degree]
			throw new NumberFormatException();
		}
		longitude = ans;
		format = d;
		
		parsed = getNext6709Token(2);
		if ( parsed == number ){		//read longitude [minute]
			longitude += ans / 60.0;
			format = dm;
			parsed = getNext6709Token(2);
		}
		
		if ( parsed == number ){		//read longitude [second]
			longitude += ans / 3600.0;
		format = dms;
			parsed = getNext6709Token(2);
		}
		
		if ( parsed == fraction ){		//read longitude fraction
			longitude += addFraction (format);
			parsed = getNext6709Token(0);
		}
		
		longitude *= sig;
		
		
		if ( longitude < -180.0 || longitude > 180.0 ){
			throw new NumberFormatException();
		}
		
		
		if (parsed == sign ){	//has altitude
			altitude = ans * parseAltitude();
			hasAltitude = true;
			parsed = getNext6709Token(0);
		}
		
		if (parsed == crss ){	//has CRS
			hasCrs = true;
		}
		
//System.out.println(latitude + " , " + longitude + " , " + altitude + " C:" + crs );
//System.out.println(getNormalizedString());
	}
	

	private int getNext6709Token( int length  ){
		int parsed;
		if ( ppoint >= qstr.length() ){
			return ( term );
//			throw new NumberFormatException();
		}
		if ( qstr.charAt(ppoint) =='.' ){
			ppoint += 1;
			getFraction();
			// read fraction string
			parsed = fraction;
		} else if ( qstr.charAt(ppoint) == '+' ) {
			ans = 1.0;
			ppoint += 1;
			parsed = sign;
		} else if ( qstr.charAt(ppoint) == '-' ) {
			ans = -1.0;
			ppoint += 1;
			parsed = sign;
		} else if ( qstr.charAt(ppoint) == '/' ) {
			ans = 0;
			ppoint += 1;
			parsed = term;
		} else if ( qstr.charAt(ppoint) == 'C' ) {
			if ( ppoint < qstr.length() - 2  && qstr.charAt(ppoint+1) == 'R' &&  qstr.charAt(ppoint+2) == 'S' ){
				ans = 0;
				ppoint += 3;
				crs = parseCRS();
				parsed = crss;
			} else {
				throw new NumberFormatException();
			}
		} else {
			ans = Integer.parseInt(qstr.substring(ppoint,ppoint + length));
			parsed = number;
			ppoint = ppoint + length;
		}
		
//		System.out.println( ans );
		return ( parsed );
	}
	
	
	private void getFraction(){
		String FString = "";
		int i=0;
		while (  ppoint < qstr.length() && qstr.charAt(ppoint) !='+' && qstr.charAt(ppoint) !='-' && qstr.charAt(ppoint) !='/' && qstr.charAt(ppoint) !='C' ){
			FString += qstr.charAt(ppoint);
			ppoint += 1;
			i += 1;
		}
		if ( i != 0 ){
			ans = Integer.parseInt(FString);
			for ( int j = 0 ; j < i ; j++){
				ans /= 10.0;
			}
		} else {
			ans = 0;
		}
	}
	
	private double parseAltitude( ){
		String AString = "0";
		while ( ppoint < qstr.length() && qstr.charAt(ppoint) !='/' && qstr.charAt(ppoint) !='C'){
			AString += qstr.charAt(ppoint);
			ppoint += 1;
		}
		return( Double.parseDouble(AString) );
	}
	
	private String parseCRS( ){
		String CString = "";
		while ( ppoint < qstr.length() && qstr.charAt(ppoint) !='/' ){
			CString += qstr.charAt(ppoint);
			ppoint += 1;
		}
		return( CString );
	}
	
	private double addFraction( int format ){
		double value = 0.0;
		if (format == d){
			value = ans;
		} else if ( format == dm ){
			value = ans / 60.0;
		} else if ( format == dms ){
			value = ans / 3600.0;
		}
//System.out.println("fractionF:" +value + "(ans)" + ans);
		return ( value );
	}
	
	
	public double getLatitude(){
		return (latitude); // latitude[deg]
	}
	public double getLongitude(){
		return (longitude); // longitude[deg]
	}
	public double getAltitude(){
		return (altitude); // altitude[m]
	}
	public String getCrs(){
		return (crs); // crs[string]
	}
	public boolean hasAltitude(){
		return (hasAltitude);
	}
	public boolean hasCrs(){
		return (hasCrs);
	}

	public static final int DMS = 3;
	public static final int DM  = 2;
	public static final int D   = 1;

	
	String getString( int format , int fractions){
		String lats , lons , alts , crss;
		alts = "";
		crss = "";
		int sign;
		int degi = 0;
		int mini = 0;
		double deg = 0;
		double min = 0;
		double sec = 0;
		double tmp = 0;
		double aLat , aLong;
		
		if ( hasAltitude ){
			NumberFormat aformatter = NumberFormat.getNumberInstance();
			aformatter.setMaximumFractionDigits(2);
			aformatter.setGroupingUsed(false);
			alts = aformatter.format( altitude );
			if ( alts.charAt(0) != '-' ){
				alts = "+" + alts;
			}
		}
		if ( hasCrs ){
			crss = "CRS" + crs;
		}
		lats = LLformat( latitude , format ,2 , fractions);
		lons = LLformat( longitude , format ,3 , fractions);
		
		return ( lats  + lons + alts + crss + "/");
	}
	
	String getString( int format ){
		return ( getString( format , -1 ));
	}

	String getString( ){
		return ( getString( D , -1 ));
	}
	
	public String getNormalizedString(){
		return ( getString() );
	}
	

	private String LLformat( double input , int format , int Ileng , int fractions){
		NumberFormat dformatter = NumberFormat.getNumberInstance();
		NumberFormat mformatter = NumberFormat.getNumberInstance();
		NumberFormat sformatter = NumberFormat.getNumberInstance();
		dformatter.setMinimumIntegerDigits(Ileng);
		dformatter.setMaximumIntegerDigits(Ileng);
		mformatter.setMinimumIntegerDigits(2);
		sformatter.setMinimumIntegerDigits(2);
		String ans = "";
		String ms;
		String ss;
		boolean minusSign = false;
		double deg = 0;
		double min = 0;
		double sec = 0;
		double tmp = 0;
		double aInput = Math.abs(input);
		if ( aInput != input ){
			minusSign = true;
		}
		if ( format == D ){
			deg =aInput;
			if ( fractions > -1 ){
				dformatter.setMaximumFractionDigits(fractions); // set resolution manually
			} else {
				dformatter.setMaximumFractionDigits(8); // set resolution to 1.1[mm]
			}
			ans = dformatter.format( deg )  ;
		} else {
			dformatter.setParseIntegerOnly(true);
			deg = (int)aInput;
			tmp = (aInput - deg ) * 60 ;
			if ( format == DM ){
				min = tmp;
				if ( fractions > -1 ){
					mformatter.setMaximumFractionDigits(fractions); // set resolution manually
				} else {
					mformatter.setMaximumFractionDigits(6); // ser resolution to 1.8[mm]
				}
				ms = mformatter.format( min );
				if (ms.equals("60")){ // When the amount of minutes becomes "60", advance it to degrees. 
					ms = "00";
					++deg;
				}
				ans = dformatter.format( deg ) + ms  ;
			} else {
				min = (int)tmp;
				sec = (tmp - min) * 60;
				// Four digits below decimal point in case of second (30[m]:0digits 3:1 0.3:2 0.03:3 0.003:4) 3.0mm resolution
				mformatter.setParseIntegerOnly(true);
				if ( fractions > -1 ){
					sformatter.setMaximumFractionDigits(fractions); // set resolution manually
				} else {
					sformatter.setMaximumFractionDigits(4);
				}
				ss = sformatter.format(sec);
				if (ss.equals("60")){ // When the amount of seconds becomes "60", advance it to minutes.
					ss = "00";
					++min;
					if ( min == 60){ // When the amount of minutes becomes "60", advance it to degrees.
						min = 0;
						++deg;
					}
				}
				ans = dformatter.format( deg ) + mformatter.format( min ) + ss;
			}
		}
		if (minusSign){
			ans = "-" + ans;
		} else {
			ans = "+" + ans;
		}
//System.out.println( deg + " " + min + " " + sec);
		return ( ans );
	}
	
	
}
