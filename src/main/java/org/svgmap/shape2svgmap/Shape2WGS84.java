package org.svgmap.shape2svgmap;

// XYやBessel形式のShapefileをWGS84形式のShapefileに座標変換するshape2svgmapのヘルパソフトウェアです。
// その他にもかなり多くの機能が搭載されています。詳しくはヘルプ参照
//
// Copyright 2007 - 2017 by Satoru Takagi
//
// geoTools2.7.5で動作確認
//

// 2007.10.11 The first version
// 2010.07.28 レイヤ分割機能を拡充
// 2010.08.18 geotools2.6.5に対応
// 2010.10.04 geotools2.6.5でプロパティ値が日本語の場合文字化け修正
// 2015.07.17 UTMをサポート。久しぶりぃ
// 2017.04.03 CSV対応：CSVDataStoreを実装 (geotools 2.7非対応 shapefilDatastoreがabstruc*クラス・・)

import java.io.*;
import java.net.URL;
import java.io.IOException;
import java.util.*;
import java.net.URL;
import java.net.URI;
import org.geotools.data.DataStore;
import java.awt.geom.*;
import java.nio.charset.Charset;


import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStore;
//import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.*;
import org.geotools.feature.simple.*;
//import org.geotools.feature.type.*;

import org.opengis.feature.simple.*;
import org.opengis.feature.type.*; 
//import org.opengis.referencing.crs.*;
//import org.geotools.referencing.*;

//import org.geotools.data.Transaction;
//import org.geotools.filter.FilterFactoryFinder;
//import org.geotools.filter.FilterType;
// import org.geotools.data.vpf.*; //このモジュールはgt2.5.2ではdisableされてる！！2.5.2_M3にはあるらしい
import org.locationtech.jts.geom.*;

import org.geotools.geometry.jts.* ;

// for CSV Reader/Exporter 2017.4.3
import org.svgmap.shape2svgmap.cds.CSVDataStore;
import org.svgmap.shape2svgmap.cds.CSVFeatureReader;
import org.svgmap.shape2svgmap.cds.CSVFeatureSource;
import org.svgmap.shape2svgmap.cds.sjisExt;
import org.geotools.data.store.ContentDataStore;
// import org.geotools.data.AbstractFileDataStore;

public class Shape2WGS84 {
	static int maxLayerCount = 1024; // 2016.11.24 最大レイヤ分割可能数をこの値で指定できるようにした　値に関しては、colorMapMax(shp2imgsvgmapのSVGMapGetColorUtilおよびshp2svgmap本体)の上限値と併せている(これは今後システム的に統一できる機構を作るべきです:issue)
	String filepath;
	GeometryFactory gf;
	int xySys=0; // XY座標系の番号　０で不使用
	int utmZone = 0; // UTMのゾーン番号　０で不使用　xySysと排他
	int datum=GeoConverter.JGD2000;
	GeoConverter gconv;
	UTMconverter uconv;
	double unitMultiplyer = 1.0;
	boolean hasUnitMultiplyer = false;
	String odir ="";
	boolean infoOnly = false;
	
	// for CSV Support 2017.4.3
	boolean inputCsv = false;
	boolean outputCsv = false;
	String csvSchemaPath="";
	
	// for CSV out support 2017.6
	boolean outCsv = false;
	boolean csvUTF8 = false;
	boolean csvGzipped = false;
	
	String ofExt = "_crs84";
	
	private static void showHelp(){
		System.out.println("Shape2WGS84: XYやBessel形式のShapeをWGS84形式のShapeに座標変換します。");
		System.out.println("Copyright 2007-2017 by Satoru Takagi @KDDI All Rights Reserved.");
		System.out.println("java Shape2WGS84 [-options] inputfile");
		System.out.println("   inputfile: Shapefile(Shapefileの関連ファイル.shp|.shx|.dbf)");
		System.out.println("            : CSVファイル(拡張子:.csv):仕様はcsvschena参照");
		System.out.println("            ");
		System.out.println("Options   : -optionName (value)");
		System.out.println("-datum    : 空間参照系を設定 tokyoのみ設定できる");
		System.out.println("            デフォルト:JGD2000(WGS 84と等価)");
		System.out.println("-xy       : XY座標系の指定");
		System.out.println("            1～19：XY系のときに、その番号を指定[m]単位 ");
		System.out.println("            -1～-19：[mm]単位 ");
		System.out.println("-utm      : UTM座標系の指定");
		System.out.println("            ゾーン番号+グリッド文字(MGRS)   例: 53s　　もしくは");
		System.out.println("            中央子午線の経度 + (north|south)   例: 135north");
		System.out.println("-duplicate: 重複図形を抑制する");
		System.out.println("            属性名: 属性番号: 重複を把握するための属性番号を指定");
		System.out.println("-dupdir   : 重複図形チェック用ハッシュファイルディレクトリを指定する");
		System.out.println("            属性名: ディレクトリ名(無ければ新規・あれば参照更新)");
		System.out.println("-layer    : 指定した属性番号を元にレイヤ(ファイル)分割する");
		System.out.println("            レイヤのファイル名：属性値を元に設定");	
		System.out.println("            追加オプション:,の後に属性セットを指定すると１つのレイヤに入れる");
		System.out.println("            例:-layer 1,shop+stand:police+hospital:*");
		System.out.println("               shop+standで１レイヤ,police+hospitalで１レイヤ,残りは全て１レイヤ");
		System.out.println("            デフォルト:なし");
		System.out.println("-odir     : 指定したディレクトリに変換後のファイルを出力する");
		System.out.println("            デフォルト:オリジナルのシェープファイルと同じディレクトリ");
		System.out.println("-multiply : データ内の座標値が緯度経度[度]もしくは[m]と違う単位の場合、倍率を指定");
		System.out.println("            デフォルト:オリジナルのシェープファイルと同じディレクトリ");
		System.out.println("-divide   : 同、割り算の指定(分母の値を設定する)");
		System.out.println("            デフォルト:オリジナルのシェープファイルと同じディレクトリ");
		System.out.println("-ext      : 出力ファイルの追加文字列(デフォルトは _crs84)");
		System.out.println("-showhead : 情報の表示のみ：変換は行わない");
		System.out.println("-csvschena: データのスキーマファイルを指定");
		System.out.println("-charset  : csvのcharsetを指定 デフォルトはsjis UTF-8のみ指定可能");
		System.out.println(" CSVファイルの説明・制約:");
		System.out.println("    Pointのみサポート,他の属性はすべて文字列。");
		System.out.println("    スキーマをデータの１行目もしくは別ファイル(-csvschema)で指定。");
		System.out.println("    スキーマでは緯度(LAT)経度(LON)その他の属性名をCSVで設定。");
		System.out.println("    -csvschemaでスキーマを別ファイル化する場合、1行で表現末尾に改行必要。");
		System.out.println("-outputcsv: CSV形式で出力　今のところPointのみ、スキーマは無い(全てString仮定)限定実装・・");
		System.out.println("            ");
		System.out.println(" 注：Linuxでは、java -Dfile.encoding=SJIS Shape2WGS84 [-options] inputfile としないと文字化けしてしまいます！！");
		System.out.println("            ");
	}

	public static void main(String[] args) {
//		System.out.println(System.getProperties());
// LINUXでは、java -Dfile.encoding=SJIS で起動するしかないのか・・
		String filepath="";
		int params = 0;
		int xySys=0;
		int utmZone = 0;
		int dupCheck = -1;
		String layerStr = "";
		String dupdir = "";
		int datum=GeoConverter.JGD2000;
		Shape2WGS84 conv = new Shape2WGS84();
		try {
			if(args.length < 1 || args[args.length -1 ].indexOf("-") == 0 ){
				showHelp();
				System.out.println("入力ファイルが指定されていません");
				throw new IOException();
//				System.exit(0);
			}
			filepath = args[args.length - 1];
			if ( filepath.endsWith(".csv")){
				conv.inputCsv = true;
				System.out.println("CSV input");
			} else if ( filepath.endsWith(".gz")){
				conv.inputCsv = true;
				conv.csvGzipped = true;
				System.out.println("gzipped CSV input");
			} else {
				System.out.println("Shapefile input");
			}
			params = args.length - 1;
			
			for (int i = 0; i < params; ++i) {
				if ( args[i].toLowerCase().equals("-xy")){
					++i;
					xySys = Integer.parseInt(args[i]);
					System.out.println( "inputXY:" + xySys );
				} else if ( args[i].toLowerCase().equals("-utm")){ // expanded 2015/07
					int centralMeridian = 0;
					int zoneNumber = 0;
					
					++i;
					if ( args[i].toLowerCase().endsWith("south") ){
						centralMeridian = Integer.parseInt(args[i].substring(0,args[i].length()-5));
						zoneNumber = (int) Math.floor(centralMeridian/6+31);
						utmZone = -zoneNumber;
					} else if ( args[i].toLowerCase().endsWith("north") ){
						centralMeridian =Integer.parseInt(args[i].substring(0,args[i].length()-5));
						zoneNumber = (int) Math.floor(centralMeridian/6+31);
						utmZone = zoneNumber;
					} else {
						zoneNumber = Integer.parseInt(args[i].substring(0,args[i].length()-1));
						String designator = args[i].toLowerCase().substring(args[i].length()-1);
						if ( "cdefghjklm".indexOf(designator) >= 0 ){
							utmZone = -zoneNumber;
						} else {
							utmZone = zoneNumber;
						}
					}
					centralMeridian = (zoneNumber - 31) * 6 + 3;
					System.out.println( "utm ZoneNumber:" + zoneNumber + ( (utmZone < 0 ) ? " (south)" : " (north)" ) + " centralMeridian:" + centralMeridian );
				} else if ( args[i].toLowerCase().equals("-layer")){ // expanded 2010/07
					++i;
					layerStr = args[i];
					System.out.println( "layerStr:" + layerStr );
				} else if ( args[i].toLowerCase().equals("-ext")){ // expanded 2010/07
					++i;
					conv.ofExt = args[i];
					System.out.println( "Set extention str for output file:" + conv.ofExt );
				} else if ( args[i].toLowerCase().equals("-duplicate")){
					++i;
					dupCheck = Integer.parseInt(args[i]);
					System.out.println( "dupCheck:" + dupCheck );
				} else if ( args[i].toLowerCase().equals("-dupdir")){
					++i;
					dupdir = args[i];
					System.out.println( "dupdir:" + dupdir );
				} else if ( args[i].toLowerCase().equals("-datum")){
					++i;
					
					if ( args[i].toLowerCase().equals("tokyo") || args[i].toLowerCase().equals("bessel")){
						datum = GeoConverter.BESSEL;
						System.out.println( "inputDatum:TOKYO" );
					}
				} else if ( args[i].toLowerCase().equals("-multiply")){ // add 2010.02.18
					++i;
					conv.unitMultiplyer = Double.parseDouble(args[i]);
					conv.hasUnitMultiplyer = true;
				} else if ( args[i].toLowerCase().equals("-divide")){ // add 2010.02.18
					++i;
					conv.unitMultiplyer = 1.0 / Double.parseDouble(args[i]);
					conv.hasUnitMultiplyer = true;
				} else if ( args[i].toLowerCase().equals("-showhead")){ // add 2010.07
					conv.infoOnly = true;
				} else if ( args[i].toLowerCase().equals("-csvschema")){ // add 2017.04
					++i;
					conv.csvSchemaPath = args[i];
					System.out.println("Schema Path for CSV file: " + conv.csvSchemaPath);
				} else if ( args[i].toLowerCase().equals("-charset")){ // add 2017.04
					++i;
					if ( (args[i].toUpperCase()).equals("UTF-8")){
						conv.csvUTF8 = true;
						System.out.println("CSV charset is UTF-8");
					}
				} else if ( args[i].toLowerCase().equals("-outputcsv")){ // add 2017.06
					conv.outCsv = true;
					System.out.println("output csv files");
				} else if ( args[i].toLowerCase().equals("-odir")){ // add 2010.02.18
					++i;
					File odf = new File(args[i]);
					if ( odf.isFile() ){
						odf = odf.getParentFile();
					}
					conv.odir = odf.getAbsolutePath() ;
//					System.out.println("OPT ODIR" + conv.odir);
				} else {
					showHelp();
					System.out.println("存在しないオプション\"" + args[i] + "\"が指定されました。");
					throw new IOException();
//					System.exit(0);
				}
			}
			if ( xySys != 0 && utmZone != 0 ){
				System.out.println("XYもしくはUTMのどちらかしか指定できません。");
				throw new IOException();
			}
			conv.convert( filepath , datum , xySys , utmZone , dupCheck , dupdir , layerStr);
		}catch(Exception e){
			if ( e instanceof FileNotFoundException ){
				System.out.println("ファイルにアクセスできません: " + e.getMessage() );
			} else if ( e instanceof IOException ){
				System.out.println("パラメータが違います: " + e.getMessage());
				e.printStackTrace();
			} else {
				System.out.println("エラー: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	

	public void convert(String filepath , int datumi , int xySysi , int utmZonei , int dupCheck , String dupdirName , String layerStr) throws Exception{
		int layerCol;
//		new HashSet();
		// 図形重複チェックのための前準備
		HashSet<Object> dupHash = new HashSet<Object>(); // 変換対象のファイルのハッシュ
		File dupDir; // ハッシュのシリアライズ物があるディレクトリ
		String[] dupHashNameList; // 重複チェックのための他のハッシュのシリアライズ物のファイル名
		ArrayList<shapeFileMeta> surFilesMeta = null; // 重複チェックのための他のファイルのメタデータのリスト
		ArrayList<HashSet> surDupHashList = new ArrayList<HashSet>();  // 重複チェックのための他のファイルのハッシュ
		int surDupHashListCount =0;
		datum = datumi;
		if ( xySysi != 0){ // 排他設定処理 xy優先
			xySys = xySysi;
		} else {
			utmZone = utmZonei;
		}
			
		gf = new GeometryFactory();
		if ( datum == GeoConverter.TOKYO_BESSEL ){
			gconv = new GeoConverter(GeoConverter.Tky2JGD); // Tky2JGDを使ってできるだけ正確な変換を行うようにしています
		} else {
			gconv = new GeoConverter();
		}
		uconv = new UTMconverter(); // added 2015.7.17
		//--------------------------------
		// リーダー部初期化
		//--------------------------------
		CSVDataStore cds =null;
		ShapefileDataStore sds = null;
		
		FeatureSource<SimpleFeatureType, SimpleFeature> source = null;
		if ( inputCsv ){
			String csvCharset="MS932";
			if ( csvUTF8 ){
				csvCharset="UTF-8";
			}
			// CSVファイルを読み込む 
			if ( csvSchemaPath =="" ){
				cds = new CSVDataStore( new File(filepath) , csvGzipped , csvCharset );
			} else {
				cds = new CSVDataStore( new File(filepath), new File(csvSchemaPath) , csvGzipped , csvCharset );
			}
			//フィーチャーソース取得
			source = cds.getFeatureSource(cds.getNames().get(0));
		} else {
			//ロードするＳｈａｐｅ形式ファイル
			URL shapeURL = (new File(filepath)).toURI().toURL();
//			readStore = (ShapefileDataStore) new ShapefileDataStore(shapeURL); // cast to ShapefileDataStore
			sds = new ShapefileDataStore(shapeURL); // cast to ShapefileDataStore
			//フィーチャーソース取得
			source = sds.getFeatureSource();
		}
		
		Envelope env = source.getBounds();
		//入力カラム情報の取得
		SimpleFeatureType readFT = source.getSchema();

		//フィーチャーソースからコレクションの取得
		FeatureCollection<SimpleFeatureType, SimpleFeature> fsShape = source.getFeatures();
		
		System.out.println("FeatureSourve.Bounds:" + env );
		
		// 複数のファイルの間の重複チェックを行うメカニズムを発動する
		if ( dupCheck >= 0 && dupdirName !="" ){
			try {
				dupDir = new File(dupdirName);
				if ( dupDir.exists()){
					if ( dupDir.isDirectory()) {
						System.out.println("READ Existing Hash Table from:" + dupdirName);
						dupHashNameList = dupDir.list();
						
						//メタデータを読み込み
						FileInputStream inFile = new FileInputStream(dupdirName + File.separator + "hash.index");
						ObjectInputStream inObject = new ObjectInputStream( inFile );
//						surFilesMeta = (ArrayList<shapeFileMeta>)inObject.readObject();
						surFilesMeta = automaticCast(inObject.readObject());
						inObject.close();
						
						//メタデータをもとに、検索対象の外部データハッシュを読み込む
						for ( int i = 0 ; i < surFilesMeta.size() ; i++ ){
							shapeFileMeta sf = surFilesMeta.get(i);
							Envelope surEnv = sf.bbox;
							if ( env.contains( surEnv ) || env.intersects( surEnv ) ){
								System.out.println ("Hit File :" + sf.filename );
								FileInputStream inSFile = new FileInputStream(dupdirName + File.separator + sf.filename );
								ObjectInputStream inSObject = new ObjectInputStream( inSFile );
								HashSet surDupHash = (HashSet)inSObject.readObject();
								surDupHashList.add(surDupHash);
								inSObject.close();
							}
						}
						surDupHashListCount = surDupHashList.size();
					} else {
						System.out.println( "File already exists!" + dupdirName );
						System.exit(0);
					}
				} else {
					System.out.println("Make NEW Hash Directory : " + dupdirName );
					dupDir.mkdir();
					
					surFilesMeta = new ArrayList<shapeFileMeta>();
					
				}
				
				// このデータをメタデータのリストに追加する
				shapeFileMeta thisSF = new shapeFileMeta();
				thisSF.bbox = env;
//					thisSF.filename = (new File(filepath)).getName();
				thisSF.filename = getHashFileName(filepath);
				
//					if ( surFilesMeta.surFileInfo == null ){
//						surFilesMeta.surFileInfo = new ArrayList<surFileInfo>();
//					}
				surFilesMeta.add(thisSF);
				
			} catch ( IOException e ){
				e.printStackTrace();
				System.exit(0);
			}
			/**
			{	FileInputStream inFile = new FileInputStream(dupfile); 
				ObjectInputStream inObject = new ObjectInputStream(inFile);
				dupHash = (HashSet)inObject.readObject();
				inObject.close();
				inFile.close();
				System.out.println("READ Hash Table :" + dupfile);
			} catch ( IOException e ){
				System.out.println(e);
				System.out.println("NEW  Hash Table :" + dupfile);
			}
			**/
		}
		
		
//			System.out.println("レコード数:" + fsShape.getCount() );
		System.out.println("レコード数:" + fsShape.size() );
		
		// レイヤー分割準備
//		String[] layerNames = new String[256];
		@SuppressWarnings("unchecked")
		HashSet<String>[] layerNames = new HashSet[maxLayerCount];
		Envelope[] layerBounds = new Envelope[maxLayerCount];
		
		int layerCount;
		
		//レイヤの数と範囲の検索
		if ( layerStr.length() >0 ){
			int dlm = layerStr.indexOf(",");
			if (dlm > 0 ){
				layerCol = Integer.parseInt(layerStr.substring(0 , dlm ));
				layerStr = layerStr.substring(dlm +1 );
			} else {
				layerCol = Integer.parseInt(layerStr);
				layerStr ="";
			}
			System.out.println ("layerCol:" + layerCol + " Str:" + layerStr );
			layerCount = getLayerNames(fsShape ,  layerNames , layerBounds , layerCol , layerStr);
//			System.out.println("layerBounds:" + layerBounds);
		} else {
			layerCol = -1;
//			layerNames[0] = "";
			layerCount = 1;
			layerBounds[0] = env;
		}
		
		if ( infoOnly){
			for ( int i = 0 ; i < readFT.getAttributeCount() ; i++ ){
				AttributeDescriptor readAT = readFT.getDescriptor(i);
				String inp = getKanjiProp(readAT.getLocalName());
//				String aName = new String(inp.getBytes("Shift_JIS"), 0);
//				System.out.println("attrNo:"+ i + "  Name:" + readAT.getName() + "  Type:" + readAT.getType().getBinding().getSimpleName());
				System.out.println("attrNo:"+ i + "  Name:" + inp + "  Type:" + readAT.getType().getBinding().getSimpleName());
			}
			return;
		}

		if ( odir == "" ) {
			odir = ((new File(filepath)).getAbsoluteFile()).getParent();
		}
		String inFileName = (new File(filepath)).getName();
//		System.out.println ("ODIR:" + odir + " Name:" + inFileName);
		for ( int layerNumber = 0 ; layerNumber < layerCount ; layerNumber ++ ){
		
		
			//コレクションをイテレータに設定
			FeatureIterator<SimpleFeature> reader = fsShape.features();
			
			// ライタ部
			//出力シェープファイル設定
			// 新geotoolsでは.shp拡張子(や.dbf)が必要らしい 2010/08
			URL anURL;
			String oFileExt;
			if (layerCount >1 ){
//				anURL = (new File(filepath.substring(0 , filepath.lastIndexOf(".")) + ofExt + "_" + layerNames[layerNumber] )).toURL();
				String layerName = (String)layerNames[layerNumber].toArray()[0];
				layerName = layerName.replace(" ","_");
				layerName = layerName.replace("/","_");
				oFileExt = ofExt + "_" + layerName;
			} else {
//				anURL = (new File(filepath.substring(0 , filepath.lastIndexOf(".")) + ofExt )).toURL()
				oFileExt = ofExt;
			}
			anURL = (new File(odir + File.separator + (inFileName.substring(0 , inFileName.lastIndexOf("."))).replace(" ","_") + oFileExt + ".shp" )).toURI().toURL();
				
			
			//書き出し用のFeatureTypeを作成する（SJIS文字化けに対応・・・）08.04.22
			// ようやく何とか文字化けを解消しつつgt2.6.5に対応できた・・・
			AttributeDescriptor[] types = new AttributeDescriptor[readFT.getAttributeCount()];
			String csvAttrName ="";
			for ( int i = 0 ; i < readFT.getAttributeCount() ; i++ ){
				
				AttributeDescriptor ad = readFT.getDescriptor(i);
				AttributeType readAT = ad.getType();
//				Name          aName2 = ad.getName();
				
//				String aName = new String(((String)readAT.getName()).getBytes("iso-8859-1"), "Shift_JIS");
//				String aName = new String(((String)readAT.getName()).getBytes("Shift_JIS"), "iso-8859-1");
//				String aName = new String(((String)readAT.getName()).getBytes("Shift_JIS"), "Shift_JIS");
//				String inp = "属性" + i;
				
				String inp = getKanjiProp(ad.getLocalName());
//				String aName = new String(inp.getBytes("Shift_JIS"), 0); // deprecated関数のため置き換え 2017/11/02
				String aName = sjisExt.getSjisStr(inp);
//				String aName = getKanjiProp(inp);
//				String aName = new String( inp.getBytes() , "UTF-8" );
//				String aName = new String( inp.getBytes() , "Shift_JIS" );
//				String aName = new String( inp.getBytes("Shift_JIS") , "Shift_JIS" );
				if ( layerNumber == 0 ){
					System.out.println("attrNo:"+ i + " : "  + inp + "  Type:" + ad.getType().getBinding().getSimpleName());
				}
				
				AttributeTypeBuilder builder = new AttributeTypeBuilder();
				builder.init(ad);
//				System.out.println( "Desc" + i + ":" + types[i] );
//				types[i] = builder.buildDescriptor( aName2 , readAT ); // 漢字変換をせず直接
//				AttributeDescriptor ad2 = new AttributeDescriptorImpl( readAT , aName2 , ad.getMinOccurs() , ad.getMaxOccurs() , ad.isNillable() , ad.getDefaultValue() ); 

				if ( ad.getType() instanceof GeometryType ){
					System.out.println("GEOM");
					types[i] = ad; // builder.buildDescriptorがエラー源--CRSがNULLだった
				} else {
					types[i] = builder.buildDescriptor( aName , readAT );
					csvAttrName +=inp +",";
				}
//				types[i] = AttributeTypeFactory.newAttributeType( aName , readAT );
//				types[i] = AttributeTypeFactory.newAttributeType( readAT.getName() , readAT.getType() );
//				System.out.println(types[i]);
			}
//			CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
//			CoordinateReferenceSystem destCrs = org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;
			csvAttrName+="lat,lon";
			
			// shapefileもしくはcsvの出力準備
			ShapefileDataStore writeStore = null;
			FeatureWriter fw = null;
			PrintWriter pw = null;
			
			if ( outCsv ){
				String cpath = anURL.getPath();
				cpath = cpath.substring(0,cpath.indexOf(".shp"))+".csv";
				System.out.println("CSV Path:"+cpath+ "  attr:"+csvAttrName);
				FileWriter filew = new FileWriter( cpath ,false );
				pw = new PrintWriter(new BufferedWriter(filew));
				pw.println(csvAttrName);
			} else {
				SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
				builder.init( readFT );
				builder.setAttributes( types ); 
				builder.setName(builder.getName() + oFileExt); // 2015.4.24 (getTools12)
				System.out.println("builderName:"+builder.getName());
				SimpleFeatureType writeFT = builder.buildFeatureType();
	  

				
	//			SimpleFeatureType writeFT = FeatureTypes.newFeatureType(types , readFT.getTypeName() , new URI(readFT.getName().getNamespaceURI() ) , readFT.isAbstract() , readFT.getSuper() );
	//			System.out.println( "writeFT:" + writeFT );
	//			System.out.println( "read FT:" + readFT );
				
				//出力データストア設定
				System.out.println("outURL:" + anURL);
	//			FileDataStoreFactorySpi factory = new ShapefileDataStoreFactory();
	//			Map map = Collections.singletonMap( "url", anURL );
	//			ShapefileDataStore writeStore = (ShapefileDataStore)factory.createNewDataStore( map );
				
	//			ShapefileDataStore writeStore = (ShapefileDataStore)factory.createDataStore( anURL );
				
				writeStore = new ShapefileDataStore(anURL);
	//			SimpleFeatureType featureType = DataUtilities.createType( "my", "geom:Point,name:String,age:Integer,description:String" );
	//			writeStore.createSchema(featureType);
				writeStore.createSchema(writeFT);
	//			writeStore.createSchema(readFT); // これで出力自体はうまくいっちゃってますね・・・
				
				fw = writeStore.getFeatureWriter(writeFT.getTypeName(), ((FeatureStore) writeStore.getFeatureSource(writeFT.getTypeName())).getTransaction());
			}
			
			int lop=0;
			int dupCount = 0 ;
			SimpleFeature oneFeature = null;
			Geometry inGeom , outGeom;
			Coordinate[] coord;
			SimpleFeature outFeature = null;
			boolean hasFeature = false;
			while (reader.hasNext()) {
				hasFeature = false;
				++ lop;
				if ( lop % 10000 == 0 ){
					System.out.print("O");
				} else if ( lop % 1000 == 0 ){
					System.out.print(".");
				}
				while ( hasFeature == false ){
					try{
						oneFeature = reader.next();
						hasFeature = true;
					} catch ( Exception e ){
						System.out.print("ERR");
						hasFeature = false;
					}
				}
				
				if ( dupCheck >=0 ){
					boolean isDup = false;
					Object dupChkAttr = oneFeature.getAttribute(dupCheck);
					
					// まずは自分自身に重複が無いか確認
					if (dupHash.contains(oneFeature.getAttribute(dupCheck))){
						isDup = true;
					} else { // 無い場合は隣接コンテンツに重複が無いか確認
						for ( int i = 0 ; i < surDupHashListCount ; i++ ){
							if ( (surDupHashList.get(i)).contains(dupChkAttr)){
								isDup = true;
								break;
							}
						}
					}
					// dupHash.contains(oneFeature.getAttribute(dupCheck));
					if ( isDup ){
						++ dupCount;
						-- lop;
						continue; // 重複があったらスルーする
					} else {
						dupHash.add(dupChkAttr ); // 重複無ければ自分のハッシュにIDを追加して処理を続ける
					}
				}
				
				// 属性によるレイヤー分割を行う場合
				if ( layerCount >1 ){
						if ( ! (layerNames[layerNumber].contains(getKanjiProp(oneFeature.getAttribute(layerCol).toString())) )){
							continue;
						}
				}
				
				inGeom = (Geometry)oneFeature.getDefaultGeometry();
				outGeom = transCoordinates( inGeom );
				if (!outCsv){
					outFeature = (SimpleFeature)fw.next();
				}
				for ( int i = 0 ; i < oneFeature.getAttributeCount(); i++){
					Object oneAttr = oneFeature.getAttribute( i );
/* geoTools2.6.5では文字化けがおきなくなったので、処理を省く
					if (oneAttr instanceof String){
//						System.out.print(oneAttr);
//						oneAttr = oneAttr.toString();
//							oneAttr = (Object)new String(((String)oneAttr).getBytes("iso-8859-1"), "Shift_JIS");
						oneAttr = (Object)getKanjiProp((String)oneAttr);
//						System.out.println(":" + oneAttr);
					}
*/
					if ( outCsv ){
						if (oneAttr instanceof String){
							String atrs = getKanjiProp((String)oneAttr);
							if ( atrs.indexOf(",")>=0 || atrs.indexOf("\n") >=0 ){
								atrs= "\""+atrs+"\"";
							}
							pw.print(atrs+",");
						} else if ( oneAttr instanceof Geometry){
							// skip
						} else {
							pw.print(oneAttr+",");
						}
					} else {
						outFeature.setAttribute(i, oneAttr);
					}
				}
				if ( outCsv ){
					if ( outGeom instanceof Point ){
						Coordinate crd = outGeom.getCoordinate();
						pw.println(crd.y+","+crd.x);
					} else {
						pw.println("Unsupported...");
					}
				} else {
					outFeature.setDefaultGeometry(outGeom);
					fw.write();
				}
			}
			
			System.out.println("\n書き出し レコード数：" + lop);
			if ( layerCount > 1 ){
				System.out.println( (layerNumber +1) + " / " + layerCount + " : " + anURL );
			} else {
				System.out.println( "FileName : " + anURL + ".shp,shx,dbf" );
			}
			
			if ( outCsv ){
				pw.close();
			} else {
				fw.close();
				writeStore.dispose(); // 2015.4.24
			}
			if ( dupCheck >=0 ){
				System.out.println("duplicated Object:" + dupCount );
			}
			reader.close();
			
			if ( dupCheck >=0  && dupdirName != "" ){
				// 変換対象のファイルのハッシュを保存
				FileOutputStream outFile = new FileOutputStream( dupdirName + File.separator + getHashFileName(filepath) ); 
				ObjectOutputStream outObject = new ObjectOutputStream(outFile);
				outObject.writeObject(dupHash);
				outObject.close();
				outFile.close();
				
				// インデックスデータを更新
				FileOutputStream outIndexFile = new FileOutputStream(dupdirName + File.separator + "hash.index");
				ObjectOutputStream outIndex = new ObjectOutputStream(outIndexFile);
				outIndex.writeObject(surFilesMeta);
				outIndex.close();
				outIndexFile.close();
			}
			dupCheck = -1; // ２レイヤー目からは、チェック処理はdisableにしないとね
		}
		if ( inputCsv ){
			cds.dispose();
		} else {
			sds.dispose();
		}
	}
	
	private Geometry transCoordinates( Geometry geom ){
		Coordinate[] coord , coord0;
		Coordinate oneCrd = new Coordinate();
		LinearRing shell;
		LinearRing[] holes;
		Geometry out;
		Geometry[] geoCol;
		if (geom instanceof Polygon ){
			coord = (((Polygon)geom).getExteriorRing()).getCoordinates();
			coord0 = new Coordinate[coord.length];
			for ( int i = 0 ; i < coord.length ; i++ ){
				coord0[i] = transCoordinate(coord[i] );
			}
			shell = gf.createLinearRing(coord0);
			
			holes = new LinearRing[((Polygon)geom).getNumInteriorRing()];
			
			for ( int j = 0 ; j < ((Polygon)geom).getNumInteriorRing() ; j++ ){
				coord = (((Polygon)geom).getInteriorRingN(j)).getCoordinates();
				coord0 = new Coordinate[coord.length];
				for ( int i = 0 ; i < coord.length ; i++ ){
					coord0[i] = transCoordinate(coord[i] );
				}
				holes[j] = gf.createLinearRing(coord0);
			}
			
			out = gf.createPolygon( shell , holes );
			
		} else if (geom instanceof LineString ){
			coord = ((LineString)geom).getCoordinates();
			coord0 = new Coordinate[coord.length];
			for ( int i = 0 ; i < coord.length ; i++ ){
				coord0[i] = transCoordinate( coord[i] );
			}
			out = gf.createLineString( coord0 );
		} else if (geom instanceof Point ){
			oneCrd = transCoordinate(((Point)geom).getCoordinate() );
			out = gf.createPoint( oneCrd );
		} else if (geom instanceof MultiPolygon ){
			geoCol = new Polygon[((MultiPolygon)geom).getNumGeometries()];
			for ( int j = 0 ; j < ((MultiPolygon)geom).getNumGeometries() ; j++){
				Geometry childGeom = ((MultiPolygon)geom).getGeometryN(j);
				geoCol[j] = (Polygon)transCoordinates(childGeom);
			}
			out = gf.createMultiPolygon((Polygon[])geoCol);
		} else if (geom instanceof MultiLineString ){
			geoCol = new LineString[((MultiLineString)geom).getNumGeometries()];
			for ( int j = 0 ; j < ((MultiLineString)geom).getNumGeometries() ; j++){
				Geometry childGeom = ((MultiLineString)geom).getGeometryN(j);
				geoCol[j] = (LineString)transCoordinates(childGeom);
			}
			out = gf.createMultiLineString((LineString[])geoCol);
		} else if ( geom instanceof MultiPoint ){
			geoCol = new Point[((MultiPoint)geom).getNumGeometries() ];
			for ( int j = 0 ; j < ((MultiPoint)geom).getNumGeometries() ; j++){
				Geometry childGeom = ((MultiPoint)geom).getGeometryN(j);
				geoCol[j] = (Point)transCoordinates(childGeom);
			}
			out = gf.createMultiPoint((Point[])geoCol);
		} else if (geom instanceof Geometry ){
			out = gf.createGeometry( geom );
			System.out.println("Type: Other Geometry...." + geom );
		} else {
			out = gf.createGeometry( geom );
			System.out.println("Type: Other Object...." + geom );
		}

		
		return ( out );
	}
	
	
	private  Coordinate transCoordinate( Coordinate inCrd ){
		Coordinate outCrd;
		LatLonAlt BL;
		if ( hasUnitMultiplyer ) {
			inCrd.x = inCrd.x * unitMultiplyer; // 安易な感じがします。おそいのでは？？
			inCrd.y = inCrd.y * unitMultiplyer;
		}
		if ( xySys > 0 ){
			gconv.setXY(inCrd.y , inCrd.x , xySys , datum );
			BL = gconv.toWGS84();
//			g2s.calcTransform(BL.longitude , BL.latitude );
		} else if ( xySys < 0 ){
			gconv.setXY(inCrd.y / 1000.0 , inCrd.x / 1000.0 , -xySys , datum );
			BL = gconv.toWGS84();
//			g2s.calcTransform(BL.longitude , BL.latitude );
		} else if ( utmZone != 0 ){
			double[] blm = uconv.getLatLng( inCrd.y , inCrd.x , utmZone );
			if ( datum == GeoConverter.BESSEL ){
				gconv.setLatLon(blm[0],blm[1] , datum );
				BL = gconv.toWGS84();
			} else {
				BL = new LatLonAlt(blm[0],blm[1],0);
			}
		} else {
			if ( datum == GeoConverter.BESSEL ){
				gconv.setLatLon(inCrd.y , inCrd.x , datum );
				BL = gconv.toWGS84();
//				g2s.calcTransform(BL.longitude , BL.latitude );
			} else {
				BL = new LatLonAlt( inCrd.y , inCrd.x , 0 );
//				g2s.calcTransform(inCrd.x , inCrd.y );
			}
		}
		outCrd = new Coordinate( BL.longitude , BL.latitude );
		return (outCrd);
	}
	
	private int getLayerNames( FeatureCollection<SimpleFeatureType, SimpleFeature> fsShape , HashSet<String>[] layerNames , Envelope[] layerBounds , int layerCol , String layerStr ){
		SimpleFeature oneFeature = null;
//		HashSet set = new HashSet(); // 無駄な気が・・・・
		HashMap<String,Envelope> layerNameMap = new HashMap<String,Envelope>();
		HashMap<String,Integer> layerNameMapCount = new HashMap<String,Integer>();
		Envelope oneEnv;
		String layerNameValue;
		boolean err = false;
		FeatureIterator<SimpleFeature> reader = fsShape.features();
		boolean hasFeature;
		// まずアトリビュートの種類を数える
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
			
//			layerNameValue = (oneFeature.getAttribute(layerCol));
			
			Object val = oneFeature.getAttribute(layerCol);
			String vals ="";
			if ( val instanceof String ){
				vals = (String)val;
			} else {
				vals = val.toString();
			}
			layerNameValue = getKanjiProp(vals);
				
//			layerNameValue = getKanjiProp((String)oneFeature.getAttribute(layerCol));
//			set.add(layerNameValue.toString());
			if ( layerNameMap.containsKey( layerNameValue ) ){
				oneEnv = (ReferencedEnvelope)(layerNameMap.get( layerNameValue ));
				int c = ((Integer)(layerNameMapCount.get(layerNameValue))).intValue() + 1;
				oneEnv.expandToInclude( (ReferencedEnvelope)oneFeature.getBounds() );
				layerNameMap.put( layerNameValue , oneEnv ); // これは不要？・・じゃないか
				layerNameMapCount.put( layerNameValue , new Integer(c) );
			} else {
				layerNameMap.put( layerNameValue , (Envelope)(oneFeature.getBounds()) );
				layerNameMapCount.put( layerNameValue , new Integer(1) );
			} 
			
			
			if ( layerNameMap.size() > maxLayerCount ){ // レイヤの数が２５６を超えたらエラーアウトしようかと・・
				System.out.println ( "Out Of Layer Size (>"+ maxLayerCount + ") Error!");
				err = true;
				break;
			}
		}
		
		System.out.println("total prop vars:" + layerNameMap.size());
		
		// propName1+propName2:propName3:propName4+propName5+propName6:*
		// p1+p2、p3、p4+p5+p6でそれぞれ１つのレイヤ、その他(*)で１つのレイヤとする
		// * が無い場合は、含まれて居ないものはそれぞれ１レイヤとする
		
		// layerNameMap:実データ内のプロパティ値マップ
		
		// まず　その他レイヤー指定の有無を検索
		boolean hasOthers = false;
		if (layerStr.indexOf("*") >= 0 ){
			hasOthers = true;
		}
		
		int[] layerRecords = new int[maxLayerCount]; // 各レイヤの要素数
		
		HashSet<String> layerSet = new HashSet<String>();
		StringTokenizer lgt = new StringTokenizer( layerStr , ":");
		int layerCount = 0;
		while ( lgt.hasMoreTokens() ){
			layerNames[layerCount] = new HashSet<String>();
			layerRecords[layerCount] = 0;
			boolean hasItems = false;
			String keys = lgt.nextToken();
			
			StringTokenizer kt = new StringTokenizer( keys , "+");
			while ( kt.hasMoreTokens() ){
				String layerName = kt.nextToken();
				if ( layerNameMap.containsKey( layerName ) && ( ! layerSet.contains(layerName) ) ){
					layerSet.add(layerName );
					layerNames[layerCount].add(layerName);
					if ( layerBounds[layerCount] == null ){
						layerBounds[layerCount] = (Envelope)(layerNameMap.get(layerName));
					} else {
						(layerBounds[layerCount]).expandToInclude((Envelope)(layerNameMap.get(layerName)));
					}
					hasItems = true;
					layerRecords[layerCount] += ((Integer)layerNameMapCount.get(layerName)).intValue();
				}
			}
			
			if ( hasItems ){
				++ layerCount;
			}
		}
		
		// layerSet完成: レイヤ名称(Key)
		//
		
		// 指名レイヤ(layerSet)に無かったものを処理する
		Iterator<String> iterator = layerNameMap.keySet().iterator();
		layerNames[layerCount] = new HashSet<String>();
		layerRecords[layerCount] = 0;
		while(iterator.hasNext()){
			String key = iterator.next();
			if ( ! layerSet.contains( key ) ){
				if ( hasOthers ){
					// 他の全てを１つのレイヤに投入
					layerNames[layerCount].add(key);
					if ( layerBounds[layerCount] == null ){
						layerBounds[layerCount] = (Envelope)(layerNameMap.get(key));
					} else {
						(layerBounds[layerCount]).expandToInclude((Envelope)(layerNameMap.get(key)));
					}
					layerRecords[layerCount] += ((Integer)layerNameMapCount.get(key)).intValue();
				} else {
//					System.out.println("key:" + key + " no:" + layerCount);
					// 他の全てを別のレイヤに投入
					layerNames[layerCount].add(key);
					layerBounds[layerCount] = (Envelope)( layerNameMap.get(key));
					layerRecords[layerCount] = ((Integer)layerNameMapCount.get(key)).intValue();
					++ layerCount;
					layerNames[layerCount] = new HashSet<String>();
				}
				
			}
		}
		
		if ( hasOthers ){
			++layerCount;
		}
		
		
		System.out.println( "LayerCount: " + layerCount);
		for ( int i = 0 ; i < layerCount ; i++ ){
			System.out.println("layer" + i + " Names:" + layerNames[i] + " Records:" + layerRecords[i] );
		}
//		System.out.println( "Envelopes: "  +  layerNameMap);
		
		return( layerCount );
	}
	
	String getKanjiProp( String input ){
		String ans ="";
		try {
			ans =  (new String(((String)input).getBytes("iso-8859-1"),"Shift_JIS")).trim();
		} catch (Exception e){
			ans = "";
		}
		return ( ans );
	}
	
	String getHashFileName ( String originalPath ){
		String name = (originalPath.replace(File.separator,"_")).replace(":","_");
		int i = name.lastIndexOf(".");
		System.out.println("orig:" + originalPath + "  ._lastIndex" + i );
		name = name.substring(0,i) + ".hash";
		return ( name );
	}
	
	@SuppressWarnings("unchecked") 
	public static <T> T automaticCast(Object src) { 
		T castedObject = (T) src; 
		return castedObject; 
	} 
	
}