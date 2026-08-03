# Summarizer

## Build a Windows executable

From the project root, run:

```powershell
.\package-windows.cmd
```

The build creates a self-contained Windows app image at:

```text
target\windows\Summarizer
```

Open the app with:

```text
target\windows\Summarizer\Summarizer.exe
```

The generated folder includes the Java runtime needed by the app, so it can run outside IntelliJ IDEA.

Requirements:

- Windows
- JDK 21 or newer on `PATH`
- `jpackage` available on `PATH`

## Build a runnable jar

From the project root, run:

```powershell
.\package-jar.cmd
```

The build creates a runnable jar distribution at:

```text
target\jar
```

Run the app with:

```powershell
java -jar target\jar\Summarizer.jar
```

Keep the `target\jar\lib` folder beside `Summarizer.jar`; it contains the JavaFX and application dependency jars needed at runtime.

Requirements:

- JDK 21 or newer on `PATH`
