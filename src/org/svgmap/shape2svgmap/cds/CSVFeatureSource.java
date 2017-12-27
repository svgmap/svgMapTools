// This code is modified code of CSVFeatureSource.java introduced in geotools's tutorial

package org.svgmap.shape2svgmap.cds;

import java.io.IOException;

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

    protected FeatureReader<SimpleFeatureType, SimpleFeature> getReaderInternal(Query query)
            throws IOException {
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
            builder.add("the_geom", Point.class );
            
       		int k = 0;
            for( String column : reader.getHeaders() ){
            	
            	System.out.println("buildFeatureType: column: " + column);
                if( "lat".equalsIgnoreCase(column) || "latitude".equalsIgnoreCase(column) || "lati".equalsIgnoreCase(column) || "ˆÜ“x".equalsIgnoreCase(column) ){
                	++k;
                    continue; // skip as it is part of Location
                }
                if( "lon".equalsIgnoreCase(column) || "longitude".equalsIgnoreCase(column) || "lng".equalsIgnoreCase(column) || "long".equalsIgnoreCase(column) || "Œo“x".equalsIgnoreCase(column) ){
                	++k;
                    continue; // skip as it is part of Location
                }
            	if ( column.toLowerCase().endsWith(":int") || (dataType !=null && dataType.length > k && dataType[k].equals("int") )){
//            		column = column.substring(0, column.toLowerCase().indexOf(":int"));
//	            	String colSjis = new String(column.getBytes("Shift_JIS"), 0);
            		String colSjis = sjisExt.getSjisStr( column );
	                builder.add(colSjis, Integer.class);
            	} else if ( column.toLowerCase().endsWith(":double")  || (dataType !=null && dataType.length > k && dataType[k].equals("double") )){
//            		column = column.substring(0, column.toLowerCase().indexOf(":double"));
//	            	String colSjis = new String(column.getBytes("Shift_JIS"), 0);
            		String colSjis = sjisExt.getSjisStr( column );
	                builder.add(colSjis, Double.class);
            	} else if ( column.toLowerCase().endsWith(":string")  || (dataType !=null && dataType.length > k && dataType[k].equals("string")  )){
//            		column = column.substring(0, column.toLowerCase().indexOf(":string"));
//	            	String colSjis = new String(column.getBytes("Shift_JIS"), 0);
            		String colSjis = sjisExt.getSjisStr( column );
	                builder.add(colSjis, String.class);
            	} else {
//	            	String colSjis = new String(column.getBytes("Shift_JIS"), 0);
            		String colSjis = sjisExt.getSjisStr( column );
	                builder.add(colSjis, String.class);
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
