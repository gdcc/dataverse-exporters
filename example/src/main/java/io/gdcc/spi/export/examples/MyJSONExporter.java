
package io.gdcc.spi.export.examples;

import com.google.auto.service.AutoService;
import io.gdcc.spi.export.ExportDataProvider;
import io.gdcc.spi.export.ExportException;
import io.gdcc.spi.export.Exporter;
import java.io.OutputStream;
import java.util.Locale;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.core.MediaType;

/**
 * An example exporter that exports dataset metadata as a JSON object. This
 * simple example demonstrates what is needed to register as a Dataverse
 * Exporter and how to retrieve input metadata and deliver the generated
 * metadata format. The new format itself is a minor variation of the built-in
 * JSON metadata format.
 * 
 */
//This annotation makes the Exporter visible to Dataverse. How it works is well documented on the Internet.
@AutoService(Exporter.class)
//All Exporter implementations must implement this interface or the XMLExporter interface that extends it.
public class MyJSONExporter implements Exporter {

    // These methods provide information about the Exporter to Dataverse

    /*
     * The name of the format it creates. If this format is already provided by a
     * built-in exporter, this Exporter will override the built-in one. (Note that
     * exports are cached, so existing metadata export files are not updated
     * immediately.)
     */
    @Override
    public String getFormatName() {
        return "dataverse_json";
    }

    // The display name shown in the UI
    @Override
    public String getDisplayName(Locale locale) {
        // This example includes the language in the name to demonstrate that locale is
        // available. A production exporter would instead use the locale to generate an
        // appropriate translation.
        return "My JSON in " + locale.getLanguage();
    }

    // Whether the exported format should be available as an option for Harvesting
    @Override
    public Boolean isHarvestable() {
        return false;
    }

    // Whether the exported format should be available for download in the UI and
    // API
    @Override
    public Boolean isAvailableToUsers() {
        return true;
    }

    // Defines the mime type of the exported format - used when metadata is
    // downloaded, i.e. to trigger an appropriate viewer in the user's browser.
    @Override
    public String getMediaType() {
        return MediaType.APPLICATION_JSON;
    }

    // This method is called by Dataverse when metadata for a given dataset in this
    // format is requested.
    @Override
    public void exportDataset(ExportDataProvider dataProvider, OutputStream outputStream) throws ExportException {
        try {
            // Start building the output format.
            JsonObjectBuilder datasetJsonBuilder = Json.createObjectBuilder();
            datasetJsonBuilder.add("name", getFormatName());
            /*
             * Retrieve and parse the input metadata. In this example, no parsing is done
             * and the input metadata is simply copied into the JSON structure as a value
             * for the inputJson field. A production exporter would parse the input metadata
             * and include only the metadata matching the output format.
             */
            datasetJsonBuilder.add("inputJson", dataProvider.getDatasetJson());
            // Write the output format to the output stream.
            outputStream.write(datasetJsonBuilder.build().toString().getBytes("UTF8"));
            // Flush the output stream - The output stream is automatically closed by
            // Dataverse and should not be closed in the Exporter.
            outputStream.flush();
        } catch (Exception e) {
            //If anything goes wrong, an Exporter should throw an ExportException.
            throw new ExportException("Unknown exception caught during JSON export.");
        }
    }
}