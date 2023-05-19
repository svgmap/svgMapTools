package org.svgmap.shape2svgmap;

// 指定したファイルから、コマンドラインのオプション相当の情報を取得する。
// コマンドラインの長さ制限に引っかかるほど長いオプションをjavaに投入するときのアシスタントクラス
// 空白文字列もしくは改行によって区切られたオプションを読み取り、getOptions()でString[]として返却する
//
// 今後 Shape2svgmap, shape2imagesvgmap 及び周辺ヘルパーアプリをすべてGPL3でオープンソース化する予定です。
// 2016.8.2 Programmed by Satoru Takagi

import java.io.*;
import java.util.*;

public class optionsReader {

	public static void main(String args[]) {
		optionsReader or = new optionsReader( new File("optionsTest.txt") );
		String[] ans = or.getOptions();
		for ( int i = 0 ; i < ans.length ; i++ ){
			System.out.print(ans[i] + " , " );
		}
	}
	
	String[] ans;
	optionsReader(File inputFile){
		ArrayList<String> ansList = new ArrayList<String>();
		try{
			FileReader fr = new FileReader(inputFile);
			BufferedReader br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				String[] lineResult = line.split("[\\s]+");
				for ( int i = 0 ; i < lineResult.length ; i++ ){
					ansList.add(lineResult[i]);
				}
			}
			ans = (String[])(ansList.toArray(new String[0]));
			
		} catch (IOException ex) {
			//例外発生時処理
			ex.printStackTrace();
		}
	}
	
	String[] getOptions(){
		return ( ans );
	}
	

}

