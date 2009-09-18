package de.jreality.plugin.basic;

import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.aggregators.ToolBarAggregator;
import de.jtem.jrworkspace.plugin.flavor.PerspectiveFlavor;

public class ViewToolBar extends ToolBarAggregator {

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("View Tool Bar", "Stefan Sechelmann");
	}

	public Class<? extends PerspectiveFlavor> getPerspective() {
		return View.class;
	}

	@Override
	public double getToolBarPriority() {
		return -10.0;
	}
}
