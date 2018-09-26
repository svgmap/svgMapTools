package org.svgmap.shape2svgmap.cds;
import java.util.*;
import java.util.regex.*;
import java.awt.*;
import com.vividsolutions.jts.geom.*;

// CSVにおける緯度経度の形式でDDMMSS.SSを今後サポートするための準備
// 2018/09/26

class DMSparser{
	
	static Pattern latPat = Pattern.compile("([+-]??\\d\\d)\\D*(\\d\\d)\\D*(\\d\\d(\\.\\d+)?)");
	static Pattern lngPat = Pattern.compile("([+-]??\\d\\d\\d)\\D*(\\d\\d)\\D*(\\d\\d(\\.\\d+)?)");
	
	public double getLatitudeFromDMS(String lats){
		double latitude=-999;
		lats=lats.trim();
		Matcher mlat = latPat.matcher(lats);
		if (mlat.find() ){
			String latDeg = mlat.group(1);
			String latMin = mlat.group(2);
			String latSec = mlat.group(3);
			latitude = Double.parseDouble(latDeg) + Double.parseDouble(latMin)/60 + Double.parseDouble(latSec) / 3600;
		} else {
			System.out.println("DMSparser: CANT FIND DMS STRING");
		}
		return ( latitude );
	}
	public double getLongitudeFromDMS(String lons){
		double longitude=-999;
		lons=lons.trim();
		Matcher mlng = lngPat.matcher(lons);
		if ( mlng.find() ){
			String lonDeg = mlng.group(1);
			String lonMin = mlng.group(2);
			String lonSec = mlng.group(3);
			longitude = Double.parseDouble(lonDeg) + Double.parseDouble(lonMin)/60 + Double.parseDouble(lonSec) / 3600;
		} else {
			System.out.println("DMSparser: CANT FIND DMS STRING");
		}
		return ( longitude );
	}
	
	// regex version
	public Coordinate getCoordinateFromDMS( String lats , String lons ){
		
		double latitude=-999, longitude=-999;
		
		lats=lats.trim();
		lons=lons.trim();
		
		Matcher mlat = latPat.matcher(lats);
		Matcher mlng = lngPat.matcher(lons);
		
		if (mlat.find() && mlng.find() ){
//			System.out.println("D:"+ mlat.group(1) + " M:"+ mlat.group(2) + " S:"+ mlat.group(3) );
			String latDeg = mlat.group(1);
			String latMin = mlat.group(2);
			String latSec = mlat.group(3);
			
			String lonDeg = mlng.group(1);
			String lonMin = mlng.group(2);
			String lonSec = mlng.group(3);
			latitude = Double.parseDouble(latDeg) + Double.parseDouble(latMin)/60 + Double.parseDouble(latSec) / 3600;
			longitude = Double.parseDouble(lonDeg) + Double.parseDouble(lonMin)/60 + Double.parseDouble(lonSec) / 3600;
		} else {
			System.out.println("DMSparser: CANT FIND DMS STRING");
		}
		
		
		return ( new Coordinate ( longitude , latitude ) );
	}
	
}