package FavaCode.Semantic.Types;

import FavaCode.Semantic.FavaType;

/**
 * Representa os números Reais (Ponto flutuante / Doubles) implementados como base nativa na linguagem Fava.
 * Permite avaliação de matemática avançada e interage com o Analisador Semântico durante as comparações/associações.
 *
 * @see FavaType
 */
public class RealType implements FavaType {

    /** Instância global estática (Singleton), usada para otimização de memória aquando a inferência na AST. */
    public static RealType INSTANCE = new RealType();

    private RealType() {}

    /**
     * O identificador único com o qual a {@link FavaCode.Semantic.TypeRules} regista o tipo, sendo ele "real".
     */
    public String getName() { return "real"; }

    /**
     * Reais são números. Permite que as instâncias nativas interajam nos blocos booleanos de matemática aritmética.
     *
     * @return {@code true}, devido à componente matemática das operações Fava sobre o tipo.
     */
    public boolean isNumeric() { return true; }
}