package fr.emevel.jconfig;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class JsonSaveMethod implements SaveMethod {

    private static final Map<Class<?>, BiFunction<JSONObject, String, ?>> PRIMITIVE_GETTERS = new HashMap<>();

    static {
        PRIMITIVE_GETTERS.put(Character.class, (json, key) -> json.getString(key).charAt(0));
        PRIMITIVE_GETTERS.put(Character.TYPE, (json, key) -> json.getString(key).charAt(0));
        PRIMITIVE_GETTERS.put(Boolean.class, JSONObject::getBoolean);
        PRIMITIVE_GETTERS.put(Boolean.TYPE, JSONObject::getBoolean);
        PRIMITIVE_GETTERS.put(Byte.class, JSONObject::getInt);
        PRIMITIVE_GETTERS.put(Byte.TYPE, JSONObject::getInt);
        PRIMITIVE_GETTERS.put(Short.class, JSONObject::getLong);
        PRIMITIVE_GETTERS.put(Short.TYPE, JSONObject::getLong);
        PRIMITIVE_GETTERS.put(Integer.class, JSONObject::getInt);
        PRIMITIVE_GETTERS.put(Integer.TYPE, JSONObject::getInt);
        PRIMITIVE_GETTERS.put(Long.class, JSONObject::getLong);
        PRIMITIVE_GETTERS.put(Long.TYPE, JSONObject::getLong);
        PRIMITIVE_GETTERS.put(Float.class, JSONObject::getFloat);
        PRIMITIVE_GETTERS.put(Float.TYPE, JSONObject::getFloat);
        PRIMITIVE_GETTERS.put(Double.class, JSONObject::getDouble);
        PRIMITIVE_GETTERS.put(Double.TYPE, JSONObject::getDouble);
        PRIMITIVE_GETTERS.put(String.class, JSONObject::getString);

    }

    private static final Map<Class<?>, Supplier<?>> COLLECTIONS = Map.of(
            List.class, ArrayList::new,
            Set.class, HashSet::new,
            Collection.class, ArrayList::new
    );

    private static Supplier<?> getCollectionSupplier(Class<?> type) {
        for (Map.Entry<Class<?>, Supplier<?>> entry : COLLECTIONS.entrySet()) {
            if (entry.getKey().isAssignableFrom(type)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private final int indentFactor;

    public JsonSaveMethod(int indentFactor) {
        this.indentFactor = indentFactor;
    }

    @Override
    public <T> boolean load(T data, File file) throws IOException {
        JSONObject json;
        try (FileInputStream fis = new FileInputStream(file)) {
            if (fis.available() == 0) {
                return false;
            }
            json = new JSONObject(new JSONTokener(fis));
        }
        try {
            return loadJSON(data, json);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access Field in class " + data.getClass().getName(), e);
        }
    }

    private <T> boolean loadJSON(T data, JSONObject json) throws IOException, IllegalAccessException {
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
            BiFunction<JSONObject, String, ?> getter = PRIMITIVE_GETTERS.get(field.getType());
            if (getter != null) {
                field.set(data, getter.apply(json, field.getName()));
            } else if (field.getType().isArray()) {
                JSONArray array = json.getJSONArray(field.getName());
                Object value = Array.newInstance(field.getType().getComponentType(), array.length());
                loadArrayField(field.getType().getComponentType(), value, array);
                field.set(data, value);
            } else {
                Supplier<?> collectionSupplier = getCollectionSupplier(field.getType());
                if (collectionSupplier != null) {
                    Save sa = field.getAnnotation(Save.class);
                    if (sa.type() == Object.class) {
                        throw new IOException("Cannot load collection " + field.getName() + " without specifying type in @Save annotation");
                    }
                    JSONArray array = json.getJSONArray(field.getName());
                    Object value = loadCollectionField(sa.type(), collectionSupplier.get(), array);
                    field.set(data, value);
                } else {
                    Object value = loadObjectField(field.getType(), json.getJSONObject(field.getName()));
                    field.set(data, value);
                }
            }
        }
        return complete;
    }

    private <T> T loadObjectField(Class<T> type, JSONObject json) throws IOException, IllegalAccessException {
        T data = ReflectionUtils.defaultInstance(type);
        loadJSON(data, json);
        return data;
    }

    private <T> Collection<?> loadCollectionField(Class<T> elementsType, Object coll, JSONArray json) throws IOException, IllegalAccessException {
        Collection<T> collection = (Collection<T>) coll;
        for (int i = 0; i < json.length(); i++) {
            T element = loadElement(elementsType, json.get(i));
            collection.add(element);
        }
        return collection;
    }

    private <T> void loadArrayField(Class<T> elementType, Object array, JSONArray json) throws IOException, IllegalAccessException {
        for (int i = 0; i < json.length(); i++) {
            Array.set(array, i, loadElement(elementType, json.get(i)));
        }
    }

    private <T> T loadElement(Class<T> type, Object obj) throws IOException, IllegalAccessException {
        if (PRIMITIVE_GETTERS.containsKey(type)) {
            return (T) obj;
        } else if (type.isArray()) {
            JSONArray array = (JSONArray) obj;
            Object value = Array.newInstance(type.getComponentType(), array.length());
            loadArrayField(type.getComponentType(), value, array);
            return (T) value;
        } else {
            return loadObjectField(type, (JSONObject) obj);
        }
    }

    @Override
    public <T> void save(T data, File file) throws IOException {
        JSONObject json = new JSONObject();
        Class<?> clazz = AutoSaver.getSaverClass(data);
        try {
            saveJSON(data, clazz, json);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access Field in class " + data.getClass().getName(), e);
        }
        try (FileWriter fw = new FileWriter(file)) {
            json.write(fw, indentFactor, 0);
        }
    }

    private <T> void saveJSON(T data, Class<?> clazz, JSONObject json) throws IOException, IllegalAccessException {
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Save.class)) {
                continue;
            }
            field.setAccessible(true);
            json.put(field.getName(), getFieldValue(field.get(data)));
        }
    }

    private Object getFieldValue(Object obj) throws IllegalAccessException, IOException {
        BiFunction<JSONObject, String, ?> getter = PRIMITIVE_GETTERS.get(obj.getClass());
        if (getter != null) {
            return obj;
        } else {
            Supplier<?> collectionSupplier = getCollectionSupplier(obj.getClass());
            if (collectionSupplier != null) {
                JSONArray array = new JSONArray();
                saveCollectionField(obj, array);
                return array;
            } else if (obj.getClass().isArray()) {
                JSONArray array = new JSONArray();
                saveArrayField(obj, array);
                return array;
            } else {
                JSONObject elementJson = new JSONObject();
                saveObjectField(obj, elementJson);
                return elementJson;
            }
        }
    }

    private <T> void saveObjectField(T data, JSONObject json) throws IOException, IllegalAccessException {
        saveJSON(data, data.getClass(), json);
    }

    @SuppressWarnings("unchecked")
    private <T> void saveCollectionField(Object coll, JSONArray json) throws IOException, IllegalAccessException {
        Collection<T> collection = (Collection<T>) coll;
        for (T element : collection) {
            Object value = getFieldValue(element);
            json.put(value);
        }
    }

    private void saveArrayField(Object array, JSONArray json) throws IOException, IllegalAccessException {
        for (int i = 0; i < Array.getLength(array); i++) {
            Object value = getFieldValue(Array.get(array, i));
            json.put(value);
        }
    }
}
