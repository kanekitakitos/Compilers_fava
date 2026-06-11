package FavaCode.Semantic;

import FavaCode.Semantic.Types.BoolType;
import FavaCode.Semantic.Types.IntegerType;
import FavaCode.Semantic.Types.RealType;
import FavaCode.Semantic.Types.StringType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Motor de inferência e coerção de tipos da linguagem Fava.
 *
 * <p>Define a hierarquia e as regras de compatibilidade entre os tipos de dados básicos.
 * É responsável por detetar incompatibilidades em ASTs e instruir o CodeGenerator
 * a emitir opcodes de 'Widening Conversion' (ex: `itod` - int to double) de forma implícita.</p>
 *
 * <p>Referências Académicas:</p>
 * <ul>
 *   <li><b>Aho, Lam, Sethi, Ullman (Dragon Book), Cap. 6.5.2 "Type Conversions":</b>
 *       Formaliza o processo de "Coercion" (Conversão Implícita), essencial em
 *       operações matemáticas binárias que partilham tipos distintos (ex: real + integer),
 *       promovendo o tipo estreito para o tipo mais largo na hierarquia
 *       sem perda semântica de dados.</li>
 * </ul>
 * <p>
 * ### Exemplo de Uso
 * <pre>{@code
 * TypeRules rules = new TypeRules();
 *
 * // Exemplo 1: Adição entre Inteiro e Real
 * FavaType tipoSoma = rules.getResultType("+", IntegerType.INSTANCE, RealType.INSTANCE);
 * // tipoSoma será igual a RealType.INSTANCE
 *
 * // Exemplo 2: Negação Lógica Booleana
 * FavaType tipoNegacao = rules.getResultType("not", BoolType.INSTANCE);
 * // tipoNegacao será igual a BoolType.INSTANCE
 * }</pre>
 *
 * @see FavaType
 * @see ReferenceSemanticVisitor
 */
public class TypeRules {


    /**
     * HashMap principal que indexa as operações binárias válidas (ex: +, *, =), combinando
     * o operador e os tipos dos dois operandos para obter o tipo inferido (resultado).
     */
    private static final Map<BinaryOperationKey, FavaType> binaryRules = new HashMap<>();

    /**
     * HashMap secundário dedicado ao mapeamento de operadores unários (ex: -, not) e o seu tipo final.
     */
    private static final Map<UnaryOperationKey, FavaType> unaryRules = new HashMap<>();

    // Inicialização estática de todas as regras semânticas permitidas pela linguagem Fava.
    static {
        // Regras Unárias (Inversão e Negação Lógica)
        String[] unaryOpt = {"-", "not"};
        addRule(unaryOpt[0], IntegerType.INSTANCE, IntegerType.INSTANCE);
        addRule(unaryOpt[0], RealType.INSTANCE, RealType.INSTANCE);
        addRule(unaryOpt[1], BoolType.INSTANCE, BoolType.INSTANCE);

        // Aritmética base entre inteiros
        addRules(IntegerType.INSTANCE, new String[]{"+", "-", "*", "/", "mod"}, IntegerType.INSTANCE, IntegerType.INSTANCE);


        // Aritmética que resulta em Reais (Coerção Implícita e Reais com Reais)
        String[] arith = {"+", "-", "*", "/"};
        addRules(RealType.INSTANCE, arith, IntegerType.INSTANCE, RealType.INSTANCE);
        addRules(RealType.INSTANCE, arith, RealType.INSTANCE, RealType.INSTANCE);

        // Operação de Concatenação Textual
        String[] striOps = {"||"};
        addRules(StringType.INSTANCE, striOps, StringType.INSTANCE, StringType.INSTANCE);
        addRules(StringType.INSTANCE, striOps, IntegerType.INSTANCE, StringType.INSTANCE);
        addRules(StringType.INSTANCE, striOps, BoolType.INSTANCE, StringType.INSTANCE);
        addRules(StringType.INSTANCE, striOps, RealType.INSTANCE, StringType.INSTANCE);

        // Operações Lógicas Booleanas
        String[] boolOps = {"and", "or"};
        addRules(BoolType.INSTANCE, boolOps, BoolType.INSTANCE, BoolType.INSTANCE);

        // Operações Relacionais que produzem Booleanos
        String[] relOps = {"<", ">", "<=", ">="};
        addRules(BoolType.INSTANCE, relOps, IntegerType.INSTANCE, IntegerType.INSTANCE);
        addRules(BoolType.INSTANCE, relOps, IntegerType.INSTANCE, RealType.INSTANCE);
        addRules(BoolType.INSTANCE, relOps, RealType.INSTANCE, RealType.INSTANCE);

        // Operações de Igualdade ou Desigualdade
        String[] eqOps = {"=", "<>"};
        addRules(BoolType.INSTANCE, eqOps, IntegerType.INSTANCE, IntegerType.INSTANCE);
        addRules(BoolType.INSTANCE, eqOps, RealType.INSTANCE, IntegerType.INSTANCE);
        addRules(BoolType.INSTANCE, eqOps, RealType.INSTANCE, RealType.INSTANCE);
        addRules(BoolType.INSTANCE, eqOps, BoolType.INSTANCE, BoolType.INSTANCE);
        addRules(BoolType.INSTANCE, eqOps, StringType.INSTANCE, StringType.INSTANCE);

        // Regras de Atribuição a variáveis (o tipo da esquerda tem de engolir o da direita)
        String[] assignOps = {":="};
        addRules(IntegerType.INSTANCE, assignOps, IntegerType.INSTANCE, IntegerType.INSTANCE);
        addRules(RealType.INSTANCE, assignOps, RealType.INSTANCE, IntegerType.INSTANCE); // Coerção de int para real no Assign
        addRules(RealType.INSTANCE, assignOps, RealType.INSTANCE, RealType.INSTANCE);
        addRules(StringType.INSTANCE, assignOps, StringType.INSTANCE, StringType.INSTANCE);
        addRules(BoolType.INSTANCE, assignOps, BoolType.INSTANCE, BoolType.INSTANCE);

    }

    /**
     * Resolve e devolve qual será o tipo base inferido de uma operação binária aplicada sobre dois operandos.
     * Esta função possui inteligência de fallback bidirecional para operações comutativas: se "real + int" não
     * for encontrado, ela tentará inverter a ordem da chave para procurar por "int + real".
     *
     * @param op A string representando o símbolo do operador semântico (ex: "+", "mod", "<=").
     * @param t1 O {@link FavaType} que diz respeito à expressão esquerda (Left-Hand Side).
     * @param t2 O {@link FavaType} referente à expressão direita (Right-Hand Side).
     * @return O {@link FavaType} retornado pela expressão, ou {@code null} se a operação for ilegal/incompatível.
     */
    public FavaType getResultType(String op, FavaType t1, FavaType t2) {
        // 1. Tenta a correspondência direta
        FavaType result = binaryRules.get(new BinaryOperationKey(op, t1, t2));

        // 2. Se falhar, e não for atribuição, tenta de forma comutativa invertida
        if (result == null && !op.equals(":="))
            result = binaryRules.get(new BinaryOperationKey(op, t2, t1));

        return result;
    }

    /**
     * Inferência direta para operadores unários prefixos, avaliando o tipo do operando sobre o qual a ação ocorre.
     *
     * @param op          A string de operador semântico, como '-' (aritmético) ou 'not' (lógico).
     * @param operandType O {@link FavaType} em que o operador se tenta fixar (argumento).
     * @return O tipo {@link FavaType} resultante, ou {@code null} se essa regra for impossível (ex: 'not' num Inteiro).
     */
    public FavaType getResultType(String op, FavaType operandType) {
        return unaryRules.get(new UnaryOperationKey(op, operandType));
    }

    /**
     * Verifica compatibilidade direcional entre tipos (ex.: em atribuições, retornos e passagem de argumentos).
     *
     * <p>Regras aplicadas:
     * <ul>
     *   <li>tipos iguais são compatíveis</li>
     *   <li>exceção: {@code real} aceita {@code integer} (promoção implícita)</li>
     * </ul>
     *
     * <p>A implementação reutiliza as regras de atribuição ({@code :=}) como fonte de verdade para compatibilidade.
     *
     * @param expected Tipo esperado (destino/assinatura).
     * @param actual   Tipo inferido (origem/expressão).
     * @return {@code true} se compatível; caso contrário {@code false}.
     */
    public boolean isCompatible(FavaType expected, FavaType actual) {
        if (expected == null || actual == null) return false;
        return getResultType(":=", expected, actual) != null;
    }

    /**
     * Predicado utilitário: determina se um {@link FavaType} é o tipo lógico {@code bool}.
     *
     * <p>Útil para validar condições de controlo de fluxo (ex.: {@code if} e {@code while})
     * sem espalhar comparações textuais pela análise semântica.</p>
     *
     * @param type Tipo a testar.
     * @return {@code true} se for booleano; caso contrário {@code false}.
     *
     * @see #isNumericType(FavaType)
     */
    public boolean isBooleanType(FavaType type) {
        return type == BoolType.INSTANCE;
    }

    /**
     * Predicado utilitário: determina se um {@link FavaType} pertence à hierarquia numérica da linguagem.
     *
     * <p>Na Fava, os tipos numéricos são {@code integer} e {@code real}.</p>
     *
     * @param type Tipo a testar.
     * @return {@code true} se for numérico; caso contrário {@code false}.
     *
     * @see #getPromotedNumericType(FavaType, FavaType)
     */
    public boolean isNumericType(FavaType type) {
        return type == IntegerType.INSTANCE || type == RealType.INSTANCE;
    }

    /**
     * Calcula a promoção numérica entre dois operandos.
     *
     * <p>Regra de coerção (widening):
     * se existir mistura entre {@code integer} e {@code real}, o denominador comum é {@code real}.</p>
     *
     * <p>Referência: Aho et al. (Dragon Book), Cap. 6.5.2 “Type Conversions”.</p>
     *
     * @param t1 Tipo do operando esquerdo.
     * @param t2 Tipo do operando direito.
     * @return {@link RealType} quando houver mistura com {@code real}; {@link IntegerType} quando ambos forem inteiros;
     * ou {@code null} se algum operando não for numérico.
     */
    public FavaType getPromotedNumericType(FavaType t1, FavaType t2) {
        if (!isNumericType(t1) || !isNumericType(t2)) return null;
        if (t1 == RealType.INSTANCE || t2 == RealType.INSTANCE) return RealType.INSTANCE;
        return IntegerType.INSTANCE;
    }

    /**
     * Resolve o tipo efetivo a usar na seleção de opcodes, após promoção/coerção.
     *
     * <p>Atualmente, apenas existe promoção entre {@code integer} e {@code real}. Para quaisquer outros casos,
     * assume-se que a análise semântica já garantiu compatibilidade e retorna o tipo do operando esquerdo.</p>
     *
     * @param t1 Tipo do operando esquerdo.
     * @param t2 Tipo do operando direito.
     * @return O tipo promovido (quando aplicável) ou o tipo original quando não há promoção.
     */
    public FavaType getPromotedType(FavaType t1, FavaType t2) {
        if (t1 == null || t2 == null) return null;
        if (t1 == t2) return t1;

        FavaType promotedNumeric = getPromotedNumericType(t1, t2);
        if (promotedNumeric != null) return promotedNumeric;

        return t1;
    }


//--------------------------------------------------------------------------------------------------------------------------

    /**
     * Adiciona no mapeamento interno uma nova regra unitária que infere um tipo final dadas as premissas de uma operação binária.
     *
     * @param op     Operador em texto (String).
     * @param t1     O identificador FavaType da expressão esquerda.
     * @param t2     O identificador FavaType da expressão direita.
     * @param result O tipo {@link FavaType} que será avaliado como retorno.
     */
    private static void addRule(String op, FavaType t1, FavaType t2, FavaType result) {
        binaryRules.put(new BinaryOperationKey(op, t1, t2), result);
    }

    /**
     * Regista, em bloco, múltiplas regras de operadores que partilham exatamente a mesma permissa de tipos e resultado
     * (ex: soma e subtração partilham a premissa de que (int, int) -> int).
     *
     * @param result O {@link FavaType} que será devolvido.
     * @param ops    Array de Strings contendo operadores semânticos.
     * @param t1     Identificador do tipo 1.
     * @param t2     Identificador do tipo 2.
     */
    private static void addRules(FavaType result, String[] ops, FavaType t1, FavaType t2) {
        for (String op : ops)
            addRule(op, t1, t2, result);

    }

    /**
     * Grava uma regra unária única de resolução direta no sistema.
     *
     * @param op          Operador unário.
     * @param operandType O tipo base suportado para este operador.
     * @param result      O tipo final validado ({@link FavaType}).
     */
    private static void addRule(String op, FavaType operandType, FavaType result) {
        unaryRules.put(new UnaryOperationKey(op, operandType), result);
    }


//--------------------------------------------------------------------------------------------------------------------------

    /**
     * Estrutura de dados imutável usada como Chave de Identificação O(1) de HashMaps para as operações binárias.
     * Ao agrupar os três fatores de contexto semântico, evita chaves criadas por concatenação de strings pouco otimizadas (String Concat).
     */
    private static class BinaryOperationKey {
        private final String operator;
        private final FavaType typeLeft;
        private final FavaType typeRight;

        /**
         * Cria uma chave hash unívoca para pesquisa nas TypeRules.
         *
         * @param operator  Operador a mapear.
         * @param typeLeft  Tipo operando LHS.
         * @param typeRight Tipo operando RHS.
         */
        public BinaryOperationKey(String operator, FavaType typeLeft, FavaType typeRight) {
            this.operator = operator;
            this.typeLeft = typeLeft;
            this.typeRight = typeRight;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BinaryOperationKey)) return false;
            BinaryOperationKey that = (BinaryOperationKey) o;
            return operator.equals(that.operator) &&
                    typeLeft == that.typeLeft &&
                    typeRight == that.typeRight;
        }

        @Override
        public int hashCode() {
            return Objects.hash(operator, System.identityHashCode(typeLeft), System.identityHashCode(typeRight));
        }
    }

    /**
     * Estrutura imutável análoga à BinaryOperationKey, focando os operadores de natureza unária e o seu operando central.
     */
    private static class UnaryOperationKey {
        final private String operator;
        final private FavaType operandType;

        /**
         * Cria chave indexante para unários.
         *
         * @param operator    O operador unário em si.
         * @param operandType O tipo base suportado por este unário.
         */
        UnaryOperationKey(String operator, FavaType operandType) {
            this.operator = operator;
            this.operandType = operandType;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UnaryOperationKey)) return false;
            UnaryOperationKey that = (UnaryOperationKey) o;
            return operator.equals(that.operator) &&
                    operandType == that.operandType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(operator, System.identityHashCode(operandType));
        }

    }
}
