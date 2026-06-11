package FavaCode.Semantic;

/**
 * Interface base para todos os tipos suportados nativamente pela linguagem Fava.
 * Permite uma gestão uniforme do Sistema de Tipos e das Regras Semânticas ({@link TypeRules}).
 * Todas as classes que representam um tipo (ex: IntegerType, StringType, etc.) devem implementar esta interface.
 */
public interface FavaType
{
    /**
     * Devolve o nome representativo do tipo, que deve ser único e utilizado nas avaliações semânticas.
     * Geralmente mapeia diretamente para a keyword correspondente no código da linguagem Fava (ex: "int", "bool").
     *
     * @return Uma String contendo o identificador do tipo na linguagem Fava.
     */
    String getName();

    /**
     * Verifica se o tipo correspondente é puramente numérico ou compatível matematicamente com operações aritméticas.
     * Facilita no cruzamento lógico da AST quando há operações como soma ou comparação.
     *
     * @return {@code true} caso seja um tipo numérico (como Integer ou Real); {@code false} caso contrário (ex: Bool ou String).
     */
    boolean isNumeric();
}