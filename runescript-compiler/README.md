# RuneScript Compiler

The `runescript-compiler` module is the core implementation of a compiler for
RuneScript. It brings in [runescript-parser] for parsing, handles type checking,
and code generation. This module is intended to be used as a library, for an
example implementation for ClientScript 2 see [ClientScriptCompiler.kt].

## Goals

- [ ] **(WIP)** Being a core library for ServerScript and ClientScript,
  regardless of the
  version of the game.
    - From the beginning of the project it has been a goal to have a core
      library that was shared between ClientScript and ServerScript, however up
      until this point ClientScript was the main focus.
- [x] Able to re-compile all script output from decompiler. (Historically was
  using [Joshua-F/cs2], but moving towards using [zwyz/osrs-cache]).
- [x] Able to produce 1-to-1 bytecode as the Jagex compiler outputs.

## Contributing

See [Contributing][contributing] in the main README.

[runescript-parser]: ../runescript-parser
[ClientScriptCompiler.kt]: ../clientscript-compiler/src/main/kotlin/me/filby/neptune/clientscript/compiler/ClientScriptCompiler.kt
[Joshua-F/cs2]: https://github.com/Joshua-F/cs2
[zwyz/osrs-cache]: https://github.com/zwyz/osrs-cache
[contributing]: ../README.md#contributing
