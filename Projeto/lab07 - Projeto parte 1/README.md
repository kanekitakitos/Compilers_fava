# Lab 07 - Projeto Parte 1

[![Stage](https://img.shields.io/badge/Fava-Part%201-blue.svg)]()
[![Focus](https://img.shields.io/badge/Focus-Compiler%20Pipeline-purple.svg)]()
[![ANTLR](https://img.shields.io/badge/ANTLR-Enabled-blue.svg)]()

> First complete implementation stage of the `Fava` language, integrating parsing, semantic validation, code generation, and VM execution.

## Quick Facts

| Item | Details |
|---|---|
| Grammar | `src/Antlr4/Fava.g4` |
| Main class | `src/FavaCode/FavaCompileAndRun.java` |
| Semantic pass | `SemanticAnalyzerVisitor` |
| Inputs | `.fava` and `.bc` examples under `input/` |
| Statement file | `tp-1.pdf` |

## Core Components

- `src/FavaCode/Compiler/FavaCompiler.java`
- `src/FavaCode/Semantic/SemanticAnalyzerVisitor.java`
- `src/FavaCode/CodeGenerator/CodeGen.java`
- `src/FavaCode/VirtualMachine/FavaVM.java`

## Typical Command

```bash
java FavaCode.FavaCompileAndRun input/a.fava -asm -trace
```

## What This Stage Establishes

- complete compile-and-run flow
- semantic validation basics
- symbol tables and type rules
- bytecode execution in the Fava VM

## Related Reading

- [Projeto README](../README.md)
- [Part 2 README](../lab08%20-%20Projeto%20parte%202/README.md)
