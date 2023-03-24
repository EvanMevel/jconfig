package fr.emevel.jconfig.json;

import fr.emevel.jconfig.Save;
import fr.emevel.jconfig.SaveFieldInfo;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.List;

public class JsonConverter {

    public static final List<JsonTypeConverter> SAVERS = List.of(
            new JsonPrimitiveConverter(),
            new JsonUUIDConverter(),
            new JsonFileConverter(),
            new JsonArrayConverter(),
            new JsonCollectionConverter(),
            new JsonMapConverter()
    );
    private static final JsonObjectConverter OBJECT_CONVERTER = new JsonObjectConverter();

    public static Object fieldToObject(Object jsonValue, SaveFieldInfo info) throws IllegalAccessException {
        for (JsonTypeConverter saver : SAVERS) {
            Object value = saver.fromJson(jsonValue, info);
            if (value != null) {
                return value;
            }
        }
        return OBJECT_CONVERTER.fromJson(jsonValue, info);
    }

    public static <T> boolean jsonToObject(T data, JSONObject json) throws IllegalAccessException {
        boolean complete = true;

        for (Field field : data.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(Save.class)) {
                continue;
            }
            if (!json.has(field.getName())) {
                complete = false;
                continue;
            }
            field.setAccessible(true);
            SaveFieldInfo info = new SaveFieldInfo(field);
            Object jsonValue = json.get(field.getName());
            Object value = fieldToObject(jsonValue, info);
            field.set(data, value);
        }

        return complete;
    }

    public static Object objectToJson(Object object) throws IllegalAccessException {
        for (JsonTypeConverter saver : SAVERS) {
            Object jsonValue = saver.toJson(object);
            if (jsonValue != null) {
                return jsonValue;
            }
        }
        return OBJECT_CONVERTER.toJson(object);
    }

    public static <T> void saveToJSON(T data, Class<?> clazz, JSONObject json) throws IllegalAccessException {
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Save.class)) {
                continue;
            }
            field.setAccessible(true);
            Object value = field.get(data);
            if (value == null) {
                continue;
            }
            Object jsonValue = objectToJson(value);
            json.put(field.getName(), jsonValue);
        }
    }
}
