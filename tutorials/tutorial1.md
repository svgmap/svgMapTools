# Tutorial1 
CSVデータからインタラクティブ地図を作製する方法のチュートリアル
> 2014.03.03 1st rev. By Satoru Takagi<br>
> 2017.12.07 OSS化に対応した更新<br>
> 2018.01.11 markdown化 <br>
> 2018.06.01 SVGMap*.jsを更新、-Xmx追加 <br>
> 2019.07.22 Firefoxのローカル利用が困難になったので説明変更

## Notes:
### 実行環境について
* データの作成のために、jreもしくはjava がインストールされている環境が必要です。
* なお、動作チェックはWindows10とJAVA8で行っています。(JAVA10(JAVA9以降)では動作しません。GeotoolsがJAVA10非互換のため。なおOpenJDK8も簡単な動作確認を行っています。)
* 生成したコンテンツは、Firefox, Chromen, Edge, IE11で動作可能です。ただし、このチュートリアルに沿ってコンテンツをローカルに置いたまま表示を試す場合は、EdgeとChrome(特別な起動オプションが必要(下記参照))でのみ使用できます。
  * Firefox, Chrome, IE11とも、生成したコンテンツを任意のウェブサーバにコピーしたうえでアクセスすれば使用可能です。
  * Edgeの場合ローカルに生成したファイルをそのまま開いて使用できます。
  * Edge以外のブラウザでは制限がかかり、そのままではアクセスできません。
    * Chromeは --allow-file-access-from-filesオプションをつけて起動することで使用可能です（ショートカットを作成すると良い）<br>
    `start chrome --allow-file-access-from-files`<br>
    注記: Chromeが常駐している場合は、それを終了させてから上記の起動をする必要があります。(常駐Chromeはタスクトレイのアイコンから終了可能)
    * IE、Firefoxでは、安全に回避する方法はないのでローカルファイルのまま使用できません。
* 生成したコンテンツは、背景地図としてインターネット上のコンテンツ(OpenStreetMapや電子国土)を参照しているため、インターネットに接続できる環境で利用する必要があります（別途背景地図をローカルに用意すればスタンドアロン環境でも利用可能）

### サンプルデータについて
* このチュートリアルは練習用のサンプルデータとしてMaxMind社が製作したWorld Cities Databaseを使用しています。下記はその宣言です。
  * This product includes data created by MaxMind, available from http://www.maxmind.com/
* なお、チュートリアルでは日本部分のみを抽出して使用していますが、このコンバータはヒープを確保すれば全世界の変換も可能です。
  * [全世界(約317万ポイント)の変換例](http://svgmap.org/devinfo/devkddi/lvl0.1/rev14/SVGMapper_r14.html#visibleLayer=worldcities&hiddenLayer=polygonAuthoringTester)
  (ヒープ確保量　-XMx800m)

## 最初の練習  
1. ツールのダウンロード
   * [releases](https://github.com/svgmap/svgMapTools/releases)から、最新のリリースのソースコード（ Source code (zip)）と、jar（shape2svgmap.jar）をダウンロードします。
   * ソースコードを解凍し、toolsディレクトリに shape2svgmap.jarを投入します。
   * もしくはjavacがあれば、srcディレクトリのMakeClass.batで自分でjarを生成することもできます。
   
1. 外部ライブラリの準備
   * (この文書のあるディレクトリ) `\..\tools\`に以下のファイルが配置されていることを確認します。
     * `shape2svgmap.jar`
     * `mesh2.txt`
     * `symbolTemplate.txt`
     * `lib\` (ディレクトリ) `svgmaptools`が使用する二つの外部ライブラリのjarをこのディレクトリに置きます
       * geotools2.7.5 : https://sourceforge.net/projects/geotools/files/GeoTools%202.7%20Releases/2.7.5/
       * javacsv2.1 : https://sourceforge.net/projects/javacsv/
<br>libディレクトリ直下に、これらのライブラリのjarファイルを投入します。なお配布形態によっては既に配置済みの場合もあります。
<br>javacsv2.1のjarファイルは一個ですが、geotools2.7.5のjarファイルは(geotools-2.7.5-bin.zip)にパックされており、解凍すると複数のjarに分かれています。これらのjarファイルをすべて投入してください。

1. 作業ディレクトリ移動とclasspath設定
   * コマンドプロンプトを開き、以下を指示します<br>
`cd (Tutorials DIR)\..\tools\`<br>
`set CLASSPATH=%CLASSPATH%;.\lib\*;shape2svgmap.jar`<br>
     * (Tutorials DIR)は、この文書のあるディレクトリです。
     * なお変換する対象ファイルは、toolsディレクトリに対して、`..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv` に格納されていると仮定して練習を進めます。

1. csvfileを大縮尺(拡大表示)用ベクター地図に変換
   * `java -Xmx500m Shape2SVGMap -poisymbol symbolTemplate.txt -micrometa2 -level 3 -limit 50 -showtile -densityControl 400 -lowresimage -charset utf-8 -linktitle 3 ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv`<br>
     * 同ディレクトリに、`JPcities_of_worldcitiespop_utf8.svg`ファイルおよび、補助の.svgファイル群が作成されます。

1. csvfileを小縮尺(縮小表示)用ラスター地図に変換
   * `java -Xmx500m Shape2ImageSVGMap ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -charset utf-8 ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv #0000ff #0000ff 0 3`<br>
     * 同ディレクトリに、JPcities_of_worldcitiespop_utf8ディレクトリが作成され、その下に付随するファイル群が作成されます。(いくつかのディレクトリとpngやsvgファイル）

1. `..\webApps\webApps\Container.svg`をテキストエディタで編集
   * `<!-- Thematic Layer -->`の行の後に、生成したコンテンツのルートとなるファイルへのリンクを追加します。（サンプルにはすでに追加済みですので確認のみしてください。）
   * `<animation title="Cities of Japan" xlink:href="sample/JPcities_of_worldcitiespop_utf8.svg" class="poi" x="-30000" y="-30000" width="60000" height="60000" />`
   * これで地図作成完了

1. `..\tutorials\webApps\SvgMapper.html`　をEdgeもしくは`start chrome --allow-file-access-from-files`で開くと変換したデータが見られる。

## 実践
1. 作業ディレクトリの設置
   * `tools`ディレクトリをカレントディレクトリと想定
   * `..\tutorials\webApps\`　以下に任意の作業ディレクトリ(英文字が好ましい)を設置
     * このフォルダを以下`..\webApps\(wdir)`とする
   * `tools\mappins\mappin.png`　を　`..\tutorials\webApps\(wdir)`にコピー

1. CSVファイルの準備
   * 設置した`..\tutorials\webApps\(wdir)`　に、あらかじめ用意したCSVファイル（適当な桁に緯度、経度が入っている）を配置
     * 以下、そのファイルを　`..\tutorials\webApps\(wdir)\(wfile).csv`　とします。
     * csvファイルの注意点：（なお、shapefileを変換することも可能）
       * 漢字が入っている場合、シフトJISにします。(Windowsの場合（OS標準文字コード）　オプションによって文字コードを明示することも可能)
       * 最初の行には項目名がカンマ区切りで入っている必要があります。（無い場合はテキストエディタで編集挿入します。なお、スキーマファイルを与えることで項目名が無いままでの変換も可能です。）
       * 緯度経度のカラムの項目名は、`latitude`, `longitude`　となっている必要があります。
       * 緯度経度のカラムは、以降のカラム指定番号から外して考えます。
       * そのうえで、1カラム目が最初のカラムとなります。

1. 大縮尺(拡大表示)用ベクトル地図を生成（データサイズによって時間がかかります）
   * `java -Xmx500m Shape2SVGMap -poisymbol symbolTemplate.txt -micrometa2 -level 3 -limit 50 -showtile -densityControl 400 -lowresimage ..\tutorials\webApps\(wdir)\(wfile).csv`
     * 同ディレクトリに、JPcities_of_worldcitiespop_utf8.svgファイルおよび、補助の.svgファイル群が作成されます。
     
1. 小縮尺(縮小表示)用ラスター地図を生成（データサイズによって時間がかかります）
   * `java -Xmx500m Shape2ImageSVGMap ..\tutorials\webApps\(wdir)\(wfile).svg -sumUp 16 -antiAlias ..\tutorials\webApps\(wdir)\(wfile).csv #0000ff #0000ff 0 3`
     * 同ディレクトリに、(wfile)ディレクトリが作成され、その下に補助ファイル群が作成されます。(いくつかのディレクトリとpngやsvgファイル）

1. ルートコンテナファイル`..\tutorials\webApps\Container.svg`　を編集
   * `<!-- Thematic Layer -->`の行の後に、以下のタグ<br>
   `<animation title="(CONTENT Title)" xlink:href="(wdir)/(wfile).svg" class="poi" x="-30000" y="-30000" width="60000" height="60000" />`
を追加
     * (CONTENT Title)は、何でも良いですが、半角英数を推奨します。(漢字の場合UTF-8です)
   * これで地図作成完了

1. `..\tutorials\webApps\SvgMapper.html`　をFirefoxで開くと変換したデータが見られます。（ウェブサイトに置いた場合は、そのコンテンツの静的なURLでアクセスできます）

## 応用：複数のデータの合成
* 複数のデータを別のディレクトリに作成し、それらを地図上でに合成したり、切り替えたりできるようにすします（レイヤリング機能）<br>
  * 実践編の作業を別のディレクトリ名を作って複数回実施
  * ルートコンテナには、追加したいレイヤー分のタグを追加
  * これでデフォルトですべてのレイヤーが表示された状態となります。

* 大縮尺表示用ピンを変更<br>
　mapins\の適当なpngを作業ディレクトリにコピーしたうえで、mappin.pngにリネームします。

* 小縮尺表示用の丸点の色を変更<br>
　Shape2ImageSVGMapコマンドの、以下の(色コード)部分を変更して実行します<br>
　`java -Xmx500m Shape2ImageSVGMap ..\tutorials\webApps\(wdir)\(wfile).svg -sumUp 8 -antiAlias ..\tutorials\webApps\(wdir)\(wfile).shp (Fill Color) (Stroke Color) 0 3`
  * (Fill Color) (Stroke Color)はPOIの色コードで、ひとまずは両方とも同じ値で良いでしょう。色コードはWebの色コードで#RRGGBB (RR,GG,BBそれぞれ00-FF)です。
　
* 初期状態で表示させたくないデータ<br>
ルートコンテナファイルのタグ編集において、`visibilty="hidden"`属性を付けます
  * `<animation title="Cities of Japan" xlink:href="(wdir)/(wfile).svg" class="poi" x="-30000" y="-30000" width="60000" height="60000" visibilty="hidden"/>`

* どれかのデータだけを表示（択一表示）<br>
ルートコンテナファイルのタグ編集において、デフォルトで表示させたいデータ以外に`visibilty="hidden"`属性を付けたうえで、全てのレイヤーのタグのclass属性を`class="poi switch"`に変更します。
  * `<animation title="Cities of Japan" xlink:href="(wdir)/(wfile).svg" class="poi switch" x="-30000" y="-30000" width="60000" height="60000" visibilty="hidden"/>`

## さらに応用：ツールの複雑な使い方など
* 以下のツールは、オプションなしで起動することでヘルプメッセージが出力され、詳細なオプションの説明が提供されます。
  * `java Shape2SVGMap`
  * `java Shape2ImageSVGMap`
  * `java Shape2WGS84`
  * `java HyperBuilder`
* 点の個数は１０００万点規模まで動作検証しています。
* このチュートリアルでは、点オブジェクトのみの地図化を実行したが、shapefileを用いることで、線や面の（ベクトルタイル）地図化にも対応できます。
* 複数のタイリング手法のサポート
  * 均一に分割されたタイリング
  * なるべく同程度のファイルサイズとなるように四分木で調整されたタイリング（Quad Tree Tiling：本チュートリアルで使用）
<br><br>
以上
