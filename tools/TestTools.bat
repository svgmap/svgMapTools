IF "%1"=="" (
  CALL :TEST0
  PAUSE
  CALL :TEST1
  PAUSE
  CALL :TEST2
  PAUSE
  CALL :TEST3
  PAUSE
  CALL :TEST4
  PAUSE
  CALL :TEST5
  PAUSE
  CALL :TEST6
  PAUSE
  CALL :TEST7
) ELSE IF "%1"=="0" (
  CALL :TEST0
) ELSE IF "%1"=="1" (
  CALL :TEST1
) ELSE IF "%1"=="2" (
  CALL :TEST2
) ELSE IF "%1"=="3" (
  CALL :TEST3
) ELSE IF "%1"=="4" (
  CALL :TEST4
) ELSE IF "%1"=="5" (
  CALL :TEST5
) ELSE IF "%1"=="6" (
  CALL :TEST6
) ELSE IF "%1"=="7" (
  CALL :TEST7
)

EXIT /b


REM TEST0
:TEST0
java -Xmx500m -jar ..\target\svgMapTools-202307-jar-with-dependencies.jar Shape2SVGMap -micrometa2 -level 3 -limit 100 -showtile -densityControl 400 -lowresimage -poisymbol .\symbolTemplate.txt -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv
java -Xmx500m -jar ..\target\svgMapTools-202307-jar-with-dependencies.jar Shape2ImageSVGMap ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv #00FF80 #000000 0 3
REM start firefox ..\tutorials\webApps\SvgMapper.html 
start chrome --allow-file-access-from-files  %cd%\..\tutorials\webApps\SvgMapper.html
EXIT /b


REM TEST1 set color by copy of latitude value
:TEST1
java -Xmx500m -jar ..\target\svgMapTools-202307-jar-with-dependencies.jar  Shape2SVGMap -micrometa2 -level 3 -limit 100 -showtile -densityControl 400 -lowresimage -directpoi rect -color 6 -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv
java -Xmx500m -jar ..\target\svgMapTools-202307-jar-with-dependencies.jar  Shape2ImageSVGMap ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv 6 - 0 3
REM start firefox ..\tutorials\webApps\SvgMapper.html
start chrome --allow-file-access-from-files  %cd%\..\tutorials\webApps\SvgMapper.html
EXIT /b

REM TEST2 set color by prefecture name
:TEST2
java -Xmx500m -jar ..\target\svgMapTools-202307-jar-with-dependencies.jar  Shape2SVGMap -micrometa2 -level 3 -limit 100 -showtile -densityControl 400 -lowresimage -directpoi rect -color 7 -strcolor 8 -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv
java -Xmx500m -jar ..\target\svgMapTools-202307-jar-with-dependencies.jar  Shape2ImageSVGMap ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -strcolor 8 -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv 7 - 0 3
REM start firefox ..\tutorials\webApps\SvgMapper.html
start chrome --allow-file-access-from-files  %cd%\..\tutorials\webApps\SvgMapper.html
EXIT /b


REM TEST3 web mercator bitimage tile generation Level 6
:TEST3
java -Xmx500m -jar ..\target\svgMapTools-202307-jar-with-dependencies.jar Shape2ImageSVGMap 6 -webMercatorTile -sumUp 16 -antiAlias -strcolor 8 -csvschema ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8_schema.txt ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.csv 7 - 0 3
start chrome --allow-file-access-from-files  %cd%\..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8\lvl6\container.svg
EXIT /b

REM TEST4 physicalSizedMeshBitimageIcon
:TEST4
java -Xmx500m -jar ..\target\svgMapTools-202307-jar-with-dependencies.jar Shape2ImageSVGMap ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -strcolor 8 -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv 7 - 0 mesh:0.1:0.1
start chrome --allow-file-access-from-files  %cd%\..\tutorials\webApps\SvgMapper.html
EXIT /b

REM TEST5 WKT vector line
:TEST5
java -Xmx500m -jar ..\target\svgMapTools-202307-jar-with-dependencies.jar Shape2SVGMap -micrometa2 -level 3 -limit 100 -showtile -densityControl 400 -lowresimage -color #8b4513 -strokefix 2 ..\tutorials\webApps\gmSample\coastl_jpn.csv ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg
java -Xmx500m -jar ..\target\svgMapTools-202307-jar-with-dependencies.jar Shape2ImageSVGMap ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias ..\tutorials\webApps\gmSample\coastl_jpn.csv #00FF80 #8b4513 2 2
start chrome --allow-file-access-from-files  %cd%\..\tutorials\webApps\SvgMapper.html
EXIT /b

REM TEST6 geojeon vector polygon
:TEST6
java -Xmx500m -jar ..\target\svgMapTools-202307-jar-with-dependencies.jar Shape2SVGMap -micrometa2 -level 3 -limit 100 -showtile -densityControl 100 -lowresimage -color #0000ff -strokefix 2 ..\tutorials\webApps\gmSample\inwatera_jpn.json ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg
java -Xmx500m -jar ..\target\svgMapTools-202307-jar-with-dependencies.jar Shape2ImageSVGMap ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias ..\tutorials\webApps\gmSample\inwatera_jpn.json #0000ff #0000ff 2 2
start chrome --allow-file-access-from-files  %cd%\..\tutorials\webApps\SvgMapper.html
EXIT /b

REM TEST7 shp vector line
:TEST7
java -Xmx500m -jar ..\target\svgMapTools-202307-jar-with-dependencies.jar Shape2SVGMap -micrometa2 -level 3 -limit 100 -showtile -densityControl 100 -lowresimage -color #305000 -strokefix 2 ..\tutorials\webApps\gmSample\polbndl_jpn.shp ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg
java -Xmx500m -jar ..\target\svgMapTools-202307-jar-with-dependencies.jar Shape2ImageSVGMap ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias ..\tutorials\webApps\gmSample\polbndl_jpn.shp #305000 #305000 2 2
start chrome --allow-file-access-from-files  %cd%\..\tutorials\webApps\SvgMapper.html
EXIT /b

