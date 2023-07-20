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
         * ..\tutorials\webApps\gmSample\polbndl_jpn.shp : ソースデータ(shapefile)

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
         * ..\tutorials\webApps\gmSample\inwatera_jpn.json : ソースデータ(GeoJSON)

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
       * ..\tutorials\webApps\gmSample\coastl_jpn.csv : ソースデータ(CSV ただし位置情報は[WKT](https://ja.wikipedia.org/wiki/Well-known_text)でエンコード)
     * ソースデータの要点
         * CSVデータである点は[tutorial1](tutorial1.md)と同じですが、位置情報(線のジオメトリ)のカラムが[WKT](https://ja.wikipedia.org/wiki/Well-known_text)でエンコードされた文字列(ダブルクオーテーションでエスケープ)である点が異なります。
         * また1行目のスキーマ行では位置情報(ジオメトリ)カラム名に`WKT` が記載されている必要があります。

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



## 実践
1. 作業ディレクトリの設置
   * toolsディレクトリをカレントディレクトリと想定
   * `tutorials\webApps\`　以下に任意の作業ディレクトリ(英文字が好ましい)を設置する
     * そのフォルダを以下`tutorials\webApps\(wdir)`とする

1. CSVファイルの準備
   * 設置した`tutorials\webApps\(wdir)`　に、あらかじめ用意したCSVファイル（適当な桁に緯度、経度が入っている）を配置する。
     * 以下、そのファイルを　`tutorials\webApps\(wdir)\(wfile).csv`　とする
   * csvファイルの注意点：tutorial1と同じ

1. CSVの(ColorCol：値に応じてアイコンの色を変化させるための属性番号)を調べる
   * 注：緯度と経度カラムが空間情報に変換され、属性番号が変化するため、CSVのカラム番号とは異なる
   * `Shape2SVGMap.bat -showhead ..\tutorials\webApps\(wdir)\(wfile).shp`を実行、以下のような表示が出る。<br>
     `attrNo:0 Name:the_geom type:Point`<br>
     `attrNo:1 Name:xxxx`<br>
     `....`<br>
     `attrNo:n Name:(ColorAttrName)`<br>
     `....`<br>
     `Finished...`<br>
     ここで、控えておいたカラムの名称`(ColorAttrName)`と一致するattrNoの値(n)を控える。これを`(ColorCol)`とする。

1. 大縮尺(拡大表示)用ベクターデータを生成（データサイズによって時間がかかる）
   * `Shape2SVGMap.bat -micrometa2 -level 3 -limit 50 -showtile -densityControl 400 -lowresimage -directpoi rect -color (colorCol) ..\tutorials\webApps\(wdir)\(wfile).shp`
   * 同ディレクトリに、Self-GS-POI-b.svgファイルおよび、補助の.svgファイル群が作成される。

1. 小縮尺(縮小表示)用ラスターデータを生成（データサイズによって時間がかかる）
   * `Shape2ImageSVGMap.bat ..\tutorials\webApps\(wdir)\(wfile).svg -sumUp 16 -antiAlias ..\tutorials\webApps\(wdir)\(wfile).shp (colorCol) #000000 0 3`
     * 同ディレクトリに、`(wfile)`ディレクトリが作成され、その下に補助ファイル群が作成される。(いくつかのディレクトリとpngやsvgファイル）

1. `tutorials\webApps\Container.svg`　を編集する。
   * `<!-- Thematic Layer -->`の行の後に、以下のタグを追加する。
   * `<animation title="(CONTENT Title)" xlink:href="(wdir)/(wfile).svg" class="poi" x="-30000" y="-30000" width="60000" height="60000" />`
     * (CONTENT Title)は、何でも良いが、半角英数を推奨(漢字の場合UTF-8です)
   * これで地図作成完了


1. `tutorials\webApps\SvgMapper.html`　をChromeで開くと変換したデータが見られる。
<br>
<br>


## 参考
地球地図データはShapefile形式で配布されていますが、これを各型式に変換するときに使用した[GDAL/OGR](https://gdal.org/)([windows版](http://www.gisinternals.com/))のogr2ogrコマンドパラメータを紹介
* `ogr2ogr -f GeoJSON inwatera_jpn.json inwatera_jpn.shp`
* `ogr2ogr -f CSV -lco GEOMETRY=AS_WKT coastl_jpn.csv coastl_jpn.shp`


以上
