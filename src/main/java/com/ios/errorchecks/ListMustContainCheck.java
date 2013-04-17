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



import java.util.List;


public class ListMustContainCheck extends PropertyCheck<List> {
	
	private Object element;

	public ListMustContainCheck(String path, Object element) {
		super(path);
		this.element = element;
	}

	@Override
	protected String getError(List value) {
		if (!value.contains(element))
			return "list must contain " + element;
		else
			return null;
	}

}
