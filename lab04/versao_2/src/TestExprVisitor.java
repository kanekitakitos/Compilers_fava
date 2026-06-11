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

public class TestExprVisitor {
    // a4 -visitor Expr.g4
    /** Visitor "calculator" */
    public static class EvalVisitor extends ExprBaseVisitor<Double>
    {
        @Override
        public Double visitPow(ExprParser.PowContext ctx)
        {
            return Math.pow(visit(ctx.expr(0)), visit(ctx.expr(1)));
        }

        @Override
        public Double visitPlusMinus(ExprParser.PlusMinusContext ctx)
        {
            if (ctx.op.getType() == ExprParser.PLUS) {
                return visit(ctx.expr(0)) + visit(ctx.expr(1));
            }
            return visit(ctx.expr(0)) - visit(ctx.expr(1));
        }

        @Override
        public Double visitTimesDiv(ExprParser.TimesDivContext ctx)
        {
            if (ctx.op.getType() == ExprParser.TIMES) {
                return visit(ctx.expr(0)) * visit(ctx.expr(1));
            }
            return visit(ctx.expr(0)) / visit(ctx.expr(1));
        }

        @Override
        public Double visitParentesis(ExprParser.ParentesisContext ctx) {
            return visit(ctx.expr());
        }

        @Override
        public Double visitId(ExprParser.IdContext ctx) {
            return 0.0;
        }

        @Override
        public Double visitNumber(ExprParser.NumberContext ctx) {
            return Double.parseDouble(ctx.NUMBER().getText());
        }
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
            ParseTree tree = parser.prog();
            EvalVisitor evalVisitor = new EvalVisitor();
            double result = evalVisitor.visit(tree);
            System.out.println("visitor result = " + result);
        }
        catch (java.io.IOException e) {
            System.out.println(e);
        }
    }
}
