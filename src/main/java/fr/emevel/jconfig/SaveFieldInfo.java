package fr.emevel.jconfig;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Field;

@AllArgsConstructor
@Getter
public class SaveFieldInfo {
    private final String name;
    private final Class<?> type;
    private final Class<?> saveType;

    public SaveFieldInfo(Field field) {
        this.name = field.getName();
        this.type = field.getType();
        Save save = field.getAnnotation(Save.class);
        this.saveType = save.type();
    }

}
