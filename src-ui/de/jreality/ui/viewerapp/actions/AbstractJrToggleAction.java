package de.jreality.ui.viewerapp.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToggleButton.ToggleButtonModel;

public abstract class AbstractJrToggleAction extends AbstractJrAction {

	ToggleButtonModel model=new ToggleButtonModel();
	
	public AbstractJrToggleAction(String name) {
		super(name);
		setSelected(false);
		model.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AbstractJrToggleAction.this.actionPerformed(e);
			}
		});
	}

	public void setSelected(boolean value) {
		model.setSelected(value);
	}
	  
	public boolean isSelected() {
	  return model.isSelected();
	}

	public JMenuItem createMenuItem() {
		JCheckBoxMenuItem ret = new JCheckBoxMenuItem();
		ret.setText((String) getValue(Action.NAME));
		ret.setIcon(getIcon());
		ret.setModel(model);
		return ret;
	}
	
	public AbstractButton createToolboxItem() {
		JToggleButton ret = new JToggleButton();
		if (getIcon() != null) {
			ret.setIcon(getIcon());
			ret.setToolTipText((String) getValue(Action.NAME));
		} else {
			ret.setText((String) getValue(Action.NAME));
		}
		ret.setModel(model);
		return ret;
	}
}
