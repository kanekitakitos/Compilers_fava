package FavaCode.Semantic;

import FavaCode.Parser.Fava.FavaBaseVisitor;
import FavaCode.Parser.Fava.FavaParser.*;
import FavaCode.Semantic.Types.*;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;

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
public class ReferenceSemanticVisitor extends FavaBaseVisitor<FavaType> {
    /**
     * Gestor dinâmico que mantém as referências às variáveis e controla escopos locais na RAM do compilador.
     */
    private SymbolTable symbolTable;

    /**
     * O motor que possui as lógicas da linguagem sobre o que é válido misturar numa operação.
     */
    private TypeRules typeRules = new TypeRules();

    /**
     * Decorador de tipos da AST (ANTLR4 {@link ParseTreeProperty}).
     *
     * <p>Funciona como uma tabela associativa {@code nó -> tipo inferido} para nós relevantes da árvore
     * (principalmente expressões e alguns contextos de statement que carregam tipo), permitindo que a fase de
     * Geração de Código consulte o tipo já inferido sem recalcular regras semânticas.</p>
     *
     * <p>Este padrão ("AST decorada") simplifica o {@code CodeGen}: ele passa a escolher opcodes
     * (ex.: {@code iadd} vs {@code dadd}) e aplicar coerções implícitas (ex.: {@code integer -> real})
     * apenas olhando para esta propriedade.</p>
     *
     * @see #saveType(org.antlr.v4.runtime.ParserRuleContext, FavaType)
     * @see FavaCode.CodeGenerator.CodeGen
     */
    private ParseTreeProperty<FavaType> typesTree = new ParseTreeProperty<>();


    /**
     * Estado interno: tipo de retorno esperado da função atualmente em validação.
     * {@code null} representa função void.
     */
    private FavaType currentFunctionReturnType = null;

    /**
     * Estado interno: nome da função atualmente em validação.
     */
    private String currentFunctionName = null;

    /**
     * Conjunto de nós de função inválidos já sinalizados na 1ª passagem (Definition Phase).
     *
     * <p>Serve para evitar cascatas de erros: se a assinatura/declaração já é inválida,
     * a Reference Phase pode ignorar o corpo da função.</p>
     */
    private final Set<FunctionDeclContext> invalidFunctionDeclarations;

    /**
     * Contador interno de erros semânticos registados pelo visitor.
     */
    private int semanticErros = 0;

    /**
     * Sink opcional de erros (injeção de dependência).
     *
     * <p>Quando definido, os erros são recolhidos externamente (ex.: lista no {@code FavaCompiler})
     * em vez de serem impressos diretamente.</p>
     */
    private final BiConsumer<Integer, String> errorSink;

    /**
     * Constrói o visitor semântico com reporting direto para consola.
     *
     * @param symbolTable Tabela de símbolos partilhada entre as passagens semânticas.
     */

    public ReferenceSemanticVisitor(SymbolTable symbolTable)
    {
        this(symbolTable, null);
    }
    /**
     * Constrói o visitor semântico com um sink de erros opcional.
     *
     * @param symbolTable Tabela de símbolos partilhada entre as passagens semânticas.
     * @param errorSink   Handler opcional para recolha de erros (linha, mensagem).
     */

    public ReferenceSemanticVisitor(SymbolTable symbolTable, BiConsumer<Integer, String> errorSink)
    {
        this(symbolTable, errorSink, Collections.emptySet());
    }
    /**
     * Constrói o visitor semântico com sink de erros e lista de funções inválidas a ignorar.
     *
     * @param symbolTable                Tabela de símbolos partilhada entre as passagens semânticas.
     * @param errorSink                  Handler opcional para recolha de erros (linha, mensagem).
     * @param invalidFunctionDeclarations Conjunto de nós {@link FunctionDeclContext} inválidos na Definition Phase.
     */

    public ReferenceSemanticVisitor(SymbolTable symbolTable, BiConsumer<Integer, String> errorSink, Set<FunctionDeclContext> invalidFunctionDeclarations)
    {
        this.symbolTable = symbolTable;
        this.errorSink = errorSink;
        this.invalidFunctionDeclarations = (invalidFunctionDeclarations != null) ? invalidFunctionDeclarations : Collections.emptySet();
    }

//*--------------- Program ------------------------------------------------------------------------

    @Override
    public FavaType visitProg(ProgContext ctx) {
        FavaType mainType = symbolTable.getType("main");
        if (!(mainType instanceof FunctionType)) {
            errorMissingMain(ctx.stop.getLine());
        }
        return visitChildren(ctx);
    }


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

//*--------------- Functions ------------------------------------------------------------------------

    /**
     * Valida uma declaração de função (corpo + regras associadas).
     * Assume que a assinatura já foi registada na Definition Phase.
     */
    @Override
    public FavaType visitFunctionDecl(FunctionDeclContext ctx) {
        String funcName = ctx.ID().getText();
        if (invalidFunctionDeclarations.contains(ctx)) {
            return null;
        }

        FunctionType funcType = resolveFunctionType(funcName, ctx.getStart().getLine());
        if (funcType == null) return null;

        enterFunctionContext(funcName, funcType);
        try {
            bindFormalParameters(ctx.formalParameters());
            visit(ctx.block());

            if (currentFunctionReturnType != null && !guaranteesReturn(ctx.block())) {
                errorMissingReturnInFunction(ctx.block().getStop().getLine(), currentFunctionName);
            }
        } finally {
            exitFunctionContext();
        }

        return null;
    }

    /**
     * Valida uma instrução return dentro de um corpo de função.
     * Regras:
     * - Em função void: "return expr;" é inválido.
     * - Em função non-void: "return;" é inválido (falta valor).
     * - Tipos devem ser compatíveis (com exceção integer -> real).
     */
    @Override
    public FavaType visitReturnStat(ReturnStatContext ctx) {
        validateReturn(ctx);
        return null;
    }

    /**
     * Tratamento quando a chamada é um Statement (ex: print_menu(); ).
     */
    @Override
    public FavaType visitFunctionCallStat(FunctionCallStatContext ctx) {
        validateFunctionCall(ctx.functionCall(), true);
        return null;
    }

    /**
     * Tratamento quando a chamada é uma Expressão (ex: a := fatorial(5) + 1; )
     */
    @Override
    public FavaType visitFunctionCallExpr(FunctionCallExprContext ctx) {
        FunctionType funcType = validateFunctionCall(ctx.functionCall(), false);

        if (funcType == null) return null;
        if (funcType.getReturnType() == null) return null;

        return saveType(ctx, funcType.getReturnType());
    }

//*--------------- Function Helpers ------------------------------------------------------------------------

    /**
     * Resolve o identificador de uma função a partir da {@link SymbolTable} e garante que o símbolo existe e é do tipo {@link FunctionType}.
     *
     * Regras aplicadas:
     * - Se não existir símbolo com este nome, reporta erro semântico.
     * - Se existir mas não for uma função (ex.: variável com o mesmo nome), reporta erro semântico.
     *
     * @param funcName Nome da função.
     * @param line     Linha onde a referência ocorre (para reporting).
     * @return A assinatura {@link FunctionType} ou {@code null} se inválido.
     */
    private FunctionType resolveFunctionType(String funcName, int line) {
        FavaType type = symbolTable.getType(funcName);
        if (type == null) {
            errorNotDeclared(line, funcName);
            return null;
        }

        if (!(type instanceof FunctionType)) {
            errorNotDeclared(line, funcName);
            return null;
        }

        return (FunctionType) type;
    }

    /**
     * Entra no contexto semântico de uma função:
     * - define o tipo de retorno esperado para validações de {@code return}
     * - abre um novo scope para parâmetros e variáveis locais do corpo
     *
     * @param funcType Assinatura da função atualmente visitada.
     */
    private void enterFunctionContext(String funcName, FunctionType funcType) {
        currentFunctionName = funcName;
        currentFunctionReturnType = funcType.getReturnType();
        symbolTable.enterScope();
    }

    /**
     * Sai do contexto semântico de uma função:
     * - fecha o scope local
     * - limpa o estado do tipo de retorno atual
     */
    private void exitFunctionContext() {
        symbolTable.exitScope();
        currentFunctionReturnType = null;
        currentFunctionName = null;
    }

    /**
     * Regista os parâmetros formais da função como variáveis no scope local atual.
     *
     * Regras aplicadas:
     * - parâmetros duplicados no mesmo cabeçalho são erro semântico
     * - os tipos são resolvidos por {@link FavaType#resolve(String)}
     *
     * @param ctx Contexto do parser que contém a lista de parâmetros formais; pode ser {@code null}.
     */
    private void bindFormalParameters(FormalParametersContext ctx) {
        if (ctx == null) return;

        for (FormalParameterContext paramCtx : ctx.formalParameter()) {
            String paramName = paramCtx.ID().getText();
            FavaType paramType = FavaType.resolve(paramCtx.type().getText());

            if (symbolTable.exists(paramName)) {
                errorAlreadyDeclared(paramCtx.getStart().getLine(), paramName);
            } else {
                symbolTable.add(paramName, paramType);
            }
        }
    }

    /**
     * Valida um {@code return} dentro de uma função, usando {@link #currentFunctionReturnType} como contrato.
     *
     * Regras aplicadas:
     * - Em função void (return type == null): {@code return expr;} é inválido.
     * - Em função não-void: {@code return;} é inválido (falta valor).
     * - Tipos devem ser compatíveis (ver {@link TypeRules#isCompatible(FavaType, FavaType)}).
     *
     * @param ctx Contexto do parser da instrução return.
     */
    private void validateReturn(ReturnStatContext ctx) {
        if (ctx.expr() == null) {
            if (currentFunctionReturnType != null) {
                errorFunctionMustReturnType(ctx.getStart().getLine(), currentFunctionName, currentFunctionReturnType.getName());
            }
            return;
        }
        FavaType exprType = visit(ctx.expr());
        if (exprType == null) {
            exprType = typesTree.get(ctx.expr());
        }

        if (currentFunctionReturnType == null) {
            errorFunctionDoesNotReturnValue(ctx.getStart().getLine(), currentFunctionName);
            return;
        }

        if (exprType == null) {
            return;
        }

        if (!typeRules.isCompatible(currentFunctionReturnType, exprType)) {
            errorFunctionMustReturnType(ctx.getStart().getLine(), currentFunctionName, currentFunctionReturnType.getName());
        }
    }

    /**
     * Determina se um bloco garante retorno (isto é, existe um caminho que termina com {@code return expr;}).
     *
     * <p>Nota: esta verificação é estrutural (baseada na AST) e não constrói um grafo completo de fluxo de controlo.
     * Por simplicidade, o algoritmo para no primeiro statement que "garante return".</p>
     *
     * @param ctx Bloco a analisar.
     * @return {@code true} se houver garantia de retorno; caso contrário {@code false}.
     */
    private boolean guaranteesReturn(BlockContext ctx) {
        for (StatContext stat : ctx.stat()) {
            if (guaranteesReturn(stat)) return true;
        }
        return false;
    }

    /**
     * Determina se um statement garante retorno em todos os caminhos relevantes.
     *
     * <p>Regras aplicadas:
     * <ul>
     *   <li>{@code return expr;} garante retorno.</li>
     *   <li>{@code if} só garante retorno quando existe {@code else} e ambos os ramos garantem retorno.</li>
     *   <li>{@code while true} pode garantir retorno se o corpo garantir retorno; outros whiles não garantem.</li>
     * </ul>
     *
     * @param ctx Statement a analisar.
     * @return {@code true} se o statement garantir retorno; caso contrário {@code false}.
     */
    private boolean guaranteesReturn(StatContext ctx) {
        if (ctx instanceof ReturnStatContext) {
            ReturnStatContext returnCtx = (ReturnStatContext) ctx;
            return returnCtx.expr() != null;
        }

        if (ctx instanceof BlockStatContext) {
            BlockStatContext blockCtx = (BlockStatContext) ctx;
            return guaranteesReturn(blockCtx.block());
        }

        if (ctx instanceof IfStatContext) {
            IfStatContext ifCtx = (IfStatContext) ctx;
            if (ifCtx.stat().size() < 2) return false;
            return guaranteesReturn(ifCtx.stat(0)) && guaranteesReturn(ifCtx.stat(1));
        }

        if (ctx instanceof WhileStatContext) {
            WhileStatContext whileCtx = (WhileStatContext) ctx;
            if (whileCtx.expr() instanceof BoolExprContext) {
                BoolExprContext boolExpr = (BoolExprContext) whileCtx.expr();
                if ("true".equalsIgnoreCase(boolExpr.BOOLEAN().getText())) {
                    return guaranteesReturn(whileCtx.stat());
                }
            }
            return false;
        }

        return false;
    }


    /**
     * Helper centralizado para validar qualquer chamada de função (seja Stat ou Expr).
     *
     * Regras aplicadas:
     * - a função tem de existir e ser {@link FunctionType}
     * - se a chamada for statement: a função tem de ser void
     * - se a chamada for expressão: a função tem de ter retorno
     * - número de argumentos tem de coincidir
     * - tipos de argumentos têm de ser compatíveis (ver {@link TypeRules#isCompatible(FavaType, FavaType)})
     *
     * @param ctx         Contexto do parser da chamada.
     * @param isStatement {@code true} quando a chamada ocorre como instrução; {@code false} quando ocorre como expressão.
     * @return A assinatura {@link FunctionType} (mesmo que existam erros de uso), ou {@code null} se o símbolo não puder ser resolvido.
     */
    private FunctionType validateFunctionCall(FunctionCallContext ctx, boolean isStatement) {
        String funcName = ctx.ID().getText();
        FunctionType funcType = resolveFunctionType(funcName, ctx.getStart().getLine());
        if (funcType == null) return null;

        validateFunctionCallContext(ctx, funcName, funcType, isStatement);
        validateFunctionCallArguments(ctx, funcName, funcType);
        return funcType;
    }

    /**
     * Valida o contexto de uso de uma chamada:
     * - se a chamada for statement, a função tem de ser void
     * - se a chamada for expressão, a função tem de retornar valor
     *
     * @param ctx         Contexto do parser da chamada.
     * @param funcName    Nome da função.
     * @param funcType    Assinatura resolvida da função.
     * @param isStatement {@code true} quando a chamada ocorre como instrução; {@code false} quando ocorre como expressão.
     */
    private void validateFunctionCallContext(FunctionCallContext ctx, String funcName, FunctionType funcType, boolean isStatement) {
        if (isStatement && funcType.getReturnType() != null) {
            errorFunctionValueMustBeAssigned(ctx.getStart().getLine(), funcName);
        } else if (!isStatement && funcType.getReturnType() == null) {
            errorVoidFunctionCannotBeUsedInExpression(ctx.getStart().getLine(), funcName);
        }
    }

    /**
     * Valida a aridade e o tipo de cada argumento passado na chamada, segundo a assinatura da função.
     *
     * @param ctx      Contexto do parser da chamada.
     * @param funcName Nome da função.
     * @param funcType Assinatura resolvida da função.
     */
    private void validateFunctionCallArguments(FunctionCallContext ctx, String funcName, FunctionType funcType) {
        int expectedArgs = funcType.getParamTypes().size();
        int actualArgs = (ctx.exprList() != null) ? ctx.exprList().expr().size() : 0;

        if (expectedArgs != actualArgs) {
            errorFunctionExpectsNArguments(ctx.getStart().getLine(), funcName, expectedArgs);
            return;
        }

        if (actualArgs == 0) return;

        for (int i = 0; i < actualArgs; i++) {
            ExprContext argCtx = ctx.exprList().expr(i);
            visit(argCtx);

            FavaType argType = typesTree.get(argCtx);
            FavaType expectedType = funcType.getParamTypes().get(i);

            if (!typeRules.isCompatible(expectedType, argType)) {
                errorInvalidFunctionArgumentType(argCtx.getStart().getLine(), funcName, expectedType.getName());
            }
        }
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

        if (type instanceof FunctionType) {
            errorNotVariable(line, varName);
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
        if (condType != null && !typeRules.isBooleanType(condType)) {
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
        if (errorSink != null) {
            errorSink.accept(line, message);
        } else {
            System.out.println("error in line " + line + ": " + message);
        }
    }

    /**
     * Helper reportando variável ou símbolo em duplicado no dicionário.
     */
    private void errorAlreadyDeclared(int line, String varName) {
        reportError(line, varName + " already declared");
    }

    /**
     * Erro de ponto de entrada: o programa deve declarar uma função {@code main}.
     */
    private void errorMissingMain(int line) {
        reportError(line, "missing main()");
    }

    /**
     * Erro de completude de retorno: funções não-void devem garantir {@code return} em todos os caminhos.
     */
    private void errorMissingReturnInFunction(int line, String functionName) {
        reportError(line, "missing return in function " + functionName);
    }

    /**
     * Erro de retorno: função não-void precisa retornar um valor do tipo esperado.
     */
    private void errorFunctionMustReturnType(int line, String functionName, String expectedTypeName) {
        reportError(line, "function " + functionName + " must return a value of type " + expectedTypeName);
    }

    /**
     * Erro de retorno: função void não pode retornar expressão.
     */
    private void errorFunctionDoesNotReturnValue(int line, String functionName) {
        reportError(line, "function " + functionName + " does not return a value");
    }

    /**
     * Erro de chamada: função com retorno não pode ser usada como statement isolado.
     */
    private void errorFunctionValueMustBeAssigned(int line, String functionName) {
        reportError(line, "value of function " + functionName + " must be assigned to a variable");
    }

    /**
     * Erro de chamada: função void não pode ser usada em contexto de expressão.
     */
    private void errorVoidFunctionCannotBeUsedInExpression(int line, String functionName) {
        reportError(line, "void function " + functionName + " cannot be used in an expression");
    }

    /**
     * Erro de chamada: aridade inválida (número de argumentos não coincide com a assinatura).
     */
    private void errorFunctionExpectsNArguments(int line, String functionName, int expectedArgs) {
        reportError(line, "function " + functionName + " expects " + expectedArgs + " arguments");
    }

    /**
     * Dispara erro sempre que uma alocação requer identificadores globais não criados.
     */
    private void errorNotDeclared(int line, String varName) {
        reportError(line, varName + " not declared");
    }

    private void errorNotVariable(int line, String name) {
        reportError(line, name + " is not a variable");
    }

    private void errorInvalidFunctionArgumentType(int line, String functionName, String expectedTypeName) {
        reportError(line, "expecting an expression of type " + expectedTypeName + " for argument of function " + functionName);
    }

    /**
     * Injeta notificação de não-reconhecimento estrito de tipologias.
     */
    private void errorTypeDoesNotExist(int line, String typeName)
    {
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
