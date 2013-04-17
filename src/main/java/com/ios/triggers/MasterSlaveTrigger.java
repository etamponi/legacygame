/*******************************************************************************
 * Copyright (c) 2012 Emanuele Tamponi.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Emanuele Tamponi - initial API and implementation
 ******************************************************************************/
package com.ios.triggers;

import java.util.ArrayList;
import java.util.List;

import com.ios.IObject;
import com.ios.Listener;
import com.ios.Property;
import com.ios.Trigger;
import com.ios.listeners.PrefixListener;

public class MasterSlaveTrigger<O> extends Trigger {
	
	private final Property master;
	
	private final List<Listener> listeners = new ArrayList<>();

	public MasterSlaveTrigger(IObject root, String masterPath, String... slavePaths) {
		assert(slavePaths.length > 0);
		
		master = new Property(root, masterPath);
		
		listeners.add(new PrefixListener(master));
//		listeners.add(new SubPathListener(master));
		
		for(String slavePath: slavePaths) {
			if (slavePath.split(".").length >= IObject.MAXIMUM_CHANGE_PROPAGATION)
				System.err.println("Warning: change propagation may be incomplete");
			
			getBoundPaths().add(slavePath);
			
			if (!slavePath.contains("."))
				continue;
			String prefix = slavePath.substring(0, slavePath.lastIndexOf('.'));
			listeners.add(new PrefixListener(new Property(root, prefix)));
		}
	}

	@Override
	public void action(Property changedPath) {
		boolean stop = true;
		for(Listener l: listeners) {
			if (l.isListeningOn(changedPath)) {
				stop = false;
				break;
			}
		}
		if (stop)
			return;
		
		O masterContent = (O)master.getContent();
		if (changedPath.isPrefix(master, false) || master.isPrefix(changedPath, false)) {
			for(String slave: (List<String>)getBoundPaths())
				updateSlave(new Property(getRoot(), slave), masterContent);
		} else {
			for(String slave: (List<String>)getBoundPaths()) {
				Property s = new Property(getRoot(), slave);
				if (changedPath.isPrefix(s, false))
					updateSlave(s, masterContent);
			}
		}
	}
	
	protected void updateSlave(Property slave, O masterContent) {
		slave.setContent(masterContent);
	}

}
