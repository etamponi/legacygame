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

import java.util.ArrayList;
import java.util.List;

public abstract class Trigger<T extends IObject> {
		
	private final List<String> boundPaths = new ArrayList<String>();
	
	private T root;

	public abstract void action(Property changedPath);
	
	public void setRoot(T root) {
		this.root = root;
	}
	
	protected T getRoot() {
		return root;
	}
	
	protected <TT extends T> TT getRoot(Class<TT> type) {
		return (TT)root;
	}
	
	public List<String> getBoundPaths() {
		return boundPaths;
	}

	public List<Property> getLocalBoundProperties(Property parentPath) {
		List<Property> ret = new ArrayList<>();
		for(String boundPath: boundPaths) {
			Property bound = getRoot().getProperty(boundPath);
			if (parentPath.isParent(bound))
				ret.add(new Property(parentPath.getContent(IObject.class), bound.getLastPart()));
		}
		return ret;
	}
	
}
