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


package de.jreality.toolsystem.raw;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JPanel;

import de.jreality.scene.Viewer;
import de.jreality.scene.tool.AxisState;
import de.jreality.scene.tool.InputSlot;
import de.jreality.toolsystem.ToolEvent;
import de.jreality.toolsystem.ToolEventQueue;
import de.jreality.util.LoggingSystem;

/**
 * This driver mainly consists of a woraround for the keyboard auto-repeat:
 * 
 * Windows:
 * The auto-repeat on windows triggers repeating key-pressed events, and also
 * key-typed events which we ignore. So the only thing to do is to check the
 * last state of the key and only handle a key-pressed if the key was not
 * pressed before. This happens in poll().
 * 
 * Linux:
 * The auto-repeat on linux triggers repeating key-released/key-pressed events,
 * so we enqueue the occuring events and merge them if possible: When a
 * key-released event occurs, we check if there is a matching key-pressed event
 * in the queue - if yes ignore both events. Happens in handleEvent(..).
 * 
 * MacOS:
 * Seems to work!
 * 
 * Solaris etc.:
 * TODO!
 * 
 * @author Steffen Weissmann
 **/
public class DeviceKeyboard implements RawDevice, AWTEventListener, PollingDevice {
  
	private HashMap<Integer, Boolean> keyState = new HashMap<Integer, Boolean>();
	
    private HashMap<Integer, InputSlot> keysToVirtual = new HashMap<Integer, InputSlot>();
    
    private ToolEventQueue queue;
    private Component component;
    
    private LinkedList<KeyEvent> myQueue = new LinkedList<KeyEvent>();
    
    public void initialize(Viewer viewer) {
      if (!viewer.hasViewingComponent() || !(viewer.getViewingComponent() instanceof Component) ) throw new UnsupportedOperationException("need AWT component");
      this.component = (Component) viewer.getViewingComponent();
      //this.component.addKeyListener(this);
      Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
    }

    public synchronized void keyPressed(KeyEvent e) {
    	InputSlot id = keysToVirtual.get(e.getKeyCode());
        if (id != null) {
          handleEvent(e);
      	  LoggingSystem.getLogger(this).fine(this.hashCode()+" added key pressed ["+id+"] "+e.getWhen());
        }
    }
    
	private synchronized void handleEvent(KeyEvent ev) {
		if (ev.getID() == KeyEvent.KEY_RELEASED) {
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			KeyEvent next = (KeyEvent) Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent(KeyEvent.KEY_PRESSED);
//			if (next != null && (next.getWhen() - ev.getWhen()) < 5) {
//				next.consume();
//				return;
//			}
			myQueue.addLast(ev);
		} else {
			if (myQueue.size() == 0) myQueue.addLast(ev);
			else {
				// two associated RELEASED/PRESSED events seem to differ at most +-1
				if ((ev.getWhen()-myQueue.getLast().getWhen())<2) myQueue.removeLast();
				else myQueue.addLast(ev);
			}
		}
	}

	public synchronized void keyReleased(final KeyEvent e) {
        InputSlot id = keysToVirtual.get(e.getKeyCode());
        if (id != null) {
            handleEvent(e);
        	LoggingSystem.getLogger(this).finer("added key released ["+id+"] "+e.getWhen());
        }
    }

	public void keyTyped(KeyEvent e) {
		//System.out.println("DeviceKeyboard.keyTyped()");
	}

	public ToolEvent mapRawDevice(String rawDeviceName, InputSlot inputDevice) {
        // rawDeviceName = VK_W (e.g.)
        keysToVirtual.put(resolveKeyCode(rawDeviceName), inputDevice);
        return new ToolEvent(this, inputDevice, AxisState.ORIGIN);
    }
    
    private int resolveKeyCode(String fieldName) {
      try {
        int val = KeyEvent.class.getField(fieldName).getInt(KeyEvent.class);
        return val;
      } catch (Exception e) {
        throw new IllegalArgumentException("no such key "+fieldName);
      }
      
    }

    public void setEventQueue(ToolEventQueue queue) {
        this.queue = queue; 
    }

    public void dispose() {
    	//component.removeKeyListener(this);
    	Toolkit.getDefaultToolkit().removeAWTEventListener(this);
    }
    
    public String getName() {
        return "Keyboard";
    }
    
    public String toString() {
      return "RawDevice: Keyboard";
    }

	public synchronized void poll() {
		long ct = System.currentTimeMillis();
		while (!myQueue.isEmpty()) {

			// heuristic min age 3 ms
			long dt = ct - myQueue.getFirst().getWhen();
			if (dt < 3) return;

			KeyEvent ev = myQueue.removeFirst();
			boolean pressed = ev.getID() == KeyEvent.KEY_PRESSED;
			if (!pressed) {
				KeyEvent next = (KeyEvent) Toolkit.getDefaultToolkit().getSystemEventQueue().peekEvent(KeyEvent.KEY_PRESSED);
				if (next != null && next.getKeyCode() == ev.getKeyCode()) {
					//two associated RELEASED/PRESSED events seem to differ at most +-1
					if (next.getWhen() - ev.getWhen() < 2) {
						next.consume();
						continue;
					}
				}
			}
			if (keyState.get(ev.getKeyCode()) != Boolean.valueOf(pressed)) {
				AxisState state = pressed ? AxisState.PRESSED : AxisState.ORIGIN;
				ToolEvent event = new ToolEvent(ev, keysToVirtual.get(ev.getKeyCode()), state);
				//System.out.println("dt="+dt+"  ["+ev.getWhen()+"] "+event);
				keyState.put((ev).getKeyCode(), pressed);
				queue.addEvent(event);
			}
		}
	}

	public void eventDispatched(AWTEvent event) {
		switch (event.getID()) {
		case KeyEvent.KEY_PRESSED:
			hasFocus=false;
			checkFocus();
			if (hasFocus) keyPressed((KeyEvent) event);
			break;
		case KeyEvent.KEY_RELEASED:
			keyReleased((KeyEvent) event);
		default:
			break;
		}
	}

	private void checkFocus() {
		if (component instanceof Container) {
			checkFocus((Container) component);
		} else {
			hasFocus = component.isFocusOwner();
		}
	}

	boolean hasFocus=false;
	
	private void checkFocus(Container cc) {
		for (Component c : cc.getComponents()) {
			if (c instanceof Container) checkFocus((Container) c);
			if (c.isFocusOwner()) hasFocus=true;
		}
	}
}
