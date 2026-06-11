package FavaCode.Semantic;

import FavaCode.Parser.Fava.FavaBaseVisitor;
import FavaCode.Parser.Fava.FavaParser;
import FavaCode.Semantic.Types.FunctionType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Visitante da 1ª passagem do Analisador Semântico da linguagem Fava (Definition Phase).
 *
 * <p>Responsável por recolher e registar no scope global tudo o que precisa existir antes da verificação de usos:
 * atualmente, assinaturas de funções e nomes globais. Esta fase existe para suportar chamadas a funções antes da sua
 * definição textual (forward calls) e para prevenir cascatas de erros na 2ª passagem.</p>
 *
 * <p>Regras principais:</p>
 * <ul>
 *   <li>Regista assinaturas de funções ({@link FunctionType}) na {@link SymbolTable}.</li>
 *   <li>Deteta declarações globais duplicadas (função/variável com o mesmo nome no global).</li>
 *   <li>Marca funções inválidas para que a Reference Phase ignore o corpo e evite erros derivados.</li>
 * </ul>
 *
 * <p>Referências Académicas:</p>
 * <ul>
 *   <li><b>Nystrom (Crafting Interpreters), Cap. 11 “Resolving and Binding”:</b>
 *       descreve a necessidade de uma fase de resolução para ligar identificadores antes da geração de código.</li>
 * </ul>
 *
 * <p>### Exemplo de Uso</p>
 * <pre>{@code
 * SymbolTable symbolTable = new SymbolTable();
 * DefinitionSemanticVisitor def = new DefinitionSemanticVisitor(symbolTable, (line, msg) -> errors.add(msg));
 * def.visit(tree);
 *
 * // 2ª passagem (Reference Phase) recebe def.getInvalidFunctionDeclarations()
 * }</pre>
 *
 * @see ReferenceSemanticVisitor
 * @see TypeRules
 * @see SymbolTable
 */
public class DefinitionSemanticVisitor extends FavaBaseVisitor<Void>
{
    /**
     * Tabela de símbolos global partilhada entre as passagens semânticas.
     */
    private final SymbolTable symbolTable;

    /**
     * Contador interno de erros semânticos detetados nesta fase.
     */
    private int semanticErrors = 0;

    /**
     * Sink opcional de erros (injeção de dependência), usado para recolher erros fora do visitor.
     */
    private final BiConsumer<Integer, String> errorSink;

    /**
     * Conjunto de funções cuja declaração/assinatura ficou inválida (ex.: duplicada).
     * A Reference Phase pode usar este conjunto para ignorar o corpo e evitar cascatas de erros.
     */
    private final Set<FavaParser.FunctionDeclContext> invalidFunctionDeclarations = new HashSet<>();

    /**
     * Conjunto canónico (lower-case) com todos os nomes globais já declarados (variáveis e funções).
     */
    private final Set<String> declaredGlobalNames = new HashSet<>();



    /**
     * Constrói o visitor da Definition Phase com reporting direto para consola.
     *
     * @param symbolTable Tabela de símbolos global partilhada entre as passagens semânticas.
     */
    public DefinitionSemanticVisitor(SymbolTable symbolTable)
    {
        this(symbolTable, null);
    }

    /**
     * Constrói o visitor da Definition Phase com um sink de erros opcional.
     *
     * @param symbolTable Tabela de símbolos global partilhada entre as passagens semânticas.
     * @param errorSink   Handler opcional para recolha de erros (linha, mensagem).
     */
    public DefinitionSemanticVisitor(SymbolTable symbolTable, BiConsumer<Integer, String> errorSink)
    {
        this.symbolTable = symbolTable;
        this.errorSink = errorSink;
    }


    /**
     * Regista a assinatura de uma função no scope global.
     *
     * <p>Responsabilidades:</p>
     * <ul>
     *   <li>Deteta duplicação do nome no global (contra variáveis globais já registadas e contra funções já existentes na {@link SymbolTable}).</li>
     *   <li>Extrai e resolve o tipo de retorno (ou {@code null} para void).</li>
     *   <li>Extrai e resolve os tipos dos parâmetros formais.</li>
     *   <li>Guarda a assinatura como {@link FunctionType} na {@link SymbolTable}.</li>
     * </ul>
     *
     * <p>Nota: esta fase não visita o corpo ({@code block}) da função, para permitir que a 2ª passagem (Reference Phase)
     * valide o corpo já com todas as assinaturas disponíveis (forward calls).</p>
     *
     * @param ctx Contexto do parser da declaração de função.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitFunctionDecl(FavaParser.FunctionDeclContext ctx) {
        String funcName = ctx.ID().getText();
        String key = funcName.toLowerCase();

        if (declaredGlobalNames.contains(key)) {
            errorAlreadyDeclared(ctx.getStart().getLine(), funcName);
            invalidFunctionDeclarations.add(ctx);
            return null;
        }

        // 1. Evitar funções duplicadas no scope global
        if (symbolTable.exists(funcName))
        {
            errorAlreadyDeclared(ctx.getStart().getLine(), funcName);
            invalidFunctionDeclarations.add(ctx);
            return null;
        }

        // 2. Extrair o tipo de retorno (se não houver '-> type', fica null/void)
        FavaType returnType = null;
        if (ctx.type() != null) {
            returnType = FavaType.resolve(ctx.type().getText());
        }

        // 3. Extrair os tipos dos parâmetros formais (argumentos)
        List<FavaType> paramTypes = new ArrayList<>();
        if (ctx.formalParameters() != null)
            for (FavaParser.FormalParameterContext paramCtx : ctx.formalParameters().formalParameter())
            {
                paramTypes.add(FavaType.resolve(paramCtx.type().getText()));
            }

        // 4. Criar a assinatura e guardar na Symbol Table
        FunctionType functionSignature = new FunctionType(returnType, paramTypes);
        symbolTable.add(funcName, functionSignature);

        // ATENÇÃO: Retornamos null e NÃO fazemos visitChildren(ctx) nem visit(ctx.block()).
        // O corpo da função (lógica, variáveis locais) só será processado na 2ª passagem!
        return null;
    }

    /**
     * Regista nomes de variáveis globais e deteta duplicações no escopo global.
     *
     * <p>Esta fase não tipa nem aloca; apenas garante unicidade de nomes a nível global.</p>
     *
     * @param ctx Contexto do parser da declaração de variáveis.
     * @return Sempre {@code null}.
     */
    @Override
    public Void visitVarDeclaration(FavaParser.VarDeclarationContext ctx) {
        for (FavaParser.VarInitContext initCtx : ctx.varInit()) {
            String varName = initCtx.ID().getText();
            String key = varName.toLowerCase();
            if (declaredGlobalNames.contains(key)) {
                errorAlreadyDeclared(initCtx.getStart().getLine(), varName);
            } else {
                declaredGlobalNames.add(key);
            }
        }
        return null;
    }

    /**
     * Ponto central de registo de erros desta fase.
     *
     * <p>Incrementa o contador interno e reporta via {@link #errorSink} quando fornecido; caso contrário imprime no terminal.</p>
     *
     * @param line    Linha de origem do erro.
     * @param message Mensagem do erro.
     */
    private void reportError(int line, String message) {
        semanticErrors++;
        if (errorSink != null) {
            errorSink.accept(line, message);
        } else {
            System.out.println("error in line " + line + ": " + message);
        }
    }

    /**
     * Erro de duplicação no escopo global (aplica-se a variáveis globais e funções).
     */
    private void errorAlreadyDeclared(int line, String name) {
        reportError(line, name + " already declared");
    }

    /**
     * Devolve o número de erros semânticos detetados na Definition Phase.
     *
     * @return Quantidade de erros.
     */
    public int getSemanticErrors() {
        return semanticErrors;
    }

    /**
     * Devolve o conjunto de funções inválidas (por exemplo, duplicadas) detetadas nesta fase.
     *
     * <p>É usado pela {@link ReferenceSemanticVisitor} para evitar analisar corpos que gerariam erros em cascata.</p>
     *
     * @return Conjunto de nós {@link FavaParser.FunctionDeclContext} inválidos.
     */
    public Set<FavaParser.FunctionDeclContext> getInvalidFunctionDeclarations() {
        return invalidFunctionDeclarations;
    }

}
