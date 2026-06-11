package FavaCode.Semantic.Types;

import FavaCode.Semantic.FavaType;

/**
 * Representa o tipo nativo String na linguagem Fava, gerindo sequências de caracteres literais.
 * Utilizado pelo Analisador Semântico durante as avaliações de tipos suportados nas expressões.
 *
 * @see FavaType
 */
public class StringType implements FavaType
{
    /** Instância única global (Singleton) do tipo para evitar sucessivas criações em memória na AST e nas Regras Semânticas. */
    public static StringType INSTANCE = new StringType();

    private StringType() {}

    /**
     * Identificador do tipo registado na arquitetura, sendo retornado "string".
     */
    @Override
    public String getName() {
        return "string";
    }

    /**
     * O tipo String não lida nativamente com matemática e não interage com adições base.
     * Retorna falso porque representa sequências de texto literais puras, incompatíveis com valores matemáticos (ex: int, double).
     *
     * @return {@code false}.
     */
    @Override
    public boolean isNumeric() {
        return false;
    }
}