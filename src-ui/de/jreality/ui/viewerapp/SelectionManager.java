/**
 *
 * This file is part of jReality. jReality is open source software, made
 * available under a BSD license:
 *
 * Copyright (c) 2003-2006, jReality Group: Charles Gunn, Tim Hoffmann, Markus
 * Schmies, Steffen Weissmann.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of jReality nor the names of its contributors nor the
 *   names of their associated organizations may be used to endorse or promote
 *   products derived from this software without specific prior written
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */


package de.jreality.ui.viewerapp;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.WeakHashMap;

import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Viewer;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.LoggingSystem;
import de.jreality.util.Rectangle3D;
import de.jreality.util.Secure;
import de.jreality.util.SystemProperties;


/**
 * Manages selections within a jReality scene.
 * 
 * @author msommer
 */
public class SelectionManager implements SelectionManagerInterface {

	private Selection defaultSelection;
	private Selection selection;

	private Vector<SelectionListener> listeners;

	private boolean renderSelection = false;  //default
	private SceneGraphComponent selectionKit;
	private SceneGraphComponent selectionKitOwner;

	
	static WeakHashMap<Viewer, SelectionManagerInterface> globalTable = new WeakHashMap<Viewer, SelectionManagerInterface>();
	
	public static SelectionManagerInterface selectionManagerForViewer(Viewer viewer)	{
		SelectionManagerInterface sm = null;
		if (globalTable.get(viewer)!=null) 
			sm = globalTable.get(viewer);
		
		if (sm != null)	{
			LoggingSystem.getLogger(SelectionManager.class).fine("Selection manager is "+sm);
			return sm;
		}
		//get all depending viewers in hierarchy and check if SelectionManager already exists
		List<Viewer> viewers = new LinkedList<Viewer>();
		Viewer v = viewer;
		viewers.add(v);
		if (v instanceof ViewerSwitch) {
			Viewer[] vs = ((ViewerSwitch)v).getViewers();
			sm = globalTable.get((ViewerSwitch)v);  //should be null
			for (int i = 0; i < vs.length; i++) {
				viewers.add(vs[i]);
				if (globalTable.get(vs[i]) != null) {  //SelectionManager exists for vs[i]
					if (sm!=null && sm!=globalTable.get(vs[i])) 
						System.err.println("Distinct SelectionManagers used in viewer hierarchy of "+v);
					sm = globalTable.get(vs[i]);
				}
			}
		} else {
			if (globalTable.get(v) != null) {  //SelectionManager exists for vs[i]
				if (sm!=null && sm!=globalTable.get(v)) 
					System.err.println("Distinct SelectionManagers used in viewer hierarchy of "+v);
				sm = globalTable.get(v);
			}
		}
		
		if (sm == null) {  //create new SelectionManager
			String selectionManager = Secure.getProperty(SystemProperties.SELECTION_MANAGER, SystemProperties.SELECTION_MANAGER_DEFAULT);		
			try { sm = (SelectionManagerInterface) Class.forName(selectionManager).newInstance();	} 
			catch (Exception e) {	e.printStackTrace(); } 
		}
		
		//add mapping for viewer depending viewers
		for (Viewer vw : viewers) globalTable.put(vw, sm);
		
//		if (viewer instanceof ToolSystemViewer) {  //used by ViewerApp
//			sm.setDefaultSelection( new Selection( ((ToolSystemViewer)viewer).getEmptyPickPath() ) );
//			sm.setSelection(sm.getDefaultSelection());
//		}
		
		return sm;
	}
	
	
	public SelectionManager() {
		this(null);
	}
	
	
	public SelectionManager(Selection defaultSelection) {
//		if (defaultSelection == null)
//			throw new IllegalArgumentException("Default selection is null!");

		listeners = new Vector<SelectionListener>();

		//set default selection and select it
		if (defaultSelection!=null) {
			setDefaultSelection(defaultSelection);
			setSelection(null);
		}
	}


	public Selection getDefaultSelection() {
		return defaultSelection;
	}


	public void setDefaultSelection(Selection defaultSelection) {
		this.defaultSelection = defaultSelection;
	}


	public Selection getSelection() {
		return selection;
	}


	/**
	 * Set the current selection.
	 * @param selection the current Selection object 
	 */
	public void setSelection(Selection selection) {
		if (this.selection!=null && this.selection.equals(selection)) return;  //already selected

		if (selection == null)  //nothing selected
			this.selection = defaultSelection;
		else this.selection = selection;

		selectionChanged();
	}


	public void addSelectionListener(SelectionListener listener)  {
		if (listeners.contains(listener)) return;
		listeners.add(listener);
	}


	public void removeSelectionListener(SelectionListener listener) {
		listeners.remove(listener);
	}


	public void selectionChanged() {

		if (!listeners.isEmpty()) {
			for (int i = 0; i<listeners.size(); i++)  {
				SelectionListener l = listeners.get(i);
				l.selectionChanged(new SelectionEvent(this, selection));
			}
		}

		if (renderSelection) {
			updateBoundingBox();
			selectionKit.setVisible(true);
		}
	}


	private void updateBoundingBox() {

		if (selectionKit == null) {
			//set up representation of selection in scene graph
			selectionKit = new SceneGraphComponent("boundingBox");
			selectionKit.setOwner(this);
			Appearance app = new Appearance("app");
			app.setAttribute(CommonAttributes.EDGE_DRAW,true);
			app.setAttribute(CommonAttributes.FACE_DRAW,false);
			app.setAttribute(CommonAttributes.VERTEX_DRAW,false);
			app.setAttribute(CommonAttributes.LIGHTING_ENABLED,false);
			app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_STIPPLE,true);
			app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_FACTOR, 1.0);
			app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_STIPPLE_PATTERN, 0x6666);
			app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.LINE_WIDTH, 2.0);
			app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DEPTH_FUDGE_FACTOR, 1.0);
			app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.TUBES_DRAW, false);
			app.setAttribute(CommonAttributes.LEVEL_OF_DETAIL,0.0);
			app.setAttribute(CommonAttributes.LINE_SHADER+"."+CommonAttributes.DIFFUSE_COLOR, java.awt.Color.WHITE);
			selectionKit.setAppearance(app);
		}

		if (selection.getLastComponent() == selectionKit) 
			return;  //bounding box selected

		Rectangle3D bbox = BoundingBoxUtility.calculateChildrenBoundingBox( selection.getLastComponent() ); 

		IndexedFaceSet box = null;
		box = IndexedFaceSetUtility.representAsSceneGraph(box, bbox);
		box.setGeometryAttributes(CommonAttributes.PICKABLE, false);

		selectionKit.setGeometry(box);
		if (selectionKitOwner!=null) selectionKitOwner.removeChild(selectionKit);
		selectionKitOwner = selection.getLastComponent();
		selectionKitOwner.addChild(selectionKit);
	}


	public boolean isRenderSelection() {
		return renderSelection;
	}


	public void setRenderSelection(boolean renderSelection) {
		this.renderSelection = renderSelection;
		if (renderSelection) updateBoundingBox();
		if (selectionKit != null) selectionKit.setVisible(renderSelection);
	}


	public SceneGraphPath getDefaultSelectionPath() {
		return getDefaultSelection().getSGPath();
	}


	public SceneGraphPath getSelectionPath() {
		return getSelection().getSGPath();
	}


	public void setDefaultSelectionPath(SceneGraphPath defaultSelection) {
		setDefaultSelection(new Selection(defaultSelection));
	}


	public void setSelectionPath(SceneGraphPath selection) {
		setSelection(new Selection(selection));
	}

}