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


public class LengthCheck extends PropertyCheck<String> {
	
	private int minimumLength;
	
	public LengthCheck(String path, int minimumLength) {
		super(path);
		this.minimumLength = minimumLength;
	}

	@Override
	protected String getError(String value) {
		if (value.length() < minimumLength)
			return "must have at least " + minimumLength + " characters";
		else
			return null;
	}

}
