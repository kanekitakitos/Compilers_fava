package FavaCode.CodeGenerator.Scopes;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Gestor de memória local (por função) para o gerador de bytecode.
 *
 * <p>Produz endereços relativos a {@code FP} (Frame Pointer), usados por {@code lload/lstore}.
 * Este modelo está alinhado com a convenção de frame usada na VM:
 * <ul>
 *   <li>{@code FP + 0}: oldFP</li>
 *   <li>{@code FP + 1}: returnAddr</li>
 *   <li>{@code FP + 2...}: variáveis locais</li>
 *   <li>{@code FP - nArgs ... FP - 1}: parâmetros formais (offsets negativos)</li>
 * </ul>
 * </p>
 *
 * <p>O gestor suporta nested scopes e reutilização LIFO de slots locais por bloco (restaurando
 * {@code nextLocalAddr} quando um scope termina).</p>
 */
public class LocalScope extends ScopeManager {
    private final int nArgs;
    private final Deque<Integer> savedAddresses = new ArrayDeque<>();

    private int nextLocalAddr = 2;
    private int maxLocalAddr = 2;

    /**
     * @param nArgs Número de argumentos formais da função atual (usado para offsets negativos).
     */
    public LocalScope(int nArgs) {
        this.nArgs = nArgs;
        enterScope();
    }

    /**
     * Abre um novo scope e salva o estado do ponteiro de alocação de locais,
     * permitindo reutilização de slots quando o bloco termina.
     */
    @Override
    public void enterScope() {
        super.enterScope();
        savedAddresses.push(nextLocalAddr);
    }

    /**
     * Fecha o scope atual e restaura o ponteiro de alocação de locais.
     */
    @Override
    public void exitScope() {
        super.exitScope();
        nextLocalAddr = savedAddresses.pop();
    }

    public int exitScopeAndGetPopCount() {
        int before = savedAddresses.pop();
        super.exitScope();
        int allocated = nextLocalAddr - before;
        nextLocalAddr = before;
        return Math.max(0, allocated);
    }

    /**
     * Define um parâmetro formal no scope atual.
     *
     * @param name       Nome do parâmetro.
     * @param paramIndex Índice 0-based do parâmetro na assinatura.
     */
    public void defineParam(String name, int paramIndex) {
        int addr = paramIndex - nArgs;
        scopes.peek().put(name.toLowerCase(), addr);
    }

    /**
     * Define uma variável local e devolve o seu endereço relativo a {@code FP}.
     *
     * @param name Nome da variável local.
     * @return Endereço relativo ({@code >= 2}).
     */
    public int defineLocal(String name) {
        int addr = nextLocalAddr++;
        maxLocalAddr = Math.max(maxLocalAddr, nextLocalAddr);
        scopes.peek().put(name.toLowerCase(), addr);
        return addr;
    }

    /**
     * @return Número de slots locais necessários (para emitir {@code lalloc}).
     */
    public int getLocalCount() {
        return Math.max(0, maxLocalAddr - 2);
    }
}
