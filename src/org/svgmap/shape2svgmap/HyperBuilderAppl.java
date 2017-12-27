package org.svgmap.shape2svgmap;

// HyperLayerBuilder: 複数のSVG Mapコンテンツを重畳するコンテナを生成します。
// Copyright 2009-2010 by Satoru Takagi All Rights Reserved.
// 2009.09.08 コンテナをコンテンツと同じディレクトリに出力。リンクを相対パスに。
// 2009.09.09 コンテナのimageのx,y,width,heightにインポートSVGのviewBoxをそのままつけていた問題を修正・・本質的なバグ
// 2009.09.09 コンテナの出力先を指定できるようにした。コンテナの存在範囲をrectで表示できるようにした。
// 2009.10.07 SVG Tiling & Layeringの新しいCRS定義に対応
// 2012.06.05 オプション２個追加(-opacity, -viewarea top)
// 2012.06.06 オプション１個追加(-global)
// 2016.11.25 オプション３個追加(-visible, -hidden, -rootattr)
//

import java.util.*;
import java.io.*;
// import javax.servlet.*;
// import javax.servlet.http.*;
import java.net.*;
import java.text.NumberFormat ;
import java.awt.geom.*;

// XMLのクラスライブラリ
import org.w3c.dom.*;
// import org.apache.xerces.parsers.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
// XML出力用のライブラリ
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class HyperBuilderAppl {
	
	boolean isSvgTL = true; // SVG 1.2 Tiling & Layering に準拠 ( <animation , visibleMax/MinZoomを使用(こっちは両論併記)  )
	
	Document layer , topLayer;
	Transform layerG2S;
	
	String rootPath ="";
	
	double projCenter = 999; // 標準緯線の位置 999:最初のインポートSVGのまま, -999:中心 その他:その緯度
	double containerHeight = 0; // コンテナSVGのheight 0:最初のインポートSVGを継承
	
	Document container;
	Transform contG2S;
	String contCRSURI = "http://purl.org/crs/84";
	Rectangle2D.Double contGeoBBOX; // コンテンツの存在エリア このシステムでは経度x,緯度y並び
	
	
	static String containerName = "Container.svg";
	
	boolean debug = false;
	boolean appendMode = false;
	
	NumberFormat fmt = NumberFormat.getNumberInstance();
	
	Map<String, String> dataAttributes = new HashMap<String, String>(); // data-カスタム属性 2016.11.25
	
	String visibilityFile="";
	int visibilityFilter=0; // 0:none , 1:visible , -1:hidden
	
	public static void main(String args[]) throws Exception{
//		String input="san.svg";
		String input="test.svg";
		String output="";
		String vbString ="";
		
		if (args.length == 0 ){
			System.out.println("HyperBuilder: 複数のSVG Mapコンテンツを重畳するコンテナを生成します。");
			System.out.println("Copyright 2009-2016 by Satoru Takagi @ KDDI All Rights Reserved.");
			System.out.println("----------");
			System.out.println("java HyperBuilder [Options] inputfile1 [Options] [... inputfileN [Options]] ");
			System.out.println("");
			System.out.println("inputfileの重なり順:先に書いたものが上になる ");
			System.out.println("");
			System.out.println("Options   : -optionName (value)");
			System.out.println("-o        : コンテナの出力先を指定");
			System.out.println("            パスを指定");
			System.out.println("            デフォルト:inputfile1と同じディレクトリにContainer.svgとして生成");
			System.out.println("-viewarea : 初期表示エリアを指定する");
			System.out.println("            ISO6709で2点: 例: +35.0+136.0/+35.5+136.5/");
			System.out.println("            1点+半径[m] : 例: +35.25+136.25/1000");
			System.out.println("            \"top\"     : 一番上レイヤ(inputfil1e)のエリアに合わせる");
			System.out.println("            デフォルト:全てのデータを包含するエリア");
			System.out.println("-proj     : 正距円筒図法の標準緯線を指定する");
			System.out.println("            center: エリアの中心"); 
			System.out.println("            緯度: 例: 34.56");
			System.out.println("            デフォルト:最初に指定されたインポートSVGを継承");
			System.out.println("-height   : コンテナSVGのheightを指定する");
			System.out.println("            デフォルト:最初に指定されたインポートSVGを継承");
			System.out.println("-range    : 直前のinputfileに対し表示レンジを指定する");
			System.out.println("            (100ピクセルあたりのメートルで指定 ※100pix=画面上で概3cm)");
			System.out.println("            min[m/100pix]-max[m/100pix] 例: 10-1000 ");
			System.out.println("            デフォルト:常時表示");
			System.out.println("-opacity  : 直前のinputfileに対しopacityを指定する");
			System.out.println("            デフォルト:なし(1の意味)");
			System.out.println("-global   : 直前のinputfileに対しx,y,width,heightを全球規模(無条件ロード)に指定");
			System.out.println("            デフォルト:注記のとおり");
			System.out.println("-append   : 最初に指定したコンテンツ自体に他のコンテンツのリンクを追加する");
			System.out.println("            形でコンテナを生成");
			System.out.println("-rootattr : 生成されるコンテナのドキュメントルートSVG要素に任意のdata-カスタム属性を挿入");
			System.out.println("            name1=value1,name2=value2 ==> data-name1=\"value1\" data-name2=\"value2\"");
			System.out.println("-visible  : 指定した文書のみをvisibility=visibleにする (-hiddenと排他)");
			System.out.println("            入力ファイルパスに対する部分一致 (*partOfInputfile*)");
			System.out.println("-hidden   : 指定した文書のみをvisibility=hiddenにする  (-visibleと排他)");
			System.out.println("            入力ファイルパスに対する部分一致 (*partOfInputfile*)");
			System.out.println("-showtile : デバッグ・確認用(タイル境界線を出力する)");
			System.out.println("            [値無し]");
			
			System.out.println("注記：全てのinputfileには地理座標メタデータと、何らかの");
			System.out.println("      範囲属性(go:dataArea(最優先)||go:boundingBox(次)||viewBox||width,height)が必要です。");
			System.out.println("      その範囲属性を元に、コンテナのimage要素にx,y,width,heightを設定します。");
			System.out.println("      コンテナには全てのinputfileの範囲を包含するgo:BoudingBoxが設定されます。");
			System.exit(0);
		}
		
		if (args.length > 0){
			input = args[0];
		}
		
		HyperBuilderAppl hba = new HyperBuilderAppl();
		
		ArrayList<String> layerPathList  = new ArrayList<String>();
		ArrayList<String> ZoomRangeList  = new ArrayList<String>();
		ArrayList<String> OpacityList    = new ArrayList<String>(); //後々拡張するかもしれないので文字列にしておく
		ArrayList<String> AreaOptionList = new ArrayList<String>(); //後々拡張するかもしれないので文字列にしておく
		
		boolean followTopViewArea = false;
		
		// コマンドオプションの取得
		for ( int k = 0 ; k < args.length ; k++){
			if ( args[k].equals("-showtile") ){
				hba.debug = true;
			} else if ( args[k].equals("-o") ){
				++k;
				hba.rootPath = (new File (args[k])).getAbsolutePath();
				if ( ! (hba.rootPath).endsWith(".svg") ){
					hba.rootPath = hba.rootPath + File.separator + hba.containerName;
				}
			} else if ( (args[k].toLowerCase()).equals("-viewarea") ){
				++k;
				if  ( (args[k].toLowerCase()).equals("top") ){
					followTopViewArea = true;
				} else {
					vbString = args[k];
				}
			} else if ( (args[k].toLowerCase()).equals("-proj") ){
				++k;
				if ( (args[k].toLowerCase()).equals("center")){
					hba.projCenter = -999;
				} else {
					hba.projCenter = Double.parseDouble(args[k]);
				}
			} else if ( (args[k].toLowerCase()).equals("-append") ){
				hba.appendMode = true;
			} else if ( (args[k].toLowerCase()).equals("-rootattr") ){
				++k;
				String[] kvs = args[k].split(",");
				for ( int l = 0 ; l < kvs.length ; l++ ){
					String[]kv = kvs[l].split("=");
					hba.dataAttributes.put(kv[0],kv[1]);
				}
			} else if ( (args[k].toLowerCase()).equals("-height") ){
				++k;
				hba.containerHeight = Double.parseDouble(args[k]);
			} else if ( (args[k].toLowerCase()).equals("-visible") ){
				++k;
				if ( hba.visibilityFile ==""){
					hba.visibilityFile = (args[k]);
					hba.visibilityFilter = 1;
				}
			} else if ( (args[k].toLowerCase()).equals("-hidden") ){
				++k;
				if ( hba.visibilityFile ==""){
					hba.visibilityFile = (args[k]);
					hba.visibilityFilter = -1;
				}
			} else if ( (args[k].toLowerCase()).equals("-range") ){
				++k;
//				System.out.println("cap: path" + layerPathList.size() + " zrl:" + ZoomRangeList.size());
				ZoomRangeList.set(layerPathList.size() -1 , args[k]);
			} else if ( (args[k].toLowerCase()).equals("-opacity") ){
				++k;
				OpacityList.set(layerPathList.size() -1 , args[k]);
			} else if ( (args[k].toLowerCase()).equals("-global") ){
				AreaOptionList.set(layerPathList.size() -1 , "global");
			} else {
				layerPathList.add(args[k]);
				ZoomRangeList.add(null); // カラのzoomRangeを追加
				OpacityList.add(null); // カラのopacityを追加
				AreaOptionList.add(null); // カラのareaOptを追加
			}
		}
		
		
		// viewBoxをオプションから得る
		Rectangle2D.Double vb = null;
		if ( vbString.length() > 0 ){
			vb = hba.getGeoBBox( vbString );
			System.out.println( "Specified geoViewBox:" + hba.getRectStr(vb));
		}
		
		
		
		// インポートレイヤーのデータを取得し、コンテナ作成の準備をする
		
		ArrayList<Rectangle2D.Double> ImageGeoBBox = new ArrayList<Rectangle2D.Double>();
		ArrayList<String> ImagePath = new ArrayList<String>();
		
		
		hba.contG2S = new Transform( 1.0 , 0.0 , 0.0 , -1.0 , 0.0 , 0.0 );
		
		System.out.println("");
		// まずリスト構造に各インポートSVG要素の情報を収集する
		Rectangle2D.Double bb= new Rectangle2D.Double( -1000.0 , -1000.0 , 1000.0 , 1000.0 );
		for ( int k = 0 ; k < layerPathList.size() ; k++){
			
			
			// Pathを準備する
			String lpath= layerPathList.get(k);
//			System.out.println("dbg0:" + lpath );
			File path = (new File (lpath)).getAbsoluteFile() ;
			lpath = path.getAbsolutePath() ;
//			System.out.println("dbg0.1:" + path.getParent() );
			if ( k == 0 && (hba.rootPath).equals("") ){
				hba.rootPath = path.getParent() + File.separator + containerName;
			}
			String relPath = (PathUtil.getRelativePath( lpath , hba.rootPath )).replace(File.separator , "/" );
//			System.out.println("dbg1:" + lpath + " : " +  hba.rootPath);
			
			// 個々のインポートSVGファイルを読み込む
			FileInputStream fis = new FileInputStream( path );
			hba.layerG2S = new Transform();
			
			
			// インポートSVGファイルの地理座標上のBBOXを得る
			// 同時にlayer(Document)オブジェクトが一時的に生成される
			bb = hba.getLayer(fis);
			
			// トップのインポートSVGの図法(CRS transform)、ドキュメントを保持しておく
			if( k == 0 ){
				hba.contG2S = hba.layerG2S;
				System.out.println("contG2S:"+hba.contG2S );
				hba.topLayer = hba.layer;
			}
			System.out.println("layer" + (k+1) + " Path:" + relPath + " geoBBOX:" + hba.getRectStr(bb));
			
			// データ存在範囲の地理座標BBOXを構築
			if ( k == 0){
				hba.contGeoBBOX = bb;
			} else if ( ! followTopViewArea ) {
				hba.contGeoBBOX = (Rectangle2D.Double)((hba.contGeoBBOX).createUnion( bb ));
			}
			
			// リストに追加する
			ImageGeoBBox.add(bb);
			ImagePath.add(relPath);
		}
		
		// 指定されていないときはviewBoxをgeoBBOXに設定する
		if ( vb == null ){
			vb = hba.contGeoBBOX ;
		}
		
		// コンテナのBBOXを得る
		// 最初のインポートSVGファイルのCRSパラメータをコンテナのCRSパラメータにする
		
		if ( ! hba.appendMode ){
			// 最初に指定されたインポートSVGファイルを参考に、コンテナを生成する
			hba.buildContainer( hba.topLayer );
		} else {
			// 最初に指定されたインポートSVGファイルに追加する
			// 重ね順は最後のものが一番下(SVG文書的には最初の行)になる
			hba.buildContainerFromLayer( hba.topLayer );
			hba.projCenter = 999;
			hba.containerHeight = 0;
		}
		
		
		//標準緯線から図法変換パラメータを設定する
		//projCenter 標準緯線の位置 999:最初のインポートSVGのまま, -999:中心 その他:その緯度
		if ( hba.projCenter != 999 ){
			double centerLat;
			if ( hba.projCenter == -999 ){
				centerLat = vb.y + vb.height / 2.0;
			} else {
				centerLat = hba.projCenter;
			}
			double sign = Math.signum(hba.contG2S.d * hba.contG2S.a);
			double ratio = Math.cos(centerLat * Math.PI / 180.0 ) * sign;
			hba.contG2S.a = hba.contG2S.d * ratio;
			
			hba.contG2S.e = - hba.contG2S.a * hba.contGeoBBOX.x + (Math.signum(hba.contG2S.a) - 1) / 2 * hba.contG2S.a * hba.contGeoBBOX.width;
			hba.contG2S.f = - hba.contG2S.d * hba.contGeoBBOX.y + (Math.signum(hba.contG2S.d) - 1) / 2 * hba.contG2S.d * hba.contGeoBBOX.height;
		}
		
		// 画面のサイズから図法変換のパラメータを設定する
		if ( hba.containerHeight > 0 ){
			double aspect = hba.contG2S.a / hba.contG2S.d;
			hba.contG2S.d = hba.containerHeight / hba.contGeoBBOX.height * Math.signum(hba.contG2S.d);
			hba.contG2S.a = hba.contG2S.d * aspect;
			
			hba.contG2S.e = - hba.contG2S.a * hba.contGeoBBOX.x + (Math.signum(hba.contG2S.a) - 1) / 2 * hba.contG2S.a * hba.contGeoBBOX.width;
			hba.contG2S.f = - hba.contG2S.d * hba.contGeoBBOX.y + (Math.signum(hba.contG2S.d) - 1) / 2 * hba.contG2S.d * hba.contGeoBBOX.height;
		}
		
		
		// CRSを設定する
		if ( ! hba.appendMode ){ // appandModeではない場合
			hba.setCRS( hba.container , hba.contG2S , hba.contCRSURI );
		}
		
		// data-カスタム属性を設定する
		if ( hba.dataAttributes.size() > 0 ){ 
			hba.setDataAttributes( hba.dataAttributes );
		}
		
		// コンテナにイメージをセットする
		for ( int k = ImageGeoBBox.size() -1 ; k >= 0  ; k-- ){
			
			// コンテナの座標系におけるBBOXを得る
			Rectangle2D.Double imageBBox = hba.contG2S.getTransformedBBox(ImageGeoBBox.get(k));
			String[] visibleRange = null;
			String opacity = null;
			if ( ZoomRangeList.get(k) != null ){
				visibleRange = hba.getZoomRange( ZoomRangeList.get(k) );
//				System.out.print ( "zr:" + visibleRange );
			}
			
			if ( OpacityList.get(k) != null ){
				opacity = OpacityList.get(k);
			}
			
			if ( AreaOptionList.get(k) != null && (AreaOptionList.get(k)).indexOf("global") >= 0 ){
				imageBBox = hba.contG2S.getTransformedBBox(new Rectangle2D.Double(-180,-90,360,180));
			}
			
			//コンテナにimageをセットする
			if ( !(hba.appendMode && k == 0) ){ // appendModeの場合は最初のレイヤは自分自身なので入れない
				
				boolean visibility = hba.getVisibility( hba.visibilityFilter , hba.visibilityFile , ImagePath.get(k) );
				
				hba.setImage( imageBBox , ImagePath.get(k) , visibleRange , opacity , visibility);
			}
			
//			System.out.println("Layer:" + k + " path:" + ImagePath.get(k) +" IMG BBox:" + hba.getRectStr(imageBBox));
		}
		
		hba.setContainerViewBox( hba.contG2S.getTransformedBBox( vb ) ) ;
		hba.setContainerBoundingBox( hba.contG2S.getTransformedBBox( hba.contGeoBBOX ) ) ;
		System.out.println("\nSave Container to: " + hba.rootPath );
//		System.out.println("Container ViewBox:" + hba.getRectStr(hba.contG2S.getTransformedBBox( hba.contGeoBBOX )));
		hba.serialize( hba.rootPath );

	}
	
	HyperBuilderAppl(  )  {
		fmt.setMaximumFractionDigits(7);
		fmt.setGroupingUsed(false);
	}
	
	boolean getVisibility( int visibilityFilter , String visibilityFile , String path ){
		boolean ans = true;
		if ( visibilityFilter == 1 ){ // 設定されたもののみvisible
			if ( path.indexOf(visibilityFile) >=0 ){
				ans = true;
			} else { 
				ans = false;
			}
		} else if ( visibilityFilter == -1 ){ // 設定されてたもののみhidden
			if ( path.indexOf(visibilityFile) >=0 ){
				ans = false;
			} else { 
				ans = true;
			}
		}
		return ( ans );
	}
	
	Rectangle2D.Double getLayer( InputStream is  ) throws Exception {
		Rectangle2D.Double docBB;
		//System.out.println("IsSvg2Map");
		layer= DocumentBuilderFactory
			.newInstance()
			.newDocumentBuilder()
			.parse( is );
		Element rootNode = layer.getDocumentElement();
		docBB = getSvgBBox( rootNode );
		layerG2S = getSvgMapCRS( rootNode );
		Transform S2G = new Transform();
		S2G.setInv(layerG2S);
		Rectangle2D.Double globalBBox =  S2G.getTransformedBBox( docBB );
//		System.out.println(" SVG BBox:" + getRectStr(docBB) + "\n GEO BBox:" + getRectStr(globalBBox));
		return ( globalBBox );
	}
	
	
	void setContainerViewBox( Rectangle2D.Double rect ) throws Exception{
		Element rt = container.getDocumentElement();
		rt.setAttribute( "viewBox" , rect.x + " " + rect.y + " " + rect.width + " " + rect.height );
	}
	void setContainerBoundingBox( Rectangle2D.Double rect ) throws Exception{
		Element rt = container.getDocumentElement();
		rt.setAttribute( "go:dataArea" , rect.x + " " + rect.y + " " + rect.width + " " + rect.height );
	}
	
	void buildContainer( Document document ) throws Exception{
		Element rootNode = document.getDocumentElement();
		// 出力するドキュメントの設定
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace( true );
		DocumentBuilder builder = factory.newDocumentBuilder();
		DOMImplementation domImpl = builder.getDOMImplementation();
		container = domImpl.createDocument(rootNode.getAttribute("xmlns"), rootNode.getNodeName(), null);
		NamedNodeMap atts = rootNode.getAttributes();
		Element rt = container.getDocumentElement();
		for (int k = 0 ; k < atts.getLength() ; k++ ){
//					System.out.println(atts.item(k).getNodeName() + "=" + atts.item(k).getNodeValue() );
			rt.setAttribute( atts.item(k).getNodeName() ,  atts.item(k).getNodeValue() );
		}
//		rt.setAttribute( "viewBox" , rect.x + " " + rect.y + " " + rect.width + " " + rect.height );
//		System.out.println( "Rect:" + rect );
		rt.setAttribute( "xmlns:xlink" , "http://www.w3.org/1999/xlink" );
		Element contRoot = container.getDocumentElement();
		
	}
	
	void setDataAttributes( Map<String, String> attrMap ) throws Exception {
		Element rt = container.getDocumentElement();
		
		for (Map.Entry<String, String> entry : attrMap.entrySet()) {
			// キーを取得
			String key = entry.getKey();
			// 値を取得
			String val = entry.getValue();
			rt.setAttribute("data-"+key, val);
		}
	}
	
	void buildContainerFromLayer( Document document ) throws Exception{
		container = document;
	}
	
	void setCRS(Document document , Transform trans ,  String uri ){
		Node rootNode=document.getFirstChild();
		
		
		
		// metadataノードを追加
		rootNode.appendChild(document.createTextNode("\n"));
		Element metaNode=document.createElement("metadata");
		rootNode.appendChild(metaNode);
		rootNode.appendChild(document.createTextNode("\n"));
		
		// metadataノードの子供にrdf:RDFを追加
		Element rdfNode = document.createElement("rdf:RDF");
		Attr rdfNsAttr=document.createAttribute("xmlns:rdf");
		rdfNsAttr.setNodeValue("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		Attr crsNsAttr=document.createAttribute("xmlns:crs");
		crsNsAttr.setNodeValue("http://www.ogc.org/crs");
		Attr svgNsAttr=document.createAttribute("xmlns:svg");
		svgNsAttr.setNodeValue("http://www.w3.org/2000/svg");
		rdfNode.setAttributeNode(rdfNsAttr);
		rdfNode.setAttributeNode(crsNsAttr);
		rdfNode.setAttributeNode(svgNsAttr);
		
		metaNode.appendChild(document.createTextNode("\n"));
		metaNode.appendChild(rdfNode);
		metaNode.appendChild(document.createTextNode("\n"));

		rdfNode.appendChild(document.createTextNode("\n"));
		Element crsNode=document.createElement("crs:CoordinateReferenceSystem");
		rdfNode.appendChild(crsNode);
		rdfNode.appendChild(document.createTextNode("\n"));
		Attr crsURIAttr=document.createAttribute("rdf:resource");
		crsURIAttr.setNodeValue( uri );
		crsNode.setAttributeNode(crsURIAttr);
		
		Attr crsTransAttr=document.createAttribute("svg:transform");
		crsTransAttr.setNodeValue( "matrix(" + trans.a + "," + trans.b + "," + trans.c + "," 
			+ trans.d + "," + trans.e + "," + trans.f + ")"  );
		crsNode.setAttributeNode(crsTransAttr);
		
		// SVG Tiling & Layering仕様に対応
		Element gcsNode=document.createElement("globalCoordinateSystem");
		rootNode.appendChild(gcsNode);
		rootNode.appendChild(document.createTextNode("\n"));
		gcsNode.setAttribute("srsName",uri);
		gcsNode.setAttribute("transform","matrix(" + trans.a + "," + trans.b + "," + trans.c + "," 
			+ trans.d + "," + trans.e + "," + trans.f + ")");
		
	}
	
	void setImage(Rectangle2D.Double rect , String path , String[] visibleRange , String opacity , boolean visibility ){
		Element contRoot = container.getDocumentElement();
//		if ( breforeNode == null ){
//			Text topCr = container.createTextNode("\n") ;
//			breforeNode = contRoot.appendChild(topCr);
//		}
		Text cr = container.createTextNode("\n") ;
		contRoot.appendChild(cr);
		Element imageElement;
		if ( isSvgTL ) {
			imageElement = container.createElement("animation");
		} else {
			imageElement = container.createElement("image");
		}
		
		if ( !visibility ){
			imageElement.setAttribute("visibility","hidden");
		}
		
		Attr xAtt = container.createAttribute("x");
		xAtt.setNodeValue(Double.toString(rect.x));
		imageElement.setAttributeNode(xAtt);
		
		Attr yAtt = container.createAttribute("y");
		yAtt.setNodeValue(Double.toString(rect.y));
		imageElement.setAttributeNode(yAtt);
		
		Attr wAtt = container.createAttribute("width");
		wAtt.setNodeValue(Double.toString(rect.width));
		imageElement.setAttributeNode(wAtt);
		
		Attr hAtt = container.createAttribute("height");
		hAtt.setNodeValue(Double.toString(rect.height));
		imageElement.setAttributeNode(hAtt);
		
		Attr refAtt = container.createAttribute("xlink:href");
		refAtt.setNodeValue(path);
		imageElement.setAttributeNode(refAtt);
		
		if ( visibleRange != null ){
			Attr vzrAtt = container.createAttribute("go:figure-visibility");
			vzrAtt.setNodeValue( visibleRange[0] );
			imageElement.setAttributeNode(vzrAtt);
			// add for SVGT&L
			if ( visibleRange[1].length() > 0 ){
				Attr vzrAttMin = container.createAttribute("visibleMinZoom");
				vzrAttMin.setNodeValue( visibleRange[1] );
				imageElement.setAttributeNode(vzrAttMin);
			}
			if ( visibleRange[2].length() > 0 ){
				Attr vzrAttMax = container.createAttribute("visibleMaxZoom");
				vzrAttMax.setNodeValue( visibleRange[2] );
				imageElement.setAttributeNode(vzrAttMax);
			}
			
		}
		
		if ( opacity != null ){
			Attr opAtt =  container.createAttribute("opacity");
			opAtt.setNodeValue( opacity );
			imageElement.setAttributeNode(opAtt);
		}
		
		contRoot.appendChild( imageElement );
		Text cr2 = container.createTextNode("\n") ;
		contRoot.appendChild(cr2);
		
		if ( debug ){
			Element rectElement = container.createElement("rect");
			rectElement.setAttribute("x" , Double.toString(rect.x));
			rectElement.setAttribute("y" , Double.toString(rect.y));
			rectElement.setAttribute("width" , Double.toString(rect.width));
			rectElement.setAttribute("height" , Double.toString(rect.height));
			rectElement.setAttribute("fill","none");
			rectElement.setAttribute("stroke","red");
			rectElement.setAttribute("stroke-width","0.0005");
			contRoot.appendChild( rectElement );
			Text cr3 = container.createTextNode("\n") ;
			contRoot.appendChild(cr3);
		}
		
	}
		
	public void serialize( String outs) throws Exception{
		
		// コンテナの出力
		FileOutputStream osFile = new FileOutputStream( outs  );
		OutputStreamWriter fos = new OutputStreamWriter( osFile , "UTF-8" );
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer = transFactory.newTransformer();
		DOMSource source = new DOMSource(container);
		StreamResult result = new StreamResult(fos);
		transformer.transform(source, result);
		
		
	}
	private Rectangle2D.Double getSvgBBox(Node svgr ){
		Rectangle2D.Double BB = new Rectangle2D.Double( 0.0 , 0.0 , 0.0 , 0.0 );
		double x = 0;
		double y = 0;
		double w = 0;
		double h = 0;
		boolean falure = false;
		
		// 先ず、go:dataAreaを見てその次にgo:boundingBoxのパラメータを見てみる・・これが一番確からしいはずなので
		// 20101022変更(go:boundingBoxがSVGMapTKで独特の動きをするので・・)
		try {
			String bbox = ((Element)svgr).getAttribute("go:dataArea");
//			System.out.println("boundingBox:" + bbox);
			StringTokenizer st = new StringTokenizer(bbox, " ,");
			x = Double.parseDouble(st.nextToken());
			y = Double.parseDouble(st.nextToken());
			w = Double.parseDouble(st.nextToken());
			h = Double.parseDouble(st.nextToken());
//			System.out.println(x + " , " + y + " , " + w + " , " + h + ":" + falure);
		} catch (Exception e) {
			falure = true;
		}
		
		if ( falure ){
			falure = false;
			try {
				String bbox = ((Element)svgr).getAttribute("go:boundingBox");
	//			System.out.println("boundingBox:" + bbox);
				StringTokenizer st = new StringTokenizer(bbox, " ,");
				x = Double.parseDouble(st.nextToken());
				y = Double.parseDouble(st.nextToken());
				w = Double.parseDouble(st.nextToken());
				h = Double.parseDouble(st.nextToken());
	//			System.out.println(x + " , " + y + " , " + w + " , " + h + ":" + falure);
			} catch (Exception e) {
				falure = true;
			}
		}
		
//		System.out.println(falure);
		
		// 存在しない場合は、viewBoxを見てみる 20100802変更
		if ( falure ){
			falure = false;
			try {
				String bbox = ((Element)svgr).getAttribute("viewBox");
//				System.out.println("viewBox:" + bbox);
				StringTokenizer st = new StringTokenizer(bbox, " ,");
				x = Double.parseDouble(st.nextToken());
				y = Double.parseDouble(st.nextToken());
				w = Double.parseDouble(st.nextToken());
				h = Double.parseDouble(st.nextToken());
//				System.out.println(x + " , " + y + " , " + w + " , " + h + ":" + falure);
			} catch (Exception e) {
				falure = true;
			}
		}
//		System.out.println(falure);
		
		// それでも無い場合はwidth,heightを使う
		if ( falure ){
			falure = false;
			try {
				x = 0.0;
				y = 0.0;
				w = Double.parseDouble(((Element)svgr).getAttribute("width"));
				h = Double.parseDouble(((Element)svgr).getAttribute("height"));
//		System.out.println(x + " , " + y + " , " + w + " , " + h + ":" + falure);
			} catch (Exception e) {
				falure = true;
			}
		}
		
//		System.out.println(falure);
		if ( !falure ) {
			BB.x = x;
			BB.y = y;
			BB.width = w;
			BB.height = h;
		}
		
		System.out.println( "SVG_BBOX x:" + BB.x + " y:" + BB.y + " w:" + BB.width + " h:" + BB.height );
		return ( BB );
	}
	
	
	
	private Transform getSvgMapCRS(Element svgr ){
		Transform crsp = new Transform();
		String transform="";
		try{
			NodeList nl = svgr.getElementsByTagName("globalCoordinateSystem");
			Element crse = (Element)nl.item(0);
			transform = crse.getAttribute("transform");
		} catch ( Exception e ) {
			// いい加減な処理ですが・・ globalCoordinateSystemが無かった場合は、crs:...を探す
//			System.out.println("no gcs: " + e );
			NodeList nl = svgr.getElementsByTagName("crs:CoordinateReferenceSystem");
			Element crse = (Element)nl.item(0);
			transform = crse.getAttribute("svg:transform");
		}
		
		String tfValueI=transform.trim().substring(7);
		tfValueI=tfValueI.substring(0,tfValueI.length()-1); 
	// System.out.println( "CRST:" + tfValueI);
		StringTokenizer stI = new StringTokenizer(tfValueI,", ");
		try {
			crsp.a= Double.parseDouble(stI.nextToken());
			crsp.b= Double.parseDouble(stI.nextToken());
			crsp.c= Double.parseDouble(stI.nextToken());
			crsp.d= Double.parseDouble(stI.nextToken());
			crsp.e= Double.parseDouble(stI.nextToken());
			crsp.f= Double.parseDouble(stI.nextToken());
		} catch  (NumberFormatException e ) {
		}
//		System.out.println("tf:" + crsp);
		return ( crsp );
	}
	
	public String getRectStr( Rectangle2D.Double rect ){
		return ( "x:" + fmt.format(rect.x) + " y:" + fmt.format(rect.y) + " w:" + fmt.format(rect.width) + " h:" + fmt.format(rect.height) );
	}
	
	
	String[] getZoomRange( String rangeStr ) throws Exception { // [0]:カンマ区切りminmax, [1]:min,[2]:max (無い場合は-1)
		String[] ans = new String[3];
		// contG2S
//		double scale = Math.sqrt( contG2S.a *  contG2S.a + contG2S.d * contG2S.d ); // |ad-bc?|
		StringTokenizer st = new StringTokenizer(rangeStr , "-" , true );
		int tkC = st.countTokens();
//		System.out.println("tkc:" + tkC );
		String tk = st.nextToken();
		double p1 , p2;
		if ( tk.indexOf("-") >= 0 ){ // -1000のような記述
			p1 = 0;
			p2 = Double.parseDouble(st.nextToken());
		} else {
			p1 = Double.parseDouble(tk);
			if ( tkC <= 2 ){ // 100- や 100のような記述
				p2 = -1; // -1:無限
			} else {
				tk = st.nextToken(); // - を読み飛ばし
				if ( tk.indexOf("inf") >=0 ){
					p2 = -1;
				} else {
					p2 = Double.parseDouble(st.nextToken());
				}
			}
		}
		
		String maxp , minp;
		
		if ( p1 > 0 ){
			p1 = getZoom( p1 );
		}
		if ( p2 > 0 ){
			p2 = getZoom( p2 );
		}
		if ( p1 > 0 && p2 > 0 && p2 > p1 ){
			double tmp = p1;
			p1 = p2;
			p2 = tmp;
		}
		
//		p2 = getZoom(Double.parseDouble(st.nextToken()));
		
		NumberFormat nFmt = NumberFormat.getNumberInstance();
		nFmt.setGroupingUsed(false);
		nFmt.setMaximumFractionDigits(0);
		
		if ( p2 > 0 ){
			minp = nFmt.format(p2);
		} else {
			minp ="";
		}
		ans[1] = minp;
		
		ans[2] ="";
		if ( p1 > 0 ){
			maxp = "," + nFmt.format(p1);
			ans[2] = nFmt.format(p1);
		} else {
			maxp ="";
		}
		
		ans[0] = minp + maxp;
		
		return ( ans );
	}
	
	double getZoom( double range ){
		return( 40000000.0 * 100.0 * 100.0 / ( range * 360.0 * Math.abs(contG2S.d) ) );
	}
	
	Rectangle2D.Double getGeoBBox ( String areaStr ) throws Exception {
		double radius;
		ISO6709 point1 , point2;
		StringTokenizer st = new StringTokenizer(areaStr , "/" );
		if ( st.countTokens() != 2 ){
			throw new IllegalArgumentException("viewareaのパラメータが違います");
		}
		
		point1 = new ISO6709(st.nextToken());
		String p2 = st.nextToken();
		if ( p2.indexOf("+") >= 0 || p2.indexOf("-") >= 0 ){
			point2 = new ISO6709( p2 );
			if (point1.latitude > point2.latitude){
				double tmp = point1.latitude;
				point1.latitude = point2.latitude;
				point2.latitude = tmp;
			}
			if (point1.longitude > point2.longitude){
				double tmp = point1.longitude;
				point1.longitude = point2.longitude;
				point2.longitude = tmp;
			}
		} else {
			radius = Double.parseDouble(p2);
			double rLon = ( 360.0 / ( 40000000.0 * Math.cos( point1.latitude * Math.PI/180.0 ) ) ) * radius;
			double rLat = ( 360.0 / 40000000.0 ) * radius;
			point1.latitude = point1.latitude - rLat;
			point1.longitude = point1.longitude - rLon;
			point2 = new ISO6709( (point1.latitude + rLat * 2.0) , (point1.longitude + rLon * 2.0) );
		}
		return ( new Rectangle2D.Double( point1.longitude , point1.latitude ,point2.longitude - point1.longitude , point2.latitude - point1.latitude ));
	}

}
