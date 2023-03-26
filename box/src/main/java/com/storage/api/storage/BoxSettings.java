package com.storage.api.storage;

public class BoxSettings {

	protected String baseurl;
	protected String uploadurl;
	protected String clientId;
	protected String secret;
	protected String authurl;
	String publicKeyID;
	String privateKey;
	String passphrase;
	String enterpriseID;
	String parentFolder = "0";
	String maxFileSize = "100000";

	public String getBaseurl() {
		return baseurl;
	}

	public void setBaseurl(String baseurl) {
		this.baseurl = baseurl;
	}

	public String getUploadurl() {
		return uploadurl;
	}

	public void setUploadurl(String uploadurl) {
		this.uploadurl = uploadurl;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getPublicKeyID() {
		return publicKeyID;
	}

	public void setPublicKeyID(String publicKeyID) {
		this.publicKeyID = publicKeyID;
	}

	public String getAuthurl() {
		return authurl;
	}

	public void setAuthurl(String authUrl) {
		this.authurl = authUrl;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getPassphrase() {
		return passphrase;
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}

	public String getEnterpriseID() {
		return enterpriseID;
	}

	public void setEnterpriseID(String enterpriseID) {
		this.enterpriseID = enterpriseID;
	}

	public String getParentFolder() {
		return parentFolder;
	}

	public void setParentFolder(String parentFolder) {
		this.parentFolder = parentFolder;
	}

	public String getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(String maxFileSize) {
		this.maxFileSize = maxFileSize;
	}
}