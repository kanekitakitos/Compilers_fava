package FavaCode.Semantic;

import FavaCode.Semantic.Types.*;

/**
 * Interface base para todos os tipos de dados suportados nativamente pela linguagem Fava.
 * Esta abstração permite uma gestão uniforme do Sistema de Tipos e das Regras Semânticas ({@link TypeRules}),
 * garantindo que todas as representações de tipo (ex: {@link IntegerType}, {@link StringType})
 * forneçam uma API consistente para a fase de Análise Semântica.
 * <p>
 * ### Exemplo de Uso
 * <pre>{@code
 * // Uma função que recebe qualquer tipo da linguagem Fava
 * public void processAnyFavaType(FavaType type) {
 *     System.out.println("Tipo processado: " + type.getName());
 *     if (type.isNumeric()) {
 *         System.out.println("Este é um tipo numérico.");
 *     }
 * }
 * }</pre>
 *
 * @see TypeRules
 * @see SemanticAnalyzerVisitor
 */
public interface FavaType {
    /**
     * Devolve o nome canónico do tipo, que deve ser único e é utilizado nas avaliações semânticas.
     * Este nome geralmente mapeia diretamente para a palavra-chave correspondente na linguagem Fava (ex: "integer", "bool").
     *
     * @return Uma String contendo o identificador do tipo.
     */
    String getName();

    /**
     * Verifica se o tipo é puramente numérico, o que o torna compatível com operações aritméticas.
     * Este método é crucial para a {@link TypeRules} determinar a validade de expressões matemáticas.
     *
     * @return {@code true} se for um tipo numérico (como {@link IntegerType} ou {@link RealType});
     * {@code false} caso contrário (ex: {@link BoolType} ou {@link StringType}).
     */
    boolean isNumeric();

    /**
     * Converte uma representação textual de um tipo (ex: "integer") na sua instância {@link FavaType} correspondente.
     * Este método de fábrica (factory method) centraliza a lógica de resolução de tipos,
     * tornando o {@link SemanticAnalyzerVisitor} mais limpo e robusto.
     *
     * @param typeName A string do nome do tipo a ser resolvido (case-insensitive).
     * @return A instância Singleton do {@link FavaType} correspondente (ex: {@link IntegerType#INSTANCE}).
     * Retorna {@code null} se o nome do tipo não for reconhecido, indicando um erro semântico.
     */
    static FavaType resolve(String typeName) {
        // Garante que a linguagem seja case-insensitive na declaração de tipos
        return switch (typeName.toLowerCase()) {
            case "integer" -> IntegerType.INSTANCE;
            case "real" -> RealType.INSTANCE;
            case "string" -> StringType.INSTANCE;
            case "bool" -> BoolType.INSTANCE;
            default ->
                // Se o tipo não for reconhecido (ex: "batata"),
                // retorna null para que o SemanticAnalyzerVisitor possa reportar o erro.
                    null;
        };
    }
}
