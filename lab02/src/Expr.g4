grammar Expr;

prog   : expr EOF;

expr   : <assoc=right> expr (POW) expr
       | expr (TIMES|DIV) expr
       | expr (PLUS|MINUS) expr
       | NUMBER
       | ID
       | LPAREN expr RPAREN
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

