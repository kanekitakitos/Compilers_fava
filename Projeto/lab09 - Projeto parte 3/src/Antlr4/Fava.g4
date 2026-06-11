grammar Fava;

// Configurações globais da Gramática Fava
options {
    caseInsensitive = true; // A linguagem Fava ignora diferenças entre maiúsculas e minúsculas (ex: INT == int)
    language = Java;
}

/**
 * A regra principal (root) do ficheiro que define o fluxo inicial de todos os programas.
 * O script é lido como um conjunto infinito de Statements.
 */
prog   : stat + EOF;

/**
 * Define todas as linhas de comando executáveis num código Fava e as suas devidas ramificações/keywords.
 */
stat           :'return' expr? ';'                              # ReturnStat
               |'print' expr ';'                                # PrintStat
               | functionDecl                                   # FunctionDeclStat
               | functionCall ';'                               # FunctionCallStat
               | varDeclaration ';'                             # VarDeclarationStat
               | ID ':=' expr';'                                # AssignStat // Atribuição a variáveis já declaradas previamente
               | block                                          # BlockStat  // Iniciar novo Scope de Memória {}
               | ';'                                            # EmptyStat  // Ignora terminação em branco
               | 'while' '(' expr ')' stat                      # WhileStat  // Loop infinito enquanto expr (condição) for True
               | 'if' '(' expr ')' stat ('else' stat)?          # IfStat     // Avalia bloco (Sem dangling-else problemativo)
               ;

/**
 * Blocos delimitam os contextos (scopes) nos quais vivem as variáveis locais da RAM LIFO.
 */
block          : '{' stat* '}'
               ;

exprList : expr (',' expr)* ;

functionCall : ID '(' exprList? ')' ;


functionDecl : 'function' ID '(' formalParameters? ')' ('->' type)?  block
             ;

formalParameters : formalParameter (',' formalParameter)* ;

formalParameter : type ID ;

/**
 * Palavras-chave estritas correspondentes às regras fixas dos Singletons FavaType no Compilador.
 */
type           : ('integer' | 'real' | 'string' | 'bool')
               | ID // Permitirá no futuro tipos complexos e instâncias de classes customizadas
               ;

/**
 * Representa os nós da AST focados em inicializações (Declaração).
 * Apanha "int x := 5" ou encadeamentos como "int x, y := 2, z;".
 */
varDeclaration : type varInit (',' varInit)*
               ;

varInit        : ID (':=' expr)?
               ;

/**
 * Especifica a hierarquia de precedência de operações a partir do topo até literais isolados base.
 */
expr           : '(' expr ')'                                   # ParensExpr       // Prioridade Máxima: Os parentesis contêm Expressão Isolada
               | op=('-'|'not') expr                            # UnaryExpr        // Operadores de negação imediata aplicados a uma variável (Unário)
               | expr op=('*'|'/'|'mod') expr                   # MulDivModExpr    // Precedência sobre adição.
               | expr op=('+'|'-') expr                         # AddSubExpr
               | expr op='||' expr                              # ConcatExpr       // Concatenação Fava baseada em `||`
               | expr op=('<' | '>' |'<=' | '>=') expr          # RelationalExpr   // Produzem booleanos
               | expr op=('=' | '<>') expr                      # EqualityExpr     // Compara igual e diferente
               | expr op='and' expr                             # AndExpr
               | expr op='or' expr                              # OrExpr
               | INTEGER                                        # IntegerExpr      // Atingiu um literal final inteiro
               | REAL                                           # RealExpr         // Double/FloatingPoint Final
               | STRING                                         # StringExpr       // Constante Textual
               | BOOLEAN                                        # BoolExpr         // True ou False
               | ID                                             # IdExpr           // Variável LIFO
               | functionCall                                   # FunctionCallExpr
               ;

/*------------------------------------------------------------------
 * REGRAS LÉXICAS - Mapeamento e Agrupamento dos Tokens Finais Fava
 *------------------------------------------------------------------*/

INTEGER    : DIGIT+ ;
REAL       : DIGIT+ '.' DIGIT* | '.' DIGIT+ ;
STRING     : '"' ( ESC | . )*? '"' ;       // Apanha qualquer sequência protegida ou não até encontrar novo "
BOOLEAN    : 'true' | 'false'  ;
ID         : ID_LETTER (ID_LETTER | DIGIT)* ;

/*------------------------------------------------------------------
 * ESPAÇOS EM BRANCO E COMENTÁRIOS DA LINGUAGEM
 *------------------------------------------------------------------*/

WS         : [ \t\r\n]+ -> skip ;                       // Tabulações e NewLines da IDE são irrelevantes
SL_COMMENT : '//' .*? (EOF|'\n') -> skip;               // Comentários Singulares (`//`)
ML_COMMENT : '/*' .*? '*/' -> skip ;                    // Comentários Múltiplos Block

/*------------------------------------------------------------------
 * FRAGMENTOS DE CÓDIGO - Atalhos reaproveitáveis nos RegEx
 *------------------------------------------------------------------*/

fragment ESC : '\\"' | '\\\\' ;
fragment ID_LETTER : [a-z_] ; // (caseInsensitive torna a-zA-Z redundante)
fragment DIGIT    : [0-9] ;
