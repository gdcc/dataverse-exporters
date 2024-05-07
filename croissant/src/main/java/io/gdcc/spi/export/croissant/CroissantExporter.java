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
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.ws.rs.core.MediaType;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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

            JsonObject datasetORE = dataProvider.getDatasetORE();
            JsonObject describes = datasetORE.getJsonObject("ore:describes");
            job.add("name", describes
                    .getString("title")
            );

            /**
             * We append ".0" to our version string to make the Croissant
             * validator happy and to comply with the Croissant spec but we
             * would rather not.
             *
             * Here's what the spec says:
             *
             * "The recommended versioning scheme to use for datasets is
             * MAJOR.MINOR.PATCH, following Semantic Versioning 2.0.0." --
             * https://mlcommons.github.io/croissant/docs/croissant-spec.html#version
             *
             * If a Dataverse user sees 1.0.0 they will hopefully know that they
             * must remove the final ".0" to see the actual version of the
             * dataset they want. Note that as of Dataverse 6.1, if you navigate
             * to 1.0.0 you will be redirected to the latest published version,
             * which is possibly quite different than version 1.0 of the
             * dataset! For example you will be redirected to version 4.0 (as of
             * this writing) if you try to navigate to
             * https://dataverse.harvard.edu/dataset.xhtml?persistentId=doi:10.7910/DVN/TJCLKP&version=1.0.0
             *
             * This is suboptimal, of course. The API will simply show the error
             * "Illegal version identifier '1.0.0'" if you try to access version
             * 1.0.0 of a dataset via API. Here's an example
             * https://dataverse.harvard.edu/api/datasets/:persistentId/versions/1.0.0?persistentId=doi:10.7910/DVN/TJCLKP
             *
             * Either way, a version of 1.0.0 is not helpful to a Dataverse
             * user. The GUI may send you to an old version. The API will bark
             * at you that version is illegal.
             *
             * We opened https://github.com/mlcommons/croissant/issues/609 to
             * argue that 1.0 is a perfect valid version for a dataset.
             * Hopefully in the future we can stop appending ".0"!
             *
             * By the way, it's technically possible to make the Croissant
             * validator happy by passing 1.0 as a float. However, this is not
             * compliant with the Croissant spec which (again) wants 1.0.0. We
             * are sticking with strings because they are a more sane way to
             * represent a version and they're what SemVer uses, even allowing
             * "-alpha", etc.
             */
            job.add("version", describes
                    .getString("schema:version") + ".0"
            );
            /**
             * We have been told that it's fine and appropriate to put the
             * citation to the dataset itself into "citeAs". However, the spec
             * says "citeAs" is "A citation for a publication that describes the
             * dataset" so we have asked for clarification here:
             * https://github.com/mlcommons/croissant/issues/638
             */
            JsonObject datasetSchemaDotOrg = dataProvider.getDatasetSchemaDotOrg();
            job.add("citeAs", getBibtex(datasetORE, datasetJson, datasetSchemaDotOrg));

            JsonArray funder = datasetSchemaDotOrg.getJsonArray("funder");
            if (funder != null) {
                job.add("funder", funder);
            }

            JsonArray spatialCoverage = datasetSchemaDotOrg.getJsonArray("spatialCoverage");
            if (spatialCoverage != null) {
                job.add("spatialCoverage", spatialCoverage);
            }

            JsonArray oreFiles = describes
                    .getJsonArray("ore:aggregates");

            JsonArrayBuilder distribution = Json.createArrayBuilder();
            JsonArrayBuilder recordSet = Json.createArrayBuilder();
            JsonArray datasetFileDetails = dataProvider.getDatasetFileDetails();
            int fileCounter = 0;
            for (JsonValue jsonValue : datasetFileDetails) {

                JsonObject fileDetails = jsonValue.asJsonObject();
                String filename = fileDetails.getString("originalFileName", null);
                if (filename == null) {
                    filename = fileDetails.getString("filename");
                }
                String fileFormat = fileDetails.getString("originalFileFormat", null);
                if (fileFormat == null) {
                    fileFormat = fileDetails.getString("contentType");
                }
                JsonNumber fileSize = fileDetails.getJsonNumber("originalFileSize");
                if (fileSize == null) {
                    fileSize = fileDetails.getJsonNumber("filesize");
                }

                // No unit because the spec says "Defaults to bytes if a unit is not specified."
                JsonNumber fileSizeInBytes = fileSize;
                JsonObject checksum = fileDetails.getJsonObject("checksum");
                // Out of the box the checksum type will be md5
                String checksumType = checksum.getString("type").toLowerCase();
                String checksumValue = checksum.getString("value");
                String contentUrl = oreFiles.getJsonObject(fileCounter).getString("schema:sameAs");
                String description = fileDetails.getString("description", "");
                /**
                 * TODO: directoryLabel is unused right now because we're not
                 * sure where to put it. The spec and examples show it in
                 * contentUrl but we use this field already. We have asked for
                 * clarification in
                 * https://github.com/mlcommons/croissant/issues/639
                 *
                 * It's suboptimal that the directoryLabel isn't already
                 * included in dataProvider.getDatasetFileDetails(). If it gets
                 * added as part of the following issue, we can get it from
                 * there: https://github.com/IQSS/dataverse/issues/10523
                 */
//                String directoryLabel = oreFiles.getJsonObject(fileCounter).getString("dvcore:directoryLabel");

                distribution.add(
                        Json.createObjectBuilder()
                                .add("@type", "cr:FileObject")
                                .add("@id", filename)
                                .add("name", filename)
                                .add("encodingFormat", fileFormat)
                                .add(checksumType, checksumValue)
                                .add("contentSize", fileSizeInBytes)
                                .add("description", description)
                                .add("contentUrl", contentUrl)
                );
                int fileIndex = 0;
                JsonArray dataTables = fileDetails.getJsonArray("dataTables");
                if (dataTables == null) {
                    dataTables = JsonArray.EMPTY_JSON_ARRAY;
                }
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
                        String variableDescription = dataVariableObject.getString("label", "");
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
                fileCounter++;
            }
            job.add("distribution", distribution);
            job.add("recordSet", recordSet);

            job.add("creator", datasetSchemaDotOrg.getJsonArray("creator"));
            job.add("description", datasetSchemaDotOrg.getJsonString("description"));
            job.add("keywords", datasetSchemaDotOrg.getJsonArray("keywords"));
            JsonArray citation = datasetSchemaDotOrg.getJsonArray("citation");
            if (citation != null) {
                job.add("citation", citation);
            }
            JsonArray temporalCoverage = datasetSchemaDotOrg.getJsonArray("temporalCoverage");
            if (temporalCoverage != null) {
                job.add("temporalCoverage", temporalCoverage);
            }
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
            System.out.println("Exception caught in Croissant exporter. Printing stacktrace...");
            ex.printStackTrace();
            // If anything goes wrong, an Exporter should throw an ExportException.
            throw new ExportException("Unknown exception caught during export: " + ex);
        }
    }

    /*
    Here's how a BibTeX export looks in Dataverse:
    @data{DVN/TJCLKP_2017,
    author = {Durbin, Philip},
    publisher = {Harvard Dataverse},
    title = {{Open Source at Harvard}},
    UNF = {UNF:6:e9+1ZqpZtjCuBzTDSrsHgA==},
    year = {2017},
    version = {DRAFT VERSION},
    doi = {10.7910/DVN/TJCLKP},
    url = {https://doi.org/10.7910/DVN/TJCLKP}
    }
     */
    /**
     *
     * The code is inspired by DataCitation.java upstream. However, Croissant
     * does not want newlines, so we omit them. Some notes about this example:
     *
     * - DVN/TJCLKP_2017 seems strange as an identifier. This is probably a bug
     * upstream.
     *
     * - "DRAFT VERSION" is an artifact from a bug that was probably fixed in
     * https://github.com/IQSS/dataverse/pull/9705
     */
    private String getBibtex(JsonObject datasetORE, JsonObject datasetJson, JsonObject datasetSchemaDotOrg) {
        String identifier = datasetJson.getString("identifier");

        JsonObject oreDescribes = datasetORE.getJsonObject("ore:describes");
        String publicationYear = oreDescribes.getString("schema:datePublished").substring(0, 4);

        JsonArray creatorArray = datasetSchemaDotOrg.getJsonArray("creator");
        List<String> creators = new ArrayList<>();
        for (JsonValue creator : creatorArray) {
            creators.add(creator.asJsonObject().getString("name"));
        }
        String creatorsFormatted = String.join(" and ", creators);

        String publisher = datasetSchemaDotOrg.getJsonObject("publisher").getString("name");
        String title = datasetSchemaDotOrg.getString("name");

        String pidAsUrl = oreDescribes.getString("@id");

        StringBuilder sb = new StringBuilder();
        sb.append("@data{").append(identifier).append("_").append(publicationYear).append(",");
        sb.append("author = {").append(creatorsFormatted).append("},");
        sb.append("publisher = {").append(publisher).append("},");
        sb.append("title = {").append(title).append("},");
        sb.append("year = {").append(publicationYear).append("},");
        sb.append("url = {").append(pidAsUrl).append("}");
        sb.append("}");
        return sb.toString();
    }

}
