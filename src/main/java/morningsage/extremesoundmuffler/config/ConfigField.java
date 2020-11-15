package morningsage.extremesoundmuffler.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigField {
    /**
     * This the category of the config
     */
    String category() default "config";

    /**
     * This is the key for the config, the default is the field name.
     */
    String key() default "";

    /**
     * This is a comment that will be supplied along with the config, use this to explain what the config does
     */
    String comment() default "";

    /**
     * This is the config file name, the default is just config.json, use this is you wish to split the config into more than one file.
     */
    String config() default "config";
}
