package io.gdcc.spi.export.examples.transformer;

import java.io.StringReader;
import java.util.Locale;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.ws.rs.core.MediaType;

public class Config {
    private static final String formatName = "formatName";
    private static final String displayName = "displayName";
    private static final String harvestable = "harvestable";
    private static final String availableToUsers = "availableToUsers";
    private static final String mediaType = "mediaType";

    private final JsonObject object;

    public Config(final String jsonString) {
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonString))) {
            final JsonObject object = jsonReader.readObject();
            this.object = object;
        }
    }

    /*
     * The name of the format it creates. If this format is already provided by a
     * built-in exporter, this Exporter will override the built-in one. (Note that
     * exports are cached, so existing metadata export files are not updated
     * immediately.)
     */
    public String getFormatName() {
        return object.containsKey(formatName) ? object.getString(formatName) : "transformer_json";
    }

    // The display name shown in the UI
    public String getDisplayName(final Locale locale) {
        String field = displayName;
        if (locale != null) {
            if (object.containsKey(field + "_" + locale.toLanguageTag())) {
                field = field + "_" + locale.toLanguageTag();
            } else if (object.containsKey(field + "_" + locale.getLanguage())) {
                field = field + "_" + locale.getLanguage();
            }
        }
        return object.containsKey(field) ? object.getString(field) : "Transformer example";
    }

    // Whether the exported format should be available as an option for Harvesting
    public Boolean isHarvestable() {
        return object.containsKey(harvestable) ? object.getBoolean(harvestable) : false;
    }

    // Whether the exported format should be available for download in the UI and
    // API
    public Boolean isAvailableToUsers() {
        return object.containsKey(availableToUsers) ? object.getBoolean(availableToUsers) : true;
    }

    // Defines the mime type of the exported format - used when metadata is
    // downloaded, i.e. to trigger an appropriate viewer in the user's browser.
    public String getMediaType() {
        return object.containsKey(mediaType) ? object.getString(mediaType) : MediaType.APPLICATION_JSON;
    }
}
