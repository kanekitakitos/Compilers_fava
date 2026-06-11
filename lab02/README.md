# Lab 02 - Grammar Modeling Exercises

[![Lab](https://img.shields.io/badge/Lab-02-blue.svg)]()
[![Focus](https://img.shields.io/badge/Focus-Recursive%20Grammars-purple.svg)]()
[![ANTLR](https://img.shields.io/badge/ANTLR-4.13.2-blue.svg)](https://www.antlr.org/)

> A set of grammar exercises dedicated to arithmetic syntax, recursive language definitions, and nested structures.

## Quick Facts

| Item | Details |
|---|---|
| Primary artifacts | `Expr.g4`, `EXER1.g4`, `EXER2.g4` |
| Style | formal grammar modeling |
| Runtime status | specification-oriented |
| Statement file | `p2.pdf` |

## Contents

- `Expr.g4` models arithmetic expressions with operator hierarchy
- `EXER1.g4` models nested list-like structures
- `EXER2.g4` explores recursive productions in a compact language

## Key Files

- `src/Expr.g4`
- `src/EXER1.g4`
- `src/EXER2.g4`
- `src/Main.java`
- `p2.pdf`

## Suggested Use

This lab is useful for validating grammar choices before writing interpreters or compiler stages. It works especially well as preparation for tree processing labs such as Lab 03 and Lab 04.

## Folder Layout

```text
lab02/
|-- README.md
|-- src/
|   |-- EXER1.g4
|   |-- EXER2.g4
|   |-- Expr.g4
|   `-- Main.java
|-- p2.pdf
`-- lab02.iml
```

## Related Reading

- [Repository README](../README.md)
- [Lab 03 README](../lab03/README.md)
