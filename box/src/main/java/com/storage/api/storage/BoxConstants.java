package com.storage.api.storage;

public final class BoxConstants {
	public static final String GRANT_TYPE_VALUE = "urn:ietf:params:oauth:grant-type:jwt-bearer";
	public static final String PUBLIC_KEY_ID = "publicKeyID";
	public static final String PRIVATE_KEY = "privateKey";
	public static final String PASSPHRASE_KEY = "passphrase";
	public static final String ENTERPRISE_ID = "enterpriseID";
	public static final String FILES = "files";
	public static final String FOLDERS = "folders";
	public static final String CONTENT = "content";
	public static final String ITEMS = "items";
	public static final String PARENT_FOLDER = "parentFolder";
	public static final String MAX_FILE_SIZE = "maxFileSize";
	public static final String CLAIM_NAME = "box_sub_type";
	public static final String CLAIM_VALUE = "enterprise";
	public static final float JWT_EXPIRATION = 0.75f;
	public static final String JWT_TYP_HEADER = "typ";
	public static final String JWT_VALUE = "JWT";
	public static final String KEY_ID_HEADER = "kid";
	public static final String ENTRIES = "entries";
	public static final String ITEM_STATUS = "item_status";
	public static final String FILE_UPLOADED = "FILE.UPLOADED";
	public static final String TRIGGER = "trigger";
	public static final String SPLIT = "#@#";
	public static final String PARENT = "parent";
	public static final String FILE_TYPE_EXTENSION = "filetypeextension";
	public static final String FILE = "file";
	public static final String BACKSLASH = "/";

	public static final String CONTENT_TYPE = "Content-Type";
	public static final int MAX_RETRY_SERVICE_REQUEST = 3;
	public static final int REST_CLIENT_TIMEOUT_MS = 300000;
	public static final String FILE_ID = "fileId";
	public static final String FILE_TYPE = "fileType";
	public static final String FILE_NAME = "fileName";
	public static final String BOX_SERVICE = "BOXSERVICE";
	public static final String APPLICATION_JSON = "application/json";
	public static final String MULTIPART_FORM_DATA = "multipart/form-data";
	public static final String MULTIPART_FORM_DATA_BOUNDARY = "multipart/form-data; boundary=%s";
	public static final String APPLICATION_JSON_CHARSET = "application/json;charset=utf-8";
	public static final String BOUNDARY_HEADER = ";boundary=";
	public static final String POSTREQUESTMETHOD = "POST";
	public static final String GETREQUESTMETHOD = "GET";
	public static final String PUTREQUESTMETHOD = "PUT";
	public static final String DELETEREQUESTMETHOD = "DELETE";
	public static final String X_AUTH_CLIENT = "X-Auth-Client";
	public static final String X_AUTH_TOKEN = "X-AUTH-TOKEN";
	public static final String VALUES = "values";
	public static final String VALUE = "value";
	public static final String AUTHORIZATION = "Authorization";
	public static final String BEARER = "Bearer";
	public static final String SPACE = " ";
	public static final String ID = "id";
	public static final String ERROR = "error";

	public static final class LogCodes {
		public static final String BOX_1652 = "BOX1652";
		public static final String BOX_1654 = "BOX_1654";

		private LogCodes() {
			throw new IllegalStateException("LogCodes should be used as a utility class");
		}
	}

	public static final class PropertyNames {
		public static final String ID = "id";
		public static final String TYPE = "type";
		public static final String ATTRIBUTES = "attributes";
		public static final String NAME = "name";
		public static final String ENTITIES = "entities";
		public static final String INLINE = "inline";
		public static final String IMAGE_TYPE = "imagetype";

		private PropertyNames() {
			throw new IllegalStateException("PropertyNames should be used as a utility class");
		}
	}

	public static final class HttpService {
		public static final String GET = "get";
		public static final String REQUESTID = "reqid";
		public static final String BOUNDARY = "*****";
		public static final String CRLF = "\r\n";
		public static final String TWO_HYPHENS = "--";
		public static final String CONTENT_DISPOSITION_FORM_DATA = "Content-Disposition: form-data; name=\"";
		public static final String PLAIN_TEXT_CONTENT_TYPE = "Content-Type: text/plain; charset=UTF-8";

		private HttpService() {
			throw new IllegalStateException("HttpService should be used as a utility class");
		}
	}

	public static final class HubConstants {
		public static final String ACCESS_TOKEN = "access_token";
		public static final String IGNORE = "ignore";
		public static final String GRANT_TYPE_PROPERTY = "grant_type";
		public static final String CLIENT_ID_PROPERTY = "client_id";
		public static final String CLIENT_SECRET_PROPERTY = "client_secret";
		public static final String ASSERTION_PROPERTY = "assertion";

		private HubConstants() {
			throw new IllegalStateException("HubConstants should be used as a utility class");
		}
	}

	private BoxConstants() {
		throw new IllegalStateException("BoxConstants should be used as a utility class");
	}
}
