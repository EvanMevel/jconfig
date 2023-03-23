package fr.emevel.jconfig;

import lombok.Data;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

class AutoSaverTest {

    @Data
    public static class AutoSaverData {
        @Save
        private String testStr = "default";
    }

    @Test
    void testAutoSave() throws IOException {
        File fi = Files.createTempFile("test", ".json").toFile();
        fi.deleteOnExit();

        JSONObject json = new JSONObject();
        json.put("testStr", "test");

        try (FileWriter fw = new FileWriter(fi)) {
            json.write(fw);
        }

        LocalFileSource source = LocalFileSource.builder(fi).json().threadedAutoSave(false).build();
        AutoSaverData data = source.loadAutoSave(AutoSaverData.class);

        data.setTestStr("test2");

        AutoSaverData data2 = source.load(AutoSaverData.class);
        Assertions.assertEquals("test2", data2.getTestStr());
    }

    @Test
    void testThreadedAutoSave() throws IOException, InterruptedException {
        File fi = Files.createTempFile("test", ".json").toFile();
        fi.deleteOnExit();

        JSONObject json = new JSONObject();
        json.put("testStr", "test");

        try (FileWriter fw = new FileWriter(fi)) {
            json.write(fw);
        }

        LocalFileSource source = LocalFileSource.builder(fi).json().threadedAutoSave(true).build();
        AutoSaverData data = source.loadAutoSave(AutoSaverData.class);

        data.setTestStr("aaaaaaaaaaaa");
        data.setTestStr("zzzzz");
        data.setTestStr("test2");

        Thread.sleep(100);

        AutoSaverData data2 = source.load(AutoSaverData.class);
        Assertions.assertEquals("test2", data2.getTestStr());
    }

}