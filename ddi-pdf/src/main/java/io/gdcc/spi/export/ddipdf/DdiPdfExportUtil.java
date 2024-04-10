package io.gdcc.spi.export.ddipdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;

// For write operation
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXResult;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import java.io.File;


public class DdiPdfExportUtil {

    private static final Logger logger = Logger.getLogger(DdiPdfExportUtil.class.getCanonicalName());
    public static void datasetPdfDDI(InputStream datafile, OutputStream outputStream) throws XMLStreamException {
        try {
            //String sysId = DdiPdfExportUtil.class.getClassLoader().getResource("/ddi-to-fo.xsl").toURI().toString();
            InputStream  styleSheetInput = DdiPdfExportUtil.class.getResourceAsStream("ddi-to-fo.xsl");

            final FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
            FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

            try {
                Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, outputStream);
                // Setup XSLT
                TransformerFactory factory = TransformerFactory.newInstance();
                Source mySrc = new StreamSource(styleSheetInput);
                //mySrc.setSystemId(sysId);
                factory.setURIResolver(new FileResolver());
                Transformer transformer = factory.newTransformer(mySrc);
                
                // Set the value of a <param> in the stylesheet
                transformer.setParameter("versionParam", "2.0");

                // Setup input for XSLT transformation
                Source src = new StreamSource(datafile);

                // Resulting SAX events (the generated FO) must be piped through to FOP
                Result res = new SAXResult(fop.getDefaultHandler());

                // Start XSLT transformation and FOP processing
                transformer.transform(src, res);

            } catch (Exception e) {
                logger.severe(e.getMessage());
            }
        }  catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }

}
