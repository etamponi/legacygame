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


public class RangeCheck extends PropertyCheck<Number> {
	
	private Number lowerBound, upperBound;
	
	public enum Bound {
		LOWER, UPPER
	}
	
	public RangeCheck(String path, Number lowerBound, Number upperBound) {
		super(path);
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
	
	public RangeCheck(String path, Number bound, Bound type) {
		super(path);
		if (type == Bound.LOWER) {
			this.lowerBound = bound;
			this.upperBound = Double.POSITIVE_INFINITY;
		} else {
			this.upperBound = bound;
			this.lowerBound = Double.NEGATIVE_INFINITY;
		}
	}

	@Override
	protected String getError(Number value) {
		if (value.doubleValue() > upperBound.doubleValue())
			return "upper bound is " + upperBound + " (current value: " + value + ")";
		if (value.doubleValue() < lowerBound.doubleValue())
			return "lower bound is " + lowerBound + " (current value: " + value + ")";
		return null;
	}

}
