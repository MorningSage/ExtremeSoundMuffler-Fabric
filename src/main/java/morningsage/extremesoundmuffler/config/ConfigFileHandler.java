package morningsage.extremesoundmuffler.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * When passed a class object, its static
 * members can be loaded and saved to a file
 */
public class ConfigFileHandler {

    /**
     * Converts objects to and from JSON
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Reference to the class that is functioning as the config
     */
    private final Class clazz;
    /**
     * The name of the file to load/save the config to
     */
    private final String modId;

    /**
     * @param clazz The class that holds the annotated static config options
     * @param modId The name of the config file locally
     */
    public ConfigFileHandler(Class clazz, String modId) {
        this.clazz = clazz;
        this.modId = modId;

        setup();
    }

    private void setup() {
        final File configDir = new File(FabricLoader.getInstance().getConfigDirectory(), modId);

        if (!configDir.exists() && !configDir.mkdirs()) return;

        final File[] configFiles = configDir.listFiles();
        if (configFiles != null) {
            final HashMap<String, JsonObject> configs = new HashMap<>();
            for (File file : configFiles) {
                final String name = file.getName().substring(0, file.getName().length() - (".json".length()));
                try {
                    final String fileContents = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                    final JsonObject jsonObject = GSON.fromJson(fileContents, JsonObject.class);
                    configs.put(name, jsonObject);
                } catch (IOException e) {
                    System.err.println("Failed to read config file: " + file.getAbsolutePath());
                    e.printStackTrace();
                }
            }
            readFromJson(configs);
        }

        saveConfigs();
    }

    public boolean saveConfigs() {
        final File configDir = new File(FabricLoader.getInstance().getConfigDirectory(), modId);

        try {
            //Save the configs
            for (Map.Entry<String, JsonObject> entry : toJson().entrySet()) {
                final File configFile = new File(configDir, entry.getKey() + ".json");
                final String jsonStr = GSON.toJson(entry.getValue());
                try {
                    FileUtils.writeStringToFile(configFile, jsonStr, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to write config file: " + configFile.getAbsolutePath(), e);
                }
            }

            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public HashMap<Field, ConfigField> getConfigFields() {
        final HashMap<Field, ConfigField> fieldMap = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(ConfigField.class)) {
                continue;
            }
            if (!Modifier.isStatic(field.getModifiers())) {
                throw new UnsupportedOperationException("Config field must be static");
            }
            ConfigField annotation = field.getAnnotation(ConfigField.class);
            fieldMap.put(field, annotation);
        }
        return fieldMap;
    }

    public HashMap<String, JsonObject> toJson() {
        final HashMap<Field, ConfigField> fieldMap = getConfigFields();
        final HashMap<String, JsonObject> configs = new HashMap<>();

        for (Map.Entry<Field, ConfigField> entry : fieldMap.entrySet()) {
            Field field = entry.getKey();
            ConfigField annotation = entry.getValue();

            final JsonObject config = configs.computeIfAbsent(annotation.config(), s -> new JsonObject());

            JsonObject categoryObject;
            if (config.has(annotation.category())) {
                categoryObject = config.getAsJsonObject(annotation.category());
            } else {
                categoryObject = new JsonObject();
                config.add(annotation.category(), categoryObject);
            }

            String key = annotation.key().isEmpty() ? field.getName() : annotation.key();
            if (categoryObject.has(key)) {
                throw new UnsupportedOperationException("Some bad happened, duplicate key found: " + key);
            }

            JsonObject fieldObject = new JsonObject();
            fieldObject.addProperty("comment", annotation.comment());

            Object value;
            try {
                value = field.get(null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            JsonElement jsonElement = GSON.toJsonTree(value);
            fieldObject.add("value", jsonElement);

            categoryObject.add(key, fieldObject);
        }

        return configs;
    }

    public void readFromJson(HashMap<String, JsonObject> configs) {
        final HashMap<Field, ConfigField> fieldMap = getConfigFields();

        for (Map.Entry<Field, ConfigField> entry : fieldMap.entrySet()) {
            Field field = entry.getKey();
            ConfigField annotation = entry.getValue();

            final JsonObject config = configs.get(annotation.config());

            if (config == null) {
                continue; //Could be possible if a new config is added
            }

            JsonObject categoryObject = config.getAsJsonObject(annotation.category());
            if (categoryObject == null) {
                continue;
            }

            String key = annotation.key().isEmpty() ? field.getName() : annotation.key();
            if (!categoryObject.has(key)) {
                continue;
            }

            JsonObject fieldObject = categoryObject.get(key).getAsJsonObject();
            if (!fieldObject.has("value")) {
                continue;
            }
            JsonElement jsonValue = fieldObject.get("value");
            Class<?> fieldType = field.getType();

            Object fieldValue = GSON.fromJson(jsonValue, fieldType);

            try {
                field.set(null, fieldValue);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to set field value", e);
            }
        }
    }

}


