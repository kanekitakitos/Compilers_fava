package FavaCode.VirtualMachine.Instruction;

import FavaCode.VirtualMachine.OpCode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Representa uma instrução da Máquina Virtual Fava que possui exatamente um argumento inteiro.
 * Esta classe estende a classe base {@link Instruction}.
 *
 * @see OpCode
 * @see Instruction
 */
public class Instruction1Arg extends Instruction {
    /**
     * O argumento associado à instrução.
     */
    int arg;

    /**
     * Construtor para uma instrução com um argumento inteiro.
     *
     * @param opc O código de operação ({@link OpCode}).
     * @param arg O valor inteiro do argumento.
     */
    public Instruction1Arg(OpCode opc, int arg) {
        super(opc);
        setArg(arg);
    }

    /**
     * Obtém o argumento inteiro desta instrução.
     *
     * @return O valor do argumento.
     */
    public int getArg() {
        return arg;
    }

    /**
     * Define ou atualiza o valor do argumento desta instrução.
     *
     * @param arg O novo valor do argumento.
     */
    public void setArg(int arg) {
        this.arg = arg;
    }

    /**
     * Retorna o número de argumentos que esta instrução possui.
     *
     * @return Sempre retorna 1, indicando que possui um argumento.
     */
    @Override public int nArgs() { return 1; }

    /**
     * Retorna a representação em formato de string da instrução.
     *
     * @return Uma string contendo o nome do {@link OpCode} seguido do seu argumento.
     */
    @Override public String toString() {
        return opc.toString() + " " + arg;
    }

    /**
     * Escreve a instrução na stream de saída. Primeiro escreve o código
     * de operação (1 byte) e, em seguida, o argumento (4 bytes - inteiro).
     *
     * @param out A stream de saída ({@link DataOutputStream}) para onde os dados serão escritos.
     * @throws IOException Se ocorrer um erro de I/O durante a escrita na stream.
     */
    @Override public void writeTo(DataOutputStream out) throws IOException {
        out.writeByte(super.opc.ordinal());
        out.writeInt(arg);
    }

}
