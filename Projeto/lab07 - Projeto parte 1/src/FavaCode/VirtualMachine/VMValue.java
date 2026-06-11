package FavaCode.VirtualMachine;

/**
 * Representa um valor em tempo de execução dentro da Máquina Virtual Fava.
 * É utilizado no runtime stack (pilha de execução) e no constant pool.
 * Encapsula diferentes tipos suportados pela linguagem (Integer, Real, Bool, String).
 *
 * <p><b>NOTA DE ARQUITETURA E OTIMIZAÇÃO (FUTURO):</b><br>
 * Atualmente, as operações da Máquina Virtual instanciam novos objetos desta classe
 * (ex: <code>new VMValue(left + right)</code>) para garantir a imutabilidade do Constant Pool.
 * Para uma otimização extrema focada em performance (evitando a sobrecarga do Garbage Collector),
 * a refatoração ideal exigiria remover o uso de objetos na Stack e implementar arrays
 * primitivos separados diretamente na VirtualMachine (ex: <code>int[] stackInt</code>,
 * <code>double[] stackDouble</code>, etc.).
 * </p>
 *
 * @see VirtualMachine
 */
public class VMValue
{
    private String string;
    private Double real;
    private Integer integer;
    private Boolean bool;
    
    /**
     * Tipo do valor atualmente armazenado na instância de {@link VMValue}.
     */
    Type type;
    
    /**
     * Enumeração dos possíveis tipos de dados suportados pelo {@link VMValue}.
     */
    private enum Type
    {
        Integer,
        Real,
        Bool,
        String,
    }

    /**
     * Construtor para um valor do tipo String.
     *
     * @param string O valor de texto a encapsular.
     */
    public VMValue(String string)
    {
        this.string = string;
        this.type = Type.String;
    }

    /**
     * Construtor para um valor do tipo Real (Double).
     *
     * @param real O valor numérico de ponto flutuante a encapsular.
     */
    public VMValue(Double real)
    {
        this.real = real;
        this.type = Type.Real;
    }

    /**
     * Construtor para um valor do tipo Integer.
     *
     * @param integer O valor numérico inteiro a encapsular.
     */
    public VMValue(Integer integer)
    {
        this.integer = integer;
        this.type = Type.Integer;
    }

    /**
     * Construtor para um valor do tipo Boolean (Booleano).
     *
     * @param bool O valor lógico verdadeiro/falso a encapsular.
     */
    public VMValue(Boolean bool)
    {
        this.bool = bool;
        this.type = Type.Bool;
    }

    /**
     * Converte o valor armazenado na sua representação em String.
     *
     * @return Representação em String do valor atual. Retorna string vazia se todos os valores forem nulos.
     */
    @Override public String toString()
    {
        String s = "";

        if(string != null)
            s = string;

        if(integer != null)
            s = integer.toString();

        if(bool != null)
            s = bool.toString();

        if(real != null)
            s = real.toString();

        return s;
    }

//*------------- GETTERS -----------------------------------------------------------------------------------------------------------

    /**
     * Devolve o valor armazenado convertendo-o ou validando-o como Integer.
     *
     * @return O valor inteiro ({@link Integer}).
     * @throws RuntimeException Se o tipo interno não for {@link Type#Integer}.
     */
    public Integer getAsInteger()
    {
        if(this.type != Type.Integer)
            throw new RuntimeException("Erro na VM: Esperava integer mas encontrou " + this.type);

        return this.integer;
    }

    /**
     * Devolve o valor armazenado convertendo-o ou validando-o como Boolean.
     *
     * @return O valor booleano ({@link Boolean}).
     * @throws RuntimeException Se o tipo interno não for {@link Type#Bool}.
     */
    public Boolean getAsBool()
    {
        if(this.type != Type.Bool)
            throw new RuntimeException("Erro na VM: Esperava bool mas encontrou " + this.type);

        return this.bool;
    }

    /**
     * Devolve o valor armazenado convertendo-o ou validando-o como String.
     *
     * @return O valor do tipo string ({@link String}).
     * @throws RuntimeException Se o tipo interno não for {@link Type#String}.
     */
    public String getAsString()
    {
        if(this.type != Type.String)
            throw new RuntimeException("Erro na VM: Esperava string mas encontrou " + this.type);

        return this.string;
    }

    /**
     * Devolve o valor armazenado convertendo-o ou validando-o como Real.
     *
     * @return O valor real de ponto flutuante ({@link Double}).
     * @throws RuntimeException Se o tipo interno não for {@link Type#Real}.
     */
    public Double getAsReal()
    {
        if(this.type != Type.Real)
            throw new RuntimeException("Erro na VM: Esperava real mas encontrou " + this.type);

        return this.real;
    }
}