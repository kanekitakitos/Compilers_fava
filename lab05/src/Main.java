import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Main {

    static BufferedReader reader;
    static int currentToken;
    
    static int line = 1;
    static StringBuilder lineBuffer = new StringBuilder(); 

    public static void main(String[] args) {
        String inputFile = null;
        if (args.length > 0) inputFile = args[0];

        InputStream is = System.in;
        try {
            if (inputFile != null) is = new FileInputStream(inputFile);

            reader = new BufferedReader(new InputStreamReader(is));

            System.out.println("=== Início da Análise ===");
            nextToken(); // Lê o primeiro token

            try {
                String resultado = nonTerminal_S();
                
                // Se S terminou, mas ainda tem tokens (não é EOF), é erro
                if (currentToken != -1) {
                    error("Conteúdo extra após o fim da derivação. Esperado fim de arquivo.");
                }
                
                System.out.println("\n=== Análise Concluída com Sucesso! ===");
                System.out.println("Resultado Final: " + resultado);

            } catch (RuntimeException e) {
                // O erro já foi impresso detalhadamente no método error()
            }

            if (inputFile != null) {
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int readNextChar() throws IOException {
        int c = reader.read();
        if (c == -1) return -1;

        char ch = (char) c;
        if (ch == '\n') {
            line++;
            lineBuffer.setLength(0); 
        } else if (ch != '\r') {
            lineBuffer.append(ch);
        }
        return c;
    }

    private static void nextToken() throws IOException {
        currentToken = readNextChar();
        while (currentToken == ' ' || currentToken == '\t' || currentToken == '\n' || currentToken == '\r')
            currentToken = readNextChar();
    }

    // Método de erro formatado especificamente para mostrar o token e o contexto
    private static void error(String msg) {
        // Tenta ler o resto da linha para mostrar o contexto completo
        try {
            while (true) {
                int c = reader.read();
                if (c == -1 || c == '\n') break;
                if (c != '\r') lineBuffer.append((char)c);
            }
        } catch (IOException e) {}

        String tokenStr = (currentToken == -1) ? "EOF" : String.valueOf((char)currentToken);

        System.err.println("\n>>> ERRO na Linha " + line + ": " + msg);
        System.err.println("    Token Problemático: '" + tokenStr + "'");
        System.err.println("    Contexto: " + lineBuffer.toString());
        
        throw new RuntimeException("Parada por erro.");
    }

    private static void match(char expected) {
        if (currentToken == expected) {
            try {
                nextToken();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            error("Esperado '" + expected + "', mas encontrado '" + (char)currentToken + "'");
        }
    }

    // Regra: S -> a | ( L )
    private static String nonTerminal_S() {
        if (currentToken == 'a') {
            System.out.println("Regra S_1: S -> a");
            match('a');
            return "a";
            
        } else if (currentToken == '(') {
            System.out.println("Regra S_2: S -> ( L )");
            match('(');
            String l = nonTerminal_L();
            match(')');
            return "(" + l + ")";
            
        } else {
            error("Esperado 'a' ou '(', mas encontrado '" + (char)currentToken + "'");
            return null;
        }
    }

    // Regra: L -> S M
    private static String nonTerminal_L() {
        System.out.println("Regra L_1: L -> S M");
        String s = nonTerminal_S();
        String m = nonTerminal_M();
        return s + m;
    }

    // Regra: M -> , S M | ε
    private static String nonTerminal_M() {
        if (currentToken == ',') {
            System.out.println("Regra M_1: M -> , S M");
            match(',');
            String s = nonTerminal_S();
            String m = nonTerminal_M();
            return "," + s + m;
        }
        
        System.out.println("Regra M_2: M -> ε");
        return "";
    }
}