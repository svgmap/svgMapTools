# Tutorial1 
CSVデータからインタラクティブ地図を作製する方法のチュートリアル
> 2014.03.03 1st rev. By Satoru Takagi<br>
> 2017.12.07 OSS化に対応した更新<br>
> 2018.01.11 markdown化 <br>
> 2018.06.01 SVGMap*.jsを更新、-Xmx追加<br>
> 2023.05.19 mavenベースのパッケージに移行。SVGMap*.jsをCDN経由に変更<br>
> 2023.07.18 Java17, Geotools28.4対応<br>
> 2023.07.20 tutorial3を追加<br>
> 2024.06.19 Macintosh, Linux用の補足説明追加<br>
> 2024.09.18 Java21で動作確認<br>

## Notes:
### 実行環境について
* データの作成のために、Java 17 (jre 17もしくはjdk 17)もしくはJava 21 がインストールされている環境が必要です。
  * なお、動作チェックはWindows11とJava21(Corretto21)で行っています。なおJavaはOracle版だけではなくOpenJDK (Corretto等)でも動作確認しています。
* 生成したコンテンツはほとんどのウェブブラウザで利用可能です。<BR>ただしローカルファイルでの動作確認には制約があります。以下に記載します。<BR>(なお、この制約はSVGMap固有でなくWebAppに一般的なもので、Webサーバ上にコンテンツを設置した場合は制限なく表示できます。)
  * ローカルに保存したコンテンツでは、Chromeで `--allow-file-access-from-files` オプションをつけて起動した場合のみ表示できます。以下起動例（２例）（ショートカットを作成すると良い）
    * `"C:\Program Files\Google\Chrome\Application\chrome.exe" --allow-file-access-from-files`
    * `start chrome --allow-file-access-from-files`
      * 既にChromeが起動している場合は無効。すべてのChromeをいったん終了させてから起動する必要があります。
      * 同オプションの参照情報([Chdromium公式ドキュメント](https://www.chromium.org/developers/how-tos/run-chromium-with-flags/)から[参照されているオプション情報](https://peter.sh/experiments/chromium-command-line-switches/#allow-file-access-from-files))
  * microsoft Edgeでも同様に動作します。(Chromeと同じChromeiumベースのブラウザのため)
    * `start msedge --allow-file-access-from-files`
* 生成したコンテンツは、背景地図としてインターネット上のコンテンツ(OpenStreetMapや地理院タイル)を参照しているため、一般的なインターネットWebサイトに接続できる環境で利用する必要があります（別途背景地図をローカルに用意すればスタンドアロン環境でも利用可能）
* 練習は[Windowsのコマンドプロンプト環境](https://ja.wikipedia.org/wiki/%E3%82%B3%E3%83%9E%E3%83%B3%E3%83%89%E3%83%97%E3%83%AD%E3%83%B3%E3%83%97%E3%83%88)をベースに行います。（[powershell](https://ja.wikipedia.org/wiki/PowerShell)でも実行可能）
  * Macintosh, Linux環境での練習については、本資料末尾の [補足説明：Linux,Macintosh環境での実行について](#補足説明：Linux,Macintosh環境での実行について) も併せて参照ください。

### サンプルデータについて
* このチュートリアルは練習用のサンプルデータとしてMaxMind社が製作したWorld Cities Databaseを使用しています。下記はその宣言です。
  * This product includes data created by MaxMind, available from http://www.maxmind.com/
* なお、チュートリアルでは日本部分のみを抽出して使用していますが、このコンバータはヒープを確保すれば全世界の変換も可能です。
  * [全世界(約317万ポイント)の変換例](https://svgmap.org/devinfo/devkddi/lvl0.1/demos/demo0.html#visibleLayer=worldcities&hiddenLayer=polygonAuthoringTester)
  (ヒープ確保量　-XMx800m)
* このチュートリアルはまた、練習用サンプルデータとして[地球地図日本](https://www.gsi.go.jp/kankyochiri/gm_jpn.html)データを使用しています。
  * 地球地図日本 ©　国土地理院

## 最初の練習  
### 環境設定
1. Java 17 の準備
   * 既にインストール済みの場合は飛ばしてください。
   * Javaには複数のディストリビューションがあります。
     * Amazon Corretto 17 : (動作確認環境 (Windows x64) )
       * https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/downloads-list.html
       * [インストール手順](https://docs.aws.amazon.com/ja_jp/corretto/latest/corretto-17-ug/windows-7-install.html)
     * OpenJDK : 
       * https://jdk.java.net/archive/
     * Redhat OpenJDK 17 : 
       * https://developers.redhat.com/products/openjdk/download
     * Microsoft Build of OpenJDK :
       * https://learn.microsoft.com/ja-jp/java/openjdk/download
     * Oracle JDK : 
       * https://www.oracle.com/jp/java/technologies/downloads/

1. ツールのダウンロード
   * [releases](https://github.com/svgmap/svgMapTools/releases)から、最新のリリースのソースコード（ Source code (zip)）と、jar（`svgMapTools-{REV}.jar`）をダウンロードします。
     * `{REV}`はリリースによって異なります。`202307`など
   * ソースコードを解凍します。アーカイブを解凍した直下のディレクトリ(`pom.xml`ファイルや`tools`ディレクトリがある)を以後ルートディレクトリと呼びます。また以降のパス表記はこのルートディレクトリからの相対パスとします<br>（ルートディレクトリ以下の構造）
      ```
      +-pom.xml
      +-src
      +-tutorials
      +-tools
      |  +-CopyDependLibs.bat
      |  +-MakeClass.bat
      |  +-...
      |
      +-...
      ```

   * ルート直下に`target`ディレクトリをつくり、そこに `svgMapTools-{REV}.jar`を投入します。
      ```
      +-target
         +svgMapTools-{REV}.jar
      ```
     
   * もしくは自分でjarを生成することもできます。
     * javacがあれば、**下記 外部ライブラリの準備後**、toolsディレクトリの`MakeClass.bat`でjarを生成できます。
     * 更にmaven環境も構築済みであれば、`pom.xml`があるアーカイブのルートディレクトリで
       * `mvn package`
       * `mvn dependency:copy-dependencies`<br>
       でも構築できます。詳しくは[readMeFirstJA.md](../readMeFirstJA.md)を参照
   
1. 外部ライブラリの準備
    * `svgmaptools`が使用する外部ライブラリ([javacsv](https://sourceforge.net/projects/javacsv/)2.1)をダウンロードします。
       * `javacsv2.1.zip` を https://sourceforge.net/projects/javacsv/ からダウンロードする
    * `svgmaptools`が使用する外部ライブラリ([geotools](https://www.geotools.org/)28.4)をダウンロードします。
       * `geotools-28.4-bin.zip` を https://sourceforge.net/projects/geotools/files/GeoTools%2028%20Releases/28.4/ からダウンロードする
       * sourceforgeサイトの目立つダウンロードボタンは最新版のダウンロードとなってしまうので、`geotools-28.4-bin.zip`を間違わずダウンロードしてください。
    * `tools`ディレクトリ下に二つのzipファイルを解凍し、以下の構成になるように`javacsv2.1`及び`geotools-28.4`ディレクトリを作成します。<br>
    (解凍・保存後のルートディレクトリ以下の構造)
      ```
      +-pom.xml
      +-src
      +-target
      +-tools
      |  +-javacsv2.1
      |  | +-javacsv.jar
      |  | +-...
      |  |
      |  +-geotools-28.4
      |  | +-lib
      |  |   +-*.jar
      |  |   +-...
      |  |
      |  +-CopyDependLibs.bat
      |  +-MakeClass.bat
      |  +-...
      |
      +-...
      ```

1. 作業ディレクトリ移動とライブラリの設定
    * コマンドプロンプトを開き、以下を指示します<br>
      * `cd {ルートディレクトリ}\tools\`<br>
      * `CopyDependLibs.bat`
    * `target\dependency`ディレクトリが作られ、ルートディレクトリ以下の構成が以下になれば準備完了です。
      ```
      +-target
      |  +svgMapTools-{REV}.jar
      |  +dependency
      |    +aircompressor-0.20.jar
      |    +(合計89個のjarファイル)
      |
      +-tutorials
      +-tools
      +-...
      ```


### 練習の開始

変換する対象ファイルは、`tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv` に格納されているものとして練習を進めます。

1. コマンドプロンプトを開き、toolsディレクトリにcdします。環境設定に続いて進めていれば不要
   * `cd {ルートディレクトリ}\tools\`<br>

1. csvfileを大縮尺(拡大表示)用ベクター地図に変換
   * `Shape2SVGMap.bat -poisymbol symbolTemplate.txt -micrometa2 -level 3 -limit 50 -showtile -densityControl 400 -lowresimage -charset utf-8 -linktitle 3 ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv`<br>
     * `tutorials\webApps\sample\`ディレクトリに、`JPcities_of_worldcitiespop_utf8.svg`ファイルおよび、補助の.svgファイル群が作成されます。

1. csvfileを小縮尺(縮小表示)用ラスター地図に変換
   * `Shape2ImageSVGMap.bat ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -charset utf-8 ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv #0000ff #0000ff 0 3`<br>
     * `tutorials\webApps\sample\`ディレクトリに、JPcities_of_worldcitiespop_utf8ディレクトリが作成され、その下に付随するファイル群が作成されます。(いくつかのディレクトリとpngやsvgファイル）

   ```
   +-target
   +-tutorials
   |   +webApps
   |     +SvgMapper.html
   |     +Container.svg
   |     +...(その他のファイル)
   |     +sample
   |       +JPcities_of_worldcitiespop_utf8.svg
   |       +...(その他のファイル)
   |       +JPcities_of_worldcitiespop_utf8
   |         +lvl2(ディレクトリ)
   |         +...(その他のディレクトリやファイル)
   |
   +-tools
   +-...
   ```
1. `{ルートディレクトリ}\tutorials\webApps\Container.svg`をテキストエディタで編集
   * このファイル(`Container.svg`)は、**ルートコンテナ**と呼ばれるファイルで、SVGMap.jsが表示する地図コンテンツの基本構成(どのようなレイヤーが配置されるか)が記述されたコンテンツです。ホームページとなるコンテンツ(`SvgMapper.html`)の`<div id="mapcanvas" title="Container.svg"></div>`から参照されます。
   * `<!-- Thematic Layer -->`の行の後に、生成したコンテンツのルートとなるファイルへのリンクを追加します。<br>（サンプルにはすでに追加済みですので確認のみで大丈夫です。実際の作業ではここにリンクを追加することで、レイヤーが追加されていきます。）
   * `<animation title="Cities of Japan" xlink:href="sample/JPcities_of_worldcitiespop_utf8.svg" class="poi" x="-30000" y="-30000" width="60000" height="60000" />`
   * これで地図作成完了
  
   
1. ローカルにあるWebAppsを動作させるための特別なモードのChromeを起動
    * 全てのChromeブラウザのウィンドを閉じてください。(同モードでの起動準備のため)
    * コマンドプロンプトで以下を入力
    * `start chrome --allow-file-access-from-files`


1. `..\tutorials\webApps\SvgMapper.html`　ファイルを上で起動したChromeのウィンドにドラッグアンドドロップすると、変換したデータが見られる。

### 作業内容のポイント
この練習で使用したコマンドと、そのパラメータの意味を解説します

SVGMapToolkitが備える、Quad Tree Composite Tiling([スライド](https://www.slideshare.net/totipalmate/quad-tree-composite-tiling-for-web-mapping-in-japanese)、[解説ページ](https://satakagi.github.io/mapsForWebWS2020-docs/QuadTreeCompositeTilingAndVectorTileStandard.html))は大規模な地理情報から容易に伸縮可能な地図コンテンツを生成できる技術です。要点は小縮尺(引いた地図)は軽いビットイメージで、大縮尺(拡大下地図)はインタラクティブなベクトルグラフィックスの地図に、場所に応じたタイミングで切り替えるものです。

SVGMapToolsはこれを実現するために2つのツールを使用します。

* Shape2SVGMap.bat
  * 大縮尺(拡大表示)用のベクトルグラフィックス地図を生成
  * 四分木タイリング : 地図データの密度に応じて四分木構造のタイル分割を行う
  * 四分木タイルのインデックスデータの生成　(インデックスデータもまたsvg形式で生成されます。インデックスデータはタイル番号がなく、元データと同じ名前の拡張子svgのデータです。)

* Shape2ImageSVGMap.bat
  * 小縮尺(引いた表示)用のビットイメージ地図を生成
  * Shape2SVGMapで生成されたインデックスデータを基に小縮尺用のビットイメージタイルを生成

* 入力データ形式
  * いずれのツールもいくつかのベクトルデータ形式(shapefile, CSV, GeoJSON)の地理情報をソースとして入力できます。空間参照系は緯度経度の世界測地系(WGS84もしくはJGC2000等)です。もしもこの形式以外のデータを処理したい場合はこれらの形式にデータ変換が必要です。ツールに付属している、Shape2WGS84.batは日本の旧測地系や平面直交座標系(19座標系)のデータを変換するときに使用できます。またよく知られたオープンソースのツールとして[OGR2OGR](https://gdal.org/programs/ogr2ogr.html)も活用できます。

#### コマンドラインパラメータのポイント
実習で実際に入力した各コマンドのパラメータのポイントを説明します
なお、いずれのコマンドも、**パラメータなしで起動するとヘルプ**情報が表示されますので参考にしてください。

自分で用意したデータからコンテンツを生成する場合も、まずはここで設定した値をベースに調整していくと良いでしょう。

##### Shape2SVGMap.bat
* -poisymbol symbolTemplate.txt :
* -micrometa2 : インタラクティブ操作で表示されるメタデータを各図形に埋め込み
* -level 3 : 四分木タイリングの最初の分割レベル (このレベルまであらかじめ分割された状態から四分木タイリングをスタートさせる)
* -limit 50 : 次の階層の四分木を生成する可を判断する閾値(KBytes)(コンテンツが概ねこの値以上になったら次の階層のタイリングをする)
* -showtile : Quad Tree Composite Tilingを行うことを指定 1
* -densityControl 400 : 画面上でタイルがこの値(px)を超えたら次の階層(より大縮尺用)のタイルに切り替える
* -lowresimage : Quad Tree Composite Tilingを行うことを指定 2
* -charset utf-8 : CSVの文字コードを指定
* -linktitle 3 : インタラクティブ操作で表示される吹き出しのカラムを指定
* ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv : 入力データ

##### Shape2ImageSVGMap.bat
Shape2ImageSVGMapのパラメータの与え方は少々複雑です。第一引数にはタイリングのためのインデックス情報を、第二引数以降は　ハイフン付きのパラメータを任意の数、ハイフン付き任意パラメータ群が終わった後は順番が固定された5個のパラメータを設定します
* 第一引数 
  * ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.svg : 
* ハイフン付き任意パラメータ
  * -sumUp 16 : ビットイメージタイルを生成するとき一気にまとめて生成(16を設定すると良い)
  * -antiAlias : [アンチエイリアス処理](https://ja.wikipedia.org/wiki/%E3%82%A2%E3%83%B3%E3%83%81%E3%82%A8%E3%82%A4%E3%83%AA%E3%82%A2%E3%82%B9)指定
  * -charset utf-8 : CSVの文字コードを指定
* 末尾の5個の固定パラメータ
  * ..\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv : 入力データ
  * #0000ff : 塗の色をHTMLカラーで指定 : 今回はポイントをアイコンで表示するので使用されない
  * #0000ff : 線の色 : 同上
  * 0 : 線の幅(px) : 同上
  * 3 : ポイントのサイズ(px) : 同上

## この次に行うこと
興味に応じて以下のいずれかを進めてみてください。

* 自分で用意したデータを使用して次章以降(実践・応用)に進む
* [tutorial2](tutorial2.md) では、本編で使用したCSVポイントデータ(JPcities*.csv)を使用して、より高度な可視化を行う練習をします
* [tutorial3](tutorial3.md) では、別途同梱してあるいくつかの形式(ライン・ポリゴン、shapefile・GeoJSON)のベクトル地理情報を使用した可視化を練習します
* 作成した地図コンテンツをインターネット上で公開
   * webAppsディレクトリ ( `{ルートディレクトリ}\tutorials\webApps` ) 以下をそのままウェブサーバ上にコピーすることで、`{公開するサーバのディレクトリのURL}/webApps/SvgMapper.html`　で地図コンテンツを公開できます。
   * なお、公開するサーバ上のディレクトリから、csvデータ(`{ルートディレクトリ}\tutorials\webApps\sample\JPcities_of_worldcitiespop_utf8.csv`) は消してしまって構いません。


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
       * 漢字が入っている場合、シフトJISにします。(Windowsの場合（OS標準文字コード）　オプションによってUTF-8テキストを使用することも可能)
       * 最初の行には項目名がカンマ区切りで入っている必要があります。（無い場合はテキストエディタで編集挿入します。なお、スキーマファイルを与えることで項目名が無いままでの変換も可能です。）
       * 緯度経度のカラムの項目名は、`latitude`, `longitude`　となっている必要があります。
       * 緯度経度のカラムは、以降のカラム指定番号から外して考えます。
       * そのうえで、1カラム目が最初のカラムとなります。

1. 大縮尺(拡大表示)用ベクトル地図を生成（データサイズによって時間がかかります）
   * コマンドプロンプトを起動し `tools`ディレクトリにcd
   * `Shape2SVGMap.bat -poisymbol symbolTemplate.txt -micrometa2 -level 3 -limit 50 -showtile -densityControl 400 -lowresimage ..\tutorials\webApps\(wdir)\(wfile).csv`
     * 同ディレクトリに、JPcities_of_worldcitiespop_utf8.svgファイルおよび、補助の.svgファイル群が作成されます。
     
1. 小縮尺(縮小表示)用ラスター地図を生成（データサイズによって時間がかかります）
   * `Shape2ImageSVGMap.bat ..\tutorials\webApps\(wdir)\(wfile).svg -sumUp 16 -antiAlias ..\tutorials\webApps\(wdir)\(wfile).csv #0000ff #0000ff 0 3`
     * 同ディレクトリに、(wfile)ディレクトリが作成され、その下に補助ファイル群が作成されます。(いくつかのディレクトリとpngやsvgファイル）

1. ルートコンテナファイル`tutorials\webApps\Container.svg`　を編集
   * `<!-- Thematic Layer -->`の行の後に、以下のタグ<br>
   `<animation title="(CONTENT Title)" xlink:href="(wdir)/(wfile).svg" class="poi" x="-30000" y="-30000" width="60000" height="60000" />`
を追加
     * (CONTENT Title)は、何でも良いですが、半角英数を推奨します。(漢字の場合UTF-8です)
   * これで地図作成完了

1. `tutorials\webApps\SvgMapper.html`　をローカルWebApp起動モードのChromeで開く(ドラッグアンドドロップする)と変換したデータが見られます。
   * Note:<br> ウェブホストに`tutorials\webApps`以下を配置すれば、そのコンテンツのURL(ウェブホストのディレクトリのURL/SvgMapper.html)で通常のブラウザで表示することができます。<br>
   生成されたコンテンツは全て静的なものですので、一般的な静的ホスティングサービスで配信できます。

## 応用：複数のデータの合成(レイヤー合成)
* 複数のデータ(レイヤー)を別のディレクトリに作成し、それらを地図上でに合成したり、切り替えたりできるようにすします（レイヤリング機能）<br>
  * 実践編の作業を別のディレクトリ名を作って複数回実施
  * ルートコンテナには、追加したいレイヤー分のタグを追加
  * これでデフォルトですべてのレイヤーが表示された状態となります。

* 大縮尺表示用ピンを変更<br>
　`tools\icons\mappins\`の適当なpngを作業ディレクトリにコピーしたうえで、mappin.pngにリネームします。<br>自分の好みのアイコン(pngやjpg形式)を使用することも可能です。アイコンのサイズ・アスペクト比もsymbolTemplate.txtを編集して変更することができます。

* 小縮尺表示用の丸点の色を変更<br>
　Shape2ImageSVGMapコマンドの、以下の(色コード)部分を変更して実行します<br>
　`java -Xmx500m Shape2ImageSVGMap ..\tutorials\webApps\(wdir)\(wfile).svg -sumUp 8 -antiAlias ..\tutorials\webApps\(wdir)\(wfile).shp (Fill Color) (Stroke Color) 0 3`
  * (Fill Color) (Stroke Color)はPOIの色コードで、ひとまずは両方とも同じ値で良いでしょう。色コードはWebの色コードで#RRGGBB (RR,GG,BBそれぞれ00-FF)です。
　
* 初期状態で表示させたくないレイヤーの設定<br>
ルートコンテナファイルのタグ編集において、`visibilty="hidden"`属性を付けます
  * `<animation title="Cities of Japan" xlink:href="(wdir)/(wfile).svg" class="poi" x="-30000" y="-30000" width="60000" height="60000" visibilty="hidden"/>`

* どれかのレイヤーだけを表示（択一表示）<br>
ルートコンテナファイルのタグ編集において、デフォルトで表示させたいデータ以外に`visibilty="hidden"`属性を付けたうえで、全てのレイヤーのタグのclass属性を`class="poi switch"`に変更します。
  * `<animation title="Cities of Japan" xlink:href="(wdir)/(wfile).svg" class="poi switch" x="-30000" y="-30000" width="60000" height="60000" visibilty="hidden"/>`

## さらに応用：ツールの複雑な使い方など
* 以下のツールは、オプションなしで起動することでヘルプメッセージが出力され、詳細なオプションの説明が提供されます。
  * `Shape2SVGMap.bat`
  * `Shape2ImageSVGMap.bat`
  * `Shape2WGS84.bat`
  * `HyperBuilder.bat`
* 点の個数は１０００万点規模まで動作検証しています。
* このチュートリアルでは、点オブジェクトのみの地図化を実行したが、shapefileを用いることで、線や面の（ベクトルタイル）地図化にも対応できます。
* 複数のタイリング手法のサポート
  * 均一に分割されたタイリング
  * なるべく同程度のファイルサイズとなるように四分木で調整されたタイリング（Quad Tree Tiling：本チュートリアルで使用）


## 補足説明：Linux,Macintosh環境での実行について
Linux(Ubuntu等), Macintosh等のUNIX系の環境で、本チュートリアルを実施するときの留意点・相違点を記載します

### 環境設定
* Java
  * Java17もしくはJDK17を使用してください。 [OpenJDK](https://jdk.java.net/archive/)で動作します。(Java21でも動作確認)
* ツール
  * 特に注意する点はありません。zipの解凍に必要なソフトウェアを別途用意する必要がある場合があります。
* 外部ライブラリ
  * 特に注意する点はありません。
* ライブラリの設定
  * `CopyDependLibs.sh` を使用します
* 自分でjarを生成する場合
  * mavenを使用する場合は特に違いはありません
  * mavenを使用しない場合は、`MakeClass.sh` を使います

### 練習
* バッチファイルの代わりにシェルスクリプトを使用します (`Shape2SVGMap.sh`, `Shape2ImageSVGMap.sh`) パラメータの与え方は以下の若干の相違点を除き同じです。
* ソースファイル、コンテンツファイルを指定するパスのセパレータがバックスラッシュ(`\`)ではなくスラッシュ(`/`)を使用します。
* 色を指定するパラメータ部をダブルクオーテーション(`"`)で囲む必要があります。
* OSデフォルトの漢字コードが異なりますので、CSVデータの文字コードがUTF-8になっていることを確認してください。(むしろSJISがデフォルトのwindowsのほうが誤りが起きやすい)
* 以降のチュートリアルについても同様です。

1. csvfileを大縮尺(拡大表示)用ベクター地図に変換
   * コマンドは以下のようになります。
   * `./Shape2SVGMap.sh -poisymbol symbolTemplate.txt -micrometa2 -level 3 -limit 50 -showtile -densityControl 400 -lowresimage -charset utf-8 -linktitle 3 ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.csv`<br>

1. csvfileを小縮尺(縮小表示)用ラスター地図に変換
   * コマンドは以下のようになります。
   * `./Shape2ImageSVGMap.sh ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.svg -sumUp 16 -antiAlias -charset utf-8 ../tutorials/webApps/sample/JPcities_of_worldcitiespop_utf8.csv "#0000ff" "#0000ff" 0 3`<br>

1. `{ルートディレクトリ}/tutorials/webApps/Container.svg`をテキストエディタで編集
   * 特に注意すべき相違点はありません

1. ローカルにあるWebAppsを動作させるための特別なモードのChromeを起動
   * 起動方法が異なります。あらかじめ起動中のChromeを終了させておく必要があります
   * MacOS:  `open -a "Google Chrome" --args --allow-file-access-from-files`
   * Linux: `google-chrome --allow-file-access-from-files`


<br><br>
以上
