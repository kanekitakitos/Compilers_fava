package FavaCode.Semantic;

import FavaCode.Parser.Fava.FavaBaseVisitor;
import FavaCode.Parser.Fava.FavaParser.*;
import FavaCode.Semantic.Types.*;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

/**
 * Visitante central do Analisador Semântico da linguagem Fava.
 * É responsável por percorrer a Árvore de Análise Sintática (AST) criada na fase do Parser (ANTLR4)
 * e detetar erros semânticos, além de inferir e decorar os nós com informações de tipagem, utilizando
 * o decorador nativo {@link ParseTreeProperty}.
 *
 * <p>Nesta fase são verificadas infrações como: uso de variáveis não declaradas ({@link SymbolTable}),
 * incompatibilidade em operações matemáticas ({@link TypeRules}) ou condições inválidas em ciclos e ifs.</p>
 *
 * <p>Referências Académicas:</p>
 * <ul>
 *   <li><b>Nystrom (Crafting Interpreters), Cap. 11 "Resolving and Binding":</b>
 *       Este Visitor simula as passagens de resolução estática na AST, tal como na linguagem `Lox`,
 *       analisando as variáveis antes da fase geradora de código.</li>
 * </ul>
 * <p>
 * ### Exemplo de Uso
 * <pre>{@code
 * // Exemplo no FavaCompiler:
 * ParseTree tree = parser.prog();
 * SemanticAnalyzerVisitor semanticAnalyzer = new SemanticAnalyzerVisitor();
 *
 * semanticAnalyzer.visit(tree);
 *
 * if (semanticAnalyzer.getSemanticErrors() == 0) {
 *     // Passa para a Geração de Código fornecendo os Tipos decorados
 *     CodeGen codeGen = new CodeGen(semanticAnalyzer.getTypesTree());
 * }
 * }</pre>
 *
 * @see FavaBaseVisitor
 * @see FavaType
 * @see SymbolTable
 * @see TypeRules
 */
public class SemanticAnalyzerVisitor extends FavaBaseVisitor<FavaType> {
    /**
     * Gestor dinâmico que mantém as referências às variáveis e controla escopos locais na RAM do compilador.
     */
    private SymbolTable symbolTable = new SymbolTable();

    /**
     * O motor que possui as lógicas da linguagem sobre o que é válido misturar numa operação.
     */
    private TypeRules typeRules = new TypeRules();

    /**
     * Coleção anotativa do ANTLR4. Atua como um "Post-it" num nó de árvore: cola um tipo ao identificador para a posterior Geração de Código consultar (AST Decorada).
     */
    private ParseTreeProperty<FavaType> typesTree = new ParseTreeProperty<>();


    private int semanticErros = 0;

//*--------------- Types ------------------------------------------------------------------------

    /**
     * Processa a visita do analisador a um literal inteiro no código.
     * Decora o nó da AST com o Singleton correspondente ({@link IntegerType}) e não propaga a visita
     * a subnós por este já ser uma "folha".
     *
     * @param ctx O contexto específico referenciando o literal Inteiro.
     * @return O {@link IntegerType} associado a esta árvore, para ser lido caso faça parte de operações pai.
     */
    @Override
    public FavaType visitIntegerExpr(IntegerExprContext ctx) {
        return saveType(ctx, IntegerType.INSTANCE);
    }

    /**
     * Acionado caso a Árvore Sintática reporte uma declaração do literal Booleano (ex: `true` / `false`).
     *
     * @param ctx O contexto lógico.
     * @return Transmite a propriedade {@link BoolType} acima no ramo da AST para a avaliação recursiva de operadores lógicos.
     */
    @Override
    public FavaType visitBoolExpr(BoolExprContext ctx) {
        return saveType(ctx, BoolType.INSTANCE);
    }

    /**
     * Visita uma expressão que contém uma string literal (texto delimitado por aspas).
     *
     * @param ctx O contexto da String do ANTLR4.
     * @return Decora o token com {@link StringType} para garantir que se for usado numa adição se converta num "sconcat".
     */
    @Override
    public FavaType visitStringExpr(StringExprContext ctx) {
        return saveType(ctx, StringType.INSTANCE);
    }

    /**
     * Visita expressões literais de ponto flutuante (Reais / Double).
     *
     * @param ctx O contexto que aponta para o número no texto original.
     * @return A classe associada aos reais {@link RealType}.
     */
    @Override
    public FavaType visitRealExpr(RealExprContext ctx) {
        return saveType(ctx, RealType.INSTANCE);
    }

    /**
     * É chamado quando uma variável é lida (invocação de identificador num cálculo).
     * Esta é uma visita crítica para inferir se a variável invocada existe e extrair as suas regras.
     *
     * @param ctx O contexto da expressão Id.
     * @return O {@link FavaType} extraído se for válido ou {@code null} se a variável invocar o tratador de erros de não declaração.
     */
    @Override
    public FavaType visitIdExpr(IdExprContext ctx) {
        String varName = ctx.ID().getText();

        // 2. Ir buscar o tipo da Tabela e COLAR O POST-IT na typesTree para o CodeGen!
        FavaType resultType = checkIfExistsInSymbolTable(varName, ctx.start.getLine());

        return saveType(ctx, resultType);
    }

//*--------------- Methods ------------------------------------------------------------------------
//* KISS - Keep It Simple, Stupid

    /**
     * Ao passar numa expressão compactada dentro de parênteses '(' expr ')', a ordem da árvore já tem esse fator processado.
     * Este Visitor apenas "túneis" os tipos para a raiz subjacente, avaliando internamente.
     *
     * @param ctx O contexto dos Parêntesis no Parser do ANTLR4.
     * @return Tipagem da expressão envolvida nos parêntesis passados sem ser alterada (ex: (2+2) -> retorna integer).
     */
    @Override
    public FavaType visitParensExpr(ParensExprContext ctx) {
        FavaType resultType = visit(ctx.expr());
        return saveType(ctx, resultType);
    }

    /**
     * Reunião de visitas de premissa matemática e com alta precedência (Mult, Div e Resto).
     * Envia o problema para a tratativa genérica do helper {@link #processBinaryOperation}.
     *
     * @param ctx O contexto matemático.
     * @return O tipo inferido.
     */
    @Override
    public FavaType visitMulDivModExpr(MulDivModExprContext ctx) {
        return processBinaryOperation(ctx, visit(ctx.expr(0)), visit(ctx.expr(1)), ctx.op.getText());
    }

    /**
     * Visita uma expressão de adição ou subtração básica.
     *
     * @param ctx O contexto da adição matemática da linguagem.
     * @return Tipagem Fava final que representa as operações (int, double ou nulo se ilegal).
     */
    @Override
    public FavaType visitAddSubExpr(AddSubExprContext ctx) {
        return processBinaryOperation(ctx, visit(ctx.expr(0)), visit(ctx.expr(1)), ctx.op.getText());
    }

    /**
     * Processo de tratamento focado no operador '||', concatenando as strings.
     *
     * @param ctx A regra gramatical Concat na árvore.
     * @return A classe associada StringType (por intermédio das {@link TypeRules}).
     */
    @Override
    public FavaType visitConcatExpr(ConcatExprContext ctx) {
        return processBinaryOperation(ctx, visit(ctx.expr(0)), visit(ctx.expr(1)), ctx.op.getText());
    }

    /**
     * Despoleta processamento relacional nos operandos para prever conversão na instrução `ilt`, `ileq`, etc. (ex: < ou >=).
     *
     * @param ctx Contexto da árvore dos comparadores de grandeza.
     * @return O booleano inferido do mapeamento lógico (verdadeiro/falso consoante tipagem permitida).
     */
    @Override
    public FavaType visitRelationalExpr(RelationalExprContext ctx) {
        return processBinaryOperation(ctx, visit(ctx.expr(0)), visit(ctx.expr(1)), ctx.op.getText());
    }

    /**
     * Avalia instâncias operacionais sobre a igualdade, gerando booleanos resultantes ( = , <>).
     *
     * @param ctx Igualdade de operandos.
     * @return Tipo de retorno BoolType validado.
     */
    @Override
    public FavaType visitEqualityExpr(EqualityExprContext ctx) {
        return processBinaryOperation(ctx, visit(ctx.expr(0)), visit(ctx.expr(1)), ctx.op.getText());
    }

    /**
     * Processa a compatibilidade entre condições do AND lógico.
     *
     * @param ctx Nó de contexto ANTLR4 correspondente ao AND.
     * @return Instância associada a verdadeiro / falso, mediante o mapa estático global de tipos ({@link BoolType}).
     */
    @Override
    public FavaType visitAndExpr(AndExprContext ctx) {
        return processBinaryOperation(ctx, visit(ctx.expr(0)), visit(ctx.expr(1)), ctx.op.getText());
    }

    /**
     * Visita uma ramificação de expressão OR (disjunção lógica).
     *
     * @param ctx Contexto da expressão "or".
     * @return Valida se a expressão final inferida de ambos os ramos pode ser considerada Booleana pela linguagem.
     */
    @Override
    public FavaType visitOrExpr(OrExprContext ctx) {
        return processBinaryOperation(ctx, visit(ctx.expr(0)), visit(ctx.expr(1)), ctx.op.getText());
    }


//*--------------- Stat ------------------------------------------------------------------------

    /**
     * Valida um laço lógico do tipo ciclo (while), verificando se a estrutura relacional iterada retorna uma condição avaliável como Boolean (TRUE/FALSE).
     *
     * @param ctx Contexto geral do While.
     * @return Nulo intencional (Statments de blocos ou de ciclo não têm "tipo devolvido" intrínseco).
     */
    @Override
    public FavaType visitWhileStat(WhileStatContext ctx) {
        checkBooleanCondition(ctx.expr(), "while");
        visit(ctx.stat());
        return null;
    }

    /**
     * Avalia condicional IF e ELSE para perceber as quebras lógicas, certificando-se
     * de que a condicional que dita a passagem pode ser logicamente inferida a algo Booleano.
     *
     * @param ctx O ramo contextual do IfStatement.
     * @return O valor natural da avaliação de Statement, ou seja, null.
     */
    @Override
    public FavaType visitIfStat(IfStatContext ctx) {
        checkBooleanCondition(ctx.expr(), "if");
        visit(ctx.stat(0));

        if (ctx.stat().size() > 1)
            visit(ctx.stat(1));

        return null;
    }

    /**
     * Ponto focal do Analisador. Gere como novas variáveis se apresentam à {@link SymbolTable}.
     * Percorre as inicializações diretas no próprio Statment de declaração de tipo.
     *
     * @param ctx Contexto das declarações globais de novas variáveis.
     * @return Processado e inserido na HashTable iterativa, devolvendo null.
     */
    @Override
    public FavaType visitVarDeclaration(VarDeclarationContext ctx) {
        // 1. Validar o tipo base
        FavaType varType = FavaType.resolve(ctx.type().getText());
        if (varType == null) {
            errorTypeDoesNotExist(ctx.start.getLine(), ctx.type().getText());
            return null;
        }

        // 2. Iterar sobre cada inicialização individual (ex: x:=3.14, y, soma)
        for (VarInitContext initCtx : ctx.varInit()) {

            String varName = initCtx.ID().getText();

            // 2.1 Prevenir dupla declaração
            if (this.symbolTable.exists(varName)) {
                errorAlreadyDeclared(initCtx.start.getLine(), varName);
                continue;
            }

            // 2.2 Adicionar à tabela de símbolos local
            this.symbolTable.add(varName, varType);

            // 2.3 Se este ID em específico tiver uma expressão de atribuição (:=), validamos se a regra obedece!
            if (initCtx.expr() != null) {
                FavaType exprType = visit(initCtx.expr());
                processAssignment(initCtx, varType, exprType);
            }
        }
        return null;
    }

    /**
     * Processa atualizações nas variáveis da Stack de dados.
     * Puxa do contexto da Stack se a variável já existe (não se pode atribuir "A = 5;" se A não foi declarada com o seu tipo numérico primeiro).
     *
     * @param ctx Contexto do símbolo Atribuição ":=".
     * @return null depois de confirmar as validações.
     */
    @Override
    public FavaType visitAssignStat(AssignStatContext ctx) {
        String varName = ctx.ID().getText();

        FavaType varType = checkIfExistsInSymbolTable(varName, ctx.start.getLine());
        FavaType exprType = visit(ctx.expr());

        return processAssignment(ctx, varType, exprType);
    }

    /**
     * Entra num Bloco "{ ... }". Gere as criações temporárias em ramificações do código que são localizadas
     * e destruídas mediante o avanço e término do seu percurso pelo compilador.
     *
     * @param ctx A declaração contida no bloco referenciado.
     * @return null.
     */
    @Override
    public FavaType visitBlock(BlockContext ctx) {
        // 1. Abrir a nova "caixa" (novo HashMap na Stack de Escopos)
        this.symbolTable.enterScope();

        // 2. Visitar tudo o que está lá dentro (que populará este contexto LIFO)
        visitChildren(ctx);

        // 3. Fechar a "caixa" (destruir/esquecer as variáveis locais que lá moram)
        this.symbolTable.exitScope();

        return null;
    }


//!---------------------------------------- Metodos Helpers ---------------------------------------------------------------

    /**
     * Decora, ou seja, associa o tipo inferido a um nó de ParserRuleContext específico da Árvore de Análise Sintática (AST).
     * Padrão Decorator utilizado intensivamente pelo motor do Geração de Código para identificar rapidamente se chama
     * um `imult` ou `dmult` sem recalcular esta lógica.
     *
     * @param ctx  O contexto da árvore do compilador (ParserRuleContext).
     * @param type O objeto a colar na AST com referência direta a esse nó.
     * @return Retorna a referência que acabou de guardar.
     */
    private FavaType saveType(org.antlr.v4.runtime.ParserRuleContext ctx, FavaType type) {
        if (type != null) {
            typesTree.put(ctx, type);
        }
        return type;
    }

    /**
     * Valida de imediato se o utilizador não está a fazer referência a variáveis inexistentes (ex: `A = B + C` onde `B` não foi declarado).
     * Chama métodos diretos de erro no console se a regra falhar.
     *
     * @param varName O nome referenciado para avaliar a validade.
     * @param line    Linha atual em que ocorreu a referencia.
     * @return O {@link FavaType} extraído dos registos locais.
     */
    private FavaType checkIfExistsInSymbolTable(String varName, int line) {
        FavaType type = this.symbolTable.getType(varName);

        if (type == null) {
            errorNotDeclared(line, varName);
            return null;
        }

        return type;
    }

    /**
     * Método auxiliar utilitário para validar estritamente se o resultado é uma expressão puramente booleana, requerida
     * em controlos de fluxo. Lança logs de anomalia se a verificação quebrar a semântica de fluxo do compilador.
     *
     * @param exprCtx  Expressão da Condição (ex: if, while).
     * @param statName Nome informativo ou keyword para logs da infração ("while", "if").
     */
    private void checkBooleanCondition(ExprContext exprCtx, String statName) {
        FavaType condType = visit(exprCtx);
        if (condType != null && !condType.getName().equals("bool")) {
            errorConditionNotBoolean(exprCtx.start.getLine(), statName);
        }
    }

    /**
     * Helper abstrato para processar validade em operativas de assignment com base no comportamento ditado em {@link TypeRules}.
     * Avalia coerção e promoção (como int para double).
     *
     * @param ctx      O nó da gramática associada à atribuição.
     * @param varType  O lado Esquerdo e variável recetora.
     * @param exprType A expressão da qual vai derivar a atribuição (direita).
     * @return Associa à arvore ou devolve null se o retorno disparar erros.
     */
    private FavaType processAssignment(org.antlr.v4.runtime.ParserRuleContext ctx, FavaType varType, FavaType exprType) {
        if (varType == null || exprType == null) return null;

        FavaType resultType = typeRules.getResultType(":=", varType, exprType);

        if (resultType == null) {
            // Trocamos a ordem para reportar: Alvo primeiro, Fonte depois
            errorInvalidBinaryOperation(ctx.start.getLine(), ":=", varType.getName(), exprType.getName());
        }

        return saveType(ctx, resultType);
    }

    /**
     * Padrão utilizado para extrair recursivamente do visitor semântico se ele próprio é capaz de interagir ou avaliar unários prefixos.
     * Exemplo lógico disto passa pelo sinal de negativo '-' a preceder a variável (`A = -5;`).
     *
     * @param ctx A declaração unária de raiz.
     * @return Sub-Expressão tratada logicamente e armazenada na arvore ou gerando null de invalidação.
     */
    @Override
    public FavaType visitUnaryExpr(UnaryExprContext ctx) {
        FavaType exprType = visit(ctx.expr());
        String op = ctx.op.getText().toLowerCase();

        FavaType resultType = typeRules.getResultType(op, exprType);

        if (resultType == null) {
            errorInvalidUnaryOperation(ctx.start.getLine(), op, exprType != null ? exprType.getName() : "unknown");
        }

        return saveType(ctx, resultType);
    }

    /**
     * Reutilização do código massivo proveniente de lógicas dos binários básicos, agrupando debaixo
     * da cobertura uniforme da inferência estática providenciada pela framework {@link TypeRules}.
     *
     * @param ctx   Contexto da execução base na Tree.
     * @param left  Ramificação de Tipagem Esquerda processada por sub-avaliação do visitor.
     * @param right Tipo na Direita devolvido pelos branches filhos deste Visitor.
     * @param op    String operadora avaliada.
     * @return A classe associada após o processamento, ou nulo se ilegal perante a linguagem (ex: Inteiro / Texto).
     */
    private FavaType processBinaryOperation(org.antlr.v4.runtime.ParserRuleContext ctx, FavaType left, FavaType right, String op) {
        if (left == null || right == null) return null;

        FavaType resultType = typeRules.getResultType(op.toLowerCase(), left, right);

        if (resultType == null) {
            errorInvalidBinaryOperation(ctx.start.getLine(), op, left.getName(), right.getName());
        }

        return saveType(ctx, resultType);
    }


//!---------------------------------------- Tratamento de Erros (Facade) --------------------------------------------------

    /**
     * Ponto central (Facade) e único de registo de erros do analisador semântico na Framework.
     * Incrementa o contador global de erros críticos e formata o log de print gerado ao utilizador para padronização.
     *
     * @param line    A linha física recolhida onde ocorre a falta sintática/semântica.
     * @param message Informação contendo os limites e origens da violação dos blocos e lógicas.
     */
    private void reportError(int line, String message) {
        this.semanticErros++;
        System.out.println("error in line " + line + ": " + message);
    }

    /**
     * Helper reportando variável ou símbolo em duplicado no dicionário.
     */
    private void errorAlreadyDeclared(int line, String varName) {
        reportError(line, varName + " already declared");
    }

    /**
     * Dispara erro sempre que uma alocação requer identificadores globais não criados.
     */
    private void errorNotDeclared(int line, String varName) {
        reportError(line, varName + " not declared");
    }

    /**
     * Injeta notificação de não-reconhecimento estrito de tipologias.
     */
    private void errorTypeDoesNotExist(int line, String typeName) {
        reportError(line, "type '" + typeName + "' does not exist");
    }

    /**
     * Traça incompatibilidades caso um Ciclo Iterativo seja intercetado usando cálculos matemáticos literais em vez de testes Condicionais (Bool).
     */
    private void errorConditionNotBoolean(int line, String statName) {
        reportError(line, statName + " expression must be of type bool");
    }

    /**
     * Impede interações lógicas incorretas baseadas nas definições explícitas de TypeRules (ex: Booleanos Multiplicados com Strings).
     */
    private void errorInvalidBinaryOperation(int line, String op, String leftType, String rightType) {
        reportError(line, "operator " + op + " is invalid between " + leftType + " and " + rightType);
    }

    /**
     * Helper para a mesma interdição acima, focada porém na limitação de manipulação singular de tipos e expressões simples por operadores Unitários.
     */
    private void errorInvalidUnaryOperation(int line, String op, String exprType) {
        reportError(line, "operator " + op + " is invalid for " + exprType);
    }

//!---------------------------------------- Getters -----------------------------------------------------------------------

    /**
     * Retorna a quantidade de problemas/incoerências processadas que se prenderam com a execução da visita.
     * Informa à {@link FavaCode.Compiler.FavaCompiler} se deve impedir a continuação para a geração da CodeGen (e parar).
     *
     * @return O número exato contabilizado da semântica invalida gerada.
     */
    public int getSemanticErrors() {
        return semanticErros;
    }

    /**
     * Resgata a árvore de dados com todas as propriedades já associadas e avaliadas na memória, de cariz fundamental
     * para a próxima fase compiladora do Gerador de Código.
     * Na Geração de Código Bytecode Fava, as ramificações semânticas devem extrair esta árvore para inferir a
     * conversão final de um OpCode baseado num Literal (ex: extrai desta tree se é `sconst` ou `iconst`).
     *
     * @return O objeto {@link ParseTreeProperty} (Map em C# equivalente ao Decorator do Java-ANTLR4) que detém internamente todos os {@link FavaType} correspondentes.
     */
    public ParseTreeProperty<FavaType> getTypesTree() {
        return typesTree;
    }
}
