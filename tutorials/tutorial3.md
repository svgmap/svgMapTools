# Tutorial3
様々なデータからコンテンツを生成する<br>
[Shapefile](https://ja.wikipedia.org/wiki/%E3%82%B7%E3%82%A7%E3%83%BC%E3%83%97%E3%83%95%E3%82%A1%E3%82%A4%E3%83%AB), [GeoJSON](https://ja.wikipedia.org/wiki/GeoJSON), CSV([WKT形式](https://ja.wikipedia.org/wiki/Well-known_text))形式の ラインもしくはポリゴンデータのコンテンツを作成します<br>
このチュートリアルでは、いずれも[地球地図日本](https://www.gsi.go.jp/kankyochiri/gm_jpn.html)データを使用しています。(地球地図日本 ©国土地理院)

> 2023.07.20 新規作成


## 練習１
[Shapefile](https://ja.wikipedia.org/wiki/%E3%82%B7%E3%82%A7%E3%83%BC%E3%83%97%E3%83%95%E3%82%A1%E3%82%A4%E3%83%AB)形式の自治体境界線を可視化します。

1. 大縮尺(拡大表示)用ベクターデータを生成
   * `Shape2SVGMap.bat -micrometa2 -level 3 -limit 100 -showtile -densityControl 100 -lowresimage -color #305000 -strokefix 2 ..\tutorials\webApps\gmSample\polbndl_jpn.shp`
      * `..\tutorials\webApps\gmSample\`ディレクトリに、polbndl_jpn.svgファイルおよび、タイル分割された大縮尺用の*.svgファイル群が作成される。
      * パラメータの要点
         * -color #305000 : 色をHTMLカラーで指定
         * -strokefix 2 : 線の太さを2pxに
         * ..\tutorials\webApps\gmSample\polbndl_jpn.shp : ソースデータ (拡張子が.shpのものはshapefileとして処理される)

1. 小縮尺(縮小表示)用ラスターデータを生成  
   * `Shape2ImageSVGMap.bat ..\tutorials\webApps\gmSample\polbndl_jpn.svg -sumUp 16 -antiAlias ..\tutorials\webApps\gmSample\polbndl_jpn.shp #305000 #305000 2 2`
      * 同ディレクトリに、polbndl_jpnディレクトリが作成され、その下に補助ファイル群が作成される。(いくつかのディレクトリとpngやsvgファイル）
      * パラメータの要点 (末尾5パラメータ)
         * ..\tutorials\webApps\gmSample\polbndl_jpn.shp : ソースデータ(shapefile)
         * #305000 : 塗りの色 (本データは線データなので不使用)
         * #305000 : 線の色
         * 2 : 線の太さ
         * 2 : ポイントアイコンのサイズ (本データは線データなので不使用)


1. `tutorials\webApps\Container.svg` をテキストエディタで編集
   * `<!-- Thematic Layer -->`の行の後に、
   * `<animation title="自治体境界" xlink:href="gmSample/polbndl_jpn.svg" class="地球地図日本" x="-30000" y="-30000" width="60000" height="60000" />`<br>を追加する。
   * これで地図作成完了


1. `tutorials\webApps\SvgMapper.html`をローカルWebApp起動可能モードのChromeで開くと変換したデータが見られる。



## 練習２
[GeoJSON](https://ja.wikipedia.org/wiki/GeoJSON)形式の湖沼ポリゴンを可視化します。

1. 大縮尺(拡大表示)用ベクターデータを生成
   * `Shape2SVGMap.bat -micrometa2 -level 3 -limit 100 -showtile -densityControl 100 -lowresimage -color #0000ff -strokefix 2 ..\tutorials\webApps\gmSample\inwatera_jpn.json`
      * `..\tutorials\webApps\gmSample\`ディレクトリに、inwatera_jpn.svgファイルおよび、タイル分割された大縮尺用の*.svgファイル群が作成される。
      * パラメータの要点
         * -color #0000ff : 色をHTMLカラーで指定 (-colorオプションはポリゴンの場合塗の色の指定となる)
         * ..\tutorials\webApps\gmSample\inwatera_jpn.json : ソースデータ (拡張子が.jsonのものはGeoJSONとして処理される)

1. 小縮尺(縮小表示)用ラスターデータを生成  
   * `Shape2ImageSVGMap.bat ..\tutorials\webApps\gmSample\inwatera_jpn.svg -sumUp 16 -antiAlias ..\tutorials\webApps\gmSample\inwatera_jpn.json #0000ff #0000ff 2 2`
      * 同ディレクトリに、inwatera_jpnディレクトリが作成され、その下に補助ファイル群が作成される。(いくつかのディレクトリとpngやsvgファイル）
      * パラメータの要点 (末尾5パラメータ)
         * ..\tutorials\webApps\gmSample\inwatera_jpn.json : ソースデータ(GeoJSON)
         * #0000ff : 塗りの色
         * #0000ff : 線の色 (本データはポリゴンデータなので不使用)
         * 2 : 線の太さ  (本データはポリゴンデータなので不使用)
         * 2 : ポイントアイコンのサイズ (本データはポリゴンデータなので不使用)


1. `tutorials\webApps\Container.svg` をテキストエディタで編集
   * `<!-- Thematic Layer -->`の行の後に、
   * `<animation title="湖沼" xlink:href="gmSample/inwatera_jpn.svg" class="地球地図日本" x="-30000" y="-30000" width="60000" height="60000" />`<br>を追加する。
   * これで地図作成完了


1. `tutorials\webApps\SvgMapper.html`をローカルWebApp起動可能モードのChromeで開くと変換したデータが見られる。

## 練習３
CSV、ただしジオメトリカラムが[WKT形式](https://ja.wikipedia.org/wiki/Well-known_text)の海岸線を可視化します。

1. 大縮尺(拡大表示)用ベクターデータを生成
   * `Shape2SVGMap.bat -micrometa2 -level 3 -limit 100 -showtile -densityControl 400 -lowresimage -color #8b4513 -strokefix 2 ..\tutorials\webApps\gmSample\coastl_jpn.csv`
      * `..\tutorials\webApps\gmSample\`ディレクトリに、coastl_jpn.svgファイルおよび、タイル分割された大縮尺用の*.svgファイル群が作成される。
     * パラメータの要点
       * -color #8b4513 : 色をHTMLカラーで指定
       * ..\tutorials\webApps\gmSample\coastl_jpn.csv : ソースデータ (CSV ただし位置情報は[WKT](https://ja.wikipedia.org/wiki/Well-known_text)でエンコード)
     * ソースデータの要点
         * 拡張子が.csvのものはCSVデータとして処理される点は[tutorial1](tutorial1.md)と同じ。
         * ただし、位置情報(線のジオメトリ)のカラムが[WKT](https://ja.wikipedia.org/wiki/Well-known_text)でエンコードされた文字列(ダブルクオーテーションでエスケープ)である点が異なります。
         * また1行目(スキーマ行)の位置情報(ジオメトリ)のカラム名に`WKT` と記載されている必要があります。

1. 小縮尺(縮小表示)用ラスターデータを生成  
   * `Shape2ImageSVGMap.bat ..\tutorials\webApps\gmSample\coastl_jpn.svg -sumUp 16 -antiAlias ..\tutorials\webApps\gmSample\coastl_jpn.csv #00FF80 #8b4513 2 2`
     * 同ディレクトリに、coastl_jpnディレクトリが作成され、その下に補助ファイル群が作成される。(いくつかのディレクトリとpngやsvgファイル）
     * パラメータの要点
       * 末尾5パラメータ
         * ..\tutorials\webApps\gmSample\coastl_jpn.csv : ソースデータ(CSV ただし位置情報は[WKT](https://ja.wikipedia.org/wiki/Well-known_text)でエンコード)
         * #00FF80 : 塗りの色 (本データは線データなので不使用)
         * #8b4513 : 線の色 
         * 2 : 線の太さ
         * 2 : ポイントアイコンのサイズ (本データは線データなので不使用)


1. `tutorials\webApps\Container.svg` をテキストエディタで編集
   * `<!-- Thematic Layer -->`の行の後に、
   * `<animation title="海岸線" xlink:href="gmSample/coastl_jpn.svg" class="地球地図日本" x="-30000" y="-30000" width="60000" height="60000" />`<br>を追加する。
   * これで地図作成完了


1. `tutorials\webApps\SvgMapper.html`をローカルWebApp起動可能モードのChromeで開くと変換したデータが見られる。





## 参考
地球地図データはShapefile形式で配布されていますが、これを各型式に変換するときに使用した[GDAL/OGR](https://gdal.org/)([windows版](http://www.gisinternals.com/))のogr2ogrコマンドパラメータを紹介
* `ogr2ogr -f GeoJSON inwatera_jpn.json inwatera_jpn.shp`
* `ogr2ogr -f CSV -lco GEOMETRY=AS_WKT coastl_jpn.csv coastl_jpn.shp`


以上
