package org.svgmap.shape2svgmap;

import java.io.*;
import java.util.*;
import java.awt.geom.*;

// 国土地理院のtky2jgdのパラメータファイルを用いて、
// 旧測地系(東京ベッセル)からGRS80(JGD2000)緯度経度座標系に変換する関数
// tky2jgdGenが必要
// Copright 2009 by Satoru Takagi @ KDDI All RIghts Reserved
// ToDo: データベース圏外の場合、各メッシュ頂点に相当するズレ量を回転楕円体によ
// る変換から求め、動的にハッシュを追加する機能（これなら結構簡単だと思う）
// ハッシュからの検索結果を一個だけキャッシュする機能(ポリラインなどの連続点の変換に有効)

public class tky2jgd {
	public static void main(String[] args) {
		tky2jgd t2j = new tky2jgd();
		
		double lon = 140.0;
		double lat = 36.0;
		if ( args.length > 1 ){
			lat = Double.parseDouble(args[0]);
			lon = Double.parseDouble(args[1]);
		}
		Point2D.Double p = t2j.transform(lat,lon);
		
		
		System.out.println( "In:" + lon + "," +  lat + " Out:" + p );
		
	}
	
	HashMap<Integer , Point2D.Float> tky2jgdTable;
	
	Point2D.Float getOutMesh( double lat0 , double lng0 ){
		Point2D.Float ans = new Point2D.Float();
//			System.out.println("NULL");
			normalConverter.setLatLon( lat0 , lng0 , GeoConverter.TOKYO_BESSEL );
			ans.x = (float)(3600.0 * ( ( normalConverter.WGPos).longitude - lng0 ));
			ans.y = (float)(3600.0 * ( ( normalConverter.WGPos).latitude - lat0 ));
		return ( ans );
	}
	
	
	int prevMeshcode = -999; // 直前の変換と同じメッシュ上の変換の場合にはデータベース検索を省略するため
	Point2D.Float crd0 , crdU , crdR , crdUR; // crd*には、xに経度、yに緯度が入っている
	
	Point2D.Double transform( double lat , double lon){
		double lat0, lng0;
		JpMesh mesh = new JpMesh( lat, lon );
		Integer meshcode = Integer.valueOf(mesh.toIntMesh3Code());
//		System.out.println("mesh:" + meshcode);
		// 四隅の３次メッシュのズレ量を得る
		// データベースに存在しない場合は、ノーマルな変換関数で該当する部分のズレ量を計算する
		if ( prevMeshcode != meshcode ){
			crd0 = tky2jgdTable.get(meshcode);
			if ( crd0 == null ){
				crd0 = getOutMesh( mesh.getLatitude() , mesh.getLongitude() );
			}
			crdU = tky2jgdTable.get(Integer.valueOf(mesh.getUpMesh3()));
			if ( crdU == null ){
				crdU = getOutMesh( mesh.getLatitude() + mesh.m3lat , mesh.getLongitude() );
			}
			crdR = tky2jgdTable.get(Integer.valueOf(mesh.getRightMesh3()));
			if ( crdR == null ){
				crdR = getOutMesh( mesh.getLatitude() , mesh.getLongitude() + mesh.m3long );
			}
			crdUR = tky2jgdTable.get(Integer.valueOf(mesh.getUpRightMesh3()));
			if ( crdUR == null ){
				crdUR = getOutMesh( mesh.getLatitude() + mesh.m3lat , mesh.getLongitude() + mesh.m3long );
			}
		}
//		System.out.println(crd0);
		
		
		//バイリニア補間
		double a,b,x,y;
//		System.out.println("mesh3:" +  mesh.getLatitude() + "," + mesh.getLongitude() );
		a = (lon - mesh.getLongitude()) / (45.0 / 3600.0); // 経度方向の変位
		b = (lat - mesh.getLatitude()) / (30.0 / 3600.0); // 緯度方向の変位
//		System.out.println( "a(lon)" + a + " b(lat):" + b);
		
		//補間式を展開したもの
		x = crd0.x + ( crdU.x - crd0.x ) * b + ( crdR.x - crd0.x ) * a + ( crdUR.x - crdR.x - crdU.x + crd0.x ) * b * a;
		y = crd0.y + ( crdU.y - crd0.y ) * b + ( crdR.y - crd0.y ) * a + ( crdUR.y - crdR.y - crdU.y + crd0.y ) * b * a;
		
		lon = x / 3600.0 + lon;
		lat = y / 3600.0 + lat;
		return ( new Point2D.Double(lon, lat));
	}
	
	Point2D.Double transform( Point2D.Double crd ){
		return ( this.transform( crd.y , crd.x ) );
	}
	
	GeoConverter normalConverter;
	boolean readFromObject = false;
	
	@SuppressWarnings("unchecked")
	tky2jgd(){
		if ( readFromObject ){
			try{
				System.out.println("Reading database");
				//基準点データのハッシュテーブルをを読み込み
				FileInputStream inFile = new FileInputStream("TKY2JGD.jso");
				ObjectInputStream inObject = new ObjectInputStream( inFile );
				tky2jgdTable = (HashMap<Integer , Point2D.Float>)inObject.readObject();
				inObject.close();
				System.out.println("Init End");
				// tky2jgdTableのpoint2dFloatは、緯度差：x , 経度差:y (逆)なので注意！
				
			} catch (Exception e) {
				// Fileオブジェクト生成時の例外捕捉
				e.printStackTrace();
			}
		} else {
			readAndBuild();
		}
		normalConverter = new GeoConverter();
	}
	
	public void readAndBuild(){
		boolean startData = false;
		Integer meshCode;
		float x,y;
		int counter = 0;
//		Point2D.Float meshCrd;
		System.out.println("Reading database");
		
		try {
			File csv = new File("TKY2JGD.par"); // CSVデータファイル
			
			tky2jgdTable = new HashMap<Integer , Point2D.Float>();
			
			BufferedReader br = new BufferedReader(new FileReader(csv));
			// 最終行まで読み込む
			while (br.ready()) {
				String line = br.readLine();
				// 1行をデータの要素に分割
				StringTokenizer st = new StringTokenizer(line, " ");
				
				if ( startData != true ){ // "MeshCode"行があるまで読み飛ばし
					
					if ((st.nextToken().indexOf("MeshCode")) > -1 ){
						startData = true;
					}
				} else {
					meshCode = Integer.decode(st.nextToken());
					y = Float.parseFloat(st.nextToken()); // 緯度変位
					x = Float.parseFloat(st.nextToken()); // 経度変位
					tky2jgdTable.put(meshCode,new Point2D.Float(x,y));
				}
				++ counter;
				if ( counter % 10000 == 0 ){
					System.out.print(".");
				}
			}
			br.close();
			System.out.println("");
			System.out.println("END");
		} catch (FileNotFoundException e) {
			// Fileオブジェクト生成時の例外捕捉
			e.printStackTrace();
		} catch (IOException e) {
			// BufferedReaderオブジェクトのクローズ時の例外捕捉
			e.printStackTrace();
		}
	}
	
}