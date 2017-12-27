About SVG SVG Map Tools

This tool generates quad tree vector and raster mixed tile SVG map content from shapefile or csv file.
You should be able to convert millions of fairly large data into map content.
There are built by Java and requires an environment that runs Java (Oracle version).

For details, please refer to http://svgmap.org/


There are four modules in svgmaptools. Shape2SVGMap and Shape2ImageSVGMap are the essential modules.

Shape2SVGMap: Convert csv or shapefile to vector data type svgMap contents.

Shape2ImageSVGMap: Convert csv or shapefile to tiled raster data type svgMap contents.

Shape2WGS84:  Preprocessor

HyperBuilder: Build Container.svg included svg layers

You should read tutorials.

Note: Currently, much of the tutorials and help files are in Japanese. Please do somehow with google translation etc :-)

Windows:

java -classpath lib\*;shape2svgmap.jar Shape2SVGMap (options)

java -classpath lib\*;shape2svgmap.jar Shape2ImageSVGMap (options)

java -classpath lib\*;shape2svgmap.jar Shape2WGS84 (options)

java -classpath lib\*;shape2svgmap.jar HyperBuilder (options)


There shold be jar files of geotools2.7.5 *1 and javacsv2.1 *2 in the libs folder.
*1: https://sourceforge.net/projects/geotools/files/GeoTools%202.7%20Releases/2.7.5/
*2: https://sourceforge.net/projects/javacsv/

If you would like to use datum conversion function from Tokyo Datum to WGS84 of Shape2WGS84,
there shold be parameter file named "TKY2JGD.par" *3 in the current directory.
*3: http://www.gsi.go.jp/sokuchikijun/tky2jgd_download.html


About license
This tool is open source software based on GPL Ver.3.