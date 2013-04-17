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
package com.ios.errorchecks;

import com.ios.Compatible;
import com.ios.ErrorCheck;
import com.ios.IObject;

public class CompatibilityCheck extends ErrorCheck<IObject> {
	
	private String compatibilityPath;
	
	public CompatibilityCheck(String compatibilityPath) {
		this.compatibilityPath = compatibilityPath;
	}

	@Override
	public String getError() {
		Compatible o = (Compatible)getRoot();
		Object content = getRoot().getContent(compatibilityPath);
		return o.compatibilityError(content);
	}

}
