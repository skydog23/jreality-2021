package de.jreality.hochtief.processing;

import java.util.ArrayList;

import de.jreality.hochtief.utility.Scan3DUtility;
import de.jreality.math.Rn;

/**
 * @author Nils Bleicher
 */ 

public class FaceSmoother {
	
	public static double[][] smoothFace(int faceNr, double depthThreshold, double[][] depth, int[][] faceId){
		int M=depth.length;
		int N=depth[0].length;
		double[][] smoothedDepth=new double[M][N];		
			
		double maxDist=0.5;		
		int maxNeighborhood=1000;
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){
				if(faceId[i][j]==faceNr){				
					//smoothedDepth[i][j]=median(i,j,3,depth,faceId);
					//smoothedDepth[i][j]=median(i,j,maxDist,maxNeighborhood,depth,faceId);
					//smoothedDepth[i][j]=averageValue(i,j,6,depth,faceId);
					smoothedDepth[i][j]=averageValue(i,j,maxDist,depthThreshold,depth,faceId);
				}else
					smoothedDepth[i][j]=depth[i][j];
			}
		}	
		
		System.out.println("maxNB="+maxNB);
		System.out.println("minNB="+minNB);
		
		return smoothedDepth;		
	}
	
	public static double median(int i, int j, int neighborhood, double depthThreshold, double[][] depth, int[][] faceId){
		int M=depth.length;
		int N=depth[0].length;
		ArrayList<Double> depthValues=new ArrayList<Double>();
		int posI,posJ;
		for(int ii=i-neighborhood;ii<i+neighborhood+1;ii++){
			posI=ii; 
			if(posI<0) posI=M+posI;
			if(posI>=M) posI=posI-M;
			for(int jj=j-neighborhood;jj<j+neighborhood+1;jj++){
				posJ=jj; 
				if(posJ<0) posJ=N+posJ;
				if(posJ>=N) posJ=posJ-N;
				if(faceId[posI][posJ]==faceId[i][j] && Math.abs(depth[posI][posJ]-depth[i][j])<=depthThreshold*Math.min(depth[posI][posJ],depth[i][j])){
					
					int listPos=0;
					while(listPos<depthValues.size() && depthValues.get(listPos)<depth[posI][posJ])
						listPos++;
					depthValues.add(listPos,depth[posI][posJ]);
					
				}
			}
		}
		if(depthValues.size()>1)
			return depthValues.get(depthValues.size()/2);
		else
			return depthValues.get(0);
	}
	
	//median adapted to median-distance to surounding face-points 
	//maxDistance is the the distance from that all median-distances above will result a neighborhood=1 and all smaller median-distances will result a neighborhood>1
	public static double median(int i, int j, double maxDistance, int maxNeighborhood, double depthThreshold, double[][] depth, int[][] faceId){
		int neighborhood=(int)Math.ceil(maxDistance/medianDistance(i, j, depthThreshold, depth, faceId));
		if(neighborhood>maxNeighborhood) neighborhood=maxNeighborhood;
		return median(i, j, neighborhood, depthThreshold, depth, faceId);
	}
	
	public static double medianDistance(int i, int j, double depthThreshold, double[][] depth, int[][] faceId){
		int M=depth.length;
		int N=depth[0].length;
		double[] p=Scan3DUtility.convertDepthValueTo3DCoordinate(i, j, depth[i][j], M, N);
		ArrayList<Double> distValues=new ArrayList<Double>();
		int posI,posJ;		
		for(int ii=i-1;ii<i+2;ii++){
			posI=ii; 
			if(posI<0) posI=M+posI;
			if(posI>=M) posI=posI-M;
			for(int jj=j-1;jj<j+2;jj++){
				posJ=jj; 
				if(posJ<0) posJ=N+posJ;
				if(posJ>=N) posJ=posJ-N;
				if(faceId[posI][posJ]==faceId[i][j] && Math.abs(depth[posI][posJ]-depth[i][j])<=depthThreshold*Math.min(depth[posI][posJ],depth[i][j])){

					double[] p2=Scan3DUtility.convertDepthValueTo3DCoordinate(posI, posJ, depth[posI][j], M, N);
					double dist=Rn.euclideanDistance(p, p2);
					int listPos=0;
					while(listPos<distValues.size() && distValues.get(listPos)<dist)
						listPos++;
					distValues.add(listPos,dist);

				}
			}
		}
		if(distValues.size()>1)
			return distValues.get(distValues.size()/2);
		else
			return distValues.get(0);

	}
	
	static double maxNB=0;
	static double minNB=10000;
	//averageValue adapted to average-distance to surounding face-points 
	
	public static double averageValue(int i, int j, double maxDistance, double depthThreshold, double[][] depth, int[][] faceId){
		int[][] oneNbh=Scan3DUtility.getNeighborhood(i, j, 1, depthThreshold, depth, faceId);
		int nbhSize=Scan3DUtility.getNeighborhoodSize(i, j, oneNbh, maxDistance, depthThreshold, depth, faceId);
		
		if(nbhSize>maxNB) maxNB=nbhSize;
		if(nbhSize<minNB) minNB=nbhSize;
		
		return averageValue(i, j, nbhSize, depthThreshold, depth, faceId);
	}
	
	private static double maxDistToAverageValue=0.01;
	
	public static double averageValue(int i, int j, int neighborhood, double depthThreshold, double[][] depth, int[][] faceId){		
		int M=depth.length;
		int N=depth[0].length;
		double averageValue=0;
		int vertexCount=0;
		int posI,posJ;
		for(int ii=i-neighborhood;ii<i+neighborhood+1;ii++){
			posI=ii; 
			if(posI<0) posI=M+posI;
			if(posI>=M) posI=posI-M;
			for(int jj=j-neighborhood;jj<j+neighborhood+1;jj++){
				posJ=jj; 
				if(posJ<0) posJ=N+posJ;
				if(posJ>=N) posJ=posJ-N;
				if(faceId[posI][posJ]==faceId[i][j] && Math.abs(depth[posI][posJ]-depth[i][j])<=depthThreshold*Math.min(depth[posI][posJ],depth[i][j])){
					
					averageValue+=depth[posI][posJ];
					vertexCount++;
					
				}
			}
		}
		averageValue=averageValue/(double)vertexCount;
		if(Math.abs(averageValue-depth[i][j])<maxDistToAverageValue) //*depth[i][j])
			return averageValue;
		else
			return depth[i][j];
	}
	
}