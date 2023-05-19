set jc=java -classpath ..\target\dependency\*;..\target\svgMapTools-202305.jar org.svgmap.shape2svgmap.MainWrapper

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

EXIT /b


REM TEST0
:TEST0
%jc% Shape2SVGMap -micrometa2 -level 3 -limit 100 -showtile -densityControl 400 -lowresimage -poisymbol ..\tutorials\webApps\symbolTemplate.txt -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv
%jc% Shape2ImageSVGMap ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv #00FF80 #000000 0 3
REM start firefox ..\tutorials\webApps\SvgMapper.html 
start chrome --allow-file-access-from-files  %cd%\..\tutorials\webApps\SvgMapper.html
EXIT /b


REM TEST1 set color by copy of latitude value
:TEST1
%jc% Shape2SVGMap -micrometa2 -level 3 -limit 100 -showtile -densityControl 400 -lowresimage -directpoi rect -color 6 -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv
%jc% Shape2ImageSVGMap ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv 6 - 0 3
REM start firefox ..\tutorials\webApps\SvgMapper.html
start chrome --allow-file-access-from-files  %cd%\..\tutorials\webApps\SvgMapper.html
EXIT /b

REM TEST2 set color by prefecture name
:TEST2
%jc% Shape2SVGMap -micrometa2 -level 3 -limit 100 -showtile -densityControl 400 -lowresimage -directpoi rect -color 7 -strcolor 8 -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv
%jc% Shape2ImageSVGMap ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -strcolor 8 -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv 7 - 0 3
REM start firefox ..\tutorials\webApps\SvgMapper.html
start chrome --allow-file-access-from-files  %cd%\..\tutorials\webApps\SvgMapper.html
EXIT /b

