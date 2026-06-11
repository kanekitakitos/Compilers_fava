package FavaCode.CodeGenerator.Scopes;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstração partilhada para gestão de scopes lexicais (nested scopes) durante a geração de código.
 *
 * <p>Modela uma pilha LIFO de mapas {@code name -> address}. O topo da pilha representa o scope
 * atualmente ativo. A resolução de identificadores percorre do scope mais interno para o mais externo.</p>
 *
 * <p>Esta classe não define como os endereços são atribuídos; isso é responsabilidade das subclasses
 * (ex.: {@link GlobalScope} e {@link LocalScope}).</p>
 */
public abstract class ScopeManager {
    /**
     * Pilha de scopes, do mais recente (topo) para o mais antigo (base).
     */
    protected final Deque<Map<String, Integer>> scopes = new ArrayDeque<>();

    /**
     * Abre um novo scope (push) para declarações a partir deste ponto.
     */
    public void enterScope() {
        scopes.push(new HashMap<>());
    }

    /**
     * Fecha o scope atual (pop), tornando as suas declarações inacessíveis.
     */
    public void exitScope() {
        scopes.pop();
    }

    /**
     * Resolve um identificador para o seu endereço, respeitando shadowing.
     *
     * @param name Nome no código fonte (case-insensitive).
     * @return Endereço associado, ou {@code null} se não existir em nenhum scope ativo.
     */
    public Integer resolve(String name) {
        String key = name.toLowerCase();
        for (Map<String, Integer> scope : scopes) {
            Integer addr = scope.get(key);
            if (addr != null) return addr;
        }
        return null;
    }
}
