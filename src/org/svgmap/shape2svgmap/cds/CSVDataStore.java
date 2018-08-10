// This code is modified code of CSVDataStore.java introduced in geotools's tutorial
// header start
// CSV形式：
//  １行目がカンマ区切りの属性名(lat,lonカラム必須)　もしくはいきなりデータ開始（その場合Schemaファイル必須）
// Schema形式：
//  １行目がカンマ区切りの属性名(lat,lonカラム必須)
//  ２行目(Option):カンマ区切りのデータタイプ(int,string,double)
//  ３行目カンマ区切り、[拡張コマンド]=[コマンドパラメータ]　。
//   今のところskip=xxx 頭のxxx行スキップする
//
// 2018/8/10 shapefileFeatureStoreの文字化け問題に呼応して同様の文字化け処理をあえて起こさせていた実装を、文字化けしない正しい処理もできるように改修(sjisInternalCharsetをfalseにする)


package org.svgmap.shape2svgmap.cds;

//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.io.Reader;
import java.io.*;
import java.util.Collections;
import java.util.List;

import java.util.zip.GZIPInputStream;

import org.geotools.data.Query;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;

import com.csvreader.CsvReader;
//import com.opencsv.CsvReader;

public class CSVDataStore extends ContentDataStore {
// header end
    
    // constructor start
    File file;
	File schemaFile = null;
	
	String schemaLine="";
	public String[] dataType;
	int skipLines = 0;
    
	boolean gZipped = false;
	String charset = "MS932";
	
	public boolean sjisInternalCharset = true; // 2018/8/10
	
    public CSVDataStore( File file ){
        this.file = file;
    }
	
    public CSVDataStore( File file, boolean gZipped ){
        this.file = file;
    	this.gZipped = gZipped;
    }
	
    public CSVDataStore( File file, boolean gZipped , String charset ){
        this.file = file;
    	this.gZipped = gZipped;
    	this.charset = charset;
    }
	
    public CSVDataStore( File file , File schemaFile){
        this.file = file;
    	this.schemaFile = schemaFile;
    }
	
    public CSVDataStore( File file , File schemaFile, boolean gZipped ){
        this.file = file;
    	this.schemaFile = schemaFile;
    	this.gZipped = gZipped;
    }
	
    public CSVDataStore( File file , File schemaFile, boolean gZipped , String charset ){
        this.file = file;
    	this.schemaFile = schemaFile;
    	this.gZipped = gZipped;
    	this.charset = charset;
    }
    // constructor end
	
	
	private void getSchema()throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(schemaFile));
		int i = 0;
		String line;
		while ((line = br.readLine()) != null) {
			if ( i == 0 ){
				schemaLine = line + "\n";
			} else if ( i == 1 ){
				dataType = line.split(",", 0); 
			} else if ( i == 2 ){
				String[] extCommands = line.split(",", 0);
				for ( int j = 0 ; j < extCommands.length ; j++ ){
					String[] cmd = extCommands[j].split("=");
					if ( cmd[0].indexOf("skip") >= 0 ){
						skipLines = Integer.parseInt(cmd[1]);
//						System.out.println("SkipLine:"+skipLines);
					}
				}
			} 
			++i;
		}
		if ( dataType.length > 0 ){
//			System.out.print("dataType:");
			for ( int j = 0 ; j < dataType.length ; j++ ){
				switch ( dataType[j].toLowerCase() ){
				case "i":
				case "int":
				case "integer":
					dataType[j] = "int";
					break;
				case "d":
				case "double":
				case "real":
					dataType[j] = "double";
					break;
				case "s":
				case "str":
				case "string":
				case "-":
					dataType[j] = "string";
					break;
				default:
					dataType[j] = "string";
					break;
				}
//				System.out.print(dataType[j]+",");
			}
//			System.out.println("");
		}
		
		br.close();
	}
    
    /**
     * Allow read access to file; for our package visibile "friends".
     * Please close the reader when done.
     * @return CsvReader for file
     */
    CsvReader read() throws IOException {
    	CsvReader csvReader = null;
    	
    	InputStream dataStream = null;
    	
    	if ( gZipped ){
    		dataStream = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)));
    	} else {
    		dataStream = new FileInputStream(file);
    	}
    	
    	if ( schemaFile == null ){
//    		BufferedReader reader = new BufferedReader(new InputStreamReader(dataStream,"MS932"));
    		BufferedReader reader = new BufferedReader(new InputStreamReader(dataStream,charset));
    		csvReader = new CsvReader(reader);
    	} else {
    		getSchema(); // schemaLine and each type on global
    		InputStream schemaStream = new ByteArrayInputStream(schemaLine.getBytes());
    		SequenceInputStream sequenceInputStream = new SequenceInputStream (schemaStream,dataStream);
//    		BufferedReader reader = new BufferedReader(new InputStreamReader(sequenceInputStream,"MS932"));
    		BufferedReader reader = new BufferedReader(new InputStreamReader(sequenceInputStream,charset));
    		 csvReader = new CsvReader(reader);
    	}
//        csvReader.close();
        return csvReader;
    }

    // createTypeNames start
    protected List<Name> createTypeNames() throws IOException {
        String name = file.getName();
        name = name.substring(0, name.lastIndexOf('.'));
        
        Name typeName = new NameImpl( name );
        return Collections.singletonList(typeName);
    }
    // createTypeNames end
    

    @Override
    protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
        return new CSVFeatureSource(entry, Query.ALL);
    }

}
