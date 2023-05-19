CLASSPATH="../target/dependency/*":"../target/svgMapTools-202305.jar"
export CLASSPATH
echo $CLASSPATH
java org.svgmap.shape2svgmap.MainWrapper Shape2SVGMap -micrometa2 -level 3 -limit 100 -showtile -densityControl 400 -lowresimage -poisymbol ../tutorials/webApps/symbolTemplate.txt -csvschema ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8_schema.txt ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.csv
java org.svgmap.shape2svgmap.MainWrapper Shape2ImageSVGMap ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -csvschema ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8_schema.txt ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.csv "#00FF80" "#000000" 0 3
google-chrome --allow-file-access-from-files  ../tutorials/webApps/SvgMapper.html

java org.svgmap.shape2svgmap.MainWrapper Shape2SVGMap -micrometa2 -level 3 -limit 100 -showtile -densityControl 400 -lowresimage -directpoi rect -color 6 -csvschema ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8_schema.txt ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.csv
java org.svgmap.shape2svgmap.MainWrapper Shape2ImageSVGMap ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -csvschema ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8_schema.txt ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.csv 6 - 0 3
google-chrome --allow-file-access-from-files  ../tutorials/webApps/SvgMapper.html

java org.svgmap.shape2svgmap.MainWrapper Shape2SVGMap -micrometa2 -level 3 -limit 100 -showtile -densityControl 400 -lowresimage -directpoi rect -color 7 -strcolor 8 -csvschema ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8_schema.txt ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.csv
java org.svgmap.shape2svgmap.MainWrapper Shape2ImageSVGMap ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -strcolor 8 -csvschema ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8_schema.txt ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.csv 7 - 0 3
google-chrome --allow-file-access-from-files  ../tutorials/webApps/SvgMapper.html
