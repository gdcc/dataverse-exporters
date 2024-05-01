package io.gdcc.spi.export.croissant;

import io.gdcc.spi.export.ExportDataProvider;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.skyscreamer.jsonassert.JSONAssert;

public class CroissantExporterTest {

    static CroissantExporter exporter;
    static OutputStream outputStreamMinimal;
    static ExportDataProvider dataProviderMinimal;
    static OutputStream outputStreamMax;
    static ExportDataProvider dataProviderMax;
    static OutputStream outputStreamCars;
    static ExportDataProvider dataProviderCars;

    @BeforeAll
    public static void setUp() {
        exporter = new CroissantExporter();

        outputStreamMinimal = new ByteArrayOutputStream();
        dataProviderMinimal = new ExportDataProvider() {
            @Override
            public JsonObject getDatasetJson() {
                String pathToJsonFile = "src/test/resources/minimal/datasetJson.json";
                try (JsonReader jsonReader = Json.createReader(new FileReader(pathToJsonFile))) {
                    return jsonReader.readObject();
                } catch (FileNotFoundException ex) {
                    return null;
                }
            }

            @Override
            public JsonObject getDatasetORE() {
                String pathToJsonFile = "src/test/resources/minimal/datasetORE.json";
                try (JsonReader jsonReader = Json.createReader(new FileReader(pathToJsonFile))) {
                    return jsonReader.readObject();
                } catch (FileNotFoundException ex) {
                    return null;
                }
            }

            @Override
            public JsonArray getDatasetFileDetails() {
                String pathToJsonFile = "src/test/resources/minimal/datasetFileDetails.json";
                try (JsonReader jsonReader = Json.createReader(new FileReader(pathToJsonFile))) {
                    return jsonReader.readArray();
                } catch (FileNotFoundException ex) {
                    return null;
                }
            }

            @Override
            public JsonObject getDatasetSchemaDotOrg() {
                String pathToJsonFile = "src/test/resources/minimal/datasetSchemaDotOrg.json";
                try (JsonReader jsonReader = Json.createReader(new FileReader(pathToJsonFile))) {
                    return jsonReader.readObject();
                } catch (FileNotFoundException ex) {
                    return null;
                }
            }

            @Override
            public String getDataCiteXml() {
                try {
                    return Files.readString(Paths.get("src/test/resources/minimal/dataCiteXml.xml"), StandardCharsets.UTF_8);
                } catch (IOException ex) {
                    return null;
                }
            }
        };

        outputStreamMax = new ByteArrayOutputStream();
        dataProviderMax = new ExportDataProvider() {
            @Override
            public JsonObject getDatasetJson() {
                String pathToJsonFile = "src/test/resources/max/datasetJson.json";
                try (JsonReader jsonReader = Json.createReader(new FileReader(pathToJsonFile))) {
                    return jsonReader.readObject();
                } catch (FileNotFoundException ex) {
                    return null;
                }
            }

            @Override
            public JsonObject getDatasetORE() {
                String pathToJsonFile = "src/test/resources/max/datasetORE.json";
                try (JsonReader jsonReader = Json.createReader(new FileReader(pathToJsonFile))) {
                    return jsonReader.readObject();
                } catch (FileNotFoundException ex) {
                    return null;
                }
            }

            @Override
            public JsonArray getDatasetFileDetails() {
                String pathToJsonFile = "src/test/resources/max/datasetFileDetails.json";
                try (JsonReader jsonReader = Json.createReader(new FileReader(pathToJsonFile))) {
                    return jsonReader.readArray();
                } catch (FileNotFoundException ex) {
                    return null;
                }
            }

            @Override
            public JsonObject getDatasetSchemaDotOrg() {
                String pathToJsonFile = "src/test/resources/max/datasetSchemaDotOrg.json";
                try (JsonReader jsonReader = Json.createReader(new FileReader(pathToJsonFile))) {
                    return jsonReader.readObject();
                } catch (FileNotFoundException ex) {
                    return null;
                }
            }

            @Override
            public String getDataCiteXml() {
                try {
                    return Files.readString(Paths.get("src/test/resources/max/dataCiteXml.xml"), StandardCharsets.UTF_8);
                } catch (IOException ex) {
                    return null;
                }
            }
        };

        outputStreamCars = new ByteArrayOutputStream();
        dataProviderCars = new ExportDataProvider() {
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
    public void testExportDatasetMinimal() throws Exception {
        exporter.exportDataset(dataProviderMinimal, outputStreamMinimal);
        String expected = """
{
    "@context": {
        "@language": "en",
        "@vocab": "https://schema.org/",
        "citeAs": "cr:citeAs",
        "column": "cr:column",
        "conformsTo": "dct:conformsTo",
        "cr": "http://mlcommons.org/croissant/",
        "rai": "http://mlcommons.org/croissant/RAI/",
        "data": {
            "@id": "cr:data",
            "@type": "@json"
        },
        "dataType": {
            "@id": "cr:dataType",
            "@type": "@vocab"
        },
        "dct": "http://purl.org/dc/terms/",
        "examples": {
            "@id": "cr:examples",
            "@type": "@json"
        },
        "extract": "cr:extract",
        "field": "cr:field",
        "fileProperty": "cr:fileProperty",
        "fileObject": "cr:fileObject",
        "fileSet": "cr:fileSet",
        "format": "cr:format",
        "includes": "cr:includes",
        "isLiveDataset": "cr:isLiveDataset",
        "jsonPath": "cr:jsonPath",
        "key": "cr:key",
        "md5": "cr:md5",
        "parentField": "cr:parentField",
        "path": "cr:path",
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
    "name": "Minimal",
    "version": "1.0.0",
    "citeAs": "@data{FK2/4C0JYC_2024,author = {Durbin, Philip},publisher = {Root},title = {Minimal},year = {2024},url = {https://doi.org/10.5072/FK2/4C0JYC}}",
    "distribution": [
    ],
    "recordSet": [
    ],
    "creator": [
        {
            "@type": "Person",
            "givenName": "Philip",
            "familyName": "Durbin",
            "name": "Durbin, Philip"
        }
    ],
    "keywords": [
        "Other"
    ],
    "license": "http://creativecommons.org/publicdomain/zero/1.0",
    "datePublished": "2024-05-01",
    "dateModified": "2024-05-01",
    "includedInDataCatalog": {
        "@type": "DataCatalog",
        "name": "Root",
        "url": "https://beta.dataverse.org"
    },
    "publisher": {
        "@type": "Organization",
        "name": "Root"
    }
}
""";
        String actual = outputStreamMinimal.toString();
        Files.writeString(Paths.get("src/test/resources/minimal/croissant.json"), prettyPrint(actual), StandardCharsets.UTF_8);
        JSONAssert.assertEquals(expected, actual, true);
        assertEquals(prettyPrint(expected), prettyPrint(outputStreamMinimal.toString()));

    }

    @Test
    public void testExportDatasetMax() throws Exception {
        exporter.exportDataset(dataProviderMax, outputStreamMax);
        String expected = """
{
    "@context": {
        "@language": "en",
        "@vocab": "https://schema.org/",
        "citeAs": "cr:citeAs",
        "column": "cr:column",
        "conformsTo": "dct:conformsTo",
        "cr": "http://mlcommons.org/croissant/",
        "rai": "http://mlcommons.org/croissant/RAI/",
        "data": {
            "@id": "cr:data",
            "@type": "@json"
        },
        "dataType": {
            "@id": "cr:dataType",
            "@type": "@vocab"
        },
        "dct": "http://purl.org/dc/terms/",
        "examples": {
            "@id": "cr:examples",
            "@type": "@json"
        },
        "extract": "cr:extract",
        "field": "cr:field",
        "fileProperty": "cr:fileProperty",
        "fileObject": "cr:fileObject",
        "fileSet": "cr:fileSet",
        "format": "cr:format",
        "includes": "cr:includes",
        "isLiveDataset": "cr:isLiveDataset",
        "jsonPath": "cr:jsonPath",
        "key": "cr:key",
        "md5": "cr:md5",
        "parentField": "cr:parentField",
        "path": "cr:path",
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
    "name": "Max Schema.org",
    "version": "1.0.0",
    "citeAs": "@data{FK2/VQTYHD_2024,author = {Durbin, Philip and IQSS},publisher = {Root},title = {Max Schema.org},year = {2024},url = {https://doi.org/10.5072/FK2/VQTYHD}}",
    "funder": [
        {
            "@type": "Organization",
            "name": "NSF"
        },
        {
            "@type": "Organization",
            "name": "NIH"
        }
    ],
    "spatialCoverage": [
            "Cambridge, MA, United States, Harvard Square"
    ],
    "distribution": [
        {
            "@type": "cr:FileObject",
            "@id": "README.md",
            "name": "README.md",
            "encodingFormat": "text/markdown",
            "md5": "ebf050ec8cce5df0a72b100cfc9f442f",
            "contentSize": "34 B",
            "contentUrl": "https://beta.dataverse.org/api/access/datafile/26148"
        }
    ],
    "recordSet": [
    ],
    "creator": [
        {
            "@type": "Person",
            "givenName": "Philip",
            "familyName": "Durbin",
            "affiliation": {
                "@type": "Organization",
                "name": "Harvard University"
            },
            "sameAs": "https://orcid.org/0000-0002-9528-9470",
            "@id": "https://orcid.org/0000-0002-9528-9470",
            "identifier": "https://orcid.org/0000-0002-9528-9470",
            "name": "Durbin, Philip"
        },
        {
            "@type": "Person",
            "affiliation": {
                "@type": "Organization",
                "name": "Harvard University"
            },
            "name": "IQSS"
        }
    ],
    "keywords": [
        "Social Sciences",
        "Other",
        "foo",
        "bar"
    ],
    "license": "http://creativecommons.org/publicdomain/zero/1.0",
    "datePublished": "2024-05-01",
    "dateModified": "2024-05-01",
    "includedInDataCatalog": {
        "@type": "DataCatalog",
        "name": "Root",
        "url": "https://beta.dataverse.org"
    },
    "publisher": {
        "@type": "Organization",
        "name": "Root"
    }
}
""";
        String actual = outputStreamMax.toString();
        Files.writeString(Paths.get("src/test/resources/max/croissant.json"), prettyPrint(actual), StandardCharsets.UTF_8);
        JSONAssert.assertEquals(expected, actual, true);
        assertEquals(prettyPrint(expected), prettyPrint(outputStreamMax.toString()));

    }

    @Test
    public void testExportDatasetCars() throws Exception {
        exporter.exportDataset(dataProviderCars, outputStreamCars);
        String expected = """
            {
                "@context": {
                    "@language": "en",
                    "@vocab": "https://schema.org/",
                    "citeAs": "cr:citeAs",
                    "column": "cr:column",
                    "conformsTo": "dct:conformsTo",
                    "cr": "http://mlcommons.org/croissant/",
                    "rai": "http://mlcommons.org/croissant/RAI/",
                    "data": {
                      "@id": "cr:data",
                      "@type": "@json"
                    },
                    "dataType": {
                      "@id": "cr:dataType",
                      "@type": "@vocab"
                    },
                    "dct": "http://purl.org/dc/terms/",
                    "examples": {
                      "@id": "cr:examples",
                      "@type": "@json"
                    },
                    "extract": "cr:extract",
                    "field": "cr:field",
                    "fileProperty": "cr:fileProperty",
                    "fileObject": "cr:fileObject",
                    "fileSet": "cr:fileSet",
                    "format": "cr:format",
                    "includes": "cr:includes",
                    "isLiveDataset": "cr:isLiveDataset",
                    "jsonPath": "cr:jsonPath",
                    "key": "cr:key",
                    "md5": "cr:md5",
                    "parentField": "cr:parentField",
                    "path": "cr:path",
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
                "version": "1.0.0",
                "citeAs": "@data{FK2/6ZUDGC_2024,author = {Durbin, Philip},publisher = {Root},title = {Cars},year = {2024},url = {https://doi.org/10.5072/FK2/6ZUDGC}}",
                "distribution": [
                    {
                        "@type": "cr:FileObject",
                        "@id": "compute.py",
                        "name": "compute.py",
                        "encodingFormat": "text/x-python",
                        "md5": "d84985e94dde671f318076bd7a137f15",
                        "contentSize": "15 B",
                        "contentUrl": "http://localhost:8080/api/access/datafile/4"
                    },
                    {
                        "@type": "cr:FileObject",
                        "@id": "README.md",
                        "name": "README.md",
                        "encodingFormat": "text/markdown",
                        "md5": "a2e484d07ee5590cc32182dc2c6ccc83",
                        "contentSize": "28 B",
                        "contentUrl": "http://localhost:8080/api/access/datafile/5"
                    },
                    {
                        "@type": "cr:FileObject",
                        "@id": "stata13-auto.dta",
                        "name": "stata13-auto.dta",
                        "encodingFormat": "application/x-stata-13",
                        "md5": "7b1201ce6b469796837a835377338c5a",
                        "contentSize": "6443 B",
                        "contentUrl": "http://localhost:8080/api/access/datafile/6?format=original"
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
                            "@id": "11",
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
                            "@id": "5",
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
                            "@id": "6",
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
                            "@id": "4",
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
                            "@id": "7",
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
                            "@id": "9",
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
                            "@id": "10",
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
                            "@id": "3",
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
                            "@id": "2",
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
                            "@id": "1",
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
                            "@id": "8",
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
                            "@id": "12",
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
                        "givenName": "Philip",
                        "familyName": "Durbin",
                        "affiliation": {
                            "@type": "Organization",
                            "name": "Harvard"
                        },
                        "name": "Durbin, Philip"
                    }
                ],
                "keywords": [
                    "Other"
                ],
                "license": "http://creativecommons.org/publicdomain/zero/1.0",
                "datePublished": "2024-03-20",
                "dateModified": "2024-03-20",
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
        String actual = outputStreamCars.toString();
        Files.writeString(Paths.get("src/test/resources/cars/croissant.json"), prettyPrint(actual), StandardCharsets.UTF_8);
        JSONAssert.assertEquals(expected, actual, true);
        assertEquals(prettyPrint(expected), prettyPrint(outputStreamCars.toString()));

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
