package utils.jsonschema;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface JsonSchema {
    /**
     * The path to the schema definition.
     */
    String value();
}
