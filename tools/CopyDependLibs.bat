mkdir "..\target\dependency"
FOR /F %%i in (gtJarList.txt) do copy geotools-28.4\lib\%%i "..\target\dependency\"

copy javacsv2.1\javacsv.jar "..\target\dependency\"