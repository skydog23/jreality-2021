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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.proxy.tree.SceneTreeNode;
import de.jreality.ui.treeview.JTreeRenderer;
import de.jreality.ui.treeview.SceneTreeModel;
import de.jreality.ui.treeview.SceneTreeModel.TreeTool;
import de.jtem.beans.BooleanEditor;
import de.jtem.beans.EditorSpawner;
import de.jtem.beans.InspectorPanel;
import de.jtem.beans.NumberSpinnerEditor;


/**
 * Scene tree and inspector panel for a given scene graph.
 * 
 * @author msommer
 */
public class Navigator implements SelectionListener {

	private InspectorPanel inspector;
	private JTree sceneTree;
	private SceneTreeModel treeModel;
	private TreeSelectionModel tsm;

	private SelectionManagerInterface selectionManager;
	private SelectionManagerInterface externalSelectionManager;
	private Viewer viewer;  //the underlying viewer

	private Container navigator;
	private Component parentComp;
	
	private boolean propagateSelections, receiveSelections;


	/**
	 * @param sceneRoot the scene root
	 * @param selectionManager the underlying selection manager
	 */
	public Navigator(Viewer viewer) {
		this(viewer, null);
	}


	/**
	 * @param sceneRoot the scene root
	 * @param selectionManager the underlying selection manager
	 * @param parentComp used by dialogs from the context menu (<code>null</code> allowed)
	 */
	public Navigator(Viewer viewer, Component parentComp) {

		externalSelectionManager = SelectionManager.selectionManagerForViewer(viewer);
		selectionManager = new SelectionManager(externalSelectionManager.getDefaultSelection());
		//selectionManager.addSelectionListener(this);
		
		setPropagateSelections(true);
		setReceiveSelections(true);

		this.parentComp = parentComp;

		inspector = new InspectorPanel(false);
		BooleanEditor.setNameOfNull("inherit");
		EditorSpawner.setNameOfNull("inherit");
		EditorSpawner.setNameOfCreation("inherited");
		NumberSpinnerEditor.setNameOfNull("inherit");
		NumberSpinnerEditor.setNameOfCreation("inherited");
		//EditorManager.registerEditor(Texture2D.class, ObjectEditor.class);

		sceneTree = new JTree();
		treeModel = new SceneTreeModel(viewer.getSceneRoot());

		sceneTree.setModel(treeModel);
		//set default (anchor) selection (use the selection manager's default)
		sceneTree.setAnchorSelectionPath(new TreePath(treeModel.convertSelection(selectionManager.getDefaultSelection())));
		sceneTree.setCellRenderer(new JTreeRenderer());
		sceneTree.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "toggle");  //collaps/expand nodes with ENTER

		tsm = sceneTree.getSelectionModel();
		tsm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		tsm.addTreeSelectionListener(new SelectionListener(){

			public void selectionChanged(SelectionEvent e) {

				Selection currentSelection = e.getSelection();
				//update inspector
				inspector.setObject(currentSelection.getLastElement());
				//update selection managers
				selectionManager.setSelection(currentSelection);
				if (propagateSelections) externalSelectionManager.setSelection(currentSelection);  //does nothing if already selected
			}
		});

		try {  //set default selection
			tsm.setSelectionPath(new TreePath(treeModel.convertSelection(selectionManager.getDefaultSelection())));  //select current selection
		} catch (Exception e) {
			//no valid default selection
		}
		
//		SceneGraphPath sgp = sm.getSelectionPath();
//		if (sgp != null && sgp.isValid()) 
//		    tsm.setSelectionPath(new TreePath(treeModel.convertSelection(sm.getSelection())));  //select current selection
		
		setupContextMenu();
	}

	
	public void selectionChanged(de.jreality.ui.viewerapp.SelectionEvent e) {
		//convert selection of manager into TreePath
		Object[] selection = null;
		try {
			selection = treeModel.convertSelection(e.getSelection());			
			TreePath path = new TreePath(selection);

			if (e.nodeSelected() && !path.equals(tsm.getSelectionPath()))  //compare paths only if a node is selected  
				tsm.setSelectionPath(path);

		} catch (NullPointerException npe) {
			//SelectionManager's selection is not valid, 
			//i.e. has no representation in tree view (scene graph)
		}
	}


	public InspectorPanel getInspector() {
		return inspector;
	}


	public JTree getSceneTree() {
		return sceneTree;
	}


	public TreeSelectionModel getTreeSelectionModel() {
		return tsm;
	}


	public SceneGraphComponent getSceneRoot() {
		return viewer.getSceneRoot();
	}


	public Selection getSelection() {
		return selectionManager.getSelection();
	}


	private void setupContextMenu() {

		final JPopupMenu cm = new JPopupMenu();
		cm.setLightWeightPopupEnabled(false);

		//create content of context menu
		JMenu editMenu = null;
		ActionMap editActions = null;
		try {
			editMenu = ViewerAppMenu.createEditMenu(parentComp, selectionManager);
			editActions = ViewerAppMenu.updateActionMap(editMenu.getActionMap(), editMenu);
		} catch (Exception e) {
			return;  //menu or actions not in classpath
		}
		for (Component c : editMenu.getMenuComponents()) cm.add(c);

		//add listener to the navigator's tree
		sceneTree.addMouseListener(new MouseAdapter() {

			public void mousePressed( MouseEvent e ) {
				handlePopup( e );
			}

			public void mouseReleased( MouseEvent e ) {
				handlePopup( e );
			}

			private void handlePopup( MouseEvent e ) {
				if ( e.isPopupTrigger() ) {
					TreePath path = sceneTree.getPathForLocation( e.getX(), e.getY() );
					if ( path != null ) {
						tsm.clearSelection();  //ensures that SelectionListeners are notified even if path did not change
						tsm.setSelectionPath( path );
						cm.show( e.getComponent(), e.getX(), e.getY()+10 );
					}
				}
			}
		});
		
		//set up input and action map to match actions of context menu instead of viewers menu bar
		try {
			Object[] keys = editActions.keys();
			for (int i = 0; i < keys.length; i++) {
				KeyStroke key = (KeyStroke) keys[i];
				sceneTree.getInputMap().put(key, key);
				sceneTree.getActionMap().put(key, editActions.get(key));
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}


	/**
	 * Get the navigator as a Component.
	 * @return the navigator
	 */
	public Component getComponent() {

		if (navigator == null) {
			sceneTree.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
			JScrollPane top = new JScrollPane(sceneTree);
			top.setBorder(BorderFactory.createEmptyBorder());

			inspector.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
			JScrollPane bottom = new JScrollPane(inspector);
			bottom.setBorder(BorderFactory.createEmptyBorder());

			JSplitPane navigator = new JSplitPane(
					JSplitPane.VERTICAL_SPLIT, top, bottom);
			navigator.setContinuousLayout(true);
			navigator.setOneTouchExpandable(true);
			navigator.setResizeWeight(1.0);  //use extra space for sceneTree
			navigator.setBorder(BorderFactory.createEmptyBorder());

//			navigator.setPreferredSize(new Dimension(0,0));  //let user set the size
			this.navigator = new Container();
			this.navigator.setLayout(new BorderLayout());
			this.navigator.add(navigator);
			this.navigator.add(createToolBar(), BorderLayout.NORTH);
		}

		return navigator;
	}

	
	private JToolBar createToolBar() {
		
		JToolBar jtb = new JToolBar(SwingConstants.HORIZONTAL);
		jtb.setFloatable(false);
		
		Action a;
		final JCheckBox propagate = new JCheckBox();
//		final URL propagateImg = Navigator.class.getResource("propagate.png");
		a = new AbstractAction("Propagate"){
			{	//putValue(Action.SMALL_ICON, new ImageIcon(propagateImg));
				putValue(Action.SHORT_DESCRIPTION, "Propagate selections to the SelectionManager"); }
			public void actionPerformed(ActionEvent e) {
				setPropagateSelections(propagate.isSelected());
			}
		};
		propagate.setAction(a);
		propagate.setSelected(propagateSelections);
		jtb.add(propagate);
		
		final JCheckBox receive = new JCheckBox();
//		final URL receiveImg = Navigator.class.getResource("receive.png");
		a = new AbstractAction("Receive"){
			{	//putValue(Action.SMALL_ICON, new ImageIcon(receiveImg));
				putValue(Action.SHORT_DESCRIPTION, "Receive selections from the SelectionManager"); }
			public void actionPerformed(ActionEvent e) {
				 setReceiveSelections(receive.isSelected());
			}
		};
		receive.setAction(a);
		receive.setSelected(receiveSelections);
		jtb.add(receive);
		
		return jtb;
	}
	
	
	/**
	 * Propagate selections to the underlying viewer's selection manager.
	 */
	public void setPropagateSelections(boolean propagate) {
		propagateSelections = propagate;
	}
	

	/**
	 * Receive selections from the underlying viewer's selection manager.
	 */
	public void setReceiveSelections(boolean receive) {
		receiveSelections = receive;
		if (receive) 
			externalSelectionManager.addSelectionListener(Navigator.this);
		else externalSelectionManager.removeSelectionListener(Navigator.this);
	}
	

//	-- INNER CLASSES -----------------------------------

	public static abstract class SelectionListener implements TreeSelectionListener {

		public abstract void selectionChanged(SelectionEvent e);

		public void valueChanged(TreeSelectionEvent e) {

			boolean[] areNew = new boolean[e.getPaths().length];
			for (int i = 0; i < areNew.length; i++)
				areNew[i] = e.isAddedPath(i);

			SelectionEvent se = new SelectionEvent(e.getSource(), e.getPaths(), 
					areNew, e.getOldLeadSelectionPath(), e.getNewLeadSelectionPath()); 

			selectionChanged(se);
		}

	}  //end of class SelectionListener


	public static class SelectionEvent extends TreeSelectionEvent {

		/** calls TreeSelectionEvent(...) */
		public SelectionEvent(Object source, TreePath[] paths, boolean[] areNew, TreePath oldLeadSelectionPath, TreePath newLeadSelectionPath) {
			super(source, paths, areNew, oldLeadSelectionPath, newLeadSelectionPath);
		}

		private Object convert(Object o) {
			if (o instanceof SceneTreeNode)
				return ((SceneTreeNode) o).getNode();
			else if (o instanceof TreeTool)
				return ((TreeTool) o).getTool();

			return o;
		}

		/**
		 * Converts the TreePath of the current selection into a Selection object.  
		 * @return the current selection
		 */
		public Selection getSelection() {
			Selection selection = new Selection();
			Object[] treePath = getPath().getPath();
			for (int i = 0; i < treePath.length; i++)
				selection.push( convert(treePath[i]) );
			return selection;
		}

	}  //end of class SelectionEvent

}