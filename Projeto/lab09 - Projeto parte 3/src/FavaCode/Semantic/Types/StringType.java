package FavaCode.Semantic.Types;

import FavaCode.Semantic.FavaType;
import FavaCode.Semantic.ReferenceSemanticVisitor;

/**
 * Representa o tipo nativo String na linguagem Fava, responsável por gerir
 * sequências literais de caracteres durante a compilação.
 *
 * <p>O Analisador Semântico ({@link ReferenceSemanticVisitor})
 * invoca esta classe ao avaliar as expressões de texto na Abstract Syntax Tree (AST),
 * para garantir que certas operações, como a concatenação ("||"), são compatíveis.
 * Emprega o padrão Singleton globalmente.</p>
 *
 * ### Exemplo de Uso
 * <pre>{@code
 * // Quando o parser reconhece um token "Ola Mundo!" no ficheiro
 * @Override
 * public FavaType visitStringExpr(StringExprContext ctx) {
 *     return saveType(ctx, StringType.INSTANCE);
 * }
 * }</pre>
 *
 * @see FavaType
 * @see FavaCode.Semantic.TypeRules
 */
public class StringType implements FavaType
{
    /** 
     * Instância única global (Singleton) alocada na inicialização do compilador Fava.
     * Serve de referência uniforme nas regras de compatibilidade da semântica.
     */
    public static StringType INSTANCE = new StringType();

    /**
     * Preserva o acesso estático impedindo a instanciação múltipla manual.
     */
    private StringType() {}

    /**
     * Determina a string exata correspondente ao registo dos tokens na
     * tabela de tipos e análise léxica interna.
     *
     * @return O texto de formatação "string".
     */
    @Override
    public String getName() {
        return "string";
    }

    /**
     * O tipo String lida apenas com concatenação textual, impossibilitando
     * operações numéricas com valores aritméticos puramente ditos.
     *
     * @return {@code false}, pois textos literais são incompatíveis com cálculos nativos.
     */
    @Override
    public boolean isNumeric() {
        return false;
    }
}
