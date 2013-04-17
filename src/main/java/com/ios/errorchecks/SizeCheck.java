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



import java.util.Collection;


public class SizeCheck extends PropertyCheck<Collection> {
	
	private int minimumSize, maximumSize;

	public SizeCheck(String path, int minimumSize) {
		super(path);
		this.minimumSize = minimumSize;
		this.maximumSize = Integer.MAX_VALUE;
	}
	
	public SizeCheck(String path, int minimumSize, int maximumSize) {
		super(path);
		this.minimumSize = minimumSize;
		this.maximumSize = maximumSize;
	}

	@Override
	protected String getError(Collection value) {
		if (value.size() < minimumSize)
			return "should have at least " + minimumSize + " elements";
		else if (value.size() > maximumSize)
			return "should have no more than " + maximumSize + " elements";
		else
			return null;
	}

}
