package fr.emevel.jconfig;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ReflectionUtilsTest {

    public static class TestClass {
        public TestClass() {

        }
    }

    public static class NotAccessibleTestClass {
        private NotAccessibleTestClass() {

        }
    }

    public static abstract class AbstractTestClass {
        public AbstractTestClass() {

        }
    }

    public static class ThrowerTestClass {
        public ThrowerTestClass() {
            throw new RuntimeException();
        }
    }

    @Test
    void getConstructor() {
        Assertions.assertNotNull(ReflectionUtils.getConstructor(TestClass.class));
    }

    @Test
    void instantiateNotAccessible() {
        Assertions.assertThrows(SaveDataFormatException.class,
                () -> ReflectionUtils.defaultInstance(NotAccessibleTestClass.class)
        );
    }

    @Test
    void instantiateAbstract() {
        Assertions.assertThrows(SaveDataFormatException.class,
                () -> ReflectionUtils.defaultInstance(AbstractTestClass.class)
        );
    }

    @Test
    void instantiateThrower() {
        Assertions.assertThrows(SaveDataFormatException.class,
                () -> ReflectionUtils.defaultInstance(ThrowerTestClass.class)
        );
    }

}