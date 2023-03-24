package fr.emevel.jconfig.json;

import fr.emevel.jconfig.SaveFieldInfo;

public interface JsonTypeConverter {

    Object fromJson(Object jsonValue, SaveFieldInfo info) throws IllegalAccessException;

    Object toJson(Object object) throws IllegalAccessException;

}
