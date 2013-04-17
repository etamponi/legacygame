package com.ios.errorchecks;

public class NotNullCheck extends PropertyCheck<Object> {

	public NotNullCheck(String path) {
		super(path);
	}

	@Override
	protected String getError(Object content) {
		if (content == null)
			return "cannot be null";
		else
			return null;
	}

}
