package com.vinay.docextractor.model;

/**
 * Supported document types for text extraction.
 */
public enum DocumentType {

    PDF("pdf", "application/pdf"),
    DOCX("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    DOC("doc", "application/msword"),
    PPTX("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    TXT("txt", "text/plain"),
    HTML("html", "text/html"),
    HTM("htm", "text/html"),
    MD("md", "text/markdown"),
    UNKNOWN("", "application/octet-stream");

    private final String extension;
    private final String mimeType;

    DocumentType(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public String getExtension() {
        return extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    /**
     * Resolve DocumentType from a file extension (case-insensitive).
     */
    public static DocumentType fromExtension(String ext) {
        if (ext == null || ext.isBlank()) {
            return UNKNOWN;
        }
        String normalised = ext.toLowerCase().replace(".", "");
        for (DocumentType type : values()) {
            if (type.extension.equals(normalised)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
