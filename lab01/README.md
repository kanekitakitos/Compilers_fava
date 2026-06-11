# Lab 01 - Introductory Calculator Grammar

[![Lab](https://img.shields.io/badge/Lab-01-blue.svg)]()
[![Focus](https://img.shields.io/badge/Focus-Grammar%20Design-purple.svg)]()
[![ANTLR](https://img.shields.io/badge/ANTLR-4.13.2-blue.svg)](https://www.antlr.org/)

> Introductory ANTLR grammar for a small imperative calculator language with assignment, input, output, and arithmetic expressions.

## Quick Facts

| Item | Details |
|---|---|
| Primary artifact | `lab01/src/Cal.g4` |
| Style | syntax specification |
| Runtime status | grammar-focused, no dedicated parser runner yet |
| Statement file | `p1.pdf` |

## What This Lab Covers

- assignment statements
- `read` and `write` instructions
- arithmetic expressions over identifiers and numbers
- parenthesized expressions
- introductory grammar modeling with ANTLR

## Key Files

- `lab01/src/Cal.g4`
- `lab01/src/Main.java`
- `p1.pdf`

## Build Notes

This lab is mainly a grammar-definition exercise. To make it executable, the typical workflow is:

1. generate the lexer and parser from `Cal.g4`
2. create a runner that reads a file or standard input
3. invoke the start rule over a small program

## Folder Layout

```text
lab01/
|-- README.md
|-- p1.pdf
`-- lab01/
    |-- src/
    |   |-- Cal.g4
    |   `-- Main.java
    `-- lab01.iml
```

## Related Reading

- [Repository README](../README.md)
- [Lab 02 README](../lab02/README.md)
