package fr.emevel.jconfig;

import lombok.Setter;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Super;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AutoSaver {

    private static final ByteBuddy byteBuddy = new ByteBuddy();

    private static boolean isSetter(Method m) {
        return m.getName().startsWith("set") && m.getReturnType().equals(void.class) && m.getParameterCount() == 1;
    }

    private static <T> DynamicType.Builder<T> overrideSetters(Class<T> type, DynamicType.Builder<T> subclass, Saver saveWrapper) {
        for (Method m : type.getDeclaredMethods()) {
            if (!isSetter(m)) {
                continue;
            }
            subclass = subclass.method(ElementMatchers.is(m))
                    .intercept(MethodDelegation.to(saveWrapper));
        }
        return subclass;
    }

    public static <T> T createAutoSaveWrapper(Class<T> type, Source source, boolean threadedAutoSave) {
        Saver saveWrapper = threadedAutoSave ? new ThreadedSaveWrapper() : new SaveWrapper();

        DynamicType.Builder<T> subclass = byteBuddy.subclass(type);
        subclass = overrideSetters(type, subclass, saveWrapper);
        subclass = subclass.defineField("saverClass", Class.class, Visibility.PUBLIC);

        Class<? extends T> clazz = subclass.make()
                .load(type.getClassLoader())
                .getLoaded();
        T data =  ReflectionUtils.defaultInstance(clazz);

        try {
            clazz.getDeclaredField("saverClass")
                            .set(data, type);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        saveWrapper.setSave(() -> {
            System.out.println("Saving data");
            try {
                source.save(data);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save data", e);
            }
        });
        return data;
    }

    public static <T> Class<?> getSaverClass(T data) {
        Field getSaverClass;
        try {
            getSaverClass = data.getClass().getDeclaredField("saverClass");
        } catch (NoSuchFieldException e) {
            return data.getClass();
        }
        try {
            return (Class<?>) getSaverClass.get(data);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to get saver class", e);
        }
    }

    public static class SaveWrapper implements Saver {

        @Setter
        private Runnable save;

        @SuppressWarnings("unused")
        public void handle(@SuperCall Runnable runnable, @Super Object data) {
            runnable.run();
            save.run();
        }

    }

    public static class ThreadedSaveWrapper implements Saver {

        @Setter
        private Runnable save;
        private Thread saveThread = null;

        @SuppressWarnings("unused")
        public void handle(@SuperCall Runnable runnable, @Super Object data) {
            runnable.run();
            if (saveThread == null) {
                saveThread = new Thread(() -> {
                    save.run();
                    saveThread = null;
                });
                saveThread.start();
            }
        }

    }

    private interface Saver {
        void setSave(Runnable save);
    }

}
