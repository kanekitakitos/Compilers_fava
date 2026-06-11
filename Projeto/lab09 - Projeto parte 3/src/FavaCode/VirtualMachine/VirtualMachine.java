package FavaCode.VirtualMachine;

import FavaCode.VirtualMachine.Instruction.*;

import java.util.*;
import java.io.*;

/**
 * Motor central de interpretação da linguagem (Virtual Machine Fava).
 *
 * <p>Responsável por inicializar e gerir o ciclo de vida do programa executável, procedendo à
 * decodificação das instruções nativas contidas no bytearray (gerado pelo {@link FavaCode.CodeGenerator.CodeGen}),
 * à construção do Memory Space para variáveis globais ({@code galloc}, {@code gload}, {@code gstore}),
 * carregamento literais complexos da Constant Pool, e operação da Pilha de Execução em tempo real (Stack).</p>
 * <p>
 * ### Exemplo de Uso
 * <pre>{@code
 * // Ler bytes pré-compilados do disco (ex: .bc file)
 * byte[] codeData = FavaVM.loadBytecodes("programa.bc");
 *
 * // Criar Máquina, carregar Pool e iniciar simulação VM do código Fava
 * VirtualMachine vm = new VirtualMachine(codeData, true); // true = Exibir Tracer (Passo a passo)
 * vm.run();
 * }</pre>
 *
 * @see FavaVM
 * @see OpCode
 * @see Instruction
 * @see VMValue
 */
public class VirtualMachine {
    /**
     * Flag ativada caso o utilizador pretenda visualizar disassembler iterativo, estado da Pilha LIFO e tracing.
     */
    private final boolean trace;       // trace flag

    /**
     * Arrays nativos brutos que guardam todo o binário lido, guardado para fins de dump iterativo na depuração de bytes hexadecimais.
     */
    private final byte[] bytecodes;    // the bytecodes, storing just for displaying them. Not really needed

    /**
     * Coleção traduzida orientada a objetos correspondente a mnemónicas sequenciais a executar (Array de PC).
     */
    private Instruction[] code;        // instructions (converted from the bytecodes)

    /**
     * Pointer que controla em que linha da {@code Instruction[]} decorre atualmente a execução.
     */
    private int IP;                    // instruction pointer

    private int FP;                    // frame pointer

    /**
     * Pilha primária da VM, gerida pelo padrão LIFO (Last In First Out) (ex: `iadd` soma os dois elementos no Topo).
     */
    private final Stack<VMValue> stack = new Stack<>();    // runtime stack

    /**
     * Pool dinâmica em lista construída no Decode, armazenando Doubles e Strings lidos da Secção Inicial do File.
     * Instruções do tipo sconst/dconst farão push baseados nos referidos index para poupar alocação de objetos e peso IO.
     */
    private ArrayList<VMValue> constantPool = new ArrayList<>();

    /**
     * Array fixo criado durante a instrução `galloc` de arranque, utilizado como Random Access Memory de endereçamento direto para atribuição (gstore/gload).
     */
    private VMValue[] globals; // Representa a memoria do meu Sistema


    // Variáveis Estáticas instanciadas preventivamente (Constantes) na JVM para poupar invocação excessiva ao Garbage Collector na Máquina Virtual.
    private static final VMValue TRUE_VALUE = new VMValue(true);
    private static final VMValue FALSE_VALUE = new VMValue(false);

    /**
     * Motor base do arranque da Máquina Fava.
     * Decodifica logo o Byte Array Físico em formato Operacional e coloca a VM a zero, pronta para `run()`).
     *
     * @param bytecodes O código estático que deve ser alimentado ao Decoder em formato DataStream.
     * @param trace     True se for ativada pelo Launcher a opção de rastreabilidade CLI da pilha a cada OpCode.
     */
    public VirtualMachine(byte[] bytecodes, boolean trace) {
        this.trace = trace;
        this.bytecodes = bytecodes;
        decode(bytecodes);
        this.IP = 0; // O Programa Counter regressa ao estado inicial absoluto
        this.FP = 0;
    }

    /**
     * Interceta a String compactada Bytecode nativa, separando em Fases o Parser:
     * <ol>
     *  <li>A extração estrita ao Cabeçalho do Ficheiro (Constant Pool) processando Doubles e text-lengths.</li>
     *  <li>A tradução e instanciação das mnemónicas até receber uma marcação terminal de exaustão EOF (End Of File).</li>
     * </ol>
     *
     * @param bytecodes O código pré-compilado a ser lido para carregar na RAM e Instruction Array de PC.
     */
    private void decode(byte[] bytecodes) {
        ArrayList<Instruction> inst = new ArrayList<>();
        try {
            // Envolve o array de bytes numa stream sequencial otimizada IO
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(bytecodes));

            // ==========================================
            // FASE 1: Ler a Constant Pool (Cabeçalho do BC)
            // ==========================================

            int poolSize = din.readInt(); // Lê Int (4 bytes) da contagem de Variáveis na CP
            for (int i = 0; i < poolSize; i++) {
                byte typeTag = din.readByte(); // Extrai qual Variante o Objeto tem no IO (String / Double)
                switch (typeTag) {
                    case 1 -> {
                        double val = din.readDouble();
                        this.constantPool.add(new VMValue(val));
                    }
                    case 3 -> {
                        int strLen = din.readInt();
                        char[] chars = new char[strLen];
                        for (int j = 0; j < strLen; j++) {
                            chars[j] = din.readChar();
                        }
                        this.constantPool.add(new VMValue(new String(chars)));
                    }
                    default -> runtime_error("Tipo de constante desconhecido ou ilegal presente no bytecode corrompido: " + typeTag);
                }
            }

            // ==========================================
            // FASE 2: Decodificar o Array de Instruções (Mnemónicas Base Fava e OpCodes)
            // ==========================================
            while (true) {
                // Este bloco infinito pára na exceção nativa da invocação EOF IO no SO
                byte b = din.readByte();
                OpCode opc = OpCode.convert(b);

                switch (opc.nArgs()) {
                    case 0:
                        inst.add(new Instruction(opc)); // Comando Base Pura (ex: iadd)
                        break;
                    case 1:
                        int val = din.readInt(); // O Argumento exigido ocupa Integer 4bytes. Usado para referenciar Jumps, Posição da CP, Posição da RAM `globals`
                        inst.add(new Instruction1Arg(opc, val));
                        break;
                    default:
                        System.out.println("Fatal VM Error: Instrução decodificada mal formatada (nArgs inválido em OpCode.java).");
                        System.exit(1);
                }
            }
        } catch (java.io.EOFException e) {
            // Abortagem Limpa em sucesso ao encontrar o Fim do binário IO File.
            // Transfere a Array list num Fix-Sized Instruction[] Array que acelera performance O(1) do Pointer Cycle.
            this.code = new Instruction[inst.size()];
            inst.toArray(this.code);

            if (trace) {
                System.out.println("Disassembled instructions (Modo Dump):");
                dumpInstructionsAndBytecodes();
            }
        } catch (java.io.IOException e) {
            System.out.println("Erro grave I/O Fava VM (Read IO Fail): " + e.getMessage());
        }
    }

    /**
     * Utilidade de Console Trace imprimindo a correspondência entre a linha do Código PC e os referidos valores extraídos
     * nativamente nos Arrays Hexadecimais originais do Decoder File BC.
     * Ajuda nas correções do File Format da Pool de Constantes.
     */
    public void dumpInstructionsAndBytecodes() {
        int idx = 0;
        for (int i = 0; i < code.length; i++) {
            StringBuilder s = new StringBuilder();
            s.append(String.format("%02X ", bytecodes[idx++])); // Transcreve formato OpCode puro para HEX
            if (code[i].nArgs() == 1) // Tem args anexos a si no Array Binário Bytecodes original?
                for (int k = 0; k < 4; k++)
                    s.append(String.format("%02X ", bytecodes[idx++]));
            System.out.println(String.format("%5s: %-15s // %s", i, code[i], s));
        }
    }

    /**
     * Mostra, perante debug manual, a mnemónica da Instrução final traduzida sem o formato complexo hexadecimal.
     */
    public void dumpInstructions() {
        for (int i = 0; i < code.length; i++)
            System.out.println(i + ": " + code[i]);
    }

    /**
     * Dispara exceção personalizada abortando a VM e os seus Pointers (Halt Implícito Forçado).
     * Usada na divisão por zero, casts nulos entre stacks mal formadas ou Tipos Nulos invocados do Runtime Environment.
     *
     * @param msg Texto de alerta injetado pelo Erro Semântico Fatal da Máquina.
     */
    private void runtime_error(String msg) {
        System.out.println("Fava VM Runtime error (Aborting): " + msg);
        if (trace)
            System.out.println(String.format("%22s Stack Dump: %s", "", stack));
        System.exit(1);
    }

    /**
     * Instancia o ciclo contínuo ininterrupto principal da VM baseada no Program Counter Pointer (`IP`).
     * Itera até exceder a Array Code Memory (limite final implícito do programa BC).
     */
    public void run() {
        if (trace) {
            System.out.println("Trace Mode Engine Start");
            System.out.println("Fava Engine Starts Execution Cycle na instrução " + IP);
        }
        while (IP < code.length) {
            exec_inst(code[IP]);
            IP++;
        }
        if (trace)
            System.out.println(String.format("%22s Final Exit Stack (Memory Footprint): %s", "", stack));
    }


//*--------------- instructions / mnemónicas core ------------------------------------------------------------------------------------------

    /**
     * Desempilha (pop) o alvo atual do Topo LIFO instanciando em format Int Base da JVM, escrevendo na Console SO nativa.
     */
    private void exec_iprint() {
        int v = stack.pop().getAsInteger();
        System.out.println(v);
    }

    /**
     * Emite (push) literais puros estáticos inteiros, declarados logo na fase compilatória.
     *
     * @param v Inteiro nativo passado à execução a anexar e transmutar em `VMValue`.
     */
    private void exec_iconst(Integer v) {
        stack.push(new VMValue(v));
    }

    /**
     * Realiza operação matemática lógica Unitária unária negativando a variável inteira topo do Frame Stack LIFO.
     */
    private void exec_iuminus() {
        int v = stack.pop().getAsInteger();
        stack.push(new VMValue(-v));
    }

    /**
     * Retira os 2 alvos imediatos no topo, processando na ULA JVM, e enviando a soma calculada de volta.
     */
    private void exec_iadd() {
        int right = stack.pop().getAsInteger();
        int left = stack.pop().getAsInteger();
        stack.push(new VMValue(left + right));
    }

    /**
     * Aplica Regra (Nível 2 Stack LIFO - Nível 1 Top Stack LIFO) para Subtração correta (Left - Right).
     */
    private void exec_isub() {
        int right = stack.pop().getAsInteger();
        int left = stack.pop().getAsInteger();
        stack.push(new VMValue(left - right));
    }

    /**
     * Aplica a comutativa Multiplicação na stack Top Level inteira.
     */
    private void exec_imult() {
        int right = stack.pop().getAsInteger();
        int left = stack.pop().getAsInteger();
        stack.push(new VMValue(left * right));
    }

    /**
     * Aplicação na Divisão (Left / Right). Invoca interrupção `runtime_error` de Fallback se for requisitada Div zero.
     */
    private void exec_idiv() {
        int right = stack.pop().getAsInteger();
        int left = stack.pop().getAsInteger();

        if (right != 0)
            stack.push(new VMValue(left / right));
        else
            runtime_error("division by 0 is illegal in Fava Virtual Machine");
    }

    /**
     * Operador Remainder, vulgo Módulo Fava, instanciando Left % Right na pilha int.
     */
    private void exec_imod() {
        int right = stack.pop().getAsInteger();
        int left = stack.pop().getAsInteger();
        stack.push(new VMValue(left % right));
    }

    /**
     * Conversão Forçada Fava `cast` em runtime que altera a variável topo para texto (`Integer.toString()`), criando novo `VMValue` de memória string.
     */
    private void exec_itos() {
        String s = stack.pop().getAsInteger().toString();
        stack.push(new VMValue(s));
    }

    /**
     * Conversão / Coerção implicada de valores Inteiros em Reais.
     */
    private void exec_itod() {
        Double v = stack.pop().getAsInteger().doubleValue();
        stack.push(new VMValue(v));
    }

    /**
     * Operação relacional (L <= R). Requer 2 instâncias Inteiras nativas devolvendo a Constante Boolean Fava final na Pilha Top.
     */
    private void exec_ileq() {
        int right = this.stack.pop().getAsInteger();
        int left = this.stack.pop().getAsInteger();
        stack.push(left <= right ? TRUE_VALUE : FALSE_VALUE);
    }

    /**
     * Operação L < R com empurramento do TRUE_VALUE pré-iniciado global.
     */
    private void exec_ilt() {
        int right = this.stack.pop().getAsInteger();
        int left = this.stack.pop().getAsInteger();
        stack.push(left < right ? TRUE_VALUE : FALSE_VALUE);
    }

    /**
     * Diferença L != R gerando o valor True se não for igual na grandeza int.
     */
    private void exec_ineq() {
        int right = this.stack.pop().getAsInteger();
        int left = this.stack.pop().getAsInteger();
        stack.push(right != left ? TRUE_VALUE : FALSE_VALUE);
    }

    /**
     * Igualdade entre variáveis de tamanho 32 Bits L == R na pilha LIFO nativa.
     */
    private void exec_ieq() {
        int right = this.stack.pop().getAsInteger();
        int left = this.stack.pop().getAsInteger();
        stack.push(right == left ? TRUE_VALUE : FALSE_VALUE);
    }

    /**
     * Carrega para o topo da Stack de Execução, o objeto Double / Real gravado pela Constant Pool no Start.
     *
     * @param index O índice do Elemento na `ArrayList<VMValue> constantPool`.
     */
    private void exec_dconst(Integer index) {
        stack.push(this.constantPool.get(index));
    }

    /**
     * Acede Index Array da VM e acopla a String formatada literal para push da Execution Stack.
     *
     * @param index O Pointer posicional em Pool para a String base Fava.
     */
    private void exec_sconst(Integer index) {
        stack.push(this.constantPool.get(index));
    }

    /**
     * Aciona I/O do System Nativo convertendo Double Real do Fava compilado.
     */
    private void exec_dprint() {
        double v = this.stack.pop().getAsReal();
        System.out.println(v);
    }

    /**
     * Módulo Unitário sobre variável ponto-flutuante nativa (-X.X).
     */
    private void exec_duminus() {
        double v = stack.pop().getAsReal();
        stack.push(new VMValue(-v));
    }

    /**
     * Push de Left + Right sob alocação Real/Double de 64 bits.
     */
    private void exec_dadd() {
        double right = stack.pop().getAsReal();
        double left = stack.pop().getAsReal();
        stack.push(new VMValue(left + right));
    }

    /**
     * Executa Subtração Base L - R real.
     */
    private void exec_dsub() {
        double right = stack.pop().getAsReal();
        double left = stack.pop().getAsReal();
        stack.push(new VMValue(left - right));
    }

    /**
     * Mult de precisão dupla flutuante de Top Stack.
     */
    private void exec_dmult() {
        double right = stack.pop().getAsReal();
        double left = stack.pop().getAsReal();
        stack.push(new VMValue(left * right));
    }

    /**
     * Divisão estritamente de valores nativos precisos. Atira Fault Traceable caso se forneça RHS a zero.
     */
    private void exec_ddiv() {
        double right = stack.pop().getAsReal();
        double left = stack.pop().getAsReal();

        if (right != 0)
            stack.push(new VMValue(left / right));
        else
            runtime_error("division by 0 is illegal in Fava Virtual Machine Double");
    }

    /**
     * Verificação == de Double/Reais empurrando as pré-calculadas variáveis True/False.
     */
    private void exec_deq() {
        double right = this.stack.pop().getAsReal();
        double left = this.stack.pop().getAsReal();
        stack.push(right == left ? TRUE_VALUE : FALSE_VALUE);
    }

    /**
     * Operação Menor < nas lógicas Real.
     */
    private void exec_dlt() {
        double right = this.stack.pop().getAsReal();
        double left = this.stack.pop().getAsReal();
        stack.push(left < right ? TRUE_VALUE : FALSE_VALUE);
    }

    /**
     * Operação Diferente Real (!=).
     */
    private void exec_dneq() {
        double right = this.stack.pop().getAsReal();
        double left = this.stack.pop().getAsReal();
        stack.push(right != left ? TRUE_VALUE : FALSE_VALUE);
    }

    /**
     * Lógica Comparativa L <= R na precisão flutuante devolvendo Variáveis de Boolean LIFO nativas (Constant TRUE/FALSE).
     */
    private void exec_dleq() {
        double right = this.stack.pop().getAsReal();
        double left = this.stack.pop().getAsReal();
        stack.push(left <= right ? TRUE_VALUE : FALSE_VALUE);
    }

    /**
     * Extrato de Stack Real para re-formato de texto e inserido como um String VMValue na Exec Stack LIFO.
     */
    private void exec_dtos() {
        String s = stack.pop().getAsReal().toString();
        stack.push(new VMValue(s));
    }

    /**
     * IO do formato Fava Textual Nativo com Output via Pop do Array Base.
     */
    private void exec_sprint() {
        String s = this.stack.pop().getAsString();
        System.out.println(s);
    }

    /**
     * Reune 2 componentes top LIFO formatadas em Strings base, criando um objecto concatenado.
     */
    private void exec_sconcat() {
        String right = this.stack.pop().getAsString();
        String left = this.stack.pop().getAsString();
        stack.push(new VMValue(left + right));
    }

    /**
     * Validação via Método Equality Base JVM `.equals()` acoplando Boolean equivalente a Strings.
     */
    private void exec_seq() {
        String right = this.stack.pop().getAsString();
        String left = this.stack.pop().getAsString();
        stack.push(left.equals(right) ? TRUE_VALUE : FALSE_VALUE);
    }

    /**
     * Oposição da operação Base com not-equals.
     */
    private void exec_sneq() {
        String right = this.stack.pop().getAsString();
        String left = this.stack.pop().getAsString();
        stack.push(!left.equals(right) ? TRUE_VALUE : FALSE_VALUE);
    }

    /**
     * Carrega a estática pre-concebida TRUE em Stack. Reutiliza o Constant Estático Singleton da VM.
     */
    private void exec_tconst() {
        stack.push(TRUE_VALUE);
    }

    /**
     * Push de estática otimizada lógica Booleano Null/False da linguagem.
     */
    private void exec_fconst() {
        stack.push(FALSE_VALUE);
    }

    /**
     * Regra (==) processada no nível Fava Boolean Stack Top Level.
     */
    private void exec_beq() {
        boolean right = this.stack.pop().getAsBool();
        boolean left = this.stack.pop().getAsBool();
        stack.push(left == right ? TRUE_VALUE : FALSE_VALUE);
    }

    /**
     * Diferença Lógica != perante 2 lógicas Bool LIFO.
     */
    private void exec_bneq() {
        boolean right = this.stack.pop().getAsBool();
        boolean left = this.stack.pop().getAsBool();
        stack.push(left != right ? TRUE_VALUE : FALSE_VALUE);
    }

    /**
     * Retirada LIFO para console IO nativa OS imprimindo true / false com pop de stack.
     */
    private void exec_bprint() {
        boolean b = this.stack.pop().getAsBool();

        if (b)
            System.out.println("true");
        else
            System.out.println("false");
    }

    /**
     * AND avaliado nos dois Tops Stack (&&).
     */
    private void exec_and() {
        boolean right = this.stack.pop().getAsBool();
        boolean left = this.stack.pop().getAsBool();
        stack.push(left && right ? TRUE_VALUE : FALSE_VALUE);
    }

    /**
     * Interfere e envia negação unária prefix (! / not) do VMValue contido na LIFO Pilha.
     */
    private void exec_not() {
        boolean b = this.stack.pop().getAsBool();
        stack.push(!b ? TRUE_VALUE : FALSE_VALUE);
    }

    /**
     * Subordinação Operativa OR da Pilha Nativa Fava (||).
     */
    private void exec_or() {
        boolean right = this.stack.pop().getAsBool();
        boolean left = this.stack.pop().getAsBool();
        stack.push(left || right ? TRUE_VALUE : FALSE_VALUE);
    }

    /**
     * Cast puro na Pilha Bool LIFO empurrando as primitivas em Strings Fava (Textual Mode).
     */
    private void exec_btos() {
        String s = this.stack.pop().getAsBool().toString();
        stack.push(new VMValue(s));
    }

    /**
     * Instrução de paragem pura encadeada e lançada com Code O ao Kernel System (Paragem sem Dump de Exceção/Limpa).
     */
    private void exec_halt() {
        System.exit(0);
    }


    /**
     * Processa a quebra de salto lógico no Ciclo (while / If sem condições) forçando alteração
     * drástica na numeração Lida do Program Counter Loop.
     *
     * @param addr Índice onde o Code[] deverá focar-se a seguir (salto estático de PC -1).
     */
    private void exec_jump(int addr) {
        // -1 porque o ciclo "run()" aplica IP++ e avançaríamos em avanço falhado em um Index!
        this.IP = addr - 1;

    }

    /**
     * Fallback Conditional (If e While) Backpatched pelo Compilador na CodeGen Memory.
     * Saca top level Booleano e salta para Index (PC -> addr - 1) APENAS SE o mesmo testar False!
     *
     * @param addr Posição em Code[] (Instruções index de falha de Loop).
     */
    private void exec_jumpf(int addr) {

        boolean b = this.stack.pop().getAsBool();

        // Se for falso (false), fazemos o salto lógico (Break Cycle / Jump To Else)
        if (!b)
            this.IP = addr - 1;


    }

    /**
     * Operação única reservada ao Boot Fava Mnemónica geradora do tamanho alocado
     * para Variáveis da RAM estática LIFO de Array Pointer (VMValue[] globals).
     *
     * @param n A dimensão bruta processada do Pico Memory (maxAllocated do CodeGen).
     */
    private void exec_galloc(int n) {
        this.globals = new VMValue[n];

        for (int i = 0; i < n; i++)
            this.globals[i] = new VMValue();

    }

    /**
     * Instrução nativa resgatando Endereço de RAM `globals` Fava e empurrando (push) cópia à Stack para ser avaliado LIFO.
     *
     * @param addr Endereço do Array de alocação de Variável Memory GenCode.
     */
    private void exec_gload(int addr) {
        this.stack.push(this.globals[addr]);
    }

    /**
     * Tira a resolução Top Stack em runtime VMValue e aloca definitivamente debaixo
     * da indexação de Variáveis instanciadas na Memory Allocation System (RAM base de Variáveis da Linguagem).
     *
     * @param addr Registo base da Variável no IO (Global Array).
     */
    private void exec_gstore(int addr) {

        this.globals[addr] = this.stack.pop();
    }

    private void exec_lalloc(int n) {
        for (int i = 0; i < n; i++) {
            stack.push(new VMValue());
        }
    }

    private void exec_lload(int addr) {
        int index = FP + addr;
        if (index < 0 || index >= stack.size()) {
            runtime_error("invalid local load address: FP=" + FP + " addr=" + addr);
        }
        stack.push(stack.get(index));
    }

    private void exec_lstore(int addr) {
        int index = FP + addr;
        if (index < 0 || index >= stack.size()) {
            runtime_error("invalid local store address: FP=" + FP + " addr=" + addr);
        }
        stack.set(index, stack.pop());
    }

    private void exec_pop(int n) {
        for (int i = 0; i < n; i++) {
            if (stack.isEmpty()) {
                runtime_error("pop from empty stack");
            }
            stack.pop();
        }
    }

    private void exec_call(int addr) {
        int returnAddr = IP + 1;
        stack.push(new VMValue(FP));
        FP = stack.size() - 1;
        stack.push(new VMValue(returnAddr));
        IP = addr - 1;
    }

    private void exec_retval(int nArgs) {
        VMValue returnValue = stack.pop();

        while (stack.size() > FP + 2) {
            stack.pop();
        }

        int returnAddr = stack.pop().getAsInteger();
        int oldFP = stack.pop().getAsInteger();
        FP = oldFP;

        exec_pop(nArgs);
        stack.push(returnValue);

        IP = returnAddr - 1;
    }

    private void exec_ret(int nArgs) {
        while (stack.size() > FP + 2) {
            stack.pop();
        }

        int returnAddr = stack.pop().getAsInteger();
        int oldFP = stack.pop().getAsInteger();
        FP = oldFP;

        exec_pop(nArgs);

        IP = returnAddr - 1;
    }

    /**
     * Interpretador e Roteador Central. Analisa cada `Instruction[]` na iteração do Code Array do programa.
     * Identifica em Swtich-Case Baseado no Mnemónico Extrativo o Método Operacional Nativo.
     * Inicia "casts" na Array Type se a Instruction exigir a sua leitura adicional em int Argumentos (Ex: {@link Instruction1Arg}).
     *
     * @param inst Sub-Array contendo as propriedades estáticas Fava a intercetar na Máquina Central.
     */
    private void exec_inst(Instruction inst) {
        if (trace) {
            String globalsStr = (globals == null) ? "[]" : Arrays.toString(globals);
            System.out.println(String.format("%5s: %-15s Stack VM: %s\n  \t\t\t\t\t   Globals: %s \n\t\t\t\t\t   FP: %s, IP: %s\n", IP, inst, stack, globalsStr, FP, IP));
        }
        OpCode opc = inst.getOpCode();
        int v;
        switch (opc) {
            case iconst:
                v = ((Instruction1Arg) inst).getArg();
                exec_iconst(v);
                break;
            case dconst:
                v = ((Instruction1Arg) inst).getArg();
                exec_dconst(v);
                break;
            case sconst:
                v = ((Instruction1Arg) inst).getArg();
                exec_sconst(v);
                break;
            case iuminus:
                exec_iuminus();
                break;
            case iadd:
                exec_iadd();
                break;
            case isub:
                exec_isub();
                break;
            case imult:
                exec_imult();
                break;
            case idiv:
                exec_idiv();
                break;
            case iprint:
                exec_iprint();
                break;
            case imod:
                exec_imod();
                break;
            case ieq:
                exec_ieq();
                break;
            case ineq:
                exec_ineq();
                break;
            case ilt:
                exec_ilt();
                break;
            case ileq:
                exec_ileq();
                break;
            case itod:
                exec_itod();
                break;
            case itos:
                exec_itos();
                break;
            case dprint:
                exec_dprint();
                break;
            case duminus:
                exec_duminus();
                break;
            case dadd:
                exec_dadd();
                break;
            case dsub:
                exec_dsub();
                break;
            case dmult:
                exec_dmult();
                break;
            case ddiv:
                exec_ddiv();
                break;
            case deq:
                exec_deq();
                break;
            case dneq:
                exec_dneq();
                break;
            case dlt:
                exec_dlt();
                break;
            case dleq:
                exec_dleq();
                break;
            case dtos:
                exec_dtos();
                break;
            case sprint:
                exec_sprint();
                break;
            case sconcat:
                exec_sconcat();
                break;
            case seq:
                exec_seq();
                break;
            case sneq:
                exec_sneq();
                break;
            case tconst:
                exec_tconst();
                break;
            case fconst:
                exec_fconst();
                break;
            case bprint:
                exec_bprint();
                break;
            case beq:
                exec_beq();
                break;
            case bneq:
                exec_bneq();
                break;
            case and:
                exec_and();
                break;
            case or:
                exec_or();
                break;
            case not:
                exec_not();
                break;
            case btos:
                exec_btos();
                break;
            case jump:
                v = ((Instruction1Arg) inst).getArg();
                exec_jump(v);
                break;
            case jumpf:
                v = ((Instruction1Arg) inst).getArg();
                exec_jumpf(v);
                break;
            case galloc:
                v = ((Instruction1Arg) inst).getArg();
                exec_galloc(v);
                break;
            case gload:
                v = ((Instruction1Arg) inst).getArg();
                exec_gload(v);
                break;
            case gstore:
                v = ((Instruction1Arg) inst).getArg();
                exec_gstore(v);
                break;
            case lalloc:
                v = ((Instruction1Arg) inst).getArg();
                exec_lalloc(v);
                break;
            case lload:
                v = ((Instruction1Arg) inst).getArg();
                exec_lload(v);
                break;
            case lstore:
                v = ((Instruction1Arg) inst).getArg();
                exec_lstore(v);
                break;
            case pop:
                v = ((Instruction1Arg) inst).getArg();
                exec_pop(v);
                break;
            case call:
                v = ((Instruction1Arg) inst).getArg();
                exec_call(v);
                break;
            case retval:
                v = ((Instruction1Arg) inst).getArg();
                exec_retval(v);
                break;
            case ret:
                v = ((Instruction1Arg) inst).getArg();
                exec_ret(v);
                break;

            case halt:
                exec_halt();
                break;


            default:
                System.out.println("Fava VM Critial Failure: Ficheiro corrompido em decodificação exec_inst() -> OpCode Inválido");
                System.exit(1);
        }
    }
}
