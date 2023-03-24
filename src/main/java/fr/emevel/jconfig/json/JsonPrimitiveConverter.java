package fr.emevel.jconfig.json;

import fr.emevel.jconfig.SaveFieldInfo;

import java.util.List;

public class JsonPrimitiveConverter implements JsonTypeConverter {

    private static final List<Class<?>> PRIMITIVES = List.of(
            Character.class,
            Character.TYPE,
            Boolean.class,
            Boolean.TYPE,
            Byte.class,
            Byte.TYPE,
            Short.class,
            Short.TYPE,
            Integer.class,
            Integer.TYPE,
            Long.class,
            Long.TYPE,
            Float.class,
            Float.TYPE,
            Double.class,
            Double.TYPE,
            String.class
    );

    @Override
    public Object fromJson(Object jsonValue, SaveFieldInfo info) {
        if (PRIMITIVES.contains(info.getType())) {
            return jsonValue;
        }
        return null;
    }

    @Override
    public Object toJson(Object object) {
        if (PRIMITIVES.contains(object.getClass())) {
            return object;
        }
        return null;
    }
}
