# SVG Map Toolsについて

SVG Map Toolsは、Shapefileやgeojson,緯度経度を持ったCSVファイルからSVGMapコンテンツを生成するツールです。
Javaで構築されており、Java 8 (OracleもしくはOpenJDK)が動作する環境が必要です。主にWindowsおよびCentOSで動作確認しています。

コンテンツは、データの密度に応じて、ラスターとベクターを混合した[四分木タイル](https://www.slideshare.net/totipalmate/tiling-51301496)としてとして生成することができます。
これにより、[この例のような](http://svgmap.org/devinfo/devkddi/lvl0.1/rev14/SVGMapper_r14.html#visibleLayer=worldcities&hiddenLayer=polygonAuthoringTester)数百万件以上のデータの地図コンテンツを構築できます。


現在のところ以下の４つのモジュールで構成されています。このうちShape2SVGMapとShape2ImageSVGMapが基本となるモジュールです。

SVGMapの概要・どのようなコンテンツが表示できるかは、http://svgmap.org/ を参照ください。

* Shape2SVGMap: CSV,geojsonもしくはShapefile形式のデータを、ベクトルデータのSVGMapコンテンツに変換します。
* Shape2ImageSVGMap: CSV,geojsonもしくはShapefile形式のデータを、ビットイメージのSVGMapコンテンツに変換します。
* Shape2WGS84:  測地系変換や属性に応じたファイル分割、Shapefile<>CSV変換などを行うプリプロセッサ
* HyperBuilder: 複数のSVGMapレイヤーを統合するコンテナデータを生成

Shape2SVGMapとShape2ImageSVGMapを組み合わせると、SVGMapで特徴的な[Quad Tree Composite Tiling](https://satakagi.github.io/mapsForWebWS2020-docs/QuadTreeCompositeTilingAndVectorTileStandard.html)コンテンツを生成することもできます

先ず、[tutorial](tutorial)ディレクトリの内容を用いて、基本モジュール（Shape2SVGMap、Shape2ImageSVGMap）の基本操作を学習し、処理の流れを理解してから使用してください。なお、tutorialsの内容はWindows用に調整されています。Linuxなどで使う場合は適宜翻訳して利用してください。

## 環境の設定
JDK (Java 8)は、環境変数PATHやJAVA_HOMEを含めて設定する必要があります。

基本的にはmavenを使用してビルドします。mavenが用意されていない場合は以下を参照ください。なお、mavenなしのセットアップも可能です。そのWindows用の方法は後ほど紹介します。

#### maven setup
##### Linux:
* `sudo apt install maven`
##### Windows:
* [こちらを参照](https://maven.apache.org/guides/getting-started/windows-prerequisites.html)
  * [こちらの日本語記事](https://qiita.com/Junichi_M_/items/20daee936cd0c03c3115)も参考になります

### Build package
リポジトリをクローンした場合は、pom.xmlのあるディレクトリに移動(cd)します。
* `mvn package`
* `mvn dependency:copy-dependencies` (Optional)

### mavenなしでのセットアップ (for Windows)
* geotools-9.5-bin.zip を https://sourceforge.net/projects/geotools/files/GeoTools%209%20Releases/9.5/ からダウンロードする
* 解凍して、以下のようにgeotools-9.5ディレクトリをtoolsディレクトリにコピーしてください。
```
+-pom.xml
+-src
+-target
+-tools
|  +-geotools-9.5
|  |  +-batik-transcoder-1.7.jar
|  |  +-*.jar
|  |  +-...
|  |
|  +-CopyDependLibs.bat
|  +-MakeClass.bat
|  +-...
|
+-...
```
* `cd tools`
* `CopyDependLibs.bat`
* ソースコードからコンパイルする場合
  * `MakeClass.bat`
* または、 [releases](https://github.com/svgmap/svgMapTools/releases)に登録されているコンパイル済みのjarファイルを使用する場合。
  * 以下のように、jarファイルを`target`ディレクトリにコピーします。

```
+-pom.xml
+-src
+-target
   +dependency
   | +*.jar
   |
   +svgMapTools-{REV}.jar
```

## 使用方法

* `java -Xmx800m -classpath target\dependency\*;target\svgMapTools-{REV}.jar org.svgmap.shape2svgmap.MainWrapper Shape2SVGMap (options)`
* `java -Xmx800m -classpath target\dependency\*;target\svgMapTools-{REV}.jar org.svgmap.shape2svgmap.MainWrapper Shape2ImageSVGMap (options)`
* `java  -classpath target\dependency\*;target\svgMapTools-{REV}.jar org.svgmap.shape2svgmap.MainWrapper Shape2WGS84 (options)`
* `java -classpath target\dependency\*;target\svgMapTools-{REV}.jar org.svgmap.shape2svgmap.MainWrapper HyperBuilder (options)`

  * *`{REV}`は "202305 "などのリリースごとに異なる数字*
  * *linux: s/;/:/*

### mavenで生成された依存ライブラリをパッケージ化したjarを利用する場合
* `java -Xmx800m -jar sibasisvgMapTools-{REV}-jar-with-dependencies.jar Shape2SVGMap (options)`
* `java -Xmx800m -jar svgMapTools-{REV}-jar-with-dependencies.jar Shape2ImageSVGMap (options)`
* `java -jar svgMapTools-{REV}-jar-with-dependencies.jar Shape2WGS84 (options)`
* `java -jar svgMapTools-{REV}-jar-with-dependencies.jar HyperBuilder (options)`

特にShape2ImageSVGMapとShape2SVGMapはヒープを多く消費しますので、-Xmxオプションは適宜設定してください。

### ショートカット
各機能のショートカット .bat または .sh ファイルが `tools` ディレクトリに格納されています。
* Shape2SVGMap.bat, Shape2SVGMap.sh
* Shape2ImageSVGMap.bat, Shape2ImageSVGMap.sh
* Shape2WGS84.bat, Shape2WGS84.sh
* HyperBuilder.bat, HyperBuilder.sh

Shape2WGS84を用いて、日本測地系(TOKYO Datum)から世界測地系への測地系変換を行いたい場合、カレントディレクトリに国土地理院が公開する変換パラメータデータ["TKY2JGD.par"(Ver2.1.2)](http://www.gsi.go.jp/sokuchikijun/tky2jgd_download.html)　をこのファイル名で配置してください。


ライセンスについて
本ツールは[GPL Ver.3](LICENSE)に基づくオープンソースソフトウェアです。
