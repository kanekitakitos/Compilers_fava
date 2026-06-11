# Lab 04 - Expression Evaluation With ANTLR

[![Lab](https://img.shields.io/badge/Lab-04-blue.svg)]()
[![Focus](https://img.shields.io/badge/Focus-Expression%20Evaluation-purple.svg)]()
[![ANTLR](https://img.shields.io/badge/ANTLR-4.13.2-blue.svg)](https://www.antlr.org/)

> Expression evaluation laboratory split into two versions, comparing listener-based, visitor-based, and parse-tree-property approaches.

## Quick Facts

| Item | Details |
|---|---|
| Versions | `Versão 1` and `versao_2` |
| Main theme | expression semantics |
| Techniques | listener, visitor, parse-tree annotations |
| Statement file | `p4.pdf` |

## Version Summary

| Version | Scope | Key Entry Points |
|---|---|---|
| `Versão 1` | integer arithmetic | `TestLEvaluator`, `TestLEvalVisitor` |
| `versao_2` | richer arithmetic with more operators | `TestExperEvaluator`, `TestExprVisitor` |

## What It Demonstrates

- evaluation using a stack during tree walking
- visitor-based semantic computation
- parse-tree properties for value propagation
- operator precedence and associativity in grammar design

## Main Files

- `Versão 1/src/LExpr.g4`
- `Versão 1/src/TestLEvaluator.java`
- `Versão 1/src/TestLEvalVisitor.java`
- `Versão 1/src/TestLEvaluatorWithProps.java`
- `versao_2/src/Expr.g4`
- `versao_2/src/TestExperEvaluator.java`
- `versao_2/src/TestExprVisitor.java`
- `versao_2/src/TestExprEvaluatorWithProps.java`

## How To Run

Use one of the evaluator classes as the main entry point and optionally pass the path to an input file.

## Related Reading

- [Repository README](../README.md)
- [Lab 05 README](../lab05/README.md)
