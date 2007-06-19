package de.jreality.hochtief.processing;

import java.util.ArrayList;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SymmPackEVD;

import de.jreality.hochtief.pointclouds.ExpectationMaximation;
import de.jreality.hochtief.utility.Scan3DPointCloudUtility;
import de.jreality.hochtief.utility.Scan3DUtility;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;

public class PointCloudSimplifier {

	public static SceneGraphComponent getSimplifiedPointCloud(int componentCount, double minProbChange, int subSample, double texRes, int[][] edgeId, double[][] depth, byte[][] colorR, byte[][] colorG, byte[][] colorB){
		int M=depth.length;  int N=depth[0].length;
		
		SceneGraphComponent sgc=new SceneGraphComponent("simplified point clouds");
		ArrayList<double[]> singlePoints=new ArrayList<double[]>();
		ArrayList<byte[]> colors=new ArrayList<byte[]>();
		
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){				
				if(edgeId[i][j]==EdgeDetector.POINT_TYPE_SINGLEPOINT){
					singlePoints.add(Scan3DUtility.convertDepthValueTo3DCoordinate(i, j, depth[i][j], M, N));
					colors.add(new byte[]{colorR[i][j],colorG[i][j],colorB[i][j]});					
				}				
			}			
		}
		
		if(subSample>1){
			for(int i=0;i<(int)((double)singlePoints.size()/(double)subSample);i++){
				for(int j=i+1;j<i+subSample-1;j++){
					singlePoints.remove(j);
					colors.remove(j);
				}
			}
		}
		
		double[][] points=new double[(int)((double)singlePoints.size()/(double)subSample)][];
		for(int i=0;i<points.length;i++)
			points[i]=singlePoints.get(i*subSample);
		
		double[][] params=ExpectationMaximation.calculateParameters(componentCount, minProbChange, points);
		int[] compId=ExpectationMaximation.evalPoints(points, params);
		
		for(int c=0;c<componentCount;c++){
//			double[] centeroid=new double[] {params[c][0],params[c][1],params[c][2]};
			DenseMatrix covMtx=new DenseMatrix(new double[][]{{params[c][3],params[c][4],params[c][5]},{params[c][6],params[c][7],params[c][8]},{params[c][9],params[c][10],params[c][11]}});
			SymmPackEVD evd=null;
			try {
				evd = SymmPackEVD.factorize(covMtx);
			} catch (NotConvergedException e) {e.printStackTrace();}
			
			//double[] ev=evd.getEigenvalues();
			DenseMatrix eig=evd.getEigenvectors();
			
			double[] faceDir1=new double[] {eig.get(0, 2),eig.get(1, 2),eig.get(2, 2)};
			double[] faceDir2=new double[] {eig.get(0, 1),eig.get(1, 1),eig.get(2, 1)};
			double[] faceDir3=new double[] {eig.get(0, 0),eig.get(1, 0),eig.get(2, 0)};
			
			ArrayList<double[]> componentPoints=new ArrayList<double[]>();
			ArrayList<byte[]> componentColors=new ArrayList<byte[]>();
			for(int i=0;i<points.length;i++){
				if(compId[i]==c){
					componentPoints.add(points[i]);
					componentColors.add(colors.get(i));
				}
			}
			
			System.out.println("\ncomponent "+c+":");
			System.out.println("contains "+componentPoints.size()+" points");
			System.out.println("centeroid: "+Rn.toString(new double[] {params[c][0],params[c][1],params[c][2]}));
			System.out.println("dir1: "+Rn.toString(faceDir1));
			System.out.println("dir2: "+Rn.toString(faceDir2));
			System.out.println("max var1="+Math.sqrt(evd.getEigenvalues()[2]));
			System.out.println("max var2="+Math.sqrt(evd.getEigenvalues()[1]));		
			

			SceneGraphComponent compSgc=Scan3DPointCloudUtility.projectPointCloud(componentPoints, componentColors, faceDir1, faceDir2, faceDir3, texRes);
			if(compSgc!=null){
				compSgc.setName("comp "+c);
				sgc.addChild(compSgc);
			}
		}
		return sgc;
	}
	
}
