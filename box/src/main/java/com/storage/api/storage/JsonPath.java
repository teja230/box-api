package com.storage.api.storage;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Json path knows how to get and set the value of field from an Json object using XPath like syntax.
 */
public class JsonPath {
	public static final String FIELD_SEPARATOR = ".";

	/**
	 * Helper method to return true if path is found.
	 *
	 * @param parentElement Json parent element to begin search for child objects.
	 * @param path          Child path to search. Ex: obj1.obj2.obj3 or obj1.array[1].obj2
	 */
	public static boolean hasValue(JsonElement parentElement, String path) {
		JsonElement element = findElement(parentElement, path);
		return element != null;
	}

	/**
	 * Helper method to get the value of an object or return null if not found.
	 *
	 * @param parentElement Json parent element to begin search for child objects.
	 * @param path          Child path to search. Ex: obj1.obj2.obj3 or obj1.array[1].obj2
	 */
	public static String getValue(JsonElement parentElement, String path) {
		return getValue(parentElement, path, null);
	}

	/**
	 * Helper method to get the string value of an object or return default value if not found.
	 *
	 * @param parentElement Json parent element to begin search for child objects.
	 * @param path          Child path to search. Ex: obj1.obj2.obj3 or obj1.array[1].obj2
	 * @param defaultValue  Value to return if path is not found.
	 */
	public static String getValue(JsonElement parentElement, String path, String defaultValue) {
		JsonElement element = findElement(parentElement, path);
		if (element != null && element.isJsonPrimitive()) {
			return element.getAsString();
		}
		return defaultValue;
	}

	/**
	 * Helper method to get the boolean value of an object or return default value if not found.
	 *
	 * @param parentElement Json parent element to begin search for child objects.
	 * @param path          Child path to search. Ex: obj1.obj2.obj3 or obj1.array[1].obj2
	 * @param defaultValue  Value to return if path is not found.
	 */
	public static boolean getValue(JsonElement parentElement, String path, boolean defaultValue) {
		JsonElement element = findElement(parentElement, path);
		if (element != null && element.isJsonPrimitive()) {
			return element.getAsBoolean();
		}
		return defaultValue;
	}

	/**
	 * Helper method to get the int value of an object or return default value if not found.
	 *
	 * @param parentElement Json parent element to begin search for child objects.
	 * @param path          Child path to search. Ex: obj1.obj2.obj3 or obj1.array[1].obj2
	 * @param defaultValue  Value to return if path is not found.
	 */
	public static int getValue(JsonElement parentElement, String path, int defaultValue) {
		JsonElement element = findElement(parentElement, path);
		if (element != null && element.isJsonPrimitive()) {
			return element.getAsInt();
		}
		return defaultValue;
	}

	/**
	 * Helper method to get the long value of an object or return default value if not found.
	 *
	 * @param parentElement Json parent element to begin search for child objects.
	 * @param path          Child path to search. Ex: obj1.obj2.obj3 or obj1.array[1].obj2
	 * @param defaultValue  Value to return if path is not found.
	 */
	public static long getValue(JsonElement parentElement, String path, long defaultValue) {
		JsonElement element = findElement(parentElement, path);
		if (element != null && element.isJsonPrimitive()) {
			return element.getAsInt();
		}
		return defaultValue;
	}

	/**
	 * Set a value in the json object.
	 *
	 * @param parentElement The parent element to set the value in.
	 * @param path          The field path to set the value.
	 * @param value         The new value of the field.
	 */
	public static void setValue(JsonElement parentElement, String path, String value) {
		Map.Entry<JsonElement, String> entry = createParentPath(parentElement, path);
		JsonObject parent = entry.getKey() != null && entry.getKey().isJsonObject() ? entry.getKey().getAsJsonObject() : null;
		String field = entry.getValue();

		if (parent != null && field != null) {
			parent.addProperty(field, value);
		}
	}

	/**
	 * Set a value in the json object.
	 *
	 * @param parentElement The parent element to set the value in.
	 * @param path          The field path to set the value.
	 * @param value         The new value of the field.
	 */
	public static void setValue(JsonElement parentElement, String path, boolean value) {
		Map.Entry<JsonElement, String> entry = createParentPath(parentElement, path);
		JsonObject parent = entry.getKey() != null && entry.getKey().isJsonObject() ? entry.getKey().getAsJsonObject() : null;
		String field = entry.getValue();

		if (parent != null && field != null) {
			parent.addProperty(field, value);
		}
	}

	/**
	 * Set a JSON object in the json object.
	 *
	 * @param parentElement The parent element to set the value in.
	 * @param path          The field path to set the value.
	 * @param jsonObject    The new value of the field.
	 */
	public static void setValue(JsonElement parentElement, String path, JsonObject jsonObject) {
		Map.Entry<JsonElement, String> entry = createParentPath(parentElement, path);
		JsonObject parent = entry.getKey() != null && entry.getKey().isJsonObject() ? entry.getKey().getAsJsonObject() : null;
		String field = entry.getValue();

		if (parent != null && field != null) {
			if (parent.has(field)) {
				parent.remove(field);
			}
			parent.add(field, jsonObject);
		}
	}

	/**
	 * Set a JSON array in the json object.
	 *
	 * @param parentElement The parent element to set the value in.
	 * @param path          The field path to set the value.
	 * @param jsonArray     The new value of the field.
	 */
	public static void setValue(JsonElement parentElement, String path, JsonArray jsonArray) {
		Map.Entry<JsonElement, String> entry = createParentPath(parentElement, path);
		JsonObject parent = entry.getKey() != null && entry.getKey().isJsonObject() ? entry.getKey().getAsJsonObject() : null;
		String field = entry.getValue();

		if (parent != null && field != null) {
			if (parent.has(field)) {
				parent.remove(field);
			}
			parent.add(field, jsonArray);
		}
	}

	/**
	 * Remove the last item in the path.
	 *
	 * @param parentElement The parent element to search for the field to remove.
	 * @param path          The path to the field to remove, relative to the parent.
	 */
	public static void removeField(JsonElement parentElement, String path) {
		removeField(parentElement, getParentPath(path), getLastField(path));
	}

	/**
	 * Set a JSON array in the json object.
	 *
	 * @param parentElement The parent element to search for the field to remove.
	 * @param parentPath    The path to the parent containing the field to remove, relative to the parent.
	 * @param field         The field to remove.
	 */
	public static void removeField(JsonElement parentElement, String parentPath, String field) {
		JsonObject parent = findObject(parentElement, parentPath);
		if (parent != null && field != null) {
			parent.remove(field);
		}
	}

	/**
	 * Helper method to find an array or return null if not found.
	 *
	 * @param parentElement Json parent element to begin search for child objects.
	 * @param path          Child path to search. Ex: obj1.obj2.array or obj1.array1[1].obj3.array2
	 */
	public static JsonArray findArray(JsonElement parentElement, String path) {
		JsonElement findElement = findElement(parentElement, path);
		if (findElement == null || !findElement.isJsonArray()) {
			return null;
		}
		return findElement.getAsJsonArray();
	}

	/**
	 * Helper method to create an array.
	 *
	 * @param parentElement Json parent element to begin search for child objects.
	 * @param path          Child path to search. Ex: obj1.obj2.array or obj1.array1[1].obj3.array2
	 */
	public static JsonArray createArray(JsonElement parentElement, String path) {
		if (parentElement == null || !parentElement.isJsonObject()) {
			throw new IllegalArgumentException("JsonRecord cannot create array, parent is not JsonObject");
		}

		JsonArray array = new JsonArray();
		setValue(parentElement, path, array);

		return array;
	}

	/**
	 * Helper method to find an object or return null if not found.
	 *
	 * @param parentElement Json parent element to begin search for child objects.
	 * @param path          Child path to search. Ex: obj1.obj2.obj3 or obj1.array[1].obj2
	 */
	public static JsonObject findObject(JsonElement parentElement, String path) {
		JsonElement findElement = findElement(parentElement, path);
		if (findElement == null || !findElement.isJsonObject()) {
			return null;
		}
		return findElement.getAsJsonObject();
	}

	/**
	 * Return the parent of the last field in the path. Ex: obj1.obj2.obj3 => obj1.obj2
	 *
	 * @param path Path to parse.
	 */
	public static String getParentPath(String path) {
		int index = path.lastIndexOf('.');
		if (index != -1) {
			return path.substring(0, index);
		}
		return path;
	}

	/**
	 * Return the last field in the path. Ex: obj1.obj2.obj3 => obj3
	 *
	 * @param path Path to parse.
	 */
	public static String getLastField(String path) {
		int index = path.lastIndexOf('.');
		if (index != -1) {
			return path.substring(index + 1);
		}
		return path;
	}

	/**
	 * Create the parent path in the json hierarchy.
	 *
	 * @param parentElement Parent element of new path.
	 * @param path          Path to create.
	 * @return Key/value pair of parent json object and leaf field name to set the value.
	 */
	private static Map.Entry<JsonElement, String> createParentPath(JsonElement parentElement, String path) {
		JsonElement currentElement = parentElement;
		JsonFieldIterator iterator = new JsonFieldIterator(path);
		final String JSON_RECORD_SET_VALUE_FAILED_INVALID_PATH = "JsonPath set value failed because of invalid field path: %s";
		final String JSON_RECORD_SET_VALUE_FAILED_UNEXPECTED_TYPE = "JsonPath set value failed, expected = %s,found = %s, field = %s";

		while (iterator.hasNext()) {
			FieldToken fieldToken = iterator.next();

			if (currentElement == null) {
				throw new IllegalArgumentException(String.format(JSON_RECORD_SET_VALUE_FAILED_INVALID_PATH, path));
			}
			if (!currentElement.isJsonObject()) {
				throw new IllegalArgumentException(String.format(JSON_RECORD_SET_VALUE_FAILED_UNEXPECTED_TYPE,
						JsonObject.class.getSimpleName(), currentElement.getClass().getSimpleName(), path));
			}

			switch (fieldToken.type) {
				case Object: {
					JsonObject parent = (JsonObject) currentElement;
					currentElement = parent.get(fieldToken.name);
					if (currentElement == null) {
						currentElement = new JsonObject();
						parent.add(fieldToken.name, currentElement);
					}
					break;
				}
				case Array: {
					// Make sure the array exists.
					JsonObject parent = (JsonObject) currentElement;
					String arrayName = getArrayName(fieldToken.name);
					JsonElement arrayElement = parent.get(arrayName);
					if (arrayElement == null) {
						arrayElement = new JsonArray();
						parent.add(arrayName, arrayElement);
					}
					if (!arrayElement.isJsonArray()) {
						throw new IllegalArgumentException(String.format(JSON_RECORD_SET_VALUE_FAILED_UNEXPECTED_TYPE,
								JsonArray.class.getSimpleName(), arrayElement.getClass().getSimpleName(), path));
					}

					// Make sure the array child object exists. Ex: if index=1 is referenced, then make sure index=0 and index=1 exist.
					JsonArray array = (JsonArray) arrayElement;
					int index = getArrayIndex(array, fieldToken.name);

					// If index is less than zero.
					if (index < 0) {
						throw new IllegalArgumentException(String.format(JSON_RECORD_SET_VALUE_FAILED_INVALID_PATH, path));
					}

					// If field is referencing array index = 5 and there are only 2 nodes, create needed nodes.
					while (index >= array.size()) {
						array.add(new JsonObject());
					}

					currentElement = array.get(index);
					break;
				}
				case Primitive: {
					return new AbstractMap.SimpleEntry<>(currentElement, fieldToken.name);
				}
			}
		}
		return new AbstractMap.SimpleEntry<>(currentElement, null);
	}

	/**
	 * Helper method to find the element or return null if not found.
	 */
	private static JsonElement findElement(JsonElement parentElement, String field) {
		JsonElement currentElement = parentElement;
		JsonFieldIterator iterator = new JsonFieldIterator(field);

		while (iterator.hasNext()) {
			FieldToken fieldToken = iterator.next();

			if (currentElement == null || !currentElement.isJsonObject()) {
				return null;
			}

			switch (fieldToken.type) {
				case Object:
					JsonObject parent = (JsonObject) currentElement;
					currentElement = parent.get(fieldToken.name);
					break;
				case Array:
					JsonObject parentObject = (JsonObject) currentElement;
					String arrayName = getArrayName(fieldToken.name);
					JsonElement arrayElement = parentObject.get(arrayName);
					if (arrayElement == null || !arrayElement.isJsonArray()) {
						return null;
					}
					JsonArray array = (JsonArray) arrayElement;
					int index = getArrayIndex(array, fieldToken.name);
					if (index < 0 || index >= array.size()) {
						return null;
					}
					currentElement = array.get(index);
					break;
				case Primitive:
					JsonObject value = (JsonObject) currentElement;
					currentElement = value.get(fieldToken.name);
					break;
			}
		}

		return currentElement;
	}

	/**
	 * Helper method to return field name with array index stripped off. Ex: "field[0]" => field.
	 *
	 * @param field The field name to parse.
	 * @return Index parsed from field name.
	 */
	private static String getArrayName(String field) {
		int pos = field.lastIndexOf('[');
		if (pos >= 0) {
			return field.substring(0, pos);
		}
		return field;
	}

	/**
	 * Helper method to return index from field name. Ex: "field[0]" => 0.
	 *
	 * @param array The array to search.
	 * @param field The field name to parse.
	 * @return Index parsed from field name.
	 */
	private static int getArrayIndex(JsonArray array, String field) {
		String indexString = parseArrayIndex(field);
		return Integer.parseInt(indexString);
	}

	/**
	 * Helper method to return index from field name. Ex: "field[0]" => "0".
	 *
	 * @param field The field name to parse.
	 * @return Index parsed from field name.
	 */
	private static String parseArrayIndex(String field) {
		final String FIELD_NAME_INVALID_ARRAY_FORMAT = "field array format is invalid, field = %s";
		int start = field.lastIndexOf('[');
		int end = field.lastIndexOf(']');
		if (start >= 0 && end >= 0 && start < end) {
			return field.substring(start + 1, end);
		}
		throw new IllegalArgumentException(String.format(FIELD_NAME_INVALID_ARRAY_FORMAT, field));
	}

	public enum FieldType {
		Object,
		Array,
		Primitive
	}

	/**
	 * Helper class to iterate a field hierarchy. Ex: obj1.obj1.arr[0].obj
	 */
	private static class JsonFieldIterator implements Iterator<FieldToken> {
		private Iterator<String> fieldIterator;

		public JsonFieldIterator(String field) {
			if (!Strings.isNullOrEmpty(field)) {
				fieldIterator = splitFields(field, FIELD_SEPARATOR).iterator();
			}
		}

		/**
		 * @return True if there are more field tokens in the message.
		 */
		@Override
		public boolean hasNext() {
			return fieldIterator != null && fieldIterator.hasNext();
		}

		/**
		 * @return The next field token.
		 */
		@Override
		public FieldToken next() {
			String name = fieldIterator.next();
			FieldType type = name.contains("[") ? FieldType.Array : fieldIterator.hasNext() ? FieldType.Object : FieldType.Primitive;

			return new FieldToken(name, type);
		}
	}

	private static class FieldToken {
		private String name;
		private FieldType type;

		public FieldToken(String name, FieldType type) {
			this.name = name;
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public FieldType getType() {
			return type;
		}

		public void setType(FieldType type) {
			this.type = type;
		}
	}

	//todo: Not required until contextkey is supported. Simple numeric array index does not require this complexity.

	/**
	 * Split the fields using separator character. Ignore separator inside array brackets. Ex: t1.t2[x.y=4] => t1,
	 * t2[x.y=4].
	 */
	private static List<String> splitFields(String path, String separator) {
		ArrayList<String> tokens = new ArrayList<>();

		String partialToken = null;
		for (String token : path.split(Pattern.quote(separator))) {
			if (!Strings.isNullOrEmpty(partialToken)) {
				if (hasUnbalancedArrayBraces(token)) {
					tokens.add(partialToken + separator + token);
					partialToken = null;
				} else {
					partialToken += separator;
					partialToken += token;
				}
			} else if (hasUnbalancedArrayBraces(token)) {
				partialToken = token;
			} else {
				tokens.add(token);
			}
		}

		return tokens;
	}

	private static boolean hasUnbalancedArrayBraces(String token) {
		int braceCount = 0;

		for (char c : token.toCharArray()) {
			if (c == '[') {
				braceCount++;
			} else if (c == ']') {
				braceCount--;
			}
		}

		return braceCount != 0;
	}
}