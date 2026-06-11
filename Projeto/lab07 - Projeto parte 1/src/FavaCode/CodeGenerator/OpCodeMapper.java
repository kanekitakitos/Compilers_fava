package FavaCode.CodeGenerator;

import FavaCode.VirtualMachine.OpCode;

/**
 * Utilitário responsável por mapear as operações genéricas encontradas na Árvore de Sintaxe (AST) 
 * para os OpCodes reais específicos e definidos pela Máquina Virtual ({@link OpCode}).
 * Por exemplo, converte o operador "+" num OpCode 'iadd', 'dadd' ou 'sconcat' com base no tipo.
 */
public class OpCodeMapper
{

    /**
     * Mapeia e devolve o OpCode da instrução "print" com base no tipo do operando atual na stack.
     *
     * @param operandType O tipo base inferido na string (ex: "integer", "real", "string").
     * @return O {@link OpCode} correspondente à instrução na VM (ex: iprint, dprint).
     * @throws RuntimeException Caso um tipo inesperado não tiver OpCode print suportado.
     */
    public static OpCode getPrint(String operandType)
    {
        if (operandType.equals("integer")) return OpCode.iprint;
        if (operandType.equals("real")) return OpCode.dprint;
        if (operandType.equals("string")) return OpCode.sprint;
        if (operandType.equals("bool")) return OpCode.bprint;


        throw new RuntimeException("Falha Crítica do CodeGen: Não foi possível mapear o operador print para " + operandType);
    }

    /**
     * Determina a instrução binária da máquina virtual associada a um operador específico e ao seu operando.
     * 
     * @param op O operador textual recolhido pelo parser (ex: "+", "and", "mod").
     * @param operandType O tipo associado aos operandos que sofrem a operação (ex: "integer").
     * @return O {@link OpCode} correspondente.
     * @throws IllegalArgumentException Para operadores inválidos na VM como '>' ou '>=', indicando ao CodeGen a sua correção por inversão de stack.
     * @throws RuntimeException Caso as regras não encontrarem mapeamento viável.
     */
    public static OpCode getBinaryOpCode(String op, String operandType)
    {
        switch (op.toLowerCase())
        {
            // --- Aritmética e Concatenação ---
            case "+":
                if (operandType.equals("integer")) return OpCode.iadd;
                if (operandType.equals("real")) return OpCode.dadd;
                if (operandType.equals("string")) return OpCode.sconcat;
                break;
            case "-":
                if (operandType.equals("integer")) return OpCode.isub;
                if (operandType.equals("real")) return OpCode.dsub;
                break;
            case "*":
                if (operandType.equals("integer")) return OpCode.imult;
                if (operandType.equals("real")) return OpCode.dmult;
                break;
            case "/":
                if (operandType.equals("integer")) return OpCode.idiv;
                if (operandType.equals("real")) return OpCode.ddiv;
                break;
            case "mod":
                if (operandType.equals("integer")) return OpCode.imod;
                break;

            case "||":
                if (operandType.equals("string")) return OpCode.sconcat;

                break;


            // --- Lógica Booleana ---
            case "and":
                if (operandType.equals("bool")) return OpCode.and;
                break;
            case "or":
                if (operandType.equals("bool")) return OpCode.or;
                break;

            // --- Comparações (Igualdade) ---
            case "=":
                if (operandType.equals("integer")) return OpCode.ieq;
                if (operandType.equals("real")) return OpCode.deq;
                if (operandType.equals("string")) return OpCode.seq;
                if (operandType.equals("bool")) return OpCode.beq;
                break;
            case "<>":
                if (operandType.equals("integer")) return OpCode.ineq;
                if (operandType.equals("real")) return OpCode.dneq;
                if (operandType.equals("string")) return OpCode.sneq;
                if (operandType.equals("bool")) return OpCode.bneq;
                break;

            // --- Comparações (Grandeza) ---
            case "<":
                if (operandType.equals("integer")) return OpCode.ilt;
                if (operandType.equals("real")) return OpCode.dlt;
                break;
            case "<=":
                if (operandType.equals("integer")) return OpCode.ileq;
                if (operandType.equals("real")) return OpCode.dleq;
                break;

            case ">":
            case ">=":
                throw new IllegalArgumentException("OpCodeMapper: A VM não tem opcodes para > e >=. O CodeGen deve inverter a visita dos operandos e usar < ou <=.");
        }

        // Se chegar aqui, é porque a Análise Semântica deixou passar uma operação ilegal
        throw new RuntimeException("Falha Crítica do CodeGen: Não foi possível mapear o operador '" + op + "' para o tipo " + operandType);
    }

    /**
     * Obtém o OpCode de cast ou promoção requerido para passar de um formato para outro 
     * (ex: integer para real com itod).
     *
     * @param fromType Tipo do valor atual da stack.
     * @param toType O tipo de destino esperado.
     * @return O {@link OpCode} indicando a conversão para efetuar a coerção, ou {@code null} se não for necessária.
     */
    public static OpCode getCastOpCode(String fromType, String toType)
    {
        if (fromType.equals("integer") && toType.equals("real")) return OpCode.itod;
        if (fromType.equals("integer") && toType.equals("string")) return OpCode.itos;
        if (fromType.equals("real") && toType.equals("string")) return OpCode.dtos;
        if (fromType.equals("bool") && toType.equals("string")) return OpCode.btos;

        return null; // Tipos iguais ou cast não suportado pela VM
    }

    /**
     * Mapeia operadores unários como a inversão aritmética '-' ou negação lógica 'not'.
     *
     * @param op String com a operação recolhida (ex: '-', 'not').
     * @param operandType Tipo sobre a qual atua a operação (ex: "bool").
     * @return {@link OpCode} de inversão/negação correspondente.
     * @throws RuntimeException Para cruzamento tipo-operador proibido (não apanhado semanticamente).
     */
    public static OpCode getUnaryOpCode(String op, String operandType)
    {
        if (op.equals("-"))
        {
            if (operandType.equals("integer")) return OpCode.iuminus;
            if (operandType.equals("real")) return OpCode.duminus;
        }
        else if (op.equals("not"))
            if (operandType.equals("bool")) return OpCode.not;


        throw new RuntimeException("Falha Crítica do CodeGen: Não foi possível mapear operador unário '" + op + "' para " + operandType);
    }
}