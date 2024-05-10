
package io.gdcc.spi.export.examples.generic;

import com.google.auto.service.AutoService;
import io.gdcc.spi.export.ExportDataProvider;
import io.gdcc.spi.export.ExportException;
import io.gdcc.spi.export.Exporter;
import io.github.erykkul.json.transformer.Transformer;
import io.github.erykkul.json.transformer.TransformerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import jakarta.json.JsonObject;
import jakarta.ws.rs.core.MediaType;

/**
 * An example exporter that exports dataset metadata as a JSON object. This
 * simple example demonstrates what is needed to register as a Dataverse
 * Exporter and how to retrieve input metadata and deliver the generated
 * metadata format. The new format itself is a minor variation of the built-in
 * JSON metadata format.
 * 
 */
// This annotation makes the Exporter visible to Dataverse. How it works is well
// documented on the Internet.
@AutoService(Exporter.class)
// All Exporter implementations must implement this interface or the XMLExporter
// interface that extends it.
public class GenericExporter implements Exporter {
    private static final Logger logger = Logger.getLogger(TransformerFactory.class.getName());
    private static final TransformerFactory factory = TransformerFactory.factory(new NashornScriptEngineFactory());
    private static final Transformer preTransformer = transformer("pre_transformer.json");
    private static final Transformer transformer = transformer("transformer.json");

    private static Transformer transformer(final String fileName) {
        try {
            // when running tests, etc.
            final Path path = Paths.get(GenericExporter.class.getClassLoader().getResource(fileName).toURI());
            return factory.createFromJsonString(Files.readString(path), path.getParent().toString());
        } catch (final Exception e) {
            try {
                // on the server:
                // copy the transformers from the jar if they are not yet in the exporters dir
                final URI jarUri = GenericExporter.class.getProtectionDomain().getCodeSource().getLocation().toURI();
                final FileSystem fs = FileSystems.newFileSystem(jarUri, new HashMap<>());
                final Path outPath = Paths.get(jarUri.toString().substring("jar:file:".length(),
                        jarUri.toString().length() - ".jar!/".length()));
                Files.createDirectories(outPath.resolve("js"));
                List.of(Paths.get("pre_transformer.json"), Paths.get("transformer.json"), Paths.get("js", "flatten.js"),
                        Paths.get("js", "map_metadata_fields.js"))
                        .stream().filter(x -> !Files.exists(outPath.resolve(x))).forEach(x -> {
                            try {
                                Files.copy(fs.getPath(x.toString()), outPath.resolve(x));
                            } catch (final IOException e2) {
                            }
                        });
                fs.close();

                // read from files, as usual
                final String transformerString = Files.readString(outPath.resolve(fileName));
                return factory.createFromJsonString(transformerString, outPath.toString());
            } catch (final Exception e3) {
                logger.severe("transformer creation failed (using identity transformer): " + e3);
                return factory.createFromJsonString("{\"transformations\":[{}]}");
            }
        }
    }

    // These methods provide information about the Exporter to Dataverse

    /*
     * The name of the format it creates. If this format is already provided by a
     * built-in exporter, this Exporter will override the built-in one. (Note that
     * exports are cached, so existing metadata export files are not updated
     * immediately.)
     */
    @Override
    public String getFormatName() {
        return "transformer_json";
    }

    // The display name shown in the UI
    @Override
    public String getDisplayName(final Locale locale) {
        // This example includes the language in the name to demonstrate that locale is
        // available. A production exporter would instead use the locale to generate an
        // appropriate translation.
        return "Generic transformer example";
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
    public void exportDataset(final ExportDataProvider dataProvider, final OutputStream outputStream)
            throws ExportException {
        try {
            final JsonObject preTransformed = preTransformer.transform(dataProvider.getDatasetJson());
            final JsonObject transformed = transformer.transform(preTransformed);
            // Write the output format to the output stream.
            outputStream.write(transformed.toString().getBytes("UTF8"));
            // Flush the output stream - The output stream is automatically closed by
            // Dataverse and should not be closed in the Exporter.
            outputStream.flush();
        } catch (final Exception e) {
            // If anything goes wrong, an Exporter should throw an ExportException.
            throw new ExportException("Unknown exception caught during JSON export.");
        }
    }
}