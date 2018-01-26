cd ..\tools\

set CLASSPATH=%CLASSPATH%;.\lib\*;shape2svgmap.jar

IF "%1"=="" (
  CALL :TEST0
  PAUSE
  CALL :TEST1
  PAUSE
  CALL :TEST2
) ELSE IF "%1"=="0" (
  CALL :TEST0
) ELSE IF "%1"=="1" (
  CALL :TEST1
) ELSE IF "%1"=="2" (
  CALL :TEST2
)

cd ..\src\
EXIT /b


REM TEST0
:TEST0
java Shape2SVGMap -micrometa2 -level 3 -limit 100 -showtile -densityControl 400 -lowresimage -poisymbol symbolTemplate.txt -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv
java Shape2ImageSVGMap ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv #00FF80 #000000 0 3
start firefox ..\tutorials\webApps\SvgMapper.html
EXIT /b


REM TEST1 set color by copy of latitude value
:TEST1
java Shape2SVGMap -micrometa2 -level 3 -limit 100 -showtile -densityControl 400 -lowresimage -directpoi rect -color 6 -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv
java Shape2ImageSVGMap ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv 6 #000000 0 3
start firefox ..\tutorials\webApps\SvgMapper.html
EXIT /b

REM TEST2 set color by prefecture name
:TEST2
java Shape2SVGMap -micrometa2 -level 3 -limit 100 -showtile -densityControl 400 -lowresimage -directpoi rect -color 7 -strcolor 8 -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv
java Shape2ImageSVGMap ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -strcolor 8 -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv 7 #000000 0 3
start firefox ..\tutorials\webApps\SvgMapper.html
EXIT /b

