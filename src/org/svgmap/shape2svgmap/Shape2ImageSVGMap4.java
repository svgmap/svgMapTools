package org.svgmap.shape2svgmap;

// Shape2ImageSVGMap4 (shp2Imageから改名)
//
// shapefileからビットイメージタイル地図群とそのSVGコンテナを生成する
// Programmed by Satoru Takagi (2012/04)
// Copyright 2012-2018 by Satoru Takagi @ KDDI All Rights Reserved
//
// 2012/04/26 1st Ver
// 2012/05/10 mesh2DBを用いて、日本陸上部のみのデータ生成で若干高速化
// 2012/05/2? レベルを指定して分割するオプションを追加
// 2012/05/2? 表示領域をデータ領域に対する倍数で指定できるオプションを追加
// 2012/05/29 階層コンテナ(2段のみ)に対応、ビットイメージの付番方法のバグ対策(smsArray)
// 2012/05/31-性能改善にチャレンジ　ビットイメージを大きく作って切り分ける方法はどうか？
// 2012/06/01 上記方式を実装した結果、大幅な性能改善に成功！ -sumUp 16　等で256倍？の高速化
// 2012/06/04 ??
// 2013/03/27 POIのレンダリングにも対応
// 2013/07/18 効率改善(特にlvl1-15とかでfixareaのとき)
// 2014/02/20 まず「一つ上のレベルでnullになったタイルは、その下のレベルは作らない」の効率化
// 2014/02/27 shape2SvgMapの出力結果から、bitimageが必要なタイルだけを生成する。
// 2014/02/28 コンテナの二次元配列をhashMapにして効率化
// 2014/03/?? for nextと二次元配列のほとんどをhashMapにしてメモリ効率改善
// 2014/05/02 debug(line objが画面上で描画されないほど細かいとshape2SvgMapで必要とされるタイルが生成されない)
// 2014/05/13 POIの色をprop値に応じて変化可能に(from Shape2SVGMap)
// 2016/02/04 文字列型の属性に対して色テーブルを、指定できる機能 -colorkey (Shape2SVGMapと互換)
// 2016/02/05 数値型の属性に対しても色テーブルを指定できる拡張 (Shape2SVGMapと互換)
// 2016/02/12 数値型の属性に対する色付けで、iHSV等を選べる -numColor (Shape2SVGMapと互換)
// 2016/04/19 エラー処理を少し強化(lowresimage作る必要がないケース)
// 2016/08/02 shape2svgmap同様に外部ファイルからオプション読める機能追加
// 2017/02/20 Rev4: マルチスレッド化開発開始 shapefileから直接生成したfeaturesourceでは、性能の向上は僅かな感じ　やはり複数のファイルをオープンしてしまうこと自体が問題だと思われる。（さらに深い調査が必要）　これに対して、http://docs.geotools.org/stable/userguide/tutorial/datastore/source.htmlなどを参考に一つのshapefile featuresourceをバッファリング＆コピーして複数のスレッドに分配するfeaturesourceを実装してそれを用いるようにすると改善するかも？
// 2017/04/04 CSV読み込み対応 geotools8(8.7)が必要だが、imageio-ext-*.jarが不具合起こすので外しておく
// 2017/05/12 ようやく文字列でのフィルタがshape2svgmapと互換(前方一致)になったと思う
// 2018/08/10 漢字プロパティ名のカラムを色分けカラムに指定すると変換できない問題を解消。この際にshapefileの文字化けの問題の解消法も分かったのでそちらも対策、そしてこれが波及してcsv data storeもネイティブ文字コードをsjis文字化けではないまともな実装ができるように改修。今とのところshape2SvgMapでは不具合が起きていないので昔のままにしておくが、追々このまともな実装に改修するべき
// 2018/08/31 strkeyの長さ制限(keyLength)に対する細かな色条件判定が間違っていたのを修正
// 2018.09.21 csv入力での単純なlineString,polygonデータ対応
// 2018/09/25 マーカーを選択可能にした。meshマーカーは実空間サイズ(degrees)を設定可能。strokeColor省略オプション追加(fillColorと合わせる)。属性値に応じた色塗りでもstrokeを指定可能に('-'で省略も可能)
// 2020/01/21 WKTなどジオメトリタイプが混合したデータでも描画可能にし始めた(まだ値によって色が変わるものは未対応)
// 2020/04/09 メルカトル正方タイルを出力する機能を追加(ただし、SVGのほうはWGS84,PlateCaree想定のコンテナを生成(背景地図と合わせる))

import java.awt.*;
import java.awt.image.*;

import java.util.*;


import java.io.*;
import java.net.*;
import javax.imageio.*;
import java.text.NumberFormat ;
import java.awt.geom.*;

import java.nio.charset.*;

import org.geotools.data.shapefile.*;
import org.geotools.feature.*;
import org.geotools.data.simple.*;
import org.opengis.filter.*;
import org.opengis.filter.expression.Expression;
import org.geotools.filter.function.FilterFunction_geometryType;
import org.geotools.factory.*;
import org.geotools.geometry.jts.*;
import org.geotools.map.*;
import org.geotools.renderer.lite.*;
import org.geotools.styling.*;
import org.opengis.feature.simple.*;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.feature.type.*;
import org.geotools.referencing.CRS;

import com.vividsolutions.jts.geom.*;

// for CSV Reader/Exporter 2017.4.3
import org.svgmap.shape2svgmap.cds.CSVDataStore;
import org.svgmap.shape2svgmap.cds.CSVFeatureReader;
import org.svgmap.shape2svgmap.cds.CSVFeatureSource;
import org.geotools.data.store.ContentDataStore;

// use Executor 2017.5.18
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.svgmap.shape2svgmap.svgMapMarkFactory;

public class Shape2ImageSVGMap4 {
	
	boolean checkJp = false;
	boolean includeBaseMap = false;
	boolean webMercatorTile = false;
	int colorResolution = 48;
	
	int maxThreads = 4;
	ExecutorService svgMapExecutorService; // for Executor multi thread 2017.4.19
	
	jpLandTester jpt;
	
	String colorKeys =""; // 2016.2.4
	int colorTable = SVGMapGetColorUtil.HSV; // 2016.2.12
	
	// step[deg] (Options) (Layer1Path #fillColor #strokeColor strokewidth) (Layer2Path #fillColor #strokeColor strokewidth)
	//
	// 
	
	// for CSV Support 2017.4.3
	boolean inputCsv = false;
	boolean gZipped = false; // 2017.5.15
	String csvSchemaPath="";
	String charset ="MS932"; // 2017.7.20 CSVのときのcharset(shpには無効)
	
	// for outOfRange Color Control
	int outOfRangeView = SVGMapGetColorUtil.MARK;
	
	// for meshMarker
	double meshMarkerGeoHeight = -1;
	//
	File complementalSourceSvg = null;
	
	public static void showHelp(){
		System.out.println( "Shape2ImageSVGMap: Generate bit image tiles type SVG Map data from (Shapefile|csv)");
		System.out.println( "Copyright 2012 - 2018 by Satoru Takagi @ KDDI All Rights Reserved");
		System.out.println( "" );
		System.out.println( "Shape2ImageSVGMap TileSize (-Options) (Path4Shape.(shp|csv) (#fillColor | attrNumb) (#strokeColor | -) strokeWidth (symbolSize | sizedMarkerShapeName) )* " );
		System.out.println( "" );
		System.out.println( "TileSize: ( level | tileSize\"deg\" | sigle | statLevl-endLevel | (vectorTiledContainer.svg) )" );
		System.out.println( " tileSize: (tileSize)\"deg\"" );
		System.out.println( " level:    (0....nn:see below)" );
		System.out.println( " single: output as single bitimagemap" );
		System.out.println( " statLevl-endLevel:   output multi-level map" );
		System.out.println( " (vectorTiledContainer.svg) : build complemental image svgmap from vectorTiledContainer.svg");
		System.out.println( "" );
		System.out.println( "lvl: tileWidth[deg]" );
		System.out.println( "  0: 360deg");
		System.out.println( "  1: 180deg");
		System.out.println( "  2:  90deg");
		System.out.println( "  3:  45deg");
		System.out.println( "  4:  22.5deg");
		System.out.println( "  5:  11.25deg");
		System.out.println( "  6:   5.625deg");
		System.out.println( "  7:   2.8125deg");
		System.out.println( "  8:   1.40625deg");
		System.out.println( "  9:   0.703125deg (1.40624-0.703125)");
		System.out.println( " 10:   0.3515625deg");
		System.out.println( " 11:   0.17578125deg");
		System.out.println( " 12:   0.087890625deg");
		System.out.println( " 13:   0.0439453125deg");
		System.out.println( " ...");
		System.out.println( " ...");
		System.out.println( " For -webMercatorTile option, the above values correspond to the longitude axis, and for the latitude axis direction, the width of +-85.05113deg is divided by 2^lvl on the projection.");
		System.out.println( "" );
		System.out.println( " rootContainerSvgfileName : Build complemental multi-level tiles for density controlled vector tiles ");
		System.out.println( "" );
		System.out.println( "#fillColor   : fillColorString(in web color format)." );
		System.out.println( "attrNumb     : Attribute number for determining fillColor. Calculation logic is the same as Shape2SVGMap's -color option. Options for outline are ignored. (no outline)" );
		System.out.println( "               If you set [numb]R[min],[max] then attr range is forced min .. max.");
		System.out.println( "#strokeColor : strokeColorString(in web color format). You cannot set up an attribute number. If '-' is set then strokeColor equals to fillColor." );
		System.out.println( "strokewidith : width of stroke(in px)(int)." );
		System.out.println( "symbolSize   : size of symbol(in px)(int).   marker is circle" );
		System.out.println( "sizedMarkerShapeName  :  ( ('cross'|'circle'|'triangle'|'x'|'star'|'arrow'|'hatch'|'square')':'size | 'mesh:'lngspan:heightspan )" );
		System.out.println( "    size: number (in px)" );
		System.out.println( "    lngspan, heightspan : number (in geoCoords(deg)) , only for 'mesh')" );
		System.out.println( "    examples:  'triangle:10', 'mesh:0.1:0.1'");
		System.out.println( " If \"dash\" is added (ex. 1dash) then stroke is dashed." );
		System.out.println( "" );
		System.out.println( "-Options:" );
		System.out.println( " -viewBuffer : set buffer area for viewBox (param: default:1 (no Buffer))" );
		System.out.println( " -sumUp      : set sumUpRendering (param: default:1 (no sumUp)) should be even number" );
		System.out.println( " -tileSize   : set tileSize(width=height)(param: default:256)" );
		System.out.println( " -antiAlias  : set anti alias rendering (no param)" );
//		System.out.println( " -fixArea    : fix GeoArea to start level (for shp2svgmap compati.)" );
		System.out.println( " -jpOnly     : Build Japan area only (no param)" );
		System.out.println( " -colorkey   : Specify the enumeration-type color table in the case of changing a color according to the string attribute in CSV." );
		System.out.println( "   param Example : value,value.... (Each colors is defined arbitrarily.)");
		System.out.println( "                 : value#color,value#color.... (Specify a color to each value.)");
		System.out.println( " -strcolor   : Set key length for changing a color according to the string attribute" );
		System.out.println( "  param  default : 2");
		System.out.println( " -numcolor   : The color table on a numerical value (param RED:Red gradation, HSV:(min:blue,nax:red), iHSV:(min:red,max:blue), QUOTA:randomly)");
		System.out.println( "                default:HSV");
		System.out.println( " -outOfRange : How to handle values exceeding limits");
		System.out.println( "                 default : gray");
		System.out.println( "                 skip  : (=skipoutofrange)");
		System.out.println( "                 counterStop  : Stick to upper limit and lower limit");
		System.out.println( " -optionFile : set -options using text file (param: optiontextfile.txt)");
		System.out.println( " -threads    : set maximum number of threads (default 4)");
		System.out.println( " -webMercatorTile : Generates bit image tiles by Web Mercator projection. However, those containers are linear in latitude and longitude coordinates.");
		System.out.println( " -charset    : set charset for csv : Default:SJIS  (SJIS:UTF-8)");
		System.out.println( " -csvschema  : set schema file for csv");
		System.out.println( " Source CSV File rules:");
		System.out.println( "   Schema (name of each columns) of CSV should be set by the first row of source CSV or single line schema file using -csvschemaoption.");
		System.out.println( "   Properties other than coordinate values are basically treated as string.");
		System.out.println( "   ");
		System.out.println( "   Point feature:");
		System.out.println("      You should declare latitude and longitude column by schema name 'LAT' and 'LON' or 'LATITUDE' and 'LONGITUDE' (case insensitive).");
		System.out.println( "   LineString feature:");
		System.out.println("      Only one unbroken polyline is supported. A 'latitude:line' and 'longitude:line' pair must be at the last column of the schema. That is, other property must exist at the column before the coordinate value. And as actual data, arbitrary numbers of latitude and longitude pairs are listed from the last column.");
		System.out.println( "   Polygon feature:");
		System.out.println("      Only one polygon that is not split and is not even a donut is supported. The 'latitude:polygon' and 'longitude:polygon' pairs must be at the last column of the schema. Other than that it is the same as above.");
		System.out.println( "   WKT encoded Geometry feature:");
		System.out.println("      You should declare WKT column by schema name 'WKT' (case insensitive). If WKT data includes comma, you should escape the column by Double quotation.");
		System.out.println("    If you use -csvschema schema file must be single line and terminated (CR|LF).");
		
		
	}
	
	public static String[] getOptionFile( String[] mainArgs) {
		// -optionFile オプションがある場合、それを優先する
		String optFilePath = null;
		int firstShpPos = -1;
		for ( int i = 0 ; i < mainArgs.length ; i++ ){
			if ( mainArgs[i].toLowerCase().equals("-optionfile")){
				optFilePath = mainArgs[i+1];
			}
			
			if ( mainArgs[i].toLowerCase().endsWith(".shp") || mainArgs[i].toLowerCase().endsWith(".gz") || mainArgs[i].toLowerCase().endsWith(".dbf")  ||  mainArgs[i].toLowerCase().endsWith(".shx") ||  mainArgs[i].toLowerCase().endsWith(".csv")  ) {
				firstShpPos = i;
				break;
			}
		}
		
		if ( firstShpPos < 0 ){
			System.out.println("Error: No shapefile or csv file assigned.");
			return ( mainArgs );
		}
		
		if ( optFilePath != null ){
			String[] ans=null;
			try {
				optionsReader or = new optionsReader( new File(optFilePath) );
				String[] opts = or.getOptions();
				
				ArrayList<String> argList = new ArrayList<String>();
				
				argList.add(mainArgs[0]); // TileSize
				
				for ( int i = 0 ; i < opts.length ; i++ ){ // Options by File
					argList.add(opts[i]);
				}
				
				for ( int i = firstShpPos ; i < mainArgs.length ; i++ ){ // each shapefiles and styles
					argList.add(mainArgs[i]);
				}
				
				ans = (String[])(argList.toArray(new String[0]));
				
			} catch ( Exception e ){
				System.out.println("Error: Invalid -optionFile option");
				System.out.println("To display help, please launch Shape2ImageSVGMap without any options.\n");
				System.exit(0);
			}
			return ( ans );
		} else {
			return ( mainArgs );
		}
		
	}
	
	public static void main(String args[]) throws Exception {
		
		args = getOptionFile( args );
		
		Shape2ImageSVGMap4 s2i = new Shape2ImageSVGMap4();
		int startLvl = 0;
		int endLvl = 0;
		
		boolean complementalSvg = false;
		SvgMapContainerReader sr = null;
		
//		boolean fixArea = true; // これをデフォルトとする！(1)の効率化のため 2014.2.20
		
		if ( args.length<1){
			showHelp();
			System.exit(0);
		}
		
		try{
			if ( args[0].indexOf(".svg") > 0 ){
				complementalSvg = true;
				System.out.println("Use Complemental Vector Svg Container:" + args[0]);
				sr = new SvgMapContainerReader( args[0] );
				s2i.complementalSourceSvg = new File(args[0]);
				
			} else if ( args[0].indexOf("d")>0){
				s2i.partDeg = Double.parseDouble(args[0].substring(0,args[0].indexOf("d")));
			} else if ( args[0].indexOf("single")==0){
				s2i.singleImage = true;
			} else if ( args[0].indexOf("-") > 0 ){
				int ix = args[0].indexOf("-");
				startLvl = Integer.parseInt(args[0].substring(0,ix));
				endLvl = Integer.parseInt(args[0].substring(ix+1));
				if ( startLvl > endLvl ){
					int lt = startLvl;
					startLvl = endLvl;
					endLvl = lt;
				}
				System.out.println("level:" + startLvl + " to " + endLvl );
			} else {
				s2i.lvl = Integer.parseInt(args[0]);
			}
		} catch ( Exception  e ){
			System.out.println("\n\nError: Invalid TileSize parameter : " + args[0]);
			System.out.println("To display help, please launch Shape2ImageSVGMap without any options.\n");
			e.printStackTrace();
			System.exit(0);
		}
		
		int i = 1;
		
		try{
			while( args[i].indexOf("-")==0){
				if ( args[i].toLowerCase().indexOf("-viewbuffer")>=0){
					++i;
					s2i.viewBuffer = Double.parseDouble(args[i]);
					System.out.println("viewBuffer:" + s2i.viewBuffer);
				} else if ( args[i].toLowerCase().indexOf("-sumup")>=0){
					++i;
					int su = Integer.parseInt(args[i]);
					if ( su <= 1){
						su = 1;
					} else if( su % 2 == 1 ){ // sumUpは偶数のみを許す(ただし１以外)
						su = 2 * (int)(1 + su / 2);
					}
					s2i.sumUp = su;
					
					System.out.println("sumUp:" + s2i.sumUp);
				} else if ( args[i].toLowerCase().indexOf("-rebuildcontaineronly")>=0){
					s2i.rebuildContainerOnly = true;
					System.out.println("rebuildContainerOnly");
				} else if ( args[i].toLowerCase().indexOf("-includebasemap")>=0){
					s2i.includeBaseMap = true;
					System.out.println("includeBaseMap");
				} else if ( args[i].toLowerCase().indexOf("-antialias")>=0){
					s2i.antiAlias = true;
					System.out.println("includeBaseMap");
				} else if ( args[i].toLowerCase().indexOf("-tilesize")>=0){
					++i;
					s2i.imageWidth = Integer.parseInt(args[i]);
					s2i.imageHeight = s2i.imageWidth;
	//			} else if ( args[i].toLowerCase().indexOf("-fixarea")>=0){
	//				fixArea = true;
				} else if ( args[i].toLowerCase().indexOf("-jponly")>=0){
					s2i.checkJp = true;
				} else if ( args[i].toLowerCase().indexOf("-colorkey")>=0){
					++i;
					s2i.colorKeys = args[i];
				} else if ( args[i].toLowerCase().indexOf("-strcolor")>=0){
					++i;
					s2i.keyLength = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().indexOf("-numcolor")>=0){
					++i;
					if (args[i].toLowerCase().equals("hsv")){
						s2i.colorTable = SVGMapGetColorUtil.HSV;
					} else if (args[i].toLowerCase().equals("ihsv")){
						s2i.colorTable = SVGMapGetColorUtil.iHSV;
					} else if (args[i].toLowerCase().equals("red")){
						s2i.colorTable = SVGMapGetColorUtil.RED;
					} else if (args[i].toLowerCase().equals("quota")){
						s2i.colorTable = SVGMapGetColorUtil.QUOTA;
					}
				} else if ( args[i].toLowerCase().indexOf("-outofrange")>=0){
					++i;
					if (args[i].toLowerCase().equals("skip")){
						s2i.outOfRangeView = SVGMapGetColorUtil.SKIP;
					} else if (args[i].toLowerCase().equals("counterstop")){
						s2i.outOfRangeView = SVGMapGetColorUtil.COUNTER_STOP;
					}
					
				} else if ( args[i].toLowerCase().indexOf("-threads")>=0){
					++i;
					if ( Integer.parseInt(args[i]) > 0 ){
						s2i.maxThreads = Integer.parseInt(args[i]);
					}
				} else if ( args[i].toLowerCase().indexOf("-csvschema")>=0){
					++i;
					s2i.csvSchemaPath = args[i];
					System.out.println("Schema Path for CSV file: " + s2i.csvSchemaPath);
				} else if ( args[i].toLowerCase().indexOf("-charset")>=0){
					++i;
					s2i.charset = args[i];
				} else if ( args[i].toLowerCase().indexOf("-webmercatortile")>=0){
					s2i.webMercatorTile = true;
				} else {
					System.out.println("\n\nError :  There is no option named " + args[i]);
					System.out.println("To display help, please launch Shape2ImageSVGMap without any options.\n");
					System.exit(0);
				}
				++i;
			}
		} catch ( Exception e ){
			System.out.println("\n\nError: Invalid Option : " + args[i]);
			System.out.println("To display help, please launch Shape2ImageSVGMap without any options.\n");
			e.printStackTrace();
			System.exit(0);
		}
		
		int layerCount=0;
		int i2 = i;
		while(i2 < args.length){
			i2+=5;
			++ layerCount;
		}
		
		s2i.setLayerCounts(layerCount);
		
		if ( ( args.length - i ) % 5 != 0 ){
			System.out.println("\n\nErrpr: Invalid layer parameters. For each layer you have to set five parameters as below.");
			System.out.println(" Path4Shape.(shp|csv) (#fillColor | attrNumb) (#strokeColor | -) strokeWidth (symbolSize | sizedMarkerShapeName)");
			System.out.println("");
			System.out.println("To display help, please launch Shape2ImageSVGMap without any options.\n");
			System.exit(0);
		}
		
		while(i < args.length ){
//			try{
				// int markerSize = Integer.parseInt(args[i+4]);
				//            sourve  , fillClr   , strokeClr , strokeWid , symbolSize
				s2i.loadLayer(args[i] , args[i+1] , args[i+2] , args[i+3] , args[i+4]);
				i += 5;
//			} catch (Exception e){
//				s2i.loadLayer(args[i] , args[i+1] , args[i+2] , args[i+3] , "0");
//				i += 4;
//			}
			++ layerCount;
		}
		
		s2i.buildSvgMapExecutorService();
		
		if ( complementalSvg ){ // shp2svgmap??で作成したベクタSVGMapを補完する小縮尺表示用のimagesvgmapを作るモード
			s2i.customTile = true;
			int[] bp = sr.getBasePart();
			Rectangle2D.Double gb = sr.getGeoBounds();
			HashSet<Long>[] rt = sr.getRequiredTiles();
			double[] tz = sr.getTileZoom();
			s2i.customArea = sr.getGeoBounds();
			s2i.buildComplementalMap( rt , tz , bp );
		} else if ( s2i.lvl == -1 &&  startLvl != endLvl ){ // スタートからエンドのタイルレベルを指定した階層的タイリング
			s2i.buildHierarchicalMap( startLvl , endLvl );
		} else if ( s2i.singleImage != true && s2i.lvl == -1 ){ // これは矛盾がありエラーアウトのさせる
			System.out.println("You should set tile level");
		} else { // さもなければ、一枚モノのデータを作るか、指定したレベルだけのタイリングされたsvgimagemapを作る
			s2i.buildImage();
		}
		
		s2i.shutdownSvgMapExecuterService();
	}
	
	void buildSvgMapExecutorService(){
		svgMapExecutorService = Executors.newFixedThreadPool(maxThreads);
	}
	
	void shutdownSvgMapExecuterService(){
		svgMapExecutorService.shutdown();
	}
	
	SvgMap getHierarchicalContainer() throws Exception{
		String containerFileName = getOutDir(true, 0, 0 )+"rootImgMap.svg";
		
		NumberFormat nFmt = NumberFormat.getNumberInstance();
		nFmt.setGroupingUsed(false);
		nFmt.setMaximumFractionDigits(6);
		
		System.out.println("Build HierarchicalMap Container:" + containerFileName );
		SvgMap smcr = new SvgMap ( containerFileName , nFmt);
		
		ReferencedEnvelope bounds = sfcArray[0].getBounds();
		double lngMin  = bounds.getMinX();
		double latMin  = bounds.getMinY(); 
		double lngSpan = bounds.getWidth();
		double latSpan = bounds.getHeight();
		smcr.putHeader( (lngMin + 0.5 * lngSpan * ( 1 - viewBuffer )) * tcmul , - (latMin + 0.5 * latSpan * ( 1 + viewBuffer) ) * tcmul  , viewBuffer * lngSpan * tcmul , viewBuffer * latSpan * tcmul );
		smcr.putCrs( tcmul , 0 , 0 , -tcmul , 0 , 0 );
		return ( smcr );
	}
	
	double getZoomTh( double tileDeg){
		return ( 100.0 * (double)imageWidth / ( tileDeg * tcmul ));
	}
	
	String getsubClink( String fpath ){
		String ans = fpath.substring( 1 + fpath.lastIndexOf(File.separator, fpath.lastIndexOf(File.separator) - 1));
		ans = ans.replace(File.separator,"/");
		return( ans );
	}
	
	void buildComplementalMap( HashSet<Long>[] rt , double[] tz , int[] bp ) throws Exception{
		SvgMap smcr = getHierarchicalContainer();
		
		for ( int lv = 0 ; lv < rt.length ; lv ++ ){
			reqTile = rt[lv];
			customPart = bp;
			System.out.println("\n=====================");
			String spath = buildImage();
			System.out.println("\nConvert partDeg:"+ partDeg);
//			double zth = getZoomTh(getPartDeg(lvl));
			double zth = getZoomTh(partDeg);
			if ( tz != null && tz[lv] >0 ){
				zth = tz[lv];
			} else {
				System.out.println("WARN: There are no zoomRange in vector svgMap container....");
			}
			double minz = zth / 2.0;
			double maxz = zth;
			if ( lv == 0 ){
				minz = -1;
			} else if( lv == rt.length-1 ){
//				maxz = -1;
			}
			smcr.putImage(new Coordinate( -30000 , -30000 ) , 60000 , 60000 , getsubClink(spath) ,minz , maxz );
			bp[0] = bp[0] * 2;
			bp[1] = bp[1] * 2;
			usePrevBounds = true;
		}
		smcr.putFooter();
	}
	
	void buildHierarchicalMap( int startLvl , int endLvl ) throws Exception{
		
		SvgMap smcr = getHierarchicalContainer();
		
		for ( int lv = startLvl ; lv <= endLvl ; lv++ ){
			lvl = lv;
			partDeg = -1;
			System.out.println("\n=====================");
			System.out.println("Convert level:" + lvl + " partDeg:"+ getPartInv(lvl)*360.0);
			double zth = getZoomTh(getPartInv(lvl)*360.0);
			
	//				if ( fixArea == true && lv != startLvl ){}
			if ( lv != startLvl ){
				usePrevBounds = true; // 地図の生成領域を一番上のレベルのもので固定する(下のレベルになれば、もっとタイトなボックスになるのだが、shp2svgmap整合を目的に、それを抑制するため)
			} else {
				usePrevBounds = false;
			}
			
			String spath = buildImage();
			double minz = zth / 2.0;
			double maxz = zth;
			if ( lv == startLvl ){
				minz = -1;
			} else if( lv == endLvl ){
				maxz = -1;
			}
			smcr.putImage(new Coordinate( -30000 , -30000 ) , 60000 , 60000 , getsubClink(spath) , minz , maxz );
		}
		smcr.putFooter();
	}
	
	
	SimpleFeatureCollection[] sfcArray;
	File[] inputFileArray;
	
	Style[] styleArray;
	double partDeg = -1;
	int lvl = -1;
	
	void setLayerCounts( int layers ){
		System.out.println("Layers count:"+layers);
		sfcArray = new SimpleFeatureCollection[layers];
		inputFileArray = new File[layers];
		styleArray = new Style[layers];
	}
	
	int layerCount = 0;
	public void loadLayer( String filePath , String fillColorS , String strokeColorS , String strokeWidthS , String markerSizeS ) throws Exception {
		
		System.out.println("call loadLayer::::::: fillColor:"+fillColorS+"  strokeColorS:"+strokeColorS+"  strokeWidthS:"+strokeWidthS+"  markerSizeS:"+markerSizeS);
		
		int attrColorNumber = -1;
		double fMin = -9e99;
		double fMax = 9e99;
		
		Color fillColor = null;
		if ( fillColorS.indexOf("#") == 0 ){
			fillColor = getColor(fillColorS);
		} else if (fillColorS.indexOf("R") < 0 ){
			attrColorNumber = Integer.parseInt(fillColorS);
		} else { // num range set
			attrColorNumber = Integer.parseInt(fillColorS.substring(0,fillColorS.indexOf("R")));
			fMin = Double.parseDouble(fillColorS.substring(fillColorS.indexOf("R")+1,fillColorS.indexOf(",")));
			fMax = Double.parseDouble(fillColorS.substring(fillColorS.indexOf(",")+1));
			if ( fMin > fMax ){
				double ft = fMin;
				fMin = fMax;
				fMax = ft;
			}
			System.out.println("Force numerical range : "+ fMin + " ... " + fMax );
		}
		Color strokeColor = getColor(strokeColorS);
		boolean dash = false;
		
		int strokeWidth = 0;
		if ( strokeWidthS.indexOf("none") != 0 ){
			if ( strokeWidthS.indexOf("dash") == -1 ){
				strokeWidth = Integer.parseInt(strokeWidthS);
			} else {
				strokeWidthS = strokeWidthS.replace("dash","");
				strokeWidth = Integer.parseInt(strokeWidthS);
				dash = true;
			}
			if (strokeWidth < 0){
				strokeWidth = 0;
			}
		}
		
		markerSizeS = markerSizeS.toLowerCase();
		String markerName = "circle";
		int markerSize = 3;
		if ( markerSizeS.indexOf(":")>0){
			String[] markerSizeSarray=markerSizeS.split(":");
			markerName = markerSizeSarray[0];
			if ( markerSizeSarray.length < 2 ){
				System.out.println("ERROR! sizedMarkerShapeName invalid : " + markerSizeS );
				System.exit(0);
			}
			if ( markerName.equals("cross") || markerName.equals("circle") || markerName.equals("triangle") || markerName.equals("x") || markerName.equals("star") || markerName.equals("arrow") || markerName.equals("hatch") || markerName.equals("square")){
				markerSize =  Integer.parseInt( markerSizeSarray[1] );
			} else if ( markerName.equals("mesh")  ){
				if ( markerSizeSarray.length < 3 ){
					System.out.println("ERROR! sizedMarkerShapeName invalid : " + markerSizeS );
					System.exit(0);
				}
				double aspect = Double.parseDouble(markerSizeSarray[2])/Double.parseDouble(markerSizeSarray[1]);
				markerName = "meshrect:"+(double)((int)(aspect*100))/100;
				if ( aspect < 1 ){ // 縦の長さがsizeのようですので・・
					markerSize = (int)(10 * aspect);
				} else {
					markerSize = 10;
				}
				meshMarkerGeoHeight = Double.parseDouble(markerSizeSarray[2]);
			} else {
				System.out.println("ERROR! sizedMarkerShapeName invalid : " + markerSizeS );
				System.exit(0);
			}
			
		} else {
			markerSize = Integer.parseInt( markerSizeS );
		}
		
		System.out.println("markerName:"+markerName+"  markerSize:"+markerSize);
		
	  	File file = new File(filePath);
		
		if ( filePath.endsWith(".csv")){
			inputCsv = true;
			System.out.println("CSV input");
		} else if ( filePath.endsWith(".gz")){
			inputCsv = true;
			gZipped = true;
			System.out.println("gz_CSV input");
		} else {
			System.out.println("Shapefile input");
		}
		
//		ContentDataStore readStore = null;
		SimpleFeatureSource fs = null;
		CSVDataStore cds =null;
		ShapefileDataStore sds = null;
		if ( inputCsv ){
			// CSVファイルを読み込む 
			cds =null;
			if ( csvSchemaPath =="" ){
				cds = new CSVDataStore( new File(filePath), gZipped, charset );
			} else {
				cds = new CSVDataStore( new File(filePath), new File(csvSchemaPath), gZipped, charset );
			}
			cds.sjisInternalCharset = false; // 2018.8.10 shapefileでも文字コードを正しく判別して読めることが分かったので・・・
			fs = cds.getFeatureSource(cds.getNames().get(0));
		} else {
			URI uri = file.toURI();
			URL url = uri.toURL();
		  	System.out.println("URL:"+url);
//		    sds = new ShapefileDataStore(url); 
		    sds = new ShapefileDataStore(url, true , Charset.forName("Windows-31j")); // 2018.8.10 debug   shapefileでもcharsetを指定すれば文字化けしないことが判明　これをしないと、FilterFactoryのpropertyが正しく処理されないため・・
		    fs = sds.getFeatureSource();
		}
		
	    SimpleFeatureCollection sfc = fs.getFeatures();
	  	
	  	System.out.println("FeatureCol. Bounds:" + sfc.getBounds() + " Size:" + sfc.size());
		
		
//	  	System.out.println("FeatureCol. Bounds:" + sfc.getBounds() );
		sfcArray[layerCount] = sfc;
		inputFileArray[layerCount] = file;
		
		Style style;
		if ( attrColorNumber < 0 ){
			style = getFeatureStyle( fillColor , strokeColor , strokeWidth , dash , markerSize , fs.getSchema(), markerName);
		} else {
//			style = getFeatureStyle2( 2 , false , 3 , fs );
			style = getFeatureStyle2( attrColorNumber , strokeColor , strokeWidth ,  dash , markerSize , fs , fMin , fMax , markerName);
		}
		styleArray[layerCount] = style;
		
		++layerCount;
	}
	
	public Color getColor(String webColor){
		Color ans = null;
		if ( webColor.indexOf("#") == 0 ){
			int colorNum = Integer.parseInt( webColor.substring(1), 16);
			ans = new Color(colorNum);
		}
		return ( ans );
	}
	
	String getOutDir(boolean singleImage, int lvl, double partDeg ){
		String outDir ="";
		String outName = "";
		if ( complementalSourceSvg != null ){
			outDir = complementalSourceSvg.getParent()+ File.separator;
			outName = complementalSourceSvg.getName().substring(0,complementalSourceSvg.getName().lastIndexOf("."));
		} else if ( inputFileArray[0].getParent() != null ){
			outDir = inputFileArray[0].getParent()+ File.separator;
			outName = inputFileArray[0].getName().substring(0,inputFileArray[0].getName().lastIndexOf("."));
		}
		
		
		
		if ( !singleImage ){
			if ( partDeg >0){
				lvl = getLvl(partDeg);
			} else {
				partDeg = getPartInv(lvl)*360.0;
			}
			
			System.out.println("tileLevel:" + lvl + "  tileWidth:" + partDeg );
			
			outDir += outName + File.separator + "lvl" + lvl + File.separator;
		} else {
			outDir += outName + File.separator ;
		}
		
		
		File outDirF = new File (outDir);
		if ( !outDirF.exists() ){
			outDirF.mkdirs();
		}
		
		System.out.println("outDir:" +outDir );
		return ( outDir );
	}
	
	boolean rebuildContainerOnly = false; // 階層コンテナに再作成のため、ビットイメージは作らない(多分一度しか使わないと思いマス)
	int containerCounts = 16; // 階層コンテナを作るかどうかの閾値(この値の２乗のタイル数をリミットにする)
	boolean singleImage = false;
	
	boolean customTile = false;
	Rectangle2D.Double customArea;
	int[] customPart;
	
	String containerName = "container.svg";
	double viewBuffer = 1.0;
	
	boolean antiAlias = false;
	
	int sumUp = 1; // タイル一括描画数（タイル数はこの値の二乗）
	
	double tcmul = 100.0; // Geo->SVG変換係数(matrixのa,-d)(全タイル共通・・)
	
	
	// 処理に必要な８つのパラメータ
	int imageWidth = 256; //タイルのサイズ
	int imageHeight = 256;
	double mapXstart, mapYstart;
	double mapXstep, mapYstep;
	int GridXsize, GridYsize;
	
	double prevMapXstart, prevMapYstart, prevMapXend, prevMapYend; // shape2svgmap18との互換をとるため
	HashSet<Long> prevTileExistence;
	HashSet<Long> thisTileExistence;
	
	// 生成すべきタイルリストのインデックスを外部から与える
	HashSet<Long> reqTile = null;
	
	int keyLength = 2; // added 2016.4.1 for -strcolor
	
	boolean usePrevBounds = false;
	
	// 事前に決めなければならない８パラメータを設定する、３種類の方法
	// int imageWidth, imageHeight;
	// double mapXstep, mapYstep;
	// double mapXstart, mapYstart;
	// int GridXsize, GridYsize;
	void setSingleBuildConditions( Rectangle2D.Double bounds ){
		imageWidth = imageWidth;
		imageHeight = (int)(imageWidth * bounds.height / bounds.width);
		mapXstep = bounds.width;
		mapYstep = bounds.height;
		mapXstart = bounds.x;
		mapYstart = bounds.y;
		GridXsize = 1;
		GridYsize = 1;
	}
	
	void setGlobalTileBuildConditions( Rectangle2D.Double mapBounds , int level , Rectangle2D.Double mapRange){
		imageWidth = imageWidth;
		imageHeight = imageHeight;
		
		double partDeg = getPartInv(level) * mapRange.width; // plateCareeもwebMercatorも基本的には正方形なのでw/h区別ない
		mapXstep = partDeg;
		mapYstep = partDeg;
		mapXstart = (int)((mapBounds.x - mapRange.x) / mapXstep ) * mapXstep + mapRange.x;
		mapYstart = (int)((mapBounds.y - mapRange.y) / mapYstep ) * mapYstep + mapRange.y;
		GridXsize = (int)Math.ceil( ((mapBounds.x + mapBounds.width)-mapXstart)/mapXstep);
		GridYsize = (int)Math.ceil( ((mapBounds.y + mapBounds.height)-mapYstart)/mapYstep);
	}
	
	Rectangle2D.Double getProjectionRange(CoordinateReferenceSystem crs, CoordinateReferenceSystem targetCRS) throws Exception{
		Rectangle2D.Double ans = null;
		if ( webMercatorTile ){
			Envelope env = new Envelope(-180,180-1e-13, -85.05113, 85.05113); // -1e-13は、transformが値域上限で破綻するため (geoToosの挙動)
			ReferencedEnvelope projenv = new ReferencedEnvelope(env, crs).transform(targetCRS, true);
			ans = new Rectangle2D.Double(projenv.getMinX(), projenv.getMinY(), projenv.getWidth(), projenv.getHeight());
		} else { // PlateCareeは、ローカルルールとして、北緯南緯も+-180(原理としては+-90だが)を設定し、正方形になるようにしている
			ans = new Rectangle2D.Double(-180,-180,360,360);
		}
//		System.out.println("getProjectionRange:"+ans);
		return ( ans );
	}
	
	ReferencedEnvelope getProjectionRangeEnvelope(CoordinateReferenceSystem crs, CoordinateReferenceSystem targetCRS) throws Exception{
		// 上と似すぎてるので、上を下に統合するかも。ただ、PlateCareeの時の1e-13が気になっている
		ReferencedEnvelope env = null;
		if ( webMercatorTile ){
			env = new ReferencedEnvelope(-180,180-1e-13, -85.05113, 85.05113, crs); // -1e-13は、transformが値域上限で破綻するため (geoToosの挙動)
		} else {
			env = new ReferencedEnvelope(-180,180-1e-13, -90, 90, crs); // 多分同上
		}
		ReferencedEnvelope ans = env.transform(targetCRS, true);
		return ( ans );
	}
	
	void setCustomTileBuildConditions( Rectangle2D.Double bounds , int xPart , int yPart ){
		imageWidth = imageWidth;
		imageHeight = (int)(imageWidth * (bounds.height / yPart) / (bounds.width / xPart));
		mapXstep = bounds.width / xPart;
		mapYstep = bounds.height / yPart;
		mapXstart =  bounds.x;
		mapYstart = bounds.y;
		GridXsize = xPart;
		GridYsize = yPart;
	}
	
	public Long getReqTileKey( int tx , int ty ){
		// 注意 yがひっくり返っていますよ！
		return (new Long((long)((long)tx * (long)100000000 + (long)(GridYsize-ty-1))));
	}
	
	public String buildImage() throws Exception {
		
		
		// mapContextを作成
		CoordinateReferenceSystem crs = 
		sfcArray[0].getSchema().getGeometryDescriptor().getCoordinateReferenceSystem();
//		sfcArray[0].getSchema().getCoordinateReferenceSystem();
//		crs = CRS.decode("EPSG:3785");
		MapLayer layers[] = {};
		
    	CoordinateReferenceSystem targetCRS=null;
		if ( webMercatorTile ){
//			targetCRS = crsFactory.createFromWKT("PROJCS[\"Google Mercator\",   GEOGCS[\"WGS 84\",     DATUM[\"World Geodetic System 1984\",       SPHEROID[\"WGS 84\", 6378137.0, 298.257223563, AUTHORITY[\"EPSG\",\"7030\"]],       AUTHORITY[\"EPSG\",\"6326\"]],     PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]],     UNIT[\"degree\", 0.017453292519943295],     AXIS[\"Geodetic latitude\", NORTH],     AXIS[\"Geodetic longitude\", EAST],     AUTHORITY[\"EPSG\",\"4326\"]],   PROJECTION[\"Mercator (1SP)\", AUTHORITY[\"EPSG\",\"9804\"]],   PARAMETER[\"semi_major\", 6378137.0],   PARAMETER[\"semi_minor\", 6378137.0],   PARAMETER[\"latitude_of_origin\", 0.0],   PARAMETER[\"central_meridian\", 0.0],   PARAMETER[\"scale_factor\", 1.0],   PARAMETER[\"false_easting\", 0.0],   PARAMETER[\"false_northing\", 0.0],   UNIT[\"m\", 1.0],   AXIS[\"Easting\", EAST],   AXIS[\"Northing\", NORTH],   AUTHORITY[\"EPSG\",\"900913\"]]");
//    		targetCRS = CRS.decode("EPSG:3785");
	    	targetCRS = CRS.decode("EPSG:900913",true);
		} else {
	    	targetCRS = crs;
		}
		
		
		DefaultMapContext map = new DefaultMapContext(layers, targetCRS);
		
		for ( int i = 0 ; i < layerCount ; i++ ){
			System.out.println("Add Layer:" + i );
			// レイヤを追加する(これがmapContextというものに、実際のshapeデータを括り付けている)
			map.addLayer(new FeatureLayer(sfcArray[i], styleArray[i]));
		}
		
		/**
		CoordinateReferenceSystem worldCRS = map.getCoordinateReferenceSystem();
		boolean lenient = true; // allow for some error due to different datums
		MathTransform transform = CRS.findMathTransform( crs, worldCRS, lenient );
		**/
		
		// レンダラを準備する　shapefileから生成される地理情報ストリームを入力してそれによって描画するレンダラという意味だと思われる
		/**
		StreamingRenderer renderer = new StreamingRenderer();
		if ( antiAlias ){
			// アンチエリアス関係のヒントを付ける
			RenderingHints rh=new RenderingHints(
				RenderingHints.KEY_ALPHA_INTERPOLATION,
				RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
			rh.add(new RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON));
			rh.add(new RenderingHints(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
			renderer.setJava2DHints(rh);		
		}
		renderer.setContext(map); // これでmapContextを介してshapefile群をストリーミングレンダラに括りつけている
		**/
		
		ReferencedEnvelope mapBounds = map.getMaxBounds(); // 地図(絵)の座標系(mercatorでは地理座標系と異なる)
//		System.out.println("\n\mapBounds:"+mapBounds+"  : "+mapBounds.getMinX()+","+mapBounds.getMinY()+","+mapBounds.getMaxX()+","+mapBounds.getMaxY()+"\n\n");
		if ( mapBounds.getMinX()==0 && mapBounds.getMaxX()==-1 ){
			mapBounds = sfcArray[0].getBounds();
			if ( webMercatorTile ){ // 2020/4/8
				mapBounds = new ReferencedEnvelope(mapBounds, crs).transform(targetCRS, true);
			}
			System.out.println("Hmmm can't get mapBounds.. fixIt by layer0 : " + mapBounds);
		}
		
		
		// 事前に決めなければならない８パラメータを設定する
		// int imageWidth, imageHeight;
		// double mapXstep, mapYstep;
		// double mapXstart, mapYstart;
		// int GridXsize, GridYsize;
		if ( singleImage ){
			setSingleBuildConditions( new Rectangle2D.Double( mapBounds.getMinX(), mapBounds.getMinY(), mapBounds.getWidth(), mapBounds.getHeight() ) );
		} else if ( customTile ){
			setCustomTileBuildConditions( customArea , customPart[0] , customPart[1] );
			partDeg = mapXstep; // とりあえずテスト用・・・
		} else {
			if ( lvl == -1 && partDeg > 0 ){
				lvl = getLvl(partDeg);
			}
			setGlobalTileBuildConditions( new Rectangle2D.Double( mapBounds.getMinX(), mapBounds.getMinY(), mapBounds.getWidth(), mapBounds.getHeight() ) , lvl , getProjectionRange(crs, targetCRS));
		}
		
//		System.out.println("imageWidth:" + imageWidth + " imageHeight:" + imageHeight + " mapXstep:" + mapXstep + " mapYstep:" + mapYstep + " mapXstart:" + mapXstart + " mapYstart:" + mapYstart + " GridXsize:" + GridXsize + " GridYsize:" + GridYsize + "   mapBounds:"+mapBounds);
		
		for ( int i = 0 ; i < styleArray.length; i++ ){
			if ( meshMarkerGeoHeight > 0 ){
				int mkSize = (int)(meshMarkerGeoHeight * imageHeight / mapYstep); // TBDメルカトルでは標準緯線で考えるか・・
				if ( mkSize == 0 ){
					mkSize = 1;
				}
				setMarkerSize(styleArray[i] , mkSize );
//				System.out.println("MESH SIZE: geo:"+meshMarkerGeoHeight+"    pix:"+ mkSize);
//				System.out.println("MESH SIZE: imageHeight:"+imageHeight+"    mapYstep:"+ mapYstep);
				
			}
		}
		
		prevMapXstart = mapXstart;
		prevMapYstart = mapYstart;
		prevMapXend = mapXstart + GridXsize * mapXstep;
		prevMapYend = mapYstart + GridYsize * mapYstep;
		
		if ( rebuildContainerOnly ){
			sumUp = 1; // コンテナ生成のみの場合は一括生成は抑制
		}
		
		
//		System.out.println("Rect:"+imageWidth * sumUp + "," + imageHeight * sumUp);
		Rectangle imageRect = new Rectangle(0, 0, imageWidth * sumUp , imageHeight * sumUp );
		
		if ( usePrevBounds ){
			mapXstart = prevMapXstart;
			mapYstart = prevMapYstart;
			GridXsize = (int)Math.round( ( prevMapXend - prevMapXstart ) / mapXstep);
			GridYsize = (int)Math.round( ( prevMapYend - prevMapYstart ) / mapYstep);
		}
		
		int totalGrid = GridXsize * GridYsize;
		
		String outDir = getOutDir( singleImage, lvl, partDeg ); // 出力先を生成しているだけでなく、partDegとか肝心な係数も作っているぞ・・・・(これを是正中)
		
		if ( checkJp ){
			// 日本領域チェッカーを読み込み
			jpt = new jpLandTester();
		}
		
//		double mapXstep=0;
//		double mapYstep=0;
		
//		System.out.println("image Rect:" + imageRect + "  \ndata Bounds: minX:" + bounds.getMinX() + " minY:" + bounds.getMinY() + " width:" + bounds.getWidth() + " height:" + bounds.getHeight() + " \nraw:" + bounds );
		
		ReferencedEnvelope geoBounds = mapBounds;
		if ( webMercatorTile ){
			geoBounds = mapBounds.transform(crs, true);
			System.out.println("webMercatorTile geoBounds:"+geoBounds);
		}
		double lngMin  = geoBounds.getMinX();
		double latMin  = geoBounds.getMinY(); 
		double lngSpan = geoBounds.getWidth();
		double latSpan = geoBounds.getHeight();
//		System.out.println("lngMin:"+lngMin+" latMin:"+latMin+" lngSpan:"+lngSpan+" latSpan:"+latSpan);
		
		String containerFileName = outDir + containerName;
		
		NumberFormat nFmt = NumberFormat.getNumberInstance();
		nFmt.setGroupingUsed(false);
		nFmt.setMaximumFractionDigits(6);
		SvgMap sm = new SvgMap ( containerFileName , nFmt);
		sm.putHeader( (lngMin + 0.5 * lngSpan * ( 1 - viewBuffer )) * tcmul , - (latMin + 0.5 * latSpan * ( 1 + viewBuffer) ) * tcmul  , viewBuffer * lngSpan * tcmul , viewBuffer * latSpan * tcmul );
		sm.putCrs( tcmul , 0 , 0 , -tcmul , 0 , 0 );
		
		if ( includeBaseMap ){
			sm.putImage(
				new Coordinate( -30000 , -30000 ) ,
				60000 , 60000 ,
				"http://svg2.mbsrv.net/extmaps/Bing/ContainerBing.svg"
			);
		}
		sm.setGroup();
		
		
		int counter = 0;
//		int GridXsize, GridYsize;
		
		
		HashMap<Long, SvgMap> smsMap = null;
		boolean hasSmsMap = false;
		
		//containerCounts^2（static）より総グリッド数が多い場合は、２階層コンテナとする
		if ( totalGrid > containerCounts * containerCounts ){
			smsMap   = buildSmsMap( GridXsize , GridYsize );
			hasSmsMap = true;
		}
		
		int smX , smY;
		
		double geoX , geoY;
		double mapX , mapY;
		
//		System.out.println("GridXsize,sumUp,ceil:" + GridXsize + "," + sumUp + "," + (int)Math.ceil( (double)GridXsize / sumUp ) );
		
		
		// add 2014.2.20 for speedup (1)
		thisTileExistence = new HashSet<Long>();
		if ( prevTileExistence == null ){ // 最初のレベルのprevTileExistenceを全部セットする
			prevTileExistence = new HashSet<Long>();
			for ( int i = 0 ; i < (int)Math.ceil( (double)GridXsize / sumUp ) ; i++ ){
				for ( int j = 0 ; j < (int)Math.ceil( (double)GridYsize / sumUp ) ; j++ ){
					prevTileExistence.add(getHashKey( i , j ));
				}
			}
		}
		
		String prg="";
		
		System.out.println("tileSize x:" + GridXsize + " y:" + GridYsize + "  sumUpTiles x:" +(int)Math.ceil((double)GridXsize/(double)sumUp) + " y:" +  (int)Math.ceil((double)GridYsize/(double)sumUp) + " total:" + ((int)Math.ceil((double)GridXsize/(double)sumUp)*(int)Math.ceil((double)GridYsize/(double)sumUp)) + "  effectiveSumUpTiles:"+ prevTileExistence.size());
		
		Iterator pti = prevTileExistence.iterator(); // prevTileExistenceを用いたハッシュループに代替 2014/2/27
		
		int skipCounter = 0;
		
		ArrayList<int[]> tilePosList = new ArrayList<int[]>();
//		ArrayList<TileRenderRunnable> tileRenderRunnables= new ArrayList<TileRenderRunnable>();
		
		while( pti.hasNext() ){
			int[] existPrevSumUpTile =getIndex((Long)pti.next());
			int tx = existPrevSumUpTile[0];
			int ty = existPrevSumUpTile[1];
//		// この2重fornextループはHashSet(prevTileExistence)ループに代えるべき(ToDo 2014/2/24)　⇒処理済
			mapX = tx * mapXstep * sumUp + mapXstart;
			mapY = ty * mapYstep * sumUp + mapYstart;
			
			if ( reqTile != null ){
				// reqTile(svgコンテナから与えられた生成すべきタイル)がある場合、それとと合致したブロックのみ処理
				boolean containsReqTile = false;
				cnrt: for ( int sx = 0 ; sx < sumUp ; sx++ ){
					for ( int sy = 0 ; sy < sumUp ; sy++ ){
						if ( reqTile.contains(getReqTileKey( tx * sumUp + sx, ty * sumUp + sy ) ) ){
							containsReqTile = true;
							break cnrt;
						}
					}
				}
				if ( ! containsReqTile ){
					if ( skipCounter <= 10 ){
						System.out.print("s"); // skip image building
					} else if ( skipCounter <= 100000 &&  skipCounter % 1000 == 0 ){
						System.out.print("S");
					} else if ( skipCounter <= 10000000 &&  skipCounter % 100000 == 0 ){
						System.out.print("S5");
					}
					++skipCounter;
					continue;
				} else {
					skipCounter = 0;
				}
			}
			
//				System.out.println("tx,ty:"+tx+","+ty);
			
			counter += sumUp * sumUp;
			ReferencedEnvelope mapSumUpTileEnvelope = new ReferencedEnvelope(mapX , mapX + (mapXstep * sumUp) , mapY , mapY + (mapYstep * sumUp), targetCRS);
			mapSumUpTileEnvelope = new ReferencedEnvelope(mapSumUpTileEnvelope.intersection(getProjectionRangeEnvelope(crs,targetCRS)),targetCRS); // クリップする
			ReferencedEnvelope geoSumUpTileEnvelope = mapSumUpTileEnvelope.transform(crs,true);
//			System.out.println("mapSumUpTileEnvelope:"+mapSumUpTileEnvelope+" geoSumUpTileEnvelope:"+geoSumUpTileEnvelope);
			geoX = geoSumUpTileEnvelope.getMinX();
			geoY = geoSumUpTileEnvelope.getMinY();
			/**
			if ( false && (geoX * geoY + geoY) % 5 == 0){ // やめました・・・
				
				String del="";
				for ( int pct = 0 ; pct < prg.length() ; pct++ ){
					del += "\b";
				}
				System.out.print(del);
				prg = nFmt.format(100.0 * (double)counter / (double)totalGrid) +"%       ";
				System.out.print(prg);
				
//					System.out.print( "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b");
//					System.out.print( nFmt.format(100.0 * (double)counter / (double)totalGrid) +"%      ");
			}
			**/
			if ( checkJp && ! jpt.hasMap( geoSumUpTileEnvelope.getMaxY() , geoSumUpTileEnvelope.getMinY() , geoSumUpTileEnvelope.getMaxX() , geoSumUpTileEnvelope.getMinX() ) ){
				// 日本域チェックOnで、日本域外ならループ飛ばす
//					System.out.println("out of ja");
				System.out.print("s");
				continue;
			}
			
//				System.out.println("*******intersec:" + geoX+","+(geoX + (mapXstep * sumUp))+","+ geoY+","+(geoY + (mapYstep * sumUp))+","+geoBounds);
			
			if ( ! geoBounds.intersects( (Envelope)geoSumUpTileEnvelope)){
				// 描画領域にフィーチャー(shapefile全体のbbox)がない場合も飛ばす
//					System.out.println("no feature in bbox");
				continue;
			}
			
//				if ( ! prevTileExistence.contains(getHashKey( tx , ty ) ) ){
//					// ひとつ前の段階のタイルに図形が存在してなければ飛ばす
//					System.out.print("s");
//					continue;
//				}
			System.out.print(".");
			
			String tileName;
				
			// 背景を透明にするためARGBを使う
//			BufferedImage image = new BufferedImage( imageWidth * sumUp , imageHeight * sumUp , BufferedImage.TYPE_INT_ARGB );
			
//			ReferencedEnvelope geoRect = new ReferencedEnvelope(geoX , geoX + (mapXstep * sumUp) , geoY , geoY + (mapYstep * sumUp) , crs);
			
			if ( rebuildContainerOnly ){
				// レンダリングは行なわない・・ sumUp != 1ではバグるのでは？(2013.3.28)
				tileName = "tile" + tx + "_" + ty + ".png";
				File imageFile = new File(outDir + tileName);
				if ( ! imageFile.exists()){
					continue;
				}
			} else {
				int[] txy = { tx, ty};
				tilePosList.add(  txy );
				// 描画するためのスレッドを準備する
//				System.out.println("build runnable");
//				TileRenderRunnable trr = new TileRenderRunnable( antiAlias, map, image, imageRect, geoRect, tx, ty );
//				tileRenderRunnables.add( trr );
			}
		}
		
		System.out.println("\n Build images: "+ tilePosList.size());
		
//		ArrayList<Thread> tileRenderThreads;
		ArrayList<TileRenderRunnable> tileRenderRunnables;
		
		int threadBlockIndex = 0;
		while ( threadBlockIndex < tilePosList.size() ){
//			tileRenderThreads = new ArrayList<Thread>();
			tileRenderRunnables = new ArrayList<TileRenderRunnable>();
			
			ArrayList<Future<?>> futureList = new ArrayList<Future<?>>();
			
			for ( int threadCount = threadBlockIndex ; ((threadCount - threadBlockIndex) < maxThreads) && (threadCount < tilePosList.size()) ; threadCount++ ){
				int[] txy = tilePosList.get(threadCount);
				int tx = txy[0];
				int ty = txy[1];
				mapX = tx * mapXstep * sumUp + mapXstart;
				mapY = ty * mapYstep * sumUp + mapYstart;
				
				// 描画用イメージを用意する：背景を透明にするためARGBを使う
				BufferedImage image = new BufferedImage( imageWidth * sumUp , imageHeight * sumUp , BufferedImage.TYPE_INT_ARGB );
				// 描画地理座標領域を生成する
				ReferencedEnvelope mapRect = new ReferencedEnvelope(mapX , mapX + (mapXstep * sumUp) , mapY , mapY + (mapYstep * sumUp), targetCRS);
//				System.out.println("mapRect:"+mapRect);
//				ReferencedEnvelope geoRect = mapRect.transform(crs, true);
				
				
				//描画runnableを生成する
//				System.out.println("build runnable");
				TileRenderRunnable trr = new TileRenderRunnable( antiAlias, map, image, imageRect, mapRect, tx, ty, sumUp, reqTile, rebuildContainerOnly , outDir );
				tileRenderRunnables.add(trr);
				
				//ビットイメージ描画・タイル分割・保存スレッドを起動する
//				System.out.println("build thread");
				
				/**
				Thread th =  new Thread(trr);
				tileRenderThreads.add(th);
				th.start();
				**/
				
				Future<?> future = svgMapExecutorService.submit(trr);
				futureList.add(future);
				
//				(tileRenderThreads.get(threadCount - threadBlockIndex)).start();
			}
			
			// 描画スレッド完了の同期のための待機ループ
			/**
			for ( int threadCount = threadBlockIndex ; ((threadCount - threadBlockIndex) < maxThreads) && (threadCount < tilePosList.size()) ; threadCount++ ){
				(tileRenderThreads.get(threadCount - threadBlockIndex)).join();
				
			}
			**/
			for (Future<?> future : futureList) {
				future.get();
			}
			
			
			// コンテナSVG(場合によっては階層的)生成処理
			for ( int threadCount = threadBlockIndex ; ((threadCount - threadBlockIndex) < maxThreads) && (threadCount < tilePosList.size()) ; threadCount++ ){
				TileRenderRunnable trr = tileRenderRunnables.get(threadCount - threadBlockIndex);
				int tx = trr.tx;
				int ty = trr.ty;
				mapX = tx * mapXstep * sumUp + mapXstart;
				mapY = ty * mapYstep * sumUp + mapYstart;
				
				int[][] hasData = trr.hasData;
				boolean[][] isImage = trr.isImage;
				
				int divs = sumUp / 2;
				if ( sumUp == 1 ){ divs = 1; } // sumUp == 1の特殊処理
				for ( int subX = 0 ; subX < sumUp ; subX++ ){
					int ttx = tx * sumUp + subX;
					smX = ttx / containerCounts;
					for ( int subY = 0 ; subY < sumUp ; subY++ ){
						int tty = ty * sumUp + subY;
						smY = tty / containerCounts;
						SvgMap smp;
						if ( isImage[subX][subY] ){
							if (hasSmsMap ){
								if ( !smsMap.containsKey(getHashKey(smX,smY) )){
									smp = buildSms(smX , smY , smsMap , geoBounds , tcmul , nFmt , containerName , outDir );
								} else {
									smp = getSms(smX,smY,smsMap);
								}
							} else {
								smp = sm;
							}
							String tileName = "tile" + ttx + "_" + (GridYsize - tty -1) + ".png";
							ReferencedEnvelope mapRect = new ReferencedEnvelope(
								mapX + subX * mapXstep,
								mapX + subX * mapXstep + mapXstep,
								mapY + subY * mapYstep,
								mapY + subY * mapYstep + mapYstep,
								targetCRS
							);
							
							ReferencedEnvelope geoRect = mapRect.transform(crs, true);
//							System.out.println("putImage subX,subY:" + subX +","+subY+"  tileName:"+tileName+" mapRect:"+mapRect+" geoRect:"+geoRect);
							
							smp.putImage( 
								new Coordinate( geoRect.getMinX() * tcmul ,
									( - ( geoRect.getMaxY() ) * tcmul  ) ) ,
								geoRect.getWidth()*tcmul , geoRect.getHeight() * tcmul  , tileName );
						}
					}
				}
				
				if ( sumUp == 1 && hasData[0][0] > 0 ){ // sumUp==1の特殊処理
					hasData[0][0] = 1; hasData[0][1] = 1; hasData[1][0] = 1; hasData[1][1] = 1; 
				}
				for ( int i = 0 ; i < 2 ; i++){
					for ( int j = 0 ; j < 2 ; j++){
						if ( hasData[i][j] > 0 ){
							thisTileExistence.add(getHashKey( i + tx * 2, j + ty * 2 ));
						}
					}
				}
			}
			threadBlockIndex += maxThreads;
		}
		
		prevTileExistence = thisTileExistence;
		
		if ( hasSmsMap ){
			Iterator<Long> it = smsMap.keySet().iterator();
			while ( it.hasNext() ){
				Long skey = (Long)it.next();
				int[] smsix = getIndex(skey);
				int i = smsix[0];
				int j = smsix[1];
				mapY = mapYstart + j * containerCounts * mapYstep;
				mapX = mapXstart + i * containerCounts * mapXstep;
				SvgMap oneSms = smsMap.get(skey);
				oneSms.termGroup();
				oneSms.putFooter();
				String subCfilePath = "sub_" + i + "_" + j + "_" + containerName;
//					File mapFile = new File( outDir + subCfilePath );
//					if ( mapFile.exists()){}
				
				ReferencedEnvelope mapRect = new ReferencedEnvelope(
					mapX,
					mapX + mapXstep * containerCounts,
					mapY,
					mapY + mapYstep * containerCounts,
					targetCRS
				);
				
				ReferencedEnvelope geoRect = mapRect.transform(crs, true);
				sm.putImage( new Coordinate( geoRect.getMinX() * tcmul ,
					( - ( geoRect.getMaxY() ) * tcmul  ) ) ,
				geoRect.getWidth()*tcmul , geoRect.getHeight() * tcmul  , subCfilePath );
			}
		}
		sm.termGroup();
		sm.putFooter();
		
//		System.out.println("</g>");
//		System.out.println("</svg>");
		map.dispose();
		
		return (containerFileName);
	}
	
	
	
	// ポリゴンスタイルの生成関数
	// Colorは　new Color(47, 184, 27)　こんなかんじ
	public Style getFeatureStyle( Color fillColor , Color strokeColor , int strokeWidth , boolean dash , int markerSize , FeatureType schema , String markerName){
		//スタイルを作成
		// FilterFactory ff = CommonFactoryFinder.getFilterFactory2(null);
		StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
		
		Rule rules[] = getSymbolizeRules( fillColor , strokeColor , strokeWidth , dash , markerSize , schema , markerName);
		
/**			
		// add for checking
//		Filter filter = ff.equals( ff.property("Brand") , ff.literal("ENEOS") );
		Filter filter1 = ff.greater( ff.property("test3") , ff.literal(35.5) );
		Filter filter2 = ff.less( ff.property("test3") , ff.literal(38.5) );
		Filter filter = ff.and( filter1 , filter2 );
		rule.setFilter(filter);
		
		// test2
		Rule ruleOther = getOneSymbolizeRule( getColor("#A0A000") , getColor("#000000") , 1 , false , 3 , schema );
		ruleOther.setElseFilter(true);
			
		
		Rule rules[] = {rule , ruleOther};
**/
		FeatureTypeStyle fts = sf.createFeatureTypeStyle(rules); 
		Style style = sf.createStyle();
		style.featureTypeStyles().add(fts);
		return ( style );
	}
	
	
	// ポリゴンスタイルの生成関数２　値に応じて色を変化させる機能用
	// ISSUE and TBD: 混合ジオメトリタイプデータへの対応(2020/1/21)
	public Style getFeatureStyle2( int colorCol , Color strokeColor , int strokeWidth , boolean dash , int markerSize ,  SimpleFeatureSource fs , double fMin , double fMax , String markerName) throws Exception {
		System.out.println("Call getFeatureStyle2 : colorCol:" + colorCol );
		FeatureType schema = fs.getSchema();
		//スタイルを作成
		FilterFactory ff = CommonFactoryFinder.getFilterFactory2(null);
		StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
		
		SVGMapGetColorUtil colu;
		if ( colorKeys ==""){
//			if ( colorTable == SVGMapGetColorUtil.HSV ){
//				colu = new SVGMapGetColorUtil( fs.getFeatures() , colorCol  );
//			} else {
				colu = new SVGMapGetColorUtil( fs.getFeatures() , colorCol , -1 , colorTable , keyLength , true , "" );
//			}
			colu.getAttrExtent(true);
		} else { // add 2016.2.4 (-colorkey)
			colu = new SVGMapGetColorUtil( fs.getFeatures() , colorCol , -1 , SVGMapGetColorUtil.HSV , 128 , true , colorKeys );
		}
		
		colu.setOutOfRangeView( outOfRangeView );
		
		
		for ( int i = 0 ; i < ((SimpleFeatureType)schema).getAttributeCount() ; i++){
//			System.out.print(i+":" + colu.getKanjiProp(((SimpleFeatureType)schema).getDescriptor(i).getLocalName()));
			System.out.print(i+":" + (((SimpleFeatureType)schema).getDescriptor(i).getLocalName()));
			if ( i == colorCol ){
				System.out.println(" => Selected Attribute");
			} else {
				System.out.println("");
			}
		}
		
		System.out.println("Attribute is Number? : "+ colu.mainAttrIsNumber);
		
//		String attrName = colu.getKanjiProp(((SimpleFeatureType)schema).getDescriptor(colorCol).getLocalName());
		String attrName = (((SimpleFeatureType)schema).getDescriptor(colorCol).getLocalName());
//		System.out.println("check attrName:"+attrName+" ::  2kanjiProp" + colu.getKanjiProp(attrName) + "   parseAsSJ:"+(new String(((String)attrName).getBytes("iso-8859-1"),"Windows-31J")).trim()+"   parseAsUTF:"+(new String(((String)attrName).getBytes("iso-8859-1"),"UTF-8")).trim());
//		attrName = colu.getKanjiProp(attrName);
		
		if ( colu.mainAttrIsNumber ) {
			System.out.println(" attrMin:" + colu.mainAttrMin + "  attrMax:" + colu.mainAttrMax );
			if ( fMin >-9e99 ){
				System.out.println(" But you forced min:" + fMin + "  max:" + fMax );
				colu.mainAttrMin = fMin;
				colu.mainAttrMax = fMax;
			}
		} else {
			HashMap colorMap = colu.colorMap;
			Set set = colorMap.keySet();
			Iterator it = set.iterator();
			while (it.hasNext()){
				String o = (String)it.next();
				System.out.println(colorMap.get(o) + " = " + o);
			}
		}
		
		
		Rule rules[] = null;
		
		if ( colu.mainAttrIsNumber ){
			
			// outOfRangeView...
			
			double vSpan = (colu.mainAttrMax - colu.mainAttrMin) / (double)colorResolution;
			
			if ( outOfRangeView == SVGMapGetColorUtil.COUNTER_STOP || outOfRangeView == SVGMapGetColorUtil.MARK ){
				// オーバーフローデータに対する描画条件　２件追加
				rules = new Rule[colorResolution+2];
				
				// オーバーフロー時
				Color overFlowColor =  getColor(colu.getColor( colu.mainAttrMax + 0.5 * vSpan , colu.mainAttrMin , colu.mainAttrMax ));
				Filter overFlowFl =  ff.greater( ff.property(attrName) , ff.literal(colu.mainAttrMax) );
				Rule overFlowRl = getOneSymbolizeRule( overFlowColor , strokeColor , strokeWidth , dash , markerSize , schema , markerName );
				overFlowRl.setFilter(overFlowFl);
				rules[colorResolution] = overFlowRl;
				
				// アンダーフロー時
				Color underFlowColor =  getColor(colu.getColor( colu.mainAttrMin - 0.5 * vSpan , colu.mainAttrMin , colu.mainAttrMax ));
				Filter underFlowFl = ff.less( ff.property(attrName) , ff.literal(colu.mainAttrMin) );
				Rule underFlowRl = getOneSymbolizeRule( underFlowColor , strokeColor , strokeWidth , dash , markerSize , schema , markerName );
				underFlowRl.setFilter(underFlowFl);
				rules[colorResolution+1] = underFlowRl;
				
			} else {
				rules = new Rule[colorResolution];
			}
			
			for ( int i = 0 ; i < colorResolution ; i ++ ){
				double vLow = i * vSpan + colu.mainAttrMin;
				double vHigh = vLow + vSpan;
				String color = colu.getColor( vLow + 0.5 * vSpan , colu.mainAttrMin , colu.mainAttrMax );
				Filter fl1;
				if ( i == 0 ){
					fl1 = ff.greaterOrEqual( ff.property(attrName) , ff.literal(vLow) );
				} else {
					fl1 = ff.greater( ff.property(attrName) , ff.literal(vLow) );
				}
				Filter fl2 = ff.lessOrEqual( ff.property(attrName) , ff.literal(vHigh) );
				Filter fl = ff.and( fl1 , fl2 );
				Rule rl = getOneSymbolizeRule( getColor(color) , strokeColor , strokeWidth , dash , markerSize , schema , markerName );
				rl.setFilter(fl);
				rules[ i ] = rl;
				System.out.println(color + " = " + vLow + " ... " + vHigh);
			}
			
		} else {
			HashMap colorMap = colu.colorMap;
			HashSet truncatedKey = colu.truncatedKey; // added 2018.8.31
			rules = new Rule[colorMap.size()+1];
			
			Set set = colorMap.keySet();
			Iterator it = set.iterator();
			int i = 0;
			ArrayList<Filter> orList = new ArrayList<Filter>();
			while (it.hasNext()){
				String o = (String)it.next();
				String color = (String)colorMap.get(o);
				boolean truncated = false;
				if ( truncatedKey.contains(o) ){
					truncated = true;
				}
//				Filter fl = ff.begins( ff.property(attrName) , ff.literal( new String( o.getBytes("Shift_JIS"), 0) ) );
//				Filter fl = ff.equals( ff.property(attrName) , ff.literal( new String( o.getBytes("Shift_JIS"), 0) ) );
				
				Filter fl = null;
				if ( truncated ){ // カットされたキーの場合は、ワイルドカード指定 added 2018.8.31
					fl = ff.like( ff.property(attrName) , o+"*" );
				} else { // そうでないものは完全一致
					fl = ff.like( ff.property(attrName) , o );
				}
				
				// 以下２行はいずれも誤り・・・　キーが短縮されたものの場合はワイルドカードで検索、そうでないものの場合は完全一致が必要　上に修正 2018.8.31
//				Filter fl = ff.like( ff.property(attrName) , o+"*" ); // http://www.programcreek.com/java-api-examples/index.php?api=org.opengis.filter.FilterFactory2 を参考にしたワイルドカード検索・・(頭の文字列を検索する) 2017.5.12
//				Filter fl = ff.like( ff.property(attrName) , o ); // ワイルドカードはまずい場合がある。完全一致に修正・・　debug: 2018/8/9
				
//				Rule rl = getOneSymbolizeRule( getColor(color) , getColor("#000000") , 0 , dash , markerSize , schema );
				Rule rl = getOneSymbolizeRule( getColor(color) , strokeColor , strokeWidth , dash , markerSize , schema , markerName );
				rl.setFilter(fl);
				rules[ i ] = rl;
				orList.add(fl);
				++i;
			}
			// それ以外の場合にnullColorのアイコンを置くためのルールを作る
			@SuppressWarnings("unchecked")
			Filter otfl = ff.or(orList);
			otfl = ff.not(otfl);
			Rule otrl = getOneSymbolizeRule( getColor(colu.nullColor) , strokeColor , strokeWidth , dash , markerSize , schema , markerName );
			otrl.setFilter(otfl);
			rules[ colorMap.size() ] = otrl;
		}
		
		
//		Rule rule = getOneSymbolizeRule( fillColor , strokeColor , strokeWidth , dash , markerSize , schema );
		
		FeatureTypeStyle fts = sf.createFeatureTypeStyle(rules);
		Style style = sf.createStyle();
		style.featureTypeStyles().add(fts);
		return ( style );
	}
	
	
	Rule[] getSymbolizeRules( Color fillColor , Color strokeColor , int strokeWidth , boolean dash , int markerSize , FeatureType schema , String markerName ){
		Class<?> type = schema.getGeometryDescriptor().getType().getBinding();
		// 2020/1/21 ジオメトリタイプの混合があり得る場合に対応する(WKTとか・・・)
		
		// Reerences:
		// https://docs.geotools.org/latest/javadocs/org/geotools/styling/Rule.html#getSymbolizers--
		// https://docs.geotools.org/latest/userguide/library/render/style.html#featuretypestyle
		// https://docs.geoserver.org/stable/en/user/styling/sld/tipstricks/mixed-geometries.html
		// https://docs.geotools.org/latest/javadocs/org/geotools/filter/function/FilterFunction_geometryType.html
		// https://github.com/geotools/geotools/blob/master/modules/library/main/src/test/java/org/geotools/filter/visitor/AbstractCapabilitiesFilterSplitterTests.java#L155
		
		if ( type.isAssignableFrom( com.vividsolutions.jts.geom.Point.class) && type.isAssignableFrom( com.vividsolutions.jts.geom.LineString.class) ){
			System.out.println("Mixed geometry type data...");
			
			Rule rulePoint = getOneSymbolizeRule_int( fillColor , strokeColor , strokeWidth , dash , markerSize , schema , markerName , 1);
			rulePoint.setFilter(getGeometryTypeFilter("Point"));
			
			Rule ruleLineString = getOneSymbolizeRule_int( fillColor , strokeColor , strokeWidth , dash , markerSize , schema , markerName , 2);
			ruleLineString.setFilter(getGeometryTypeFilter("LineString"));
			
			Rule rulePolygon = getOneSymbolizeRule_int( fillColor , strokeColor , strokeWidth , dash , markerSize , schema , markerName , 3);
			rulePolygon.setFilter(getGeometryTypeFilter("Polygon"));
			
			// for multi 2021/6/10
			Rule ruleMultiPoint = getOneSymbolizeRule_int( fillColor , strokeColor , strokeWidth , dash , markerSize , schema , markerName , 1);
			ruleMultiPoint.setFilter(getGeometryTypeFilter("MultiPoint"));
			
			Rule ruleMultiLineString = getOneSymbolizeRule_int( fillColor , strokeColor , strokeWidth , dash , markerSize , schema , markerName , 2);
			ruleMultiLineString.setFilter(getGeometryTypeFilter("MultiLineString"));
			
			Rule ruleMultiPolygon = getOneSymbolizeRule_int( fillColor , strokeColor , strokeWidth , dash , markerSize , schema , markerName , 3);
			ruleMultiPolygon.setFilter(getGeometryTypeFilter("MultiPolygon"));
			
			
			Rule rules[] = {rulePoint,ruleLineString,rulePolygon,ruleMultiPoint,ruleMultiLineString,ruleMultiPolygon};
			
			return ( rules );
			
		} else {
			Rule rule = getOneSymbolizeRule( fillColor , strokeColor , strokeWidth , dash , markerSize , schema , markerName );
			Rule rules[] = {rule};
			return (rules);
		}
	}
	
	Filter getGeometryTypeFilter(String type){ // type: "Point","LineString","Polygon"
		FilterFactory ff = CommonFactoryFinder.getFilterFactory2(null);
		FilterFunction_geometryType geomTypeExpr = new FilterFunction_geometryType();
		geomTypeExpr.setParameters(Arrays.asList(new Expression[] {ff.property("the_geom")}));
		PropertyIsEqualTo filter = ff.equals(geomTypeExpr, ff.literal(type));
		return ( filter );
	}
	
	Rule getOneSymbolizeRule( Color fillColor , Color strokeColor , int strokeWidth , boolean dash , int markerSize , FeatureType schema , String markerName ){
		return (getOneSymbolizeRule_int( fillColor , strokeColor , strokeWidth , dash , markerSize , schema , markerName , 0 ));
	}
	
	// https://docs.geotools.org/stable/tutorials/map/style.html
	Rule getOneSymbolizeRule_int( Color fillColor , Color strokeColor , int strokeWidth , boolean dash , int markerSize , FeatureType schema , String markerName , int specificType){
		//スタイルを作成
		if ( strokeColor == null ){
			strokeColor = fillColor;
		}
		FilterFactory ff = CommonFactoryFinder.getFilterFactory2(null);
		StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
		
		Rule rule = sf.createRule();
//		System.out.println("Geometry Binding:"+type);
		
		if (specificType == 0 ){
			specificType = 3;
			Class<?> type = schema.getGeometryDescriptor().getType().getBinding();
			if ( type.isAssignableFrom( com.vividsolutions.jts.geom.Point.class) || 
			type.isAssignableFrom( com.vividsolutions.jts.geom.MultiPoint.class)){
				specificType = 1;
			} else if(type.isAssignableFrom( com.vividsolutions.jts.geom.LineString.class) ||
			type.isAssignableFrom( com.vividsolutions.jts.geom.MultiLineString.class) ){
				specificType = 2;
			} else if(type.isAssignableFrom( com.vividsolutions.jts.geom.Polygon.class) ||
			type.isAssignableFrom( com.vividsolutions.jts.geom.MultiPolygon.class) ){
				specificType = 3;
			}
		}
		
		if(specificType == 1 ){
			if ( markerSize <= 0 ){
				markerSize = strokeWidth * 3; // とりあえずね・・
			}
//			System.out.println("Point Style : " +  fillColor + ", size:" + markerSize );
			// point分を追加
//			Mark mark = sf.getCircleMark(); // 円形のマークを選択 以下２行で線と塗り設定
			Mark mark = sf.getDefaultMark();
			mark.setWellKnownName(ff.literal(markerName));
// https://geoserver-pdf.readthedocs.io/en/stable/styling/sld-extensions/pointsymbols.html
// をもとに、独自のMarkerFactoryを追加することができるらしい
// そうすると、SLDStyleFactory　の　DynamicSymbolFactoryFinder.getMarkFactories()　でひっかけることができる
// SPIについてはhttps://www.glamenv-septzen.net/view/1324　や　http://www.ne.jp/asahi/hishidama/home/tech/java/jar.html#h_Service_Providerなどを参考に
			if ( strokeColor == null ){
				strokeColor = fillColor;
			}
			org.geotools.styling.Stroke strokeP = sf.createStroke(
				ff.literal(strokeColor),
				ff.literal(strokeWidth)
			);
			mark.setStroke(strokeP);
			org.geotools.styling.Fill fillP = sf.createFill(ff.literal(fillColor));
			mark.setFill(fillP);
		
			Graphic graphic = sf.createDefaultGraphic();
			graphic.graphicalSymbols().clear();
			graphic.graphicalSymbols().add(mark);
			graphic.setSize(ff.literal(markerSize));
			
			PointSymbolizer poiSym = sf.createPointSymbolizer();
	//		poiSym.setGraphic(sf.getDefaultGraphic());
			poiSym.setGraphic(graphic);
			
			rule.symbolizers().add(poiSym);
		} else if( specificType == 2 ){
//			System.out.println("Line Style");
			// 線の色・太さ
			org.geotools.styling.Stroke stroke = null;
			if ( strokeWidth <= 0 ){
				strokeWidth = 1;
			}
			if ( strokeColor != null && strokeWidth > 0 ){
				if ( !dash ){
					stroke = sf.createStroke( ff.literal(strokeColor) , ff.literal(strokeWidth) );
				} else {
					stroke = sf.createStroke( 
						ff.literal(strokeColor) ,
						ff.literal(strokeWidth) ,
						ff.literal(1),
						ff.literal("bevel"),
						ff.literal("square"),
							new float[] { (float)(strokeWidth*3.0), (float)(strokeWidth*3.0) },
						ff.literal(0),
						null,
						null
					);
				}
			}
			
			LineSymbolizer sym = sf.createLineSymbolizer(stroke , null);
			
			rule.symbolizers().add(sym);
		} else if( specificType == 3 ){
//			System.out.println("Polygon Style");
			// ポリゴンの線の色
			org.geotools.styling.Stroke stroke = null;
			if ( strokeColor != null && strokeWidth > 0 ){
				stroke = sf.createStroke( ff.literal(strokeColor) , ff.literal(strokeWidth) );
			}
			
			// ポリゴンの塗りつぶし色
			org.geotools.styling.Fill fill = null;
			if ( fillColor != null ){
				fill = sf.createFill( ff.literal(fillColor) );
			}
			
			PolygonSymbolizer sym = sf.createPolygonSymbolizer(stroke, fill, null);
			
			rule.symbolizers().add(sym);
		}
		
		return ( rule );
	}
	
	boolean notUseIsNullImage = false;
	
	// Imageがカラなのかどうかを判別する・・・　重すぎる?ほどではない レンダラが遥かに支配的だった
	boolean isNullImage( BufferedImage image ){
//		System.out.print("isNullImg?:");
		boolean ans = true;
		if (notUseIsNullImage ){
			ans = false;
		} else {
			int height = image.getHeight();
			int width  = image.getWidth();
			loop0: for ( int i = 0 ; i < height ; i++ ){
				for ( int j = 0 ; j < width ; j++ ){
					int pix = image.getRGB(j , i) ;
					if ( pix >> 24 != 0 ){
						ans = false;
						break loop0;
					}
				}
			}
		}
//		System.out.println(ans);
		return ( ans );
	}
	
	int getLvl(double deg ){
		int lvl = 0;
		double sub = 360 / deg;
		while ( sub > 1){
			sub = sub / 2;
			++lvl;
		}
		return (lvl);
	}
	
	double getPartInv(int lvl){
		double partD = 1.0;
		for ( int i = 0 ; i < lvl ; i++){
			partD = partD / 2.0;
		}
		return ( partD );
	}
	
	private HashMap<Long,SvgMap> buildSmsMap( int GridXsize , int GridYsize ) throws Exception{
		return ( new HashMap<Long,SvgMap>() );
		
	}
	private SvgMap getSms( int GridX , int GridY , HashMap<Long,SvgMap> smsMap ) throws Exception{
		return( (SvgMap)smsMap.get(getHashKey( GridX , GridY ) ) );
	}
	
	public SvgMap buildSms( int j , int i , HashMap<Long,SvgMap> smsMap , ReferencedEnvelope geoBounds , double tcmul , NumberFormat nFmt ,  String fileName , String dir ) throws Exception{
		double lngMin  = geoBounds.getMinX();
		double latMin  = geoBounds.getMinY(); 
		double lngSpan = geoBounds.getWidth();
		double latSpan = geoBounds.getHeight();
		SvgMap newsm = new SvgMap ( dir + "sub_" + j + "_" + i + "_" + fileName , nFmt);
		newsm.putHeader( lngMin * tcmul , - (latMin + latSpan) * tcmul  , lngSpan * tcmul , latSpan * tcmul );
		newsm.putCrs( tcmul , 0 , 0 , -tcmul , 0 , 0 );
		newsm.setGroup();
		smsMap.put(getHashKey(j,i),newsm);
		return ( newsm);
	}
	
	public Long getHashKey( int index1 , int index2 ){
		return ( new Long((long)((long)index1 * (long)100000000 + (long)index2)));
	}
		
	public int[] getIndex( Long key ){
		int[] ans = new int[2];
		long kl = key.longValue( );
		ans[0] = (int)(kl / (long)100000000);
		ans[1] = (int)(kl % (long)100000000);
//		System.out.println(key+" , " + ans[0] + ":"+ans[1]);
		return ( ans );
	}
	
	void setMarkerSize(Style style , int markerSize){
		FilterFactory ff = CommonFactoryFinder.getFilterFactory2(null);
		java.util.List<FeatureTypeStyle> ftsl=style.featureTypeStyles();
		for ( FeatureTypeStyle fts : ftsl){
			java.util.List<Rule> rules = fts.rules();
			for(Rule rule : rules){
				java.util.List<Symbolizer> symbolizers =  rule.symbolizers();
				for ( Symbolizer symbolizer : symbolizers){
					if ( symbolizer instanceof  PointSymbolizer){
						Graphic gp = ((PointSymbolizer)symbolizer).getGraphic();
						gp.setSize(ff.literal(markerSize));
					}
				}
			}
		}
	}	
	
	private class  TileRenderRunnable implements Runnable{
		
		StreamingRenderer renderer;
		Graphics2D gr;
		BufferedImage image;
		Rectangle imageRect;
		ReferencedEnvelope mapRect;
		int tx, ty;
		int[][] hasData;
		boolean[][] isImage;
		String outDir;
		
		TileRenderRunnable( boolean antiAlias , DefaultMapContext map , BufferedImage image , Rectangle imageRect , ReferencedEnvelope mapRect , int tx, int ty , int sumUp , HashSet<Long> reqTile , boolean rebuildContainerOnly , String outDir ){
			renderer = new StreamingRenderer();
			if ( antiAlias ){
				// アンチエリアス関係のヒントを付ける
				RenderingHints rh=new RenderingHints(
					RenderingHints.KEY_ALPHA_INTERPOLATION,
					RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT);
				rh.add(new RenderingHints(
					RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON));
				rh.add(new RenderingHints(
					RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
				renderer.setJava2DHints(rh);		
			}
			renderer.setContext(map);
			this.gr = gr;
			this.imageRect = imageRect;
			this.mapRect = mapRect;
			this.tx = tx;
			this.ty = ty;
			this.image = image;
			this.outDir = outDir;
			gr = image.createGraphics();
			int[][] hd = {{0,0},{0,0}};
			hasData = hd;
			isImage = new boolean[sumUp][sumUp];
		}
		
		public void run(){
			// geotoolsに投げてgr経由でimageにレンダリングさせる
//			System.out.println("Thread: tx:" + tx + ", ty:"+ ty + "  描画開始");
			renderer.paint(gr, imageRect, mapRect);
//			System.out.println("Thread: tx:" + tx + ", ty:"+ ty + "  描画終了");
			
			// sumUpタイルの分割処理
//			System.out.println("GridXsizex:" + GridXsize + ", GridYsize:"+ GridYsize);
			
			int divs = sumUp / 2;
			if ( sumUp == 1 ){ divs = 1; } // sumUp == 1の特殊処理
			for ( int subX = 0 ; subX < sumUp ; subX++ ){
				int ttx = tx * sumUp + subX;
				int smX = ttx / containerCounts;
				for ( int subY = 0 ; subY < sumUp ; subY++ ){
					isImage[subX][subY] = false; // 念のため・・
					int tty = ty * sumUp + subY;
					int smY = tty / containerCounts;
					if ( ttx >= GridXsize || tty >= GridYsize ){
//						System.out.println(">=GridSize");
						continue;
					}
					if ( reqTile != null && ! reqTile.contains(getReqTileKey( ttx, tty ) ) ){ // 2014.2.28 debug
//						System.out.println("noReqTile");
						continue;
					}
					String tileName = "tile" + ttx + "_" + (GridYsize - tty -1) + ".png";
					if ( !rebuildContainerOnly ){
						BufferedImage bim = image.getSubimage( imageWidth * subX , imageHeight * (sumUp - 1 - subY) , imageWidth , imageHeight );
						if( reqTile == null && isNullImage( bim ) ){ // 2014.5.2 debug line objの場合nullImageになるときがあるので・・
//							System.out.println("NULL IMAGE....");
							continue;
						} else {
							++ hasData[subX/divs][subY/divs];
						}
						try{
							ImageIO.write(bim , "png" , new File(outDir + tileName ) );
						} catch ( Exception e ){
							System.out.println(e);
							System.exit(0);
						}
						isImage[subX][subY] = true;
					} else {
						isImage[subX][subY] = true;
					}
//					System.out.println("subX,subY:" + subX +","+subY);
				}
			}
			
			if ( sumUp == 1 && hasData[0][0] > 0 ){ // sumUp==1の特殊処理
				hasData[0][0] = 1; hasData[0][1] = 1; hasData[1][0] = 1; hasData[1][1] = 1; 
			}
			
			System.out.print("*");
		}
	}	
	
}

