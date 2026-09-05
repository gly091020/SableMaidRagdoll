# Repository Guidelines

## 规范补充
- 回答统一用中文
- 不需要过多注释，仅在api部分使用`/**`注释
- `../SableRagdollLib`为此模组前置，需要时也可编辑
- 其他子项目的代码编写前需要经过允许
- 不要阅读/参考`CallResponse`模组的代码，除非特殊要求

## Project Structure & Module Organization

- `src/main/java/com/gly091020/SableMaidRagdoll/` — mod source, split by concern:
  `block`, `client`, `command`, `compat`, `editor`, `geo`, `item`, `mixin`,
  `network`, `util`.
- `src/main/resources/` — assets (`lang/`, `blockstates/`, `models/`, `sounds.json`)
  and the `sablemaidragdoll.mixins.json` configuration.
- `run/` — local development runtime for the NeoForge client/server.
- `settings.gradle` includes two composite builds that are part of every build:
  `../SableRagdollLib` (core ragdoll/sub-level engine) and `../Love-And-Loathe`
  (optional compatibility). Changes in those repositories are compiled together
  with this project.

## Build, Test, and Development Commands

NeoForge `net.neoforged.moddev` is the build system (Minecraft 1.21.1, Java 21).

- `.\gradlew.bat compileJava --console=plain -q` — compile sources (Windows).
- `./gradlew build` — full build and jar packaging.
- `./gradlew runClient` / `./gradlew runServer` — launch the development
  client/server.
- There is no automated test suite; verify changes by compiling and testing
  in-game.

## Coding Style & Naming Conventions

- Java 21, 4-space indentation, no tabs; use the official Mojang mappings.
- Package names are lowercase (`com.gly091020.SableMaidRagdoll.*`); classes use
  PascalCase, fields/methods camelCase, constants `UPPER_SNAKE_CASE`.
- Code comments and commit messages in this repository are predominantly
  Chinese — keep that convention for consistency.
- No formatter or linter is configured; match the surrounding code style and
  keep diffs focused.

## Testing Guidelines

There is no test framework and no `src/test` directory. New features are
validated by a successful `compileJava` plus manual in-game testing. When
changing rendering or networking, verify both single-player behavior and, where
relevant, client/server sync.

## Commit & Pull Request Guidelines

- Commit messages are short Chinese summaries, e.g. `初步布娃娃控制`,
  `表情显示`, `bug修复`. Keep each commit focused on one change.
- Pull requests should describe what changed and why, mention any related
  issue, and note what was tested in-game. Keep scope limited to the change at
  hand.
- CI workflows under `.github/workflows/` (`publish.yml`, `release.yml`) handle
  release automation; contributors do not need to run them locally.
