package io.gdcc.spi.export.croissant;

import com.google.auto.service.AutoService;
import io.gdcc.spi.export.ExportDataProvider;
import io.gdcc.spi.export.ExportException;
import io.gdcc.spi.export.Exporter;
import java.io.OutputStream;
import java.util.Locale;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
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
                }
            }
            """;
            try (JsonReader jsonReader = Json.createReader(new StringReader(contextString))) {
                JsonObject contextObject = jsonReader.readObject();
                job.add("@context", contextObject.getJsonObject("@context"));
            }

            JsonObject datasetJson = dataProvider.getDatasetJson();
            // TODO: Add much more than just the name/title
            job.add("name", datasetJson.getString("name"));

            JsonObject datasetORE = dataProvider.getDatasetORE();
            // TODO: Ok for this to be a URL? https://doi.org/10.5072/FK2/EKY1NP
            // Or should it start with 10? 10.5072/FK2/EKY1NP
            job.add("citeAs", datasetORE.getString("citeAs"));

            JsonArray datasetFileDetails = dataProvider.getDatasetFileDetails();
            // TODO: get more than the first file!
            JsonObject firstFile = datasetFileDetails.getJsonObject(0);
            job.add("distribution", Json.createArrayBuilder().add(Json.createObjectBuilder()
                    .add("contentUrl", firstFile.getString("filename"))));

            JsonObject datasetSchemaDotOrg = dataProvider.getDatasetSchemaDotOrg();
            job.add("dateModified", datasetSchemaDotOrg.getString("dateModified"));

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
