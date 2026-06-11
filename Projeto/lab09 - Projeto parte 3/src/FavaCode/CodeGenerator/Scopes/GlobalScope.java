package FavaCode.CodeGenerator.Scopes;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * Gestor de memória global para o gerador de bytecode.
 *
 * <p>Converte identificadores globais em endereços absolutos usados por {@code gload/gstore}.
 * Implementa scopes aninhados e reutilização LIFO de endereços (restaurando {@code nextAddress}
 * ao sair de um scope), mantendo {@code maxAllocated} como "high-water mark" para emitir {@code galloc}.</p>
 */
public class GlobalScope extends ScopeManager {
    private final Deque<Integer> savedAddresses = new ArrayDeque<>();
    private int nextAddress = 0;
    private int maxAllocated = 0;

    /**
     * Inicializa a memória global já com um scope base.
     */
    public GlobalScope() {
        enterScope();
    }

    /**
     * Abre um novo scope e guarda o ponteiro de alocação atual para permitir rollback ao sair.
     */
    @Override
    public void enterScope() {
        super.enterScope();
        savedAddresses.push(nextAddress);
    }

    /**
     * Fecha o scope atual e restaura o ponteiro de alocação para permitir reutilização LIFO.
     */
    @Override
    public void exitScope() {
        super.exitScope();
        nextAddress = savedAddresses.pop();
    }

    /**
     * Reserva (ou reaproveita) um endereço global para um identificador no scope atual.
     *
     * @param varName Nome da variável (case-insensitive).
     * @return Endereço absoluto.
     */
    public int alloc(String varName) {
        String lowerName = varName.toLowerCase();
        Map<String, Integer> currentScope = scopes.peek();

        if (currentScope.containsKey(lowerName))
            return currentScope.get(lowerName);

        int addr = nextAddress++;
        currentScope.put(lowerName, addr);

        if (nextAddress > maxAllocated)
            maxAllocated = nextAddress;

        return addr;
    }

    /**
     * Resolve o endereço absoluto de um identificador global, respeitando scopes ativos.
     *
     * @param varName Nome da variável.
     * @return Endereço absoluto.
     * @throws RuntimeException Se o símbolo não existir em nenhum scope.
     */
    public int getAddress(String varName) {
        Integer addr = resolve(varName);
        if (addr == null)
            throw new RuntimeException("CodeGen Memory: Endereço para '" + varName + "' não encontrado.");
        return addr;
    }

    /**
     * @return Pico máximo de alocações simultâneas (high-water mark), usado para {@code galloc}.
     */
    public int getTotalAllocations() {
        return maxAllocated;
    }
}
