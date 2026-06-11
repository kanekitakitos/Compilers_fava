package FavaCode.CodeGenerator;

import FavaCode.Semantic.FavaType;
import FavaCode.VirtualMachine.OpCode;
import FavaCode.Semantic.Types.*;

/**
 * Utilitário responsável por mapear as operações genéricas encontradas na Árvore de Sintaxe (AST)
 * para os OpCodes reais específicos e definidos pela Máquina Virtual ({@link OpCode}).
 *
 * <p>Por exemplo, converte o operador "+" num OpCode `iadd`, `dadd` ou `sconcat` com base no tipo
 * inferido durante a fase Semântica. Centraliza as decisões de seleção de OpCodes,
 * garantindo que o `CodeGen` permaneça independente das nuances de cada instrução da VM.</p>
 * <p>
 * ### Exemplo de Uso
 * <pre>{@code
 * // Durante a visita a um operador de adição na AST:
 * OpCode mappedCode = OpCodeMapper.getBinaryOpCode("+", IntegerType.INSTANCE);
 * // mappedCode será OpCode.iadd
 *
 * emit(mappedCode);
 * }</pre>
 *
 * @see CodeGen
 * @see OpCode
 */
public class OpCodeMapper {


    /**
     * Mapeia a instrução genérica "print" para o OpCode específico com base no tipo do operando atual na stack.
     *
     * @param operandType O tipo inferido do valor a imprimir (ex: {@link IntegerType}, {@link RealType}, {@link StringType}, {@link BoolType}).
     * @return O {@link OpCode} correspondente à instrução na VM (ex: `iprint`, `dprint`).
     * @throws RuntimeException Caso um tipo inesperado não tenha OpCode de impressão suportado.
     */
    public static OpCode getPrint(FavaType operandType) {
        if (operandType == IntegerType.INSTANCE) return OpCode.iprint;
        if (operandType == RealType.INSTANCE) return OpCode.dprint;
        if (operandType == StringType.INSTANCE) return OpCode.sprint;
        if (operandType == BoolType.INSTANCE) return OpCode.bprint;

        throw new RuntimeException("Falha Crítica do CodeGen: Não foi possível mapear o operador print para " + (operandType != null ? operandType.getName() : "null"));
    }

    /**
     * Determina a instrução binária da máquina virtual associada a um operador específico e ao seu operando.
     *
     * @param op          O símbolo do operador textual recolhido pelo parser (ex: "+", "and", "mod").
     * @param operandType O tipo base inferido dos operandos que sofrem a operação (ex: {@link IntegerType}).
     * @return O {@link OpCode} correspondente.
     * @throws IllegalArgumentException Para operadores não suportados nativamente ('>' ou '>='), indicando ao CodeGen a sua correção por inversão de stack.
     * @throws RuntimeException         Caso as regras não encontrem um mapeamento viável, indicando uma falha da Análise Semântica.
     */
    public static OpCode getBinaryOpCode(String op, FavaType operandType) {
        switch (op.toLowerCase()) {
            // --- Aritmética e Concatenação ---
            case "+":
                if (operandType == IntegerType.INSTANCE) return OpCode.iadd;
                if (operandType == RealType.INSTANCE) return OpCode.dadd;
                if (operandType == StringType.INSTANCE) return OpCode.sconcat;
                break;
            case "-":
                if (operandType == IntegerType.INSTANCE) return OpCode.isub;
                if (operandType == RealType.INSTANCE) return OpCode.dsub;
                break;
            case "*":
                if (operandType == IntegerType.INSTANCE) return OpCode.imult;
                if (operandType == RealType.INSTANCE) return OpCode.dmult;
                break;
            case "/":
                if (operandType == IntegerType.INSTANCE) return OpCode.idiv;
                if (operandType == RealType.INSTANCE) return OpCode.ddiv;
                break;
            case "mod":
                if (operandType == IntegerType.INSTANCE) return OpCode.imod;
                break;

            case "||":
                if (operandType == StringType.INSTANCE) return OpCode.sconcat;
                break;

            // --- Lógica Booleana ---
            case "and":
                if (operandType == BoolType.INSTANCE) return OpCode.and;
                break;
            case "or":
                if (operandType == BoolType.INSTANCE) return OpCode.or;
                break;

            // --- Comparações (Igualdade) ---
            case "=":
                if (operandType == IntegerType.INSTANCE) return OpCode.ieq;
                if (operandType == RealType.INSTANCE) return OpCode.deq;
                if (operandType == StringType.INSTANCE) return OpCode.seq;
                if (operandType == BoolType.INSTANCE) return OpCode.beq;
                break;
            case "<>":
                if (operandType == IntegerType.INSTANCE) return OpCode.ineq;
                if (operandType == RealType.INSTANCE) return OpCode.dneq;
                if (operandType == StringType.INSTANCE) return OpCode.sneq;
                if (operandType == BoolType.INSTANCE) return OpCode.bneq;
                break;

            // --- Comparações (Grandeza) ---
            case "<":
                if (operandType == IntegerType.INSTANCE) return OpCode.ilt;
                if (operandType == RealType.INSTANCE) return OpCode.dlt;
                break;
            case "<=":
                if (operandType == IntegerType.INSTANCE) return OpCode.ileq;
                if (operandType == RealType.INSTANCE) return OpCode.dleq;
                break;

            case ">":
            case ">=":
                throw new IllegalArgumentException("OpCodeMapper: A VM não tem opcodes nativos para > e >=. O CodeGen deve inverter a visita dos operandos e usar < ou <=.");
        }

        // Se chegar aqui, é porque a Análise Semântica deixou passar uma operação ilegal
        throw new RuntimeException("Falha Crítica do CodeGen: Não foi possível mapear o operador '" + op + "' para o tipo " + (operandType != null ? operandType.getName() : "null"));
    }

    /**
     * Obtém o OpCode de conversão de tipo (cast) ou promoção implícita requerido para passar de um formato
     * para outro na Máquina Virtual (ex: de `integer` para `real` com `itod`).
     *
     * @param fromType Tipo de dados do valor atual no topo da stack.
     * @param toType   O tipo de destino desejado (promovido).
     * @return O {@link OpCode} de coerção necessário, ou {@code null} se os tipos já forem compatíveis ou o cast não for suportado.
     */
    public static OpCode getCastOpCode(FavaType fromType, FavaType toType) {
        if (fromType == null || toType == null) return null;
        if (fromType == toType) return null;

        if (fromType == IntegerType.INSTANCE && toType == RealType.INSTANCE) return OpCode.itod;
        if (fromType == IntegerType.INSTANCE && toType == StringType.INSTANCE) return OpCode.itos;
        if (fromType == RealType.INSTANCE && toType == StringType.INSTANCE) return OpCode.dtos;
        if (fromType == BoolType.INSTANCE && toType == StringType.INSTANCE) return OpCode.btos;

        return null; // Tipos iguais ou cast não suportado/necessário pela VM
    }

    /**
     * Mapeia operadores unários (ex: inversão aritmética '-' ou negação lógica 'not').
     *
     * @param op          String com a operação recolhida do Parser (ex: '-', 'not').
     * @param operandType O tipo base sobre o qual atua a operação (ex: {@link BoolType}).
     * @return O {@link OpCode} de inversão ou negação correspondente.
     * @throws RuntimeException Para cruzamento entre tipo e operador não mapeado.
     */
    public static OpCode getUnaryOpCode(String op, FavaType operandType) {
        if (op.equals("-")) {
            if (operandType == IntegerType.INSTANCE) return OpCode.iuminus;
            if (operandType == RealType.INSTANCE) return OpCode.duminus;
        } else if (op.equals("not"))
            if (operandType == BoolType.INSTANCE) return OpCode.not;

        throw new RuntimeException("Falha Crítica do CodeGen: Não foi possível mapear operador unário '" + op + "' para " + (operandType != null ? operandType.getName() : "null"));
    }
}
