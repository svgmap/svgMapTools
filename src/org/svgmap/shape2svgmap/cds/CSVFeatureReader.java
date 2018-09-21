// This code is modified code of CSVFeatureReader.java introduced in geotools's tutorial

package org.svgmap.shape2svgmap.cds;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.ArrayList;

import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentState;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import com.csvreader.CsvReader;
//import com.opencsv.CsvReader;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.LinearRing;

public class CSVFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {
	
	private ContentState state;
	private Query query;
	private CsvReader reader;
	private SimpleFeature next;
	private SimpleFeatureBuilder builder;
	private int row;
	private GeometryFactory geometryFactory;
	private boolean sjisInternalCharset;
	private String[] headers;
	private int geometryType;
	private int latitudeColumn;
	private int longitudeColumn;
	
	private int skipLines = 0;
	
	public CSVFeatureReader(ContentState contentState, Query query) throws IOException {
		this.state = contentState;
		this.query = query;
		CSVDataStore csv = (CSVDataStore) contentState.getEntry().getDataStore();
		sjisInternalCharset = csv.sjisInternalCharset;
		headers = csv.headers;
		geometryType = csv.geometryType;
		latitudeColumn = csv.latitudeColumn;
		longitudeColumn = csv.longitudeColumn;
		skipLines = csv.skipLines;
		reader = csv.read(); // this may throw an IOException if it could not connect
		boolean header = reader.readHeaders();
		if (! header ){
			throw new IOException("Unable to read csv header");
		}
		
		for ( int i = 0 ; i < skipLines ; i++ ){
			boolean read = reader.readRecord(); // read and skip the "next" record
		}
		
		builder = new SimpleFeatureBuilder( state.getFeatureType() );
		geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
		row = 0;
	}
	
	public SimpleFeatureType getFeatureType() {
		return (SimpleFeatureType) state.getFeatureType();
	}
	
	public SimpleFeature next() throws IOException, IllegalArgumentException,
	NoSuchElementException {
		SimpleFeature feature;
		if( next != null ){
			feature = next;
			next = null;
		}
		else {
			feature = readFeature();
		}
		return feature;
	}
	
	SimpleFeature readFeature() throws IOException {
		SimpleFeature ans=null;
		boolean failed = true;
		while ( failed ){
			try{
				ans = readFeature_int();
				failed = false;
			} catch (Exception e ){
				failed = true;
			}
		}
		return ( ans );
	}
	
	SimpleFeature readFeature_int() throws IOException {
		if( reader == null ){
			throw new IOException("FeatureReader is closed; no additional features can be read");
		}
		boolean read = reader.readRecord(); // read the "next" record
		if( read == false ){
			close(); // automatic close to be nice
			return null; // no additional features are available
		}
		Coordinate coordinate = new Coordinate();
		double crdx =0, crdy =0;
		ArrayList<Coordinate> crds = null;
		if ( geometryType != CSVDataStore.Point ){
			crds = new ArrayList<Coordinate>();
		}
//    	String vals="";
		for ( int  i = 0 ; i < reader.getColumnCount() ; i++ ){
			String column;
			if ( i < headers.length ){
				column = headers[i];
			} else {
				column = null;
			}
//		for( String column : reader.getHeaders() ){}
//			String value = reader.get(column);
			String value = reader.get(i);
//        	System.out.println("column:"+column + " val:"+value);
			if( i == latitudeColumn && geometryType == CSVDataStore.Point ){
				coordinate.y = Double.valueOf( value.trim() );
			} else if( i == longitudeColumn && geometryType == CSVDataStore.Point ){
				coordinate.x = Double.valueOf( value.trim() );
			} else if ( ( i >= latitudeColumn || i >= longitudeColumn ) && geometryType != CSVDataStore.Point ){
				if ( ( i - latitudeColumn ) % 2 == 0 ){
					crdy = Double.valueOf( value.trim() );
					if ( latitudeColumn > longitudeColumn ) {
//						System.out.println("x,y:"+crdx+","+crdy);
						crds.add( new Coordinate( crdx, crdy ) );
					}
				} else {
					crdx = Double.valueOf( value.trim() );
					if ( latitudeColumn < longitudeColumn ) {
//						System.out.println("x,y:"+crdx+","+crdy);
						crds.add( new Coordinate( crdx, crdy ) );
					}
				}
			} else {
//            	vals += ","+ value;
				if (  sjisInternalCharset ){ // shapefile readerもcharsetが指定できることが分かったので、このフラグでどちらにも対応できるようにした 2018/8/10
					// Shapefileの悲しい実装とアラインさせるためにあえて文字化けさせる・・・
					// deprecate関数を削除 2017.11.2
					String valSjis = sjisExt.getSjisStr(value);
					String colSjis = sjisExt.getSjisStr(column);
					builder.set(colSjis, valSjis );
				} else {
					builder.set(column, value );
				}
			}
		}
//        builder.set("Location", geometryFactory.createPoint( coordinate ) ); 
		if ( geometryType == CSVDataStore.Point ){
			builder.set("the_geom", geometryFactory.createPoint( coordinate ) ); 
		} else if ( geometryType == CSVDataStore.LineString ){
			builder.set("the_geom", geometryFactory.createLineString( crds.toArray(new Coordinate[crds.size()]) ) ); 
		} else if ( geometryType == CSVDataStore.Polygon ){
			if ( crds.get(0).x != crds.get(crds.size()-1).x || crds.get(0).y != crds.get(crds.size()-1).y ){
				Coordinate termCoord = new Coordinate( crds.get(0).x , crds.get(0).y );
				crds.add(termCoord);
			}
			LinearRing shell = geometryFactory.createLinearRing( crds.toArray(new Coordinate[crds.size()]) );
			LinearRing[] holes = null;
			builder.set("the_geom", geometryFactory.createPolygon( shell, holes ) ); 
		}
//    	 --> the_geom ( for compatibility with shapefile source )
//    	System.out.println("readFeature:"+coordinate + " : "+ vals);
		row += 1;
		return builder.buildFeature( state.getEntry().getTypeName()+"."+row );
	}
	
	public boolean hasNext() throws IOException {
		if( next != null ){
			return true;
		}
		else {
			next = readFeature(); // read next feature so we can check
			return next != null;
		}
	}
	
	public void close() throws IOException {
		if( reader == null ){
		} else {
			reader.close();
			reader = null;
		}
		builder = null;
		geometryFactory = null;
		next = null;
	}
	
}
