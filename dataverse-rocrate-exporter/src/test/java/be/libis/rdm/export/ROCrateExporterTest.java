package be.libis.rdm.export;

import io.gdcc.spi.export.ExportDataProvider;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

import java.io.StringReader;
import be.libis.rdm.export.ROCrate.*;

public class ROCrateExporterTest {

    static ROCrateExporter roCrateExporter;
    static OutputStream outputStream;
    static ExportDataProvider dataProvider;

    @BeforeAll
    public static void setUp() {
        roCrateExporter = new ROCrateExporter();
        roCrateExporter.setCsvPath("./dataverse2ro-crate.csv");
        outputStream = new ByteArrayOutputStream();
        dataProvider = new ExportDataProvider() {
            @Override
            public JsonObject getDatasetJson() {
                InputStream is = null;
                try {
                    is = new FileInputStream("./src/test/resources/testDataset/datasetJson.json");
                } catch (Exception e) {
                    System.err.println("Test dataset file not found.");

                }

                JsonReader jsonReader = Json.createReader(is);
                return (jsonReader.readObject());
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
        assertEquals("rocrate_json", roCrateExporter.getFormatName());
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("RO-Crate", roCrateExporter.getDisplayName(new Locale("en", "US")));
    }

    @Test
    public void testIsHarvestable() {
        assertEquals(false, roCrateExporter.isHarvestable());
    }

    @Test
    public void testIsAvailableToUsers() {
        assertEquals(true, roCrateExporter.isAvailableToUsers());
    }

    @Test
    public void testGetMediaType() {
        assertEquals("application/json", roCrateExporter.getMediaType());
    }

    static String prettifyJson(JsonObject jsonObject) {

        StringWriter stringWriter = new StringWriter();
        Map<String, Object> properties = Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true);
        JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
        JsonWriter jsonWriter = writerFactory.createWriter(stringWriter);

        jsonWriter.write(jsonObject);
        jsonWriter.close();

        String prettyJson = stringWriter.toString();
        return prettyJson;

    }

    @Test
    public void testExportDataset() throws Exception {
        roCrateExporter.exportDataset(dataProvider, outputStream);
        // String fromExporter = outputStream.toString();

        JsonReader exportReader = Json.createReader(new StringReader(outputStream.toString().trim()));
        JsonArray fromExporter = exportReader.readObject().getJsonArray("graph");
        ;

        InputStream is = null;
        try {
            is = new FileInputStream("./src/test/resources/testDataset/ro-crate-metadata.json");
        } catch (Exception e) {
            System.err.println("File not found.");
        }

        JsonReader jsonReader = Json.createReader(is);
        JsonArray expected = jsonReader.readObject().getJsonArray("graph");
        assertEquals(expected.size(), fromExporter.size());
        for (JsonValue object : expected) {
            assert (fromExporter.contains(object));
        }

    }

}
