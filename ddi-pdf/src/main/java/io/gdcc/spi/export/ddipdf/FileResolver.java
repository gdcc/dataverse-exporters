package io.gdcc.spi.export.ddipdf;

import java.io.InputStream;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

public class FileResolver implements URIResolver {
    
    private static final Logger logger = Logger.getLogger(FileResolver.class.getCanonicalName());
    

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        logger.info("In File Resolver: " + href + "  " + base);
        if (href.startsWith("file:")) {
        String url =href.substring("file:".length()); // some calculation from its parameters
                InputStream is = this.getClass().getResourceAsStream(url);
                return new StreamSource(is);
        } else {
            return null;
        }
    }

}
