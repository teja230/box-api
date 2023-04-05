package com.storage.api.utility;

import com.google.common.base.Strings;
import com.google.gson.*;
import com.storage.api.storage.BoxConstants;
import com.storage.api.storage.BoxSettings;
import com.storage.api.storage.BoxUtility;
import com.storage.api.storage.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.SecureRandom;
import java.util.*;

import static com.storage.api.storage.BoxConstants.*;
import static com.storage.api.storage.BoxConstants.HubConstants.*;
import static com.storage.api.storage.BoxConstants.LogCodes.BOX_1652;
import static com.storage.api.storage.BoxConstants.PropertyNames.ATTRIBUTES;
import static com.storage.api.storage.BoxConstants.PropertyNames.NAME;

public class StorageAPI {
	private static final Logger logger = LoggerFactory.getLogger(StorageAPI.class);
	private static BoxSettings boxSettings;

	static {
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();

		InputStream inputStream = StorageAPI.class.getResourceAsStream("boxsettings.json");
		try {
			String settingInput = IOUtils.toString(inputStream);
			boxSettings = gson.fromJson(settingInput, BoxSettings.class);
		} catch (IOException e) {
			logger.error("Exception Initializing Box Settings", e);
		}
	}

	private static String constructFilesUrl(String folderId) {
		StringBuilder requestURL = new StringBuilder();

		requestURL.append(boxSettings.getBaseurl());
		requestURL.append(BACKSLASH);
		requestURL.append(FOLDERS);
		requestURL.append(BACKSLASH);

		if (!Strings.isNullOrEmpty(folderId)) {
			requestURL.append(folderId);
			requestURL.append(BACKSLASH);
		}

		requestURL.append(BoxConstants.ITEMS);

		return requestURL.toString();
	}

	private static String constructDownloadUrl(String fileId) {
		StringBuilder requestURL = new StringBuilder();

		requestURL.append(boxSettings.getBaseurl());
		requestURL.append(BACKSLASH);
		requestURL.append(FILES);
		requestURL.append(BACKSLASH);

		if (!Strings.isNullOrEmpty(fileId)) {
			requestURL.append(fileId);
			requestURL.append(BACKSLASH);
		}

		requestURL.append(BoxConstants.CONTENT);

		return requestURL.toString();
	}

	private static String constructShareUrl(String fileId) {
		StringBuilder requestURL = new StringBuilder();

		requestURL.append(boxSettings.getBaseurl());
		requestURL.append(BACKSLASH);
		requestURL.append(FILES);
		requestURL.append(BACKSLASH);

		if (!Strings.isNullOrEmpty(fileId)) {
			requestURL.append(fileId);
		}

		return requestURL.toString();
	}

	private static String constructUploadUrl(String hubId) {
		StringBuilder requestURL = new StringBuilder();

		requestURL.append(boxSettings.getUploadurl());
		requestURL.append(BACKSLASH);
		requestURL.append(FILES);
		requestURL.append(BACKSLASH);

		if (!Strings.isNullOrEmpty(hubId)) {
			requestURL.append(hubId);
			requestURL.append(BACKSLASH);
		}

		requestURL.append(BoxConstants.CONTENT);

		return requestURL.toString();
	}

	public static String download(Request req, Response res) throws IOException {
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		StringBuilder apiError = new StringBuilder();

		String fileId = req.params(":id");
		String requestUrl = constructDownloadUrl(fileId);
		String filesResponse = BoxUtility.sendGetRequest(requestUrl, GETREQUESTMETHOD, getAccessToken(), apiError, "api");
		JsonObject fileResponseObject = gson.fromJson(filesResponse, JsonObject.class);

		return fileResponseObject.toString();
	}

	public static String share(Request req, Response res) throws IOException {
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		StringBuilder apiError = new StringBuilder();

		String fileId = req.params(":id");
		return getSharedUrl(apiError, fileId, getAccessToken());
	}

	private static String getSharedUrl(StringBuilder apiError, String fileId, String accessToken) throws IOException {
		String requestUrl = constructShareUrl(fileId);

		JsonObject requestQuery = new JsonObject();
		JsonObject sharedLink = new JsonObject();
		sharedLink.addProperty("access", "open");
		sharedLink.addProperty("password", "password");
		JsonObject permissions = new JsonObject();
		permissions.addProperty("can_download", false);
		sharedLink.add("permissions", permissions);
		requestQuery.add("shared_link", sharedLink);

		JsonObject shareResponse = BoxUtility.sendShareRequest(requestUrl, requestQuery, PUTREQUESTMETHOD, accessToken, apiError, "api");
		return JsonPath.getValue(JsonPath.findObject(shareResponse, "shared_link"), "url");
	}

	public static void upload() throws IOException {
		String requestURL = constructUploadUrl(null);

		StringBuilder apiError = new StringBuilder();

		File directory = new File(boxSettings.getFilePath());
		String fileNameRegex = ".*";

		List<String> assetURLList = new ArrayList<>();
		search(directory, fileNameRegex, assetURLList);

		for(String assetURL : assetURLList) {

			byte[] file = BoxUtility.downloadBlob(assetURL, Integer.parseInt(boxSettings.getMaxFileSize()));

			JsonArray requestQuery = new JsonArray();

			JsonObject formData = new JsonObject();

			JsonObject parent = new JsonObject();
			JsonPath.setValue(parent, ID, boxSettings.getParentFolder());

			String fileId = UUID.randomUUID().toString();

			JsonObject attributeObject = new JsonObject();

			StringBuilder fileName = new StringBuilder();

			fileName.append(fileId);

			String[] extension = assetURL.split("\\.");

			String fileType = extension[extension.length - 1];

			if (!Strings.isNullOrEmpty(fileType)) {
				fileName.append(".");
				fileName.append(fileType);
			}

			JsonPath.setValue(attributeObject, NAME, fileName.toString());

			attributeObject.add(BoxConstants.PARENT, parent);

			JsonPath.setValue(formData, NAME, ATTRIBUTES);
			JsonPath.setValue(formData, VALUE, attributeObject);
			JsonPath.setValue(formData, FILE_ID, BoxConstants.FILE);
			JsonPath.setValue(formData, FILE_NAME, fileId);

			requestQuery.add(formData);

			if((fileType.equals("jpg") || fileType.equals("png") || fileType.equals("jpeg"))) {
				BoxUtility.sendUploadRequest(requestURL, requestQuery, file, POSTREQUESTMETHOD, getAccessToken(), apiError, "api");
			}
		}
	}

	public static void search(File file, String fileNameRegex, List<String> assetURLList) {
		if (file.isDirectory()) {
			for (File f : Objects.requireNonNull(file.listFiles())) {
				search(f, fileNameRegex, assetURLList);
			}
		} else {
			if (file.getName().matches(fileNameRegex)) {
				try {
					assetURLList.add(file.toURI().toURL().toString());
				} catch (MalformedURLException ex) {
					logger.error(BOX_SERVICE, BOX_1652, boxSettings.getAuthurl(), ex);
				}
			}
		}
	}


	public static String generateRandomString(int length) {
		final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		final SecureRandom random = new SecureRandom();
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			int randomIndex = random.nextInt(ALPHABET.length());
			char randomChar = ALPHABET.charAt(randomIndex);
			sb.append(randomChar);
		}
		return sb.toString();
	}


	protected static String getAccessToken() {
		String token = null;
		try {
			JsonObject reqBody = new JsonObject();
			reqBody.addProperty(GRANT_TYPE_PROPERTY, BoxConstants.GRANT_TYPE_VALUE);
			reqBody.addProperty(CLIENT_ID_PROPERTY, boxSettings.getClientId());
			reqBody.addProperty(CLIENT_SECRET_PROPERTY, boxSettings.getSecret());
			reqBody.addProperty(ASSERTION_PROPERTY, BoxUtility.getAssertion(boxSettings, "api"));

			token = BoxUtility.getAccessToken(boxSettings.getAuthurl(), reqBody, "api");
		} catch (Exception ex) {
			logger.error(BOX_SERVICE, BOX_1652, boxSettings.getAuthurl(), ex);
		}
		return token;
	}
}
