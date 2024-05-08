package io.gdcc.spi.export.examples.generic;

import io.gdcc.spi.export.ExportDataProvider;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GenericExporterTest {

    static GenericExporter genericExporter;
    static OutputStream outputStream;
    static ExportDataProvider dataProvider;

    public static JsonObject parse(final String fileName) throws FileNotFoundException, URISyntaxException {
        final String f = GenericExporterTest.class.getClassLoader().getResource(fileName).getFile();
        final JsonReader jsonReader = Json.createReader(new FileReader(f));
        final JsonObject object = jsonReader.readObject();
        jsonReader.close();
        return object;
    }

    @BeforeAll
    public static void setUp() {
        genericExporter = new GenericExporter();
        outputStream = new ByteArrayOutputStream();
        dataProvider = new ExportDataProvider() {
            @Override
            public JsonObject getDatasetJson() {
                try {
                    return parse("source.json");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
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
        assertEquals("transformer_json", genericExporter.getFormatName());
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Generic transformer example", genericExporter.getDisplayName(new Locale("en", "US")));
    }

    @Test
    public void testIsHarvestable() {
        assertEquals(false, genericExporter.isHarvestable());
    }

    @Test
    public void testIsAvailableToUsers() {
        assertEquals(true, genericExporter.isAvailableToUsers());
    }

    @Test
    public void testGetMediaType() {
        assertEquals("application/json", genericExporter.getMediaType());
    }

    @Test
    public void testExportDataset() throws Exception {
        genericExporter.exportDataset(dataProvider, outputStream);
        String expected = parse("result.json").toString();
        assertEquals(expected.trim(), outputStream.toString().trim());
    }

}
