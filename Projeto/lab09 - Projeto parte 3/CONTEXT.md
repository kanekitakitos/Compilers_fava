# Contexto — TP3 (Fava: Funções + Variáveis Locais)

Este ficheiro resume o estado atual do projeto para a Parte 3 (TP-3), o que já está implementado, o que falta, e quais as escolhas arquiteturais assumidas até agora.

## Objetivo do TP-3 (resumo operacional)

- Estender a linguagem Fava para suportar:
  - Declaração e chamada de funções (incluindo forward calls: chamar antes de declarar).
  - Variáveis locais e nested scopes.
  - `return expr;` e `return;`.
- Alinhar a execução com a VM Fava que passa a ter:
  - Registo `FP` (Frame Pointer).
  - Novos opcodes: `lalloc`, `lload`, `lstore`, `pop`, `call`, `retval`, `ret`.

## Pipeline atual (como o compilador está organizado)

- Entrada: `FavaCode/FavaCompileAndRun.java`
  - Faz parse do CLI e invoca:
    - `FavaCode/Compiler/FavaCompiler.java` (compila)
    - `FavaCode/VirtualMachine/FavaVM.java` (executa o `.bc`)

- Fases dentro de `FavaCompiler`:
  1. Lexer/Parser ANTLR
  2. Semântica (2-pass):
     - Pass 1: `DefinitionSemanticVisitor` (regista assinaturas de funções)
     - Pass 2: `ReferenceSemanticVisitor` (valida usos, tipos, returns, etc.)
  3. Codegen: `CodeGenerator/CodeGen.java` gera bytecode e grava `.bc`

## Gramática (ANTLR) — estado atual

Ficheiro: `src/Antlr4/Fava.g4`

### Já suportado na gramática

- `return expr? ;`
- `function` declarations: `function ID '(' params? ')' ('->' type)? block`
- `functionCall`: `ID '(' exprList? ')'`
- function call como:
  - statement: `functionCall ';'`
  - expressão: `functionCall` dentro de `expr`

### Divergências vs enunciado (ainda não tratadas)

- `prog` ainda é `stat+ EOF` (não impõe “globais antes de funções”, nem “funções obrigatórias”).
- `block` é `{ stat* }` (não impõe “declarações antes de instruções”).

Nota: por agora isto é aceite para acelerar a implementação e validar a VM/CodeGen; o alinhamento estrito pode ser feito mais tarde.

## Semântica — estado atual

Ficheiros:
- `src/FavaCode/Semantic/DefinitionSemanticVisitor.java`
- `src/FavaCode/Semantic/ReferenceSemanticVisitor.java`
- `src/FavaCode/Semantic/SymbolTable.java`
- `src/FavaCode/Semantic/Types/FunctionType.java` (assumido existente no projeto)

### Escolha arquitetural: Two-Pass (DefPhase / RefPhase)

- Pass 1 (Definition):
  - Regista todas as funções na SymbolTable (permite forward calls).
  - Guarda assinatura como `FunctionType(returnType, paramTypes)`.
- Pass 2 (Reference):
  - Valida declarações/uso de variáveis com scopes (`enterScope/exitScope`).
  - Valida `return` dentro de funções:
    - `void` não pode retornar valor.
    - `non-void` exige valor e tipo compatível (com exceção `integer -> real`).
  - Valida chamadas de função:
    - Função com retorno não pode ser statement isolado.
    - Função `void` não pode ser usada como expressão.
    - Número e tipos dos argumentos (com exceção `integer -> real`).

### O que já está “feito”

- Forward calls (por semântica): chamadas conseguem ser validadas mesmo antes da declaração.
- Tabela de símbolos com scopes (Stack de Maps) para variáveis locais.
- `return` e verificação de tipos (inclui exceção `integer -> real`).
- Validação de chamadas de função (arity + type checking + “statement vs expression”).

### O que ainda falta na semântica (importante)

- Garantir “a função retorna de facto” quando tem `-> type`:
  - Atualmente valida cada `return`, mas não garante cobertura total de fluxos (ex.: `if (...) return 1;` sem `else`).
- Ordenação dos erros semânticos por linha:
  - Atualmente imprime durante a visita. O enunciado pede ordenação por número da linha.
- Verificação de `main` e formato do programa:
  - Foi adiado (explicitamente).

## CodeGen — estado atual

Ficheiro: `src/FavaCode/CodeGenerator/CodeGen.java`

### O que já está feito (TP anterior)

- Emissão de bytecode para:
  - Literais (`iconst`, `dconst`, `sconst`, boolean consts)
  - Operações aritméticas/lógicas e casts (via `OpCodeMapper` e regras existentes)
  - `if/else` e `while` com backpatching (`jump`/`jumpf`)
  - Variáveis (modelo atual): `galloc` + `gload/gstore`
  - Constant Pool (strings/doubles)

### O que falta para TP-3 (core)

- Geração de código para funções:
  - “bootstrap” que chama `main` (ou salta para o endereço de `main`).
  - Emissão de blocos de função com endereços estáveis (labels/offsets).
  - Geração de `call`, `ret`, `retval`.
- Variáveis locais e parâmetros via novos opcodes:
  - `lalloc`, `lload`, `lstore`, `pop`.
  - Definir convenção de frame (layout) coerente com a VM.
- Resolução de endereço de funções:
  - Necessário mapear `funcName -> addr` em codegen.
  - Opções típicas:
    - backpatch de `call` (placeholders) ou
    - 2-pass no codegen (primeiro calcula endereços, segundo emite).

### Estado do gestor de memória do CodeGen

Ficheiro: `src/FavaCode/CodeGenerator/Memory.java`

- Atualmente funciona como alocador de “slots” para variáveis em escopos (LIFO), mas o acesso no bytecode é global (gload/gstore).
- Para TP-3, este módulo provavelmente vai ser dividido (ou encapsulado) em:
  - memória global (para `g*`)
  - memória local por função (offsets relativos ao `FP`, para `l*`)

## Virtual Machine — estado atual

Ficheiros:
- `src/FavaCode/VirtualMachine/VirtualMachine.java`
- `src/FavaCode/VirtualMachine/OpCode.java`

### Já está feito

- Decoder de bytecode (constant pool + instruções).
- Execução de opcodes do TP anterior (`iconst`, `galloc`, `gload`, `gstore`, `jump`, `jumpf`, etc.).
- Enum `OpCode` já contém os novos opcodes do TP-3.

### Falta (core TP-3)

- Implementar no `VirtualMachine.exec_inst` e lógica associada:
  - registo `FP`
  - `lalloc`, `lload`, `lstore`, `pop`, `call`, `retval`, `ret`
- Definir com precisão o “Activation Record / frame layout”:
  - Onde ficam guardados FP antigo e return address.
  - Como calcular `Stack[FP + addr]` para locals/parâmetros.

## Escolhas feitas até agora (decisões)

### 1) SymbolTable unificada para variáveis + funções

- A SymbolTable guarda `FavaType` e, para funções, guarda `FunctionType`.
- A distinção “é variável” vs “é função” é feita verificando o tipo (`instanceof FunctionType`) na semântica.

### 2) Suporte a forward declarations

- Adotado via two-pass na semântica:
  - `DefinitionSemanticVisitor` antes de `ReferenceSemanticVisitor`.

### 3) Coerção permitida

- Em semântica: compatível quando o esperado é `real` e o fornecido é `integer`.
- Em codegen: já existe emissão de `itod` em assignments globais; terá de ser replicado em:
  - argumentos e returns

## Próximos passos (ordem recomendada para “ficar a funcionar”)

1. Implementar na VM os novos opcodes e `FP`.
2. Definir convenção do frame (documentar e manter consistente).
3. Implementar CodeGen para funções:
   - tabela `funcName -> addr`
   - emissão de `call/ret/retval`
   - locals/params com `lalloc/lload/lstore/pop`
4. Ajustar semântica apenas o necessário para desbloquear execução (ordenar erros e “main” podem ficar para depois).

## Notas de validação (inputs existentes)

- Existe um exemplo em `input/a.fava` com vários casos de erro semântico (duplas declarações, tipos errados, arity errada, uso de função como variável, etc.).

