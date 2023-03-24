package fr.emevel.jconfig;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class LocalFileSourceTest {

    public static class TestData {
        @Save
        private String testStr = "default";
        @Save
        private int testInt = 2;
        @Save
        private boolean testBool = true;
        @Save
        private NestedData nestedData = new NestedData(10);
        @Save(type = NestedData.class)
        private List<NestedData> nestedDataList = List.of(new NestedData(10), new NestedData(20));
        @Save
        private NestedData[] nestedDataArray = new NestedData[]{new NestedData(10), new NestedData(20)};
        @Save(type = Integer.class)
        private List<Integer> intList = List.of(1, 2, 3);
        @Save
        private int[][] intArrayArray = new int[][]{{1, 2, 3}, {4, 5, 6}};
        @Save(type = NestedList.class)
        private Map<String, NestedList> nestedMap = Map.of("key1", new NestedList(), "key2", new NestedList());
        @Save
        private UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

        @SuppressWarnings({"unused", "FieldMayBeFinal"})
        private String notSaved = "notSaved";
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class NestedData {
        @Save
        private int nestedInt = 5;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class NestedList {
        @Save(type = Integer.class)
        private List<Integer> nestedDataList = List.of(10, 20);
    }

    private static final JSONObject TEST_JSON = new JSONObject();
    private static final TestData TEST_DATA = new TestData();
    private static File file;

    @BeforeAll
    static void setupJson() {
        TEST_JSON.put("testStr", "test");
        TEST_JSON.put("testInt", 4444);
        TEST_JSON.put("testBool", false);

        JSONObject nested = new JSONObject();
        nested.put("nestedInt", 20);
        TEST_JSON.put("nestedData", nested);

        JSONArray nestedList = new JSONArray();
        JSONObject nested2 = new JSONObject();
        nested2.put("nestedInt", 5555);
        nestedList.put(nested2);
        TEST_JSON.put("nestedDataList", nestedList);

        JSONArray nestedArray = new JSONArray();
        JSONObject nested3 = new JSONObject();
        nested3.put("nestedInt", 6666);
        nestedArray.put(nested3);
        TEST_JSON.put("nestedDataArray", nestedArray);

        JSONArray intList = new JSONArray();
        intList.put(444);
        intList.put(555);
        TEST_JSON.put("intList", intList);

        JSONArray intArrayArray = new JSONArray();
        JSONArray intArray2 = new JSONArray();
        intArray2.put(999);
        intArray2.put(888);
        intArrayArray.put(intArray2);
        TEST_JSON.put("intArrayArray", intArrayArray);

        JSONObject nestedMap = new JSONObject();
        JSONObject nestedMapList = new JSONObject();
        JSONArray nestedMapListArray = new JSONArray();
        nestedMapListArray.put(1111);
        nestedMapListArray.put(2222);
        nestedMapList.put("nestedDataList", nestedMapListArray);
        nestedMap.put("key5", nestedMapList);
        TEST_JSON.put("nestedMap", nestedMap);

        TEST_JSON.put("uuid", "00000000-0000-1111-0000-000000000000");


        TEST_DATA.testStr = "test";
        TEST_DATA.testInt = 4444;
        TEST_DATA.testBool = false;
        TEST_DATA.nestedData = new NestedData(20);
        TEST_DATA.nestedDataList = List.of(new NestedData(5555));
        TEST_DATA.nestedDataArray = new NestedData[]{new NestedData(6666)};
        TEST_DATA.intList = List.of(444, 555);
        TEST_DATA.intArrayArray = new int[][]{{999, 888}};
        TEST_DATA.nestedMap = Map.of("key5", new NestedList(List.of(1111, 2222)));
        TEST_DATA.uuid = UUID.fromString("00000000-0000-1111-0000-000000000000");
    }

    @BeforeEach
    void setupFile() throws IOException {
        file = Files.createTempFile("test", ".json").toFile();
        file.deleteOnExit();
    }

    @Test
    void load() throws IOException {
        try (FileWriter fw = new FileWriter(file)) {
            TEST_JSON.write(fw);
        }

        LocalFileSource source = LocalFileSource.builder(file).json().build();
        TestData test = source.load(TestData.class);

        Assertions.assertEquals("test", test.testStr);
        Assertions.assertEquals(4444, test.testInt);
        Assertions.assertFalse(test.testBool);
        Assertions.assertEquals(20, test.nestedData.nestedInt);
        Assertions.assertEquals(1, test.nestedDataList.size());
        Assertions.assertEquals(5555, test.nestedDataList.get(0).nestedInt);
        Assertions.assertEquals(1, test.nestedDataArray.length);
        Assertions.assertEquals(6666, test.nestedDataArray[0].nestedInt);
        Assertions.assertEquals(2, test.intList.size());
        Assertions.assertEquals(444, test.intList.get(0));
        Assertions.assertEquals(555, test.intList.get(1));
        Assertions.assertEquals(1, test.intArrayArray.length);
        Assertions.assertEquals(2, test.intArrayArray[0].length);
        Assertions.assertEquals(999, test.intArrayArray[0][0]);
        Assertions.assertEquals(888, test.intArrayArray[0][1]);
        Assertions.assertEquals(1, test.nestedMap.size());
        Assertions.assertEquals(2, test.nestedMap.get("key5").nestedDataList.size());
        Assertions.assertEquals(1111, test.nestedMap.get("key5").nestedDataList.get(0));
        Assertions.assertEquals(2222, test.nestedMap.get("key5").nestedDataList.get(1));
    }

    @Test
    void save() throws IOException {
        LocalFileSource source = LocalFileSource.builder(file).json().build();
        source.save(TEST_DATA);

        JSONObject json = new JSONObject(new String(Files.readAllBytes(file.toPath())));
        Assertions.assertEquals(TEST_JSON.toString(2), json.toString(2));
    }

    @Test
    void saveFileNotExists() throws IOException {
        Assertions.assertTrue(file.delete());

        LocalFileSource source = LocalFileSource.builder(file).json().saveIfNotExists(true).build();
        source.load(TestData.class);

        JSONObject json = new JSONObject(new String(Files.readAllBytes(file.toPath())));
        Assertions.assertNotNull(json);
        Assertions.assertTrue(json.has("testStr"));
    }

    @Test
    void saveFileEmpty() throws IOException {
        LocalFileSource source = LocalFileSource.builder(file).json().saveIfNotExists(true).build();
        source.load(TestData.class);

        JSONObject json = new JSONObject(new String(Files.readAllBytes(file.toPath())));
        Assertions.assertNotNull(json);
        Assertions.assertTrue(json.has("testStr"));
    }

}