package FavaCode.Semantic;

import FavaCode.Parser.Fava.FavaBaseVisitor;
import FavaCode.Parser.Fava.FavaParser;
import FavaCode.Semantic.Types.*;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

/**
 * Visitante do analisador semântico para a linguagem Fava.
 * Caminha sobre a Árvore de Análise Sintática (AST) gerada pelo ANTLR4 para verificar e inferir tipos
 * das expressões, gerindo as regras estabelecidas por {@link TypeRules} e as variáveis através da {@link SymbolTable}.
 * Utilizado primariamente na etapa anterior à Geração de Código.
 *
 * @see FavaBaseVisitor
 * @see FavaType
 */
public class SemanticAnalyzerVisitor extends FavaBaseVisitor<FavaType>
{
    /** Dicionário de símbolos que regista e valida as variáveis (identificadores e tipos) presentes no código fonte. */
    private SymbolTable symbolTable = new SymbolTable();

    /** O motor de avaliação de regras que estipula conversões ou tipos devolvidos para operações e operandos. */
    private TypeRules typeRules = new TypeRules();

    /** Árvore de propriedades que decora cada nó (Context) do Parser da AST do ANTLR4 com os respetivos {@link FavaType} inferidos. */
    private ParseTreeProperty<FavaType> typesTree = new ParseTreeProperty<>();


    private int semanticErros = 0;

    /**
     * Resgata a árvore com propriedades já processadas, fundamental para o Gerador de Código.
     * Na Geração de Código, o tipo do contexto é recuperado por meio desta referência para que se possa invocar
     * a instrução correta da máquina virtual (ex: iconst ou sconst).
     *
     * @return O objeto {@link ParseTreeProperty} que armazena referências aos tipos para cada nó (Context) que foi avaliado e aprovado no Analisador Semântico.
     */
    public ParseTreeProperty<FavaType> getTypesTree()
    {
        return typesTree;
    }
//*--------------- Types ------------------------------------------------------------------------

    /**
     * Visita um nó de Inteiro estático ('int' ou literal) e associa-o com o {@link IntegerType}.
     *
     * @param ctx O contexto da árvore (IntContext).
     * @return O {@link IntegerType} associado a este literal.
     */
    @Override
    public FavaType visitIntExpr(FavaParser.IntExprContext ctx)
    {
        FavaType type = IntegerType.INSTANCE;
        typesTree.put(ctx, type);
        return type;
    }

    /**
     * Visita um nó de Booleano ('true', 'false' ou 'bool').
     *
     * @param ctx O contexto que aponta para um valor de condição lógica no script Fava.
     * @return {@link BoolType} indicando sucesso semântico para o token.
     */
    @Override
    public FavaType visitBoolExpr(FavaParser.BoolExprContext ctx)
    {
        FavaType type = BoolType.INSTANCE;
        typesTree.put(ctx, type);
        return type;
    }

    /**
     * Visita e regista propriedades num literal de texto ('string').
     *
     * @param ctx Contexto do token de string.
     * @return Instância associada de {@link StringType}.
     */
    @Override
    public FavaType visitStringExpr(FavaParser.StringExprContext ctx)
    {
        FavaType type = StringType.INSTANCE;
        typesTree.put(ctx, type);
        return type;
    }

    /**
     * Valida números Reais (Double/Float) e designa o {@link RealType}.
     *
     * @param ctx O contexto onde aparece o número real.
     * @return Instância singleton ou designada do {@link RealType}.
     */
    @Override
    public FavaType visitRealExpr(FavaParser.RealExprContext ctx)
    {
        FavaType type = RealType.INSTANCE;
        typesTree.put(ctx, type);
        return type;
    }
//*--------------- Methods ------------------------------------------------------------------------
//*          KISS - Keep It Simple, Stupid

    /**
     * Função auxiliar/Helper utilizada por operações binárias (visitas a Addition, Mul, Concat, etc.).
     * Submete a operação às {@link TypeRules} e, se a operação não for válida entre os dois tipos de dados, 
     * sinaliza um erro indicando a linha com falha e sai do programa, prevendo crash em runtime.
     *
     * @param ctx   Nó Contexto onde a operação está definida (necessário para log do erro e gravar na {@link #typesTree}).
     * @param left  O tipo previamente visitado na sub-expressão à esquerda do operador.
     * @param right O tipo previamente inferido pela AST à direita do operador.
     * @param op    Operador detetado no Parsing (ex: '+', '||').
     * @return O tipo de retorno esperado ({@link FavaType}) caso a operação seja validada pelas regras da linguagem.
     */
    private FavaType processBinaryOperation(org.antlr.v4.runtime.ParserRuleContext ctx, FavaType left, FavaType right, String op)
    {

        if (left == null || right == null) return null;

        FavaType resultType = typeRules.getResultType(op.toLowerCase(), left, right);

        if (resultType == null)
        {
            int line = ctx.start.getLine();
            this.semanticErros++;
            System.out.println("error in line "+ line +": operator " + op +
                    " is invalid between " +
                    left.getName() + " and " + right.getName());
            //System.exit(1);
        }
        typesTree.put(ctx, resultType);
        return resultType;
    }


    /**
     * Visita uma expressão entre parênteses '(' expr ')'.
     * Descompacta a operação iterando no nó filho sem mudar a precedência semântica.
     *
     * @param ctx Contexto dos Parêntesis.
     * @return Repassa o tipo validado da sub-expressão iterada e anota-o nesta etapa da árvore.
     */
    @Override
    public FavaType visitParensExpr(FavaParser.ParensExprContext ctx)
    {
        FavaType resultType = visit(ctx.expr());
        typesTree.put(ctx,resultType);

        return resultType;
    }

    /**
     * Visita uma operação unária prefixa do género ('-' ou 'not') seguida pela sua expressão associada.
     * Semelhante ao Helper, detém tratamento dedicado de verificação nas {@link TypeRules} e sai se for inválido (ex: not 3).
     *
     * @param ctx Contexto Unário ('not' / '-').
     * @return O {@link FavaType} retornado. Por exemplo, Bool para o 'not' e Integer ou Real para o '-'.
     */
    @Override
    public FavaType visitUnaryExpr(FavaParser.UnaryExprContext ctx)
    {

        FavaType exprType = visit(ctx.expr());
        String op = ctx.op.getText().toLowerCase();

        // Consulta o teu motor de regras
        FavaType resultType = typeRules.getResultType(op, exprType);

        // Se o motor devolver null, o código do utilizador tem um erro!
        if (resultType == null)
        {

            int line = ctx.start.getLine();
            this.semanticErros++;
            System.out.println("error in line "+ line +": operator "+ op +" is invalid for " + exprType.getName());
            //System.exit(1);
        }

        // Grava o tipo aprovado neste nó da árvore (para o CodeGen usar mais tarde)
        typesTree.put(ctx, resultType);

        return resultType;
    }

    /**
     * Processa a semântica da multiplicação, divisão ou módulo ('*', '/', 'mod').
     * Direciona para {@link #processBinaryOperation(org.antlr.v4.runtime.ParserRuleContext, FavaType, FavaType, String)}.
     */
    @Override
    public FavaType visitMulDivModExpr(FavaParser.MulDivModExprContext ctx)
    {
        return processBinaryOperation(ctx, visit(ctx.expr(0)), visit(ctx.expr(1)), ctx.op.getText());
    }

    /**
     * Visita a regra de Adição ou Subtração ('+', '-').
     */
    @Override
    public FavaType visitAddSubExpr(FavaParser.AddSubExprContext ctx)
    {
        return processBinaryOperation(ctx, visit(ctx.expr(0)), visit(ctx.expr(1)), ctx.op.getText());
    }

    /**
     * Visita nós de concatenação ('||').
     */
    @Override
    public FavaType visitConcatExpr(FavaParser.ConcatExprContext ctx)
    {
        return processBinaryOperation(ctx, visit(ctx.expr(0)), visit(ctx.expr(1)), ctx.op.getText());
    }

    /**
     * Visita nós Relacionais de comparação ('>', '<', '<=', '>=').
     */
    @Override
    public FavaType visitRelationalExpr(FavaParser.RelationalExprContext ctx)
    {
        return processBinaryOperation(ctx, visit(ctx.expr(0)), visit(ctx.expr(1)), ctx.op.getText());
    }

    /**
     * Comparações lógicas de igualdade ('=', '<>').
     */
    @Override
    public FavaType visitEqualityExpr(FavaParser.EqualityExprContext ctx)
    {
        return processBinaryOperation(ctx, visit(ctx.expr(0)), visit(ctx.expr(1)), ctx.op.getText());
    }

    /**
     * Processa Operador AND lógico ('and').
     */
    @Override
    public FavaType visitAndExpr(FavaParser.AndExprContext ctx)
    {
        return processBinaryOperation(ctx, visit(ctx.expr(0)), visit(ctx.expr(1)), ctx.op.getText());
    }

    /**
     * Visita Operador OR lógico ('or').
     */
    @Override
    public FavaType visitOrExpr(FavaParser.OrExprContext ctx)
    {
        return processBinaryOperation(ctx, visit(ctx.expr(0)), visit(ctx.expr(1)), ctx.op.getText());
    }



    public int getSemanticErrors() {
        return semanticErros;
    }

}