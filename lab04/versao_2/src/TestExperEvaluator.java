import Expr.ExprBaseListener;
import Expr.ExprLexer;
import Expr.ExprParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Stack;

public class TestExperEvaluator
{
    public static class Evaluator extends ExprBaseListener
    {
        Stack<Double> stack = new Stack<>();

        public void exitPow(ExprParser.PowContext ctx)
        {
            double right = stack.pop();
            double left = stack.pop();
            stack.push( Math.pow(left,right) );
        }

        public void exitPlusMinus(ExprParser.PlusMinusContext ctx)
        {
            double right = stack.pop();
            double left = stack.pop();

            boolean isPlus = ctx.op.getText().equals("+");
            if (isPlus)
                stack.push( left + right );
            else
                stack.push( left - right );
        }

        public void exitTimesDiv(ExprParser.TimesDivContext ctx)
        {

            double right = stack.pop();
            double left = stack.pop();
            boolean isTimes = ctx.op.getText().equals("*");
            if (isTimes)
                stack.push( left * right );
            else
                stack.push( left / right );
        }

        public void exitId(ExprParser.IdContext ctx)
        {
            stack.push( Double.valueOf(ctx.ID().getText()) );
        }

        public void exitNumber(ExprParser.NumberContext ctx)
        {
            stack.push( Double.valueOf(ctx.NUMBER().getText()) );
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
                ParseTreeWalker walker = new ParseTreeWalker();
                Evaluator eval = new Evaluator();
                walker.walk(eval, tree);
                System.out.println("stack result = " + eval.stack.pop());
            }
            catch (java.io.IOException e) {
                System.out.println(e);
            }
        }
}
