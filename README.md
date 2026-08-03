# Summarizer

A JavaFX desktop application for summarizing texts and articles using LLMs, including both cloud-based and local models via Ollama.
<img width="1116" height="742" alt="image" src="https://github.com/user-attachments/assets/fa1d1f66-5c8c-4d82-b931-bdc5ef5f0bd0" />

## Features

**Flexible LLM selection** — use cloud providers such as OpenAI ChatGPT and Anthropic Claude, or keep everything local with Ollama without sending data to external services.

**Automatic hardware-based configuration** — local model parameters, including context size and batch size, are configured by default for GPUs with 8 GB of VRAM, allowing the application to work out of the box without manual tuning.

**Smart chunking for large texts** — long articles are automatically split into smaller parts and processed sequentially, preventing the LLM from exceeding its context limit.

**Result export** — save generated summaries as `.txt` or `.md` files.

**Local favorites database** — favorite articles and summaries are stored in SQLite and remain available offline.

For detailed instructions on connecting each provider, selecting a model, and configuring Ollama for different amounts of VRAM, see additional info at `docs`.

<img width="1120" height="751" alt="image" src="https://github.com/user-attachments/assets/59603ef6-a375-456a-bb3e-f7615c1bc38c" />


## How It Works

1. The article text is loaded into the application.
2. If the text exceeds the context limit of the selected model, it is split into smaller parts called chunks.
3. Each chunk is sent to the LLM separately, and the intermediate summaries are then combined into a final summary.
4. The finished result can be exported or saved to the local database.

For more information about the chunking strategy and pipeline architecture, see `docs`.

## arXiv Search

The application includes built-in search for scientific papers on arXiv by title, author, or keywords. A paper found in the search results can be opened and sent for summarization immediately, without manually copying its text or URL.

## Requirements

* Java 21 or newer
* Maven 3.8+
* Ollama installed and running when using local models
* A GPU with at least 8 GB of VRAM for comfortable use of local models. Systems with less VRAM may also work but will require manual configuration.

## Installation and Launch

Clone the repository:

```
git clone https://github.com/Nikafge/Summarizer.git
```

### Building an `.exe` File

From the project root directory, run:

```
.\package-windows.cmd
```
After the build is complete, a `windows` directory will be created containing the required file.

Executable path:
```
target\windows\Summarizer\Summarizer.exe
```

### Building a `.jar` File

From the project root directory, run:

```
.\package-jar.cmd
```

After the build is complete, a `jar` directory containing the `.jar` file will be created.

Directory path:
```
target\jar
```

To launch the application, run:
```
java -jar target\jar\Summarizer.jar
```

## Configuring LLM Providers

When launching the application for the first time, specify the following in the application settings:

* An OpenAI and/or Anthropic API key if you plan to use cloud-based models.
* The address of the local Ollama server if you changed default parameter. The default address is the following: http://localhost:11434


## Exporting and Saving

**Exporting a summary** — generated summaries can be exported as `.txt` or `.md` files using the corresponding button in the interface.

**Saving to the database** — favorite articles and summaries can be stored locally. They are saved in an SQLite file and remain available across application sessions without an internet connection.

## Documentation

Additional documentation is available in the `docs` directory.

## Contributing

Pull requests and ideas are welcome. If you plan to expand this section, add a separate `CONTRIBUTING.md` file containing the pull request contribution guidelines.
