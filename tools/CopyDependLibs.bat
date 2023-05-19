mkdir "..\target\dependency"
FOR /F %%i in (gtJarList.txt) do copy geotools-9.5\%%i "..\target\dependency\"