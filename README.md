# Compilers Fava - Java Laboratories

[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/technologies/downloads/)
[![ANTLR](https://img.shields.io/badge/ANTLR-4.13.2-blue.svg)](https://www.antlr.org/)
[![Domain](https://img.shields.io/badge/Domain-Compilers%20%26%20Formal%20Languages-purple.svg)]()
[![Status](https://img.shields.io/badge/Status-Academic%20Repository-yellow.svg)]()

> A progressive collection of Java-based compiler construction laboratories focused on grammars, parsing, semantic analysis, bytecode generation, and virtual machines.

## Overview

This repository documents the evolution from introductory ANTLR grammars to complete educational compiler pipelines. The early laboratories focus on syntax modeling and tree processing. The later ones introduce semantic rules, code generation, and execution through custom stack-based virtual machines.

The most advanced line of work is the `Projeto` track, where the `Fava` language grows across three stages into a more complete compiler with functions, scopes, and multi-pass semantic analysis.

## Quick Navigation

| Section | Description | README |
|---|---|---|
| Lab 01 | Minimal imperative calculator grammar | [Open](./lab01/README.md) |
| Lab 02 | Grammar modeling exercises | [Open](./lab02/README.md) |
| Lab 03 | Property-file parsing with listener and visitor | [Open](./lab03/README.md) |
| Lab 04 | Expression evaluation with ANTLR | [Open](./lab04/README.md) |
| Lab 05 | Manual recursive-descent parser | [Open](./lab05/README.md) |
| Lab 06 | Compiler and virtual machine | [Open](./lab06/README.md) |
| Projeto | Fava compiler project | [Open](./Projeto/README.md) |

## Laboratory Progression

| Stage | Main Theme | Key Techniques |
|---|---|---|
| Lab 01 | Basic grammar definition | ANTLR grammar design |
| Lab 02 | Recursive grammar exercises | precedence, recursion, nested structures |
| Lab 03 | Parse-tree processing | listeners, visitors, ordered extraction |
| Lab 04 | Semantic evaluation | stacks, visitors, parse-tree properties |
| Lab 05 | Manual parsing | recursive descent, diagnostics |
| Lab 06 | Full compiler pipeline | bytecode generation, stack VM |
| Projeto | Language evolution | semantics, scopes, functions, VM |

## Repository Structure

```text
Compilers_fava/
|-- antlr-4.13.2-complete.jar
|-- lab01/
|-- lab02/
|-- lab03/
|-- lab04/
|-- lab05/
|-- lab06/
|-- Projeto/
|   |-- lab07 - Projeto parte 1/
|   |-- lab08 - Projeto parte 2/
|   `-- lab09 - Projeto parte 3/
|-- .gitattributes
|-- .gitignore
`-- README.md
```

## Technical Topics

- Formal grammars with ANTLR 4
- Lexical and syntactic analysis
- Parse trees, listeners, and visitors
- Hand-written recursive-descent parsing
- Symbol tables and type rules
- Bytecode generation for stack-based execution
- Virtual machine design and tracing
- Scope handling and function semantics

## Requirements

| Requirement | Notes |
|---|---|
| JDK | `17+` recommended |
| ANTLR | `4.13.2` |
| IDE | IntelliJ IDEA, VS Code, or Eclipse |

The repository already includes `antlr-4.13.2-complete.jar` at the root.

## Typical Entry Points

- `lab03/PropertyFile-ch7/src/TestPropertyFileListener.java`
- `lab03/PropertyFile-ch7/src/TestPropertyFileVisitor.java`
- `lab04/Versão 1/src/TestLEvaluator.java`
- `lab04/Versão 1/src/TestLEvalVisitor.java`
- `lab04/versao_2/src/TestExperEvaluator.java`
- `lab04/versao_2/src/TestExprVisitor.java`
- `lab05/src/Main.java`
- `lab06/src/calCompiler.java`
- `lab06/src/calVM.java`
- `Projeto/lab07 - Projeto parte 1/src/FavaCode/FavaCompileAndRun.java`
- `Projeto/lab08 - Projeto parte 2/src/FavaCode/FavaCompileAndRun.java`
- `Projeto/lab09 - Projeto parte 3/src/FavaCode/FavaCompileAndRun.java`

## Example Workflow

```bash
git clone https://github.com/your-username/Compilers_fava.git
cd Compilers_fava
```

Example for `lab06`:

```bash
java calCompiler inputs/in1.cal -asm
java calVM inputs/in1.calbc -trace
```

## Methodology

This repository follows a didactic progression:

- from syntax specification to semantic validation
- from generated parsers to hand-written parsing logic
- from arithmetic expressions to complete source programs
- from parse trees to executable bytecode

## Author

Brandon Mejia
