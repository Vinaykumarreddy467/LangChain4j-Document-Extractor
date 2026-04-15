package com.vinay.docextractor;

import com.vinay.docextractor.model.DocumentType;
import com.vinay.docextractor.model.ExtractionResult;
import com.vinay.docextractor.parser.DocumentParserFactory;
import com.vinay.docextractor.parser.UnsupportedDocumentTypeException;
import com.vinay.docextractor.service.DocumentExtractionService;
import com.vinay.docextractor.service.TextChunkingService;
import com.vinay.docextractor.util.FileExtensionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LangChain4j Document Extractor Tests")
class DocumentExtractorTest {

    private DocumentExtractionService extractionService;
    private TextChunkingService chunkingService;

    @BeforeEach
    void setUp() {
        extractionService = new DocumentExtractionService();
        chunkingService   = new TextChunkingService();
    }

    // -----------------------------------------------------------------------
    // DocumentType tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("DocumentType resolves correctly from extension")
    void testDocumentTypeResolution() {
        assertEquals(DocumentType.PDF,  DocumentType.fromExtension("pdf"));
        assertEquals(DocumentType.DOCX, DocumentType.fromExtension("docx"));
        assertEquals(DocumentType.DOC,  DocumentType.fromExtension("doc"));
        assertEquals(DocumentType.PPTX, DocumentType.fromExtension("pptx"));
        assertEquals(DocumentType.XLSX, DocumentType.fromExtension("xlsx"));
        assertEquals(DocumentType.TXT,  DocumentType.fromExtension("txt"));
        assertEquals(DocumentType.HTML, DocumentType.fromExtension("html"));
        assertEquals(DocumentType.MD,   DocumentType.fromExtension("md"));
    }

    @Test
    @DisplayName("DocumentType is case-insensitive")
    void testDocumentTypeCaseInsensitive() {
        assertEquals(DocumentType.PDF,  DocumentType.fromExtension("PDF"));
        assertEquals(DocumentType.DOCX, DocumentType.fromExtension("DOCX"));
        assertEquals(DocumentType.TXT,  DocumentType.fromExtension("TXT"));
    }

    @Test
    @DisplayName("Unknown extension returns UNKNOWN type")
    void testUnknownExtension() {
        assertEquals(DocumentType.UNKNOWN, DocumentType.fromExtension("xyz"));
        assertEquals(DocumentType.UNKNOWN, DocumentType.fromExtension(""));
        assertEquals(DocumentType.UNKNOWN, DocumentType.fromExtension(null));
    }

    // -----------------------------------------------------------------------
    // FileExtensionUtil tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("FileExtensionUtil extracts extensions correctly")
    void testFileExtensionUtil() {
        assertEquals("pdf",  FileExtensionUtil.getExtension("/docs/resume.pdf"));
        assertEquals("docx", FileExtensionUtil.getExtension("report.DOCX"));
        assertEquals("",     FileExtensionUtil.getExtension("noextension"));
        assertEquals("",     FileExtensionUtil.getExtension(null));
    }

    @Test
    @DisplayName("FileExtensionUtil returns correct file name")
    void testGetFileName() {
        assertEquals("resume.pdf", FileExtensionUtil.getFileName("/home/user/docs/resume.pdf"));
        assertEquals("report.docx", FileExtensionUtil.getFileName("report.docx"));
    }

    @Test
    @DisplayName("FileExtensionUtil returns correct base name")
    void testGetBaseName() {
        assertEquals("resume",   FileExtensionUtil.getBaseName("resume.pdf"));
        assertEquals("my-report", FileExtensionUtil.getBaseName("my-report.docx"));
    }

    // -----------------------------------------------------------------------
    // DocumentParserFactory tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("ParserFactory returns parsers for all supported types")
    void testParserFactoryReturnsParser() {
        assertNotNull(DocumentParserFactory.getParser(DocumentType.PDF));
        assertNotNull(DocumentParserFactory.getParser(DocumentType.DOCX));
        assertNotNull(DocumentParserFactory.getParser(DocumentType.PPTX));
        assertNotNull(DocumentParserFactory.getParser(DocumentType.XLSX));
        assertNotNull(DocumentParserFactory.getParser(DocumentType.TXT));
        assertNotNull(DocumentParserFactory.getParser(DocumentType.HTML));
        assertNotNull(DocumentParserFactory.getParser(DocumentType.MD));
    }

    @Test
    @DisplayName("ParserFactory throws for UNKNOWN type")
    void testParserFactoryThrowsForUnknown() {
        assertThrows(UnsupportedDocumentTypeException.class,
                () -> DocumentParserFactory.getParser(DocumentType.UNKNOWN));
    }

    @Test
    @DisplayName("isSupported returns false for UNKNOWN")
    void testIsSupportedReturnsFalseForUnknown() {
        assertFalse(DocumentParserFactory.isSupported(DocumentType.UNKNOWN));
        assertFalse(DocumentParserFactory.isSupported(null));
        assertTrue(DocumentParserFactory.isSupported(DocumentType.PDF));
    }

    // -----------------------------------------------------------------------
    // Text extraction — TXT file (no external dependency)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Extracts text from a plain TXT file")
    void testExtractTxtFile() throws IOException {
        File tempFile = File.createTempFile("test-doc", ".txt");
        tempFile.deleteOnExit();

        String content = "Hello from LangChain4j!\nThis is a test document.\nLine three.";
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }

        ExtractionResult result = extractionService.extract(tempFile.getAbsolutePath());

        assertTrue(result.isSuccess());
        assertEquals(DocumentType.TXT, result.getDocumentType());
        assertTrue(result.getExtractedText().contains("Hello from LangChain4j"));
        assertTrue(result.getWordCount() > 0);
        assertTrue(result.getCharCount() > 0);
        assertTrue(result.getExtractionTimeMs() >= 0);
    }

    @Test
    @DisplayName("Extracts text from an HTML file")
    void testExtractHtmlFile() throws IOException {
        File tempFile = File.createTempFile("test-page", ".html");
        tempFile.deleteOnExit();

        String html = "<html><body><h1>Title</h1><p>Paragraph content here.</p></body></html>";
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(html);
        }

        ExtractionResult result = extractionService.extract(tempFile.getAbsolutePath());

        assertTrue(result.isSuccess());
        assertEquals(DocumentType.HTML, result.getDocumentType());
    }

    @Test
    @DisplayName("Extracts text from a Markdown file")
    void testExtractMarkdownFile() throws IOException {
        File tempFile = File.createTempFile("readme", ".md");
        tempFile.deleteOnExit();

        String markdown = "# Heading\n\nThis is **bold** and *italic* text.\n\n- Item 1\n- Item 2";
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(markdown);
        }

        ExtractionResult result = extractionService.extract(tempFile.getAbsolutePath());

        assertTrue(result.isSuccess());
        assertEquals(DocumentType.MD, result.getDocumentType());
        assertTrue(result.getExtractedText().contains("Heading"));
    }

    // -----------------------------------------------------------------------
    // Error handling
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Returns failure result for non-existent file")
    void testNonExistentFile() {
        ExtractionResult result = extractionService.extract("/no/such/file.pdf");

        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("not found") || result.getErrorMessage().contains("readable"));
    }

    @Test
    @DisplayName("Returns failure result for unsupported file type")
    void testUnsupportedFileType() throws IOException {
        File tempFile = File.createTempFile("test-binary", ".xyz");
        tempFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("dummy content");
        }

        ExtractionResult result = extractionService.extract(tempFile.getAbsolutePath());

        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Unsupported"));
    }

    // -----------------------------------------------------------------------
    // ExtractionResult model tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("ExtractionResult builder works correctly")
    void testExtractionResultBuilder() {
        ExtractionResult result = ExtractionResult.builder()
                .filePath("/test/doc.pdf")
                .documentType(DocumentType.PDF)
                .success(true)
                .extractedText("Hello World! This is a test.")
                .extractionTimeMs(123L)
                .build();

        assertEquals("/test/doc.pdf", result.getFilePath());
        assertEquals(DocumentType.PDF, result.getDocumentType());
        assertTrue(result.isSuccess());
        assertEquals(6, result.getWordCount());
        assertEquals(28, result.getCharCount());
        assertEquals(123L, result.getExtractionTimeMs());
    }

    // -----------------------------------------------------------------------
    // TextChunkingService tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Chunks long text into multiple segments")
    void testTextChunking() {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            longText.append("This is sentence number ").append(i).append(" in the document. ");
        }

        List<String> chunks = chunkingService.chunkToStrings(longText.toString());

        assertFalse(chunks.isEmpty());
        assertTrue(chunks.size() > 1, "Long text should produce multiple chunks");
    }

    @Test
    @DisplayName("Returns empty list for null or blank text")
    void testChunkEmptyText() {
        List<String> chunks = chunkingService.chunkToStrings(null);
        assertTrue(chunks.isEmpty());

        List<String> chunksBlank = chunkingService.chunkToStrings("   ");
        assertTrue(chunksBlank.isEmpty());
    }

    @Test
    @DisplayName("Custom chunk size is respected")
    void testCustomChunkSize() {
        TextChunkingService customChunker = new TextChunkingService(100, 10);
        assertEquals(100, customChunker.getMaxTokensPerChunk());
        assertEquals(10,  customChunker.getOverlapTokens());
    }

    // -----------------------------------------------------------------------
    // Directory extraction
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Directory extraction skips unsupported files")
    void testDirectoryExtraction() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "lc4j-test-" + System.nanoTime());
        tempDir.mkdirs();
        tempDir.deleteOnExit();

        // Create one supported and one unsupported file
        File txtFile = new File(tempDir, "sample.txt");
        try (FileWriter writer = new FileWriter(txtFile)) {
            writer.write("Sample text content for extraction.");
        }
        txtFile.deleteOnExit();

        File unknownFile = new File(tempDir, "data.xyz");
        try (FileWriter writer = new FileWriter(unknownFile)) {
            writer.write("unsupported content");
        }
        unknownFile.deleteOnExit();

        List<ExtractionResult> results = extractionService.extractFromDirectory(tempDir.getAbsolutePath());

        // Only the .txt file should have been processed
        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
    }
}
