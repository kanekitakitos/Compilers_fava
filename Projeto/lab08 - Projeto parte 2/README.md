# Lab 08 - Projeto Parte 2

[![Stage](https://img.shields.io/badge/Fava-Part%202-blue.svg)]()
[![Focus](https://img.shields.io/badge/Focus-Architecture%20Refinement-purple.svg)]()
[![VM](https://img.shields.io/badge/VM-Integrated-orange.svg)]()

> Second Fava stage focused on strengthening the compiler architecture, broadening the validation corpus, and improving code-generation support.

## Quick Facts

| Item | Details |
|---|---|
| Grammar | `src/Antlr4/Fava.g4` |
| Main class | `src/FavaCode/FavaCompileAndRun.java` |
| Extra codegen support | `src/FavaCode/CodeGenerator/Memory.java` |
| Inputs | multiple `.fava` and `.bc` samples |
| Statement file | `tp-2.pdf` |

## Main Improvements Over Part 1

- cleaner internal separation of compiler stages
- explicit memory-oriented support in code generation
- larger collection of test programs
- stronger overall project modularity

## Typical Command

```bash
java FavaCode.FavaCompileAndRun input/T7input.fava -asm -trace
```

## Key Files

- `src/FavaCode/Compiler/FavaCompiler.java`
- `src/FavaCode/CodeGenerator/CodeGen.java`
- `src/FavaCode/CodeGenerator/Memory.java`
- `src/FavaCode/Semantic/SemanticAnalyzerVisitor.java`
- `src/FavaCode/VirtualMachine/FavaVM.java`

## Related Reading

- [Projeto README](../README.md)
- [Part 3 README](../lab09%20-%20Projeto%20parte%203/README.md)
