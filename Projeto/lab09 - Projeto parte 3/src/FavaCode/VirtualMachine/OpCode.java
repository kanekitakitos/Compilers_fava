package FavaCode.VirtualMachine;

/**
 * Códigos de Operação (OpCodes) suportados nativamente pela Máquina Virtual Fava.
 *
 * <p>Esta enumeração mapeia de forma ordinal (pela posição de declaração de 0 a N) as mnemónicas de Assembly
 * geradas na fase de compilação (CodeGen) para as instruções reais que a VM deve interpretar e executar (Bytecode).
 * Contém operações matemáticas, lógicas, saltos condicionais (jumps) e manipulação de memória/stack.</p>
 * <p>
 * ### Exemplo de Uso
 * <pre>{@code
 * // Quando o CodeGen converte "x + y"
 * OpCode operacaoSoma = OpCode.iadd; // Soma inteira (ordinal pode ser, ex: 5)
 *
 * // Extrair o enum pelo byte lido do disco (05) na FavaVM:
 * OpCode op = OpCode.convert((byte) 5); // Retorna OpCode.iadd
 * }</pre>
 *
 * @see FavaCode.VirtualMachine.Instruction.Instruction
 * @see VirtualMachine
 * @see FavaCode.CodeGenerator.OpCodeMapper
 */
public enum OpCode {
    // Instruções Single-Byte (Sem argumentos)
    iconst(1),
    dconst(1),
    sconst(1),
    iprint(0),
    iuminus(0),
    iadd(0),
    isub(0),
    imult(0),
    idiv(0),
    imod(0),
    ieq(0),
    ineq(0),
    ilt(0),
    ileq(0),
    itod(0),
    itos(0),
    dprint(0),
    duminus(0),
    dadd(0),
    dsub(0),
    dmult(0),
    ddiv(0),
    deq(0),
    dneq(0),
    dlt(0),
    dleq(0),
    dtos(0),
    sprint(0),
    sconcat(0),
    seq(0),
    sneq(0),
    tconst(0),
    fconst(0),
    bprint(0),
    beq(0),
    bneq(0),
    and(0),
    or(0),
    not(0),
    btos(0),

    // Instruções Multibyte (Requerem 1 Argumento Integer, ex: Index ou Endereço)
    jump(1), // addr
    jumpf(1), // addr
    galloc(1), // n
    gload(1), // addr
    gstore(1), // addr


    lalloc(1), // Novas instruções apra o 3 trabalho
    lload(1),
    lstore(1),
    pop(1),
    call(1),
    retval(1),
    ret(1),


    // Fim Limpo
    halt(0);

    /**
     * Define o número de argumentos estáticos (variáveis/inteiros) que acompanham este OpCode específico no disco.
     */
    private final int nArgs;

    /**
     * Construtor padrão da Enumeração que vincula cada OpCode ao seu número de argumentos requeridos.
     *
     * @param nArgs A quantidade fixa de bytes extras exigidos (0 para mnemónicas puras, 1 para apontadores de 4 bytes).
     */
    OpCode(int nArgs) {
        this.nArgs = nArgs;
    }

    /**
     * Devolve a quantidade exata de argumentos subsequentes que o interpretador da VM (decoder) precisa ler
     * logo a seguir ao OpCode na pipeline de decodificação binária.
     *
     * @return O número de argumentos (0 ou 1).
     */
    public int nArgs() {
        return nArgs;
    }

    /**
     * Motor primário de conversão em runtime para descodificação de um byte nativo extraído do disco (`.bc`)
     * para o seu respetivo objeto de instância Enumeração {@link OpCode}.
     * Baseia-se na posição ordinal natural (indexação 0-base da declaração no Java).
     *
     * @param value O bit ordinal, originário do ficheiro pré-compilado, contido num byte singular.
     * @return A instância Enumera {@link OpCode} identificada pelo ordinal injetado (Ex: 0 -> `iconst`).
     * @throws ArrayIndexOutOfBoundsException Impede lixo lógico ou ficheiros de Bytecode defeituosos / não Fava.
     */
    public static OpCode convert(byte value) {
        return OpCode.values()[value];
    }
}
