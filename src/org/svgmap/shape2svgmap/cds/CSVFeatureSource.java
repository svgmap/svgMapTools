// This code is modified code of CSVFeatureSource.java introduced in geotools's tutorial

package org.svgmap.shape2svgmap.cds;

import java.io.IOException;
import java.util.HashSet;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.csvreader.CsvReader;
//import com.opencsv.CsvReader;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.Geometry;

@SuppressWarnings("unchecked")
public class CSVFeatureSource extends ContentFeatureSource {
	
	public CSVFeatureSource(ContentEntry entry, Query query) {
		super(entry,query);
	}
	/**
	* Access parent CSVDataStore
	*/
	public CSVDataStore getDataStore(){
		return (CSVDataStore) super.getDataStore();
	}
	
	/**
	* Implementation that generates the total bounds
	* (many file formats record this information in the header)
	*/
	protected ReferencedEnvelope getBoundsInternal(Query query) throws IOException {
		System.out.println("called getBoundsInternal:");
		ReferencedEnvelope bounds = new ReferencedEnvelope( getSchema().getCoordinateReferenceSystem() );
		
		FeatureReader<SimpleFeatureType, SimpleFeature> featureReader = getReaderInternal(query);
		try {
			while( featureReader.hasNext() ){
				SimpleFeature feature = featureReader.next();
//				System.out.println(feature.getBounds());
				bounds.include( feature.getBounds() );
			}
		}
		finally {
			featureReader.close();
		}
		return bounds;
	}
	
	protected int getCountInternal(Query query) throws IOException {
		CsvReader reader = getDataStore().read();
		try {
			boolean connect = reader.readHeaders();
			if( connect == false ){
				throw new IOException("Unable to connect");
			}
			int count = 0;
			while( reader.readRecord() ){
				count += 1;
			}
			return count;
		}
		finally {
			reader.close();
		}
	}
	
	protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query)            throws IOException {
		return new CSVFeatureReader( getState(), query );
	}
	
	protected SimpleFeatureType buildFeatureType() throws IOException {
		System.out.println("called buildFeatureType:");
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName( entry.getName() );
		
		// read headers
		CsvReader reader = getDataStore().read();
		System.out.println("buildFeatureType: reader: " + reader );
		String[] dataType = getDataStore().dataType;
		boolean sjisInternalCharset =  getDataStore().sjisInternalCharset;
		try {
			System.out.println("buildFeatureType: readHeaders call: ");
			boolean success = reader.readHeaders();
			System.out.println("buildFeatureType: readHeaders success0: " + success);
			if( success == false ){
				throw new IOException("Header of CSV file not available");
			}
			System.out.println("buildFeatureType: readHeaders success1: " + success);
			
			// we are going to hard code a point location
			// columns like lat and lon will be gathered into a
			// Point called Location --> the_geom ( for compatibility with shapefile source )
			builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference system
//            builder.add("Location", Point.class );
			boolean grometryTypeResolved = false;
			// まずPointかLineStringかSimplePolygonかを同定する
			String[] headers = reader.getHeaders();
			getDataStore().headers = headers;
			for( String column : headers ){
				if( column.toLowerCase().startsWith("lat") || column.toLowerCase().startsWith("latitude") || column.toLowerCase().startsWith("lati") || column.toLowerCase().startsWith("緯度") ){ // この弁別ロジック・・ちょっと半端になってしまった・・正規表現導入すべき2020/5/15
					if ( column.toLowerCase().endsWith(":line") ){
						builder.add("the_geom", LineString.class );
						System.out.println("buildFeatureType: LineString Geom File");
						getDataStore().geometryType = CSVDataStore.LineString;
					} else if( column.toLowerCase().endsWith(":polygon") ){
						builder.add("the_geom", Polygon.class );
						System.out.println("buildFeatureType: Polygon Geom File");
						getDataStore().geometryType = CSVDataStore.Polygon;
					} else {
						builder.add("the_geom", Point.class );
						System.out.println("buildFeatureType: Point Geom File");
						getDataStore().geometryType = CSVDataStore.Point;
					}
					grometryTypeResolved = true;
				} else if ( column.toLowerCase().equals("wkt")){
					grometryTypeResolved = true;
					builder.add("the_geom", Geometry.class );
					getDataStore().geometryType = CSVDataStore.Wkt;
				}
			}
			if ( ! grometryTypeResolved ){
				System.out.println("ERROR! No geometry column on input csv file. Exit.");
				System.exit(0);
			}
			
			int k = 0;
			int dblCnt = 0;
			HashSet<String> dblCheck = new HashSet<String>();
			// 次に、latitudeColumn,longitudeColumnを同定する
			for( String column : headers ){
				
				if ( dblCheck.contains(column) ){ // Patch 2018.2.13 S.Takagi Check and Fix Duplicated Feature Type Name
					++ dblCnt;
					System.out.println("WARNING:  Duplicate Column Name: "+column +".  Rename to :"+ column + dblCnt);
					column = column + dblCnt;
				} else {
					dblCheck.add(column);
				}
				
				System.out.print("buildFeatureType: column: " + column+" :");
				if( column.toLowerCase().startsWith("lat") || column.toLowerCase().startsWith("latitude") || column.toLowerCase().startsWith("lati") || column.toLowerCase().startsWith("緯度") ){ // この弁別ロジック・・ちょっと半端になってしまった・・正規表現導入すべき2020/5/15
					getDataStore().latitudeColumn = k;
					if ( column.toLowerCase().indexOf("dms") > 0 ){
						getDataStore().latitudeFormat = CSVDataStore.DMS;
						System.out.println("latitude:DMS");
					} else if ( column.toLowerCase().indexOf("dm") > 0 ){
						getDataStore().latitudeFormat = CSVDataStore.DM;
						System.out.println("latitude:DM");
					} else {
						getDataStore().latitudeFormat = CSVDataStore.DEG;
						System.out.println("latitude");
					}
					++k;
					continue; // skip as it is part of Location
				}
				if( column.toLowerCase().startsWith("lon") || column.toLowerCase().startsWith("longitude") || column.toLowerCase().startsWith("lng") || column.toLowerCase().startsWith("経度") ){ // この弁別ロジック・・ちょっと半端になってしまった・・正規表現導入すべき2020/5/15
					getDataStore().longitudeColumn = k;
					if ( column.toLowerCase().indexOf("dms") > 0 ){
						getDataStore().longitudeFormat = CSVDataStore.DMS;
						System.out.println("longitude:DMS");
					} else if ( column.toLowerCase().indexOf("dm") > 0 ){
						getDataStore().longitudeFormat = CSVDataStore.DM;
						System.out.println("longitude:DM");
					} else {
						getDataStore().longitudeFormat = CSVDataStore.DEG;
						System.out.println("longitude");
					}
					++k;
					continue; // skip as it is part of Location
				}
				if(column.toLowerCase().equals("wkt")){
					getDataStore().latitudeColumn = k;
					getDataStore().longitudeColumn = k;
					System.out.println("WKT geometry");
					++k;
					continue; // skip as it is part of Location
				}
				if ( column.toLowerCase().endsWith(":int") || (dataType !=null && dataType.length > k && dataType[k].equals("int") )){
					System.out.println("integer");
					if ( sjisInternalCharset){
						column = sjisExt.getSjisStr( column );
					}
					builder.add(column, Integer.class);
				} else if ( column.toLowerCase().endsWith(":double")  || (dataType !=null && dataType.length > k && dataType[k].equals("double") )){
					System.out.println("double");
					if ( sjisInternalCharset){
						column = sjisExt.getSjisStr( column );
					}
					builder.add(column, Double.class);
				} else if ( column.toLowerCase().endsWith(":string")  || (dataType !=null && dataType.length > k && dataType[k].equals("string")  )){
					System.out.println("string");
					if ( sjisInternalCharset){
						column = sjisExt.getSjisStr( column );
					}
					builder.add(column, String.class);
				} else {
					System.out.println("etcString");
					if ( sjisInternalCharset){
						column= sjisExt.getSjisStr( column );
					}
					builder.add(column, String.class);
				}
//                builder.add(column, String.class);
				++k;
			}
			
			// build the type (it is immutable and cannot be modified)
			final SimpleFeatureType SCHEMA = builder.buildFeatureType();
			System.out.println("buildFeatureType: SCHEMA: " + SCHEMA);
			
			return SCHEMA;
		}
		finally {
			reader.close();
		}
	}
	
	
}
