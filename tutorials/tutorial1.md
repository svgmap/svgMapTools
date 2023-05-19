# Tutorial1 
CSVデータからインタラクティブ地図を作製する方法のチュートリアル
> 2014.03.03 1st rev. By Satoru Takagi<br>
> 2017.12.07 OSS化に対応した更新<br>
> 2018.01.11 markdown化 <br>
> 2018.06.01 SVGMap*.jsを更新、-Xmx追加<br>
> 2023.05.19 mavenベースのパッケージに移行。SVGMap*.jsをCDN経由に変更

## Notes:
### 実行環境について
* データの作成のために、Java 8 (jre 8もしくはjdk 8) がインストールされている環境が必要です。
  * なお、動作チェックはWindows11とJAVA8で行っています。(JAVA10(JAVA9以降)では動作しません。使用しているGeotoolsがJAVA10非互換のため。なおJavaはOracle版だけではなくOpenJDK (Corretto等)でも動作確認しています。)
* 生成したコンテンツはほとんどのウェブブラウザで利用可能です。<BR>ただしローカルファイルでの動作確認には制約があります。以下に記載します。<BR>(なお、Webサーバ上にコンテンツを設置した場合は、制限なく表示できます。)
  * ローカルに保存したコンテンツでは、Chromeで `--allow-file-access-from-files` オプションをつけて起動した場合のみ表示できます。以下起動例（２例）（ショートカットを作成すると良い）
    * `"C:\Program Files\Google\Chrome\Application\chrome.exe" --allow-file-access-from-files`
    * `start chrome --allow-file-access-from-files`
      * 既にChromeが起動している場合は無効。すべてのChromeをいったん終了させてから起動する必要があります。
      * 同オプションの参照情報([Chdromium公式ドキュメント](https://www.chromium.org/developers/how-tos/run-chromium-with-flags/)から[参照されているオプション情報](https://peter.sh/experiments/chromium-command-line-switches/#allow-file-access-from-files))
  * microsoft Edgeでも同様に動作します。(Chromeと同じChromeiumベースのブラウザのため)
    * `start msedge --allow-file-access-from-files`
* 生成したコンテンツは、背景地図としてインターネット上のコンテンツ(OpenStreetMapや電子国土)を参照しているため、一般的なインターネットWebサイトに接続できる環境で利用する必要があります（別途背景地図をローカルに用意すればスタンドアロン環境でも利用可能）
* 練習は[Windowsのコマンドプロンプト環境](https://ja.wikipedia.org/wiki/%E3%82%B3%E3%83%9E%E3%83%B3%E3%83%89%E3%83%97%E3%83%AD%E3%83%B3%E3%83%97%E3%83%88)をベースに行います。（[powershell](https://ja.wikipedia.org/wiki/PowerShell)でも実行可能）

### サンプルデータについて
* このチュートリアルは練習用のサンプルデータとしてMaxMind社が製作したWorld Cities Databaseを使用しています。下記はその宣言です。
  * This product includes data created by MaxMind, available from http://www.maxmind.com/
* なお、チュートリアルでは日本部分のみを抽出して使用していますが、このコンバータはヒープを確保すれば全世界の変換も可能です。
  * [全世界(約317万ポイント)の変換例](https://svgmap.org/devinfo/devkddi/lvl0.1/demos/demo0.html#visibleLayer=worldcities&hiddenLayer=polygonAuthoringTester)
  (ヒープ確保量　-XMx800m)
<!--
* このチュートリアルはまた、練習用サンプルデータとして[地球地図日本](https://www.gsi.go.jp/kankyochiri/gm_jpn.html)データを使用しています。
  * 地球地図日本 ©　国土地理院
-->

## 最初の練習  
### 環境設定
1. Java 8 の準備
   * 既にインストール済みの場合は飛ばしてください。
   * Javaには複数のディストリビューションがあります。
     * Amazon Corretto 8 : (動作確認環境 (Windows x64) )
       * https://docs.aws.amazon.com/ja_jp/corretto/latest/corretto-8-ug/downloads-list.html
       * [インストール手順](https://docs.aws.amazon.com/ja_jp/corretto/latest/corretto-8-ug/windows-7-install.html)
     * Redhat OpenJDK 8 : 
       * https://developers.redhat.com/products/openjdk/download
     * Oracle JDK : 
       * https://www.oracle.com/java/technologies/downloads/#java8-windows

1. ツールのダウンロード
   * [releases](https://github.com/svgmap/svgMapTools/releases)から、最新のリリースのソースコード（ Source code (zip)）と、jar（`svgMapTools-{REV}.jar`）をダウンロードします。
     * `{REV}`はリリースによって異なります。`202305`など
   * ソースコードを解凍します。アーカイブを解凍した直下のディレクトリ(`pom.xml`ファイルや`tools`ディレクトリがある)を以後ルートディレクトリと呼びます。また以降のパス表記はこのルートディレクトリからの相対パスとします<br>（ルートディレクトリ以下の構造）
      ```
      +-pom.xml
      +-src
      +-target
      +-tutorials
      +-tools
      |  +-CopyDependLibs.bat
      |  +-MakeClass.bat
      |  +-...
      |
      +-...
      ```

   * ルート直下の`target`ディレクトリに `svgMapTools-{REV}.jar`を投入します。
      ```
      +-target
         +svgMapTools-{REV}.jar
      ```
     
   * もしくは自分でjarを生成することもできます。
     * javacがあれば、**下記 外部ライブラリの準備後**、toolsディレクトリの`MakeClass.bat`でjarを生成できます。
     * 更にmaven環境も構築済みであれば、`pom.xml`があるアーカイブのルートディレクトリで
       * `mvn release`
       * `mvn dependency:copy-dependencies`<br>
       でも構築できます。詳しくは[readMeFirstJA.md](../readMeFirstJA.md)を参照
   
1. 外部ライブラリの準備
    * `svgmaptools`が使用する外部ライブラリ([geotools](https://www.geotools.org/)9.5)をダウンロードします。
       * `geotools-9.5-bin.zip` を https://sourceforge.net/projects/geotools/files/GeoTools%209%20Releases/9.5/ からダウンロード
    * `tools`ディレクトリ下に`geotools-9.5`ディレクトリを用意し、ここにzipを解凍した内容のjarファイル群を全て保存します。<br>
    geotools9.5のjarファイルは(geotools-9.5-bin.zip)にパックされており、解凍すると複数のjarに分かれています。これらのjarファイルをすべて`tools/geotools-9.5`下に投入してください。<br>
    (解凍・保存後のルートディレクトリ以下の構造)
      ```
      +-pom.xml
      +-target
      |  +svgMapTools-{REV}.jar
      +-tutorials
      +-tools
      |  +-geotools-9.5
      |  |  +-batik-transcoder-1.7.jar
      |  |  +-gt-main-9.5.jar
      |  |  +-(たくさんのjarファイル)..
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
      |    +commons-pool-1.5.4.jar
      |    +(合計28個のjarファイル)
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
<br><br>
以上
