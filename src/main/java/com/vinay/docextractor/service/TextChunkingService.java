package com.vinay.docextractor.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits extracted document text into smaller chunks ready for
 * embedding and RAG pipelines.
 *
 * <p>Uses LangChain4j's {@link DocumentSplitters#recursive} strategy,
 * which tries to split on paragraph breaks, then sentences, then words,
 * before hard-cutting at the token limit.
 *
 * <p>Default: 512 tokens per chunk, 50-token overlap.
 */
public class TextChunkingService {

    private static final Logger logger = LoggerFactory.getLogger(TextChunkingService.class);

    private static final int DEFAULT_MAX_TOKENS  = 512;
    private static final int DEFAULT_OVERLAP     = 50;

    private final int maxTokensPerChunk;
    private final int overlapTokens;

    public TextChunkingService() {
        this.maxTokensPerChunk = DEFAULT_MAX_TOKENS;
        this.overlapTokens     = DEFAULT_OVERLAP;
    }

    public TextChunkingService(int maxTokensPerChunk, int overlapTokens) {
        this.maxTokensPerChunk = maxTokensPerChunk;
        this.overlapTokens     = overlapTokens;
    }

    /**
     * Splits the given plain text into {@link TextSegment} chunks.
     *
     * @param rawText the extracted text to split
     * @return list of text segments
     */
    public List<TextSegment> chunk(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            logger.warn("Empty text provided for chunking — returning empty list");
            return new ArrayList<>();
        }

        Document document = Document.from(rawText);
        DocumentSplitter splitter = DocumentSplitters.recursive(maxTokensPerChunk, overlapTokens);
        List<TextSegment> segments = splitter.split(document);

        logger.info("Text split into {} chunks (maxTokens={}, overlap={})",
                segments.size(), maxTokensPerChunk, overlapTokens);

        return segments;
    }

    /**
     * Splits text and returns raw string chunks (useful for display / debugging).
     *
     * @param rawText the extracted text to split
     * @return list of plain text chunk strings
     */
    public List<String> chunkToStrings(String rawText) {
        List<TextSegment> segments = chunk(rawText);
        List<String> chunkStrings = new ArrayList<>();
        for (TextSegment segment : segments) {
            chunkStrings.add(segment.text());
        }
        return chunkStrings;
    }

    public int getMaxTokensPerChunk() {
        return maxTokensPerChunk;
    }

    public int getOverlapTokens() {
        return overlapTokens;
    }
}
