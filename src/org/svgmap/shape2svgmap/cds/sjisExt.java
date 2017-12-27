package org.svgmap.shape2svgmap.cds;

public class sjisExt{
	public static void main(String[] args) throws Exception {
		String in = "Hello‚±‚ñ‚É‚¿‚Í";
		System.out.println( getSjisStr(in) );
	}
	
	public static String getSjisStr(String s) {
		byte[] bts = null;
		char[] cs = null;
		try{
			bts = s.getBytes("Shift_JIS");
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