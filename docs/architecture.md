# Architecture

## Overview

Summarizer is a JavaFX desktop application for finding, opening, summarizing, saving, and exporting scientific papers. It supports arXiv search results and user-selected local PDF files. Summaries are generated through a configurable LLM provider, with Ollama as the default local provider and OpenAI, Anthropic, or OpenAI-compatible custom endpoints available from Settings.

The application follows a lightweight MVVM structure:

- View: FXML layouts and JavaFX controllers.
- ViewModel: JavaFX properties, UI state, validation, and asynchronous UI actions.
- Service: application use cases and orchestration.
- Repository: persistence contracts for saved papers and summaries.
- Infrastructure: HTTP clients, PDF extraction, chunking, Ollama/cloud provider calls, and SQLite persistence.
- Domain: immutable records and enums used across the app.

## Runtime Composition

`HelloApplication` creates the main object graph when the application starts:

- `DBInitializer` points to the user data directory at `~/.summarizer`.
- `SettingsService` reads and writes `settings.json` in that directory.
- `ArxivClient` and `PaperSearchService` provide arXiv searching.
- `TextExtractor`, `TextShredder`, `OllamaClient`, and `SummaryService` provide document processing and summarization.
- `ReportExportService` handles PDF downloads and summary file exports.
- `MainViewModel` is shared by the home screen.
- `NavigationService` loads FXML views and preserves the current window size, maximized state, and fullscreen state across navigation.

On startup, `DBInitializer.initialize()` creates or migrates the SQLite database. On shutdown, `HelloApplication.stop()` deletes configured temporary files when `clearTemporaryFilesOnExit` is enabled.

## Views and Controllers

- `Main.fxml` / `MainViewController`: search arXiv by text and category, page through results, upload a local PDF, and open a paper detail view.
- `detailedPaper.fxml` / `PaperDetailViewController`: show paper metadata, choose a summary type, generate a summary, save the paper, and save the generated summary.
- `SavedContent.fxml` / `SavedContentController`: list saved articles or summaries, open items by double-clicking, and delete the selected item.
- `detailedSummary.fxml` / `SummaryDetailViewController`: show a saved summary and export it to a configured directory.
- `Settings.fxml` / `SettingsController`: configure model provider, model name, endpoints, context limits, parsing, chunking, summary defaults, output language, storage directories, export format, and local file handling.
- `TopNavigation.fxml` / `TopNavigationController`: shared navigation for Home, Saved Summaries, Saved Articles, and Settings.

Controllers bind JavaFX controls to ViewModel properties and delegate work to services. Long-running operations such as arXiv search, loading more results, connection tests, and summary generation run on daemon JavaFX `Task` threads so the UI remains responsive.

## ViewModels

- `MainViewModel` owns search query, selected arXiv category, search results, loading state, errors, and "More results" state. It validates that both a query and category are selected before searching. It also converts local PDF selections into `Paper` records.
- `PaperDetailsViewModel` exposes selected paper metadata, summary type, generated summary text, loading state, and save actions. It delegates summary generation to `SummaryService`, paper persistence to `SQLitePaperRepository`, summary persistence to `SQLiteSummaryRepository`, and optional file output to `ReportExportService`.
- `SummaryDetailsViewModel` exposes saved summary fields and exports summaries through `ReportExportService`.
- `PaperViewModel` and `SummaryViewModel` adapt domain records for list display.

## Services

### Paper Search

`PaperSearchService` builds arXiv API query parameters from a user query and a selected top-level category. It requests pages of 20 results by default and delegates HTTP access and response parsing to `ArxivClient`.

`ArxivClient` calls `https://export.arxiv.org/api/query`, applies the shared arXiv HTTP policy, parses Atom entries, and maps them into `Paper` records with title, abstract, authors, publication dates, and PDF URL.

### Summarization

`SummaryService` coordinates the full summarization pipeline:

1. Load current `AppSettings`.
2. Extract text from a URL or local PDF path.
3. Optionally save extracted plain text in the configured temporary directory.
4. Decide whether the document fits the selected context budget.
5. Use a single prompt when the document fits and chunking is not forced.
6. Otherwise split the text and run a map/reduce summarization flow.
7. Return the generated summary text to the ViewModel.

The context fit check uses an approximate four characters per token and subtracts `maxOutputTokens` from `contextTokens` to reserve response space.

### PDF Extraction and Chunking

`TextExtractor` supports:

- Local PDF extraction through PDFBox.
- Remote PDF extraction from arXiv or another URL.
- Optional page-by-page OCR through the Ollama model `frob/unlimited-ocr:q8_0`.

`TextShredder` removes the references section when it can identify it, then chunks text in one of two ways:

- Section-based splitting for numbered headings in automatic mode.
- Fixed token-size windows with configurable overlap in manual mode or as a fallback.

### LLM Providers

`OllamaClient` is the LLM gateway for all providers despite its historical name. It supports:

- Ollama `/api/generate` for local summaries.
- Ollama `/api/chat` for Unlimited-OCR image extraction.
- OpenAI-compatible `/v1/chat/completions` requests for OpenAI and custom providers.
- Anthropic `/v1/messages` requests.
- Provider connection tests from Settings.

API keys can be entered in Settings, read from environment variables, or both depending on provider:

- OpenAI: `OPENAI_API_KEY`
- Anthropic: `ANTHROPIC_API_KEY`
- Custom OpenAI-compatible provider: `CLOUD_LLM_API_KEY`

## Summary Types

The app supports four summary modes:

- `STRUCTURED`: summarize section by section while preserving source structure.
- `TLDR`: produce a short final summary focused on the core question, method, findings, and conclusion.
- `EXECUTIVE`: produce a longer narrative summary for technically literate readers.
- `RESEARCH_NOTE`: answer research-oriented questions about contribution, prior work, strengths, assumptions, limitations, and future work using only explicitly stated information.

For chunked documents, `TLDR`, `EXECUTIVE`, and `RESEARCH_NOTE` use a map/reduce flow. `STRUCTURED` summarizes chunks and joins the resulting section summaries.

## Settings

Settings are stored as JSON in:

```text
~/.summarizer/settings.json
```

`SettingsService` loads this file and fills missing or invalid values with `AppSettings.defaults()`.

Default settings include:

- Provider: `Ollama`
- Model: `qwen3:8b`
- Ollama URL: `http://localhost:11434`
- Context tokens: `8192`
- Maximum output tokens: `1024`
- Request timeout: `10` minutes
- Parsing method: `Auto`
- Chunking mode: `Auto`
- Manual chunk size: `6000` tokens
- Chunk overlap: `500` tokens
- Default summary type: `STRUCTURED`
- Output language: `English`
- Summary export format: `Markdown`
- Download PDF when saving an article: enabled

## Persistence and Files

The local SQLite database is stored at:

```text
~/.summarizer/db.db
```

It contains:

- `paper`: saved articles with title, abstract, dates, PDF link/path, and authors.
- `summary`: saved generated summaries with title, authors, summary text, source PDF link/path, publication date, and summary type.

`ReportExportService` can:

- Download a paper PDF when saving an article.
- Export a summary as Markdown or plain text.
- Avoid overwriting existing files by adding numeric suffixes.

If no storage directory is configured, file output defaults to the settings directory.

## Main Data Flows

### Search and Open an arXiv Paper

```text
User enters query and category
MainViewController
MainViewModel
PaperSearchService
ArxivClient
arXiv API
Paper records
PaperViewModel list
Double-click result
NavigationService
Paper detail view
```

### Upload and Open a Local PDF

```text
User chooses Upload PDF
MainViewController
FileChooser
MainViewModel.createLocalPdfPaper
NavigationService
Paper detail view
```

### Generate a Summary

```text
User selects summary type
PaperDetailViewController
PaperDetailsViewModel
SummaryService
TextExtractor
TextShredder when needed
OllamaClient
Configured provider
Generated summary
PaperDetailsViewModel
Paper detail view
```

### Save and Export Content

```text
Save paper
PaperDetailsViewModel
SQLitePaperRepository
Optional PDF download

Save summary
PaperDetailsViewModel
SQLiteSummaryRepository
Optional summary export

Export saved summary
SummaryDetailsViewModel
ReportExportService
Markdown or text file
```
