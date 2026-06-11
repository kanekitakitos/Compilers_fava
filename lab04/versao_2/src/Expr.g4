grammar Expr;

prog   : expr EOF;

expr   : <assoc=right> expr (POW) expr     # Pow
       | expr op=(TIMES|DIV) expr          # TimesDiv
       | expr op=(PLUS|MINUS) expr         # PlusMinus
       | NUMBER                            # Number
       | ID                                # Id
       | LPAREN expr RPAREN                # Parentesis
       ;

PLUS     : '+' ;
POW     : '^' ;
MINUS    : '-' ;
TIMES    : '*' ;
DIV      : '/' ;
LPAREN   : '(' ;
RPAREN   : ')' ;
ID       : ('_' | LETTER) (LETTER | DIGIT | '_')* ;
NUMBER   : DIGIT+ | DIGIT* ('.' DIGIT | DIGIT '.') DIGIT* ;
WS       : [ \r\t\n]+ -> skip ;

fragment
DIGIT    : [0-9] ; 

fragment
LETTER   : [a-zA-Z] ;

