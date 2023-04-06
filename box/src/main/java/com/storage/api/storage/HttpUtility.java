package com.storage.api.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtility {
	private static Logger logger = LoggerFactory.getLogger(HttpUtility.class);

	private HttpUtility() {
		throw new IllegalStateException("HttpUtility should be used as a utility class");
	}

	/**
	 * Build HTTP URL Connection with basic information
	 */
	public static HttpURLConnection buildHttpURLConnection(String url, String requestMethod, String contentType, boolean doOutput, String tenantId) {
		HttpURLConnection httpURLConnection = null;
		int connectionTimeout = BoxConstants.REST_CLIENT_TIMEOUT_MS;
		try {
			httpURLConnection = createHttpUrlConnection(url);
			httpURLConnection.setDoOutput(doOutput);
			httpURLConnection.setRequestMethod(requestMethod);
			httpURLConnection.setRequestProperty(BoxConstants.CONTENT_TYPE, contentType);
			httpURLConnection.setConnectTimeout(connectionTimeout);
			httpURLConnection.setReadTimeout(connectionTimeout);
		} catch (Exception exception) {
			logger.error(tenantId, BoxConstants.BOX_SERVICE, BoxConstants.LogCodes.BOX_1652, exception);
		}
		return httpURLConnection;
	}


	/**
	 * Send HTTP POST request (supports, application/json and form fields with file)
	 */
	public static JsonElement sendHttpRequest(String requestQuery, boolean isFormData, byte[] file, HttpURLConnection httpURLConnection, String boundary, String tenantId) throws IOException {
		JsonElement jsonResponse = new JsonObject();
		int connectionTimeout = BoxConstants.REST_CLIENT_TIMEOUT_MS;
		// Implement retry for request timeout.
		int count = 0;
		if (httpURLConnection != null) {
			while (count < BoxConstants.MAX_RETRY_SERVICE_REQUEST) {
				count++;
				try (OutputStream outputStream = httpURLConnection.getOutputStream()) {
					if (requestQuery != null && !isFormData) {
						outputStream.write(requestQuery.getBytes());
					} else {
						sendField(requestQuery, outputStream, boundary);
						if (file != null) {
							sendFile(requestQuery, file, outputStream);
						}
						finish(outputStream, boundary);
					}
					return processHttpResponse(httpURLConnection, tenantId);
				} catch (SocketTimeoutException ex) {
					if (count >= BoxConstants.MAX_RETRY_SERVICE_REQUEST) {
						throw new BoxRuntimeException(BoxConstants.LogCodes.BOX_1654, ex, count, connectionTimeout, ex.getMessage());
					} else {
						logger.warn(tenantId, BoxConstants.BOX_SERVICE, BoxConstants.LogCodes.BOX_1652, count, BoxConstants.MAX_RETRY_SERVICE_REQUEST, connectionTimeout);
					}
				}
			}
		} else {
			logger.warn(tenantId, BoxConstants.BOX_SERVICE, BoxConstants.LogCodes.BOX_1652, "HTTPURLConnection not a valid value in sendHttpRequest()");
		}
		return jsonResponse;
	}

	public static JsonElement sendHttpRequest(String requestQuery, HttpURLConnection httpURLConnection, String appId) throws IOException {
		return sendHttpRequest(requestQuery, false, null, httpURLConnection, null, appId);
	}

	private static void sendField(String requestQuery, OutputStream outputStream, String boundary) throws IOException {
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();

		JsonArray requestArray = gson.fromJson(requestQuery, JsonArray.class);

		for (JsonElement requestElement : requestArray) {
			String name = JsonPath.getValue(requestElement, BoxConstants.PropertyNames.NAME);
			String value = null;
			if (!Strings.isNullOrEmpty(boundary)) {
				value = JsonPath.getValue(requestElement, BoxConstants.VALUE);
			} else {
				value = JsonPath.findObject(requestElement, BoxConstants.VALUE).toString();
			}

			outputStream.write((String.format("%s%s%s", BoxConstants.HttpService.TWO_HYPHENS, Strings.isNullOrEmpty(boundary) ? BoxConstants.HttpService.BOUNDARY : boundary, BoxConstants.HttpService.CRLF)).getBytes());
			outputStream.write((String.format("%s%s\"%s", BoxConstants.HttpService.CONTENT_DISPOSITION_FORM_DATA, name, BoxConstants.HttpService.CRLF)).getBytes());
			outputStream.write((String.format("%s%s", BoxConstants.HttpService.PLAIN_TEXT_CONTENT_TYPE, BoxConstants.HttpService.CRLF)).getBytes());
			outputStream.write(BoxConstants.HttpService.CRLF.getBytes());
			outputStream.write((String.format("%s%s", value, BoxConstants.HttpService.CRLF)).getBytes());
			outputStream.flush();
		}
	}

	private static void sendFile(String requestQuery, byte[] file, OutputStream outputStream) throws IOException {
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();

		JsonArray requestArray = gson.fromJson(requestQuery, JsonArray.class);

		for (JsonElement requestElement : requestArray) {
			String fileID = JsonPath.getValue(requestElement, BoxConstants.FILE_ID);
			String fileName = JsonPath.getValue(requestElement, BoxConstants.FILE_NAME);

			outputStream.write((String.format("%s%s%s", BoxConstants.HttpService.TWO_HYPHENS, BoxConstants.HttpService.BOUNDARY, BoxConstants.HttpService.CRLF)).getBytes());
			outputStream.write((String.format("%s%s\";filename=\"%s\"%s", BoxConstants.HttpService.CONTENT_DISPOSITION_FORM_DATA, fileID, fileName, BoxConstants.HttpService.CRLF)).getBytes());
			outputStream.write(BoxConstants.HttpService.CRLF.getBytes());
			outputStream.write(file);
			outputStream.write(BoxConstants.HttpService.CRLF.getBytes());
		}
	}

	private static void finish(OutputStream outputStream, String boundary) throws IOException {
		outputStream.write(String.format("%s%s%s%s", BoxConstants.HttpService.TWO_HYPHENS, Strings.isNullOrEmpty(boundary) ? BoxConstants.HttpService.BOUNDARY : boundary, BoxConstants.HttpService.TWO_HYPHENS, BoxConstants.HttpService.CRLF).getBytes());
		outputStream.flush();
	}

	public static void addFormField(JsonArray requestArray, String name, String value) {
		JsonObject formData = new JsonObject();

		formData.addProperty(BoxConstants.PropertyNames.NAME, name);
		formData.addProperty(BoxConstants.VALUE, value);

		requestArray.add(formData);
	}

	/**
	 * Send HTTP GET request
	 */
	public static InputStream sendHttpDownloadRequest(HttpURLConnection httpURLConnection, String appId) throws IOException {
		JsonElement jsonResponse = new JsonObject();
		int connectionTimeout = BoxConstants.REST_CLIENT_TIMEOUT_MS;
		// Implement retry for request timeout.
		int count = 0;
		while (count < BoxConstants.MAX_RETRY_SERVICE_REQUEST) {
			count++;
			try (InputStream ignored = httpURLConnection.getInputStream()) {
				return processHttpDownloadResponse(httpURLConnection, appId);
			} catch (SocketTimeoutException ex) {
				if (count >= BoxConstants.MAX_RETRY_SERVICE_REQUEST) {
					throw new BoxRuntimeException(BoxConstants.LogCodes.BOX_1654, ex, count, connectionTimeout, ex.getMessage());
				} else {
					logger.warn(appId, BoxConstants.BOX_SERVICE, BoxConstants.LogCodes.BOX_1652, count, BoxConstants.MAX_RETRY_SERVICE_REQUEST, connectionTimeout);
				}
			}
		}
		return null;
	}

	/**
	 * Process HTTP Response
	 * @return
	 */
	private static InputStream processHttpDownloadResponse(HttpURLConnection httpURLConnection, String tenantId) throws IOException {
		JsonElement jsonResponse = new JsonObject();
		int httpResponseCode = httpURLConnection.getResponseCode();

		if (httpResponseCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
			String errorStream = StringUtils.toString(httpURLConnection.getErrorStream());
			logger.info(String.format("Server returned error response: %s with code %s: ", errorStream, httpResponseCode));
			throw new BoxRuntimeException(BoxConstants.LogCodes.BOX_1654, httpResponseCode, errorStream);
		}
		return httpURLConnection.getInputStream();
	}

	/**
	 * Send HTTP GET request
	 */
	public static JsonElement sendHttpRequest(HttpURLConnection httpURLConnection, String appId) throws IOException {
		JsonElement jsonResponse = new JsonObject();
		int connectionTimeout = BoxConstants.REST_CLIENT_TIMEOUT_MS;
		// Implement retry for request timeout.
		int count = 0;
		while (count < BoxConstants.MAX_RETRY_SERVICE_REQUEST) {
			count++;
			try (InputStream ignored = httpURLConnection.getInputStream()) {
				return processHttpResponse(httpURLConnection, appId);
			} catch (SocketTimeoutException ex) {
				if (count >= BoxConstants.MAX_RETRY_SERVICE_REQUEST) {
					throw new BoxRuntimeException(BoxConstants.LogCodes.BOX_1654, ex, count, connectionTimeout, ex.getMessage());
				} else {
					logger.warn(appId, BoxConstants.BOX_SERVICE, BoxConstants.LogCodes.BOX_1652, count, BoxConstants.MAX_RETRY_SERVICE_REQUEST, connectionTimeout);
				}
			}
		}
		return jsonResponse;
	}

	/**
	 * Process HTTP Response
	 */
	private static JsonElement processHttpResponse(HttpURLConnection httpURLConnection, String tenantId) throws IOException {
		JsonElement jsonResponse = new JsonObject();
		int httpResponseCode = httpURLConnection.getResponseCode();

		if (httpResponseCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
			String errorStream = StringUtils.toString(httpURLConnection.getErrorStream());
			logger.info(String.format("Server returned error response: %s with code %s: ", errorStream, httpResponseCode));
			throw new BoxRuntimeException(BoxConstants.LogCodes.BOX_1654, httpResponseCode, errorStream);
		}
		String response = StringUtils.toString(httpURLConnection.getInputStream());
		if (!Strings.isNullOrEmpty(response)) {
			GsonBuilder gsonBuilder = new GsonBuilder();
			Gson gson = gsonBuilder.create();

			return gson.fromJson(response, JsonElement.class);
		}
		return jsonResponse;
	}

	public static void extractHttpErrors(StringBuilder apiError, BoxRuntimeException ex) throws IOException {
		if (ex.getMessageArguements() != null && ex.getMessageArguements().length > 1) {
			String errorResponse = ex.getMessageArguements()[1].toString();
			if (!Strings.isNullOrEmpty(errorResponse)) {
				JsonReader jsonErrorReader = new JsonReader(new StringReader(errorResponse));
				jsonErrorReader.setLenient(true);
				while (jsonErrorReader.hasNext()) {
					JsonToken nextToken = jsonErrorReader.peek();
					if (JsonToken.STRING.equals(nextToken)) {
						apiError.append(jsonErrorReader.nextString()).append(" ");
					} else if (JsonToken.BEGIN_OBJECT.equals(nextToken) || JsonToken.BEGIN_ARRAY.equals(nextToken)) {
						processErrorJson(apiError, errorResponse);
						return;
					} else if (JsonToken.END_DOCUMENT.equals(nextToken)) {
						break;
					}
				}
			}
		}
	}

	private static void processErrorJson(StringBuilder apiError, String errorResponse) {
		try {
			GsonBuilder gsonBuilder = new GsonBuilder();
			Gson gson = gsonBuilder.create();

			JsonElement jsonElement = gson.fromJson(errorResponse, JsonElement.class);
			JsonObject jsonObject;
			if (jsonElement != null) {
				if (jsonElement.isJsonArray()) {
					jsonObject = jsonElement.getAsJsonArray().get(0).getAsJsonObject();
					Map<String, Object> errorMap = gson.fromJson(jsonObject.toString(), HashMap.class);
					errorMap.forEach((key, value) -> apiError.append(key).append(" - ").append(value).append("  "));
				} else {
					jsonObject = jsonElement.getAsJsonObject();
					Map<String, Object> errorMap = gson.fromJson(jsonObject.toString(), HashMap.class);
					errorMap.forEach((key, value) -> apiError.append(key).append(" - ").append(value).append("  "));
				}
			}
		} catch (JsonSyntaxException ex) {
			apiError.append(errorResponse);
		}
	}

	private static HttpURLConnection createHttpUrlConnection(String url) throws IOException {
		URL requestUrl = new URL(url);
		return (HttpURLConnection) requestUrl.openConnection();
	}
}