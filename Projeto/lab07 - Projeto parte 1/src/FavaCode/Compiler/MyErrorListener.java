package FavaCode.Compiler;
import org.antlr.v4.runtime.*;

/**
 * Listener de erros customizado que capta e reporta os erros reportados pelo ANTLR4
 * (seja durante a etapa de Lexing ou Parsing).
 * Herda de {@link BaseErrorListener} e permite configurar se os erros são silenciosamente
 * engolidos ou mostrados no terminal para efeito de debug e correção.
 */
public class MyErrorListener extends BaseErrorListener {
    /** Determina se a mensagem detalhada de erro na fase de Lexing (reconhecimento dos tokens) será mostrada ou ocultada. */
    private boolean showLexerErrors;
    
    /** Regista se a mensagem de erro da fase do Parser (análise sintática) é impressa no terminal. */
    private boolean showParserErrors;
    
    /** Um contador do número de erros detetados pela leitura dos tokens durante o Lexing. */
    private int numLexerErrors = 0;
    
    /** O somatório de erros ou violações à gramática decorridos no parse (construção da AST). */
    private int numParsingErrors = 0;

    /**
     * Instancia o Listener personalizado, passando de antemão de que fase se pretende ver os relatórios de falha (caso ocorram).
     *
     * @param showLexerErrors Mostrar erros derivados do analisador léxico (falhas em tokens).
     * @param showParserErrors Mostrar erros de quebras com a sintaxe ou regras gramaticais.
     */
    public MyErrorListener(boolean showLexerErrors, boolean showParserErrors){
        super();
        this.showLexerErrors = showLexerErrors;
        this.showParserErrors = showParserErrors;
    }

    /**
     * Invocado pelo ANTLR4 imediatamente depois que ele se depara e lança um erro quer de semântica, léxico ou parser.
     * Avalia o recognizer e discrimina-o se este é da classe Lexer ou de um Parser para exibir a correta informação ao programador.
     *
     * @param recognizer      A instância a partir da qual o erro deriva (geralmente {@link Lexer} ou {@link Parser}).
     * @param offendingSymbol O Token que quebrou alguma regra ou produziu o estado inconsistente na análise.
     * @param line            Número da linha indexado no programa no local falho.
     * @param charPositionInLine O número da coluna no offset local.
     * @param msg             Mensagem descritiva providenciada sobre o estado que despoletou.
     * @param e               A exceção original detetada, que foi encapsulada.
     */
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line, int charPositionInLine,
                            String msg,
                            RecognitionException e)
    {
        //System.out.println(msg);
        if (recognizer instanceof Lexer)
        {
            this.numLexerErrors++;
            if (this.showLexerErrors)
                System.out.printf("line %d:%d error: %s\n", line, charPositionInLine, msg);
        }
        if (recognizer instanceof Parser) {
            this.numParsingErrors++;
            if (this.showParserErrors)
                System.out.printf("line %d:%d error: %s\n", line, charPositionInLine, msg);
        }
    }

    /**
     * O total de falhas na análise sintática detetadas desde a iniciação pelo programa.
     *
     * @return O somatório local do {@code numLexerErrors}.
     */
    public int getNumLexerErrors() {
        return this.numLexerErrors;
    }

    /**
     * Quantifica a soma de erros gerados pelas validações em {@link Parser}.
     * Usado muitas vezes para abortar a sequência do compilador, impedindo código mal gerado.
     *
     * @return O total cumulativo detetado pelo parser.
     */
    public int getNumParsingErrors() {
        return this.numParsingErrors;
    }
}