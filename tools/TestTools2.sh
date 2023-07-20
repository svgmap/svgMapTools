CLASSPATH="../target/dependency/*":"../target/svgMapTools-202307.jar"
export CLASSPATH
echo $CLASSPATH
java org.svgmap.shape2svgmap.MainWrapper Shape2SVGMap -micrometa2 -level 3 -limit 100 -showtile -densityControl 400 -lowresimage -poisymbol ./symbolTemplate.txt -csvschema ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8_schema.txt ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.csv
java org.svgmap.shape2svgmap.MainWrapper Shape2ImageSVGMap ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -csvschema ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8_schema.txt ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.csv "#00FF80" "#000000" 0 3
google-chrome --allow-file-access-from-files  ../tutorials/webApps/SvgMapper.html

java org.svgmap.shape2svgmap.MainWrapper Shape2SVGMap -micrometa2 -level 3 -limit 100 -showtile -densityControl 400 -lowresimage -directpoi rect -color 6 -csvschema ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8_schema.txt ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.csv
java org.svgmap.shape2svgmap.MainWrapper Shape2ImageSVGMap ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -csvschema ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8_schema.txt ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.csv 6 - 0 3
google-chrome --allow-file-access-from-files  ../tutorials/webApps/SvgMapper.html

java org.svgmap.shape2svgmap.MainWrapper Shape2SVGMap -micrometa2 -level 3 -limit 100 -showtile -densityControl 400 -lowresimage -directpoi rect -color 7 -strcolor 8 -csvschema ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8_schema.txt ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.csv
java org.svgmap.shape2svgmap.MainWrapper Shape2ImageSVGMap ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -strcolor 8 -csvschema ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8_schema.txt ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.csv 7 - 0 3
google-chrome --allow-file-access-from-files  ../tutorials/webApps/SvgMapper.html


java org.svgmap.shape2svgmap.MainWrapper Shape2ImageSVGMap 6 -webMercatorTile -sumUp 16 -antiAlias -strcolor 8 -csvschema ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8_schema.txt ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.csv 7 - 0 3
google-chrome --allow-file-access-from-files  ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8/lvl6/container.svg

java org.svgmap.shape2svgmap.MainWrapper Shape2ImageSVGMap ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -strcolor 8 -csvschema ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8_schema.txt ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.csv 7 - 0 mesh:0.1:0.1
google-chrome --allow-file-access-from-files  ../tutorials/webApps/SvgMapper.html

java org.svgmap.shape2svgmap.MainWrapper Shape2SVGMap -micrometa2 -level 3 -limit 100 -showtile -densityControl 400 -lowresimage -color "#8b4513" -strokefix 2 ../tutorials/webApps/gmSample/coastl_jpn.csv ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.svg
java org.svgmap.shape2svgmap.MainWrapper Shape2ImageSVGMap ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias ../tutorials/webApps/gmSample/coastl_jpn.csv "#00FF80" "#8b4513" 2 2
google-chrome --allow-file-access-from-files  ../tutorials/webApps/SvgMapper.html

java org.svgmap.shape2svgmap.MainWrapper Shape2SVGMap -micrometa2 -level 3 -limit 100 -showtile -densityControl 100 -lowresimage -color "#0000ff" -strokefix 2 ../tutorials/webApps/gmSample/inwatera_jpn.json ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.svg
java org.svgmap.shape2svgmap.MainWrapper Shape2ImageSVGMap ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias ../tutorials/webApps/gmSample/inwatera_jpn.json "#0000ff" "#0000ff" 2 2
google-chrome --allow-file-access-from-files  ../tutorials/webApps/SvgMapper.html

java org.svgmap.shape2svgmap.MainWrapper Shape2SVGMap -micrometa2 -level 3 -limit 100 -showtile -densityControl 100 -lowresimage -color "#305000" -strokefix 2 ../tutorials/webApps/gmSample/polbndl_jpn.shp ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.svg
java org.svgmap.shape2svgmap.MainWrapper Shape2ImageSVGMap ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias ../tutorials/webApps/gmSample/polbndl_jpn.shp "#305000" "#305000" 2 2
google-chrome --allow-file-access-from-files  ../tutorials/webApps/SvgMapper.html
