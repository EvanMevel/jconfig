package fr.emevel.jconfig.json;

import fr.emevel.jconfig.ReflectionUtils;
import fr.emevel.jconfig.SaveFieldInfo;
import org.json.JSONObject;

public class JsonObjectConverter implements JsonTypeConverter {

    private <T> T loadObjectField(Class<T> type, JSONObject json) throws IllegalAccessException {
        T data = ReflectionUtils.defaultInstance(type);
        JsonConverter.jsonToObject(data, json);
        return data;
    }

    @Override
    public Object fromJson(Object jsonValue, SaveFieldInfo info) throws IllegalAccessException {
        JSONObject child = (JSONObject) jsonValue;
        return loadObjectField(info.getType(), child);
    }

    private <T> void saveObjectField(T object, JSONObject jsonObject) throws IllegalAccessException {
        JsonConverter.saveToJSON(object, object.getClass(), jsonObject);
    }

    @Override
    public Object toJson(Object object) throws IllegalAccessException {
        JSONObject jsonObject = new JSONObject();
        saveObjectField(object, jsonObject);
        return jsonObject;
    }
}
