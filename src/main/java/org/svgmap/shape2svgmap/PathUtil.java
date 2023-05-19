package org.svgmap.shape2svgmap;

import java.util.*;
import java.io.*;
import java.text.NumberFormat ;

// from http://sqs.prof.cuc.ac.jp/tdiary/20070310.html#p02

public class PathUtil{
	public static String getSharedPrefix(String a, String b){
		if(a == null || b == null){
			throw new IllegalArgumentException();
		}
		int al = a.length();
		int bl = b.length();
		if(al == 0 || bl == 0){
			return "";
		}
		for(int i = 0; i < al && i < bl; i++){
			if(a.charAt(i) != b.charAt(i)){
				return a.substring(0, i);
			}
		}
		if(al <= bl){
			return a;
		}else{
			return b;
		}
	}
	
	public static String getSharedPathPrefix(String a, String b){
		if(a == null || b == null){
			throw new IllegalArgumentException();
		}
		
		int al = a.length();
		int bl = b.length();
		if(al == 0 || bl == 0){
			return "";
		}
		
		int separatorPosition = -1;
		for(int i = 0; i < al && i < bl; i++){
			if(a.charAt(i) == File.separatorChar && 
					b.charAt(i) == File.separatorChar){
				separatorPosition = i;
			}
			if(a.charAt(i) != b.charAt(i)){
				break;
			}
		}
		if(separatorPosition == -1){
			return "";
		}else if(separatorPosition == -1){
			return File.separator;
		}else{
			return a.substring(0, separatorPosition);
		}
	}
	
	public static int count(String src, char delim){
		int count = 0;
		for(int i = 0; i < src.length(); i++){
			char c = src.charAt(i);
			if(c == delim){
				count++;
			}
		}
		return count;
	}
	
	public static String getRelativePath(File a, File b)throws IOException{
		return  getRelativePath(a.getCanonicalPath(), b.getCanonicalPath());
	}
	
	public static String getRelativePath(String a, String b){
		if(a == null || b == null){
			throw new IllegalArgumentException();
		}
		a = a.trim();
		b = b.trim();
		int al = a.length();
		int bl = b.length();
		if(al == 0 || bl == 0){
			return "";
		}
		String sharedPrefix = PathUtil.getSharedPathPrefix(a, b);
		String uniqueSuffixA = a.substring(sharedPrefix.length() + 1);// remove +1? 
		String uniqueSuffixB = b.substring(sharedPrefix.length() + 1);// remove +1? 
		int uniqueSuffixDepthB = PathUtil.count(uniqueSuffixB, File.separatorChar);
		StringBuilder ret = new StringBuilder();
 		
		for(int i = 0; i < uniqueSuffixDepthB; i++){
			ret.append(".."+File.separatorChar);
		}
		ret.append(uniqueSuffixA);
		return ret.toString();
	}
}
