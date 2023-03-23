package fr.emevel.jconfig;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.FIELD})
public @interface Save {
    /**
     * This needs to be set when the field is a collection.
     * We need to know the type of the collection to add elements to it.
     */
    Class<?> type() default Object.class;
}
