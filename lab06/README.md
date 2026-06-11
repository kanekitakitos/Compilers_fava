# Lab 06 - Compiler And Virtual Machine

[![Lab](https://img.shields.io/badge/Lab-06-blue.svg)]()
[![Focus](https://img.shields.io/badge/Focus-Bytecode%20%26%20VM-purple.svg)]()
[![Java](https://img.shields.io/badge/Java-Compiler-orange.svg)]()

> Full educational compiler pipeline for a small arithmetic language, including parsing, bytecode generation, and execution on a custom stack-based virtual machine.

## Quick Facts

| Item | Details |
|---|---|
| Grammar | `src/Calc.g4` |
| Compiler entry point | `src/calCompiler.java` |
| VM entry point | `src/calVM.java` |
| Sample programs | `inputs/in1.cal`, `inputs/in2.cal` |
| Statement file | `p6.pdf` |

## Pipeline Overview

1. parse a `.cal` source file
2. generate stack-based bytecode
3. save the output as `.calbc`
4. execute the bytecode on the VM

## Main Files

- `src/Calc.g4`
- `src/calCompiler.java`
- `src/calVM.java`
- `src/CodeGenerator/CodeGen.java`
- `src/VM/vm.java`

## Typical Commands

```bash
java calCompiler inputs/in1.cal -asm
java calVM inputs/in1.calbc -trace
```

## Useful Flags

| Flag | Meaning |
|---|---|
| `-asm` | prints generated assembly-like output |
| `-trace` | shows virtual machine execution trace |

## Related Reading

- [Repository README](../README.md)
- [Projeto README](../Projeto/README.md)
