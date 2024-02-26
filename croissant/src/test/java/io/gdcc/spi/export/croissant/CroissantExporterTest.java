package io.gdcc.spi.export.croissant;

import io.gdcc.spi.export.ExportDataProvider;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CroissantExporterTest {

    static CroissantExporter exporter;
    static OutputStream outputStream;
    static ExportDataProvider dataProvider;

    @BeforeAll
    public static void setUp() {
        exporter = new CroissantExporter();
        outputStream = new ByteArrayOutputStream();
        dataProvider = new ExportDataProvider() {
            @Override
            public JsonObject getDatasetJson() {
                JsonObjectBuilder job = Json.createObjectBuilder();
                String pathToJsonFile = "src/test/resources/earthquakes.json";

                try (JsonReader jsonReader = Json.createReader(new FileReader(pathToJsonFile))) {
                    JsonObject jsonObject = jsonReader.readObject();
                    // FIXME: just a proof of concept to get the dataset title
                    job.add("name", jsonObject
                            .getJsonObject("datasetVersion")
                            .getJsonObject("metadataBlocks")
                            .getJsonObject("citation")
                            .getJsonArray("fields")
                            .getJsonObject(0)
                            .getString("value")
                    );
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(CroissantExporterTest.class.getName()).log(Level.SEVERE, null, ex);
                }

                return Json.createObjectBuilder()
                        .addAll(job)
                        .build();
            }

            @Override
            public JsonObject getDatasetORE() {
                return Json.createObjectBuilder().build();
            }

            @Override
            public JsonArray getDatasetFileDetails() {
                return Json.createArrayBuilder().build();
            }

            @Override
            public JsonObject getDatasetSchemaDotOrg() {
                return Json.createObjectBuilder().build();
            }

            @Override
            public String getDataCiteXml() {
                return null;
            }
        };
    }

    @Test
    public void testGetFormatName() {
        CroissantExporter instance = new CroissantExporter();
        String expResult = "";
        String result = instance.getFormatName();
        assertEquals("croissant", result);
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Croissant", exporter.getDisplayName(null));
    }

    @Test
    public void testIsHarvestable() {
        assertEquals(false, exporter.isHarvestable());
    }

    @Test
    public void testIsAvailableToUsers() {
        assertEquals(true, exporter.isAvailableToUsers());
    }

    @Test
    public void testGetMediaType() {
        assertEquals("application/json", exporter.getMediaType());
    }

    @Test
    public void testExportDataset() throws Exception {
        exporter.exportDataset(dataProvider, outputStream);
        String expected = """
            {
                "@context": {
                    "@language": "en",
                    "@vocab": "https://schema.org/",
                    "column": "ml:column",
                    "data": {
                      "@id": "ml:data",
                      "@type": "@json"
                    },
                    "dataType": {
                      "@id": "ml:dataType",
                      "@type": "@vocab"
                    },
                    "extract": "ml:extract",
                    "field": "ml:field",
                    "fileProperty": "ml:fileProperty",
                    "format": "ml:format",
                    "includes": "ml:includes",
                    "isEnumeration": "ml:isEnumeration",
                    "jsonPath": "ml:jsonPath",
                    "ml": "http://mlcommons.org/schema/",
                    "parentField": "ml:parentField",
                    "path": "ml:path",
                    "recordSet": "ml:recordSet",
                    "references": "ml:references",
                    "regex": "ml:regex",
                    "repeated": "ml:repeated",
                    "replace": "ml:replace",
                    "sc": "https://schema.org/",
                    "separator": "ml:separator",
                    "source": "ml:source",
                    "subField": "ml:subField",
                    "transform": "ml:transform"
                },
                "name": "Replication Data for: initial study on predicting earthquakes using machine learning"
            }
            """;
        assertEquals(prettyPrint(expected), prettyPrint(outputStream.toString()));

    }

    public static String prettyPrint(String jsonObject) {
        try {
            return prettyPrint(getJsonObject(jsonObject));
        } catch (Exception ex) {
            return jsonObject;
        }
    }

    public static String prettyPrint(JsonObject jsonObject) {
        Map<String, Boolean> config = new HashMap<>();
        config.put(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriterFactory jsonWriterFactory = Json.createWriterFactory(config);
        StringWriter stringWriter = new StringWriter();
        try (JsonWriter jsonWriter = jsonWriterFactory.createWriter(stringWriter)) {
            jsonWriter.writeObject(jsonObject);
        }
        return stringWriter.toString();
    }

    public static JsonObject getJsonObject(String serializedJson) {
        try (StringReader rdr = new StringReader(serializedJson)) {
            try (JsonReader jsonReader = Json.createReader(rdr)) {
                return jsonReader.readObject();
            }
        }
    }

}
