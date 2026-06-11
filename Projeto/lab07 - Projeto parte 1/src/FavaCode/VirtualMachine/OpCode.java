package FavaCode.VirtualMachine;

/**
 * Códigos de operação (OpCodes) suportados pela Máquina Virtual Fava.
 * Cada valor do enum representa uma instrução possível que a VM pode executar.
 * 
 * @see VirtualMachine
 * @see FavaCode.VirtualMachine.Instruction.Instruction
 */
public enum OpCode {
    // single-byte instructions, just the OpCode: no arguments
    iconst   (1),
    dconst   (1),
    sconst   (1),
    iprint   (0),
    iuminus  (0),
    iadd     (0),
    isub     (0),
    imult    (0),
    idiv     (0),
    imod     (0),
    ieq      (0),
    ineq     (0),
    ilt      (0),
    ileq     (0),
    itod     (0),
    itos     (0),
    dprint   (0),
    duminus  (0),
    dadd     (0),
    dsub     (0),
    dmult    (0),
    ddiv     (0),
    deq      (0),
    dneq     (0),
    dlt      (0),
    dleq     (0),
    dtos     (0),
    sprint   (0),
    sconcat  (0),
    seq      (0),
    sneq     (0),
    tconst   (0),
    fconst   (0),
    bprint   (0),
    beq      (0),
    bneq     (0),
    and      (0),
    or       (0),
    not      (0),
    btos     (0),
    halt     (0);

    /**
     * Número de argumentos necessários para a instrução.
     */
    private final int nArgs;

    /**
     * Construtor para inicializar o OpCode com a sua quantidade de argumentos requeridos.
     *
     * @param nArgs A quantidade de argumentos que a instrução necessita (0 ou 1).
     */
    OpCode(int nArgs) {
        this.nArgs = nArgs;
    }

    /**
     * Devolve o número de argumentos exigidos por este OpCode.
     *
     * @return A quantidade de argumentos do OpCode.
     */
    public int nArgs() { return nArgs; }

    /**
     * Converte um valor do tipo byte num enum {@link OpCode} válido, baseado na
     * sua posição ordinal (ordem de declaração).
     *
     * @param value O valor numérico que representa o OpCode (byte).
     * @return O objeto {@link OpCode} correspondente.
     * @throws ArrayIndexOutOfBoundsException Se o valor em byte for maior ou igual ao tamanho de opções do enum, ou menor que zero.
     */
    public static OpCode convert(byte value) {
        return OpCode.values()[value];
    }
}