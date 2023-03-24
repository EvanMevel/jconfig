package fr.emevel.jconfig.json;

import fr.emevel.jconfig.SaveFieldInfo;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class JsonMapConverter implements JsonTypeConverter {

    private <T> Map<?, ?> loadMapField(SaveFieldInfo info, Object m, JSONObject json) throws IllegalAccessException {
        Map<String, T> map = (Map<String, T>) m;
        SaveFieldInfo childInfo = new SaveFieldInfo(info.getName(), info.getSaveType(), Object.class);
        for (String key : json.keySet()) {
            map.put(key, (T) JsonConverter.fieldToObject(json.get(key), childInfo));
        }
        return map;
    }

    @Override
    public Object fromJson(Object jsonValue, SaveFieldInfo info) throws IllegalAccessException {
        if (!Map.class.isAssignableFrom(info.getType())) {
            return null;
        }
        if (info.getSaveType() == Object.class) {
            throw new IllegalStateException("Cannot load Map " + info.getName() + " without specifying the value type in @Save annotation");
        }
        JSONObject json = (JSONObject) jsonValue;
        return loadMapField(info, new HashMap<>(), json);
    }

    private <T> void saveMapField(Object m, JSONObject json) throws IllegalAccessException {
        Map<?, T> map = (Map<?, T>) m;
        for (Map.Entry<?, T> entry : map.entrySet()) {
            json.put(entry.getKey().toString(), JsonConverter.objectToJson(entry.getValue()));
        }
    }

    @Override
    public Object toJson(Object object) throws IllegalAccessException {
        if (!Map.class.isAssignableFrom(object.getClass())) {
            return null;
        }
        JSONObject mapJson = new JSONObject();
        saveMapField(object, mapJson);
        return mapJson;
    }
}
