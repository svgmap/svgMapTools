# About SVG SVG Map Tools

This tool generates quad tree vector and raster mixed tile SVG map content from shapefile or csv file.
You can create your own map from CSV or shapefile using this tool.

You should be able to convert millions of fairly large data into map content by [quad tree tiling](https://www.slideshare.net/totipalmate/tiling-51301496). [(Example)](http://svgmap.org/devinfo/devkddi/lvl0.1/rev14/SVGMapper_r14.html#visibleLayer=worldcities&hiddenLayer=polygonAuthoringTester)

There are built by Java and requires an environment that runs Java (Oracle version).

For details, please refer to http://svgmap.org/


There are four modules in svgmaptools. Shape2SVGMap and Shape2ImageSVGMap are the essential modules.

* Shape2SVGMap: Convert csv or shapefile to vector data type svgMap contents.
* Shape2ImageSVGMap: Convert csv or shapefile to tiled raster data type svgMap contents.
* Shape2WGS84:  Preprocessor
* HyperBuilder: Build Container.svg included svg layers

You should read tutorials.

Note: Currently, much of the tutorials and help files are in Japanese. Please do somehow with google translation etc :-)

## Usage (Windows)

A compiled jar file is registered in the [releases](https://github.com/svgmap/svgMapTools/releases). Preparation of environment is described in [tutorial1](tutorials).

* `java -Xmx500m -classpath lib\*;shape2svgmap.jar Shape2SVGMap (options)`
* `java -Xmx500m -classpath lib\*;shape2svgmap.jar Shape2ImageSVGMap (options)`
* `java -Xmx500m -classpath lib\*;shape2svgmap.jar Shape2WGS84 (options)`
* `java -Xmx500m -classpath lib\*;shape2svgmap.jar HyperBuilder (options)`

Especially Shape2ImageSVGMap consumes much heap, so please set the -Xmx option accordingly.

There shold be jar files of [geotools2.7.5](https://sourceforge.net/projects/geotools/files/GeoTools%202.7%20Releases/2.7.5/) and [javacsv2.1](https://sourceforge.net/projects/javacsv/) in the libs folder.

If you would like to use datum conversion function from Tokyo Datum to WGS84 of Shape2WGS84,
there shold be parameter file named ["TKY2JGD.par"](http://www.gsi.go.jp/sokuchikijun/tky2jgd_download.html) in the current directory.

About license
This tool is open source software based on [GPL Ver.3](LICENSE).
