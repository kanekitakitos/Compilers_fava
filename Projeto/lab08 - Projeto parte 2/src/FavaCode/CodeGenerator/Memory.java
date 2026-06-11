package FavaCode.CodeGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Gestor de Memória Estática por índices relativos para a FavaVM.
 * 
 * <p>Durante a fase de Geração de Código, converte identificadores (Strings) em índices
 * numéricos diretos (slots), permitindo à VM aceder à memória em tempo O(1).
 * Implementa uma política "Last-In, First-Out" (LIFO) de alocação de escopo, que 
 * liberta e reaproveita endereços físicos quando os blocos sintáticos são encerrados.</p>
 * 
 * <p>Referências Académicas:</p>
 * <ul>
 *   <li><b>Nystrom (Crafting Interpreters), Cap. 22 "Local Variables":</b>
 *       Implementa a estratégia de simular a stack no momento da compilação ('Stack-Slot Reuse')
 *       para converter nomes arbitrários de variáveis em índices absolutos de uma array virtual.</li>
 *   <li><b>Aho, Lam, Sethi, Ullman (Dragon Book), Cap. 7.2 "Stack Allocation of Space":</b>
 *       Justifica o ciclo de vida e a gestão efetuada com base em "Activation Records".</li>
 * </ul>
 * 
 * <p>O registo dinâmico de `maxAllocated` informa a instrução 'galloc' sobre o pico 
 * de consumo de memória do programa, otimizando o arranque da VM.</p>
 *
 * ### Exemplo de Uso
 * <pre>{@code
 * Memory memory = new Memory();
 * 
 * // Início de um bloco
 * memory.enterScope();
 * 
 * // Aloca a variável "x" e obtém o seu endereço
 * int addrX = memory.alloc("x"); // ex: retorna 0
 * 
 * // Aloca "y"
 * int addrY = memory.alloc("y"); // ex: retorna 1
 * 
 * // Acessa o endereço de "x"
 * int retrievedAddrX = memory.getAddress("x"); // retorna 0
 * 
 * // Fim do bloco
 * memory.exitScope(); // Os endereços de "x" e "y" podem ser reutilizados
 * }</pre>
 *
 * @see CodeGen
 */
public class Memory
{

    /**
     * Pilha de escopos, onde cada escopo é um mapa que associa o nome de uma variável (em minúsculas)
     * ao seu endereço de memória (índice inteiro).
     */
    private final Stack<Map<String, Integer>> scopes = new Stack<>();

    /**
     * Pilha para guardar os "ponteiros" de memória (offsets) quando entramos em novos escopos.
     * Essencial para restaurar o estado da memória ao sair de um bloco.
     */
    private final Stack<Integer> savedAddresses = new Stack<>();

    /** O próximo endereço de memória disponível para alocação. */
    private int nextAddress = 0;
    
    /** O pico máximo de endereços alocados simultaneamente, usado para a instrução `galloc`. */
    private int maxAllocated = 0;

    /**
     * Construtor que inicializa a memória, criando imediatamente o escopo global.
     */
    public Memory() {
        enterScope(); // Cria o escopo global
    }

    /**
     * Inicia um novo escopo de memória, empurrando um novo mapa para a pilha de escopos
     * e salvando o endereço de memória atual para restauração posterior.
     */
    public void enterScope() {
        scopes.push(new HashMap<>());
        savedAddresses.push(nextAddress); // Guarda o estado atual do ponteiro de memória
    }

    /**
     * Finaliza o escopo atual, removendo-o da pilha e restaurando o ponteiro de memória
     * para o seu estado anterior (comportamento LIFO).
     */
    public void exitScope() {
        scopes.pop();
        nextAddress = savedAddresses.pop(); // Restaura o ponteiro de memória
    }

    /**
     * Aloca um novo endereço de memória para uma variável no escopo atual.
     * Se a variável já estiver alocada no mesmo escopo, retorna o endereço existente.
     *
     * @param varName O nome da variável a ser alocada.
     * @return O endereço de memória (índice) alocado para a variável.
     */
    public int alloc(String varName) {
        String lowerName = varName.toLowerCase();
        Map<String, Integer> currentScope = scopes.peek();

        // Evita dupla alocação no mesmo escopo
        if (currentScope.containsKey(lowerName))
            return currentScope.get(lowerName);

        int addr = nextAddress++;
        currentScope.put(lowerName, addr);

        // Atualiza o pico de memória, crucial para a instrução `galloc`
        if (nextAddress > maxAllocated)
            maxAllocated = nextAddress;

        return addr;
    }

    /**
     * Obtém o endereço de memória de uma variável, procurando do escopo mais interno para o mais externo.
     *
     * @param varName O nome da variável cujo endereço é procurado.
     * @return O endereço de memória (índice) da variável.
     * @throws RuntimeException se o endereço para a variável não for encontrado em nenhum escopo.
     */
    public int getAddress(String varName)
    {
        String lowerName = varName.toLowerCase();

        // Procura do escopo local para o global (de dentro para fora)
        for (int i = scopes.size() - 1; i >= 0; i--)
            if (scopes.get(i).containsKey(lowerName))
                return scopes.get(i).get(lowerName);



        throw new RuntimeException("CodeGen Memory: Endereço para '" + varName + "' não encontrado.");
    }

    /**
     * Retorna o número total de alocações de memória que foram necessárias no pico de execução do programa.
     * Este valor é usado para a instrução `galloc` da Máquina Virtual.
     *
     * @return O número máximo de variáveis alocadas simultaneamente.
     */
    public int getTotalAllocations() {
        return maxAllocated;
    }
}
