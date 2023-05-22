#!/bin/bash
mkdir -p "../target/dependency"
file_list="./gtJarList.txt"

while IFS= read -r line; do
  filename=$(echo "$line" | tr -d '\r')
  echo "geotools-9.5/$filename ../target/dependency"
  cp "geotools-9.5/$filename" "../target/dependency"
done < "$file_list"
