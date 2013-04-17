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
package com.ios;

import com.ios.listeners.SubPathListener;
import com.ios.triggers.SimpleTrigger;

public abstract class Observer extends IObject {
	
	private static final int PROPERTYLEN = "observed".length();
	
	public IObject observed;
	
	public Observer(IObject observed) {
		setContent("observed", observed);
		addTrigger(new SimpleTrigger<Observer>(new SubPathListener(getProperty("observed"))) {
			@Override protected void makeAction(Property changedPath) {
				if (getRoot().observed != null) {
					String subPath = changedPath.getPath().substring(PROPERTYLEN);
					getRoot().action(new Property(getRoot().observed, subPath));
				}
			}
		});
	}
	
	protected abstract void action(Property changedPath);

}
