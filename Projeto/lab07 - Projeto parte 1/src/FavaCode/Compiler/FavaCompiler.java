package FavaCode.Compiler;

import FavaCode.Semantic.SemanticAnalyzerVisitor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.FileInputStream;
import java.io.InputStream;
import FavaCode.Parser.Fava.*;
import FavaCode.CodeGenerator.*;

/**
 * A classe base central responsável por encapsular as fases do compilador para a linguagem Fava.
 * Executa, na ordem: Lexing e Parsing (com o ANTLR4), Análise Semântica (validação de tipos),
 * e, por último, a Geração de Código Bytecode.
 */
public class FavaCompiler
{
    /** Flag interna global que decide se exibe as instruções resultantes em modo Assembly / Bytecodes. */
    static boolean showAsm;    // flag for showing generated assembly
    
    /** Verifica no {@link MyErrorListener} se devem ser imprimidas as falhas lexicais detetadas no terminal. */
    boolean showLexerErrors;
    
    /** Verifica no {@link MyErrorListener} se exibe as falhas detetadas pelo parser caso existam quebras da gramática. */
    boolean showParserErrors;

    /**
     * O construtor serve como o inicializador de todo o pipeline de compilação.
     * Recebe um caminho de um ficheiro de origem e trata das execuções em cadeia do compilador, 
     * produzindo e exportando para o disco o binário em extensão .bc.
     *
     * @param inputFilename Caminho integral ou relativo para o ficheiro que possua o script (ex: "script.fava").
     * @param asmFlag Caso ativada (`true`), aciona o CodeGen para exibir a instrução disassemblada e os bytecode.
     * @param showLexerErrors Liga a verbosidade de erros do tipo Lexical.
     * @param showParserErrors Liga a verbosidade de erros detetados pela árvore de Parser.
     */
    public FavaCompiler(String inputFilename, boolean asmFlag,boolean showLexerErrors, boolean showParserErrors)
    {
        showAsm = asmFlag;
        this.showLexerErrors = showLexerErrors;
        this.showParserErrors = showParserErrors;

        String outputFilename = (inputFilename != null) ? inputFilename.replace(".fava", ".bc") : "output.bc";
        try {

                CharStream input;
                if (inputFilename != null)
                {
                    // Modo Normal: Lê do ficheiro físico (.fava)
                    InputStream is = new FileInputStream(inputFilename);
                    input = CharStreams.fromStream(is);
                }
                else
                {
                    // Modo Mooshak: Lê diretamente do terminal (Standard Input)
                    input = CharStreams.fromStream(System.in);
                }

            FavaLexer lexer = new FavaLexer(input);


            // Novo Listener na fase do lexer Erros
            MyErrorListener errorListener = new MyErrorListener(showLexerErrors, showParserErrors);
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);


            // Novo Listener na fase do parser Erros
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            FavaParser parser = new FavaParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            // Expresão que começa a análise sintática
            ParseTree tree = parser.prog();
            if (errorListener.getNumLexerErrors() > 0)
            {
                System.out.println("Input has lexical errors");
                System.exit(0);
            }

            else if (errorListener.getNumParsingErrors() > 0)
            {
                System.out.println("Input has parsing errors");
                System.exit(0);
            }
            else
            {
                // Semantic Analysis
                SemanticAnalyzerVisitor semanticAnalyzer = new SemanticAnalyzerVisitor();
                semanticAnalyzer.visit(tree);


                if (semanticAnalyzer.getSemanticErrors() > 0)
                    System.exit(0);

                else
                {
                    // Code Generation --> ByteCode
                    CodeGen codeGen = new CodeGen(semanticAnalyzer.getTypesTree());
                    codeGen.visit(tree);
                    if (showAsm)
                        codeGen.dumpCode();

                    codeGen.dumpCodePool();
                    codeGen.dumpCodeInstructions();

                    codeGen.saveBytecodes(outputFilename);

                }
            }
        }
        catch (java.io.IOException e)
        {
            System.out.println(e);
        }
    }

}