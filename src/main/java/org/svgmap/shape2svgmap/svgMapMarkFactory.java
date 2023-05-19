package org.svgmap.shape2svgmap;

// svgMapMarkFactory programmed by Satoru Takagi
// メッシュ(物理空間上でのサイズを持ったポイントの概念としての)を実現ずるための特殊なマーカーを定義・追加する
// SPYなのでMETA-INF/services/org.geotools.renderer.style.MarkFactoryの登録も必要

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
//import java.awt.geom.Rectangle2D.Double;
import java.util.logging.Logger;

import org.geotools.renderer.style.shape.ExplicitBoundsShape;
import org.opengis.feature.Feature;
import org.opengis.filter.expression.Expression;
import org.geotools.renderer.style.MarkFactory;

public class svgMapMarkFactory implements MarkFactory {
	private static Shape square;
	static {
		square = new Rectangle2D.Double(-.5, -.5, 1., 1.); // x,y,w,h
	}
	
	public Shape getShape(Graphics2D graphics, Expression symbolUrl, Feature feature) throws Exception {
		String wellKnownName = symbolUrl.evaluate(feature, String.class);
		wellKnownName = wellKnownName.toLowerCase();
		if (wellKnownName.startsWith("meshrect")) {
			if (  wellKnownName.indexOf(":") >0){
				double aspect = Double.parseDouble(wellKnownName.substring(wellKnownName.indexOf(":")+1));
				double x,y,width,height;
				// ここで設定しても、sizeがheight方向でオートサイジングをかけているらしい
				if ( aspect < 0 ){
					return square;
				} else if ( aspect > 1. ){ // aspect= height/width
					height = 1.0;
					width = height / aspect;
				} else {
					width = 1.0;
					height = width * aspect;
				}
				x = - width  / 2.0;
				y = - height / 2.0;
//				System.out.println("svgMapMarkFactory meshRect:"+x+","+y+","+width+","+height);
				return ( new Rectangle2D.Double(x, y, width, height));
			} else {
				return square;
			}
		}
		return null;
	}
}