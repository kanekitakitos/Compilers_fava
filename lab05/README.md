# Lab 05 - Manual Recursive-Descent Parser

[![Lab](https://img.shields.io/badge/Lab-05-blue.svg)]()
[![Focus](https://img.shields.io/badge/Focus-Manual%20Parsing-purple.svg)]()
[![Java](https://img.shields.io/badge/Java-Parser-orange.svg)]()

> Hand-written recursive-descent parser that exposes parsing logic explicitly instead of relying fully on generated parser code.

## Quick Facts

| Item | Details |
|---|---|
| Main file | `src/Main.java` |
| Reference grammar | `src/EXER1.g4` |
| Parsing style | recursive descent |
| Input example | `src/input.txt` |

## What This Lab Emphasizes

- manual control over token consumption
- direct mapping between grammar rules and Java methods
- explicit syntax diagnostics
- educational comparison with ANTLR-generated parsers

## Key Methods In The Parser

- `nonTerminal_S`
- `nonTerminal_L`
- `nonTerminal_M`
- token matching and error-report helpers

## How To Run

Use `src/Main.java` as the program entry point. The parser accepts an optional file path argument and otherwise reads from standard input.

## Folder Layout

```text
lab05/
|-- README.md
|-- src/
|   |-- EXER1.g4
|   |-- Main.java
|   `-- input.txt
`-- pratica05.iml
```

## Related Reading

- [Repository README](../README.md)
- [Lab 06 README](../lab06/README.md)
