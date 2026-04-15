package com.vinay.docextractor.service;

import com.vinay.docextractor.model.DocumentType;
import com.vinay.docextractor.model.ExtractionResult;
import com.vinay.docextractor.parser.DocumentParserFactory;
import com.vinay.docextractor.util.FileExtensionUtil;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core service for extracting text from documents.
 *
 * <p>Supports: PDF, DOCX, DOC, PPTX, XLSX, TXT, HTML, HTM, MD.
 *
 * <p>Usage:
 * <pre>
 *   DocumentExtractionService service = new DocumentExtractionService();
 *   ExtractionResult result = service.extract("/path/to/file.pdf");
 * </pre>
 */
public class DocumentExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentExtractionService.class);

    /**
     * Extracts text from a single file at the given path.
     *
     * @param filePath absolute or relative path to the document
     * @return {@link ExtractionResult} with extracted text and metadata
     */
    public ExtractionResult extract(String filePath) {
        logger.info("Starting extraction for: {}", filePath);

        long startTime = System.currentTimeMillis();

        // 1. Validate file existence
        if (!FileExtensionUtil.isReadableFile(filePath)) {
            return buildFailure(filePath, DocumentType.UNKNOWN, startTime,
                    "File not found or not readable: " + filePath);
        }

        // 2. Detect document type from extension
        String extension = FileExtensionUtil.getExtension(filePath);
        DocumentType documentType = DocumentType.fromExtension(extension);

        if (!DocumentParserFactory.isSupported(documentType)) {
            return buildFailure(filePath, documentType, startTime,
                    "Unsupported file type: '." + extension + "'. Supported types: "
                            + getSupportedExtensionsList());
        }

        // 3. Select parser and extract
        try {
            DocumentParser parser = DocumentParserFactory.getParser(documentType);
            Path path = new File(filePath).toPath();

            Document document = FileSystemDocumentLoader.loadDocument(path, parser);

            String extractedText = document.text();
            Map<String, String> metadata = buildMetadata(document, filePath, documentType);

            long elapsedMs = System.currentTimeMillis() - startTime;

            logger.info("Extraction complete: {} chars, {} words, {}ms — {}",
                    extractedText.length(),
                    extractedText.trim().split("\\s+").length,
                    elapsedMs,
                    FileExtensionUtil.getFileName(filePath));

            return ExtractionResult.builder()
                    .filePath(filePath)
                    .documentType(documentType)
                    .success(true)
                    .extractedText(extractedText)
                    .metadata(metadata)
                    .extractionTimeMs(elapsedMs)
                    .build();

        } catch (Exception e) {
            logger.error("Extraction failed for {}: {}", filePath, e.getMessage(), e);
            return buildFailure(filePath, documentType, startTime,
                    "Extraction error: " + e.getMessage());
        }
    }

    /**
     * Extracts text from all supported documents in a directory (non-recursive).
     *
     * @param directoryPath path to the directory
     * @return list of {@link ExtractionResult} for each supported file found
     */
    
    public List<ExtractionResult> extractFromDirectory(String directoryPath) {
        List<ExtractionResult> results = new ArrayList<>();
        File directory = new File(directoryPath);

        if (!directory.exists() || !directory.isDirectory()) {
            logger.error("Not a valid directory: {}", directoryPath);
            return results;
        }

        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            logger.warn("Directory is empty: {}", directoryPath);
            return results;
        }

        logger.info("Scanning directory: {} ({} files found)", directoryPath, files.length);

        for (File file : files) {
            if (!file.isFile()) {
                continue;
            }
            String extension = FileExtensionUtil.getExtension(file.getAbsolutePath());
            DocumentType type = DocumentType.fromExtension(extension);

            if (!DocumentParserFactory.isSupported(type)) {
                logger.debug("Skipping unsupported file: {}", file.getName());
                continue;
            }

            ExtractionResult result = extract(file.getAbsolutePath());
            results.add(result);
        }

        logger.info("Directory extraction complete: {}/{} files extracted successfully",
                countSuccesses(results), results.size());

        return results;
    }

    /**
     * Prints a formatted summary of extraction results to the console.
     */
    public void printSummary(List<ExtractionResult> results) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  EXTRACTION SUMMARY");
        System.out.println("=".repeat(70));

        int successCount = 0;
        int failCount    = 0;

        for (ExtractionResult result : results) {
            if (result.isSuccess()) {
                successCount++;
                System.out.printf("  ✔  %-40s  %6d words  %4dms%n",
                        FileExtensionUtil.getFileName(result.getFilePath()),
                        result.getWordCount(),
                        result.getExtractionTimeMs());
            } else {
                failCount++;
                System.out.printf("  ✘  %-40s  ERROR: %s%n",
                        FileExtensionUtil.getFileName(result.getFilePath()),
                        result.getErrorMessage());
            }
        }

        System.out.println("-".repeat(70));
        System.out.printf("  Total: %d  |  Success: %d  |  Failed: %d%n",
                results.size(), successCount, failCount);
        System.out.println("=".repeat(70) + "\n");
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private ExtractionResult buildFailure(String filePath, DocumentType documentType,
                                          long startTime, String message) {
        return ExtractionResult.builder()
                .filePath(filePath)
                .documentType(documentType)
                .success(false)
                .extractedText("")
                .errorMessage(message)
                .extractionTimeMs(System.currentTimeMillis() - startTime)
                .build();
    }

    private Map<String, String> buildMetadata(Document document, String filePath,
                                              DocumentType documentType) {
        Map<String, String> metadata = new HashMap<>();

        // Copy LangChain4j document metadata
        if (document.metadata() != null) {
            Map<String, Object> lc4jMeta = document.metadata().toMap();
            for (Map.Entry<String, Object> entry : lc4jMeta.entrySet()) {
                if (entry.getValue() != null) {
                    metadata.put(entry.getKey(), entry.getValue().toString());
                }
            }
        }

        // Add our own enriched metadata
        metadata.put("source_file",    FileExtensionUtil.getFileName(filePath));
        metadata.put("source_path",    filePath);
        metadata.put("document_type",  documentType.name());
        metadata.put("mime_type",      documentType.getMimeType());
        metadata.put("file_size_bytes",
                String.valueOf(new File(filePath).length()));

        return metadata;
    }

    private int countSuccesses(List<ExtractionResult> results) {
        int count = 0;
        for (ExtractionResult result : results) {
            if (result.isSuccess()) {
                count++;
            }
        }
        return count;
    }

    private String getSupportedExtensionsList() {
        return "pdf, docx, doc, pptx, xlsx, txt, html, htm, md";
    }
}
