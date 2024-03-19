package io.gdcc.spi.export.croissant;

import com.google.auto.service.AutoService;
import io.gdcc.spi.export.ExportDataProvider;
import io.gdcc.spi.export.ExportException;
import io.gdcc.spi.export.Exporter;
import java.io.OutputStream;
import java.util.Locale;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.ws.rs.core.MediaType;
import java.io.StringReader;

/**
 * https://github.com/mlcommons/croissant
 */
@AutoService(Exporter.class)
public class CroissantExporter implements Exporter {

    /*
     * The name of the format it creates. If this format is already provided by a
     * built-in exporter, this Exporter will override the built-in one. (Note that
     * exports are cached, so existing metadata export files are not updated
     * immediately.)
     */
    @Override
    public String getFormatName() {
        return "croissant";
    }

    /**
     * The display name shown in the UI
     *
     * @param locale
     */
    @Override
    public String getDisplayName(Locale locale) {
        // This example includes the language in the name to demonstrate that locale is
        // available. A production exporter would instead use the locale to generate an
        // appropriate translation.
        return "Croissant";
    }

    /**
     * Whether the exported format should be available as an option for
     * Harvesting
     */
    @Override
    public Boolean isHarvestable() {
        return false;
    }

    /**
     * Whether the exported format should be available for download in the UI
     * and API
     */
    @Override
    public Boolean isAvailableToUsers() {
        return true;
    }

    /**
     * Defines the mime type of the exported format - used when metadata is
     * downloaded, i.e. to trigger an appropriate viewer in the user's browser.
     */
    @Override
    public String getMediaType() {
        return MediaType.APPLICATION_JSON;
    }

    /**
     * This method is called by Dataverse when metadata for a given dataset in
     * this format is requested.
     */
    @Override
    public void exportDataset(ExportDataProvider dataProvider, OutputStream outputStream) throws ExportException {
        try {
            // Start building the output format.
            JsonObjectBuilder job = Json.createObjectBuilder();
            String contextString = """
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
                }
            }
            """;
            try (JsonReader jsonReader = Json.createReader(new StringReader(contextString))) {
                JsonObject contextObject = jsonReader.readObject();
                job.add("@context", contextObject.getJsonObject("@context"));
            }

            job.add("@type", "sc:Dataset");
            job.add("conformsTo", "http://mlcommons.org/croissant/1.0");

            JsonObject datasetJson = dataProvider.getDatasetJson();
            // TODO: Get title more easily elsewhere? https://github.com/IQSS/dataverse/issues/2110
            job.add("name", datasetJson
                    .getJsonObject("datasetVersion")
                    .getJsonObject("metadataBlocks")
                    .getJsonObject("citation")
                    .getJsonArray("fields")
                    // FIXME: brittle to assume title is element zero
                    .getJsonObject(0)
                    .getString("value")
            );

            JsonObject datasetORE = dataProvider.getDatasetORE();
            // If you make 1.0 a float it passes validation but this seems brittle.
            // Versions should be strings. And besides, 1.0.1 is not a float.
            // The expected error if you use "1.0" as a string is this:
            //  -  [Metadata(Cars)] Version doesn't follow MAJOR.MINOR.PATCH: 1.0. For more information refer to: https://semver.org/spec/v2.0.0.html
            // See also https://github.com/mlcommons/croissant/issues/609
//            job.add("version", Float.parseFloat(datasetORE
//                    .getJsonObject("ore:describes")
//                    .getString("schema:version"))
//            );
            job.add("version", datasetORE
                    .getJsonObject("ore:describes")
                    .getString("schema:version")
            );
            // TODO: Ok for this to be a URL? https://doi.org/10.5072/FK2/EKY1NP
            // Or should it start with 10? 10.5072/FK2/EKY1NP
            job.add("citeAs", datasetORE
                    .getJsonObject("ore:describes")
                    .getString("@id")
            );

            JsonArrayBuilder distribution = Json.createArrayBuilder();
            JsonArrayBuilder recordSet = Json.createArrayBuilder();
            JsonArray datasetFileDetails = dataProvider.getDatasetFileDetails();
            for (JsonValue jsonValue : datasetFileDetails) {

                JsonObject fileDetails = jsonValue.asJsonObject();
                String filename = fileDetails.getString("originalFileName");
                JsonObject checksum = fileDetails.getJsonObject("checksum");
                // Out of the box the checksum type will be md5
                String checksumType = checksum.getString("type").toLowerCase();
                String checksumValue = checksum.getString("value");

                distribution.add(
                        Json.createObjectBuilder()
                                .add("@type", "cr:FileObject")
                                .add("@id", filename)
                                .add("name", fileDetails.getString("originalFileName"))
                                .add("encodingFormat", fileDetails.getString("originalFileFormat"))
                                .add(checksumType, checksumValue)
                                .add("contentUrl", fileDetails.getString("originalFileName"))
                );
                int fileIndex = 0;
                JsonArray dataTables = fileDetails.getJsonArray("dataTables");
                for (JsonValue dataTableValue : dataTables) {
                    JsonObject dataTableObject = dataTableValue.asJsonObject();
                    // Unused
                    int varQuantity = dataTableObject.getInt("varQuantity");
                    // Unused
                    int caseQuantity = dataTableObject.getInt("caseQuantity");
                    JsonArray dataVariables = dataTableObject.getJsonArray("dataVariables");
                    JsonArrayBuilder fieldSetArray = Json.createArrayBuilder();
                    for (JsonValue dataVariableValue : dataVariables) {
                        JsonObjectBuilder fieldSetObject = Json.createObjectBuilder();
                        fieldSetObject.add("@type", "cr:RecordSet");
                        JsonObject dataVariableObject = dataVariableValue.asJsonObject();
                        // TODO: should this be an integer?
                        Integer variableId = dataVariableObject.getInt("id");
                        String variableName = dataVariableObject.getString("name");
                        String variableDescription = dataVariableObject.getString("label");
                        String variableFormatType = dataVariableObject.getString("variableFormatType");
                        String dataType = null;
                        switch (variableFormatType) {
                            case "CHARACTER":
                                dataType = "sc:Text";
                                break;
                            case "NUMERIC":
                                // TODO: Integer? What about other numeric types?
                                dataType = "sc:Integer";
                                break;
                            default:
                                break;
                        }
                        fieldSetArray.add(
                                Json.createObjectBuilder()
                                        .add("@type", "cr:Field")
                                        .add("name", variableName)
                                        .add("description", variableDescription)
                                        .add("dataType", dataType)
                                        .add("source", Json.createObjectBuilder()
                                                .add("@id", variableId.toString())
                                                .add("fileObject", Json.createObjectBuilder()
                                                        .add("@id", filename)
                                                )
                                        )
                        );
                        fieldSetObject.add("field", fieldSetArray);
                        recordSet.add(fieldSetObject);
                    }
                    fileIndex++;
                }
            }
            job.add("distribution", distribution);
            job.add("recordSet", recordSet);

            JsonObject datasetSchemaDotOrg = dataProvider.getDatasetSchemaDotOrg();
            job.add("creator", datasetSchemaDotOrg.getJsonArray("creator"));
            job.add("keywords", datasetSchemaDotOrg.getJsonArray("keywords"));
            job.add("license", datasetSchemaDotOrg.getString("license"));
            job.add("datePublished", datasetSchemaDotOrg.getString("datePublished"));
            job.add("dateModified", datasetSchemaDotOrg.getString("dateModified"));
            job.add("includedInDataCatalog", datasetSchemaDotOrg.getJsonObject("includedInDataCatalog"));
            job.add("publisher", datasetSchemaDotOrg.getJsonObject("publisher"));

            // TODO: Do we need DataCite XML?
            String dataCiteXml = dataProvider.getDataCiteXml();

            // Write the output format to the output stream.
            outputStream.write(job.build().toString().getBytes("UTF8"));
            // Flush the output stream - The output stream is automatically closed by
            // Dataverse and should not be closed in the Exporter.
            outputStream.flush();
        } catch (Exception ex) {
            // If anything goes wrong, an Exporter should throw an ExportException.
            throw new ExportException("Unknown exception caught during export: " + ex);
        }
    }

}
