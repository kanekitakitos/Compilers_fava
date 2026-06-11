package FavaCode.Compiler;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * Listener de erros personalizado para o Lexer e Parser do ANTLR4, utilizado no compilador Fava.
 * Esta classe permite controlar a forma como os erros de sintaxe e léxicos são reportados,
 * oferecendo a opção de suprimir ou exibir mensagens de erro detalhadas com base nas flags de compilação.
 * <p>
 * ### Exemplo de Uso
 * <pre>{@code
 * // No FavaCompiler, para controlar a verbosidade dos erros:
 * MyErrorListener errorListener = new MyErrorListener(true, false); // Mostra erros do lexer, mas não do parser
 *
 * FavaLexer lexer = new FavaLexer(input);
 * lexer.removeErrorListeners(); // Remove o listener padrão
 * lexer.addErrorListener(errorListener); // Adiciona o nosso listener personalizado
 *
 * FavaParser parser = new FavaParser(tokens);
 * parser.removeErrorListeners();
 * parser.addErrorListener(errorListener);
 * }</pre>
 *
 * @see BaseErrorListener
 * @see FavaCompiler
 */
public class MyErrorListener extends BaseErrorListener {
    private int numLexerErrors = 0;
    private int numParsingErrors = 0;
    private final boolean showLexerErrors;
    private final boolean showParserErrors;

    /**
     * Construtor do listener de erros.
     *
     * @param showLexerErrors  Se {@code true}, imprime no terminal os erros detetados na fase de análise léxica.
     * @param showParserErrors Se {@code true}, imprime no terminal os erros detetados na fase de análise sintática.
     */
    public MyErrorListener(boolean showLexerErrors, boolean showParserErrors) {
        this.showLexerErrors = showLexerErrors;
        this.showParserErrors = showParserErrors;
    }

    /**
     * Método invocado pelo ANTLR4 sempre que um erro de sintaxe ou léxico é detetado.
     * A lógica interna distingue entre erros do lexer e do parser para contagem e exibição seletiva.
     *
     * @param recognizer         O componente do ANTLR4 que detetou o erro (Lexer ou Parser).
     * @param offendingSymbol    O token ou símbolo que causou o erro.
     * @param line               O número da linha onde o erro ocorreu.
     * @param charPositionInLine A posição do caractere na linha.
     * @param msg                A mensagem de erro descritiva gerada pelo ANTLR4.
     * @param e                  A exceção de reconhecimento que encapsula os detalhes do erro.
     */
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        // O nome do recognizer ajuda a distinguir entre Lexer e Parser
        String recognizerName = recognizer.getClass().getSimpleName();

        if (recognizerName.contains("Lexer")) {
            numLexerErrors++;
            if (showLexerErrors) {
                System.err.println("Lexer error at line " + line + ":" + charPositionInLine + " - " + msg);
            }
        } else {
            numParsingErrors++;
            if (showParserErrors) {
                System.err.println("Parser error at line " + line + ":" + charPositionInLine + " - " + msg);
            }
        }
    }

    /**
     * Retorna o número total de erros léxicos encontrados.
     *
     * @return A contagem de erros do lexer.
     */
    public int getNumLexerErrors() {
        return numLexerErrors;
    }

    /**
     * Retorna o número total de erros de parsing (sintaxe) encontrados.
     *
     * @return A contagem de erros do parser.
     */
    public int getNumParsingErrors() {
        return numParsingErrors;
    }
}
