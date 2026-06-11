package FavaCode.Semantic.Types;

import FavaCode.Semantic.FavaType;

/**
 * Representa os literais booleanos (verdadeiro / falso) processados nativamente na linguagem Fava,
 * validando operações relacionais, de igualdade ou lógica unária pelo Analisador Semântico da AST.
 *
 * @see FavaType
 */
public class BoolType implements FavaType {

    /** Variável estática do próprio tipo base implementado. Uma instância contígua singleton diminui instâncias em execução. */
    public static BoolType INSTANCE = new BoolType();


    private BoolType() {}
    /**
     * O nome com o qual as lógicas de verificação da Regra de Tipos validam operações relativas ao tipo booleano.
     *
     * @return O texto de identificação: "bool".
     */
    @Override
    public String getName() {
        return "bool";
    }

    /**
     * Sendo avaliado no Analisador como booleano (true/false), impossibilita as operações de lógica matemática aritmética do {@link FavaCode.Semantic.TypeRules}.
     *
     * @return {@code false}.
     */
    @Override
    public boolean isNumeric() {
        return false;
    }
}