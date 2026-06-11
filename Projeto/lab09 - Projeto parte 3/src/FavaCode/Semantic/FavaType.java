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
 * @see ReferenceSemanticVisitor
 */
public interface FavaType {
    /**
     * Nomes canónicos dos tipos primitivos da linguagem, derivados das instâncias Singleton.
     *
     * <p>Centraliza os identificadores textuais para evitar duplicação de strings espalhadas no código.</p>
     */
    String INTEGER_NAME = IntegerType.INSTANCE.getName();
    String REAL_NAME = RealType.INSTANCE.getName();
    String STRING_NAME = StringType.INSTANCE.getName();
    String BOOL_NAME = BoolType.INSTANCE.getName();

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
     * tornando o {@link ReferenceSemanticVisitor} mais limpo e robusto.
     *
     * @param typeName A string do nome do tipo a ser resolvido (case-insensitive).
     * @return A instância Singleton do {@link FavaType} correspondente (ex: {@link IntegerType#INSTANCE}).
     * Retorna {@code null} se o nome do tipo não for reconhecido, indicando um erro semântico.
     */
    static FavaType resolve(String typeName) {
        String normalized = typeName.toLowerCase();
        if (INTEGER_NAME.equals(normalized)) return IntegerType.INSTANCE;
        if (REAL_NAME.equals(normalized)) return RealType.INSTANCE;
        if (STRING_NAME.equals(normalized)) return StringType.INSTANCE;
        if (BOOL_NAME.equals(normalized)) return BoolType.INSTANCE;
        return null;
    }
}
