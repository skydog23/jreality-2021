package de.jreality.shader;

import de.jreality.scene.data.AttributeCollection;

public interface HapticShader extends AttributeCollection {

	static final double STIFFNESS_DEFAULT = CommonAttributes.HAPTIC_STIFFNESS_DEFAULT;
	static final double DYNAMIC_FRICTION_DEFAULT = CommonAttributes.HAPTIC_DYNAMIC_FRICTION_DEFAULT;
	static final double STATIC_FRICTION_DEFAULT = CommonAttributes.HAPTIC_STATIC_FRICTION_DEFAULT;
	static final double DAMPING_DEFAULT = CommonAttributes.HAPTIC_DAMPING_DEFAULT;
	
	public double getStiffness();
	public void setStiffness(double stiffness);

	public double getDynamicFriction();
	public void setDynamicFriction(double dynamicFriction);

	public double getStaticFriction();
	public void setStaticFriction(double staticFriction);

	public double getDamping();
	public void setDamping(double damping);
	
}
