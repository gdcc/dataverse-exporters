package io.gdcc.export.helloworld;

import io.gdcc.spi.export.ExportDataProvider;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class HelloWorldExporterTest {

    static HelloWorldExporter helloWorldExporter;
    static OutputStream outputStream;
    static ExportDataProvider dataProvider;

    @BeforeAll
    public static void setUp() {
        helloWorldExporter = new HelloWorldExporter();
        outputStream = new ByteArrayOutputStream();
        dataProvider = new ExportDataProvider() {
            @Override
            public JsonObject getDatasetJson() {
                return Json.createObjectBuilder()
                        .add("foo", "bar")
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

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testGetFormatName() {
        assertEquals("dataverse_json", helloWorldExporter.getFormatName());
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("My JSON in en", helloWorldExporter.getDisplayName(new Locale("en", "US")));
    }

    @Test
    public void testIsHarvestable() {
        assertEquals(false, helloWorldExporter.isHarvestable());
    }

    @Test
    public void testIsAvailableToUsers() {
        assertEquals(true, helloWorldExporter.isAvailableToUsers());
    }

    @Test
    public void testGetMediaType() {
        assertEquals("application/json", helloWorldExporter.getMediaType());
    }

    @Test
    public void testExportDataset() throws Exception {
        helloWorldExporter.exportDataset(dataProvider, outputStream);
        String expected = """
{"name":"dataverse_json","inputJson":{"foo":"bar"}}
""";
        assertEquals(expected.trim(), outputStream.toString().trim());
    }

}
