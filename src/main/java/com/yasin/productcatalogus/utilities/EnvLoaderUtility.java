package com.yasin.productcatalogus.utilities;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;

/**
 * Utility class for loading environment variables from a .env file into system properties.
 */
public class EnvLoaderUtility {

    private final Dotenv dotenv;

    public EnvLoaderUtility() {
        dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .ignoreIfMalformed()
                .load();
    }

    /**
     * Load all environment variables from .env into system properties dynamically.
     * Existing system properties are not overwritten.
     */
    public void loadIntoSystemProperties() {
        for (DotenvEntry entry : dotenv.entries()) {
            if (System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Get a specific environment variable by key.
     *
     * @param key the name of the environment variable
     * @return the value of the variable or null if not present
     */
    public String get(String key) {
        return dotenv.get(key);
    }
}
