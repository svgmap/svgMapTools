package org.svgmap.shape2svgmap;

// SVG Mapデータを作成するクラスです
// Copyright 2007-2016 by Satoru Takagi
//
// 2007.05.08 : Ver.1
// 2010.07 デフォルトスタイル系の改修
// 2010.08.19 geoTools 2.6.5 support
// 2010.10.21 animationに対応
// 2012.02.08 rectとcustomAttrの機能を追加。SVGMapTilesの方にはまだ追加してないので注意・・・
// 2012.04.10 putImageにvisibleMinMaxZoom追加
// 2012.07.30 POIの改修に着手 putPoiShape() <=vectorPoiShapesを使う 
// 2013.10.21 putUseでPOIを使い分けられるようにする　ただしpoiが定義されてないと、そのままエラーに・・・ putPoiShapeはintでこっちはStringで不整合中・・
// 2014.03.20 2月に遅延ファイル出力に変更、それに伴うバグの修正
// 2016.03.22 ファイル出力なしの状態での性能評価
// 2016.10.31 CustomAttrをsvgMapTilesでも使えるように改修
// 2017.09.15 minor update (add hmmm message..)
// 2018.07.10 クリッピングされたデータのputPolylineでの、無駄なデータ生成を抑制(polygon用クリッピングルーチンを流用しているのが原因・・)
//

import java.io.*;
import java.io.IOException;
import java.util.*;
import java.lang.*;
import java.net.URL;
import java.net.URI;
import java.text.NumberFormat ;
import com.vividsolutions.jts.geom.*;


public class SvgMap {
	
	private Writer out;
	private NumberFormat nFmt , opaFmt;
	private vectorPoiShapes vps;
	private boolean hasMicroMeta = false;
	private String microMeta2Schema = "";
	private String microMetaProp="";
	private String metaNs;
	private String metaUrl;
	private String customAttributes="";
//	private StringBuffer pathString;
	public StringBuffer pathString;
	public StringBuffer strokeString;
	public File svgFile;
	public long length;
	public int nElements;
	public int nPoints;
	public long limit; 
	public boolean isSvgTL = true;
	public boolean forceOos = false;
	public boolean isDefaultCustomAttr = true; // 2016/10/31 <g>要素にcustomAttrを付けるかどうか
	public boolean appendableCustomAttr = true; // 2016/10/31 svgmaptilesの場合はappendableにするとまずい・・
	
	public boolean nullFileDebug = false; // trueで、null Streamへ出力するようにして、 ファイル出力なしの状態での性能評価 2016.3.22
	

/**
	SvgMap( Writer sw , NumberFormat nf ){
		out = sw;
		nFmt = nf;
	}
**/	
	
	
	SvgMap( String svgFileName , NumberFormat nf  ) throws Exception{
		vectorPoiShapes vps = null;
		initSvgMap( svgFileName , nf , vps );
	}
	
	SvgMap( String svgFileName , NumberFormat nf , vectorPoiShapes vpShapes ) throws Exception{
		initSvgMap( svgFileName , nf , vpShapes );
	}
	
	public void flush() throws Exception{
		out.flush();
	}
	
	private void initSvgMap( String svgFileName , NumberFormat nf , vectorPoiShapes vpShapes ) throws Exception{
//		System.out.println("Make New SVGMap:" + svgFileName);
		length = 0;
		nPoints = 0;
		nElements = 0;
		limit = 100000000; //100MBぐらい・・
		// 出力SVGファイルを用意する。
		svgFile = new File( svgFileName );
		// 遅延ファイル書き出しを試してみる2014.2
//		FileOutputStream osFile = new FileOutputStream( svgFile );
//		out = new BufferedWriter(new OutputStreamWriter( osFile , "UTF-8" ));
		out = new CharArrayWriter( 500 );
		nFmt = nf;
		vps = vpShapes;
		opaFmt = NumberFormat.getNumberInstance();
		opaFmt.setGroupingUsed(false);
		opaFmt.setMaximumFractionDigits(2);
		pathString = new StringBuffer();
		strokeString = new StringBuffer();
	}
	
	HashMap<String,String> entityMap = new HashMap<String,String>();
	public void addEntity( String name , String value ){
		entityMap.put ( name , value );
	}
	
	public void putHeader( double viewBoxX , double viewBoxY , double viewBoxW , double viewBoxH ) throws Exception{
		// SVG のヘッダ部分など
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		Iterator it = entityMap.keySet().iterator();
		if ( it.hasNext() ){
			out.write("<!DOCTYPE svg [\n");
			while (it.hasNext()) {
				String ename = (String)it.next();
				out.write(" <!ENTITY " + ename + " '" + entityMap.get(ename) + "'>\n");
			}
			out.write("]>\n");
		}
		out.write("<svg  xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:go=\"http://purl.org/svgmap/profile\" ");
		
		if ( hasMicroMeta ) {
			out.write("xmlns:" + metaNs + "=\"" + metaUrl + "\" ");
		}
		
		if ( microMeta2Schema != "" ){
			out.write("property=\"" + microMeta2Schema + "\" ");
		}
		
		out.write("viewBox=\"" + viewBoxX + " " + viewBoxY + " " + viewBoxW + " " + viewBoxH + "\" ");
		// add 20100802 (viewBoxと実データの存在範囲が異なる場合があるが、とりあえず省略)
		// Mod 20101022 (go:boundingBoxがSVG Map Toolkitでは表示領域の抑制のために動くため別の属性を使うことにした)
		out.write("go:dataArea=\"" + viewBoxX + " " + viewBoxY + " " + viewBoxW + " " + viewBoxH + "\" ");
			
		out.write(">\n");
	}
	
	// デフォルトプロパティ関連関数群
	// 常にデフォルト<g>を設定した上で、非デフォルト値だけプロパティ設定することができるようになった(2010/06/22)
	boolean hasDefaultStyle = false;
	String defaultFill = "";
	String defaultStroke = "";
	double defaultStrokeWidth = -1;
	double defaultOpacity = -1;
	double defaultFontSize = -1;
	boolean defaultVE = false;
	
	String fillProp( String input ){
		return (  input.equals( defaultFill ) ? "" : ("fill=\"" + input+ "\" ") );
	}
	String strokeProp( String input ){
		return (  input.equals( defaultStroke ) ? "" : ("stroke=\"" + input + "\" ") );
	}
	String strokeWidthProp( double input ){
		return (  (input == defaultStrokeWidth || input == 0 ) ? "" : ("stroke-width=\"" + nFmt.format(input) + "\" ") );
	}
	String fontSizeProp( double input ){
		return (  input == defaultFontSize ? "" : ("font-size=\"" + nFmt.format(input) + "\" ") );
	}
	String opacityProp( double input ){
		return (  ( input == defaultOpacity || input == 1.0 ) ? "" : ("opacity=\"" + input + "\" ") );
	}
	
	String VEProp( boolean input ){
		String ans = "";
		if ( input != defaultVE ){
			if ( input ){
				ans = "vector-effect=\"non-scaling-stroke\" ";
			} else {
				ans = "vector-effect=\"default\" ";
			}
		}
		return (  ans );
	}
	
	
	public void setDefaultStyle( String defFill , double defStrokeWidth , String defStroke , double defOpacity , boolean vectorEffect ) throws Exception{
		defaultFill = defFill;
		defaultStrokeWidth = defStrokeWidth;
		defaultStroke = defStroke;
		defaultOpacity = defOpacity;
		defaultVE = vectorEffect;
		
		if ( defaultFill.equals("none") ){
			noFillGroup = true;
		}
		
		if ( hasDefaultStyle ){ // 既に先に一度デフォルトスタイル設定している場合は</g>する
			out.write("</g>\n");
		}
		out.write("<g ");
		out.write( capDefStyle );
		
		if ( defFill.length() != 0 ){
			out.write("fill=\"" + defFill + "\" fill-rule=\"evenodd\" ");
		}
		if ( defStroke.length() != 0 ){
			out.write("stroke=\"" + defStroke + "\" ");
		}
		if ( defStrokeWidth > 0 ){
			out.write("stroke-width=\"" + nFmt.format(defStrokeWidth) + "\" " );
		}
		if ( defOpacity > 0 ){
			out.write("opacity=\"" + defOpacity + "\" " );
		}
		if ( vectorEffect ){ // bevelはとりあえずvectorEffectのときだけにしてみたけど、いつも付いてて良いかも
			out.write("vector-effect=\"non-scaling-stroke\" stroke-linejoin=\"bevel\" ");
		}
		
		if ( customAttributes.length() > 0 && isDefaultCustomAttr ){ // 2012.2.8 add isDefaultCustomAttr: 個々のelemにcustomAttrを付けたい場合に対応(2016/10/31)
			out.write(customAttributes);
			customAttributes="";
		}
		
		out.write(">\n");
		hasDefaultStyle = true;
		capDefStyle="";
	}
	
	// これは上に統合すべき 2010/06/07
	String capDefStyle ="";
	boolean strokeGroup = false; // ペアの図形がストロークを持つときに設定(textでstroke消すため)
	boolean noFillGroup = false; // ペアの図形が塗りを持たないときに設定(textでfill設定するため)
	public void setDefaultCaptionStyle( double defFontSize , boolean strokeG ) throws Exception{
		defaultFontSize = defFontSize;
		capDefStyle="font-size=\"" + nFmt.format(defFontSize) + "\" text-anchor=\"middle\" ";
		strokeGroup = strokeG;
	}
	
	public boolean putFooter()throws Exception{ // 最後にファイル出力サイズチェックを行うことにした。 2017.4.19
		termAnchor(); // 忘れている人用 2011/02
		termGroup(); // 同上
		if ( hasDefaultStyle ){
			out.write("</g>\n");
		}
		out.write("</svg>\n");
//		System.out.println("File:" + svgFile.length());
		out.close();
		if ( nElements == 0 ) {
			if ( nullFileDebug ){
				// do nothing
			} else {
				boolean deleted = svgFile.delete();
				if ( ! deleted ){
//		System.out.println("File:Can't deleted:" + svgFile);
					svgFile.deleteOnExit();
				}
			}
		}
//		System.out.println("File:" + svgFile.length());
		
		// added a part of checksize()
		boolean ret;
		if ( nullFileDebug ){
			length = ((NullWriter)out).length();
		} else {
			length = svgFile.length();
		}
		if ( forceOos && nElements > 0 ){
			ret = false;
		} else if ( length < limit ){
			ret = true;
		} else {
			ret = false;
		}
		
		return ( ret );
		
		
	}
	
	public void removeFile()throws Exception{
		out.close();
		if ( nullFileDebug ){
			// do nothing
		} else {
			if (svgFile.exists()){ // 2016.5.11 added forceOosのケースでは無い可能性があるので
				boolean deleted = svgFile.delete();
				if ( ! deleted ){
//				System.out.println("File:Can't deleted:" + svgFile);
					svgFile.deleteOnExit();
				}
			}
		}
	}
	
	
	public void putCrs( double a ,  double b , double c , double d , double e , double f ) throws Exception{
		// CRSを出力
		out.write("<metadata>\n");
		
		// CRSを出力
		out.write(" <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:crs=\"http://opengis.org/xmldtds/transformations.dtd\" xmlns:svg=\"http://www.w3.org/svg\"");
		out.write(" >\n");
		out.write("  <rdf:Description>\n");
		out.write("   <crs:CoordinateReferenceSystem rdf:resource=\"http://purl.org/crs/84\" ");
		out.write("svg:transform=\"matrix(" + a + "," + b + "," + c + "," + d + "," + e + "," + f +")\" />\n");
		out.write("  </rdf:Description>\n");
		out.write(" </rdf:RDF>\n");
		out.write("</metadata>\n");
		
		// add 2009.10.7 support for SVG Tiling & Layering spec
		out.write("<globalCoordinateSystem srsName=\"http://purl.org/crs/84\" ");
		out.write("transform=\"matrix(" + a + "," + b + "," + c + "," + d + "," + e + "," + f +")\" />\n");
//		System.out.println("File:" + svgFile.length());
	}

	public void putComment( String comment ) throws Exception{
		out.write("<!-- " + comment + " -->\n");
	}
	
	
	// 図形要素ではない構造のセッター類
	private boolean tobeTermAnchor = false;
	private boolean tobePutAnchor = false;
	private boolean hasId = false;
	private boolean tobeTermGroup = false; // グループは１重しかサポートしていない・・・
	private boolean tobePutGroup = false; 
	private boolean anchorFirst = false;
	// IDのセッター
	private String id ="";
	
	private int subId = 0;
	private void putIdAndMicroMeta() throws Exception{
		if (hasId){
			if ( inheritMeta ){
				if ( subId != 0 ){
					out.write("id=\"" + id + "-" + subId + "\" ");
				} else {
					out.write("id=\"" + id + "\" ");
				}
				++ subId;
			} else {
				out.write("id=\"" + id + "\" ");
				hasId = false;
			}
		}
		if ( microMetaProp.length() > 0 ){
			out.write( microMetaProp );
			if ( !inheritMeta ){
				microMetaProp="";
			}
		}
		
		
		if (customAttributes.length() > 0){
			out.write( customAttributes );
			if ( !inheritMeta ){
				customAttributes ="";
			}
		}
	}
	
	public void setId( String sid ){
		id = sid;
		hasId = true;
	}
	
	// アンカーとグループは、それぞれ一階層しかサポートされていない・・・・
	
	// アンカーのセッター
	private String title , link;
	boolean tgt;
	public void setAnchor(String ttl , String lnk , boolean toTarget) {
		if (tobePutAnchor == false){
			title = ttl;
			link = lnk;
			tgt = toTarget;
			if (tobePutGroup){ // アンカーは、グループの内側に設定されている
				anchorFirst = false;
			} else { // アンカーは、グループ全体に設定されている
				anchorFirst = true;
			}
		}
		tobePutAnchor = true;
	}
	
	public void setAnchor(String ttl , String lnk ) {
		setAnchor(ttl , lnk , false );
	}
	
	public void setAnchor( String lnk ) {
		setAnchor("" , lnk , false );
	}

	
	private void putAnchor() throws Exception{
		if ( tobePutAnchor ){
			out.write("<a ");
			putIdAndMicroMeta();
			if (title !=""){
				out.write("xlink:title=\"" + title + "\" ");
			}
			if (link !=""){
				out.write("xlink:href=\"" + link + "\" ");
			}
			if (tgt){
				out.write("target=\"null\" ");
			}
			out.write(">\n");
			tobePutAnchor = false;
			tobeTermAnchor = true;
			title = "";
			link = "";
			tgt = false;
		}
	}
	
	public void termAnchor()throws Exception{
		if (tobeTermAnchor){
			if (tobeTermGroup && anchorFirst){ // 間違っている場合の処理
				termGroup();
			}
			out.write("</a>\n");
		}
		tobeTermAnchor = false;
		tobePutAnchor = false;
	}
	
	// グループのセッター
	public void setGroup() {
		if ( tobePutGroup == false ){
			if ( tobePutAnchor ){ // グループがアンカーの内側にある
				anchorFirst = true;
			} else { // グループは、アンカーの外側
				anchorFirst = false;
			}
		}
		tobePutGroup = true;
	}
	private void putGroup() throws Exception{
		if (tobePutGroup){
			out.write("<g ");
			putIdAndMicroMeta();
			out.write(">\n");
			tobePutGroup = false;
			tobeTermGroup = true;
		}
	}
	public void termGroup() throws Exception{
		if (tobeTermGroup){
			if (tobeTermAnchor && anchorFirst == false){
				termAnchor();
			}
			out.write("</g>\n");
		}
		tobeTermGroup = false;
		tobePutGroup = false;
	}
	
	
	// geomertry collection(本来はGroup)にmetadataが設定されていると、
	// それが最初のgeometryにしか設定されないことへの対策 2014.5.1
	boolean inheritMeta = false;
	public void setShadowGroup(){
		inheritMeta = true;
		subId = 0;
	}
	public void termShadowGroup(){
		inheritMeta = false;
		microMetaProp="";
		customAttributes ="";
		hasId = false;
	}
	
	
	private void putGroupAnchor()throws Exception{
		if ( anchorFirst ){
			putAnchor();
			putGroup();
		} else {
			putGroup();
			putAnchor();
		}
	}
	
	// 図形要素の設置
	//Rect
	public boolean putRect( Coordinate coo , double width , double height , String fillColor , double strokeWidth , String strokeColor , double opacity )throws Exception{
		putGroupAnchor();
		out.write("<rect ");
		putIdAndMicroMeta();
		out.write( opacityProp( opacity ) + strokeWidthProp( strokeWidth ) + strokeProp( strokeColor ) + fillProp( fillColor ) );
		out.write("x=\"" + nFmt.format(coo.x)+"\" ");
		out.write("y=\"" + nFmt.format(coo.y)+"\" ");
		out.write("width=\"" + nFmt.format(width)+"\" ");
		out.write("height=\"" + nFmt.format(height));
		out.write ("\"/>\n");
		nPoints += 2;
		return ( checkSize() );
	}
	
	//Polyline
	public boolean putPolyline( Coordinate[] coord , String strokeColor , double strokeWidth , double opacity ) throws Exception{
		putGroupAnchor();
		out.write("<polyline ");
		putIdAndMicroMeta();
		
		out.write( opacityProp( opacity ) + strokeWidthProp( strokeWidth ) + strokeProp( strokeColor ) );
		out.write("points=\"");
		pathString = new StringBuffer();
		for (int i = 0 ; i < coord.length ; i++ ){
			pathString.append(nFmt.format(coord[i].x));
			pathString.append(",");
			pathString.append(nFmt.format(coord[i].y));
			pathString.append(" ");
//			out.write( nFmt.format(coord[i].x) + "," + nFmt.format(coord[i].y) + " " );
		}
		out.write(pathString.toString());
	
	
		out.write ("\"/>\n");
		nPoints += coord.length;
		return ( checkSize() );
	}
	
	public boolean putPolyline( PolygonDouble pol , String strokeColor , double strokeWidth , double opacity ) throws Exception{
//		String outStr ="";
		pathString = new StringBuffer();
		StringBuffer subPathString = new StringBuffer();
		boolean hasRealPath=false;
		boolean hasRealSubPath=false;
		for ( int i = 0 ; i < pol.npoints ; i++ ){ // カラのデータやM連発のデータがあるので、あらかじめd生成とチェックを行う
			if ( pol.clippedEdge[i] || i == 0 ){
				if ( hasRealSubPath ){
					pathString.append(subPathString);
				}
				subPathString = new StringBuffer();
				hasRealSubPath = false;
				subPathString.append("M");
				subPathString.append( nFmt.format(pol.xpoints[i]) );
				subPathString.append(",");
				subPathString.append( nFmt.format(pol.ypoints[i]) );
			} else {
				if ( !hasRealPath ){
					hasRealPath = true;
				}
				if ( !hasRealSubPath ){
					hasRealSubPath = true;
				}
				subPathString.append("L");
				subPathString.append( nFmt.format(pol.xpoints[i]) );
				subPathString.append(",");
				subPathString.append( nFmt.format(pol.ypoints[i]) );
			}
		}
		if ( hasRealSubPath ){
			pathString.append(subPathString);
		}
		
		if (hasRealPath){
			putGroupAnchor();
			out.write("<path ");
			putIdAndMicroMeta();
			out.write( opacityProp( opacity ) + strokeWidthProp( strokeWidth ) + strokeProp( strokeColor ) );
			out.write( "d=\"");
			out.write(pathString.toString());
			out.write ("\"/>\n");
//			System.out.println("PolyLine");
			nPoints += pol.npoints;
			return ( checkSize() );
		} else {
			boolean ret = true;
			if ( forceOos && nElements > 0 ){
				ret = false;
			}
			return ( ret ); // trueだけで良いのかも・・・
		}
//		System.out.println("File:" + svgFile.length());
	}
	
	// Polygon
	public void setInterior( Coordinate[] coord ){
		for ( int i = 0 ; i < coord.length ; i++ ){
			if ( i!= 0 ){
//				pathString += "L" ;
				pathString.append("L") ;
			} else {
				pathString.append("M") ;
			}
//			pathString.append( nFmt.format(coord[i].x) + " " + nFmt.format(coord[i].y) + " ");
			pathString.append( nFmt.format(coord[i].x) );
			pathString.append( "," );
			pathString.append( nFmt.format(coord[i].y) );
//			pathString.append( " " );
		}
		pathString.append("Z") ;
		nPoints += coord.length;
	}
	public void setExterior(Coordinate[] coord ){
		pathString = new StringBuffer();
		setInterior( coord );
	}
	
	public void setInterior( PolygonDouble pol ){
		for ( int i = 0 ; i < pol.npoints ; i++ ){
			if ( i!= 0 ){ // 始点ではない
				if (pol.clippedEdge[i]){
					pathString.append( "L") ;
					strokeString.append( "M") ;
				} else {
				pathString.append( "L") ;
				strokeString.append( "L") ;
				}
			} else { // 始点
				pathString.append( "M") ;
				strokeString.append( "M") ;
			}
//			pathString.append( nFmt.format(pol.xpoints[i]) + " " + nFmt.format(pol.ypoints[i]) + " ");
			pathString.append( nFmt.format(pol.xpoints[i]) );
			pathString.append( "," );
			pathString.append( nFmt.format(pol.ypoints[i]) );
//			pathString.append( " " );
//			strokeString.append( nFmt.format(pol.xpoints[i]) + " " + nFmt.format(pol.ypoints[i]) + " ");
			strokeString.append( nFmt.format(pol.xpoints[i]) );
			strokeString.append( "," );
			strokeString.append( nFmt.format(pol.ypoints[i]) );
//			strokeString.append( " " );
		}
		pathString.append( "Z") ;
//		System.out.println("setInterior:pathStringLength:" + pathString.length());
		nPoints += pol.npoints;
	}
	
	boolean clipped = false;
	public void setExterior( PolygonDouble pol ){
		clipped = pol.clipped;
		pathString = new StringBuffer();
		strokeString = new StringBuffer();
		setInterior( pol );
	}
	
	public boolean putPolygon(String fillColor , double strokeWidth , String strokeColor  ) throws Exception{
		return (putPolygon( fillColor , strokeWidth , strokeColor , 1.0 ));
	}
	
	public boolean putPolygon(String fillColor , double strokeWidth , String strokeColor , double opacity ) throws Exception{
//		strokeColor=""; // debug
//		System.out.println("strokeColor:"+strokeColor);
		// stroke colorが設定されているときだけ、輪郭を描きます
		boolean cs = true;
		if (pathString.length() > 0 ){
			putGroupAnchor();
			out.write("<path "); // 主たるポリゴン図形の描画
			putIdAndMicroMeta();
			if ( strokeColor.length() > 0 ){ // 枠線がある場合
				if ( !clipped ){ // クリッピングされていない場合
					out.write( strokeWidthProp( strokeWidth ) + strokeProp( strokeColor ) );
				} else {
					// デフォルトで枠線が出てしまうので消す
					out.write("stroke=\"none\" ");
				}
			}
			out.write( opacityProp( opacity ) );
			out.write( fillProp( fillColor ) );
			out.write("d=\"" + pathString.toString()  + "\"/>\n");
//			++ nElements; これはバグでしょ・・・ダブルカウント
			
			cs = checkSize(); // nElementsがstrokeColor.length() > 0 だと早く加算されてファイル生成が破たんするので、先にcheckSize() リンク核があるときはその分多めの評価になるけど 2014.3.20
			
			if ( clipped == true && strokeColor.length() > 0 ){ //
				out.write("<path fill=\"none\" ");
				out.write( strokeWidthProp( strokeWidth ) + strokeProp( strokeColor ) );
				out.write("d=\"" + strokeString.toString()  + "\"/>\n");
				++ nElements; 
			}
		}
		pathString = new StringBuffer();
		strokeString = new StringBuffer();
//		System.out.println("Polygon");
//		System.out.println("File:" + svgFile.length());
		return ( cs );
	}
	
	// Text
	public boolean putText( Coordinate coo , double size , String attr , boolean abs ) throws Exception{
		return ( putText( coo , size , attr , abs , 0 ) );
	}
	public boolean putText( Coordinate coo , double size , String attr , boolean abs , double textShift) throws Exception{
		putGroupAnchor();
		out.write( "<text ");
		out.write( fontSizeProp( size ) );
		
		if ( strokeGroup ){
			out.write("stroke=\"none\" ");
		}
		if ( noFillGroup ){
//			out.write("fill=\"black\" ");
			out.write("fill=\"" + defaultStroke + "\" ");
		}
		
		if ( abs ){
			out.write( "transform=\"ref(svg," + nFmt.format(coo.x) + "," + nFmt.format(coo.y) + ")\"" );
			if ( textShift != 0 ){
				out.write( " y=\"" + nFmt.format(textShift) + "\"" );
			}
			out.write( ">" + attr + "</text>\n" );
		} else {
			out.write( "x=\"" + nFmt.format(coo.x) + "\" y=\"" + nFmt.format(coo.y) + "\">" + attr + "</text>\n");
		}
		++ nPoints;
		return ( checkSize() );
	}
	
	// Use , PoiShape
	public double defaultPoiSize = 0;
	
	public boolean putUse( Coordinate coo , String fillColor ) throws Exception{
		return (putUse( coo , fillColor , false));
	}
	
	
	public boolean putUse( Coordinate coo , String fillColor , boolean fixedSize ) throws Exception{
		return (putUse( coo , fillColor , fixedSize , "p0" ));
	}
	
	// add 2013.10.21
	public boolean putUse( Coordinate coo , String fillColor , boolean fixedSize , String symbolId ) throws Exception{
		putGroupAnchor();
		out.write("<use ");
		out.write("xlink:href=\"#" + symbolId + "\" ");

		putIdAndMicroMeta();
		out.write( fillProp( fillColor ) );
		
		if ( fixedSize ){
			// transform="ref(svg,266.427442841629,174.075712909382)" x="0" y="0"
			out.write("transform=\"ref(svg," + nFmt.format(coo.x) +"," + nFmt.format(coo.y) + ")\" x=\"0\" y=\"0\"/>\n");
		} else {
			out.write("x=\"" + nFmt.format(coo.x) + "\"" + " y=\"" + nFmt.format(coo.y) + "\"/>\n");
		}
		++ nPoints;
		return ( checkSize() );
	}
	
	
	// Symbol(defs)
	
	
	public boolean putSymbol( String symbolTemplate ) throws Exception{
		// 返り値は、テンプレートの正当性（他の返り値と違うので注意！）
		if ( symbolTemplate.indexOf("p0")>0){
			out.write("<defs>\n");
			out.write( symbolTemplate );
			out.write("</defs>\n");
			return ( true );
		} else {
			System.out.println("ERROR: Hmmmm.. this template don't have id=p0 symbol.");
			return ( false );
		}
	}
	
	public boolean putSymbol(double size ) throws Exception{
		putSymbol( size , 0 );
		return ( true ); // symbol定義は要素数カウントに入れないことにした。
	}
	
	public boolean putSymbol(double size , double fixedStrokeWidth) throws Exception{
		out.write("<defs>\n");
		out.write(" <g id=\"p0\" >\n"); 
		if ( fixedStrokeWidth > 0 ){
			out.write("  <circle cx=\"0.0\" cy=\"0.0\" r=\"" + nFmt.format(size) + "\" stroke=\"green\" stroke-width=\"" +  fixedStrokeWidth + "\" vector-effect=\"non-scaling-stroke\" />\n");
		} else {
			out.write("  <circle cx=\"0.0\" cy=\"0.0\" r=\"" + nFmt.format(size) + "\" stroke=\"none\" />\n");
		}
		out.write(" </g>\n");
		out.write("</defs>\n");
//		++ nPoints;
//		return ( checkSize() );
		return ( true ); // symbol定義は要素数カウントに入れないことにした。
	}
	
	
	// for directPOI (not use use element)
	public boolean putPoiShape( Coordinate coo , int type , double poiSize , String fillColor , double strokeWidth , String strokeColor , boolean nonScalingStroke , boolean nonScalingObj ) throws Exception{
//		if (poiSize <= 0 && defaultPoiSize > 0){ // まだ使っていない
//			poiSize = defaultPoiSize;
//		}
		
		
		putGroupAnchor();
		out.write("<path ");
		putIdAndMicroMeta();
		out.write( fillProp( fillColor ) );
		out.write( strokeWidthProp( strokeWidth ) );
		out.write( strokeProp( strokeColor ) );
		out.write( VEProp(nonScalingStroke ) );
		if ( poiSize >0 ){ // 2017/7/14
			// poiSizeが明示されているときは、個々のPOIに別々のサイズを指定する場合
			out.write( vps.getSizedPoiShapeAttrs( coo.x , coo.y , type , poiSize , "" , "" , 0 , nonScalingObj ) );
		} else {
			// 最初にvectorpoishape初期化時に設定したサイズで描画する場合
			out.write( vps.getPoiShapeAttrs( coo.x , coo.y , type , "" , "" , 0 , nonScalingObj ) );
		}
		out.write("/>\n");
		++ nPoints;
		return ( checkSize() );
	}
	
	
	
	// Image , Animation 要素
	public boolean putImage( Coordinate coo , double width , double height , String href ) throws Exception{
		// 下方互換用
		boolean ans = putImage( coo , width , height , href , -1 , -1 );
		return ( ans );
	}
	
	// .svg拡張子の場合　Image -> animationに変更--2010.10.21 
	// visibleMin,MaxZoomを設定可能に -- 2012.4.10
	public boolean putImage( Coordinate coo , double width , double height , String href , double visibleMinZoom , double visibleMaxZoom ) throws Exception{
		putGroupAnchor();
		// インポートするデータがsvgの場合 (本来はmime-typeを見るべきだけど・・)
//		System.out.println("href:" + href + " len:" + href.length() + " cut:" + (href.substring(href.length() - 4 , href.length() )));
		if ( (href.substring(href.length() - 4 , href.length() )).equals(".svg")){
			if ( isSvgTL ){
				
				out.write("<animation xlink:href=\"" + href + "\"");
			} else {
				out.write("<image xlink:href=\"" + href + "\"");
			}
		// ビットイメージの場合
		} else {
			out.write("<image xlink:href=\"" + href + "\" preserveAspectRatio=\"none\"");
		}
		out.write(" x=\"" + nFmt.format(coo.x) + "\"" + " y=\"" + nFmt.format(coo.y) + "\"");
		out.write(" width=\"" + nFmt.format(width) + "\"" + " height=\"" + nFmt.format(height) + "\"" );
		if ( visibleMinZoom > 0 ){
			out.write(" visibleMinZoom=\"" + visibleMinZoom +"\"");
		}
		if ( visibleMaxZoom > 0 ){
			out.write(" visibleMaxZoom=\"" + visibleMaxZoom +"\"");
		}
		out.write("/>\n");
		++ nPoints;
		return ( checkSize() );
	}
	
	public void putPlaneString( String str )  throws Exception{
		out.write(str);
	}
	
	public void setMicroMetaHeader( String mns , String murl ) throws Exception{
		hasMicroMeta = true;
		metaNs = mns;
		metaUrl = murl;
	}
	
	public void setMicroMeta2Header( String metaSchema ) throws Exception{
		microMeta2Schema = metaSchema;
		metaNs = "";
		metaUrl = "";
	}
	
	
	public void setMicroMeta( String mtStr )  throws Exception{
		// 注意：この関数を使うときは、attName="attValue" を正確に記述するだけでなく、前後に空白を入れたり、漢字コードを気にしたり、ネームスペースが破綻していないことなども全て利用者側でケアして使うこと・・
		microMetaProp = mtStr;
	}
	
	public void setMicroMeta( String attrName , String attrVal ) throws Exception{
		// 一方、この関数は属性名(nsはあらかじめ点けたものが自動で付く)と属性値を指定するだけで良く、少しインスタント。図形を出力する前ならば、何度でも呼ぶことができる。その度に、メタデータが追加される。ただし、属性名の重複判定はしてないので、その点は注意すること！　また、XML上許されない文字のエスケープも行なってないので、それも注意です。
		microMetaProp = microMetaProp + metaNs + ":" + attrName + "=\"" + attrVal + "\" ";
	}
	
	public void setCustomAttribute( String cAttr ) throws Exception{
		// 注意：この関数を使うときは、attName="attValue" を正確に記述するだけでなく、前後に空白を入れたり、漢字コードを気にしたり、ネームスペースが破綻していないことなども全て利用者側でケアして使うこと・・ 図形を出力する前ならば、何度でも呼ぶことができる。（Appendされる）
		if ( appendableCustomAttr ){
			customAttributes = customAttributes + cAttr;
		} else {
			customAttributes = cAttr;
		}
	}
	
	private boolean checkSize() throws Exception{
		// リミッターを超えたら、falseを返す
		++ nElements;
		// 遅延ファイル出力を試す(2014.02)
		// forceOosのときはファイル生成自体行わないように改良 2016.5.11 (delete()に影響ないか？)
		if ( nElements == 1 && !forceOos ){ // 最初の実効的書き込み
			if ( nullFileDebug ){
				NullWriter nullSt = new NullWriter();
				out = nullSt;
			} else {
//				System.out.println("At last file create!"+svgFile);
				FileOutputStream osFile = new FileOutputStream( svgFile );
				Writer out2 = new BufferedWriter(new OutputStreamWriter( osFile , "UTF-8" ));
				((CharArrayWriter)out).writeTo( out2 );
				// もともとoutはcharArrayWriterだったものをここで、BufferedWriterに
				out = out2;
				out.flush(); // いったん書き出し
			}
		}
//		System.out.println( nElements );
		boolean ret;
		if ( nullFileDebug ){
			length = ((NullWriter)out).length();
		} else {
			length = svgFile.length();
		}
		if ( forceOos && nElements > 0 ){
			ret = false;
		} else if ( length < limit ){
			ret = true;
		} else {
			ret = false;
//			System.out.println("Exceeded fileLimit!");
//			throw new IndexOutOfBoundsException();
		}
		
		return ( ret );
	}
}


class NullWriter extends Writer {
	int length = 0;
	public int length(){
		return (length);
	}
	public void write(String b) throws IOException {
		length += b.length();
	}
	public void write(int b) throws IOException {
	}
	public void write(char[] a, int b, int c) throws IOException {
	}
	public void close() throws IOException {
	}
	public void flush() throws IOException {
	}

}

