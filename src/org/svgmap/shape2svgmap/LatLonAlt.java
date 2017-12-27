package org.svgmap.shape2svgmap;

public class LatLonAlt{
	// 地理座標のクラス
	// Ver.1.0:  2006.6.8 bu Satoru Takagi
	public double latitude;  // 緯度[deg] ラジアンにすべきか？ 微妙・・
	public double longitude; // 経度[deg]
	public double altitude; // 高度(ジオイド高らしい)[m]
	
	
	LatLonAlt( ){
	}
	
	LatLonAlt( double lat , double lon , double alt ){
		latitude = lat;
		longitude = lon;
		altitude = alt;
	}
	
	public String toString(){
		return ( "lat:" + latitude + " lon:" + longitude + " alt:" + altitude );
	}
	
}
		