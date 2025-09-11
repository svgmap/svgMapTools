package org.svgmap.shape2svgmap;

// License: (MPL v2)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

import java.io.*;
import java.io.IOException;

public class SVGMapSymbolTemplate {
	// 単にテキストファイル読み込んで中身をsymbolFileに入れるだけ
	// 2017.9.15 マイナー更新　気にすることはたぶん何もない
	public void readSymbolFile( String file )throws Exception{
		BufferedReader sreader = null;
		try{
	//		BufferedReader sreader = new BufferedReader(new FileReader(new File(file)));
			sreader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(file)),"UTF-8"));
			
			String line;
			while((line=sreader.readLine())!=null){
				symbolFile += line + "\n";
			}
			sreader.close();
		} catch ( Exception e ){
			e.printStackTrace();
			sreader.close();
		}
	}
	
	public String symbolFile ="";

}