package FavaCode.VirtualMachine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Classe responsável por inicializar a máquina virtual e carregar os ficheiros binários (bytecodes).
 * Funciona como o ponto de entrada principal (ou launcher) para iniciar a execução do programa compilado.
 *
 * @see VirtualMachine
 */
public class FavaVM {

    /**
     * Cria uma nova instância de FavaVM. O construtor encarrega-se de:
     * 1. Carregar os bytecodes a partir do ficheiro indicado.
     * 2. Exibir o dump de bytecodes caso o "trace" esteja ativado.
     * 3. Instanciar e iniciar a execução da {@link VirtualMachine}.
     *
     * @param filename Caminho para o ficheiro binário que contém as instruções.
     * @param trace    Se verdadeiro, exibe as operações passo-a-passo (disassembler e estado da pilha) no terminal.
     */
    public FavaVM(String filename, boolean trace) {
        try {
            byte[] bytecodes = loadBytecodes(filename);
            if (trace)
            {
                System.out.println("Bytecodes");
                dumpBytecodes(bytecodes);
            }
            VirtualMachine VirtualMachine = new VirtualMachine(bytecodes, trace);
            VirtualMachine.run();
        }
        catch (java.io.IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Exibe os bytecodes carregados em formato hexadecimal para facilitar debug e análise.
     *
     * @param bytecodes O array de bytes que contém as instruções em código de máquina.
     */
    public static void dumpBytecodes(byte [] bytecodes) {
        /*
        for (byte b : bytecodes)
            System.out.print(b + " ");
        System.out.println();
        */
        StringBuilder s = new StringBuilder();
        for (byte b : bytecodes)
            s.append(String.format("%02X ", b));
        System.out.println(s);
    }

    /**
     * Carrega todo o conteúdo do ficheiro de bytecodes especificado para a memória
     * e devolve-o como um array de bytes.
     *
     * @param filename Caminho do ficheiro a ler.
     * @return Um array com todos os bytes do ficheiro, ou null em caso de exceção de I/O tratada localmente.
     * @throws IOException Se existir um problema ao abrir ou ler o ficheiro (se propagada).
     */
    public static byte[] loadBytecodes(String filename) throws IOException {
        try {
            File file = new File(filename);
            byte [] bytecodes = new byte[(int) file.length()];
            try(FileInputStream fis = new FileInputStream(file))
            {
                fis.read(bytecodes);
            }
            return bytecodes;
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

}