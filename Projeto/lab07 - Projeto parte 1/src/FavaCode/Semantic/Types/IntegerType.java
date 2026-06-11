package FavaCode.Semantic.Types;

import FavaCode.Semantic.FavaType;

/**
 * Representação no Analisador Semântico de dados do tipo inteiro, permitindo ao Analisador gerir
 * valores puramente numéricos de dimensão inteira durante avaliações lógicas.
 *
 * @see FavaType
 */
public class IntegerType implements FavaType {
    
    /** Singleton para a referência global de 'integer', reduzindo sobrecarga de objetos repetidos na validação da árvore semântica. */
    public static IntegerType INSTANCE = new IntegerType();


    private IntegerType() {}
    /**
     * Designação referencial com a qual esta classe assina o tipo numérico correspondente.
     *
     * @return O texto de identificação: "integer".
     */
    public String getName() { return "integer"; }

    /**
     * Sendo um tipo inteiro, indica à linguagem que aceita operações matemáticas, lógicas ou literais.
     *
     * @return {@code true}.
     */
    public boolean isNumeric() { return true; }
}