package FavaCode.Semantic;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Implementação de uma Tabela de Símbolos baseada em Escopo Estático (Lexical Scoping).
 * Utiliza uma pilha de dicionários (Stack of Maps) para refletir o aninhamento de blocos no código-fonte.
 *
 * <p>Referências Académicas:</p>
 * <ul>
 *   <li><b>Aho, Lam, Sethi, Ullman (Dragon Book), Cap. 2.7.1 "Symbol Table Per Scope":</b>
 *       Garante a resolução de nomes respeitando a "most-closely nested rule", suportando nativamente
 *       o "Shadowing" de variáveis globais por variáveis locais.</li>
 *   <li><b>Nystrom (Crafting Interpreters), Cap. 11 "Resolving and Binding":</b>
 *       Demonstra a utilização de uma Stack de Maps durante a fase de Semantic Analysis para garantir
 *       a resolução estática de nomes.</li>
 *   <li><b>Parr (The Definitive ANTLR 4 Reference), Cap. 8.4 "Validating Program Symbol Usage".</b></li>
 * </ul>
 * <p>
 * ### Exemplo de Uso
 * <pre>{@code
 * SymbolTable symbolTable = new SymbolTable();
 *
 * // Ao entrar num bloco novo na AST:
 * symbolTable.enterScope();
 *
 * // Quando se declara "integer count;"
 * symbolTable.add("count", IntegerType.INSTANCE);
 *
 * // Verifica se a variável existe quando se vai atribuir um valor ("count := 5")
 * if (symbolTable.exists("count")) {
 *     FavaType tipo = symbolTable.getType("count"); // Retorna IntegerType.INSTANCE
 * }
 *
 * // Ao sair do bloco
 * symbolTable.exitScope();
 * }</pre>
 *
 * @see FavaType
 * @see SemanticAnalyzerVisitor
 */
public class SymbolTable {

    /**
     * Pilha de escopos, onde o topo representa o escopo léxico atual.
     * Cada mapa associa um nome de variável ("x", minúsculas) ao seu tipo respetivo (ex: {@link FavaCode.Semantic.Types.IntegerType}).
     */
    private Stack<Map<String, FavaType>> scope = new Stack<>();

    /**
     * Inicializa a Tabela de Símbolos, criando imediatamente o escopo global (base da pilha).
     */
    public SymbolTable() {
        enterScope();
    }

    /**
     * Regista uma nova declaração de variável no escopo atual (topo da pilha).
     * Todos os identificadores são convertidos para minúsculas (case-insensitive).
     *
     * @param name O identificador textual da variável definida no código Fava.
     * @param type O objeto {@link FavaType} base correspondente, inferido pelo Parser.
     */
    public void add(String name, FavaType type) {
        Map<String, FavaType> currentScope = scope.peek();

        currentScope.put(name.toLowerCase(), type);
    }

    /**
     * Verifica estritamente se um identificador já se encontra declarado **no escopo atual**.
     * Este método é crítico para o {@link SemanticAnalyzerVisitor} prevenir "double declarations" no mesmo bloco
     * (ex: tentar declarar duas vezes "integer x;").
     *
     * @param name O identificador pretendido.
     * @return Devolve {@code true} se o símbolo existir no escopo atual; {@code false} caso contrário.
     */
    public boolean exists(String name) {
        Map<String, FavaType> currentScope = scope.peek();

        return currentScope.containsKey(name.toLowerCase());
    }

    /**
     * Procura pelo tipo base de uma variável acedida no código, percorrendo os escopos
     * de dentro (atual) para fora (global).
     *
     * @param name O identificador que se pretende resgatar.
     * @return O {@link FavaType} que diz respeito à variável registada,
     * ou {@code null} se a variável não tiver sido declarada previamente em nenhum escopo válido.
     */
    public FavaType getType(String name) {

        String lowerName = name.toLowerCase();

        // Pesquisa bottom-up (LIFO), do escopo local para o global
        for (int i = scope.size() - 1; i >= 0; i--) {
            if (scope.get(i).containsKey(lowerName)) {
                return scope.get(i).get(lowerName);
            }

        }
        return null;
    }

    /**
     * Adiciona uma nova camada (HashMap) no topo da pilha, definindo o início de um novo escopo local de variáveis.
     * Deve ser chamado sempre que uma estrutura de bloco (como "{" no Fava) começa.
     */
    public void enterScope() {
        this.scope.push(new HashMap<>());

    }

    /**
     * Remove o contexto atual (o topo da pilha), finalizando o escopo.
     * As variáveis nele contidas são efetivamente "esquecidas" pela fase semântica.
     */
    public void exitScope() {
        this.scope.pop();
    }
}
