package org.svgmap.shape2svgmap;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.awt.geom.*;

public class ClipPolygonDouble{

	PolygonDouble lastClipped;
	EdgeDouble currentEdge ; //current edge for clipping
	PolygonDouble clipped ;  //p clipped so far
	Point2D.Double lastp ; //previous point in algorithm
	Point2D.Double thisp ; //current point in algorithm
  	boolean thisCE; //Is current point Clipped Edge?
	
	
  ClipPolygonDouble(PolygonDouble p, Rectangle2D.Double r){
    lastClipped = p;
    lastClipped = clipSide(lastClipped,
       new EdgeDouble(r.x,r.y,r.x,r.y+r.height));
//System.out.println( "左" );
//lastClipped.print();
    lastClipped = clipSide(lastClipped,
       new EdgeDouble(r.x,r.y+r.height,r.x+r.width,r.y+r.height));
//System.out.println( "下" );
//lastClipped.print();
    lastClipped = clipSide(lastClipped,
       new EdgeDouble(r.x+r.width,r.y+r.height,r.x+r.width,r.y));
//System.out.println( "右" );
//lastClipped.print();
    lastClipped = clipSide(lastClipped,
       new EdgeDouble(r.x+r.width,r.y,r.x,r.y));
//System.out.println( "上" );
//lastClipped.print();
//    return lastClipped;
  }
	
	public PolygonDouble getClippedPolygon(){
		return lastClipped;
	}

  /* clip p against Edge e and return result */
  PolygonDouble clipSide(PolygonDouble p, EdgeDouble e){
    Point2D.Double intersect;
    if (p.npoints == 0){
      return p; //nothing to do
    }
    currentEdge = e;
    clipped = new PolygonDouble((int)(p.npoints * 1.3));
  	clipped.clipped=p.clipped;
    lastp = new Point2D.Double(p.xpoints[p.npoints-1],p.ypoints[p.npoints-1]);
    for (int i = 0; i < p.npoints; i++){
		thisp = new Point2D.Double(p.xpoints[i],p.ypoints[i]);
    	thisCE = p.clippedEdge[i];
		if (e.inside(thisp) && e.inside(lastp)){ // 前の点も今の点も入っていたら今の点を追加
			clipped.addPoint(thisp.x,thisp.y,thisCE);
		} else if (!e.inside(thisp) && e.inside(lastp)){ // 前の点だけ入っていたら交点を追加
			intersect = e.intersect(thisp,lastp);
			clipped.addPoint(intersect.x,intersect.y, thisCE); // その点のClipEdgeフラグ継承
		} else if (!e.inside(thisp) && !e.inside(lastp)){ // 共に入って無い場合何も追加しない
			/*nothing */
		} else if (e.inside(thisp) && !e.inside(lastp)){ // 今の点だけ入っていたら・・
			intersect = e.intersect(lastp,thisp); // 交点を追加して、今の点を更に追加
			clipped.addPoint(intersect.x,intersect.y, true); // しかもそれはクリップされたエッジの終点
			clipped.addPoint(thisp.x,thisp.y,thisCE);
			clipped.clipped=true;
      	}
    	lastp = thisp;
    }
  	
  	
    currentEdge = null;  //so that paint won't draw currentEdge now we've 
                        //left the loop
    return clipped;
  }
}