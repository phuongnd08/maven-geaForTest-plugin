package org.seamoo.gaeForTest;

public class PrivateCtorClass {
	private String privateField;
	private String publicField;

	private PrivateCtorClass() {
		this.publicField = "0";
	}

	private PrivateCtorClass(String[] arguments) {
		this.publicField = "1";
	}

	public void setPublicField(String publicField) {
		this.publicField = publicField;
	}

	public String getPublicField() {
		return publicField;
	}

}