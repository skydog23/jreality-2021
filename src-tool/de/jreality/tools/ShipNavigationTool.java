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


package de.jreality.tools;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.AxisState;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.util.LoggingSystem;

/**
 * @author weissman
 *  
 */
public class ShipNavigationTool extends AbstractTool {

  private transient final InputSlot forwardBackward = InputSlot.getDevice("ForwardBackwardAxis");
  private transient final InputSlot leftRight = InputSlot.getDevice("LeftRightAxis");
  private transient final InputSlot jump = InputSlot.getDevice("JumpActivation");
  private transient final InputSlot rotateActivation = InputSlot.getDevice("ShipRotateActivation");
  private transient final InputSlot horizontalRotation = InputSlot.getDevice("HorizontalShipRotationAngleEvolution");
  private transient final InputSlot timer = InputSlot.getDevice("SystemTime");
  private transient final InputSlot run = InputSlot.getDevice("RunActivation");

  private transient final InputSlot gravityToggle = InputSlot.getDevice("GravityToggle");
  private transient final InputSlot groundToggle = InputSlot.getDevice("GroundToggle");

  private transient double[] velocity = {0,0,0};
  private transient boolean touchGround;
  
  double gain = 4;
  private double runFactor=2;
  private double gravity = 9.81;
  private transient double lastGravity = 0;
  private double jumpSpeed = 8;
  private transient double lastJumpSpeed;
  private transient boolean rotate=false;
  
  private boolean center = false;

  private boolean pollingDevice=true; // should be true for mouse look, false for some axis/button device TODO!!
  private transient boolean fall;
  
  public ShipNavigationTool() {
    addCurrentSlot(forwardBackward);
    addCurrentSlot(leftRight);
    addCurrentSlot(rotateActivation);
    addCurrentSlot(jump);
    addCurrentSlot(gravityToggle);
    addCurrentSlot(groundToggle);
  }

  public void perform(ToolContext tc) {
	  
	  if (tc.getSource() == gravityToggle) {
		  if (tc.getAxisState(gravityToggle).isReleased()) return;
		  double lg = gravity;
		  gravity=lastGravity;
		  lastGravity=lg;
		  velocity[1]=0;
		  touchGround=gravity==0;
	  }
	  
	  if (tc.getSource() == groundToggle) {
		  fall=tc.getAxisState(groundToggle).isPressed();
		  if (fall) touchGround=false;
	  }

    if (rotate) {
      if (!tc.getAxisState(rotateActivation).isPressed()) {
        removeCurrentSlot(horizontalRotation);
        rotate = false;
      }
    } else {
      if (tc.getAxisState(rotateActivation).isPressed()) {
        addCurrentSlot(horizontalRotation);
        rotate = true;
      }
    }
    if (tc.getSource() == rotateActivation) return;
    checkNeedTimer(tc);
    SceneGraphPath path = tc.getRootToLocal();
    SceneGraphComponent myComponent = path.getLastComponent();
    Matrix myMatrix = myComponent.getTransformation() != null ? new Matrix(myComponent.getTransformation()) : new Matrix();
    if (pollingDevice && tc.getSource() == horizontalRotation) {
      MatrixBuilder.euclidean(myMatrix).rotateY(-tc.getAxisState(horizontalRotation).doubleValue());
      myMatrix.assignTo(myComponent);
      return;
    }
    if (tc.getSource() == jump && tc.getAxisState(jump).isPressed() && gravity != 0) {
      velocity[1] = jumpSpeed;
    }
    if (tc.getSource() == timer) {
      AxisState axis;
      if ((axis = tc.getAxisState(forwardBackward)) != null) {
        velocity[2] = gain*axis.doubleValue();
      }
      if ((axis = tc.getAxisState(leftRight)) != null) {
        velocity[0] = -gain*axis.doubleValue();
      }
      if (tc.getAxisState(run) != null && tc.getAxisState(run).isPressed()) {
        velocity[0]*=runFactor;
        velocity[2]*=runFactor;
      }
      if (!(touchGround && velocity[0] == 0 && velocity[1] == 0 && velocity[2] == 0)) {

        double sec = 0.001* tc.getAxisState(timer).intValue(); // time since
    	if (!pollingDevice) {
  	      MatrixBuilder.euclidean(myMatrix).rotateY(-tc.getAxisState(horizontalRotation).doubleValue()*sec).assignTo(myComponent);
      	}
        double[] trans = new double[]{sec*velocity[0], sec*velocity[1], sec*velocity[2], 1};
        double[] dest = myMatrix.multiplyVector(trans);

        if (myMatrix.containsNanOrInfinite()) System.out.println("NAN!!! 1");
        
        double[] pickStart;
        if (!center) pickStart = new double[]{dest[0], dest[1]+1.7, dest[2], 1};
        else {
        	double n = Math.sqrt(dest[0]*dest[0]+dest[1]*dest[1]+dest[2]*dest[2]);
        	if (n != 0) n=1.7/n;
        	n+=1;
        	pickStart = new double[]{dest[0]*n, dest[1]*n, dest[2]*n, 1};
        }
        List picks = Collections.EMPTY_LIST;
        if (gravity != 0) {
          if (!fall) {
	          try {
	            picks = tc.getPickSystem().computePick(pickStart, dest);
	          } catch (Exception e) {
	            LoggingSystem.getLogger(this).warning("pick system error");
	            return;
	          }
          }
          if (!picks.isEmpty()) {
            PickResult pr = (PickResult) picks.get(0);
            double[] hit = pr.getWorldCoordinates();
            dest[1] = hit[1];
            velocity[1] = 0;
            touchGround = true;
          } else {
            velocity[1] -= sec*gravity;
            touchGround = false;
          }
        }
        if (center)	{
        	Matrix rotation = MatrixBuilder.euclidean().rotateFromTo(myMatrix.getColumn(3), dest).getMatrix();
        	if (!rotation.containsNanOrInfinite()) myMatrix.multiplyOnLeft(rotation);
        	else System.out.println("rotation NAN: from="+Arrays.toString(myMatrix.getColumn(3))+" to="+Arrays.toString(dest));
        }
        myMatrix.setColumn(3, dest);
        myMatrix.assignTo(myComponent);
      }
    }
  }

  transient boolean timerOnline;
  
  private void checkNeedTimer(ToolContext tc) {
    boolean needTimer = false;
    if (tc.getAxisState(forwardBackward) != null && !tc.getAxisState(forwardBackward).isReleased()) {
      needTimer = true;
    }
    if (tc.getAxisState(leftRight) != null && !tc.getAxisState(leftRight).isReleased()) {
          needTimer = true;
        }
    if (!pollingDevice && tc.getAxisState(horizontalRotation) != null && !tc.getAxisState(horizontalRotation).isReleased()) {
          needTimer = true;
        }
    if (tc.getAxisState(jump) != null
        && tc.getAxisState(jump).isPressed())
      needTimer = true;
    if (needTimer || !touchGround) {
      if (!timerOnline) {
        addCurrentSlot(timer);
        timerOnline = true;
      }
    } else {
      if (timerOnline) {
        removeCurrentSlot(timer);
        timerOnline = false;
      }
    }
  }

  public double getGain() {
    return gain;
  }

  public void setGain(double gain) {
    this.gain = gain;
  }

  public double getGravity() {
    return gravity;
  }

  public void setGravity(double gravity) {
    if (this.gravity == 0) {
      jumpSpeed = lastJumpSpeed;
      touchGround = false;
    }
    this.gravity = gravity;
    if (gravity==0) {
      touchGround=true;
      lastJumpSpeed=jumpSpeed;
      jumpSpeed=0;
    }
  }

  public double getJumpSpeed() {
    return jumpSpeed;
  }

  public void setJumpSpeed(double jumpSpeed) {
    this.jumpSpeed = jumpSpeed;
  }

  public boolean isPollingDevice() {
    return pollingDevice;
  }

  public void setPollingDevice(boolean pollingDevice) {
    this.pollingDevice = pollingDevice;
  }

  public double getRunFactor() {
    return runFactor;
  }

  public void setRunFactor(double runFactor) {
    this.runFactor = runFactor;
  }

  public boolean isCenter() {
    return center;
  }

  public void setCenter(boolean center) {
	  this.center = center;
  }
}