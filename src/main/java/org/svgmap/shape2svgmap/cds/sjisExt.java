package org.svgmap.shape2svgmap.cds;

// License: (MPL v2)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

public class sjisExt{
	public static void main(String[] args) throws Exception {
		String in = "Helloこんにちは";
		System.out.println( getSjisStr(in) );
	}
	
	public static String getSjisStr(String s) {
		byte[] bts = null;
		char[] cs = null;
		try{
//			bts = s.getBytes("Shift_JIS"); // debug 2018.3.29
			bts = s.getBytes("Windows-31J");
			cs = new char [bts.length];
			for ( int i = 0 ; i < cs.length ; i++ ){
				int ci = bts[i] & 0xFF;
				cs[i] = (char)ci;
			}
		} catch ( Exception  e ){
		}
		return ( new String( cs ) );
	}
}