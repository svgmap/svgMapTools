package org.svgmap.shape2svgmap;
import java.lang.*;
public class MainWrapper  {
	public static void main(String[] args) throws Exception{
		if (args.length ==0){
			showHelp();
			return;
		}
		String[] m_args = new String[args.length-1];
		for (int i = 1 ; i < args.length ; i++){
			m_args[i-1]=args[i];
		}
		String tgt = args[0].toLowerCase();
		switch (tgt){
		case "shape2wgs84":
			org.svgmap.shape2svgmap.Shape2WGS84.main(m_args);
			break;
		case "shape2svgmap":
			org.svgmap.shape2svgmap.Shape2SVGMap19.main(m_args);
			break;
		case "shape2imagesvgmap":
			org.svgmap.shape2svgmap.Shape2ImageSVGMap4.main(m_args);
			break;
		case "hyperbuilder":
			org.svgmap.shape2svgmap.HyperBuilderAppl.main(m_args);
			break;
		default:
			System.out.println(tgt + " 機能はありません");
			showHelp();
		}
	}
	
	public static void showHelp(){
		System.out.println("第一引数に Shape2WGS84 | Shape2SVGMap | Shape2ImageSVGMap | HyperBuilder のいずれかのコマンド名が必要です");
		System.out.println("その後に、それぞれのコマンドのための引数を連ねます");
	}
}
