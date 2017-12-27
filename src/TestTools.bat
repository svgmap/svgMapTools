cd ..\tools\

set CLASSPATH=%CLASSPATH%;.\lib\*;shape2svgmap.jar

java Shape2SVGMap -micrometa2 -level 3 -limit 200 -showtile -densityControl 400 -lowresimage -directpoi rect -color 6 -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv

java Shape2ImageSVGMap ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv 6 #000000 0 3

start firefox ..\tutorials\webApps\SvgMapper.html

cd ..\src\