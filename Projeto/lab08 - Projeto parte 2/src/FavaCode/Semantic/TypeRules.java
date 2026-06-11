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
 * @see SemanticAnalyzerVisitor
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
        addRule(unaryOpt[0], "integer", IntegerType.INSTANCE);
        addRule(unaryOpt[0], "real", RealType.INSTANCE);
        addRule(unaryOpt[1], "bool", BoolType.INSTANCE);

        // Aritmética base entre inteiros
        addRules(IntegerType.INSTANCE, new String[]{"+", "-", "*", "/", "mod"}, "integer", "integer");


        // Aritmética que resulta em Reais (Coerção Implícita e Reais com Reais)
        String[] arith = {"+", "-", "*", "/"};
        addRules(RealType.INSTANCE, arith, "integer", "real");
        addRules(RealType.INSTANCE, arith, "real", "real");

        // Operação de Concatenação Textual
        String[] striOps = {"||"};
        addRules(StringType.INSTANCE, striOps, "string", "string");
        addRules(StringType.INSTANCE, striOps, "integer", "string");
        addRules(StringType.INSTANCE, striOps, "bool", "string");
        addRules(StringType.INSTANCE, striOps, "real", "string");

        // Operações Lógicas Booleanas
        String[] boolOps = {"and", "or"};
        addRules(BoolType.INSTANCE, boolOps, "bool", "bool");

        // Operações Relacionais que produzem Booleanos
        String[] relOps = {"<", ">", "<=", ">="};
        addRules(BoolType.INSTANCE, relOps, "integer", "integer");
        addRules(BoolType.INSTANCE, relOps, "integer", "real");
        addRules(BoolType.INSTANCE, relOps, "real", "real");

        // Operações de Igualdade ou Desigualdade
        String[] eqOps = {"=", "<>"};
        addRules(BoolType.INSTANCE, eqOps, "integer", "integer");
        addRules(BoolType.INSTANCE, eqOps, "real", "integer");
        addRules(BoolType.INSTANCE, eqOps, "real", "real");
        addRules(BoolType.INSTANCE, eqOps, "bool", "bool");
        addRules(BoolType.INSTANCE, eqOps, "string", "string");

        // Regras de Atribuição a variáveis (o tipo da esquerda tem de engolir o da direita)
        String[] assignOps = {":="};
        addRules(IntegerType.INSTANCE, assignOps, "integer", "integer");
        addRules(RealType.INSTANCE, assignOps, "real", "integer"); // Coerção de int para real no Assign
        addRules(StringType.INSTANCE, assignOps, "string", "string");
        addRules(BoolType.INSTANCE, assignOps, "bool", "bool");

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
        FavaType result = binaryRules.get(new BinaryOperationKey(op, t1.getName(), t2.getName()));

        // 2. Se falhar, e não for atribuição, tenta de forma comutativa invertida
        if (result == null && !op.equals(":="))
            result = binaryRules.get(new BinaryOperationKey(op, t2.getName(), t1.getName()));

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
        return unaryRules.get(new UnaryOperationKey(op, operandType.getName()));
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
    private static void addRule(String op, String t1, String t2, FavaType result) {
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
    private static void addRules(FavaType result, String[] ops, String t1, String t2) {
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
    private static void addRule(String op, String operandType, FavaType result) {
        unaryRules.put(new UnaryOperationKey(op, operandType), result);
    }

    /**
     * Regista, em bloco, operadores unários que partilhem as mesmas assinaturas e retornos.
     *
     * @param result      O tipo base que é retornado como correto ({@link FavaType}).
     * @param ops         Conjunto de Strings (ex: unários matemáticos).
     * @param operandType Tipo de alvo das operações.
     */
    private static void addRules(FavaType result, String[] ops, String operandType) {
        for (String op : ops)
            addRule(op, operandType, result);

    }

//--------------------------------------------------------------------------------------------------------------------------

    /**
     * Estrutura de dados imutável usada como Chave de Identificação O(1) de HashMaps para as operações binárias.
     * Ao agrupar os três fatores de contexto semântico, evita chaves criadas por concatenação de strings pouco otimizadas (String Concat).
     */
    private static class BinaryOperationKey {
        private final String operator;
        private final String typeLeft;
        private final String typeRight;

        /**
         * Cria uma chave hash unívoca para pesquisa nas TypeRules.
         *
         * @param operator  Operador a mapear.
         * @param typeLeft  Tipo operando LHS.
         * @param typeRight Tipo operando RHS.
         */
        public BinaryOperationKey(String operator, String typeLeft, String typeRight) {
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
                    typeLeft.equals(that.typeLeft) &&
                    typeRight.equals(that.typeRight);
        }

        @Override
        public int hashCode() {
            return Objects.hash(operator, typeLeft, typeRight);
        }
    }

    /**
     * Estrutura imutável análoga à BinaryOperationKey, focando os operadores de natureza unária e o seu operando central.
     */
    private static class UnaryOperationKey {
        final private String operator;
        final private String operandType;

        /**
         * Cria chave indexante para unários.
         *
         * @param operator    O operador unário em si.
         * @param operandType O tipo base suportado por este unário.
         */
        UnaryOperationKey(String operator, String operandType) {
            this.operator = operator;
            this.operandType = operandType;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UnaryOperationKey)) return false;
            UnaryOperationKey that = (UnaryOperationKey) o;
            return operator.equals(that.operator) &&
                    operandType.equals(that.operandType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(operator, operandType);
        }

    }
}
