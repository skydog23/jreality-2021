/*
 * Created on Apr 13, 2005
 *
 * This file is part of the  package.
 * 
 * This program is free software; you can redistribute and/or modify 
 * it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation; either version 2 of the license, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITTNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307
 * USA 
 */
package de.jreality.portal;

import java.awt.Component;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import de.jreality.jogl.Viewer;
import de.jreality.scene.Lock;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.proxy.scene.RemoteSceneGraphComponent;
import de.jreality.scene.proxy.smrj.SMRJMirrorScene;
import de.smrj.RemoteFactory;
import de.smrj.tcp.TCPBroadcasterNIO;

/**
 * @author weissman
 * 
 */
public class NewPortalServerViewer implements de.jreality.scene.Viewer {

  SceneGraphComponent root;
  SceneGraphComponent auxRoot;
	SceneGraphPath camPath;
  private int signature;

	RemoteFactory factory;
	RemoteViewer clients;

	SMRJMirrorScene proxyScene;
  final Lock renderLock = new Lock();

  public NewPortalServerViewer() throws RemoteException, IOException, NotBoundException {
    this(new TCPBroadcasterNIO(8868).getRemoteFactory());
  }
  
	public NewPortalServerViewer(RemoteFactory factory, Class viewerClass) throws IOException,
	MalformedURLException, RemoteException, NotBoundException {
      init(factory, viewerClass);
    }

	public NewPortalServerViewer(RemoteFactory factory) throws IOException, RemoteException, NotBoundException {
    init(factory, Viewer.class);
	}

  public void init(RemoteFactory factory, Class viewerClass) throws IOException {
    this.factory = factory;
    clients = (RemoteViewer) factory.createRemoteViaStaticMethod(
        HeadTrackedViewer.class, HeadTrackedViewer.class,
        "createFullscreen", new Class[]{Class.class}, new Object[]{viewerClass});
    proxyScene = new SMRJMirrorScene(factory, renderLock);
  }
  
  public SceneGraphComponent getSceneRoot() {
		return root;
	}

	public void setSceneRoot(SceneGraphComponent r) {
    if (root != null) {
      // dispose proxies for old root
      proxyScene.disposeProxy(root);
    }
		root = r;
    if (root != null) {
  		RemoteSceneGraphComponent rsgc = (RemoteSceneGraphComponent) proxyScene.createProxyScene(root);
	  	clients.setRemoteSceneRoot(rsgc);
    } else {
      clients.setRemoteSceneRoot(null);
    }
	}

	public SceneGraphPath getCameraPath() {
		return camPath;
	}

	public void setCameraPath(SceneGraphPath p) {
		camPath = p;
		clients.setRemoteCameraPath(p == null ? null : proxyScene.getProxies(p.toList()));
	}

	/**
	 * TODO: open frame for keyboard/mouse input!?
	 */
	public boolean hasViewingComponent() {
		return false;
	}

	public Component getViewingComponent() {
		return null;
	}

	public void initializeFrom(Viewer v) {
	  setSceneRoot(v.getSceneRoot());
    setCameraPath(v.getCameraPath());
    setAuxiliaryRoot(v.getAuxiliaryRoot());
	}

	public int getSignature() {
		return signature;
	}

	public void setSignature(int sig) {
		this.signature = sig;
		clients.setSignature(this.signature);
	}

	public SceneGraphComponent getAuxiliaryRoot() {
	  return auxRoot;
	}

  public void setAuxiliaryRoot(SceneGraphComponent ar) {
    this.auxRoot = ar;
    RemoteSceneGraphComponent rsgc = (RemoteSceneGraphComponent) proxyScene.createProxyScene(auxRoot);
    clients.setRemoteAuxiliaryRoot(rsgc);
  }

  public void render() {
    renderStart();
    renderEnd();
  }
  
  void renderStart() {
    renderLock.writeLock();
    clients.render();
  }

  void renderEnd() {
    clients.waitForRenderFinish();
    renderLock.writeUnlock();
  }
}
