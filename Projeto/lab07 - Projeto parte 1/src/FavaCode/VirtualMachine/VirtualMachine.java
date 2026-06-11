package FavaCode.VirtualMachine;

import FavaCode.VirtualMachine.Instruction.*;

import java.util.*;
import java.io.*;

/**
 * A Máquina Virtual (VM) da linguagem Fava, responsável por interpretar e executar os bytecodes gerados pelo compilador.
 * <p>
 * O ciclo de vida da VM é: decodificação das instruções a partir de bytecodes -> carregamento do constant pool -> execução das instruções usando a pilha.
 *
 * @see FavaVM
 * @see OpCode
 * @see Instruction
 * @see VMValue
 */
public class VirtualMachine
{
    /** Se verdadeiro, ativa a exibição em tempo de execução das instruções e da pilha para depuração. */
    private final boolean trace;       // trace flag
    
    /** Armazenamento de bytecodes originais para os exibir, se necessário. */
    private final byte[] bytecodes;    // the bytecodes, storing just for displaying them. Not really needed
    
    /** Array de instruções convertidas que vão ser efetivamente executadas pela VM. */
    private Instruction[] code;        // instructions (converted from the bytecodes)
    
    /** Instruction Pointer (Apontador de Instrução), aponta para a instrução em execução. */
    private int IP;                    // instruction pointer

    /** A pilha de execução (stack) utilizada pelas operações (LIFO - Last In First Out). */
    private final Stack<VMValue> stack = new Stack<>();    // runtime stack
    
    /** Tabela de literais carregada a partir dos bytecodes com as constantes utilizadas no código (Strings, Doubles, etc.). */
    private ArrayList<VMValue> constantPool = new ArrayList<>();



    // Instâncias estáticas pré-criadas para poupar alocações em RAM
    private static final VMValue TRUE_VALUE = new VMValue(true);
    private static final VMValue FALSE_VALUE = new VMValue(false);

    /**
     * Construtor da Máquina Virtual.
     *
     * @param bytecodes O código gerado em formato de bytes obtido por meio de um ficheiro.
     * @param trace Indica se deve ser apresentado um log passo a passo das instruções.
     */
    public VirtualMachine(byte [] bytecodes, boolean trace ) {
        this.trace = trace;
        this.bytecodes = bytecodes;
        decode(bytecodes);
        this.IP = 0;
    }

    /**
     * Transforma a representação binária (array de bytes) recebida e extrai a tabela
     * de constantes e a lista de instruções ({@link Instruction}).
     * Ocorre um término silencioso sem erro quando alcança o fim do ficheiro (EOFException).
     *
     * @param bytecodes Os bytecodes a serem descodificados para execução.
     */
    private void decode(byte [] bytecodes)
    {
        ArrayList<Instruction> inst = new ArrayList<>();
        try {
            // Envolve o array de bytes numa stream de leitura sequencial
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(bytecodes));

            // ==========================================
            // FASE 1: Ler a Constant Pool
            // ==========================================

            // din.readByte(): Lê 1 byte
            // din.readChar(): Lê 2 bytes
            // din.readInt(): Lê 4 bytes
            // din.readDouble(): Lê 8 bytes
            int poolSize = din.readInt(); //4 bytes (tamanho da pool)
            for (int i = 0; i < poolSize; i++)
            {
                byte typeTag = din.readByte(); // Lê o identificador do tipo

                // É um Double (ocupa 8 bytes)
                if (typeTag == 1)
                {
                    double val = din.readDouble();
                    this.constantPool.add(new VMValue(val));
                }

                // É uma String
                else if (typeTag == 3)
                {

                    int strLen = din.readInt(); // Lê o número de caracteres
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < strLen; j++)
                        sb.append(din.readChar()); // Cada char ocupa 2 bytes

                    this.constantPool.add(new VMValue(sb.toString()));

                }
                else
                    runtime_error("Tipo de constante desconhecido no bytecode: " + typeTag);

            }

            // ==========================================
            // FASE 2: Ler as Instruções
            // ==========================================
            // Este loop infinito só para quando o din.readByte() atinge o fim do ficheiro
            // e lança a EOFException (é o comportamento normal do Java aqui).
            while (true)
            {
                byte b = din.readByte();
                OpCode opc = OpCode.convert(b);

                switch (opc.nArgs())
                {
                    case 0:
                        inst.add(new Instruction(opc));
                        break;
                    case 1:
                        int val = din.readInt(); // O argumento é sempre um inteiro (4 bytes)
                        inst.add(new Instruction1Arg(opc, val));
                        break;
                    default:
                        System.out.println("Erro crítico: nArgs inválido no decode().");
                        System.exit(1);
                }
            }
        }
        catch (java.io.EOFException e)
        {
            // Chegámos ao fim do ficheiro com sucesso.
            // Passar de ArrayList para Array fixo por motivos de performance na execução.
            this.code = new Instruction[ inst.size() ];
            inst.toArray(this.code);

            if (trace) {
                System.out.println("Disassembled instructions:");
                dumpInstructionsAndBytecodes();
            }
        }
        catch (java.io.IOException e) {
            System.out.println("Erro de I/O na leitura dos bytecodes: " + e.getMessage());
        }
    }

    /**
     * Imprime no terminal o dump contendo as instruções descodificadas com os seus respetivos valores ordinais em byte.
     * Facilita no rastreio do endereçamento real dos bytes.
     */
    public void dumpInstructionsAndBytecodes() {
        int idx = 0;
        for (int i=0; i< code.length; i++) {
            StringBuilder s = new StringBuilder();
            s.append(String.format("%02X ", bytecodes[idx++]));
            if (code[i].nArgs() == 1)
                for (int k=0; k<4; k++)
                    s.append(String.format("%02X ", bytecodes[idx++]));
            System.out.println( String.format("%5s: %-15s // %s", i, code[i], s) );
        }
    }

    /**
     * Imprime no ecrã todas as instruções da lista decodificada em conjunto com a sua posição indexada no array ({@link #code}).
     */
    public void dumpInstructions() {
        for (int i=0; i< code.length; i++)
            System.out.println( i + ": " + code[i] );
    }

    /**
     * Cancela e termina a execução da Máquina Virtual devido a um erro gravoso de runtime, indicando a mensagem.
     * Se houver o modo "trace" ativo, é exibido o estado atual da Stack.
     *
     * @param msg Mensagem a ser exibida como razão para o erro e paragem brusca da VM.
     */
    private void runtime_error(String msg) {
        System.out.println("runtime error: " + msg);
        if (trace)
            System.out.println( String.format("%22s Stack: %s", "", stack ) );
        System.exit(1);
    }

    /**
     * Inicializa a execução linha a linha (instrução a instrução) de todo o programa armazenado na instância da VirtualMachine.
     */
    public void run() {
        if (trace) {
            System.out.println("Trace while running the code");
            System.out.println("Execution starts at instrution " + IP);
        }
        while (IP < code.length) {
            exec_inst( code[IP] );
            IP++;
        }
        if (trace)
            System.out.println( String.format("%22s Stack: %s", "",stack ) );
    }


//*--------------- instructions ------------------------------------------------------------------------------------------

    /**
     * Puxa do topo da pilha e imprime o valor da stack correspondente como Integer.
     */
    private void exec_iprint()
    {
        int v = stack.pop().getAsInteger();
        System.out.println(v);
    }

    /**
     * Executa um carregamento de um número inteiro na Pilha.
     *
     * @param v O valor inteiro a carregar no topo da pilha.
     */
    private void exec_iconst(Integer v) {
        stack.push(new VMValue(v));
    }

    /** Realiza a operação unária de inversão de sinal (ex: -X) com Inteiros, empurrando o resultado atualizado na pilha. */
    private void exec_iuminus()
    {
        int v = stack.pop().getAsInteger();
        stack.push(new VMValue(-v));
    }
    
    /** Soma entre Inteiros e empurra o resultado para a Pilha. */
    private void exec_iadd()
    {
        int right = stack.pop().getAsInteger();
        int left = stack.pop().getAsInteger();
        stack.push(new VMValue(left + right));
    }
    
    /** Subtração entre Inteiros (Left - Right) e empurra o resultado para a Pilha. */
    private void exec_isub()
    {
        int right = stack.pop().getAsInteger();
        int left = stack.pop().getAsInteger();
        stack.push(new VMValue(left - right));
    }
    
    /** Multiplicação entre Inteiros e empurra o resultado para a Pilha. */
    private void exec_imult()
    {
        int right = stack.pop().getAsInteger();
        int left = stack.pop().getAsInteger();
        stack.push(new VMValue(left * right));
    }
    
    /** Divisão entre Inteiros. Retorna {@link #runtime_error(String)} se ocorrer divisão por zero. */
    private void exec_idiv()
    {
        int right = stack.pop().getAsInteger();
        int left = stack.pop().getAsInteger();

        if (right != 0)
            stack.push(new VMValue(left / right));
        else
            runtime_error("division by 0");
    }

    /** Módulo/Resto de divisão de Inteiros. */
    private void exec_imod()
    {
        int right = stack.pop().getAsInteger();
        int left = stack.pop().getAsInteger();
        stack.push(new VMValue(left % right));
    }

    /** Conversão de Inteiro na Stack (Integer to String) e inserção do novo formato de texto. */
    private void exec_itos()
    {
        String s = stack.pop().getAsInteger().toString();
        stack.push(new VMValue( s ));
    }

    /** Conversão de Inteiro na Stack (Integer to Double/Real) e inserção. */
    private void exec_itod()
    {
        Double v = stack.pop().getAsInteger().doubleValue();
        stack.push(new VMValue( v ));
    }

    /** Verificação Menor ou Igual para Inteiros e empurrar booleano correspondente. */
    private void exec_ileq()
    {
        int right = this.stack.pop().getAsInteger();
        int left = this.stack.pop().getAsInteger();
        stack.push(left <= right ? TRUE_VALUE : FALSE_VALUE);
    }

    /** Verificação Menor que para Inteiros. */
    private void exec_ilt()
    {
        int right = this.stack.pop().getAsInteger();
        int left = this.stack.pop().getAsInteger();
        stack.push(left < right ? TRUE_VALUE : FALSE_VALUE);
    }

    /** Verificação de Diferença Inteira (!=). */
    private void exec_ineq()
    {
        int right = this.stack.pop().getAsInteger();
        int left = this.stack.pop().getAsInteger();
        stack.push(right != left ? TRUE_VALUE : FALSE_VALUE);
    }

    /** Verificação de Igualdade Inteira (==). */
    private void exec_ieq()
    {
        int right = this.stack.pop().getAsInteger();
        int left = this.stack.pop().getAsInteger();
        stack.push(right == left ? TRUE_VALUE : FALSE_VALUE);
    }

    /** Carregamento de constante Real (Double) a partir do Pool de Constantes.
     * @param index O Índice onde a constante existe no listamento da Constant Pool.
     */
    private void exec_dconst(Integer index)
    {
        stack.push(this.constantPool.get(index));
    }

    /** Carregamento de constante String a partir do Pool de Constantes.
     * @param index O Índice onde a String reside no listamento da Constant Pool.
     */
    private void exec_sconst(Integer index)
    {
        stack.push( this.constantPool.get(index));
    }

    /** Puxa valor Double da Pilha e exibe-o no terminal. */
    private void exec_dprint()
    {
        double v = this.stack.pop().getAsReal();
        System.out.println(v);
    }

    /** Inversão de sinal unária (-x) para números Reais. */
    private void exec_duminus()
    {
        double v = stack.pop().getAsReal();
        stack.push(new VMValue(-v));
    }

    /** Adição de valores Reais (Doubles). */
    private void exec_dadd()
    {
        double right = stack.pop().getAsReal();
        double left = stack.pop().getAsReal();
        stack.push(new VMValue(left + right));
    }

    /** Subtração de valores Reais (Doubles). */
    private void exec_dsub()
    {
        double right = stack.pop().getAsReal();
        double left = stack.pop().getAsReal();
        stack.push(new VMValue(left - right));
    }

    /** Multiplicação de valores Reais. */
    private void exec_dmult()
    {
        double right = stack.pop().getAsReal();
        double left = stack.pop().getAsReal();
        stack.push(new VMValue(left* right));
    }

    /** Divisão de valores Reais. Envia runtime error em caso de divisão por 0. */
    private void exec_ddiv()
    {
        double right = stack.pop().getAsReal();
        double left = stack.pop().getAsReal();

        if (right != 0)
            stack.push(new VMValue(left / right));
        else
            runtime_error("division by 0");
    }

    /** Verificação de Igualdade entre Reais (==). */
    private void exec_deq()
    {
        double right = this.stack.pop().getAsReal();
        double left = this.stack.pop().getAsReal();
        stack.push(right == left ? TRUE_VALUE : FALSE_VALUE);
    }

    /** Verificação Menor que para Reais (<). */
    private void exec_dlt()
    {
        double right = this.stack.pop().getAsReal();
        double left = this.stack.pop().getAsReal();
        stack.push(left < right ? TRUE_VALUE : FALSE_VALUE);
    }

    /** Diferença em valor Real (!=). */
    private void exec_dneq()
    {
        double right = this.stack.pop().getAsReal();
        double left = this.stack.pop().getAsReal();
        stack.push(right != left ? TRUE_VALUE : FALSE_VALUE);
    }

    /** Menor ou Igual em valor Real (<=). */
    private void exec_dleq()
    {
        double right = this.stack.pop().getAsReal();
        double left = this.stack.pop().getAsReal();
        stack.push(left <= right ? TRUE_VALUE : FALSE_VALUE);
    }

    /** Converte de Double para String e insere na Stack. */
    private void exec_dtos()
    {
        String s = stack.pop().getAsReal().toString();
        stack.push(new VMValue(s));
    }

    /** Extrai e exibe o valor em formato de String no terminal. */
    private void exec_sprint()
    {
        String s = this.stack.pop().getAsString();
        System.out.println(s);
    }

    /** Concatena duas strings extraídas da Stack e insere a string concatenada no topo. */
    private void exec_sconcat()
    {
        String right = this.stack.pop().getAsString();
        String left = this.stack.pop().getAsString();
        stack.push(new VMValue(left + right));
    }

    /** Verifica se duas Strings são iguais via método .equals(). */
    private void exec_seq()
    {
        String right = this.stack.pop().getAsString();
        String left = this.stack.pop().getAsString();
        stack.push(left.equals(right) ? TRUE_VALUE : FALSE_VALUE);
    }

    /** Verifica diferença entre as Strings. */
    private void exec_sneq()
    {
        String right = this.stack.pop().getAsString();
        String left = this.stack.pop().getAsString();
        stack.push(!left.equals(right) ? TRUE_VALUE : FALSE_VALUE);
    }

    /** Inserção de Booleano TRUE direto na Stack. */
    private void exec_tconst()
    {
        stack.push(TRUE_VALUE);
    }

    /** Inserção de Booleano FALSE na Stack. */
    private void exec_fconst()
    {
        stack.push(FALSE_VALUE);
    }

    /** Equivalência (Igualdade) entre dois booleanos (==). */
    private void exec_beq()
    {
        boolean right = this.stack.pop().getAsBool();
        boolean left = this.stack.pop().getAsBool();
        stack.push(left == right ? TRUE_VALUE : FALSE_VALUE);
    }

    /** Diferença entre valores lógicos (!=). */
    private void exec_bneq()
    {
        boolean right = this.stack.pop().getAsBool();
        boolean left = this.stack.pop().getAsBool();
        stack.push(left != right ? TRUE_VALUE : FALSE_VALUE);
    }

    /** Puxa o topo da stack em formato booleano e Imprime "true" ou "false". */
    private void exec_bprint()
    {
        boolean b = this.stack.pop().getAsBool();

        if(b)
            System.out.println("true");
        else
            System.out.println("false");
    }

    /** Operador lógico AND (&&). */
    private void exec_and()
    {
        boolean right = this.stack.pop().getAsBool();
        boolean left = this.stack.pop().getAsBool();
        stack.push(left && right ? TRUE_VALUE : FALSE_VALUE);
    }

    /** Operação lógica de negação (NOT / !). */
    private void exec_not()
    {
        boolean b = this.stack.pop().getAsBool();
        stack.push(!b ? TRUE_VALUE : FALSE_VALUE);
    }

    /** Operação Lógica OR (||). */
    private void exec_or()
    {
        boolean right = this.stack.pop().getAsBool();
        boolean left = this.stack.pop().getAsBool();
        stack.push(left || right ? TRUE_VALUE : FALSE_VALUE);
    }

    /** Converte Booleano para representação correspondente em String e guarda na Pilha. */
    private void exec_btos()
    {
        String s = this.stack.pop().getAsBool().toString();
        stack.push(new VMValue(s));
    }

    /** Instrui encerramento da execução da Máquina Virtual de forma limpa. */
    private void exec_halt()
    {
        System.exit(0);
    }

    /**
     * Interceta a Instrução ({@link Instruction}) extraída e invoca o seu método em runtime de acordo
     * com o {@link OpCode}. Faz casting de argumentos, se a instrução for do tipo {@link Instruction1Arg}.
     *
     * @param inst A instrução contendo o código de operação e, possivelmente, o argumento para ser processada.
     */
    private void exec_inst( Instruction inst ) {
        if (trace) {
            System.out.println( String.format("%5s: %-15s Stack: %s", IP, inst, stack ) );
        }
        OpCode opc = inst.getOpCode();
        int v;
        switch(opc) {
            case iconst:
                v = ((Instruction1Arg) inst).getArg();
                exec_iconst( v ); break;
            case dconst:
                v = ((Instruction1Arg) inst).getArg();
                exec_dconst( v ); break;
            case sconst:
                v = ((Instruction1Arg) inst).getArg();
                exec_sconst( v ); break;
            case iuminus:
                exec_iuminus(); break;
            case iadd:
                exec_iadd(); break;
            case isub:
                exec_isub(); break;
            case imult:
                exec_imult(); break;
            case idiv:
                exec_idiv(); break;
            case iprint:
                exec_iprint(); break;
            case imod:
                exec_imod(); break;
            case ieq:
                exec_ieq(); break;
            case ineq:
                exec_ineq(); break;
            case ilt:
                exec_ilt(); break;
            case ileq:
                exec_ileq(); break;
            case itod:
                exec_itod(); break;
            case itos:
                exec_itos(); break;
            case dprint:
                exec_dprint(); break;
            case duminus:
                exec_duminus(); break;
            case dadd:
                exec_dadd(); break;
            case dsub:
                exec_dsub(); break;
            case dmult:
                exec_dmult(); break;
            case ddiv:
                exec_ddiv(); break;
            case deq:
                exec_deq(); break;
            case dneq:
                exec_dneq(); break;
            case dlt:
                exec_dlt(); break;
            case dleq:
                exec_dleq(); break;
            case dtos:
                exec_dtos(); break;
            case sprint:
                exec_sprint(); break;
            case sconcat:
                exec_sconcat(); break;
            case seq:
                exec_seq(); break;
            case sneq:
                exec_sneq(); break;
            case tconst:
                exec_tconst(); break;
            case fconst:
                exec_fconst(); break;
            case bprint:
                exec_bprint(); break;
            case beq:
                exec_beq(); break;
            case bneq:
                exec_bneq(); break;
            case and:
                exec_and(); break;
            case or:
                exec_or(); break;
            case not:
                exec_not(); break;
            case btos:
                exec_btos(); break;
            case halt:
                exec_halt(); break;


            default:
                System.out.println("This should never happen! In file vm.java, method exec_inst()");
                System.exit(1);
        }
    }
}