# Lab 09 - Projeto Parte 3

[![Stage](https://img.shields.io/badge/Fava-Part%203-blue.svg)]()
[![Focus](https://img.shields.io/badge/Focus-Functions%20%26%20Scopes-purple.svg)]()
[![Semantics](https://img.shields.io/badge/Semantics-Multi--Pass-orange.svg)]()

> Most advanced Fava stage, introducing functions, scoped analysis, and a more mature semantic and code-generation architecture.

## Quick Facts

| Item | Details |
|---|---|
| Grammar | `src/Antlr4/Fava.g4` |
| Main class | `src/FavaCode/FavaCompileAndRun.java` |
| Semantic passes | definition and reference visitors |
| Scope support | global and local scope managers |
| Statement file | `tp-3.pdf` |

## Main Files

- `src/FavaCode/Semantic/DefinitionSemanticVisitor.java`
- `src/FavaCode/Semantic/ReferenceSemanticVisitor.java`
- `src/FavaCode/Semantic/Types/FunctionType.java`
- `src/FavaCode/CodeGenerator/Scopes/GlobalScope.java`
- `src/FavaCode/CodeGenerator/Scopes/LocalScope.java`
- `src/FavaCode/CodeGenerator/Scopes/ScopeManager.java`
- `CONTEXT.md`

## What This Stage Adds

- multi-pass semantic validation
- function declarations and calls
- return checking
- local and global scope handling
- stronger compiler organization for advanced language features

## Typical Command

```bash
java FavaCode.FavaCompileAndRun input/T1input.fava -asm -trace
```

## Useful Flags

- `-asm`
- `-trace`
- `-showLexerErrors`
- `-showParserErrors`

## Related Reading

- [Projeto README](../README.md)
- [Repository README](../../README.md)
