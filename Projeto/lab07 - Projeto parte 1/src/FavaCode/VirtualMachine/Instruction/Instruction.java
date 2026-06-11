package FavaCode.VirtualMachine.Instruction;

import FavaCode.VirtualMachine.OpCode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Representa uma instrução da Máquina Virtual Fava sem argumentos adicionais.
 * Serve como classe base para outras instruções que possam necessitar de argumentos.
 *
 * @see OpCode
 * @see Instruction1Arg
 */
public class Instruction {
    /**
     * O código de operação desta instrução.
     */
    OpCode opc;

    /**
     * Construtor para uma instrução sem argumentos.
     *
     * @param opc O código de operação ({@link OpCode}) associado a esta instrução.
     */
    public Instruction(OpCode opc) {
        this.opc = opc;
    }

    /**
     * Obtém o código de operação desta instrução.
     *
     * @return O {@link OpCode} correspondente à instrução.
     */
    public OpCode getOpCode() {
        return opc;
    }

    /**
     * Retorna o número de argumentos que esta instrução possui.
     * Para esta classe base, o número de argumentos é sempre 0.
     *
     * @return Inteiro representando a quantidade de argumentos (0).
     */
    public int nArgs() { return 0; }

    /**
     * Retorna a representação em formato de string da instrução.
     *
     * @return O nome do {@link OpCode}.
     */
    @Override
    public String toString() {
        return opc.toString();
    }

    /**
     * Escreve o código de operação no output stream especificado, em formato de byte.
     * Pode ser utilizado para gerar código compilado num ficheiro binário.
     *
     * @param out A stream de saída ({@link DataOutputStream}) para onde o byte será escrito.
     * @throws IOException Se ocorrer um erro de I/O durante a escrita.
     */
    public void writeTo(DataOutputStream out) throws IOException {
        out.writeByte(opc.ordinal());
    }
}
