package FavaCode.CodeGenerator;

import java.io.*;
import java.util.*;

import FavaCode.Parser.Fava.*;
import FavaCode.Semantic.FavaType;
import FavaCode.Semantic.SymbolTable;
import FavaCode.Semantic.TypeRules;
import FavaCode.Semantic.Types.FunctionType;
import FavaCode.CodeGenerator.Scopes.GlobalScope;
import FavaCode.CodeGenerator.Scopes.LocalScope;
import FavaCode.VirtualMachine.OpCode;
import FavaCode.VirtualMachine.Instruction.*;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

/**
 * Visitante do Gerador de Código para o compilador da linguagem Fava.
 *
 * <p>Atua após a fase da Análise Semântica, traduzindo a Abstract Syntax Tree (AST) em instruções bytecode ({@link OpCode})
 * executáveis pela Máquina Virtual Fava. Gere a alocação de variáveis na memória ({@link GlobalScope}), resolve Jumps de controlo de fluxo
 * (IFs e While) através de Backpatching e constrói a Pool de Constantes para literais (Strings e Doubles).</p>
 *
 * <p><b>Suporte TP-3 (Funções e Variáveis Locais)</b></p>
 * <p>
 * Este gerador suporta funções através de:
 * <ul>
 *   <li>Emissão de um bootstrap: executa inicializações globais, chama {@code main} e termina com {@code halt}.</li>
 *   <li>Emissão dos corpos das funções após o bootstrap (para evitar queda em execução).</li>
 *   <li>Backpatching de {@code call} para suportar forward calls (inclui recursão).</li>
 *   <li>Gestão de variáveis locais/parametrização via {@link LocalScope} e opcodes {@code lalloc/lload/lstore}.</li>
 * </ul>
 * </p>
 *
 * <p><b>Convenção de frame (alinhada com a VM)</b></p>
 * <ul>
 *   <li>{@code FP + 0}: oldFP</li>
 *   <li>{@code FP + 1}: returnAddr</li>
 *   <li>{@code FP + 2...}: variáveis locais</li>
 *   <li>{@code FP - nArgs ... FP - 1}: parâmetros (offsets negativos)</li>
 * </ul>
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
 * CodeGen codeGen = new CodeGen(symbolTable, semanticAnalyzer.getTypesTree());
 * codeGen.visit(tree);
 *
 * // Depois da AST ter sido visitada e as instruções geradas:
 * codeGen.dumpCodeInstructions(); // Imprime o código no terminal
 * codeGen.saveBytecodes("output.bc"); // Exporta o binário
 * }</pre>
 *
 * @see FavaCode.VirtualMachine.VirtualMachine
 * @see OpCodeMapper
 * @see GlobalScope
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
    private final GlobalScope globalScope = new GlobalScope();

    /**
     * Memória local da função atualmente em compilação.
     * {@code null} significa que o gerador está a compilar código fora de uma função (top-level).
     */
    private LocalScope localScope = null;

    /**
     * Número de argumentos da função atual. Usado para emitir {@code ret/retval} e offsets de parâmetros.
     */
    private int currentFunctionNArgs = 0;

    /**
     * Tipo de retorno da função atual, ou {@code null} se a função for void.
     */
    private FavaType currentFunctionReturnType = null;

    /**
     * Motor de regras de tipos, usado para centralizar coerções e promoções (ex.: {@code integer -> real}).
     */
    private final TypeRules typeRules = new TypeRules();


    /**
     * Tabela de símbolos já populada na fase semântica (inclui assinaturas de funções).
     * É usada pelo CodeGen para inferir o número de argumentos e o tipo de retorno.
     */
    private final SymbolTable symbolTable;

    /**
     * Mapa de endereços de entrada de função: {@code functionName -> instructionIndex}.
     */
    private final HashMap<String, Integer> functionAddrs = new HashMap<>();

    /**
     * Lista de {@code call} emitidos com destino desconhecido no momento (forward calls).
     * É resolvida no final de {@link #visitProg(FavaParser.ProgContext)}.
     */
    private final ArrayList<PendingCall> pendingCalls = new ArrayList<>();

    /**
     * Índice do {@code call main} emitido no bootstrap e que será backpatchado após gerar as funções.
     */
    private int mainCallPatchIndex = -1;

    /**
     * Marcador imutável para backpatching de chamadas a funções.
     *
     * @param instructionIndex Índice da instrução {@code call} no array {@link #code}.
     * @param functionName     Nome da função alvo (normalizado para lower-case).
     */
    private record PendingCall(int instructionIndex, String functionName) {}

    /**
     * Inicializa o Gerador de Código.
     *
     * @param symbolTable  Tabela de símbolos já populada (inclui assinaturas de funções).
     * @param treeProperty Propriedades extraídas da Análise Semântica, vitais para inferir as instruções corretas de cast e matemática.
     */
    public CodeGen(SymbolTable symbolTable, ParseTreeProperty<FavaType> treeProperty) {
        this.symbolTable = symbolTable;
        this.treeProperty = treeProperty;
    }

//*-------------- Helpers de Otimização ------------------------------------------------------------------------

    /**
     * Adiciona ou reaproveita literais na Constant Pool de forma eficiente.
     * Previne o inchaço da memória binária ao reutilizar referências para strings ou doubles idênticos.
     *
     * @param value O valor literal (Double ou String) a registar.
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
    public Void visitBlock(FavaParser.BlockContext ctx) 
    {
        if (localScope != null)
        {
            localScope.enterScope();
            int lallocPatchIndex = code.size();
            emit(OpCode.lalloc, 0);

            visitChildren(ctx);

            int nLocals = localScope.exitScopeAndGetPopCount();
            code.set(lallocPatchIndex, new Instruction1Arg(OpCode.lalloc, nLocals));
            if (nLocals > 0) {
                emit(OpCode.pop, nLocals);
            }
        }
        else
        {
            globalScope.enterScope();
            visitChildren(ctx);
            globalScope.exitScope();
        }
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
        FavaType declaredType = FavaType.resolve(typeName);

        for (FavaParser.VarInitContext initCtx : ctx.varInit()) {
            String varName = initCtx.ID().getText();
            boolean isLocal = localScope != null;
            int addr = isLocal ? localScope.defineLocal(varName) : globalScope.alloc(varName);

            if (initCtx.expr() != null) {
                visit(initCtx.expr());
                FavaType exprType = treeProperty.get(initCtx.expr());

                emitCastIfNeeded(exprType, declaredType);
                emit(isLocal ? OpCode.lstore : OpCode.gstore, addr);
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
        String varName = ctx.ID().getText();
        Integer localAddr = (localScope != null) ? localScope.resolve(varName) : null;
        int addr = (localAddr != null) ? localAddr : globalScope.getAddress(varName);
        FavaType expectedType = treeProperty.get(ctx);

        visit(ctx.expr());

        FavaType actualType = treeProperty.get(ctx.expr());
        emitCastIfNeeded(actualType, expectedType);

        emit((localAddr != null) ? OpCode.lstore : OpCode.gstore, addr);
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
        String varName = ctx.ID().getText();
        Integer localAddr = (localScope != null) ? localScope.resolve(varName) : null;
        if (localAddr != null) {
            emit(OpCode.lload, localAddr);
        } else {
            int addr = globalScope.getAddress(varName);
            emit(OpCode.gload, addr);
        }
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
        // Reserva a instrução inicial para alocar a memória global (valor final é backpatchado no fim).
        emit(OpCode.galloc, 0);

        // Top-level (antes das funções): permite variáveis globais e statements soltos (mantém compatibilidade com a gramática atual).
        for (FavaParser.StatContext stat : ctx.stat()) {
            if (!(stat instanceof FavaParser.FunctionDeclStatContext)) {
                visit(stat);
            }
        }

        // Bootstrap: chama main e termina o programa.
        mainCallPatchIndex = code.size();
        emit(OpCode.call, -1);
        emit(OpCode.halt);

        // Emite os corpos das funções (não executados diretamente devido ao halt anterior).
        for (FavaParser.StatContext stat : ctx.stat()) {
            if (stat instanceof FavaParser.FunctionDeclStatContext) {
                visit(stat);
            }
        }

        // Backpatch do galloc com o pico de memória global.
        int total = globalScope.getTotalAllocations();
        backpatch(0, OpCode.galloc, total);

        // Backpatch do call main.
        Integer mainAddr = functionAddrs.get("main");
        if (mainAddr != null && mainCallPatchIndex >= 0) {
            code.set(mainCallPatchIndex, new Instruction1Arg(OpCode.call, mainAddr));
        }

        // Backpatch de todos os call com destino resolvido.
        for (PendingCall pendingCall : pendingCalls) {
            Integer addr = functionAddrs.get(pendingCall.functionName());
            if (addr != null) {
                code.set(pendingCall.instructionIndex(), new Instruction1Arg(OpCode.call, addr));
            }
        }

        optimizeNoOpsAndRebaseAddresses();
        return null;
    }

    /**
     * Entrada auxiliar para declarações de função quando elas surgem como {@code stat} no topo do programa.
     *
     * @param ctx Contexto do parser para {@code functionDeclStat}.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitFunctionDeclStat(FavaParser.FunctionDeclStatContext ctx) {
        return visit(ctx.functionDecl());
    }

    /**
     * Emite o bytecode do corpo de uma função, criando um contexto de variáveis locais e mapeando parâmetros.
     *
     * <p>Responsabilidades:</p>
     * <ul>
     *   <li>Registar o endereço de entrada da função ({@code functionName -> instructionIndex}).</li>
     *   <li>Inicializar {@link LocalScope} com o número de argumentos (para offsets negativos a {@code FP}).</li>
     *   <li>Visitar o bloco, emitindo instruções e alocações locais.</li>
     *   <li>Em funções void, garantir um {@code ret nArgs} caso o utilizador não escreva {@code return}.</li>
     * </ul>
     *
     * <p>Pré-condição: a assinatura ({@link FunctionType}) já foi registada na {@link SymbolTable} na fase semântica.</p>
     *
     * @param ctx Contexto do parser para {@code functionDecl}.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitFunctionDecl(FavaParser.FunctionDeclContext ctx) {
        String funcName = ctx.ID().getText().toLowerCase();

        // Regista o endereço de entrada da função.
        functionAddrs.put(funcName, code.size());

        // Resolve a assinatura (nArgs e tipo de retorno) a partir da SymbolTable.
        FavaType fType = symbolTable.getType(funcName);
        FunctionType signature = (fType instanceof FunctionType) ? (FunctionType) fType : null;

        int nArgs = (signature != null) ? signature.getParamTypes().size() : 0;
        FavaType returnType = (signature != null) ? signature.getReturnType() : null;

        // Salva o contexto atual para suportar futuras extensões (ex.: funções aninhadas).
        LocalScope previousLocalMemory = localScope;
        int previousNArgs = currentFunctionNArgs;
        FavaType previousReturnType = currentFunctionReturnType;

        localScope = new LocalScope(nArgs);
        currentFunctionNArgs = nArgs;
        currentFunctionReturnType = returnType;

        // Mapeia os parâmetros como endereços relativos negativos a FP.
        if (ctx.formalParameters() != null) {
            List<FavaParser.FormalParameterContext> params = ctx.formalParameters().formalParameter();
            for (int i = 0; i < params.size(); i++) {
                localScope.defineParam(params.get(i).ID().getText(), i);
            }
        }

        visit(ctx.block());

        if (currentFunctionReturnType == null) {
            emit(OpCode.ret, nArgs);
        }

        // Restaura o contexto anterior.
        localScope = previousLocalMemory;
        currentFunctionNArgs = previousNArgs;
        currentFunctionReturnType = previousReturnType;
        return null;
    }

    /**
     * Otimização local (peephole) e rebaseamento de endereços de controlo de fluxo.
     *
     * <p>Remove instruções semanticamente neutras emitidas como placeholders (ex.: {@code galloc 0} e {@code lalloc 0})
     * e ajusta destinos de {@code call/jump/jumpf} para refletir os novos índices após remoções.</p>
     *
     * <p>Nota: esta etapa é uma otimização simples, aplicada no final da geração.</p>
     */
    private void optimizeNoOpsAndRebaseAddresses() {
        boolean[] removed = new boolean[code.size()];
        for (int i = 0; i < code.size(); i++) {
            Instruction inst = code.get(i);
            if (inst instanceof Instruction1Arg inst1) {
                if (inst1.getOpCode() == OpCode.galloc && inst1.getArg() == 0) {
                    removed[i] = true;
                }
                if (inst1.getOpCode() == OpCode.lalloc && inst1.getArg() == 0) {
                    removed[i] = true;
                }
            }
        }

        int[] removedBefore = new int[removed.length + 1];
        for (int i = 0; i < removed.length; i++) {
            removedBefore[i + 1] = removedBefore[i] + (removed[i] ? 1 : 0);
        }
        if (removedBefore[removed.length] == 0) return;

        ArrayList<Instruction> newCode = new ArrayList<>(code.size() - removedBefore[removed.length]);
        for (int i = 0; i < code.size(); i++) {
            if (removed[i]) continue;
            Instruction inst = code.get(i);
            if (inst instanceof Instruction1Arg inst1) {
                OpCode opc = inst1.getOpCode();
                int arg = inst1.getArg();
                if (arg >= 0 && (opc == OpCode.call || opc == OpCode.jump || opc == OpCode.jumpf)) {
                    int idx = Math.min(arg, removed.length);
                    int newArg = arg - removedBefore[idx];
                    newCode.add(new Instruction1Arg(opc, newArg));
                    continue;
                }
            }
            newCode.add(inst);
        }
        code.clear();
        code.addAll(newCode);
    }

    /**
     * Emite o retorno de uma função no contexto de variáveis locais (quando {@link #localScope} não é {@code null}).
     *
     * <p>Regras aplicadas:</p>
     * <ul>
     *   <li>{@code return expr;} avalia a expressão, aplica coerção implícita quando necessário e emite {@code retval nArgs}.</li>
     *   <li>{@code return;} emite {@code ret nArgs}.</li>
     * </ul>
     *
     * @param ctx Contexto do parser do {@code return}.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitReturnStat(FavaParser.ReturnStatContext ctx) {
        if (localScope == null) return null;

        if (ctx.expr() != null) {
            // Return com valor: avalia expressão, aplica coerção quando necessário, e devolve via retval(nArgs).
            visit(ctx.expr());

            FavaType exprType = treeProperty.get(ctx.expr());
            emitCastIfNeeded(exprType, currentFunctionReturnType);

            emit(OpCode.retval, currentFunctionNArgs);
        } else {
            // Return void: devolve via ret(nArgs).
            emit(OpCode.ret, currentFunctionNArgs);
        }
        return null;
    }

    /**
     * Emite uma chamada de função usada como statement (o valor de retorno, se existir, é descartado).
     *
     * @param ctx Contexto do parser da chamada em statement.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitFunctionCallStat(FavaParser.FunctionCallStatContext ctx) {
        FunctionType signature = emitFunctionCall(ctx.functionCall());
        if (signature != null && signature.getReturnType() != null) {
            emit(OpCode.pop, 1);
        }
        return null;
    }

    /**
     * Emite uma chamada de função usada como expressão (o valor de retorno fica no topo da stack).
     *
     * @param ctx Contexto do parser da chamada em expressão.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitFunctionCallExpr(FavaParser.FunctionCallExprContext ctx) {
        emitFunctionCall(ctx.functionCall());
        return null;
    }

    /**
     * Emite a sequência de bytecode para uma chamada de função:
     * empilha argumentos (com coerções implícitas quando necessário) e emite {@code call} com placeholder.
     *
     * <p>O endereço final do {@code call} é resolvido por backpatching no final de {@link #visitProg(FavaParser.ProgContext)}.</p>
     *
     * @param ctx Contexto do parser da chamada.
     * @return A assinatura da função (quando disponível) para o chamador decidir o que fazer com o retorno.
     */
    private FunctionType emitFunctionCall(FavaParser.FunctionCallContext ctx) {
        String funcName = ctx.ID().getText().toLowerCase();

        FavaType fType = symbolTable.getType(funcName);
        FunctionType signature = (fType instanceof FunctionType) ? (FunctionType) fType : null;

        List<FavaParser.ExprContext> args = (ctx.exprList() != null) ? ctx.exprList().expr() : Collections.emptyList();
        List<FavaType> paramTypes = (signature != null) ? signature.getParamTypes() : Collections.emptyList();

        for (int i = 0; i < args.size(); i++) {
            FavaParser.ExprContext arg = args.get(i);
            visit(arg);

            if (i < paramTypes.size()) {
                FavaType actualType = treeProperty.get(arg);
                FavaType expectedType = paramTypes.get(i);
                emitCastIfNeeded(actualType, expectedType);
            }
        }

        // call é emitido com placeholder e backpatchado no fim do programa.
        int idx = code.size();
        emit(OpCode.call, -1);
        pendingCalls.add(new PendingCall(idx, funcName));
        return signature;
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
        FavaType operandType = this.treeProperty.get(ctx.expr());
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
        FavaType operandType = this.treeProperty.get(ctx.expr());
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
        FavaType resultType = this.treeProperty.get(ctx);
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
        FavaType resultType = this.treeProperty.get(ctx);
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
        FavaType resultType = this.treeProperty.get(ctx);
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
        FavaType leftType = this.treeProperty.get(ctx.expr(0));
        FavaType rightType = this.treeProperty.get(ctx.expr(1));
        FavaType promotedType = typeRules.getPromotedType(leftType, rightType);

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
        FavaType leftType = this.treeProperty.get(ctx.expr(0));
        FavaType rightType = this.treeProperty.get(ctx.expr(1));
        FavaType promotedType = typeRules.getPromotedType(leftType, rightType);
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
        FavaType resultType = this.treeProperty.get(ctx);
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
        FavaType resultType = this.treeProperty.get(ctx);
        visit(ctx.expr(0));
        visit(ctx.expr(1));
        emit(OpCodeMapper.getBinaryOpCode(ctx.op.getText(), resultType));
        return null;
    }

//------------- Utility functions -------------------------------------------------------------------------------------------------------------

    /**
     * Função nuclear do emissor.
     * Desce na AST, executa coerções implícitas exigidas pela promoção matemática,
     * e envia a invocação Mnemónica binária final à classe Helper de mapeamento nativo.
     *
     * @param leftExpr   Contexto originário da esquerda do Sinal.
     * @param rightExpr  Contexto da direita.
     * @param op         Sinal em String recolhido na linguagem base.
     * @param targetType O tipo final (inferido na análise semântica) ao qual as instruções subjacentes devem submeter-se e converterem-se.
     */
    private void processBinaryOperation(FavaParser.ExprContext leftExpr, FavaParser.ExprContext rightExpr, String op, FavaType targetType) {
        FavaType leftType = this.treeProperty.get(leftExpr);
        FavaType rightType = this.treeProperty.get(rightExpr);

        visit(leftExpr);
        OpCode castLeft = OpCodeMapper.getCastOpCode(leftType, targetType);
        if (castLeft != null) emit(castLeft);

        visit(rightExpr);
        OpCode castRight = OpCodeMapper.getCastOpCode(rightType, targetType);
        if (castRight != null) emit(castRight);

        emit(OpCodeMapper.getBinaryOpCode(op, targetType));
    }

    /**
     * Emite um cast implícito quando a VM requer conversão entre {@code fromType} e {@code toType}.
     *
     * <p>A decisão do opcode de coerção é centralizada no {@link OpCodeMapper#getCastOpCode(FavaType, FavaType)}.</p>
     *
     * @param fromType Tipo atual do valor.
     * @param toType   Tipo desejado/esperado.
     */
    private void emitCastIfNeeded(FavaType fromType, FavaType toType) {
        if (fromType == null || toType == null) return;
        OpCode cast = OpCodeMapper.getCastOpCode(fromType, toType);
        if (cast != null) emit(cast);
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
                switch (constValue) {
                    case Double d -> {
                        dout.writeByte(1);
                        dout.writeDouble(d);
                    }
                    case String s -> {
                        dout.writeByte(3);
                        dout.writeInt(s.length());
                        dout.writeChars(s);
                    }
                    default -> throw new IllegalStateException("Tipo não suportado na constant pool: " + constValue.getClass().getName());
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
