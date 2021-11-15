package org.svgmap.shape2svgmap;

// Shape��SVG Map�ɕϊ�����\�t�g�E�F�A�ł��B
// Copyright 2007 - 2018 by Satoru Takagi
// 
// geoTools2.7.5 �œ���m�F
//
// 2007.02.26 The First Version
// �������ʐ}�@�̕W���ܐ��w��
// SVG�����w��
// XY�n->�ܓx�o�x�ϊ�
// �}�`�F�w��
// 2007.04.02 RDF/XML�o�͋@�\�ǉ�
// 2007.04.27
// 2007.05.17 �^�C�������V�X�e��������������
// 2007.07.09 �œK���^�C�����O�@
// 2007.10.24 ��������}��(�}�`��BBOX���g���ă��t�ȃN���b�v�����邱�Ƃŏ������y��)
// 2007.11.1 �X�Ȃ鍂����
// 2007.12.10- microMetadata�d�l�Anull Tile�����Acontainer�K�w���AVector Effect�AFontAutoSizing�@�Ȃǂ̃R�[�f�B���O���J�n�������O�O
// 2007.12.13 null Tile����(���͑g��ł��������o�O���o�Ă���), FontAutoSizing
// 2007.12.14 microMetadata
// 2008.04.22 �f�[�^�̏d���`�F�b�N(�C�ӂ�metadata��ID�ԍ��ɂ��)�@�\
// 2009.01.25 �X�^�C���̂܂Ƃߋ@�\������(useDefaultStyle�t���O) > SVG�̃h�L�������g�\�����傫���ς��̂ł��Ȃ�낤���@�\�����ǁE�E
// 2009.02.06 �f�[�^�̏d���`�F�b�N(�C�ӂ�metadata��ID�ԍ��ɂ��)�@�\
// 2009.08.12 ���̎����܂łɐF�X�ǉ�  GUI���b�p�[(Shape2SVGMapGUI) tky2jgd�ɂ�鍂���x�ϊ��A�X�g���[�N���Œ�op.�ASJIS�ȊO�̕����R�[�h��
// 2009.09.08 �V���{���̃T�C�Y���Œ肷��I�v�V�����A�w�肵���ԍ��̃��^�f�[�^�݂̂��o��
// 2010.06.08 �L���v�V�����̕\����K����(�}�`�̃X�^�C�������������Ă�������fix)
// 2010.08.19 geoTools2.6.5�Ή��̂��߂̉��C(���\��|����B�����炭����݊����͖���)
// 2010.10.19 SVG1.2Tiny+SVG MapJIS�Ή����f�t�H���g�ɁB crs��transform�P�O�O���f�t�H���g��
// 2010.12.28 parse�֐��𐴑|�A�����f�t�H���g��fixedwidth=1��
// 2011.12.16 �F�e�[�u������̐ݒ���ڍׂɂł���悤�ɂ���(numcolor��iHSV�ǉ��Anumrange�ǉ�)
// 2012.04.05 POI(Point)����̕\���͊g�[
// 2012.04.10 �^�C���̖��x�ɉ������\������@�\���g�[(densityControl=dd[pix/TILE])
// 2012.07.30 POI�̉��C�ɒ��� putPoiShape() <=vectorPoiShapes���g�� ���̂Ƃ���Adefault�����AvectorEffect���܂��܂Ƃ�����Ȃ��B����ɁA�`��͈̔̓`�F�b�N���Ȃ��B����ɁA�F�e�[�u���̖������ł��Ȃ��B
// 2013.02.18 ������̗񋓌^�̏���F�e�[�u���Ɋ��蓖�Ă� useColorKeys
// 2013.03.11 SVGMapLvl0�p��POI����(micrometa2)�@�O������POI symbol�̃e���v���[�g��ǂݍ��ދ@�\�ȂǑ�g��
// 2013.03.28 xml�G�X�P�[�v(for metadata)
// 2013.08.06 �K�w�R���e�i�̃f�[�^�E�����\���𔲖{�I�Ɏ蒼��(�P����)
// 2013.10.21 POI�̃V���{��ID���f�[�^����w��ł���悤�ɂ����@�������A���̐����̂�
// 2014.02.14 �q�[�v�������ɂ���e�ʃf�[�^�̏������\�ɂ���(�啝�ȃA�[�L�e�N�`���ύX�����{)
// 2014.04.25 -poisymbolidnumber
// 2014.05.12 �V���{���T�C�Y���Œ�łȂ��΂���
// 2015.04.17 -colorkey���������P
// 2016.02.05 -colorkey���X�ɏ������P
// 2016.02.26 poi�V���{���̃T�C�Y���ׂ����ݒ�ł���I�v�V�����̊g���@����directpoi�ɑ΂���
// 2016.03.23 --- �}���`�X���b�h�����J�n
// 2016.04.03 rev19a (��ꎟ)�}���`�X���b�h���`�������W:FeatureIterator���X���b�h�����ғ���������@�E�E�������o�����s (���̂ɂ���Ă͂R���Ƃ��x���Ȃ���)
// 2016.04.07 rev19b �}���`�X���b�h�����`�������W��:
// 2016.04.08 15%���x�̐��\������m�F�B���̕��@�iFeatureIterator�P��ɂ��A�o�b�t�@�����O���Ȃ���W�I���g�����Ƃ̃~�N���ȃX���b�h�𗧂āA�~�N���ɓ�������������@�j���̗p���ĊJ���𑱍s����B�i�����_�ł͂܂���ʂ̃o�O����j	
// 2016.04.08 actualGenerateMeshLevel�I�v�V���� (18�ɐ�s������������)
// 2016/08/02 �O���t�@�C������I�v�V�����ǂ߂�@�\�ǉ�
// 2017.04.05 CSV�Ή��FCSVDataStore������
// 2017.09.15 �V���{���̎Q��ID�Ɍ��f�[�^�̕�����J�����̒l��ݒ�\�ɁB�����ĂQ�̃J�����̉��Z(Int,String)�l���ݒ�\��
// 2017.11.10 package��
// 2017.12.27 OSS���@github�o�^
// 2018.01.26 �����̐F�֌W�̊֐����폜���ASVGMapGetColorUtil(�R���v����Shape2ImageSVGMap�Ŏg�p��)���g�p
// 2018.09.21 csv���͂ł̒P����lineString,polygon�f�[�^�Ή�
// 2019.01.24 -layermeta
// 2019.06.14 WKT�ŃG���R�[�h���ꂽgeometry�̓�����CSV�ɑΉ�
// 
// BUG 130806�̎蒼���ɂ�菉����������1x1�̊K�w�I�f�[�^�������ł��Ȃ��o�O���ł��Ă���Bglobal level tiling�����{����Ƃ�(�������x���̎��̏����^�C����������1x1�ɂȂ�ꍇ)�ɑ傫�Ȗ�肪����B(�f�[�^�����Ɏ��s����) (2014.03 �m�F 1420,1439�s������H)  ���{�����̃f�[�^��-level 0 -limit 100 �Ƃ��w�肷��ƁAlimit�����őł��؂��������̃��[�g�f�[�^�𐶐����Ă��܂��A���̏�Ń^�C���������n�߂Ă��܂��ˁE�E 2017.4.19 �^�C���͐����ł���悤�ɂȂ��Ă��邪�A�R���e�i��Level0�̃^�C���̃t�@�C���������B
//
// ISSUES:
//   geotools�̑Ή����r�W�������グ��E�E


import java.io.*;
import java.io.IOException;
import java.util.*;
import java.lang.*;
import java.net.URL;
import java.net.URI;
import java.text.NumberFormat ;
import java.text.DecimalFormat;
import java.util.regex.*;

import java.lang.management.ManagementFactory; // �q�[�v�Ď��p 2014.02
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
// import net.sourceforge.sizeof.SizeOf;


import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.*;
import org.opengis.feature.simple.*;

import java.awt.geom.*;

import org.geotools.data.Transaction;
import org.geotools.filter.*;
// import org.geotools.data.vpf.*;
import org.geotools.geometry.jts.* ;
import com.vividsolutions.jts.geom.*;

import com.vividsolutions.jts.simplify.*;

import com.vividsolutions.jts.operation.linemerge.LineMerger;

// import java.security.*;

// for CSV Reader/Exporter 2017.4.3
import org.svgmap.shape2svgmap.cds.CSVDataStore;
import org.svgmap.shape2svgmap.cds.CSVFeatureReader;
import org.svgmap.shape2svgmap.cds.CSVFeatureSource;
import org.geotools.data.store.ContentDataStore;
// import org.geotools.data.AbstractFileDataStore;

// use Executor 2017.4.19
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Shape2SVGMap19 {
	NumberFormat nFmt;
	DecimalFormat tzformat = new DecimalFormat("0.###########");
//	GeoConverter gconv;
	SvgMapAffineTransformer smat;
	int lvl;
	int xySys = 0;
	int datum = GeoConverter.JGD2000;
//	HashMap<Object,String> colorMap; // 2018.1.26 SVGMapGetColorUtil(���L)�Ɉڍs
	SVGMapGetColorUtil colorUtil;
	String mainColor = "green"; // �����͓h��̐F
	String outlineColor = ""; // �|���S���̗֊s�F
	double opacity = 0.5; // �����x 
	double POIsize = -1; // 2017/7/14 directpoi����poiSizeCol���w�肳��Ă���Ƃ��������̒l�����ɓ��I��POI�̃T�C�Y���ݒ肳���(-1:�f�t�H���g�T�C�Y(putPoiShape))
	
	int layerCol = -1;
	
	int captionFlg = -1;
	int dupCheck = -1;
	int linkFlg = -1;
	int hrefFlg = -1; // 2016.10.31 �A���J�[�͖{��href���Ȃ��Ɩ��Ӗ��Ȃ̂ł��̕ӂ̉��C (���������̂Ƃ���href�g���Ȃ�)
//	TreeMap metaIndex = new TreeMap();
	LinkedHashMap<Integer,String> metaIndex = new LinkedHashMap<Integer,String>(); // �w�菇�ŕ��ׂ��ق����ǂ���������Ȃ�(2013.3.11)
	Integer[] linkedMetaIndex;
	String[] linkedMetaName;
//	boolean noMetaId = false; // ID������Ɣj�]����炵���H 2017.4.17 svgmaptilestm�Ń}���`�X���b�h������ID���X���b�h�ԏd�����N�����E�E
	boolean noMetaId = true;
	
	double strokeWidth = 0.0; // SVG���W�n�ł̐���(���낢��ȏ������������Đݒ肳���) [m]�l�������ݒ肷��ƕϊ��ݒ�
	double defaultFontSize = 0.0; // �K���ɉ������Đݒ肳���E�E
	
	boolean vectorEffect = false; // �g�債�Ă��g�債�Ȃ������g���Ƃ�true(strokeWidth�Ƒg�ł�)
	
	
	// 2012/12/28 parse�𐮗����ďo�Ă��������ݒ�ϐ��Q
	boolean outGroup = false; // SVG�̃O���[�v�^�O���o�̓t���O�i�������A�s���S�E��d�����E�E�E�j
	double projCenter = 0; // ��ܐ�
	double boundHeight = -100; // SVG�f�[�^�̍��� ( "-"�l�̏ꍇ�́A�n�����W��(-boundHeight)�{ )
	String colorProp = ""; // ���C���̐F #nnnnnn:RGB�w�� �������͑�����: �����ԍ�
	String olColorProp = ""; // �֊s���̐F
	String symbolProp = ""; // �V���{��(POI�p)�̐F
	String dupProp = ""; // �d���}�`�̗}�� ������: �����ԍ�(���̑����̒l�������Ƃ��}��)
	String linkProp = ""; // �n�C�p�[�����N�ݒu�@�������F�����ԍ�
	boolean metaEmbed = false; // ���^�f�[�^(RDF/XML) �p�~�E�E
	boolean microMetaEmbed = false; // �}�C�N�����^�f�[�^ to be obsoluted...
	boolean microMeta2Embed = false; // �}�C�N�����^�f�[�^2
	double accuracy = 1.0; // ���x : �f�[�^���l�̌����E�@ ��������Ő��̑��������肳���
	int meshPart = 0; // ���b�V��������
	int meshLevel = -1; // >=0�ŁAquad tree���b�V��������enable�� 2013.3.28 : ���Ӂ@���ۂ̕������x���� meshLevel + level �ɂȂ��Ă���
	int actualGenerateMeshLevel = -1; // ���ۂɃO���t�B�b�N�X����������郁�b�V�����x��(���̏�܂ł̓J���̃R���e�i�ɂ���)
	int maxLevel = 20; // 2017.4.19 -level�I�v�V�������̍ő啪�����x��(Global Q-tree level)
	String crstProp = ""; // CRS��transform�l�𒼐ڎw�肷��Ƃ�
	int limitProp = -1; // �^�C���̍ő�T�C�Y(KByte)
	String layerProp = ""; // ���C�� ������: �����ԍ�(���̑����̒l�������Ƃ��������C��)
	double simplifyParam = -10.0; // -10�̂Ƃ��͒P�������Ȃ�
	double fixedFont = 0.0; // �T�C�Y�̌Œ�[px] 0:fix���Ȃ� 
	double fixedStroke = 0.75; // �T�C�Y�̌Œ�[px] 0:fix���Ȃ� (�}�C�i�X�l:�w�肵�������ԍ��̒l��fix)
	double fixedSymbol = 6.0; // �T�C�Y�̌Œ�[px] 0:fix���Ȃ� (�}�C�i�X�l�F�x�P�ʂŌŒ�)
	double fixedSymbolSub = 0.0; // �T�C�Y�̌Œ�̂��߂̃T�u�p�����[�^
	int directPoi = 0; // �V���{�����Adefs-use���g�킸�A���`���i����2 2012/07/30) "0"��disable (-)��-POI�V���{���ԍ�, (+)attrNo��attrNo�̒l�ɂ����directPOI�V���{���ԍ���ݒ�
	int customPoiType = -1; // ��L�A�V���{����ID�ԍ���poiSymbolIdNumber�ŃJ�X�^���w��
	int poiColumn = -1; // poi�̃V���{���w��̂��߂̑����ԍ�(2013/10/21)
	int poiColumn2 = -1; // poi�̃V���{���w��̂��߂̓�ڂ̑����ԍ�
	
	String symbolTemplate = ""; // 2013.3.11 �V���{�����O���e���v���[�g�t�@�C������ݒ肷��
	
	boolean noShape = false; // �}�`���o���Ȃ�
	boolean mergeProp = false; // ���C���}�[�W
	boolean showHead = false; // �w�b�_�̕\��
	int densityControl = 0; // �^�C�����x�ɉ������\������ [px/TILE]
	int bitimageGlobalTileLevel = -1; // ���x��densityControl�\���̂��߂̎��ʕϐ� 2013.3.28(globalTiling�ł̂݋@�\����)
	
	
	int colorCol = -1;
	int olColorCol = -1;
	int strokeWCol = -1;
	int POIsizeCol = -1; // 2017/7/14
	int pStep = 0;
	Class colorClass , olColorClass, sizeClass;
	
	boolean isSvgTL = true;
	
	// �������F�Ɋ��蓖�Ă邽�߂̗񋓃f�[�^ 2013/02
	String colorKeys ="";
	boolean useColorKeys = false;
	
	// ���^�f�[�^�̖��O��ԂȂ�(��������)
	String metaNs = "lm";
	String metaUrl = "http://www.svg-map.org/svgmap/localmetadata/";
	boolean IDset = true;
	int lop=0;
	
	//
	int topCount = -1;
	
	// ����������̃`�F�b�N�����{����ꍇ��true
	boolean heapCheck = false;
	
	// �X���b�h�̏ڍ׃p�����[�^�ݒ�  2016.5.19
	int maxThreads = 4; // �X���b�h�̍ő吔
	int threadBuffer = -1; // �X���b�h�̖��߃o�b�t�@��(-1��svgMapThreadTM�̃f�t�H���g�l)
	ExecutorService svgMapExecutorService; // for Executor multi thread 2017.4.19

	
	// �A������divErrMax�K�w�������ʂ��Ȃ��ꍇ�A���������𒆒f����  2016.5.19
	int divErrMax = 3;
	
	// for CSV Support 2017.4.3
	boolean inputCsv = false;
	boolean gZipped = false; // 2017.5.15
	String csvSchemaPath="";
	
	// 2018.1.26 SVGMapGetColorUtil�ڍs�ɔ����ϐ�
	int colorTable = SVGMapGetColorUtil.HSV;
	int outOfRangeViewMethod = SVGMapGetColorUtil.MARK;
	int colorKeyLength = 2;
	
	// 2019.1.24 ���C���[���[�g�R���e�i��<metadata>�v�f�ɁA�C�ӂ̃f�[�^(�܂ރ^�O������)�����邽�߂̕�����ϐ�
	String layerMetadata ="";
	
	boolean putRecord = false; // 2021.6.10 ���R�[�h�ԍ���data-record�ɋL�ڂ���
	
	static boolean layerDebug = false;
	
	private static void showHelp(){
		System.out.println("Shape2SVGMap: Shape��SVGMap�ɕϊ����܂��B");
		System.out.println("Copyright 2007-2018 by Satoru Takagi @ KDDI All Rights Reserved.");
		System.out.println("----------");
		System.out.println("java Shape2SVGMap [Options] (input.shp|input.csv) [output.svg]");
		System.out.println("input.(shp|csv) : �\�[�X�t�@�C���w��Bcsv�Ɋւ��Ă�-csvschena�����Q��");
		System.out.println("output.svg : �ϊ��斾���B�����ꍇ�g���q�����\�[�X�Ɠ����p�X�ŕϊ�");
		System.out.println("");
		System.out.println("Options   : -optionName (value)");
		System.out.println("-proj     : �������ʐ}�@�̕W���ܐ����w��");
		System.out.println("            center:�n�}��������ܐ���");
		System.out.println("            �l:��ܐ��l���w��l��");
		System.out.println("            �f�t�H���g:0 = �ԓ�");
		System.out.println("-height   : SVG�����̎w��");
		System.out.println("            ���l�FSVG�̍����̒l���w��B");
		System.out.println("            ���lx�F�n�����W(�ܓx�o�x)�~[���l]�{");
		System.out.println("            �f�t�H���g:100x :�n�����W��100�{�l(SVGT��10cm�܂ŕ\����)");
		System.out.println("-xy       : XY���W�n(���{���n�n)�̎w��");
		System.out.println("            1�`19�FXY�n�̂Ƃ��ɁA���̔ԍ����w��[m]�P�� ");
		System.out.println("            �f�t�H���g:0 = XY�n�ł͂Ȃ��ܓx�o�x�Ƃ��Ĉ���");
		System.out.println("            SPECIAL:-1�`-19�F[mm]�P��");
		System.out.println("-color    : �}�`�̐F�ݒ� (���C���̏ꍇ�͐��F�A�|���S���̏ꍇ�͓h�F)");
		System.out.println("            #000000~#ffffff:�F���w�� , (none�ŐF����)");
		System.out.println("            ������:�w�肵�������̒l�ŉς���B�����̌^�ɂ�莩���F�ݒ�");
		System.out.println("                  ������: ������ɉ����������_���ȓh��");
		System.out.println("                  ���l  : ���l�̑傫���ɉ������h��(�ԁFff�Fmax,00:min)");
		System.out.println("            �����ԍ�:�w�肵�������ԍ��̒l�ŉς���B�����̌^�ɂ�莩��");
		System.out.println("              �ǂ�ȑ��������邩�́A-showhead�I�v�V�����ŋN������΂킩��܂��B");
		System.out.println("            �f�t�H���g:#00FF00(green) ");
		System.out.println("-numcolor : ���l�ɂ�����J���[�e�[�u���̐ݒ� (RED:�ԂŖ��x, HSV:H�l�ω�(��:��,��:��), iHSV(���t), QUOTA:�l���ɂł��邾���ʐF���t)");
		System.out.println("            �f�t�H���g:HSV");
		System.out.println("-numrange : ���l�x�[�X�F�ݒ�̏�������ݒ�(�Q�l) 200 1000");
		System.out.println("            �f�t�H���g:���ۂ̒l���玩���ݒ�");
		System.out.println("-skipoutofrange: ��L�̏�������𒴂���l���X�L�b�v����(deplicate)");
		System.out.println("-outofrange:��������𒴂����l�̏���");
		System.out.println("            �f�t�H���g:�O���[�ɐݒ�");
		System.out.println("            skip (=skipoutofrange)");
		System.out.println("            counterStop ��������l�ɒ���t��");
		System.out.println("-strcolor : ������ɂ�����J���[�e�[�u���̃n�b�V���L�[�������ݒ� (1-n)");
		System.out.println("            �f�t�H���g:2");
		System.out.println("-colorkey : ������ɂ�����n�b�V���L�[(�J���[�e�[�u��)��񋓌^�Ō��߂�i�F���сF�����j");
		System.out.println("            CSV�ő����l��񋓂���B�e�����l�̌��#xxxxxx�������ꍇ�͒��ڐF���w��ł���");
		System.out.println("            �n�b�V���L�[�̕����񒷂��͑S�ē����łȂ��ƂȂ�Ȃ��B-numcolor�͂��̒l���玩���ݒ�");
		System.out.println("            �����֌W����I�v�V�����F�F�̊��t��:-numcolor");
		System.out.println("            ��P�F�����l�P,�����l�Q,....�@��Q�F�����l�P#F08020,�����l�Q#30D000,....");
		System.out.println("-opacity  : �h��̏ꍇ�̓����x�ݒ� (0.0�`1.0)");
		System.out.println("            �f�t�H���g:0.5");
		System.out.println("-outline  : �|���S���֊s�̐F�ݒ�");
		System.out.println("            #000000~#ffffff:�F���w��");
		System.out.println("              (���̑����͎����ݒ�)");
		System.out.println("            �f�t�H���g:null = �֊s���Ȃ�");
		System.out.println("-caption  : ���L�̐ݒ�");
		System.out.println("            ������:�w�肵�������̒l���g��");
		System.out.println("            �����ԍ�:�w�肵�������ԍ��̒l���g��");
		System.out.println("            �ǂ�ȑ��������邩�́A-showhead�I�v�V�����ŋN������΂킩��܂��B");
		System.out.println("-capfix   : ���L�̕����T�C�Y��L�k�Ɉ˂炸�Œ�ɂ���");
		System.out.println("            ��ʏ�ł̃t�H���g�T�C�Y");
		System.out.println("            p+[�����ԍ�](p4��)�F�����ԍ��̒l��ݒ�(�����l�͐����̂�)");
		System.out.println("-strokefix: ���̕���L�k�Ɉ˂炸�Œ�ɂ���");
		System.out.println("            ���l : ��ʏ�ł̐���");
		System.out.println("            p+[�����ԍ�]: �w�肵�������ԍ��̒l��ݒ�(�����l�͐����̂�)");
		System.out.println("            �f�t�H���g:0.75 �f�t�H���g��0.75px�Ɏw��");
		System.out.println("-strokew  : ���̕���L�k�Ɉˑ�������(-strokefix�Ɣr��)");
		System.out.println("            ���l : ����[m]");
		System.out.println("            0 : accuracy�̔��l��ݒ�");
		System.out.println("-poisize  : �V���{��(point,POI)�̃T�C�Y���w�肷��(�ȉ��̂R�p�^�[��)");
		System.out.println("            w(,h)    �����V���{���̃T�C�Y(�~�̏ꍇ���a�A�l�p�̏ꍇ�c��)���w��[degree]");
		System.out.println("            w(,h)m   �����V���{���̃T�C�Y���w��[meter]");
		System.out.println("            w(,h)px  ��ʏ�ł̃T�C�Y���w��[px]");
		System.out.println("            w,h �͂��ꂼ�ꏬ���_��");
		System.out.println("            attrN    �����ԍ�N�̒l���猈��B�����͐��l�ł���K�v�B�l�����W��-numrangesize�̒ʂ�B�T�C�Y�͉�ʏ�T�C�Y�ƂȂ�-sizerange�̒ʂ�B�܂�directpoi�w��K�{");
		System.out.println("            �Q�l: �P�����b�V��:1.0,0.666666   �Q�����b�V��:0.125,0.083333 ");
		System.out.println("            �f�t�H���g: 6px   w,h�ŏc���ʎw��Aw�݂̂ŏc�������T�C�Y�w��");
		System.out.println("-numrangesize: poisize��attrN��ݒ肵�����́A���̑����l�̏�������ݒ�(�Q�l) 200 1000");
		System.out.println("            �f�t�H���g:���ۂ̒l���玩���ݒ�");
		System.out.println("-sizerange: minSize maxSize[px]  -poisize��attrN��ݒ肵������POI�σT�C�Y�̍ŏ��ő�T�C�Y");
		System.out.println("            �f�t�H���g: 3 24");
		System.out.println("-directpoi: POI�𒼕`��");
		System.out.println("            rect, rect2, rect3: POI���l�p�ŕ`��");
		System.out.println("            diamond, diamond2, diamond3: POI���Ђ��`�ŕ`��");
		System.out.println("            triangle, triangle2, triangle3: POI���O�p�ŕ`��");
		System.out.println("            itriangle, itriangle2, itriangle3: POI���t�O�p�ŕ`��");
		System.out.println("            ��L2,3�͉���,�c���}�`");
		System.out.println("            �����ԍ�:�w�肵�������ԍ��̒l(�l��0..n�̐����̂ݑΉ�)�Ő؂�ւ���");
		
		System.out.println("            �f�t�H���g: �g�p���Ȃ�");
		
		System.out.println("-poicolumn: POI�̃V���{����؂�ւ��邽�߂̑����ԍ� or �����ԍ�1+�����ԍ�2");
		System.out.println("            �w�肵�������ԍ��̒l�F");
		System.out.println("            ����(0..n)�̏ꍇ�F�V���{��ID��0:p0...n:pn�@���K�v");
		System.out.println("            ������̏ꍇ�F�Ή����镶�����ID�Ɏ������V���{�����K�v");
		System.out.println("            �f�t�H���g: �g�p���Ȃ�)(0���ݒ�)");
		
		System.out.println("            ��̑����ԍ�������ꍇ�����ł͉��Z�����l�ŕ]���A������ł͌��������l�ŕ]��");
		
		System.out.println("-poisymbol: �V���{���e���v���[�g�t�@�C�����g�p����B");
		System.out.println("            �l: �V���{���e���v���[�g�t�@�C���̃p�X");
		System.out.println("            �f�t�H���g: �g�p���Ȃ��isvg�}�`�ō��ꂽ�ȒP�ȃV���{�����g���j");
		System.out.println("            ���t�@�C���̓��e�́Asvg��<defs>�v�f���ɃR�s�[�����B(���L�K�v�����̓��e�`�F�b�N�͂��܂���)");
		System.out.println("            �f�t�H���g��id=\"p0\"�̐}�`�v�f���K�v�B");
		System.out.println("            poiSymbolIdNumber�w��̏ꍇ��\"p\"+���̔ԍ��̐}�`�v�f���K�v");
		System.out.println("            poiSymbolIdNumber�w�� or poicolumn(�����^�C�v)�̏ꍇ��id=\"p\"+���̔ԍ��}�`�v�f���K�v");
		System.out.println("            poicolumn(������^�C�v)�̏ꍇ�͑Ή����镶�����ID�̐}�`�v�f���K�v");
		System.out.println("        ��: <g id=\"p0\">");
		System.out.println("              <image xlink:href=\"mappin.png\" preserveAspectRatio=\"none\" x=\"-8\" y=\"-25\" width=\"19\" height=\"27\"/>");
		System.out.println("            </g>");
		System.out.println("            ...");
		System.out.println("            �����R�[�h��UTF-8����");
		
		System.out.println("-poiSymbolIdNumber: POI�̃V���{��ID�ԍ����w��");
		System.out.println("            �l: ����");
		System.out.println("            �f�t�H���g: 0 (=\"p0\")");
		
		System.out.println("-noshape  : �}�`��z�u���Ȃ�(���L�̂ݕ\������Ƃ��ȂǂɎg�p)");
		System.out.println("            [�l����]");
		System.out.println("-accuracy : ���x");
		System.out.println("            [m]�P��");
		System.out.println("            �f�t�H���g:1.0[m]");
		System.out.println("-linkTitle: �A���J�[�̐ݒ�");
		System.out.println("            ������:�w�肵�������̒l��xlink:title�ɐݒ�E�E");
		System.out.println("            �����ԍ�:�w�肵�������ԍ��̒l���g��");
		System.out.println("            �ǂ�ȑ��������邩�́A-showhead�I�v�V�����ŋN������΂킩��܂��B");
		/** �p�~�i�ꉞ�̂̎����͎c���Ă��邪�A�F�X�Ȋg���̌��ʁA���퓮��͊��҂ł��Ȃ��j
		System.out.println("-meta:      RDF/XML���^�f�[�^���ߍ���");
		System.out.println("            �l����");
		**/
		/**
		System.out.println("-micrometa: micro���^�f�[�^���ߍ���");
		System.out.println("            [�l����]�F�S�Ă̑����𖄂ߍ���");
		System.out.println("            (�X�y�[�X��؂�ŕ�����)[�����ԍ�]�F�w�肵�������ԍ��̑����𖄍���");
		System.out.println("            (�X�y�[�X��؂�ŕ�����)[�����ԍ�]=[������]�F�w�肵���������Ŗ�����");
		**/
		System.out.println("-micrometa2: micro���^�f�[�^  ���ߍ���");
		System.out.println("            [�l����]�F�S�Ă̑����𖄂ߍ���");
		System.out.println("            (�X�y�[�X��؂�ŕ�����)[�����ԍ�]�F�w�肵�������ԍ��̑����𖄍���");
		System.out.println("            (�X�y�[�X��؂�ŕ�����)[�����ԍ�]=[������]�F�w�肵���������Ŗ�����");
		System.out.println("            �ŏ��Ɏw�肵�������ԍ��̃f�[�^��xlink:title�����ɂ��t�^����");
		System.out.println("-layermeta: (\"���^�f�[�^������\"||file [path])");
		System.out.println("            ���^�f�[�^������́Axml�m�[�h�Ƃ��ċ������C�ӂ̕����񕡐��̃^�O����������邱�Ƃ���");
		System.out.println("            file �̏ꍇ�̓��^�f�[�^�}��������������̓������e�L�X�g�t�@�C���̃p�X��[path]�Ŏw�肷��(�t�@�C�����͂̏ꍇ��UTF-8)");
		System.out.println("            ������ł��A�G�X�P�[�v�Ȃǂ͎���s���Ă���");
		System.out.println("-noid     : micrometa�̂Ƃ�id������t���Ȃ�");
		System.out.println("            [�l����]");
		System.out.println("-group    : �O���[�v���쐬");
		System.out.println("            [�l����]");
		System.out.println("-datum    : ��ԎQ�ƌn��ݒ� tokyo�̂ݐݒ�ł���");
		System.out.println("            �f�t�H���g:JGD2000(WGS 84�Ɠ���)");
		System.out.println("-mesh     : ���b�V�������A�R���e�iSVG���쐬");
		System.out.println("            �Z�ӑ��̕������B���ӑ��̕������͎����v�Z");
		System.out.println("            �f�t�H���g:0(�������Ȃ�)");
		System.out.println("-level    : �S��quad tree�ɂ��^�C������ (-mesh�I�v�V�����͖��������)");
		System.out.println("            �����F���x��(0...(�T��20�܂�)) ���x���ƃT�C�Y(�x)�̊֌W�͉��L");
		System.out.println("            0:360deg,1:180deg,2:90deg,3:45deg,4:22.5deg,5:11.25deg,6:5.625deg..");
		System.out.println("-limit    : �o�̓^�C���T�C�Y�̃��~�b�^��ݒ�(�P��KBytes(3�ȏ�))");
		System.out.println("            �f�t�H���g:��100MB�@���~�b�^�𒴂���Ɖ����܂ōċA�I�ɂS����");
		
		System.out.println("-layer    : �w�肵������(�ԍ����͖��O)�����Ƀ��C��(�t�@�C��)��������");
		System.out.println("            ���C���̃t�@�C�����F�����l�����ɐݒ�");		
		System.out.println("            �f�t�H���g:�Ȃ�");
		System.out.println("-simplify : �}�`���ȒP������");
		System.out.println("            �P�����p�����[�^[�x] 0.01���炢���K���ȂƂ���");
		System.out.println("            (-)�l: �g�|���W���ێ����Ȃ��P�������[�`�����g�p");
		System.out.println("            �f�t�H���g�F�P�������Ȃ�");
		System.out.println("-duplicate: �d���}�`��}������");
		System.out.println("            ������: �����ԍ�: �d����c�����邽�߂̑������w��(ID�ԍ��Ȃ�)");
		System.out.println("            ");
		System.out.println("-linemerge: �����}�[�W���ăf�[�^�T�C�Y�����k����");
		System.out.println("            [�l����]");
		System.out.println("            ");
		System.out.println("-showtile : �f�o�b�O�E�m�F�p(�^�C�����E�����o�͂���)");
		System.out.println("            [�l����]");
		System.out.println("-charset  : �����R�[�h��ݒ� UTF-8�̂ݐݒ�\�B���������΍�ɗ��p��");
		System.out.println("            �f�t�H���g:ShiftJIS");
//		System.out.println("-pstep    : �K�w�����̃X�e�b�v(�B���R�}���h) >=2 ");
		System.out.println("-showhead : �w�b�_�̕\��");
		System.out.println("-csvschema: �f�[�^�̃X�L�[�}�t�@�C�����w��");
		System.out.println(" CSV�t�@�C���̐����E����:");
		System.out.println("    Point,LineString(�������r�؂�Ȃ���),Polygon(�������h�[�i�c��������Ȃ�����),WKT�G���R�[�hgeometry���T�|�[�g�B���̑����͊�{�I�ɂ��ׂĕ�����Ƃ��Ĉ����B");
		System.out.println("    1�s�ڂ̓X�L�[�}�s�A�ȍ~�̍s���瑱���f�[�^�̑��������w�肷��(�X�L�[�}��CSV)");
		System.out.println("    ���������Ƃ��Ĉܓx�o�x�������}�W�b�N���[�h(��q)��ݒu���č��W�̃J�����ł��邱�Ƃ��w�肷��B");
		System.out.println("    �X�L�[�}�s�Ȃ��ɁA-csvschema�ŃX�L�[�}��ʃt�@�C�������邱�Ƃ��\�B���̏ꍇ1�s�̃t�@�C���ŕ\�����A�����ɉ��s���K�v�B");
		System.out.println("    Point: �C�ӂ̃J�����ɍ��W�̑������������}�W�b�N���[�h('�ܓx','�o�x','latitude','longitude','lat','lng' �p�����͑召�����̋�ʂȂ�)���w�肷��B");
		System.out.println("    LineString:�X�L�[�}�sCSV�̖����̃J�����Ƀ}�W�b�N���[�h'latitude:line','longitude:line'(latitude�Ȃǂ̑���ɐ�q�̕�������g�p��)�̃y�A��ݒ肷��K�v������B���f�[�^�͔C�ӂ̍��W�𖖔��ɗ��񂵂ĕ\������B");
		System.out.println("    Polygon:���� �}�W�b�N���[�h'latitude:polygon','longitude:polygon'");
		System.out.println("    �x���b�\��:�ܓx�o�x�}�W�b�N���[�h���DMS��������DM��ǋL����'�o�xDMS','latitudeDM:polygon'�ȂǁADDDMMSS.SSS��,DDMM.MM�̂悤�ȕ\����������");
		System.out.println("    WKT:�C�ӂ̃J������WKT�������}�W�b�N���[�h'WKT'���w��BWKT���J���}���܂ޏꍇ�_�u���N�H�[�e�[�V������WKT�J�����̃G�X�P�[�v���K�v");
		System.out.println("-top      : �����炎�̃f�[�^�������o�͂���B�@�l�F��");
//		System.out.println("-test     : �e�X�g�p"); // �قƂ�ǈӖ��Ȃ�
//		System.out.println("-transform: CRS��transform 6�l�𒼐ڎw��(csv)");
//		System.out.println("-dataver  : �R���e���c�o�[�W�������w��(animation��image�������̑���)");
//		System.out.println("            �f�t�H���g:1.2  1.1���w��ł���");
//		System.out.println("-heapCheck: ��������`�F�b�N�p"); 
		// �B���@�\�ɂ��Ă���
		System.out.println("-densityControl: �^�C�����x�ɉ������\������ [px/TILE]");
		System.out.println("            �f�t�H���g:[�l����]");
		System.out.println("-lowResImage: ���k�ڎ�(densitioControl�~���l�ȉ�)��bitImageTile���֕\��");
		System.out.println("               -level��-densityControl�ݒ�K�v�BbitImageTile��Shape2ImageSVGMap�g�p");
		System.out.println("-actualGenerateMeshLevel: ���ۂɐ�������郌�x��������ȏ�Ƃ���(-level���p)");
		System.out.println("-maxLevel : ���̃��x���𒴂��������̓L�����Z������: -level�ƕ��p���Ă���Ƃ��͑S��quad tree�x�[�X�̃��x�� (default:20)");
		System.out.println("-maxThreads: �ő�X���b�h�� �f�t�H���g�F4");
		System.out.println("-threadBuffer: �e�X���b�h���󂯎��`�施�߃o�b�t�@�� �f�t�H���g�F16384");
		System.out.println("-divErrMax: �������ʂ����̐����Ȃ��ꍇ�A�����������f �f�t�H���g�F3");
		System.out.println("-optionFile: �I�v�V�����t�@�C�����w�肷��B(���o�̓t�@�C���͂��̃t�@�C�����ł͎w��ł��Ȃ�)�R�}���h���C���ł̃I�v�V�����Ɠ�������(���������s����)�@�����炪�D�悳���");
		System.out.println("-putRecord: data-record�����ɁA���R�[�h�ԍ����L�ڂ���");
		
		
		
		
		System.out.println("            ");
	}
	
	public static String[] getOptionFile( String[] mainArgs) {
		// -optionFile �I�v�V����������ꍇ�A�����D�悷��
		String optFilePath = null;
		for ( int i = 0 ; i < mainArgs.length ; i++ ){
			if ( mainArgs[i].toLowerCase().equals("-optionfile")){
				optFilePath = mainArgs[i+1];
				break;
			}
		}
		
		if ( optFilePath != null ){
			String[] ans=null;
			try {
				optionsReader or = new optionsReader( new File(optFilePath) );
				String[] opts = or.getOptions();
				
				if ( mainArgs[ mainArgs.length - 1 ].indexOf(".svg") > 0 ){
					ans = new String[ opts.length + 2 ];
					ans[ opts.length + 1 ] = mainArgs[mainArgs.length -1]; // svgPath
					ans[ opts.length ] = mainArgs[mainArgs.length - 2]; // shpPath
				} else {
					ans = new String[ opts.length + 1 ];
					ans[ opts.length ] = mainArgs[mainArgs.length - 1]; // shpPath
				}
				
				for ( int i = 0 ; i < opts.length ; i++ ){
					ans[i] = opts[i];
				}
				
			} catch ( Exception e ){
				System.out.println("-optionFile �I�v�V����������Ă��܂�");
				showHelp();
				System.exit(0);
			}
			return ( ans );
		} else {
			return ( mainArgs );
		}
		
	}
	
	public static void main(String[] args) {
		
		
		args = getOptionFile( args );
		
		double boundHeight = -100; // 2010/10/29 SVG1.2Tiny�̐��񂩂�A10cm���x���o�����߂�100�{����
		// SVG1.2Tiny�ł� -32,767.9999 to +32,767.9999 (���������_�\��(E+xx)�͉�,�����_�ȉ�4��)
		int params = 0;
		String outfile ="";
		String infile = "";
		
		boolean lowResImage = false;
		
		Shape2SVGMap19 s2sm = new Shape2SVGMap19();
		try {
			if(args.length < 1 || args[args.length -1 ].indexOf("-") == 0 ){
				showHelp();
				System.out.println("���̓t�@�C�����w�肳��Ă��܂���");
				throw new IOException();
//				System.exit(0);
			}
			
			if ( args[ args.length - 1 ].indexOf(".svg") > 0 && args.length > 1 ){
				infile = args[args.length - 2];
				outfile = args[args.length - 1];
				params = args.length - 2;
			} else {
				infile = args[args.length - 1];
//				outfile = args[args.length - 1] + ".svg";
				try{
					outfile = (args[args.length - 1]).substring(0 , (args[args.length - 1]).lastIndexOf(".")) + ".svg";
				} catch ( Exception e ){
					System.out.println("���̓t�@�C�����w�肳��Ă��܂���");
					throw new IOException();
				}
//				System.out.println("InputFile:" + infile + "  OutputFile:" + outfile);
				params = args.length - 1;
			}
			
			if ( infile.endsWith(".csv")){
				s2sm.inputCsv = true;
				System.out.println("CSV input");
			} else if ( infile.endsWith(".gz")){
				s2sm.inputCsv = true;
				s2sm.gZipped = true;
				System.out.println("gz_CSV input");
			} else {
				System.out.println("Shapefile input");
			}
			
			for (int i = 0; i < params; ++i) {
				if ( args[i].toLowerCase().equals("-proj")){
					++i;
					if (args[i].equals("center")){
						s2sm.projCenter = 999.0;
					} else {
						s2sm.projCenter = Double.parseDouble(args[i]);
					}
				} else if ( args[i].toLowerCase().equals("-height")){
					++i;
					if ( args[i].indexOf("x") > 0 ){
						s2sm.boundHeight = - Double.parseDouble(args[i].substring( 0 , args[i].indexOf("x") ));
//						System.out.println("xx:" + s2sm.boundHeight );
					} else {
						s2sm.boundHeight = Integer.parseInt(args[i]);
					}
				} else if ( args[i].toLowerCase().equals("-xy")){
					++i;
					s2sm.xySys = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-color")){
					++i;
					s2sm.colorProp = args[i];
				} else if ( args[i].toLowerCase().equals("-numcolor")){
					++i;
					if (args[i].toLowerCase().equals("hsv")){
						s2sm.colorTable = SVGMapGetColorUtil.HSV;
					} else if (args[i].toLowerCase().equals("ihsv")){
						s2sm.colorTable = SVGMapGetColorUtil.iHSV;
					} else if (args[i].toLowerCase().equals("red")){
						s2sm.colorTable = SVGMapGetColorUtil.RED;
					} else if (args[i].toLowerCase().equals("quota")){
						s2sm.colorTable = SVGMapGetColorUtil.QUOTA;
					}
				} else if ( args[i].toLowerCase().equals("-numrange")){
					++i;
					double p1,p2;
					p1 = Double.parseDouble(args[i]);
					++i;
					p2 = Double.parseDouble(args[i]);
					if ( p1 < p2 ){
						s2sm.mainAttrMax = p2;
						s2sm.mainAttrMin = p1;
					} else {
						s2sm.mainAttrMax = p1;
						s2sm.mainAttrMin = p2;
					}
				} else if ( args[i].toLowerCase().equals("-numrangesize")){
					++i;
					double p1,p2;
					p1 = Double.parseDouble(args[i]);
					++i;
					p2 = Double.parseDouble(args[i]);
					if ( p1 < p2 ){
						s2sm.sizeAttrMax = p2;
						s2sm.sizeAttrMin = p1;
					} else {
						s2sm.sizeAttrMax = p1;
						s2sm.sizeAttrMin = p2;
					}
				} else if ( args[i].toLowerCase().equals("-sizerange")){
					++i;
					double p1,p2;
					p1 = Double.parseDouble(args[i]);
					++i;
					p2 = Double.parseDouble(args[i]);
					if ( p1 < p2 ){
						s2sm.sizeRangeMax = p2;
						s2sm.sizeRangeMin = p1;
					} else {
						s2sm.sizeRangeMax = p1;
						s2sm.sizeRangeMin = p2;
					}
				} else if ( args[i].toLowerCase().equals("-skipoutofrange")){
//					s2sm.nullColor = "";
					s2sm.outOfRangeViewMethod = SVGMapGetColorUtil.SKIP;
				} else if ( args[i].toLowerCase().equals("-outofrange")){
					++i;
					if (args[i].toLowerCase().equals("skip")){
//						s2sm.nullColor = "";
						s2sm.outOfRangeViewMethod = SVGMapGetColorUtil.SKIP;
					} else if (args[i].toLowerCase().equals("counterstop")) {
//						s2sm.counterStop = true;
						s2sm.outOfRangeViewMethod = SVGMapGetColorUtil.COUNTER_STOP;
					} else {
						// set null color
					}
				} else if ( args[i].toLowerCase().equals("-strcolor")){
					++i;
					s2sm.colorKeyLength = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-colorkey")){
					++i;
					s2sm.colorKeys = args[i];
				} else if ( args[i].toLowerCase().equals("-outline")){
					++i;
					s2sm.olColorProp = args[i];
				} else if ( args[i].toLowerCase().equals("-caption")){
					++i;
					s2sm.symbolProp = args[i];
				} else if ( args[i].toLowerCase().equals("-duplicate")){
					++i;
					s2sm.dupProp = args[i];
				} else if ( args[i].toLowerCase().equals("-accuracy")){
					++i;
					s2sm.accuracy = Double.parseDouble(args[i]);
					if ( s2sm.accuracy <= 0.0 ) {
						s2sm.accuracy = 1.0;
					}
				} else if ( args[i].toLowerCase().equals("-linktitle")){
					++i;
					s2sm.linkProp = args[i];
				} else if ( args[i].toLowerCase().equals("-top")){
					++i;
					s2sm.topCount = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-meta")){ // �T�|�[�g���~�i�������邪���얢�m�F�j
					s2sm.metaEmbed = true;
				} else if ( args[i].toLowerCase().equals("-micrometa")){
					s2sm.microMetaEmbed = true;
					
					while ( args[i+1].indexOf("-") == -1 && i < params - 1 ){
						++i;
						if ( args[i].indexOf("=") == -1 ){
							s2sm.metaIndex.put(new Integer(args[i]) , "" );
						} else {
							s2sm.metaIndex.put(new Integer(args[i].substring(0,args[i].indexOf("="))), args[i].substring(args[i].indexOf("=")+1));
						}
						
					}
					System.out.println( "Micrometa MetaIndex:" + s2sm.metaIndex );
				} else if ( args[i].toLowerCase().equals("-micrometa2")){
					s2sm.microMeta2Embed = true;
					
					while ( args[i+1].indexOf("-") == -1 && i < params - 1 ){
						++i;
						if ( args[i].indexOf("=") == -1 ){
							s2sm.metaIndex.put(new Integer(args[i]) , "" );
						} else {
							s2sm.metaIndex.put(new Integer(args[i].substring(0,args[i].indexOf("="))), args[i].substring(args[i].indexOf("=")+1));
						}
						
					}
					System.out.println( "Micrometa\"2\" MetaIndex:" + s2sm.metaIndex );
				} else if ( args[i].toLowerCase().equals("-layermeta")){
					++i;
					if ( args[i].toLowerCase().equals("file")){
						++i;
						SVGMapSymbolTemplate lm = new SVGMapSymbolTemplate(); // �ړI���Ⴄ��SVGMapSymbolTemplate���p���܂��E�E�E 2019.1.24
						lm.readSymbolFile(args[i]);
						s2sm.layerMetadata = lm.symbolFile;
					} else {
						s2sm.layerMetadata = args[i];
					}
				} else if ( args[i].toLowerCase().equals("-noshape")){
					s2sm.noShape = true;
				} else if ( args[i].toLowerCase().equals("-noid")){
					s2sm.noMetaId = true;
				} else if ( args[i].toLowerCase().equals("-group")){
					s2sm.outGroup = true;
				} else if ( args[i].toLowerCase().equals("-mesh")){
					++i;
					s2sm.meshPart = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-level")){
					++i;
					s2sm.meshLevel = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-actualgeneratemeshlevel")){
					++i;
					s2sm.actualGenerateMeshLevel = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-maxlevel")){
					++i;
					s2sm.maxLevel = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-lowresimage")){
					lowResImage = true;
				} else if ( args[i].toLowerCase().equals("-showtile")){
					layerDebug = true;
				} else if ( args[i].toLowerCase().equals("-datum")){
					++i;
					
					if ( args[i].toLowerCase().equals("tokyo") || args[i].toLowerCase().equals("bessel")){
						s2sm.datum = GeoConverter.BESSEL;
					}
				} else if ( args[i].toLowerCase().equals("-transform")){
					++i;
					s2sm.crstProp = args[i];
				} else if ( args[i].toLowerCase().equals("-dataver")){
					++i;
					if ( args[i].equals("1.1")){
						s2sm.isSvgTL =false;
						System.out.println("use old format");
					}
				} else if ( args[i].toLowerCase().equals("-limit")){
					++i;
					s2sm.limitProp = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-layer")){
					++i;
					s2sm.layerProp = args[i];
				} else if ( args[i].toLowerCase().equals("-opacity")){
					++i;
					s2sm.opacity = Double.parseDouble(args[i]);
					if ( s2sm.opacity < 0.0 || s2sm.opacity > 1.0){
						s2sm.opacity = 0.5;
					}
				} else if ( args[i].toLowerCase().equals("-simplify")){
					++i;
					s2sm.simplifyParam = Double.parseDouble(args[i]);
					if ( s2sm.simplifyParam < -10.0 || s2sm.simplifyParam > 10.0){
						s2sm.simplifyParam = 0.01;
					}
				} else if ( args[i].toLowerCase().equals("-capfix")){
					++i;
					if ( args[i].indexOf("p") == 0 ){
						s2sm.fixedFont = -Double.parseDouble(args[i].substring(1));
						System.out.println("capFix by Prop" + s2sm.fixedFont );
					} else {
						s2sm.fixedFont = Double.parseDouble(args[i]);
						if ( s2sm.fixedFont > 100.0){
							s2sm.fixedFont = 0.0;
						}
					}
				} else if ( args[i].toLowerCase().equals("-strokefix")){
					++i;
					if ( args[i].indexOf("p") == 0 ){
						s2sm.fixedStroke  = -Integer.parseInt(args[i].substring(1));
					} else {
						s2sm.fixedStroke = Double.parseDouble(args[i]);
						if ( s2sm.fixedStroke < 0 || s2sm.fixedStroke > 50.0){
							s2sm.fixedStroke = 0.0;
						}
					}
				} else if ( args[i].toLowerCase().equals("-strokew")){
					++i;
					s2sm.fixedStroke = 0.0;
					s2sm.strokeWidth = Double.parseDouble(args[i]);
					if ( s2sm.strokeWidth < 0 ){
						s2sm.strokeWidth = 0.0;
					}
				} else if ( args[i].toLowerCase().equals("-poisize")){
					++i;
					String ssProp = args[i];
					if (ssProp.indexOf("attr")==0){
						s2sm.POIsizeCol = Integer.parseInt(ssProp.substring(4));
						System.out.println("Set POI size based on attr:"+s2sm.POIsizeCol);
					} else if (ssProp.indexOf("px")>0){
						if (ssProp.indexOf(",")>0){
							s2sm.fixedSymbol = Double.parseDouble(ssProp.substring(0,ssProp.indexOf(",")));
							s2sm.fixedSymbolSub = Double.parseDouble(ssProp.substring(ssProp.indexOf(",")+1,ssProp.indexOf("px")));
						} else {
							s2sm.fixedSymbol = Double.parseDouble(ssProp.substring(0,ssProp.indexOf("px")));
							s2sm.fixedSymbolSub = s2sm.fixedSymbol;
						}
						if ( s2sm.fixedSymbol < 0 || s2sm.fixedSymbol > 50.0){
							s2sm.fixedSymbol = 6.0;
							s2sm.fixedSymbolSub = 6.0;
						}
						System.out.println("sizing poi:" + (s2sm.fixedSymbol)+"px");
						if (ssProp.indexOf(",")>0){
							System.out.println("sizing poi(height):" + (s2sm.fixedSymbolSub)+"px");
						}
					} else if (ssProp.indexOf("m")>0){
						// ���[�g���̃T�C�Y�ŃV���{�����Œ肳����.. ���������������E�E
						if (ssProp.indexOf(",")>0){
							s2sm.fixedSymbol = (- Double.parseDouble(ssProp.substring(0,ssProp.indexOf(",")))) * 360 / 40000000;
							s2sm.fixedSymbolSub = (- Double.parseDouble(ssProp.substring(ssProp.indexOf(",")+1,ssProp.indexOf("m")))) * 360 / 40000000;
						} else {
							s2sm.fixedSymbol = (- Double.parseDouble(ssProp.substring(0,ssProp.indexOf("m")))) * 360 / 40000000;
							s2sm.fixedSymbolSub = s2sm.fixedSymbol;
						}
						
						System.out.println("sizing poi:" + ((-s2sm.fixedSymbol) * 40000000 / 360 )+"meters :"+(-s2sm.fixedSymbol)+"degrees");
						if ( ssProp.indexOf(",")>0){
							System.out.println("sizing(height) poi:" + ((-s2sm.fixedSymbolSub) * 40000000 / 360 )+"meters :"+(-s2sm.fixedSymbolSub)+"degrees");
						}
					} else {
						if (ssProp.indexOf(",")>0){
							s2sm.fixedSymbol = (- Double.parseDouble(ssProp.substring(0,ssProp.indexOf(","))));
							s2sm.fixedSymbolSub = (- Double.parseDouble(ssProp.substring(ssProp.indexOf(",")+1)));
						} else {
							s2sm.fixedSymbol = - Double.parseDouble(ssProp);
							s2sm.fixedSymbolSub = s2sm.fixedSymbol;
						}
						System.out.println("sizing poi:" + (-s2sm.fixedSymbol)+"degrees");
						if (ssProp.indexOf(",")>0){
							System.out.println("sizing poi(height):" + (-s2sm.fixedSymbolSub)+"degrees");
						}
					}
				} else if ( args[i].toLowerCase().equals("-directpoi")){ // 2012.7.9(����)
					++i;
					if (args[i].toLowerCase().indexOf("rect") >= 0 && args[i].toLowerCase().indexOf("3") >= 0 ){
						s2sm.directPoi = -9; // (-)vectorPoishape�ԍ� ( 0�͎g���Ȃ��E�E�E1�͎g����悤�ɂ���(2016.2.26) )
					} else if (args[i].toLowerCase().indexOf("rect") >= 0 && args[i].toLowerCase().indexOf("2") >= 0 ){
						s2sm.directPoi = -5;
					} else if (args[i].toLowerCase().indexOf("rect") >= 0 ){
						s2sm.directPoi = -1;
					} else if (args[i].toLowerCase().indexOf("dia") >= 0 && args[i].toLowerCase().indexOf("3") >= 0  ){
						s2sm.directPoi = -10;
					} else if (args[i].toLowerCase().indexOf("dia") >= 0 && args[i].toLowerCase().indexOf("2") >= 0  ){
						s2sm.directPoi = -6;
					} else if (args[i].toLowerCase().indexOf("dia") >= 0   ){
						s2sm.directPoi = -2;
					} else if (args[i].toLowerCase().indexOf("itr") >= 0 && args[i].toLowerCase().indexOf("3") >= 0  ){
						s2sm.directPoi = -12;
					} else if (args[i].toLowerCase().indexOf("itr") >= 0 && args[i].toLowerCase().indexOf("2") >= 0  ){
						s2sm.directPoi = -8;
					} else if (args[i].toLowerCase().indexOf("itr") >= 0  ){
						s2sm.directPoi = -4;
					} else if (args[i].toLowerCase().indexOf("tri") >= 0 && args[i].toLowerCase().indexOf("3") >= 0  ){
						s2sm.directPoi = -11;
					} else if (args[i].toLowerCase().indexOf("tri") >= 0 && args[i].toLowerCase().indexOf("2") >= 0  ){
						s2sm.directPoi = -7;
					} else if (args[i].toLowerCase().indexOf("tri") >= 0  ){
						s2sm.directPoi = -3;
					} else {
						s2sm.directPoi = Math.abs(Integer.parseInt(args[i]));
					}
					
					
					System.out.println("DirectPoi:" + s2sm.directPoi );
				} else if ( args[i].toLowerCase().equals("-poicolumn")){ // 2013.10.21
					++i;
					if ( args[i].indexOf("+") >0 ){
						String[] pcol = args[i].split("\\+");
						s2sm.poiColumn = Integer.parseInt(pcol[0]);
						s2sm.poiColumn2 = Integer.parseInt(pcol[1]);
						System.out.println("PoiColumn:" + s2sm.poiColumn + " + " + s2sm.poiColumn2 );
					} else {
						s2sm.poiColumn = Integer.parseInt(args[i]);
						System.out.println("PoiColumn:" + s2sm.poiColumn );
					}
				} else if ( args[i].toLowerCase().equals("-poisymbol")){ // 2012.7.9(����)
					++i;
					SVGMapSymbolTemplate tp = new SVGMapSymbolTemplate();
					tp.readSymbolFile( args[i] );
					s2sm.symbolTemplate = tp.symbolFile;
					System.out.println("use externalPoiSymbol:");
					System.out.println(tp.symbolFile);
				} else if ( args[i].toLowerCase().equals("-poisymbolidnumber")){ // 2014.4.25 from DRS req.
					++i;
					s2sm.customPoiType = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-linemerge")){
					s2sm.mergeProp = true;
				} else if ( args[i].toLowerCase().equals("-charset")){
					++i;
					if ( (args[i].toUpperCase()).equals("UTF-8")){
						s2sm.strIsSJIS = false;
					}
				} else if ( args[i].toLowerCase().equals("-showhead")){
					s2sm.showHead = true;
				/**
				} else if ( args[i].toLowerCase().equals("-test")){
					System.out.println("TEST");
					s2sm.test();
					throw new IOException();
				**/
				} else if ( args[i].toLowerCase().equals("-heapcheck")){
					s2sm.heapCheck = true;
				} else if ( args[i].toLowerCase().equals("-pstep")){
					++i;
					s2sm.pStep = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-densitycontrol")){
					++i;
					s2sm.densityControl = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-maxthreads")){
					++i;
					s2sm.maxThreads = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-threadbuffer")){
					++i;
					s2sm.threadBuffer = Integer.parseInt(args[i]);
				} else if ( args[i].toLowerCase().equals("-diverrmax")){
					++i;
					if (  Integer.parseInt(args[i]) > 1 ){
						s2sm.divErrMax = Integer.parseInt(args[i]);
					}
				} else if ( args[i].toLowerCase().equals("-csvschema")){ // add 2017.04
					++i;
					s2sm.csvSchemaPath = args[i];
					System.out.println("Schema Path for CSV file: " + s2sm.csvSchemaPath);
				} else if ( args[i].toLowerCase().equals("-putrecord")){ // add 2021.06
					++i;
					s2sm.putRecord=true;
					System.out.println("Put record number on data-record attr");
				} else {
					showHelp();
					System.out.println("���݂��Ȃ��I�v�V����\"" + args[i] + "\"���w�肳��܂����B");
					throw new IOException();
				}
			}
			
			if ( s2sm.POIsizeCol !=-1 && s2sm.directPoi == 0 ){
				System.out.println("-directpoi���ݒ肳��Ȃ��܂܁A-poisize attr"+ s2sm.POIsizeCol+" ���w�肳��܂����@�I�����܂�");
				throw new IOException();
			}
			
			// 2013.3.28 ���x��density controle�@�\���g������I�I(low res��bitimage�ő�ւ���@�\)
			if ( lowResImage == true){
				if ( s2sm.meshLevel >= 0 && s2sm.densityControl > 0 ){
					System.out.println("-lowresimage��L���ɂ��܂��B base level:"+s2sm.meshLevel);
					layerDebug = true;
					s2sm.bitimageGlobalTileLevel = s2sm.meshLevel;
				} else {
					System.out.println("-lowresimage�͖����ł��I : -level��-densityControl��ݒ肵�Ă�������");
//					showHelp();
					throw new IOException();
				}
			}
			
			// �ϊ��̖{�̋@�\���Ăяo���I�I�I
			s2sm.parse( infile ,  outfile );
			
			System.out.println("Finished...");
			
		}catch(Exception e){
			if ( e instanceof FileNotFoundException ){
				System.out.println("�t�@�C���ɃA�N�Z�X�ł��܂���: " + e.getMessage() );
			} else if ( e instanceof IOException ){
				System.out.println("�p�����[�^���Ⴂ�܂�: " + e.getMessage());
				e.printStackTrace();
			} else {
				System.out.println("�G���[: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	
	public void parse( String infile , String outfile ) throws Exception {
		MemoryUsage usage = null;
		if ( heapCheck ){
			MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
			usage = mbean.getHeapMemoryUsage();
			System.out.printf("�ő�T�C�Y�F%10d%n", usage.getMax());
			System.out.printf("�g�p�T�C�Y�F%10d%n", usage.getUsed());
//			SizeOf.skipStaticField(true); // �������R�[�h 
//			SizeOf.setMinSizeToLog(1000000); // �������R�[�h 
		}
		
		
//		System.out.println("Java Version:" + System.getProperty("java.version"));
//		Provider sunJceProvider = java.security.Security.getProvider("SunJCE"); 
//		System.out.println("Java Security:" + sunJceProvider.getInfo());
		
		svgMapExecutorService = Executors.newFixedThreadPool(maxThreads);
		System.out.println("Thread Pool Size:"+maxThreads);
		
		SvgMapTilesTM sm , sm0;
		double maxX, minX, maxY, minY;
//		gconv = new GeoConverter(GeoConverter.Tky2JGD); // Bessel->WGS��TKY2JGD��
//		gconv.tiny = true; // WGS<>Bessel�ϊ����ȈՔł�
		
		if ( microMetaEmbed && microMeta2Embed ){
			microMetaEmbed = false;
		}
		if ( !metaEmbed && ! microMetaEmbed && !microMeta2Embed){
			lineMerge = mergeProp;
		} else {
			lineMerge = false;
		}
		
		// �F�e�[�u���̍쐬  SVGMapGetColorUtil�Ɉڍs
		/**
		colorMap = new HashMap<Object,String>();
		colorMap.put("default" , "green");
		
		if ( colorKeys != ""){
			initColorKeyEnum(); // add 2013.02
		}
		**/
		
		// �}�`�P�����G���W���̑g�ݍ���(JTS�𗘗p)
		boolean simplify = false ;
		boolean TopoPresSimp = true;
		Object simplifier;
		DouglasPeuckerSimplifier Dsimplifier = new DouglasPeuckerSimplifier(null);
		TopologyPreservingSimplifier Tsimplifier = new TopologyPreservingSimplifier( null );
		
		if ( simplifyParam  > 0.0 && simplifyParam < 10.0 ){
			simplify = true;
			TopoPresSimp = true;
		} else if ( simplifyParam < 0.0 && simplifyParam > -10.0 ){
			simplify = true;
			simplifyParam = - simplifyParam;
			TopoPresSimp = false;
		}
		
		
		//--------------------------------
		// ���[�_�[��������
		//--------------------------------
		/**
		//���̓f�[�^�X�g�A�쐬
		ShapefileDataStore readStore = new ShapefileDataStore(shapeURL);
		
		int tLeng = (readStore.getTypeNames()).length;
//		for ( int i = 0 ; i < tLeng ; i++){
//			System.out.println("No." + i + " TypeName:"+ readStore.getTypeNames()[i] );
//		}
		//�t�B�[�`���[�\�[�X�擾
		FeatureSource<SimpleFeatureType, SimpleFeature>  source = readStore.getFeatureSource();
		**/
		
		CSVDataStore cds =null;
		ShapefileDataStore sds = null;
		FeatureSource<SimpleFeatureType, SimpleFeature> source = null;
		if ( inputCsv ){
			String charset ="MS932";
			if ( !strIsSJIS ){
				charset = "UTF-8";
				strIsSJIS = true; // CSV�̎��́A���̐ݒ��́ACDS����UTF�ł����ׂ�SJIS�ɋz�������
			}
			// CSV�t�@�C����ǂݍ��� 
			cds =null;
			if ( csvSchemaPath =="" ){
				cds = new CSVDataStore( new File(infile), gZipped , charset);
			} else {
				cds = new CSVDataStore( new File(infile), new File(csvSchemaPath), gZipped , charset);
			}
			//�t�B�[�`���[�\�[�X�擾
			source = cds.getFeatureSource(cds.getNames().get(0));
		} else {
			//���[�h����r���������`���t�@�C��
			URL shapeURL = (new File(infile)).toURI().toURL();
			sds = new ShapefileDataStore(shapeURL); // cast to ShapefileDataStore
			//�t�B�[�`���[�\�[�X�擾
			source = sds.getFeatureSource();
		}
		
		//���̓J�������̎擾
		SimpleFeatureType readFT = source.getSchema();
		
		int nameCol = -1;
		Envelope env = source.getBounds();
//		System.out.println("source-bounds:" + env );
//		System.out.println("BBX:" + env.getMaxX() + ":" + env.getMaxY() + ":" + env.getMinX() + ":" + env.getMinY() );
//		System.out.println("--:" + ( Math.abs(env.getMaxX()) > 300 ) + "::" + Math.abs(env.getMaxX()) );
		if ( xySys == 0 ){
			if ( Math.abs(env.getMaxX()) > 300 || Math.abs(env.getMinX()) > 300 ||
				Math.abs(env.getMaxY()) > 300 || Math.abs(env.getMinY()) > 300 ||
				( env.getMaxX() - env.getMinX() ) > 1000 || ( env.getMaxY() - env.getMinY() ) > 500 ){
				// �̈��񂪂��������E�E�E
				System.out.println("BOUNDS ERROR BBox:" + env );
				env = getFSExtent( source.getFeatures() );
//				System.out.println("GET ERR-bounds:" + env );
			}
		}
		
		
		// �A�t�B���p�����[�^�ݒ�
		smat = new SvgMapAffineTransformer( datum , xySys );
//		g2s = new Transform ( 1.0 , 0.0 , 0.0 , -1.0 , 0.0 , 0.0 ); // �Ƃ肠�����ˁE�E
		
		if ( crstProp.length() > 0 ){
			int i = 0;
			double[] tf = new double[6];
			StringTokenizer st = new StringTokenizer(crstProp , ",");
			while(st.hasMoreTokens()) {
				tf[i] = Double.parseDouble(st.nextToken());
				++i;
			}
			if ( i == 6 ){
				smat.g2s.a = tf[0];
				smat.g2s.b = tf[1];
				smat.g2s.c = tf[2];
				smat.g2s.d = tf[3];
				smat.g2s.e = tf[4];
				smat.g2s.f = tf[5];
			}
		}
		
		Envelope wgsEnv = smat.getWgsBounds( env  );
		maxX = wgsEnv.getMaxX() ;
		minX = wgsEnv.getMinX() ;
		maxY = wgsEnv.getMaxY() ;
		minY = wgsEnv.getMinY() ;
		
		// CRS��Transform���v�Z
		if ( boundHeight > 0 ) {
			smat.g2s.d = - boundHeight / ( maxY - minY );
			smat.g2s.a = - smat.g2s.d;
		} else if ( boundHeight < 0 ){ // �ܓx�o�x�ɑ΂���{���Ŏw�� 2010/10
			smat.g2s.a = - boundHeight;
			smat.g2s.d = boundHeight;
		}
		
		if ( projCenter == 999.0 ){
			projCenter = ( maxY + minY ) / 2.0;
		}
		smat.g2s.a = smat.g2s.a * Math.cos( projCenter * Math.PI / 180.0 );
		
		if ( boundHeight == 0 && projCenter == 0 ){
			// �f�t�H���g�̏ꍇ�� SVG���_�����Ȃ�
		} else if ( boundHeight < 0 && projCenter == 0 ){
			// �ܓx�o�x�̒P���{���̏ꍇ�@����
		} else if ( boundHeight < 0 && projCenter != 0 ){
			// �ܓx�o�x�̒P���{�������A�W���ܐ����w�肵�Ă���ꍇ
			// SVG���_���f�[�^�̒��S���W�ɂ��� // added 2012.07.26
			smat.g2s.e = -smat.g2s.a * ((minX+maxX)/2.0);
			smat.g2s.f = -smat.g2s.d * ((minY+maxY)/2.0);
			
		} else {
			// SVG���_���f�[�^�̍���ɂ���
			smat.g2s.e = -smat.g2s.a * minX;
			smat.g2s.f = -smat.g2s.d * maxY;
		}
		
		// �����Ɛ�����ݒ�
		setNumberFormat(smat.g2s.d , accuracy);
		if ( strokeWidth == 0.0 ){
			strokeWidth = -smat.g2s.d * accuracy / (2.0 * 111111.1); // ���x�̔����̐���
		} else {
			strokeWidth = -smat.g2s.d * strokeWidth / (111111.1); // �\�ߓ����Ă���strokeWidth��[m]�Ƒz�肵�A�ϊ�
		}
		
		if ( fixedStroke > 0 ){ // �L�k�ɑI��Ȃ������̃I�v�V�����̂Ƃ�
			strokeWidth = fixedStroke;
			vectorEffect = true;
		} else if ( fixedStroke < 0 ){ // �����ԍ��ɂ��Ƃ�
			strokeWidth = 0;
			vectorEffect = true;
			strokeWCol = - (int)fixedStroke;
			if ( strokeWCol > readFT.getAttributeCount() ){
				strokeWCol = -1;
			}
		}
		
		
		// POI�e�[�u���𐶐� 2016.2.26 �����Ɉړ�
		vectorPoiShapes vps = null;
		if ( fixedSymbol > 0 ){
			vps = new vectorPoiShapes(fixedSymbol, fixedSymbolSub , nFmt);
		} else {
			vps = new vectorPoiShapes(Math.abs(smat.g2s.d) * ( - fixedSymbol ), Math.abs(smat.g2s.d) * ( - fixedSymbolSub ) , nFmt ) ;
		}
		
		
		// SVG �̃r���[�{�b�N�X�����߂Ă���B �Ƃ肠����(���C����������ƁA���C�����ƂɈႤbounds������ꍇ������H)
		double origX , origY , cWidth , cHeight , fontSize;
		boolean abs = false; // �t�H���g�̃T�C�Y���Œ肷��Ƃ��̃t���O
		int fontSizeCol = 0;
		
		Envelope svgBounds = smat.getSvgBoundsW( wgsEnv  );
		origX =   svgBounds.getMinX();
		origY =   svgBounds.getMinY();
		cWidth =  svgBounds.getWidth();
		cHeight = svgBounds.getHeight();
		
		int hPart = 1;
		int wPart = 1;
		// �������w��ɂ��^�C�������̂΂���
		if ( limitProp > 2 && meshPart == 0){ // 2013.8��BUG�������Ȃ����邽�߁E�E�E
			meshPart = 2;
		}
		if (meshPart > 0 ){
			if (cWidth > cHeight){
				hPart = meshPart;
				wPart = (int)( (meshPart * cWidth) / cHeight);
			} else {
				wPart = meshPart;
				hPart = (int)( (meshPart * cHeight) / cWidth);
			}
			System.out.println("hP:" + hPart + " wP:" + wPart  );
		}
		
		// globalTile���[�h�p�̏��� �V���ȕϐ�(globalPart,globalTile)��ǉ�
		// ���b�V�����x���w��ɂ��^�C�������̏ꍇ
		// origXY,cWH�̓O���b�f�B���O�����
		int[] globalPart = new int[2]; // ���b�V�����x���w��ɂ��^�C�������̕�����
		Envelope globalTileSVG = null; // ���A�f�[�^�G���A
		if (meshLevel >= 0 ){
			Envelope globalTile = getGlobalTileArea( wgsEnv  , meshLevel , globalPart);
			globalTileSVG = smat.getSvgBoundsW( globalTile );
			wPart = globalPart[0];
			hPart = globalPart[1];
			// hPart, wPart , origX , origY , cWidth , cHeight �p
//			System.out.println("original:" + wgsEnv + " lvl:" + meshLevel);
			System.out.println("GlobalMeshLevel:" + globalPart[0]+","+globalPart[1] + ":" + globalTileSVG );
			if ( meshLevel < actualGenerateMeshLevel ){
				actualGenerateMeshLevel = actualGenerateMeshLevel - meshLevel;
			} else {
				actualGenerateMeshLevel = -1;
			}
		} else {
			actualGenerateMeshLevel = -1;
		}
		
		//�t�B�[�`���[�\�[�X����R���N�V�����̎擾
		FeatureCollection<SimpleFeatureType, SimpleFeature> fsShape = source.getFeatures();
//		int featureCount = fsShape.getCount();
		int featureCount = fsShape.size();
		System.out.println("���R�[�h��:" + featureCount );
		
		
		// �w�肵���v���p�e�B�J�����̒l��p���Ċe�p�����[�^��ݒ肷��
		
		//�X�^�C���p�̃J�����̎擾
		Pattern p = Pattern.compile("#[0123456789abcdefABCDEF]{6}");
		
		try{
			linkFlg = Integer.parseInt( linkProp );
			if ( linkFlg > readFT.getAttributeCount() ){
				linkFlg = -1;
			}
		} catch (Exception e){
			linkFlg = -1;
		}
		
		try{
			captionFlg = Integer.parseInt( symbolProp );
			if ( captionFlg > readFT.getAttributeCount() ){
				captionFlg = -1;
			}
		} catch (Exception e){
			captionFlg = -1;
		}
		
		try{
			dupCheck = Integer.parseInt( dupProp );
			if ( dupCheck > readFT.getAttributeCount() ){
				dupCheck = -1;
			}
		} catch (Exception e){
			dupCheck = -1;
		}
		
//		System.out.println("================ColorProp:"+colorProp);
		
		try{ // �v���p�e�B�ˑ��̐F�w��(�v���p�e�B�ԍ��w��)
			colorCol = Integer.parseInt( colorProp );
//			System.out.println("================ColorCol:"+colorCol);
			if ( colorCol > readFT.getAttributeCount() ){
				colorCol = -1;
			}
		} catch (Exception e){ // #�w��̐F�p�����[�^
			colorCol = -1;
			Matcher m = p.matcher(colorProp);
			if ( m.matches() ){
				mainColor = colorProp;
			} else if ( colorProp.equalsIgnoreCase("none") ){
				mainColor = "none";
			}
		}
		
		try{ // �v���p�e�B�ˑ��̐F�w��(�v���p�e�B�ԍ��w��)
			olColorCol = Integer.parseInt( olColorProp );
//			System.out.println("================olColorCol:"+olColorCol);
			if ( olColorCol > readFT.getAttributeCount() ){
				olColorCol = -1;
			}
		} catch (Exception e){ // #�w��̐F�p�����[�^
			olColorCol = -1;
			Matcher m = p.matcher(olColorProp);
			if ( m.matches() ){
				outlineColor = olColorProp;
			} else if ( olColorProp.equalsIgnoreCase("none") ){
				outlineColor = "none";
			}
		}
		
		try{
			layerCol = Integer.parseInt( layerProp );
			if ( layerCol > readFT.getAttributeCount() ){
				layerCol = -1;
			}
		} catch (Exception e){
			layerCol = -1;
		}
		
		//������v���p�e�B���̂���ݒ�
		for ( int i = 0 ; i < readFT.getAttributeCount() ; i++){
			// �v���p�e�B�ˑ��̐F�w��(�v���p�e�B���w��)
			if (colorProp != "" && readFT.getDescriptor(i).getLocalName().equals(colorProp) ){
				colorCol = i;
			}
			if (olColorProp != "" && readFT.getDescriptor(i).getLocalName().equals(olColorProp) ){
				olColorCol = i;
			}
			
			if (readFT.getDescriptor(i).getLocalName().equals(symbolProp) ){
				captionFlg = i;
			}
			if (readFT.getDescriptor(i).getLocalName().equals(dupProp) ){
				dupCheck = i;
			}
			if (readFT.getDescriptor(i).getLocalName().equals(linkProp) ){
				linkFlg = i;
			}
			
			if (readFT.getDescriptor(i).getLocalName().equals(layerProp) ){
				layerCol = i;
			}
			System.out.println("attrNo:" + i +" Name:" + getKanjiProp(readFT.getDescriptor(i).getLocalName()) + 
			" type:" + readFT.getDescriptor(i).getType().getBinding().getSimpleName() ); 
		}
		
		if ( showHead ){
			return;
		}
		
		System.out.println("OriginalDatum:" + ((datum == 1) ? "JGD2000" : "TOKYO BESSEL") );
		System.out.println("OriginalCrd:" + ((xySys != 0) ? (String)("XY" + xySys) : "LatLon") );
		System.out.println("defMainColor:" + mainColor);
		System.out.println("outlineColor:" + outlineColor);
		System.out.println("mainColorCol:" + colorCol);
		System.out.println("outlineColorCol:" + olColorCol);
		System.out.println("POIsizeCol:" + POIsizeCol);
		System.out.println("strokeWCol:" + strokeWCol);
		System.out.println("captionCol:" + captionFlg);
		System.out.println("dupCheckCol:" + dupCheck);
		System.out.println("linkCol:" + linkFlg);
		System.out.println("layerCol:" + layerCol);
		
		
		boolean poiColumnString = false; // poiColumn������^�̏ꍇ�̃t���O 2017.9.15
		boolean poiColumn2String = false;
		// poiColumn,2�̃f�[�^�^�𒲂ׁA������^�̏ꍇ�̓t���O��ݒ肷��
		if ( poiColumn >= 0 ){
			if ( readFT.getDescriptor(poiColumn).getType().getBinding() == Class.forName("java.lang.String")){
				poiColumnString = true;
			}
		}
		if ( poiColumn2 >= 0 ){
			poiColumn2String = poiColumnString; // poiColumn2�̕���poiColumn�Ɠ����łȂ��ƃ_���@����������΃��C�����[�v����exception�ł�
		}
		
		// �J���[���[�e�B���e�B�̏����� (2018.1.26)
		colorUtil = new SVGMapGetColorUtil(fsShape, colorCol, olColorCol, colorTable, colorKeyLength, strIsSJIS, colorKeys);
		colorUtil.setOutOfRangeView(outOfRangeViewMethod);
		
		// �}�`�d���`�F�b�N�̂��߂̑O����
		HashSet<Object> dupHash = new HashSet<Object>();
		
		//�����͈̔͌��� �d����
		if ( colorCol >=0 || olColorCol >=0 || POIsizeCol >=0 ){
			boolean mColor = false;
			boolean oColor = false;
			boolean mSize  = false; // added 2017.7.14
			if ( colorCol >= 0){
				colorClass = readFT.getDescriptor(colorCol).getType().getBinding();
				if ( colorClass == Class.forName("java.lang.Double") || colorClass == Class.forName("java.lang.Integer") || colorClass == Class.forName("java.lang.Long")){
					System.out.println("Searching Extent for mainColor");
					mColor = true;
				}
			}
			if ( olColorCol >= 0){
				olColorClass = readFT.getDescriptor(olColorCol).getType().getBinding();
				if ( olColorClass == Class.forName("java.lang.Double") || olColorClass == Class.forName("java.lang.Integer") || olColorClass == Class.forName("java.lang.Long")){
					System.out.println("Searching Extent for outline Color");
					oColor = true;
				}
			}
			if ( POIsizeCol >= 0){
				sizeClass = readFT.getDescriptor(POIsizeCol).getType().getBinding();
				if ( sizeClass == Class.forName("java.lang.Double") || sizeClass == Class.forName("java.lang.Integer") || sizeClass == Class.forName("java.lang.Long")){
					System.out.println("Searching Extent for poiSize");
					mSize = true;
				}
			}
			if ( mainAttrMax != -9e99 && mainAttrMin != 9e99 ){
				mColor = false; // ����mainAttrMinMax���ݒ肳��Ă���ꍇ�́A������g���Ηǂ�
			}
			if ( sizeAttrMax != -9e99 && sizeAttrMin != 9e99 ){
				mSize = false; // ����mainAttrMinMax���ݒ肳��Ă���ꍇ�́A������g���Ηǂ�
			}
			if ( mColor || oColor || mSize ){
				getAttrExtent(fsShape , mColor , oColor , mSize); // ���̃��[�`�����܂�SVGMapGetColorUtil��݊��E�E
				colorUtil.setAttrExtent( mainAttrMin , mainAttrMax , outlineAttrMin , outlineAttrMax ); // 2018.1.26
			}
		}
		
		String[] layerNames = new String[1024];
		Envelope[] layerBounds = new Envelope[1024];
		
		int layerCount;
		
		//���C���̐��Ɣ͈͂̌���
		if ( layerCol >=0 ){
			layerCount = getLayerNames(fsShape ,  layerNames , layerBounds , layerCol);
			System.out.println("layerBounds:" + layerBounds);
		} else {
			layerNames[0] = "";
			layerCount = 1;
			layerBounds[0] = env;
		}
		
		for ( int layerNumber = 0 ; layerNumber < layerCount ; layerNumber ++ ){
			String outFileName;
			if ( layerCount > 1 ){
				outFileName = outfile.substring(0 , outfile.indexOf(".svg")) + "_" + layerNames[layerNumber] + ".svg";
			} else {
				outFileName = outfile;
			}
			System.out.println("Layer:" + outFileName);
			// �ċA�����p
			int level = 0;
//			boolean[][] tileExistence = new boolean[wPart][hPart];
			HashSet<Long> tileExistence = new HashSet<Long>();
			
			boolean outOfSize = true;
			Vector<HashSet<Long>> rTiles =new Vector<HashSet<Long>>(); // tileExistence�����x�����ɑ��K�w�Œ~�ς�������
			Vector<HashSet<Long>> rTileElements =new Vector<HashSet<Long>>(); // thisTileHasElements�����x�����ɑ��K�w�Œ~��
			sm0 = new SvgMapTilesTM( ); // 
			sm0.isSvgTL = isSvgTL;
			
			boolean topContainer = true;
			
			
			// �ȉ��̈�A��(���C���[���Ƃ�)SVG�̈�ݒ�́A
			// �^�C�����O�ɂ����đz��O�̏�������������̂ł͂Ȃ����H(2013/3/28)
			// �e���C���[�́A�قȂ�̈�������Ă���̂ɁAhp,wp�́A���̑O�ō���Ă��܂��Ă�
			wgsEnv = smat.getWgsBounds( layerBounds[layerNumber]  );
			System.out.println( "WgsEnvelope:" + wgsEnv );
			svgBounds = smat.getSvgBoundsW( wgsEnv  );
			origX =   svgBounds.getMinX();
			origY =   svgBounds.getMinY();
			cWidth =  svgBounds.getWidth();
			cHeight = svgBounds.getHeight();
			
			// globalTileMesh���[�h�ł͏�L�̖��͂Ƃ肠�������������Ă���H 2013/3/28
			if ( meshLevel >= 0 ){
				origX = globalTileSVG.getMinX();
				origY = globalTileSVG.getMinY();
				cWidth = globalTileSVG.getWidth();
				cHeight = globalTileSVG.getHeight();
			}
			
			if ( fixedFont == 0.0 ){
				defaultFontSize = Math.sqrt(cWidth * cHeight / ( featureCount * 300.0 ));
				abs = false;
			} else if ( fixedFont > 0 ) { 
				defaultFontSize = fixedFont;
				abs = true;
			} else { // �}�C�i�X�̏ꍇ�́A-[�����ԍ�]�̑����l��p����@�T�C�Y�͌Œ� ���L�k���Ȃ�����
				defaultFontSize = Math.sqrt(cWidth * cHeight / ( featureCount * 300.0 ));
				fontSizeCol = - (int) fixedFont ;
				abs = true;
			}
			fontSize = defaultFontSize;
			
			HashMap<Long,Integer> divErr = new HashMap<Long,Integer>();
			
			while ( outOfSize ){
				if ( meshLevel > 0 ){
					System.out.println("meshLevel:" + (level + meshLevel) + "  h:" + hPart + "  w:" + wPart);
				} else {
					System.out.println("Level:" + level + "  h:" + hPart + "  w:" + wPart);
				}
				
				// SVG�̏o�͊J�n 
				sm = new SvgMapTilesTM( outFileName , nFmt , origX , origY , cWidth , cHeight , wPart , hPart , level , tileExistence , smat , vps , isSvgTL, maxThreads, svgMapExecutorService );
				
				sm.setDefauleCustomAttr(false); // 2016/10/31 customAttr�͌X�̗v�f�ɕt����(<g>�ɂ͕t���Ȃ�)
				
				
				if ( threadBuffer > 1023 ){
					sm.bufferedDrawSize = threadBuffer;
				}
				
//				System.out.println("sm Build Size:" + SizeOf.deepSizeOf(sm));
				sm.bitimageGlobalTileLevel = bitimageGlobalTileLevel; // ���x��density control�padded 2013.3.28
				
				// �����I�Ƀ��x�����X�L�b�v������
				if ( level < actualGenerateMeshLevel ){
					sm.setForceOos(true);
				}
				
				// �V�F�[�v�t�@�C���̊����R�[�h�ݒ� (CSV�̎��͂����Őݒ肷��K�v�͂Ȃ��B���͎��ɋz���ł���)
				if ( !inputCsv && strIsSJIS == false ){
					sm.strIsSJIS = false;
				}
				
				// �f�o�b�O�p�̘g��
				sm.tileDebug = layerDebug;
				
				// ���~�b�^�ݒ�
				if ( limitProp > 2 ){
					sm.setLimitter( limitProp * 1024 );
				}
				
				//micro metadata�p�̐ݒ������B
				if (microMetaEmbed || microMeta2Embed){
					sm.noMetaId = noMetaId;
					
					// �w�肳��Ă��Ȃ��ꍇ(metaIndex���J��)�͑S���o�����߂ɁA�S�����������o��
					if ( metaIndex.size() == 0 ){
						for ( int i = 0 ; i < readFT.getAttributeCount() ; i++){
							metaIndex.put(new Integer(i) , "");
						}
					}
					
					
					linkedMetaIndex = new Integer[metaIndex.size()];
					linkedMetaName = new String[metaIndex.size()];
					Iterator it = metaIndex.keySet().iterator();
					int i = 0;
					while(it.hasNext()){
						Integer key = (Integer)it.next();
						
						linkedMetaIndex[i] = key;
						if ( (String)metaIndex.get(key) ==""){ // �蓮�ő��������w�肳��ĂȂ��ꍇ
							linkedMetaName[i] = getKanjiProp(readFT.getDescriptor(key.intValue()).getLocalName());
						} else {
							linkedMetaName[i] = (String)metaIndex.get(key);
						}
//						System.out.println( "meta::: " + i + " : " + linkedMetaIndex[i] + " , " + linkedMetaName[i] );
						++i;
					}
					
//					linkedMetaIndex = (Integer[])metaIndex.toArray(new Integer[0]);
					
					sm.linkedMetaIndex = linkedMetaIndex;
					sm.linkedMetaName = linkedMetaName;
					
					if ( microMetaEmbed ){
						sm.setMicroMetaHeader( readFT , metaNs , metaUrl );
					} else {
						sm.setMicroMeta2Header( readFT , true );
					}
				}
				
				// SVG �̃w�b�_�����Ȃ�
				sm.putHeader();
				
				//�Q�l�f�[�^�̃v�����g�A�E�g
				sm.putComment(" �Q�l�f�[�^�ł�");
				sm.putComment("Tramsform:"  + smat.g2s  );
				sm.putComment("��ܐ�:"  + projCenter  );
				sm.putComment("Bounds:"  + layerBounds[layerNumber]  );
				sm.putComment("TypeName:" + readFT.getTypeName()   );
				sm.putComment("Count:" + readFT.getAttributeCount()  );
				sm.putComment("NS:" + readFT.getName() );
				sm.putComment("Geom:" + readFT.getGeometryDescriptor() ); 
				for ( int i = 0 ; i < readFT.getAttributeCount() ; i++){
					String atn = getKanjiProp(delCC(readFT.getDescriptor(i).getLocalName()));
					sm.putComment("attrNo:" + i +" Name:" + atn +
					" type:" + readFT.getDescriptor(i).getType().getBinding().getSimpleName()  ); 
				}
			
				sm.putComment("OriginalDatum:" + ((datum == 1) ? "JGD2000" : "TOKYO BESSEL") );
				sm.putComment("OriginalCrd:" + ((xySys != 0) ? (String)("XY" + xySys) : "LatLon") );
				sm.putComment("defMainColor:" + mainColor );
				sm.putComment("outlineColor:" + outlineColor );
				sm.putComment("mainColorCol:" + colorCol );
				sm.putComment("outLineColorCol:" + olColorCol );
				sm.putComment("POIsizeCol:" + POIsizeCol );
				sm.putComment("captionCol:" + captionFlg );
				sm.putComment("dupCheckCol:" + dupCheck );
				sm.putComment("linkCol:" + linkFlg );
				//out.write("Type:"  + readFT.getNamespace() + "\n");
				
				// �f�t�H���g�X�^�C������Ɏg��(2010/06/22) 
				useDefaultStyle = true;
				prevType = 0;
				
				System.out.println("useDefaultStyle:" + useDefaultStyle );
				
				// CRS���^�f�[�^���o��
				if ( layerMetadata.length() > 0 ){
					sm.setLayerMetadata(layerMetadata);
				}
				sm.putCrs( smat.g2s.a , smat.g2s.b , smat.g2s.c , smat.g2s.d , smat.g2s.e , smat.g2s.f );
				
				// GIS���^�f�[�^�o�͂�ݒ�
				if (metaEmbed ){
					sm.setMetadata(readFT  , fsShape , metaNs , metaUrl );
				}
				
				if ( directPoi != 0 ){ // POI���V���{����`���g�킸�ɏo�͂���ꍇ(directPoi���[�h)
						System.out.println("setDirectShapeSymbol = true");
					if ( directPoi >0){ // �J�����ԍ�����POI�̔ԍ��𓾂� 0�J����(geometry)�͎g���Ȃ�
						System.out.println("  SymbolTypeCol:" + directPoi);
					}
					sm.setDefaultPoiSize(  accuracy * (-smat.g2s.d) / 111111.1  );
				} else {
					// �V���{����`�̏o��
//					System.out.println("OUTPUT SYMBOL");
					if ( symbolTemplate != "" ){ // �O������e���v���[�g���g�p���ăV���{����ݒ肷��ꍇ 2013.3.11
//						System.out.println("OUTPUT EXTERNAL SYMBOL");
						sm.putSymbol( symbolTemplate );
						if ( fixedSymbol <= 0 ){
							fixedSymbol = 16 ; // �O������e���v���[�g���g�p���ăV���{����ݒ肷��ꍇ�́Afixed��p�ɂ��āA16px�̃V���{���ƌ����Ăď�������E�E(caption shift�ɉe��)
						}
					} else {
						if ( fixedSymbol == 0 ){ // �V���{�����Œ肵�Ȃ��ꍇ
							sm.putSymbol( cWidth / 500.0 ,fixedStroke); // ����͂��܂�ɂ������� 2012/04
						} else if ( fixedSymbol > 0 ){ // �V���{�������T�C�Y�ŌŒ肷��ꍇ�idefault�F6�j
							sm.putSymbol( fixedSymbol/2.0 ,fixedStroke); // �T�C�Y�𒼌a�ɂ����̂ŁE�E 2016/2/26
						} else { // �V���{�����Œ肵�Ȃ����A�����T�C�Y[m]���w�肵�Ă���ꍇ
//							sm.putSymbol( Math.abs(smat.g2s.d) * ( - (fixedSymbol/2.0) * 360 / 40000000) ,fixedStroke) ;
							sm.putSymbol( Math.abs(smat.g2s.d) * ( - (fixedSymbol/2.0) ) ,fixedStroke) ; // 2016/2/26 m->deg�ύX
							fixedSymbol = 0;
							System.out.println("�V���{�����Œ肵�Ȃ����A�����T�C�Y[m]���w��");
						}
					}
				}
				
				
				//�R���N�V�������C�e���[�^�ɐݒ�
				FeatureIterator<SimpleFeature> reader = fsShape.features();
				Coordinate capCrd = new Coordinate();
				lop=0;
				dupHash.clear();
				int dupCount = 0;
				
				SimpleFeature oneFeature = null;
				Geometry oneGeom;
				boolean hasAnchor = false;
				boolean hasFeature;
				
				int topCounter = topCount;
				int poiType = -1; // �V���{����"P"+ID�ԍ�
				String poiSymbolId ="";
				
				// �f�[�^�������o�����C�����[�v
				while ( reader.hasNext() && topCounter != 0 ) {
					hasFeature = false; // �G���[���o��Ƃ����L��̂ŃG���[���p 09/01/xx
					while ( hasFeature == false ){
						try{
							oneFeature = reader.next();
							hasFeature = true;
						} catch ( Exception e ){
							System.out.print("rERR");
							hasFeature = false;
						}
					}
					
					if ( dupCheck >=0 ){
						if ( ! dupHash.add(oneFeature.getAttribute(dupCheck))){
							++ dupCount;
							continue; // �d������������X���[����
						}
					}
					
					
					// �����ɂ�郌�C���[�������s���ꍇ
					if ( layerCol >=0 ){
							if ( ! (getKanjiProp(oneFeature.getAttribute(layerCol).toString())).equals(layerNames[layerNumber]) ){
								continue;
							}
					}
					if ( linkFlg >= 0 ){ // 2016/10/31 xlink:title�ɗ��񂾃o�O(micrometa2�Əd��)�΍�
						sm.useTitleAttr = false;  // micrometa�ł�xlink:title�}�� ���ꂢ�łȂ����ǁE�E
					}
					
					if ( metaEmbed ){
						sm.setId( );
					} else if ( microMetaEmbed ){
						sm.setMicroMeta( oneFeature );
					} else if ( microMeta2Embed ){
						sm.setMicroMeta2( oneFeature );
					}
					
					if (colorCol >= 0 ){
						mainColor = colorUtil.getColor( oneFeature.getAttribute(colorCol) , mainAttrMin , mainAttrMax);
					} 
					if (olColorCol >= 0 ){
						outlineColor = colorUtil.getColor( oneFeature.getAttribute(olColorCol) , outlineAttrMin , outlineAttrMax);
					} 
					if (mainColor.length() == 0 ){
						continue;
					}
					if (POIsizeCol >= 0 ){
						POIsize = getPOIsize(oneFeature.getAttribute(POIsizeCol) , sizeAttrMin , sizeAttrMax , sizeRangeMin , sizeRangeMax );
					}
					
					// POI�̘b�E�E�������쐬���i�܂��G���[�`�F�b�N�s���S�����܂� 2012/7/30�j
					if ( directPoi > 0){ // directPoi���J�����ԍ�����POI�ԍ��𓾂�
						poiType = getPoiTypeNumber(oneFeature.getAttribute(directPoi));
					} else if ( directPoi < 0){ // ���ڎw��
						poiType = - directPoi;
					} else if ( poiColumn >= 0 ){
						if ( !poiColumnString ){
							if ( poiColumn2 >= 0 ){
								poiType = getPoiTypeNumber(oneFeature.getAttribute(poiColumn)) + getPoiTypeNumber(oneFeature.getAttribute(poiColumn2));
							} else {
								poiType = getPoiTypeNumber(oneFeature.getAttribute(poiColumn));
							}
						} else {
							poiType = -1;
							if ( poiColumn2 >= 0 ){
								poiSymbolId = getKanjiProp((String)oneFeature.getAttribute(poiColumn)) + getKanjiProp((String)oneFeature.getAttribute(poiColumn2));
							} else {
								poiSymbolId = getKanjiProp((String)oneFeature.getAttribute(poiColumn));
							}
						}
					} else if ( customPoiType >= 0 ){
						poiType = customPoiType;
					} else {
						poiType = 0;
					}
					
					if (strokeWCol >= 0 ){
						Object sw = oneFeature.getAttribute(strokeWCol);
						if ( sw instanceof String ){
							try{
								strokeWidth = Double.parseDouble((String)sw);
							} catch ( Exception e ) {
							}
						} else if ( sw instanceof Number ){
							strokeWidth = ((Number)sw).doubleValue();
						}
					}
					
					if ( linkFlg >= 0 ){ // 2016/10/31 href���Ȃ��̂�<a>��݂���K�v���Ȃ����@�̉���
						String unicodeStirng = getKanjiProp((String)oneFeature.getAttribute(linkFlg));
						if ( hrefFlg >= 0 ){ // ���̂Ƃ��낱�̃t���O�͌����ė����Ȃ�
							String lnkString =  getKanjiProp((String)oneFeature.getAttribute(hrefFlg));
							if ( unicodeStirng.length() > 0 ){
								sm.setAnchor( unicodeStirng , lnkString );
								hasAnchor = true;
							}
						} else { // 2016/10/31 customAttr�Ƃ���xlink:title��t���Ă���B
							sm.setCustomAttribute("xlink:title=\""+sm.htmlEscape(unicodeStirng)+"\" ");
//							sm.setAnchor( unicodeStirng , "" );
//							hasAnchor = true;
						}
					}
					
					if ( putRecord ){
						sm.setCustomAttribute("data-record=\""+sm.htmlEscape(Integer.toString(lop))+"\" ");
					}
					
					oneGeom = (Geometry) oneFeature.getDefaultGeometry();
					
					if ( ! noShape ){
						if ( simplify ){ // �}�`�̊ȒP��
							if ( TopoPresSimp ){
								oneGeom = Tsimplifier.simplify( oneGeom , simplifyParam );
							} else {
								oneGeom = Dsimplifier.simplify( oneGeom , simplifyParam );
							}
							
						}
						
						parseGeometry(oneGeom , sm , poiType , poiSymbolId ); // �ϊ��@�\�̖{�̂��Ăяo��
					} else if ( lop == 0 ) { // �L���v�V�����݂̂ōŏ��̃��[�v�̂Ƃ��Ƀf�t�H���g�X�^�C���ݒ肷��
						setCapOnlyDefaultStyle( sm );
					}
					if ( captionFlg >= 0 ){
						if ( fontSizeCol > 0 ){
							Object fs = oneFeature.getAttribute(fontSizeCol);
							if ( fs instanceof String ){
								try{
									fontSize = Double.parseDouble((String)fs);
								} catch ( Exception e ){
								}
							} else if ( fs instanceof Number ){
								fontSize = ((Number)fs).doubleValue();
							}
						} else {
							// fontSize = defaultFontSize;
						}
						double capSft;
						if (prevType == 3 && abs && fixedSymbol > 0.0 ){
							// �W�I���g����Point�ł��̃V���{���T�C�Y���Œ�ŃL���v�V�����T�C�Y���Œ�(abs)�̏ꍇ�L���v�V�������V�t�g����
							capSft = -(fixedSymbol/2.0);
						} else {
							capSft = 0;
						}
						putCaption( smat.transCoordinate(oneGeom.getEnvelopeInternal().centre())
							, oneFeature.getAttribute(captionFlg) , fontSize , sm , abs , capSft);
						
					}
					if ( hasAnchor ){
						sm.termAnchor();
						hasAnchor = false;
					}
					++ lop;
					if ( lop % 10000 == 0 ){
						System.out.print("O");
					} else if ( lop % 1000 == 0 ){
						System.out.print(".");
					}
					if (sm.allTilesOOS){
						System.out.println("\nAll tiles are out of size. Skip to next level.");
						break;
					}
					
					if ( topCounter > 0 ){
//						System.out.println("c:" + topCounter);
						--topCounter;
					}
				
				}
				//limemerge�ŁA�c�������̂��o�͂���B
				if ( lineMerge && lineList.size() > 0 ){
//					System.out.println("merge:" + lineList.size());
					mergeAndDrawLineList ( lineList , mainColor , strokeWidth , opacity , sm );
					lineList = new Vector<LineString>();
				}
				sm.putComment("Total:"+lop+" records." );
				if (colorCol >=0 && colorUtil.colorMap.size() <= 128 ){
					sm.putComment( "colorMap(" + colorUtil.colorMap.size() + "vals : under 128vals):" + colorUtil.colorMap );
					System.out.println( "colorMap(" + colorUtil.colorMap.size() + "vals : under 128vals):" + colorUtil.colorMap );
				}
				System.out.println("Total:"+lop+" records." );
				if ( dupCheck >=0 ){
					sm.putComment("Total:"+dupCount+" duplicates." );
					System.out.println("Total:"+dupCount+" duplicates." );
				}
				sm.defaultFill = mainColor;
				divErr = sm.putFooter(densityControl, divErr );
				
//				System.out.println( "divCheck (true:ok,false:exception): " + sm.divCheck( divErr, 3 ) );
				
//				if ( sm.divCheck( divErr, 3 ) == false ){ //}���Ƃ��Ƃ�3�ł��� 2016.3.14
				if ( sm.divCheck( divErr, divErrMax , level ) == false ){ // 2016.3.15 ���f���W�b�N����������
					System.out.println("No div effect at all...... HALT PROGRAM...");
					outOfSize = false;
				} else if ( meshLevel > 0 && (level + meshLevel ) >= maxLevel){ // 2017.4.20
					System.out.println("Tile div level exceeds limitter :"+ maxLevel + "..   Treminate processing.");
					// �{���A�����ł܂��o�͂ł��Ă��Ȃ��^�C�����̃f�[�^�𐶐�������ʃ^�C�����\�z����ׂ�(TODO)
					outOfSize = false;
				} else {
					outOfSize = sm.outOfSize;
				}
				reader.close();
				
//				tileExistence = sm.getThisTileExsitenceArray();
				tileExistence = sm.getThisTileExistenceSet();
				//�^�C���̑��݂ƃ^�C�����̐}�`�v�f�̑��݂̃t���O���K�w�I�ɒ~�ς���
				rTiles.addElement( tileExistence );
//				rTileElements.addElement( sm.getThisTileHasElementsArray() );
				rTileElements.addElement( sm.getThisTileHasElementsSet() );
				//��ԏ�̊K�w�̃R���e���c���쐬���Ă���Ƃ��A�����sm0�ŕێ����Ă���
				if ( sm.tiled && topContainer ){
					// limit�ɂ�镪���ł́Alevel=1��sm0�ɂȂ�(����͔ώG�ɂȂ邽�ߔp�~ 2013.8.6)
					// mesh�ɂ�镪���ł�level=0��sm0�ɂȂ�
					// limit��mesh�̗��w��ł�level=0��sm0�ɂȂ�(���̂Ƃ�BUG������H�����Ȃ��̂ł�)�@����������2015.5.15
					sm0 = sm;
					topContainer = false;
					System.out.println("ContainerLevel:" + sm0.level );
				}
				
				// �Ђ��[�Ճ`�F�b�N
				if ( heapCheck ){
					System.out.printf("�ő�T�C�Y�F%10d%n", usage.getMax());
					System.out.printf("�g�p�T�C�Y�F%10d%n", usage.getUsed());
//					System.out.println("rTiles Size:" + SizeOf.deepSizeOf(rTiles));
//					System.out.println("sm Size:" + SizeOf.deepSizeOf(sm));
				}
				
				++ level;
			} //�K�w�I�^�C���������[�v�̏I��
			
			//�R���e�i���쐬����B�ꍇ�ɂ���ẮA�K�w�I�ȃR���e�i�ɂȂ�
//			if ( wPart * hPart > 1 ){ //}
			if ( sm0.tiled ){ // debug 2015.5.15 ����ŊK�w�R���e�i�̃��t�@�N�^�����O�ɔ����o�O�������H
//			if ( level > 2 || ( wPart * hPart > 1 && level > 1) ){ // }
//				if ( layerDebug ){
//					sm0.tileDebug = true;
//				}
				if ( pStep > 1 ){
					sm0.pStep = pStep;
				}
				sm0.defaultFill = mainColor;
				sm0.createRContainer2(rTiles , rTileElements , densityControl);
				
			}
			if ( meshLevel >= 0 ){
				System.out.println("globalTileLevel:" + meshLevel + " .. " + (meshLevel + rTiles.size() - 1) );
			}
		} //�����l��p�������C���[�������[�v�̏I��
		
		if ( layerCount > 1 ){
			buildHyperLayerContainer(outfile , nFmt , env , layerCount , layerNames , layerBounds);
		}
		
		svgMapExecutorService.shutdown();
	
	}
	
	private void setCapOnlyDefaultStyle( SvgMapTilesTM sm ) throws Exception {
		if ( captionFlg >= 0 ){
			sm.setDefaultCaptionStyle( defaultFontSize , false );
			sm.setDefaultStyle( mainColor , -1 , "" , opacity , false );
		}
	}
	
	private void setDefaultStyle( int mode , SvgMapTilesTM sm ) throws Exception{ // 1:Polygon , 2:Line , 3:Point   2009/01/31
		
		switch (mode) {
		case 1: // Polygon
			if ( outlineColor != "" ){ // �g���t���̏ꍇ�́A�g���̐F�Ƒ������ݒ�
				if ( captionFlg >= 0 ){
					// �g������ꍇ�͕����̗֊s����(true)
					sm.setDefaultCaptionStyle( defaultFontSize  , true );
				}
				sm.setDefaultStyle( mainColor , strokeWidth , outlineColor , opacity , vectorEffect);
			} else { // �g���Ȃ��̏ꍇ�͓h��̐F�Ɠ����x����
				if ( captionFlg >= 0 ){
					// �g���Ȃ��ꍇ�͕����̗֊s�ݒ肵�Ȃ��ŗǂ�(false)
					sm.setDefaultCaptionStyle( defaultFontSize  , false );
				}
				sm.setDefaultStyle( mainColor , -1 , "" , opacity , false );
			}
			break;
		case 2: // Line
			if ( captionFlg >= 0 ){
				// ���̏ꍇ�͕����̗֊s����(true)
				sm.setDefaultCaptionStyle( defaultFontSize  , true );
			}
			// ���̏ꍇ�͓h��͊֌W�Ȃ������炸�A���C���J���[�͓h��ł͂Ȃ��Đ��̐F�ɂȂ�
			sm.setDefaultStyle( "none" , strokeWidth , mainColor , opacity , vectorEffect );
			break;
		case 3: // Point
			if ( captionFlg >= 0 ){
				// �_�̏ꍇ�͕����̗֊s�ݒ肵�Ȃ��ŗǂ�(false)
				sm.setDefaultCaptionStyle( defaultFontSize  , false );
			}
			// �_�̏ꍇ�h�肾���ݒ肷��
			sm.setDefaultStyle( mainColor , -1 , "" , -1 , false );
			break;
		default:
			//�Ȃɂ����Ȃ��H
			// sm.setDefaultStyle( null , null , null , null , null );
			break;
		}
		
	}
	
	// ���������W�b�N�p
	boolean lineMerge = false;
	String prevMainColor = "";
	double prevStrokeWidth = 0;
	double prevOpacity = 0;
	Vector<LineString> lineList = new Vector<LineString>();
	
	
	// �}�`�ϊ��@�\
	private int prevType = 0; // �ЂƂ܂��̃W�I���g���̃^�C�v 1:Polygon , 2:LineString , 3:Point
	public boolean useDefaultStyle = true;
	private void parseGeometry(Geometry geom , SvgMapTilesTM sm , int poiType , String poiSymbolId ) throws Exception {
		Coordinate[] coord , coord0;
//		Coordinate oneCrd = new Coordinate();
		Coordinate oneCrd ;
		PolygonDouble pol;
		Envelope svgEnv;
		if (geom instanceof Polygon ){
			if ( useDefaultStyle && prevType != 1 && !outGroup){ //<g>���g���ăX�^�C�����܂Ƃ߂ăT�C�Y���팸���鏈��
				setDefaultStyle(1 , sm);
			}
			svgEnv = smat.getSvgBounds( geom.getEnvelopeInternal() );
			coord = (((Polygon)geom).getExteriorRing()).getCoordinates();
			pol = smat.transCoordinates(coord);
			sm.setExterior( pol , svgEnv );
			
			for ( int j = 0 ; j < ((Polygon)geom).getNumInteriorRing() ; j++ ){
				coord = (((Polygon)geom).getInteriorRingN(j)).getCoordinates();
				pol = smat.transCoordinates(coord);
				sm.setInterior( pol , svgEnv );
			}
			sm.putPolygon( mainColor , strokeWidth , outlineColor , opacity );
			prevType = 1;
		} else if (geom instanceof LineString ){
			if ( useDefaultStyle && prevType != 2 && !outGroup){
				setDefaultStyle(2 , sm);
			}
			if (  lineMerge ){ // ���̌������W�b�N�𓮂���
//				System.out.print("M");
				if ( prevType == 2 &&  prevMainColor.equals(mainColor) && prevStrokeWidth == strokeWidth && prevOpacity == opacity ){
					// �X�^�C���������Ȃ烉�C���X�g�����O���}�[�W���X�g�ɉ����Ă���
					lineList.add((LineString)geom);
				} else {
					// �Ⴄ���̂�������A�܂��͏o�͂��āA���̌�V�������X�g�̍쐬���J�n����B
					if ( lineList.size() > 0 ){
						mergeAndDrawLineList ( lineList , mainColor , strokeWidth , opacity , sm );
					}
					lineList = new Vector<LineString>();
					lineList.add( (LineString)geom );
					
					prevOpacity = opacity;
					prevMainColor = mainColor;
					prevStrokeWidth = strokeWidth;
				}
			} else {
				svgEnv = smat.getSvgBounds( geom.getEnvelopeInternal()  );
				coord = ((LineString)geom).getCoordinates();
				pol = smat.transCoordinates(coord);
				sm.putPolyline( pol , mainColor , strokeWidth , svgEnv , opacity );
			}
			prevType = 2;
		} else if (geom instanceof Point ){
			if ( useDefaultStyle && prevType != 3 && !outGroup){
				setDefaultStyle(3 , sm);
			}
			oneCrd = smat.transCoordinate(((Point)geom).getCoordinate() );
			if ( directPoi == 0 ){ // �V���{��use�̏ꍇ
				if ( fixedSymbol == 0 ){ // �V���{���T�C�Y���Œ�łȂ��΂���
					if ( poiType >= 0 ){
						sm.putUse( oneCrd , mainColor , false , "p" + poiType );
					} else {
						sm.putUse( oneCrd , mainColor , false , poiSymbolId );
					}
				} else { // �V���{���T�C�Y���Œ�̏ꍇ
					if ( poiType >= 0 ){
						sm.putUse( oneCrd , mainColor , true  , "p" + poiType );
					} else {
						sm.putUse( oneCrd , mainColor , true  , poiSymbolId );
					}
				}
			} else { // directPOI�̏ꍇ
				if ( poiType == -1 ){
					poiType = 9;
				}
//				System.out.println("POIT:"+poiType);
				if ( fixedSymbol <= 0 ){ // �V���{���T�C�Y���Œ�łȂ��΂��� 2014.5.12
					sm.putPoiShape( oneCrd , poiType , POIsize , mainColor , 3 , outlineColor , true , false );
				} else {
//					���W, �^�C�v(0..11), �T�C�Y, �h�F, ����, ���F, nonScale����, nonScale�}�`
//					sm.putPoiShape( oneCrd , poiType , -1 , mainColor , 3 , outlineColor , true , true );
					sm.putPoiShape( oneCrd , poiType , POIsize , mainColor , 0 , outlineColor , true , true );
				}
			}
			prevType = 3;
		} else if (geom instanceof GeometryCollection ){
//http://www.jump-project.org/docs/jts/1.7/api/com/vividsolutions/jts/geom/GeometryCollection.html
			if ( outGroup ){
				sm.setGroup();
			} else { // GeometryCollection�ɂQ�ȏ�Geom������ƂQ�ڈȍ~�̃��^�f�[�^���Ȃ��Ȃ��Ă��܂�
				sm.setShadowGroup();
			}
			for ( int j = 0 ; j < ((GeometryCollection)geom).getNumGeometries() ; j++){
				Geometry childGeom = ((GeometryCollection)geom).getGeometryN(j);
				if ( childGeom.getNumPoints() > 0 ){ // �G���[�����ǉ�-���ʂ��������f�[�^�͂Ȃ��͂������ǁE�E
					parseGeometry(childGeom , sm , poiType , poiSymbolId );
				}
			}
			if ( outGroup ){
				sm.termGroup();
			} else {
				sm.termShadowGroup();
			}
//			prevType = 0;
		} else if (geom instanceof Geometry ){
			sm.putComment("Type: Other Geometry...." + geom );
//			prevType = 0;
		} else if (geom instanceof Object){
			sm.putComment("Type: Other Object...." + geom );
//			prevType = 0;
		}
	}
	
	
	private void mergeAndDrawLineList ( Vector<LineString> lineList , String mainColor , double strokeWidth , double opacity , SvgMapTilesTM sm ) throws Exception {
		Coordinate[] coord;
		Envelope svgEnv;
		PolygonDouble pol;
		LineMerger merger = new LineMerger();
		System.out.print("m");
		for(int i = 0; i < lineList.size(); i++) {
//			System.out.println("+adding");
			
			LineString lines = lineList.get(i);
//			System.out.println("p" + lines.getNumPoints() );
			try{
				merger.add(lines);
			} catch ( Exception e ){
//				System.out.print("E");
			}
//			System.out.println("added");
		}
//		System.out.println("end mergeradd");
		java.util.Collection mergedLines = merger.getMergedLineStrings();
		for(Iterator lineStrings = mergedLines.iterator(); lineStrings.hasNext();) {
			LineString line = (LineString)lineStrings.next();
			svgEnv = smat.getSvgBounds( line.getEnvelopeInternal()  );
			coord = ((LineString)line).getCoordinates();
			pol = smat.transCoordinates(coord);
			sm.putPolyline( pol , mainColor , strokeWidth , svgEnv , opacity );
		}
	}
	
	private int getPoiTypeNumber( Object sValue ){
		int iValue = -1;
		if ( sValue instanceof Integer ){
			iValue = ((Integer)sValue).intValue();
		} else if ( sValue instanceof Double ){
			iValue = ((Double)sValue).intValue();
		} else if ( sValue instanceof Long ){
			iValue = ((Long)sValue).intValue();
		}
		return ( iValue);
	}
	
	
	
	// added 2017/07/14 POI�̃T�C�Y�𑮐��l����ݒ肳����
	private double getPOIsize( Object sValue , double attrMin , double attrMax , double sizeMin , double sizeMax ){
		double dValue = 0.0;
		if ( sValue instanceof Integer ){
			dValue = ((Integer)sValue).doubleValue();
		} else if ( sValue instanceof Double ){
			dValue = ((Double)sValue).doubleValue();
		} else if ( sValue instanceof Long ){
			dValue = ((Long)sValue).doubleValue();
		}
		
		if ( dValue < attrMin ){
			return ( sizeMin );
		} else if ( dValue > attrMax ){
			return ( sizeMax );
		} else {
			return (( ( dValue - attrMin ) / ( attrMax - attrMin ) ) * ( sizeMax - sizeMin ) + sizeMin );
		}
	}	
	
	double mainAttrMax = -9e99;
	double mainAttrMin = 9e99;
	double outlineAttrMax = -9e99;
	double outlineAttrMin = 9e99;
	
	// added 2017.7.14
	double sizeAttrMax = -9e99;
	double sizeAttrMin = 9e99;
	double sizeRangeMax = 24;
	double sizeRangeMin = 3;
	
	private void getAttrExtent(FeatureCollection<SimpleFeatureType, SimpleFeature> fsShape , boolean mColor , boolean oColor , boolean mSize ){
		SimpleFeature oneFeature = null;
		Object valueM , valueO , valueS;
		double dvalM = 0;
		double dvalO = 0;
		double dvalS = 0;
		FeatureIterator<SimpleFeature> reader = fsShape.features();
		boolean hasFeature;
		while (reader.hasNext()) {
			hasFeature = false;
			while ( hasFeature == false ){
				try{
					oneFeature = reader.next();
					hasFeature = true;
				} catch ( Exception e ){
					System.out.print("attrERR");
					hasFeature = false;
				}
			}
			if ( mColor ){
				valueM = (oneFeature.getAttribute(colorCol));
				if ( valueM != null ){
					if ( valueM instanceof Double ){
						dvalM = ((Double)valueM).doubleValue();
					} else if ( valueM instanceof Integer ){
						dvalM = ((Integer)valueM).intValue();
					} else if ( valueM instanceof Long ){
						dvalM = ((Long)valueM).longValue();
					}
					if ( dvalM > mainAttrMax ){
						mainAttrMax = dvalM;
					}
					if ( dvalM < mainAttrMin ){
						mainAttrMin = dvalM;
					}
				}
			}
			if ( oColor ){
				valueO = (oneFeature.getAttribute(olColorCol));
				if ( valueO != null ){
					if ( valueO instanceof Double ){
						dvalO = ((Double)valueO).doubleValue();
					} else if ( valueO instanceof Integer ){
						dvalO = ((Integer)valueO).intValue();
					} else if ( valueO instanceof Long ){
						dvalO = ((Long)valueO).longValue();
					}
					if ( dvalO > outlineAttrMax ){
						outlineAttrMax = dvalO;
					}
					if ( dvalO < outlineAttrMin ){
						outlineAttrMin = dvalO;
					}
				}
			}
			if ( mSize ){
				valueS = (oneFeature.getAttribute(POIsizeCol));
				if ( valueS != null ){
					if ( valueS instanceof Double ){
						dvalS = ((Double)valueS).doubleValue();
					} else if ( valueS instanceof Integer ){
						dvalS = ((Integer)valueS).intValue();
					} else if ( valueS instanceof Long ){
						dvalS = ((Long)valueS).longValue();
					}
					if ( dvalS > sizeAttrMax ){
						sizeAttrMax = dvalS;
					}
					if ( dvalS < sizeAttrMin ){
						sizeAttrMin = dvalS;
					}
				}
			}
		}
		if ( colorCol != -1  ){
			System.out.println( "mainColorAttr    Col:" + colorCol + ": Min:" + mainAttrMin + " Max:" + mainAttrMax );
		}
		if ( olColorCol != -1 ){
			System.out.println( "outlineColorAttr Col:" + olColorCol + ": Min:" + outlineAttrMin + " Max:" + outlineAttrMax );
		}
		if ( POIsizeCol != -1 ){
			System.out.println( "POIsizeAttr Col:" + POIsizeCol + ": Min:" + sizeAttrMin + " Max:" + sizeAttrMax );
		}
	}
	
	private Envelope getFSExtent(FeatureCollection<SimpleFeatureType, SimpleFeature> fsShape){
		boolean hasFeature;
		Envelope newBBox = new Envelope();
		Envelope internal;
		SimpleFeature oneFeature = null;
		FeatureIterator<SimpleFeature> reader = fsShape.features();
		while (reader.hasNext()) {
			hasFeature = false;
			while ( hasFeature == false ){
				try{
					oneFeature = reader.next();
					hasFeature = true;
				} catch ( Exception e ){
					System.out.print("extERR");
					hasFeature = false;
				}
			}
			internal = ((Geometry)oneFeature.getDefaultGeometry()).getEnvelopeInternal();
			if ( Math.abs(internal.getMaxX()) > 300 || Math.abs(internal.getMinX()) > 300 ||
			Math.abs(internal.getMaxY()) > 300 || Math.abs(internal.getMinY()) > 300 ||
			Math.abs(internal.getMaxX()) > 300 || Math.abs(internal.getMinX()) > 300 ){
				// �ُ�l�̏ꍇ�@don't append
			} else {
				newBBox.expandToInclude(internal);
			}
		}
		System.out.println( "TracedBBOX:" + newBBox );
		return newBBox;
	}
	
	
	// �w�肵�������ԍ��Ŋe���C���[�̖��O(�n�b�V���L�[)�Ƃ��̗̈���擾����
	private int getLayerNames( FeatureCollection<SimpleFeatureType, SimpleFeature> fsShape , String[] layerNames , Envelope[] layerBounds , int layerCol ){
		SimpleFeature oneFeature = null;
		HashSet<String> set = new HashSet<String>(); // ���ʂȋC���E�E�E�E
		HashMap<Object,ReferencedEnvelope> map = new HashMap<Object,ReferencedEnvelope>();
		ReferencedEnvelope oneEnv;
		Object value;
		boolean err = false;
		FeatureIterator<SimpleFeature> reader = fsShape.features();
		boolean hasFeature;
		while (reader.hasNext()) {
			hasFeature = false;
			while ( hasFeature == false ){
				try{
					oneFeature = reader.next();
					hasFeature = true;
				} catch ( Exception e ){
					System.out.print("lnERR");
					hasFeature = false;
				}
			}
			
//			value = (oneFeature.getAttribute(layerCol));
			
			Object val = oneFeature.getAttribute(layerCol);
			String vals ="";
			if ( val instanceof String ){
				vals = (String)val;
			} else {
				vals = val.toString();
			}
			value = getKanjiProp(vals);
				
//			value = getKanjiProp((String)oneFeature.getAttribute(layerCol));
			set.add(value.toString());
			if ( map.containsKey( value ) ){
				oneEnv = (ReferencedEnvelope)(map.get( value ));
				oneEnv.expandToInclude( (ReferencedEnvelope)oneFeature.getBounds() );
				map.put( value , oneEnv ); // ����͕s�v�H
			} else {
				map.put( value , (ReferencedEnvelope)(oneFeature.getBounds()) );
			} 
			
			
			if ( set.size() > 1024 ){
				System.out.println ( "Out Of Size Error!");
				err = true;
				break;
			}
		}
		
		
		System.out.println( "Attr Size: " + set.size()  +  set);
//		System.out.println( "Envelopes: "  +  map);
		layerNames = (String[])set.toArray( layerNames );
		
		
		Iterator iterator = map.keySet().iterator();
		Object obj;
		int i = 0;
		while(iterator.hasNext()){
			obj = iterator.next();
			layerNames[i] = obj.toString();
			layerBounds[i] = (Envelope)( map.get(obj));
			System.out.print(layerBounds[i] + " : " );
			++i;
		}
		return( map.size() );
		
	}

	
	private void setNumberFormat(double d , double accuracy){
	// NumberFormatter������
		nFmt = NumberFormat.getNumberInstance();
		nFmt.setGroupingUsed(false);
		
		int fractions;
		
		fractions = -(int)(Math.log(-d * accuracy / 111111 ) / Math.log(10.0)) + 1;
		System.out.println( "Fractions:" + fractions + "( d=" + d + " acc=" + accuracy + ")" );
		if ( fractions < 0 ){
			fractions = 0;
		}
		nFmt.setMaximumFractionDigits(fractions);
		
		
	}
	
	private void putCaption ( Coordinate coo , Object attr , double cWidth , SvgMapTilesTM sm , boolean abs , double capSft ) throws Exception{
		attr = attr.toString();
//		System.out.print(attr);
		String unicodeStirng = getKanjiProp( (String)attr ); // 2016.10 �֐�������
//		System.out.println(":" + unicodeStirng);
		if ( unicodeStirng.length() > 0 ){
			sm.putText( coo , cWidth , unicodeStirng , abs , capSft );
		}
	}
	
	
	// ���̃��[�`���͂܂������Ă��Ȃ��ł��E�E�E�E�Ή��͌��\���
	private void putRdfMetadata(SimpleFeatureType readFT , FeatureCollection<SimpleFeatureType, SimpleFeature> fsShape , Writer out) throws Exception{
		SimpleFeature oneFeature = null;
		Object value;
		double dval=0;
		int i , j;
		FeatureIterator<SimpleFeature> reader = fsShape.features();
		j = 0;
		boolean hasFeature;
		while (reader.hasNext()) {
			hasFeature = false;
			while ( hasFeature == false ){
				try{
					oneFeature = reader.next();
					hasFeature = true;
				} catch ( Exception e ){
					System.out.print("metaERR");
					hasFeature = false;
				}
			}
			
			out.write("  <rdf:Description rdf:about=\"#f" + j + "\" \n");
			for ( i = 0 ; i < readFT.getAttributeCount() ; i++){
				if (oneFeature.getAttribute(i) instanceof Geometry == false ){
					out.write( "   " + metaNs + ":" + readFT.getDescriptor(i).getLocalName() + "=\"" + oneFeature.getAttribute(i) + "\" \n");
				}
			}
			out.write("  />\n");
			j++;
		}
	}
	
	
	private void buildHyperLayerContainer(String outfile , NumberFormat nFmt , Envelope allBounds , int layerCount , String[] layerNames , Envelope[] layerBounds)throws Exception{
		SvgMap hlc;
		Envelope svgBounds ;
		String layerFileName , href;
		int p = outfile.lastIndexOf("\\");
		if ( p<0 ){
			p = outfile.lastIndexOf("/");
		}
		if ( p<0 ){
			p=0;
		} else {
			p = p + 1;
		}
		href = outfile.substring(p);
		
		
		hlc = new SvgMap(outfile , nFmt);
		
		svgBounds = smat.getSvgBounds(  allBounds  );
		hlc.putHeader(svgBounds.getMinX(), svgBounds.getMinY(), svgBounds.getWidth(), svgBounds.getHeight());
		double hlcWidth= svgBounds.getWidth();
		
		hlc.putCrs( smat.g2s.a , smat.g2s.b , smat.g2s.c , smat.g2s.d , smat.g2s.e , smat.g2s.f );
		
//		System.out.println("DEBUG::::" + layerDebug);
		
		for ( int i = 0 ; i < layerCount ; i++ ){
			svgBounds = smat.getSvgBounds(  layerBounds[i]  );
			layerFileName = href.substring(0 , href.indexOf(".svg")) + "_" + layerNames[i] + ".svg";
			
			hlc.putImage( new Coordinate( svgBounds.getMinX(), svgBounds.getMinY() ), svgBounds.getWidth(), svgBounds.getHeight(), layerFileName );
			if ( layerDebug ){
//				hlc.putPlaneString("<rect x=\"" + (svgBounds.getMinX() ) + "\" y=\"" + (svgBounds.getMinY() ) + "\" width=\"" + svgBounds.getWidth() + "\" height=\"" + svgBounds.getHeight() + "\" fill=\"none\" stroke=\"#A00040\" stroke-width=\"" + (hlcWidth / 1000.0 ) + "\" />\n");
				hlc.putPlaneString("<rect x=\"" + (svgBounds.getMinX() ) + "\" y=\"" + (svgBounds.getMinY() ) + "\" width=\"" + svgBounds.getWidth() + "\" height=\"" + svgBounds.getHeight() + "\" fill=\"none\" stroke=\"#A00040\" stroke-width=\"0.5\" vector-effect=\"non-scaling-stroke\" stroke-linejoin=\"bevel\" />\n");
			}
			
		}
		hlc.putFooter();
	}
	
	boolean strIsSJIS = true;
	String getKanjiProp( String input ){
		String ans ="";
		try {
			if ( strIsSJIS ){
				// 2013/02/15 WINDOWS...
//				ans =  (new String(((String)input).getBytes("iso-8859-1"),"Shift_JIS")).trim();
				ans =  (new String(((String)input).getBytes("iso-8859-1"),"Windows-31J")).trim();
			} else {
				ans =  (new String(((String)input).getBytes("iso-8859-1"),"UTF-8")).trim();
			}
		} catch (Exception e){
			ans = "";
		}
		return ( ans );
	}
	
	String sjis2str(String input ){
		String ans ="";
		try {
			ans =  (new String(((String)input).getBytes("iso-8859-1"),"Windows-31J")).trim();
		} catch (Exception e){
			ans = "";
		}
	return ( ans );
	}
	
	
	//����R�[�h���폜
    String delCC(String S){
        StringBuffer SB=new StringBuffer();
        for(int i=0;i<S.length();i++){
            SB.append(Full((int)S.charAt(i)));
        }
        return(SB.toString());
    }
    char Full(int c){
    	if( c<32 ){
    		c=95;
    	}
        return (char)c;
    }
	
	// Global QuadKeyTiling�Ɋւ���֐��Q 2013/3/28
	double getPartDeg(int lvl){
		double partD = 360.0;
		for ( int i = 0 ; i < lvl ; i++){
			partD = partD / 2.0;
		}
		return ( partD );
	}
	
	Envelope getGlobalTileArea( Envelope bounds  , int level , int[] part){
		double tileStep = getPartDeg(level);
		double geoXstart = (int)((bounds.getMinX() + 180.0)/ tileStep ) * tileStep - 180.0;
		double geoYstart = (int)((bounds.getMinY() + 180.0)/ tileStep ) * tileStep - 180.0;
		
		part[0] = (int)Math.ceil( (bounds.getMaxX()-geoXstart)/tileStep);
		part[1] = (int)Math.ceil( (bounds.getMaxY()-geoYstart)/tileStep);
		Envelope ans = new Envelope(geoXstart , geoXstart + tileStep * part[0] , geoYstart , geoYstart + tileStep * part[1] );
//		System.out.println("part:" + part[0]+","+part[1]);
		return ( ans );
	}
	
}