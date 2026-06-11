package FavaCode;

import FavaCode.Compiler.FavaCompiler;
import FavaCode.VirtualMachine.FavaVM;

/**
 * Ponto de entrada principal da aplicação (Main Class) da linguagem Fava.
 *
 * <p>Responsável por gerir os argumentos de linha de comandos (CLI) fornecidos pelo utilizador,
 * arrancar o processo de compilação (gerando os bytecodes) e, em seguida, instanciar a Máquina Virtual
 * para carregar e executar o programa.</p>
 * <p>
 * ### Exemplo de Uso (Linha de Comandos)
 * <pre>{@code
 * # Para compilar e executar mostrando o trace da pilha e erros léxicos:
 * java -jar fava.jar meucodigo.fava -trace -showLexerErrors
 * }</pre>
 *
 * @see FavaCompiler
 * @see FavaVM
 */
public class FavaCompileAndRun {

    /**
     * Instância estática global do compilador responsável pela conversão do código-fonte para bytecode (.bc).
     */
    static public FavaCompiler compiler;

    /**
     * Instância estática da Máquina Virtual Fava que interpreta o binário resultante.
     */
    static public FavaVM vm;

    /**
     * O método principal que inicia o fluxo do compilador Fava. Processa os argumentos de CLI para ativar
     * funcionalidades de depuração (debug), compila o script Fava e inicia a sua execução.
     * <p>
     * Opções de linha de comando suportadas:
     * <ul>
     *   <li>{@code arquivo.fava} - Ficheiro contendo o script na linguagem Fava.</li>
     *   <li>{@code -asm} - Imprime o código assembly/bytecode gerado no terminal.</li>
     *   <li>{@code -trace} - Ativa o modo de depuração passo-a-passo da VM (Mostra Stack e Disassembly).</li>
     *   <li>{@code -showLexerErrors} - Imprime no terminal mensagens de erro se forem detetados caracteres estranhos (erros de Lexing).</li>
     *   <li>{@code -showParserErrors} - Força a exibição de erros sintáticos (ex: faltam blocos de código ou parênteses).</li>
     * </ul>
     *
     * @param args Matriz de argumentos da CLI.
     */
    public static void main(String[] args) {
        String filePath = null;
        boolean showAsm = false;
        boolean trace = false;
        boolean showLexerErrors = false;
        boolean showParserErrors = false;

        // Processamento das flags de linha de comandos
        for (String arg : args) {
            if (arg.endsWith(".fava"))
                filePath = arg;
            else if (arg.equals("-asm"))
                showAsm = true;
            else if (arg.equals("-trace"))
                trace = true;
            else if (arg.equals("-showLexerErrors"))
                showLexerErrors = true;
            else if (arg.equals("-showParserErrors"))
                showParserErrors = true;
        }

        // 1. Inicia o processo de Compilação
        compiler = new FavaCompiler(filePath, showAsm, showLexerErrors, showParserErrors);

        // Se o ficheiro de entrada for nulo, foi lido de System.in e o binário gerado chamar-se-á output.bc
        String outputBcPath = (filePath != null) ? filePath.replace(".fava", ".bc") : "output.bc";
        System.out.println("*** VM output ***");

        // 2. Carrega o Binário gerado e Executa na Máquina Virtual
        vm = new FavaVM(outputBcPath, trace);
    }
}
