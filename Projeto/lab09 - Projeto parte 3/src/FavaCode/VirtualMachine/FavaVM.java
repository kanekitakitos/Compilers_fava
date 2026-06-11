package FavaCode.VirtualMachine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Classe de arranque (Launcher) e carregamento da Máquina Virtual Fava.
 * Responsável por inicializar a execução interpretada, lendo o ficheiro binário (`.bc`)
 * gerado pelo compilador e transitar o array de bytecodes para o ciclo de vida da {@link VirtualMachine}.
 *
 * <p>Funciona como a ponte entre o sistema de ficheiros (onde reside o código compilado)
 * e o motor de execução (Virtual Machine). Inclui suporte para debug (Trace Mode) que imprime
 * os bytecodes carregados antes de iniciar a execução.</p>
 * <p>
 * ### Exemplo de Uso
 * <pre>{@code
 * // Instancia e roda um ficheiro compilado "programa.bc", ativando o log de trace da stack.
 * FavaVM vm = new FavaVM("programa.bc", true);
 * }</pre>
 *
 * @see VirtualMachine
 * @see FavaCode.FavaCompileAndRun
 */
public class FavaVM {

    /**
     * O construtor orquestra o carregamento e arranque automático do programa na VM.
     * <ol>
     *   <li>Lê os bytecodes nativos contidos no ficheiro fornecido.</li>
     *   <li>Verifica se a verbosidade de Trace está ativada e executa o dump de hexadecimais ({@link #dumpBytecodes(byte[])}).</li>
     *   <li>Inicializa e despacha os bytecodes para a nova instância da {@link VirtualMachine}.</li>
     * </ol>
     *
     * @param filename Caminho (Path) físico para o ficheiro compilado contendo a stream de bytecodes (extensão recomendada `.bc`).
     * @param trace    Ativa logs detalhados que mostram as operações disassembladas, evolução da stack LIFO e bytecodes puramente lidos do disco.
     */
    public FavaVM(String filename, boolean trace) {
        try {
            byte[] bytecodes = loadBytecodes(filename);
            if (trace) {
                System.out.println("Bytecodes");
                dumpBytecodes(bytecodes);
            }
            VirtualMachine VirtualMachine = new VirtualMachine(bytecodes, trace);
            VirtualMachine.run();
        } catch (java.io.IOException e) {
            System.out.println("FavaVM Erro fatal de leitura: " + e.getMessage());
        }
    }

    /**
     * Imprime os bytecodes puramente lidos na consola sob a representação hexadecimal formatada (ex: `0A 1F 00`).
     * Útil na inspeção de integridade ou validação do Constant Pool gerado no ficheiro (Debugging Visual).
     *
     * @param bytecodes O array de dados obtidos na leitura `loadBytecodes`.
     */
    public static void dumpBytecodes(byte[] bytecodes) {
        StringBuilder s = new StringBuilder();
        for (byte b : bytecodes)
            s.append(String.format("%02X ", b));
        System.out.println(s);
    }

    /**
     * Processa a extração contígua e integral do conteúdo residente no ficheiro compilado Fava,
     * vertendo para o formato genérico binário (`byte[]`) alocado de antemão pela verificação prévia do tamanho (length).
     *
     * @param filename O referencial do ficheiro de onde serão sugadas as ordens compiladas.
     * @return O pacote `byte[]` com o compilado preparado ou {@code null} perante aborto sistémico na leitura.
     * @throws IOException Quando problemas de restrição em SO, caminho defeituoso ou I/O impedem o manuseio.
     */
    public static byte[] loadBytecodes(String filename) throws IOException {
        try {
            File file = new File(filename);
            byte[] bytecodes = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(bytecodes);
            }
            return bytecodes;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
