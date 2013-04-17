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


public abstract class ErrorCheck<T extends IObject> {
	
	private T root;
	
	public void setRoot(T root) {
		this.root = root;
	}
	
	public T getRoot() {
		return root;
	}
	
	public <TT extends T> TT getRoot(Class<TT> type) {
		return (TT)root;
	}
	
	public abstract String getError();

}
