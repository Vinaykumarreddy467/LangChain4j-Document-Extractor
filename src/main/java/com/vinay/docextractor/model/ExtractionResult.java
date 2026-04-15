package com.vinay.docextractor.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the result of a document text extraction operation.
 */
public class ExtractionResult {

    private final String filePath;
    private final DocumentType documentType;
    private final boolean success;
    private final String extractedText;
    private final Map<String, String> metadata;
    private final String errorMessage;
    private final long extractionTimeMs;

    // Private constructor — use the builder
    private ExtractionResult(Builder builder) {
        this.filePath = builder.filePath;
        this.documentType = builder.documentType;
        this.success = builder.success;
        this.extractedText = builder.extractedText;
        this.metadata = builder.metadata;
        this.errorMessage = builder.errorMessage;
        this.extractionTimeMs = builder.extractionTimeMs;
    }

    public String getFilePath() {
        return filePath;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getExtractionTimeMs() {
        return extractionTimeMs;
    }

    public int getWordCount() {
        if (extractedText == null || extractedText.isBlank()) {
            return 0;
        }
        return extractedText.trim().split("\\s+").length;
    }

    public int getCharCount() {
        if (extractedText == null) {
            return 0;
        }
        return extractedText.length();
    }

    @Override
    public String toString() {
        return "ExtractionResult{"
                + "filePath='" + filePath + '\''
                + ", documentType=" + documentType
                + ", success=" + success
                + ", charCount=" + getCharCount()
                + ", wordCount=" + getWordCount()
                + ", extractionTimeMs=" + extractionTimeMs
                + (errorMessage != null ? ", errorMessage='" + errorMessage + '\'' : "")
                + '}';
    }

    // -----------------------------------------------------------------------
    // Builder
    // -----------------------------------------------------------------------

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String filePath;
        private DocumentType documentType;
        private boolean success;
        private String extractedText;
        private Map<String, String> metadata = new HashMap<>();
        private String errorMessage;
        private long extractionTimeMs;

        private Builder() {
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder documentType(DocumentType documentType) {
            this.documentType = documentType;
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder extractedText(String extractedText) {
            this.extractedText = extractedText;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder extractionTimeMs(long extractionTimeMs) {
            this.extractionTimeMs = extractionTimeMs;
            return this;
        }

        public ExtractionResult build() {
            return new ExtractionResult(this);
        }
    }
}
