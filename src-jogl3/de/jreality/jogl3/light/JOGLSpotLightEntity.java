package de.jreality.jogl3.light;

import de.jreality.scene.SpotLight;

public class JOGLSpotLightEntity extends JOGLPointLightEntity {

	public double coneAngle, coneAngleDelta, distribution;
	
	public JOGLSpotLightEntity(SpotLight node) {
		super(node);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void updateData() {
		// TODO Auto-generated method stub
		if (!dataUpToDate) {
			
			super.updateData();
			
			SpotLight l = (SpotLight) getNode();
			coneAngle = l.getConeAngle();
			coneAngleDelta = l.getConeDeltaAngle();
			distribution = l.getDistribution();
			
			dataUpToDate = true;
		}
	}
}
