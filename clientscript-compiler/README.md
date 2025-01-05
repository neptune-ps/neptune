# ClientScript Compiler

This module is an implementation on [runescript-compiler] for ClientScript 2.
The primary target for this project is Old School RuneScape, so some changes may
be required to support other versions of the game.

## Usage

The main entry point is [`ClientScriptCompilerApplication.kt`][entry-point]. For
example projects, see [neptune-ps/clientscript].

## Running

_Note: The executable mentioned below is available pre-built [here][releases].
For obtaining the executable yourself, `./gradlew assemble` and locate it
at `build/distributions`._

Running the program is as simple as just running the executable. A
`neptune.toml` file is required to tell the compiler some information about your
project. See [neptune.toml](#neptunetoml) for more information on the valid
properties, for a working example project see [neptune-ps/clientscript].

```shell
cs2
```

```shell
cs2 --config-path=custom_config.toml
```

For a list of all options, see `cs2 --help`.

### `neptune.toml`

The `neptune.toml` file lets the compiler know information about the project.

```toml
# The name of the project, currently this has no usage within the compiler
# itself, but it is used within the RuneScript IntelliJ plugin.
name = "neptune"

# An array of source locations. A source can be a directory or a specific file.
# For folders, the folder acts as the root directory and all children are
# recursively visited.
sources = ["src/"]

# An array of symbol root locations. These locations must be to a folder.
# Symbols are searched for by <path>/<type>.sym and <path>/<type>/*.sym.
symbols = ["symbols/"]

# An array of library source files. These files should also be included in
# 'sources', but source files that match or are a child of a library path will
# be excluded from output. The main intention behind this is to have all scripts
# for a specific version available, but without actually fully recompiling them
# all.
libraries = ["src/orig"]

# An array of paths that are excluded from the project. This is currently only
# in used by the RuneScript IntelliJ plugin for marking a folder as excluded.
excludes = ["pack/"]

# Configuration for the binary file writer. This writer outputs scripts to the
# output path specified in a format that the client expects (for OSRS).
# Currently, this is the only supported writer.
[writer.binary]
output = "pack/"
```

## Contributing

See [Contributing][contributing] in the main README.

[releases]: https://github.com/neptune-ps/neptune/releases
[runescript-compiler]: ../runescript-compiler
[entry-point]: src/main/kotlin/me/filby/neptune/clientscript/compiler/ClientScriptCompilerApplication.kt
[neptune-ps/clientscript]: https://github.com/neptune-ps/clientscript
[contributing]: ../README.md#contributing
