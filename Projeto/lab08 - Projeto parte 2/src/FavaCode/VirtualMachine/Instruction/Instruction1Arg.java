package FavaCode.VirtualMachine.Instruction;

import FavaCode.VirtualMachine.OpCode;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Representa uma instrução da Máquina Virtual Fava que requer exatamente um argumento complementar inteiro.
 * Esta subclasse de {@link Instruction} acomoda operações que precisam de aceder à memória, saltar no código
 * ou referenciar a Pool de Constantes (ex: {@code iconst 5}, {@code sconst 2}, {@code gload 0}, {@code jump 14}).
 * <p>
 * ### Exemplo de Uso
 * <pre>{@code
 * // Emite a instrução de carregamento (gload) da variável com o endereço de RAM 0
 * Instruction1Arg loadVarX = new Instruction1Arg(OpCode.gload, 0);
 *
 * // Quando exportada, gera o bytecode `2b 00 00 00 00` (Assumindo gload=43, argumento=0 em 4 bytes).
 * loadVarX.writeTo(dataOutputStream);
 * }</pre>
 *
 * @see OpCode
 * @see Instruction
 * @see FavaCode.CodeGenerator.CodeGen
 */
public class Instruction1Arg extends Instruction {

    /**
     * O argumento que acompanha o OpCode, armazenado em memória de 32 bits ({@code int}).
     */
    int arg;

    /**
     * Instancia e inicializa uma instrução operativa associando-lhe o parâmetro específico de invocação da rotina.
     *
     * @param opc O motor operacional desta instrução ({@link OpCode}).
     * @param arg O parâmetro associado de índice (Inteiro).
     */
    public Instruction1Arg(OpCode opc, int arg) {
        super(opc);
        setArg(arg);
    }

    /**
     * Lê ou inspeciona o fator complementar desta instrução, para cálculos posteriores ou depuração.
     *
     * @return O argumento alocado no estado inteiro.
     */
    public int getArg() {
        return arg;
    }

    /**
     * Reescreve a variável em runtime, possibilitando correções da Máquina após instanciamento da Tree.
     * Comumente invocada pelo gerador de código nas lógicas de Jumps temporários (Backpatching).
     *
     * @param arg O valor Inteiro de Backpatch que dita o salto a re-endereçar.
     */
    public void setArg(int arg) {
        this.arg = arg;
    }

    /**
     * Declara explicitamente a obrigatoriedade de acoplar bytes adicionais (além do Mnemónico de base), aquando o decode ou encode.
     *
     * @return Retorna o número estático `1`, referente a um operando exclusivo que completa a diretiva original.
     */
    @Override
    public int nArgs() {
        return 1;
    }

    /**
     * Converte os atributos da estrutura na forma padronizada e inteligível humana para prints disassembladores
     * e o modo verbose "trace" da CLI.
     *
     * @return String formatada contendo "OpCodeName ArgValue" (ex: `gstore 2`).
     */
    @Override
    public String toString() {
        return opc.toString() + " " + arg;
    }

    /**
     * Exportador sequencial do código fonte compilado, focado na tradução e encadeamento em Bytes IO para o disco (Ficheiro `.bc`).
     * Primeiro serializa a diretiva global proveniente da classe-mãe (1 Byte), concatenando então o arg (Integer com alocação bruta de 4 Bytes).
     *
     * @param out Stream encapsuladora dos dados byte binários resultantes da emissão do CodeGen.
     * @throws IOException Aborta, protegendo os dados se houver limitação fatal da API I/O durante a tradução binária.
     */
    @Override
    public void writeTo(DataOutputStream out) throws IOException {
        out.writeByte(super.opc.ordinal());
        out.writeInt(arg);
    }

}
