package org.svgmap.shape2svgmap;

import java.util.*;

// Original: http://stackoverflow.com/questions/176137/java-convert-lat-lon-to-utm
// 2015.7.17 re-furbished by Satoru Takagi
// Note: ALL coordinate parameters are North First!!
class UTMconverter{
	
	public static void main( String args[] ) throws Exception {
		UTMconverter uc = new UTMconverter();
		uc.Deg2UTM(34.0 + 37.0/60.0 + 59.65/3600.0,131.0 + 53.0/60.0 + 4.0/3600.0 , 53);
		System.out.println( " N:"+ uc.northing + "E:" + uc.easting + " zone:" + uc.zone + uc.letter);
		uc.UTM2Deg( 3836787.5 , 214387.5 , 53 , false );
		System.out.println( "Lat:" + uc.latitude + " Lng:"+ uc.longitude );
		System.out.println( "Lat:" + toDMS(uc.latitude)[0] +":" + toDMS(uc.latitude)[1] + ":" + toDMS(uc.latitude)[2] + " Lng:"+ toDMS(uc.longitude)[0] + ":" +  toDMS(uc.longitude)[1] + ":" + toDMS(uc.longitude)[2]);
		double[] um = uc.getUTM(34.0 + 37.0/60.0 + 59.65/3600.0,131.0 + 53.0/60.0 + 4.0/3600.0 , 53.0);
		System.out.println(" getUTM: n:"+ um[0] + " e:"+um[1] + " z:" + um[2]);
		double[] ll = uc.getLatLng(3836787.5 , 214387.5 , 53.0 , false );
		System.out.println(" getLatLng: lat:" + ll[0] + " lon:" + ll[1]);
		 ll = uc.getLatLng(3636787.5 , 294387.5 , 53.0 , false );
		System.out.println(" getLatLng: lat:" + ll[0] + " lon:" + ll[1]);
		 ll = uc.getLatLng(10005000.5 , 294387.5 , 53.0 , false );
		System.out.println(" getLatLng: lat:" + ll[0] + " lon:" + ll[1]);
		 ll = uc.getLatLng(5000.5 , 294387.5 , 53.0 , false );
		System.out.println(" getLatLng: lat:" + ll[0] + " lon:" + ll[1]);
		 ll = uc.getLatLng(9994999.5 , 294387.5 , 53.0 , true );
		System.out.println(" getLatLng: lat:" + ll[0] + " lon:" + ll[1]);
		um = uc.getUTM(-0.0452174172 , 133.152534175 , 53.0);
		System.out.println(" getUTM: n:"+ um[0] + " e:"+um[1] + " z:" + um[2]);
		 ll = uc.getLatLng(9994999.5 , 294387.5 , 53.0 , false );
		System.out.println(" getLatLng: lat:" + ll[0] + " lon:" + ll[1]);
		
	}
	
	
	public static String datum = "WGS84"; // this converter is only for WGS84
	
	// lat/lng params
	public double latitude;
	public double longitude;
	
	// UTM params
	public double northing;
	public double easting;
	public int zone; // central meridian
	public char letter; // auxiliar param
	
	static public double[] toDMS( double d ){
		double[] dms = new double[3];
		dms[0] = Math.floor(d);
		dms[1] = (d - dms[0]) * 60.0;
		dms[2] = (dms[1] - Math.floor(dms[1])  ) * 60.0;
		dms[1] = Math.floor(dms[1]);
		return ( dms );
	}
	
	public void getLetter( double Lat ){
		if (Lat<-72) 
		    letter='C';
		else if (Lat<-64) 
		    letter='D';
		else if (Lat<-56)
		    letter='E';
		else if (Lat<-48)
		    letter='F';
		else if (Lat<-40)
		    letter='G';
		else if (Lat<-32)
		    letter='H';
		else if (Lat<-24)
		    letter='J';
		else if (Lat<-16)
		    letter='K';
		else if (Lat<-8) 
		    letter='L';
		else if (Lat<0)
		    letter='M';
		else if (Lat<8)  
		    letter='N';
		else if (Lat<16) 
		    letter='P';
		else if (Lat<24) 
		    letter='Q';
		else if (Lat<32) 
		    letter='R';
		else if (Lat<40) 
		    letter='S';
		else if (Lat<48) 
		    letter='T';
		else if (Lat<56) 
		    letter='U';
		else if (Lat<64) 
		    letter='V';
		else if (Lat<72) 
		    letter='W';
		else
		    letter='X';
	}
	
	int getZone(double Longitude ){
		return ( (int) Math.floor(Longitude/6+31) );
	}
	
	public double[] getUTM(double Latitude, double Longitude){
		Deg2UTM( Latitude, Longitude , -200 );
		double[] ans = new double[3];
		ans[0]=northing;
		ans[1]=easting;
		ans[2]=zone;
		return( ans );
	}
	public double[] getUTM(double Latitude, double Longitude , double Zone){
		Deg2UTM( Latitude, Longitude , (int)Zone );
		double[] ans = new double[3];
		ans[0]=northing;
		ans[1]=easting;
		ans[2]=zone;
		return( ans );
	}
	
	public double[] getLatLng( double nth , double est , double zn ){
		// znは符号付ゾーン番号（南半球の場合マイナス番号にする）
		boolean isSouth = false;
		if ( zn < 0 ){
			isSouth = true;
			zn = -zn;
		}
		UTM2Deg( nth , est , (int)zn , isSouth );
		double[] ans = new double[2];
		ans[0]=latitude;
		ans[1]=longitude;
		return ( ans );
	}
	
	public double[] getLatLng( double nth , double est , double zn , boolean isSouth){
		UTM2Deg( nth , est , (int)zn , isSouth );
		double[] ans = new double[2];
		ans[0]=latitude;
		ans[1]=longitude;
		return ( ans );
	}
	
	
	
	public void Deg2UTM(double Lat,double Lon , int zn)
	{
		latitude =Lat;
		longitude = Lon;
		if ( zn < -180 || zn > 180 ){
			zone= getZone(Lon);
		} else {
			zone = zn;
		}
		getLetter( Lat );
		easting=0.5*Math.log((1+Math.cos(Lat*Math.PI/180)*Math.sin(Lon*Math.PI/180-(6*zone-183)*Math.PI/180))/(1-Math.cos(Lat*Math.PI/180)*Math.sin(Lon*Math.PI/180-(6*zone-183)*Math.PI/180)))*0.9996*6399593.62/Math.pow((1+Math.pow(0.0820944379, 2)*Math.pow(Math.cos(Lat*Math.PI/180), 2)), 0.5)*(1+ Math.pow(0.0820944379,2)/2*Math.pow((0.5*Math.log((1+Math.cos(Lat*Math.PI/180)*Math.sin(Lon*Math.PI/180-(6*zone-183)*Math.PI/180))/(1-Math.cos(Lat*Math.PI/180)*Math.sin(Lon*Math.PI/180-(6*zone-183)*Math.PI/180)))),2)*Math.pow(Math.cos(Lat*Math.PI/180),2)/3)+500000;
//	        easting=Math.round(easting*100)*0.01;
		northing = (Math.atan(Math.tan(Lat*Math.PI/180)/Math.cos((Lon*Math.PI/180-(6*zone -183)*Math.PI/180)))-Lat*Math.PI/180)*0.9996*6399593.625/Math.sqrt(1+0.006739496742*Math.pow(Math.cos(Lat*Math.PI/180),2))*(1+0.006739496742/2*Math.pow(0.5*Math.log((1+Math.cos(Lat*Math.PI/180)*Math.sin((Lon*Math.PI/180-(6*zone -183)*Math.PI/180)))/(1-Math.cos(Lat*Math.PI/180)*Math.sin((Lon*Math.PI/180-(6*zone -183)*Math.PI/180)))),2)*Math.pow(Math.cos(Lat*Math.PI/180),2))+0.9996*6399593.625*(Lat*Math.PI/180-0.005054622556*(Lat*Math.PI/180+Math.sin(2*Lat*Math.PI/180)/2)+4.258201531e-05*(3*(Lat*Math.PI/180+Math.sin(2*Lat*Math.PI/180)/2)+Math.sin(2*Lat*Math.PI/180)*Math.pow(Math.cos(Lat*Math.PI/180),2))/4-1.674057895e-07*(5*(3*(Lat*Math.PI/180+Math.sin(2*Lat*Math.PI/180)/2)+Math.sin(2*Lat*Math.PI/180)*Math.pow(Math.cos(Lat*Math.PI/180),2))/4+Math.sin(2*Lat*Math.PI/180)*Math.pow(Math.cos(Lat*Math.PI/180),2)*Math.pow(Math.cos(Lat*Math.PI/180),2))/3);
		if (Lat<0){
			northing = northing + 10000000;
		}
//	        northing=Math.round(northing*100)*0.01;
	}
	
	
	public void UTM2Deg( double nth , double est , int zn , boolean isSouth ){
//		letter = lt;
		easting = est;
		northing = nth;
		double north = 0;
		zone = zn;
		if ( isSouth ){
			north = northing - 10000000;
		} else {
			north = northing;
		}
		latitude = (north/6366197.724/0.9996+(1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)-0.006739496742*Math.sin(north/6366197.724/0.9996)*Math.cos(north/6366197.724/0.9996)*(Math.atan(Math.cos(Math.atan(( Math.exp((easting - 500000) / (0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((easting - 500000) / (0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3))-Math.exp(-(easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*( 1 -  0.006739496742*Math.pow((easting - 500000) / (0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3)))/2/Math.cos((north-0.9996*6399593.625*(north/6366197.724/0.9996-0.006739496742*3/4*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.pow(0.006739496742*3/4,2)*5/3*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996 )/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4-Math.pow(0.006739496742*3/4,3)*35/27*(5*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/3))/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2))+north/6366197.724/0.9996)))*Math.tan((north-0.9996*6399593.625*(north/6366197.724/0.9996 - 0.006739496742*3/4*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.pow(0.006739496742*3/4,2)*5/3*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996 )*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4-Math.pow(0.006739496742*3/4,3)*35/27*(5*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/3))/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2))+north/6366197.724/0.9996))-north/6366197.724/0.9996)*3/2)*(Math.atan(Math.cos(Math.atan((Math.exp((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3))-Math.exp(-(easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3)))/2/Math.cos((north-0.9996*6399593.625*(north/6366197.724/0.9996-0.006739496742*3/4*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.pow(0.006739496742*3/4,2)*5/3*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4-Math.pow(0.006739496742*3/4,3)*35/27*(5*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/3))/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2))+north/6366197.724/0.9996)))*Math.tan((north-0.9996*6399593.625*(north/6366197.724/0.9996-0.006739496742*3/4*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.pow(0.006739496742*3/4,2)*5/3*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4-Math.pow(0.006739496742*3/4,3)*35/27*(5*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/3))/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2))+north/6366197.724/0.9996))-north/6366197.724/0.9996))*180/Math.PI;
//	        latitude=Math.round(latitude*10000000);
//	        latitude=latitude/10000000;
		longitude =Math.atan((Math.exp((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3))-Math.exp(-(easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2)/3)))/2/Math.cos((north-0.9996*6399593.625*( north/6366197.724/0.9996-0.006739496742*3/4*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.pow(0.006739496742*3/4,2)*5/3*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2* north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4-Math.pow(0.006739496742*3/4,3)*35/27*(5*(3*(north/6366197.724/0.9996+Math.sin(2*north/6366197.724/0.9996)/2)+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/4+Math.sin(2*north/6366197.724/0.9996)*Math.pow(Math.cos(north/6366197.724/0.9996),2)*Math.pow(Math.cos(north/6366197.724/0.9996),2))/3)) / (0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2))))*(1-0.006739496742*Math.pow((easting-500000)/(0.9996*6399593.625/Math.sqrt((1+0.006739496742*Math.pow(Math.cos(north/6366197.724/0.9996),2)))),2)/2*Math.pow(Math.cos(north/6366197.724/0.9996),2))+north/6366197.724/0.9996))*180/Math.PI+zone*6-183;
//	        longitude=Math.round(longitude*10000000);
//	        longitude=longitude/10000000;       
	}
}