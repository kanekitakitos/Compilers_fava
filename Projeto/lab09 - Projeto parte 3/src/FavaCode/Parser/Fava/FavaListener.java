// Generated from C:/Users/brand/OneDrive/Documentos/GitHub/Compiladores/Projeto/lab09 - Projeto parte 3/src/Antlr4/Fava.g4 by ANTLR 4.13.2
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
	 * Enter a parse tree produced by the {@code ReturnStat}
	 * labeled alternative in {@link FavaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterReturnStat(FavaParser.ReturnStatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ReturnStat}
	 * labeled alternative in {@link FavaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitReturnStat(FavaParser.ReturnStatContext ctx);
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
	 * Enter a parse tree produced by the {@code FunctionDeclStat}
	 * labeled alternative in {@link FavaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDeclStat(FavaParser.FunctionDeclStatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FunctionDeclStat}
	 * labeled alternative in {@link FavaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDeclStat(FavaParser.FunctionDeclStatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FunctionCallStat}
	 * labeled alternative in {@link FavaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCallStat(FavaParser.FunctionCallStatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FunctionCallStat}
	 * labeled alternative in {@link FavaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCallStat(FavaParser.FunctionCallStatContext ctx);
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
	 * Enter a parse tree produced by the {@code BlockStat}
	 * labeled alternative in {@link FavaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterBlockStat(FavaParser.BlockStatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code BlockStat}
	 * labeled alternative in {@link FavaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitBlockStat(FavaParser.BlockStatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EmptyStat}
	 * labeled alternative in {@link FavaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterEmptyStat(FavaParser.EmptyStatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EmptyStat}
	 * labeled alternative in {@link FavaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitEmptyStat(FavaParser.EmptyStatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code WhileStat}
	 * labeled alternative in {@link FavaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterWhileStat(FavaParser.WhileStatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code WhileStat}
	 * labeled alternative in {@link FavaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitWhileStat(FavaParser.WhileStatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IfStat}
	 * labeled alternative in {@link FavaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterIfStat(FavaParser.IfStatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IfStat}
	 * labeled alternative in {@link FavaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitIfStat(FavaParser.IfStatContext ctx);
	/**
	 * Enter a parse tree produced by {@link FavaParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(FavaParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link FavaParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(FavaParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link FavaParser#exprList}.
	 * @param ctx the parse tree
	 */
	void enterExprList(FavaParser.ExprListContext ctx);
	/**
	 * Exit a parse tree produced by {@link FavaParser#exprList}.
	 * @param ctx the parse tree
	 */
	void exitExprList(FavaParser.ExprListContext ctx);
	/**
	 * Enter a parse tree produced by {@link FavaParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(FavaParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link FavaParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(FavaParser.FunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link FavaParser#functionDecl}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDecl(FavaParser.FunctionDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link FavaParser#functionDecl}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDecl(FavaParser.FunctionDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link FavaParser#formalParameters}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameters(FavaParser.FormalParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link FavaParser#formalParameters}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameters(FavaParser.FormalParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link FavaParser#formalParameter}.
	 * @param ctx the parse tree
	 */
	void enterFormalParameter(FavaParser.FormalParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link FavaParser#formalParameter}.
	 * @param ctx the parse tree
	 */
	void exitFormalParameter(FavaParser.FormalParameterContext ctx);
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
	/**
	 * Enter a parse tree produced by {@link FavaParser#varInit}.
	 * @param ctx the parse tree
	 */
	void enterVarInit(FavaParser.VarInitContext ctx);
	/**
	 * Exit a parse tree produced by {@link FavaParser#varInit}.
	 * @param ctx the parse tree
	 */
	void exitVarInit(FavaParser.VarInitContext ctx);
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
	 * Enter a parse tree produced by the {@code IntegerExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterIntegerExpr(FavaParser.IntegerExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IntegerExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitIntegerExpr(FavaParser.IntegerExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FunctionCallExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCallExpr(FavaParser.FunctionCallExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FunctionCallExpr}
	 * labeled alternative in {@link FavaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCallExpr(FavaParser.FunctionCallExprContext ctx);
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
}