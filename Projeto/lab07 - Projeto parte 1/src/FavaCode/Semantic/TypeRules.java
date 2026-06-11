package FavaCode.Semantic;

import FavaCode.Semantic.Types.BoolType;
import FavaCode.Semantic.Types.IntegerType;
import FavaCode.Semantic.Types.RealType;
import FavaCode.Semantic.Types.StringType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Define e avalia as regras de inferência de tipos suportadas pela linguagem Fava.
 * Gere operações binárias e unárias verificando a compatibilidade entre os tipos de dados e
 * devolvendo o tipo de resultado correspondente.
 */
public class TypeRules {
    
    /** Mapa que associa as chaves de operações binárias a um tipo resultante. */
    private static final Map<BinaryOperationKey, FavaType> binaryRules = new HashMap<>();
    
    /** Mapa que associa as chaves de operações unárias a um tipo resultante. */
    private static final Map<UnaryOperationKey, FavaType> unaryRules = new HashMap<>();

    static {
        // Regras Unarias apra certos tipos
        String[] unaryOpt = {"-","not"};
        addRule(unaryOpt[0],"integer", IntegerType.INSTANCE);
        addRule(unaryOpt[0],"real", RealType.INSTANCE);
        addRule(unaryOpt[1],"bool", BoolType.INSTANCE);

        // Aritmética básica entre Integers
        addRules(IntegerType.INSTANCE, new String[]{"+", "-", "*", "/", "mod"}, "integer", "integer");


        String[] arith = {"+", "-", "*", "/"};
        addRules(RealType.INSTANCE, arith, "integer", "real");
        addRules(RealType.INSTANCE, arith, "real", "real");

        // Concatenar
        String[] striOps = {"||"};
        addRules(StringType.INSTANCE, striOps ,"string","string");
        addRules(StringType.INSTANCE, striOps ,"integer","string");
        addRules(StringType.INSTANCE, striOps ,"bool","string");
        addRules(StringType.INSTANCE, striOps ,"real","string");

        // Bool
        String[] boolOps = {"and","or"};
        addRules(BoolType.INSTANCE, boolOps ,"bool","bool");

        // Comparações que resultam em Bool
        String[] relOps = {"<", ">", "<=", ">="};
        addRules(BoolType.INSTANCE, relOps, "integer", "integer");
        addRules(BoolType.INSTANCE, relOps, "integer", "real");
        addRules(BoolType.INSTANCE, relOps, "real", "real");

        // Igualdade
        String[] eqOps = {"=", "<>"};
        addRules(BoolType.INSTANCE, eqOps, "integer", "integer");
        addRules(BoolType.INSTANCE, eqOps, "real", "integer");
        addRules(BoolType.INSTANCE, eqOps, "real", "real");
        addRules(BoolType.INSTANCE, eqOps, "bool", "bool");
        addRules(BoolType.INSTANCE, eqOps, "string", "string");

    }

    /**
     * Devolve o tipo resultante de uma operação binária aplicada em dois tipos de dados conhecidos.
     * Tenta resolver em ambas as direções, pois operadores como concatenação ou somas podem ser inversamente compatíveis (ex: real + int ou int + real).
     *
     * @param op A string representando o operador (ex: "+", "-", "<=").
     * @param t1 O tipo da expressão da esquerda ({@link FavaType}).
     * @param t2 O tipo da expressão da direita ({@link FavaType}).
     * @return O {@link FavaType} resultante, ou {@code null} se a operação for ilegal/incompatível.
     */
    public FavaType getResultType(String op, FavaType t1, FavaType t2)
    {

        // 1. Tenta a ordem normal (ex: int + string)
        FavaType result = binaryRules.get(new BinaryOperationKey(op, t1.getName(), t2.getName()));


        // 2. Se não encontrar, tenta a ordem invertida (ex: string + int)
        if (result == null)
            result = binaryRules.get(new BinaryOperationKey(op, t2.getName(), t1.getName()));


        return result;
    }

    /**
     * Devolve o tipo resultante de uma operação unária (ex: '-', 'not').
     *
     * @param op A string representando o operador.
     * @param operandType O tipo ({@link FavaType}) do operando sobre o qual a operação será efetuada.
     * @return O {@link FavaType} resultante, ou {@code null} se a operação for ilegal.
     */
    public FavaType getResultType(String op, FavaType operandType)
    {
        return unaryRules.get(new UnaryOperationKey(op, operandType.getName()));
    }

//--------------------------------------------------------------------------------------------------------------------------

    /**
     * Adiciona no mapeamento interno uma nova regra que infere um tipo final dadas as premissas de uma operação binária.
     *
     * @param op O operador (String).
     * @param t1 O identificador de tipo (nome em String) da esquerda.
     * @param t2 O identificador de tipo da direita.
     * @param result O objeto em formato {@link FavaType} que será inferido como tipo de retorno.
     */
    private static void addRule(String op, String t1, String t2, FavaType result)
    {
        binaryRules.put(new BinaryOperationKey(op, t1, t2), result);
    }

    /**
     * Regista em bloco múltiplas regras de operadores que partilham a mesma premissa (ex: soma e subtração têm os mesmos tipos de operando).
     *
     * @param result O {@link FavaType} devolvido por qualquer um dos operadores na array.
     * @param ops Array de Strings contendo operadores.
     * @param t1 Nome em string do primeiro tipo de operando.
     * @param t2 Nome em string do segundo tipo de operando.
     */
    private static void addRules(FavaType result, String[] ops, String t1, String t2)
    {
        for (String op : ops)
            addRule(op, t1, t2, result);

    }

    /**
     * Adiciona no mapa interno uma regra de operação unária.
     *
     * @param op O operador unário (String).
     * @param operandType Nome em string do operando.
     * @param result O tipo {@link FavaType} devolvido caso a regra seja validada.
     */
    private static void addRule(String op, String operandType, FavaType result)
    {
        unaryRules.put(new UnaryOperationKey(op, operandType), result);
    }

    /**
     * Regista em bloco múltiplas operações unárias partilhando o mesmo tipo de operando e devolvendo o mesmo tipo de resultado.
     *
     * @param result {@link FavaType} inferido para retorno.
     * @param ops A lista de operadores compatíveis com a regra.
     * @param operandType String a designar o tipo do argumento.
     */
    private static void addRules(FavaType result, String[] ops, String operandType)
    {
        for (String op : ops)
            addRule(op, operandType, result);

    }

//--------------------------------------------------------------------------------------------------------------------------

    /**
     * Classe utilitária, usada para gerar chaves de indexação unívocas no HashMap com base nos tipos e operador (binários).
     */
    private static class BinaryOperationKey
    {
        private final String operator;
        private final String typeLeft;
        private final String typeRight;

        /**
         * Criação de chave de indexação.
         *
         * @param operator Operador a mapear.
         * @param typeLeft Tipo na esquerda do operador.
         * @param typeRight Tipo na direita.
         */
        public BinaryOperationKey(String operator, String typeLeft, String typeRight) {
            this.operator = operator;
            this.typeLeft = typeLeft;
            this.typeRight = typeRight;
        }

        // Obrigatório para o HashMap funcionar corretamente
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
     * Subclasse de chave de indexação em HashMaps, servindo a indexação de operadores unários.
     */
    private static class UnaryOperationKey
    {
        final private String operator;
        final private String operandType;

        /**
         * Criação de chave de mapeamento para unários.
         *
         * @param operator O operador unário.
         * @param operandType Tipo de dados do argumento.
         */
        UnaryOperationKey(String operator, String operandType)
        {
            this.operator = operator;
            this.operandType = operandType;
        }


        // Obrigatório para o HashMap funcionar corretamente
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