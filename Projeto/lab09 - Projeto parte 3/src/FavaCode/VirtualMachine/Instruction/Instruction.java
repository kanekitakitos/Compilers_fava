package FavaCode.VirtualMachine.Instruction;

import FavaCode.VirtualMachine.OpCode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Representa uma instrução base (Mnemónica pura) gerada para a Máquina Virtual Fava sem argumentos adicionais
 * (ex: {@code iadd}, {@code halt}, {@code not}).
 *
 * <p>Serve como classe pai para outras instruções estendidas que requeiram argumentos (como {@link Instruction1Arg}).
 * A instrução base consiste puramente no seu respetivo {@link OpCode}.</p>
 * <p>
 * ### Exemplo de Uso
 * <pre>{@code
 * // Adicionar dois números extraídos do topo da stack
 * Instruction soma = new Instruction(OpCode.iadd);
 *
 * // Quando exportada, gera o bytecode `05` (assumindo que `iadd` seja a quinta constante no enum).
 * soma.writeTo(dataOutputStream);
 * }</pre>
 *
 * @see OpCode
 * @see Instruction1Arg
 * @see FavaCode.CodeGenerator.CodeGen
 */
public class Instruction {

    /**
     * O código operacional da máquina virtual instanciado nesta classe.
     */
    OpCode opc;

    /**
     * Instancia uma nova instrução elementar sem argumentos operacionais extra.
     *
     * @param opc A referência explícita correspondente do Enum ({@link OpCode}).
     */
    public Instruction(OpCode opc) {
        this.opc = opc;
    }

    /**
     * Devolve o motor operacional desta chamada.
     *
     * @return O Enum referenciado por esta instrução.
     */
    public OpCode getOpCode() {
        return opc;
    }

    /**
     * Confirma se esta instrução requisitou bytes posteriores durante o decode da Máquina Virtual Fava.
     * Nesta classe elementar, o padrão estipula ausência de argumentos a transitar na Bytecode.
     *
     * @return A constante {@code 0} atestando que apenas o OpCode requer leitura.
     */
    public int nArgs() {
        return 0;
    }

    /**
     * Devolve o nome em formato String do elemento de enumeração.
     * Essencial para exibição legível de logs na consola aquando depurações e o modo "-asm" (Disassembler).
     *
     * @return Texto em string representando a invocação nativa do Fava.
     */
    @Override
    public String toString() {
        return opc.toString();
    }

    /**
     * Traduz fisicamente esta classe em escrita Bytecode no disco (geração do binário `.bc`).
     * Assina sequencialmente o byte que representa as indicações puras de index do OpCode,
     * baseando-se no `ordinal()` da sua enumeração do Java.
     *
     * @param out Recetáculo nativo de dados IO em que a função serializa.
     * @throws IOException Abortagem controlada em casos de falhas I/O do SO ou espaço no disco do processo da Geração Fava.
     */
    public void writeTo(DataOutputStream out) throws IOException {
        out.writeByte(opc.ordinal());
    }
}
