package fr.emevel.jconfig.json;

import fr.emevel.jconfig.Save;
import fr.emevel.jconfig.SaveFieldInfo;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.List;

public class JsonConverter {

    private static final List<JsonTypeConverter> SAVERS = List.of(
            new JsonPrimitiveConverter(),
            new JsonUUIDConverter(),
            new JsonArrayConverter(),
            new JsonCollectionConverter(),
            new JsonMapConverter(),
            new JsonObjectConverter()
    );

    public static Object fieldToObject(Object jsonValue, SaveFieldInfo info) throws IllegalAccessException {
        for (JsonTypeConverter saver : SAVERS) {
            Object value = saver.fromJson(jsonValue, info);
            if (value != null) {
                return value;
            }
        }
        throw new IllegalStateException("Cannot load field " + info.getName() + " of type " + info.getType().getName());
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
        throw new IllegalStateException("Cannot save field of type " + object.getClass().getName());
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
