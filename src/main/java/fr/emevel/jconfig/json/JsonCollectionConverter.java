package fr.emevel.jconfig.json;

import fr.emevel.jconfig.SaveFieldInfo;
import org.json.JSONArray;

import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class JsonCollectionConverter implements JsonTypeConverter {

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

    private <T> Collection<?> loadCollectionField(SaveFieldInfo info, Object coll, JSONArray jsonArray) throws IllegalAccessException {
        Collection<T> collection = (Collection<T>) coll;
        SaveFieldInfo childInfo = new SaveFieldInfo(info.getName(), info.getSaveType(), Object.class);
        for (int i = 0; i < jsonArray.length(); i++) {
            T element = (T) JsonConverter.fieldToObject(jsonArray.get(i), childInfo);
            collection.add(element);
        }
        return collection;
    }

    @Override
    public Object fromJson(Object jsonValue, SaveFieldInfo info) throws IllegalAccessException {
        Supplier<?> collectionSupplier = getCollectionSupplier(info.getType());
        if (collectionSupplier == null) {
            return null;
        }
        if (info.getSaveType() == Object.class) {
            throw new IllegalStateException("Cannot load collection " + info.getName() + " without specifying type in @Save annotation");
        }
        JSONArray array = (JSONArray) jsonValue;
        return loadCollectionField(info, collectionSupplier.get(), array);
    }

    private <T> void saveCollectionField(Object coll, JSONArray json) throws IllegalAccessException {
        Collection<T> collection = (Collection<T>) coll;
        for (T element : collection) {
            json.put(JsonConverter.objectToJson(element));
        }
    }

    @Override
    public Object toJson(Object object) throws IllegalAccessException {
        Supplier<?> collectionSupplier = getCollectionSupplier(object.getClass());
        if (collectionSupplier == null) {
            return null;
        }
        JSONArray array = new JSONArray();
        saveCollectionField(object, array);
        return array;
    }
}
