package com.storage.api.storage;


public class BoxRuntimeException extends RuntimeException {
	private final String messageCode;
	private final transient Object[] args;

	public String getMessageCode() {
		return this.messageCode;
	}

	public BoxRuntimeException(String messageCode) {
		super(messageCode);
		this.messageCode = messageCode;
		this.args = null;
	}

	public BoxRuntimeException(Throwable throwable) {
		super(throwable);
		this.messageCode = null;
		this.args = null;
	}

	public BoxRuntimeException(String messageCode, Throwable throwable) {
		super(messageCode, throwable);
		this.messageCode = messageCode;
		this.args = null;
	}

	public BoxRuntimeException(String messageCode, String taskMessage) {
		super(taskMessage);
		this.messageCode = messageCode;
		this.args = null;
	}

	public BoxRuntimeException(String messageCode, String taskMessage, Object... args) {
		super(taskMessage);
		this.messageCode = messageCode;
		this.args = args;
	}

	public BoxRuntimeException(String messageCode, Throwable throwable, Object... args) {
		super(String.format(messageCode, args), throwable);
		this.messageCode = messageCode;
		this.args = args;
	}

	public BoxRuntimeException(String messageCode, String taskErrorMessage, Throwable throwable, Object... args) {
		super(taskErrorMessage, throwable);
		this.messageCode = messageCode;
		this.args = args;
	}

	public BoxRuntimeException(String messageCode, Object... args) {
		super(String.format(messageCode, args));
		this.messageCode = messageCode;
		this.args = args;
	}

	public Object[] getMessageArguements() {
		return this.args;
	}
}
