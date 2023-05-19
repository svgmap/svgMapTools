package org.svgmap.shape2svgmap;

// SVGのコンテナ構造を読み込み、データ構造を生成する
// Programmed by Satoru Takagi @ KDDI
// 2014.02.20 start coding
//


import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.geom.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;

public class SvgMapContainerReader{
	
	TreeMap<Long,HashSet<Long>> lvlMap = new TreeMap<Long,HashSet<Long>>();
	HashMap<Long,Double> lvlZoom = new HashMap<Long,Double>();
	
	HashSet<Long>[] requiredTiles = null;
	double[] tileZoom = null;
	
	
	double partAspect=0;
	
	int[] basePart;
	
	Rectangle2D.Double rootArea;
	double[] rootCrs;
	Rectangle2D.Double rootGeoArea;
	
	public static void main(String args[]) throws Exception{
		SvgMapContainerReader sr = new SvgMapContainerReader( args[0] );
		sr.testSet();
		
		System.out.println("==========================================================================");
		
//		sr.testMat( sr.getRequiredTiles() );
		
		Rectangle2D.Double gb = sr.getGeoBounds();
		int[] bp = sr.getBasePart();
		
		System.out.println("BasePart:" + bp[0] + ", " + bp[1] + "   geoBBOX:" + gb.x + ", " + gb.y + ", " + gb.width + ", " + gb.height );
		
		/** ロジックの考え落ちを発見 廃止
		HashSet<Long> set =  sr.getLevel0Set();
		System.out.println("Level:"+sr.currentLevel);
		sr.printSet( set );
		
		while ( sr.intLevel != -1 ){
			set =  sr.getNextLevelSet();
			System.out.println("Level:"+sr.currentLevel);
			sr.printSet( set );
		}
		**/
		
	}
	
	private Document document;
	SvgMapContainerReader( String input ) throws Exception {
		
		// コンテナを読み、データ構造を生成する
		readContainer( input , 0 , true);
		if ( lvlMap.size() == 0 ){
			System.out.println("There are no tiled contents. Exit program.");
			System.exit(0);
		}
		// RequiredTilesを生成する。
		buildRequiredTiles();
//		testSet();
	}
	
	
	public void testSet(){
		// データ構造を読み出している
		System.out.println("START testSet");
		long initLvl = 0;
		Iterator lvlIt = lvlMap.keySet().iterator();
		while( lvlIt.hasNext()){
			Long lkey = (Long)lvlIt.next();
			if ( initLvl == 0 ){
				initLvl = basePart[0] * basePart[1];
				System.out.println("basePart:" + basePart[0] + "," + basePart[1]);
				
			}
			
			int level = (int)Math.round((Math.log10((double)(-lkey.longValue() / initLvl))/Math.log10(4)));
			
			HashSet<Long> mapSet = lvlMap.get(lkey);
			int[] part = getPart(lkey , partAspect );
			System.out.println("Lvl:" + level + " : " + lkey + "   xp:" + part[0] + " yp:" + part[1]);
			printSet( mapSet );
		}
		System.out.println("END   testSet");
	}
	
	public void testMat( HashSet<Long>[] mapSetArray ){
		// データ構造(実際に使うモノ)を読み出している
		for ( int lvl = 0 ; lvl < mapSetArray.length ; lvl++){
			HashSet<Long> mapSet = mapSetArray[lvl];
			System.out.println("Lvl:" + lvl + " : " );
			printSet( mapSet );
		}
	}
	
	
	public void printSet( HashSet<Long> mapSet ){
		int count = 0;
		Iterator it2 = mapSet.iterator();
		while(it2.hasNext()){
			int[] index = getIndex( (Long)it2.next() );
			System.out.print( index[0] + ":" + index[1] + ", ");
			++count;
		}
		System.out.println("===>  "+ count);
	}
	
	
	public HashSet<Long>[] getRequiredTiles(){
		return ( requiredTiles );
	}
	
	public double[] getTileZoom(){
		return ( tileZoom );
	}
	
	public Rectangle2D.Double getGeoBounds(){
		return ( rootGeoArea );
	}
	
	public Rectangle2D.Double getSvgBounds(){
		return ( rootArea );
	}
	
	public int[] getBasePart(){
		return ( basePart );
	}
	
	@SuppressWarnings("unchecked")
	public void buildRequiredTiles(){
		Long mKey = lvlMap.lastKey();
		
		// 基本レベル(Level0)のタイル分割数(x,y)を得る。
		// この2^(l+1)がその上のタイルレベルのタイル分割数
		basePart = getBasePart(getPart(-mKey.longValue(),partAspect));
		
		mKey = lvlMap.firstKey();
		int maxLevel = getLevel(mKey);
		
		requiredTiles = new HashSet[maxLevel+1];
//		requiredTiles = (HashSet<Long>[])new HashSet<?>[maxLevel+1]; // これでも良いんだけど結局ワーニング出る
		tileZoom = new double[maxLevel+1];
		for ( int lv = 0 ; lv <= maxLevel ; lv++ ){
			requiredTiles[lv] = new HashSet<Long>();
		}
		
		// 一番高いレベルからrequiredTilesを生成していく
//		NavigableMap<Long,HashSet> invMap = lvlMap.descendingMap();
		Iterator lvlIt = lvlMap.keySet().iterator();
//		Iterator lvlIt = lvlMap.descendingKeySet().iterator();
		int cLvlMap = maxLevel;
		Long lKey = (Long)lvlIt.next();
		double zoom = 0;
		for ( int level = maxLevel ; level >= 0 ; level -- ){
			System.out.print( "key:" + lKey +" level:"+level);
			int count = 0;
			// まずはそのレベルのベクトルタイルの存在から、requiredTilesを設定する
			if ( cLvlMap == level ){
				HashSet<Long> mapSet = lvlMap.get(lKey);
				zoom = ((Double)lvlZoom.get(lKey)).doubleValue();
				
				Iterator it2 = mapSet.iterator();
				while(it2.hasNext()){
					Long hKey = (Long)it2.next();
					requiredTiles[level].add(hKey);
					tileZoom[level]=zoom;
					++count;
				}
				if ( lvlIt.hasNext()){
					lKey = (Long)lvlIt.next();
					cLvlMap = getLevel(lKey);
				}
			}
			System.out.println( " zoom:" + zoom + " items:" + count);
			// 次に、上のレベルのrequiredTilesに、このレベルからコピーする
			if ( level > 0 ){
				Iterator it2 = requiredTiles[level].iterator();
				while(it2.hasNext()){
					int[] currentIndex = getIndex((Long)it2.next());
					Long parentKey = getHashKey( currentIndex[0] / 2 , currentIndex[1] / 2 );
					requiredTiles[level-1].add(parentKey);
				}
				zoom = zoom / 2.0;
				tileZoom[level-1]=zoom;
			}
		}
		
	}
	
	public int getLevel(Long lKey){
//		System.out.println("basetPart:"+basePart[0]+","+basePart[1]);
		long initTiles = basePart[0] * basePart[1];
		return ( (int)Math.round(Math.log10((double)(-lKey.longValue() / initTiles))/Math.log10(4)));
	}
	
	
	/**
	// まだこれだとセットが巨大になるので、セット自体でなく、セットのインデックスを聞くと、それの答えだけ返してくれるものを作るべき
	// ロジックの考え抜けが判明～～廃止する 2014.2.24
	Iterator lvlIt;
	long lvl0Key;
	int currentLevel; // 外向けイテレータのレベル
	int intLevel; // 内部のイテレータのレベル(lvlItから取り出したもののレベル)
	HashSet<Long> parentVMapSet; // コンテナから得られる、ひとつ上の階層の（ベクトル）地図タイルセット
	HashSet<Long> parentSet; // getNextLevelSet()が生成する、そのレベルで生成すべきビットイメージタイルのセット
	
	HashSet<Long> getLevel0Set(){
		currentLevel = 0;
		lvlIt = lvlMap.keySet().iterator();
		
		Long lkey = (Long)lvlIt.next();
		basePart = getBasePart(getPart(lkey.longValue(),partAspect));
		lvl0Key = basePart[0] * basePart[1];
		intLevel = getLevel(lkey , lvl0Key);
		parentVMapSet = lvlMap.get(lkey);
		System.out.println("basePart:" + basePart[0] + "," + basePart[1] + "  firstTiledLevel:"+intLevel);
		
		parentSet = new HashSet<Long>();
		
		for ( int i = 0 ; i < basePart[0] ; i++ ){
			for ( int j = 0 ; j < basePart[1] ; j++ ){
				parentSet.add(getHashKey( i , j ));
			}
		}
		return ( parentSet );
	}
	
	HashSet<Long> getNextLevelSet(){
		if ( intLevel == -1 ){
			return ( null );
		}
		
		HashSet<Long> currentSet = new HashSet<Long>();
		++ currentLevel;
		
		Iterator it2 = parentSet.iterator();
		while(it2.hasNext()){
			Long key = (Long)it2.next();

			if ( intLevel + 1 == currentLevel && parentVMapSet.contains(key) ){
				// コンテナから得られる、ひとつ上の階層の（ベクトル）地図タイルセットがある場合、
				// その下のタイルは作らないで良い
			} else {
				int[] parentIndex = getIndex( key );

				for ( int i = 0 ; i < 2 ; i++){
					for ( int j = 0 ; j < 2 ; j++ ){
						currentSet.add(getHashKey(parentIndex[0]*2+i,parentIndex[1]*2+j));
					}
				}
			}
		}
		
		
		parentSet = currentSet;
		
		if ( intLevel + 1 == currentLevel ){ // 次のレベルのベクトル地図タイルセットを得る
			if ( lvlIt.hasNext() ){
				Long lkey = (Long)lvlIt.next();
				intLevel = getLevel(lkey , lvl0Key);
				parentVMapSet = lvlMap.get(lkey);
			} else {
				intLevel = -1;
				parentVMapSet = null;
			}
		}
		
		return ( currentSet );
	}
	
	int getLevel( Long lkey , long lvl0Key){
		return ( (int)(Math.log10((double)(lkey.longValue() / lvl0Key))/Math.log10(4)));
	}
	
	**/
	
	
	
	
	
	public void readContainer( String input , int lvl , boolean isLastContainerOfLevel) throws Exception{
		if ( lvl == 0 ){
			nextLevelContainers = new ArrayList<String>();
		}
		
//		System.out.println("file: "+input + "   last?:"+isLastContainerOfLevel + " lvl:"+lvl);
		String parent = (new File(input)).getParent();
//		System.out.println(parent);
		
		FileInputStream fis = new FileInputStream(input);
		
		document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fis);
		
		
		Element svgr = document.getDocumentElement();
		Rectangle2D.Double area = getArea( svgr.getAttribute("go:dataArea") );
//		System.out.println("area:"+area);

		
		double[] crs = getMatrix ( (Element)( document.getElementsByTagName("globalCoordinateSystem").item(0) ) );
//		System.out.println( crs[0]+":"+crs[1]+":"+crs[2]+":"+crs[3]+":"+crs[4]+":"+crs[5]);
		
		if ( lvl == 0 ){
			rootArea = area;
			rootCrs = crs;
			rootGeoArea = getGeoRect( area , crs );
//			System.out.println("rootArea:"+rootArea+" geo:"+rootGeoArea);
		}
		
//		ArrayList<String> clist = new ArrayList<String>();
		NodeList anims = document.getElementsByTagName("animation");
		for ( int i = 0 ; i < anims.getLength() ; i++ ){
			Element anim = (Element)anims.item( i );
			
			String href = anim.getAttribute("xlink:href");
			
			/**
			boolean target=false;
			if ( href.indexOf("mediaInfo1_26-2.svg")>=0 || href.indexOf("mediaInfo1_29-2.svg")>=0){
				System.out.println("Found target");
				target = true;
			}
			**/
			
			Rectangle2D.Double rect = new Rectangle2D.Double( 
				Double.parseDouble(anim.getAttribute("x")),
				Double.parseDouble(anim.getAttribute("y")),
				Double.parseDouble(anim.getAttribute("width")),
				Double.parseDouble(anim.getAttribute("height"))
			);
			
			if ( partAspect == 0 ){ // コンテナも用いてこの値を計算することにした
				int partx = (int)Math.round(rootArea.width / rect.width); // 値が大きいと無視できない丸めが起きる！
				int party = (int)Math.round(rootArea.height / rect.height);
//					System.out.println("rootArea.width:"+rootArea.width + "  rect.width:"+rect.width);
//					System.out.println("rootArea.height:"+rootArea.height + "  rect.height:"+rect.height);
				System.out.println("partx:"+partx + "  party:"+party + "   :::"+(rootArea.width / rect.width) + " :: " + (rootArea.height / rect.height) );
				
				partAspect = (double)party / (double)partx;
				basePart = getBasePart(new int[]{partx,party}); // partx,partyが小さくないと危ない感じがする　childContを後回しにすることで原理的には回避？ 2014.2.28
				System.out.println("partAspect:"+partAspect+" partX:"+partx+" partY:"+party + " basePart:" + basePart[0]+","+basePart[1]);
			}
			
			// 子コンテナ
			if ( href.indexOf("_cont")>=0|| href.indexOf("container")>=0){
				String childPath = parent + File.separator + href;
//				System.out.println("Sub:" + childPath);
//				readContainer( childPath , lvl+1 ); 後回しにする
//				clist.add(childPath);
				nextLevelContainers.add(childPath); // debug レベルの大きい（精度の低い）コンテナを先に読み込んでしまい解析が失敗する。レベルの小さいコンテンツから順に読み込む再帰呼び出しの改良を行った。 2016.12.26
			} else {
				
//				int posx = (int)Math.round((rect.x - rootArea.x) / rect.width);
//				int posy = (int)Math.round((rect.y - rootArea.y) / rect.height);
				
				
				int partxy[] = getPartXY(rect.width , rect.height ); // basePart割った倍率だけで十分？
				
				int posxy[] = getPosXY(rect.x , rect.y , partxy );
				
				
				HashSet<Long> mapSet;
				Long lvKey = new Long(-((long)partxy[0] * (long)partxy[1])); // マイナスの値をキーにして最初から降順に・・2014.2.28
//				Long lvKey = new Long(-((long)partxy[0] )); // どっちかの軸だけで十分では？(これでもまだ無駄)
//				System.out.println("lvKey:"+lvKey+ " ptx:"+partxy[0] + " pty:" + partxy[1] + " posx:"+posxy[0] + " posy:"+posxy[1]);
				if ( lvlMap.containsKey( lvKey )){
					mapSet = lvlMap.get( lvKey );
				} else {
					mapSet = new HashSet<Long>();
					lvlMap.put(lvKey, mapSet );
				}
				
				/**
				if ( target ){
					System.out.println(href);
					System.out.println(lvKey);
					System.out.println(getHashKey(posxy[0],posxy[1]));
				}
				**/
				
				if ( !lvlZoom.containsKey( lvKey )){
					String vz = anim.getAttribute("visibleMinZoom");
					if ( vz != "" ){
						lvlZoom.put(lvKey , new Double( vz ));
					} else {
						lvlZoom.put(lvKey , new Double( -1 ));
					}
				}
				
				mapSet.add(getHashKey(posxy[0],posxy[1]));
				
				/**
				System.out.print("PARTx:" + partxy[0] );
				System.out.print(" y:" + partxy[1] );
				System.out.print(" posx:" + posxy[0] );
				System.out.print(" posy:" + posxy[1] );
				System.out.println("");
				**/
				
				
//				System.out.println(lvl + ": " + href + "," + rect.x + "," + rect.y + "," + rect.width + "," + rect.height );
				
			}
		}
		
		// 後回しにしたchild containerを読み込む
		// レベルの小さいコンテンツから順に読み込む再帰呼び出しの改良を行った。 2016.12.26
		if ( isLastContainerOfLevel == true ){
			ArrayList<String> toBeReadedContainers = new ArrayList<String>();
			for ( int i = 0 ; i < nextLevelContainers.size() ; i++ ){
				toBeReadedContainers.add(nextLevelContainers.get(i));
			}
			
			nextLevelContainers = new ArrayList<String>();
			
//			System.out.println("StartNextLevelContainerReading  lvl:"+(lvl+1) + "    isLast?:"+isLastContainerOfLevel);
			for ( int i = 0 ; i < toBeReadedContainers.size() -1 ; i++ ){
//				System.out.println("NextLevelContainerReading: "+i + "   file:"+input);
				readContainer( toBeReadedContainers.get(i) , lvl+1 , false);
			}
			
			if (toBeReadedContainers.size() > 0 ){
				readContainer( toBeReadedContainers.get(toBeReadedContainers.size()-1) , lvl+1 , true);
			}
			
//			System.out.println("END NextLevelContainerReading  lvl:"+(lvl+1)+"\n\n");
		}
		
	}
	
	// レベルの小さいコンテンツから順に読み込む再帰呼び出しの改良用。 2016.12.26
	ArrayList<String> nextLevelContainers;
	
	Rectangle2D.Double getArea( String bbox ){
//		System.out.println("bb:"+bbox);
		String[] bbs = bbox.split( " *, *| +" );
		return ( new Rectangle2D.Double(
			Double.parseDouble(bbs[0]),
			Double.parseDouble(bbs[1]),
			Double.parseDouble(bbs[2]),
			Double.parseDouble(bbs[3])
		));
		
	}
	
	double[] getMatrix( Element crse ){
		String[] crs = ( crse.getAttribute("transform") ).split("matrix\\(|\\)|,");
//		System.out.println( crs[0]+":"+crs[1]+":"+crs[2]+":"+crs[3]+":"+crs[4]+":"+crs[5]+":"+crs[6]);
		double[] ans = new double[6];
		for ( int i = 0 ; i < 6 ; i++ ){
			ans[i] = Double.parseDouble( crs[i+1] );
		}
		return ( ans );
	}
	
	// 手抜き(1,2を0とみなしてる)
	Point2D.Double getGeoCoords( Point2D.Double coords , double[] crs ){
		return ( 
			new Point2D.Double(
				( coords.x - crs[4] ) / crs[0] ,
				( coords.y - crs[5] ) / crs[3]
			)
		);
	}
	
	Rectangle2D.Double getGeoRect( Rectangle2D.Double rect , double[] crs ){
		return ( 
			new Rectangle2D.Double(
				(rect.x - crs[4] ) / crs[0] ,
				((rect.y+rect.height) - crs[5]) / crs[3] ,
				rect.width / crs[0] ,
				- rect.height / crs[3]
			)
		);
	}
	
	public int[] getIndex( Long key ){
		int[] ans = new int[2];
		long kl = key.longValue( );
		ans[0] = (int)(kl / (long)100000000);
		ans[1] = (int)(kl % (long)100000000);
//		System.out.println(key+" , " + ans[0] + ":"+ans[1]);
		return ( ans );
	}
	
	public Long getHashKey( int index1 , int index2 ){
		return ( new Long((long)((long)index1 * (long)100000000 + (long)index2)));
	}
	
	public int[] getPart( Long lvl , double partAspect ){
		int[] ans = new int[2];
//		System.out.println("partAspect:" + partAspect + " lvl:"+lvl);
		ans[0] = (int)Math.round(Math.sqrt( (double)lvl / partAspect ));
		ans[1] = (int)Math.round(Math.sqrt( (double)lvl * partAspect ));
		return ( ans );
	}
	
	public int[] getBasePart( int[] part ){
		int px = part[0];
		int py = part[1];
//		System.out.println("seed part:" + part[0]+","+part[1]);
		while ( px % 2 == 0 && py % 2 == 0 ){
			px = px / 2;
			py = py / 2;
		}
		
		int[] ans = new int[2];
		ans[0] = px;
		ans[1] = py;
		return ( ans );
	}
	
	private int[] getPartXY( double tw , double th ){
		// rootArea.x.y.w.h
		// 丸め誤差を加味して、basePart[] の2^n倍のルールでpartを決める
		
		double xz = ( rootArea.width  / (double)basePart[0] ) / tw;
		double yz = ( rootArea.height / (double)basePart[1] ) / th;
//		System.out.print("xz:"+xz + " yz:"+yz);
		double az = (xz + yz) / 2.0;
		double z = 1.0;
		while ( az / z > 1.4 ){
			z = z*2.0;
		}
//		System.out.println(" z:"+z);
		return ( new int[]{ (int)(z*basePart[0]) , (int)(z*basePart[1]) });
	}
	
	private int[] getPosXY( double tx , double ty , int[] part){
		//				int posx = (int)Math.round((rect.x - rootArea.x) / rect.width);
		//				int posy = (int)Math.round((rect.y - rootArea.y) / rect.height);
		int  px = (int)Math.round((double)part[0] * (tx - rootArea.x) / rootArea.width);
		int  py = (int)Math.round((double)part[1] * (ty - rootArea.y) / rootArea.height);
		return ( new int[]{ px , py } );
	}
	
}