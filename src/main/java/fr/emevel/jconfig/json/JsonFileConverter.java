package fr.emevel.jconfig.json;

import fr.emevel.jconfig.SaveFieldInfo;

import java.io.File;

public class JsonFileConverter implements JsonTypeConverter {
    @Override
    public Object fromJson(Object jsonValue, SaveFieldInfo info) throws IllegalAccessException {
        if (info.getType() == File.class) {
            return new File((String) jsonValue);
        }
        return null;
    }

    @Override
    public Object toJson(Object object) throws IllegalAccessException {
        if (object.getClass() == File.class) {
            return ((File) object).getAbsolutePath();
        }
        return null;
    }
}
