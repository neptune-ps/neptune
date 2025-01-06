# Neptune

[![MIT License][license-badge]][license]
[![GitHub Actions][github-actions-badge]][github-actions]
[![Discord][discord-badge]][discord]

Neptune is a free, open-source multiplayer game server and toolset primarily
targeting Old School RuneScape.

## Requirements

- Java 17
- Kotlin 2.1.0

## Goals

The primary goal for this project is to have a development environment similar
to the real thing. The follow is a brief overview of the overall goals of the
project.

- [ ] Accurate game server replicating the majority of the core mechanics.
- [ ] Cache tooling for making additions and modifications to the game cache.
    - [x] ClientScript compiler
    - [ ] Config type packers (`obj`, `npc`, `struct`, etc.)
- And more...

## Project Layout

Some modules may have more information in their `README`.

- [clientscript-compiler] - An implementation of `runescript-compiler` that
  targets ClientScript 2.
- [runescript-compiler] - A base compiler implementation intended to be used for
  ClientScript 2 and RuneScript 2.
- [runescript-parser] - A parser for the RuneScript language.
- [runescript-runtime] - A simple bytecode interpreter for RuneScript.

## Building

To build the project run `./gradlew build`.

## Contributing

Pull requests are welcome on [GitHub][pull-requests].

- All contributions must follow the style guidelines, for checking you can use
  `./gradlew check`.
- Commit messages must follow [conventional commits][conventionalcommits]. If a
  commit message doesn't follow what we would like, we will request changes or
  modify it ourselves if the PR allows it.

## Links

- [Discord][discord]

## License

Neptune is licensed under the MIT license. See [LICENSE][license] for the full
text.

[license]: LICENSE
[license-badge]: https://img.shields.io/github/license/neptune-ps/neptune
[github-actions]: https://github.com/neptune-ps/neptune/actions/workflows/ci.yml
[github-actions-badge]: https://github.com/neptune-ps/neptune/actions/workflows/ci.yml/badge.svg?branch=master
[discord]: https://discord.com/invite/BcrqBVJT4W
[discord-badge]: https://img.shields.io/discord/1087194962342457354?color=%237289da&logo=discord
[clientscript-compiler]: ./clientscript-compiler
[runescript-compiler]: ./runescript-compiler
[runescript-parser]: ./runescript-parser
[runescript-runtime]: ./runescript-runtime
[pull-requests]: https://github.com/neptune-ps/neptune/pulls
[conventionalcommits]: https://www.conventionalcommits.org/en/v1.0.0/#summary
