package FavaCode.Semantic.Types;

import FavaCode.Semantic.FavaType;

/**
 * Representa o tipo lógico Booleano ("bool") na linguagem de programação Fava.
 * O compilador usa esta classe durante a fase de Análise Semântica para inferir
 * condições em blocos como 'if' ou 'while', assim como garantir validade em
 * operações relacionais (>, <) ou de igualdade (==, !=).
 * 
 * <p>Implementa o padrão Singleton para otimizar o consumo de memória durante o 
 * parse da Abstract Syntax Tree (AST).</p>
 *
 * ### Exemplo de Uso
 * <pre>{@code
 * // Registo de uma condição lógica válida na AST
 * public FavaType visitBoolExpr(BoolExprContext ctx) {
 *     return saveType(ctx, BoolType.INSTANCE);
 * }
 * }</pre>
 *
 * @see FavaType
 * @see FavaCode.Semantic.SemanticAnalyzerVisitor
 */
public class BoolType implements FavaType {

    /** 
     * Instância global (Singleton) que define o tipo Booleano para a linguagem Fava.
     * Esta instância é reutilizada em toda a árvore sintática para verificar a tipagem.
     */
    public static BoolType INSTANCE = new BoolType();

    /**
     * Impede a criação manual de instâncias externas mantendo o uso focado no Singleton {@link #INSTANCE}.
     */
    private BoolType() {}
    
    /**
     * Devolve o identificador interno utilizado nas tabelas de conversão ({@link FavaCode.Semantic.TypeRules})
     * e tabela de símbolos ({@link FavaCode.Semantic.SymbolTable}) para a linguagem Fava.
     *
     * @return A palavra-chave "bool".
     */
    @Override
    public String getName() {
        return "bool";
    }

    /**
     * Identifica se as operações com este tipo permitem cálculos lógicos baseados em números (ex: aritmética).
     * Como se trata de um valor de verdadeiro/falso, este método retorna {@code false}.
     *
     * @return O valor {@code false}, pois não pode sofrer operações aritméticas nativas da linguagem.
     */
    @Override
    public boolean isNumeric() {
        return false;
    }
}
