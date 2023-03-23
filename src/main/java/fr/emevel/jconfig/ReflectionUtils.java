package fr.emevel.jconfig;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ReflectionUtils {

    public static <T> Constructor<T> getConstructor(Class<T> type) {
        try {
            return type.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new SaveDataFormatException("No default constructor found for class " + type.getName());
        }
    }

    public static <T> T defaultInstance(Class<T> type) {
        Constructor<T> constructor = getConstructor(type);
        try {
            return constructor.newInstance();
        } catch (InstantiationException e) {
            throw new SaveDataFormatException("Class " + type.getName() + " cannot be instantiated.", e);
        } catch (IllegalAccessException e) {
            throw new SaveDataFormatException("Constructor for class " + type.getName() + " is not accessible", e);
        } catch (InvocationTargetException e) {
            throw new SaveDataFormatException("Got an exception on using default constructor for class " + type.getName(), e);
        }
    }
}
