package com.ios.errorchecks;

import com.ios.ErrorCheck;
import com.ios.IObject;

public abstract class PropertyCheck<P> extends ErrorCheck<IObject> {
	
	private final String path;
	
	public PropertyCheck(String path) {
		this.path = path;
	}
	
	protected abstract String getError(P content);

	@Override
	public String getError() {
		P content = getRoot().getContent(path);
		if (content != null) {
			String error = getError(content);
			if (error != null)
				return path + ": " + getError(content);
		}
		return null;
	}

}
