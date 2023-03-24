package fr.emevel.jconfig.json;

import fr.emevel.jconfig.SaveFieldInfo;
import org.json.JSONArray;

import java.lang.reflect.Array;

public class JsonArrayConverter implements JsonTypeConverter {

    @Override
    public Object fromJson(Object jsonValue, SaveFieldInfo info) throws IllegalAccessException {
        if (!info.getType().isArray()) {
            return null;
        }
        JSONArray jsonArray = (JSONArray) jsonValue;
        Object value = Array.newInstance(info.getType().getComponentType(), jsonArray.length());
        SaveFieldInfo childInfo = new SaveFieldInfo(info.getName(), info.getType().getComponentType(), Object.class);
        for (int i = 0; i < jsonArray.length(); i++) {
            Object element = JsonConverter.fieldToObject(jsonArray.get(i), childInfo);
            Array.set(value, i, element);
        }
        return value;
    }

    @Override
    public Object toJson(Object object) throws IllegalAccessException {
        if (!object.getClass().isArray()) {
            return null;
        }
        JSONArray array = new JSONArray();
        for (int i = 0; i < Array.getLength(object); i++) {
            Object value = Array.get(object, i);
            array.put(JsonConverter.objectToJson(value));
        }
        return array;
    }
}
