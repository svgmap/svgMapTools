package org.svgmap.shape2svgmap;

// License: (MPL v2)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

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
		