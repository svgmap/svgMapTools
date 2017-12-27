package org.svgmap.shape2svgmap;

class PolygonDouble{
	public double[] xpoints;
	public double[] ypoints;
	
	public boolean[] clippedEdge; // その点が、クリップされたエッジの終点のときTrue
	
	public boolean clipped=false; // クリッピングされたポリゴンのときTrue #クリッピング機能自体がこのクラスに有るわけではない
	
	public int npoints;

	PolygonDouble(){
		npoints=0;
		xpoints=new double[10];
		ypoints=new double[10];
		clippedEdge=new boolean[10];
	}
	
	
	PolygonDouble(int length){
		npoints=0;
		xpoints=new double[length];
		ypoints=new double[length];
		clippedEdge=new boolean[length];
	}

	PolygonDouble(double[] xp , double[] yp , int np){
		xpoints = (double[])xp.clone();
		ypoints = (double[])yp.clone();
		clippedEdge = new boolean[xp.length]; // falseで初期化されているはず
		npoints = np;
	}
	
	PolygonDouble(double[] xp , double[] yp , boolean[] ce, int np){
		xpoints = (double[])xp.clone();
		ypoints = (double[])yp.clone();
		clippedEdge = (boolean[])ce.clone();
		npoints = np;
	}
	
	void addPoint(double x, double y){
		if (xpoints.length == npoints){
			extend(100);
		}
		xpoints[npoints]=x;
		ypoints[npoints]=y;
		++npoints;
	}
	
	void addPoint(double x, double y, boolean clipE){
		if (xpoints.length == npoints){
			extend(100);
		}
		xpoints[npoints]=x;
		ypoints[npoints]=y;
		clippedEdge[npoints]=clipE;
		++npoints;
	}
	
	private void extend( int numb){
		double[] xt =  (double[])xpoints.clone();
		double[] yt =  (double[])ypoints.clone();
		boolean[] ct =  (boolean[])clippedEdge.clone();
		xpoints = new double[npoints + numb ];
		ypoints = new double[npoints + numb ];
		clippedEdge = new boolean[npoints + numb ];
		System.arraycopy(xt , 0 , xpoints , 0 , npoints);
		System.arraycopy(yt , 0 , ypoints , 0 , npoints);
		System.arraycopy(ct , 0 , clippedEdge , 0 , npoints);
	}
	
	public void print(){
		for (int i = 0; i < npoints; i++){
System.out.println(xpoints[i] + " " + ypoints[i]+" isCE" + clippedEdge[i]);
		}
System.out.println(" Clip:" + clipped);
System.out.println("--");
	}

}