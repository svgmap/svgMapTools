package org.svgmap.shape2svgmap;

// SvgMapの集合からなるタイルのクラスです
// Rev19から複数に分離され、こちらはスレッドマネージャ SVGMapTileTM
//
// Copyright 2007 by Satoru Takagi
// 2007.5.10 : Ver.1
// 2010.08.19 geoTools 2.6.5 support
// 2013.08.06 タイリングのシステムを大きくリストラした　ただし、この結果レア(だが無視できない)なバグが生じている（詳細はShp2SVGMap18参照）
// 2014.02.13 ヒープ抑制の検討開始
// 2014.02.14 [][]配列が莫大なことが原因であることが判明、HashSetによる仮想配列化に取り組んだ(メジャーバージョンアップ。変更部分は多数です！)
// 2014.03.27 タイルされていないときにおかしくなるのを修正
// 2014.05.01 setShadowGroup debug
// 2014.10.14 case '\\' debug
// 2015.04.17 カンマがメタデータに入っているときのmetaembed2誤動作防ぐ
// 2015.09.04 pStepを変更(一個減らした コンテナ大きくなりすぎる)、tileAreaRectを<path>から<rect>に改善
//
// === リファクタリング クラス名も変更 Shape2SVGMap19用のモジュール
//
// 2016.04.07 二度目のマルチスレッド化チャレンジを開始　今回はSVGMapTilesレベルでマルチスレッドを実装してみる。各コマンドを、メソッドと同名のインナークラスとして規定し、コマンドシーケンスをインスタンス・配列化してバッファリングし、今後新設するSVGMapTilesThreadに投入するメカニズムのアーキテクチャ検証と準備工事（リファクタリング）を開始
// 2016.04.15 上記アーキテクチャでようやく安定しつつある
// 2016.05.12 まだスレッドセーフでない部分があり、改修中。特にoutofsize判定部分がひどい。
// 2016.10.31 CustomAttrをsvgMapTilesでも使えるように
//
// === リファクタリング 高性能化
// 2017.04.19 PointGeomでのclipingループを消去。subTileのArrayList->HashSet化、ThreadのExecutor化 (参考:http://java-study.blog.jp/archives/1036862519.html)
// 2017.05.12 二次元ハッシュに盛大なバグ・・修正
// 2017.09.15 メッセージのマイナー改良(気にすることは何もない)
// 2018.06.28 バグ修正 clipingループ消去ルーチンに問題
// 2018.07.04 オーバーフローしたタイルは早々にクローズしFile Open数をなるべく減らす。

// ISSUES:
// putHeaderはマルチスレッド化準備してない（たぶん必要ない）　そのため、ファイル冒頭のcommentパートの入り方が変わってしまう（とにかくまずはヘッダまでは入る感じ）
// putCommentはちょっと怪しい感じがする・・(containerのみにコメントを入れるという処理)
// idを入れると破たんする。(スレッド間でIDが重複を起こす・・) -noidオプションをデフォルトとしておくが・・・



import java.io.*;
import java.io.IOException;
import java.util.*;
import java.lang.*;
import java.net.URL;
import java.net.URI;
import java.text.NumberFormat ;
import com.vividsolutions.jts.geom.*;
import java.awt.geom.*;
import org.geotools.feature.*;
// import org.geotools.feature.type.*;

import org.opengis.feature.simple.*;

/**
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
**/

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;



public class SvgMapTilesTM {
	public int pStep = 3; // コンテナ階層化のステップ数です 最大4^step数のimageがコンテナに設置 (2015.9.4 変更)
	//SVG Map Tilesは、コンテナとタイルから構成されます。
	//タイルが１個しかないときは、コンテナとタイルは一体となります。
	SvgMap container;
//	SvgMap[][] tiles;
	HashMap<Long,SvgMap> tiles;
	double width , height;
	double minX , minY;
	int wPart , hPart;
	double meshWidth , meshHeight;
	public boolean tiled  = false; // タイル分割されていたらtrue
	boolean hasId = false; // RDFXMLメタデータがあるとき
	boolean hasMicroMeta = false;
	Integer[] linkedMetaIndex;
	String[] linkedMetaName;
	boolean noMetaId = false;
	
	String subFile , href , dir;
	
	SvgMapAffineTransformer smat;
	
	int bitimageGlobalTileLevel = -1;
	
	// 個々のタイルのBBOX
	
	// [tileCount][tileCount]によるオーバーフロー防止のためハッシュに
	// http://www.atmarkit.co.jp/fjava/javatips/081java010.html
	// そもそもこの値は、テーブルにしておくほどのもの？簡単に算出できる気もする 2014.2.13
	HashMap<Long,Double> x0;
	HashMap<Long,Double> x1;
	HashMap<Long,Double> y0;
	HashMap<Long,Double> y1;
	
	
	// 地理メタデータ用
//	boolean[][] hasFigure;
	HashSet<Long> hasFigure; // default: false
	ArrayList<HashSet<Long>> elem = new ArrayList<HashSet<Long>>();
	int elementCount = 0;
	String idPrefix = "f";
	
	// 2007.07.23
	// 階層的タイル分割アルゴリズム用
	// ファイルサイズオーバー検出
	public boolean outOfSize;
	public boolean allTilesOOS; // outOfSizeはtrueで、一つ以上のタイルでサイズオーバーが起きている
//	public boolean[][] parentTileExistence; // 親のタイルの存在（無いもののみ作成する）この配列は、下の配列の半分の大きさ（使うときは２倍に広げて使う）
	HashSet<Long> parentTileExistence; // default: true
	
//	public boolean[][] thisTileExistence; // このレベルのタイルの存在・・本当にこのフラグがtrueのもの全てのタイルが有るわけではなく、親のタイルに無いフラグが付いているものだけが実際に存在している。すなわち、このフラグが立っているものは、その親のレベルに遡っていけばどこかに相当するタイルが存在していることを意味する。　逆に言えば、このフラグがfalseのものは、その下のレベルにタイルが有ることを示している。
	HashSet<Long> thisTileExistence; // default: true　・・・　結局のところ、このセットに入っているものはオーバーフローしたタイル番号(getHashKey( index1 , index2 ))のリストです　2016.5.12
	
	//	public boolean[][] thisTileHasElements;// このタイルの中に有意な図形要素が有るものにフラグが立つ(オーバーフローしていたり、カラのものはfalse)
	HashSet<Long> thisTileHasElements; // default: false
	public int level = 0; // level = 0 : lvl = 1 , lvl = 2^level 再帰タイル分割のレベル
//	public vois set
//	boolean oos; // グローバル変数廃止 2016.5
	String lvls = "";
	boolean hasContainer = false;
	boolean tileDebug = false;
	public boolean isSvgTL = true;
	
	NumberFormat nf;
	
	ExecutorService svgMapExecutorService; // for multi thread 2017.4.19
	ArrayList<SvgMapExporterRunnable> svgMapExporterRunnables;
	
	SvgMapTilesTM(){
		System.out.println("NULL SvgMapTilesTM instanciated");
	}
	
	SvgMapTilesTM(String svgFileName , NumberFormat nf , double xc0 , double yc0 , double w , double h , int wp , int hp , int lvl , HashSet<Long> parentTiles , SvgMapAffineTransformer st , vectorPoiShapes vps , boolean isSvgTLp , int maxThreadsp , ExecutorService svgMapExecutorServicep ) throws Exception{
		isSvgTL = isSvgTLp;
		maxThreads = maxThreadsp;
		svgMapExecutorService = svgMapExecutorServicep;
		
		SvgMapTilesBuilder( svgFileName , nf , xc0 , yc0 , w , h , wp , hp , lvl , parentTiles , st , vps);
	}
	
	// xc0,yc0: コンテンツのbbox原点 w,h:コンテンツのbboxサイズ hp,wp:初期分割数 lvl:再帰分割レベル(初期分割と合わせ実際の分割数に)
	private void SvgMapTilesBuilder(String svgFileName , NumberFormat nf0 , double xc0 , double yc0 , double w , double h , int wp , int hp , int lvl , HashSet<Long> parentTiles , SvgMapAffineTransformer st , vectorPoiShapes vps ) throws Exception{
//		System.out.println("NEW SvgMapTilesTM instanciated");
		
//		MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
//		MemoryUsage usage = mbean.getHeapMemoryUsage();
//		System.out.printf("最大サイズ：%10d%n", usage.getMax());
//		System.out.printf("使用サイズ：%10d%n", usage.getUsed());
		
		nf = nf0;
		smat = st;
		level = lvl;
		int mpl = 1;
		if ( level == 0 ){
			parentTileExistence = new HashSet<Long>();
			for ( int i = 0 ; i < (wp / 2) + 1 ; i++ ){
				for ( int j = 0 ; j < (hp / 2) + 1 ; j++ ){
					set2DHashB(parentTileExistence,i,j,false,true);
				}
			}
		} else {
			parentTileExistence = parentTiles;
		}
		for ( int i = 0 ; i < level ; i++ ){
			mpl = mpl * 2;
		}
		width = w;
		height = h;
		// wPart,hPartはレベル分割と指定分割を合わせた分割数
		// wp,hpは指定分割での指定数
		wPart = wp * mpl;
		hPart = hp * mpl;
		minX = xc0;
		minY = yc0;
		outOfSize = false;
		allTilesOOS = false;
		meshWidth = width / wPart;
		meshHeight = height / hPart;
		
		
		
		tiles = new HashMap<Long,SvgMap>();
		
		thisTileExistence = new HashSet<Long>();
		thisTileHasElements = new HashSet<Long>();
		
		x0 = new HashMap<Long,Double>();
		x1 = new HashMap<Long,Double>();
		y0 = new HashMap<Long,Double>();
		y1 = new HashMap<Long,Double>();
		
		hasFigure = new HashSet<Long>();
		
		System.out.println("Lvl:" + level + " Part:h:" + hPart + " w:" + wPart );
		
		int[] tileIndex;
		if ( hPart == 1 && wPart == 1 ){ // タイルされていない
			container = new SvgMap( svgFileName ,  nf , vps);
			container.isSvgTL = isSvgTL;
			container.appendableCustomAttr = false; // 結局常にappendableはまずい・・・2017.6.30
			set2DHashSM( tiles, 0, 0, container);
			hasContainer = true; // コンテナが有ると詐称してコンテナ自身に図形を描画している・・トリッキー
			tiled = false;
			set2DHashD( x0 , 0 , 0 , minX );
			set2DHashD( y0 , 0 , 0 , minY );
			set2DHashD( x1 , 0 , 0 , minX + meshWidth );
			set2DHashD( y1 , 0 , 0 , minY + meshHeight );
			
			
			tileList = new ArrayList<Object>();
			tileIndex = new int[2];
			tileIndex[0]=0;
			tileIndex[1]=0;
			tileList.add(tileIndex);
			initialEffectiveTileCount = 1;
		} else { // タイルされている
			// レベルが０の場合(meshオプションによるタイル化)、またはlevelが１で、しかもmeshオプションでタイル化されていない場合
			// この判断ルーチンも、一部意味ない感じがする(レベル１ではwp,hp>1しかないため) 2013.8.6
			if ( level == 0 || ( level == 1 && wp == 1 && hp == 1 ) ){
				container = new SvgMap( svgFileName ,  nf , vps);
				container.isSvgTL = isSvgTL;
				hasContainer = true;
			} 
			if ( level > 0 ){
				lvls = "_l" + level;
			}
			tiled = true;
			buildTileList();
			subFile = svgFileName.substring(0 , svgFileName.indexOf(".svg"));
			System.out.println("Total Effective Tiles Count:" + tileList.size() );
			for ( int k = 0 ; k < tileList.size() ; k++ ){
				tileIndex = (int[])tileList.get(k);
				int i = tileIndex[0];
				int j = tileIndex[1];
//						System.out.println("create tile: j,i:" + j+","+i);
						SvgMap tileMap = new SvgMap( subFile + lvls + "_" + j + "-" + i + ".svg" , nf , vps);
						tileMap.appendableCustomAttr = false; // タイリングの場合はCustomAttrは、appendableではなくする。（一発勝負）
						set2DHashSM(tiles, j, i, tileMap ); // これでoverFlow
						(get2DHashSM( tiles, j, i)).isSvgTL = isSvgTL;
						set2DHashD( x0 , j , i , minX + j * meshWidth );
						set2DHashD( y0 , j , i , minY + i * meshHeight );
						set2DHashD( x1 , j , i , minX + j * meshWidth + meshWidth );
						set2DHashD( y1 , j , i , minY + i * meshHeight+ meshHeight );
			}
		}
		buildSubTileSets();
//		svgMapThreads = new ArrayList<Thread>();
		svgMapExporterRunnables = new ArrayList<SvgMapExporterRunnable>();
	}
	
	// 一個でもオブジェクトがあるデータを強制out of sizeさせる 2016.4.8
	public void setForceOos(boolean forceOos){
		for ( int k = 0 ; k < tileList.size() ; k++ ){
				int[] tileIndex = (int[])tileList.get(k);
				int i = tileIndex[0];
				int j = tileIndex[1];
				(get2DHashSM( tiles, j, i)).forceOos = forceOos;
		}
	}
	
	public void setDefauleCustomAttr( boolean isDefaultCustomAttr ){
		for ( int k = 0 ; k < tileList.size() ; k++ ){
				int[] tileIndex = (int[])tileList.get(k);
				int i = tileIndex[0];
				int j = tileIndex[1];
				(get2DHashSM( tiles, j, i)).isDefaultCustomAttr = isDefaultCustomAttr;
		}
	}
	
	ArrayList<Object> tileList; // tileListは、(上の階層まで遡っても)生成されていないタイルのリスト
//	int[] tileIndex; // グローバル変数がマルチスレッド化でひどいバグを引き起こしていた・・・ 2016.04
	long initialEffectiveTileCount;
	private void buildTileList(){
		int[] tileIndex;
		tileList = new ArrayList<Object>();
		
		// parentTileExistence は　falseのものが入っている
		// すなわち、tileListには、(上の階層まで遡っても)生成されていないタイルのリストが生成される。
		Iterator<Long> it = parentTileExistence.iterator();
		while( it.hasNext()){
			int[] idx = getIndex( it.next() );
			for ( int ii = 0 ; ii < 2 ; ii++ ){
				for ( int jj = 0 ; jj < 2 ; jj++ ){
					int i = idx[1] * 2 + ii;
					int j = idx[0] * 2 + jj;
					if ( i < hPart && j < wPart ){
						if ( get2DHashB(thisTileExistence, j, i, true) ){ // この判断は必要？(2014.2.14)
							tileIndex = new int[2];
							tileIndex[0] = i;
							tileIndex[1] = j;
							
							tileList.add(tileIndex);
						}
					}
				}
			}
		}
		
//		printTileList();
		initialEffectiveTileCount = tileList.size();
	}
	
	private void printTileList(){
		for ( int i = 0 ; i < tileList.size() ; i++ ){
			int[] tileIndex = (int[])tileList.get(i);
			System.out.println(tileIndex[1] + ":" + tileIndex[0]);
		}

	}
	
	private class PutHeader extends svgMapCommands{
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).putHeader( minX + j * meshWidth ,minY + i * meshHeight , meshWidth , meshHeight );
			return k;
		}
	}
	public void putHeader( ) throws Exception{
		if ( hasContainer ){
			container.putHeader( minX ,minY , width , height );
		}
		if ( tiled ){
			for ( int k = 0 ; k < tileList.size() ; k++ ){
				int[] tileIndex = (int[])tileList.get(k);
				int i = tileIndex[0];
				int j = tileIndex[1];
				(get2DHashSM( tiles, j, i)).putHeader( minX + j * meshWidth ,minY + i * meshHeight , meshWidth , meshHeight );
			}
		}
	}
	
	
	String defaultFill = "";
	String tileRectFill = defaultFill;
	
	private class SetDefaultStyle extends svgMapCommands{
		String defFill;
		double defStrokeWidth;
		String defStroke;
		double defOpacity;
		boolean vectorEffect;
		SetDefaultStyle( String defFill , double defStrokeWidth , String defStroke , double defOpacity , boolean vectorEffect ){
			this.defFill = defFill;
			this.defStrokeWidth = defStrokeWidth;
			this.defStroke = defStroke;
			this.defOpacity = defOpacity;
			this.vectorEffect = vectorEffect;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).setDefaultStyle( defFill , defStrokeWidth , defStroke , defOpacity , vectorEffect );
			return k;
		}
	}
	public void setDefaultStyle( String defFill , double defStrokeWidth , String defStroke , double defOpacity , boolean vectorEffect ) throws Exception{
		defaultFill = defFill;
		if ( tileRectFill.equals("") ){
			tileRectFill = defaultFill;
		}
		svgMapCommands pu = new SetDefaultStyle( defFill , defStrokeWidth , defStroke , defOpacity , vectorEffect );
		bufferedDraw( pu , false );
	}
	
	public void setTileRectFill( String tFill ){
		tileRectFill = tFill;
	}
	
	private class SetDefaultCaptionStyle extends svgMapCommands{
		double defaultFontSize;
		boolean strokeGroup;
		SetDefaultCaptionStyle( double defaultFontSize , boolean strokeGroup ){
			this.defaultFontSize = defaultFontSize;
			this.strokeGroup = strokeGroup;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).setDefaultCaptionStyle( defaultFontSize , strokeGroup );
			return k;
		}
	}
	public void setDefaultCaptionStyle( double defaultFontSize , boolean strokeGroup ) throws Exception{
		for ( int k = 0 ; k < tileList.size() ; k++ ){
			int[] tileIndex = (int[])tileList.get(k);
			int i = tileIndex[0];
			int j = tileIndex[1];
			(get2DHashSM( tiles, j, i)).setDefaultCaptionStyle( defaultFontSize , strokeGroup );
//					checkOutOfSize( oos , j , i );
		}
	}
	
	
	public void putFooter() throws Exception{
		putFooter( 0 , null );
	}
	
	public void putFooter( int densityControl ) throws Exception{
		putFooter( densityControl , null );
	}
	
	public HashMap<Long,Integer> divErr = null;
	
	public HashMap<Long,Integer> putFooter( int densityControl , HashMap<Long,Integer> parentDivErr ) throws Exception{
		// 分割エラーカウンターを追加 2014.2.17
		// 返却値: 親から分割された４個のタイルのうち１個しかタイルが生成できなかった場合、この値がインクリメントされる Keyはタイルの番号
		// parentDivErr: 親の分割エラーカウンター（呼び元から与えて使う必要がある）　各タイル(hash)に対する階層的エラー連続数が入る
		
		// 2017.4.19 本当にlimitを超えたかどうかは各svgmaptileのputfooter close()をしないと判明しないので、
		// 本来は、まずは各タイルのputfooterをかけた後に、limit越えを再度確認してthisTileHasElements設定などの処理をすべき
		
		bufferedDraw(null,true); // 描画コマンドバッファのフラッシュ : スレッド毎のthisTileExistence(overflowlist)も纏まる(2016.4.8)
		
		divErr = null;
		
		if ( tiled ){
//			System.out.println("initialTileListSize:"+tileList.size());
			aggregateTileList(); // tileList：この時点でオーバーフローしてないタイルのリストになる（ただし要素が無いタイルはまだあり得る）
//			System.out.println("overflowedTileSize:"+thisTileExistence.size()+"   ReducedTilieListSize:"+tileList.size());
			
			if ( hasId ){
				elem.add(hasFigure);
			}
			int p = subFile.lastIndexOf("\\");
			if (p<0){
				p = subFile.lastIndexOf("/");
			}
			if (p<0){
				p=0;
			} else {
				p = p + 1;
			}
			href = subFile.substring(p);
			dir = subFile.substring(0,p);
			
			
			// このタイルがデータを持っていることを示すフラグをthisTileHasElementsに立てて完了する
			// オーバーフローもしていないし、カラでもないタイル
			for ( int k = 0 ; k < tileList.size() ; k++ ){
				int[] tileIndex = (int[])tileList.get(k);
				int i = tileIndex[0];
				int j = tileIndex[1];
				if ( (get2DHashSM( tiles, j, i)).nElements > 0 ){
					set2DHashB( thisTileHasElements, j, i, true, false);
				}
			}
			
			
			if ( parentDivErr != null ){
				divErr = new HashMap<Long,Integer>();
				// 兄弟の他の三つのタイルに要素が存在していない場合、分割エラー
				Iterator<Long> it = parentTileExistence.iterator();
				while( it.hasNext()){
					int[] idx = getIndex( it.next() );
					int divc = 0;
					for ( int ii = 0 ; ii < 2 ; ii++ ){
						for ( int jj = 0 ; jj < 2 ; jj++ ){
							int i = idx[1] * 2 + ii;
							int j = idx[0] * 2 + jj;
							if ( i < hPart && j < wPart ){
								if ( get2DHashB( thisTileHasElements, j, i, false) || ! get2DHashB( thisTileExistence, j, i, true) ){ // このタイルが要素を持つか、オーバーフローしている場合
									++ divc;
								}
							}
						}
					}
					for ( int ii = 0 ; ii < 2 ; ii++ ){
						for ( int jj = 0 ; jj < 2 ; jj++ ){
							int i = idx[1] * 2 + ii;
							int j = idx[0] * 2 + jj;
							if ( i < hPart && j < wPart ){
								if ( divc > 1 ){ // ２個以上のタイルにエレメントがある場合はエラークリアする
									set2DHashI( divErr, j, i, 0 );
								} else { // そうでない場合、親のエラーカウント＋１して入れる
									set2DHashI( divErr, j, i, get2DHashI( parentDivErr, idx[0], idx[1] ) + 1 );
								}
							}
						}
					}
				}
			}
			
			
			
			if ( hasId ){
				putMetadataToTiles(  );
			}
			
			
			// 
			// parentTileExistence は　falseのものが入っている
			Iterator <Long> it = parentTileExistence.iterator();
			while( it.hasNext()){
				int[] idx = getIndex( it.next() );
				for ( int ii = 0 ; ii < 2 ; ii++ ){
					for ( int jj = 0 ; jj < 2 ; jj++ ){
						int i = idx[1] * 2 + ii;
						int j = idx[0] * 2 + jj;
						if ( i < hPart && j < wPart ){
							if ( get2DHashB(thisTileExistence, j, i, true) ){ // この判断は必要？(2014.2.14)
//								System.out.print("sv File:"+ j + ","+i+"  :");
//								System.out.println (" : Size:"+(get2DHashSM( tiles, j, i)).svgFile.length()+"  limit:"+(get2DHashSM( tiles, j, i)).limit);
								
								(get2DHashSM( tiles, j, i)).putFooter();
							}
							if ( ! get2DHashB(thisTileExistence, j, i, true) ){
//								System.out.print("rm File:"+ j + ","+i+"  :");
//								System.out.println (" : Size:"+(get2DHashSM( tiles, j, i)).svgFile.length()+"  limit:"+(get2DHashSM( tiles, j, i)).limit);
								(get2DHashSM( tiles, j, i)).removeFile();
							}
						}
					}
				}
			}
			
		}
		
		if ( hPart * wPart == 1 ){ // タイルされてない 上のelseで良いのでは？
			divErr = new HashMap<Long,Integer>();
			set2DHashI( divErr, 0, 0, 0 );
			set2DHashB( thisTileHasElements, 0, 0, true, false);
			container.putFooter();
		}
		
		
		System.out.println("TileCount: Effective:" + initialEffectiveTileCount + " Generated:" + thisTileHasElements.size()  + " Empty:" +  (tileList.size() - thisTileHasElements.size()) + " Overflowed:" + thisTileExistence.size() );
		return ( divErr );
	}
	
	String layerMetadata="";
	// レイヤールートコンテナに対して<metadata>要素内に任意のめたーデータ文字列を追加する機能  2019/1/24
	// escapeが必要ならば、SvgMapTilesTM.htmlEscape()などを使って、自分で行ったものを入れてください。
	// putCrs()前ならいつ呼んでも良い
	public void setLayerMetadata(String layerMetadata){
		this.layerMetadata = layerMetadata;
	}
	
	double commonA, commonB, commonC, commonD, commonE, commonF;
	public void putCrs( double a ,  double b , double c , double d , double e , double f ) throws Exception{
		commonA = a;
		commonB = b;
		commonC = c;
		commonD = d;
		commonE = e;
		commonF = f;
		if ( hasContainer ){
			if ( layerMetadata.length() > 0 ){
				container.setUserMetadata( layerMetadata );
			}
			container.putCrs(  a ,  b , c , d , e , f );
		}
		if ( tiled ){
			for ( int k = 0 ; k < tileList.size() ; k++ ){
				int[] tileIndex = (int[])tileList.get(k);
				int i = tileIndex[0];
				int j = tileIndex[1];
						(get2DHashSM( tiles, j, i)).putCrs(  a ,  b , c , d , e , f );
//						checkOutOfSize( oos , j , i );
			}
		}
	}
	
	private class PutComment extends svgMapCommands{
		// コンテナのみにコメントを入れる
		String comment;
		PutComment( String comment ){
			this.comment = comment;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			if ( i == 0 && j == 0 ){
				(get2DHashSM( tiles, j, i)).putComment( comment );
			}
			return k;
		}
	}
	public void putComment( String comment ) throws Exception{
		if ( !tiled ){
			svgMapCommands pu = new PutComment( comment );
			bufferedDraw( pu , false );
		} else {
//		System.out.println("called putComment hasContainer?:"+hasContainer + "  val:"+comment);
			if ( hasContainer ){
				container.putComment( comment );
			}
		}
	}
	
	private class PutCommentToAll extends svgMapCommands{
		String comment;
		PutCommentToAll( String comment ){
			this.comment = comment;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).putComment( comment );
			return k;
		}
	}
	public void putCommentToAll( String comment ) throws Exception{
		svgMapCommands pu = new PutCommentToAll( comment );
		bufferedDraw( pu , false );
	}
	
	private class SetId extends svgMapCommands{
		String idNumb;
		SetId( String idNumb ){
			this.idNumb = idNumb;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).setId( idNumb );
			return k;
		}
	}
	public void setId( ) throws Exception{
		if ( tiled ){
			if ( elementCount > 0 ){
				elem.add(hasFigure);
			} else {
				hasId = true;
			}
		}
		hasFigure = new HashSet<Long>();
		svgMapCommands pu = new SetId( idPrefix + elementCount );
		bufferedDraw( pu , false );
		++ elementCount;
	}
	
	private class SetAnchor extends svgMapCommands{
		String title;
		String link;
		SetAnchor(String title , String link){
			this.title = title;
			this.link = link;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).setAnchor(title , link) ;
			return k;
		}
	}
	public void setAnchor(String title , String link) throws Exception{
		svgMapCommands pu = new SetAnchor( title , link);
		bufferedDraw( pu , false );
	}
	
	private class TermAnchor extends svgMapCommands{
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).termAnchor() ;
			return k;
		}
	}
	public void termAnchor() throws Exception {
		svgMapCommands pu = new TermAnchor();
		bufferedDraw( pu , false );
	}
	
	
	private class PutPolyline extends svgMapCommands{
		Coordinate[] coord = null;
		String strokeColor;
		double strokeWidth;
		double opacity;
		
		PolygonDouble pol = null;
		Envelope env = null;
		
		PutPolyline( Coordinate[] coord , String strokeColor , double strokeWidth , double opacity ){
			this.coord = coord;
			this.strokeColor = strokeColor;
			this.strokeWidth = strokeWidth;
			this.opacity = opacity;
		}
		
		PutPolyline( PolygonDouble pol , String strokeColor , double strokeWidth , Envelope env , double opacity ){
			this.pol = pol;
			this.strokeColor = strokeColor;
			this.strokeWidth = strokeWidth;
			this.env = env;
			this.opacity = opacity;
		}
		
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			PolygonDouble answer = null;
			if ( pol == null ){
				answer = clip( j , i , coord , false);
			} else {
				answer = clip( j , i , pol , false , env );
			}
			
			if (answer.npoints > 1){
				boolean oos = (get2DHashSM( tiles, j, i)).putPolyline( answer , strokeColor , strokeWidth , opacity );
				k = checkOutOfSize( oos , j , i , k , subTileInfo );
			}
			return ( k );
		}
	}
	
	public void putPolyline( Coordinate[] coord , String strokeColor , double strokeWidth , double opacity ) throws Exception{
		svgMapCommands pu = new PutPolyline( coord , strokeColor , strokeWidth , opacity );
		bufferedDraw( pu , false );
	}
	
	public void putPolyline( PolygonDouble pol , String strokeColor , double strokeWidth , Envelope env , double opacity ) throws Exception{
		svgMapCommands pu = new PutPolyline( pol , strokeColor , strokeWidth , env , opacity );
		bufferedDraw( pu , false );
	}
	
	private class SetInterior extends svgMapCommands{
		Coordinate[] coord = null;
		PolygonDouble pol = null;
		Envelope env = null;
		SetInterior( Coordinate[] coord ){
			this.coord = coord;
		}
		
		SetInterior( PolygonDouble pol , Envelope env ){
			this.pol = pol;
			this.env = env;
		}
		
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			PolygonDouble answer;
			if ( coord == null ){
				answer= clip( j , i , pol , true , env );
			} else {
				answer = clip( j , i , coord , true);
			}
			if (answer.npoints > 1){
				(get2DHashSM( tiles, j, i)).setInterior( answer );
//						checkOutOfSize( oos , j , i );
			}
			return ( k );
		}
		
	}
	
	public void setInterior( Coordinate[] coord ) throws Exception{
		svgMapCommands pu = new SetInterior( coord );
		bufferedDraw( pu , false );
	}
	
	public void setInterior( PolygonDouble pol , Envelope env ) throws Exception{
		svgMapCommands pu = new SetInterior( pol , env );
		bufferedDraw( pu , false );
	}
	
	
	private class SetExterior extends svgMapCommands{
		Coordinate[] coord=null;
		PolygonDouble pol=null;
		Envelope env=null;
		SetExterior( Coordinate[] coord ){
			this.coord = coord;
		}
		
		SetExterior( PolygonDouble pol , Envelope env ){
			this.pol = pol;
			this.env = env;
		}
		
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			PolygonDouble answer;
			if ( coord == null ){
				answer= clip( j , i , pol , true , env );
			} else {
				answer = clip( j , i , coord , true);
			}
			if (answer.npoints > 1){
				(get2DHashSM( tiles, j, i)).setExterior( answer );
//						checkOutOfSize( oos , j , i );
			}
			return ( k );
		}
	}
	
	public void setExterior( Coordinate[] coord ) throws Exception{
		svgMapCommands pu = new SetExterior( coord );
		bufferedDraw( pu , false );
	}
	
	public void setExterior( PolygonDouble pol , Envelope env ) throws Exception{
		svgMapCommands pu = new SetExterior( pol , env );
		bufferedDraw( pu , false );
	}
	
	private class PutPolygon extends svgMapCommands{
		String fillColor; 
		double strokeWidth;
		String strokeColor;
		double opacity;
		PutPolygon(String fillColor , double strokeWidth , String strokeColor , double opacity ){
			this.fillColor = fillColor;
			this.strokeWidth = strokeWidth;
			this.strokeColor = strokeColor;
			this.opacity = opacity;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			boolean oos = (get2DHashSM( tiles, j, i)).putPolygon( fillColor , strokeWidth , strokeColor , opacity );
			return( checkOutOfSize( oos , j , i , k , subTileInfo ));
		}
	}
	
	public void putPolygon(String fillColor , double strokeWidth , String strokeColor ) throws Exception {
		putPolygon( fillColor , strokeWidth , strokeColor , 1.0 );
	}
	
	public void putPolygon(String fillColor , double strokeWidth , String strokeColor , double opacity ) throws Exception {
		svgMapCommands pu = new PutPolygon( fillColor , strokeWidth , strokeColor , opacity );
		bufferedDraw( pu , false );
	}
	
	
	private class SetGroup extends svgMapCommands{
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).setGroup();
			return k;
		}
	}
	public void setGroup() throws Exception{
		svgMapCommands pu = new SetGroup();
		bufferedDraw( pu , false );
	}
	
	private class TermGroup extends svgMapCommands{
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).termGroup();
			return k;
		}
	}
	public void termGroup() throws Exception{
		svgMapCommands pu = new TermGroup();
		bufferedDraw( pu , false );
	}
	
	// geomertry collection(本来はGroup)にmetadataが設定されていると、
	// それが最初のgeometryにしか設定されないことへの対策 2014.5.1
	private class SetShadowGroup extends svgMapCommands{
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).setShadowGroup();
			return k;
		}
	}
	public void setShadowGroup()  throws Exception{
		svgMapCommands pu = new SetShadowGroup();
		bufferedDraw( pu , false );
	}
	private class TermShadowGroup extends svgMapCommands{
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).termShadowGroup();
			return k;
		}
	}
	public void termShadowGroup() throws Exception{
		svgMapCommands pu = new TermShadowGroup();
		bufferedDraw( pu , false );
	}
	
	public void putText( Coordinate coo , double size , String attr ) throws Exception{
		putText( coo , size , attr , false , 0 );
	}
	
	public void putText( Coordinate coo , double size , String attr , boolean abs ) throws Exception{
		putText( coo , size , attr , abs , 0 );
	}
	
	
	private class PutText extends svgMapCommands{
		Coordinate coo;
		double size;
		String attr;
		boolean abs;
		double textShift;
		PutText( Coordinate coo , double size , String attr , boolean abs , double textShift ){
			this.coo = coo;
			this.size = size;
			this.attr = attr;
			this.abs = abs;
			this.textShift = textShift;
			
			// pointジオメトリ高速化のためのヒント設定 2017.4.17
			int[] tn = getIncludedTileIndex(coo);
			super.pointHintKey = getHashKey(tn[1],tn[0]); // 逆かも・・・
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			if ( k== -1 || isInclude ( j , i , coo ) ){ // k== -1 はpointHintKey時、意味ないisIncludeを動かさないための仮設のHackです 2017.4.18
				boolean oos = (get2DHashSM( tiles, j, i)).putText( coo , size , attr , abs , textShift );
				k = checkOutOfSize( oos , j , i , k , subTileInfo );
			}
			return ( k );
		}
	}
	
	public void putText( Coordinate coo , double size , String attr , boolean abs , double textShift ) throws Exception{
		svgMapCommands pu = new PutText( coo , size , htmlEscape( attr ) , abs , textShift ); // 2016.10.17debug
		bufferedDraw( pu , false );
	}
	
	
	
	// poiのデフォルトサイズを設定する
	private class SetDefaultPoiSize extends svgMapCommands{
		double flag;
		SetDefaultPoiSize( double flag ){
			this.flag = flag;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).defaultPoiSize = flag;
			return ( k );
		}
	}
	
	public void setDefaultPoiSize( double flag ) throws Exception{
		svgMapCommands pu = new SetDefaultPoiSize( flag );
		bufferedDraw( pu , false );
	}
	
	
	private class PutUse extends svgMapCommands{
		Coordinate coo;
		String fillColor;
		boolean fixed;
		String symbolId = null;
		
		PutUse( Coordinate coo , String fillColor , boolean fixed , String symbolId ){
			this.coo = coo;
			this.fillColor = fillColor;
			this.fixed = fixed;
			this.symbolId = symbolId;
			
			// pointジオメトリ高速化のためのヒント設定 2017.4.17
			int[] tn = getIncludedTileIndex(coo);
			super.pointHintKey = getHashKey(tn[1],tn[0]); // 逆かも・・・
		}
		
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			if ( k== -1 || isInclude ( j , i , coo ) ){
				boolean oos;
				if ( fixed == false && symbolId==null ){
					oos = (get2DHashSM( tiles, j, i)).putUse( coo , fillColor );
				} else if ( symbolId == null ){
					oos = (get2DHashSM( tiles, j, i)).putUse( coo , fillColor , fixed );
				} else {
					oos = (get2DHashSM( tiles, j, i)).putUse( coo , fillColor , fixed , symbolId );
				}
				k = checkOutOfSize( oos , j , i , k , subTileInfo );
			}
			return ( k );
		}
		
	}
	
	// 2013.10.21 add
	public void putUse( Coordinate coo , String fillColor , boolean fixed , String symbolId ) throws Exception{
		svgMapCommands pu = new PutUse( coo , fillColor , fixed , symbolId );
		bufferedDraw( pu , false );
	}
	
	public void putUse( Coordinate coo , String fillColor , boolean fixed ) throws Exception{
		svgMapCommands pu = new PutUse( coo , fillColor , fixed , null );
		bufferedDraw( pu , false );
	}
	
	public void putUse( Coordinate coo , String fillColor ) throws Exception{
		svgMapCommands pu = new PutUse( coo , fillColor , false , null );
		bufferedDraw( pu , false );
	}
	
	public void putSymbol( String templateData ) throws Exception{ // added 2013.3.11 カスタムシンボル定義を各タイルに設定
		// putSymbolの前に呼ばないと意味がない
		for ( int k = 0 ; k < tileList.size() ; k++ ){
			int[] tileIndex = (int[])tileList.get(k);
			int i = tileIndex[0];
			int j = tileIndex[1];
			boolean success = (get2DHashSM( tiles, j, i)).putSymbol( templateData );
			if ( !success ){
				System.out.println("ERROR!!! : Can't set custom symbol definition. EXIT.");
				System.exit(0);
			}
		}
	}
	
	public void putSymbol(double size ) throws Exception{
		for ( int k = 0 ; k < tileList.size() ; k++ ){
			int[] tileIndex = (int[])tileList.get(k);
			int i = tileIndex[0];
			int j = tileIndex[1];
			boolean oos = (get2DHashSM( tiles, j, i)).putSymbol( size );
		}
	}
	
	public void putSymbol(double size , double fixedStrokeWidth ) throws Exception{
		for ( int k = 0 ; k < tileList.size() ; k++ ){
			int[] tileIndex = (int[])tileList.get(k);
			int i = tileIndex[0];
			int j = tileIndex[1];
			boolean oos = (get2DHashSM( tiles, j, i)).putSymbol( size , fixedStrokeWidth );
		}
	}
	
	// 2012.7.30 add (POI改修に伴い)
	private class PutPoiShape extends svgMapCommands{
		Coordinate coo;
		int type;
		double poiSize;
		String fillColor;
		double strokeWidth;
		String strokeColor;
		boolean nonScalingStroke;
		boolean nonScalingObj;
		PutPoiShape( Coordinate coo , int type , double poiSize , String fillColor , double strokeWidth , String strokeColor , boolean nonScalingStroke , boolean nonScalingObj ){
			this.coo = coo;
			this.type = type;
			this.poiSize = poiSize;
			this.fillColor = fillColor;
			this.strokeWidth = strokeWidth;
			this.strokeColor = strokeColor;
			this.nonScalingStroke = nonScalingStroke;
			this.nonScalingObj = nonScalingObj;
			
			// pointジオメトリ高速化のためのヒント設定 2017.4.17
			int[] tn = getIncludedTileIndex(coo);
			super.pointHintKey = getHashKey(tn[1],tn[0]); // 逆かも・・・
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			if ( k== -1 || isInclude ( j , i , coo ) ){
				boolean oos = (get2DHashSM( tiles, j, i)).putPoiShape( coo , type , poiSize , fillColor , strokeWidth , strokeColor , nonScalingStroke , nonScalingObj );
				// リミッターを超えたら、oosはfalseを返す
				k = checkOutOfSize( oos , j , i , k , subTileInfo );
			}
			return k;
		}
	}
	public void putPoiShape( Coordinate coo , int type , double poiSize , String fillColor , double strokeWidth , String strokeColor , boolean nonScalingStroke , boolean nonScalingObj ) throws Exception{
		svgMapCommands pu = new PutPoiShape( coo , type , poiSize , fillColor , strokeWidth , strokeColor , nonScalingStroke , nonScalingObj );
		bufferedDraw( pu , false );
	}
	
	
	private boolean isInclude(int w , int h , Coordinate coo ){
		if (tiled){
			boolean ret = false;
//System.out.println("include:" +  coo.x + "," + coo.y + "  bb:" + x0[w][h] + "," + y0[w][h] + " " + x1[w][h] + "," + y1[w][h]);
//			if ( coo.x >= x0[w][h] && coo.x <= x1[w][h] && coo.y >= y0[w][h] && coo.y <= y1[w][h] ){}
//			if ( coo.x >= get2DHashD( x0, w, h ) && coo.x <= get2DHashD( x1, w, h ) && coo.y >= get2DHashD( y0, w, h ) && coo.y <= get2DHashD( y1, w, h ) ){} // ジャストの値の時に、隣のタイルと重複して生成されてしまう問題のfix 2016.8.30
			if ( coo.x >= get2DHashD( x0, w, h ) && coo.x < get2DHashD( x1, w, h ) && coo.y >= get2DHashD( y0, w, h ) && coo.y < get2DHashD( y1, w, h ) ){
				ret = true;
				if ( hasId ){
					set2DHashB( hasFigure, w, h, true, false);
				}
			}
	//		System.out.println( x0[w][h] + "," + x1[w][h] + "," + y0[w][h] + "," + y1[w][h]);
	//		System.out.println( "w:" + w + " h:" + h + " coo:" + coo + " ret:" + ret );
		return ( ret );
		} else {
			return (true);
		}
	}
	
	// 2017.4.17 point系要素の処理の高速化（ループ省略）のため、pointの座標値から該当するタイル番号を直算出する
	// ToDo: BBOX(線・面)版も準備すべき
	private int[] getIncludedTileIndex( Coordinate coo ){
		int[] ans = new int[2];
		
		ans[1] = (int)( ( coo.x - minX ) / meshWidth ); // for j
		ans[0] = (int)( ( coo.y - minY ) / meshHeight ); // for i
		
		if ( ans[1] >= wPart ){ // debug 2018/6/28 上の式だと最大値がはみ出ちゃいます・・・
//			System.out.println("ERR tile width number exceeds : "+coo.x);
			ans[1] = wPart -1;
		}
		if ( ans[0] >= hPart ){
//			System.out.println("ERR tile height number exceeds : "+coo.y);
			ans[0] = hPart -1;
		}
		
//		System.out.println( "getIncludedTileIndex" + ans[0]+","+ans[1]);
		
//		System.out.println( "giti:"+ coo.x +","+ minX +","+ meshWidth);
		
		return ( ans );
	}
	
	
	private PolygonDouble clip( int w , int h , Coordinate[] coord , boolean filled ){
//		Rectangle2D.Double rect = new Rectangle2D.Double( x0[w][h] , y0[w][h] , meshWidth , meshHeight );
		Rectangle2D.Double rect = new Rectangle2D.Double( get2DHashD( x0, w, h ) , get2DHashD( y0, w, h ) , meshWidth , meshHeight );
//		System.out.println("clip:Copy to PolugonDouble:" + coord.length);
		PolygonDouble pol= new PolygonDouble(coord.length);
		for ( int i = 0 ; i < coord.length ; i++ ){
			pol.addPoint( coord[i].x , coord[i].y );
		}
		if (! filled ){
		// fill=noneのパス・ポリラインは、始点と終点は結んではいけないので、始点がクリップされた端点としておく
			pol.clippedEdge[0]=true;
		}
//		System.out.println("clip:StartClipping");
		
		if ( tiled ){
			ClipPolygonDouble cp = new ClipPolygonDouble(pol , rect);
			if ( hasId && cp.lastClipped.npoints > 1 ){
				set2DHashB( hasFigure, w, h, true, false);
			}
			return ( cp.getClippedPolygon() );
		} else {
			return(pol);
		}
	}
	
	private PolygonDouble clip( int w , int h , PolygonDouble pol , boolean filled , Envelope env){
		// これもいちいち作っているのはどうかと・・・
		PolygonDouble ans;
//		Rectangle2D.Double rect = new Rectangle2D.Double( x0[w][h] , y0[w][h] , meshWidth , meshHeight );
		Rectangle2D.Double rect = new Rectangle2D.Double( get2DHashD( x0, w, h ) , get2DHashD( y0, w, h ) , meshWidth , meshHeight );
		//		System.out.println("clip:Copy to PolugonDouble:" + coord.length);
		
		if (! filled ){
		// fill=noneのパス・ポリラインは、始点と終点は結んではいけないので、始点がクリップされた端点としておく
			pol.clippedEdge[0]=true;
		}
//		System.out.println("clip:StartClipping");
		
		if ( tiled ){
//			Envelope tileEnv = new Envelope( x0[w][h] , x0[w][h]+meshWidth , y0[w][h] , y0[w][h]+meshHeight );
			Envelope tileEnv = new Envelope( get2DHashD( x0, w, h ) , get2DHashD( x0, w, h )+meshWidth , get2DHashD( y0, w, h ) , get2DHashD( y0, w, h )+meshHeight );
			if (tileEnv.contains(env)){
//				System.out.print("cont:");
				set2DHashB( hasFigure, w, h, true, false);
				ans = pol;
			} else if ( tileEnv.intersects( env ) ){
//				System.out.print("sect:");
				ClipPolygonDouble cp = new ClipPolygonDouble(pol , rect);
				if ( hasId && cp.lastClipped.npoints > 1 ){
					set2DHashB( hasFigure, w, h, true, false);
				}
				ans = cp.getClippedPolygon();
			} else {
//				System.out.print("outs:");
				// NULLです。
				ans = new PolygonDouble(1);
			}
			
			return ( ans );
		} else {
			return(pol);
		}
	}

	SimpleFeatureType readFT;
	FeatureCollection<SimpleFeatureType,SimpleFeature> fsShape;
	String metaNs , metaUrl;
	public void setMetadata( SimpleFeatureType rFT , FeatureCollection<SimpleFeatureType,SimpleFeature> fS , String mns , String murl ) throws Exception{
		readFT = rFT;
		fsShape = fS;
		metaNs = mns;
		metaUrl = murl;
		if ( tiled == false ){
			putMetadataToContainer(  );
		}
	}
	
	public void setMicroMeta2Header( SimpleFeatureType rFT , boolean useTitleAttrParam ) throws Exception{
		useTitleAttr = useTitleAttrParam;
		hasMicroMeta = true;
		readFT = rFT;
		metaNs = "";
		metaUrl = "";
		String metaSchema ="";
		for ( int i = 0 ; i < linkedMetaName.length ; i ++ ){
			if ( linkedMetaName[i].indexOf("the_geom") == -1 ){
				metaSchema += linkedMetaName[i];
				if ( i < linkedMetaName.length -1 ){
					metaSchema += ",";
				}
			}
		}
		for ( int k = 0 ; k < tileList.size() ; k++ ){
			int[] tileIndex = (int[])tileList.get(k);
			int i = tileIndex[0];
			int j = tileIndex[1];
			(get2DHashSM( tiles, j, i)).setMicroMeta2Header( metaSchema );
		}
	}
	
	public void setMicroMetaHeader( SimpleFeatureType rFT , String mns , String murl ) throws Exception{
		hasMicroMeta = true;
		readFT = rFT;
		metaNs = mns;
		metaUrl = murl;
		for ( int k = 0 ; k < tileList.size() ; k++ ){
			int[] tileIndex = (int[])tileList.get(k);
			int i = tileIndex[0];
			int j = tileIndex[1];
			(get2DHashSM( tiles, j, i)).setMicroMetaHeader( metaNs , metaUrl);
		}
	}
	
	private void putMetadataToContainer(  ) throws Exception{
		if ( hasContainer ){
			SimpleFeature oneFeature;
			Object value;
			double dval=0;
			int i , j;
			FeatureIterator<SimpleFeature> reader = fsShape.features();
			j = 0;
			putMetaHeader(container);
			
			while (reader.hasNext()) {
				oneFeature = reader.next();
				putMetaElement( container , (String)( idPrefix + j ) , oneFeature );
				j++;
			}
			putMetaFooter( container );
		}
	}
		
	private void putMetadataToTiles(  ) throws Exception{
		SimpleFeature oneFeature;
		Object value;
		double dval=0;
		int i , j;
		FeatureIterator<SimpleFeature> reader = fsShape.features();
		j = 0;
		for ( int w = 0 ; w < wPart ; w++ ){
			for ( int h = 0 ; h < hPart ; h++ ){
				if ( ! get2DHashB(parentTileExistence, w/2, h/2, true)  && get2DHashB(thisTileExistence, w, h, true) ){
					putMetaHeader((get2DHashSM( tiles, w, h)));
//					checkOutOfSize( oos , w , h );
				}
			}
		}
		
		while (reader.hasNext()) {
			hasFigure = (HashSet<Long>)elem.get(j);
			oneFeature = reader.next();
			for ( int w = 0 ; w < wPart ; w++ ){
				for ( int h = 0 ; h < hPart ; h++ ){
					if ( get2DHashB( hasFigure, w, h, false) ){
						if ( ! get2DHashB(parentTileExistence, w/2, h/2, true )  && get2DHashB(thisTileExistence, w, h, true) ){
							putMetaElement( (get2DHashSM( tiles, w, h)) , (String)( idPrefix + j ) , oneFeature );
//							checkOutOfSize( oos , w , h );
						}
					}
				}
			}
			j++;
		}
		for ( int w = 0 ; w < wPart ; w++ ){
			for ( int h = 0 ; h < hPart ; h++ ){
				if ( ! get2DHashB(parentTileExistence, w/2, h/2, true)  && get2DHashB(thisTileExistence, w, h, true) ){
					putMetaFooter((get2DHashSM( tiles, w, h)));
//					checkOutOfSize( oos , w , h );
				}
			}
		}
	}
	
	private void putMetaHeader( SvgMap smp ) throws Exception{
		smp.putPlaneString("<metadata>\n");
		smp.putPlaneString(" <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" ");
		smp.putPlaneString(" xmlns:" + metaNs + "=\"" + metaUrl + "\" >\n");
	}
	
	private void putMetaFooter( SvgMap smp ) throws Exception{
		smp.putPlaneString(" </rdf:RDF>\n");
		smp.putPlaneString("</metadata>\n");
	}
	
	private void putMetaElement(SvgMap smp , String elemId , SimpleFeature oneFeature ) throws Exception{
		smp.putPlaneString("  <rdf:Description rdf:about=\"" + elemId  + "\" ");
		for ( int i = 0 ; i < readFT.getAttributeCount() ; i++){
			if (oneFeature.getAttribute(i) instanceof Geometry == false ){
				smp.putPlaneString( metaNs + ":" + readFT.getDescriptor(i).getLocalName() + "=\"" + oneFeature.getAttribute(i) + "\" ");
			}
		}
		smp.putPlaneString("/>\n");
	}
	
	public boolean strIsSJIS = true;
	private class SetMicroMeta extends svgMapCommands{
		String metas;
		SetMicroMeta( String metas ){
			this.metas = metas;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).setMicroMeta( metas );
			return k;
		}
	}
	public void setMicroMeta( SimpleFeature oneFeature ) throws Exception{
		int mi = 0;
		StringBuffer metaString = new StringBuffer();
		for ( int i = 0 ; i < linkedMetaIndex.length ; i++){
			if (oneFeature.getAttribute(linkedMetaIndex[i]) instanceof Geometry == false ){
				metaString.append( metaNs );
				metaString.append(":");
//				metaString.append( readFT.getAttributeType(linkedMetaIndex[i]).getName());
				metaString.append( linkedMetaName[i] );
				metaString.append("=\"");
				Object oneAttr = oneFeature.getAttribute( linkedMetaIndex[i] );
				if (oneAttr instanceof String){
//					System.out.print((String)oneAttr + " : " );
					if ( strIsSJIS ){
						// patch 2013/2/15 windows.....
//						oneAttr = (Object)new String(((String)oneAttr).getBytes("iso-8859-1"), "Shift_JIS");
						oneAttr = (Object)new String(((String)oneAttr).getBytes("iso-8859-1"), "Windows-31J");
					} else {
						oneAttr = (Object)new String(((String)oneAttr).getBytes("iso-8859-1"), "UTF-8");
					}
					oneAttr = (Object)htmlEscape((String)oneAttr);
//					System.out.println(oneAttr);
				}
				metaString.append( oneAttr );
				metaString.append( "\" ");
			}
		}
		
		svgMapCommands pu = new SetMicroMeta( metaString.toString() );
		bufferedDraw( pu , false );
		
		++ elementCount;
	}
	
	
	int titleAttrIndex = -1;
	boolean useTitleAttr = false;
	private class SetMicroMeta2 extends svgMapCommands{
		String metas;
		SetMicroMeta2( String metas ){
			this.metas = metas;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			if ( ! noMetaId ){
				(get2DHashSM( tiles, j, i)).setId( idPrefix + elementCount );
			}
			(get2DHashSM( tiles, j, i)).setMicroMeta( metas );
			return k;
		}
	}
	public void setMicroMeta2( SimpleFeature oneFeature ) throws Exception{
		int mi = 0;
		StringBuffer metaString = new StringBuffer();
		String titleAttr ="";
		metaString.append( "content=\"");
		for ( int i = 0 ; i < linkedMetaIndex.length ; i++){
			if (oneFeature.getAttribute(linkedMetaIndex[i]) instanceof Geometry == false ){
				if ( titleAttrIndex == -1 ){
					titleAttrIndex = linkedMetaIndex[i];
				}
				Object oneAttr = oneFeature.getAttribute( linkedMetaIndex[i] );
				if (oneAttr instanceof String){
					if ( strIsSJIS ){
						oneAttr = (Object)new String(((String)oneAttr).getBytes("iso-8859-1"), "Windows-31J");
					} else {
						oneAttr = (Object)new String(((String)oneAttr).getBytes("iso-8859-1"), "UTF-8");
					}
					if ( ((String)oneAttr).indexOf(",") > 0 ){ // カンマがデータに入っているときの誤動作防ぐ・・・ 2015.4.17
						oneAttr=(Object)("'"+(String)oneAttr+"'");
					}
					oneAttr = (Object)htmlEscape((String)oneAttr);
				}
				if ( linkedMetaIndex[i] == titleAttrIndex ){
					titleAttr = oneAttr.toString();
				}
				metaString.append( oneAttr );
				if ( i < linkedMetaIndex.length -1 ){
					metaString.append( ",");
				}
			}
		}
		metaString.append("\" ");
		
		if ( titleAttrIndex != -1 && useTitleAttr ){
			metaString.append("xlink:title=\"" + titleAttr + "\" ");
		}
		
		svgMapCommands pu = new SetMicroMeta2( metaString.toString() );
		bufferedDraw( pu , false );
		
		++ elementCount;
	}
	
	private class SetCustomAttribute extends svgMapCommands{
		String cAttr;
		SetCustomAttribute( String cAttr ){
			this.cAttr = cAttr;
		}
		int drawObjectToTile(int i , int j , int k , SubTileInfo subTileInfo ) throws Exception{
			(get2DHashSM( tiles, j, i)).setCustomAttribute( cAttr );
			return k;
		}
	}
	public void setCustomAttribute( String cAttr ) throws Exception{
		
		svgMapCommands pu = new SetCustomAttribute( cAttr );
		bufferedDraw( pu , false );
		
		++ elementCount;
	}
	
	
	public int checkOutOfSize( boolean oos , int j , int i , int k , SubTileInfo subTileInfo ){
		if ( ! oos ){
//			System.out.println("Out Of Size!" + ":" + j + ":" + i + ":" + k);
			//リミッターを超えたら(oosがfalseだったら)、そのタイルのExsitenceをfalseにする
//			set2DHashB( thisTileExistence, j, i, false, true); // こちらの方は、次のレベルの処理をするためのもの。　これはスレッドセーフじゃないバグ
			subTileInfo.subTileOverflowSet.add(getHashKey(j,i));
			
			/**
			// 該当する、オーバーフローしたタイルを飛ばす処理はsubTileListの要素削除に代わり、subTileOverflowSetを用いてsvgMapCommandsのexec()中で行う 2017.4.19
			if ( k >=0){
				subTileInfo.subTileList.remove( k ); // (sub)tileListのオーバーフロータイル要素を外すことで、その次のパースから該当タイルを外す。
				--k;
			}
			**/
			
			if ( subTileInfo.subTileSet.size() == subTileInfo.subTileOverflowSet.size() ){
				subTileInfo.subAllTilesOOS = true; 
//				allTilesOOS = true; // マルチスレッドの場合、スレッド毎のsubTileListに対してのみの判断に過ぎないのでバグ2016/4/12
//			System.out.println("Sub All OOS!! : " + subTileInfo.subAllTilesOOS);
				
			}
			subTileInfo.subOutOfSize = true; // どれか一つでもアウトオブサイズのタイルがある
//			System.out.println("OOS!! : " + subTileInfo.subOutOfSize);
		}
		return ( k );
	}
	
	public void setLimitter( long size ){
		for ( int k = 0 ; k < tileList.size() ; k++ ){
			int[] tileIndex = (int[])tileList.get(k);
			int i = tileIndex[0];
			int j = tileIndex[1];
					(get2DHashSM( tiles, j, i)).limit = size;
		}
	}
	
	
	// 下位互換用 (Vectorの中身がArrayのバージョン) 2014.2.14
	public void createRContainer( Vector tileIndexArrays , Vector tileElementsArrays ) throws Exception{
		createRContainer( tileIndexArrays , tileElementsArrays , 0 );
	}
	
	// (Vectorの中身がArrayのバージョン) 2014.2.14
	public void createRContainer( Vector tileIndexArrays , Vector tileElementsArrays , int densityControl) throws Exception{
		Vector<HashSet<Long>> tileIndex = new Vector<HashSet<Long>>();
		Vector<HashSet<Long>> tileElements = new Vector<HashSet<Long>>();
		for ( int lvl = level ; lvl < tileIndexArrays.size() ; lvl++ ){
			boolean[][] tileIndexA , tileElementsA;
			tileIndexA = (boolean[][])tileIndexArrays.get(lvl);
			tileElementsA = (boolean[][])tileElementsArrays.get(lvl);
			tileIndex.addElement(array2HashSetB(tileIndexA,true));
			tileElements.addElement(array2HashSetB(tileElementsA,false));
		}
		createRContainer2( tileIndex , tileElements , densityControl);
	}
	
	//階層的なコンテナを作成する 2012.4.10 密度に応じた表示制御機能　densityContorolを追加
	// VectorをHashSet化！(tileIndexはExistenceなのでdef:true, tileElements:def:false) 2014.2.14
	public void createRContainer2( Vector<HashSet<Long>> tileIndex , Vector<HashSet<Long>> tileElements , int densityControl) throws Exception{
		// tileIndex: タイルの存在有無(tileExistence)をレベル毎に多階層で蓄積したもの
		// tileElements: タイルに要素があるかないか(thisTileHasElements)をレベル毎に多階層で蓄積
		System.out.println("CreateContainer2 : level: " + level );
		int wp = wPart;
		int hp = hPart;
		System.out.println("createRContainer wPart:"+wPart + " hPart:" + hPart );
		HashSet<Long> thisIndex , parentIndex , thisElements;
		
		HashMap<String,SvgMap> subContainers = new HashMap<String,SvgMap>();
//		HashMap parentContainers = new HashMap();
//		SvgMap subContainer;
		
		int startSize = ((HashSet<Long>)(tileIndex.get(level))).size();
		int vLevel = 0;
		int vSize = 1;
		while ( startSize > vSize ){
			vSize = vSize * 4;
			++vLevel;
		}
		
		
		boolean pFlg =false;
		int pStartLevel = (tileIndex.size()-1) % pStep; // 次の階層コンテナを作るレベルを設定
		pStartLevel -= vLevel;
		if ( pStartLevel < 1 ){
			pStartLevel = 1;
		}
			
//		if ( pStartLevel < 2 ){
//			pStartLevel += pStep;
//		}
		
		double mw , mh;
		String lvlStr;
		
		/** 何かこれって間違いな気がする・・ 基本的には(levelが0か1の場合しかないなら)常に２倍すればいいのでは
		for ( int lvl = 0 ; lvl < level  ; lvl ++ ){
			wp = wp * 2;
			hp = hp * 2;
		}
		**/
		
// 2013.8.6 level0から(level1でなく)こちらの生成ルーチンを使うように改修したため、*2は不要になった
//		wp = wp * 2; 
//		hp = hp * 2;
		
		SvgMap sm;
		System.out.println("StartLevel:" + level );
		System.out.println("EndLevel:" + (tileIndex.size() - 1) );
		System.out.println("PstartLevel:" + pStartLevel );
		
		SvgMap rootContainer = container;
		
		int pDiv = 1;
		int pLevel = 0;
		
		//再帰的なタイルのコンテナを生成している
		for ( int lvl = level ; lvl < tileIndex.size() ; lvl++ ){
			rootContainer.putPlaneString("<!-- LEVEL: " + lvl + " -->\n");
			// このレベル以下のレベルで、タイルがあるかどうかを得る (これは論理的に意味がないと思われる　消去する
//			HashSet<Long> allTiles = getAllTilesTable( lvl , tileIndex.size() - 1 , (HashSet<Long>)tileIndex.get(tileIndex.size() - 1));
			
			mw = width / wp;
			mh = height / hp;
			
			thisIndex = (HashSet<Long>)tileIndex.get( lvl );
			thisElements = (HashSet<Long>)tileElements.get( lvl );
			
			if ( lvl < 1 ){
//				parentIndex = new boolean[wp][hp]; // 本当はこの半分だけどまぁいいかな
				parentIndex = new HashSet<Long>();
				for ( int i = 0 ; i < wp ; i++ ){
					for ( int j = 0 ; j < hp ; j++ ){
						set2DHashB( parentIndex , i , j , false , true );
					}
				}
			} else {
				parentIndex = (HashSet<Long>)tileIndex.get( lvl - 1 );
			}
			
//			if (lvl == level){  //} level=0の時だけが_lなしで良いケースだと思ったけど、そうでもないんですかね・・・ 2017.5.12
			if (lvl == 0){  // やはりバグが出たようですのでひとまず直して様子見ます 2017.8.4
				lvlStr = "";
			} else {
				lvlStr = "_l" + lvl;
			}
			
//			System.out.println("lvl:" + lvl );
			if ( lvl == pStartLevel ){
				// 次の階層タイルレベルに入った
				pFlg = true;
				pStartLevel += pStep;		// parentTileExistence は　falseのものが入っている

//				System.out.println("rec:" + lvl );
//				parentContainers = (HashMap)subContainers.clone();
//				subContainers = new HashMap();
			} else {
				pFlg = false;
			}
			
			// デンシティに応じた表示制御用(2012.4.10)
			double minZoom = -1;
			double minZoomPng = -1;
			if (densityControl > 0){
				minZoom = (int)( 10000 * densityControl / mh ) / 100.0;
				if ( lvl != level ){
					minZoomPng = minZoom / 2;
				}
			}
			
			int contI , contJ;
			
			String key ="";
//			System.out.println("subCs:" + subContainers.keySet());
			Iterator<Long> it = parentIndex.iterator();
			while( it.hasNext()){
				int[] idx = getIndex( it.next() );
				for ( int ii = 0 ; ii < 2 ; ii++ ){
					for ( int jj = 0 ; jj < 2 ; jj++ ){
						int j = idx[0] * 2 + jj;
						int i = idx[1] * 2 + ii;
						if ( j >= wp || i >= hp ){
							continue;
						}
						
						if ( pLevel > 0 ){ // 階層タイルを使っている場合
							key =  pLevel + "_" + (j / pDiv) + "_" + (i / pDiv) ;
							boolean emp = subContainers.containsKey( key );
	//						System.out.println("get SubC  pLevel:" + pLevel + "key:" + key + " contains:" + emp + " key:" + subContainers.keySet());
							container = (SvgMap)(subContainers.get( key ));							///////
	//						System.out.println("subContainers:" + pLevel);
						}
	//					System.out.println( "TileExistence " + i + ":" + j + "=" +  thisIndex[j][i] );
						Coordinate pt = new Coordinate ( minX + j * mw , minY + i * mh );
	//					System.out.println("lvl:" + lvl + ":::parent:" + parentIndex.length + "," + parentIndex[0].length + ":::this:" + thisIndex.length + "," + thisIndex[0].length + ":" + j + "," + i );
						if ( ! get2DHashB( parentIndex, j/2, i/2, true)  && get2DHashB( thisIndex, j, i, true ) ){ // そのレベルにタイルが有る場合
							if ( get2DHashB( thisElements, j, i, false ) ){ // そのタイルがからだったら書き出す必要ない
	//							Coordinate pt = new Coordinate ( minX + j * mw , minY + i * mh );
	//							System.out.println ( "file:cont" + key + " : " + pt +","+ mw +","+ mh +","+ href + lvlStr + "_" + j + "-" + i + ".svg" );
								
								
								container.putImage( pt , mw , mh , href + lvlStr + "_" + j + "-" + i + ".svg" , minZoom , -1 );
	//							container.putPlaneString("<!-- write to l" + pLevel + ":" + (j / pDiv) + ":" + (i / pDiv) + " -->\n");
								if ( tileDebug  ){
									if ( densityControl > 0){
										if ( bitimageGlobalTileLevel >= 0){
	//										container.putImage(pt , mw , mh , href + "/lvl" + (bitimageGlobalTileLevel + lvl ) + "/tile" + j + "_" + i + ".png" , -1 , minZoom );
											container.putImage(pt , mw , mh , href + "/lvl" + (bitimageGlobalTileLevel + lvl ) + "/tile" + j + "_" + i + ".png" , minZoomPng , minZoom );
											
											// debug
											/**
											container.putPlaneString("<rect x=\"" + (minX + j * mw) + "\" y=\"" + (minY + i * mh) + 
												"\" width=\"" + mw + "\" height=\"" + mh + 
												"\" fill=\"none\" stroke=\"red\" stroke-width=\"0.5\" " + 
												"visibleMinZoom=\"" + minZoomPng + "\" visibleMaxZoom=\"" + minZoom + "\" "+
												"vector-effect=\"non-scaling-stroke\" stroke-linejoin=\"bevel\" />\n");
											**/
											
										} else {
	//										System.out.println("plane Rect");
											putAreaRect( container, minX + j * mw , minY + i * mh ,mw , mh , tileRectFill , minZoom );
										}
									} else {
										
										container.putPlaneString("<rect x=\"" + (minX + j * mw) + "\" y=\"" + (minY + i * mh) + 
											"\" width=\"" + mw + "\" height=\"" + mh + 
											"\" fill=\"none\" stroke=\"red\" stroke-width=\"0.5\" " + 
											"vector-effect=\"non-scaling-stroke\" stroke-linejoin=\"bevel\" />\n");
									}
								}
							}
						} else if ( (lvl < tileIndex.size() - 1) && ! get2DHashB( thisIndex, j, i, true ) ){ // その下のレベルのコンテンツが有る場合 
							if ( pFlg ){ //次のレベルに入り、その下のレベルのコンテンツが有る場合、[多階層]コンテナを生成
		//						Coordinate pt = new Coordinate ( minX + j * mw , minY + i * mh );
								String subCname =  href + "_cont" + lvlStr + "_" + j + "-" + i + ".svg";
								container.putImage( pt , mw , mh , subCname , minZoom , -1 );
								
								if ( tileDebug && densityControl > 0 ){
									if ( bitimageGlobalTileLevel >= 0){
										
										// TODO? 2014.02.14
										// 多階層コンテナになったときに、ビットイメージが消えるような気がする？
										// 2015.9.4 未確認
									} else {
										putAreaRect( container, minX + j * mw , minY + i * mh ,mw , mh , tileRectFill , minZoom );
									}
								}
								
		//						container.putHeader( minX ,minY , width , height );
		//						System.out.println ("make:" + subCname );
								sm = new SvgMap( dir + subCname , nf );
								sm.isSvgTL = isSvgTL;
		//						System.out.println("make subC "+ subCname);
								sm.putHeader( minX ,minY , width , height );
								sm.putCrs(commonA,commonB,commonC,commonD,commonE,commonF);
		//						sm.putPlaneString( "fileName:" + subCname );
		//						sm.putImage( pt , mw , mh , subCname );
		//						sm.putFooter();
								subContainers.put( lvl + "_" + j + "_" + i  , sm );
								
		//						container.putPlaneString("<!-- write to l" + pLevel + ":" + (j / pDiv) + ":" + (i / pDiv) + " -->\n");
								
							}
	//						if ( allTiles[j][i] && tileDebug && densityControl > 0 && bitimageGlobalTileLevel >= 0){}
								// そのレベルにタイルがなく、下のレベルにタイルがあり、且つビットイメージローレゾタイルを出力する場合 del
							if ( tileDebug && densityControl > 0 && bitimageGlobalTileLevel >= 0){
								// そのレベルにタイルがなく、且つビットイメージローレゾタイルを出力する場合(allTilesは原理的に常にtrueでしょう 2014.2.14)
								container.putImage(pt , mw , mh , href + "/lvl" + (bitimageGlobalTileLevel + lvl ) + "/tile" + j + "_" + i + ".png" , minZoomPng , minZoom );
								/**
								// debug
								container.putPlaneString("<rect x=\"" + (minX + j * mw) + "\" y=\"" + (minY + i * mh) + 
									"\" width=\"" + mw + "\" height=\"" + mh + 
									"\" fill=\"none\" stroke=\"red\" stroke-width=\"0.5\" " + 
									"visibleMinZoom=\"" + minZoomPng + "\" visibleMaxZoom=\"" + minZoom + "\" "+
									"vector-effect=\"non-scaling-stroke\" stroke-linejoin=\"bevel\" />\n");
								**/
							}
						} else if ( (lvl == tileIndex.size() - 1) && ! get2DHashB( thisIndex, j, i, true ) ){ // すでに最大分割レベルに到達しているのに、その下にタイルがあるといっている場合は、オーバーフローして処理を打ち切っているタイル(特殊処理) 2014.2.19
							if ( tileDebug  ){
								if ( densityControl > 0){
									if ( bitimageGlobalTileLevel >= 0){
										container.putImage(pt , mw , mh , href + "/lvl" + (bitimageGlobalTileLevel + lvl ) + "/tile" + j + "_" + i + ".png" , minZoomPng , -1 );
									} else {
										putAreaRect( container, minX + j * mw , minY + i * mh ,mw , mh , "#ff0000" , -1 );
									}
								} else {
									
									container.putPlaneString("<rect x=\"" + (minX + j * mw) + "\" y=\"" + (minY + i * mh) + 
										"\" width=\"" + mw + "\" height=\"" + mh + 
										"\" fill=\"none\" stroke=\"red\" stroke-width=\"0.5\" " + 
										"vector-effect=\"non-scaling-stroke\" stroke-linejoin=\"bevel\" />\n");
								}
							}
						}
					}
				}
			}
			wp = wp * 2;
			hp = hp * 2;
			
			if ( pFlg  ){ // 次の階層タイルレベルに入ったら
//				if ( pLevel == 0 ){ //階層タイルレベルが0(ルート)の場合
//					System.out.println("close container");
//					container.putFooter();														///////
//				} else {
//				}
				pLevel = pStartLevel - pStep; // pLevelは、今の階層タイルレベル
				pDiv = 2;
			} else {
				pDiv *= 2;
			}
//				System.out.println( "pDiv:" + pDiv );
			
		}
		
		System.out.println("close root container");
		rootContainer.putFooter();																	///////
		Iterator it = subContainers.entrySet().iterator();
		boolean hasSub = it.hasNext();
		if ( hasSub ){
			System.out.println("close multi container");
			while ( it.hasNext() ){
				Map.Entry entry = (Map.Entry) it.next();
				sm = (SvgMap)(entry.getValue());
				sm.putFooter();															//////
			}
		}
	}
	
	private void putAreaRect( SvgMap doc, double x , double y , double w , double h , String color , double MaxZoom ) throws Exception{
		String fillColor = "#90FF90";
//									System.out.println("putFooter : defaultFill:" + defaultFill);
		if (color.length()>0){
			fillColor = color;
		} else {
		}
		
		if ( true ){
			doc.putPlaneString("<rect x=\"" + nf.format(x) + "\" y=\"" + nf.format(y) + "\" width=\"" + nf.format(w) + "\" height=\"" + nf.format(h) + "\" fill-opacity=\"0.5\" fill=\"" + fillColor + "\" stroke=\"none\"");
		} else {
			doc.putPlaneString("<path d=\"M" + nf.format(x) + " " + nf.format(y) + " L" + nf.format(x+w) + " " + nf.format(y) + " " + nf.format(x+w) + " " + nf.format(y+h) + " " + nf.format(x) + " " + nf.format(y+h) + "\" fill-opacity=\"0.5\" fill=\"" + fillColor + "\" stroke=\"none\"");
		}
		
		if ( MaxZoom >0 ){
			doc.putPlaneString(" visibleMaxZoom=\"" + nf.format(MaxZoom) + "\"");
		}
		doc.putPlaneString("/>\n");
		
	}
	
	
	
	public static String htmlEscape(String text){
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<text.length();i++){
			switch(text.charAt(i)){
			case '&' :
				sb.append("&amp;");
				break;
			case '<' :
				sb.append("&lt;");
				break;
			case '>' :
				sb.append("&gt;");
				break;
			case '"' :
				sb.append("&quot;");
				break;
			case '\'' :
				sb.append("&#39;");
				break;
			case ' ' :
				sb.append("&#32;"); // 2016.10.17 debug
				break;
			case '\\' :
//				sb.append("&yen;"); // 2014.10.14 debug
				sb.append("&#165;");
				break;
			default :
				sb.append(text.charAt(i));
				break;
			}
		}
		return sb.toString();
	}	
	
	
	public void set2DHashSM( HashMap<Long,SvgMap> hMap , int index1 , int index2 , SvgMap value ){
		Long key = getHashKey( index1 , index2 );
		hMap.put(key ,  value  );
	}
	
	public SvgMap get2DHashSM( HashMap<Long,SvgMap> hMap , int index1 , int index2 ){
		Long key = getHashKey( index1 , index2 );
		SvgMap ans = hMap.get(key);
		return ( ans );
	}
	
	public void set2DHashD( HashMap<Long,Double> hMap , int index1 , int index2 , double value ){
		Long key = getHashKey( index1 , index2 );
		hMap.put(key , new Double( value ) );
	}
	
	public double get2DHashD( HashMap<Long,Double> hMap , int index1 , int index2 ){
		Long key = getHashKey( index1 , index2 );
		Double ans = hMap.get(key);
		return ( ans.doubleValue() );
	}
	
	public void set2DHashI( HashMap<Long,Integer> hMap , int index1 , int index2 , int value ){
		Long key = getHashKey( index1 , index2 );
		hMap.put(key , new Integer( value ) );
	}
	
	public int get2DHashI( HashMap<Long,Integer> hMap , int index1 , int index2 ){
		Long key = getHashKey( index1 , index2 );
		int ans;
		if ( hMap.containsKey(key)){
			ans = (hMap.get(key)).intValue();
		} else {
			ans = 0;
		}
		return ( ans );
	}
	
	public void set2DHashB( HashSet<Long> hMap , int index1 , int index2 , boolean value , boolean defaultVal){
		// defaultの値はハッシュを作らない！
		if ( defaultVal != value ){
		Long key = getHashKey( index1 , index2 );
			hMap.add(key);
		}
	}
	
	public boolean get2DHashB( HashSet<Long> hMap , int index1 , int index2 , boolean defaultVal){
		Long key = getHashKey( index1 , index2 );
		boolean ans;
		if ( hMap.contains(key) ){
			ans = !defaultVal;
		} else {
			ans = defaultVal;
		}
		return ( ans );
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
		
	
	public HashSet<Long> array2HashSetB( boolean[][] array2 , boolean defaultVal){
		// defaultの値はハッシュを作らない！
		HashSet<Long> ans = new HashSet<Long>();
		for ( int i = 0 ; i < array2[0].length ; i++ ){
			for ( int j = 0 ; j < array2.length ; j++ ){
//				System.out.println( j+","+i+":"+array2[j][i]);
				if ( array2[j][i] != defaultVal ){
					Long key = new Long((long)((long)j * (long)100000000 + (long)i));
					ans.add( key );
				
				}
//				System.out.println( "key:"+ new Long(j * 100000000 + i) + " contains:" + ans.contains(  new Long(j * 100000000 + i) ) );
			}
		}
		return ( ans );
	}	
	
	public boolean[][] getThisTileExistenceArray(){
		boolean[][] ans = new boolean[wPart][hPart];
		for ( int i = 0 ; i < wPart ; i++ ){
			for ( int j = 0 ; j < hPart ; j++ ){
				ans[i][j] = get2DHashB( thisTileExistence , i , j , true);
//				System.out.print(ans[i][j]?"o":"x");
			}
//			System.out.println("");
		}
		return(ans);
	}
	
	public boolean[][] getThisTileHasElementsArray(){
		boolean[][] ans = new boolean[wPart][hPart];
		for ( int i = 0 ; i < wPart ; i++ ){
			for ( int j = 0 ; j < hPart ; j++ ){
				ans[i][j] = get2DHashB( thisTileHasElements , i , j , false);
			}
		}
		return(ans);
	}
	
	public HashSet<Long> getThisTileExistenceSet(){
		return( thisTileExistence );
	}
	
	public HashSet<Long> getThisTileHasElementsSet(){
		return( thisTileHasElements );
	}
	
	public boolean divCheck( HashMap<Long,Integer> divErr , int limit , int currentLevel ){
		// すべてのタイルの分割エラー連続数が、limitを超えたらアラート(false)を出す
		// currentLevelと、分割エラー連続数が同じ場合は、階層的タイル分割が一度も成功していない状態なので、limitよりも+5粘り強くチェックする(2016/3/15)
		boolean ans = false;
		Iterator it = divErr.keySet().iterator();
//		System.out.print("divCheck:  level:" + currentLevel + " :: ");
		for ( Long key : divErr.keySet()){
			int ec = divErr.get(key).intValue();
//			System.out.print(ec + "," );
			if ( ec == currentLevel && ec < limit + 5 ){
				ans = true;
			} else if ( ec < limit ){
				ans = true;
//				break;
			}
		}
//		System.out.println("");
//		System.out.println("totalSize:" + divErr.size());
		return ( ans );
	}
	
	// SvgMapの各種メソッド(描画命令他に対応)をインスタンス化するための抽象クラス 2016.4.7- リファクタリングの中心
	// マルチスレッド化のために、描画命令列を一時蓄積し、一気にスレッド群に手渡し並列処理効率を上げるため、メソッドのベクタを作ることが目的
	private abstract class svgMapCommands{
		long pointHintKey = -1; // pointのHashKey
		
		void exec() throws Exception{
			SubTileInfo subTileInfo = new SubTileInfo( null );
			for ( int k = 0 ; k < tileList.size() ; k++ ){
				int[] tileIndex = (int[])tileList.get(k);
				int i = tileIndex[0];
				int j = tileIndex[1];
//				SvgMap hsm = get2DHashSM( tiles, j, i);
				k = drawObjectToTile( i, j, k , subTileInfo );
			}
			outOfSize = subTileInfo.subOutOfSize;
			allTilesOOS = subTileInfo.subAllTilesOOS;
		}
		
		// マルチスレッド分割用 (subTileListはそれぞれのスレッド用に小分けされたタイルのリスト)
		void exec(SubTileInfo subTileInfo) throws Exception{
//			System.out.println("exec:pointHintKey:"+pointHintKey);
			if ( pointHintKey < 0 ){
				for ( Long key : subTileInfo.subTileSet){
					if ( ! subTileInfo.subTileOverflowSet.contains(key) && !thisTileExistence.contains(key)){ // この描画シリーズと一つ前までのところまででオーバーフローしてないもののみ描画
						int[] tileIndex = getIndex(key);
						int i = tileIndex[1];
						int j = tileIndex[0];
						drawObjectToTile( i, j, 0 , subTileInfo );
					}
				}
			} else { // pointHintKeyを持つpointジオメトリに対してタイル位置を一発で決め、高速化する
				if ( subTileInfo.subTileSet.contains(pointHintKey) && (!subTileInfo.subTileOverflowSet.contains(pointHintKey)&& !thisTileExistence.contains(pointHintKey) )){
					
					int[] tileIndex = getIndex(pointHintKey);
					int i = tileIndex[1];
					int j = tileIndex[0];
					drawObjectToTile( i, j, -1 , subTileInfo );
				}
			}
		}
		abstract int drawObjectToTile( int i , int j , int k , SubTileInfo subTileInfo ) throws Exception;
	}
	
	// 描画命令列の蓄積と本当の描画を実施させるコントローラのフロントエンド
//	int bufferedDrawSize = 128; // ここで指定した個数分バッファされてスレッドに一気に送付される
	int bufferedDrawSize = 16384;
	ArrayList<svgMapCommands> smcArray = new ArrayList<svgMapCommands>(); // 描画命令列蓄積用array 上限は上のbufferedDrawSize
	
	private void bufferedDraw( svgMapCommands smc , boolean forceOutput) throws Exception{ // ラッパです
		bufferedDrawMS( smc , forceOutput);
	}
	
	// スレッドにしないで実行するタイプ(準備工事用：obsolute)
	private void bufferedDrawSS( svgMapCommands smc , boolean forceOutput) throws Exception{
		if ( smc != null ){
			smcArray.add(smc); // タイルにもスレッドにも分割せずに、コマンドをただ貯めるだけを行っている。
		}
		if ( smcArray.size() == bufferedDrawSize || forceOutput ){
//			System.out.println("compute each tile and buffer clear");
			// バッファがいっぱいになったら実際の出力処理を実行
			// 本来はスレッド発動
			for ( int i = 0 ; i < smcArray.size() ; i++ ){
				(smcArray.get(i)).exec(); // このexecがタイル分割を間接的に発動させる
			}
			smcArray.clear();
		}
	}
	
	// マルチスレッドで実行するタイプ(実用)
	private void bufferedDrawMS( svgMapCommands smc , boolean forceOutput) throws Exception{
		if ( smc != null ){
			smcArray.add(smc); // タイルにもスレッドにも分割せずに、コマンドをただ貯めるだけを行っている。
		}
		if ( smcArray.size() == bufferedDrawSize || forceOutput ){
//			System.out.println("compute each tile and buffer clear");
			// バッファがいっぱいになったら実際の出力処理を実行
			
//			System.out.println("subTileSets.size(): "+subTileSets.size());
			
			List<Future<?>> futureList = new ArrayList<Future<?>>();
			
			for ( int j = 0 ; j < subTileSets.size() ; j++ ){
				SvgMapExporterRunnable smet;
				if ( svgMapExporterRunnables.size() > j ){ // ランなブルを再利用している
//					System.out.println("ランなブルを再利用");
					smet = svgMapExporterRunnables.get(j);
					smet.initRunnable( subTileSets.get(j) );
				} else {
//					System.out.println("ランなブル新規");
					smet = new SvgMapExporterRunnable( subTileSets.get(j), j);
					svgMapExporterRunnables.add(smet);
				}
				
				
				Future<?> future = svgMapExecutorService.submit(smet);
				futureList.add(future);
				
			}
			
			// 同期のための待機ループ
			for (Future<?> future : futureList) {
				future.get();
			}
			
			
			allTilesOOS = true;
			for ( int j = 0 ; j < subTileSets.size() ; j++ ){
				SvgMapExporterRunnable smet = svgMapExporterRunnables.get(j);
				SubTileInfo si = smet.subTileInfo;
				if ( si.subAllTilesOOS == false ){ // 小分けされたsubAllTilesOOSに一個でもfalseがあればallTilesOOSはfalse
					allTilesOOS = false;
				}
				if ( si.subOutOfSize == true ){ // 小分けされたsubOutOfSizeに一個でもtrueがあればoutOfSizeはtrue
					outOfSize = true;
				}
				// thisTileExistence（実際はこのレベルでオーバーフローしたタイルの全リスト）へのコピー
				thisTileExistence.addAll(si.subTileOverflowSet);
				
//				System.out.println("tiles:"+si.subTileSet + "  overflow:"+si.subTileOverflowSet);
				
//				si.deleteMember();
				si = null;
				smet = null;
//				System.out.println( svgMapThreads.get(j).getState()+" :: " + svgMapThreads.get(j).isAlive());
				
			}
			
			
//			svgMapThreads.clear();
			smcArray.clear();
		}
	}
	
	
	//スレッド分割機構：マルチスレッド用にtileList(グローバル変数)を小分けしたsubTileSets(グローバル変数)を作る
	int maxThreads; // 最大のスレッド数～スレッドプールと同数（タイルの数がこれに満たなければ当然タイル数）
		
	ArrayList<HashSet<Long>> subTileSets; // マルチスレッド用に小分けされたtileListをHashSet化したもの
		
	private void buildSubTileSets(){
//		System.out.println("CALLED buildSubTileSets   tileList.size():" + tileList.size() + "    maxThreads:" + maxThreads);
		
		subTileSets = new ArrayList<HashSet<Long>>();
		
		int j = 0;
		for ( int i = 0 ; i < tileList.size() ; i++ ){
//			Object tileIndex = tileList.get(i);
			int[] tileIndex = (int[])(tileList.get(i));
			if ( i < maxThreads ){
				
				HashSet<Long> subSet = new HashSet<Long>();
				set2DHashB( subSet, tileIndex[1], tileIndex[0],true,false);
//				set2DHashB( subSet, tileIndex[0], tileIndex[1],true,false);
				subTileSets.add(subSet);
				
			} else {
				
				HashSet<Long> subSet = subTileSets.get( i % maxThreads );
				set2DHashB( subSet, tileIndex[1], tileIndex[0],true,false);
//				set2DHashB( subSet, tileIndex[0], tileIndex[1],true,false);
				
			}
			
			
			
		}
	}
	
	private void aggregateTileList(){
//		System.out.println("thisTileExistence:"+thisTileExistence);
		// tileListをoutofsizeのものを除いたものに作り直す
		// thisTileExistence(オーバーフローしたタイル)　を差し引けば良い
//		System.out.print("TileList:");
		for ( int i = tileList.size()-1 ; i >=0 ; i-- ){
			int[] tileIndex = (int[])(tileList.get(i));
//			System.out.print(tileIndex[1]+"_"+tileIndex[0]+",");
			Long key = getHashKey(tileIndex[1],tileIndex[0]);
//			Long key = getHashKey(tileIndex[0],tileIndex[1]);
			if ( thisTileExistence.contains(key) ){
				tileList.remove( i );
			}
		}
//		System.out.println("");
	}
	
	private class SubTileInfo{
//		ArrayList<Object> subTileList;
		HashSet<Long> subTileOverflowSet;
		boolean subOutOfSize;
		boolean subAllTilesOOS;
		
		HashSet<Long> subTileSet;
		
		
		SubTileInfo(  HashSet<Long> subTileSet ){
			this.subTileSet = subTileSet;
			subTileOverflowSet = new HashSet<Long>();
			subOutOfSize = false;
			subAllTilesOOS = false;
			if ( subTileSet.size() == 0 ){
				subAllTilesOOS = true;
//				System.out.println("new SubTileInfo subAllTilesOOS!!");
			}
		}
		
		void deleteMember(){
			subTileOverflowSet.clear();
			subTileSet.clear();
			subTileOverflowSet = null;
			subTileSet = null;
		}
		
	}
	
	//タイル出力スレッド
	private class SvgMapExporterRunnable implements Runnable{
//		ArrayList<svgMapCommands> smcArray; // コマンドリスト : 今のところ内部クラスなので参照できますね
//		ArrayList<Object> subTileList; //実際のタイル出力への参照用　タイル番号リスト : subTileInfoに統合
//		HashMap<Long,SvgMap> tiles; // 実際に入っているタイル（個々のスレッド以外のものも入っている共通のもの）今のところ内部クラスなので参照できますね
		int threadNumber;
		
		SubTileInfo subTileInfo;
		
//		HashSet<Long> subTileOverflowSet;
//		boolean subOutOfSize;
//		boolean subAllTilesOOS;
		
		SvgMapExporterRunnable(  HashSet<Long> subTileSet , int threadNumber ){ // for debug
//			this.smcArray = smcArray;
			this.threadNumber = threadNumber;
//			int size = tiles.size();
			this.subTileInfo = new SubTileInfo( subTileSet );
		}
		SvgMapExporterRunnable( HashSet<Long> subTileSet ){
			this.subTileInfo = new SubTileInfo( subTileSet );
			this.threadNumber = 0;
		}
		public void initRunnable( HashSet<Long> subTileSet ){
			this.subTileInfo = new SubTileInfo( subTileSet);
		}
		public void run(){
//			System.out.print("T"+ threadNumber);
			try{
				for ( int i = 0 ; i < smcArray.size() ; i++){ // コマンド回すループ
					svgMapCommands command = smcArray.get(i);
					command.exec(subTileInfo); // そのスレッド用のタイルに対してだけ出力指示を出す
				}
				flush(); // しっかり書き出す
			} catch ( Exception e ){
				System.out.println("exception: " + e.getMessage());
				e.printStackTrace();
				System.exit(0);
			}
		}
		private void flush() throws Exception {
			for ( Long key : subTileInfo.subTileSet ){
				
				int[] tileIndex = getIndex(key);
				SvgMap hsm = get2DHashSM( tiles, tileIndex[0], tileIndex[1]); // 逆かも？ 2017.4.18
				if ( !thisTileExistence.contains(key) ){ // 一つ前までの描画バッチで存在しているものはflush
					hsm.flush();
					if( subTileInfo.subTileOverflowSet.contains(key) ){ // しかし、この描画バッチでオーバーフローしたらここでクローズ
						hsm.putFooter();
					}
				}
			}
		}
	}
	
}