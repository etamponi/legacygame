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
package com.ios.listeners;

import com.ios.Property;
import com.ios.Listener;

public class PrefixListener extends Listener {
	
	private final Property completePath;
	
	private final boolean parentOnly;
	
	public PrefixListener(Property completePath) {
		this(completePath, false);
	}
	
	public PrefixListener(Property completePath, boolean parentOnly) {
		this.completePath = completePath;
		this.parentOnly = parentOnly;
	}

	@Override
	public boolean isListeningOn(Property path) {
		return path.isPrefix(completePath, parentOnly);
	}

}
