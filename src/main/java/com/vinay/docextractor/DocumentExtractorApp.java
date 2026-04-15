package com.vinay.docextractor;

import com.vinay.docextractor.model.ExtractionResult;
import com.vinay.docextractor.service.DocumentExtractionService;
import com.vinay.docextractor.service.TextChunkingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Entry point for the LangChain4j Document Extractor.
 *
 * <p>Usage:
 * <pre>
 *   # Extract a single file
 *   java -jar langchain4j-doc-extractor.jar /path/to/file.pdf
 *
 *   # Extract all supported files in a directory
 *   java -jar langchain4j-doc-extractor.jar /path/to/docs/
 *
 *   # Extract and chunk (for RAG)
 *   java -jar langchain4j-doc-extractor.jar --chunk /path/to/file.pdf
 * </pre>
 */
public class DocumentExtractorApp {

    private static final Logger logger = LoggerFactory.getLogger(DocumentExtractorApp.class);

    public static void main(String[] args) {

        if (args.length == 0) {
            printUsage();
            System.exit(1);
        }

        boolean chunkMode = false;
        String targetPath = null;

        // Parse CLI arguments
        for (String arg : args) {
            if ("--chunk".equalsIgnoreCase(arg)) {
                chunkMode = true;
            } else {
                targetPath = arg;
            }
        }

        if (targetPath == null) {
            System.err.println("ERROR: No file or directory path provided.");
            printUsage();
            System.exit(1);
        }

        DocumentExtractionService extractionService = new DocumentExtractionService();
        TextChunkingService chunkingService = new TextChunkingService();

        File target = new File(targetPath);

        if (target.isDirectory()) {

            // Batch mode — extract all files in the directory
            List<ExtractionResult> results = extractionService.extractFromDirectory(targetPath);
            extractionService.printSummary(results);

            if (chunkMode) {
                System.out.println("\n--- Chunking extracted text ---");

                for (ExtractionResult result : results) {
                    if (result.isSuccess()) {
                        List<String> chunks =
                                chunkingService.chunkToStrings(result.getExtractedText());

                        System.out.printf("%nFile: %s → %d chunks%n",
                                result.getFilePath(), chunks.size());
                    }
                }
            }

        } else if (target.isFile()) {

            // Single file mode
            ExtractionResult result = extractionService.extract(targetPath);
            printSingleResult(result, chunkMode, chunkingService);

        } else {
            System.err.println("ERROR: Path does not exist: " + targetPath);
            System.exit(1);
        }
    }

    private static void printSingleResult(ExtractionResult result,
                                          boolean chunkMode,
                                          TextChunkingService chunkingService) {

        System.out.println("\n" + "=".repeat(70));
        System.out.println("  EXTRACTION RESULT");
        System.out.println("=".repeat(70));
        System.out.println("  File   : " + result.getFilePath());
        System.out.println("  Type   : " + result.getDocumentType());
        System.out.println("  Status : " + (result.isSuccess() ? "SUCCESS" : "FAILED"));

        if (result.isSuccess()) {

            System.out.println("  Words  : " + result.getWordCount());
            System.out.println("  Chars  : " + result.getCharCount());
            System.out.println("  Time   : " + result.getExtractionTimeMs() + "ms");

            // Metadata
            System.out.println("\n--- Metadata ---");
            for (Map.Entry<String, String> entry : result.getMetadata().entrySet()) {
                System.out.printf("  %-25s : %s%n", entry.getKey(), entry.getValue());
            }

            // Preview (first 500 chars)
            String preview = result.getExtractedText();

            if (preview.length() > 500) {
                preview = preview.substring(0, 500) + "...";
            }

            System.out.println("\n--- Text Preview ---");
            System.out.println(preview);

            // Save full extracted text in project folder: output/
            try {
                Path outputDir = Paths.get("output");
                Files.createDirectories(outputDir);

                String inputFileName = Paths.get(result.getFilePath())
                        .getFileName()
                        .toString();

                String baseFileName = inputFileName.contains(".")
                        ? inputFileName.substring(0, inputFileName.lastIndexOf('.'))
                        : inputFileName;

                Path outputFile = outputDir.resolve(baseFileName + "_extracted.txt");

                Files.writeString(outputFile, result.getExtractedText());

                System.out.println("\nFull extracted text saved to:");
                System.out.println("  " + outputFile.toAbsolutePath());

            } catch (Exception e) {
                System.err.println("Failed to save extracted text: " + e.getMessage());
            }

            // Chunking output
            if (chunkMode) {

                List<String> chunks =
                        chunkingService.chunkToStrings(result.getExtractedText());

                System.out.printf("%n--- Chunks (total: %d) ---%n", chunks.size());

                for (int i = 0; i < Math.min(chunks.size(), 3); i++) {
                    System.out.printf("%nChunk %d:%n%s%n", (i + 1), chunks.get(i));
                }

                if (chunks.size() > 3) {
                    System.out.printf("... and %d more chunks%n", chunks.size() - 3);
                }
            }

        } else {
            System.out.println("  Error  : " + result.getErrorMessage());
        }

        System.out.println("=".repeat(70) + "\n");
    }

    private static void printUsage() {
        System.out.println("""
                Usage:
                  java -jar langchain4j-doc-extractor.jar <file-or-directory>
                  java -jar langchain4j-doc-extractor.jar --chunk <file-or-directory>

                Supported formats:
                  PDF, DOCX, DOC, PPTX, XLSX, TXT, HTML, HTM, MD

                Examples:
                  java -jar langchain4j-doc-extractor.jar resume.pdf
                  java -jar langchain4j-doc-extractor.jar /docs/
                  java -jar langchain4j-doc-extractor.jar --chunk report.docx
                """);
    }
}