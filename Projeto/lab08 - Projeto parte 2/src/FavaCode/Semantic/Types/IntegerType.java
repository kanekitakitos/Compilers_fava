package FavaCode.Semantic.Types;

import FavaCode.Semantic.FavaType;

/**
 * Representa o tipo de dados Inteiro dentro do compilador Fava.
 * Esta classe é fundamental durante a fase de Análise Semântica para validar operações aritméticas
 * e garantir a compatibilidade de tipos (ex: verificar se é possível somar um inteiro com um real).
 * 
 * <p>Implementa o padrão Singleton para evitar a criação desnecessária de múltiplos objetos do mesmo tipo
 * durante o percurso da Abstract Syntax Tree (AST).</p>
 * 
 * ### Exemplo de Uso
 * <pre>{@code
 * // Durante a visita a um nó de expressão inteira na AST
 * @Override
 * public FavaType visitIntegerExpr(IntegerExprContext ctx) {
 *     return saveType(ctx, IntegerType.INSTANCE);
 * }
 * }</pre>
 *
 * @see FavaType
 * @see FavaCode.Semantic.TypeRules
 */
public class IntegerType implements FavaType {
    
    /** 
     * Instância única (Singleton) que representa o tipo Integer na linguagem Fava.
     * Utilizada globalmente pela {@link FavaCode.Semantic.SemanticAnalyzerVisitor} e {@link FavaCode.Semantic.TypeRules}.
     */
    public static IntegerType INSTANCE = new IntegerType();

    /**
     * Construtor privado para garantir o padrão Singleton.
     */
    private IntegerType() {}
    
    /**
     * Devolve o identificador interno utilizado pela linguagem Fava para este tipo de dados.
     * Este nome é crucial para procurar regras na tabela de {@link FavaCode.Semantic.TypeRules}.
     *
     * @return A string "integer" representando o nome do tipo.
     */
    @Override
    public String getName() { 
        return "integer"; 
    }

    /**
     * Indica se este tipo suporta operações matemáticas nativas (ex: adição, multiplicação).
     * Sendo um inteiro, permite cálculos diretos na Virtual Machine.
     *
     * @return {@code true}, pois inteiros são numericamente operáveis.
     */
    @Override
    public boolean isNumeric() { 
        return true; 
    }
}
