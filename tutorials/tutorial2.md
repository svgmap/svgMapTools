# Tutorial2
CSVデータからインタラクティブ地図を作製する方法のチュートリアル　その２<br>
（ポイントのカテゴリーに応じて、ポイントの色を変える）<br>
> 1st rev. 2014.5.13 By Satoru Takagi<br>
> updated  2017.12.07<br>


## 練習１
1. toolsフォルダをカレントにしたコマンドプロンプトで作業する。その他は省略（tutorial1の練習と同じ）

1. 大縮尺(拡大表示)用ベクターデータに変換
   * `java Shape2SVGMap -micrometa2 -level 3 -limit 50 -showtile -densityControl 400 -lowresimage -charset utf-8 -linktitle 3 -directpoi rect -color 4 ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv`
     * 同ディレクトリに、JPcities_of_worldcitiespop_utf8.svgファイルおよび、補助の.svgファイル群が作成される。

1. 小縮尺(縮小表示)用ラスターデータに変換
   * `java Shape2ImageSVGMap ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -charset utf-8 ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv 4 #000000 0 3`
     * 同ディレクトリに、JPcities_of_worldcitiespop_utf8ディレクトリが作成され、その下に補助ファイル群が作成される。(いくつかのディレクトリとpngやsvgファイル）

1. `..\tutorials\webApps\Container.svg` をテキストエディタで編集
   * `<!-- Thematic Layer -->`の行の後に、
   * `<animation title="Cities of Japan" xlink:href="sample/JPcities_of_worldcitiespop_utf8.svg" class="poi" x="-30000" y="-30000" width="60000" height="60000" />`を追加する。（サンプルにはすでに追加済みです）
   * これで地図作成完了


1. `..\tutorials\webApps\SvgMapper.html`をFirefoxで開くと変換したデータが見られる。
   * Regeon(県)の値に応じて色分けしたポイントが表示されている。

1. 地図のUIの簡単な説明
   * ある程度（点が分離する程度）拡大すると、点の形が四角形になり、クリックしてプロパティを表示することができるようbになる。（どの程度拡大すると四角形に変化するかはデータの密度に依る）
   * 点の形が四角形になった段階では、文字入力欄(ENEOSと初期値が入っている)にプロパティの値を入力してSetPOIfilterボタンを押すとフィルタ表示ができる（正規表現）。
     * 複数のプロパティの複合検索は、プロパティがCSV形式（並び順はCSVのデータと同一)であることを想定して、世紀表現形式で入力することで可能



## 練習２
この練習では、Regeonの値ではなくTest3カラムの値(緯度をコピーしたカラム)をもとに、ポイントに色付けする。練習１と違うのは色付けのためのカラムが練習１では文字列（Regeon名）に対し、練習２は数値（緯度）であること。csvのデータが数値であるかどうかを明示するため、スキーマファイルを用いている（-csvschemaオプション　詳細はヘルプを参照）
この場合、色はHSVのH(色相)値に基づいてつけられる。（値が高いと赤(H:0)、低いと青(H:270)）

1. 省略（練習１と同じ）

2. 大縮尺(拡大表示)用ベクターデータに変換
   * `java Shape2SVGMap -micrometa2 -level 3 -limit 50 -showtile -densityControl 400 -lowresimage -directpoi rect -color 6 -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv`

1. 小縮尺(縮小表示)用ラスターデータに変換
   * `java Shape2ImageSVGMap ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -csvschema ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8_schema.txt ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv 6 #000000 0 3`

1. 省略（練習１と同じ）
   * これで地図作成完了

1. `..\tutorials\webApps\SvgMapper.html　をFirefoxで開くと変換したデータが見られる。`
   * Test3(緯度の値のコピー)の値に応じて色分けしたポイントが表示されている。（北が赤く、南が青く表示）

1. 省略（練習１と同じ）



## 実践
1. 作業ディレクトリの設置
   * toolsディレクトリをカレントディレクトリと想定
   * `..\tutorials\webApps\`　以下に任意の作業ディレクトリ(英文字が好ましい)を設置する
     * そのフォルダを以下..\tutorials\webApps\(wdir)とする

1. CSVファイルの準備
   * 設置した..\tutorials\webApps\(wdir)　に、あらかじめ用意したCSVファイル（適当な桁に緯度、経度が入っている）を配置する。
     * 以下、そのファイルを　..\tutorials\webApps\(wdir)\(wfile).csv　とする
   * csvファイルの注意点：tutorial1と同じ

1. CSVの(ColorCol：値に応じてアイコンの色を変化させるための属性番号)を調べる
   * 注：緯度と経度カラムが空間情報に変換され、属性番号が変化するため、CSVのカラム番号とは異なる
   * `java Shape2SVGMap -showhead ..\tutorials\webApps\(wdir)\(wfile).shp`を実行、以下のような表示が出る。<br>
     `attrNo:0 Name:the_geom type:Point`<br>
     `attrNo:1 Name:xxxx`<br>
     `....`<br>
     `attrNo:n Name:(ColorAttrName)名`<br>
     `....`<br>
     `Finished...`<br>
     ここで、控えておいたカラムの名称`(ColorAttrName)`と一致するattrNoの値(n)を控える。これを`(ColorCol)`とする。

1. Shapefileを大縮尺(拡大表示)用地図に変換する。（データサイズによって時間がかかる）
   * `java Shape2SVGMap -micrometa2 -level 3 -limit 50 -showtile -densityControl 400 -lowresimage -directpoi rect -color (colorCol) ..\tutorials\webApps\(wdir)\(wfile).shp
   * 同ディレクトリに、Self-GS-POI-b.svgファイルおよび、補助の.svgファイル群が作成される。

1. Shapefileを小縮尺(縮小表示)用地図に変換する。（データサイズによって時間がかかる）
   * `java Shape2ImageSVGMap ..\tutorials\webApps\(wdir)\(wfile).svg -sumUp 16 -antiAlias ..\tutorials\webApps\(wdir)\(wfile).shp (colorCol) #000000 0 3`
     * 同ディレクトリに、`(wfile)`ディレクトリが作成され、その下に補助ファイル群が作成される。(いくつかのディレクトリとpngやsvgファイル）

1. `..\tutorials\webApps\Container.svg`　を編集する。
   * `<!-- Thematic Layer -->`の行の後に、以下のタグを追加する。
   * `<animation title="(適当なデータ名)" xlink:href="(wdir)/(wfile).svg" class="poi" x="-30000" y="-30000" width="60000" height="60000" />`
     * (適当なデータ名)は、何でも良いが、半角英数を推奨(漢字の場合UTF-8です)
   * これで地図作成完了


1. `..\tutorials\webApps\SvgMapper.html`　をFirefoxで開くと変換したデータが見られる。
<br>
<br>

以上
