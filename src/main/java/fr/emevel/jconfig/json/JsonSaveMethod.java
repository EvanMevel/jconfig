package fr.emevel.jconfig.json;

import fr.emevel.jconfig.AutoSaver;
import fr.emevel.jconfig.SaveMethod;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class JsonSaveMethod implements SaveMethod {

    private final int indentFactor;

    public JsonSaveMethod(int indentFactor) {
        this.indentFactor = indentFactor;
    }

    @Override
    public <T> boolean load(T data, File file) throws IOException {
        JSONObject json;
        try (FileInputStream fis = new FileInputStream(file)) {
            if (fis.available() == 0) {
                return false;
            }
            json = new JSONObject(new JSONTokener(fis));
        }
        try {
            return JsonConverter.jsonToObject(data, json);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access Field in class " + data.getClass().getName(), e);
        }
    }

    @Override
    public <T> void save(T data, File file) throws IOException {
        JSONObject json = new JSONObject();
        Class<?> clazz = AutoSaver.getSaverClass(data);
        try {
            JsonConverter.saveToJSON(data, clazz, json);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access Field in class " + data.getClass().getName(), e);
        }
        try (FileWriter fw = new FileWriter(file)) {
            json.write(fw, indentFactor, 0);
        }
    }
}
