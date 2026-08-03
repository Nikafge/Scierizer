# User Guide

## What Summarizer Does

Summarizer is a desktop app for working with scientific papers. You can search arXiv, open a paper from the search results, upload a local PDF, generate an LLM summary, save papers and summaries locally, and export summaries as Markdown or plain text.

The app can use Ollama locally by default, or a cloud provider configured in Settings.

## First Launch

Before generating summaries, open Settings and check the model configuration.

For local summaries with Ollama:

1. Install and start Ollama.
2. Pull the model you want to use, for example:

   ```text
   ollama pull qwen3:8b
   ```

3. In Settings, keep Provider set to `Ollama`.
4. Keep the Ollama URL as `http://localhost:11434` unless your Ollama server uses a different address.
5. Click `Test connection`.

For cloud summaries:

1. Open Settings.
2. Select `OpenAI`, `Anthropic`, or `Custom`.
3. Enter the model name.
4. Enter an API key or set the matching environment variable.
5. Click `Test connection`.

Supported environment variables:

- `OPENAI_API_KEY` for OpenAI.
- `ANTHROPIC_API_KEY` for Anthropic.
- `CLOUD_LLM_API_KEY` for a custom OpenAI-compatible provider.

If `Remember API key on this device` is enabled, the key is saved in the ordinary application settings file.

## Search arXiv

1. Go to Home.
2. Enter a title, author name, or keyword in the search field.
3. Select one arXiv category.
4. Click `Search`.
5. Double-click a result to open the paper detail screen.

Available categories:

- Physics
- Comp-sci
- Quant. finance
- Engineering
- Mathematics
- Quant. biology
- Statistics
- Economics

The first search returns up to 20 papers. When more results may be available, `More results` appears after a short cooldown. Click it to load the next page.

## Upload a Local PDF

1. Go to Home.
2. Click `Upload PDF`.
3. Select a `.pdf` file.
4. The app opens the PDF in the paper detail screen.

Uploaded PDFs use the file name as the title, `Local PDF` as the author field, and the local file path as the source.

## Generate a Summary

On the paper detail screen:

1. Choose a summary type.
2. Click `Generate summary`.
3. Wait for the progress indicator to finish.
4. Review the generated summary in the summary text area.

Summary types:

- `Structured`: summarizes the paper section by section.
- `Research`: answers research-note questions about contribution, prior work, strengths, assumptions, limitations, and future work.
- `TLDR`: creates a short high-level summary.
- `Executive`: creates a longer narrative summary for technically literate readers.

The app extracts text from the paper PDF first. If the paper is too large for the configured context size, it splits the text into chunks, summarizes those chunks, and combines the results.

## Save Papers and Summaries

On the paper detail screen:

- Click `Save paper` to store the paper metadata in the local database.
- Click `Save summary` after generating a summary to store the generated summary in the local database.

Saved data remains available offline in:

```text
~/.summarizer/db.db
```

Depending on Storage settings, saving a paper can also download the PDF, and saving a summary can also export a file.

## View Saved Content

Use the top navigation bar:

- `Saved Articles`: shows saved papers.
- `Saved Summaries`: shows saved generated summaries.

Double-click a saved article to open its paper detail screen. Double-click a saved summary to open the summary detail screen.

To remove an item:

1. Select it in the saved-content list.
2. Click `Delete selected`.

## Export Summaries

Saved summaries can be exported from the summary detail screen:

1. Open `Saved Summaries`.
2. Double-click a saved summary.
3. Click `Export summary`.

The export format and output directory come from Settings. Supported formats are:

- `Markdown`, saved as `.md`.
- `Text`, saved as `.txt`.

When a file with the same name already exists, the app creates a new file with a numeric suffix instead of overwriting it.

## Settings

Settings are stored in:

```text
~/.summarizer/settings.json
```

### Model

Use the Model tab to configure:

- Provider: `Ollama`, `OpenAI`, `Anthropic`, or `Custom`.
- Model name.
- Request timeout in minutes.
- Ollama base URL.
- Context tokens.
- Maximum output tokens.
- Cloud endpoint.
- API key handling.

The `Refresh models` button currently reports that model refresh is not available yet.

### Processing

Use the Processing tab to configure:

- Parsing method: `Auto` or `Unlimited-OCR`.
- Chunking policy: `Auto` or `Manual`.
- Chunk size in tokens.
- Chunk overlap in tokens.
- Default summary type.
- Output language.
- Whether to preserve numbers.
- Whether to mention equations.
- Whether to include important references.
- Whether to mention figures and tables.

In `Auto` chunking mode, the app sends the full extracted text when it fits into the configured context budget. If it does not fit, it tries section-based splitting and falls back to manual-size chunks.

`Unlimited-OCR` renders PDF pages as images and sends them to the Ollama OCR model:

```text
frob/unlimited-ocr:q8_0
```

Use this mode for PDFs where normal text extraction fails or produces poor text. It requires Ollama and the OCR model even when the main summary provider is cloud-based.

### Storage

Use the Storage tab to configure:

- Directory for downloaded saved article PDFs.
- Directory for exported summaries.
- Directory for temporary extracted text.
- Summary export format.
- Whether to download the PDF when an article is saved.
- Whether to export the summary when it is saved.
- Whether to keep extracted plain text.
- Whether to delete temporary files when the application closes.

If a directory is left empty, the app uses the application settings directory.

## Defaults

The default configuration is:

- Provider: `Ollama`
- Model: `qwen3:8b`
- Ollama URL: `http://localhost:11434`
- Context tokens: `8192`
- Maximum output tokens: `1024`
- Request timeout: `10` minutes
- Parsing method: `Auto`
- Chunking policy: `Auto`
- Chunk size: `6000` tokens
- Chunk overlap: `500` tokens
- Default summary type: `Structured`
- Output language: `English`
- Export format: `Markdown`
- Download PDF when saving a paper: enabled

Click `Reset defaults` in Settings to restore these values in the UI, then click `Save` to persist them.

## Build and Run

Build a runnable JAR:

```text
.\package-jar.cmd
```

Run it:

```text
java -jar target\jar\Summarizer.jar
```

Build a Windows executable:

```text
.\package-windows.cmd
```

The executable is created at:

```text
target\windows\Summarizer\Summarizer.exe
```

## Troubleshooting

- `Type something to search`: enter a query before searching.
- `Select a category`: choose one arXiv category before searching.
- `Could not load papers from arXiv`: check your internet connection and try again later.
- `Could not generate summary`: verify the selected provider, model name, endpoint, API key, and timeout.
- Ollama model missing: run `ollama pull <model-name>`.
- Unlimited-OCR model missing: run `ollama pull frob/unlimited-ocr:q8_0`.
- Poor summary quality from scanned PDFs: try `Unlimited-OCR` in Processing settings.
