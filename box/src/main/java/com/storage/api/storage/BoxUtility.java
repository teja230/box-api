package com.storage.api.storage;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PrivateKey;
import java.security.Security;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoxUtility {
	private static final Logger logger = LoggerFactory.getLogger(BoxUtility.class);

	private BoxUtility() {
		throw new IllegalStateException("BoxUtility should be used as a utility class");
	}

	public static String getAccessToken(String url, JsonObject requestBody, String appId) throws IOException {
		String accessToken = null;

		HttpURLConnection httpURLConnection = HttpUtility.buildHttpURLConnection(url, BoxConstants.POSTREQUESTMETHOD, BoxConstants.APPLICATION_JSON, true, appId);
		httpURLConnection.setDoInput(true);
		JsonObject jsonResponse = HttpUtility.sendHttpRequest(requestBody.toString(), httpURLConnection, appId).getAsJsonObject();

		if (jsonResponse.size() > 0 && jsonResponse.has(BoxConstants.HubConstants.ACCESS_TOKEN)) {
			accessToken = JsonPath.getValue(jsonResponse, BoxConstants.HubConstants.ACCESS_TOKEN);
		}

		return accessToken;
	}

	public static InputStream sendDownloadRequest(String url, String requestMethod, String accessToken, StringBuilder apiError, String appId) throws IOException {
		JsonObject jsonResponse = null;
		try {
			StringBuilder contentType = new StringBuilder();
			contentType.append(BoxConstants.APPLICATION_JSON);

			HttpURLConnection httpURLConnection = HttpUtility.buildHttpURLConnection(url, requestMethod, contentType.toString(), true, appId);
			httpURLConnection.setRequestProperty(BoxConstants.AUTHORIZATION, BoxConstants.BEARER.concat(BoxConstants.SPACE).concat(accessToken));

			return HttpUtility.sendHttpDownloadRequest(httpURLConnection, "api");
		} catch (BoxRuntimeException ex) {
			HttpUtility.extractHttpErrors(apiError, ex);
			logger.error(appId, BoxConstants.BOX_SERVICE, BoxConstants.LogCodes.BOX_1652, url, ex);
		} catch (Exception ex) {
			logger.error(appId, BoxConstants.BOX_SERVICE, BoxConstants.LogCodes.BOX_1652, url, ex);
			throw new IOException(String.format("Box connector failed to push the message to url [%s]", url));
		}

		logger.info("Box Responded");
		return null;
	}

	public static String sendGetRequest(String url, String requestMethod, String accessToken, StringBuilder apiError, String appId) throws IOException {
		JsonObject jsonResponse = null;
		try {
			StringBuilder contentType = new StringBuilder();
			contentType.append(BoxConstants.APPLICATION_JSON);

			HttpURLConnection httpURLConnection = HttpUtility.buildHttpURLConnection(url, requestMethod, contentType.toString(), true, appId);
			httpURLConnection.setRequestProperty(BoxConstants.AUTHORIZATION, BoxConstants.BEARER.concat(BoxConstants.SPACE).concat(accessToken));

			jsonResponse = HttpUtility.sendHttpRequest(httpURLConnection, "api").getAsJsonObject();
		} catch (BoxRuntimeException ex) {
			HttpUtility.extractHttpErrors(apiError, ex);
			logger.error(appId, BoxConstants.BOX_SERVICE, BoxConstants.LogCodes.BOX_1652, url, ex);
		} catch (Exception ex) {
			logger.error(appId, BoxConstants.BOX_SERVICE, BoxConstants.LogCodes.BOX_1652, url, ex);
			throw new IOException(String.format("Box connector failed to push the message to url [%s]", url));
		}

		logger.info(appId, BoxConstants.BOX_SERVICE, BoxConstants.LogCodes.BOX_1652, "Box Responded");
		return jsonResponse.toString();
	}

	public static JsonObject sendPostRequest(String url, JsonElement requestBody, String requestMethod, String accessToken, StringBuilder apiError, String appId) throws IOException {
		logger.info(String.format("Payload for sendHubRequest: [%s] ", requestBody.toString()));
		JsonObject jsonResponse = null;
		try {
			StringBuilder contentType = new StringBuilder();
			contentType.append(BoxConstants.APPLICATION_JSON);

			HttpURLConnection httpURLConnection = HttpUtility.buildHttpURLConnection(url, requestMethod, contentType.toString(), true, appId);
			httpURLConnection.setRequestProperty(BoxConstants.AUTHORIZATION, BoxConstants.BEARER.concat(BoxConstants.SPACE).concat(accessToken));

			jsonResponse = HttpUtility.sendHttpRequest(requestBody.toString(), false, null, httpURLConnection, null, appId).getAsJsonObject();

			if (!jsonResponse.isJsonNull() && jsonResponse.size() > 0) {
				logger.info("Response for BoxConstants req: " + jsonResponse.toString());
			}
		} catch (BoxRuntimeException ex) {
			HttpUtility.extractHttpErrors(apiError, ex);
			logger.error(ex.getMessage());
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			throw new IOException(String.format("BoxConstants connector failed to push the message to url [%s]", url));
		}

		return jsonResponse;
	}

	public static String sendUploadRequest(String url, JsonElement requestBody, byte[] file, String requestMethod, String accessToken, StringBuilder apiError, String appId) throws IOException {
		logger.info(String.format("Payload for sendHubRequest: [%s] ", requestBody.toString()));
		String hubId = null;
		try {
			StringBuilder contentType = new StringBuilder();
			contentType.append(BoxConstants.MULTIPART_FORM_DATA);
			contentType.append(BoxConstants.BOUNDARY_HEADER);
			contentType.append(BoxConstants.HttpService.BOUNDARY);

			HttpURLConnection httpURLConnection = HttpUtility.buildHttpURLConnection(url, requestMethod, contentType.toString(), true, appId);
			httpURLConnection.setRequestProperty(BoxConstants.AUTHORIZATION, BoxConstants.BEARER.concat(BoxConstants.SPACE).concat(accessToken));

			JsonObject jsonResponse = HttpUtility.sendHttpRequest(requestBody.toString(), true, file, httpURLConnection, null, appId).getAsJsonObject();

			if (!jsonResponse.isJsonNull() && jsonResponse.size() > 0) {
				logger.info("Response for BoxConstants req: " + jsonResponse.toString());
				JsonArray entriesArray = JsonPath.findArray(jsonResponse, BoxConstants.ENTRIES);
				if (entriesArray != null && entriesArray.size() > 0) {
					hubId = JsonPath.getValue(entriesArray.get(0), BoxConstants.PropertyNames.ID);
				}
				logger.info(String.format("BoxConstants item id is: [%s]", hubId));
			}
		} catch (BoxRuntimeException ex) {
			HttpUtility.extractHttpErrors(apiError, ex);
			logger.error(appId, BoxConstants.BOX_SERVICE, BoxConstants.LogCodes.BOX_1652, url, ex);
		} catch (Exception ex) {
			logger.error(appId, BoxConstants.BOX_SERVICE, BoxConstants.LogCodes.BOX_1652, url, ex);
			throw new IOException(String.format("BoxConstants connector failed to push the message to url [%s]", url));
		}

		logger.info("BoxConstants Item Id: " + hubId);
		return hubId;
	}

	public static byte[] downloadBlob(String assetURL, int limit) throws IOException {
		byte[] byteArray;
		try (BufferedInputStream inputStream = new BufferedInputStream(new URL(assetURL).openStream())) {
			try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
				int nRead;
				int totRead = 0;
				byte[] data = new byte[1024];
				while (totRead <= limit && (nRead = inputStream.read(data, 0, data.length)) != -1) {
					buffer.write(data, 0, nRead);
					totRead += nRead;
				}
				buffer.flush();
				byteArray = buffer.toByteArray();
			}
		}
		return byteArray;
	}

	public static String getAssertion(BoxSettings boxSettings, String appId) {
		try {
			PrivateKey key = getPrivateKey(boxSettings);

			JsonWebSignature jws = getJsonWebSignature(boxSettings, key);

			return jws.getCompactSerialization();
		} catch (IOException | JoseException | OperatorCreationException | PKCSException ex) {
			logger.error(appId, BoxConstants.BOX_SERVICE, BoxConstants.LogCodes.BOX_1652, ex);
		}
		return null;
	}

	private static JsonWebSignature getJsonWebSignature(BoxSettings boxSettings, PrivateKey key) {
		JwtClaims claims = new JwtClaims();
		claims.setIssuer(boxSettings.getClientId());
		claims.setAudience(boxSettings.getAuthurl());
		claims.setSubject(boxSettings.getEnterpriseID());
		claims.setClaim(BoxConstants.CLAIM_NAME, BoxConstants.CLAIM_VALUE);
		claims.setGeneratedJwtId(64);
		claims.setExpirationTimeMinutesInTheFuture(BoxConstants.JWT_EXPIRATION);

		JsonWebSignature jws = new JsonWebSignature();
		jws.setPayload(claims.toJson());
		jws.setKey(key);

		jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA512);
		jws.setHeader(BoxConstants.JWT_TYP_HEADER, BoxConstants.JWT_VALUE);
		jws.setHeader(BoxConstants.KEY_ID_HEADER, boxSettings.getPublicKeyID());
		return jws;
	}

	private static PrivateKey getPrivateKey(BoxSettings boxSettings) throws IOException, OperatorCreationException, PKCSException {
		Security.addProvider(new BouncyCastleProvider());

		PEMParser pemParser = new PEMParser(new StringReader(boxSettings.getPrivateKey()));
		Object keyPair;
		keyPair = pemParser.readObject();

		pemParser.close();

		// Finally, we decrypt the key using the passphrase
		JceOpenSSLPKCS8DecryptorProviderBuilder decryptBuilder = new JceOpenSSLPKCS8DecryptorProviderBuilder()
				.setProvider("BC");
		InputDecryptorProvider decryptProvider;
		decryptProvider = decryptBuilder.build(boxSettings.getPassphrase().toCharArray());
		PrivateKeyInfo keyInfo;
		keyInfo = ((PKCS8EncryptedPrivateKeyInfo) keyPair).decryptPrivateKeyInfo(decryptProvider);

		return (new JcaPEMKeyConverter()).getPrivateKey(keyInfo);
	}

}
