grammar Fava;

options {
    caseInsensitive = true;
    language = Java;
}

prog   : stat + EOF;

stat   : 'print' expr ';'                               # PrintStat
       | varDeclaration ';'                             # VarDeclarationStat
       | ID '=' expr ';'                                # AssignStat // variaveis já declaradas
       ;

expr   : '(' expr ')'                                   # ParensExpr
       | op=('-'|'not') expr                            # UnaryExpr
       | expr op=('*'|'/'|'mod') expr                   # MulDivModExpr
       | expr op=('+'|'-') expr                         # AddSubExpr
       | expr op='||' expr                              # ConcatExpr
       | expr op=('<' | '>' |'<=' | '>=') expr          # RelationalExpr
       | expr op=('=' | '<>') expr                      # EqualityExpr
       | expr op='and' expr                             # AndExpr
       | expr op='or' expr                              # OrExpr
       | INT                                            # IntExpr
       | REAL                                           # RealExpr
       | STRING                                         # StringExpr
       | BOOLEAN                                        # BoolExpr
       | ID                                             # IdExpr
       ;

type  : ('int' | 'real' | 'string' | 'bool')
      | type '[' ']' // Arrays no futuro
      | ID // Suportar instancias no futuro de classes
      ;

varDeclaration : type ID ('=' expr)?
        ;


INT      : DIGIT+ ;
REAL     : DIGIT+ '.' DIGIT* | '.' DIGIT+ ;
STRING   : '"' ( ESC | . )*? '"' ; // a forma basica '"' .*? '"'
BOOLEAN :  'true' | 'false'  ;
ID : ID_LETTER (ID_LETTER | DIGIT)* ;



WS       : [ \t\r\n]+ -> skip ;
SL_COMMENT : '//' .*? (EOF|'\n') -> skip; // single-line comment
ML_COMMENT : '/*' .*? '*/' -> skip ; // multi-line comment

fragment ESC : '\\"' | '\\\\' ;
fragment ID_LETTER : [a-z_] ; // como temos caseInsensitive já não precisamos de [a-zA-Z_]
fragment DIGIT    : [0-9] ;
