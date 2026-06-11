package FavaCode.Semantic.Types;

import FavaCode.Semantic.FavaType;
import java.util.List;

public class FunctionType implements FavaType {

    // O tipo devolvido pela função (null representa 'void')
    private final FavaType returnType;

    // Lista ordenada dos tipos dos argumentos
    private final List<FavaType> paramTypes;

    public FunctionType(FavaType returnType, List<FavaType> paramTypes) {
        this.returnType = returnType;
        // Imutabilidade: garante que a assinatura não é alterada por acidente noutras fases
        this.paramTypes = List.copyOf(paramTypes);
    }

    public FavaType getReturnType() {
        return returnType;
    }

    public List<FavaType> getParamTypes() {
        return paramTypes;
    }

    @Override
    public String getName() {
        return "function";
    }

    @Override
    public boolean isNumeric() {
        return false; // Uma função em si não é operável matematicamente
    }
}