package org.svgmap.shape2svgmap;

// ShapeをSVG Mapに変換するソフトウェアです。
// Copyright 2007 - 2018 by Satoru Takagi
// 
// geoTools2.7.5 で動作確認
//
// 2007.02.26 The First Version
// 正距方位図法の標準緯線指定
// SVG高さ指定
// XY系->緯度経度変換
// 図形色指定
// 2007.04.02 RDF/XML出力機能追加
// 2007.04.27
// 2007.05.17 タイル分割システムを完成させた
// 2007.07.09 最適化タイリング法
// 2007.10.24 高速化を図る(図形のBBOXを使ってラフなクリップをすることで処理を軽減)
// 2007.11.1 更なる高速化
// 2007.12.10- microMetadata仕様、null Tile除去、container階層化、Vector Effect、FontAutoSizing　などのコーディングを開始したい＾＾
// 2007.12.13 null Tile除去(実は組んであったがバグが出ていた), FontAutoSizing
// 2007.12.14 microMetadata
// 2008.04.22 データの重複チェック(任意のmetadataのID番号による)機構
// 2009.01.25 スタイルのまとめ機能を実装(useDefaultStyleフラグ) > SVGのドキュメント構造が大きく変わるのでかなり危うい機能だけど・・
// 2009.02.06 データの重複チェック(任意のmetadataのID番号による)機構
// 2009.08.12 この時期までに色々追加  GUIラッパー(Shape2SVGMapGUI) tky2jgdによる高精度変換、ストローク幅固定op.、SJIS以外の文字コード等
// 2009.09.08 シンボルのサイズを固定するオプション、指定した番号のメタデータのみを出力
// 2010.06.08 キャプションの表示を適正化(図形のスタイルを引きずっていた問題をfix)
// 2010.08.19 geoTools2.6.5対応のための改修(結構大掛かり。おそらく後方互換性は無い)
// 2010.10.19 SVG1.2Tiny+SVG MapJIS対応をデフォルトに。 crsのtransform１００をデフォルト化
// 2010.12.28 parse関数を清掃、線幅デフォルトをfixedwidth=1に
// 2011.12.16 色テーブル周りの設定を詳細にできるようにした(numcolorにiHSV追加、numrange追加)
// 2012.04.05 POI(Point)周りの表現力拡充
// 2012.04.10 タイルの密度に応じた表示制御機能を拡充(densityControl=dd[pix/TILE])
// 2012.07.30 POIの改修に着手 putPoiShape() <=vectorPoiShapesを使う 今のところ、default属性、vectorEffectがまだまともじゃない。さらに、形状の範囲チェックがない。さらに、色テーブルの明示ができない。
// 2013.02.18 文字列の列挙型の情報を色テーブルに割り当てる useColorKeys
// 2013.03.11 SVGMapLvl0用のPOI処理(micrometa2)　外部からPOI symbolのテンプレートを読み込む機能など大拡張
// 2013.03.28 xmlエスケープ(for metadata)
// 2013.08.06 階層コンテナのデータ・処理構造を抜本的に手直し(単純化)
// 2013.10.21 POIのシンボルIDをデータから指定できるようにした　ただし、正の整数のみ
// 2014.02.14 ヒープ効率化により大容量データの処理を可能にする(大幅なアーキテクチャ変更を実施)
// 2014.04.25 -poisymbolidnumber
// 2014.05.12 シンボルサイズが固定でないばあい
// 2015.04.17 -colorkeyを少し改善
// 2016.02.05 -colorkeyを更に少し改善
// 2016.02.26 poiシンボルのサイズを細かく設定できるオプションの拡張　特にdirectpoiに対して
// 2016.03.23 --- マルチスレッド化を開始
// 2016.04.03 rev19a (第一次)マルチスレッド化チャレンジ:FeatureIteratorをスレッド数分稼働させる方法・・効率が出ず失敗 (ものによっては３割とか遅くなった)
// 2016.04.07 rev19b マルチスレッド化第二チャレンジ中:
// 2016.04.08 15%程度の性能向上を確認。この方法（FeatureIterator単一にし、バッファリングしながらジオメトリごとのミクロなスレッドを立て、ミクロに同期処理する方法）を採用して開発を続行する。（現時点ではまだ大量のバグあり）	
// 2016.04.08 actualGenerateMeshLevelオプション (18に先行実装したもの)
// 2016/08/02 外部ファイルからオプション読める機能追加
// 2017.04.05 CSV対応：CSVDataStoreを実装
// 2017.09.15 シンボルの参照IDに元データの文字列カラムの値を設定可能に。加えて２つのカラムの加算(Int,String)値も設定可能に
// 2017.11.10 package化
// 2017.12.27 OSS化　github登録
// 2018.01.26 内蔵の色関係の関数を削除し、SVGMapGetColorUtil(コンプリのShape2ImageSVGMapで使用中)を使用
// 2018.09.21 csv入力での単純なlineString,polygonデータ対応
// 2019.01.24 -layermeta
// 2019.06.14 WKTでエンコードされたgeometryの入ったCSVに対応
// 
// BUG 130806の手直しにより初期分割数が1x1の階層的データが生成できないバグができている。global level tilingを実施するとき(初期レベルの時の初期タイル分割数が1x1になる場合)に大きな問題がある。(データ生成に失敗する) (2014.03 確認 1420,1439行あたり？)  日本だけのデータで-level 0 -limit 100 とか指定すると、limit超えで打ち切っただけのルートデータを生成してしまい、その上でタイル生成を始めていますね・・ 2017.4.19 タイルは生成できるようになっているが、コンテナのLevel0のタイルのファイル名が誤る。
//
// ISSUES:
//   geotoolsの対応リビジョンを上げる・・


import java.io.*;
import java.io.IOException;
import java.util.*;
import java.lang.*;
import java.net.URL;
import java.net.URI;
import java.text.NumberFormat ;
import java.text.DecimalFormat;
import java.util.regex.*;

import java.lang.management.ManagementFactory; // ヒープ監視用 2014.02
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
// import net.sourceforge.sizeof.SizeOf;


import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.*;
import org.opengis.feature.simple.*;

import java.awt.geom.*;

import org.geotools.data.Transaction;
import org.geotools.filter.*;
// import org.geotools.data.vpf.*;
import org.geotools.geometry.jts.* ;
import com.vividsolutions.jts.geom.*;

import com.vividsolutions.jts.simplify.*;

import com.vividsolutions.jts.operation.linemerge.LineMerger;

// import java.security.*;

// for CSV Reader/Exporter 2017.4.3
import org.svgmap.shape2svgmap.cds.CSVDataStore;
import org.svgmap.shape2svgmap.cds.CSVFeatureReader;
import org.svgmap.shape2svgmap.cds.CSVFeatureSource;
import org.geotools.data.store.ContentDataStore;
// import org.geotools.data.AbstractFileDataStore;

// use Executor 2017.4.19
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Shape2SVGMap19 {
	NumberFormat nFmt;
	DecimalFormat tzformat = new DecimalFormat("0.###########");
//	GeoConverter gconv;
	SvgMapAffineTransformer smat;
	int lvl;
	int xySys = 0;
	int datum = GeoConverter.JGD2000;
//	HashMap<Object,String> colorMap; // 2018.1.26 SVGMapGetColorUtil(下記)に移行
	SVGMapGetColorUtil colorUtil;
	String mainColor = "green"; // 線又は塗りの色
	String outlineColor = ""; // ポリゴンの輪郭色
	double opacity = 0.5; // 透明度 
	double POIsize = -1; // 2017/7/14 directpoi且つpoiSizeColが指定されているときだけこの値を元に動的にPOIのサイズが設定される(-1:デフォルトサイズ(putPoiShape))
	
	int layerCol = -1;
	
	int captionFlg = -1;
	int dupCheck = -1;
	int linkFlg = -1;
	int hrefFlg = -1; // 2016.10.31 アンカーは本来hrefがないと無意味なのでその辺の改修 (ただし今のところhref使えない)
//	TreeMap metaIndex = new TreeMap();
	LinkedHashMap<Integer,String> metaIndex = new LinkedHashMap<Integer,String>(); // 指定順で並べたほうが良いかもしれない(2013.3.11)
	Integer[] linkedMetaIndex;
	String[] linkedMetaName;
//	boolean noMetaId = false; // IDが入ると破綻するらしい？ 2017.4.17 svgmaptilestmでマルチスレッド化してIDがスレッド間重複を起こす・・
	boolean noMetaId = true;
	
	double strokeWidth = 0.0; // SVG座標系での線幅(いろいろな条件を加味して設定される) [m]値を初期設定すると変換設定
	double defaultFontSize = 0.0; // 適当に加味して設定される・・
	
	boolean vectorEffect = false; // 拡大しても拡大しない線を使うときtrue(strokeWidthと組です)
	
	
	// 2012/12/28 parseを整理して出てきた初期設定変数群
	boolean outGroup = false; // SVGのグループタグを出力フラグ（ただし、不完全・一重だけ・・・）
	double projCenter = 0; // 基準緯線
	double boundHeight = -100; // SVGデータの高さ ( "-"値の場合は、地理座標の(-boundHeight)倍 )
	String colorProp = ""; // メインの色 #nnnnnn:RGB指定 もしくは属性名: 属性番号
	String olColorProp = ""; // 輪郭線の色
	String symbolProp = ""; // シンボル(POI用)の色
	String dupProp = ""; // 重複図形の抑制 属性名: 属性番号(この属性の値が同じとき抑制)
	String linkProp = ""; // ハイパーリンク設置　属性名：属性番号
	boolean metaEmbed = false; // メタデータ(RDF/XML) 廃止・・
	boolean microMetaEmbed = false; // マイクロメタデータ to be obsoluted...
	boolean microMeta2Embed = false; // マイクロメタデータ2
	double accuracy = 1.0; // 精度 : データ数値の桁数・　 条件次第で線の太さも決定される
	int meshPart = 0; // メッシュ分割数
	int meshLevel = -1; // >=0で、quad treeメッシュ分割をenableに 2013.3.28 : 注意　実際の分割レベルは meshLevel + level になっている
	int actualGenerateMeshLevel = -1; // 実際にグラフィックスが生成されるメッシュレベル(その上まではカラのコンテナにする)
	int maxLevel = 20; // 2017.4.19 -levelオプション時の最大分割レベル(Global Q-tree level)
	String crstProp = ""; // CRSのtransform値を直接指定するとき
	int limitProp = -1; // タイルの最大サイズ(KByte)
	String layerProp = ""; // レイヤ 属性名: 属性番号(この属性の値が同じとき同じレイヤ)
	double simplifyParam = -10.0; // -10のときは単純化しない
	double fixedFont = 0.0; // サイズの固定[px] 0:fixしない 
	double fixedStroke = 0.75; // サイズの固定[px] 0:fixしない (マイナス値:指定した属性番号の値でfix)
	double fixedSymbol = 6.0; // サイズの固定[px] 0:fixしない (マイナス値：度単位で固定)
	double fixedSymbolSub = 0.0; // サイズの固定のためのサブパラメータ
	int directPoi = 0; // シンボルを、defs-useを使わず、直描き（仮設2 2012/07/30) "0"はdisable (-)は-POIシンボル番号, (+)attrNoはattrNoの値によってdirectPOIシンボル番号を設定
	int customPoiType = -1; // 上記、シンボルのID番号をpoiSymbolIdNumberでカスタム指定
	int poiColumn = -1; // poiのシンボル指定のための属性番号(2013/10/21)
	int poiColumn2 = -1; // poiのシンボル指定のための二個目の属性番号
	
	String symbolTemplate = ""; // 2013.3.11 シンボルを外部テンプレートファイルから設定する
	
	boolean noShape = false; // 図形を出さない
	boolean mergeProp = false; // ラインマージ
	boolean showHead = false; // ヘッダの表示
	int densityControl = 0; // タイル密度に応じた表示制御 [px/TILE]
	int bitimageGlobalTileLevel = -1; // 高度なdensityControl表現のための識別変数 2013.3.28(globalTilingでのみ機能する)
	
	
	int colorCol = -1;
	int olColorCol = -1;
	int strokeWCol = -1;
	int POIsizeCol = -1; // 2017/7/14
	int pStep = 0;
	Class colorClass , olColorClass, sizeClass;
	
	boolean isSvgTL = true;
	
	// 文字列を色に割り当てるための列挙データ 2013/02
	String colorKeys ="";
	boolean useColorKeys = false;
	
	// メタデータの名前空間など(いい加減)
	String metaNs = "lm";
	String metaUrl = "http://www.svg-map.org/svgmap/localmetadata/";
	boolean IDset = true;
	int lop=0;
	
	//
	int topCount = -1;
	
	// メモリ消費のチェックを実施する場合はtrue
	boolean heapCheck = false;
	
	// スレッドの詳細パラメータ設定  2016.5.19
	int maxThreads = 4; // スレッドの最大数
	int threadBuffer = -1; // スレッドの命令バッファ数(-1でsvgMapThreadTMのデフォルト値)
	ExecutorService svgMapExecutorService; // for Executor multi thread 2017.4.19

	
	// 連続してdivErrMax階層分割効果がない場合、分割処理を中断する  2016.5.19
	int divErrMax = 3;
	
	// for CSV Support 2017.4.3
	boolean inputCsv = false;
	boolean gZipped = false; // 2017.5.15
	String csvSchemaPath="";
	
	// 2018.1.26 SVGMapGetColorUtil移行に伴う変数
	int colorTable = SVGMapGetColorUtil.HSV;
	int outOfRangeViewMethod = SVGMapGetColorUtil.MARK;
	int colorKeyLength = 2;
	
	// 2019.1.24 レイヤールートコンテナの<metadata>要素に、任意のデータ(含むタグ文字列)を入れるための文字列変数
	String layerMetadata ="";
	
	boolean putRecord = false; // 2021.6.10 レコード番号をdata-recordに記載する
	
	static boolean layerDebug = false;
	
	private static void showHelp(){
		System.out.println("Shape2SVGMap: ShapeをSVGMapに変換します。");
		System.out.println("Copyright 2007-2018 by Satoru Takagi @ KDDI All Rights Reserved.");
		System.out.println("----------");
		System.out.println("java Shape2SVGMap [Options] (input.shp|input.csv) [output.svg]");
		System.out.println("input.(shp|csv) : ソースファイル指定。csvに関しては-csvschena説明参照");
		System.out.println("output.svg : 変換先明示。無い場合拡張子除きソースと同じパスで変換");
		System.out.println("");
		System.out.println("Options   : -optionName (value)");
		System.out.println("-proj     : 正距方位図法の標準緯線を指定");
		System.out.println("            center:地図中央を基準緯線に");
		System.out.println("            値:基準緯線値を指定値に");
		System.out.println("            デフォルト:0 = 赤道");
		System.out.println("-height   : SVG高さの指定");
		System.out.println("            数値：SVGの高さの値を指定。");
		System.out.println("            数値x：地理座標(緯度経度)×[数値]倍");
		System.out.println("            デフォルト:100x :地理座標の100倍値(SVGTで10cmまで表現可)");
		System.out.println("-xy       : XY座標系(日本測地系)の指定");
		System.out.println("            1～19：XY系のときに、その番号を指定[m]単位 ");
		System.out.println("            デフォルト:0 = XY系ではなく緯度経度として扱う");
		System.out.println("            SPECIAL:-1～-19：[mm]単位");
		System.out.println("-color    : 図形の色設定 (ラインの場合は線色、ポリゴンの場合は塗色)");
		System.out.println("            #000000~#ffffff:色を指定 , (noneで色無し)");
		System.out.println("            属性名:指定した属性の値で可変する。属性の型により自動色設定");
		System.out.println("                  文字列: 文字列に応じたランダムな塗り");
		System.out.println("                  数値  : 数値の大きさに応じた塗り(赤：ff：max,00:min)");
		System.out.println("            属性番号:指定した属性番号の値で可変する。属性の型により自動");
		System.out.println("              どんな属性があるかは、-showheadオプションで起動すればわかります。");
		System.out.println("            デフォルト:#00FF00(green) ");
		System.out.println("-numcolor : 数値におけるカラーテーブルの設定 (RED:赤で明度, HSV:H値変化(小:青,大:赤), iHSV(同逆), QUOTA:値毎にできるだけ別色割付)");
		System.out.println("            デフォルト:HSV");
		System.out.println("-numrange : 数値ベース色設定の上限下限設定(２値) 200 1000");
		System.out.println("            デフォルト:実際の値から自動設定");
		System.out.println("-skipoutofrange: 上記の上限下限を超える値をスキップする(deplicate)");
		System.out.println("-outofrange:上限加減を超えた値の処理");
		System.out.println("            デフォルト:グレーに設定");
		System.out.println("            skip (=skipoutofrange)");
		System.out.println("            counterStop 上限下限値に張り付く");
		System.out.println("-strcolor : 文字列におけるカラーテーブルのハッシュキー文字数設定 (1-n)");
		System.out.println("            デフォルト:2");
		System.out.println("-colorkey : 文字列におけるハッシュキー(カラーテーブル)を列挙型で決める（色並び：昇順）");
		System.out.println("            CSVで属性値を列挙する。各属性値の後に#xxxxxxが続く場合は直接色も指定できる");
		System.out.println("            ハッシュキーの文字列長さは全て同じでないとならない。-numcolorはその値から自動設定");
		System.out.println("            強く関係するオプション：色の割付け:-numcolor");
		System.out.println("            例１：属性値１,属性値２,....　例２：属性値１#F08020,属性値２#30D000,....");
		System.out.println("-opacity  : 塗りの場合の透明度設定 (0.0～1.0)");
		System.out.println("            デフォルト:0.5");
		System.out.println("-outline  : ポリゴン輪郭の色設定");
		System.out.println("            #000000~#ffffff:色を指定");
		System.out.println("              (線の太さは自動設定)");
		System.out.println("            デフォルト:null = 輪郭線なし");
		System.out.println("-caption  : 注記の設定");
		System.out.println("            属性名:指定した属性の値を使う");
		System.out.println("            属性番号:指定した属性番号の値を使う");
		System.out.println("            どんな属性があるかは、-showheadオプションで起動すればわかります。");
		System.out.println("-capfix   : 注記の文字サイズを伸縮に依らず固定にする");
		System.out.println("            画面上でのフォントサイズ");
		System.out.println("            p+[属性番号](p4等)：属性番号の値を設定(属性値は数字のみ)");
		System.out.println("-strokefix: 線の幅を伸縮に依らず固定にする");
		System.out.println("            数値 : 画面上での線幅");
		System.out.println("            p+[属性番号]: 指定した属性番号の値を設定(属性値は数字のみ)");
		System.out.println("            デフォルト:0.75 デフォルトで0.75pxに指定");
		System.out.println("-strokew  : 線の幅を伸縮に依存させる(-strokefixと排他)");
		System.out.println("            数値 : 線幅[m]");
		System.out.println("            0 : accuracyの半値を設定");
		System.out.println("-poisize  : シンボル(point,POI)のサイズを指定する(以下の３パターン)");
		System.out.println("            w(,h)    物理シンボルのサイズ(円の場合直径、四角の場合縦横)を指定[degree]");
		System.out.println("            w(,h)m   物理シンボルのサイズを指定[meter]");
		System.out.println("            w(,h)px  画面上でのサイズを指定[px]");
		System.out.println("            w,h はそれぞれ小数点数");
		System.out.println("            attrN    属性番号Nの値から決定。属性は数値である必要。値レンジは-numrangesizeの通り。サイズは画面上サイズとなり-sizerangeの通り。またdirectpoi指定必須");
		System.out.println("            参考: １次メッシュ:1.0,0.666666   ２次メッシュ:0.125,0.083333 ");
		System.out.println("            デフォルト: 6px   w,hで縦横別指定、wのみで縦横同じサイズ指定");
		System.out.println("-numrangesize: poisizeでattrNを設定した時の、その属性値の上限下限設定(２値) 200 1000");
		System.out.println("            デフォルト:実際の値から自動設定");
		System.out.println("-sizerange: minSize maxSize[px]  -poisizeでattrNを設定した時のPOI可変サイズの最小最大サイズ");
		System.out.println("            デフォルト: 3 24");
		System.out.println("-directpoi: POIを直描き");
		System.out.println("            rect, rect2, rect3: POIを四角で描く");
		System.out.println("            diamond, diamond2, diamond3: POIをひし形で描く");
		System.out.println("            triangle, triangle2, triangle3: POIを三角で描く");
		System.out.println("            itriangle, itriangle2, itriangle3: POIを逆三角で描く");
		System.out.println("            上記2,3は横長,縦長図形");
		System.out.println("            属性番号:指定した属性番号の値(値は0..nの整数のみ対応)で切り替える");
		
		System.out.println("            デフォルト: 使用しない");
		
		System.out.println("-poicolumn: POIのシンボルを切り替えるための属性番号 or 属性番号1+属性番号2");
		System.out.println("            指定した属性番号の値：");
		System.out.println("            整数(0..n)の場合：シンボルIDは0:p0...n:pn　が必要");
		System.out.println("            文字列の場合：対応する文字列をIDに持ったシンボルが必要");
		System.out.println("            デフォルト: 使用しない)(0が設定)");
		
		System.out.println("            二つの属性番号がある場合整数では加算した値で評価、文字列では結合した値で評価");
		
		System.out.println("-poisymbol: シンボルテンプレートファイルを使用する。");
		System.out.println("            値: シンボルテンプレートファイルのパス");
		System.out.println("            デフォルト: 使用しない（svg図形で作られた簡単なシンボルを使う）");
		System.out.println("            同ファイルの内容は、svgの<defs>要素下にコピーされる。(下記必要条件の内容チェックはしません)");
		System.out.println("            デフォルトはid=\"p0\"の図形要素が必要。");
		System.out.println("            poiSymbolIdNumber指定の場合は\"p\"+その番号の図形要素が必要");
		System.out.println("            poiSymbolIdNumber指定 or poicolumn(整数タイプ)の場合はid=\"p\"+その番号図形要素が必要");
		System.out.println("            poicolumn(文字列タイプ)の場合は対応する文字列のIDの図形要素が必要");
		System.out.println("        例: <g id=\"p0\">");
		System.out.println("              <image xlink:href=\"mappin.png\" preserveAspectRatio=\"none\" x=\"-8\" y=\"-25\" width=\"19\" height=\"27\"/>");
		System.out.println("            </g>");
		System.out.println("            ...");
		System.out.println("            文字コードはUTF-8限定");
		
		System.out.println("-poiSymbolIdNumber: POIのシンボルID番号を指定");
		System.out.println("            値: 数字");
		System.out.println("            デフォルト: 0 (=\"p0\")");
		
		System.out.println("-noshape  : 図形を配置しない(注記のみ表示するときなどに使用)");
		System.out.println("            [値無し]");
		System.out.println("-accuracy : 精度");
		System.out.println("            [m]単位");
		System.out.println("            デフォルト:1.0[m]");
		System.out.println("-linkTitle: アンカーの設定");
		System.out.println("            属性名:指定した属性の値をxlink:titleに設定・・");
		System.out.println("            属性番号:指定した属性番号の値を使う");
		System.out.println("            どんな属性があるかは、-showheadオプションで起動すればわかります。");
		/** 廃止（一応昔の実装は残してあるが、色々な拡張の結果、正常動作は期待できない）
		System.out.println("-meta:      RDF/XMLメタデータ埋め込み");
		System.out.println("            値無し");
		**/
		/**
		System.out.println("-micrometa: microメタデータ埋め込み");
		System.out.println("            [値無し]：全ての属性を埋め込み");
		System.out.println("            (スペース区切りで複数の)[属性番号]：指定した属性番号の属性を埋込み");
		System.out.println("            (スペース区切りで複数の)[属性番号]=[属性名]：指定した属性名で埋込み");
		**/
		System.out.println("-micrometa2: microメタデータ  埋め込み");
		System.out.println("            [値無し]：全ての属性を埋め込み");
		System.out.println("            (スペース区切りで複数の)[属性番号]：指定した属性番号の属性を埋込み");
		System.out.println("            (スペース区切りで複数の)[属性番号]=[属性名]：指定した属性名で埋込み");
		System.out.println("            最初に指定した属性番号のデータをxlink:title属性にも付与する");
		System.out.println("-layermeta: (\"メタデータ文字列\"||file [path])");
		System.out.println("            メタデータ文字列は、xmlノードとして許される任意の文字列複数のタグ文字列を入れることも可");
		System.out.println("            file の場合はメタデータ挿入したい文字列の入ったテキストファイルのパスを[path]で指定する(ファイル入力の場合はUTF-8)");
		System.out.println("            いずれでも、エスケープなどは自ら行っておく");
		System.out.println("-noid     : micrometaのときid属性を付けない");
		System.out.println("            [値無し]");
		System.out.println("-group    : グループを作成");
		System.out.println("            [値無し]");
		System.out.println("-datum    : 空間参照系を設定 tokyoのみ設定できる");
		System.out.println("            デフォルト:JGD2000(WGS 84と等価)");
		System.out.println("-mesh     : メッシュ分割、コンテナSVGも作成");
		System.out.println("            短辺側の分割数。長辺側の分割数は自動計算");
		System.out.println("            デフォルト:0(分割しない)");
		System.out.println("-level    : 全球quad treeによるタイル分割 (-meshオプションは無視される)");
		System.out.println("            引数：レベル(0...(概ね20まで)) レベルとサイズ(度)の関係は下記");
		System.out.println("            0:360deg,1:180deg,2:90deg,3:45deg,4:22.5deg,5:11.25deg,6:5.625deg..");
		System.out.println("-limit    : 出力タイルサイズのリミッタを設定(単位KBytes(3以上))");
		System.out.println("            デフォルト:約100MB　リミッタを超えると下回るまで再帰的に４分割");
		
		System.out.println("-layer    : 指定した属性(番号又は名前)を元にレイヤ(ファイル)分割する");
		System.out.println("            レイヤのファイル名：属性値を元に設定");		
		System.out.println("            デフォルト:なし");
		System.out.println("-simplify : 図形を簡単化する");
		System.out.println("            単純化パラメータ[度] 0.01ぐらいが適当なところ");
		System.out.println("            (-)値: トポロジを維持しない単純化ルーチンを使用");
		System.out.println("            デフォルト：単純化しない");
		System.out.println("-duplicate: 重複図形を抑制する");
		System.out.println("            属性名: 属性番号: 重複を把握するための属性を指定(ID番号など)");
		System.out.println("            ");
		System.out.println("-linemerge: 線をマージしてデータサイズを圧縮する");
		System.out.println("            [値無し]");
		System.out.println("            ");
		System.out.println("-showtile : デバッグ・確認用(タイル境界線を出力する)");
		System.out.println("            [値無し]");
		System.out.println("-charset  : 文字コードを設定 UTF-8のみ設定可能。文字化け対策に利用可");
		System.out.println("            デフォルト:ShiftJIS");
//		System.out.println("-pstep    : 階層分割のステップ(隠しコマンド) >=2 ");
		System.out.println("-showhead : ヘッダの表示");
		System.out.println("-csvschema: データのスキーマファイルを指定");
		System.out.println(" CSVファイルの説明・制約:");
		System.out.println("    Point,LineString(ただし途切れない線),Polygon(ただしドーナツも分裂もないもの),WKTエンコードgeometryをサポート。他の属性は基本的にすべて文字列として扱い。");
		System.out.println("    1行目はスキーマ行、以降の行から続くデータの属性名を指定する(スキーマもCSV)");
		System.out.println("    同属性名として緯度経度を示すマジックワード(後述)を設置して座標のカラムであることを指定する。");
		System.out.println("    スキーマ行なしに、-csvschemaでスキーマを別ファイル化することも可能。その場合1行のファイルで表現し、末尾に改行が必要。");
		System.out.println("    Point: 任意のカラムに座標の属性名を示すマジックワード('緯度','経度','latitude','longitude','lat','lng' 英文字は大小文字の区別なし)を指定する。");
		System.out.println("    LineString:スキーマ行CSVの末尾のカラムにマジックワード'latitude:line','longitude:line'(latitudeなどの代わりに先述の文字列も使用可)のペアを設定する必要がある。実データは任意の座標を末尾に羅列して表現する。");
		System.out.println("    Polygon:同上 マジックワード'latitude:polygon','longitude:polygon'");
		System.out.println("    度分秒表現:緯度経度マジックワード後にDMSもしくはDMを追記する'経度DMS','latitudeDM:polygon'など、DDDMMSS.SSSの,DDMM.MMのような表現を扱える");
		System.out.println("    WKT:任意のカラムにWKTを示すマジックワード'WKT'を指定。WKTがカンマを含む場合ダブルクォーテーションでWKTカラムのエスケープが必要");
		System.out.println("-top      : 頭からｎ個のデータだけを出力する。　値：個数");
//		System.out.println("-test     : テスト用"); // ほとんど意味ない
//		System.out.println("-transform: CRSのtransform 6値を直接指定(csv)");
//		System.out.println("-dataver  : コンテンツバージョンを指定(animationかimageかだけの相違)");
//		System.out.println("            デフォルト:1.2  1.1が指定できる");
//		System.out.println("-heapCheck: 消費メモリチェック用"); 
		// 隠し機能にしておく
		System.out.println("-densityControl: タイル密度に応じた表示制御 [px/TILE]");
		System.out.println("            デフォルト:[値無し]");
		System.out.println("-lowResImage: 小縮尺時(densitioControl敷居値以下)でbitImageTileを代替表示");
		System.out.println("               -levelと-densityControl設定必要。bitImageTileはShape2ImageSVGMap使用");
		System.out.println("-actualGenerateMeshLevel: 実際に生成されるレベルをこれ以上とする(-level併用)");
		System.out.println("-maxLevel : このレベルを超えた分割はキャンセルする: -levelと併用しているときは全球quad treeベースのレベル (default:20)");
		System.out.println("-maxThreads: 最大スレッド数 デフォルト：4");
		System.out.println("-threadBuffer: 各スレッドが受け取る描画命令バッファ数 デフォルト：16384");
		System.out.println("-divErrMax: 分割効果がこの数分ない場合、分割処理中断 デフォルト：3");
		System.out.println("-optionFile: オプションファイルを指定する。(入出力ファイルはこのファイル中では指定できない)コマンドラインでのオプションと同じ書式(ただし改行許可)　こちらが優先される");
		System.out.println("-putRecord: data-record属性に、レコード番号を記載する");
		
		
		
		
		System.out.println("            ");
	}
	
	public static String[] getOptionFile( String[] mainArgs) {
		// -optionFile オプションがある場合、それを優先する
		String optFilePath = null;
		for ( int i = 0 ; i < mainArgs.length ; i++ ){
			if ( mainArgs[i].toLowerCase().equals("-optionfile")){
				optFilePath = mainArgs[i+1];
				break;
			}
		}
		
		if ( optFilePath != null ){
			String[] ans=null;
			try {
				optionsReader or = new optionsReader( new File(optFilePath) );
				String[] opts = or.getOptions();
				
				if ( mainArgs[ mainArgs.length - 1 ].indexOf(".svg") > 0 ){
					ans = new String[ opts.length + 2 ];
					ans[ opts.length + 1 ] = mainArgs[mainArgs.length -1]; // svgPath
					ans[ opts.length ] = mainArgs[mainArgs.length - 2]; // shpPath
				} else {
					ans = new String[ opts.length + 1 ];
					ans[ opts.length ] = mainArgs[mainArgs.length - 1]; // shpPath
				}
				
				for ( int i = 0 ; i < opts.length ; i++ ){
					ans[i] = opts[i];
				}
				
			} catch ( Exception e ){
				System.out.println("-optionFile オプションが誤っています");
				showHelp();
				System.exit(0);
			}
			return ( ans );
		} else {
			return ( mainArgs );
		}
		
	}
	
	public static void main(String[] args) {
		
		
		args = getOptionFile( args );
		
		double boundHeight = -100; // 2010/10/29 SVG1.2Tinyの制約から、10cm精度を出すために100倍する
		// SVG1.2Tinyでは -32,767.9999 to +32,767.9999 (浮動小数点表現(E+xx)は可,小数点以下4桁)
		int params = 0;
		String outfile ="";
		String infile = "";
		
		boolean lowResImage = false;
		
		Shape2SVGMap19 s2sm = new Shape2SVGMap19();
		try {
			if(args.length < 1 || args[args.length -1 ].indexOf("-") == 0 ){
				showHelp();
				System.out.println("入力ファイルが指定されていません");
				throw new IOException();
//				System.exit(0);
			}
			
			if ( args[ args.length - 1 ].indexOf(".svg") > 0 && args.length > 1 ){
				infile = args[args.length - 2];
				outfile = args[args.length - 1];
				params = args.length - 2;
			} else {
				infile = args[args.length - 1];
//				outfile = args[args.length - 1] + ".svg";
				try{
					outfile = (args[args.length - 1]).substring(0 , (args[args.length - 1]).lastIndexOf(".")) + ".svg";
				} catch ( Exception e ){
					System.out.println("入力ファイルが指定されていません");
					throw new IOException();
				}
//				System.out.println("InputFile:" + infile + "  OutputFile:" + outfile);
				params = args.length - 1;
			}
			
			if ( infile.endsWith(".csv")){
				s2sm.inputCsv = true;
				System.out.println("CSV input");
			} else if ( infile.endsWith(".gz")){
				s2sm.inputCsv = true;
				s2sm.gZipped = true;
				System.out.println("gz_CSV input");
			} else {
				System.out.println("Shapefile input");
			}
			
			for (int i = 0; i < params; ++i) {
				if ( args[i].toLowerCase().equals("-proj")){
					++i;
					if (args[i].equals("center")){
						s2sm.projCenter = 999.0;
					} else {
						s2sm.projCenter = Double.parseDouble(args[i]);
					}
				} else if ( args[i].toLowerCase().equals("-height")){
					++i;
					if ( args[i].indexOf("x") > 0 ){
						s2sm.boundHeight = - Double.parseDouble(args[i].substring( 0 , args[i].indexOf("x") ));
//						System.out.println("xx:" + s2sm.boundHeight );
					} else {
						s2sm.boundHeight = Integer.parseInt(args[i]);
					}
				} else if ( args[i].toLowerCase().equals("-xy")){
					++i;
					s2sm.xySys = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-color")){
					++i;
					s2sm.colorProp = args[i];
				} else if ( args[i].toLowerCase().equals("-numcolor")){
					++i;
					if (args[i].toLowerCase().equals("hsv")){
						s2sm.colorTable = SVGMapGetColorUtil.HSV;
					} else if (args[i].toLowerCase().equals("ihsv")){
						s2sm.colorTable = SVGMapGetColorUtil.iHSV;
					} else if (args[i].toLowerCase().equals("red")){
						s2sm.colorTable = SVGMapGetColorUtil.RED;
					} else if (args[i].toLowerCase().equals("quota")){
						s2sm.colorTable = SVGMapGetColorUtil.QUOTA;
					}
				} else if ( args[i].toLowerCase().equals("-numrange")){
					++i;
					double p1,p2;
					p1 = Double.parseDouble(args[i]);
					++i;
					p2 = Double.parseDouble(args[i]);
					if ( p1 < p2 ){
						s2sm.mainAttrMax = p2;
						s2sm.mainAttrMin = p1;
					} else {
						s2sm.mainAttrMax = p1;
						s2sm.mainAttrMin = p2;
					}
				} else if ( args[i].toLowerCase().equals("-numrangesize")){
					++i;
					double p1,p2;
					p1 = Double.parseDouble(args[i]);
					++i;
					p2 = Double.parseDouble(args[i]);
					if ( p1 < p2 ){
						s2sm.sizeAttrMax = p2;
						s2sm.sizeAttrMin = p1;
					} else {
						s2sm.sizeAttrMax = p1;
						s2sm.sizeAttrMin = p2;
					}
				} else if ( args[i].toLowerCase().equals("-sizerange")){
					++i;
					double p1,p2;
					p1 = Double.parseDouble(args[i]);
					++i;
					p2 = Double.parseDouble(args[i]);
					if ( p1 < p2 ){
						s2sm.sizeRangeMax = p2;
						s2sm.sizeRangeMin = p1;
					} else {
						s2sm.sizeRangeMax = p1;
						s2sm.sizeRangeMin = p2;
					}
				} else if ( args[i].toLowerCase().equals("-skipoutofrange")){
//					s2sm.nullColor = "";
					s2sm.outOfRangeViewMethod = SVGMapGetColorUtil.SKIP;
				} else if ( args[i].toLowerCase().equals("-outofrange")){
					++i;
					if (args[i].toLowerCase().equals("skip")){
//						s2sm.nullColor = "";
						s2sm.outOfRangeViewMethod = SVGMapGetColorUtil.SKIP;
					} else if (args[i].toLowerCase().equals("counterstop")) {
//						s2sm.counterStop = true;
						s2sm.outOfRangeViewMethod = SVGMapGetColorUtil.COUNTER_STOP;
					} else {
						// set null color
					}
				} else if ( args[i].toLowerCase().equals("-strcolor")){
					++i;
					s2sm.colorKeyLength = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-colorkey")){
					++i;
					s2sm.colorKeys = args[i];
				} else if ( args[i].toLowerCase().equals("-outline")){
					++i;
					s2sm.olColorProp = args[i];
				} else if ( args[i].toLowerCase().equals("-caption")){
					++i;
					s2sm.symbolProp = args[i];
				} else if ( args[i].toLowerCase().equals("-duplicate")){
					++i;
					s2sm.dupProp = args[i];
				} else if ( args[i].toLowerCase().equals("-accuracy")){
					++i;
					s2sm.accuracy = Double.parseDouble(args[i]);
					if ( s2sm.accuracy <= 0.0 ) {
						s2sm.accuracy = 1.0;
					}
				} else if ( args[i].toLowerCase().equals("-linktitle")){
					++i;
					s2sm.linkProp = args[i];
				} else if ( args[i].toLowerCase().equals("-top")){
					++i;
					s2sm.topCount = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-meta")){ // サポート中止（実装あるが動作未確認）
					s2sm.metaEmbed = true;
				} else if ( args[i].toLowerCase().equals("-micrometa")){
					s2sm.microMetaEmbed = true;
					
					while ( args[i+1].indexOf("-") == -1 && i < params - 1 ){
						++i;
						if ( args[i].indexOf("=") == -1 ){
							s2sm.metaIndex.put(new Integer(args[i]) , "" );
						} else {
							s2sm.metaIndex.put(new Integer(args[i].substring(0,args[i].indexOf("="))), args[i].substring(args[i].indexOf("=")+1));
						}
						
					}
					System.out.println( "Micrometa MetaIndex:" + s2sm.metaIndex );
				} else if ( args[i].toLowerCase().equals("-micrometa2")){
					s2sm.microMeta2Embed = true;
					
					while ( args[i+1].indexOf("-") == -1 && i < params - 1 ){
						++i;
						if ( args[i].indexOf("=") == -1 ){
							s2sm.metaIndex.put(new Integer(args[i]) , "" );
						} else {
							s2sm.metaIndex.put(new Integer(args[i].substring(0,args[i].indexOf("="))), args[i].substring(args[i].indexOf("=")+1));
						}
						
					}
					System.out.println( "Micrometa\"2\" MetaIndex:" + s2sm.metaIndex );
				} else if ( args[i].toLowerCase().equals("-layermeta")){
					++i;
					if ( args[i].toLowerCase().equals("file")){
						++i;
						SVGMapSymbolTemplate lm = new SVGMapSymbolTemplate(); // 目的が違うがSVGMapSymbolTemplate流用します・・・ 2019.1.24
						lm.readSymbolFile(args[i]);
						s2sm.layerMetadata = lm.symbolFile;
					} else {
						s2sm.layerMetadata = args[i];
					}
				} else if ( args[i].toLowerCase().equals("-noshape")){
					s2sm.noShape = true;
				} else if ( args[i].toLowerCase().equals("-noid")){
					s2sm.noMetaId = true;
				} else if ( args[i].toLowerCase().equals("-group")){
					s2sm.outGroup = true;
				} else if ( args[i].toLowerCase().equals("-mesh")){
					++i;
					s2sm.meshPart = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-level")){
					++i;
					s2sm.meshLevel = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-actualgeneratemeshlevel")){
					++i;
					s2sm.actualGenerateMeshLevel = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-maxlevel")){
					++i;
					s2sm.maxLevel = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-lowresimage")){
					lowResImage = true;
				} else if ( args[i].toLowerCase().equals("-showtile")){
					layerDebug = true;
				} else if ( args[i].toLowerCase().equals("-datum")){
					++i;
					
					if ( args[i].toLowerCase().equals("tokyo") || args[i].toLowerCase().equals("bessel")){
						s2sm.datum = GeoConverter.BESSEL;
					}
				} else if ( args[i].toLowerCase().equals("-transform")){
					++i;
					s2sm.crstProp = args[i];
				} else if ( args[i].toLowerCase().equals("-dataver")){
					++i;
					if ( args[i].equals("1.1")){
						s2sm.isSvgTL =false;
						System.out.println("use old format");
					}
				} else if ( args[i].toLowerCase().equals("-limit")){
					++i;
					s2sm.limitProp = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-layer")){
					++i;
					s2sm.layerProp = args[i];
				} else if ( args[i].toLowerCase().equals("-opacity")){
					++i;
					s2sm.opacity = Double.parseDouble(args[i]);
					if ( s2sm.opacity < 0.0 || s2sm.opacity > 1.0){
						s2sm.opacity = 0.5;
					}
				} else if ( args[i].toLowerCase().equals("-simplify")){
					++i;
					s2sm.simplifyParam = Double.parseDouble(args[i]);
					if ( s2sm.simplifyParam < -10.0 || s2sm.simplifyParam > 10.0){
						s2sm.simplifyParam = 0.01;
					}
				} else if ( args[i].toLowerCase().equals("-capfix")){
					++i;
					if ( args[i].indexOf("p") == 0 ){
						s2sm.fixedFont = -Double.parseDouble(args[i].substring(1));
						System.out.println("capFix by Prop" + s2sm.fixedFont );
					} else {
						s2sm.fixedFont = Double.parseDouble(args[i]);
						if ( s2sm.fixedFont > 100.0){
							s2sm.fixedFont = 0.0;
						}
					}
				} else if ( args[i].toLowerCase().equals("-strokefix")){
					++i;
					if ( args[i].indexOf("p") == 0 ){
						s2sm.fixedStroke  = -Integer.parseInt(args[i].substring(1));
					} else {
						s2sm.fixedStroke = Double.parseDouble(args[i]);
						if ( s2sm.fixedStroke < 0 || s2sm.fixedStroke > 50.0){
							s2sm.fixedStroke = 0.0;
						}
					}
				} else if ( args[i].toLowerCase().equals("-strokew")){
					++i;
					s2sm.fixedStroke = 0.0;
					s2sm.strokeWidth = Double.parseDouble(args[i]);
					if ( s2sm.strokeWidth < 0 ){
						s2sm.strokeWidth = 0.0;
					}
				} else if ( args[i].toLowerCase().equals("-poisize")){
					++i;
					String ssProp = args[i];
					if (ssProp.indexOf("attr")==0){
						s2sm.POIsizeCol = Integer.parseInt(ssProp.substring(4));
						System.out.println("Set POI size based on attr:"+s2sm.POIsizeCol);
					} else if (ssProp.indexOf("px")>0){
						if (ssProp.indexOf(",")>0){
							s2sm.fixedSymbol = Double.parseDouble(ssProp.substring(0,ssProp.indexOf(",")));
							s2sm.fixedSymbolSub = Double.parseDouble(ssProp.substring(ssProp.indexOf(",")+1,ssProp.indexOf("px")));
						} else {
							s2sm.fixedSymbol = Double.parseDouble(ssProp.substring(0,ssProp.indexOf("px")));
							s2sm.fixedSymbolSub = s2sm.fixedSymbol;
						}
						if ( s2sm.fixedSymbol < 0 || s2sm.fixedSymbol > 50.0){
							s2sm.fixedSymbol = 6.0;
							s2sm.fixedSymbolSub = 6.0;
						}
						System.out.println("sizing poi:" + (s2sm.fixedSymbol)+"px");
						if (ssProp.indexOf(",")>0){
							System.out.println("sizing poi(height):" + (s2sm.fixedSymbolSub)+"px");
						}
					} else if (ssProp.indexOf("m")>0){
						// メートルのサイズでシンボルを固定させる.. 処理がいい加減・・
						if (ssProp.indexOf(",")>0){
							s2sm.fixedSymbol = (- Double.parseDouble(ssProp.substring(0,ssProp.indexOf(",")))) * 360 / 40000000;
							s2sm.fixedSymbolSub = (- Double.parseDouble(ssProp.substring(ssProp.indexOf(",")+1,ssProp.indexOf("m")))) * 360 / 40000000;
						} else {
							s2sm.fixedSymbol = (- Double.parseDouble(ssProp.substring(0,ssProp.indexOf("m")))) * 360 / 40000000;
							s2sm.fixedSymbolSub = s2sm.fixedSymbol;
						}
						
						System.out.println("sizing poi:" + ((-s2sm.fixedSymbol) * 40000000 / 360 )+"meters :"+(-s2sm.fixedSymbol)+"degrees");
						if ( ssProp.indexOf(",")>0){
							System.out.println("sizing(height) poi:" + ((-s2sm.fixedSymbolSub) * 40000000 / 360 )+"meters :"+(-s2sm.fixedSymbolSub)+"degrees");
						}
					} else {
						if (ssProp.indexOf(",")>0){
							s2sm.fixedSymbol = (- Double.parseDouble(ssProp.substring(0,ssProp.indexOf(","))));
							s2sm.fixedSymbolSub = (- Double.parseDouble(ssProp.substring(ssProp.indexOf(",")+1)));
						} else {
							s2sm.fixedSymbol = - Double.parseDouble(ssProp);
							s2sm.fixedSymbolSub = s2sm.fixedSymbol;
						}
						System.out.println("sizing poi:" + (-s2sm.fixedSymbol)+"degrees");
						if (ssProp.indexOf(",")>0){
							System.out.println("sizing poi(height):" + (-s2sm.fixedSymbolSub)+"degrees");
						}
					}
				} else if ( args[i].toLowerCase().equals("-directpoi")){ // 2012.7.9(仮設)
					++i;
					if (args[i].toLowerCase().indexOf("rect") >= 0 && args[i].toLowerCase().indexOf("3") >= 0 ){
						s2sm.directPoi = -9; // (-)vectorPoishape番号 ( 0は使えない・・・1は使えるようにした(2016.2.26) )
					} else if (args[i].toLowerCase().indexOf("rect") >= 0 && args[i].toLowerCase().indexOf("2") >= 0 ){
						s2sm.directPoi = -5;
					} else if (args[i].toLowerCase().indexOf("rect") >= 0 ){
						s2sm.directPoi = -1;
					} else if (args[i].toLowerCase().indexOf("dia") >= 0 && args[i].toLowerCase().indexOf("3") >= 0  ){
						s2sm.directPoi = -10;
					} else if (args[i].toLowerCase().indexOf("dia") >= 0 && args[i].toLowerCase().indexOf("2") >= 0  ){
						s2sm.directPoi = -6;
					} else if (args[i].toLowerCase().indexOf("dia") >= 0   ){
						s2sm.directPoi = -2;
					} else if (args[i].toLowerCase().indexOf("itr") >= 0 && args[i].toLowerCase().indexOf("3") >= 0  ){
						s2sm.directPoi = -12;
					} else if (args[i].toLowerCase().indexOf("itr") >= 0 && args[i].toLowerCase().indexOf("2") >= 0  ){
						s2sm.directPoi = -8;
					} else if (args[i].toLowerCase().indexOf("itr") >= 0  ){
						s2sm.directPoi = -4;
					} else if (args[i].toLowerCase().indexOf("tri") >= 0 && args[i].toLowerCase().indexOf("3") >= 0  ){
						s2sm.directPoi = -11;
					} else if (args[i].toLowerCase().indexOf("tri") >= 0 && args[i].toLowerCase().indexOf("2") >= 0  ){
						s2sm.directPoi = -7;
					} else if (args[i].toLowerCase().indexOf("tri") >= 0  ){
						s2sm.directPoi = -3;
					} else {
						s2sm.directPoi = Math.abs(Integer.parseInt(args[i]));
					}
					
					
					System.out.println("DirectPoi:" + s2sm.directPoi );
				} else if ( args[i].toLowerCase().equals("-poicolumn")){ // 2013.10.21
					++i;
					if ( args[i].indexOf("+") >0 ){
						String[] pcol = args[i].split("\\+");
						s2sm.poiColumn = Integer.parseInt(pcol[0]);
						s2sm.poiColumn2 = Integer.parseInt(pcol[1]);
						System.out.println("PoiColumn:" + s2sm.poiColumn + " + " + s2sm.poiColumn2 );
					} else {
						s2sm.poiColumn = Integer.parseInt(args[i]);
						System.out.println("PoiColumn:" + s2sm.poiColumn );
					}
				} else if ( args[i].toLowerCase().equals("-poisymbol")){ // 2012.7.9(仮設)
					++i;
					SVGMapSymbolTemplate tp = new SVGMapSymbolTemplate();
					tp.readSymbolFile( args[i] );
					s2sm.symbolTemplate = tp.symbolFile;
					System.out.println("use externalPoiSymbol:");
					System.out.println(tp.symbolFile);
				} else if ( args[i].toLowerCase().equals("-poisymbolidnumber")){ // 2014.4.25 from DRS req.
					++i;
					s2sm.customPoiType = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-linemerge")){
					s2sm.mergeProp = true;
				} else if ( args[i].toLowerCase().equals("-charset")){
					++i;
					if ( (args[i].toUpperCase()).equals("UTF-8")){
						s2sm.strIsSJIS = false;
					}
				} else if ( args[i].toLowerCase().equals("-showhead")){
					s2sm.showHead = true;
				/**
				} else if ( args[i].toLowerCase().equals("-test")){
					System.out.println("TEST");
					s2sm.test();
					throw new IOException();
				**/
				} else if ( args[i].toLowerCase().equals("-heapcheck")){
					s2sm.heapCheck = true;
				} else if ( args[i].toLowerCase().equals("-pstep")){
					++i;
					s2sm.pStep = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-densitycontrol")){
					++i;
					s2sm.densityControl = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-maxthreads")){
					++i;
					s2sm.maxThreads = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-threadbuffer")){
					++i;
					s2sm.threadBuffer = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-diverrmax")){
					++i;
					if (  Integer.parseInt(args[i]) > 1 ){
						s2sm.divErrMax = Integer.parseInt(args[i]);
					}
				} else if ( args[i].toLowerCase().equals("-csvschema")){ // add 2017.04
					++i;
					s2sm.csvSchemaPath = args[i];
					System.out.println("Schema Path for CSV file: " + s2sm.csvSchemaPath);
				} else if ( args[i].toLowerCase().equals("-putrecord")){ // add 2021.06
					++i;
					s2sm.putRecord=true;
					System.out.println("Put record number on data-record attr");
				} else {
					showHelp();
					System.out.println("存在しないオプション\"" + args[i] + "\"が指定されました。");
					throw new IOException();
				}
			}
			
			if ( s2sm.POIsizeCol !=-1 && s2sm.directPoi == 0 ){
				System.out.println("-directpoiが設定されないまま、-poisize attr"+ s2sm.POIsizeCol+" が指定されました　終了します");
				throw new IOException();
			}
			
			// 2013.3.28 高度なdensity controle機能を拡張する！！(low resをbitimageで代替する機能)
			if ( lowResImage == true){
				if ( s2sm.meshLevel >= 0 && s2sm.densityControl > 0 ){
					System.out.println("-lowresimageを有効にします。 base level:"+s2sm.meshLevel);
					layerDebug = true;
					s2sm.bitimageGlobalTileLevel = s2sm.meshLevel;
				} else {
					System.out.println("-lowresimageは無効です！ : -levelと-densityControlを設定してください");
//					showHelp();
					throw new IOException();
				}
			}
			
			// 変換の本体機能を呼び出す！！！
			s2sm.parse( infile ,  outfile );
			
			System.out.println("Finished...");
			
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
	
	
	public void parse( String infile , String outfile ) throws Exception {
		MemoryUsage usage = null;
		if ( heapCheck ){
			MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
			usage = mbean.getHeapMemoryUsage();
			System.out.printf("最大サイズ：%10d%n", usage.getMax());
			System.out.printf("使用サイズ：%10d%n", usage.getUsed());
//			SizeOf.skipStaticField(true); // 初期化コード 
//			SizeOf.setMinSizeToLog(1000000); // 初期化コード 
		}
		
		
//		System.out.println("Java Version:" + System.getProperty("java.version"));
//		Provider sunJceProvider = java.security.Security.getProvider("SunJCE"); 
//		System.out.println("Java Security:" + sunJceProvider.getInfo());
		
		svgMapExecutorService = Executors.newFixedThreadPool(maxThreads);
		System.out.println("Thread Pool Size:"+maxThreads);
		
		SvgMapTilesTM sm , sm0;
		double maxX, minX, maxY, minY;
//		gconv = new GeoConverter(GeoConverter.Tky2JGD); // Bessel->WGSをTKY2JGDに
//		gconv.tiny = true; // WGS<>Bessel変換を簡易版に
		
		if ( microMetaEmbed && microMeta2Embed ){
			microMetaEmbed = false;
		}
		if ( !metaEmbed && ! microMetaEmbed && !microMeta2Embed){
			lineMerge = mergeProp;
		} else {
			lineMerge = false;
		}
		
		// 色テーブルの作成  SVGMapGetColorUtilに移行
		/**
		colorMap = new HashMap<Object,String>();
		colorMap.put("default" , "green");
		
		if ( colorKeys != ""){
			initColorKeyEnum(); // add 2013.02
		}
		**/
		
		// 図形単純化エンジンの組み込み(JTSを利用)
		boolean simplify = false ;
		boolean TopoPresSimp = true;
		Object simplifier;
		DouglasPeuckerSimplifier Dsimplifier = new DouglasPeuckerSimplifier(null);
		TopologyPreservingSimplifier Tsimplifier = new TopologyPreservingSimplifier( null );
		
		if ( simplifyParam  > 0.0 && simplifyParam < 10.0 ){
			simplify = true;
			TopoPresSimp = true;
		} else if ( simplifyParam < 0.0 && simplifyParam > -10.0 ){
			simplify = true;
			simplifyParam = - simplifyParam;
			TopoPresSimp = false;
		}
		
		
		//--------------------------------
		// リーダー部初期化
		//--------------------------------
		/**
		//入力データストア作成
		ShapefileDataStore readStore = new ShapefileDataStore(shapeURL);
		
		int tLeng = (readStore.getTypeNames()).length;
//		for ( int i = 0 ; i < tLeng ; i++){
//			System.out.println("No." + i + " TypeName:"+ readStore.getTypeNames()[i] );
//		}
		//フィーチャーソース取得
		FeatureSource<SimpleFeatureType, SimpleFeature>  source = readStore.getFeatureSource();
		**/
		
		CSVDataStore cds =null;
		ShapefileDataStore sds = null;
		FeatureSource<SimpleFeatureType, SimpleFeature> source = null;
		if ( inputCsv ){
			String charset ="MS932";
			if ( !strIsSJIS ){
				charset = "UTF-8";
				strIsSJIS = true; // CSVの時は、この設定後は、CDS側でUTFでもすべてSJISに吸収される
			}
			// CSVファイルを読み込む 
			cds =null;
			if ( csvSchemaPath =="" ){
				cds = new CSVDataStore( new File(infile), gZipped , charset);
			} else {
				cds = new CSVDataStore( new File(infile), new File(csvSchemaPath), gZipped , charset);
			}
			//フィーチャーソース取得
			source = cds.getFeatureSource(cds.getNames().get(0));
		} else {
			//ロードするＳｈａｐｅ形式ファイル
			URL shapeURL = (new File(infile)).toURI().toURL();
			sds = new ShapefileDataStore(shapeURL); // cast to ShapefileDataStore
			//フィーチャーソース取得
			source = sds.getFeatureSource();
		}
		
		//入力カラム情報の取得
		SimpleFeatureType readFT = source.getSchema();
		
		int nameCol = -1;
		Envelope env = source.getBounds();
//		System.out.println("source-bounds:" + env );
//		System.out.println("BBX:" + env.getMaxX() + ":" + env.getMaxY() + ":" + env.getMinX() + ":" + env.getMinY() );
//		System.out.println("--:" + ( Math.abs(env.getMaxX()) > 300 ) + "::" + Math.abs(env.getMaxX()) );
		if ( xySys == 0 ){
			if ( Math.abs(env.getMaxX()) > 300 || Math.abs(env.getMinX()) > 300 ||
				Math.abs(env.getMaxY()) > 300 || Math.abs(env.getMinY()) > 300 ||
				( env.getMaxX() - env.getMinX() ) > 1000 || ( env.getMaxY() - env.getMinY() ) > 500 ){
				// 領域情報がおかしい・・・
				System.out.println("BOUNDS ERROR BBox:" + env );
				env = getFSExtent( source.getFeatures() );
//				System.out.println("GET ERR-bounds:" + env );
			}
		}
		
		
		// アフィンパラメータ設定
		smat = new SvgMapAffineTransformer( datum , xySys );
//		g2s = new Transform ( 1.0 , 0.0 , 0.0 , -1.0 , 0.0 , 0.0 ); // とりあえずね・・
		
		if ( crstProp.length() > 0 ){
			int i = 0;
			double[] tf = new double[6];
			StringTokenizer st = new StringTokenizer(crstProp , ",");
			while(st.hasMoreTokens()) {
				tf[i] = Double.parseDouble(st.nextToken());
				++i;
			}
			if ( i == 6 ){
				smat.g2s.a = tf[0];
				smat.g2s.b = tf[1];
				smat.g2s.c = tf[2];
				smat.g2s.d = tf[3];
				smat.g2s.e = tf[4];
				smat.g2s.f = tf[5];
			}
		}
		
		Envelope wgsEnv = smat.getWgsBounds( env  );
		maxX = wgsEnv.getMaxX() ;
		minX = wgsEnv.getMinX() ;
		maxY = wgsEnv.getMaxY() ;
		minY = wgsEnv.getMinY() ;
		
		// CRSのTransformを計算
		if ( boundHeight > 0 ) {
			smat.g2s.d = - boundHeight / ( maxY - minY );
			smat.g2s.a = - smat.g2s.d;
		} else if ( boundHeight < 0 ){ // 緯度経度に対する倍数で指定 2010/10
			smat.g2s.a = - boundHeight;
			smat.g2s.d = boundHeight;
		}
		
		if ( projCenter == 999.0 ){
			projCenter = ( maxY + minY ) / 2.0;
		}
		smat.g2s.a = smat.g2s.a * Math.cos( projCenter * Math.PI / 180.0 );
		
		if ( boundHeight == 0 && projCenter == 0 ){
			// デフォルトの場合は SVG原点調整なし
		} else if ( boundHeight < 0 && projCenter == 0 ){
			// 緯度経度の単純倍数の場合　同上
		} else if ( boundHeight < 0 && projCenter != 0 ){
			// 緯度経度の単純倍数だが、標準緯線を指定している場合
			// SVG原点をデータの中心座標にする // added 2012.07.26
			smat.g2s.e = -smat.g2s.a * ((minX+maxX)/2.0);
			smat.g2s.f = -smat.g2s.d * ((minY+maxY)/2.0);
			
		} else {
			// SVG原点をデータの左上にする
			smat.g2s.e = -smat.g2s.a * minX;
			smat.g2s.f = -smat.g2s.d * maxY;
		}
		
		// 桁数と線幅を設定
		setNumberFormat(smat.g2s.d , accuracy);
		if ( strokeWidth == 0.0 ){
			strokeWidth = -smat.g2s.d * accuracy / (2.0 * 111111.1); // 精度の半分の線幅
		} else {
			strokeWidth = -smat.g2s.d * strokeWidth / (111111.1); // 予め入っていたstrokeWidthを[m]と想定し、変換
		}
		
		if ( fixedStroke > 0 ){ // 伸縮に選らない線幅のオプションのとき
			strokeWidth = fixedStroke;
			vectorEffect = true;
		} else if ( fixedStroke < 0 ){ // 属性番号によるとき
			strokeWidth = 0;
			vectorEffect = true;
			strokeWCol = - (int)fixedStroke;
			if ( strokeWCol > readFT.getAttributeCount() ){
				strokeWCol = -1;
			}
		}
		
		
		// POIテーブルを生成 2016.2.26 ここに移動
		vectorPoiShapes vps = null;
		if ( fixedSymbol > 0 ){
			vps = new vectorPoiShapes(fixedSymbol, fixedSymbolSub , nFmt);
		} else {
			vps = new vectorPoiShapes(Math.abs(smat.g2s.d) * ( - fixedSymbol ), Math.abs(smat.g2s.d) * ( - fixedSymbolSub ) , nFmt ) ;
		}
		
		
		// SVG のビューボックスを決めている。 とりあえず(レイヤ分離すると、レイヤごとに違うboundsがある場合がある？)
		double origX , origY , cWidth , cHeight , fontSize;
		boolean abs = false; // フォントのサイズを固定するときのフラグ
		int fontSizeCol = 0;
		
		Envelope svgBounds = smat.getSvgBoundsW( wgsEnv  );
		origX =   svgBounds.getMinX();
		origY =   svgBounds.getMinY();
		cWidth =  svgBounds.getWidth();
		cHeight = svgBounds.getHeight();
		
		int hPart = 1;
		int wPart = 1;
		// 分割数指定によるタイル分割のばあい
		if ( limitProp > 2 && meshPart == 0){ // 2013.8のBUGを見えなくするため・・・
			meshPart = 2;
		}
		if (meshPart > 0 ){
			if (cWidth > cHeight){
				hPart = meshPart;
				wPart = (int)( (meshPart * cWidth) / cHeight);
			} else {
				wPart = meshPart;
				hPart = (int)( (meshPart * cHeight) / cWidth);
			}
			System.out.println("hP:" + hPart + " wP:" + wPart  );
		}
		
		// globalTileモード用の準備 新たな変数(globalPart,globalTile)を追加
		// メッシュレベル指定によるタイル分割の場合
		// origXY,cWHはグリッディングされる
		int[] globalPart = new int[2]; // メッシュレベル指定によるタイル分割の分割数
		Envelope globalTileSVG = null; // 同、データエリア
		if (meshLevel >= 0 ){
			Envelope globalTile = getGlobalTileArea( wgsEnv  , meshLevel , globalPart);
			globalTileSVG = smat.getSvgBoundsW( globalTile );
			wPart = globalPart[0];
			hPart = globalPart[1];
			// hPart, wPart , origX , origY , cWidth , cHeight 用
//			System.out.println("original:" + wgsEnv + " lvl:" + meshLevel);
			System.out.println("GlobalMeshLevel:" + globalPart[0]+","+globalPart[1] + ":" + globalTileSVG );
			if ( meshLevel < actualGenerateMeshLevel ){
				actualGenerateMeshLevel = actualGenerateMeshLevel - meshLevel;
			} else {
				actualGenerateMeshLevel = -1;
			}
		} else {
			actualGenerateMeshLevel = -1;
		}
		
		//フィーチャーソースからコレクションの取得
		FeatureCollection<SimpleFeatureType, SimpleFeature> fsShape = source.getFeatures();
//		int featureCount = fsShape.getCount();
		int featureCount = fsShape.size();
		System.out.println("レコード数:" + featureCount );
		
		
		// 指定したプロパティカラムの値を用いて各パラメータを設定する
		
		//スタイル用のカラムの取得
		Pattern p = Pattern.compile("#[0123456789abcdefABCDEF]{6}");
		
		try{
			linkFlg = Integer.parseInt( linkProp );
			if ( linkFlg > readFT.getAttributeCount() ){
				linkFlg = -1;
			}
		} catch (Exception e){
			linkFlg = -1;
		}
		
		try{
			captionFlg = Integer.parseInt( symbolProp );
			if ( captionFlg > readFT.getAttributeCount() ){
				captionFlg = -1;
			}
		} catch (Exception e){
			captionFlg = -1;
		}
		
		try{
			dupCheck = Integer.parseInt( dupProp );
			if ( dupCheck > readFT.getAttributeCount() ){
				dupCheck = -1;
			}
		} catch (Exception e){
			dupCheck = -1;
		}
		
//		System.out.println("================ColorProp:"+colorProp);
		
		try{ // プロパティ依存の色指定(プロパティ番号指定)
			colorCol = Integer.parseInt( colorProp );
//			System.out.println("================ColorCol:"+colorCol);
			if ( colorCol > readFT.getAttributeCount() ){
				colorCol = -1;
			}
		} catch (Exception e){ // #指定の色パラメータ
			colorCol = -1;
			Matcher m = p.matcher(colorProp);
			if ( m.matches() ){
				mainColor = colorProp;
			} else if ( colorProp.equalsIgnoreCase("none") ){
				mainColor = "none";
			}
		}
		
		try{ // プロパティ依存の色指定(プロパティ番号指定)
			olColorCol = Integer.parseInt( olColorProp );
//			System.out.println("================olColorCol:"+olColorCol);
			if ( olColorCol > readFT.getAttributeCount() ){
				olColorCol = -1;
			}
		} catch (Exception e){ // #指定の色パラメータ
			olColorCol = -1;
			Matcher m = p.matcher(olColorProp);
			if ( m.matches() ){
				outlineColor = olColorProp;
			} else if ( olColorProp.equalsIgnoreCase("none") ){
				outlineColor = "none";
			}
		}
		
		try{
			layerCol = Integer.parseInt( layerProp );
			if ( layerCol > readFT.getAttributeCount() ){
				layerCol = -1;
			}
		} catch (Exception e){
			layerCol = -1;
		}
		
		//同上をプロパティ名称から設定
		for ( int i = 0 ; i < readFT.getAttributeCount() ; i++){
			// プロパティ依存の色指定(プロパティ名指定)
			if (colorProp != "" && readFT.getDescriptor(i).getLocalName().equals(colorProp) ){
				colorCol = i;
			}
			if (olColorProp != "" && readFT.getDescriptor(i).getLocalName().equals(olColorProp) ){
				olColorCol = i;
			}
			
			if (readFT.getDescriptor(i).getLocalName().equals(symbolProp) ){
				captionFlg = i;
			}
			if (readFT.getDescriptor(i).getLocalName().equals(dupProp) ){
				dupCheck = i;
			}
			if (readFT.getDescriptor(i).getLocalName().equals(linkProp) ){
				linkFlg = i;
			}
			
			if (readFT.getDescriptor(i).getLocalName().equals(layerProp) ){
				layerCol = i;
			}
			System.out.println("attrNo:" + i +" Name:" + getKanjiProp(readFT.getDescriptor(i).getLocalName()) + 
			" type:" + readFT.getDescriptor(i).getType().getBinding().getSimpleName() ); 
		}
		
		if ( showHead ){
			return;
		}
		
		System.out.println("OriginalDatum:" + ((datum == 1) ? "JGD2000" : "TOKYO BESSEL") );
		System.out.println("OriginalCrd:" + ((xySys != 0) ? (String)("XY" + xySys) : "LatLon") );
		System.out.println("defMainColor:" + mainColor);
		System.out.println("outlineColor:" + outlineColor);
		System.out.println("mainColorCol:" + colorCol);
		System.out.println("outlineColorCol:" + olColorCol);
		System.out.println("POIsizeCol:" + POIsizeCol);
		System.out.println("strokeWCol:" + strokeWCol);
		System.out.println("captionCol:" + captionFlg);
		System.out.println("dupCheckCol:" + dupCheck);
		System.out.println("linkCol:" + linkFlg);
		System.out.println("layerCol:" + layerCol);
		
		
		boolean poiColumnString = false; // poiColumn文字列型の場合のフラグ 2017.9.15
		boolean poiColumn2String = false;
		// poiColumn,2のデータ型を調べ、文字列型の場合はフラグを設定する
		if ( poiColumn >= 0 ){
			if ( readFT.getDescriptor(poiColumn).getType().getBinding() == Class.forName("java.lang.String")){
				poiColumnString = true;
			}
		}
		if ( poiColumn2 >= 0 ){
			poiColumn2String = poiColumnString; // poiColumn2の方はpoiColumnと同じでないとダメ　おかしければメインループ内でexceptionです
		}
		
		// カラーユーティリティの初期化 (2018.1.26)
		colorUtil = new SVGMapGetColorUtil(fsShape, colorCol, olColorCol, colorTable, colorKeyLength, strIsSJIS, colorKeys);
		colorUtil.setOutOfRangeView(outOfRangeViewMethod);
		
		// 図形重複チェックのための前準備
		HashSet<Object> dupHash = new HashSet<Object>();
		
		//属性の範囲検索 重いね
		if ( colorCol >=0 || olColorCol >=0 || POIsizeCol >=0 ){
			boolean mColor = false;
			boolean oColor = false;
			boolean mSize  = false; // added 2017.7.14
			if ( colorCol >= 0){
				colorClass = readFT.getDescriptor(colorCol).getType().getBinding();
				if ( colorClass == Class.forName("java.lang.Double") || colorClass == Class.forName("java.lang.Integer") || colorClass == Class.forName("java.lang.Long")){
					System.out.println("Searching Extent for mainColor");
					mColor = true;
				}
			}
			if ( olColorCol >= 0){
				olColorClass = readFT.getDescriptor(olColorCol).getType().getBinding();
				if ( olColorClass == Class.forName("java.lang.Double") || olColorClass == Class.forName("java.lang.Integer") || olColorClass == Class.forName("java.lang.Long")){
					System.out.println("Searching Extent for outline Color");
					oColor = true;
				}
			}
			if ( POIsizeCol >= 0){
				sizeClass = readFT.getDescriptor(POIsizeCol).getType().getBinding();
				if ( sizeClass == Class.forName("java.lang.Double") || sizeClass == Class.forName("java.lang.Integer") || sizeClass == Class.forName("java.lang.Long")){
					System.out.println("Searching Extent for poiSize");
					mSize = true;
				}
			}
			if ( mainAttrMax != -9e99 && mainAttrMin != 9e99 ){
				mColor = false; // 既にmainAttrMinMaxが設定されている場合は、それを使えば良い
			}
			if ( sizeAttrMax != -9e99 && sizeAttrMin != 9e99 ){
				mSize = false; // 既にmainAttrMinMaxが設定されている場合は、それを使えば良い
			}
			if ( mColor || oColor || mSize ){
				getAttrExtent(fsShape , mColor , oColor , mSize); // このルーチンがまだSVGMapGetColorUtil非互換・・
				colorUtil.setAttrExtent( mainAttrMin , mainAttrMax , outlineAttrMin , outlineAttrMax ); // 2018.1.26
			}
		}
		
		String[] layerNames = new String[1024];
		Envelope[] layerBounds = new Envelope[1024];
		
		int layerCount;
		
		//レイヤの数と範囲の検索
		if ( layerCol >=0 ){
			layerCount = getLayerNames(fsShape ,  layerNames , layerBounds , layerCol);
			System.out.println("layerBounds:" + layerBounds);
		} else {
			layerNames[0] = "";
			layerCount = 1;
			layerBounds[0] = env;
		}
		
		for ( int layerNumber = 0 ; layerNumber < layerCount ; layerNumber ++ ){
			String outFileName;
			if ( layerCount > 1 ){
				outFileName = outfile.substring(0 , outfile.indexOf(".svg")) + "_" + layerNames[layerNumber] + ".svg";
			} else {
				outFileName = outfile;
			}
			System.out.println("Layer:" + outFileName);
			// 再帰分割用
			int level = 0;
//			boolean[][] tileExistence = new boolean[wPart][hPart];
			HashSet<Long> tileExistence = new HashSet<Long>();
			
			boolean outOfSize = true;
			Vector<HashSet<Long>> rTiles =new Vector<HashSet<Long>>(); // tileExistenceをレベル毎に多階層で蓄積したもの
			Vector<HashSet<Long>> rTileElements =new Vector<HashSet<Long>>(); // thisTileHasElementsをレベル毎に多階層で蓄積
			sm0 = new SvgMapTilesTM( ); // 
			sm0.isSvgTL = isSvgTL;
			
			boolean topContainer = true;
			
			
			// 以下の一連の(レイヤーごとの)SVG領域設定は、
			// タイリングにおいて想定外の処理が発生するのではないか？(2013/3/28)
			// 各レイヤーは、異なる領域を持っているのに、hp,wpは、その前で作ってしまってる
			wgsEnv = smat.getWgsBounds( layerBounds[layerNumber]  );
			System.out.println( "WgsEnvelope:" + wgsEnv );
			svgBounds = smat.getSvgBoundsW( wgsEnv  );
			origX =   svgBounds.getMinX();
			origY =   svgBounds.getMinY();
			cWidth =  svgBounds.getWidth();
			cHeight = svgBounds.getHeight();
			
			// globalTileMeshモードでは上記の問題はとりあえず解消させている？ 2013/3/28
			if ( meshLevel >= 0 ){
				origX = globalTileSVG.getMinX();
				origY = globalTileSVG.getMinY();
				cWidth = globalTileSVG.getWidth();
				cHeight = globalTileSVG.getHeight();
			}
			
			if ( fixedFont == 0.0 ){
				defaultFontSize = Math.sqrt(cWidth * cHeight / ( featureCount * 300.0 ));
				abs = false;
			} else if ( fixedFont > 0 ) { 
				defaultFontSize = fixedFont;
				abs = true;
			} else { // マイナスの場合は、-[属性番号]の属性値を用いる　サイズは固定 且つ伸縮しない文字
				defaultFontSize = Math.sqrt(cWidth * cHeight / ( featureCount * 300.0 ));
				fontSizeCol = - (int) fixedFont ;
				abs = true;
			}
			fontSize = defaultFontSize;
			
			HashMap<Long,Integer> divErr = new HashMap<Long,Integer>();
			
			while ( outOfSize ){
				if ( meshLevel > 0 ){
					System.out.println("meshLevel:" + (level + meshLevel) + "  h:" + hPart + "  w:" + wPart);
				} else {
					System.out.println("Level:" + level + "  h:" + hPart + "  w:" + wPart);
				}
				
				// SVGの出力開始 
				sm = new SvgMapTilesTM( outFileName , nFmt , origX , origY , cWidth , cHeight , wPart , hPart , level , tileExistence , smat , vps , isSvgTL, maxThreads, svgMapExecutorService );
				
				sm.setDefauleCustomAttr(false); // 2016/10/31 customAttrは個々の要素に付ける(<g>には付けない)
				
				
				if ( threadBuffer > 1023 ){
					sm.bufferedDrawSize = threadBuffer;
				}
				
//				System.out.println("sm Build Size:" + SizeOf.deepSizeOf(sm));
				sm.bitimageGlobalTileLevel = bitimageGlobalTileLevel; // 高度なdensity control用added 2013.3.28
				
				// 強制的にレベルをスキップさせる
				if ( level < actualGenerateMeshLevel ){
					sm.setForceOos(true);
				}
				
				// シェープファイルの漢字コード設定 (CSVの時はここで設定する必要はない。入力時に吸収できる)
				if ( !inputCsv && strIsSJIS == false ){
					sm.strIsSJIS = false;
				}
				
				// デバッグ用の枠線
				sm.tileDebug = layerDebug;
				
				// リミッタ設定
				if ( limitProp > 2 ){
					sm.setLimitter( limitProp * 1024 );
				}
				
				//micro metadata用の設定をする。
				if (microMetaEmbed || microMeta2Embed){
					sm.noMetaId = noMetaId;
					
					// 指定されていない場合(metaIndexがカラ)は全部出すために、全属性を書き出す
					if ( metaIndex.size() == 0 ){
						for ( int i = 0 ; i < readFT.getAttributeCount() ; i++){
							metaIndex.put(new Integer(i) , "");
						}
					}
					
					
					linkedMetaIndex = new Integer[metaIndex.size()];
					linkedMetaName = new String[metaIndex.size()];
					Iterator it = metaIndex.keySet().iterator();
					int i = 0;
					while(it.hasNext()){
						Integer key = (Integer)it.next();
						
						linkedMetaIndex[i] = key;
						if ( (String)metaIndex.get(key) ==""){ // 手動で属性名が指定されてない場合
							linkedMetaName[i] = getKanjiProp(readFT.getDescriptor(key.intValue()).getLocalName());
						} else {
							linkedMetaName[i] = (String)metaIndex.get(key);
						}
//						System.out.println( "meta::: " + i + " : " + linkedMetaIndex[i] + " , " + linkedMetaName[i] );
						++i;
					}
					
//					linkedMetaIndex = (Integer[])metaIndex.toArray(new Integer[0]);
					
					sm.linkedMetaIndex = linkedMetaIndex;
					sm.linkedMetaName = linkedMetaName;
					
					if ( microMetaEmbed ){
						sm.setMicroMetaHeader( readFT , metaNs , metaUrl );
					} else {
						sm.setMicroMeta2Header( readFT , true );
					}
				}
				
				// SVG のヘッダ部分など
				sm.putHeader();
				
				//参考データのプリントアウト
				sm.putComment(" 参考データです");
				sm.putComment("Tramsform:"  + smat.g2s  );
				sm.putComment("基準緯線:"  + projCenter  );
				sm.putComment("Bounds:"  + layerBounds[layerNumber]  );
				sm.putComment("TypeName:" + readFT.getTypeName()   );
				sm.putComment("Count:" + readFT.getAttributeCount()  );
				sm.putComment("NS:" + readFT.getName() );
				sm.putComment("Geom:" + readFT.getGeometryDescriptor() ); 
				for ( int i = 0 ; i < readFT.getAttributeCount() ; i++){
					String atn = getKanjiProp(delCC(readFT.getDescriptor(i).getLocalName()));
					sm.putComment("attrNo:" + i +" Name:" + atn +
					" type:" + readFT.getDescriptor(i).getType().getBinding().getSimpleName()  ); 
				}
			
				sm.putComment("OriginalDatum:" + ((datum == 1) ? "JGD2000" : "TOKYO BESSEL") );
				sm.putComment("OriginalCrd:" + ((xySys != 0) ? (String)("XY" + xySys) : "LatLon") );
				sm.putComment("defMainColor:" + mainColor );
				sm.putComment("outlineColor:" + outlineColor );
				sm.putComment("mainColorCol:" + colorCol );
				sm.putComment("outLineColorCol:" + olColorCol );
				sm.putComment("POIsizeCol:" + POIsizeCol );
				sm.putComment("captionCol:" + captionFlg );
				sm.putComment("dupCheckCol:" + dupCheck );
				sm.putComment("linkCol:" + linkFlg );
				//out.write("Type:"  + readFT.getNamespace() + "\n");
				
				// デフォルトスタイルを常に使う(2010/06/22) 
				useDefaultStyle = true;
				prevType = 0;
				
				System.out.println("useDefaultStyle:" + useDefaultStyle );
				
				// CRSメタデータを出力
				if ( layerMetadata.length() > 0 ){
					sm.setLayerMetadata(layerMetadata);
				}
				sm.putCrs( smat.g2s.a , smat.g2s.b , smat.g2s.c , smat.g2s.d , smat.g2s.e , smat.g2s.f );
				
				// GISメタデータ出力を設定
				if (metaEmbed ){
					sm.setMetadata(readFT  , fsShape , metaNs , metaUrl );
				}
				
				if ( directPoi != 0 ){ // POIをシンボル定義を使わずに出力する場合(directPoiモード)
						System.out.println("setDirectShapeSymbol = true");
					if ( directPoi >0){ // カラム番号からPOIの番号を得る 0カラム(geometry)は使えない
						System.out.println("  SymbolTypeCol:" + directPoi);
					}
					sm.setDefaultPoiSize(  accuracy * (-smat.g2s.d) / 111111.1  );
				} else {
					// シンボル定義の出力
//					System.out.println("OUTPUT SYMBOL");
					if ( symbolTemplate != "" ){ // 外部からテンプレートを使用してシンボルを設定する場合 2013.3.11
//						System.out.println("OUTPUT EXTERNAL SYMBOL");
						sm.putSymbol( symbolTemplate );
						if ( fixedSymbol <= 0 ){
							fixedSymbol = 16 ; // 外部からテンプレートを使用してシンボルを設定する場合は、fixed専用にして、16pxのシンボルと見立てて処理する・・(caption shiftに影響)
						}
					} else {
						if ( fixedSymbol == 0 ){ // シンボルを固定しない場合
							sm.putSymbol( cWidth / 500.0 ,fixedStroke); // これはあまりにいい加減 2012/04
						} else if ( fixedSymbol > 0 ){ // シンボルを一定サイズで固定する場合（default：6）
							sm.putSymbol( fixedSymbol/2.0 ,fixedStroke); // サイズを直径にしたので・・ 2016/2/26
						} else { // シンボルを固定しないが、物理サイズ[m]を指定している場合
//							sm.putSymbol( Math.abs(smat.g2s.d) * ( - (fixedSymbol/2.0) * 360 / 40000000) ,fixedStroke) ;
							sm.putSymbol( Math.abs(smat.g2s.d) * ( - (fixedSymbol/2.0) ) ,fixedStroke) ; // 2016/2/26 m->deg変更
							fixedSymbol = 0;
							System.out.println("シンボルを固定しないが、物理サイズ[m]を指定");
						}
					}
				}
				
				
				//コレクションをイテレータに設定
				FeatureIterator<SimpleFeature> reader = fsShape.features();
				Coordinate capCrd = new Coordinate();
				lop=0;
				dupHash.clear();
				int dupCount = 0;
				
				SimpleFeature oneFeature = null;
				Geometry oneGeom;
				boolean hasAnchor = false;
				boolean hasFeature;
				
				int topCounter = topCount;
				int poiType = -1; // シンボルの"P"+ID番号
				String poiSymbolId ="";
				
				// データを書き出すメインループ
				while ( reader.hasNext() && topCounter != 0 ) {
					hasFeature = false; // エラーが出るときが有るのでエラー回避用 09/01/xx
					while ( hasFeature == false ){
						try{
							oneFeature = reader.next();
							hasFeature = true;
						} catch ( Exception e ){
							System.out.print("rERR");
							hasFeature = false;
						}
					}
					
					if ( dupCheck >=0 ){
						if ( ! dupHash.add(oneFeature.getAttribute(dupCheck))){
							++ dupCount;
							continue; // 重複があったらスルーする
						}
					}
					
					
					// 属性によるレイヤー分割を行う場合
					if ( layerCol >=0 ){
							if ( ! (getKanjiProp(oneFeature.getAttribute(layerCol).toString())).equals(layerNames[layerNumber]) ){
								continue;
							}
					}
					if ( linkFlg >= 0 ){ // 2016/10/31 xlink:titleに絡んだバグ(micrometa2と重複)対策
						sm.useTitleAttr = false;  // micrometaでのxlink:title抑制 きれいでないけど・・
					}
					
					if ( metaEmbed ){
						sm.setId( );
					} else if ( microMetaEmbed ){
						sm.setMicroMeta( oneFeature );
					} else if ( microMeta2Embed ){
						sm.setMicroMeta2( oneFeature );
					}
					
					if (colorCol >= 0 ){
						mainColor = colorUtil.getColor( oneFeature.getAttribute(colorCol) , mainAttrMin , mainAttrMax);
					} 
					if (olColorCol >= 0 ){
						outlineColor = colorUtil.getColor( oneFeature.getAttribute(olColorCol) , outlineAttrMin , outlineAttrMax);
					} 
					if (mainColor.length() == 0 ){
						continue;
					}
					if (POIsizeCol >= 0 ){
						POIsize = getPOIsize(oneFeature.getAttribute(POIsizeCol) , sizeAttrMin , sizeAttrMax , sizeRangeMin , sizeRangeMax );
					}
					
					// POIの話・・ただ今作成中（まだエラーチェック不完全すぎます 2012/7/30）
					if ( directPoi > 0){ // directPoi且つカラム番号からPOI番号を得る
						poiType = getPoiTypeNumber(oneFeature.getAttribute(directPoi));
					} else if ( directPoi < 0){ // 直接指定
						poiType = - directPoi;
					} else if ( poiColumn >= 0 ){
						if ( !poiColumnString ){
							if ( poiColumn2 >= 0 ){
								poiType = getPoiTypeNumber(oneFeature.getAttribute(poiColumn)) + getPoiTypeNumber(oneFeature.getAttribute(poiColumn2));
							} else {
								poiType = getPoiTypeNumber(oneFeature.getAttribute(poiColumn));
							}
						} else {
							poiType = -1;
							if ( poiColumn2 >= 0 ){
								poiSymbolId = getKanjiProp((String)oneFeature.getAttribute(poiColumn)) + getKanjiProp((String)oneFeature.getAttribute(poiColumn2));
							} else {
								poiSymbolId = getKanjiProp((String)oneFeature.getAttribute(poiColumn));
							}
						}
					} else if ( customPoiType >= 0 ){
						poiType = customPoiType;
					} else {
						poiType = 0;
					}
					
					if (strokeWCol >= 0 ){
						Object sw = oneFeature.getAttribute(strokeWCol);
						if ( sw instanceof String ){
							try{
								strokeWidth = Double.parseDouble((String)sw);
							} catch ( Exception e ) {
							}
						} else if ( sw instanceof Number ){
							strokeWidth = ((Number)sw).doubleValue();
						}
					}
					
					if ( linkFlg >= 0 ){ // 2016/10/31 hrefがないのに<a>を設ける必要がない問題　の解消
						String unicodeStirng = getKanjiProp((String)oneFeature.getAttribute(linkFlg));
						if ( hrefFlg >= 0 ){ // 今のところこのフラグは決して立たない
							String lnkString =  getKanjiProp((String)oneFeature.getAttribute(hrefFlg));
							if ( unicodeStirng.length() > 0 ){
								sm.setAnchor( unicodeStirng , lnkString );
								hasAnchor = true;
							}
						} else { // 2016/10/31 customAttrとしてxlink:titleを付けている。
							sm.setCustomAttribute("xlink:title=\""+sm.htmlEscape(unicodeStirng)+"\" ");
//							sm.setAnchor( unicodeStirng , "" );
//							hasAnchor = true;
						}
					}
					
					if ( putRecord ){
						sm.setCustomAttribute("data-record=\""+sm.htmlEscape(Integer.toString(lop))+"\" ");
					}
					
					oneGeom = (Geometry) oneFeature.getDefaultGeometry();
					
					if ( ! noShape ){
						if ( simplify ){ // 図形の簡単化
							if ( TopoPresSimp ){
								oneGeom = Tsimplifier.simplify( oneGeom , simplifyParam );
							} else {
								oneGeom = Dsimplifier.simplify( oneGeom , simplifyParam );
							}
							
						}
						
						parseGeometry(oneGeom , sm , poiType , poiSymbolId ); // 変換機能の本体を呼び出す
					} else if ( lop == 0 ) { // キャプションのみで最初のループのときにデフォルトスタイル設定する
						setCapOnlyDefaultStyle( sm );
					}
					if ( captionFlg >= 0 ){
						if ( fontSizeCol > 0 ){
							Object fs = oneFeature.getAttribute(fontSizeCol);
							if ( fs instanceof String ){
								try{
									fontSize = Double.parseDouble((String)fs);
								} catch ( Exception e ){
								}
							} else if ( fs instanceof Number ){
								fontSize = ((Number)fs).doubleValue();
							}
						} else {
							// fontSize = defaultFontSize;
						}
						double capSft;
						if (prevType == 3 && abs && fixedSymbol > 0.0 ){
							// ジオメトリがPointでそのシンボルサイズが固定でキャプションサイズも固定(abs)の場合キャプションをシフトする
							capSft = -(fixedSymbol/2.0);
						} else {
							capSft = 0;
						}
						putCaption( smat.transCoordinate(oneGeom.getEnvelopeInternal().centre())
							, oneFeature.getAttribute(captionFlg) , fontSize , sm , abs , capSft);
						
					}
					if ( hasAnchor ){
						sm.termAnchor();
						hasAnchor = false;
					}
					++ lop;
					if ( lop % 10000 == 0 ){
						System.out.print("O");
					} else if ( lop % 1000 == 0 ){
						System.out.print(".");
					}
					if (sm.allTilesOOS){
						System.out.println("\nAll tiles are out of size. Skip to next level.");
						break;
					}
					
					if ( topCounter > 0 ){
//						System.out.println("c:" + topCounter);
						--topCounter;
					}
				
				}
				//limemergeで、残ったものを出力する。
				if ( lineMerge && lineList.size() > 0 ){
//					System.out.println("merge:" + lineList.size());
					mergeAndDrawLineList ( lineList , mainColor , strokeWidth , opacity , sm );
					lineList = new Vector<LineString>();
				}
				sm.putComment("Total:"+lop+" records." );
				if (colorCol >=0 && colorUtil.colorMap.size() <= 128 ){
					sm.putComment( "colorMap(" + colorUtil.colorMap.size() + "vals : under 128vals):" + colorUtil.colorMap );
					System.out.println( "colorMap(" + colorUtil.colorMap.size() + "vals : under 128vals):" + colorUtil.colorMap );
				}
				System.out.println("Total:"+lop+" records." );
				if ( dupCheck >=0 ){
					sm.putComment("Total:"+dupCount+" duplicates." );
					System.out.println("Total:"+dupCount+" duplicates." );
				}
				sm.defaultFill = mainColor;
				divErr = sm.putFooter(densityControl, divErr );
				
//				System.out.println( "divCheck (true:ok,false:exception): " + sm.divCheck( divErr, 3 ) );
				
//				if ( sm.divCheck( divErr, 3 ) == false ){ //}もともとは3でした 2016.3.14
				if ( sm.divCheck( divErr, divErrMax , level ) == false ){ // 2016.3.15 判断ロジックを少し改良
					System.out.println("No div effect at all...... HALT PROGRAM...");
					outOfSize = false;
				} else if ( meshLevel > 0 && (level + meshLevel ) >= maxLevel){ // 2017.4.20
					System.out.println("Tile div level exceeds limitter :"+ maxLevel + "..   Treminate processing.");
					// 本来、ここでまだ出力できていないタイル分のデータを生成する特別タイルを構築するべき(TODO)
					outOfSize = false;
				} else {
					outOfSize = sm.outOfSize;
				}
				reader.close();
				
//				tileExistence = sm.getThisTileExsitenceArray();
				tileExistence = sm.getThisTileExistenceSet();
				//タイルの存在とタイル中の図形要素の存在のフラグを階層的に蓄積する
				rTiles.addElement( tileExistence );
//				rTileElements.addElement( sm.getThisTileHasElementsArray() );
				rTileElements.addElement( sm.getThisTileHasElementsSet() );
				//一番上の階層のコンテンツを作成しているとき、それをsm0で保持しておく
				if ( sm.tiled && topContainer ){
					// limitによる分割では、level=1がsm0になる(これは煩雑になるため廃止 2013.8.6)
					// meshによる分割ではlevel=0がsm0になる
					// limitとmeshの両指定ではlevel=0がsm0になる(このときBUGがある？もうないのでは)　あった感じ2015.5.15
					sm0 = sm;
					topContainer = false;
					System.out.println("ContainerLevel:" + sm0.level );
				}
				
				// ひいーぷチェック
				if ( heapCheck ){
					System.out.printf("最大サイズ：%10d%n", usage.getMax());
					System.out.printf("使用サイズ：%10d%n", usage.getUsed());
//					System.out.println("rTiles Size:" + SizeOf.deepSizeOf(rTiles));
//					System.out.println("sm Size:" + SizeOf.deepSizeOf(sm));
				}
				
				++ level;
			} //階層的タイル分割ループの終了
			
			//コンテナを作成する。場合によっては、階層的なコンテナになる
//			if ( wPart * hPart > 1 ){ //}
			if ( sm0.tiled ){ // debug 2015.5.15 これで階層コンテナのリファクタリングに伴うバグ解消か？
//			if ( level > 2 || ( wPart * hPart > 1 && level > 1) ){ // }
//				if ( layerDebug ){
//					sm0.tileDebug = true;
//				}
				if ( pStep > 1 ){
					sm0.pStep = pStep;
				}
				sm0.defaultFill = mainColor;
				sm0.createRContainer2(rTiles , rTileElements , densityControl);
				
			}
			if ( meshLevel >= 0 ){
				System.out.println("globalTileLevel:" + meshLevel + " .. " + (meshLevel + rTiles.size() - 1) );
			}
		} //属性値を用いたレイヤー分割ループの終了
		
		if ( layerCount > 1 ){
			buildHyperLayerContainer(outfile , nFmt , env , layerCount , layerNames , layerBounds);
		}
		
		svgMapExecutorService.shutdown();
	
	}
	
	private void setCapOnlyDefaultStyle( SvgMapTilesTM sm ) throws Exception {
		if ( captionFlg >= 0 ){
			sm.setDefaultCaptionStyle( defaultFontSize , false );
			sm.setDefaultStyle( mainColor , -1 , "" , opacity , false );
		}
	}
	
	private void setDefaultStyle( int mode , SvgMapTilesTM sm ) throws Exception{ // 1:Polygon , 2:Line , 3:Point   2009/01/31
		
		switch (mode) {
		case 1: // Polygon
			if ( outlineColor != "" ){ // 枠線付きの場合は、枠線の色と太さも設定
				if ( captionFlg >= 0 ){
					// 枠線ある場合は文字の輪郭消す(true)
					sm.setDefaultCaptionStyle( defaultFontSize  , true );
				}
				sm.setDefaultStyle( mainColor , strokeWidth , outlineColor , opacity , vectorEffect);
			} else { // 枠線なしの場合は塗りの色と透明度だけ
				if ( captionFlg >= 0 ){
					// 枠線ない場合は文字の輪郭設定しないで良い(false)
					sm.setDefaultCaptionStyle( defaultFontSize  , false );
				}
				sm.setDefaultStyle( mainColor , -1 , "" , opacity , false );
			}
			break;
		case 2: // Line
			if ( captionFlg >= 0 ){
				// 線の場合は文字の輪郭消す(true)
				sm.setDefaultCaptionStyle( defaultFontSize  , true );
			}
			// 線の場合は塗りは関係ないから作らず、メインカラーは塗りではなくて線の色になる
			sm.setDefaultStyle( "none" , strokeWidth , mainColor , opacity , vectorEffect );
			break;
		case 3: // Point
			if ( captionFlg >= 0 ){
				// 点の場合は文字の輪郭設定しないで良い(false)
				sm.setDefaultCaptionStyle( defaultFontSize  , false );
			}
			// 点の場合塗りだけ設定する
			sm.setDefaultStyle( mainColor , -1 , "" , -1 , false );
			break;
		default:
			//なにもしない？
			// sm.setDefaultStyle( null , null , null , null , null );
			break;
		}
		
	}
	
	// 線結合ロジック用
	boolean lineMerge = false;
	String prevMainColor = "";
	double prevStrokeWidth = 0;
	double prevOpacity = 0;
	Vector<LineString> lineList = new Vector<LineString>();
	
	
	// 図形変換機能
	private int prevType = 0; // ひとつまえのジオメトリのタイプ 1:Polygon , 2:LineString , 3:Point
	public boolean useDefaultStyle = true;
	private void parseGeometry(Geometry geom , SvgMapTilesTM sm , int poiType , String poiSymbolId ) throws Exception {
		Coordinate[] coord , coord0;
//		Coordinate oneCrd = new Coordinate();
		Coordinate oneCrd ;
		PolygonDouble pol;
		Envelope svgEnv;
		if (geom instanceof Polygon ){
			if ( useDefaultStyle && prevType != 1 && !outGroup){ //<g>を使ってスタイルをまとめてサイズを削減する処理
				setDefaultStyle(1 , sm);
			}
			svgEnv = smat.getSvgBounds( geom.getEnvelopeInternal() );
			coord = (((Polygon)geom).getExteriorRing()).getCoordinates();
			pol = smat.transCoordinates(coord);
			sm.setExterior( pol , svgEnv );
			
			for ( int j = 0 ; j < ((Polygon)geom).getNumInteriorRing() ; j++ ){
				coord = (((Polygon)geom).getInteriorRingN(j)).getCoordinates();
				pol = smat.transCoordinates(coord);
				sm.setInterior( pol , svgEnv );
			}
			sm.putPolygon( mainColor , strokeWidth , outlineColor , opacity );
			prevType = 1;
		} else if (geom instanceof LineString ){
			if ( useDefaultStyle && prevType != 2 && !outGroup){
				setDefaultStyle(2 , sm);
			}
			if (  lineMerge ){ // 線の結合ロジックを動かす
//				System.out.print("M");
				if ( prevType == 2 &&  prevMainColor.equals(mainColor) && prevStrokeWidth == strokeWidth && prevOpacity == opacity ){
					// スタイルが同じならラインストリングをマージリストに加えていく
					lineList.add((LineString)geom);
				} else {
					// 違うものがきたら、まずは出力して、その後新しいリストの作成を開始する。
					if ( lineList.size() > 0 ){
						mergeAndDrawLineList ( lineList , mainColor , strokeWidth , opacity , sm );
					}
					lineList = new Vector<LineString>();
					lineList.add( (LineString)geom );
					
					prevOpacity = opacity;
					prevMainColor = mainColor;
					prevStrokeWidth = strokeWidth;
				}
			} else {
				svgEnv = smat.getSvgBounds( geom.getEnvelopeInternal()  );
				coord = ((LineString)geom).getCoordinates();
				pol = smat.transCoordinates(coord);
				sm.putPolyline( pol , mainColor , strokeWidth , svgEnv , opacity );
			}
			prevType = 2;
		} else if (geom instanceof Point ){
			if ( useDefaultStyle && prevType != 3 && !outGroup){
				setDefaultStyle(3 , sm);
			}
			oneCrd = smat.transCoordinate(((Point)geom).getCoordinate() );
			if ( directPoi == 0 ){ // シンボルuseの場合
				if ( fixedSymbol == 0 ){ // シンボルサイズが固定でないばあい
					if ( poiType >= 0 ){
						sm.putUse( oneCrd , mainColor , false , "p" + poiType );
					} else {
						sm.putUse( oneCrd , mainColor , false , poiSymbolId );
					}
				} else { // シンボルサイズが固定の場合
					if ( poiType >= 0 ){
						sm.putUse( oneCrd , mainColor , true  , "p" + poiType );
					} else {
						sm.putUse( oneCrd , mainColor , true  , poiSymbolId );
					}
				}
			} else { // directPOIの場合
				if ( poiType == -1 ){
					poiType = 9;
				}
//				System.out.println("POIT:"+poiType);
				if ( fixedSymbol <= 0 ){ // シンボルサイズが固定でないばあい 2014.5.12
					sm.putPoiShape( oneCrd , poiType , POIsize , mainColor , 3 , outlineColor , true , false );
				} else {
//					座標, タイプ(0..11), サイズ, 塗色, 線幅, 線色, nonScale線幅, nonScale図形
//					sm.putPoiShape( oneCrd , poiType , -1 , mainColor , 3 , outlineColor , true , true );
					sm.putPoiShape( oneCrd , poiType , POIsize , mainColor , 0 , outlineColor , true , true );
				}
			}
			prevType = 3;
		} else if (geom instanceof GeometryCollection ){
//http://www.jump-project.org/docs/jts/1.7/api/com/vividsolutions/jts/geom/GeometryCollection.html
			if ( outGroup ){
				sm.setGroup();
			} else { // GeometryCollectionに２個以上Geomがあると２個目以降のメタデータがなくなってしまう
				sm.setShadowGroup();
			}
			for ( int j = 0 ; j < ((GeometryCollection)geom).getNumGeometries() ; j++){
				Geometry childGeom = ((GeometryCollection)geom).getGeometryN(j);
				if ( childGeom.getNumPoints() > 0 ){ // エラー処理追加-普通こういうデータはないはずだけど・・
					parseGeometry(childGeom , sm , poiType , poiSymbolId );
				}
			}
			if ( outGroup ){
				sm.termGroup();
			} else {
				sm.termShadowGroup();
			}
//			prevType = 0;
		} else if (geom instanceof Geometry ){
			sm.putComment("Type: Other Geometry...." + geom );
//			prevType = 0;
		} else if (geom instanceof Object){
			sm.putComment("Type: Other Object...." + geom );
//			prevType = 0;
		}
	}
	
	
	private void mergeAndDrawLineList ( Vector<LineString> lineList , String mainColor , double strokeWidth , double opacity , SvgMapTilesTM sm ) throws Exception {
		Coordinate[] coord;
		Envelope svgEnv;
		PolygonDouble pol;
		LineMerger merger = new LineMerger();
		System.out.print("m");
		for(int i = 0; i < lineList.size(); i++) {
//			System.out.println("+adding");
			
			LineString lines = lineList.get(i);
//			System.out.println("p" + lines.getNumPoints() );
			try{
				merger.add(lines);
			} catch ( Exception e ){
//				System.out.print("E");
			}
//			System.out.println("added");
		}
//		System.out.println("end mergeradd");
		java.util.Collection mergedLines = merger.getMergedLineStrings();
		for(Iterator lineStrings = mergedLines.iterator(); lineStrings.hasNext();) {
			LineString line = (LineString)lineStrings.next();
			svgEnv = smat.getSvgBounds( line.getEnvelopeInternal()  );
			coord = ((LineString)line).getCoordinates();
			pol = smat.transCoordinates(coord);
			sm.putPolyline( pol , mainColor , strokeWidth , svgEnv , opacity );
		}
	}
	
	private int getPoiTypeNumber( Object sValue ){
		int iValue = -1;
		if ( sValue instanceof Integer ){
			iValue = ((Integer)sValue).intValue();
		} else if ( sValue instanceof Double ){
			iValue = ((Double)sValue).intValue();
		} else if ( sValue instanceof Long ){
			iValue = ((Long)sValue).intValue();
		}
		return ( iValue);
	}
	
	
	
	// added 2017/07/14 POIのサイズを属性値から設定させる
	private double getPOIsize( Object sValue , double attrMin , double attrMax , double sizeMin , double sizeMax ){
		double dValue = 0.0;
		if ( sValue instanceof Integer ){
			dValue = ((Integer)sValue).doubleValue();
		} else if ( sValue instanceof Double ){
			dValue = ((Double)sValue).doubleValue();
		} else if ( sValue instanceof Long ){
			dValue = ((Long)sValue).doubleValue();
		}
		
		if ( dValue < attrMin ){
			return ( sizeMin );
		} else if ( dValue > attrMax ){
			return ( sizeMax );
		} else {
			return (( ( dValue - attrMin ) / ( attrMax - attrMin ) ) * ( sizeMax - sizeMin ) + sizeMin );
		}
	}	
	
	double mainAttrMax = -9e99;
	double mainAttrMin = 9e99;
	double outlineAttrMax = -9e99;
	double outlineAttrMin = 9e99;
	
	// added 2017.7.14
	double sizeAttrMax = -9e99;
	double sizeAttrMin = 9e99;
	double sizeRangeMax = 24;
	double sizeRangeMin = 3;
	
	private void getAttrExtent(FeatureCollection<SimpleFeatureType, SimpleFeature> fsShape , boolean mColor , boolean oColor , boolean mSize ){
		SimpleFeature oneFeature = null;
		Object valueM , valueO , valueS;
		double dvalM = 0;
		double dvalO = 0;
		double dvalS = 0;
		FeatureIterator<SimpleFeature> reader = fsShape.features();
		boolean hasFeature;
		while (reader.hasNext()) {
			hasFeature = false;
			while ( hasFeature == false ){
				try{
					oneFeature = reader.next();
					hasFeature = true;
				} catch ( Exception e ){
					System.out.print("attrERR");
					hasFeature = false;
				}
			}
			if ( mColor ){
				valueM = (oneFeature.getAttribute(colorCol));
				if ( valueM != null ){
					if ( valueM instanceof Double ){
						dvalM = ((Double)valueM).doubleValue();
					} else if ( valueM instanceof Integer ){
						dvalM = ((Integer)valueM).intValue();
					} else if ( valueM instanceof Long ){
						dvalM = ((Long)valueM).longValue();
					}
					if ( dvalM > mainAttrMax ){
						mainAttrMax = dvalM;
					}
					if ( dvalM < mainAttrMin ){
						mainAttrMin = dvalM;
					}
				}
			}
			if ( oColor ){
				valueO = (oneFeature.getAttribute(olColorCol));
				if ( valueO != null ){
					if ( valueO instanceof Double ){
						dvalO = ((Double)valueO).doubleValue();
					} else if ( valueO instanceof Integer ){
						dvalO = ((Integer)valueO).intValue();
					} else if ( valueO instanceof Long ){
						dvalO = ((Long)valueO).longValue();
					}
					if ( dvalO > outlineAttrMax ){
						outlineAttrMax = dvalO;
					}
					if ( dvalO < outlineAttrMin ){
						outlineAttrMin = dvalO;
					}
				}
			}
			if ( mSize ){
				valueS = (oneFeature.getAttribute(POIsizeCol));
				if ( valueS != null ){
					if ( valueS instanceof Double ){
						dvalS = ((Double)valueS).doubleValue();
					} else if ( valueS instanceof Integer ){
						dvalS = ((Integer)valueS).intValue();
					} else if ( valueS instanceof Long ){
						dvalS = ((Long)valueS).longValue();
					}
					if ( dvalS > sizeAttrMax ){
						sizeAttrMax = dvalS;
					}
					if ( dvalS < sizeAttrMin ){
						sizeAttrMin = dvalS;
					}
				}
			}
		}
		if ( colorCol != -1  ){
			System.out.println( "mainColorAttr    Col:" + colorCol + ": Min:" + mainAttrMin + " Max:" + mainAttrMax );
		}
		if ( olColorCol != -1 ){
			System.out.println( "outlineColorAttr Col:" + olColorCol + ": Min:" + outlineAttrMin + " Max:" + outlineAttrMax );
		}
		if ( POIsizeCol != -1 ){
			System.out.println( "POIsizeAttr Col:" + POIsizeCol + ": Min:" + sizeAttrMin + " Max:" + sizeAttrMax );
		}
	}
	
	private Envelope getFSExtent(FeatureCollection<SimpleFeatureType, SimpleFeature> fsShape){
		boolean hasFeature;
		Envelope newBBox = new Envelope();
		Envelope internal;
		SimpleFeature oneFeature = null;
		FeatureIterator<SimpleFeature> reader = fsShape.features();
		while (reader.hasNext()) {
			hasFeature = false;
			while ( hasFeature == false ){
				try{
					oneFeature = reader.next();
					hasFeature = true;
				} catch ( Exception e ){
					System.out.print("extERR");
					hasFeature = false;
				}
			}
			internal = ((Geometry)oneFeature.getDefaultGeometry()).getEnvelopeInternal();
			if ( Math.abs(internal.getMaxX()) > 300 || Math.abs(internal.getMinX()) > 300 ||
			Math.abs(internal.getMaxY()) > 300 || Math.abs(internal.getMinY()) > 300 ||
			Math.abs(internal.getMaxX()) > 300 || Math.abs(internal.getMinX()) > 300 ){
				// 異常値の場合　don't append
			} else {
				newBBox.expandToInclude(internal);
			}
		}
		System.out.println( "TracedBBOX:" + newBBox );
		return newBBox;
	}
	
	
	// 指定した属性番号で各レイヤーの名前(ハッシュキー)とその領域を取得する
	private int getLayerNames( FeatureCollection<SimpleFeatureType, SimpleFeature> fsShape , String[] layerNames , Envelope[] layerBounds , int layerCol ){
		SimpleFeature oneFeature = null;
		HashSet<String> set = new HashSet<String>(); // 無駄な気が・・・・
		HashMap<Object,ReferencedEnvelope> map = new HashMap<Object,ReferencedEnvelope>();
		ReferencedEnvelope oneEnv;
		Object value;
		boolean err = false;
		FeatureIterator<SimpleFeature> reader = fsShape.features();
		boolean hasFeature;
		while (reader.hasNext()) {
			hasFeature = false;
			while ( hasFeature == false ){
				try{
					oneFeature = reader.next();
					hasFeature = true;
				} catch ( Exception e ){
					System.out.print("lnERR");
					hasFeature = false;
				}
			}
			
//			value = (oneFeature.getAttribute(layerCol));
			
			Object val = oneFeature.getAttribute(layerCol);
			String vals ="";
			if ( val instanceof String ){
				vals = (String)val;
			} else {
				vals = val.toString();
			}
			value = getKanjiProp(vals);
				
//			value = getKanjiProp((String)oneFeature.getAttribute(layerCol));
			set.add(value.toString());
			if ( map.containsKey( value ) ){
				oneEnv = (ReferencedEnvelope)(map.get( value ));
				oneEnv.expandToInclude( (ReferencedEnvelope)oneFeature.getBounds() );
				map.put( value , oneEnv ); // これは不要？
			} else {
				map.put( value , (ReferencedEnvelope)(oneFeature.getBounds()) );
			} 
			
			
			if ( set.size() > 1024 ){
				System.out.println ( "Out Of Size Error!");
				err = true;
				break;
			}
		}
		
		
		System.out.println( "Attr Size: " + set.size()  +  set);
//		System.out.println( "Envelopes: "  +  map);
		layerNames = (String[])set.toArray( layerNames );
		
		
		Iterator iterator = map.keySet().iterator();
		Object obj;
		int i = 0;
		while(iterator.hasNext()){
			obj = iterator.next();
			layerNames[i] = obj.toString();
			layerBounds[i] = (Envelope)( map.get(obj));
			System.out.print(layerBounds[i] + " : " );
			++i;
		}
		return( map.size() );
		
	}

	
	private void setNumberFormat(double d , double accuracy){
	// NumberFormatter初期化
		nFmt = NumberFormat.getNumberInstance();
		nFmt.setGroupingUsed(false);
		
		int fractions;
		
		fractions = -(int)(Math.log(-d * accuracy / 111111 ) / Math.log(10.0)) + 1;
		System.out.println( "Fractions:" + fractions + "( d=" + d + " acc=" + accuracy + ")" );
		if ( fractions < 0 ){
			fractions = 0;
		}
		nFmt.setMaximumFractionDigits(fractions);
		
		
	}
	
	private void putCaption ( Coordinate coo , Object attr , double cWidth , SvgMapTilesTM sm , boolean abs , double capSft ) throws Exception{
		attr = attr.toString();
//		System.out.print(attr);
		String unicodeStirng = getKanjiProp( (String)attr ); // 2016.10 関数統合化
//		System.out.println(":" + unicodeStirng);
		if ( unicodeStirng.length() > 0 ){
			sm.putText( coo , cWidth , unicodeStirng , abs , capSft );
		}
	}
	
	
	// このルーチンはまだ動いていないです・・・・対応は結構難しい
	private void putRdfMetadata(SimpleFeatureType readFT , FeatureCollection<SimpleFeatureType, SimpleFeature> fsShape , Writer out) throws Exception{
		SimpleFeature oneFeature = null;
		Object value;
		double dval=0;
		int i , j;
		FeatureIterator<SimpleFeature> reader = fsShape.features();
		j = 0;
		boolean hasFeature;
		while (reader.hasNext()) {
			hasFeature = false;
			while ( hasFeature == false ){
				try{
					oneFeature = reader.next();
					hasFeature = true;
				} catch ( Exception e ){
					System.out.print("metaERR");
					hasFeature = false;
				}
			}
			
			out.write("  <rdf:Description rdf:about=\"#f" + j + "\" \n");
			for ( i = 0 ; i < readFT.getAttributeCount() ; i++){
				if (oneFeature.getAttribute(i) instanceof Geometry == false ){
					out.write( "   " + metaNs + ":" + readFT.getDescriptor(i).getLocalName() + "=\"" + oneFeature.getAttribute(i) + "\" \n");
				}
			}
			out.write("  />\n");
			j++;
		}
	}
	
	
	private void buildHyperLayerContainer(String outfile , NumberFormat nFmt , Envelope allBounds , int layerCount , String[] layerNames , Envelope[] layerBounds)throws Exception{
		SvgMap hlc;
		Envelope svgBounds ;
		String layerFileName , href;
		int p = outfile.lastIndexOf("\\");
		if ( p<0 ){
			p = outfile.lastIndexOf("/");
		}
		if ( p<0 ){
			p=0;
		} else {
			p = p + 1;
		}
		href = outfile.substring(p);
		
		
		hlc = new SvgMap(outfile , nFmt);
		
		svgBounds = smat.getSvgBounds(  allBounds  );
		hlc.putHeader(svgBounds.getMinX(), svgBounds.getMinY(), svgBounds.getWidth(), svgBounds.getHeight());
		double hlcWidth= svgBounds.getWidth();
		
		hlc.putCrs( smat.g2s.a , smat.g2s.b , smat.g2s.c , smat.g2s.d , smat.g2s.e , smat.g2s.f );
		
//		System.out.println("DEBUG::::" + layerDebug);
		
		for ( int i = 0 ; i < layerCount ; i++ ){
			svgBounds = smat.getSvgBounds(  layerBounds[i]  );
			layerFileName = href.substring(0 , href.indexOf(".svg")) + "_" + layerNames[i] + ".svg";
			
			hlc.putImage( new Coordinate( svgBounds.getMinX(), svgBounds.getMinY() ), svgBounds.getWidth(), svgBounds.getHeight(), layerFileName );
			if ( layerDebug ){
//				hlc.putPlaneString("<rect x=\"" + (svgBounds.getMinX() ) + "\" y=\"" + (svgBounds.getMinY() ) + "\" width=\"" + svgBounds.getWidth() + "\" height=\"" + svgBounds.getHeight() + "\" fill=\"none\" stroke=\"#A00040\" stroke-width=\"" + (hlcWidth / 1000.0 ) + "\" />\n");
				hlc.putPlaneString("<rect x=\"" + (svgBounds.getMinX() ) + "\" y=\"" + (svgBounds.getMinY() ) + "\" width=\"" + svgBounds.getWidth() + "\" height=\"" + svgBounds.getHeight() + "\" fill=\"none\" stroke=\"#A00040\" stroke-width=\"0.5\" vector-effect=\"non-scaling-stroke\" stroke-linejoin=\"bevel\" />\n");
			}
			
		}
		hlc.putFooter();
	}
	
	boolean strIsSJIS = true;
	String getKanjiProp( String input ){
		String ans ="";
		try {
			if ( strIsSJIS ){
				// 2013/02/15 WINDOWS...
//				ans =  (new String(((String)input).getBytes("iso-8859-1"),"Shift_JIS")).trim();
				ans =  (new String(((String)input).getBytes("iso-8859-1"),"Windows-31J")).trim();
			} else {
				ans =  (new String(((String)input).getBytes("iso-8859-1"),"UTF-8")).trim();
			}
		} catch (Exception e){
			ans = "";
		}
		return ( ans );
	}
	
	String sjis2str(String input ){
		String ans ="";
		try {
			ans =  (new String(((String)input).getBytes("iso-8859-1"),"Windows-31J")).trim();
		} catch (Exception e){
			ans = "";
		}
	return ( ans );
	}
	
	
	//制御コードを削除
    String delCC(String S){
        StringBuffer SB=new StringBuffer();
        for(int i=0;i<S.length();i++){
            SB.append(Full((int)S.charAt(i)));
        }
        return(SB.toString());
    }
    char Full(int c){
    	if( c<32 ){
    		c=95;
    	}
        return (char)c;
    }
	
	// Global QuadKeyTilingに関する関数群 2013/3/28
	double getPartDeg(int lvl){
		double partD = 360.0;
		for ( int i = 0 ; i < lvl ; i++){
			partD = partD / 2.0;
		}
		return ( partD );
	}
	
	Envelope getGlobalTileArea( Envelope bounds  , int level , int[] part){
		double tileStep = getPartDeg(level);
		double geoXstart = (int)((bounds.getMinX() + 180.0)/ tileStep ) * tileStep - 180.0;
		double geoYstart = (int)((bounds.getMinY() + 180.0)/ tileStep ) * tileStep - 180.0;
		
		part[0] = (int)Math.ceil( (bounds.getMaxX()-geoXstart)/tileStep);
		part[1] = (int)Math.ceil( (bounds.getMaxY()-geoYstart)/tileStep);
		Envelope ans = new Envelope(geoXstart , geoXstart + tileStep * part[0] , geoYstart , geoYstart + tileStep * part[1] );
//		System.out.println("part:" + part[0]+","+part[1]);
		return ( ans );
	}
	
}