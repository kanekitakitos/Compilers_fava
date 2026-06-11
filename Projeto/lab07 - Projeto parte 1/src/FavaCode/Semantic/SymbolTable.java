package FavaCode.Semantic;

import java.util.HashMap;
import java.util.Map;

/**
 * Tabela de Símbolos, responsável por gerir o escopo, tipagem e listagem das variáveis do programa Fava compilado.
 * Implementa de forma simples um HashMap que guarda mapeamentos diretos de identificador de variável (String) para tipo ({@link FavaType}).
 */
public class SymbolTable
{
    // "nome_da_variavel" -> Tipo (int, double, etc)
    private Map<String, FavaType> table = new HashMap<>();


    /**
     * Adiciona (ou atualiza) um novo símbolo na Tabela de Símbolos.
     * Mapeia o nome do identificador (String) com um tipo suportado ({@link FavaType}).
     *
     * @param name Nome (identificador) da variável que foi declarada.
     * @param type Tipo base do valor, de acordo com o definido durante o parse.
     */
    public void add(String name, FavaType type)
    {
        table.put(name, type);
    }

    /**
     * Verifica se uma variável já foi adicionada na Tabela de Símbolos do escopo atual.
     * É útil durante a passagem do semantic analyzer para prevenir "double declarations" (ex: "int a" declarado duas vezes) 
     * ou utilização de variáveis sem declaração prévia.
     *
     * @param name Nome ou identificador a procurar.
     * @return {@code true} se encontrar a variável no HashMap; {@code false} se esta não existir no scope.
     */
    public boolean exists(String name) {
        return table.containsKey(name);
    }

    /**
     * Tenta recuperar qual é o tipo base que foi declarado e que já deve estar contido na tabela, permitindo
     * que sejam inferidas regras de tipos para cálculos ou operações.
     *
     * @param name O identificador da variável que o utilizador pretende resgatar.
     * @return O tipo {@link FavaType} que diz respeito à variável registada, ou {@code null} se a variável não tiver sido inicializada.
     */
    public FavaType getType(String name) {
        return table.get(name);
    }
}