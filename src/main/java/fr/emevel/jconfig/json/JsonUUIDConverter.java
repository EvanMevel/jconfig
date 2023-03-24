package fr.emevel.jconfig.json;

import fr.emevel.jconfig.SaveFieldInfo;

import java.util.UUID;

public class JsonUUIDConverter implements JsonTypeConverter {

    @Override
    public Object fromJson(Object jsonValue, SaveFieldInfo info) throws IllegalAccessException {
        if (info.getType() == UUID.class) {
            return UUID.fromString((String) jsonValue);
        }
        return null;
    }

    @Override
    public Object toJson(Object object) throws IllegalAccessException {
        if (object.getClass() == UUID.class) {
            return object.toString();
        }
        return null;
    }
}
