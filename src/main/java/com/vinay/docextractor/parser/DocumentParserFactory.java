package com.vinay.docextractor.parser;

import com.vinay.docextractor.model.DocumentType;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory that returns the appropriate LangChain4j {@link DocumentParser}
 * for each {@link DocumentType}.
 *
 * <p>Supported formats:
 * <ul>
 *   <li>PDF  → ApachePdfBoxDocumentParser</li>
 *   <li>DOCX, DOC, PPTX, XLSX → ApachePoiDocumentParser</li>
 *   <li>TXT, HTML, HTM, MD → TextDocumentParser</li>
 * </ul>
 */
public class DocumentParserFactory {

    private static final Logger logger = LoggerFactory.getLogger(DocumentParserFactory.class);

    // Singleton parsers — stateless, safe to reuse
    private static final ApachePdfBoxDocumentParser PDF_PARSER   = new ApachePdfBoxDocumentParser();
    private static final ApachePoiDocumentParser    POI_PARSER   = new ApachePoiDocumentParser();
    private static final TextDocumentParser         TEXT_PARSER  = new TextDocumentParser();

    private DocumentParserFactory() {
        // Utility class — no instantiation
    }

    /**
     * Returns the correct parser for the given document type.
     *
     * @param documentType the resolved {@link DocumentType}
     * @return a {@link DocumentParser} instance
     * @throws UnsupportedDocumentTypeException if the type is UNKNOWN
     */
    public static DocumentParser getParser(DocumentType documentType) {
        logger.debug("Resolving parser for document type: {}", documentType);

        switch (documentType) {

            case PDF:
                return PDF_PARSER;

            case DOCX:
            case DOC:
            case PPTX:
            case XLSX:
                return POI_PARSER;

            case TXT:
            case HTML:
            case HTM:
            case MD:
                return TEXT_PARSER;

            case UNKNOWN:
            default:
                throw new UnsupportedDocumentTypeException(
                        "No parser available for document type: " + documentType);
        }
    }

    /**
     * Returns true if the given type has a registered parser.
     */
    public static boolean isSupported(DocumentType documentType) {
        return documentType != null && documentType != DocumentType.UNKNOWN;
    }
}
