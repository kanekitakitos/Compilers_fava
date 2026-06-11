# Lab 03 - Property File Parsing

[![Lab](https://img.shields.io/badge/Lab-03-blue.svg)]()
[![Focus](https://img.shields.io/badge/Focus-Listeners%20%26%20Visitors-purple.svg)]()
[![ANTLR](https://img.shields.io/badge/ANTLR-4.13.2-blue.svg)](https://www.antlr.org/)

> First complete parsing workflow in the repository, comparing listener-based and visitor-based processing over `.properties` files.

## Quick Facts

| Item | Details |
|---|---|
| Grammar | `PropertyFile.g4` |
| Processing styles | listener and visitor |
| Sample inputs | `t.properties`, `contarVIRGULA.properties` |
| Statement file | `p3.pdf` |

## Implemented Ideas

- parsing structured key-value files
- tree walking through `ParseTreeWalker`
- visitor traversal over parse nodes
- ordered extraction and storage of properties
- input from file or standard input

## Main Entry Points

- `PropertyFile-ch7/src/TestPropertyFileListener.java`
- `PropertyFile-ch7/src/TestPropertyFileVisitor.java`

## Typical Usage

Run either entry point and optionally pass a path to a `.properties` file. Without an argument, the program reads from standard input.

## Folder Layout

```text
lab03/
|-- README.md
|-- p3.pdf
`-- PropertyFile-ch7/
    |-- src/
    |   |-- PropertyFile.g4
    |   |-- TestPropertyFileListener.java
    |   `-- TestPropertyFileVisitor.java
    |-- t.properties
    |-- contarVIRGULA.properties
    `-- readme.txt
```

## Related Reading

- [Repository README](../README.md)
- [Lab 04 README](../lab04/README.md)
