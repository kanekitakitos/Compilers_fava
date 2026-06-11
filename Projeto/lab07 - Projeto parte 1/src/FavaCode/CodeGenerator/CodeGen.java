package FavaCode.CodeGenerator;

import java.io.*;
import java.util.*;
import FavaCode.Parser.Fava.*;
import FavaCode.Semantic.FavaType;
import FavaCode.VirtualMachine.OpCode;
import FavaCode.VirtualMachine.Instruction.*;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

/**
 * Visitante do Gerador de Código para a linguagem Fava.
 * O Gerador de Código atua sobre a Árvore de Análise Sintática (AST) após esta ter sido validada semanticamente.
 * O seu papel é traduzir cada regra e expressão num conjunto de instruções ({@link Instruction}) 
 * e constantes para a Máquina Virtual (Bytecode).
 * <p>
 * O Bytecode gerado é então exportado num ficheiro binário (.bc).
 * 
 * @see FavaBaseVisitor
 * @see FavaCode.VirtualMachine.VirtualMachine
 */
public class CodeGen extends FavaBaseVisitor<Void>
{
    /** Armazena a lista sequencial de instruções da Máquina Virtual que vão compor o programa final. */
    private final ArrayList<Instruction> code = new ArrayList<>();

    /** Tabela dinâmica (Pool) de constantes estáticas encontradas no código fonte, como literais String e Double (Reais). */
    private final ArrayList<Object> constantPool = new ArrayList<>();
    // Auxiliar para fazer a procura dentro do array O(1) e não O(n)
    private final HashMap<Object, Integer> poolIndexMap = new HashMap<>();
    
    /** Árvore de propriedades herdada da Análise Semântica, mapeando cada nó da árvore sintática ao seu tipo de dados verificado. */
    private final ParseTreeProperty<FavaType> treeProperty;

    /**
     * Inicializa a Geração de Código a partir de uma propriedade em árvore que já processou os tipos com sucesso (semântica).
     *
     * @param treeProperty Objeto {@link ParseTreeProperty} gerado pelo Analisador Semântico contendo os tipos (ex: int, real) em cada Contexto.
     */
    public CodeGen(ParseTreeProperty<FavaType> treeProperty)
    {
        this.treeProperty = treeProperty;
    }

//*-------------- Types -------------------------------------------------------------------------------------------------

    /**
     * Traduz um literal inteiro. Emite o OpCode {@code iconst} para enviar o inteiro para a Pilha da Máquina Virtual.
     *
     * @param ctx O nó referente ao literal Inteiro na AST.
     * @return null.
     */
    @Override public Void visitIntExpr(FavaParser.IntExprContext ctx)
    {
        emit(OpCode.iconst, Integer.parseInt(ctx.INT().getText()));
        return null;
    }

    /**
     * Avalia e emite na stack um Booleano (true ou false) correspondente às instruções {@code tconst} ou {@code fconst}.
     *
     * @param ctx Contexto do token booleano.
     * @return null.
     */
    @Override public Void visitBoolExpr(FavaParser.BoolExprContext ctx)
    {
        String value = ctx.BOOLEAN().getText().toLowerCase();

        if(value.equals("true"))
            emit(OpCode.tconst);
        else
            emit(OpCode.fconst);
        return null;
    }

    /**
     * Processa e insere um valor numérico Real (Double) na {@link #constantPool}.
     * A seguir, emite a instrução {@code dconst} passando como argumento o indíce da Constant Pool onde o valor foi guardado.
     *
     * @param ctx O nó da AST que representa o valor Real.
     * @return null.
     */
    @Override public Void visitRealExpr(FavaParser.RealExprContext ctx)
    {
        Double value = Double.parseDouble(ctx.REAL().getText());

        // Verifica se a constante já existe na pool
        Integer index = poolIndexMap.get(value);

        // Se não existir (indexOf devolve -1), adiciona uma nova
        if (index == null) {
            constantPool.add(value);
            index = constantPool.size() - 1;
            poolIndexMap.put(value, index); // Regista no mapa para usos futuros
        }

        // Emite a instrução com o índice correto (novo ou reciclado)
        emit(OpCode.dconst, index);

        return null;
    }

    /**
     * Visita uma String constante e insere o seu valor na {@link #constantPool}.
     * Similar ao número real, emite {@code sconst} para referenciar a localização da Pool por via de indíce.
     *
     * @param ctx Contexto que abrange o valor em String (com aspas no texto original que são aqui removidas).
     * @return null.
     */
    @Override public Void visitStringExpr(FavaParser.StringExprContext ctx)
    {
        String value = ctx.STRING().getText();
        value = value.substring(1, value.length() - 1);

        // Procura instantânea no HashMap (O(1))
        Integer index = poolIndexMap.get(value);

        if (index == null) {
            constantPool.add(value);
            index = constantPool.size() - 1;
            poolIndexMap.put(value, index); // Regista no mapa para usos futuros
        }

        emit(OpCode.sconst, index);
        return null;
    }
//*---------------------------------------------------------------------------------------------------------------

    /**
     * Ponto de entrada que caminha por todos os nós 'stat' (Statements) do programa e, no final, 
     * emite a instrução {@code halt} como delimitadora do fecho da Máquina Virtual Fava.
     *
     * @param ctx O nó raiz (program) correspondente à totalidade do código na AST.
     * @return null.
     */
    @Override public Void visitProg(FavaParser.ProgContext ctx)
    {

        visitChildren(ctx);
        emit(OpCode.halt);

        return null;
    }

    /**
     * Visita um "Statement" que na versão atual da linguagem Fava compreende uma expressão e termina num ponto-e-vírgula (;) e executa implicitamente um Print.
     * Para inferir qual versão do opcode "Print" deve ser emitido, resgata o tipo de dados da respectiva expressão pelo Semantic Analyzer e usa o {@link OpCodeMapper}.
     *
     * @param ctx Contexto que engloba o Statement.
     * @return null.
     */
    @Override public Void visitPrintStat(FavaParser.PrintStatContext ctx)
    {
        visit(ctx.expr());

        String operandType = this.treeProperty.get(ctx.expr()).getName();
        emit(OpCodeMapper.getPrint(operandType));
        return null;
    }

    /**
     * Retira e processa uma expressão envolvida em parênteses.
     *
     * @param ctx Contexto dos Parênteses.
     * @return O resultado da visita sobre a sub-expressão interna.
     */
    @Override public Void visitParensExpr(FavaParser.ParensExprContext ctx) {
        return visit(ctx.expr());
    }

    /**
     * Emite um OpCode unário após avaliar a expressão adjacente (ex: 'iuminus' para '-' ou 'not' para lógico).
     *
     * @param ctx Contexto da operação unária.
     * @return null.
     */
    @Override public Void visitUnaryExpr(FavaParser.UnaryExprContext ctx)
    {
        visit(ctx.expr());

        String operandType = this.treeProperty.get(ctx.expr()).getName();
        emit(OpCodeMapper.getUnaryOpCode(ctx.op.getText(), operandType));

        return null;
    }

    /**
     * Regra para operações de multiplicação, divisão ou módulo (*, /, mod).
     * Delega as visitas com base na regra definida e aplica emissão de código após inferir o tipo dominante e a operação apropriada em {@link #processBinaryOperation}.
     *
     * @param ctx O contexto da multiplicação, divisão ou mod.
     * @return null.
     */
    @Override public Void visitMulDivModExpr(FavaParser.MulDivModExprContext ctx)
    {
        String resultType = this.treeProperty.get(ctx).getName();

        processBinaryOperation(ctx.expr(0), ctx.expr(1), ctx.op.getText(), resultType);
        return null;
    }

    /**
     * Converte num OpCode matemático a adição (+) ou a subtração (-) entre duas expressões.
     *
     * @param ctx Contexto de soma e subtração.
     * @return null.
     */
    @Override public Void visitAddSubExpr(FavaParser.AddSubExprContext ctx)
    {
        String resultType = this.treeProperty.get(ctx).getName();

        processBinaryOperation(ctx.expr(0), ctx.expr(1), ctx.op.getText(), resultType);
        return null;
    }

    /**
     * Processa a operação binária de concatenação de strings (||).
     *
     * @param ctx Contexto de concatenação.
     * @return null.
     */
    @Override public Void visitConcatExpr(FavaParser.ConcatExprContext ctx)
    {
        String resultType = this.treeProperty.get(ctx).getName();

        processBinaryOperation(ctx.expr(0), ctx.expr(1), ctx.op.getText(), resultType);
        return null;
    }

    /**
     * Processa a Geração de Código das operações de grandeza relacional (<, <=, >, >=).
     * Se os operadores forem '>' ou '>=', como não existe OpCode compatível na Máquina Virtual, o código é
     * reordenado (stacking dos argumentos invertido e substituição pelo sinal de '<' ou '<=').
     *
     * @param ctx Contexto para os sinais relacionais detetados.
     * @return null.
     */
    @Override public Void visitRelationalExpr(FavaParser.RelationalExprContext ctx)
    {
        String op = ctx.op.getText();
        String leftType = this.treeProperty.get(ctx.expr(0)).getName();
        String rightType = this.treeProperty.get(ctx.expr(1)).getName();

        // 1. Descobre o tipo dominante (Type Promotion)
        String promotedType = getPromotedType(leftType, rightType);

        if (op.equals(">") || op.equals(">=")) {
            // INVERTE A ORDEM: Empilha a direita, depois a esquerda
            visit(ctx.expr(1));
            OpCode castRight = OpCodeMapper.getCastOpCode(rightType, promotedType);
            if (castRight != null) emit(castRight);

            visit(ctx.expr(0));
            OpCode castLeft = OpCodeMapper.getCastOpCode(leftType, promotedType);
            if (castLeft != null) emit(castLeft);

            // Pede o opcode inverso ao Mapper usando o tipo dominante
            OpCode correctOp = op.equals(">") ?
                    OpCodeMapper.getBinaryOpCode("<", promotedType) :
                    OpCodeMapper.getBinaryOpCode("<=", promotedType);

            emit(correctOp);
        } else {
            // ORDEM NORMAL (< ou <=)
            visit(ctx.expr(0));
            OpCode castLeft = OpCodeMapper.getCastOpCode(leftType, promotedType);
            if (castLeft != null) emit(castLeft);

            visit(ctx.expr(1));
            OpCode castRight = OpCodeMapper.getCastOpCode(rightType, promotedType);
            if (castRight != null) emit(castRight);

            // Emite a instrução baseada no tipo que está na stack (promotedType)
            emit(OpCodeMapper.getBinaryOpCode(op, promotedType));
        }
        return null;
    }

    /**
     * Aplica testes de Igualdade ('=' ou '<>').
     * Usa promoção de tipo caso uma mistura inteira e real esteja presente (ex: 5.0 = 5).
     *
     * @param ctx Contexto das operações de igualdade.
     * @return null.
     */
    @Override public Void visitEqualityExpr(FavaParser.EqualityExprContext ctx)
    {
        String leftType = this.treeProperty.get(ctx.expr(0)).getName();
        String rightType = this.treeProperty.get(ctx.expr(1)).getName();

        // promotedType em vez do resultType ("Bool"), Problemas com Bool
        // 5.0 = 5 ---> Resultado final é Bool
        // mas são numeros "Real" e "integer"
        String promotedType = getPromotedType(leftType, rightType);
        processBinaryOperation(ctx.expr(0), ctx.expr(1), ctx.op.getText(), promotedType);
        return null;
    }

    /**
     * Operação booleana 'AND' (e lógico).
     *
     * @param ctx Contexto AND.
     * @return null.
     */
    @Override public Void visitAndExpr(FavaParser.AndExprContext ctx)
    {
        String resultType = this.treeProperty.get(ctx).getName();
        String op = ctx.op.getText();

        visit(ctx.expr(0));
        visit(ctx.expr(1));

        // Emite a instrução principal com o tipo unificado
        emit(OpCodeMapper.getBinaryOpCode(op, resultType));

        return null;
    }

    /**
     * Operação booleana 'OR' (ou lógico).
     *
     * @param ctx Contexto OR.
     * @return null.
     */
    @Override public Void visitOrExpr(FavaParser.OrExprContext ctx)
    {
        String resultType = this.treeProperty.get(ctx).getName();
        String op = ctx.op.getText();

        visit(ctx.expr(0));
        visit(ctx.expr(1));

        // Emite a instrução principal com o tipo unificado
        emit(OpCodeMapper.getBinaryOpCode(op, resultType));

        return null;
    }

//------------- Utility functions -------------------------------------------------------------------------------------------------------------


    /**
     * Função auxiliar para calcular o nível da conversão quando a stack tem dois números diferentes para a mesma operação,
     * impedindo incoerências no processador (ex: calcular real com integer promove ambos para real).
     *
     * @param type1 O primeiro tipo.
     * @param type2 O segundo tipo.
     * @return O tipo dominante sob o qual a operação será avaliada.
     */
    private String getPromotedType(String type1, String type2) {
        if (type1.equals(type2)) return type1;

        // Mistura de Real e Integer nivela sempre para Real
        if ((type1.equals("real") && type2.equals("integer")) ||
                (type1.equals("integer") && type2.equals("real")))
                    return "real";

        return "String";
    }

    /**
     * Função base responsável por tratar e emitir o código das operações binárias standard, encarregando-se
     * em automático de inserir eventuais instruções de coerção ou cast na Pilha da Máquina Virtual caso os tipos divirjam do `targetType`.
     *
     * @param leftExpr  A expressão/nó correspondente ao lado esquerdo da operação.
     * @param rightExpr A expressão/nó correspondente ao lado direito da operação.
     * @param op        O símbolo textual do operador (ex: "+").
     * @param targetType O tipo de dados esperado para unificar o cálculo.
     */
    private void processBinaryOperation(FavaParser.ExprContext leftExpr, FavaParser.ExprContext rightExpr, String op, String targetType)
    {
        String leftType = this.treeProperty.get(leftExpr).getName();
        String rightType = this.treeProperty.get(rightExpr).getName();

        // Visita a esquerda e faz cast se o tipo for diferente do targetType
        visit(leftExpr);
        OpCode castLeft = OpCodeMapper.getCastOpCode(leftType, targetType);
        if (castLeft != null) emit(castLeft);

        // Visita a direita e faz cast se necessário
        visit(rightExpr);
        OpCode castRight = OpCodeMapper.getCastOpCode(rightType, targetType);
        if (castRight != null) emit(castRight);

        // Emite a instrução principal
        emit(OpCodeMapper.getBinaryOpCode(op, targetType));
    }

    /**
     * Emite uma instrução puramente executiva para a memória, sem argumentos extra.
     *
     * @param opc Código da instrução (OpCode).
     */
    public void emit(OpCode opc)
    {
        code.add( new Instruction(opc) );
    }

    /**
     * Emite e constrói na memória uma instrução que dependa de um argumento específico atrelado na assinatura (como um index da Pool ou valor Inteiro).
     *
     * @param opc Código da instrução (OpCode).
     * @param val O argumento numérico para alimentar o OpCode.
     */
    public void emit(OpCode opc, int val)
    {
        code.add( new Instruction1Arg(opc, val) );

    }

    /**
     * Efetua o dump do código gerado no ecrã formatado sequencialmente de forma similar a Assembly para visualização.
     */
    public void dumpCode() {
        System.out.println("Generated code in assembly format");
        for (int i=0; i< code.size(); i++)
            System.out.println( i + ": " + code.get(i) );
    }


    /**
     * Efetua o print no ecrã (Debug) da Constant Pool carregada durante o Parse, listando identificadores (Índices) associados ao valor no seu interior.
     */
    public void dumpCodePool()
    {
        System.out.println("*** Constant pool ***");
        for (int i=0; i< constantPool.size(); i++)
        {
            if (constantPool.get(i) instanceof String)
                System.out.println( i + ": "+"\""+  constantPool.get(i)+"\"");
                
            else
                System.out.println( i + ": " + constantPool.get(i) );
        }
            
    }
    
    /**
     * Emite no log do ecrã as instruções (OpCodes e index) sem mais verbosidades.
     */
    public void dumpCodeInstructions()
    {
        System.out.println("*** Instructions ***");
        for (int i=0; i< code.size(); i++)
            System.out.println( i + ": " + code.get(i) );
    }

    /**
     * Encarrega-se da escrita física num ficheiro binário do Bytecode processado pelo Compilador na Geração de Código.
     * É este o ficheiro final consumido pelo utilitário FavaVM.
     *
     * @param filename A designação final que vai compor o nome do ficheiro '.bc' e o seu caminho correspondente no disco.
     */
    public void saveBytecodes(String filename)
    {
        try (DataOutputStream dout = new DataOutputStream(new FileOutputStream(filename)))
        {
            /*
                Os primeiros 4 bytes do ficheiro correspondem a um inteiro que indica o numero
                de entradas da constant pool. Se esse numero for n, sabemos que temos de ler n
                constantes. Cada constante apenas pode ser um double ou uma string.

                De modo a distinguir se estamos perante um double ou uma string, usamos 1 byte
                adicional. No caso de ser um double necessitaremos de 8 bytes para o representar
                (com o byte adicional gastamos 9 bytes por cada double). No caso de ser uma string,
                necessitamos de saber quantos caracteres tem a string. Para tal usamos um inteiro (4
                bytes) para representar o tamanho da string, e depois sabemos exactamente quantos
                caracteres temos de ler. Cada caracter e codificado com 2 bytes.

                01 -> double , 8 bytes a seguir o representam
                03 -> string , 4 bytes a seguir o tamanho da string

            */

            // 1. O Cabeçalho: Tamanho da Constant Pool (escreve 4 bytes)
            dout.writeInt(this.constantPool.size());

            // 2. O Corpo da Constant Pool
            for (Object constValue : this.constantPool) {
                if (constValue instanceof Double)
                {
                    dout.writeByte(1); // Tag para Double
                    dout.writeDouble((Double) constValue); // Escreve 8 bytes

                } else if (constValue instanceof String)
                {
                    dout.writeByte(3); // Tag para String
                    String str = (String) constValue;
                    dout.writeInt(str.length()); // Tamanho da string (4 bytes)

                    // Escrever a string char a char (2 bytes cada char)
                    for (int i = 0; i < str.length(); i++) {
                        dout.writeChar(str.charAt(i));
                    }
                } else {
                    System.err.println("Erro: Tipo desconhecido na Constant Pool.");
                }
            }

            // 3. As Instruções (o código real)
            for (Instruction inst : code) {
                inst.writeTo(dout);
            }

            //System.out.println("Saving the bytecodes to " + filename);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}