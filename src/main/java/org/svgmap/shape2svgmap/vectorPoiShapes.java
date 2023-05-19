package org.svgmap.shape2svgmap;

// SVG MapのPOIのpathによる形状書庫です
// Programmed by Satoru Takagi
// まずは面積4程度のものを作る
// 2016.2.25 広く使われつつあるため、スケーラブルな関数型のメカニズムへの改良を始める
//
// issues: 円がほしい

import java.io.*;
import java.io.IOException;
import java.util.*;
import java.lang.*;
import java.net.URL;
import java.net.URI;
import java.text.NumberFormat ;

public class vectorPoiShapes{
	private NumberFormat nFmt;
	
	public String[] shapes  = new String[12];
	public String[] fills   = new String[3];
	public String[] strokes = new String[3];
//	public static int HRECT = 0;
//	public static int SRECT = 1;
//	public static int TRIANGLE = 2;
	
//	public static final String hrect = "m-1,-1 l2,0 l0,2 l-2,0 l0,-2 z";
	public static final String hrect = "m-3,-3 l6,0 l0,6 l-6,0 l0,-6 z"; // 正方形 (面積36)
//	public static final String srect = "m-1.4,0 l1.4,-1.4 l1.4,1.4 l-1.4,1.4 l-1.4,-1.4 z";
	public static final String srect = "m-4.2,0 l4.2,-4.2 l4.2,4.2 l-4.2,4.2 l-4.2,-4.2 z"; // ひし形
//	public static final String triangle = "m0,-1.75 l1.52,2.63 l-3.04,0 l1.52,-2.63 z";
	public static final String triangle = "m0,-5.25 l4.56,7.89 l-9.12,0 l4.56,-7.89 z"; // 三角形
	public static final String itriangle = "m0,5.25 l4.56,-7.89 l-9.12,0 l4.56,7.89 z"; // 下向き三角形
	public static final String hrect2 = "m-4,-2 l8,0 l0,4 l-8,0 l0,-4 z"; // 横長四角形
	public static final String srect2 = "m-5.6,0 l5.6,-2.8 l5.6,2.8 l-5.6,2.8 l-5.6,-2.8 z"; // 横長ひし形
	public static final String hrect3 = "m-2,-4 l4,0 l0,8 l-4,0 l0,-8 z"; // 縦長四角形
	public static final String srect3 = "m-2.8,0 l2.8,-5.6 l2.8,5.6 l-2.8,5.6 l-2.8,-5.6 z"; // 横長ひし形
	
	// 単位面積を持つ基本形状4種(四角形、45°回転四角形、三角形、下向き三角形)関数用定数
	public static final double[][] rectC = {{-0.5,-0.5},{1,0},{0,1},{-1,0},{0,-1}};
	public static final double[][] srectC = {{-0.71,0},{0.71,-0.71},{0.71,0.71},{-0.71,0.71},{-0.71,-0.71}};
	public static final double[][] triangleC = {{-0.76,0.44},{1.52,0},{-0.76,-1.32},{-0.76,1.32}};
	public static final double[][] striangleC = {{-0.76,-0.44},{1.52,0},{-0.76,1.32},{-0.76,-1.32}};
	
	
	public static final String aqua =   "#00ffff";
	public static final String orange = "#ffa500";
	public static final String pink =   "#ffc0cb";
	public static final String blue =   "#0000ff";
	public static final String red =    "#ff0000";
	public static final String green =  "#00ff00";
	
	public static final int AQUA_F   = 0;
	public static final int ORANGE_F = 1;
	public static final int PINK_F   = 2;
	public static final int BLUE_S   = 0;
	public static final int RED_S    = 1;
	public static final int GREEN_S  = 2;
	
	
	// getDstring のためのTYPE static
	public static final int RECTANGLE  = 1; // 四角形
	public static final int RECTANGLE2 = 5; // 同横長
	public static final int RECTANGLE3 = 9; // 同縦長
	
	public static final int SRECTANGLE  = 2; // 45°四角形
	public static final int SRECTANGLE2 = 6; // 同横長
	public static final int SRECTANGLE3 = 10; // 同縦長
	
	public static final int TRIANGLE  =  3; // 三角形
	public static final int TRIANGLE2 =  7; // 同横長
	public static final int TRIANGLE3 = 11; // 同縦長
	
	public static final int STRIANGLE  =  4; // 下向き三角形
	public static final int STRIANGLE2 =  8; // 同横長
	public static final int STRIANGLE3 = 12; // 同縦長
	
	
	public static final Map<String, Integer> shapeList = 
	new HashMap<String, Integer>() {
		{
			put("rectangle", 1);
			put("rectangle2", 5);
			put("rectangle3", 9);
			put("srectangle", 2);
			put("srectangle2", 6);
			put("srectangle3", 10);
			put("triangle", 3);
			put("triangle2", 7);
			put("triangle3", 11);
			put("striangle", 4);
			put("striangle2", 8);
			put("striangle3", 12);
			
			// 短縮名称
			put("rect", 1);
			put("rect2", 5);
			put("rect3", 9);
			put("srect", 2);
			put("srect2", 6);
			put("srect3", 10);
			put("tri", 3);
			put("tri2", 7);
			put("tri3", 11);
			put("stri", 4);
			put("stri2", 8);
			put("stri3", 12);
			
			// 45°四角形の別名
			put("diamond", 2);
			put("diamond2", 6);
			put("diamond3", 10);
			put("dia", 2);
			put("dia2", 6);
			put("dia3", 10);
			
			//逆三角形別名
			put("itriangle", 4);
			put("itriangle2", 8);
			put("itriangle3", 12);
			put("itri", 4);
			put("itri2", 8);
			put("itri3", 12);
	  }
	};
	
	
	
	public static void main(String[] args) {
		vectorPoiShapes vps = new vectorPoiShapes();
		String ans = vps.getPoiShapeAttrs( 123.456 , -654.321 , 1 , 1 , 1 , 3 , true );
		System.out.println(ans);
		System.out.println(vps.getDstring(vectorPoiShapes.RECTANGLE, 6 , 6) );
		System.out.println(vectorPoiShapes.shapeList.get("rectangle2") );
	}
	
	// 
	public String getDstring( int type , double scaleX , double scaleY ){
		// scaleX,Y:引き伸ばし量。　ただし、もとの図形に対する引き伸ばしなので、*2,*3図形では加えて引き伸ばされるイメージ
		
		if ( type > 4 ){
			if ( type > 8 ){
				// ほぼ単位面積のまま、縦長
				scaleX *= 0.7;
				scaleY *= 1.4;
				type -=8;
			} else {
				// ほぼ単位面積のまま、横長
				scaleX *= 1.4;
				scaleY *= 0.7;
				type -=4;
			}
		}
		
		double[][] points=null;
		switch (type){
		case RECTANGLE:
			points = rectC;
			break;
		case SRECTANGLE:
			points = srectC;
			break;
		case TRIANGLE:
			points = triangleC;
			break;
		case STRIANGLE:
			points = striangleC;
			break;
		}
		
		String ans = "m" + nFmt.format(scaleX * points[0][0])+","+nFmt.format(scaleY * points[0][1]);
		for ( int i = 1 ; i < points.length ; i++ ){
			ans += "l"+nFmt.format(scaleX * points[i][0])+","+nFmt.format(scaleY * points[i][1]);
		}
		ans +="z";
		return ( ans );
	}
	
	vectorPoiShapes(){
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(3);
		
//		initVectorPoiShapes(nf);
		initVectorPoiShapesF(nf,6,6);
	}
	
	vectorPoiShapes(double sizeX, double sizeY){
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(3);
		
		initVectorPoiShapesF(nf, sizeX, sizeY);
	}
	
	vectorPoiShapes(double sizeX, double sizeY , NumberFormat nf){
//		NumberFormat nf = NumberFormat.getNumberInstance();
//		nf.setGroupingUsed(false);
//		nf.setMaximumFractionDigits(3);
		
		initVectorPoiShapesF(nf, sizeX, sizeY);
	}
	
	vectorPoiShapes(NumberFormat nf){
		initVectorPoiShapesF(nf,6,6);
	}
	
	private void initVectorPoiShapes(NumberFormat nf){
		nFmt = nf;
		shapes[0] = hrect;
		shapes[1] = srect;
		shapes[2] = triangle;
		shapes[3] = itriangle;
		shapes[4] = hrect2;
		shapes[5] = srect2;
		shapes[6] = triangle;
		shapes[7] = itriangle;
		shapes[8] = hrect3;
		shapes[9] = srect3;
		shapes[10] = triangle;
		shapes[11] = itriangle;
		
		fills[AQUA_F]   = aqua;
		fills[ORANGE_F] = orange;
		fills[PINK_F]   = pink;
		
		strokes[BLUE_S]  = blue;
		strokes[RED_S]   = red;
		strokes[GREEN_S] = green;
	}
	
	
	private void initVectorPoiShapesF(NumberFormat nf , double sizeX, double sizeY){ // 2016.2.26
		if ( sizeX <= 0 ){
			sizeX = 6;
		}
		if ( sizeY <= 0 ){
			sizeY = sizeX;
		}
		
		nFmt = nf;
		for ( int i = 0 ; i < 12 ; i++ ){
			shapes[i] = getDstring( i+1 , sizeX , sizeY );
		}
		
		fills[AQUA_F]   = aqua;
		fills[ORANGE_F] = orange;
		fills[PINK_F]   = pink;
		
		strokes[BLUE_S]  = blue;
		strokes[RED_S]   = red;
		strokes[GREEN_S] = green;
	}
	
	
	public String getShape(int type ){
		return ( shapes[type-1] );
	}
	
	public String getFill(int type ){
		return ( fills[type] );
	}
	
	public String getStroke(int type ){
		return ( strokes[type] );
	}
	
	public String getPoiShapeAttrs( double x , double y , int shapeType , int fillType , int strokeType , double strokeWidth , boolean nonScalingObject){
		// ほとんど使われていない？
		String strokeColor = strokes[strokeType];
		String fillColor = fills[fillType];
		String ans = getPoiShapeAttrs( x , y , shapeType , strokeColor , strokeColor , strokeWidth , nonScalingObject);
		return ( ans );
		
	}
	
	public String getPoiShapeAttrs( double x , double y , int shapeType , String fillColor , String strokeColor , double strokeWidth , boolean nonScalingObject){
		String ans;
		if ( nonScalingObject ){
			ans = "transform=\"ref(svg," + nFmt.format(x) + "," + nFmt.format(y) + ")\" ";
			ans +="d=\"M" + nFmt.format(0) + "," + nFmt.format(0) + " ";
		} else {
			ans ="d=\"M" + nFmt.format(x) + "," + nFmt.format(y) + " ";
		}
		ans += shapes[shapeType-1] + "\" ";
		
		if ( strokeColor.length() > 0){
			ans += "stroke=\"" + strokeColor + "\" ";
		}
		if ( fillColor.length() > 0 ){
			ans += "fill=\"" + fillColor + "\" ";
		}
		if ( strokeWidth > 0 ){
			ans += "stroke-width=\"" + strokeWidth + "\" ";
		}
		return ( ans );
		
	}
	
	public String getSizedPoiShapeAttrs( double x , double y , int shapeType , double size , String fillColor , String strokeColor , double strokeWidth , boolean nonScalingObject){
		String ans;
		if ( nonScalingObject ){
			ans = "transform=\"ref(svg," + nFmt.format(x) + "," + nFmt.format(y) + ")\" ";
			ans +="d=\"M" + nFmt.format(0) + "," + nFmt.format(0) + " ";
		} else {
			ans ="d=\"M" + nFmt.format(x) + "," + nFmt.format(y) + " ";
		}
//		ans += shapes[shapeType-1] + "\" ";
		ans += getDstring(shapeType, size, size)+ "\" ";
		
		if ( strokeColor.length() > 0){
			ans += "stroke=\"" + strokeColor + "\" ";
		}
		if ( fillColor.length() > 0 ){
			ans += "fill=\"" + fillColor + "\" ";
		}
		if ( strokeWidth > 0 ){
			ans += "stroke-width=\"" + strokeWidth + "\" ";
		}
		return ( ans );
		
	}
}