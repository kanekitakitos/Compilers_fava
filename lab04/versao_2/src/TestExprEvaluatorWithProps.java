/***
 * Excerpted from "The Definitive ANTLR 4 Reference",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/tpantlr2 for more book information.
***/
import Expr.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.FileInputStream;
import java.io.InputStream;

public class TestExprEvaluatorWithProps {
    /**
     * Diferença em relação aos outros métodos de avaliação:
     *
     * 1. Visitor (TestExprVisitor.java):
     *    - O controle do fluxo de visita é manual (você deve chamar visit() para os filhos).
     *    - Os valores calculados são retornados diretamente pelos métodos (ex: public Double visitAdd(...)).
     *    - É mais intuitivo para quem está acostumado com chamadas de função recursivas.
     *
     * 2. Listener com Stack (TestExperEvaluator.java):
     *    - O ParseTreeWalker percorre a árvore automaticamente (profundidade-primeiro).
     *    - Usa uma pilha (Stack) global na classe para armazenar resultados intermediários.
     *    - Os operandos são empilhados nas folhas e desempilhados/processados nos nós pais.
     *    - Risco: Se a lógica falhar, a pilha pode ficar dessincronizada (sobrar ou faltar itens).
     *
     * 3. Listener com ParseTreeProperty (ESTE ARQUIVO):
     *    - Assim como o Listener com Stack, o Walker percorre a árvore automaticamente.
     *    - Em vez de uma pilha, usa um Mapa (ParseTreeProperty) para associar um valor a cada nó específico da árvore.
     *    - É mais seguro que a Stack porque o valor fica "anotado" no nó da árvore.
     *    - Funciona como se estivéssemos decorando a árvore de sintaxe com atributos (Attribute Grammar).
     */
    public static class EvaluatorWithProps extends ExprBaseListener {
        /** 
         * ParseTreeProperty é essencialmente um Map<ParseTree, Double>.
         * Ele permite associar um objeto (neste caso, o valor Double calculado) a um nó da árvore de análise.
         */
        ParseTreeProperty<Double> values = new ParseTreeProperty<Double>();

        /**
         * Método chamado quando o analisador termina de processar a regra 'prog'.
         * Regra: prog : expr EOF ;
         * 
         * Aqui, simplesmente pegamos o valor calculado da expressão filha (expr) 
         * e definimos como o valor deste nó raiz (prog).
         */
        @Override
        public void exitProg(ExprParser.ProgContext ctx) {
            setValue(ctx, getValue(ctx.expr()));
        }

        /**
         * Método chamado quando saímos de uma expressão de potência (ex: 2^3).
         * Regra: expr (POW) expr
         *
         * O que são ctx.expr(0) e ctx.expr(1)?
         * A regra gramatical define dois elementos 'expr': o da base e o do expoente.
         * ctx.expr(0) -> Retorna o contexto do primeiro 'expr' que aparece na regra (filho da esquerda).
         * ctx.expr(1) -> Retorna o contexto do segundo 'expr' que aparece na regra (filho da direita).
         *
         * Se a regra tivesse mais expressões (ex: expr ',' expr ',' expr), teríamos expr(2), expr(3), etc.
         * O índice é baseado na ordem de aparição na definição da gramática.
         */
        @Override
        public void exitPow(ExprParser.PowContext ctx) {
            double left = getValue(ctx.expr(0));  // Recupera o valor anotado no nó filho da esquerda
            double right = getValue(ctx.expr(1)); // Recupera o valor anotado no nó filho da direita
            setValue(ctx, Math.pow(left, right)); // Calcula a potência e anota no nó atual (PowContext)
        }

        /**
         * Método para multiplicação e divisão.
         * Regra: expr op=(TIMES|DIV) expr
         */
        @Override
        public void exitTimesDiv(ExprParser.TimesDivContext ctx) {
            double left = getValue(ctx.expr(0));
            double right = getValue(ctx.expr(1));
            
            // ctx.op contém o token do operador que foi casado.
            // Verificamos se o tipo do token é TIMES (*) ou DIV (/)
            if ( ctx.op.getType() == ExprParser.TIMES ) {
                setValue(ctx, left * right);
            } else {
                setValue(ctx, left / right);
            }
        }

        /**
         * Método para soma e subtração.
         * Regra: expr op=(PLUS|MINUS) expr
         */
        @Override
        public void exitPlusMinus(ExprParser.PlusMinusContext ctx) {
            double left = getValue(ctx.expr(0));
            double right = getValue(ctx.expr(1));
            
            if ( ctx.op.getType() == ExprParser.PLUS ) {
                setValue(ctx, left + right);
            } else {
                setValue(ctx, left - right);
            }
        }

        /**
         * Regra: LPAREN expr RPAREN
         * Como os parênteses servem apenas para agrupamento na precedência e não alteram o valor,
         * simplesmente repassamos o valor da expressão interna para o nó dos parênteses.
         * 
         * ctx.expr() (sem índice) é usado quando existe apenas um filho daquele tipo na regra.
         */
        @Override
        public void exitParentesis(ExprParser.ParentesisContext ctx) {
            setValue(ctx, getValue(ctx.expr()));
        }

        /**
         * Regra: ID
         * Representa uma variável. ctx.ID().getText() retornaria o nome (ex: "x").
         * Como não implementamos uma tabela de símbolos para guardar valores de variáveis,
         * retornamos 0.0 como um valor padrão (placeholder).
         */
        @Override
        public void exitId(ExprParser.IdContext ctx) {
            setValue(ctx, 0.0);
        }

        /**
         * Regra: NUMBER
         * Esta é uma folha da árvore (nó terminal).
         * É aqui que a conversão de texto para número acontece.
         * O valor é armazenado neste nó e subirá a árvore conforme as regras acima forem processadas.
         */
        @Override
        public void exitNumber(ExprParser.NumberContext ctx) {
            String text = ctx.NUMBER().getText();
            setValue(ctx, Double.parseDouble(text));
        }

        // Métodos auxiliares para interagir com o ParseTreeProperty
        public void setValue(ParseTree node, double value) { values.put(node, value); }
        public double getValue(ParseTree node) { return values.get(node); }
    }

    public static void main(String[] args) throws Exception {
        String inputFile = null;
        if ( args.length>0 ) inputFile = args[0];
        InputStream is = System.in;
        try {
            if (inputFile != null) is = new FileInputStream(inputFile);
            CharStream input = CharStreams.fromStream(is);
            ExprLexer lexer = new ExprLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            ExprParser parser = new ExprParser(tokens);
            
            // Inicia o parsing na regra raiz 'prog', gerando a árvore
            ParseTree tree = parser.prog(); 
            
            // O ParseTreeWalker é o motor que percorre a árvore
            ParseTreeWalker walker = new ParseTreeWalker();
            
            // Instanciamos nosso Listener que contém a lógica de anotação
            EvaluatorWithProps evalProp = new EvaluatorWithProps();
            
            // O walker percorre a 'tree' e chama os métodos exit... do 'evalProp'
            walker.walk(evalProp, tree);
            
            // Ao final da caminhada, o resultado final estará anotado na raiz da árvore
            System.out.println("properties result = " + evalProp.getValue(tree));
        }
        catch (java.io.IOException e) {
                System.out.println(e);
        }
    }
}
