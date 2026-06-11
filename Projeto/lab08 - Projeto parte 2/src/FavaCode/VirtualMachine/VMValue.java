package FavaCode.VirtualMachine;

/**
 * Representa um valor genérico encapsulado em tempo de execução dentro da Máquina Virtual Fava.
 *
 * <p>Utilizado uniformemente no <i>runtime stack</i> (pilha de execução), na memória global (variáveis)
 * e no <i>constant pool</i>. Serve como um "Wrapper" que abstrai os diferentes tipos de dados suportados
 * pela linguagem (Inteiro, Real, Booleano e String), permitindo que a VM manipule todos os valores
 * de forma homogénea através de uma única classe.</p>
 *
 * <p><b>Nota de Arquitetura e Otimização (Futuro):</b><br>
 * Atualmente, cada operação da Máquina Virtual (ex: adição) instancia novos objetos desta classe
 * (ex: {@code new VMValue(left + right)}). Para uma otimização focada em alta performance (evitando
 * a sobrecarga do <i>Garbage Collector</i>), a refatoração ideal exigiria remover o uso de objetos
 * na Stack e implementar arrays primitivos separados diretamente na {@link VirtualMachine}
 * (ex: {@code int[] stackInt}, {@code double[] stackDouble}).</p>
 * <p>
 * ### Exemplo de Uso
 * <pre>{@code
 * // Instanciar e empurrar um inteiro para a stack
 * VMValue valorInteiro = new VMValue(42);
 * stack.push(valorInteiro);
 *
 * // Recuperar e usar o valor garantindo o tipo
 * VMValue topo = stack.pop();
 * int num = topo.getAsInteger(); // Retorna 42
 * }</pre>
 *
 * @see VirtualMachine
 */
public class VMValue {
    private String string;
    private Double real;
    private Integer integer;
    private Boolean bool;

    /**
     * Identificador do tipo de dado atualmente armazenado nesta instância.
     */
    Type type;

    /**
     * Enumeração restrita dos possíveis tipos de dados suportados nativamente pelo {@link VMValue}.
     */
    private enum Type {
        Integer,
        Real,
        Bool,
        String,
        Null,
    }

    public VMValue() {
        this.type = Type.Null;
    }

    /**
     * Construtor que encapsula um valor do tipo String.
     *
     * @param string O valor de texto literal a ser armazenado.
     */
    public VMValue(String string) {
        this.string = string;
        this.type = Type.String;
    }

    /**
     * Construtor que encapsula um valor numérico do tipo Real (Double).
     *
     * @param real O valor numérico de ponto flutuante.
     */
    public VMValue(Double real) {
        this.real = real;
        this.type = Type.Real;
    }

    /**
     * Construtor que encapsula um valor numérico do tipo Inteiro.
     *
     * @param integer O valor numérico inteiro (32 bits).
     */
    public VMValue(Integer integer) {
        this.integer = integer;
        this.type = Type.Integer;
    }

    /**
     * Construtor que encapsula um valor lógico Booleano.
     *
     * @param bool O valor lógico (verdadeiro/falso).
     */
    public VMValue(Boolean bool) {
        this.bool = bool;
        this.type = Type.Bool;
    }

    /**
     * Converte o valor subjacente armazenado na sua representação em String.
     * Utilizado primariamente por instruções de impressão (`sprint`, `iprint`) ou conversão (`itos`).
     *
     * @return A representação textual do valor. Retorna uma string vazia se o objeto não contiver dados (estado inválido).
     */
    @Override
    public String toString() {
        String s = "NULL";

        if (string != null)
            s = string;

        if (integer != null)
            s = integer.toString();

        if (bool != null)
            s = bool.toString();

        if (real != null)
            s = real.toString();


        return s;
    }

//*------------- GETTERS COM VALIDAÇÃO DE TIPO ---------------------------------------------------------------------

    /**
     * Devolve o valor armazenado forçando a sua extração como um Inteiro.
     *
     * @return O valor inteiro ({@link Integer}).
     * @throws RuntimeException Se o tipo interno não corresponder a {@link Type#Integer},
     *                          indicando uma quebra de segurança de tipos na VM.
     */
    public Integer getAsInteger() {
        if (this.type != Type.Integer)
            reportErrorNullType();

        return this.integer;
    }

    /**
     * Devolve o valor armazenado forçando a sua extração como um Booleano.
     *
     * @return O valor booleano ({@link Boolean}).
     * @throws RuntimeException Se o tipo interno não corresponder a {@link Type#Bool}.
     */
    public Boolean getAsBool() {
        if (this.type != Type.Bool)
            reportErrorNullType();

        return this.bool;
    }

    /**
     * Devolve o valor armazenado forçando a sua extração como uma String.
     *
     * @return O valor de texto ({@link String}).
     * @throws RuntimeException Se o tipo interno não corresponder a {@link Type#String}.
     */
    public String getAsString() {
        if (this.type != Type.String)
            reportErrorNullType();

        return this.string;
    }

    /**
     * Devolve o valor armazenado forçando a sua extração como um Real (Double).
     *
     * @return O valor numérico de precisão dupla ({@link Double}).
     * @throws RuntimeException Se o tipo interno não corresponder a {@link Type#Real}.
     */
    public Double getAsReal() {
        if (this.type != Type.Real)
            reportErrorNullType();

        return this.real;
    }


    private void reportErrorNullType() {
        System.out.println("runtime error: accessing a NULL value");
        System.exit(0);
    }
}
