package FavaCode.Compiler;

import FavaCode.Semantic.SemanticAnalyzerVisitor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.FileInputStream;
import java.io.InputStream;

import FavaCode.Parser.Fava.*;
import FavaCode.CodeGenerator.*;

/**
 * A classe base central que encapsula todo o "pipeline" (fluxo de execução) do compilador para a linguagem Fava.
 * Executa, de forma sequencial:
 * <ol>
 *   <li>Análise Léxica e Sintática (Lexer e Parser gerados pelo ANTLR4)</li>
 *   <li>Análise Semântica (validação de tipos e variáveis via {@link SemanticAnalyzerVisitor})</li>
 *   <li>Geração de Código (tradução da AST para Bytecodes da Virtual Machine via {@link CodeGen})</li>
 * </ol>
 * <p>
 * ### Exemplo de Uso
 * <pre>{@code
 * // Instanciar o compilador com opções de debug ativadas
 * FavaCompiler compiler = new FavaCompiler("meuScript.fava", true, true, true);
 * // O compilador irá gerar um ficheiro "meuScript.bc" no disco
 * }</pre>
 *
 * @see SemanticAnalyzerVisitor
 * @see CodeGen
 * @see MyErrorListener
 */
public class FavaCompiler {
    /**
     * Flag interna global que determina se o compilador deve imprimir no terminal as instruções geradas
     * no formato Assembly (mnemónicas como `iadd`, `iprint`).
     */
    static boolean showAsm;

    /**
     * Flag que indica ao {@link MyErrorListener} se deve reportar ao utilizador os erros léxicos
     * (ex: carateres inválidos não reconhecidos pela gramática).
     */
    boolean showLexerErrors;

    /**
     * Flag que indica ao {@link MyErrorListener} se deve reportar erros de parser
     * (ex: falhas de sintaxe, blocos mal formados, ponto e vírgula em falta).
     */
    boolean showParserErrors;

    /**
     * Inicia o processo completo de compilação.
     * Recebe um caminho para o ficheiro de origem, efetua todas as fases do compilador e,
     * em caso de sucesso sem erros, produz e guarda um ficheiro binário executável (`.bc`).
     *
     * @param inputFilename    Caminho absoluto ou relativo para o ficheiro de código-fonte Fava (ex: "script.fava").
     *                         Se for {@code null}, o compilador entra em modo interativo e lê da standard input (System.in).
     * @param asmFlag          Se {@code true}, imprime no terminal o dump das instruções geradas no formato Assembly.
     * @param showLexerErrors  Liga a verbosidade de erros do tipo Lexical no ANTLR4.
     * @param showParserErrors Liga a verbosidade de erros detetados pela árvore de Parser no ANTLR4.
     */
    public FavaCompiler(String inputFilename, boolean asmFlag, boolean showLexerErrors, boolean showParserErrors) {
        showAsm = asmFlag;
        this.showLexerErrors = showLexerErrors;
        this.showParserErrors = showParserErrors;

        // O nome do ficheiro de saída substitui a extensão .fava por .bc
        String outputFilename = (inputFilename != null) ? inputFilename.replace(".fava", ".bc") : "output.bc";

        try {
            CharStream input;
            if (inputFilename != null) {
                // Modo Normal: Lê do ficheiro físico (.fava)
                InputStream is = new FileInputStream(inputFilename);
                input = CharStreams.fromStream(is);
            } else {
                // Modo Mooshak: Lê diretamente do terminal (Standard Input)
                input = CharStreams.fromStream(System.in);
            }

            // 1. Fase de Análise Léxica (Lexing)
            FavaLexer lexer = new FavaLexer(input);

            // Registo do Error Listener personalizado para controlar a verbosidade de erros do Lexer
            MyErrorListener errorListener = new MyErrorListener(showLexerErrors, showParserErrors);
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);


            // 2. Fase de Análise Sintática (Parsing)
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            FavaParser parser = new FavaParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            // Inicia a criação da Abstract Syntax Tree (AST) a partir da regra de topo 'prog'
            ParseTree tree = parser.prog();

            // Aborta a compilação se existirem erros sintáticos ou léxicos
            if (errorListener.getNumLexerErrors() > 0) {
                System.out.println("Input has lexical errors");
                System.exit(0);
            } else if (errorListener.getNumParsingErrors() > 0) {
                System.out.println("Input has parsing errors");
                System.exit(0);
            } else {
                // 3. Fase de Análise Semântica (Semantic Analysis)
                SemanticAnalyzerVisitor semanticAnalyzer = new SemanticAnalyzerVisitor();
                semanticAnalyzer.visit(tree);

                // Aborta se existirem tipos incompatíveis, variáveis não declaradas, etc.
                if (semanticAnalyzer.getSemanticErrors() > 0)
                    System.exit(0);

                else {
                    // 4. Fase de Geração de Código (Code Generation)
                    // Passamos a árvore de tipos (ParseTreeProperty) para o CodeGen
                    CodeGen codeGen = new CodeGen(semanticAnalyzer.getTypesTree());
                    codeGen.visit(tree);

                    if (showAsm)
                        codeGen.dumpCode();

                    // Imprime a estrutura da pool de constantes e as instruções geradas
                    codeGen.dumpCodePool();
                    codeGen.dumpCodeInstructions();

                    // Guarda os bytecodes finais no ficheiro executável (.bc)
                    codeGen.saveBytecodes(outputFilename);
                }
            }
        } catch (java.io.IOException e) {
            System.out.println("Erro de entrada/saída durante a compilação: " + e.getMessage());
        }
    }

}
