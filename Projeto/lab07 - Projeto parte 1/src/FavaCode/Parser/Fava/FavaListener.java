// Generated from C:/Users/brand/OneDrive/Documentos/GitHub/Compiladores/Projeto/lab08 - Projeto parte 2/src/Antlr4/Fava.g4 by ANTLR 4.13.2
package FavaCode.Parser.Fava;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link FavaParser}.
 */
public interface FavaListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link FavaParser#prog}.
	 * @param ctx the parse tree
	 */
	void enterProg(FavaParser.ProgContext ctx);
	/**
	 * Exit a parse tree produced by {@link FavaParser#prog}.
	 * @param ctx the parse tree
	 */
	void exitProg(FavaParser.ProgContext ctx);
	/**
	 * Enter a parse tree produced by the {@code PrintStat}
	 * labeled alternative in {@link FavaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterPrintStat(FavaParser.PrintStatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code PrintStat}
	 * labeled alternative in {@link FavaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitPrintStat(FavaParser.PrintStatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code VarDeclarationStat}
	 * labeled alternative in {@link FavaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterVarDeclarationStat(FavaParser.VarDeclarationStatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code VarDeclarationStat}
	 * labeled alternative in {@link FavaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitVarDeclarationStat(FavaParser.VarDeclarationStatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AssignStat}
	 * labeled alternative in {@link FavaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterAssignStat(FavaParser.AssignStatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AssignStat}
	 * labeled alternative in {@link FavaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitAssignStat(FavaParser.AssignStatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AndExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAndExpr(FavaParser.AndExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AndExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAndExpr(FavaParser.AndExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StringExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterStringExpr(FavaParser.StringExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StringExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitStringExpr(FavaParser.StringExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code BoolExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterBoolExpr(FavaParser.BoolExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code BoolExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitBoolExpr(FavaParser.BoolExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IdExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterIdExpr(FavaParser.IdExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IdExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitIdExpr(FavaParser.IdExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code RelationalExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterRelationalExpr(FavaParser.RelationalExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code RelationalExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitRelationalExpr(FavaParser.RelationalExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code UnaryExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpr(FavaParser.UnaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code UnaryExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpr(FavaParser.UnaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code OrExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterOrExpr(FavaParser.OrExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code OrExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitOrExpr(FavaParser.OrExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ConcatExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterConcatExpr(FavaParser.ConcatExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ConcatExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitConcatExpr(FavaParser.ConcatExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EqualityExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterEqualityExpr(FavaParser.EqualityExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EqualityExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitEqualityExpr(FavaParser.EqualityExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MulDivModExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMulDivModExpr(FavaParser.MulDivModExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MulDivModExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMulDivModExpr(FavaParser.MulDivModExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ParensExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterParensExpr(FavaParser.ParensExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ParensExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitParensExpr(FavaParser.ParensExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IntExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterIntExpr(FavaParser.IntExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IntExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitIntExpr(FavaParser.IntExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code RealExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterRealExpr(FavaParser.RealExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code RealExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitRealExpr(FavaParser.RealExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AddSubExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAddSubExpr(FavaParser.AddSubExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AddSubExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAddSubExpr(FavaParser.AddSubExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link FavaParser#type}.
	 * @param ctx the parse tree
	 */
	void enterType(FavaParser.TypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link FavaParser#type}.
	 * @param ctx the parse tree
	 */
	void exitType(FavaParser.TypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link FavaParser#varDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterVarDeclaration(FavaParser.VarDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link FavaParser#varDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitVarDeclaration(FavaParser.VarDeclarationContext ctx);
}