#!/bin/bash
mkdir -p "../target/dependency"
file_list="./gtJarList.txt"

while IFS= read -r line; do
  filename=$(echo "$line" | tr -d '\r')
  echo "geotools-28.4/lib/$filename ../target/dependency"
  cp "geotools-28.4/lib/$filename" "../target/dependency"
done < "$file_list"

cp "javacsv2.1/javacsv.jar" "../target/dependency"
