package com.storage.api.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class StringUtils {
	private StringUtils() {
		throw new IllegalStateException("StringUtils should be used as a utility class");
	}

	public static String toString(InputStream inputStream) throws IOException {
		final int bufferSize = 1024;
		final char[] buffer = new char[bufferSize];
		final StringBuilder out = new StringBuilder();
		if (inputStream != null) {
			Reader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
			for (; ; ) {
				int rsz = in.read(buffer, 0, buffer.length);
				if (rsz < 0) {
					break;
				}
				out.append(buffer, 0, rsz);
			}
		}
		return out.toString();
	}

	public static String trimDoubleQuotes(String text) {
		int textLength = text.length();

		if (textLength >= 2 && text.charAt(0) == '"' && text.charAt(textLength - 1) == '"') {
			return text.substring(1, textLength - 1);
		}

		return text;
	}
}
