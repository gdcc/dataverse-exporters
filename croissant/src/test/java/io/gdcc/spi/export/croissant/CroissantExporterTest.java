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
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.skyscreamer.jsonassert.JSONAssert;

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
                String pathToJsonFile = "src/test/resources/cars/datasetJson.json";
                try (JsonReader jsonReader = Json.createReader(new FileReader(pathToJsonFile))) {
                    return jsonReader.readObject();
                } catch (FileNotFoundException ex) {
                    return null;
                }
            }

            @Override
            public JsonObject getDatasetORE() {
                String pathToJsonFile = "src/test/resources/cars/datasetORE.json";
                try (JsonReader jsonReader = Json.createReader(new FileReader(pathToJsonFile))) {
                    return jsonReader.readObject();
                } catch (FileNotFoundException ex) {
                    return null;
                }
            }

            @Override
            public JsonArray getDatasetFileDetails() {
                String pathToJsonFile = "src/test/resources/cars/datasetFileDetails.json";
                try (JsonReader jsonReader = Json.createReader(new FileReader(pathToJsonFile))) {
                    return jsonReader.readArray();
                } catch (FileNotFoundException ex) {
                    return null;
                }
            }

            @Override
            public JsonObject getDatasetSchemaDotOrg() {
                String pathToJsonFile = "src/test/resources/cars/datasetSchemaDotOrg.json";
                try (JsonReader jsonReader = Json.createReader(new FileReader(pathToJsonFile))) {
                    return jsonReader.readObject();
                } catch (FileNotFoundException ex) {
                    return null;
                }
            }

            @Override
            public String getDataCiteXml() {
                try {
                    return Files.readString(Paths.get("src/test/resources/cars/dataCiteXml.xml"), StandardCharsets.UTF_8);
                } catch (IOException ex) {
                    return null;
                }
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
                    "citeAs": "cr:citeAs",
                    "column": "cr:column",
                    "conformsTo": "dct:conformsTo",
                    "cr": "http://mlcommons.org/croissant/",
                    "data": {
                      "@id": "cr:data",
                      "@type": "@json"
                    },
                    "dataBiases": "cr:dataBiases",
                    "dataCollection": "cr:dataCollection",
                    "dataType": {
                      "@id": "cr:dataType",
                      "@type": "@vocab"
                    },
                    "dct": "http://purl.org/dc/terms/",
                    "extract": "cr:extract",
                    "field": "cr:field",
                    "fileProperty": "cr:fileProperty",
                    "fileObject": "cr:fileObject",
                    "fileSet": "cr:fileSet",
                    "format": "cr:format",
                    "includes": "cr:includes",
                    "isEnumeration": "cr:isEnumeration",
                    "jsonPath": "cr:jsonPath",
                    "key": "cr:key",
                    "md5": "cr:md5",
                    "parentField": "cr:parentField",
                    "path": "cr:path",
                    "personalSensitiveInformation": "cr:personalSensitiveInformation",
                    "recordSet": "cr:recordSet",
                    "references": "cr:references",
                    "regex": "cr:regex",
                    "repeated": "cr:repeated",
                    "replace": "cr:replace",
                    "sc": "https://schema.org/",
                    "separator": "cr:separator",
                    "source": "cr:source",
                    "subField": "cr:subField",
                    "transform": "cr:transform",
                    "wd": "https://www.wikidata.org/wiki/"
                },
                "@type": "sc:Dataset",
                "conformsTo": "http://mlcommons.org/croissant/1.0",
                "name": "Cars",
                "version": "1.0",
                "citeAs": "https://doi.org/10.5072/FK2/EKY1NP",
                "distribution": [
                    {
                        "@type": "cr:FileObject",
                        "@id": "stata13-auto.dta",
                        "name": "stata13-auto.dta",
                        "encodingFormat": "application/x-stata-13",
                        "md5": "7b1201ce6b469796837a835377338c5a",
                        "sha256": "see md5",
                        "contentUrl": "stata13-auto.dta"
                    }
                ],
                "recordSet": [
                    {
                      "@type": "cr:RecordSet",
                      "field": [
                        {
                          "@type": "cr:Field",
                          "name": "make",
                          "description": "Make and Model",
                          "dataType": "sc:Text",
                          "source": {
                            "@id": "105",
                            "fileObject": {
                              "@id": "stata13-auto.dta"
                            }
                          }
                        }
                      ]
                    },
                    {
                      "@type": "cr:RecordSet",
                      "field": [
                        {
                          "@type": "cr:Field",
                          "name": "price",
                          "description": "Price",
                          "dataType": "sc:Integer",
                          "source": {
                            "@id": "102",
                            "fileObject": {
                              "@id": "stata13-auto.dta"
                            }
                          }
                        }
                      ]
                    },
                    {
                      "@type": "cr:RecordSet",
                      "field": [
                        {
                          "@type": "cr:Field",
                          "name": "mpg",
                          "description": "Mileage (mpg)",
                          "dataType": "sc:Integer",
                          "source": {
                            "@id": "103",
                            "fileObject": {
                              "@id": "stata13-auto.dta"
                            }
                          }
                        }
                      ]
                    },
                    {
                      "@type": "cr:RecordSet",
                      "field": [
                        {
                          "@type": "cr:Field",
                          "name": "rep78",
                          "description": "Repair Record 1978",
                          "dataType": "sc:Integer",
                          "source": {
                            "@id": "107",
                            "fileObject": {
                              "@id": "stata13-auto.dta"
                            }
                          }
                        }
                      ]
                    },
                    {
                      "@type": "cr:RecordSet",
                      "field": [
                        {
                          "@type": "cr:Field",
                          "name": "headroom",
                          "description": "Headroom (in.)",
                          "dataType": "sc:Integer",
                          "source": {
                            "@id": "101",
                            "fileObject": {
                              "@id": "stata13-auto.dta"
                            }
                          }
                        }
                      ]
                    },
                    {
                      "@type": "cr:RecordSet",
                      "field": [
                        {
                          "@type": "cr:Field",
                          "name": "trunk",
                          "description": "Trunk space (cu. ft.)",
                          "dataType": "sc:Integer",
                          "source": {
                            "@id": "106",
                            "fileObject": {
                              "@id": "stata13-auto.dta"
                            }
                          }
                        }
                      ]
                    },
                    {
                      "@type": "cr:RecordSet",
                      "field": [
                        {
                          "@type": "cr:Field",
                          "name": "weight",
                          "description": "Weight (lbs.)",
                          "dataType": "sc:Integer",
                          "source": {
                            "@id": "99",
                            "fileObject": {
                              "@id": "stata13-auto.dta"
                            }
                          }
                        }
                      ]
                    },
                    {
                      "@type": "cr:RecordSet",
                      "field": [
                        {
                          "@type": "cr:Field",
                          "name": "length",
                          "description": "Length (in.)",
                          "dataType": "sc:Integer",
                          "source": {
                            "@id": "96",
                            "fileObject": {
                              "@id": "stata13-auto.dta"
                            }
                          }
                        }
                      ]
                    },
                    {
                      "@type": "cr:RecordSet",
                      "field": [
                        {
                          "@type": "cr:Field",
                          "name": "turn",
                          "description": "Turn Circle (ft.) ",
                          "dataType": "sc:Integer",
                          "source": {
                            "@id": "104",
                            "fileObject": {
                              "@id": "stata13-auto.dta"
                            }
                          }
                        }
                      ]
                    },
                    {
                      "@type": "cr:RecordSet",
                      "field": [
                        {
                          "@type": "cr:Field",
                          "name": "displacement",
                          "description": "Displacement (cu. in.)",
                          "dataType": "sc:Integer",
                          "source": {
                            "@id": "97",
                            "fileObject": {
                              "@id": "stata13-auto.dta"
                            }
                          }
                        }
                      ]
                    },
                    {
                      "@type": "cr:RecordSet",
                      "field": [
                        {
                          "@type": "cr:Field",
                          "name": "gear_ratio",
                          "description": "Gear Ratio",
                          "dataType": "sc:Integer",
                          "source": {
                            "@id": "100",
                            "fileObject": {
                              "@id": "stata13-auto.dta"
                            }
                          }
                        }
                      ]
                    },
                    {
                      "@type": "cr:RecordSet",
                      "field": [
                        {
                          "@type": "cr:Field",
                          "name": "foreign",
                          "description": "Car type",
                          "dataType": "sc:Integer",
                          "source": {
                            "@id": "98",
                            "fileObject": {
                              "@id": "stata13-auto.dta"
                            }
                          }
                        }
                      ]
                    }
                  ],
                "creator": [
                    {
                        "@type": "Person",
                        "givenName": "Dataverse",
                        "familyName": "Admin",
                        "affiliation": {
                            "@type": "Organization",
                            "name": "Dataverse.org"
                        },
                        "name": "Admin, Dataverse"
                    }
                ],
                "keywords": [
                    "Other"
                ],
                "license": "http://creativecommons.org/publicdomain/zero/1.0",
                "datePublished": "2024-03-13",
                "dateModified": "2024-03-13",
                "includedInDataCatalog": {
                    "@type": "DataCatalog",
                    "name": "Root",
                    "url": "http://localhost:8080"
                },                 
                "publisher": {
                    "@type": "Organization",
                    "name": "Root"
                }
            }
            """;
        String actual = outputStream.toString();
        Files.writeString(Paths.get("src/test/resources/cars/croissant.json"), prettyPrint(actual), StandardCharsets.UTF_8);
        JSONAssert.assertEquals(expected, actual, true);
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
