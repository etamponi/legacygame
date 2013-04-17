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

import com.ios.IObject;
import com.ios.Property;
import com.ios.Trigger;

public class BoundProperties<T extends IObject> extends Trigger<T> {
	
	public BoundProperties(String... paths) {
		for(String path: paths)
			getBoundPaths().add(path);
	}

	@Override
	public void action(Property changedPath) {
		// Does nothing
	}

}
