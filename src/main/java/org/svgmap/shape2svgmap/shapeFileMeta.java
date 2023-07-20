package org.svgmap.shape2svgmap;

import org.locationtech.jts.geom.*;

import java.io.*;
import java.net.URL;
import java.io.IOException;
import java.util.*;

	class shapeFileMeta implements java.io.Serializable {
		Envelope bbox;
		String filename;
		long count;
	}

