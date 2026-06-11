package FavaCode.CodeGenerator;

import java.io.*;
import java.util.*;

import FavaCode.Parser.Fava.*;
import FavaCode.Semantic.FavaType;
import FavaCode.VirtualMachine.OpCode;
import FavaCode.VirtualMachine.Instruction.*;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

/**
 * Visitante do Gerador de Código para o compilador da linguagem Fava.
 *
 * <p>Atua após a fase da Análise Semântica, traduzindo a Abstract Syntax Tree (AST) em instruções bytecode ({@link OpCode})
 * executáveis pela Máquina Virtual Fava. Gere a alocação de variáveis na memória ({@link Memory}), resolve Jumps de controlo de fluxo
 * (IFs e While) através de Backpatching e constrói a Pool de Constantes para literais (Strings e Doubles).</p>
 *
 * <p>Referências Académicas:</p>
 * <ul>
 * <li><b>Aho, Lam, Sethi, Ullman (Dragon Book), Cap. 6.7 "Backpatching":</b>
 * Descreve a geração de código num único passe (Single-Pass), onde as instruções de
 * salto (como ifs ou whiles) são inicialmente emitidas com endereços temporários.
 * Utiliza listas lógicas preenchidas posteriormente pela função {@code backpatch}.</li>
 * <li><b>Nystrom (Crafting Interpreters), Cap. 23 "Jumping Back and Forth":</b>
 * Demonstra a aplicação prática do Backpatching na compilação de bytecode, substituindo
 * 'placeholders' assim que o tamanho do bloco de código avaliado (o ramo then/else) é conhecido.</li>
 * </ul>
 * <p>
 * ### Exemplo de Uso
 * <pre>{@code
 * // Exemplo simplificado da execução no FavaCompiler:
 * CodeGen codeGen = new CodeGen(semanticAnalyzer.getTypesTree());
 * codeGen.visit(tree);
 *
 * // Depois da AST ter sido visitada e as instruções geradas:
 * codeGen.dumpCodeInstructions(); // Imprime o código no terminal
 * codeGen.saveBytecodes("output.bc"); // Exporta o binário
 * }</pre>
 *
 * @see FavaCode.VirtualMachine.VirtualMachine
 * @see OpCodeMapper
 * @see Memory
 */
public class CodeGen extends FavaBaseVisitor<Void> {
    /**
     * O array sequencial das instruções de Bytecode geradas pelo compilador.
     */
    private final ArrayList<Instruction> code = new ArrayList<>();

    /**
     * A Pool de Constantes (Constant Pool) guarda todos os literais complexos usados no código (Doubles e Strings).
     */
    private final ArrayList<Object> constantPool = new ArrayList<>();

    /**
     * Índice otimizado para evitar duplicação de constantes na pool (reaproveitamento do mesmo índice LIFO/FIFO).
     */
    private final HashMap<Object, Integer> poolIndexMap = new HashMap<>();

    /**
     * A árvore de propriedades decorada pelo Semantic Analyzer com a tipagem de cada nó.
     */
    private final ParseTreeProperty<FavaType> treeProperty;

    /**
     * Gestor de escopos e endereços virtuais LIFO para as variáveis locais.
     */
    private final Memory memory = new Memory();

    /**
     * Inicializa o Gerador de Código.
     *
     * @param treeProperty Propriedades extraídas da Análise Semântica, vitais para inferir as instruções corretas de cast e matemática.
     */
    public CodeGen(ParseTreeProperty<FavaType> treeProperty) {
        this.treeProperty = treeProperty;
    }

//*-------------- Helpers de Otimização ------------------------------------------------------------------------

    /**
     * Adiciona ou reaproveita literais na Constant Pool de forma eficiente.
     * Previne o inchaço da memória binária ao reutilizar referências para strings ou doubles idênticos.
     * * @param value O valor literal (Double ou String) a registar.
     *
     * @return O índice inteiro (offset) onde o valor foi guardado na pool.
     */
    private int getOrAddConstant(Object value) {
        Integer index = poolIndexMap.get(value);
        if (index == null) {
            constantPool.add(value);
            index = constantPool.size() - 1;
            poolIndexMap.put(value, index);
        }
        return index;
    }

    /**
     * Executa a técnica de Backpatching (Remendo de Saltos).
     * Substitui uma instrução de salto provisória (dummy) com o destino final já calculado pelo parser.
     *
     * @param targetIndex     A linha de código do bytecode onde o salto incompleto foi inicialmente emitido.
     * @param jumpInstruction O OpCode incondicional ou condicional do salto (ex: {@code OpCode.jumpf}).
     * @param destinationLine A linha real alvo para a qual o Instruction Pointer (IP) da máquina virtual deve transitar.
     */
    private void backpatch(int targetIndex, OpCode jumpInstruction, int destinationLine) {
        code.set(targetIndex, new Instruction1Arg(jumpInstruction, destinationLine));
    }

//*-------------- Types -------------------------------------------------------------------------------------------------

    /**
     * Traduz uma expressão literal inteira empurrando-a para a Stack da Máquina Virtual.
     *
     * @param ctx O contexto do nó contendo o texto do número inteiro.
     * @return Sempre {@code null}, pois o CodeGen altera estados internos e não propaga valores na AST.
     */
    @Override
    public Void visitIntegerExpr(FavaParser.IntegerExprContext ctx) {
        emit(OpCode.iconst, Integer.parseInt(ctx.INTEGER().getText()));
        return null;
    }

    /**
     * Transcreve a palavra-chave booleana nativa para o equivalente binário (True/False Const).
     *
     * @param ctx O nó contendo o literal booleano.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitBoolExpr(FavaParser.BoolExprContext ctx) {
        if (ctx.BOOLEAN().getText().equalsIgnoreCase("true")) emit(OpCode.tconst);
        else emit(OpCode.fconst);
        return null;
    }

    /**
     * Traduz uma expressão literal de vírgula flutuante (Real).
     * O valor é externalizado para a Constant Pool e substituído por uma referência de carregamento.
     *
     * @param ctx O nó AST do número real.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitRealExpr(FavaParser.RealExprContext ctx) {
        Double value = Double.parseDouble(ctx.REAL().getText());
        emit(OpCode.dconst, getOrAddConstant(value));
        return null;
    }

    /**
     * Trata literais String. Remove a formatação de aspas da linguagem e indexa o texto na Constant Pool.
     *
     * @param ctx O nó da String.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitStringExpr(FavaParser.StringExprContext ctx) {
        String value = ctx.STRING().getText();
        value = value.substring(1, value.length() - 1); // Corta as aspas
        emit(OpCode.sconst, getOrAddConstant(value));
        return null;
    }

//*-------------- Memory & Variables ------------------------------------------------------------------------------------

    /**
     * Delimita um novo espaço temporal de memória para variáveis locais.
     *
     * @param ctx O contexto que envolve o bloco entre chavetas ( { ... } ).
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitBlock(FavaParser.BlockContext ctx) {
        memory.enterScope();
        visitChildren(ctx);
        memory.exitScope();
        return null;
    }

    /**
     * Executa a alocação de endereços para novas variáveis.
     * Caso possuam uma expressão de inicialização à direita, converte e armazena os valores
     * com eventuais conversões ascendentes (Cast de Inteiro para Real se necessário).
     *
     * @param ctx Declaração contendo o tipo da linguagem e a lista de variáveis.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitVarDeclaration(FavaParser.VarDeclarationContext ctx) {
        String typeName = ctx.type().getText().toLowerCase();

        for (FavaParser.VarInitContext initCtx : ctx.varInit()) {
            String varName = initCtx.ID().getText();
            int addr = memory.alloc(varName);

            if (initCtx.expr() != null) {
                visit(initCtx.expr());
                String exprType = treeProperty.get(initCtx.expr()).getName();

                if (typeName.equals("real") && exprType.equals("integer")) {
                    emit(OpCode.itod);
                }
                emit(OpCode.gstore, addr);
            }
        }
        return null;
    }

    /**
     * Atualiza o valor de uma variável existente. Avalia a expressão da direita e guarda no endereço LIFO correspondente.
     *
     * @param ctx Atribuição contendo a variável destino e a expressão.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitAssignStat(FavaParser.AssignStatContext ctx) {
        int addr = memory.getAddress(ctx.ID().getText());
        String varTypeName = treeProperty.get(ctx).getName();

        visit(ctx.expr());

        String exprTypeName = treeProperty.get(ctx.expr()).getName();
        if (varTypeName.equals("real") && exprTypeName.equals("integer"))
            emit(OpCode.itod);

        emit(OpCode.gstore, addr);
        return null;
    }

    /**
     * Extrai o valor de uma variável a partir do offset indexado de memória.
     *
     * @param ctx Contexto que evoca o identificador.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitIdExpr(FavaParser.IdExprContext ctx) {
        int addr = memory.getAddress(ctx.ID().getText());
        emit(OpCode.gload, addr);
        return null;
    }

//*-------------- Control Flow (If / While) -----------------------------------------------------------------------------

    /**
     * Emite a lógica de ramificação IF/ELSE.
     * Como o compilador Single-Pass não prevê o tamanho do corpo da condição, insere saltos temporários ("dummy").
     * Após visitar o corpo, aciona o helper de backpatching para reescrever os endereços físicos na memória do bytecode.
     * * <p>Ref: Nystrom, R. (2021) "Crafting Interpreters" - Chapter 23: Jumping Back and Forth.</p>
     *
     * @param ctx A instrução condicional com os seus blocos associados (then e optativamente else).
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitIfStat(FavaParser.IfStatContext ctx) {
        visit(ctx.expr());

        int jumpfIdx = code.size();
        emit(OpCode.jumpf, -1); // Salto condicional provisório

        visit(ctx.stat(0));

        if (ctx.stat().size() > 1) { // Lógica se houver bloco Else
            int jumpEndIdx = code.size();
            emit(OpCode.jump, -1);

            backpatch(jumpfIdx, OpCode.jumpf, code.size()); // Remenda saída falsa para iniciar o Else

            visit(ctx.stat(1));

            backpatch(jumpEndIdx, OpCode.jump, code.size()); // Remenda o final do True para saltar fora do Else
        } else {
            backpatch(jumpfIdx, OpCode.jumpf, code.size());
        }

        return null;
    }

    /**
     * Estrutura o bytecode cíclico de um loop While.
     * Guarda o ponto de ancoragem para regresso automático ao topo após a iteração,
     * e soluciona a quebra do ciclo através do backpatching da instrução 'jumpf'.
     *
     * @param ctx A estrutura de repetição e respetivo bloco anidado.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitWhileStat(FavaParser.WhileStatContext ctx) {
        int cycleStartLine = code.size();
        visit(ctx.expr());

        int exitPatchLine = code.size();
        emit(OpCode.jumpf, -1); // Marcador "dummy" de quebra do ciclo

        visit(ctx.stat());

        emit(OpCode.jump, cycleStartLine); // Força a máquina de estados a rever a condição de entrada

        backpatch(exitPatchLine, OpCode.jumpf, code.size()); // Consolida a morada de fuga

        return null;
    }

//*-------------- Methods ------------------------------------------------------------------------------------------------

    /**
     * Despoleta o ciclo do Gerador.
     * Efetua um pré-registo vazio para a alocação global de memória (galloc), visita toda a árvore para compilar as contas de offsets,
     * e encerra fazendo backpatching dessa primeira instrução informando do High-Water Mark final da memória utilizada.
     *
     * @param ctx A raiz superior de sintaxe do programa completo.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitProg(FavaParser.ProgContext ctx) {
        emit(OpCode.galloc, 0);

        visitChildren(ctx);
        emit(OpCode.halt); // Força encerramento limpo da VM

        int total = memory.getTotalAllocations();
        if (total > 0)
            backpatch(0, OpCode.galloc, total);
        else
            code.remove(0); // Otimização: remove alocação se o programa não usar variáveis

        return null;
    }

    /**
     * Despacha uma instrução terminal de escrita para ecrã, delegando o sulfixo físico da VM ao {@link OpCodeMapper}.
     *
     * @param ctx Chamada nativa de visualização.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitPrintStat(FavaParser.PrintStatContext ctx) {
        visit(ctx.expr());
        String operandType = this.treeProperty.get(ctx.expr()).getName();
        emit(OpCodeMapper.getPrint(operandType));
        return null;
    }

    /**
     * Reencaminha a avaliação removendo ruído sintático gerado pelos delimitadores parentéticos.
     *
     * @param ctx A expressão agrupada.
     * @return O resultado da sub-expressão interna.
     */
    @Override
    public Void visitParensExpr(FavaParser.ParensExprContext ctx) {
        return visit(ctx.expr());
    }

    /**
     * Resolve prefixos e negações matemáticas ou lógicas (-x, not x).
     *
     * @param ctx Operador unário e elemento base.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitUnaryExpr(FavaParser.UnaryExprContext ctx) {
        visit(ctx.expr());
        String operandType = this.treeProperty.get(ctx.expr()).getName();
        emit(OpCodeMapper.getUnaryOpCode(ctx.op.getText(), operandType));
        return null;
    }

    /**
     * Encaminha operações Multiplicativas e de Resto para processamento e cast automático.
     *
     * @param ctx Contexto da árvore em Mul/Div/Mod.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitMulDivModExpr(FavaParser.MulDivModExprContext ctx) {
        String resultType = this.treeProperty.get(ctx).getName();
        processBinaryOperation(ctx.expr(0), ctx.expr(1), ctx.op.getText(), resultType);
        return null;
    }

    /**
     * Resolve e propaga a geração nativa de bytecode de Adição e Subtração.
     *
     * @param ctx A operação correspondente na AST.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitAddSubExpr(FavaParser.AddSubExprContext ctx) {
        String resultType = this.treeProperty.get(ctx).getName();
        processBinaryOperation(ctx.expr(0), ctx.expr(1), ctx.op.getText(), resultType);
        return null;
    }

    /**
     * Combina strings textuais com outros tipos primitivos, forçando casts para "sconcat".
     *
     * @param ctx Bloco operador '||'.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitConcatExpr(FavaParser.ConcatExprContext ctx) {
        String resultType = this.treeProperty.get(ctx).getName();
        processBinaryOperation(ctx.expr(0), ctx.expr(1), ctx.op.getText(), resultType);
        return null;
    }


    /**
     * Soluciona lacunas da arquitetura FVM (falta de comparadores GT e GTE) invertendo o processamento
     * da Stack num bypass elegante, trocando a prioridade direita-esquerda e remapeando o salto LT ou LTE.
     *
     * @param ctx Comparadores de proporção.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitRelationalExpr(FavaParser.RelationalExprContext ctx)
    {
        String op = ctx.op.getText();
        String leftType = this.treeProperty.get(ctx.expr(0)).getName();
        String rightType = this.treeProperty.get(ctx.expr(1)).getName();
        String promotedType = getPromotedType(leftType, rightType);

        if (op.equals(">") || op.equals(">=")) {
            String invertedOp = op.equals(">") ? "<" : "<=";
            // Magia: passamos a expr(1) primeiro para inverter a ordem na stack!
            processBinaryOperation(ctx.expr(1), ctx.expr(0), invertedOp, promotedType);
        } else {
            processBinaryOperation(ctx.expr(0), ctx.expr(1), op, promotedType);
        }
        return null;
    }

    /**
     * Regista verificações estritas de paridade e disparidade (= e <>).
     *
     * @param ctx Equações simétricas.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitEqualityExpr(FavaParser.EqualityExprContext ctx) {
        String leftType = this.treeProperty.get(ctx.expr(0)).getName();
        String rightType = this.treeProperty.get(ctx.expr(1)).getName();
        String promotedType = getPromotedType(leftType, rightType);
        processBinaryOperation(ctx.expr(0), ctx.expr(1), ctx.op.getText(), promotedType);
        return null;
    }

    /**
     * Instrui junção conjuntiva lógica de operadores (AND).
     *
     * @param ctx Portões booleanos AND.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitAndExpr(FavaParser.AndExprContext ctx) {
        String resultType = this.treeProperty.get(ctx).getName();
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        emit(OpCodeMapper.getBinaryOpCode(ctx.op.getText(), resultType));
        return null;
    }

    /**
     * Instrui disjunção conjuntiva (OR).
     *
     * @param ctx Portões lógicos booleanos OR.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitOrExpr(FavaParser.OrExprContext ctx) {
        String resultType = this.treeProperty.get(ctx).getName();
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        emit(OpCodeMapper.getBinaryOpCode(ctx.op.getText(), resultType));
        return null;
    }

//------------- Utility functions -------------------------------------------------------------------------------------------------------------

    /**
     * Avalia e responde pelas regras de coerção numérica. Se cruzarmos reais com inteiros,
     * a resolução eleva o bytecode ao denominador comum superior (Real).
     *
     * @param type1 O string descritor do Tipo da esquerda.
     * @param type2 O string descritor do Tipo da direita.
     * @return A formatação em String indicando a castagem target (ex: "real" ou "integer").
     */
    private String getPromotedType(String type1, String type2) {
        if (type1.equals(type2)) return type1;
        if ((type1.equals("real") && type2.equals("integer")) ||
                (type1.equals("integer") && type2.equals("real")))
            return "real";
        return "String";
    }

    /**
     * Função nuclear do emissor.
     * Desce na AST, executa coerções implícitas exigidas pela promoção matemática,
     * e envia a invocação Mnemónica binária final à classe Helper de mapeamento nativo.
     *
     * @param leftExpr   Contexto originário da esquerda do Sinal.
     * @param rightExpr  Contexto da direita.
     * @param op         Sinal em String recolhido na linguagem base.
     * @param targetType O tipo final ao qual as instruções subjacentes devem submeter-se e converterem-se.
     */
    private void processBinaryOperation(FavaParser.ExprContext leftExpr, FavaParser.ExprContext rightExpr, String op, String targetType) {
        String leftType = this.treeProperty.get(leftExpr).getName();
        String rightType = this.treeProperty.get(rightExpr).getName();

        visit(leftExpr);
        OpCode castLeft = OpCodeMapper.getCastOpCode(leftType, targetType);
        if (castLeft != null) emit(castLeft);

        visit(rightExpr);
        OpCode castRight = OpCodeMapper.getCastOpCode(rightType, targetType);
        if (castRight != null) emit(castRight);

        emit(OpCodeMapper.getBinaryOpCode(op, targetType));
    }

    /**
     * Dispara e encapsula um OpCode isolado no Stream global.
     *
     * @param opc Enumeração formatada Mnemónica sem argumento.
     */
    public void emit(OpCode opc) {
        code.add(new Instruction(opc));
    }

    /**
     * Dispara código de manipulação posicional associado a um argumento estático (Store/Load/Jumps).
     *
     * @param opc A mnemónica associada a processamentos com endereço/valor referencial.
     * @param val O offset absoluto de Memória LIFO ou a flag de salto.
     */
    public void emit(OpCode opc, int val) {
        code.add(new Instruction1Arg(opc, val));
    }

    /**
     * Depura localmente o código extraído num formato de pseudo-assembly em consola.
     */
    public void dumpCode() {
        System.out.println("Generated code in assembly format");
        for (int i = 0; i < code.size(); i++)
            System.out.println(i + ": " + code.get(i));
    }

    /**
     * Imprime no terminal a árvore total em memória referenciada pela Pool de Constantes,
     * útil para testar offsets de leitura do sconst e dconst.
     */
    public void dumpCodePool() {
        System.out.println("*** Constant pool ***");
        for (int i = 0; i < constantPool.size(); i++) {
            if (constantPool.get(i) instanceof String)
                System.out.println(i + ": " + "\"" + constantPool.get(i) + "\"");
            else
                System.out.println(i + ": " + constantPool.get(i));
        }
    }

    /**
     * Registo final formatado requerido pelas avaliações do Mooshak no dump nativo das mnemónicas.
     */
    public void dumpCodeInstructions() {
        System.out.println("*** Instructions ***");
        for (int i = 0; i < code.size(); i++)
            System.out.println(i + ": " + code.get(i));
    }

    /**
     * Finaliza o pipeline de compilação encodando os Arrays Virtuais numa cadência restrita de ficheiros Bytecode (`*.bc`).
     * Realiza a escrita do Header das Constants no topo, embutindo os Bytes de validação (01 para Reais, 03 para Strings),
     * e procede para um dump puro dos construtores da VM.
     *
     * @param filename A representação em String do caminho alvo da exportação (ex: "bytecodes.bc").
     */
    public void saveBytecodes(String filename) {
        try (DataOutputStream dout = new DataOutputStream(new FileOutputStream(filename))) {
            dout.writeInt(this.constantPool.size());
            for (Object constValue : this.constantPool) {
                if (constValue instanceof Double) {
                    dout.writeByte(1);
                    dout.writeDouble((Double) constValue);
                } else if (constValue instanceof String) {
                    dout.writeByte(3);
                    String str = (String) constValue;
                    dout.writeInt(str.length());
                    for (int i = 0; i < str.length(); i++)
                        dout.writeChar(str.charAt(i));

                }
            }
            for (Instruction inst : code) {
                inst.writeTo(dout);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}