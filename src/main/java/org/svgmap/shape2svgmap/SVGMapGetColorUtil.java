package org.svgmap.shape2svgmap;

// SVGMapGetColorUtil featureのprop値からcolorを取り出すユーティリティ
// もともとはShape2SVGMap*の内部にあったものを切り出し。
// Shape2ImageSVGMapに同等機能を設けるために作成
// Shape2SVGMap*もこちらに機能を移譲する方向を考えている
//
// Programmed by Satoru Takagi (2014/05-)
// Copyright 2014 by Satoru Takagi @ KDDI All Rights Reserved
//
// 2016/02/05 colorkeysを数値型でも有効にした
// 2016/04/0x Shape2SVGMap19以降は、これを使用する形に共通化したいがまだ・・
// 2017/05/15 outofrangeに対する処理を三択(skip,nullColor,counterStop)
// 2018/08/31 truncatedKey追加 文字制限よりも長くて切り捨てた場合にそのkeyを同定するためのhash for shape2ImageSvgMap bug fix
//



import java.awt.*;
import java.awt.image.*;

import java.util.*;


import java.io.*;
import java.net.*;
import javax.imageio.*;
import java.text.NumberFormat ;
import java.text.DecimalFormat;
import java.awt.geom.*;


import org.geotools.data.shapefile.*;
import org.geotools.data.simple.*;
import org.opengis.filter.*;
import org.geotools.factory.*;
import org.geotools.geometry.jts.*;
import org.geotools.map.*;
import org.geotools.renderer.lite.*;
import org.geotools.styling.*;
import org.geotools.feature.*;
import org.opengis.feature.simple.*;
import org.opengis.referencing.crs.*;
import org.opengis.feature.type.*;

import com.vividsolutions.jts.geom.*;

public class SVGMapGetColorUtil {
	// numcolorの方法
	static final int RED = 0;
	static final int HSV = 1;
	static final int QUOTA = 2;
	static final int iHSV = 3;
	
	// outOfRagteの表現方法
	static final int MARK = 0; // アウトオブレンジカラーでマーク ( default )
	static final int SKIP = 1; // 表示しない～スキップ
	static final int COUNTER_STOP = 2; // 上限・下限値でスティック
	
	static int colorMapMax=1024;
	static String nullColor = "#808080";
	boolean counterStop = false;
	
	DecimalFormat tzformat = new DecimalFormat("0.###########");
	// fsShape    データ
	// mColorCol  メインの色の属性番号
	// oColorCol  サブ(アウトライン)の色の属性番号
	// colorTable 色テーブル(SVGMapGetColorUtil.RED,SVGMapGetColorUtil.HSV,SVGMapGetColorUtil.QUOTA,SVGMapGetColorUtil.iHSV)
	// keyLength  文字列値の場合のキー長
	// strIsSJIS  文字列の場合の文字コードがSJISかどうか
	// strIsNative 文字列の場合の文字コードがなんも変更しなくていいかどうか (geotools9.5で改善したところかな)
	// colorKeys  
	//		文字列におけるハッシュキー(カラーテーブル)を列挙型文字列で指定（色並び：昇順）");
	//		CSVで属性値を列挙する。各属性値の後に#xxxxxxが続く場合は直接色も指定できる");
	//		ハッシュキーの文字列長さは全て同じでないとならない。-numcolorはその値から自動設定");
	//		強く関係するオプション：色の割付け:-numcolor");
	//		例１：属性値１,属性値２,....　例２：属性値１#F08020,属性値２#30D000,....");
	//
	// エクステントを計算するために getAttrExtent( boolean makeStringColorMap ) を呼ぶ必要がある。
	
	SVGMapGetColorUtil(FeatureCollection fsShapeI , int mColorColI , int oColorColI , int colorTableI , int keyLengthI , boolean strIsSJISI , boolean strIsNativeI , String colorKeys ){
		keyLength = keyLengthI;
		colorTable = colorTableI;
		strIsSJIS = strIsSJISI;
		strIsNative = strIsNativeI;
		mColorCol = mColorColI;
		oColorCol = oColorColI;
		fsShape = fsShapeI;
//		getAttrExtent( fsShape , mColorCol , oColorCol , false );
		if ( colorKeys!=""){
			mainAttrIsNumber = false;
			colorMap = initColorKeyEnum( colorKeys );
		}
	}
	
	SVGMapGetColorUtil(FeatureCollection fsShapeI , int mColorColI , int oColorColI , int colorTableI , int keyLengthI , boolean strIsSJISI , String colorKeys ){
		keyLength = keyLengthI;
		colorTable = colorTableI;
		strIsSJIS = strIsSJISI;
		mColorCol = mColorColI;
		oColorCol = oColorColI;
		fsShape = fsShapeI;
//		getAttrExtent( fsShape , mColorCol , oColorCol , false );
		if ( colorKeys!=""){
			mainAttrIsNumber = false;
			colorMap = initColorKeyEnum( colorKeys );
		}
	}
	
	SVGMapGetColorUtil(FeatureCollection fsShapeI , int mColorColI  ){
		mColorCol = mColorColI;
		fsShape = fsShapeI;
//		getAttrExtent( fsShape , mColorCol , oColorCol , false );
	}
	
	SVGMapGetColorUtil(FeatureCollection fsShapeI , int mColorColI , int colorTableI  ){
		colorTable = colorTableI;
		mColorCol = mColorColI;
		fsShape = fsShapeI;
//		getAttrExtent( fsShape , mColorCol , oColorCol , false );
	}
	
	FeatureCollection fsShape;
	
	int mColorCol = -1;
	int oColorCol = -1;
	int colorTable = HSV;
	int keyLength = 128;
	boolean strIsSJIS = true;
	boolean strIsNative = false;
	
	public HashMap<Object,String> colorMap = new HashMap<Object,String>(); // ここに文字列～列挙値の場合のカラーテーブルがたまる(Key:文字列,Val:#color)
	public HashSet<String> truncatedKey = new HashSet<String>(); // 上のcolorMapのKeyが文字数上限越えでカットされたものの場合に設定 2018/8/31 added for shape2ImageSvgMap
	
	
	public double mainAttrMax = -9e99;
	public double mainAttrMin = 9e99;
	public double outlineAttrMax = -9e99;
	public double outlineAttrMin = 9e99;
	
	public boolean mainAttrIsNumber = true;
	public boolean outlineAttrIsNumber = true; 
	
	public String getMainColor( SimpleFeature  oneFeature ){
		return ( getColor( oneFeature.getAttribute(mColorCol) , mainAttrMin , mainAttrMax) );
	}
	public String getOutlineColor( SimpleFeature  oneFeature ){
		if ( oColorCol >=0 ){
			return ( getColor( oneFeature.getAttribute(oColorCol) , outlineAttrMin , outlineAttrMax) );
		} else {
			return ("none");
		}
	}
	
	// extentが数値の場合はtrue stringの場合はfalse
	public void getAttrExtent( ){
		getAttrExtent( fsShape , mColorCol , oColorCol , false );
	}
	
	public void getAttrExtent( boolean makeStringColorMap ){
		initColorSeq();
		getAttrExtent( fsShape , mColorCol , oColorCol , makeStringColorMap );
		initColorSeq();
	}
	
	public void setOutOfRangeView( int method ){
		if ( method == COUNTER_STOP ){
			counterStop = true;
		} else if ( method == SKIP ){
			nullColor ="";
		} else if ( method == MARK ){ 
			// default mark by nullColor
		} else {
			// do nothing
		}
	}
	
	
	private boolean useColorKeys = false;
	public String getColor( Object sValue , double attrMin , double attrMax){
		String key;
		if ( sValue == null ){
			return (nullColor);
		}
		String color = "green";
		double dValue = 0.0;
		if ( sValue instanceof String ){ // 文字列ベースの属性から色をつくる
//			System.out.println("color build fm STRING");
			String sVal = getKanjiProp((String)sValue);
			if ( sVal.length() > keyLength ){
				key = sVal.substring(0,keyLength);
			} else {
				key = sVal;
			}
//			key = (String)sValue + "  ";
//			key = key.substring(0,keyLength);
			
			if ( useColorKeys ){ // あらかじめ設定されたカラーテーブルを使う 2013/2
				if ( colorMap.containsKey(key) ){
					color = (String)colorMap.get(key);
				} else {
					color = nullColor; // テーブルに無いものは灰色
				}
			} else { // 適当な配分
				if ( colorMap.containsKey(key) ){
					color = (String)colorMap.get(key);
				} else {
					
					getRGBtSeq();
					color = "#" 
						+ Integer.toHexString(btR+256).substring(1,3)
						+ Integer.toHexString(btG+256).substring(1,3)
						+ Integer.toHexString(btB+256).substring(1,3);
					
	//				color = "#" + Integer.toHexString(getBtSeq()).toUpperCase();
					colorMap.put(key , color);
				}
			}
//			System.out.println(sVal + " : " + key + " : " + color);
		} else { // 数値ベースの属性から色を作る
//			System.out.println("color build fm number");
			if ( sValue instanceof Integer ){
				dValue = ((Integer)sValue).doubleValue();
			} else if ( sValue instanceof Double ){
				dValue = ((Double)sValue).doubleValue();
			} else if ( sValue instanceof Long ){
				dValue = ((Long)sValue).doubleValue();
			}
			
			// 最小最大値を超えたものは無効色を設定する（か色をクリップする）
			// 本来、この仕様もオプションで選べるべきかも
			if ( dValue < attrMin ){
				if ( !counterStop ){
					color = nullColor;
					dValue = attrMin;
	//				System.out.println("< attrMin :" + color);
					return ( color );
				} else {
					dValue = attrMin;
				}
			} else if ( dValue >attrMax ){
				if ( !counterStop ){
					color = nullColor;
					dValue = attrMax;
	//				System.out.println("> attrMax :" + color);
					return ( color );
				} else {
					dValue = attrMax;
				}
			} 
			
			if ( useColorKeys ){ // 数値に対してもカラーキーテーブルを使えるようにした(2016.2.5)
				key = tzformat.format(dValue);
				if ( colorMap.containsKey( key ) ){
					color = (String)colorMap.get(key);
				} else {
					color = nullColor; // テーブルに無いものは灰色
				}
			} else if ( colorTable == HSV || colorTable == iHSV){
				// HSV色　赤(0)：最低、　青(270ぐらい)：最高　で塗りわけ (多分300ぐらいまでが妥当かと)
//				getRGB( ( 360.0 * ( dValue - attrMin )  /  ( attrMax - attrMin ) ) , 1.0 , 1.0 );
				if ( dValue >= attrMin && dValue <= attrMax){
					if ( colorTable == iHSV ){
						getRGB( ( 270.0 * ( dValue - attrMin )  /  ( attrMax - attrMin ) ) , 1.0 , 1.0 );
					} else {
						getRGB( 270.0 - ( 270.0 * ( dValue - attrMin )  /  ( attrMax - attrMin ) ) , 1.0 , 1.0 );
					}
				} else { // 上の設定で、これはなくなってるかな（この辺がオプション選択制の理由）
					cr = 0;
					cg = 0;
					cb = 0;
				}
				color = "#" + Integer.toHexString(256 + cr).substring(1,3)
				+ Integer.toHexString(256 + cg).substring(1,3)
				+ Integer.toHexString(256 + cb).substring(1,3);
				if ( colorMap.size() < colorMapMax ){ // 数値のcolorMapは使われていないのでは？（最後にコメント出力する用途だけでしか使われてない）
					if ( ! colorMap.containsKey(sValue) ){
						colorMap.put( sValue, color );
					}
				}
			} else if ( colorTable == RED) {
				color = "#" + Integer.toHexString(256 + (int)(255.9 * ( (dValue - attrMin ) / (attrMax - attrMin)))).substring(1,3) + "0000";
				if ( colorMap.size() < colorMapMax ){
					if ( ! colorMap.containsKey(sValue) ){
						colorMap.put( sValue, color );
					}
				}
			} else { // もっともいい加減な色分け法
				key = Double.toString(dValue);
//				key = key.substring(0,keyLength);
				if ( colorMap.containsKey(key) ){
					color = (String)colorMap.get(key);
				} else {
					
					getRGBtSeq();
					color = "#" 
						+ Integer.toHexString(btR+256).substring(1,3)
						+ Integer.toHexString(btG+256).substring(1,3)
						+ Integer.toHexString(btB+256).substring(1,3);
					colorMap.put(key , color);
				}
				
			}
			
//			System.out.println("in:" + sValue + " double:" + dValue + " color:" + color );
		}
		
		
		return ( color );
	}
	
	
	public void setAttrExtent(double mainAttrMin , double mainAttrMax , double outlineAttrMin , double outlineAttrMax ){
		this.mainAttrMax = mainAttrMax;
		this.mainAttrMin = mainAttrMin;
		this.outlineAttrMax = outlineAttrMax;
		this.outlineAttrMin = outlineAttrMin;
		this.mainAttrIsNumber = true;
		this.outlineAttrIsNumber = true;
	}
	
	private void getAttrExtent(FeatureCollection fsShape , int mColorCol , int oColorCol , boolean makeStringColorMap ){
		SimpleFeature oneFeature = null;
		Object valueM , valueO;
		double dvalM = 0;
		double dvalO = 0;
		@SuppressWarnings("unchecked")
		FeatureIterator<SimpleFeature> reader = fsShape.features();
		boolean hasFeature;
		boolean mValIsStr = false;
		boolean oValIsStr = false;
		while (reader.hasNext()) {
			hasFeature = false;
			while ( hasFeature == false ){
				try{
					oneFeature = reader.next();
					hasFeature = true;
				} catch ( Exception e ){
					System.out.print("ERR");
					hasFeature = false;
				}
			}
			if ( mColorCol >=0  && !mValIsStr ){
				valueM = (oneFeature.getAttribute(mColorCol));
				if ( valueM != null ){
					if ( valueM instanceof Double ){
						dvalM = ((Double)valueM).doubleValue();
					} else if ( valueM instanceof Integer ){
						dvalM = ((Integer)valueM).intValue();
					} else if ( valueM instanceof Long ){
						dvalM = ((Long)valueM).longValue();
					} else {
						mainAttrIsNumber = false;
						if ( makeStringColorMap ){
							registColorMap((String)valueM);
						} else {
							mValIsStr = true;
							System.out.println("main Value is String");
						}
					}
					
					if ( dvalM > mainAttrMax ){
						mainAttrMax = dvalM;
					}
					if ( dvalM < mainAttrMin ){
						mainAttrMin = dvalM;
					}
				}
			}
			if ( oColorCol >=0 && !oValIsStr ){
				valueO = (oneFeature.getAttribute(oColorCol));
				if ( valueO != null ){
					if ( valueO instanceof Double ){
						dvalO = ((Double)valueO).doubleValue();
					} else if ( valueO instanceof Integer ){
						dvalO = ((Integer)valueO).intValue();
					} else if ( valueO instanceof Long ){
						dvalO = ((Long)valueO).longValue();
					} else {
						outlineAttrIsNumber = false;
						if ( makeStringColorMap ){
							registColorMap((String)valueO);
						} else {
							oValIsStr = true;
							System.out.println("outline Value is String");
						}
					}
					
					if ( dvalO > outlineAttrMax ){
						outlineAttrMax = dvalO;
					}
					if ( dvalO < outlineAttrMin ){
						outlineAttrMin = dvalO;
					}
				}
			}
			if ( mValIsStr && oValIsStr ){
				// 両方とも文字列だったら意味ないので終了
				break;
			}
		}
		if ( mColorCol >=0 && !mValIsStr ){
			System.out.println( "mainColorAttr    Col:" + mColorCol + ": Min:" + mainAttrMin + " Max:" + mainAttrMax );
		}
		if ( oColorCol >=0 && !oValIsStr ){
			System.out.println( "outlineColorAttr Col:" + oColorCol + ": Min:" + outlineAttrMin + " Max:" + outlineAttrMax );
		}
	}
	
	// グローバル変数に直接投入している
	private void registColorMap( String sValue ){
		String sVal = getKanjiProp((String)sValue);
		String key;
		if ( sVal.length() > keyLength ){
			key = sVal.substring(0,keyLength);
			if ( !truncatedKey.contains(key) ){  // for shape2ImageSvgMap 2018.8.31
				truncatedKey.add(key);
			}
		} else {
			key = sVal;
		}
		if ( colorMap.containsKey(key) ){
		} else {
			getRGBtSeq();
			String color = "#" 
				+ Integer.toHexString(btR+256).substring(1,3)
				+ Integer.toHexString(btG+256).substring(1,3)
				+ Integer.toHexString(btB+256).substring(1,3);
			colorMap.put(key , color);
		}
	}
	
	private HashMap<Object,String> initColorKeyEnum( String colorKeys ){ // add 2013/2 for -colorkey
		
		
		HashMap<Object,String> colorMap = new HashMap<Object,String>();
		colorMap.put("default" , "green");
		
		if (colorKeys.length() == 0){
			return ( colorMap );
		}
		
		String[] colorKeyEnum  = colorKeys.split(",");
//		System.out.println("colorEnum:" + colorKeys );
		int kl=0;
		keyLength = 0;
		if ( colorKeyEnum.length > 1 ){
			useColorKeys = true;
			for ( int i = 0 ; i < colorKeyEnum.length ; i++ ){
				String ck;
				String color="";
				
				ck = colorKeyEnum[i];
//				System.out.println("orig:" + ck);
				
				if ( ck.indexOf("#") > 0){ // パラメータで色を明示しているケース
					color = ck.substring(ck.indexOf("#")); // 色を設定する
					ck = ck.substring(0,ck.indexOf("#"));
//					System.out.println("incl#" + ck.indexOf("#") + " : " + ck + " : " + color);
				}
				
				
				// keyの長さは、パラメータから自動設定させる。　すべてのキーの文字列長は等しい必要がある
				kl = ck.length();
				if ( keyLength < kl ){
					keyLength = kl;
				}
				/**
				if ( i == 0 ){
					keyLength = ck.length();
				}
				
				if ( ck.length() != keyLength ){
					System.out.println("ERROR! Inconsistent key length.");
					System.exit(0);
				}
				**/
				/**
				if ( ck.length() > keyLength ){ // keyLengthよりも指定したKeyが長いときは短縮する
					ck = ck.substring(0,keyLength);
				} else {
					ck = ck;
				}
				**/
				
				if ( color == ""){
					// パラメータで色を明示していないケースでは、パラメータの並び順をベースにHTV||invHSVで色を設定する
					if ( colorTable == iHSV ){
						getRGB( ( 270.0 * i  /  ( colorKeyEnum.length - 1 ) ) , 1.0 , 1.0 );
					} else {
						getRGB( 270.0 - ( 270.0 * i /  ( colorKeyEnum.length - 1 ) ) , 1.0 , 1.0 );
					}
					color = "#" + Integer.toHexString(256 + cr).substring(1,3)
						+ Integer.toHexString(256 + cg).substring(1,3)
						+ Integer.toHexString(256 + cb).substring(1,3);
				}
//				System.out.println("colorMap:" + ck + " : " + color );
				colorMap.put(ck , color);
			}
			System.out.println( "colorMap(" + colorMap.size() + "vals , keyLength:" + keyLength + "):" + colorMap );
		} else {
			System.out.println("ERROR! -colorkey syntax error");
			System.exit(0);
		}
		
		return ( colorMap );
	}

	private void test(){
		for (double h = 0 ; h < 360 ; h +=1 ){
			getRGB( h , 1.0 , 1.0 );
			System.out.println( "H:" + h + " : " + cr + ":" + cg + ":" + cb );
		}
	}
	
	
	private int cr, cg, cb;
	private void getRGB( double h , double s , double v ){
		// h: 0-360 , s: 0-1 , v: 0-1
		double f ;
		int m , n , k ;
		int i = (int)( h / 60.0 );
		v = v * 255.9;
		f = h / 60.0  - i;
		m = (int)(v * ( 1.0 - s ));
		n = (int)(v * ( 1.0 - s * f ));
		k = (int)(v * ( 1.0 - s * ( 1.0 - f ) ));
		switch (i){
		case 0:
			cr = (int)v;
			cg = k;
			cb = m;
			break;
		case 1:
			cr = n;
			cg = (int)v;
			cb = m;
			break;
		case 2:
			cr = m;
			cg = (int)v;
			cb = k;
			break;
		case 3:
			cr = m;
			cg = n;
			cb = (int)v;
			break;
		case 4:
			cr = k;
			cg = m;
			cb = (int)v;
			break;
		case 5:
			cr = (int)v;
			cg = m;
			cb = n;
			break;
		default:
			break;
		}
		
	}
	
	
	private static int btMin = 0;
	private static int btMax= 256;
	private int btR = 128;
	private int btG = 128;
	private int btB = 128;
	private int btStart = 128;
	private int btStep = 256;
	
	private void initColorSeq(){
		btR = 128;
		btG = 128;
		btB = 128;
		btStart = 128;
		btStep = 256;
	}
	
	private void getRGBtSeq(){
		if ((btR + btStep) <= btMax ){
			btR += btStep;
		} else {
			btR = btStart;
			if ((btG + btStep) <= btMax ){
				btG += btStep;
			} else {
				btG = btStart;
				if ((btB + btStep) <= btMax ){
					btB += btStep;
				} else {
					btStep = btStep / 2;
					btStart = btStep/2;
					btB = btStart;
					btG = btStart;
					btR = btStart;
					
				}
			}
		}
	}
	
	
	private int btSp = 256;
	private int btPrev = -1;
	private int getBtSeq(){
		// 二分木数列の生成
		if (btPrev < 0){
			btPrev = 0;
		} else if ( (btPrev + btSp) < btMax ) {
			btPrev += (btSp + btSp );
		} else {
			btSp = btSp/2;
			if (btSp < 1){
				btSp = btMax;
				btPrev = btMin;
			} else {
				btPrev = btSp;
			}
		}
		
		return ( btPrev );
	}

	String getKanjiProp( String input ){
		String ans ="";
		try {
			if ( strIsNative ){
				ans = input;
			} else {
				if ( strIsSJIS ){
					// 2013/02/15 WINDOWS...
	//				ans =  (new String(((String)input).getBytes("iso-8859-1"),"Shift_JIS")).trim();
					ans =  (new String(((String)input).getBytes("iso-8859-1"),"Windows-31J")).trim();
				} else {
					ans =  (new String(((String)input).getBytes("iso-8859-1"),"UTF-8")).trim();
				}
			}
		} catch (Exception e){
			ans = "";
		}
		return ( ans );
	}

}