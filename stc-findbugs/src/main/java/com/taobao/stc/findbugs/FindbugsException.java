package com.taobao.stc.findbugs;

public class FindbugsException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public FindbugsException() {
	}

	public FindbugsException(String message) {
		super(message);
	}

	public FindbugsException(Throwable cause) {
		super(cause);
	}

	public FindbugsException(String message, Throwable cause) {
		super(message, cause);
	}

}
