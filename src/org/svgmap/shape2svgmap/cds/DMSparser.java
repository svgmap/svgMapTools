package org.svgmap.shape2svgmap.cds;
import java.util.*;
import java.util.regex.*;
import java.awt.*;
//import com.vividsolutions.jts.geom.*;

// CSVにおける緯度経度の形式でDDMMSS.SS, DDMM.MMをサポートする
// 2018/09/26
// 2020/05/15 実際に使うために、もっと簡便＆汎用化した

// DMS　ほぼ任意のデリミタが入っていても可、小数点数があっても可
// +341323.234  34:13:23.234 341323


class DMSparser{
	
	static int DEG = 0;
	static int DM  = 1;
	static int DMS = 2;
	
//	static Pattern latPat = Pattern.compile("([+-]??\\d\\d)\\D*(\\d\\d)\\D*(\\d\\d(\\.\\d+)?)");
//	static Pattern lngPat = Pattern.compile("([+-]??\\d\\d\\d)\\D*(\\d\\d)\\D*(\\d\\d(\\.\\d+)?)");
	static Pattern dmsPat = Pattern.compile("([+-]?)(\\d?\\d?\\d)[^0-9\\.]*(\\d\\d)[^0-9\\.]*(\\d\\d(\\.\\d+)?)");
	static Pattern dmPat = Pattern.compile("([+-]?)(\\d?\\d?\\d)[^0-9\\.]*(\\d\\d(\\.\\d+)?)");
	
    public static void main(String[] args) throws Exception {
    	String inp = "";
    	DMSparser dp = new DMSparser();
    	if ( args.length >0 ){
    		inp = args[0];
	    	double ans = dp.getValueFromDMS(inp);
	        System.out.println("DMS:"+inp+"  ans : " + ans);
    		if ( args.length > 1 ){
    			inp = args[1];
    			ans = dp.getValueFromDM(inp);
		        System.out.println("DM:"+inp+"  ans : " + ans);
    		}
    	}
    }
	
	public double getValue(String inputValS, int format ){
		if ( format == DEG ){
			return ( Double.parseDouble(inputValS));
		} else if ( format == DMS ){
			return ( getValueFromDMS(inputValS));
		} else if ( format == DM ){
			return ( getValueFromDM(inputValS));
		} else {
			return ( Double.parseDouble(inputValS));
		}
	}
	
	public double getValueFromDMS(String inpuValS){
		double ansVal=-999;
		inpuValS=inpuValS.trim();
		Matcher mlat = dmsPat.matcher(inpuValS);
		if (mlat.find() ){
			String latSgn = mlat.group(1);
			String latDeg = mlat.group(2);
			String latMin = mlat.group(3);
			String latSec = mlat.group(4);
//			System.out.println("SG:"+latSgn+" D:"+latDeg+" M:"+latMin+" S:"+latSec);
			ansVal = Double.parseDouble(latDeg) + Double.parseDouble(latMin)/60 + Double.parseDouble(latSec) / 3600;
			if ( latSgn.equals("-")){
				ansVal = -ansVal;
			}
		} else {
//			System.out.println("DMSparser: CANT FIND DMS STRING");
		}
		return ( ansVal );
	}
	
	public double getValueFromDM(String inpuValS){
		double ansVal=-999;
		inpuValS=inpuValS.trim();
		Matcher mlat = dmPat.matcher(inpuValS);
		if (mlat.find() ){
			String latSgn = mlat.group(1);
			String latDeg = mlat.group(2);
			String latMin = mlat.group(3);
//			System.out.println("SG:"+latSgn+" D:"+latDeg+" M:"+latMin);
			ansVal = Double.parseDouble(latDeg) + Double.parseDouble(latMin)/60 ;
			if ( latSgn.equals("-")){
				ansVal = -ansVal;
			}
		} else {
//			System.out.println("DMSparser: CANT FIND DMS STRING");
		}
		return ( ansVal );
	}
	
}