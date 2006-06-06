package de.jreality.geometry;

import java.awt.Color;
import java.util.HashMap;
import javax.swing.SwingConstants;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.Appearance;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.Attribute;
import de.jreality.geometry.Primitives;
import de.jreality.shader.CommonAttributes;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.Quaternion;
import de.jreality.math.Rn;


/**
 * Represents a coordinate system in Euclidean space and is
 * either created for an existing SceneGraphComponent or for a given extent. 
 *  
 * @author msommer
 * 
 * TO DO:
 * - determine default value of labelScale via bounding box of the component
 * - CoordinateSystemBeautifier
 * - documentation
 * 
 */
public class CoordinateSystemFactory {

	
	
	private double[] boxMin, boxMax;
	
	public final static int X = 0, Y = 1, Z = 2;
	
	private double[][][] axesVertices, boxVertices;
	private SceneGraphComponent coordinateSystem, box, axes;
	
	private final String[] axesNames = {"x", "y", "z"};
	
	private double[][] octagonalCrossSection = {
			{1,0,0}, 
			{.707, .707, 0}, 
			{0,1,0},
			{-.707, .707, 0},
			{-1,0,0},
			{-.707, -.707, 0},
			{0,-1,0},
			{.707, -.707, 0},
			{1,0,0}};
	
	private final double arrowHeight = 3;	
	private IndexedFaceSet urCone = null;
	{
		urCone = Primitives.pyramid(octagonalCrossSection(-arrowHeight), new double[]{0,0,0});
		//GeometryUtility.calculateAndSetVertexNormals(urCone);
	}
	
	//private int signature = Pn.EUCLIDEAN;
	
	private int currentClosestBoxVertex = -1;  //index of a currently closest box vertex in boxVertices[0] 
	
	private HashMap nodes = new HashMap();
	
	
	//DEFAULT VALUES OF PROPERTIES			
	private double axisScale = 0.5;  //the distance between two ticks on an axis
	private double labelScale = 0.01;  //size of labels
	private double arrowStretch = 4*labelScale; //stretch of arrows of axes (octagonalCrossSection)
	private double tickStretch = 2*labelScale; //stretch of ticks of axes (octagonalCrossSection)
	private boolean showAxes = false;  //show or hide axes
	private boolean showBox = false;  //show or hide box
	private boolean showGrid = false;  //show or hide grid on box
	private boolean showAxesArrows = true;  //show or hide arrows on axes
	private boolean showBoxArrows = false;  //show or hide arrows on box
	private boolean showLabels = true;  //show or hide labels of ticks & axes
	private Color coordinateSystemColor = Color.BLACK;
	private Color labelColor = Color.BLACK;
	private Color gridColor = Color.GRAY;
	
	
	
	//CONSTRUCTORS
	
	/**
	 * coordinate system with extent 4
	 */
	public CoordinateSystemFactory() {
		this(4);
	}
	
	
	/**
	 * coordinate system with given extent
	 * @param extent the extent of the coordinate system
	 */
	public CoordinateSystemFactory(int extent) {
		//To DO: validate extent
		
		boxMin = new double[]{-extent, -extent, -extent};
		boxMax = new double[]{ extent,  extent,  extent};
		
		box = calculateBox();
		axes = calculateAxes();
		coordinateSystem = createCoordinateSystem();
	}
	
	
	/**
	 * coordinate system for an existing SceneGraphComponent
	 * @param component the SceneGraphComponent specifying the extent of the coordinate system
	 */
	public CoordinateSystemFactory(SceneGraphComponent component) {
		
		//need to calculate bounding box without transformation of component
		Transformation tmp = component.getTransformation();
		component.setTransformation(new Transformation());
				
		//get boundingbox from componment
		double[][] minMax = GeometryUtility.calculateBoundingBox(component).getBounds();
		this.boxMin = minMax[0];
		this.boxMax = minMax[1];
		
		//enlarge box if graphic is 2d
		for (int axis=X; axis<=Z; axis++) {
			if (boxMin[axis] == boxMax[axis]) {
				boxMin[axis] -= 0.5;
				boxMax[axis] += 0.5;
			}
		}
		
		box = calculateBox();
		axes = calculateAxes();
		coordinateSystem = createCoordinateSystem();
		component.addChild(coordinateSystem);
		
		component.setTransformation(tmp);
	}

	
	
	//METHODS

	private SceneGraphComponent createCoordinateSystem() {
		
		coordinateSystem = new SceneGraphComponent();
		coordinateSystem.setName("CoordinateSystem");
		
		coordinateSystem.addChild(box);  //invisible child, use displayBox(boolean)
		coordinateSystem.addChild(axes); //invisible child, use displayBox(boolean)
		
		//set appearance of coordinate system node
		Appearance app = new Appearance();
		app.setName("Appearance");
	    app.setAttribute(CommonAttributes.EDGE_DRAW, true);
	    app.setAttribute(CommonAttributes.SPHERES_DRAW, true);
	    app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
	    app.setAttribute(CommonAttributes.VERTEX_DRAW, showLabels);  //label visibility
		app.setAttribute(CommonAttributes.POINT_RADIUS, 0.001);  //don't show label points
		app.setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, labelColor);
		app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, coordinateSystemColor);
		app.setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, coordinateSystemColor);
		//app.setAttribute(CommonAttributes.SPECULAR_COLOR, Color.BLACK);
	    app.setAttribute(CommonAttributes.DEPTH_FUDGE_FACTOR, 1.0);
	    app.setAttribute(CommonAttributes.POINT_SHADER+"."+"scale", labelScale);  //label scale
	    app.setAttribute(CommonAttributes.POINT_SHADER+"."+"offset", new double[]{0.04, 0, 0});  //label offset of ticks
	    app.setAttribute(CommonAttributes.POINT_SHADER+"."+"alignment", SwingConstants.EAST);
	    coordinateSystem.setAppearance(app);
		
		return coordinateSystem;
	}
	
	
	private SceneGraphComponent calculateBox() {
		
		calculateBoxVertices();
		
		//create SceneGraphComponent which has each coordinate axis as its child
		SceneGraphComponent box = new SceneGraphComponent();
		box.setName("Box");
		
		for (int axis=X; axis<=Z; axis++) {  //for each coordinate axis
			
			SceneGraphComponent singleAxis = new SceneGraphComponent();
			singleAxis.setName(axesNames[axis] +"-axis");
			nodes.put(axesNames[axis] +"Box", singleAxis);  //e.g. xBox
			
			for (int k=0; k<=3; k++) {
				
				//create SceneGraphComponent with children line, arrow, ticks
				SceneGraphComponent singleAxisK = new SceneGraphComponent();
				//assign binary value of k to the name of the SGC
				singleAxisK.setName(toBinaryString(k));

				nodes.put(axesNames[axis]+singleAxisK.getName(), singleAxisK);  //e.g. x00
				
				//create line with label
				SceneGraphComponent line = getLine(axis, boxVertices[axis][2*k], boxVertices[axis][2*k+1], true);
				//create arrow
				SceneGraphComponent arrow = getArrow(axis, boxVertices[axis][2*k], boxVertices[axis][2*k+1]);
				arrow.setVisible(showBoxArrows);
				nodes.put(axesNames[axis]+singleAxisK.getName()+"arrow", arrow);  //e.g. x00arrow
				//create ticks with labels
				SceneGraphComponent ticks = getBoxTicks(axis, k, boxVertices[axis][2*k], boxVertices[axis][2*k+1]);
				nodes.put(axesNames[axis]+singleAxisK.getName()+"ticks", ticks);  //e.g. x00ticks
				
				singleAxisK.addChild(line);
				singleAxisK.addChild(arrow);
				singleAxisK.addChild(ticks);

				singleAxis.addChild(singleAxisK);
			}
			box.addChild(singleAxis);
		}
		
	    //calculate grid and add to box
	    box.addChild(calculate2DGrid());
	    
	    //Appearance app = new Appearance();
	    //app.setName("boxAppearance");
	    //box.setAppearance(app);
	    box.setVisible(showBox);

	    return box;
	}
	
	
	private SceneGraphComponent calculateAxes() {

		calculateAxesVertices();
		
		//create SceneGraphComponent which has each coordinate axis as its child
		SceneGraphComponent axes = new SceneGraphComponent();
		axes.setName("Axes");
		
		for (int axis=X; axis<=Z; axis++) {  //for each coordinate axis

			//create SceneGraphComponent with children line, arrow, ticks
			SceneGraphComponent singleAxis = new SceneGraphComponent();
			singleAxis.setName(axesNames[axis] +"-axis");
			nodes.put(axesNames[axis] +"Axis", singleAxis);  //e.g. xAxis
			
			//create line with label
			SceneGraphComponent line = getLine(axis, axesVertices[axis][0], axesVertices[axis][1], false);
			// create arrow
			SceneGraphComponent arrow = getArrow(axis, axesVertices[axis][0], axesVertices[axis][1]);
			arrow.setVisible(showAxesArrows);
			nodes.put(axesNames[axis]+"Arrow", arrow);  //e.g. xArrow
			// create ticks with labels
			SceneGraphComponent ticks = getAxesTicks(axis, axesVertices[axis][0], axesVertices[axis][1]);
			nodes.put(axesNames[axis]+"Ticks", ticks);  //e.g. xTicks
			
			singleAxis.addChild(line);
			singleAxis.addChild(arrow);
			singleAxis.addChild(ticks);
			
			axes.addChild(singleAxis);
		}
		
	    //Appearance app = new Appearance();
	    //app.setName("axesAppearance");
	    //box.setAppearance(app);
	    axes.setVisible(showAxes);
	    
	    return axes;
	}
	

	/**
	 * calculate the vertices of the axes (specified by the choice of the constructor)
 	 * first index specifies the coordinate axis (0,1,2)
	 * second index specifies starting and endpoint of each coordinate axis {start, end}
	 */
	private void calculateAxesVertices(){
		
		this.axesVertices = new double[][][] {
			{{Math.min(boxMin[X]-0.5, 0),0,0},
			 {Math.max(boxMax[X]+0.5, 0),0,0}},
			{{0,Math.min(boxMin[Y]-0.5, 0),0},
			 {0,Math.max(boxMax[Y]+0.5, 0),0}},
			{{0,0,Math.min(boxMin[Z]-0.5, 0)},
			 {0,0,Math.max(boxMax[Z]+0.5, 0)}}
		};
	}
	
		
	/**
	 * calculate the vertices of the bounding box (specified by the choice of the constructor)
	 * first index specifies the coordinate axis (0,1,2)
	 * second index specifies starting and endpoint for each of the 4 copies of each coordinate axis {start1, end1, start2, end2,...}
	 */
	private void calculateBoxVertices(){
		
		this.boxVertices = new double[][][] {
			{boxMin, {boxMax[X], boxMin[Y], boxMin[Z]},  //04
			 {boxMin[X], boxMin[Y], boxMax[Z]}, {boxMax[X], boxMin[Y], boxMax[Z]},  //37
			 {boxMin[X], boxMax[Y], boxMin[Z]}, {boxMax[X], boxMax[Y], boxMin[Z]},  //15
			 {boxMin[X], boxMax[Y], boxMax[Z]}, boxMax  //26
			},
			{boxMin, {boxMin[X], boxMax[Y], boxMin[Z]},  //01
			 {boxMin[X], boxMin[Y], boxMax[Z]}, {boxMin[X], boxMax[Y], boxMax[Z]},  //32
			 {boxMax[X], boxMin[Y], boxMin[Z]}, {boxMax[X], boxMax[Y], boxMin[Z]},  //45
			 {boxMax[X], boxMin[Y], boxMax[Z]}, boxMax  //76
			},
			{boxMin, {boxMin[X], boxMin[Y], boxMax[Z]},  //03
			 {boxMin[X], boxMax[Y], boxMin[Z]}, {boxMin[X], boxMax[Y], boxMax[Z]},  //12
			 {boxMax[X], boxMin[Y], boxMin[Z]}, {boxMax[X], boxMin[Y], boxMax[Z]},  //47
			 {boxMax[X], boxMax[Y], boxMin[Z]}, boxMax  //56			 
			}
		}; //note that the ordering of the copies of each coordinate axis is significant

	//  0    boxMin
	//	1	{boxMin[X], boxMax[Y], boxMin[Z]}
	//	2	{boxMin[X], boxMax[Y], boxMax[Z]}    1   ---   5     y
	//	3	{boxMin[X], boxMin[Y], boxMax[Z]}  2   ---   6       |_ x
	//	4	{boxMax[X], boxMin[Y], boxMin[Z]}    0   - -   4     /
	//	5	{boxMax[X], boxMax[Y], boxMin[Z]}  3   ---   7      z
	//	6	 boxMax
	//	7	{boxMax[X], boxMin[Y], boxMax[Z]}
	}	
	

	/**
	 * get the line for the coordinate axis specified by <code>axis</code> 
	 * between min and max as a SceneGraphComponent (IndexedLineSet)
	 * (the line thru min and max has to be parallel to the coordinate axis)
	 * @param axis the coordinate axis (0,1,2)
	 * @param min the starting point of the line
	 * @param max the endpoint of the line
	 * @param forBox is the line intended for a box (line is labelled differently then)
	 * @return the line
	 */
	private SceneGraphComponent getLine(int axis, double[] min, double[] max, boolean forBox) {
		//line through min and max has to be parallel to the coordinate axis specified by axis 
	
		IndexedLineSetFactory lineLSF = new IndexedLineSetFactory();
		lineLSF.setVertexCount(2);
		lineLSF.setLineCount(1);
		lineLSF.setVertexCoordinates(new double[][]{min, max});
		lineLSF.setEdgeIndices(new int[]{0,1});
		lineLSF.update();
		
		//create line label
		PointSetFactory labelPSF = new PointSetFactory();
		labelPSF.setVertexCount(1);
		if (forBox) {
			double[] p = (double[])max.clone();
			p[axis] = min[axis]+(max[axis]-min[axis])/2;
			labelPSF.setVertexCoordinates(p);
		}
		else labelPSF.setVertexCoordinates(max);

		labelPSF.setVertexLabels(new String[]{axesNames[axis]});
		labelPSF.update();
		
		SceneGraphComponent label = new SceneGraphComponent();
		label.setName("label");
		Geometry geom = labelPSF.getPointSet();
		geom.setName("anchorPoint");
		label.setGeometry(geom);
		
		SceneGraphComponent line = new SceneGraphComponent();
		line.setName("line");
		line.addChild(label);
		//set axis label offset
		Appearance app =  new Appearance();
		app.setName("lineAppearance");
		app.setAttribute(CommonAttributes.POINT_SHADER+"."+"offset", new double[]{0,-.2,0});
		//app.setAttribute(CommonAttributes.POINT_SHADER+"."+"alignment", SwingConstants.EAST);  //inherited
		line.setAppearance(app);
		geom = lineLSF.getIndexedLineSet();
		geom.setName("line");
		line.setGeometry(geom);
		
		return line;
	}
		
		
	/**
	 * get the arrow for the coordinate axis specified by <code>axis</code> 
	 * between min and max as a SceneGraphComponent (IndexedFaceSet)
	 * @param axis the coordinate axis (0,1,2)
	 * @param min the starting point of the line
	 * @param max the endpoint of the line
	 * @return the arrow
	 */
	private SceneGraphComponent getArrow(int axis, double[] min, double[] max) {
	
		SceneGraphComponent arrow = new SceneGraphComponent();
		arrow.setName("arrow");
		Geometry geom = urCone;
		geom.setName("arrow");
		arrow.setGeometry(geom);
		//get rotation for axis
		//FactoredMatrix m = new FactoredMatrix(TubeUtility.tubeOneEdge(min, max, 0.025, null, signature).getTransformation());
		//above method results in incorrect translation
		FactoredMatrix m = new FactoredMatrix();
		m.setRotation(getAxisRotation(axis));
		m.setStretch(arrowStretch); //stretch urCone
		//translate to axis tip
		m.setTranslation(max);
		Transformation trans = new Transformation(m.getArray());
		trans.setName("arrowTransformation");
		trans.setReadOnly(true);
		arrow.setTransformation(trans);
		
		return arrow;
	}
		
	
	/**
	 * get the ticks on the coordinate axis specified by <code>axis</code> 
	 * between min and max as a SceneGraphComponent (IndexedFaceSet)
	 * @param axis the coordinate axis (0,1,2)
	 * @param min the starting point of the line
	 * @param max the endpoint of the line
	 * @return the ticks
	 */
	private SceneGraphComponent getAxesTicks(int axis, double[] min, double[] max) {
		
		//create the ticks on a line in z-direction
		//determine minimum and maximum value of the tick level
		final double minLevel = round(axisScale*Math.ceil( (min[axis])/axisScale ));  //round to 3 decimal places
		final double maxLevel = round(axisScale*Math.floor( (max[axis]-arrowHeight*arrowStretch)/axisScale ));  //give space for arrow
		
		SceneGraphComponent ticks = new SceneGraphComponent();
		ticks.setName("ticks");
		if (minLevel>maxLevel) return ticks;
		
		IndexedFaceSet ticksGeom = Primitives.pyramid(octagonalCrossSection(minLevel), new double[]{0,0,minLevel});  //init
		int numOfTicks = 1;
		for (double level=round(minLevel+axisScale); level<=maxLevel; level=round(level+axisScale) ) {
			if (level==0) continue;  //no tick at origin
			ticksGeom = IndexedFaceSetUtility.mergeIndexedFaceSets(
				new IndexedFaceSet[]{ ticksGeom, 
				Primitives.pyramid(octagonalCrossSection(level), new double[]{0,0,level}) });
			numOfTicks++;
		}
		//GeometryUtility.calculateAndSetVertexNormals(ticksIFS);
		
		//create labels
		PointSetFactory labelPSF = new PointSetFactory();
		labelPSF.setVertexCount(numOfTicks);
		double[][] labelPoints = new double[numOfTicks][];
		String[] labelStr = new String[numOfTicks];
		double level = minLevel;
		for (int i=0; i<numOfTicks; i++, level=round(level+axisScale) ) {
			if (level==0) level+=axisScale;  //skip tick at origin
			labelPoints[i] = new double[]{0, 0, level};
			labelStr[i] = Math.round(level*1000)/1000. + "";  //3 decimal places
		}
		labelPSF.setVertexCoordinates(labelPoints);
		labelPSF.setVertexLabels(labelStr);
		labelPSF.update();
		SceneGraphComponent labels = new SceneGraphComponent();
		labels.setName("label");
		Geometry geom = labelPSF.getPointSet();
		geom.setName("anchorPoints");
		labels.setGeometry(geom);

		//create the SceneGraphComponent and rotate the ticks onto the corresponding coordinate axis
		ticksGeom.setName("ticks");
		ticks.setGeometry(ticksGeom);
		FactoredMatrix m = new FactoredMatrix();
		m.setRotation(getAxisRotation(axis));
		double[] translation = (double[])min.clone();
		translation[axis] = 0;
		m.setTranslation(translation);
		m.setStretch(tickStretch, tickStretch, 1); //stretch ticks
		Transformation trans = new Transformation(m.getArray());
		trans.setName("tickTransformation");
		trans.setReadOnly(true);
		ticks.setTransformation(trans);

		ticks.addChild(labels);
		return ticks;
	}
	
	
	/**
	 * get the ticks on the box for coordinate axis specified by <code>axis</code> 
	 * between min and max as a SceneGraphComponent (IndexedFaceSet)
	 * @param axis the coordinate axis (0,1,2)
	 * @param min the starting point of the line
	 * @param max the endpoint of the line
	 * @return the ticks
	 */
	private SceneGraphComponent getBoxTicks(int axis, int k, double[] min, double[] max) {
		
		//create the ticks on a line in z-direction
		//determine minimum and maximum value of the tick level
		final double minLevel = round(axisScale*Math.ceil( (min[axis]+0.05)/axisScale ));  //round to 3 decimal places
		final double maxLevel = round(axisScale*Math.floor( (max[axis]-0.05)/axisScale));  //give space for box corners
		
		SceneGraphComponent ticks = new SceneGraphComponent();
		ticks.setName("ticks");
		if (minLevel>maxLevel) return ticks;
		
		IndexedLineSet ticksGeom = new IndexedLineSet();
		IndexedLineSetFactory newTick;
		int numOfTicks = 0;
		
		for (double level=minLevel; level<=maxLevel; level=round(level+axisScale) ) {
			newTick = new IndexedLineSetFactory();
			newTick.setVertexCount(3);
			newTick.setLineCount(2);
			newTick.setVertexCoordinates(new double[][]{{5,0,level},{0,0,level},{0,5,level}});
			newTick.setEdgeIndices(new int[][]{{0,1},{1,2}});
			newTick.update();
			ticksGeom = mergeIndexedLineSets(ticksGeom, newTick.getIndexedLineSet());
			numOfTicks++;
		}
		
		//create labels
		PointSetFactory labelPSF = new PointSetFactory();
		labelPSF.setVertexCount(numOfTicks);
		double[][] labelPoints = new double[numOfTicks][];
		String[] labelStr = new String[numOfTicks];
		double level = minLevel;
		for (int i=0; i<numOfTicks; i++, level=round(level+axisScale) ) {
			labelPoints[i] = new double[]{0, 0, level};
			labelStr[i] = Math.round(level*1000)/1000. + "";  //3 decimal places
		}
		labelPSF.setVertexCoordinates(labelPoints);
		labelPSF.setVertexLabels(labelStr);
		labelPSF.update();
		SceneGraphComponent labels = new SceneGraphComponent();
		labels.setName("label");
		Geometry geom = labelPSF.getPointSet();
		geom.setName("anchorPoints");
		labels.setGeometry(geom);
		
		//create the SceneGraphComponent and rotate the ticks onto the corresponding coordinate axis
		ticksGeom.setName("ticks");
		ticks.setGeometry(ticksGeom);
		FactoredMatrix m = new FactoredMatrix();
		m.setRotation(Quaternion.times(new Quaternion(), getTickRotation(axis, k), getAxisRotation(axis)));
		double[] translation = (double[])min.clone();
		translation[axis] = 0;
		m.setTranslation(translation);
		m.setStretch(tickStretch, tickStretch, 1); //stretch ticks
		Transformation trans = new Transformation(m.getArray());
		trans.setName("tickTransformation");
		trans.setReadOnly(true);
		ticks.setTransformation(trans);

		ticks.addChild(labels);
		return ticks;
	}
	
	
	private SceneGraphComponent calculate2DGrid() {
		
		PointSet ps;
		double[] points, current, translation, result;
		FactoredMatrix trans;
		IndexedLineSet[] gridComp = new IndexedLineSet[6];
		for (int i=0; i<gridComp.length; i++)
			gridComp[i] = new IndexedLineSet();
		
		for (int axis=X; axis<=Z; axis++) {
			
			//get number of ticks on axis
			final int n = ((PointSet)getSGC(axesNames[axis]+"00ticks").getChildComponent(0).getGeometry()).getNumPoints();  //child.name="labels"
			
			for (int k=0; k<=3; k++) {
				
				double[][] vertices = new double[2*n][3];
				int[][] indices = new int[n][2];
				final int next = new int[]{1,2,-2,-1}[k]; //toBinaryString(k+next) = name of axis copy to which grid lines are to be drawn
				
				for (int i=0; i<n; i++) {
					for (int line=0; line<=1; line++) {
						//get anchor points of tick labels
						ps = (PointSet)getSGC(axesNames[axis]+toBinaryString(k+line*next)+"ticks").getChildComponent(0).getGeometry();  //child.name="labels"
						points = ps.getVertexAttributes(Attribute.COORDINATES).toDoubleArray(null);
						current = new double[]{points[3*i], points[3*i+1], points[3*i+2], 0};
						translation = (double[])boxVertices[axis][2*(k+line*next)].clone();  //minimum end point
						translation[axis] = 0;
						trans = new FactoredMatrix(getSGC(axesNames[axis]+toBinaryString(k+line*next)+"ticks").getTransformation());  //e.g. x00ticks
						result = trans.multiplyVector(current);  
						vertices[n*line+i][0] = result[0]+translation[0];
						vertices[n*line+i][1] = result[1]+translation[1];
						vertices[n*line+i][2] = result[2]+translation[2];
					}
					
					indices[i] = new int[]{i, n+i};
				}
				
				//create line set
				IndexedLineSetFactory fac = new IndexedLineSetFactory();
				fac.setVertexCount(vertices.length);
				fac.setLineCount(indices.length);
				fac.setVertexCoordinates(vertices);
				fac.setEdgeIndices(indices);
				fac.update();
				
				//determine face and add to gridComp
				//0=xMin, 1=xMax, 2=yMin, 3=yMax, 4=zMin, 5=zMax
				int face = 0;
				switch (axis) {
				case X: face = new int[]{2,5,4,3}[k]; break;
				case Y: face = new int[]{0,5,4,1}[k]; break;
				case Z: face = new int[]{0,3,2,1}[k];
				}
				gridComp[face] = mergeIndexedLineSets(gridComp[face], fac.getIndexedLineSet());
				
			} //end loop for k
		} //end loop for axis
		
		//create SceneGraphNodes
		SceneGraphComponent grid = new SceneGraphComponent();
		grid.setName("grid");
		nodes.put("grid", grid);
		for (int i=0; i<gridComp.length; i++) {
			SceneGraphComponent face = new SceneGraphComponent();
			face.setName("face"+i);
			gridComp[i].setName("face");
			face.setGeometry(gridComp[i]);
			nodes.put("face"+i, face);
			grid.addChild(face);
		}
		
		Appearance app = new Appearance();
		app.setName("gridAppearance");
		app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, gridColor);
		grid.setAppearance(app);
		grid.setVisible(showGrid);
		return grid;
	}
	
	
	/**
	 * get the octagonalCrossSection on a different level than 0
	 * @param level the level of the octagonalCrossSection (z-value) 
	 */
	private double[][] octagonalCrossSection(double level) {
		
		double[][] octagonalCrossSection = this.octagonalCrossSection; 
		for (int i=0; i<octagonalCrossSection.length; i++)
			octagonalCrossSection[i][2] = level;
		return octagonalCrossSection;
	}
	
	
	//merges two LineSets into a single one
	private IndexedLineSet mergeIndexedLineSets(IndexedLineSet a, IndexedLineSet b) {
		
		if (a==null) a = new IndexedLineSet();
		if (b==null) b = new IndexedLineSet();
		
		double[] aVertices = new double[0];
		double[] bVertices = new double[0];
		int[] aIndices = new int[0];
		int[] bIndices = new int[0];
		
		if (a.getNumPoints() != 0) aVertices=a.getVertexAttributes(Attribute.COORDINATES).toDoubleArray(null);
		if (b.getNumPoints() != 0) bVertices=b.getVertexAttributes(Attribute.COORDINATES).toDoubleArray(null);
		if (a.getNumEdges() != 0) aIndices=a.getEdgeAttributes(Attribute.INDICES).toIntArray(null);
		if (b.getNumEdges() != 0) bIndices=b.getEdgeAttributes(Attribute.INDICES).toIntArray(null);
		
		double[] vertices = new double[aVertices.length+bVertices.length];
		int[] indices = new int[aIndices.length+bIndices.length];
		
		for (int i=0; i<aVertices.length; i++)
			vertices[i] = aVertices[i];
		for (int i=0; i<bVertices.length; i++)
			vertices[i+aVertices.length] = bVertices[i];
		
		for (int i=0; i<aIndices.length; i++)
			indices[i] = aIndices[i];
		for (int i=0; i<bIndices.length; i++)
			indices[i+aIndices.length] = bIndices[i]+aVertices.length/3;
		
		IndexedLineSetFactory fac = new IndexedLineSetFactory();
		fac.setVertexCount(vertices.length/3);
		fac.setLineCount(indices.length/2);
		fac.setVertexCoordinates(vertices);
		fac.setEdgeIndices(indices);
		fac.update();
		
		return fac.getIndexedLineSet();		
	}
	
	
	//calculates the rotation from z-axis on specified axis
	private Quaternion getAxisRotation(int axis) {
	
		FactoredMatrix rot = new FactoredMatrix();
		switch(axis) {
		case X : rot.setRotation(Math.PI/2,0,1,0); break;
		case Y : rot.setRotation(-Math.PI/2,1,0,0); break;
		//case Z : z-axis => no rotation
		}
		return Quaternion.rotationMatrixToQuaternion(new Quaternion(), rot.getArray());
	}
	
	//calculates the rotation of ticks for specified box edge
	private Quaternion getTickRotation(int axis, int k) {
		
		int c = new int[]{0,3,1,2}[k];
		switch(axis) {  //regard axis
		case X : c++; break;
		case Y : c*=-1; c--;  //*-1 => switch 1 and 3
		}
		
		double[] rotationAxis = new double[3];
		rotationAxis[axis] = 1;
		FactoredMatrix rot = new FactoredMatrix();
		rot.setRotation(c*Math.PI/2, rotationAxis);

		return Quaternion.rotationMatrixToQuaternion(new Quaternion(), rot.getArray());
	}
	
	
	
	/**
	 * returns the SGC to which the specified key is mapped in hashMap
	 * @param key the key specifying the SGC
	 * @return the SGC 
	 */
	private SceneGraphComponent getSGC(Object key) {
		return (SceneGraphComponent)nodes.get(key);
	}


	private String toBinaryString(int k) {
		if (k<2) return ("0"+k);
		else return Integer.toBinaryString(k);
	}
	
	
	//round d to 3 decimal places
	private double round(double d) {
		return Math.round(d*1000)/1000.;
	}
	
	
	
//-----------------------------------------------------------------------------------
//the following methods are intended to be used in a tool
//to hide specific box vertices, axes or labels
//-----------------------------------------------------------------------------------
	
	/**
	 * get index of a box vertex which is "closest to the screen" when looking in a specified direction
	 * @param direction the direction
	 * @return the index of a closest box vertex in boxVertices[X]
	 */
	private int getClosestBoxVertex(double[] dir) {
		double[] direction;
		if (dir.length==3) direction=dir;
		else {
			direction=new double[3];
			direction[X]=dir[X]/dir[3];
			direction[Y]=dir[Y]/dir[3];
			direction[Z]=dir[Z]/dir[3];
		}
		//closest box vertex has minimal inner product with direction
		int closest = 0;
		
		double tmp = Rn.innerProduct(boxVertices[X][closest], direction);
		
		for (int k=1; k<8; k++) { //boxVertices[X] contains all box vertices
			if ( Rn.innerProduct(boxVertices[X][k], direction) < tmp) {
				closest = k;
				tmp = Rn.innerProduct(boxVertices[X][k], direction);
			}
		}
		return closest; //index of closest box vertex in boxVertices[X]
	}
	
	
	/**
	 * hide closest box vertex (resp. only edges if there are more than one closest box vertices)
	 */
	public void updateBox(double[] cameraToObject) {
		
		//direction of view in camera coordinates is (0,0,-1)
		//transform camera coordinates to local coordinates
		double[] direction = new Matrix(cameraToObject).multiplyVector(new double[]{0,0,-1, 0});
		direction[3]=1;
		
		//only do something if closest box vertex changed
		final int index = getClosestBoxVertex(direction);
		if (currentClosestBoxVertex == index) return;
		
		//set all adges and grid faces to visible
		for (int axis=X; axis<=Z; axis++) {
			for (int k=0; k<=3; k++) {
				getSGC(axesNames[axis]+toBinaryString(k)).setVisible(true);
			}
			getSGC("face"+2*axis).setVisible(true);
			getSGC("face"+(2*axis+1)).setVisible(true);
		}
		
		currentClosestBoxVertex = index;
		double[] closest = boxVertices[X][currentClosestBoxVertex];
		int[] edgeCriteria = new int[3];
		
		//get the 3 edges belonging to a closest box vertex
		for (int axis=X; axis<=Z; axis++) {
			if (closest[axis] == boxMin[axis]) edgeCriteria[axis] = 0;  //0 corresponds to vertex with minimum value on axis i
			else edgeCriteria[axis] = 1;  //1 corresponds to vertex with maximum value on axis i 
		}

		//hide edges which don't have copies of same "distance to the screen"
		if (direction[Y]!=0 && direction[Z]!=0)
			getSGC("x" + edgeCriteria[Y] + edgeCriteria[Z]).setVisible(false);
		if (direction[X]!=0 && direction[Z]!=0)
			getSGC("y" + edgeCriteria[X] + edgeCriteria[Z]).setVisible(false);
		if (direction[X]!=0 && direction[Y]!=0)
			getSGC("z" + edgeCriteria[X] + edgeCriteria[Y]).setVisible(false);
		
		//hide corresponding grid faces
		for (int axis=X; axis<=Z; axis++) 
			getSGC("face"+(2*axis+edgeCriteria[axis])).setVisible(false);
	}
	
	
	
//-----------------------------------------------------------------------------------
//SETTING & GETTING PROPERTIES
//-----------------------------------------------------------------------------------
	
	
	public SceneGraphComponent getCoordinateSystem() {
		
		return coordinateSystem;
	}
	
	
	
	//set distance between two ticks
	public void setAxisScale(double axisScale) {
		if (this.axisScale == axisScale) return;
		//else
		this.axisScale = axisScale;

		//update ticks and labels
		for (int axis = X; axis <= Z; axis++) { // for each coordinate axis

			//for box:
			for (int k = 0; k <= 3; k++) {
				//remove old ticks and labels
				SceneGraphComponent singleAxisK = getSGC(axesNames[axis]+ toBinaryString(k));  //e.g. x00
				singleAxisK.removeChild(getSGC(axesNames[axis]+ toBinaryString(k) + "ticks"));  //e.g. x00ticks
				//create new ticks with labels
				SceneGraphComponent ticks = getBoxTicks(axis, k, 
						boxVertices[axis][2 * k], boxVertices[axis][2 * k + 1]);
				//update hash table
				nodes.put(axesNames[axis] + toBinaryString(k) + "ticks", ticks);  //e.g. x00ticks
				//add new ticks and labels to SceneGraph
				singleAxisK.addChild(ticks);
			}

			//for axes:

			//remove old ticks and labels
			SceneGraphComponent singleAxis = getSGC(axesNames[axis] +"Axis");  //e.g. xAxis
			singleAxis.removeChild(getSGC(axesNames[axis]+"Ticks"));  //e.g. xTicks
			//create new ticks with labels
			SceneGraphComponent ticks = getAxesTicks(axis,
					axesVertices[axis][0], axesVertices[axis][1]);
			//update hash table
			nodes.put(axesNames[axis] + "Ticks", ticks);  //e.g. xTicks
			//add new ticks and labels to SceneGraph
			singleAxis.addChild(ticks);
		}
		
		//update grid
		box.removeChild(getSGC("grid"));
		box.addChild(calculate2DGrid());
	}
	
	//get distance between two ticks
	public double getAxisScale() {
		return axisScale;
	}
	
	
	//set stretch size of arrows, ticks and tick labels
	public void setLabelScale(double labelScale) {
		if (this.labelScale == labelScale) return;
		//else
		this.labelScale = labelScale;
		
		coordinateSystem.getAppearance().setAttribute("pointShader.scale", labelScale);
		
		//update size of arrows and ticks
		arrowStretch = 4*labelScale;
		tickStretch = 2*labelScale;
		SceneGraphComponent arrow, ticks;
		FactoredMatrix m;

		for (int axis=X; axis<=Z; axis++) {
			//for box:
			for (int k=0; k<=3; k++) {
				arrow = getSGC(axesNames[axis]+toBinaryString(k)+"arrow");  //e.g. x00arrow
				m = new FactoredMatrix(arrow.getTransformation());
				m.setStretch(arrowStretch); //stretch urCone
				Transformation trans = new Transformation(m.getArray());
				trans.setName("arrowTransformation");
				trans.setReadOnly(true);
				arrow.setTransformation(trans);
				
				ticks = getSGC(axesNames[axis]+toBinaryString(k)+"ticks");  //e.g. x00ticks
				m = new FactoredMatrix(ticks.getTransformation());
				m.setStretch(tickStretch, tickStretch, 1); //stretch ticks
				trans = new Transformation(m.getArray());
				trans.setName("tickTransformation");
				trans.setReadOnly(true);
				ticks.setTransformation(trans);
			}
			//for axes:
			arrow = getSGC(axesNames[axis]+"Arrow");  //e.g. xArrow
			m = new FactoredMatrix(arrow.getTransformation());
			m.setStretch(arrowStretch); //stretch urCone
			Transformation trans = new Transformation(m.getArray());
			trans.setName("arrowTransformation");
			trans.setReadOnly(true);
			arrow.setTransformation(trans);
			
			ticks = getSGC(axesNames[axis]+"Ticks");  //e.g. xTicks
			m = new FactoredMatrix(ticks.getTransformation());
			m.setStretch(tickStretch, tickStretch, 1); //stretch ticks
			trans = new Transformation(m.getArray());
			trans.setName("tickTransformation");
			trans.setReadOnly(true);
			ticks.setTransformation(trans);
		}
		// recalculate tick levels since arrow size changed ???
	}
	
	//set stretch size of arrows, ticks and tick labels
	public double getLabelScale() {
		return labelScale;
	}
	
	
	/**
	 * show or hide the axes of the coordinate system, whose extent is specified by the choice of the constructor
	 * (either by an existing SceneGraphComponent or by a given extent)
	 */
	public void showAxes(boolean b) {
		showAxes = b;
		axes.setVisible(b);
	}

	
	/**
	 * show or hide the bounding box of the coordinate system, which is specified by the choice of the constructor
	 * (either by an existing SceneGraphComponent or by a given extent)
	 */
	public void showBox(boolean b) {
		showBox = b;
		box.setVisible(b);
	}


	public void showGrid(boolean b) {
		showGrid = b;
		getSGC("grid").setVisible(b);
	}
	
	
	public void showAxesArrows(boolean b) {
		//if (showAxesArrows==b) return;
		showAxesArrows = b;
		//set visiblity of all arrows
		for (int axis=X; axis<=Z; axis++)
			getSGC(axesNames[axis]+"Arrow").setVisible(b);
	}

	
	public void showBoxArrows(boolean b) {
		//if (showBoxArrows==b) return;
		showBoxArrows = b;
		//set visiblity of all arrows
		for (int axis=X; axis<=Z; axis++) {
			for (int k=0; k<=3; k++) {
				getSGC(axesNames[axis]+toBinaryString(k)+"arrow").setVisible(b);
			}
		}
	}
	
	
	//show or hide tick & axis labels of axes and box
	public void showLabels(boolean b) {
		//if (showLabels==b) return;
		showLabels = b;
		coordinateSystem.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, b);
	}


	//set color of coordinate system
	public void setColor(Color c) {
		coordinateSystemColor = c;
		coordinateSystem.getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, c);
		coordinateSystem.getAppearance().setAttribute(CommonAttributes.POLYGON_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, c);
	}
	//get color of box
	public Color setColor() {
		return coordinateSystemColor;
	}

	
	//set color of labels
	public void setLabelColor(Color c) {
		labelColor = c;
		coordinateSystem.getAppearance().setAttribute(CommonAttributes.POINT_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, c);
	}
	//get color of labels
	public Color getLabelColor() {
		return labelColor;
	}

	
	//set color of grid
	public void setGridColor(Color c) {
		gridColor = c;
		getSGC("grid").getAppearance().setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, c);
	}
	//get color of grid
	public Color getGridColor() {
		return gridColor;
	}
	
}