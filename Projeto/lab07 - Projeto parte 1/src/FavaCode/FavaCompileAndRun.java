package FavaCode;

import FavaCode.Compiler.FavaCompiler;
import FavaCode.VirtualMachine.FavaVM;

/**
 * Ponto de entrada principal da aplicação.
 * Responsável por gerir os argumentos de linha de comandos (CLI) fornecidos pelo utilizador e 
 * iniciar as fases da compilação e subsequente execução da Máquina Virtual Fava.
 */
public class FavaCompileAndRun
{

    /** Instância ativa do Compilador para geração da árvore e bytecode a partir do código fonte Fava. */
    static public FavaCompiler compiler;
    
    /** Máquina Virtual designada para ler e rodar o bytecode processado. */
    static public FavaVM vm;

    /**
     * O método main que arranca a execução. Processa as flags da CLI antes de instanciar as diferentes partes da framework Fava.
     * <p>
     * Opções de linha de comando suportadas:
     * <ul>
     *   <li>{@code arquivo.fava} - Ficheiro com a extensão {@code .fava} indicando a fonte para o programa.</li>
     *   <li>{@code -asm} - Mostra instruções no estilo Assembly extraídas.</li>
     *   <li>{@code -trace} - Inicia a FavaVM com dump da Constant Pool e tracing ativado, demonstrando detalhadamente as mudanças na Stack.</li>
     *   <li>{@code -lexerErrors} - Sinaliza o Listener para demonstrar todas as quebras lexicais (tokens perdidos) provenientes do motor ANTLR4.</li>
     *   <li>{@code -parserErrors} - Força log dos erros por infrações detetadas pelo parser na estrutura gramatical.</li>
     * </ul>
     *
     * @param args Matriz das Strings contendo todos os argumentos fornecidos na execução.
     */
    public static void main(String[] args)
    {

        String filePath = null;
        boolean showAsm = false;
        boolean trace = false;
        boolean showLexerErrors = false;
        boolean showParserErrors = false;

        for (String arg : args)
        {
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


        compiler = new FavaCompiler(filePath, showAsm, showLexerErrors, showParserErrors);

            String outputBcPath = (filePath != null) ? filePath.replace(".fava", ".bc") : "output.bc";
            System.out.println("*** VM output ***");
            vm = new FavaVM(outputBcPath, trace);



    }
}