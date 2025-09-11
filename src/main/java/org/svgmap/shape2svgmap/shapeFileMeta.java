package org.svgmap.shape2svgmap;

// License: (MPL v2)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

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

