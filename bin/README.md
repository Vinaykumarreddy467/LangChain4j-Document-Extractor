# LangChain4j Document Extractor

A clean, production-ready Maven project that extracts text from multiple document
formats using [LangChain4j](https://github.com/langchain4j/langchain4j) document parsers.
Designed as a foundation for RAG (Retrieval-Augmented Generation) pipelines.

---

## Supported Formats

| Format | Extension | Parser Used               |
|--------|-----------|---------------------------|
| PDF    | `.pdf`    | ApachePdfBoxDocumentParser |
| Word   | `.docx`   | ApachePoiDocumentParser    |
| Word   | `.doc`    | ApachePoiDocumentParser    |
| PowerPoint | `.pptx` | ApachePoiDocumentParser  |
| Excel  | `.xlsx`   | ApachePoiDocumentParser    |
| Plain Text | `.txt`  | TextDocumentParser        |
| HTML   | `.html`, `.htm` | TextDocumentParser  |
| Markdown | `.md`   | TextDocumentParser         |

---

## Project Structure

```
src/main/java/com/vinay/docextractor/
├── DocumentExtractorApp.java          ← CLI entry point
├── model/
│   ├── DocumentType.java              ← Enum: PDF, DOCX, PPTX, etc.
│   └── ExtractionResult.java         ← Result with text + metadata
├── parser/
│   ├── DocumentParserFactory.java     ← Picks the right LangChain4j parser
│   └── UnsupportedDocumentTypeException.java
├── service/
│   ├── DocumentExtractionService.java ← Core extraction logic
│   └── TextChunkingService.java       ← RAG-ready text chunking
└── util/
    └── FileExtensionUtil.java         ← File helper utilities
```

---

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Build fat JAR
```bash
mvn clean package -DskipTests
```

### Run

**Single file:**
```bash
java -jar target/langchain4j-doc-extractor-1.0.0.jar /path/to/file.pdf
```

**Directory (batch):**
```bash
java -jar target/langchain4j-doc-extractor-1.0.0.jar /path/to/docs/
```

**Extract + chunk for RAG:**
```bash
java -jar target/langchain4j-doc-extractor-1.0.0.jar --chunk /path/to/file.pdf
```

### Run tests
```bash
mvn test
```

---

## Use as a Library

```java
// Extract a single file
DocumentExtractionService service = new DocumentExtractionService();
ExtractionResult result = service.extract("/path/to/resume.pdf");

if (result.isSuccess()) {
    String text     = result.getExtractedText();
    int wordCount   = result.getWordCount();
    Map<String,String> meta = result.getMetadata();
}

// Batch extract a directory
List<ExtractionResult> results = service.extractFromDirectory("/docs/");
service.printSummary(results);

// Chunk for RAG (512 tokens, 50 overlap)
TextChunkingService chunker = new TextChunkingService();
List<String> chunks = chunker.chunkToStrings(result.getExtractedText());
```

---

## Extending to Other LLMs (Ollama)

You can pipe extracted text directly into your local Ollama models:

```java
// Add to pom.xml: langchain4j-ollama
OllamaChatModel model = OllamaChatModel.builder()
        .baseUrl("http://localhost:11434")
        .modelName("llama3.1:8b")
        .build();

String answer = model.generate("Summarise this document:\n\n" + result.getExtractedText());
```

---

## LangChain4j Version
`0.36.2` — stable release with full Apache PDFBox + POI parser support.
