# About SVG SVG Map Tools

This tool generates quad tree vector and raster mixed tile SVG map content from shapefile, geojson or CSV file.
You can create your own map from such source data using this tool.

You should be able to convert millions of fairly large data into map content by [quad tree tiling](https://www.slideshare.net/totipalmate/tiling-51301496). [(Example)](http://svgmap.org/devinfo/devkddi/lvl0.1/rev14/SVGMapper_r14.html#visibleLayer=worldcities&hiddenLayer=polygonAuthoringTester)

There are built by Java and requires an environment that runs Java 17 (Oracle and OpenJDK).

For details, please refer to http://svgmap.org/


There are four modules in svgmaptools. Shape2SVGMap and Shape2ImageSVGMap are the essential modules.

* Shape2SVGMap: Convert csv, geojson or shapefile to vector data type svgMap contents.
* Shape2ImageSVGMap: Convert csv, geojson or shapefile to tiled raster data type svgMap contents.
* Shape2WGS84:  Preprocessor
* HyperBuilder: Build Container.svg included svg layers

By combining Shape2SVGMap and Shape2ImageSVGMap, you can also generate the [Quad Tree Composite Tiling](https://satakagi.github.io/mapsForWebWS2020-docs/QuadTreeCompositeTilingAndVectorTileStandard.html) content featured in SVGMap

You should read  [tutorials](tutorials) for preparation of environment and basic usages.

Note: Currently, much of the tutorials and help files are in Japanese. Please do somehow with google translation etc :-)

## Setup
The JDK 17 (Java 17) should be set up including the PATH and JAVA_HOME environment variable.

Basically build using maven. If maven is not provided, see below. It is also possible to set up without maven, but it is a bit cumbersome; instructions for Windows are given below.

#### maven setup
##### Linux:
* `sudo apt install maven`
##### Windows:
* Refer [this link](https://maven.apache.org/guides/getting-started/windows-prerequisites.html)

### Build package
If you cloned the repository, `cd` to the directory where pom.xml is located.
* `mvn package`
* `mvn dependency:copy-dependencies` (Optional)

### Without maven
#### Windows:
* Download javacsv2.1.zip from https://sourceforge.net/projects/javacsv/
* Download geotools-28.4-bin.zip from https://sourceforge.net/projects/geotools/files/GeoTools%2028%20Releases/28.4/
* Unzip them and copy the `javacsv2.1` and `geotools-28.4` directories into the `tools` directory as follows
```
+-pom.xml
+-src
+-target
+-tools
|  +-javacsv2.1
|  | +-javacsv.jar
|  | +-...
|  |
|  +-geotools-28.4
|  | +-lib
|  |   +-*.jar
|  |   +-...
|  |
|  +-CopyDependLibs.bat
|  +-MakeClass.bat
|  +-...
|
+-...
```
* `cd tools`
* `CopyDependLibs.bat`
* If you would like to compile from source code
  * `MakeClass.bat`
* Else, if you use compiled jar file which is registered in the [releases](https://github.com/svgmap/svgMapTools/releases)
  * Copy jar file to target directory as follows

```
+-pom.xml
+-src
+-target
   +dependency
   | +*.jar
   |
   +svgMapTools-{REV}.jar
```

#### Linux:
Almost the same as Windows. However, use `CopyDependLibs.bat` and `MakeClass.bat`.

## Usage

* `java -Xmx800m -classpath target\dependency\*;target\svgMapTools-{REV}.jar org.svgmap.shape2svgmap.MainWrapper Shape2SVGMap (options)`
* `java -Xmx800m -classpath target\dependency\*;target\svgMapTools-{REV}.jar org.svgmap.shape2svgmap.MainWrapper Shape2ImageSVGMap (options)`
* `java -Xmx800m -classpath target\dependency\*;target\svgMapTools-{REV}.jar org.svgmap.shape2svgmap.MainWrapper Shape2WGS84 (options)`
* `java -Xmx800m -classpath target\dependency\*;target\svgMapTools-{REV}.jar org.svgmap.shape2svgmap.MainWrapper HyperBuilder (options)`

  * *{REV} are numbers that vary by release such as "202307"*
  * *linux: s/;/:/*

### In case of using packaged jars with dependent libraries by maven
* `java -Xmx800m -jar svgMapTools-{REV}-jar-with-dependencies.jar Shape2SVGMap (options)`
* `java -Xmx800m -jar svgMapTools-{REV}-jar-with-dependencies.jar Shape2ImageSVGMap (options)`
* `java -jar svgMapTools-{REV}-jar-with-dependencies.jar Shape2WGS84 (options)`
* `java -jar svgMapTools-{REV}-jar-with-dependencies.jar HyperBuilder (options)`

Especially Shape2ImageSVGMap and Shape2SVGMap consumes much heap, so please set the -Xmx option accordingly.

### Shortcuts
Shortcuts .bat or .sh files for each function are stored in the tools directory.
* Shape2SVGMap.bat, Shape2SVGMap.sh
* Shape2ImageSVGMap.bat, Shape2ImageSVGMap.sh
* Shape2WGS84.bat, Shape2WGS84.sh
* HyperBuilder.bat, HyperBuilder.sh

If you would like to use datum conversion function from Tokyo Datum to WGS84 of Shape2WGS84,
there shold be parameter file named ["TKY2JGD.par"](http://www.gsi.go.jp/sokuchikijun/tky2jgd_download.html) in the tools directory.

About license
This tool is open source software based on [GPL Ver.3](LICENSE).
