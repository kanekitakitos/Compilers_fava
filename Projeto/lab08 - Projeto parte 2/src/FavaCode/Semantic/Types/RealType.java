package FavaCode.Semantic.Types;

import FavaCode.Semantic.FavaType;

/**
 * Representa o tipo de dados Real (números de ponto flutuante / double) nativo do compilador Fava.
 * Permite que a fase de Análise Semântica ({@link FavaCode.Semantic.SemanticAnalyzerVisitor})
 * valide operações aritméticas avançadas e interaja com as regras de conversão, 
 * por exemplo, avaliando coerção implícita entre Inteiros e Reais.
 *
 * <p>Implementa o padrão Singleton, partilhando uma única instância global durante
 * a travessia da Abstract Syntax Tree (AST), o que reduz as chamadas ao Garbage Collector.</p>
 *
 * ### Exemplo de Uso
 * <pre>{@code
 * // Quando o parser depara-se com "3.14" na linguagem
 * @Override
 * public FavaType visitRealExpr(RealExprContext ctx) {
 *     return saveType(ctx, RealType.INSTANCE);
 * }
 * }</pre>
 *
 * @see FavaType
 * @see FavaCode.Semantic.TypeRules
 */
public class RealType implements FavaType {

    /** 
     * Instância global estática (Singleton) que define e representa o tipo "real".
     * Reutilizada pelo motor semântico para qualquer referência a números reais na árvore sintática.
     */
    public static RealType INSTANCE = new RealType();

    /** 
     * Oculta o construtor, assegurando que seja estritamente usado através da variável {@link #INSTANCE}. 
     */
    private RealType() {}

    /**
     * Devolve a assinatura de identificação textual pela qual as lógicas de
     * coerção ({@link FavaCode.Semantic.TypeRules}) tratam os tipos de dados
     * para inferir a conversão a bytecodes.
     *
     * @return O texto de tipo "real".
     */
    @Override
    public String getName() { 
        return "real"; 
    }

    /**
     * Indica se este tipo é considerado matemático na linguagem, o que o torna elegível
     * para submissão a operações aritméticas nativas pelo {@link FavaCode.Semantic.TypeRules}.
     *
     * @return O valor constante {@code true}.
     */
    @Override
    public boolean isNumeric() { 
        return true; 
    }
}
